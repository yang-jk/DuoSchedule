package com.duoschedule.data.sync

import org.junit.Assert.*
import org.junit.Test

class TodoSmartMergeTest {

    //region 辅助方法

    private fun makeTodo(
        syncId: String = "local-sync-0",
        ownerProfileId: String = "profile-a",
        title: String = "买书",
        description: String = "",
        date: Long = 20260609,
        startHour: Int = 8,
        startMinute: Int = 0,
        endHour: Int = 9,
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

    //region 场景1：相同内容匹配 → 复用云端 syncId

    @Test
    fun sameContent_reusesCloudSyncId() {
        val local = listOf(
            makeTodo(syncId = "local-1", ownerProfileId = "profile-a", title = "买书",
                date = 20260609, startHour = 8, startMinute = 0, endHour = 9, endMinute = 0)
        )
        val cloud = listOf(
            makeTodo(syncId = "cloud-10", ownerProfileId = "profile-a", title = "买书",
                date = 20260609, startHour = 8, startMinute = 0, endHour = 9, endMinute = 0)
        )

        val result = SmartMergeTodoHelper.smartMergeTodosLogic(local, cloud, "profile-a", null)

        assertEquals(1, result.mergedTodos.size)
        assertEquals("cloud-10", result.mergedTodos[0].syncId)
        assertEquals("买书", result.mergedTodos[0].title)
    }

    //endregion

    //region 场景2：本地独有 Todo → 保留本地 syncId

    @Test
    fun localOnlyTodo_keepsLocalSyncId() {
        val local = listOf(
            makeTodo(syncId = "local-1", ownerProfileId = "profile-a", title = "还书")
        )
        val cloud = emptyList<CloudTodo>()

        val result = SmartMergeTodoHelper.smartMergeTodosLogic(local, cloud, "profile-a", null)

        assertEquals(1, result.mergedTodos.size)
        assertEquals("local-1", result.mergedTodos[0].syncId)
        assertEquals("还书", result.mergedTodos[0].title)
    }

    //endregion

    //region 场景3：云端独有 Todo → 正常保留

    @Test
    fun cloudOnlyTodo_isKept() {
        val local = emptyList<CloudTodo>()
        val cloud = listOf(
            makeTodo(syncId = "cloud-10", ownerProfileId = "profile-a", title = "交作业")
        )

        val result = SmartMergeTodoHelper.smartMergeTodosLogic(local, cloud, "profile-a", null)

        assertEquals(1, result.mergedTodos.size)
        assertEquals("cloud-10", result.mergedTodos[0].syncId)
        assertEquals("交作业", result.mergedTodos[0].title)
    }

    //endregion

    //region 场景4：内容有差异 → 两版本都保留

    @Test
    fun differentContent_bothVersionsKept() {
        val local = listOf(
            makeTodo(syncId = "local-1", ownerProfileId = "profile-a", title = "买书",
                date = 20260609, startHour = 8, startMinute = 0, endHour = 9, endMinute = 0)
        )
        val cloud = listOf(
            makeTodo(syncId = "cloud-10", ownerProfileId = "profile-a", title = "买书",
                date = 20260609, startHour = 10, startMinute = 0, endHour = 11, endMinute = 0)
        )

        val result = SmartMergeTodoHelper.smartMergeTodosLogic(local, cloud, "profile-a", null)

        assertEquals(2, result.mergedTodos.size)
        val syncIds = result.mergedTodos.map { it.syncId }
        assertTrue(syncIds.contains("local-1"))
        assertTrue(syncIds.contains("cloud-10"))
    }

    //endregion

    //region 场景5：不同 ownerProfileId 的 Todo 不交叉匹配

    @Test
    fun differentProfiles_noCrossMatch() {
        // profile-a 的本地 Todo 和 profile-b 的云端 Todo 内容相同
        val local = listOf(
            makeTodo(syncId = "local-1", ownerProfileId = "profile-a", title = "买书",
                date = 20260609, startHour = 8, startMinute = 0, endHour = 9, endMinute = 0)
        )
        val cloud = listOf(
            makeTodo(syncId = "cloud-10", ownerProfileId = "profile-b", title = "买书",
                date = 20260609, startHour = 8, startMinute = 0, endHour = 9, endMinute = 0)
        )

        val result = SmartMergeTodoHelper.smartMergeTodosLogic(local, cloud, "profile-a", "profile-b")

        // 两个 Todo 都应保留，不应合并
        assertEquals(2, result.mergedTodos.size)
        val syncIds = result.mergedTodos.map { it.syncId }
        assertTrue(syncIds.contains("local-1"))
        assertTrue(syncIds.contains("cloud-10"))
    }

    //endregion

    //region 场景6：todoContentMatchKey 一致性

    @Test
    fun todoContentMatchKey_sameContent_producesSameKey() {
        val todo1 = makeTodo(syncId = "sync-a", ownerProfileId = "profile-a", title = "买书",
            date = 20260609, startHour = 8, startMinute = 0, endHour = 9, endMinute = 0)
        val todo2 = makeTodo(syncId = "sync-b", ownerProfileId = "profile-b", title = "买书",
            date = 20260609, startHour = 8, startMinute = 0, endHour = 9, endMinute = 0)

        assertEquals(
            SmartMergeTodoHelper.todoContentMatchKey(todo1),
            SmartMergeTodoHelper.todoContentMatchKey(todo2)
        )
    }

    @Test
    fun todoContentMatchKey_differentField_producesDifferentKey() {
        val base = makeTodo(title = "买书", date = 20260609,
            startHour = 8, startMinute = 0, endHour = 9, endMinute = 0)

        // 修改 title
        val diffTitle = base.copy(title = "还书")
        assertNotEquals(
            SmartMergeTodoHelper.todoContentMatchKey(base),
            SmartMergeTodoHelper.todoContentMatchKey(diffTitle)
        )

        // 修改 date
        val diffDate = base.copy(date = 20260610)
        assertNotEquals(
            SmartMergeTodoHelper.todoContentMatchKey(base),
            SmartMergeTodoHelper.todoContentMatchKey(diffDate)
        )

        // 修改 startHour
        val diffStartHour = base.copy(startHour = 10)
        assertNotEquals(
            SmartMergeTodoHelper.todoContentMatchKey(base),
            SmartMergeTodoHelper.todoContentMatchKey(diffStartHour)
        )

        // 修改 startMinute
        val diffStartMinute = base.copy(startMinute = 30)
        assertNotEquals(
            SmartMergeTodoHelper.todoContentMatchKey(base),
            SmartMergeTodoHelper.todoContentMatchKey(diffStartMinute)
        )

        // 修改 endHour
        val diffEndHour = base.copy(endHour = 11)
        assertNotEquals(
            SmartMergeTodoHelper.todoContentMatchKey(base),
            SmartMergeTodoHelper.todoContentMatchKey(diffEndHour)
        )

        // 修改 endMinute
        val diffEndMinute = base.copy(endMinute = 50)
        assertNotEquals(
            SmartMergeTodoHelper.todoContentMatchKey(base),
            SmartMergeTodoHelper.todoContentMatchKey(diffEndMinute)
        )
    }

    //endregion
}
