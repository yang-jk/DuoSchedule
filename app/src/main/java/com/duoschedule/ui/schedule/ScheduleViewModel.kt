package com.duoschedule.ui.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duoschedule.data.model.Course
import com.duoschedule.data.model.PersonType
import com.duoschedule.data.repository.CourseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val repository: CourseRepository
) : ViewModel() {

    private val cachedFlows = mutableMapOf<String, StateFlow<*>>()
    
    @Suppress("UNCHECKED_CAST")
    private fun <T> getCachedFlow(key: String, create: () -> StateFlow<T>): StateFlow<T> {
        return cachedFlows.getOrPut(key) { create() } as StateFlow<T>
    }

    val personAName: StateFlow<String> = getCachedFlow("personAName") {
        repository.getPersonAName()
            .stateIn(viewModelScope, SharingStarted.Eagerly, "Ta")
    }

    val personBName: StateFlow<String> = getCachedFlow("personBName") {
        repository.getPersonBName()
            .stateIn(viewModelScope, SharingStarted.Eagerly, "我")
    }

    fun getPersonName(personType: PersonType): StateFlow<String> =
        if (personType == PersonType.PERSON_A) personAName else personBName

    val showNonCurrentWeekCourses: StateFlow<Boolean> = getCachedFlow("showNonCurrentWeekCourses") {
        repository.getShowNonCurrentWeekCourses()
            .stateIn(viewModelScope, SharingStarted.Eagerly, false)
    }

    val showSaturday: StateFlow<Boolean> = getCachedFlow("showSaturday") {
        repository.getShowSaturday()
            .stateIn(viewModelScope, SharingStarted.Eagerly, true)
    }

    val showSunday: StateFlow<Boolean> = getCachedFlow("showSunday") {
        repository.getShowSunday()
            .stateIn(viewModelScope, SharingStarted.Eagerly, false)
    }

    val courseCellHeight: StateFlow<Int> = getCachedFlow("courseCellHeight") {
        repository.getCourseCellHeight()
            .stateIn(viewModelScope, SharingStarted.Eagerly, 60)
    }

    val showDashedBorder: StateFlow<Boolean> = getCachedFlow("showDashedBorder") {
        repository.getShowDashedBorder()
            .stateIn(viewModelScope, SharingStarted.Eagerly, true)
    }

    val courseNameFontSize: StateFlow<Int> = getCachedFlow("courseNameFontSize") {
        repository.getCourseNameFontSize()
            .stateIn(viewModelScope, SharingStarted.Eagerly, 12)
    }

    val courseLocationFontSize: StateFlow<Int> = getCachedFlow("courseLocationFontSize") {
        repository.getCourseLocationFontSize()
            .stateIn(viewModelScope, SharingStarted.Eagerly, 11)
    }

    private val personACurrentWeek: StateFlow<Int> = getCachedFlow("personACurrentWeek") {
        combine(
            repository.getCurrentWeek(PersonType.PERSON_A),
            repository.getSemesterStartDate(PersonType.PERSON_A),
            repository.getTotalWeeks(PersonType.PERSON_A)
        ) { storedWeek, startDate, totalWeeks ->
            val calculated = repository.calculateCurrentWeek(startDate, totalWeeks)
            if (storedWeek != calculated) {
                viewModelScope.launch {
                    repository.setCurrentWeek(PersonType.PERSON_A, calculated)
                }
            }
            calculated
        }.stateIn(viewModelScope, SharingStarted.Eagerly, 1)
    }

    private val personBCurrentWeek: StateFlow<Int> = getCachedFlow("personBCurrentWeek") {
        combine(
            repository.getCurrentWeek(PersonType.PERSON_B),
            repository.getSemesterStartDate(PersonType.PERSON_B),
            repository.getTotalWeeks(PersonType.PERSON_B)
        ) { storedWeek, startDate, totalWeeks ->
            val calculated = repository.calculateCurrentWeek(startDate, totalWeeks)
            if (storedWeek != calculated) {
                viewModelScope.launch {
                    repository.setCurrentWeek(PersonType.PERSON_B, calculated)
                }
            }
            calculated
        }.stateIn(viewModelScope, SharingStarted.Eagerly, 1)
    }

    private val personATotalWeeks: StateFlow<Int> = getCachedFlow("personATotalWeeks") {
        repository.getTotalWeeks(PersonType.PERSON_A)
            .stateIn(viewModelScope, SharingStarted.Eagerly, 16)
    }

    private val personBTotalWeeks: StateFlow<Int> = getCachedFlow("personBTotalWeeks") {
        repository.getTotalWeeks(PersonType.PERSON_B)
            .stateIn(viewModelScope, SharingStarted.Eagerly, 16)
    }

    private val personASemesterStartDate: StateFlow<LocalDate> = getCachedFlow("personASemesterStartDate") {
        repository.getSemesterStartDate(PersonType.PERSON_A)
            .stateIn(viewModelScope, SharingStarted.Eagerly, LocalDate.now())
    }

    private val personBSemesterStartDate: StateFlow<LocalDate> = getCachedFlow("personBSemesterStartDate") {
        repository.getSemesterStartDate(PersonType.PERSON_B)
            .stateIn(viewModelScope, SharingStarted.Eagerly, LocalDate.now())
    }

    private val personATotalPeriods: StateFlow<Int> = getCachedFlow("personATotalPeriods") {
        repository.getTotalPeriods(PersonType.PERSON_A)
            .stateIn(viewModelScope, SharingStarted.Eagerly, 5)
    }

    private val personBTotalPeriods: StateFlow<Int> = getCachedFlow("personBTotalPeriods") {
        repository.getTotalPeriods(PersonType.PERSON_B)
            .stateIn(viewModelScope, SharingStarted.Eagerly, 5)
    }

    private val personAPeriodTimes: StateFlow<List<String>> = getCachedFlow("personAPeriodTimes") {
        repository.getPeriodTimes(PersonType.PERSON_A)
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    }

    private val personBPeriodTimes: StateFlow<List<String>> = getCachedFlow("personBPeriodTimes") {
        repository.getPeriodTimes(PersonType.PERSON_B)
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    }

    private val personACourses = repository.getCoursesByPerson(PersonType.PERSON_A)
        .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    private val personBCourses = repository.getCoursesByPerson(PersonType.PERSON_B)
        .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    fun getCurrentWeek(personType: PersonType): StateFlow<Int> = 
        if (personType == PersonType.PERSON_A) personACurrentWeek else personBCurrentWeek

    fun getTotalWeeks(personType: PersonType): StateFlow<Int> = 
        if (personType == PersonType.PERSON_A) personATotalWeeks else personBTotalWeeks

    fun getSemesterStartDate(personType: PersonType): StateFlow<LocalDate> = 
        if (personType == PersonType.PERSON_A) personASemesterStartDate else personBSemesterStartDate

    fun getTotalPeriods(personType: PersonType): StateFlow<Int> = 
        if (personType == PersonType.PERSON_A) personATotalPeriods else personBTotalPeriods

    fun getPeriodTimes(personType: PersonType): StateFlow<List<String>> = 
        if (personType == PersonType.PERSON_A) personAPeriodTimes else personBPeriodTimes

    fun getCoursesByPerson(personType: PersonType) = 
        if (personType == PersonType.PERSON_A) personACourses else personBCourses

    fun getWeekDates(semesterStartDate: LocalDate, currentWeek: Int): List<LocalDate> =
        repository.getWeekDates(semesterStartDate, currentWeek)

    fun getCourseCellHeight(personType: PersonType): StateFlow<Int> = courseCellHeight

    fun deleteCourse(courseId: Long) {
        viewModelScope.launch {
            repository.deleteCourseById(courseId)
        }
    }

    suspend fun pasteCourse(
        dayOfWeek: Int,
        period: Int,
        periodTimes: List<String>,
        personType: PersonType
    ): PasteResult {
        val clippedCourse = CourseClipboard.clippedCourse.value ?: return PasteResult.NoContent
        
        val timeRange = periodTimes.getOrNull(period - 1) ?: return PasteResult.InvalidPeriod
        
        val times = timeRange.split("-")
        if (times.size != 2) return PasteResult.InvalidPeriod
        
        val startParts = times[0].split(":")
        val endParts = times[1].split(":")
        
        if (startParts.size != 2 || endParts.size != 2) return PasteResult.InvalidPeriod
        
        val startHour = startParts[0].toIntOrNull() ?: return PasteResult.InvalidPeriod
        val startMinute = startParts[1].toIntOrNull() ?: return PasteResult.InvalidPeriod
        val endHour = endParts[0].toIntOrNull() ?: return PasteResult.InvalidPeriod
        val endMinute = endParts[1].toIntOrNull() ?: return PasteResult.InvalidPeriod
        
        val newCourse = CourseClipboard.createPastedCourse(
            dayOfWeek = dayOfWeek,
            targetPersonType = personType,
            startHour = startHour,
            startMinute = startMinute,
            endHour = endHour,
            endMinute = endMinute,
            startPeriod = period,
            endPeriod = period
        ) ?: return PasteResult.NoContent
        
        val conflictCourse = findConflictCourse(newCourse)
        
        if (conflictCourse != null) {
            return PasteResult.Conflict(newCourse, conflictCourse)
        }
        
        repository.insertCourse(newCourse)
        return PasteResult.Success
    }
    
    private suspend fun findConflictCourse(course: Course): Course? {
        val courses = repository.getCoursesByPerson(course.personType).first()
            .filter { it.dayOfWeek == course.dayOfWeek }
        
        for (existing in courses) {
            if (hasTimeOverlap(course, existing)) {
                return existing
            }
        }
        return null
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
        val weeks1 = course1.getActiveWeeks()
        val weeks2 = course2.getActiveWeeks()
        return weeks1.intersect(weeks2).isNotEmpty()
    }

    suspend fun checkPasteConflict(
        dayOfWeek: Int,
        period: Int,
        periodTimes: List<String>,
        personType: PersonType
    ): Boolean {
        val clippedCourse = CourseClipboard.clippedCourse.value ?: return false
        
        val timeRange = periodTimes.getOrNull(period - 1) ?: return false
        val times = timeRange.split("-")
        if (times.size != 2) return false
        
        val startParts = times[0].split(":")
        val endParts = times[1].split(":")
        
        if (startParts.size != 2 || endParts.size != 2) return false
        
        val startHour = startParts[0].toIntOrNull() ?: return false
        val startMinute = startParts[1].toIntOrNull() ?: return false
        val endHour = endParts[0].toIntOrNull() ?: return false
        val endMinute = endParts[1].toIntOrNull() ?: return false
        
        val newCourse = CourseClipboard.createPastedCourse(
            dayOfWeek = dayOfWeek,
            targetPersonType = personType,
            startHour = startHour,
            startMinute = startMinute,
            endHour = endHour,
            endMinute = endMinute,
            startPeriod = period,
            endPeriod = period
        ) ?: return false
        
        return repository.checkTimeConflict(newCourse)
    }

    suspend fun forcePasteCourse(
        dayOfWeek: Int,
        period: Int,
        periodTimes: List<String>,
        personType: PersonType
    ): Boolean {
        val clippedCourse = CourseClipboard.clippedCourse.value ?: return false
        
        val timeRange = periodTimes.getOrNull(period - 1) ?: return false
        val times = timeRange.split("-")
        if (times.size != 2) return false
        
        val startParts = times[0].split(":")
        val endParts = times[1].split(":")
        
        if (startParts.size != 2 || endParts.size != 2) return false
        
        val startHour = startParts[0].toIntOrNull() ?: return false
        val startMinute = startParts[1].toIntOrNull() ?: return false
        val endHour = endParts[0].toIntOrNull() ?: return false
        val endMinute = endParts[1].toIntOrNull() ?: return false
        
        val newCourse = CourseClipboard.createPastedCourse(
            dayOfWeek = dayOfWeek,
            targetPersonType = personType,
            startHour = startHour,
            startMinute = startMinute,
            endHour = endHour,
            endMinute = endMinute,
            startPeriod = period,
            endPeriod = period
        ) ?: return false
        
        repository.insertCourse(newCourse)
        return true
    }
    
    suspend fun forcePasteWithConflictResolution(
        conflictCourse: Course,
        dayOfWeek: Int,
        period: Int,
        periodTimes: List<String>,
        personType: PersonType
    ): Boolean {
        repository.deleteCourse(conflictCourse)
        return forcePasteCourse(dayOfWeek, period, periodTimes, personType)
    }
}

sealed class PasteResult {
    object Success : PasteResult()
    object NoContent : PasteResult()
    object InvalidPeriod : PasteResult()
    data class Conflict(
        val newCourse: com.duoschedule.data.model.Course,
        val existingCourse: com.duoschedule.data.model.Course
    ) : PasteResult()
}
