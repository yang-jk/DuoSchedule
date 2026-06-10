package com.duoschedule.data.local

import androidx.room.*
import com.duoschedule.data.model.TodoTag
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoTagDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: TodoTag): Long

    @Update
    suspend fun updateTag(tag: TodoTag)

    @Query("DELETE FROM todo_tags WHERE id = :id")
    suspend fun deleteTagById(id: String)

    @Query("SELECT * FROM todo_tags WHERE id = :id")
    suspend fun getTagById(id: String): TodoTag?

    @Query("SELECT * FROM todo_tags ORDER BY isPreset DESC, name")
    fun getAllTags(): Flow<List<TodoTag>>

    @Query("SELECT * FROM todo_tags")
    suspend fun getAllTagsSync(): List<TodoTag>

    @Query("SELECT * FROM todo_tags WHERE isPreset = 1 ORDER BY name")
    fun getPresetTags(): Flow<List<TodoTag>>

    @Query("DELETE FROM todo_tags")
    suspend fun deleteAllTags()
}
