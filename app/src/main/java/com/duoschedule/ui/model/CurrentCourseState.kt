package com.duoschedule.ui.model

import com.duoschedule.data.model.Course
import com.duoschedule.data.model.PersonType

data class CurrentCourseState(
    val personType: PersonType,
    val personName: String,
    val course: Course? = null,
    val remainingMinutes: Int = 0,
    val hasCourse: Boolean = false,
    val progress: Float = 0f,
    val nextCourse: Course? = null,
    val nextCourseStartTime: String = "",
    val periodText: String = "",
    val nextCoursePeriodText: String = ""
) {
    val displayText: String
        get() = if (hasCourse && course != null) course.name else "休息中"

    val timeText: String
        get() = if (hasCourse && course != null) "剩余 $remainingMinutes 分钟" else ""

    val locationText: String
        get() = if (hasCourse && course != null && course.location.isNotBlank()) course.location else ""

    val hasNextCourse: Boolean
        get() = nextCourse != null

    val nextCourseDisplayText: String
        get() = nextCourse?.name ?: ""

    val nextCourseLocationText: String
        get() = if (nextCourse != null && nextCourse.location.isNotBlank()) nextCourse.location else ""
}
