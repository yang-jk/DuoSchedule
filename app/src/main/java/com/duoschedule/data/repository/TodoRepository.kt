package com.duoschedule.data.repository

import com.duoschedule.data.local.RepeatRuleDao
import com.duoschedule.data.local.TodoDao
import com.duoschedule.data.local.TodoTagDao
import com.duoschedule.data.model.PersonType
import com.duoschedule.data.model.RepeatFrequency
import com.duoschedule.data.model.RepeatRule
import com.duoschedule.data.model.Todo
import com.duoschedule.data.model.TodoStatus
import com.duoschedule.data.model.TodoTag
import com.duoschedule.notification.TodoAlarmScheduler
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TodoRepository @Inject constructor(
    private val todoDao: TodoDao,
    private val todoTagDao: TodoTagDao,
    private val repeatRuleDao: RepeatRuleDao,
    private val todoAlarmScheduler: TodoAlarmScheduler
) {
    // ==================== Todo 操作 ====================

    /** 获取所有待办 */
    fun getAllTodos(): Flow<List<Todo>> = todoDao.getAllTodos()

    /** 获取指定日期的待办 */
    fun getTodosByDate(date: Long): Flow<List<Todo>> = todoDao.getTodosByDate(date)

    /** 获取指定人物在指定日期的待办 */
    fun getTodosByPersonAndDate(personType: PersonType, date: Long): Flow<List<Todo>> =
        todoDao.getTodosByPersonAndDate(personType, date)

    /** 获取指定人物在指定日期的待完成待办 */
    fun getPendingTodosByPersonAndDate(personType: PersonType, date: Long): Flow<List<Todo>> =
        todoDao.getPendingTodosByPersonAndDate(personType, date)

    /** 获取指定人物在日期范围内的待办 */
    fun getTodosByPersonAndDateRange(personType: PersonType, startEpochDay: Long, endEpochDay: Long): Flow<List<Todo>> =
        todoDao.getTodosByPersonAndDateRange(personType, startEpochDay, endEpochDay)

    /** 根据 ID 获取待办 */
    suspend fun getTodoById(id: Long): Todo? = todoDao.getTodoById(id)

    /** 根据 syncId 获取待办 */
    suspend fun getTodoBySyncId(syncId: String): Todo? = todoDao.getTodoBySyncId(syncId)

    /** 获取所有待办（同步用，非 Flow） */
    suspend fun getAllTodosSync(): List<Todo> = todoDao.getAllTodosSync()

    /** 插入待办，返回行 ID，同时调度提醒闹钟 */
    suspend fun insertTodo(todo: Todo): Long {
        val id = todoDao.insertTodo(todo)
        // 使用返回的 id 获取完整待办并调度闹钟
        val insertedTodo = todo.copy(id = id)
        todoAlarmScheduler.scheduleTodoReminder(insertedTodo)
        return id
    }

    /** 更新待办，同时更新提醒闹钟 */
    suspend fun updateTodo(todo: Todo) {
        todoDao.updateTodo(todo)
        // 更新闹钟：先取消旧的，再根据新状态调度
        todoAlarmScheduler.cancelTodoReminder(todo.id)
        todoAlarmScheduler.scheduleTodoReminder(todo)
    }

    /** 根据 ID 删除待办，同时取消提醒闹钟 */
    suspend fun deleteTodoById(id: Long) {
        todoAlarmScheduler.cancelTodoReminder(id)
        todoDao.deleteTodoById(id)
    }

    /** 根据 syncId 删除待办，同时取消提醒闹钟 */
    suspend fun deleteTodoBySyncId(syncId: String) {
        val todo = todoDao.getTodoBySyncId(syncId)
        if (todo != null) {
            todoAlarmScheduler.cancelTodoReminder(todo.id)
        }
        todoDao.deleteTodoBySyncId(syncId)
    }

    /** 完成待办，设置状态为 COMPLETED 并记录完成时间，同时取消提醒闹钟 */
    suspend fun completeTodo(id: Long) {
        val todo = todoDao.getTodoById(id) ?: return
        todoDao.updateTodo(todo.copy(status = TodoStatus.COMPLETED, completedAt = System.currentTimeMillis()))
        todoAlarmScheduler.cancelTodoReminder(id)
    }

    /** 取消完成待办，设置状态为 PENDING 并清除完成时间，同时调度提醒闹钟 */
    suspend fun uncompleteTodo(id: Long) {
        val todo = todoDao.getTodoById(id) ?: return
        val updatedTodo = todo.copy(status = TodoStatus.PENDING, completedAt = null)
        todoDao.updateTodo(updatedTodo)
        todoAlarmScheduler.scheduleTodoReminder(updatedTodo)
    }

    /** 切换待办状态（PENDING ↔ COMPLETED），同时更新提醒闹钟 */
    suspend fun toggleTodoStatus(id: Long) {
        val todo = todoDao.getTodoById(id) ?: return
        if (todo.status == TodoStatus.PENDING) {
            todoDao.updateTodo(todo.copy(status = TodoStatus.COMPLETED, completedAt = System.currentTimeMillis()))
            todoAlarmScheduler.cancelTodoReminder(id)
        } else {
            val updatedTodo = todo.copy(status = TodoStatus.PENDING, completedAt = null)
            todoDao.updateTodo(updatedTodo)
            todoAlarmScheduler.scheduleTodoReminder(updatedTodo)
        }
    }

    // ==================== 标签操作 ====================

    /** 获取所有标签 */
    fun getAllTags(): Flow<List<TodoTag>> = todoTagDao.getAllTags()

    /** 获取所有标签（同步用，非 Flow） */
    suspend fun getAllTagsSync(): List<TodoTag> = todoTagDao.getAllTagsSync()

    /** 插入标签 */
    suspend fun insertTag(tag: TodoTag) {
        todoTagDao.insertTag(tag)
    }

    /** 更新标签 */
    suspend fun updateTag(tag: TodoTag) = todoTagDao.updateTag(tag)

    /** 根据 ID 删除标签 */
    suspend fun deleteTagById(id: String) = todoTagDao.deleteTagById(id)

    /** 初始化预设标签（如果不存在则插入） */
    suspend fun initPresetTags() {
        val presetTags = listOf(
            TodoTag(id = "学习", name = "学习", color = 0xFF4A90D9, isPreset = true),
            TodoTag(id = "生活", name = "生活", color = 0xFF52C41A, isPreset = true),
            TodoTag(id = "考试", name = "考试", color = 0xFFFF4D4F, isPreset = true),
            TodoTag(id = "运动", name = "运动", color = 0xFF722ED1, isPreset = true),
            TodoTag(id = "其他", name = "其他", color = 0xFF8C8C8C, isPreset = true)
        )
        for (tag in presetTags) {
            val existing = todoTagDao.getTagById(tag.id)
            if (existing == null) {
                todoTagDao.insertTag(tag)
            }
        }
    }

    // ==================== 重复规则操作 ====================

    /** 根据 ID 获取重复规则 */
    suspend fun getRepeatRuleById(id: String): RepeatRule? = repeatRuleDao.getRepeatRuleById(id)

    /** 获取所有重复规则（同步用，非 Flow） */
    suspend fun getAllRepeatRulesSync(): List<RepeatRule> = repeatRuleDao.getAllRepeatRulesSync()

    /** 插入重复规则 */
    suspend fun insertRepeatRule(rule: RepeatRule) {
        repeatRuleDao.insertRepeatRule(rule)
    }

    /** 更新重复规则 */
    suspend fun updateRepeatRule(rule: RepeatRule) = repeatRuleDao.updateRepeatRule(rule)

    /** 根据 ID 删除重复规则 */
    suspend fun deleteRepeatRuleById(id: String) = repeatRuleDao.deleteRepeatRuleById(id)

    // ==================== 重复实例生成 ====================

    /**
     * 根据重复规则和基准待办，生成指定日期范围内的重复实例
     * @param rule 重复规则
     * @param baseTodo 基准待办
     * @param startDate 起始日期（epoch day）
     * @param endDate 结束日期（epoch day）
     * @return 生成的待办实例列表（不含基准待办本身）
     */
    private fun generateRepeatInstances(rule: RepeatRule, baseTodo: Todo, startDate: Long, endDate: Long): List<Todo> {
        val instances = mutableListOf<Todo>()
        val baseDate = LocalDate.ofEpochDay(baseTodo.date)
        val rangeStart = LocalDate.ofEpochDay(startDate)
        val rangeEnd = LocalDate.ofEpochDay(endDate)
        // 规则的结束日期，null 表示永不结束
        val ruleEndDate = rule.endDate?.let { LocalDate.ofEpochDay(it) }

        when (rule.frequency) {
            RepeatFrequency.DAILY -> {
                // 每隔 interval 天生成一个实例
                var currentDate = baseDate.plusDays(rule.interval.toLong())
                while (!currentDate.isAfter(rangeEnd)) {
                    if (!currentDate.isBefore(rangeStart) && (ruleEndDate == null || !currentDate.isAfter(ruleEndDate))) {
                        instances.add(createRepeatInstance(baseTodo, currentDate.toEpochDay()))
                    }
                    currentDate = currentDate.plusDays(rule.interval.toLong())
                }
            }
            RepeatFrequency.WEEKLY -> {
                // 解析 daysOfWeek，格式为 "1,3,5"（1=周一，7=周日）
                val targetDays = rule.daysOfWeek.split(",")
                    .filter { it.isNotBlank() }
                    .mapNotNull { it.trim().toIntOrNull() }
                    .map { dayNum ->
                        when (dayNum) {
                            1 -> DayOfWeek.MONDAY
                            2 -> DayOfWeek.TUESDAY
                            3 -> DayOfWeek.WEDNESDAY
                            4 -> DayOfWeek.THURSDAY
                            5 -> DayOfWeek.FRIDAY
                            6 -> DayOfWeek.SATURDAY
                            7 -> DayOfWeek.SUNDAY
                            else -> null
                        }
                    }.filterNotNull().toSet()

                if (targetDays.isEmpty()) return emptyList()

                // 从基准日期所在周的周一开始遍历
                var weekStart = baseDate.with(DayOfWeek.MONDAY)
                while (!weekStart.isAfter(rangeEnd)) {
                    for (dayOfWeek in targetDays) {
                        val currentDate = weekStart.with(dayOfWeek)
                        // 跳过基准日期及之前的日期
                        if (!currentDate.isAfter(baseDate)) continue
                        // 检查是否在范围内
                        if (currentDate.isBefore(rangeStart) || currentDate.isAfter(rangeEnd)) continue
                        // 检查是否在规则结束日期内
                        if (ruleEndDate != null && currentDate.isAfter(ruleEndDate)) continue
                        // 检查间隔：计算从基准日期到当前日期的周数差，必须是 interval 的整数倍
                        val weeksDiff = ChronoUnit.WEEKS.between(baseDate.with(DayOfWeek.MONDAY), weekStart)
                        if (weeksDiff % rule.interval != 0L) continue

                        instances.add(createRepeatInstance(baseTodo, currentDate.toEpochDay()))
                    }
                    weekStart = weekStart.plusWeeks(1)
                }
            }
            RepeatFrequency.CUSTOM -> {
                // 解析 customDates，格式为 "18993,19000,19007"（epoch day）
                val customDates = rule.customDates.split(",")
                    .filter { it.isNotBlank() }
                    .mapNotNull { it.trim().toLongOrNull() }

                for (epochDay in customDates) {
                    val currentDate = LocalDate.ofEpochDay(epochDay)
                    // 跳过基准日期
                    if (epochDay <= baseTodo.date) continue
                    // 检查是否在范围内
                    if (epochDay < startDate || epochDay > endDate) continue
                    // 检查是否在规则结束日期内
                    if (ruleEndDate != null && currentDate.isAfter(ruleEndDate)) continue

                    instances.add(createRepeatInstance(baseTodo, epochDay))
                }
            }
        }

        return instances
    }

    /**
     * 创建一个重复实例，保留基准待办的所有属性，但使用新的 syncId 和指定的日期
     */
    private fun createRepeatInstance(baseTodo: Todo, date: Long): Todo {
        return baseTodo.copy(
            id = 0,  // 自动生成
            syncId = UUID.randomUUID().toString(),
            date = date,
            status = TodoStatus.PENDING,
            completedAt = null
        )
    }

    /**
     * 确保指定日期范围内的重复实例已生成
     * 查找所有有重复规则的待办，为每个待办生成缺失的重复实例
     * @param startEpochDay 起始日期（epoch day）
     * @param endEpochDay 结束日期（epoch day）
     */
    suspend fun ensureRepeatInstancesForDateRange(startEpochDay: Long, endEpochDay: Long) {
        // 获取所有有重复规则的待办
        val todosWithRepeat = todoDao.getTodosWithRepeatRule()
        // 按 repeatRuleId 分组，取每组中日期最早的那个作为基准待办
        val baseTodos = todosWithRepeat
            .filter { it.repeatRuleId != null }
            .groupBy { it.repeatRuleId!! }
            .mapValues { (_, todos) -> todos.minByOrNull { it.date }!! }

        for ((repeatRuleId, baseTodo) in baseTodos) {
            val rule = repeatRuleDao.getRepeatRuleById(repeatRuleId) ?: continue
            val instances = generateRepeatInstances(rule, baseTodo, startEpochDay, endEpochDay)

            // 检查每个实例是否已存在（通过 repeatRuleId + date + personType 判断）
            for (instance in instances) {
                val existing = todoDao.getTodoByRepeatRuleIdAndDateAndPerson(
                    instance.repeatRuleId!!,
                    instance.date,
                    instance.personType
                )
                if (existing == null) {
                    insertTodo(instance)
                }
            }
        }
    }
}
