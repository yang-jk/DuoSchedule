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
class DailyRescheduleReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationManager: CourseNotificationManager

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == CourseNotificationManager.ACTION_DAILY_RESCHEDULE) {
            Log.d(TAG, "Daily reschedule triggered")

            NotificationDebugLogger.log(NotificationDebugLog(
                type = NotificationDebugLog.LogType.ALARM_TRIGGERED,
                result = NotificationDebugLog.LogResult.SUCCESS,
                message = "每日重新调度闹钟触发"
            ))

            val pendingResult = goAsync()

            CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                try {
                    notificationManager.scheduleReminderNotifications()
                    Log.d(TAG, "Daily reschedule completed")
                } catch (e: Exception) {
                    Log.e(TAG, "Daily reschedule failed", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    companion object {
        private const val TAG = "DailyRescheduleReceiver"
    }
}
