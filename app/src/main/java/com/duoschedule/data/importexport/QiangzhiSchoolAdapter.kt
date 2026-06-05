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
import org.jsoup.nodes.Element
import java.net.SocketException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

class QiangzhiSchoolAdapter : SchoolAdapter {

    companion object {
        private const val TAG = "QiangzhiSchoolAdapter"
    }

    override val schoolInfo = SchoolInfo(
        id = "lnpu_qiangzhi",
        name = "辽宁石油化工大学",
        baseUrl = "https://jwxt.lnpu.edu.cn/jsxsd",
        systemType = "qiangzhi",
        vpnHint = "请先连接学校 VPN",
        loginUrl = "https://jwxt.lnpu.edu.cn/jsxsd/",
        scheduleUrlPath = "/xskb/xskb_list.do"
    )

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    override suspend fun login(
        username: String,
        password: String,
        captcha: String,
        cookies: Map<String, String>
    ): SchoolLoginResult = withContext(Dispatchers.IO) {
        try {
            val baseUrl = schoolInfo.baseUrl

            // Step 1: GET login page to obtain JSESSIONID
            val loginPageRequest = Request.Builder()
                .url("$baseUrl/")
                .build()

            val loginPageResponse = client.newCall(loginPageRequest).execute()
            val jsessionId = loginPageResponse.headers("Set-Cookie")
                .firstOrNull { it.contains("JSESSIONID") }
                ?.let { it.substringAfter("JSESSIONID=").substringBefore(";") }

            val loginPageHtml = loginPageResponse.body?.string() ?: ""
            val loginDoc = Jsoup.parse(loginPageHtml)

            // Find the form action URL
            val form = loginDoc.select("form").firstOrNull()
            val formAction = form?.attr("action")?.trim()
            val loginPostUrl = if (!formAction.isNullOrEmpty()) {
                if (formAction.startsWith("http")) formAction
                else "$baseUrl/${formAction.removePrefix("/")}"
            } else {
                "$baseUrl/xk/Logon.do?method=logon"
            }

            // Collect hidden form fields
            val formFields = mutableMapOf<String, String>()
            form?.select("input[type=hidden]")?.forEach { input ->
                val name = input.attr("name")
                val value = input.attr("value")
                if (name.isNotEmpty()) {
                    formFields[name] = value
                }
            }

            // Step 2: POST login
            val formBuilder = FormBody.Builder()
            formFields.forEach { (key, value) -> formBuilder.add(key, value) }
            formBuilder.add("USERNAME", username)
            formBuilder.add("PASSWORD", password)
            if (captcha.isNotEmpty()) {
                formBuilder.add("RANDOMCODE", captcha)
            }

            val cookieHeader = buildString {
                append("JSESSIONID=$jsessionId")
                cookies.forEach { (k, v) -> append("; $k=$v") }
            }

            val loginRequest = Request.Builder()
                .url(loginPostUrl)
                .post(formBuilder.build())
                .header("Cookie", cookieHeader)
                .build()

            val loginResponse = client.newCall(loginRequest).execute()

            // Update JSESSIONID from response if present
            val updatedJsessionId = loginResponse.headers("Set-Cookie")
                .firstOrNull { it.contains("JSESSIONID") }
                ?.let { it.substringAfter("JSESSIONID=").substringBefore(";") }
                ?: jsessionId

            val loginResultHtml = loginResponse.body?.string() ?: ""

            // Check for captcha requirement
            if (loginResultHtml.contains("验证码") && captcha.isEmpty()) {
                val captchaUrl = "$baseUrl/verifycode.servlet"
                return@withContext SchoolLoginResult(
                    success = false,
                    cookies = if (updatedJsessionId != null) mapOf("JSESSIONID" to updatedJsessionId) else emptyMap(),
                    needCaptcha = true,
                    captchaImageUrl = captchaUrl,
                    errors = listOf("需要输入验证码")
                )
            }

            // Check for login failure indicators
            if (loginResultHtml.contains("用户名或密码不正确")
                || loginResultHtml.contains("密码错误")
                || loginResultHtml.contains("用户名不存在")
                || loginResultHtml.contains("学号或密码错误")
            ) {
                return@withContext SchoolLoginResult(
                    success = false,
                    errors = listOf("学号或密码错误")
                )
            }

            if (loginResultHtml.contains("验证码不正确") || loginResultHtml.contains("验证码错误")) {
                return@withContext SchoolLoginResult(
                    success = false,
                    needCaptcha = true,
                    errors = listOf("验证码错误，请重新输入")
                )
            }

            // Check for success — if we got the main page or a redirect to the portal
            val success = updatedJsessionId != null && (
                loginResultHtml.contains("欢迎")
                || loginResultHtml.contains("主控面板")
                || loginResultHtml.contains("框架")
                || loginResultHtml.contains("menu")
                || !loginResultHtml.contains("Logon")
                )

            if (!success && updatedJsessionId == null) {
                return@withContext SchoolLoginResult(
                    success = false,
                    errors = listOf("登录失败，未获取到会话信息")
                )
            }

            val resultCookies = mutableMapOf<String, String>()
            resultCookies["JSESSIONID"] = updatedJsessionId

            SchoolLoginResult(
                success = true,
                cookies = resultCookies
            )
        } catch (e: UnknownHostException) {
            Log.e(TAG, "无法连接教务系统", e)
            SchoolLoginResult(
                success = false,
                errors = listOf("无法连接教务系统，${schoolInfo.vpnHint}")
            )
        } catch (e: SocketException) {
            Log.e(TAG, "网络连接异常", e)
            SchoolLoginResult(
                success = false,
                errors = listOf("网络连接异常，${schoolInfo.vpnHint}")
            )
        } catch (e: Exception) {
            Log.e(TAG, "登录教务系统失败", e)
            SchoolLoginResult(
                success = false,
                errors = listOf("登录失败: ${e.message}")
            )
        }
    }

