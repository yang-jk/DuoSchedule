package com.duoschedule.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.duoschedule.DuoScheduleApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ReminderAlarmReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "=== ReminderAlarmReceiver.onReceive ===")
        Log.d(TAG, "action: ${intent.action}")
        Log.d(TAG, "expected action: ${CourseNotificationManager.ACTION_REMINDER_ALARM}")
        
        if (intent.action == CourseNotificationManager.ACTION_REMINDER_ALARM) {
            val courseName = intent.getStringExtra("course_name")
            val courseLocation = intent.getStringExtra("course_location") ?: ""
            val startHour = intent.getIntExtra("start_hour", 0)
            val startMinute = intent.getIntExtra("start_minute", 0)
            val advanceMinutes = intent.getIntExtra("advance_minutes", 5)

            Log.d(TAG, "Alarm triggered, sending reminder: $courseName")
            Log.d(TAG, "Course info: $courseName, $courseLocation, $startHour:$startMinute, advance=$advanceMinutes min")

            if (courseName.isNullOrEmpty()) {
                Log.e(TAG, "Course name is empty, skip notification")
                return
            }

            val app = context.applicationContext as DuoScheduleApp
            val notificationManager = app.notificationManager

            scope.launch {
                try {
                    notificationManager.showReminderNotification(
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
                }
            }
        } else {
            Log.w(TAG, "Unknown action: ${intent.action}")
        }
    }

    companion object {
        private const val TAG = "ReminderAlarmReceiver"
    }
}
