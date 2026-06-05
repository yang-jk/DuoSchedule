package com.duoschedule.data.sync

import com.duoschedule.data.model.Course
import com.duoschedule.data.model.PersonType
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
    val version: Int,
    val lastModified: String,
    val lastModifiedBy: String,
    val courses: List<CloudCourse>,
    val settingsA: CloudSettings?,
    val settingsB: CloudSettings?,
    val personAName: String,
    val personBName: String,
    val schemaVersion: Int = 1,
    val profiles: List<CloudProfile> = emptyList(),
    val profileSettings: Map<String, CloudSettings> = emptyMap()
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
    val currentVersion: Int
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
    val lastSyncVersion: Int = 0,
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
