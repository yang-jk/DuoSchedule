package com.duoschedule.data.sync

sealed class SyncResult {
    data class Success(
        val pulledCourses: Int = 0,
        val pushedCourses: Int = 0,
        val pulledSettings: Boolean = false,
        val pushedSettings: Boolean = false
    ) : SyncResult()

    data class Conflict(
        val localVersion: Long,
        val cloudVersion: Long,
        val conflictItems: List<ConflictItem>
    ) : SyncResult()

    data class Error(
        val message: String,
        val exception: Exception? = null
    ) : SyncResult()

    object NoChanges : SyncResult()
    object NotConfigured : SyncResult()
}

data class ConflictItem(
    val courseName: String,
    val localVersion: CloudCourse?,
    val cloudVersion: CloudCourse?,
    val conflictType: ConflictType,
    val courseKey: String = localVersion?.syncId ?: cloudVersion?.syncId ?: courseName,
    // Todo 冲突数据
    val localTodoVersion: CloudTodo? = null,
    val cloudTodoVersion: CloudTodo? = null
)

enum class ConflictType {
    BOTH_MODIFIED,
    LOCAL_DELETED_CLOUD_MODIFIED,
    LOCAL_MODIFIED_CLOUD_DELETED,
    BOTH_DELETED
}

data class ConflictResolution(
    val resolutions: Map<String, ConflictChoice>
)

enum class ConflictChoice {
    KEEP_LOCAL,
    KEEP_CLOUD,
    KEEP_BOTH
}
