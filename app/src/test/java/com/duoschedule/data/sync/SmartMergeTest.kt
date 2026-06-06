package com.duoschedule.data.sync

import org.junit.Assert.*
import org.junit.Test

class SmartMergeTest {

    //region 辅助方法

    private fun makeCourse(
        id: Long = 0,
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
        syncId: String = "local-sync-$id",
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

    //endregion

    //region 场景1：相同课程内容匹配 → 复用云端 syncId

    @Test
    fun sameContent_reusesCloudSyncId() {
        val local = listOf(
            makeCourse(id = 1, name = "高等数学", syncId = "local-1", ownerProfileId = "profile-a")
        )
        val cloud = listOf(
            makeCourse(id = 10, name = "高等数学", syncId = "cloud-10", ownerProfileId = "profile-a")
        )

        val result = SmartMergeHelper.smartMergeLogic(local, cloud, "profile-a", null)

        assertEquals(1, result.size)
        assertEquals("cloud-10", result[0].syncId)
        assertEquals("高等数学", result[0].name)
    }

    //endregion

    //region 场景2：本地独有课程 → 保留本地 syncId

    @Test
    fun localOnlyCourse_keepsLocalSyncId() {
        val local = listOf(
            makeCourse(id = 1, name = "线性代数", syncId = "local-1", ownerProfileId = "profile-a")
        )
        val cloud = emptyList<CloudCourse>()

        val result = SmartMergeHelper.smartMergeLogic(local, cloud, "profile-a", null)

        assertEquals(1, result.size)
        assertEquals("local-1", result[0].syncId)
        assertEquals("线性代数", result[0].name)
    }

    //endregion

    //region 场景3：云端独有课程 → 正常保留

    @Test
    fun cloudOnlyCourse_isKept() {
        val local = emptyList<CloudCourse>()
        val cloud = listOf(
            makeCourse(id = 10, name = "大学物理", syncId = "cloud-10", ownerProfileId = "profile-a")
        )

        val result = SmartMergeHelper.smartMergeLogic(local, cloud, "profile-a", null)

        assertEquals(1, result.size)
        assertEquals("cloud-10", result[0].syncId)
        assertEquals("大学物理", result[0].name)
    }

    //endregion

    //region 场景4：内容有差异 → 两版本都保留

    @Test
    fun differentContent_bothVersionsKept() {
        val local = listOf(
            makeCourse(id = 1, name = "高等数学", location = "A101", syncId = "local-1", ownerProfileId = "profile-a")
        )
        val cloud = listOf(
            makeCourse(id = 10, name = "高等数学", location = "B202", syncId = "cloud-10", ownerProfileId = "profile-a")
        )

        val result = SmartMergeHelper.smartMergeLogic(local, cloud, "profile-a", null)

        assertEquals(2, result.size)
        val syncIds = result.map { it.syncId }
        assertTrue(syncIds.contains("local-1"))
        assertTrue(syncIds.contains("cloud-10"))
    }

    //endregion

    //region 场景5：同一 ownerProfileId 下多门课程的一对一匹配

    @Test
    fun multipleCourses_partialMatch() {
        val local = listOf(
            makeCourse(id = 1, name = "高等数学", syncId = "local-1", ownerProfileId = "profile-a"),
            makeCourse(id = 2, name = "线性代数", syncId = "local-2", ownerProfileId = "profile-a"),
            makeCourse(id = 3, name = "概率论", syncId = "local-3", ownerProfileId = "profile-a")
        )
        val cloud = listOf(
            makeCourse(id = 10, name = "高等数学", syncId = "cloud-10", ownerProfileId = "profile-a"),
            makeCourse(id = 11, name = "线性代数", syncId = "cloud-11", ownerProfileId = "profile-a"),
            makeCourse(id = 12, name = "大学物理", syncId = "cloud-12", ownerProfileId = "profile-a")
        )

        val result = SmartMergeHelper.smartMergeLogic(local, cloud, "profile-a", null)

        // 高等数学和线性代数匹配，概率论本地独有，大学物理云端独有
        assertEquals(4, result.size)

        val byName = result.groupBy { it.name }
        // 高等数学：匹配，使用云端 syncId
        assertEquals(1, byName["高等数学"]!!.size)
        assertEquals("cloud-10", byName["高等数学"]!![0].syncId)
        // 线性代数：匹配，使用云端 syncId
        assertEquals(1, byName["线性代数"]!!.size)
        assertEquals("cloud-11", byName["线性代数"]!![0].syncId)
        // 概率论：本地独有，保留本地 syncId
        assertEquals(1, byName["概率论"]!!.size)
        assertEquals("local-3", byName["概率论"]!![0].syncId)
        // 大学物理：云端独有
        assertEquals(1, byName["大学物理"]!!.size)
        assertEquals("cloud-12", byName["大学物理"]!![0].syncId)
    }

    //endregion

    //region 场景6：不同 ownerProfileId 的课程不交叉匹配

    @Test
    fun differentProfiles_noCrossMatch() {
        // profile-a 的本地课程和 profile-b 的云端课程内容相同
        val local = listOf(
            makeCourse(id = 1, name = "高等数学", syncId = "local-1", ownerProfileId = "profile-a")
        )
        val cloud = listOf(
            makeCourse(id = 10, name = "高等数学", syncId = "cloud-10", ownerProfileId = "profile-b")
        )

        val result = SmartMergeHelper.smartMergeLogic(local, cloud, "profile-a", "profile-b")

        // 两门课程都应保留，不应合并
        assertEquals(2, result.size)
        val syncIds = result.map { it.syncId }
        assertTrue(syncIds.contains("local-1"))
        assertTrue(syncIds.contains("cloud-10"))
    }

    //endregion

    //region 场景7：contentMatchKey 一致性

    @Test
    fun contentMatchKey_sameContent_producesSameKey() {
        val course1 = makeCourse(id = 1, name = "高等数学", location = "A101", teacher = "张老师",
            dayOfWeek = 1, startHour = 8, startMinute = 0, endHour = 9, endMinute = 40,
            weekType = "ALL", startWeek = 1, endWeek = 16, customWeeks = "",
            startPeriod = 1, endPeriod = 2, isCustomTime = false,
            syncId = "sync-a", ownerProfileId = "profile-a")
        val course2 = makeCourse(id = 2, name = "高等数学", location = "A101", teacher = "张老师",
            dayOfWeek = 1, startHour = 8, startMinute = 0, endHour = 9, endMinute = 40,
            weekType = "ALL", startWeek = 1, endWeek = 16, customWeeks = "",
            startPeriod = 1, endPeriod = 2, isCustomTime = false,
            syncId = "sync-b", ownerProfileId = "profile-b")

        assertEquals(
            SmartMergeHelper.contentMatchKey(course1),
            SmartMergeHelper.contentMatchKey(course2)
        )
    }

    @Test
    fun contentMatchKey_differentField_producesDifferentKey() {
        val base = makeCourse(name = "高等数学", location = "A101", teacher = "张老师",
            dayOfWeek = 1, startHour = 8, startMinute = 0, endHour = 9, endMinute = 40,
            weekType = "ALL", startWeek = 1, endWeek = 16, customWeeks = "",
            startPeriod = 1, endPeriod = 2, isCustomTime = false)

        // 修改 name
        val diffName = base.copy(name = "线性代数")
        assertNotEquals(
            SmartMergeHelper.contentMatchKey(base),
            SmartMergeHelper.contentMatchKey(diffName)
        )

        // 修改 location
        val diffLocation = base.copy(location = "B202")
        assertNotEquals(
            SmartMergeHelper.contentMatchKey(base),
            SmartMergeHelper.contentMatchKey(diffLocation)
        )

        // 修改 teacher
        val diffTeacher = base.copy(teacher = "李老师")
        assertNotEquals(
            SmartMergeHelper.contentMatchKey(base),
            SmartMergeHelper.contentMatchKey(diffTeacher)
        )

        // 修改 dayOfWeek
        val diffDay = base.copy(dayOfWeek = 2)
        assertNotEquals(
            SmartMergeHelper.contentMatchKey(base),
            SmartMergeHelper.contentMatchKey(diffDay)
        )

        // 修改 startHour
        val diffStartHour = base.copy(startHour = 10)
        assertNotEquals(
            SmartMergeHelper.contentMatchKey(base),
            SmartMergeHelper.contentMatchKey(diffStartHour)
        )

        // 修改 startMinute
        val diffStartMinute = base.copy(startMinute = 30)
        assertNotEquals(
            SmartMergeHelper.contentMatchKey(base),
            SmartMergeHelper.contentMatchKey(diffStartMinute)
        )

        // 修改 endHour
        val diffEndHour = base.copy(endHour = 11)
        assertNotEquals(
            SmartMergeHelper.contentMatchKey(base),
            SmartMergeHelper.contentMatchKey(diffEndHour)
        )

        // 修改 endMinute
        val diffEndMinute = base.copy(endMinute = 50)
        assertNotEquals(
            SmartMergeHelper.contentMatchKey(base),
            SmartMergeHelper.contentMatchKey(diffEndMinute)
        )

        // 修改 weekType
        val diffWeekType = base.copy(weekType = "ODD")
        assertNotEquals(
            SmartMergeHelper.contentMatchKey(base),
            SmartMergeHelper.contentMatchKey(diffWeekType)
        )

        // 修改 startWeek
        val diffStartWeek = base.copy(startWeek = 2)
        assertNotEquals(
            SmartMergeHelper.contentMatchKey(base),
            SmartMergeHelper.contentMatchKey(diffStartWeek)
        )

        // 修改 endWeek
        val diffEndWeek = base.copy(endWeek = 18)
        assertNotEquals(
            SmartMergeHelper.contentMatchKey(base),
            SmartMergeHelper.contentMatchKey(diffEndWeek)
        )

        // 修改 customWeeks
        val diffCustomWeeks = base.copy(customWeeks = "1,3,5")
        assertNotEquals(
            SmartMergeHelper.contentMatchKey(base),
            SmartMergeHelper.contentMatchKey(diffCustomWeeks)
        )

        // 修改 startPeriod
        val diffStartPeriod = base.copy(startPeriod = 3)
        assertNotEquals(
            SmartMergeHelper.contentMatchKey(base),
            SmartMergeHelper.contentMatchKey(diffStartPeriod)
        )

        // 修改 endPeriod
        val diffEndPeriod = base.copy(endPeriod = 4)
        assertNotEquals(
            SmartMergeHelper.contentMatchKey(base),
            SmartMergeHelper.contentMatchKey(diffEndPeriod)
        )

        // 修改 isCustomTime
        val diffIsCustomTime = base.copy(isCustomTime = true)
        assertNotEquals(
            SmartMergeHelper.contentMatchKey(base),
            SmartMergeHelper.contentMatchKey(diffIsCustomTime)
        )
    }

    //endregion
}
