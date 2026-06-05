package com.duoschedule.data.importexport

import android.util.Log
import com.duoschedule.data.model.PersonType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

    init {
        SupportedSchools.register(ZhengfangSchoolAdapter())
        SupportedSchools.register(QiangzhiSchoolAdapter())
    }

    /**
     * 旧版 API：通过凭据登录正方教务系统并获取课表。
     * 内部创建 ZhengfangSchoolAdapter 实例，保持向后兼容。
     */
    suspend fun loginAndGetCourses(
        credentials: EducationSystemCredentials,
        targetPerson: PersonType
    ): EducationSystemResult = withContext(Dispatchers.IO) {
        val adapter = ZhengfangSchoolAdapter(baseUrl = credentials.baseUrl)
        loginAndGetCourses(adapter, credentials.studentId, credentials.password, credentials.captcha, targetPerson)
    }

    /**
     * 新版 API：通过 SchoolAdapter 登录并获取课表。
     */
    suspend fun loginAndGetCourses(
        adapter: SchoolAdapter,
        username: String,
        password: String,
        captcha: String = "",
        targetPerson: PersonType = PersonType.PERSON_A
    ): EducationSystemResult = withContext(Dispatchers.IO) {
        try {
            val loginResult = adapter.login(username, password, captcha)

            if (!loginResult.success) {
                return@withContext EducationSystemResult(
                    success = false,
                    needCaptcha = loginResult.needCaptcha,
                    captchaImageUrl = loginResult.captchaImageUrl,
                    sessionId = loginResult.cookies.values.firstOrNull(),
                    errors = loginResult.errors
                )
            }

            val scheduleHtml = adapter.fetchScheduleHtml(loginResult.cookies, username)

            if (scheduleHtml.isNullOrBlank()) {
                return@withContext EducationSystemResult(
                    success = false,
                    errors = listOf("未能获取课表页面")
                )
            }

            val courses = adapter.parseScheduleHtml(scheduleHtml, targetPerson)

            if (courses.isEmpty()) {
                EducationSystemResult(
                    success = false,
                    errors = listOf("未能获取课表数据，请检查学号是否正确")
                )
            } else {
                EducationSystemResult(
                    success = true,
                    sessionId = loginResult.cookies.values.firstOrNull(),
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
}
