package com.duoschedule.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.duoschedule.DuoScheduleApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Boot completed, rescheduling notifications")
            
            val app = context.applicationContext as DuoScheduleApp
            val notificationManager = app.notificationManager

            scope.launch {
                try {
                    notificationManager.scheduleReminderNotifications()
                    Log.d(TAG, "Notifications rescheduled after boot")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to reschedule notifications after boot", e)
                }
            }

            scheduleDailyRescheduleWork(context)
        }
    }

    private fun scheduleDailyRescheduleWork(context: Context) {
        val workRequest = OneTimeWorkRequestBuilder<RescheduleWorker>()
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_NAME_BOOT_RESCHEDULE,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
        Log.d(TAG, "Boot reschedule work enqueued")
    }

    companion object {
        private const val TAG = "BootReceiver"
        private const val WORK_NAME_BOOT_RESCHEDULE = "boot_reschedule_work"
    }
}
