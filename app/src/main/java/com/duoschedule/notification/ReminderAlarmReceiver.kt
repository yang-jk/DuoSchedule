package com.duoschedule.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReminderAlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationManager: CourseNotificationManager

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "=== ReminderAlarmReceiver.onReceive ===")
        Log.d(TAG, "action: ${intent.action}")

        if (intent.action == CourseNotificationManager.ACTION_REMINDER_ALARM) {
            val courseName = intent.getStringExtra("course_name")
            val courseId = intent.getLongExtra(CourseNotificationManager.EXTRA_COURSE_ID, 0L)
            val courseLocation = intent.getStringExtra("course_location") ?: ""
            val startHour = intent.getIntExtra("start_hour", 0)
            val startMinute = intent.getIntExtra("start_minute", 0)
            val advanceMinutes = intent.getIntExtra("advance_minutes", 5)

            Log.d(TAG, "Alarm triggered, sending reminder: $courseName")

            if (courseName.isNullOrEmpty()) {
                Log.e(TAG, "Course name is empty, skip notification")
                return
            }

            NotificationDebugLogger.log(NotificationDebugLog(
                type = NotificationDebugLog.LogType.ALARM_TRIGGERED,
                result = NotificationDebugLog.LogResult.SUCCESS,
                message = "课前提醒闹钟触发",
                params = mapOf("courseName" to courseName)
            ))

            val pendingResult = goAsync()

            CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                try {
                    notificationManager.showReminderNotification(
                        courseId = courseId,
                        courseName = courseName,
                        courseLocation = courseLocation,
                        startHour = startHour,
                        startMinute = startMinute,
                        advanceMinutes = advanceMinutes
                    )
                    Log.d(TAG, "Reminder notification sent: $courseName")
                    notificationManager.scheduleReminderNotifications()
                    Log.d(TAG, "Notifications rescheduled after reminder triggered")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to send reminder notification", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    companion object {
        private const val TAG = "ReminderAlarmReceiver"
    }
}
