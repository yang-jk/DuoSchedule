package com.duoschedule.data.sync

import com.duoschedule.data.model.Course
import com.duoschedule.data.model.PersonType
import com.duoschedule.data.model.Todo

/**
 * 纯逻辑辅助类，从 SyncManager 中提取内容比较逻辑，
 * 不依赖 Android 框架或数据库操作，便于单元测试。
 */
object SyncComparatorHelper {

    /**
     * 比较本地课程与云端课程的内容是否一致
     */
    fun courseContentEquals(local: Course, cloud: CloudCourse, cloudPersonType: PersonType): Boolean {
        return local.name == cloud.name &&
            local.location == cloud.location &&
            local.teacher == cloud.teacher &&
            local.dayOfWeek == cloud.dayOfWeek &&
            local.startHour == cloud.startHour &&
            local.startMinute == cloud.startMinute &&
            local.endHour == cloud.endHour &&
            local.endMinute == cloud.endMinute &&
            local.weekType.name == cloud.weekType &&
            local.startWeek == cloud.startWeek &&
            local.endWeek == cloud.endWeek &&
            local.customWeeks == cloud.customWeeks &&
            local.personType == cloudPersonType &&
            local.startPeriod == cloud.startPeriod &&
            local.endPeriod == cloud.endPeriod &&
            local.isCustomTime == cloud.isCustomTime
    }

    /**
     * 比较本地 Todo 与云端 CloudTodo 的内容是否一致
     */
    fun todoContentEquals(local: Todo, cloud: CloudTodo, cloudPersonType: PersonType): Boolean {
        return local.title == cloud.title &&
            local.description == cloud.description &&
            local.date == cloud.date &&
            local.startHour == cloud.startHour &&
            local.startMinute == cloud.startMinute &&
            local.endHour == cloud.endHour &&
            local.endMinute == cloud.endMinute &&
            local.priority.name == cloud.priority &&
            local.status.name == cloud.status &&
            local.tags == cloud.tags &&
            local.linkedCourseSyncId == cloud.linkedCourseSyncId &&
            local.repeatRuleId == cloud.repeatRuleId &&
            local.completedAt == cloud.completedAt &&
            local.personType == cloudPersonType
    }
}
