package com.duoschedule.data.repository

import com.duoschedule.data.local.CourseDao
import com.duoschedule.data.local.SettingsDataStore
import com.duoschedule.data.model.Course
import com.duoschedule.data.model.PersonType
import com.duoschedule.data.model.ThemeMode
import com.duoschedule.data.model.TodayCourseDisplayMode
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

    fun getNotificationEnabled(): Flow<Boolean> = 
        settingsDataStore.notificationEnabled

    fun getNotificationAdvanceTime(): Flow<Int> = 
        settingsDataStore.notificationAdvanceTime

    fun getIslandDisplayMode(): Flow<String> = 
        settingsDataStore.islandDisplayMode

    fun getLiveNotificationEnabled(): Flow<Boolean> = 
        settingsDataStore.liveNotificationEnabled

    fun getTodayCourseDisplayMode(): Flow<TodayCourseDisplayMode> = 
        settingsDataStore.todayCourseDisplayMode

    fun getThemeMode(): Flow<ThemeMode> = 
        settingsDataStore.themeMode

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

    suspend fun setNotificationEnabled(enabled: Boolean) = 
        settingsDataStore.setNotificationEnabled(enabled)

    suspend fun setNotificationAdvanceTime(minutes: Int) = 
        settingsDataStore.setNotificationAdvanceTime(minutes)

    suspend fun setIslandDisplayMode(mode: String) = 
        settingsDataStore.setIslandDisplayMode(mode)

    suspend fun setLiveNotificationEnabled(enabled: Boolean) = 
        settingsDataStore.setLiveNotificationEnabled(enabled)

    suspend fun setTodayCourseDisplayMode(mode: TodayCourseDisplayMode) = 
        settingsDataStore.setTodayCourseDisplayMode(mode)

    suspend fun setThemeMode(mode: ThemeMode) = 
        settingsDataStore.setThemeMode(mode)

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
        
        for (existing in courses) {
            if (hasTimeOverlap(course, existing)) {
                return true
            }
        }
        return false
    }

    private fun hasTimeOverlap(course1: Course, course2: Course): Boolean {
        val start1 = course1.startHour * 60 + course1.startMinute
        val end1 = course1.endHour * 60 + course1.endMinute
        val start2 = course2.startHour * 60 + course2.startMinute
        val end2 = course2.endHour * 60 + course2.endMinute
        
        if (start1 >= end2 || start2 >= end1) {
            return false
        }
        
        return hasWeekOverlap(course1, course2)
    }

    private fun hasWeekOverlap(course1: Course, course2: Course): Boolean {
        if (course1.weekType == com.duoschedule.data.model.WeekType.ALL && 
            course2.weekType == com.duoschedule.data.model.WeekType.ALL) {
            return true
        }
        
        val weeks1 = getActiveWeeks(course1)
        val weeks2 = getActiveWeeks(course2)
        return weeks1.intersect(weeks2).isNotEmpty()
    }

    private fun getActiveWeeks(course: Course): Set<Int> {
        return when (course.weekType) {
            com.duoschedule.data.model.WeekType.ALL -> (course.startWeek..course.endWeek).toSet()
            com.duoschedule.data.model.WeekType.ODD -> (course.startWeek..course.endWeek step 2).toSet()
            com.duoschedule.data.model.WeekType.EVEN -> ((course.startWeek + 1)..course.endWeek step 2).toSet()
            com.duoschedule.data.model.WeekType.CUSTOM -> {
                if (course.customWeeks.isNotEmpty()) {
                    course.customWeeks.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
                } else {
                    emptySet()
                }
            }
        }
    }
}
