package com.duoschedule.data.model

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Locale
import java.util.UUID

@Immutable
@Entity(
    tableName = "todos",
    indices = [
        Index(value = ["personType"]),
        Index(value = ["date"]),
        Index(value = ["syncId"], unique = true),
        Index(value = ["personType", "date"]),
        Index(value = ["repeatRuleId"])
    ]
)
data class Todo(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val syncId: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val personType: PersonType,
    val date: Long,  // epoch day，必填
    val startHour: Int = -1,  // -1 表示未设置
    val startMinute: Int = -1,
    val endHour: Int = -1,  // -1 表示未设置
    val endMinute: Int = -1,
    val priority: Priority = Priority.MEDIUM,
    val status: TodoStatus = TodoStatus.PENDING,
    val tags: String = "",  // 逗号分隔的标签 ID
    val linkedCourseSyncId: String? = null,
    val repeatRuleId: String? = null,
    val completedAt: Long? = null  // 完成时间戳
) {
    /** 是否有开始时间 */
    fun hasStartTime(): Boolean = startHour >= 0

    /** 是否有结束时间 */
    fun hasEndTime(): Boolean = endHour >= 0

    /** 是否有时间段（开始+结束） */
    fun hasTimeRange(): Boolean = hasStartTime() && hasEndTime()

    /** 是否仅有截止时间（无开始时间，有结束时间） */
    fun isDeadlineOnly(): Boolean = !hasStartTime() && hasEndTime()

    /** 获取开始时间字符串，如 "14:00" */
    fun getStartTimeString(): String {
        return if (hasStartTime()) {
            String.format(Locale.ROOT, "%02d:%02d", startHour, startMinute)
        } else ""
    }

    /** 获取结束时间字符串，如 "16:00" */
    fun getEndTimeString(): String {
        return if (hasEndTime()) {
            String.format(Locale.ROOT, "%02d:%02d", endHour, endMinute)
        } else ""
    }

    /** 获取时间范围字符串，如 "14:00-16:00" */
    fun getTimeString(): String {
        return when {
            hasTimeRange() -> "${getStartTimeString()}-${getEndTimeString()}"
            isDeadlineOnly() -> "${getEndTimeString()}前"
            hasStartTime() -> getStartTimeString()
            else -> ""
        }
    }
}
