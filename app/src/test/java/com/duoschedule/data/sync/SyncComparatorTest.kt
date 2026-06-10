package com.duoschedule.data.sync

import com.duoschedule.data.model.Course
import com.duoschedule.data.model.PersonType
import com.duoschedule.data.model.Priority
import com.duoschedule.data.model.Todo
import com.duoschedule.data.model.TodoStatus
import com.duoschedule.data.model.WeekType
import org.junit.Assert.*
import org.junit.Test

class SyncComparatorTest {

    //region 辅助方法

    private fun makeCourse(
        id: Long = 1,
        syncId: String = "sync-1",
        name: String = "高等数学",
        location: String = "A101",
        teacher: String = "张老师",
        dayOfWeek: Int = 1,
        startHour: Int = 8,
        startMinute: Int = 0,
        endHour: Int = 9,
        endMinute: Int = 40,
        weekType: WeekType = WeekType.ALL,
        startWeek: Int = 1,
        endWeek: Int = 16,
        customWeeks: String = "",
        personType: PersonType = PersonType.PERSON_A,
        startPeriod: Int = 1,
        endPeriod: Int = 2,
        isCustomTime: Boolean = false
    ): Course = Course(
        id = id,
        syncId = syncId,
        name = name,
        location = location,
        teacher = teacher,
        dayOfWeek = dayOfWeek,
        startHour = startHour,
        startMinute = startMinute,
        endHour = endHour,
        endMinute = endMinute,
        weekType = weekType,
        startWeek = startWeek,
        endWeek = endWeek,
        customWeeks = customWeeks,
        personType = personType,
        startPeriod = startPeriod,
        endPeriod = endPeriod,
        isCustomTime = isCustomTime
    )

    private fun makeCloudCourse(
        id: Long = 1,
        name: String = "高等数学",
        location: String = "A101",
        teacher: String = "张老师",
        dayOfWeek: Int = 1,
        startHour: Int = 8,
        startMinute: Int = 0,
        endHour: Int = 9,
        endMinute: Int = 40,
        weekType: String = "ALL",
        startWeek: Int = 1,
        endWeek: Int = 16,
        customWeeks: String = "",
        personType: String = "PERSON_A",
        startPeriod: Int = 1,
        endPeriod: Int = 2,
        isCustomTime: Boolean = false,
        syncId: String = "sync-1",
        ownerProfileId: String = "profile-a"
    ): CloudCourse = CloudCourse(
        id = id,
        name = name,
        location = location,
        teacher = teacher,
        dayOfWeek = dayOfWeek,
        startHour = startHour,
        startMinute = startMinute,
        endHour = endHour,
        endMinute = endMinute,
        weekType = weekType,
        startWeek = startWeek,
        endWeek = endWeek,
        customWeeks = customWeeks,
        personType = personType,
        startPeriod = startPeriod,
        endPeriod = endPeriod,
        isCustomTime = isCustomTime,
        syncId = syncId,
        ownerProfileId = ownerProfileId
    )

    private fun makeTodo(
        id: Long = 1,
        syncId: String = "todo-sync-1",
        title: String = "完成作业",
        description: String = "数学作业",
        personType: PersonType = PersonType.PERSON_A,
        date: Long = 20000L,
        startHour: Int = 14,
        startMinute: Int = 0,
        endHour: Int = 16,
        endMinute: Int = 0,
        priority: Priority = Priority.MEDIUM,
        status: TodoStatus = TodoStatus.PENDING,
        tags: String = "",
        linkedCourseSyncId: String? = null,
        repeatRuleId: String? = null,
        completedAt: Long? = null
    ): Todo = Todo(
        id = id,
        syncId = syncId,
        title = title,
        description = description,
        personType = personType,
        date = date,
        startHour = startHour,
        startMinute = startMinute,
        endHour = endHour,
        endMinute = endMinute,
        priority = priority,
        status = status,
        tags = tags,
        linkedCourseSyncId = linkedCourseSyncId,
        repeatRuleId = repeatRuleId,
        completedAt = completedAt
    )

