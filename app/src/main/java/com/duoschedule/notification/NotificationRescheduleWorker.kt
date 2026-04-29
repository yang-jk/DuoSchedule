package com.duoschedule.notification

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.duoschedule.DuoScheduleApp
import java.util.concurrent.TimeUnit

class NotificationRescheduleWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        try {
            Log.i(TAG, "========== NotificationRescheduleWorker 开始执行 ==========")
            Log.i(TAG, "当前时间: ${java.time.LocalTime.now()}")
            
            val app = applicationContext as DuoScheduleApp
            
            val hasOngoing = app.notificationManager.checkAndShowOngoingNotification()
            Log.i(TAG, "当前课程检查结果: hasOngoing=$hasOngoing")
            
            app.notificationManager.scheduleReminderNotifications()
            
            Log.i(TAG, "通知重新调度完成")
            Log.i(TAG, "========== NotificationRescheduleWorker 执行完成 ==========")
            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "通知重新调度失败", e)
            return Result.retry()
        }
    }

    companion object {
        private const val TAG = "NotificationRescheduleWorker"
        const val WORK_NAME = "notification_reschedule_work"

        fun schedule(context: Context) {
            Log.d(TAG, "调度定期通知重新调度任务")
            
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresBatteryNotLow(false)
                .setRequiresCharging(false)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<NotificationRescheduleWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setInitialDelay(1, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.UPDATE,
                    workRequest
                )
            
            Log.d(TAG, "定期通知重新调度任务已调度，间隔 15 分钟")
        }
        
        fun scheduleQuickCheck(context: Context) {
            Log.d(TAG, "调度快速检查任务")
            
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresBatteryNotLow(false)
                .setRequiresCharging(false)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<NotificationRescheduleWorker>(
                5, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setInitialDelay(30, TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    "${WORK_NAME}_quick",
                    ExistingPeriodicWorkPolicy.UPDATE,
                    workRequest
                )
            
            Log.d(TAG, "快速检查任务已调度，间隔 5 分钟")
        }
        
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            WorkManager.getInstance(context).cancelUniqueWork("${WORK_NAME}_quick")
            Log.d(TAG, "定期通知重新调度任务已取消")
        }
    }
}
