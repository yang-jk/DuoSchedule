package com.duoschedule.ui.schedule

import com.duoschedule.data.model.Course
import com.duoschedule.data.model.PersonType
import com.duoschedule.data.model.WeekType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object CourseClipboard {
    private val _clippedCourse = MutableStateFlow<ClippedCourse?>(null)
    val clippedCourse: StateFlow<ClippedCourse?> = _clippedCourse.asStateFlow()

    fun copy(course: Course) {
        _clippedCourse.value = ClippedCourse(
            name = course.name,
            location = course.location,
            teacher = course.teacher,
            startHour = course.startHour,
            startMinute = course.startMinute,
            endHour = course.endHour,
            endMinute = course.endMinute,
            weekType = course.weekType,
            startWeek = course.startWeek,
            endWeek = course.endWeek,
            customWeeks = course.customWeeks,
            personType = course.personType,
            startPeriod = course.startPeriod,
            endPeriod = course.endPeriod
        )
    }

    fun clear() {
        _clippedCourse.value = null
    }

    fun hasContent(): Boolean = _clippedCourse.value != null

    fun createPastedCourse(
        dayOfWeek: Int,
        targetPersonType: PersonType,
        startHour: Int? = null,
        startMinute: Int? = null,
        endHour: Int? = null,
        endMinute: Int? = null,
        startPeriod: Int? = null,
        endPeriod: Int? = null
    ): Course? {
        val clipped = _clippedCourse.value ?: return null
        
        return Course(
            id = 0,
            name = clipped.name,
            location = clipped.location,
            teacher = clipped.teacher,
            dayOfWeek = dayOfWeek,
            startHour = startHour ?: clipped.startHour,
            startMinute = startMinute ?: clipped.startMinute,
            endHour = endHour ?: clipped.endHour,
            endMinute = endMinute ?: clipped.endMinute,
            weekType = clipped.weekType,
            startWeek = clipped.startWeek,
            endWeek = clipped.endWeek,
            customWeeks = clipped.customWeeks,
            personType = targetPersonType,
            startPeriod = startPeriod ?: clipped.startPeriod,
            endPeriod = endPeriod ?: clipped.endPeriod
        )
    }
}

data class ClippedCourse(
    val name: String,
    val location: String,
    val teacher: String,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val weekType: WeekType,
    val startWeek: Int,
    val endWeek: Int,
    val customWeeks: String,
    val personType: PersonType,
    val startPeriod: Int,
    val endPeriod: Int
)
