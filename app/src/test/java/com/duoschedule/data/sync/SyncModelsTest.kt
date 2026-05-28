package com.duoschedule.data.sync

import com.duoschedule.data.model.Course
import com.duoschedule.data.model.PersonType
import com.duoschedule.data.model.WeekType
import org.junit.Assert.*
import org.junit.Test

class SyncModelsTest {

    @Test
    fun course_toCloudCourse_roundTrip() {
        val course = Course(
            id = 1,
            name = "高等数学",
            location = "A101",
            teacher = "张老师",
            dayOfWeek = 1,
            startHour = 8,
            startMinute = 0,
            endHour = 9,
            endMinute = 40,
            weekType = WeekType.ODD,
            startWeek = 1,
            endWeek = 16,
            customWeeks = "",
            personType = PersonType.PERSON_A,
            startPeriod = 1,
            endPeriod = 2,
            isCustomTime = false
        )
        val cloudCourse = course.toCloudCourse()
        val restored = cloudCourse.toCourse()
        assertEquals(course, restored)
    }

    @Test
    fun cloudCourse_invalidWeekType_defaultsToAll() {
        val cloudCourse = CloudCourse(
            id = 1, name = "test", location = "", teacher = "",
            dayOfWeek = 1, startHour = 8, startMinute = 0,
            endHour = 9, endMinute = 40, weekType = "INVALID",
            startWeek = 1, endWeek = 16, customWeeks = "",
            personType = "PERSON_A", startPeriod = 1, endPeriod = 1,
            isCustomTime = false
        )
        assertEquals(WeekType.ALL, cloudCourse.toCourse().weekType)
    }

    @Test
    fun cloudCourse_invalidPersonType_defaultsToPersonA() {
        val cloudCourse = CloudCourse(
            id = 1, name = "test", location = "", teacher = "",
            dayOfWeek = 1, startHour = 8, startMinute = 0,
            endHour = 9, endMinute = 40, weekType = "ALL",
            startWeek = 1, endWeek = 16, customWeeks = "",
            personType = "INVALID", startPeriod = 1, endPeriod = 1,
            isCustomTime = false
        )
        assertEquals(PersonType.PERSON_A, cloudCourse.toCourse().personType)
    }
}
