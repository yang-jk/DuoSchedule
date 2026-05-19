package com.duoschedule.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class CourseTest {

    @Test
    fun duration_normalCourse_returnsCorrectMinutes() {
        val course = createCourse(startHour = 8, startMinute = 0, endHour = 9, endMinute = 30)
        assertEquals(90, course.duration)
    }

    @Test
    fun duration_oneHourCourse_returns60() {
        val course = createCourse(startHour = 10, startMinute = 0, endHour = 11, endMinute = 0)
        assertEquals(60, course.duration)
    }

    @Test
    fun duration_sameStartAndEnd_returns0() {
        val course = createCourse(startHour = 8, startMinute = 0, endHour = 8, endMinute = 0)
        assertEquals(0, course.duration)
    }

    @Test
    fun duration_endBeforeStart_returns0_notNegative() {
        val course = createCourse(startHour = 10, startMinute = 0, endHour = 8, endMinute = 0)
        assertEquals("Duration should be coerced to 0 when end < start", 0, course.duration)
    }

    @Test
    fun duration_endBeforeStartByMinutes_returns0_notNegative() {
        val course = createCourse(startHour = 8, startMinute = 30, endHour = 8, endMinute = 0)
        assertEquals("Duration should be 0 when end time is before start time", 0, course.duration)
    }

    @Test
    fun duration_45minuteCourse_returns45() {
        val course = createCourse(startHour = 8, startMinute = 0, endHour = 8, endMinute = 45)
        assertEquals(45, course.duration)
    }

    @Test
    fun getRemainingMinutes_duringCourse_returnsCorrect() {
        val course = createCourse(startHour = 8, startMinute = 0, endHour = 9, endMinute = 30)
        assertEquals(60, course.getRemainingMinutes(8, 30))
    }

    @Test
    fun getRemainingMinutes_atCourseEnd_returns0() {
        val course = createCourse(startHour = 8, startMinute = 0, endHour = 9, endMinute = 30)
        assertEquals(0, course.getRemainingMinutes(9, 30))
    }

    @Test
    fun getRemainingMinutes_afterCourseEnd_returns0() {
        val course = createCourse(startHour = 8, startMinute = 0, endHour = 9, endMinute = 30)
        assertEquals(0, course.getRemainingMinutes(10, 0))
    }

    @Test
    fun hasEnded_afterCourseEnd_returnsTrue() {
        val course = createCourse(startHour = 8, startMinute = 0, endHour = 9, endMinute = 30)
        assertEquals(true, course.hasEnded(10, 0))
    }

    @Test
    fun hasEnded_duringCourse_returnsFalse() {
        val course = createCourse(startHour = 8, startMinute = 0, endHour = 9, endMinute = 30)
        assertEquals(false, course.hasEnded(8, 30))
    }

    @Test
    fun isOngoing_duringCourse_returnsTrue() {
        val course = createCourse(startHour = 8, startMinute = 0, endHour = 9, endMinute = 30)
        assertEquals(true, course.isOngoing(8, 30, 1))
    }

    @Test
    fun isOngoing_beforeCourse_returnsFalse() {
        val course = createCourse(startHour = 8, startMinute = 0, endHour = 9, endMinute = 30)
        assertEquals(false, course.isOngoing(7, 30, 1))
    }

    @Test
    fun isOngoing_afterCourse_returnsFalse() {
        val course = createCourse(startHour = 8, startMinute = 0, endHour = 9, endMinute = 30)
        assertEquals(false, course.isOngoing(9, 30, 1))
    }

    @Test
    fun course_personTypeA_isSelf() {
        val course = createCourse(personType = PersonType.PERSON_A)
        assertEquals(PersonType.PERSON_A, course.personType)
    }

    @Test
    fun course_personTypeB_isOther() {
        val course = createCourse(personType = PersonType.PERSON_B)
        assertEquals(PersonType.PERSON_B, course.personType)
    }

    private fun createCourse(
        startHour: Int = 8,
        startMinute: Int = 0,
        endHour: Int = 9,
        endMinute: Int = 30,
        personType: PersonType = PersonType.PERSON_A
    ) = Course(
        name = "测试课程",
        dayOfWeek = 1,
        startHour = startHour,
        startMinute = startMinute,
        endHour = endHour,
        endMinute = endMinute,
        weekType = WeekType.ALL,
        personType = personType
    )
}
