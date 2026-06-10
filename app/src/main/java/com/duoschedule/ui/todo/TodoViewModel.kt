package com.duoschedule.ui.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duoschedule.data.local.SettingsDataStore
import com.duoschedule.data.model.PersonType
import com.duoschedule.data.model.Todo
import com.duoschedule.data.model.TodoTag
import com.duoschedule.data.repository.TodoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TodoViewModel @Inject constructor(
    private val todoRepository: TodoRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    /** 获取指定日期的待办 */
    fun getTodosByDate(date: Long): Flow<List<Todo>> =
        todoRepository.getTodosByDate(date)

    /** 获取指定人物在指定日期的待办 */
    fun getTodosByPersonAndDate(personType: PersonType, date: Long): Flow<List<Todo>> =
        todoRepository.getTodosByPersonAndDate(personType, date)

    /** 获取指定人物在指定日期的待完成待办 */
    fun getPendingTodosByPersonAndDate(personType: PersonType, date: Long): Flow<List<Todo>> =
        todoRepository.getPendingTodosByPersonAndDate(personType, date)

    /** 切换待办状态（PENDING ↔ COMPLETED） */
    fun toggleTodoStatus(id: Long) {
        viewModelScope.launch {
            todoRepository.toggleTodoStatus(id)
        }
    }

    /** 根据 ID 删除待办 */
    fun deleteTodo(id: Long) {
        viewModelScope.launch {
            todoRepository.deleteTodoById(id)
        }
    }

    /** 完成待办 */
    fun completeTodo(id: Long) {
        viewModelScope.launch {
            todoRepository.completeTodo(id)
        }
    }

    /** 取消完成待办 */
    fun uncompleteTodo(id: Long) {
        viewModelScope.launch {
            todoRepository.uncompleteTodo(id)
        }
    }

    /** 初始化预设标签 */
    fun initPresetTags() {
        viewModelScope.launch {
            todoRepository.initPresetTags()
        }
    }

    /** 获取所有标签 */
    fun getAllTags(): Flow<List<TodoTag>> =
        todoRepository.getAllTags()
}
