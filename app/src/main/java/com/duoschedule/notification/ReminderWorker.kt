package com.duoschedule.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val notificationManager: CourseNotificationManager,
    private val settingsDataStore: com.duoschedule.data.local.SettingsDataStore
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val courseName = inputData.getString("course_name") ?: return Result.failure()
        val courseLocation = inputData.getString("course_location") ?: ""
        val startHour = inputData.getInt("start_hour", 0)
        val startMinute = inputData.getInt("start_minute", 0)
        val advanceMinutes = settingsDataStore.getNotificationAdvanceTime()

        notificationManager.showReminderNotification(
            courseName = courseName,
            courseLocation = courseLocation,
            startHour = startHour,
            startMinute = startMinute,
            advanceMinutes = advanceMinutes
        )

        return Result.success()
    }
}
