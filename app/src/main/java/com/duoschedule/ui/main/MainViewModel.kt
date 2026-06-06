package com.duoschedule.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duoschedule.data.model.Course
import com.duoschedule.data.model.PersonType
import com.duoschedule.data.model.TodayCourseDisplayMode
import com.duoschedule.data.model.Todo
import com.duoschedule.data.repository.CourseRepository
import com.duoschedule.data.repository.TodoRepository
import com.duoschedule.notification.AlarmScheduler
import com.duoschedule.ui.main.components.TodayTimelineItem
import com.duoschedule.ui.model.CurrentCourseState
import com.duoschedule.ui.model.FreeTimeSlot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel @Inject constructor(
    private val repository: CourseRepository,
    private val todoRepository: TodoRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    private val _currentHour = MutableStateFlow(LocalTime.now().hour)
    private val _currentMinute = MutableStateFlow(LocalTime.now().minute)

    val currentHour: StateFlow<Int> get() = _currentHour
    val currentMinute: StateFlow<Int> get() = _currentMinute

    val personAName: StateFlow<String> = repository.getPersonAName()
        .stateIn(viewModelScope, SharingStarted.Eagerly, "我")

    val personBName: StateFlow<String> = repository.getPersonBName()
        .stateIn(viewModelScope, SharingStarted.Eagerly, "Ta")

    val personACurrentWeek: StateFlow<Int> = repository.getCurrentWeek(PersonType.PERSON_A)
        .stateIn(viewModelScope, SharingStarted.Eagerly, 1)

    val personBCurrentWeek: StateFlow<Int> = repository.getCurrentWeek(PersonType.PERSON_B)
        .stateIn(viewModelScope, SharingStarted.Eagerly, 1)

    val todayCourseDisplayMode: StateFlow<TodayCourseDisplayMode> = repository.getTodayCourseDisplayMode()
        .stateIn(viewModelScope, SharingStarted.Eagerly, TodayCourseDisplayMode.BOTH)

    val personAPeriodTimes: StateFlow<List<String>> = repository.getPeriodTimes(PersonType.PERSON_A)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val personBPeriodTimes: StateFlow<List<String>> = repository.getPeriodTimes(PersonType.PERSON_B)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val singleModeEnabled: StateFlow<Boolean> = repository.getSingleModeEnabled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val currentDayOfWeek: StateFlow<Int> = kotlinx.coroutines.flow.MutableStateFlow(getCurrentDayOfWeek())
        .stateIn(viewModelScope, SharingStarted.Eagerly, getCurrentDayOfWeek())

    fun refreshCurrentDay() {
        (currentDayOfWeek as? kotlinx.coroutines.flow.MutableStateFlow)?.value = getCurrentDayOfWeek()
    }

    private val todayCourses: StateFlow<List<Course>> = currentDayOfWeek
        .flatMapLatest { day -> repository.getCoursesByDay(day) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val personATodayCourses: StateFlow<List<Course>> = todayCourses
        .combine(personACurrentWeek) { courses, week ->
            courses.filter { it.personType == PersonType.PERSON_A && it.isInWeek(week) }
                .sortedBy { it.startHour * 60 + it.startMinute }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val personBTodayCourses: StateFlow<List<Course>> = todayCourses
        .combine(personBCurrentWeek) { courses, week ->
            courses.filter { it.personType == PersonType.PERSON_B && it.isInWeek(week) }
                .sortedBy { it.startHour * 60 + it.startMinute }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        viewModelScope.launch {
            while (true) {
                updateTime()
                val delayMs = calculateNextUpdateDelay()
                delay(delayMs)
            }
        }
    }

    private data class TimeState(
        val hour: Int,
        val minute: Int
    )

    private val currentTime = combine(_currentHour, _currentMinute) { hour, minute ->
        TimeState(hour, minute)
    }

    val personACurrentCourse: StateFlow<CurrentCourseState> = combine(
        personATodayCourses,
        currentTime,
        personAName,
        personAPeriodTimes,
        personACurrentWeek
    ) { courses, time, name, periodTimes, week ->
        val currentCourse = courses.find { 
            it.isOngoing(time.hour, time.minute, week)
        }
        val nextCourse = courses.find { course ->
            course.startHour * 60 + course.startMinute > time.hour * 60 + time.minute
        }
        val progress = if (currentCourse != null) {
            val totalMinutes = (currentCourse.endHour * 60 + currentCourse.endMinute) - 
                              (currentCourse.startHour * 60 + currentCourse.startMinute)
            val elapsedMinutes = (time.hour * 60 + time.minute) - 
                                (currentCourse.startHour * 60 + currentCourse.startMinute)
            if (totalMinutes > 0) elapsedMinutes.toFloat() / totalMinutes.toFloat() else 0f
        } else 0f
        val nextCourseStartTime = if (nextCourse != null) {
            String.format(Locale.ROOT, "%02d:%02d", nextCourse.startHour, nextCourse.startMinute)
        } else ""
        CurrentCourseState(
            personType = PersonType.PERSON_A,
            personName = name,
            course = currentCourse,
            remainingMinutes = currentCourse?.getRemainingMinutes(time.hour, time.minute) ?: 0,
            hasCourse = currentCourse != null,
            progress = progress.coerceIn(0f, 1f),
            nextCourse = nextCourse,
            nextCourseStartTime = nextCourseStartTime,
            periodText = currentCourse?.let { getPeriodText(it, periodTimes) } ?: "",
            nextCoursePeriodText = nextCourse?.let { getPeriodText(it, periodTimes) } ?: ""
        )
    }.distinctUntilChanged().stateIn(viewModelScope, SharingStarted.Eagerly, 
        CurrentCourseState(PersonType.PERSON_A, "我"))

    val personBCurrentCourse: StateFlow<CurrentCourseState> = combine(
        personBTodayCourses,
        currentTime,
        personBName,
        personBPeriodTimes,
        personBCurrentWeek
    ) { courses, time, name, periodTimes, week ->
        val currentCourse = courses.find { 
            it.isOngoing(time.hour, time.minute, week)
        }
        val nextCourse = courses.find { course ->
            course.startHour * 60 + course.startMinute > time.hour * 60 + time.minute
        }
        val progress = if (currentCourse != null) {
            val totalMinutes = (currentCourse.endHour * 60 + currentCourse.endMinute) - 
                              (currentCourse.startHour * 60 + currentCourse.startMinute)
            val elapsedMinutes = (time.hour * 60 + time.minute) - 
                                (currentCourse.startHour * 60 + currentCourse.startMinute)
            if (totalMinutes > 0) elapsedMinutes.toFloat() / totalMinutes.toFloat() else 0f
        } else 0f
        val nextCourseStartTime = if (nextCourse != null) {
            String.format(Locale.ROOT, "%02d:%02d", nextCourse.startHour, nextCourse.startMinute)
        } else ""
        CurrentCourseState(
            personType = PersonType.PERSON_B,
            personName = name,
            course = currentCourse,
            remainingMinutes = currentCourse?.getRemainingMinutes(time.hour, time.minute) ?: 0,
            hasCourse = currentCourse != null,
            progress = progress.coerceIn(0f, 1f),
            nextCourse = nextCourse,
            nextCourseStartTime = nextCourseStartTime,
            periodText = currentCourse?.let { getPeriodText(it, periodTimes) } ?: "",
            nextCoursePeriodText = nextCourse?.let { getPeriodText(it, periodTimes) } ?: ""
        )
    }.distinctUntilChanged().stateIn(viewModelScope, SharingStarted.Eagerly, 
        CurrentCourseState(PersonType.PERSON_B, "Ta"))

    val freeTimeSlots: StateFlow<List<FreeTimeSlot>> = combine(
        personATodayCourses,
        personBTodayCourses,
        currentTime
    ) { coursesA, coursesB, time ->
        calculateFreeTimeSlots(coursesA, coursesB, time.hour, time.minute)
    }.distinctUntilChanged().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** 今日待办列表，按时间排序：有开始时间 > 仅有截止时间 > 无时间 */
    val todayTodos: StateFlow<List<Todo>> = todoRepository.getTodosByDate(LocalDate.now().toEpochDay())
        .combine(singleModeEnabled) { todos, singleMode ->
            val filtered = if (singleMode) {
                todos.filter { it.personType == PersonType.PERSON_A }
            } else {
                todos
            }
            filtered.sortedWith(
                compareBy<Todo> { todo ->
                    when {
                        todo.hasStartTime() -> 0
                        todo.isDeadlineOnly() -> 1
                        else -> 2
                    }
                }.thenBy { todo ->
                    when {
                        todo.hasStartTime() -> todo.startHour * 60 + todo.startMinute
                        todo.isDeadlineOnly() -> todo.endHour * 60 + todo.endMinute
                        else -> Int.MAX_VALUE
                    }
                }
            )
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** 合并时间线：将今日课程和待办按时间排序合并展示，根据显示模式过滤 */
    val mergedTimeline: StateFlow<List<TodayTimelineItem>> = combine(
        personATodayCourses, personBTodayCourses, todayTodos, singleModeEnabled, todayCourseDisplayMode
    ) { aCourses, bCourses, todos, singleMode, displayMode ->
        val courseItems = mutableListOf<TodayTimelineItem.CourseItem>()

        // 根据显示模式过滤课程
        val showPersonA = singleMode || displayMode == TodayCourseDisplayMode.SELF_ONLY || displayMode == TodayCourseDisplayMode.BOTH
        val showPersonB = !singleMode && (displayMode == TodayCourseDisplayMode.TA_ONLY || displayMode == TodayCourseDisplayMode.BOTH)

        if (showPersonA) courseItems.addAll(aCourses.map { TodayTimelineItem.CourseItem(it) })
        if (showPersonB) courseItems.addAll(bCourses.map { TodayTimelineItem.CourseItem(it) })

        // 根据显示模式过滤待办
        val filteredTodos = if (singleMode) {
            todos  // 单人模式下 todayTodos 已过滤
        } else {
            when (displayMode) {
                TodayCourseDisplayMode.SELF_ONLY -> todos.filter { it.personType == PersonType.PERSON_A }
                TodayCourseDisplayMode.TA_ONLY -> todos.filter { it.personType == PersonType.PERSON_B }
                TodayCourseDisplayMode.BOTH -> todos
            }
        }

        // 有时间的待办条目
        val timedTodoItems = filteredTodos.filter { it.hasStartTime() || it.isDeadlineOnly() }
            .map { TodayTimelineItem.TimedTodoItem(it) }
        // 无时间的待办条目
        val untimedTodoItems = filteredTodos.filter { !it.hasStartTime() && !it.isDeadlineOnly() }
            .map { TodayTimelineItem.UntimedTodoItem(it) }

        // 按时间排序：课程和有时间的待办混排，无时间待办排末尾
        (courseItems + timedTodoItems).sortedBy { it.sortKey } + untimedTodoItems
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** 切换待办完成状态 */
    fun toggleTodoStatus(id: Long) {
        viewModelScope.launch {
            todoRepository.toggleTodoStatus(id)
        }
    }

    private var lastDate: LocalDate = LocalDate.now()

    fun updateTime() {
        val now = LocalTime.now()
        _currentHour.value = now.hour
        _currentMinute.value = now.minute
        
        val today = LocalDate.now()
        if (today != lastDate) {
            lastDate = today
            refreshCurrentDay()
        }
    }

    private fun calculateNextUpdateDelay(): Long {
        val now = LocalTime.now()
        val nowMinutes = now.hour * 60 + now.minute

        val allCourses = personATodayCourses.value + personBTodayCourses.value

        val isInCourse = allCourses.any { course ->
            val startMinutes = course.startHour * 60 + course.startMinute
            val endMinutes = course.endHour * 60 + course.endMinute
            nowMinutes in startMinutes until endMinutes
        }

        if (isInCourse) {
            return 60_000L
        }

        val nextCourseStartMinutes = allCourses
            .map { it.startHour * 60 + it.startMinute }
            .filter { it > nowMinutes }
            .minOrNull()

        if (nextCourseStartMinutes != null) {
            val minutesUntilNext = nextCourseStartMinutes - nowMinutes
            val delayMs = minutesUntilNext * 60 * 1000L
            return delayMs.coerceIn(60_000L, 30 * 60 * 1000L)
        }

        return 60_000L
    }

    fun setSemesterStartDate(personType: PersonType, date: LocalDate) {
        viewModelScope.launch {
            repository.setSemesterStartDate(personType, date)
        }
    }

    fun setPersonName(personType: PersonType, name: String) {
        viewModelScope.launch {
            repository.setPersonName(personType, name)
        }
    }

    fun deleteCourse(courseId: Long) {
        viewModelScope.launch {
            alarmScheduler.cancelAlarmsForCourse(courseId)
            repository.deleteCourseById(courseId)
        }
    }

    fun setTodayCourseDisplayMode(mode: TodayCourseDisplayMode) {
        viewModelScope.launch {
            repository.setTodayCourseDisplayMode(mode)
        }
    }

    private fun getCurrentDayOfWeek(): Int {
        return LocalDate.now().dayOfWeek.value
    }

    private fun calculateFreeTimeSlots(
        coursesA: List<Course>,
        coursesB: List<Course>,
        currentHour: Int,
        currentMinute: Int
    ): List<FreeTimeSlot> {
        val allCourses = (coursesA + coursesB).sortedBy { it.startHour * 60 + it.startMinute }
        val currentTimeInMinutes = currentHour * 60 + currentMinute

        if (allCourses.isEmpty()) {
            val startMinutes = maxOf(8 * 60, currentTimeInMinutes)
            if (22 * 60 + 30 - startMinutes >= 30) {
                return listOf(FreeTimeSlot(startMinutes / 60, startMinutes % 60, 22, 30))
            }
            return emptyList()
        }

        val freeSlots = mutableListOf<FreeTimeSlot>()
        var lastEndTime = maxOf(8 * 60, currentTimeInMinutes)

        for (course in allCourses) {
            val startTime = course.startHour * 60 + course.startMinute
            val endTime = course.endHour * 60 + course.endMinute

            if (endTime <= currentTimeInMinutes) {
                continue
            }

            if (startTime > currentTimeInMinutes && startTime - lastEndTime >= 30) {
                freeSlots.add(FreeTimeSlot(
                    lastEndTime / 60, lastEndTime % 60,
                    startTime / 60, startTime % 60
                ))
            }

            if (endTime > lastEndTime) {
                lastEndTime = endTime
            }
        }

        if (22 * 60 + 30 - lastEndTime >= 30) {
            freeSlots.add(FreeTimeSlot(lastEndTime / 60, lastEndTime % 60, 22, 30))
        }

        return freeSlots
    }

    private fun getPeriodText(course: Course, periodTimes: List<String>): String {
        if (course.isCustomTime) {
            return course.getTimeString()
        }
        if (course.startPeriod > 0 && course.endPeriod > 0) {
            return if (course.startPeriod == course.endPeriod) {
                "第${course.startPeriod}节"
            } else {
                "第${course.startPeriod}-${course.endPeriod}节"
            }
        }
        
        if (periodTimes.isEmpty()) return ""
        
        val courseStartMinutes = course.startHour * 60 + course.startMinute
        val courseEndMinutes = course.endHour * 60 + course.endMinute
        
        var startPeriod = -1
        var endPeriod = -1
        
        for ((index, periodTime) in periodTimes.withIndex()) {
            val parts = periodTime.split("-")
            if (parts.size == 2) {
                val startParts = parts[0].split(":")
                val endParts = parts[1].split(":")
                if (startParts.size == 2 && endParts.size == 2) {
                    val periodStartMinutes = (startParts[0].toIntOrNull() ?: 0) * 60 +
                        (startParts[1].toIntOrNull() ?: 0)
                    val periodEndMinutes = (endParts[0].toIntOrNull() ?: 0) * 60 +
                        (endParts[1].toIntOrNull() ?: 0)
                    
                    if (startPeriod == -1 && courseStartMinutes >= periodStartMinutes && courseStartMinutes < periodEndMinutes) {
                        startPeriod = index + 1
                    }
                    if (courseEndMinutes > periodStartMinutes && courseEndMinutes <= periodEndMinutes) {
                        endPeriod = index + 1
                    }
                }
            }
        }
        
        return if (startPeriod > 0 && endPeriod > 0) {
            if (startPeriod == endPeriod) {
                "第${startPeriod}节"
            } else {
                "第${startPeriod}-${endPeriod}节"
            }
        } else {
            ""
        }
    }
}
