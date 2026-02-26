package com.duoschedule.ui.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duoschedule.data.model.PersonType
import com.duoschedule.data.repository.CourseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
            .stateIn(viewModelScope, SharingStarted.Lazily, "Ta")
    }

    val personBName: StateFlow<String> = getCachedFlow("personBName") {
        repository.getPersonBName()
            .stateIn(viewModelScope, SharingStarted.Lazily, "我")
    }

    val showNonCurrentWeekCourses: StateFlow<Boolean> = getCachedFlow("showNonCurrentWeekCourses") {
        repository.getShowNonCurrentWeekCourses()
            .stateIn(viewModelScope, SharingStarted.Lazily, false)
    }

    val showSaturday: StateFlow<Boolean> = getCachedFlow("showSaturday") {
        repository.getShowSaturday()
            .stateIn(viewModelScope, SharingStarted.Lazily, true)
    }

    val showSunday: StateFlow<Boolean> = getCachedFlow("showSunday") {
        repository.getShowSunday()
            .stateIn(viewModelScope, SharingStarted.Lazily, false)
    }

    val courseCellHeight: StateFlow<Int> = getCachedFlow("courseCellHeight") {
        repository.getCourseCellHeight()
            .stateIn(viewModelScope, SharingStarted.Lazily, 60)
    }

    private val personACurrentWeek: StateFlow<Int> = getCachedFlow("personACurrentWeek") {
        repository.getCurrentWeek(PersonType.PERSON_A)
            .stateIn(viewModelScope, SharingStarted.Lazily, 1)
    }

    private val personBCurrentWeek: StateFlow<Int> = getCachedFlow("personBCurrentWeek") {
        repository.getCurrentWeek(PersonType.PERSON_B)
            .stateIn(viewModelScope, SharingStarted.Lazily, 1)
    }

    private val personATotalWeeks: StateFlow<Int> = getCachedFlow("personATotalWeeks") {
        repository.getTotalWeeks(PersonType.PERSON_A)
            .stateIn(viewModelScope, SharingStarted.Lazily, 16)
    }

    private val personBTotalWeeks: StateFlow<Int> = getCachedFlow("personBTotalWeeks") {
        repository.getTotalWeeks(PersonType.PERSON_B)
            .stateIn(viewModelScope, SharingStarted.Lazily, 16)
    }

    private val personASemesterStartDate: StateFlow<LocalDate> = getCachedFlow("personASemesterStartDate") {
        repository.getSemesterStartDate(PersonType.PERSON_A)
            .stateIn(viewModelScope, SharingStarted.Lazily, LocalDate.now())
    }

    private val personBSemesterStartDate: StateFlow<LocalDate> = getCachedFlow("personBSemesterStartDate") {
        repository.getSemesterStartDate(PersonType.PERSON_B)
            .stateIn(viewModelScope, SharingStarted.Lazily, LocalDate.now())
    }

    private val personATotalPeriods: StateFlow<Int> = getCachedFlow("personATotalPeriods") {
        repository.getTotalPeriods(PersonType.PERSON_A)
            .stateIn(viewModelScope, SharingStarted.Lazily, 12)
    }

    private val personBTotalPeriods: StateFlow<Int> = getCachedFlow("personBTotalPeriods") {
        repository.getTotalPeriods(PersonType.PERSON_B)
            .stateIn(viewModelScope, SharingStarted.Lazily, 12)
    }

    private val personAPeriodTimes: StateFlow<List<String>> = getCachedFlow("personAPeriodTimes") {
        repository.getPeriodTimes(PersonType.PERSON_A)
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    }

    private val personBPeriodTimes: StateFlow<List<String>> = getCachedFlow("personBPeriodTimes") {
        repository.getPeriodTimes(PersonType.PERSON_B)
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    }

    private val personACourses = repository.getCoursesByPerson(PersonType.PERSON_A)
        .shareIn(viewModelScope, SharingStarted.Lazily, replay = 1)

    private val personBCourses = repository.getCoursesByPerson(PersonType.PERSON_B)
        .shareIn(viewModelScope, SharingStarted.Lazily, replay = 1)

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
}
