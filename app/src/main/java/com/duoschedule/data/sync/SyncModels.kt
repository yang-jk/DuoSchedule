package com.duoschedule.data.sync

import com.duoschedule.data.model.Course
import com.duoschedule.data.model.PersonType
import com.duoschedule.data.model.Priority
import com.duoschedule.data.model.Todo
import com.duoschedule.data.model.TodoStatus
import com.duoschedule.data.model.WeekType

data class SyncConfig(
    val webDavUrl: String,
    val username: String,
    val password: String,
    val roomId: String,
    val deviceId: String,
    val inviteSenderProfileId: String? = null,
    val inviteReceiverProfileId: String? = null
)

data class CloudData(
    val roomId: String,
    val version: Long,
    val lastModified: String,
    val lastModifiedBy: String,
    val courses: List<CloudCourse>,
    val settingsA: CloudSettings?,
    val settingsB: CloudSettings?,
    val personAName: String,
    val personBName: String,
    val schemaVersion: Int = 1,
    val profiles: List<CloudProfile> = emptyList(),
    val profileSettings: Map<String, CloudSettings> = emptyMap(),
    // Todo 同步数据（schema v3 新增）
    val todos: List<CloudTodo> = emptyList(),
    val todoTags: List<CloudTodoTag> = emptyList(),
    val repeatRules: List<CloudRepeatRule> = emptyList()
)

data class CloudCourse(
    val id: Long,
    val name: String,
    val location: String,
    val teacher: String,
    val dayOfWeek: Int,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val weekType: String,
    val startWeek: Int,
    val endWeek: Int,
    val customWeeks: String,
    val personType: String,
    val startPeriod: Int,
    val endPeriod: Int,
    val isCustomTime: Boolean,
    val syncId: String = "",
    val ownerProfileId: String = ""
)

data class CloudProfile(
    val id: String,
    val name: String
)

data class JoinRoomInfo(
    val roomCode: String,
    val config: SyncConfig,
    val profileA: CloudProfile,
    val profileB: CloudProfile,
    val personAName: String,
    val personBName: String
)

data class CloudSettings(
    val semesterStartDate: Long,
    val totalWeeks: Int,
    val currentWeek: Int,
    val totalPeriods: Int,
    val periodTimes: List<String>
)

data class CloudMeta(
    val roomId: String,
    val createdAt: String,
    val createdBy: String,
    val members: List<String>,
    val currentVersion: Long
)

enum class SyncState {
    IDLE,
    SYNCING,
    SYNCED,
    ERROR,
    CONFLICT,
    DISABLED
}

data class SyncStatus(
    val state: SyncState = SyncState.DISABLED,
    val lastSyncTime: Long = 0,
    val lastSyncVersion: Long = 0,
    val errorMessage: String? = null
)

fun Course.toCloudCourse(): CloudCourse {
    return CloudCourse(
        id = id,
        name = name,
        location = location,
        teacher = teacher,
        dayOfWeek = dayOfWeek,
        startHour = startHour,
        startMinute = startMinute,
        endHour = endHour,
        endMinute = endMinute,
        weekType = weekType.name,
        startWeek = startWeek,
        endWeek = endWeek,
        customWeeks = customWeeks,
        personType = personType.name,
        startPeriod = startPeriod,
        endPeriod = endPeriod,
        isCustomTime = isCustomTime,
        syncId = syncId
    )
}

fun Course.toCloudCourse(ownerProfileId: String): CloudCourse {
    return toCloudCourse().copy(ownerProfileId = ownerProfileId, syncId = syncId)
}

fun CloudCourse.toCourse(personTypeOverride: PersonType? = null): Course {
    return Course(
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
        weekType = try { WeekType.valueOf(weekType) } catch (_: Exception) { WeekType.ALL },
        startWeek = startWeek,
        endWeek = endWeek,
        customWeeks = customWeeks,
        personType = personTypeOverride ?: try { PersonType.valueOf(personType) } catch (_: Exception) { PersonType.PERSON_A },
        startPeriod = startPeriod,
        endPeriod = endPeriod,
        isCustomTime = isCustomTime
    )
}

// ========== Todo 同步模型（schema v3） ==========

/**
 * 云端 Todo 数据模型
 * 与 CloudCourse 类似，通过 ownerProfileId 区分归属
 */
data class CloudTodo(
    val syncId: String,
    val ownerProfileId: String,
    val title: String,
    val description: String = "",
    val date: Long,
    val startHour: Int = -1,
    val startMinute: Int = -1,
    val endHour: Int = -1,
    val endMinute: Int = -1,
    val priority: String = "MEDIUM",
    val status: String = "PENDING",
    val tags: String = "",
    val linkedCourseSyncId: String? = null,
    val repeatRuleId: String? = null,
    val completedAt: Long? = null
)

/**
 * 云端 Todo 标签数据模型
 */
data class CloudTodoTag(
    val id: String,
    val name: String,
    val color: Long,
    val isPreset: Boolean = false
)

/**
 * 云端重复规则数据模型
 */
data class CloudRepeatRule(
    val id: String,
    val frequency: String,  // "DAILY", "WEEKLY", "CUSTOM"
    val interval: Int = 1,
    val daysOfWeek: String = "",
    val customDates: String = "",
    val endDate: Long? = null
)

// ========== Todo 转换函数 ==========

/** 本地 Todo → 云端 CloudTodo */
fun Todo.toCloudTodo(ownerProfileId: String): CloudTodo {
    return CloudTodo(
        syncId = syncId,
        ownerProfileId = ownerProfileId,
        title = title,
        description = description,
        date = date,
        startHour = startHour,
        startMinute = startMinute,
        endHour = endHour,
        endMinute = endMinute,
        priority = priority.name,
        status = status.name,
        tags = tags,
        linkedCourseSyncId = linkedCourseSyncId,
        repeatRuleId = repeatRuleId,
        completedAt = completedAt
    )
}

/** 云端 CloudTodo → 本地 Todo */
fun CloudTodo.toTodo(personTypeOverride: PersonType? = null): Todo {
    return Todo(
        syncId = syncId,
        title = title,
        description = description,
        personType = personTypeOverride ?: PersonType.PERSON_A,
        date = date,
        startHour = startHour,
        startMinute = startMinute,
        endHour = endHour,
        endMinute = endMinute,
        priority = try { Priority.valueOf(priority) } catch (_: Exception) { Priority.MEDIUM },
        status = try { TodoStatus.valueOf(status) } catch (_: Exception) { TodoStatus.PENDING },
        tags = tags,
        linkedCourseSyncId = linkedCourseSyncId,
        repeatRuleId = repeatRuleId,
        completedAt = completedAt
    )
}
