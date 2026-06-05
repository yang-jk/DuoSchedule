package com.duoschedule.data.importexport

import com.duoschedule.data.model.Course
import com.duoschedule.data.model.PersonType
import com.duoschedule.data.model.WeekType

data class CourseImportData(
    val name: String,
    val location: String = "",
    val teacher: String = "",
    val dayOfWeek: Int,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val weekType: WeekType,
    val startWeek: Int = 1,
    val endWeek: Int = 16,
    val customWeeks: String = "",
    val personType: PersonType,
    val startPeriod: Int = 1,
    val endPeriod: Int = 1,
    val isCustomTime: Boolean = false
) {
    fun toCourse(): Course {
        return Course(
            name = name,
            location = location,
            teacher = teacher,
            dayOfWeek = dayOfWeek,
            startHour = startHour,
            startMinute = startMinute,
            endHour = endHour,
            endMinute = endMinute,
            weekType = weekType,
            startWeek = startWeek,
            endWeek = endWeek,
            customWeeks = customWeeks,
            personType = personType,
            startPeriod = startPeriod,
            endPeriod = endPeriod,
            isCustomTime = isCustomTime
        )
    }

    fun hasConflictWith(existing: Course): Boolean {
        if (dayOfWeek != existing.dayOfWeek) return false
        
        val startMinutes = startHour * 60 + startMinute
        val endMinutes = endHour * 60 + endMinute
        val existingStart = existing.startHour * 60 + existing.startMinute
        val existingEnd = existing.endHour * 60 + existing.endMinute
        
        if (startMinutes >= existingEnd || existingStart >= endMinutes) {
            return false
        }
        
        return hasWeekOverlap(existing)
    }

    private fun hasWeekOverlap(existing: Course): Boolean {
        val weeks1 = getActiveWeeks()
        val weeks2 = existing.getActiveWeeks()
        return weeks1.intersect(weeks2).isNotEmpty()
    }

    private fun getActiveWeeks(): Set<Int> {
        val weekRange = startWeek..endWeek
        return when (weekType) {
            WeekType.ALL -> weekRange.toSet()
            WeekType.ODD -> weekRange.filter { it % 2 == 1 }.toSet()
            WeekType.EVEN -> weekRange.filter { it % 2 == 0 }.toSet()
            WeekType.CUSTOM -> {
                if (customWeeks.isNotEmpty()) {
                    customWeeks.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
                } else {
                    emptySet()
                }
            }
        }
    }
}

data class ScheduleSettingsExport(
    val semesterStartDate: Long,
    val totalWeeks: Int,
    val currentWeek: Int,
    val totalPeriods: Int,
    val periodTimes: List<String>
)

data class ExportData(
    val courses: List<Course>,
    val settingsA: ScheduleSettingsExport?,
    val settingsB: ScheduleSettingsExport?,
    val personAName: String,
    val personBName: String,
    val exportTime: Long = System.currentTimeMillis()
)

data class ImportResult(
    val success: Boolean,
    val importedCount: Int = 0,
    val failedCount: Int = 0,
    val conflictCount: Int = 0,
    val errors: List<String> = emptyList(),
    val courses: List<CourseImportData> = emptyList(),
    val fileType: CsvFileType = CsvFileType.TEMPLATE,
    val settingsA: ScheduleSettingsExport? = null,
    val settingsB: ScheduleSettingsExport? = null,
    val personAName: String? = null,
    val personBName: String? = null,
    val exportVersion: Int = 0
)

enum class CsvFileType {
    APP_EXPORT,
    TEMPLATE
}

data class ImportPreviewData(
    val courses: List<CourseImportData>,
    val fileType: CsvFileType = CsvFileType.TEMPLATE,
    val settingsA: ScheduleSettingsExport? = null,
    val settingsB: ScheduleSettingsExport? = null,
    val personAName: String? = null,
    val personBName: String? = null,
    val exportVersion: Int = 0
)

data class ImportedSettings(
    val personAName: String? = null,
    val personBName: String? = null,
    val settingsA: ScheduleSettingsExport? = null,
    val settingsB: ScheduleSettingsExport? = null
)

enum class ImportSource {
    CSV_FILE,
    WAKEUP_BACKUP,
    EDUCATION_SYSTEM
}

enum class ExportScope {
    PERSON_A_ONLY,
    PERSON_B_ONLY,
    BOTH,
    ALL_WITH_SETTINGS
}

data class SchoolInfo(
    val id: String,
    val name: String,
    val baseUrl: String,
    val systemType: String,
    val vpnHint: String = "",
    val loginUrl: String = "",
    val scheduleUrlPath: String = ""
)

data class SchoolLoginResult(
    val success: Boolean,
    val cookies: Map<String, String> = emptyMap(),
    val needCaptcha: Boolean = false,
    val captchaImageUrl: String? = null,
    val errors: List<String> = emptyList()
)

interface SchoolAdapter {
    val schoolInfo: SchoolInfo

    suspend fun login(
        username: String,
        password: String,
        captcha: String = "",
        cookies: Map<String, String> = emptyMap()
    ): SchoolLoginResult

    suspend fun fetchScheduleHtml(
        cookies: Map<String, String>,
        studentId: String
    ): String?

    fun parseScheduleHtml(
        html: String,
        targetPerson: PersonType
    ): List<CourseImportData>
}

object SupportedSchools {
    private val adapters = mutableMapOf<String, SchoolAdapter>()

    fun register(adapter: SchoolAdapter) {
        adapters[adapter.schoolInfo.id] = adapter
    }

    fun getSchools(): List<SchoolInfo> = adapters.values.map { it.schoolInfo }

    fun getAdapter(schoolId: String): SchoolAdapter? = adapters[schoolId]

    fun getAdapterByPosition(position: Int): SchoolAdapter? {
        val list = adapters.values.toList()
        return list.getOrNull(position)
    }
}
