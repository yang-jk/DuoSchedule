package com.duoschedule.ui.todo

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duoschedule.data.model.Course
import com.duoschedule.data.model.PersonType
import com.duoschedule.data.model.Priority
import com.duoschedule.data.model.RepeatFrequency
import com.duoschedule.data.model.RepeatRule
import com.duoschedule.data.model.Todo
import com.duoschedule.data.model.TodoStatus
import com.duoschedule.data.model.TodoTag
import com.duoschedule.data.repository.CourseRepository
import com.duoschedule.data.repository.TodoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class TodoEditState(
    val id: Long = 0,
    val title: String = "",
    val description: String = "",
    val personType: PersonType = PersonType.PERSON_A,
    val date: Long = LocalDate.now().toEpochDay(),
    val startHour: Int = -1,
    val startMinute: Int = -1,
    val endHour: Int = -1,
    val endMinute: Int = -1,
    val priority: Priority = Priority.MEDIUM,
    val status: TodoStatus = TodoStatus.PENDING,
    val selectedTagIds: Set<String> = emptySet(),
    val linkedCourseSyncId: String? = null,
    val repeatRuleId: String? = null,
    val repeatRule: RepeatRule? = null,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val isDeleted: Boolean = false,
    val errorMessage: String? = null,
    val syncId: String = ""
)

@HiltViewModel
class TodoEditViewModel @Inject constructor(
    private val todoRepository: TodoRepository,
    private val courseRepository: CourseRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(TodoEditState())
    val state: StateFlow<TodoEditState> = _state.asStateFlow()

    private val _allTags = MutableStateFlow<List<TodoTag>>(emptyList())
    val allTags: StateFlow<List<TodoTag>> = _allTags.asStateFlow()

    private val _courses = MutableStateFlow<List<Course>>(emptyList())
    val courses: StateFlow<List<Course>> = _courses.asStateFlow()

    init {
        loadTags()
        loadCourses()
    }

    private fun loadTags() {
        viewModelScope.launch {
            todoRepository.getAllTags().collect { tags ->
                _allTags.value = tags
            }
        }
    }

    private fun loadCourses() {
        viewModelScope.launch {
            courseRepository.getAllCourses().collect { courseList ->
                _courses.value = courseList.distinctBy { it.syncId }
            }
        }
    }

    fun loadTodo(todoId: Long) {
        viewModelScope.launch {
            val todo = todoRepository.getTodoById(todoId) ?: return@launch
            // 如果有待办有重复规则，加载规则
            val rule = todo.repeatRuleId?.let { todoRepository.getRepeatRuleById(it) }
            _state.value = TodoEditState(
                id = todo.id,
                title = todo.title,
                description = todo.description,
                personType = todo.personType,
                date = todo.date,
                startHour = todo.startHour,
                startMinute = todo.startMinute,
                endHour = todo.endHour,
                endMinute = todo.endMinute,
                priority = todo.priority,
                status = todo.status,
                selectedTagIds = if (todo.tags.isNotEmpty()) todo.tags.split(",").toSet() else emptySet(),
                linkedCourseSyncId = todo.linkedCourseSyncId,
                repeatRuleId = todo.repeatRuleId,
                repeatRule = rule,
                isEditing = true,
                syncId = todo.syncId
            )
        }
    }

    fun initialize(
        todoId: Long?,
        initialDate: Long? = null,
        initialPersonType: PersonType? = null,
        initialStartHour: Int = -1,
        initialStartMinute: Int = -1,
        initialEndHour: Int = -1,
        initialEndMinute: Int = -1
    ) {
        if (todoId != null && todoId > 0) {
            loadTodo(todoId)
        } else {
            initialDate?.let { _state.value = _state.value.copy(date = it) }
            initialPersonType?.let { _state.value = _state.value.copy(personType = it) }
            if (initialStartHour >= 0 && initialStartMinute >= 0) {
                _state.value = _state.value.copy(startHour = initialStartHour, startMinute = initialStartMinute)
            }
            if (initialEndHour >= 0 && initialEndMinute >= 0) {
                _state.value = _state.value.copy(endHour = initialEndHour, endMinute = initialEndMinute)
            }
        }
    }

    fun setTitle(title: String) {
        _state.value = _state.value.copy(title = title, errorMessage = null)
    }

    fun setDescription(description: String) {
        _state.value = _state.value.copy(description = description)
    }

    fun setPersonType(personType: PersonType) {
        _state.value = _state.value.copy(personType = personType)
    }

    fun setDate(epochDay: Long) {
        _state.value = _state.value.copy(date = epochDay)
    }

    fun setStartTime(hour: Int, minute: Int) {
        _state.value = _state.value.copy(startHour = hour, startMinute = minute)
    }

    fun clearStartTime() {
        _state.value = _state.value.copy(startHour = -1, startMinute = -1)
    }

    fun setEndTime(hour: Int, minute: Int) {
        _state.value = _state.value.copy(endHour = hour, endMinute = minute)
    }

    fun clearEndTime() {
        _state.value = _state.value.copy(endHour = -1, endMinute = -1)
    }

    fun setPriority(priority: Priority) {
        _state.value = _state.value.copy(priority = priority)
    }

    fun toggleTag(tagId: String) {
        val current = _state.value.selectedTagIds
        val newTags = if (current.contains(tagId)) current - tagId else current + tagId
        _state.value = _state.value.copy(selectedTagIds = newTags)
    }

    fun setLinkedCourse(syncId: String?) {
        _state.value = _state.value.copy(linkedCourseSyncId = syncId)
    }

    /** 设置重复规则，null 表示不重复 */
    fun setRepeatRule(rule: RepeatRule?) {
        _state.value = _state.value.copy(
            repeatRule = rule,
            repeatRuleId = rule?.id
        )
    }

    fun saveTodo() {
        val currentState = _state.value
        _state.value = _state.value.copy(isSaving = true)

        if (currentState.title.isBlank()) {
            _state.value = currentState.copy(errorMessage = "请输入待办标题", isSaving = false)
            return
        }

        viewModelScope.launch {
            // 如果有重复规则，先保存规则
            val rule = currentState.repeatRule
            if (rule != null) {
                todoRepository.insertRepeatRule(rule)
            }

            val todo = Todo(
                id = currentState.id,
                syncId = currentState.syncId,
                title = currentState.title,
                description = currentState.description,
                personType = currentState.personType,
                date = currentState.date,
                startHour = currentState.startHour,
                startMinute = currentState.startMinute,
                endHour = currentState.endHour,
                endMinute = currentState.endMinute,
                priority = currentState.priority,
                status = currentState.status,
                tags = currentState.selectedTagIds.joinToString(","),
                linkedCourseSyncId = currentState.linkedCourseSyncId,
                repeatRuleId = rule?.id
            )

            if (currentState.isEditing) {
                todoRepository.updateTodo(todo)
            } else {
                todoRepository.insertTodo(todo)
            }

            _state.value = currentState.copy(isSaved = true, isSaving = false)
        }
    }

    fun deleteTodo() {
        val currentState = _state.value
        if (!currentState.isEditing) return

        viewModelScope.launch {
            todoRepository.deleteTodoById(currentState.id)
            _state.value = currentState.copy(isDeleted = true)
        }
    }

}
