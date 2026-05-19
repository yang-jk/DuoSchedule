package com.duoschedule.notification

import android.content.Context
import android.util.Log
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

    companion object {
        private const val TAG = "RescheduleWorker"
    }

    override suspend fun doWork(): Result {
        return try {
            val isEnabled = settingsDataStore.getNotificationEnabled()
            if (!isEnabled) {
                return Result.success()
            }

            val allCourses = courseDao.getAllCoursesSync()
            if (allCourses.isEmpty()) {
                Log.i(TAG, "No courses found, skipping reschedule")
                return Result.success()
            }

            notificationManager.scheduleReminderNotifications()

            val today = LocalDate.now()
            val currentTime = LocalTime.now()
            val currentWeekA = settingsDataStore.getCurrentWeek(
                com.duoschedule.data.model.PersonType.PERSON_A
            ).first()

            val personACourses = courseDao.getCoursesForDaySync(
                dayOfWeek = today.dayOfWeek.value,
                personType = com.duoschedule.data.model.PersonType.PERSON_A
            ).filter { it.isInWeek(currentWeekA) }

            for (course in personACourses) {
                val courseStartTime = LocalTime.of(course.startHour, course.startMinute)
                val courseEndTime = LocalTime.of(course.endHour, course.endMinute)

                if (currentTime.isAfter(courseStartTime) && currentTime.isBefore(courseEndTime)) {
                    val remainingMinutes = java.time.Duration.between(currentTime, courseEndTime).toMinutes().toInt()
                    LiveUpdateService.start(
                        context = context,
                        courseName = course.name,
                        courseLocation = course.location ?: "",
                        remainingMinutes = remainingMinutes,
                        endHour = course.endHour,
                        endMinute = course.endMinute,
                        totalMinutes = course.duration
                    )
                    break
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
