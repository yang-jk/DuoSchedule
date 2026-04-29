package com.duoschedule.data.repository

import com.duoschedule.data.local.CourseDao
import com.duoschedule.data.local.SettingsDataStore
import com.duoschedule.data.model.Course
import com.duoschedule.data.model.PersonType
import com.duoschedule.data.model.ThemeMode
import com.duoschedule.data.model.TodayCourseDisplayMode
import com.duoschedule.notification.SilentModeType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CourseRepository @Inject constructor(
    private val courseDao: CourseDao,
    private val settingsDataStore: SettingsDataStore
) {
    fun getAllCourses(): Flow<List<Course>> = courseDao.getAllCourses()

    fun getCoursesByPerson(personType: PersonType): Flow<List<Course>> = 
        courseDao.getCoursesByPerson(personType)

    fun getCoursesByDay(dayOfWeek: Int): Flow<List<Course>> = 
        courseDao.getCoursesByDay(dayOfWeek)

    fun getCoursesByDayAndPerson(dayOfWeek: Int, personType: PersonType): Flow<List<Course>> = 
        courseDao.getCoursesByDayAndPerson(dayOfWeek, personType)

    suspend fun getCourseById(id: Long): Course? = courseDao.getCourseById(id)

    suspend fun insertCourse(course: Course): Long = courseDao.insertCourse(course)

    suspend fun updateCourse(course: Course) = courseDao.updateCourse(course)

    suspend fun deleteCourse(course: Course) = courseDao.deleteCourse(course)

    suspend fun deleteCourseById(id: Long) = courseDao.deleteCourseById(id)

    suspend fun deleteCoursesByPerson(personType: PersonType) = 
        courseDao.deleteCoursesByPerson(personType)

    suspend fun deleteAllCourses() = courseDao.deleteAllCourses()

    fun getCourseCountByPerson(personType: PersonType): Flow<Int> = 
        courseDao.getCourseCountByPerson(personType)

    fun getPersonAName(): Flow<String> = settingsDataStore.personAName

    fun getPersonBName(): Flow<String> = settingsDataStore.personBName

    fun getSemesterStartDate(personType: PersonType): Flow<LocalDate> = 
        settingsDataStore.getSemesterStartDate(personType)

    fun getTotalWeeks(personType: PersonType): Flow<Int> = 
        settingsDataStore.getTotalWeeks(personType)

    fun getCurrentWeek(personType: PersonType): Flow<Int> = 
        settingsDataStore.getCurrentWeek(personType)

    fun getTotalPeriods(personType: PersonType): Flow<Int> = 
        settingsDataStore.getTotalPeriods(personType)

    fun getPeriodTimes(personType: PersonType): Flow<List<String>> = 
        settingsDataStore.getPeriodTimes(personType)

    fun getShowNonCurrentWeekCourses(): Flow<Boolean> = 
        settingsDataStore.showNonCurrentWeekCourses

    fun getShowSaturday(): Flow<Boolean> = 
        settingsDataStore.showSaturday

    fun getShowSunday(): Flow<Boolean> = 
        settingsDataStore.showSunday

    fun getCourseCellHeight(): Flow<Int> = 
        settingsDataStore.courseCellHeight

    fun getShowDashedBorder(): Flow<Boolean> =
        settingsDataStore.showDashedBorder

    fun getNotificationEnabled(): Flow<Boolean> = 
        settingsDataStore.notificationEnabled

    fun getNotificationAdvanceTime(): Flow<Int> = 
        settingsDataStore.notificationAdvanceTime

    fun getLiveNotificationEnabled(): Flow<Boolean> = 
        settingsDataStore.liveNotificationEnabled

    fun getTodayCourseDisplayMode(): Flow<TodayCourseDisplayMode> = 
        settingsDataStore.todayCourseDisplayMode

    fun getThemeMode(): Flow<ThemeMode> = 
        settingsDataStore.themeMode

    fun getAutoSilentEnabled(): Flow<Boolean> = 
        settingsDataStore.autoSilentEnabled

    fun getAutoSilentModeType(): Flow<String> = 
        settingsDataStore.autoSilentModeType

    fun getAutoSilentAdvanceTime(): Flow<Int> =
        settingsDataStore.autoSilentAdvanceTime

    fun getCourseNameFontSize(): Flow<Int> =
        settingsDataStore.courseNameFontSize

    fun getCourseLocationFontSize(): Flow<Int> =
        settingsDataStore.courseLocationFontSize

    suspend fun getAutoSilentEnabledSync(): Boolean = 
        settingsDataStore.getAutoSilentEnabled()

    suspend fun getAutoSilentModeTypeSync(): SilentModeType = 
        settingsDataStore.getAutoSilentModeType()

    suspend fun getAutoSilentAdvanceTimeSync(): Int =
        settingsDataStore.getAutoSilentAdvanceTime()

    suspend fun setSemesterStartDate(personType: PersonType, date: LocalDate) = 
        settingsDataStore.setSemesterStartDate(personType, date)

    suspend fun setTotalWeeks(personType: PersonType, weeks: Int) = 
        settingsDataStore.setTotalWeeks(personType, weeks)

    suspend fun setCurrentWeek(personType: PersonType, week: Int) = 
        settingsDataStore.setCurrentWeek(personType, week)

    suspend fun setTotalPeriods(personType: PersonType, periods: Int) = 
        settingsDataStore.setTotalPeriods(personType, periods)

    suspend fun setPeriodTimes(personType: PersonType, times: List<String>) = 
        settingsDataStore.setPeriodTimes(personType, times)

    suspend fun setPersonName(personType: PersonType, name: String) = 
        settingsDataStore.setPersonName(personType, name)

    suspend fun setShowNonCurrentWeekCourses(show: Boolean) = 
        settingsDataStore.setShowNonCurrentWeekCourses(show)

    suspend fun setShowSaturday(show: Boolean) = 
        settingsDataStore.setShowSaturday(show)

    suspend fun setShowSunday(show: Boolean) = 
        settingsDataStore.setShowSunday(show)

    suspend fun setCourseCellHeight(height: Int) = 
        settingsDataStore.setCourseCellHeight(height)

    suspend fun setShowDashedBorder(show: Boolean) =
        settingsDataStore.setShowDashedBorder(show)

    suspend fun setNotificationEnabled(enabled: Boolean) = 
        settingsDataStore.setNotificationEnabled(enabled)

    suspend fun setNotificationAdvanceTime(minutes: Int) = 
        settingsDataStore.setNotificationAdvanceTime(minutes)

    suspend fun setLiveNotificationEnabled(enabled: Boolean) = 
        settingsDataStore.setLiveNotificationEnabled(enabled)

    suspend fun setTodayCourseDisplayMode(mode: TodayCourseDisplayMode) = 
        settingsDataStore.setTodayCourseDisplayMode(mode)

    suspend fun setThemeMode(mode: ThemeMode) = 
        settingsDataStore.setThemeMode(mode)

    suspend fun setAutoSilentEnabled(enabled: Boolean) = 
        settingsDataStore.setAutoSilentEnabled(enabled)

    suspend fun setAutoSilentModeType(modeType: SilentModeType) = 
        settingsDataStore.setAutoSilentModeType(modeType)

    suspend fun setAutoSilentAdvanceTime(minutes: Int) =
        settingsDataStore.setAutoSilentAdvanceTime(minutes)

    suspend fun setCourseNameFontSize(size: Int) =
        settingsDataStore.setCourseNameFontSize(size)

    suspend fun setCourseLocationFontSize(size: Int) =
        settingsDataStore.setCourseLocationFontSize(size)

    fun calculateCurrentWeek(startDate: LocalDate, totalWeeks: Int): Int = 
        settingsDataStore.calculateCurrentWeek(startDate, totalWeeks)

    fun getWeekDates(semesterStartDate: LocalDate, currentWeek: Int): List<LocalDate> =
        settingsDataStore.getWeekDates(semesterStartDate, currentWeek)

    suspend fun checkTimeConflict(
        course: Course,
        excludeId: Long = 0
    ): Boolean {
        val courses = courseDao.getCoursesByPersonSync(course.personType)
            .filter { it.id != excludeId && it.dayOfWeek == course.dayOfWeek }
        
        android.util.Log.d("CourseRepository", "checkTimeConflict: course=${course.name}, excludeId=$excludeId, checking ${courses.size} courses")
        
        for (existing in courses) {
            android.util.Log.d("CourseRepository", "  checking against: ${existing.name} (id=${existing.id})")
            if (hasTimeOverlap(course, existing)) {
                android.util.Log.d("CourseRepository", "  CONFLICT DETECTED with ${existing.name}")
                return true
            }
        }
        android.util.Log.d("CourseRepository", "  NO CONFLICT")
        return false
    }

    private fun hasTimeOverlap(course1: Course, course2: Course): Boolean {
        val start1 = course1.startHour * 60 + course1.startMinute
        val end1 = course1.endHour * 60 + course1.endMinute
        val start2 = course2.startHour * 60 + course2.startMinute
        val end2 = course2.endHour * 60 + course2.endMinute
        
        android.util.Log.d("CourseRepository", "    time check: course1(${course1.startWeek}-${course1.endWeek}) ${course1.getStartTimeString()}-${course1.getEndTimeString()} vs course2(${course2.startWeek}-${course2.endWeek}) ${course2.getStartTimeString()}-${course2.getEndTimeString()}")
        
        if (start1 >= end2 || start2 >= end1) {
            android.util.Log.d("CourseRepository", "    no time overlap (time ranges don't intersect)")
            return false
        }
        
        android.util.Log.d("CourseRepository", "    time overlap exists, checking weeks...")
        return hasWeekOverlap(course1, course2)
    }

    private fun hasWeekOverlap(course1: Course, course2: Course): Boolean {
        val weeks1 = getActiveWeeks(course1)
        val weeks2 = getActiveWeeks(course2)
        val intersection = weeks1.intersect(weeks2)
        android.util.Log.d("CourseRepository", "    weeks1=$weeks1, weeks2=$weeks2, intersection=$intersection")
        return intersection.isNotEmpty()
    }

    private fun getActiveWeeks(course: Course): Set<Int> {
        val weekRange = course.startWeek..course.endWeek
        return when (course.weekType) {
            com.duoschedule.data.model.WeekType.ALL -> weekRange.toSet()
            com.duoschedule.data.model.WeekType.ODD -> weekRange.filter { it % 2 == 1 }.toSet()
            com.duoschedule.data.model.WeekType.EVEN -> weekRange.filter { it % 2 == 0 }.toSet()
            com.duoschedule.data.model.WeekType.CUSTOM -> {
                if (course.customWeeks.isNotEmpty()) {
                    course.customWeeks.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
                } else {
                    emptySet()
                }
            }
        }
    }

    suspend fun updateCourseTimesForPerson(
        personType: PersonType,
        periodTimes: List<String>
    ) {
        val courses = courseDao.getCoursesByPersonSync(personType)
        
        for (course in courses) {
            val newStartTime = getTimeFromPeriod(course.startPeriod, periodTimes)
            val newEndTime = getTimeFromPeriodEnd(course.endPeriod, periodTimes)
            
            if (newStartTime != null && newEndTime != null) {
                val updatedCourse = course.copy(
                    startHour = newStartTime.first,
                    startMinute = newStartTime.second,
                    endHour = newEndTime.first,
                    endMinute = newEndTime.second
                )
                courseDao.updateCourse(updatedCourse)
            }
        }
    }

    private fun getTimeFromPeriod(period: Int, periodTimes: List<String>): Pair<Int, Int>? {
        if (periodTimes.isEmpty() || period < 1 || period > periodTimes.size) return null
        
        val timeRange = periodTimes[period - 1]
        val times = timeRange.split("-")
        
        if (times.size == 2) {
            val startParts = times[0].split(":")
            if (startParts.size == 2) {
                try {
                    return Pair(startParts[0].toInt(), startParts[1].toInt())
                } catch (e: NumberFormatException) {
                    return null
                }
            }
        }
        
        return null
    }

    private fun getTimeFromPeriodEnd(period: Int, periodTimes: List<String>): Pair<Int, Int>? {
        if (periodTimes.isEmpty() || period < 1 || period > periodTimes.size) return null
        
        val timeRange = periodTimes[period - 1]
        val times = timeRange.split("-")
        
        if (times.size == 2) {
            val endParts = times[1].split(":")
            if (endParts.size == 2) {
                try {
                    return Pair(endParts[0].toInt(), endParts[1].toInt())
                } catch (e: NumberFormatException) {
                    return null
                }
            }
        }
        
        return null
    }
}
