package com.duoschedule.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.duoschedule.data.local.SettingsDataStore
import com.duoschedule.data.local.TodoDao
import com.duoschedule.data.model.Todo
import com.duoschedule.data.model.TodoStatus
import com.duoschedule.util.AppLogger
import com.duoschedule.util.SafeConverters
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 待办提醒闹钟调度器
 * 负责调度和取消待办提醒闹钟
 */
@Singleton
class TodoAlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val todoDao: TodoDao,
    private val settingsDataStore: SettingsDataStore
) {
    companion object {
        private const val TAG = "TodoAlarmScheduler"

        /** 待办提醒闹钟的 action */
        const val ACTION_TODO_REMINDER = "com.duoschedule.action.TODO_REMINDER"

        /** Intent Extra: 待办 ID */
        const val EXTRA_TODO_ID = "todo_id"

        /** Intent Extra: 待办标题 */
        const val EXTRA_TODO_TITLE = "todo_title"

        /** Intent Extra: 提醒时间字符串 */
        const val EXTRA_TODO_TIME = "todo_time"

        /** 请求码偏移量，用于区分课程闹钟 */
        private const val REQUEST_CODE_OFFSET = 70000
    }

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /** 计算待办的提醒时间戳（毫秒），返回 null 表示不需要提醒 */
    private fun calculateReminderTime(todo: Todo): Long? {
        // 已完成的不提醒
        if (todo.status == TodoStatus.COMPLETED) return null

        val todoDate = LocalDate.ofEpochDay(todo.date)

        return when {
            // 有开始时间：提前 5 分钟提醒
            todo.hasStartTime() -> {
                val startTime = LocalTime.of(todo.startHour, todo.startMinute)
                val reminderTime = startTime.minusMinutes(5)
                LocalDateTime.of(todoDate, reminderTime)
                    .atZone(ZoneId.systemDefault()).toEpochSecond() * 1000
            }
            // 仅有截止时间：提前 30 分钟提醒
            todo.isDeadlineOnly() -> {
                val endTime = LocalTime.of(todo.endHour, todo.endMinute)
                val reminderTime = endTime.minusMinutes(30)
                LocalDateTime.of(todoDate, reminderTime)
                    .atZone(ZoneId.systemDefault()).toEpochSecond() * 1000
            }
            // 没有时间信息：不提醒
            else -> null
        }
    }

    /** 调度待办提醒闹钟 */
    fun scheduleTodoReminder(todo: Todo) {
        // 已完成的不调度
        if (todo.status == TodoStatus.COMPLETED) {
            AppLogger.d(TAG, "待办已完成，跳过调度: ${todo.title}")
            return
        }

        val reminderTime = calculateReminderTime(todo) ?: run {
            AppLogger.d(TAG, "待办无时间信息，跳过调度: ${todo.title}")
            return
        }

        // 如果提醒时间已过，不调度
        if (reminderTime <= System.currentTimeMillis()) {
            AppLogger.d(TAG, "待办提醒时间已过，跳过调度: ${todo.title}")
            return
        }

        val requestCode = SafeConverters.safeRequestCode(todo.id, REQUEST_CODE_OFFSET)
        val timeString = todo.getTimeString()

        val intent = Intent(context, TodoNotificationReceiver::class.java).apply {
            action = ACTION_TODO_REMINDER
            putExtra(EXTRA_TODO_ID, todo.id)
            putExtra(EXTRA_TODO_TITLE, todo.title)
            putExtra(EXTRA_TODO_TIME, timeString)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    scheduleAlarmClock(
                        triggerTime = reminderTime,
                        pendingIntent = pendingIntent,
                        label = "待办提醒: ${todo.title}"
                    )
                    AppLogger.d(TAG, "待办提醒 AlarmClock 已设置: ${todo.title}")
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminderTime,
                        pendingIntent
                    )
                    AppLogger.d(TAG, "待办提醒非精确闹钟已设置: ${todo.title}")
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime,
                    pendingIntent
                )
                AppLogger.d(TAG, "待办提醒精确闹钟已设置: ${todo.title}")
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime,
                    pendingIntent
                )
                AppLogger.d(TAG, "待办提醒精确闹钟已设置: ${todo.title}")
            }

            NotificationDebugLogger.log(NotificationDebugLog(
                type = NotificationDebugLog.LogType.ALARM_SCHEDULED,
                result = NotificationDebugLog.LogResult.SUCCESS,
                message = "待办提醒闹钟已设置",
                params = mapOf("todoTitle" to todo.title, "triggerTime" to reminderTime.toString())
            ))
        } catch (e: SecurityException) {
            Log.e(TAG, "无法设置待办提醒闹钟: ${e.message}")
            NotificationDebugLogger.log(NotificationDebugLog(
                type = NotificationDebugLog.LogType.ALARM_SCHEDULED,
                result = NotificationDebugLog.LogResult.FAILURE,
                message = "待办提醒闹钟设置失败",
                params = mapOf("todoTitle" to todo.title, "error" to (e.message ?: ""))
            ))
        }
    }

    /** 取消待办提醒闹钟 */
    fun cancelTodoReminder(todoId: Long) {
        val requestCode = SafeConverters.safeRequestCode(todoId, REQUEST_CODE_OFFSET)
        val intent = Intent(context, TodoNotificationReceiver::class.java).apply {
            action = ACTION_TODO_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let {
            alarmManager.cancel(it)
            AppLogger.d(TAG, "待办提醒闹钟已取消: todoId=$todoId")
        }

        NotificationDebugLogger.log(NotificationDebugLog(
            type = NotificationDebugLog.LogType.ALARM_CANCELLED,
            result = NotificationDebugLog.LogResult.SUCCESS,
            message = "已取消待办提醒闹钟",
            params = mapOf("todoId" to todoId.toString())
        ))
    }

    /** 取消所有待办提醒闹钟 */
    suspend fun cancelAllTodoReminders() {
        val todos = todoDao.getPendingTodosWithTime()
        for (todo in todos) {
            cancelTodoReminder(todo.id)
        }
        AppLogger.d(TAG, "已取消所有待办提醒闹钟，共 ${todos.size} 条")
    }

    /** 重新调度所有待办提醒（开机后调用） */
    suspend fun scheduleAllTodoReminders() {
        val todoNotificationEnabled = settingsDataStore.getTodoNotificationEnabled()
        if (!todoNotificationEnabled) {
            AppLogger.d(TAG, "待办提醒已关闭，跳过调度")
            return
        }

        val todos = todoDao.getPendingTodosWithTime()
        AppLogger.d(TAG, "开始调度待办提醒，共 ${todos.size} 条待办")

        for (todo in todos) {
            scheduleTodoReminder(todo)
        }

        AppLogger.d(TAG, "待办提醒调度完成")
    }

    /** 设置 AlarmClock（显示在系统闹钟列表中） */
    private fun scheduleAlarmClock(triggerTime: Long, pendingIntent: PendingIntent, label: String) {
        val showIntent = Intent(context, com.duoschedule.MainActivity::class.java)
        val showPendingIntent = PendingIntent.getActivity(
            context, 0, showIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmClockInfo = AlarmManager.AlarmClockInfo(
            triggerTime,
            showPendingIntent
        )

        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
        AppLogger.d(TAG, "AlarmClock scheduled: $label at $triggerTime")
    }
}
