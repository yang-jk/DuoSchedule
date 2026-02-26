package com.duoschedule.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duoschedule.data.model.Course
import com.duoschedule.data.model.PersonType
import com.duoschedule.data.model.TodayCourseDisplayMode
import com.duoschedule.data.repository.CourseRepository
import com.duoschedule.ui.model.CurrentCourseState
import com.duoschedule.ui.model.FreeTimeSlot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: CourseRepository
) : ViewModel() {

    private val _currentHour = MutableStateFlow(LocalTime.now().hour)
    private val _currentMinute = MutableStateFlow(LocalTime.now().minute)
    
    val personAName: StateFlow<String> = repository.getPersonAName()
        .stateIn(viewModelScope, SharingStarted.Lazily, "Ta")

    val personBName: StateFlow<String> = repository.getPersonBName()
        .stateIn(viewModelScope, SharingStarted.Lazily, "我")

    val personACurrentWeek: StateFlow<Int> = repository.getCurrentWeek(PersonType.PERSON_A)
        .stateIn(viewModelScope, SharingStarted.Lazily, 1)

    val personBCurrentWeek: StateFlow<Int> = repository.getCurrentWeek(PersonType.PERSON_B)
        .stateIn(viewModelScope, SharingStarted.Lazily, 1)

    val todayCourseDisplayMode: StateFlow<TodayCourseDisplayMode> = repository.getTodayCourseDisplayMode()
        .stateIn(viewModelScope, SharingStarted.Lazily, TodayCourseDisplayMode.BOTH)

    val personAPeriodTimes: StateFlow<List<String>> = repository.getPeriodTimes(PersonType.PERSON_A)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val personBPeriodTimes: StateFlow<List<String>> = repository.getPeriodTimes(PersonType.PERSON_B)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val todayCourses: StateFlow<List<Course>> = repository.getCoursesByDay(getCurrentDayOfWeek())
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val personATodayCourses: StateFlow<List<Course>> = todayCourses
        .combine(personACurrentWeek) { courses, week ->
            courses.filter { it.personType == PersonType.PERSON_A && it.isInWeek(week) }
                .sortedBy { it.startHour * 60 + it.startMinute }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val personBTodayCourses: StateFlow<List<Course>> = todayCourses
        .combine(personBCurrentWeek) { courses, week ->
            courses.filter { it.personType == PersonType.PERSON_B && it.isInWeek(week) }
                .sortedBy { it.startHour * 60 + it.startMinute }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private data class TimeState(
        val hour: Int,
        val minute: Int
    )

    private val currentTime = combine(_currentHour, _currentMinute) { hour, minute ->
        TimeState(hour, minute)
    }

    val personACurrentCourse: StateFlow<CurrentCourseState> = combine(
        personATodayCourses,
        currentTime,
        personAName,
        personAPeriodTimes
    ) { courses, time, name, periodTimes ->
        val currentCourse = courses.find { 
            it.isOngoing(time.hour, time.minute, 1)
        }
        val nextCourse = courses.find { course ->
            course.startHour * 60 + course.startMinute > time.hour * 60 + time.minute
        }
        val progress = if (currentCourse != null) {
            val totalMinutes = (currentCourse.endHour * 60 + currentCourse.endMinute) - 
                              (currentCourse.startHour * 60 + currentCourse.startMinute)
            val elapsedMinutes = (time.hour * 60 + time.minute) - 
                                (currentCourse.startHour * 60 + currentCourse.startMinute)
            if (totalMinutes > 0) elapsedMinutes.toFloat() / totalMinutes.toFloat() else 0f
        } else 0f
        val nextCourseStartTime = if (nextCourse != null) {
            String.format("%02d:%02d", nextCourse.startHour, nextCourse.startMinute)
        } else ""
        CurrentCourseState(
            personType = PersonType.PERSON_A,
            personName = name,
            course = currentCourse,
            remainingMinutes = currentCourse?.getRemainingMinutes(time.hour, time.minute) ?: 0,
            hasCourse = currentCourse != null,
            progress = progress.coerceIn(0f, 1f),
            nextCourse = nextCourse,
            nextCourseStartTime = nextCourseStartTime,
            periodText = currentCourse?.let { getPeriodText(it, periodTimes) } ?: "",
            nextCoursePeriodText = nextCourse?.let { getPeriodText(it, periodTimes) } ?: ""
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, 
        CurrentCourseState(PersonType.PERSON_A, "Ta"))

    val personBCurrentCourse: StateFlow<CurrentCourseState> = combine(
        personBTodayCourses,
        currentTime,
        personBName,
        personBPeriodTimes
    ) { courses, time, name, periodTimes ->
        val currentCourse = courses.find { 
            it.isOngoing(time.hour, time.minute, 1)
        }
        val nextCourse = courses.find { course ->
            course.startHour * 60 + course.startMinute > time.hour * 60 + time.minute
        }
        val progress = if (currentCourse != null) {
            val totalMinutes = (currentCourse.endHour * 60 + currentCourse.endMinute) - 
                              (currentCourse.startHour * 60 + currentCourse.startMinute)
            val elapsedMinutes = (time.hour * 60 + time.minute) - 
                                (currentCourse.startHour * 60 + currentCourse.startMinute)
            if (totalMinutes > 0) elapsedMinutes.toFloat() / totalMinutes.toFloat() else 0f
        } else 0f
        val nextCourseStartTime = if (nextCourse != null) {
            String.format("%02d:%02d", nextCourse.startHour, nextCourse.startMinute)
        } else ""
        CurrentCourseState(
            personType = PersonType.PERSON_B,
            personName = name,
            course = currentCourse,
            remainingMinutes = currentCourse?.getRemainingMinutes(time.hour, time.minute) ?: 0,
            hasCourse = currentCourse != null,
            progress = progress.coerceIn(0f, 1f),
            nextCourse = nextCourse,
            nextCourseStartTime = nextCourseStartTime,
            periodText = currentCourse?.let { getPeriodText(it, periodTimes) } ?: "",
            nextCoursePeriodText = nextCourse?.let { getPeriodText(it, periodTimes) } ?: ""
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, 
        CurrentCourseState(PersonType.PERSON_B, "我"))

    val freeTimeSlots: StateFlow<List<FreeTimeSlot>> = combine(
        personATodayCourses,
        personBTodayCourses
    ) { coursesA, coursesB ->
        calculateFreeTimeSlots(coursesA, coursesB)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun updateTime() {
        val now = LocalTime.now()
        _currentHour.value = now.hour
        _currentMinute.value = now.minute
    }

    fun setCurrentWeek(personType: PersonType, week: Int) {
        viewModelScope.launch {
            repository.setCurrentWeek(personType, week)
        }
    }

    fun setSemesterStartDate(personType: PersonType, date: LocalDate) {
        viewModelScope.launch {
            repository.setSemesterStartDate(personType, date)
            val totalWeeks = repository.getTotalWeeks(personType).stateIn(viewModelScope).value
            val week = repository.calculateCurrentWeek(date, totalWeeks)
            repository.setCurrentWeek(personType, week)
        }
    }

    fun setPersonName(personType: PersonType, name: String) {
        viewModelScope.launch {
            repository.setPersonName(personType, name)
        }
    }

    fun deleteCourse(courseId: Long) {
        viewModelScope.launch {
            repository.deleteCourseById(courseId)
        }
    }

    fun setTodayCourseDisplayMode(mode: TodayCourseDisplayMode) {
        viewModelScope.launch {
            repository.setTodayCourseDisplayMode(mode)
        }
    }

    private fun getCurrentDayOfWeek(): Int {
        return LocalDate.now().dayOfWeek.value
    }

    private fun calculateFreeTimeSlots(
        coursesA: List<Course>,
        coursesB: List<Course>
    ): List<FreeTimeSlot> {
        val allCourses = (coursesA + coursesB).sortedBy { it.startHour * 60 + it.startMinute }
        
        if (allCourses.isEmpty()) {
            return listOf(FreeTimeSlot(8, 0, 22, 0))
        }

        val freeSlots = mutableListOf<FreeTimeSlot>()
        var lastEndTime = 8 * 60

        for (course in allCourses) {
            val startTime = course.startHour * 60 + course.startMinute
            if (startTime - lastEndTime >= 30) {
                freeSlots.add(FreeTimeSlot(
                    lastEndTime / 60, lastEndTime % 60,
                    startTime / 60, startTime % 60
                ))
            }
            val endTime = course.endHour * 60 + course.endMinute
            if (endTime > lastEndTime) {
                lastEndTime = endTime
            }
        }

        if (22 * 60 - lastEndTime >= 30) {
            freeSlots.add(FreeTimeSlot(lastEndTime / 60, lastEndTime % 60, 22, 0))
        }

        return freeSlots
    }

    private fun getPeriodText(course: Course, periodTimes: List<String>): String {
        if (periodTimes.isEmpty()) return ""
        
        val courseStartMinutes = course.startHour * 60 + course.startMinute
        val courseEndMinutes = course.endHour * 60 + course.endMinute
        
        var startPeriod = -1
        var endPeriod = -1
        
        for ((index, periodTime) in periodTimes.withIndex()) {
            val parts = periodTime.split("-")
            if (parts.size == 2) {
                val startParts = parts[0].split(":")
                val endParts = parts[1].split(":")
                if (startParts.size == 2 && endParts.size == 2) {
                    val periodStartMinutes = (startParts[0].toIntOrNull() ?: 0) * 60 +
                        (startParts[1].toIntOrNull() ?: 0)
                    val periodEndMinutes = (endParts[0].toIntOrNull() ?: 0) * 60 +
                        (endParts[1].toIntOrNull() ?: 0)
                    
                    if (startPeriod == -1 && courseStartMinutes >= periodStartMinutes && courseStartMinutes < periodEndMinutes) {
                        startPeriod = index + 1
                    }
                    if (courseEndMinutes > periodStartMinutes && courseEndMinutes <= periodEndMinutes) {
                        endPeriod = index + 1
                    }
                }
            }
        }
        
        return if (startPeriod > 0 && endPeriod > 0) {
            if (startPeriod == endPeriod) {
                "第${startPeriod}节"
            } else {
                "第${startPeriod}-${endPeriod}节"
            }
        } else {
            ""
        }
    }
}
