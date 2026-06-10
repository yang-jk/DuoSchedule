package com.duoschedule.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.duoschedule.MainActivity
import com.duoschedule.R
import com.duoschedule.data.local.TodoDao
import com.duoschedule.data.model.TodoStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 待办提醒广播接收器
 * 接收待办提醒闹钟，显示通知
 */
@AndroidEntryPoint
class TodoNotificationReceiver : BroadcastReceiver() {

    @Inject
    lateinit var todoDao: TodoDao

    @Inject
    lateinit var todoAlarmScheduler: TodoAlarmScheduler

    companion object {
        private const val TAG = "TodoNotificationReceiver"

        /** 通知渠道 ID */
        const val CHANNEL_ID_TODO_REMINDER = "todo_reminder_channel"

        /** 通知 ID 基础值，加上 todoId 作为最终通知 ID */
        private const val NOTIFICATION_ID_BASE = 5000
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "=== TodoNotificationReceiver.onReceive ===")
        Log.d(TAG, "action: ${intent.action}")

        if (intent.action != TodoAlarmScheduler.ACTION_TODO_REMINDER) {
            Log.w(TAG, "未知的 action，跳过")
            return
        }

        val todoId = intent.getLongExtra(TodoAlarmScheduler.EXTRA_TODO_ID, 0L)
        val todoTitle = intent.getStringExtra(TodoAlarmScheduler.EXTRA_TODO_TITLE) ?: ""
        val todoTime = intent.getStringExtra(TodoAlarmScheduler.EXTRA_TODO_TIME) ?: ""

        Log.d(TAG, "待办提醒闹钟触发: todoId=$todoId, title=$todoTitle, time=$todoTime")

        if (todoTitle.isEmpty()) {
            Log.e(TAG, "待办标题为空，跳过通知")
            return
        }

        // 确保通知渠道已创建
        createNotificationChannel(context)

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                // 检查待办是否仍然未完成
                val todo = todoDao.getTodoById(todoId)
                if (todo != null && todo.status == TodoStatus.COMPLETED) {
                    Log.d(TAG, "待办已完成，跳过通知: $todoTitle")
                    return@launch
                }

                // 显示通知
                showTodoReminderNotification(
                    context = context,
                    todoId = todoId,
                    todoTitle = todoTitle,
                    todoTime = todoTime
                )

                Log.d(TAG, "待办提醒通知已发送: $todoTitle")

                NotificationDebugLogger.log(NotificationDebugLog(
                    type = NotificationDebugLog.LogType.REMINDER,
                    result = NotificationDebugLog.LogResult.SUCCESS,
                    message = "待办提醒通知已发送",
                    params = mapOf("todoTitle" to todoTitle, "todoTime" to todoTime)
                ))
            } catch (e: Exception) {
                Log.e(TAG, "发送待办提醒通知失败", e)
                NotificationDebugLogger.log(NotificationDebugLog(
                    type = NotificationDebugLog.LogType.REMINDER,
                    result = NotificationDebugLog.LogResult.FAILURE,
                    message = "待办提醒通知发送失败",
                    params = mapOf("todoTitle" to todoTitle, "error" to (e.message ?: ""))
                ))
            } finally {
                pendingResult.finish()
            }
        }
    }

    /** 显示待办提醒通知 */
    private fun showTodoReminderNotification(
        context: Context,
        todoId: Long,
        todoTitle: String,
        todoTime: String
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 点击通知打开待办编辑页
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to_todo_edit", true)
            putExtra("todo_id", todoId)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            todoId.toInt(),
            contentIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val contentText = if (todoTime.isNotEmpty()) {
            "$todoTime · 即将到来"
        } else {
            "即将到来"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_TODO_REMINDER)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(todoTitle)
            .setContentText(contentText)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(if (todoTime.isNotEmpty()) "$todoTime\n即将到来，请做好准备" else "即将到来，请做好准备")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        val notificationId = (NOTIFICATION_ID_BASE + todoId % Int.MAX_VALUE).toInt()
        notificationManager.notify(notificationId, notification)
        Log.d(TAG, "通知已发送: id=$notificationId, title=$todoTitle")
    }

    /** 创建待办提醒通知渠道 */
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // 检查渠道是否已存在
            if (notificationManager.getNotificationChannel(CHANNEL_ID_TODO_REMINDER) != null) {
                return
            }

            val channel = NotificationChannel(
                CHANNEL_ID_TODO_REMINDER,
                "待办提醒",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "待办事项的提醒通知"
                enableVibration(true)
                setShowBadge(true)
            }

            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "待办提醒通知渠道已创建")
        }
    }
}
