package com.duoschedule.data.model

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Immutable
@Entity(tableName = "todo_tags")
data class TodoTag(
    @PrimaryKey
    val id: String,  // 自定义标签用 UUID，预设标签用预设名称
    val name: String,
    val color: Long,
    val isPreset: Boolean = false
)
