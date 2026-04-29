package com.duoschedule.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalTime

@HiltWorker
class RescheduleWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val notificationManager: CourseNotificationManager,
    private val settingsDataStore: com.duoschedule.data.local.SettingsDataStore,
    private val courseDao: com.duoschedule.data.local.CourseDao
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val isEnabled = settingsDataStore.getNotificationEnabled()
            if (!isEnabled) {
                return Result.success()
            }

            val today = LocalDate.now()
            val currentTime = LocalTime.now()
            val currentWeekB = settingsDataStore.getCurrentWeek(
                com.duoschedule.data.model.PersonType.PERSON_B
            ).first()

            val personBCourses = courseDao.getCoursesForDaySync(
                dayOfWeek = today.dayOfWeek.value,
                personType = com.duoschedule.data.model.PersonType.PERSON_B
            ).filter { it.isInWeek(currentWeekB) }

            for (course in personBCourses) {
                val courseStartTime = LocalTime.of(course.startHour, course.startMinute)
                val courseEndTime = courseStartTime.plusMinutes(course.duration.toLong())
                val advanceMinutes = settingsDataStore.getNotificationAdvanceTime()
                val reminderTime = courseStartTime.minusMinutes(advanceMinutes.toLong())

                if (currentTime.isBefore(courseStartTime)) {
                    if (reminderTime.isAfter(currentTime)) {
                        notificationManager.scheduleReminderNotifications()
                    }
                }

                if (currentTime.isAfter(courseStartTime) && currentTime.isBefore(courseEndTime)) {
                    val remainingMinutes = java.time.Duration.between(currentTime, courseEndTime).toMinutes().toInt()
                    notificationManager.showOngoingNotification(
                        courseName = course.name,
                        courseLocation = course.location ?: "",
                        remainingMinutes = remainingMinutes
                    )
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
