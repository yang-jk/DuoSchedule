package com.duoschedule.ui.edit

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duoschedule.data.local.CourseDao
import com.duoschedule.data.model.Course
import com.duoschedule.data.model.PersonType
import com.duoschedule.data.model.WeekType
import com.duoschedule.data.repository.CourseRepository
import com.duoschedule.notification.AlarmScheduler
import com.duoschedule.notification.BootReceiverManager
import com.duoschedule.notification.CourseNotificationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CourseHistoryItem(
    val name: String,
    val location: String,
    val teacher: String
)

data class CourseEditState(
    val id: Long = 0,
    val name: String = "",
    val location: String = "",
    val teacher: String = "",
    val dayOfWeek: Int = 1,
    val startPeriod: Int = 1,
    val endPeriod: Int = 2,
    val weekType: WeekType = WeekType.ALL,
    val startWeek: Int = 1,
    val endWeek: Int = 16,
    val customWeeks: String = "",
    val personType: PersonType = PersonType.PERSON_A,
    val isEditing: Boolean = false,
    val errorMessage: String? = null,
    val saved: Boolean = false,
    val deleted: Boolean = false,
    val selectedWeeks: Set<Int> = emptySet(),
    val selectedPeriods: Set<Int> = setOf(1, 2),
    val isCustomTime: Boolean = false,
    val customStartHour: Int = 8,
    val customStartMinute: Int = 0,
    val customEndHour: Int = 9,
    val customEndMinute: Int = 30
)

