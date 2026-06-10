package com.duoschedule.data.local

import androidx.room.*
import com.duoschedule.data.model.PersonType
import com.duoschedule.data.model.Todo
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodo(todo: Todo): Long

    @Update
    suspend fun updateTodo(todo: Todo)

    @Query("DELETE FROM todos WHERE id = :id")
    suspend fun deleteTodoById(id: Long)

    @Query("DELETE FROM todos WHERE syncId = :syncId")
    suspend fun deleteTodoBySyncId(syncId: String)

    @Query("SELECT * FROM todos WHERE id = :id")
    suspend fun getTodoById(id: Long): Todo?

    @Query("SELECT * FROM todos WHERE syncId = :syncId")
    suspend fun getTodoBySyncId(syncId: String): Todo?

    /** 获取所有待办（同步用，非 Flow） */
    @Query("SELECT * FROM todos")
    suspend fun getAllTodosSync(): List<Todo>

    /** 获取所有待办 */
    @Query("SELECT * FROM todos ORDER BY date, startHour, startMinute")
    fun getAllTodos(): Flow<List<Todo>>

    @Query("SELECT * FROM todos WHERE date = :date ORDER BY startHour, startMinute")
    fun getTodosByDate(date: Long): Flow<List<Todo>>

    @Query("SELECT * FROM todos WHERE personType = :personType AND date = :date ORDER BY startHour, startMinute")
    fun getTodosByPersonAndDate(personType: PersonType, date: Long): Flow<List<Todo>>

    @Query("SELECT * FROM todos WHERE personType = :personType ORDER BY date, startHour, startMinute")
    fun getTodosByPerson(personType: PersonType): Flow<List<Todo>>

    @Query("SELECT * FROM todos WHERE repeatRuleId = :repeatRuleId")
    fun getTodosByRepeatRuleId(repeatRuleId: String): Flow<List<Todo>>

    /** 获取指定重复规则的所有待办（同步用，非 Flow） */
    @Query("SELECT * FROM todos WHERE repeatRuleId = :repeatRuleId")
    suspend fun getTodosByRepeatRuleIdSync(repeatRuleId: String): List<Todo>

    /** 获取所有有重复规则的待办（同步用） */
    @Query("SELECT * FROM todos WHERE repeatRuleId IS NOT NULL")
    suspend fun getTodosWithRepeatRule(): List<Todo>

    /** 检查指定重复规则+日期+人物是否已存在待办 */
    @Query("SELECT * FROM todos WHERE repeatRuleId = :repeatRuleId AND date = :date AND personType = :personType LIMIT 1")
    suspend fun getTodoByRepeatRuleIdAndDateAndPerson(repeatRuleId: String, date: Long, personType: PersonType): Todo?

    @Query("SELECT * FROM todos WHERE personType = :personType AND date = :date AND status = 'PENDING' ORDER BY startHour, startMinute")
    fun getPendingTodosByPersonAndDate(personType: PersonType, date: Long): Flow<List<Todo>>

    /** 获取指定人物在日期范围内的待办 */
    @Query("SELECT * FROM todos WHERE personType = :personType AND date >= :startEpochDay AND date <= :endEpochDay ORDER BY date, startHour, startMinute")
    fun getTodosByPersonAndDateRange(personType: PersonType, startEpochDay: Long, endEpochDay: Long): Flow<List<Todo>>

    /** 获取所有未完成且有时间的待办（用于闹钟调度） */
    @Query("SELECT * FROM todos WHERE status = 'PENDING' AND (startHour >= 0 OR endHour >= 0)")
    suspend fun getPendingTodosWithTime(): List<Todo>

    @Query("DELETE FROM todos")
    suspend fun deleteAllTodos()
}