    override suspend fun fetchScheduleHtml(
        cookies: Map<String, String>,
        studentId: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            val cookieHeader = cookies.entries.joinToString("; ") { "${it.key}=${it.value}" }

            val request = Request.Builder()
                .url("${schoolInfo.baseUrl}/xskb/xskb_list.do")
                .header("Cookie", cookieHeader)
                .build()

            val response = client.newCall(request).execute()
            val html = response.body?.string()

            if (html.isNullOrEmpty() || !html.contains("kbtable")) {
                Log.w(TAG, "获取课表HTML失败：响应为空或不包含课表数据")
                return@withContext null
            }

            html
        } catch (e: Exception) {
            Log.e(TAG, "获取课表HTML失败", e)
            null
        }
    }

    override fun parseScheduleHtml(
        html: String,
        targetPerson: PersonType
    ): List<CourseImportData> {
        val courses = mutableListOf<CourseImportData>()
        val doc = Jsoup.parse(html)

        val table = doc.select("table#kbtable").firstOrNull()
        if (table == null) {
            Log.w(TAG, "未找到课表表格 #kbtable")
            return courses
        }

        val rows = table.select("tr")
        for (rowIndex in 1 until rows.size) {
            val row = rows[rowIndex]
            val ths = row.select("th")
            val tds = row.select("td")

            // First <th> contains the 大节 info (e.g., "第一大节<br>08:20-10:00")
            val periodInfo = if (ths.isNotEmpty()) ths.first()!!.text() else ""

            // Each <td> = one day of the week (Mon=1, Tue=2, ..., Sun=7)
            for (dayIndex in tds.indices) {
                val td = tds[dayIndex]
                val dayOfWeek = dayIndex + 1

                // Find the visible kbcontent div (not kbcontent1)
                val contentDivs = td.select("div.kbcontent")
                for (div in contentDivs) {
                    val cellCourses = parseKbContentDiv(div, dayOfWeek, periodInfo, targetPerson)
                    courses.addAll(cellCourses)
                }
            }
        }

        return courses
    }

    /**
     * Parse a single <div class="kbcontent"> which may contain multiple courses
     * separated by "---------------------" or "----------------------"
     */
    private fun parseKbContentDiv(
        div: Element,
        dayOfWeek: Int,
        periodInfo: String,
        targetPerson: PersonType
    ): List<CourseImportData> {
        val courses = mutableListOf<CourseImportData>()

        // Split by the separator line (dashes)
        val html = div.html()
        val blocks = html.split(Regex("-{15,30}"))

        for (blockHtml in blocks) {
            if (blockHtml.isBlank()) continue

            // Wrap each block in a div so Jsoup can parse it
            val blockElement = Jsoup.parseBodyFragment(blockHtml).body()
            val text = blockElement.text().trim()

            // Skip empty cells
            if (text.isEmpty() || text == "\u00a0" || text == "&nbsp;") continue

            try {
                val course = parseCourseBlock(blockElement, dayOfWeek, periodInfo, targetPerson)
                if (course != null) {
                    courses.add(course)
                }
            } catch (e: Exception) {
                Log.w(TAG, "解析课程块失败: $text", e)
            }
        }

        return courses
    }

    /**
     * Parse a single course block within a kbcontent div.
     *
     * Structure:
     *   Course name (first text node)
     *   <font title="老师">teacher</font>
     *   <font title="周次(节次)">1-13,15(周)[01-02节]</font>
     *   <font title="教室">location</font> (optional)
     */
    private fun parseCourseBlock(
        blockElement: Element,
        dayOfWeek: Int,
        periodInfo: String,
        targetPerson: PersonType
    ): CourseImportData? {
        // Extract course name: first text node before any <font> tag
        val rawName = StringBuilder()
        for (node in blockElement.childNodes()) {
            if (node is org.jsoup.nodes.TextNode) {
                val t = node.text().trim()
                if (t.isNotEmpty()) {
                    rawName.append(t)
                }
            } else if (node is org.jsoup.nodes.Element) {
                // Stop at the first <font> element — that's where metadata begins
                break
            }
        }

        var courseName = rawName.toString().trim()
            .replace("\u00a0", " ")
            .trim()

        // Strip P marker: <span><font color="red">&nbsp;P</font></span>
        courseName = courseName.replace(Regex("\\s*P\\s*$"), "").trim()

        if (courseName.isEmpty()) return null

        // Extract metadata from <font> tags
        var teacher = ""
        var weekPeriodStr = ""
        var location = ""

        for (font in blockElement.select("font")) {
            val title = font.attr("title")
            val value = font.text().trim()
            when {
                title == "老师" -> teacher = value
                title.contains("周次") -> weekPeriodStr = value
                title == "教室" -> location = value
            }
        }

        if (weekPeriodStr.isEmpty()) {
            Log.w(TAG, "课程 '$courseName' 缺少周次信息，跳过")
            return null
        }

        // Parse week/period info
        val weekPeriodData = parseWeekPeriodString(weekPeriodStr)

        // Determine time from period info or fallback to 大节
        val (startHour, startMinute, endHour, endMinute) = if (weekPeriodData.startPeriod > 0) {
            getPeriodTime(weekPeriodData.startPeriod, weekPeriodData.endPeriod)
        } else {
            // Fallback: parse time from the 大节 info
            parseTimeFromPeriodInfo(periodInfo)
        }

        return CourseImportData(
            name = courseName,
            location = location,
            teacher = teacher,
            dayOfWeek = dayOfWeek,
            startHour = startHour,
            startMinute = startMinute,
            endHour = endHour,
            endMinute = endMinute,
            weekType = weekPeriodData.weekType,
            startWeek = weekPeriodData.startWeek,
            endWeek = weekPeriodData.endWeek,
            customWeeks = weekPeriodData.customWeeks,
            personType = targetPerson,
            startPeriod = weekPeriodData.startPeriod,
            endPeriod = weekPeriodData.endPeriod
        )
    }

    /**
     * Parse week/period string like "1-13,15(周)[01-02节]" or "1-15(周)[09-10-11-12节]"
     */
    private fun parseWeekPeriodString(input: String): WeekPeriodData {
        val default = WeekPeriodData(
            startWeek = 1, endWeek = 16,
            weekType = WeekType.ALL,
            startPeriod = 0, endPeriod = 0
        )

        // Extract period info from [XX-XX节] or [XX-XX-XX节]
        val periodRegex = Regex("\\[([\\d\\-]+)节\\]")
        val periodMatch = periodRegex.find(input)
        val (startPeriod, endPeriod) = if (periodMatch != null) {
            val periodStr = periodMatch.groupValues[1]
            parsePeriodRange(periodStr)
        } else {
            Pair(0, 0)
        }

        // Remove period part and "(周)" for week parsing
        val weekPart = input
            .replace(periodRegex, "")
            .replace("(周)", "")
            .trim()

        if (weekPart.isEmpty()) return default.copy(startPeriod = startPeriod, endPeriod = endPeriod)

        // Parse week ranges: "1-13,15" or "1-16" or "1,3,5,7,9" or "1-16单"
        val isOdd = weekPart.contains("单")
        val isEven = weekPart.contains("双")
        val cleanWeekPart = weekPart.replace("单", "").replace("双", "").trim()

        val weekComponents = cleanWeekPart.split(",")
        val allWeeks = mutableListOf<Int>()

        for (component in weekComponents) {
            val trimmed = component.trim()
            if (trimmed.isEmpty()) continue

            if (trimmed.contains("-")) {
                val parts = trimmed.split("-")
                val start = parts.getOrNull(0)?.toIntOrNull() ?: continue
                val end = parts.getOrNull(1)?.toIntOrNull() ?: continue
                for (w in start..end) {
                    allWeeks.add(w)
                }
            } else {
                trimmed.toIntOrNull()?.let { allWeeks.add(it) }
            }
        }

        if (allWeeks.isEmpty()) return default.copy(startPeriod = startPeriod, endPeriod = endPeriod)

        // Apply odd/even filter if specified
        if (isOdd) {
            allWeeks.retainAll { it % 2 == 1 }
        } else if (isEven) {
            allWeeks.retainAll { it % 2 == 0 }
        }

        val sortedWeeks = allWeeks.sorted()

        // Determine if we can use a simple range or need custom weeks
        val isContiguous = isContiguousRange(sortedWeeks) && !isOdd && !isEven

        return if (isContiguous) {
            WeekPeriodData(
                startWeek = sortedWeeks.first(),
                endWeek = sortedWeeks.last(),
                weekType = WeekType.ALL,
                startPeriod = startPeriod,
                endPeriod = endPeriod
            )
        } else {
            // Check if it's a simple odd/even range
            if (isOdd && isContiguousRange(sortedWeeks)) {
                WeekPeriodData(
                    startWeek = sortedWeeks.first(),
                    endWeek = sortedWeeks.last(),
                    weekType = WeekType.ODD,
                    startPeriod = startPeriod,
                    endPeriod = endPeriod
                )
            } else if (isEven && isContiguousRange(sortedWeeks)) {
                WeekPeriodData(
                    startWeek = sortedWeeks.first(),
                    endWeek = sortedWeeks.last(),
                    weekType = WeekType.EVEN,
                    startPeriod = startPeriod,
                    endPeriod = endPeriod
                )
            } else {
                // Discontinuous weeks — use CUSTOM
                WeekPeriodData(
                    startWeek = sortedWeeks.first(),
                    endWeek = sortedWeeks.last(),
                    weekType = WeekType.CUSTOM,
                    customWeeks = sortedWeeks.joinToString(","),
                    startPeriod = startPeriod,
                    endPeriod = endPeriod
                )
            }
        }
    }

    /**
     * Parse period range like "01-02" or "09-10-11-12"
     * Returns (startPeriod, endPeriod)
     */
    private fun parsePeriodRange(periodStr: String): Pair<Int, Int> {
        val parts = periodStr.split("-").mapNotNull { it.trim().toIntOrNull() }
        return if (parts.size >= 2) {
            Pair(parts.first(), parts.last())
        } else if (parts.size == 1) {
            Pair(parts[0], parts[0])
        } else {
            Pair(0, 0)
        }
    }

    /**
     * Check if a sorted list of integers forms a contiguous range
     */
    private fun isContiguousRange(weeks: List<Int>): Boolean {
        if (weeks.size <= 1) return true
        for (i in 1 until weeks.size) {
            if (weeks[i] != weeks[i - 1] + 1) return false
        }
        return true
    }

    /**
     * Get start/end time from period numbers.
     * Period 1-2: 08:20-10:00
     * Period 3-4: 10:20-12:00
     * Period 5-6: 13:20-15:00
     * Period 7-8: 15:20-17:00
     * Period 9-10: 18:00-19:40
     * Period 11-12: 19:50-21:30
     */
    private fun getPeriodTime(startPeriod: Int, endPeriod: Int): TimeRange {
        val start = periodStartTime(startPeriod)
        val end = periodEndTime(endPeriod)
        return TimeRange(start.first, start.second, end.first, end.second)
    }

    private fun periodStartTime(period: Int): Pair<Int, Int> = when (period) {
        1, 2 -> Pair(8, 20)
        3, 4 -> Pair(10, 20)
        5, 6 -> Pair(13, 20)
        7, 8 -> Pair(15, 20)
        9, 10 -> Pair(18, 0)
        11, 12 -> Pair(19, 50)
        else -> Pair(8, 20)
    }

    private fun periodEndTime(period: Int): Pair<Int, Int> = when (period) {
        1, 2 -> Pair(10, 0)
        3, 4 -> Pair(12, 0)
        5, 6 -> Pair(15, 0)
        7, 8 -> Pair(17, 0)
        9, 10 -> Pair(19, 40)
        11, 12 -> Pair(21, 30)
        else -> Pair(10, 0)
    }

    /**
     * Fallback: parse time from 大节 info string like "第一大节08:20-10:00"
     */
    private fun parseTimeFromPeriodInfo(periodInfo: String): TimeRange {
        val timeRegex = Regex("(\\d{1,2}):(\\d{2})\\s*-\\s*(\\d{1,2}):(\\d{2})")
        val match = timeRegex.find(periodInfo)
        return if (match != null) {
            TimeRange(
                match.groupValues[1].toInt(),
                match.groupValues[2].toInt(),
                match.groupValues[3].toInt(),
                match.groupValues[4].toInt()
            )
        } else {
            TimeRange(8, 20, 10, 0)
        }
    }

    private data class WeekPeriodData(
        val startWeek: Int,
        val endWeek: Int,
        val weekType: WeekType,
        val customWeeks: String = "",
        val startPeriod: Int = 0,
        val endPeriod: Int = 0
    )

    private data class TimeRange(
        val startHour: Int,
        val startMinute: Int,
        val endHour: Int,
        val endMinute: Int
    )
}