    private fun makeCloudTodo(
        syncId: String = "todo-sync-1",
        ownerProfileId: String = "profile-a",
        title: String = "完成作业",
        description: String = "数学作业",
        date: Long = 20000L,
        startHour: Int = 14,
        startMinute: Int = 0,
        endHour: Int = 16,
        endMinute: Int = 0,
        priority: String = "MEDIUM",
        status: String = "PENDING",
        tags: String = "",
        linkedCourseSyncId: String? = null,
        repeatRuleId: String? = null,
        completedAt: Long? = null
    ): CloudTodo = CloudTodo(
        syncId = syncId,
        ownerProfileId = ownerProfileId,
        title = title,
        description = description,
        date = date,
        startHour = startHour,
        startMinute = startMinute,
        endHour = endHour,
        endMinute = endMinute,
        priority = priority,
        status = status,
        tags = tags,
        linkedCourseSyncId = linkedCourseSyncId,
        repeatRuleId = repeatRuleId,
        completedAt = completedAt
    )

    //endregion

    //region courseContentEquals 测试

    @Test
    fun courseContentEquals_allFieldsMatch_returnsTrue() {
        val local = makeCourse()
        val cloud = makeCloudCourse()
        val cloudPersonType = PersonType.PERSON_A

        assertTrue(SyncComparatorHelper.courseContentEquals(local, cloud, cloudPersonType))
    }

    @Test
    fun courseContentEquals_nameDiffers_returnsFalse() {
        val local = makeCourse(name = "高等数学")
        val cloud = makeCloudCourse(name = "线性代数")
        val cloudPersonType = PersonType.PERSON_A

        assertFalse(SyncComparatorHelper.courseContentEquals(local, cloud, cloudPersonType))
    }

    @Test
    fun courseContentEquals_locationDiffers_returnsFalse() {
        val local = makeCourse(location = "A101")
        val cloud = makeCloudCourse(location = "B202")
        val cloudPersonType = PersonType.PERSON_A

        assertFalse(SyncComparatorHelper.courseContentEquals(local, cloud, cloudPersonType))
    }

    @Test
    fun courseContentEquals_personTypeDiffers_returnsFalse() {
        val local = makeCourse(personType = PersonType.PERSON_A)
        val cloud = makeCloudCourse(personType = "PERSON_A")
        val cloudPersonType = PersonType.PERSON_B

        assertFalse(SyncComparatorHelper.courseContentEquals(local, cloud, cloudPersonType))
    }

    @Test
    fun courseContentEquals_isCustomTimeDiffers_returnsFalse() {
        val local = makeCourse(isCustomTime = false)
        val cloud = makeCloudCourse(isCustomTime = true)
        val cloudPersonType = PersonType.PERSON_A

        assertFalse(SyncComparatorHelper.courseContentEquals(local, cloud, cloudPersonType))
    }

    //endregion

    //region todoContentEquals 测试

    @Test
    fun todoContentEquals_allFieldsMatch_returnsTrue() {
        val local = makeTodo()
        val cloud = makeCloudTodo()
        val cloudPersonType = PersonType.PERSON_A

        assertTrue(SyncComparatorHelper.todoContentEquals(local, cloud, cloudPersonType))
    }

    @Test
    fun todoContentEquals_titleDiffers_returnsFalse() {
        val local = makeTodo(title = "完成作业")
        val cloud = makeCloudTodo(title = "复习考试")
        val cloudPersonType = PersonType.PERSON_A

        assertFalse(SyncComparatorHelper.todoContentEquals(local, cloud, cloudPersonType))
    }

    @Test
    fun todoContentEquals_priorityDiffers_returnsFalse() {
        val local = makeTodo(priority = Priority.MEDIUM)
        val cloud = makeCloudTodo(priority = "HIGH")
        val cloudPersonType = PersonType.PERSON_A

        assertFalse(SyncComparatorHelper.todoContentEquals(local, cloud, cloudPersonType))
    }

    @Test
    fun todoContentEquals_statusDiffers_returnsFalse() {
        val local = makeTodo(status = TodoStatus.PENDING)
        val cloud = makeCloudTodo(status = "COMPLETED")
        val cloudPersonType = PersonType.PERSON_A

        assertFalse(SyncComparatorHelper.todoContentEquals(local, cloud, cloudPersonType))
    }

    @Test
    fun todoContentEquals_personTypeDiffers_returnsFalse() {
        val local = makeTodo(personType = PersonType.PERSON_A)
        val cloud = makeCloudTodo()
        val cloudPersonType = PersonType.PERSON_B

        assertFalse(SyncComparatorHelper.todoContentEquals(local, cloud, cloudPersonType))
    }

    //endregion
}
