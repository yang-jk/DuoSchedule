package com.duoschedule.ui.main.components

import com.duoschedule.data.model.Course
import com.duoschedule.data.model.PersonType
import com.duoschedule.data.model.Todo

/**
 * 首页统一时间线条目
 */
sealed class TodayTimelineItem {
    /** 排序用的时间分钟数（0-1439），-1 表示无时间 */
    abstract val sortKey: Int

    /** 所属人物 */
    abstract val personType: PersonType

    /** 课程条目 */
    data class CourseItem(
        val course: Course,
        override val personType: PersonType = course.personType
    ) : TodayTimelineItem() {
        override val sortKey: Int = course.startHour * 60 + course.startMinute
    }

    /** 待办条目（有时间） */
    data class TimedTodoItem(
        val todo: Todo,
        override val personType: PersonType = todo.personType
    ) : TodayTimelineItem() {
        override val sortKey: Int = when {
            todo.hasStartTime() -> todo.startHour * 60 + todo.startMinute
            todo.hasEndTime() -> todo.endHour * 60 + todo.endMinute
            else -> -1
        }
        /** 时间标签文本 */
        val timeLabel: String = when {
            todo.hasStartTime() -> todo.getStartTimeString()
            todo.isDeadlineOnly() -> todo.getEndTimeString()
            else -> ""
        }
    }

    /** 待办条目（无时间） */
    data class UntimedTodoItem(
        val todo: Todo,
        override val personType: PersonType = todo.personType
    ) : TodayTimelineItem() {
        override val sortKey: Int = Int.MAX_VALUE  // 排在最后
    }
}
