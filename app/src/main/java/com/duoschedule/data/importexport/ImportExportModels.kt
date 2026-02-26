package com.duoschedule.data.importexport

import com.duoschedule.data.model.Course
import com.duoschedule.data.model.PersonType
import com.duoschedule.data.model.WeekType

data class CourseImportData(
    val name: String,
    val location: String = "",
    val dayOfWeek: Int,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val weekType: WeekType,
    val startWeek: Int = 1,
    val endWeek: Int = 16,
    val customWeeks: String = "",
    val personType: PersonType
) {
    fun toCourse(): Course {
        return Course(
            name = name,
            location = location,
            dayOfWeek = dayOfWeek,
            startHour = startHour,
            startMinute = startMinute,
            endHour = endHour,
            endMinute = endMinute,
            weekType = weekType,
            startWeek = startWeek,
            endWeek = endWeek,
            customWeeks = customWeeks,
            personType = personType
        )
    }

    fun hasConflictWith(existing: Course): Boolean {
        if (dayOfWeek != existing.dayOfWeek) return false
        
        val startMinutes = startHour * 60 + startMinute
        val endMinutes = endHour * 60 + endMinute
        val existingStart = existing.startHour * 60 + existing.startMinute
        val existingEnd = existing.endHour * 60 + existing.endMinute
        
        return startMinutes < existingEnd && endMinutes > existingStart
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
    val courses: List<CourseImportData> = emptyList()
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
