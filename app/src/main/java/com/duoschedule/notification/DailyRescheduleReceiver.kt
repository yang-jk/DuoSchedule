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

class DailyRescheduleReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == CourseNotificationManager.ACTION_DAILY_RESCHEDULE) {
            Log.d(TAG, "Daily reschedule triggered")

            val app = context.applicationContext as DuoScheduleApp
            val notificationManager = app.notificationManager

            scope.launch {
                try {
                    notificationManager.scheduleReminderNotifications()
                    Log.d(TAG, "Daily reschedule completed")
                } catch (e: Exception) {
                    Log.e(TAG, "Daily reschedule failed", e)
                }
            }
        }
    }

    companion object {
        private const val TAG = "DailyRescheduleReceiver"
    }
}
