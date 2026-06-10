package com.duoschedule.data.model

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

/** 重复频率 */
enum class RepeatFrequency {
    DAILY,  // 每天
    WEEKLY, // 每周
    CUSTOM  // 自定义
}

@Immutable
@Entity(tableName = "repeat_rules")
data class RepeatRule(
    @PrimaryKey
    val id: String,
    val frequency: RepeatFrequency,
    val interval: Int = 1,  // 间隔，如每2天=2
    val daysOfWeek: String = "",  // 逗号分隔的周几，WEEKLY 用，如 "1,3,5"
    val customDates: String = "",  // 逗号分隔的 epoch day，CUSTOM 用
    val endDate: Long? = null  // 结束日期 epoch day，null 表示永久
)
