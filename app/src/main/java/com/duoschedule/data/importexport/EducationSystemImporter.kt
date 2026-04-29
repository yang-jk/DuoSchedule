package com.duoschedule.data.importexport

import android.util.Log
import com.duoschedule.data.model.PersonType
import com.duoschedule.data.model.WeekType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.util.concurrent.TimeUnit

data class EducationSystemCredentials(
    val baseUrl: String,
    val studentId: String,
    val password: String,
    val captcha: String = ""
)

data class EducationSystemResult(
    val success: Boolean,
    val needCaptcha: Boolean = false,
    val captchaImageUrl: String? = null,
    val sessionId: String? = null,
    val courses: List<CourseImportData> = emptyList(),
    val errors: List<String> = emptyList()
)

object EducationSystemImporter {
    private const val TAG = "EducationSystemImporter"
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    suspend fun loginAndGetCourses(
        credentials: EducationSystemCredentials,
        targetPerson: PersonType
    ): EducationSystemResult = withContext(Dispatchers.IO) {
        try {
            val baseUrl = credentials.baseUrl.trimEnd('/')
            
            val loginPageResponse = client.newCall(
                Request.Builder()
                    .url("$baseUrl/default2.aspx")
                    .build()
            ).execute()
            
            var sessionId = loginPageResponse.headers("Set-Cookie")
                .firstOrNull { it.contains("ASP.NET_SessionId") }
                ?.let { 
                    it.substringAfter("ASP.NET_SessionId=").substringBefore(";") 
                }
            
            val loginPageHtml = loginPageResponse.body?.string() ?: ""
            val loginDoc = Jsoup.parse(loginPageHtml)
            
            val viewState = loginDoc.select("input[name=__VIEWSTATE]").attr("value")
            val eventValidation = loginDoc.select("input[name=__EVENTVALIDATION]").attr("value")
            
            val loginBody = FormBody.Builder()
                .add("__VIEWSTATE", viewState)
                .add("__EVENTVALIDATION", eventValidation ?: "")
                .add("txtUserName", credentials.studentId)
                .add("TextBox2", credentials.password)
                .add("txtSecretCode", credentials.captcha)
                .add("RadioButtonList1", "%D1%A7%C9%FA")
                .add("Button1", "")
                .add("lbLanguage", "")
                .add("hidPdrs", "")
                .add("hidsc", "")
                .build()
            
            val loginResponse = client.newCall(
                Request.Builder()
                    .url("$baseUrl/default2.aspx")
                    .post(loginBody)
                    .header("Cookie", "ASP.NET_SessionId=$sessionId")
                    .build()
            ).execute()
            
            val loginResultHtml = loginResponse.body?.string() ?: ""
            
            if (loginResultHtml.contains("验证码不正确") || loginResultHtml.contains("验证码错误")) {
                return@withContext EducationSystemResult(
                    success = false,
                    needCaptcha = true,
                    errors = listOf("验证码错误，请重新输入")
                )
            }
            
            if (loginResultHtml.contains("密码错误") || loginResultHtml.contains("用户名不存在")) {
                return@withContext EducationSystemResult(
                    success = false,
                    errors = listOf("学号或密码错误")
                )
            }
            
            if (loginResultHtml.contains("验证码") && credentials.captcha.isEmpty()) {
                val captchaUrl = "$baseUrl/CheckCode.aspx"
                return@withContext EducationSystemResult(
                    success = false,
                    needCaptcha = true,
                    captchaImageUrl = captchaUrl,
                    sessionId = sessionId,
                    errors = listOf("需要输入验证码")
                )
            }
            
            val scheduleResponse = client.newCall(
                Request.Builder()
                    .url("$baseUrl/xskbcx.aspx?xh=${credentials.studentId}")
                    .header("Cookie", "ASP.NET_SessionId=$sessionId")
                    .build()
            ).execute()
            
            val scheduleHtml = scheduleResponse.body?.string() ?: ""
            val courses = parseScheduleHtml(scheduleHtml, targetPerson)
            
            if (courses.isEmpty()) {
                EducationSystemResult(
                    success = false,
                    errors = listOf("未能获取课表数据，请检查学号是否正确")
                )
            } else {
                EducationSystemResult(
                    success = true,
                    courses = courses
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "登录教务系统失败", e)
            EducationSystemResult(
                success = false,
                errors = listOf("连接教务系统失败: ${e.message}")
            )
        }
    }

    private fun parseScheduleHtml(html: String, targetPerson: PersonType): List<CourseImportData> {
        val courses = mutableListOf<CourseImportData>()
        val doc = Jsoup.parse(html)
        
        val table = doc.select("table#Table1").first() 
            ?: doc.select("table.blacktab").first()
            ?: doc.select("table").firstOrNull { it.select("tr").size > 5 }
        
        if (table == null) {
            return courses
        }
        
        val rows = table.select("tr")
        for (rowIndex in 1 until rows.size) {
            val row = rows[rowIndex]
            val cells = row.select("td")
            
            for (cellIndex in 1 until cells.size) {
                val cell = cells[cellIndex]
                val text = cell.text().trim()
                
                if (text.isNotEmpty() && text != "&nbsp;" && !text.contains("第") || text.contains("{")) {
                    val parsedCourses = parseCourseCell(text, cellIndex, rowIndex, targetPerson)
                    courses.addAll(parsedCourses)
                }
            }
        }
        
        return courses
    }

    private fun parseCourseCell(
        text: String,
        dayIndex: Int,
        rowIndex: Int,
        targetPerson: PersonType
    ): List<CourseImportData> {
        val courses = mutableListOf<CourseImportData>()
        
        val courseBlocks = text.split(Regex("(?=\\{[^}]+\\})|(?<=\\})"))
        
        for (block in courseBlocks) {
            if (block.isBlank()) continue
            
            try {
                val nameMatch = Regex("^(.+?)\\s*\\{").find(block)
                val name = nameMatch?.groupValues?.get(1)?.trim() ?: continue
                
                val infoMatch = Regex("\\{(.+?)\\}").find(block)
                val info = infoMatch?.groupValues?.get(1) ?: continue
                
                val parts = info.split(",")
                var location = ""
                var teacher = ""
                var weeks = "1-16"
                var weekType = WeekType.ALL
                
                for (part in parts) {
                    when {
                        part.contains("周") && part.contains("-") -> {
                            weeks = part.replace("周", "").trim()
                            if (part.contains("单")) weekType = WeekType.ODD
                            if (part.contains("双")) weekType = WeekType.EVEN
                        }
                        part.contains("楼") || part.contains("室") || part.contains("区") -> {
                            location = part.trim()
                        }
                        else -> {
                            teacher = part.trim()
                        }
                    }
                }
                
                val weekRange = weeks.split("-")
                val startWeek = weekRange.getOrNull(0)?.toIntOrNull() ?: 1
                val endWeek = weekRange.getOrNull(1)?.toIntOrNull() ?: 16
                
                val period = (rowIndex + 1) / 2
                val startTime = getPeriodStartTime(period)
                val endTime = getPeriodEndTime(period)
                
                courses.add(
                    CourseImportData(
                        name = name,
                        location = location,
                        dayOfWeek = dayIndex,
                        startHour = startTime.first,
                        startMinute = startTime.second,
                        endHour = endTime.first,
                        endMinute = endTime.second,
                        weekType = weekType,
                        startWeek = startWeek,
                        endWeek = endWeek,
                        personType = targetPerson
                    )
                )
            } catch (e: Exception) {
                Log.w(TAG, "解析课程单元格失败: $block", e)
            }
        }
        
        return courses
    }

    private fun getPeriodStartTime(period: Int): Pair<Int, Int> {
        return when (period) {
            1 -> Pair(8, 0)
            2 -> Pair(8, 55)
            3 -> Pair(10, 0)
            4 -> Pair(10, 55)
            5 -> Pair(14, 0)
            6 -> Pair(14, 55)
            7 -> Pair(16, 0)
            8 -> Pair(16, 55)
            9 -> Pair(19, 0)
            10 -> Pair(19, 55)
            11 -> Pair(20, 50)
            12 -> Pair(21, 45)
            else -> Pair(8, 0)
        }
    }

    private fun getPeriodEndTime(period: Int): Pair<Int, Int> {
        return when (period) {
            1 -> Pair(8, 45)
            2 -> Pair(9, 40)
            3 -> Pair(10, 45)
            4 -> Pair(11, 40)
            5 -> Pair(14, 45)
            6 -> Pair(15, 40)
            7 -> Pair(16, 45)
            8 -> Pair(17, 40)
            9 -> Pair(19, 45)
            10 -> Pair(20, 40)
            11 -> Pair(21, 35)
            12 -> Pair(22, 30)
            else -> Pair(9, 40)
        }
    }
}