@HiltViewModel
class CourseEditViewModel @Inject constructor(
    private val repository: CourseRepository,
    private val notificationManager: CourseNotificationManager,
    private val alarmScheduler: AlarmScheduler,
    private val courseDao: CourseDao,
    private val application: Application
) : ViewModel() {

    private val _state = MutableStateFlow(CourseEditState())
    val state: StateFlow<CourseEditState> = _state.asStateFlow()

    val singleModeEnabled: StateFlow<Boolean> = repository.getSingleModeEnabled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val cachedTotalWeeks = MutableStateFlow(16)
    private val cachedTotalPeriods = MutableStateFlow(5)
    private val cachedPeriodTimes = MutableStateFlow<List<String>>(emptyList())

    val totalWeeks: StateFlow<Int> = cachedTotalWeeks.asStateFlow()
    val totalPeriods: StateFlow<Int> = cachedTotalPeriods.asStateFlow()
    val periodTimes: StateFlow<List<String>> = cachedPeriodTimes.asStateFlow()

    private val _courseHistory = MutableStateFlow<List<CourseHistoryItem>>(emptyList())
    val courseHistory: StateFlow<List<CourseHistoryItem>> = _courseHistory.asStateFlow()

    private val _teacherHistory = MutableStateFlow<List<String>>(emptyList())
    val teacherHistory: StateFlow<List<String>> = _teacherHistory.asStateFlow()

    init {
        viewModelScope.launch {
            val isSingleMode = repository.getSingleModeEnabled().first()
            if (isSingleMode) {
                _state.value = _state.value.copy(personType = PersonType.PERSON_A)
                loadSettings(PersonType.PERSON_A)
            } else {
                loadSettings(PersonType.PERSON_A)
            }
        }
        loadHistory()
    }

    private fun loadSettings(personType: PersonType) {
        viewModelScope.launch {
            cachedTotalWeeks.value = repository.getTotalWeeks(personType).first()
            cachedTotalPeriods.value = repository.getTotalPeriods(personType).first()
            cachedPeriodTimes.value = repository.getPeriodTimes(personType).first()
            
            if (_state.value.selectedWeeks.isEmpty()) {
                _state.value = _state.value.copy(
                    selectedWeeks = (1..cachedTotalWeeks.value).toSet(),
                    endWeek = cachedTotalWeeks.value
                )
            }
        }
    }

    private fun loadHistory() {
        viewModelScope.launch {
            val allCourses = repository.getAllCourses().first()
            
            val courseMap = mutableMapOf<String, CourseHistoryItem>()
            val teacherSet = mutableSetOf<String>()
            
            allCourses.forEach { course ->
                val key = course.name.lowercase()
                if (!courseMap.containsKey(key)) {
                    courseMap[key] = CourseHistoryItem(
                        name = course.name,
                        location = course.location,
                        teacher = course.teacher
                    )
                }
                if (course.teacher.isNotEmpty()) {
                    teacherSet.add(course.teacher)
                }
            }
            
            _courseHistory.value = courseMap.values.toList().sortedBy { it.name }
            _teacherHistory.value = teacherSet.toList().sorted()
        }
    }

    fun loadCourse(courseId: Long) {
        viewModelScope.launch {
            val course = repository.getCourseById(courseId)
            if (course != null) {
                val selectedWeeks = when (course.weekType) {
                    WeekType.ALL -> (course.startWeek..course.endWeek).toSet()
                    WeekType.ODD -> (course.startWeek..course.endWeek).filter { it % 2 == 1 }.toSet()
                    WeekType.EVEN -> (course.startWeek..course.endWeek).filter { it % 2 == 0 }.toSet()
                    WeekType.CUSTOM -> {
                        if (course.customWeeks.isNotEmpty()) {
                            course.customWeeks.split(",").mapNotNull { it.toIntOrNull() }.toSet()
                        } else {
                            emptySet()
                        }
                    }
                }

                val times = periodTimes.value
                val startPeriod = if (course.startPeriod > 0) course.startPeriod else getPeriodFromTime(course.startHour, course.startMinute, times)
                val endPeriod = if (course.endPeriod > 0) course.endPeriod else getPeriodFromTime(course.endHour, course.endMinute, times)
                val selectedPeriods = (startPeriod..endPeriod).toSet()

                _state.value = CourseEditState(
                    id = course.id,
                    name = course.name,
                    location = course.location,
                    teacher = course.teacher,
                    dayOfWeek = course.dayOfWeek,
                    startPeriod = startPeriod,
                    endPeriod = endPeriod,
                    weekType = course.weekType,
                    startWeek = course.startWeek,
                    endWeek = course.endWeek,
                    customWeeks = course.customWeeks,
                    personType = course.personType,
                    isEditing = true,
                    selectedWeeks = selectedWeeks,
                    selectedPeriods = selectedPeriods,
                    isCustomTime = course.isCustomTime,
                    customStartHour = course.startHour,
                    customStartMinute = course.startMinute,
                    customEndHour = course.endHour,
                    customEndMinute = course.endMinute
                )
                
                loadSettings(course.personType)
            }
        }
    }

    fun setName(name: String) {
        _state.value = _state.value.copy(name = name, errorMessage = null)
    }

    fun setLocation(location: String) {
        _state.value = _state.value.copy(location = location)
    }

    fun setTeacher(teacher: String) {
        _state.value = _state.value.copy(teacher = teacher)
    }

    fun setDayOfWeek(dayOfWeek: Int) {
        _state.value = _state.value.copy(dayOfWeek = dayOfWeek)
    }

    fun setPeriods(startPeriod: Int, endPeriod: Int) {
        _state.value = _state.value.copy(
            startPeriod = startPeriod,
            endPeriod = endPeriod,
            selectedPeriods = (startPeriod..endPeriod).toSet()
        )
    }

    fun setSelectedWeeks(weeks: Set<Int>) {
        if (weeks.isEmpty()) {
            return
        }
        
        val totalWeeks = totalWeeks.value
        val newWeekType = if (weeks.size == totalWeeks) WeekType.ALL else WeekType.CUSTOM
        
        _state.value = _state.value.copy(
            selectedWeeks = weeks,
            weekType = newWeekType,
            startWeek = weeks.min(),
            endWeek = weeks.max(),
            customWeeks = if (newWeekType == WeekType.CUSTOM) {
                weeks.sorted().joinToString(",")
            } else {
                ""
            }
        )
    }

    fun setWeekType(weekType: WeekType) {
        val currentState = _state.value
        val totalWeeks = totalWeeks.value
        
        val newSelectedWeeks = when (weekType) {
            WeekType.ALL -> (1..totalWeeks).toSet()
            WeekType.ODD -> (1..totalWeeks).filter { it % 2 == 1 }.toSet()
            WeekType.EVEN -> (1..totalWeeks).filter { it % 2 == 0 }.toSet()
            WeekType.CUSTOM -> emptySet()
        }
        
        _state.value = currentState.copy(
            weekType = weekType,
            selectedWeeks = newSelectedWeeks,
            startWeek = if (newSelectedWeeks.isNotEmpty()) newSelectedWeeks.min() else 1,
            endWeek = if (newSelectedWeeks.isNotEmpty()) newSelectedWeeks.max() else totalWeeks
        )
    }

    fun toggleWeekSelection(week: Int) {
        val currentState = _state.value
        val newSelectedWeeks = if (currentState.selectedWeeks.contains(week)) {
            currentState.selectedWeeks - week
        } else {
            currentState.selectedWeeks + week
        }
        
        val newWeekType = if (newSelectedWeeks.isEmpty()) {
            WeekType.ALL
        } else if (newSelectedWeeks.size == totalWeeks.value) {
            WeekType.ALL
        } else {
            WeekType.CUSTOM
        }
        
        _state.value = currentState.copy(
            selectedWeeks = newSelectedWeeks,
            weekType = newWeekType,
            startWeek = if (newSelectedWeeks.isNotEmpty()) newSelectedWeeks.min() else 1,
            endWeek = if (newSelectedWeeks.isNotEmpty()) newSelectedWeeks.max() else totalWeeks.value,
            customWeeks = if (newWeekType == WeekType.CUSTOM) {
                newSelectedWeeks.sorted().joinToString(",")
            } else {
                ""
            }
        )
    }

    fun setPersonType(personType: PersonType) {
        val currentType = _state.value.personType
        if (currentType != personType) {
            _state.value = _state.value.copy(personType = personType)
            loadSettings(personType)
        }
    }

    fun setTimeMode(isCustomTime: Boolean) {
        val currentState = _state.value
        if (currentState.isCustomTime == isCustomTime) return

        if (isCustomTime) {
            val times = periodTimes.value
            val (startHour, startMinute) = getTimeFromPeriod(currentState.startPeriod, times)
            val (endHour, endMinute) = getTimeFromPeriodEnd(currentState.endPeriod, times)
            _state.value = currentState.copy(
                isCustomTime = true,
                customStartHour = startHour,
                customStartMinute = startMinute,
                customEndHour = endHour,
                customEndMinute = endMinute
            )
        } else {
            val times = periodTimes.value
            val startPeriod = getPeriodFromTime(currentState.customStartHour, currentState.customStartMinute, times)
            val endPeriod = getPeriodFromTime(currentState.customEndHour, currentState.customEndMinute, times)
            val clampedEndPeriod = endPeriod.coerceAtLeast(startPeriod)
            _state.value = currentState.copy(
                isCustomTime = false,
                startPeriod = startPeriod,
                endPeriod = clampedEndPeriod,
                selectedPeriods = (startPeriod..clampedEndPeriod).toSet()
            )
        }
    }

    fun setCustomTime(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) {
        _state.value = _state.value.copy(
            customStartHour = startHour,
            customStartMinute = startMinute,
            customEndHour = endHour,
            customEndMinute = endMinute
        )
    }

    fun selectFromHistory(historyItem: CourseHistoryItem) {
        _state.value = _state.value.copy(
            name = historyItem.name,
            location = historyItem.location,
            teacher = historyItem.teacher
        )
    }

    fun saveCourse() {
        val currentState = _state.value

        if (currentState.name.isBlank()) {
            _state.value = currentState.copy(errorMessage = "请输入课程名称")
            return
        }

        if (currentState.isCustomTime) {
            val startTotal = currentState.customStartHour * 60 + currentState.customStartMinute
            val endTotal = currentState.customEndHour * 60 + currentState.customEndMinute
            if (endTotal <= startTotal) {
                _state.value = currentState.copy(errorMessage = "结束时间必须晚于开始时间")
                return
            }
        }

        viewModelScope.launch {
            val weekType = if (currentState.selectedWeeks.isEmpty()) {
                WeekType.ALL
            } else if (currentState.selectedWeeks.size == totalWeeks.value) {
                WeekType.ALL
            } else {
                WeekType.CUSTOM
            }

            val startWeek = if (currentState.selectedWeeks.isNotEmpty()) currentState.selectedWeeks.min() else 1
            val endWeek = if (currentState.selectedWeeks.isNotEmpty()) currentState.selectedWeeks.max() else totalWeeks.value
            val customWeeks = if (weekType == WeekType.CUSTOM) {
                currentState.selectedWeeks.sorted().joinToString(",")
            } else {
                ""
            }

            val (startHour, startMinute, endHour, endMinute, startPeriod, endPeriod) = if (currentState.isCustomTime) {
                Tuple6(
                    currentState.customStartHour,
                    currentState.customStartMinute,
                    currentState.customEndHour,
                    currentState.customEndMinute,
                    0,
                    0
                )
            } else {
                val times = periodTimes.value
                val sp = currentState.startPeriod
                val ep = currentState.endPeriod
                val (sh, sm) = getTimeFromPeriod(sp, times)
                val (eh, em) = getTimeFromPeriodEnd(ep, times)
                Tuple6(sh, sm, eh, em, sp, ep)
            }

            val course = Course(
                id = currentState.id,
                name = currentState.name,
                location = currentState.location,
                teacher = currentState.teacher,
                dayOfWeek = currentState.dayOfWeek,
                startHour = startHour,
                startMinute = startMinute,
                endHour = endHour,
                endMinute = endMinute,
                weekType = weekType,
                startWeek = startWeek,
                endWeek = endWeek,
                customWeeks = customWeeks,
                personType = currentState.personType,
                startPeriod = startPeriod,
                endPeriod = endPeriod,
                isCustomTime = currentState.isCustomTime
            )

            val hasConflict = repository.checkTimeConflict(course, currentState.id)
            if (hasConflict) {
                _state.value = currentState.copy(errorMessage = "时间冲突：该时段已有其他课程")
                return@launch
            }

            if (currentState.isEditing) {
                alarmScheduler.cancelAlarmsForCourse(currentState.id)
                repository.updateCourse(course)
            } else {
                repository.insertCourse(course)
            }

            Log.d("CourseEditViewModel", "课程已保存: ${course.name}, 重新调度通知")
            notificationManager.scheduleReminderNotifications()
            BootReceiverManager.updateBootReceiverEnabled(application, courseDao)

            _state.value = currentState.copy(saved = true)
        }
    }

    private data class Tuple6<T1, T2, T3, T4, T5, T6>(
        val v1: T1, val v2: T2, val v3: T3, val v4: T4, val v5: T5, val v6: T6
    )

    fun deleteCourse() {
        val currentState = _state.value
        if (!currentState.isEditing) return

        viewModelScope.launch {
            alarmScheduler.cancelAlarmsForCourse(currentState.id)
            repository.deleteCourseById(currentState.id)
            
            Log.d("CourseEditViewModel", "课程已删除, 重新调度通知")
            notificationManager.scheduleReminderNotifications()
            BootReceiverManager.updateBootReceiverEnabled(application, courseDao)
            
            _state.value = currentState.copy(deleted = true)
        }
    }

    private fun getPeriodFromTime(hour: Int, minute: Int, periodTimes: List<String>): Int {
        val totalMinutes = hour * 60 + minute
        
        periodTimes.forEachIndexed { index, timeRange ->
            val times = timeRange.split("-")
            if (times.size == 2) {
                val startParts = times[0].split(":")
                if (startParts.size == 2) {
                    try {
                        val startMinutes = startParts[0].toInt() * 60 + startParts[1].toInt()
                        if (totalMinutes >= startMinutes) {
                            return index + 1
                        }
                    } catch (e: NumberFormatException) {
                        e.printStackTrace()
                    }
                }
            }
        }
        
        return 1
    }

    private fun getTimeFromPeriod(period: Int, periodTimes: List<String>): Pair<Int, Int> {
        val defaultTime = Pair(8, 0)
        if (periodTimes.isEmpty()) return defaultTime
        
        val timeRange = periodTimes.getOrNull(period - 1) ?: return defaultTime
        val times = timeRange.split("-")
        
        if (times.size == 2) {
            val startParts = times[0].split(":")
            if (startParts.size == 2) {
                try {
                    return Pair(startParts[0].toInt(), startParts[1].toInt())
                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                }
            }
        }
        
        return defaultTime
    }

    private fun getTimeFromPeriodEnd(period: Int, periodTimes: List<String>): Pair<Int, Int> {
        val defaultTime = Pair(8, 45)
        if (periodTimes.isEmpty()) return defaultTime
        
        val timeRange = periodTimes.getOrNull(period - 1) ?: return defaultTime
        val times = timeRange.split("-")
        
        if (times.size == 2) {
            val endParts = times[1].split(":")
            if (endParts.size == 2) {
                try {
                    return Pair(endParts[0].toInt(), endParts[1].toInt())
                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                }
            }
        }
        
        return defaultTime
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }

    fun setInitialDayOfWeek(dayOfWeek: Int) {
        _state.value = _state.value.copy(dayOfWeek = dayOfWeek)
    }

    fun setInitialPeriod(period: Int) {
        _state.value = _state.value.copy(
            selectedPeriods = setOf(period),
            startPeriod = period,
            endPeriod = period
        )
    }

    fun setInitialPersonType(personType: PersonType) {
        _state.value = _state.value.copy(personType = personType)
        loadSettings(personType)
    }
}
