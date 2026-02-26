package com.duoschedule.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import com.duoschedule.MainActivity
import com.duoschedule.R
import com.duoschedule.data.local.CourseDao
import com.duoschedule.data.local.SettingsDataStore
import com.duoschedule.data.model.Course
import com.duoschedule.data.model.PersonType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CourseNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val courseDao: CourseDao,
    private val settingsDataStore: SettingsDataStore
) {
    companion object {
        private const val TAG = "CourseNotification"
        const val CHANNEL_ID_REMINDER = "course_reminder_channel"
        const val CHANNEL_ID_ONGOING = "course_ongoing_channel"
        const val NOTIFICATION_ID_REMINDER = 1001
        const val NOTIFICATION_ID_ONGOING = 1002
        
        const val WORK_NAME_REMINDER = "course_reminder_work"
        const val WORK_NAME_ONGOING = "course_ongoing_work"
        
        const val PREF_NOTIFICATION_ENABLED = "notification_enabled"
        const val PREF_NOTIFICATION_ADVANCE_TIME = "notification_advance_time"
        const val PREF_ISLAND_DISPLAY_MODE = "island_display_mode"
    }

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun canPostPromotedNotifications(): Boolean {
        return if (Build.VERSION.SDK_INT >= 36) {
            notificationManager.canPostPromotedNotifications()
        } else {
            false
        }
    }

    fun requestPromotedNotificationsPermission(): Boolean {
        return canPostPromotedNotifications()
    }

    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val reminderChannel = NotificationChannel(
                CHANNEL_ID_REMINDER,
                "课程提醒",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "课程开始前的提醒通知"
                enableVibration(true)
            }
            
            val ongoingChannel = NotificationChannel(
                CHANNEL_ID_ONGOING,
                "上课状态",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "课程进行中的实时状态和实况通知"
                enableVibration(false)
                setSound(null, null)
                setShowBadge(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            
            notificationManager.createNotificationChannels(listOf(reminderChannel, ongoingChannel))
            Log.d(TAG, "通知渠道已创建")
        }
    }

    suspend fun scheduleReminderNotifications() {
        val isEnabled = settingsDataStore.getNotificationEnabled()
        if (!isEnabled) {
            cancelReminderNotifications()
            return
        }

        val advanceMinutes = settingsDataStore.getNotificationAdvanceTime()
        val today = LocalDate.now()
        val currentWeekB = settingsDataStore.getCurrentWeek(PersonType.PERSON_B).first()

        val personBCourses = courseDao.getCoursesForDaySync(
            dayOfWeek = today.dayOfWeek.value,
            personType = PersonType.PERSON_B
        ).filter { it.isInWeek(currentWeekB) }

        val currentTime = LocalTime.now()
        val upcomingCourses = personBCourses
            .filter { course ->
                val courseStartTime = LocalTime.of(course.startHour, course.startMinute)
                courseStartTime.isAfter(currentTime)
            }
            .sortedBy { it.startHour * 60 + it.startMinute }

        for (course in upcomingCourses) {
            val courseStartTime = LocalTime.of(course.startHour, course.startMinute)
            val reminderTime = courseStartTime.minusMinutes(advanceMinutes.toLong())
            
            if (reminderTime.isAfter(currentTime)) {
                scheduleReminderWork(course, reminderTime, advanceMinutes)
            }
        }
    }

    private fun scheduleReminderWork(course: Course, reminderTime: LocalTime, advanceMinutes: Int) {
        val currentTime = LocalTime.now()
        val delayMinutes = java.time.Duration.between(currentTime, reminderTime).toMinutes()
        
        if (delayMinutes <= 0) return

        val workData = workDataOf(
            "course_id" to course.id,
            "course_name" to course.name,
            "course_location" to (course.location ?: ""),
            "person_type" to PersonType.PERSON_B.name,
            "start_hour" to course.startHour,
            "start_minute" to course.startMinute,
            "advance_minutes" to advanceMinutes
        )

        val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
            .setInputData(workData)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "${WORK_NAME_REMINDER}_${course.id}",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun showReminderNotification(
        courseName: String,
        courseLocation: String,
        startHour: Int,
        startMinute: Int,
        advanceMinutes: Int
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val startTime = String.format("%02d:%02d", startHour, startMinute)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && MiuiIslandHelper.isMiuiDevice()) {
            val miuiNotification = MiuiIslandHelper.createMiuiReminderNotification(
                context = context,
                channelId = CHANNEL_ID_REMINDER,
                courseName = courseName,
                location = courseLocation,
                advanceMinutes = advanceMinutes,
                pendingIntent = pendingIntent
            )
            
            if (miuiNotification != null) {
                notificationManager.notify(NOTIFICATION_ID_REMINDER, miuiNotification)
                Log.d(TAG, "小米动态岛提醒通知已发送: $courseName")
                return
            }
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_REMINDER)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("即将上课")
            .setContentText("$courseName · $courseLocation · 还有 $advanceMinutes 分钟")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$courseName\n$courseLocation\n$startTime 开始，还有 $advanceMinutes 分钟")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID_REMINDER, notification)
    }

    suspend fun showOngoingNotification(
        courseName: String,
        courseLocation: String,
        remainingMinutes: Int
    ) {
        val liveNotificationEnabled = settingsDataStore.getLiveNotificationEnabled()
        val canPostPromoted = canPostPromotedNotifications()
        
        Log.d(TAG, "=== 实况通知调试 ===")
        Log.d(TAG, "SDK版本: ${Build.VERSION.SDK_INT}, 需要: 36")
        Log.d(TAG, "liveNotificationEnabled: $liveNotificationEnabled")
        Log.d(TAG, "canPostPromotedNotifications: $canPostPromoted")
        Log.d(TAG, "课程: $courseName, 剩余: ${remainingMinutes}分钟")

        if (!NotificationThrottler.shouldNotify(courseName, remainingMinutes)) {
            Log.d(TAG, "通知节流中，跳过更新")
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val endTime = System.currentTimeMillis() + remainingMinutes * 60 * 1000L

        if (Build.VERSION.SDK_INT >= 36) {
            if (MiuiIslandHelper.isMiuiDevice()) {
                val miuiNotification = MiuiIslandHelper.createMiuiIslandNotification(
                    context = context,
                    channelId = CHANNEL_ID_ONGOING,
                    courseName = courseName,
                    location = courseLocation,
                    remainingMinutes = remainingMinutes,
                    pendingIntent = pendingIntent,
                    notificationId = NOTIFICATION_ID_ONGOING
                )
                
                if (miuiNotification != null) {
                    notificationManager.notify(NOTIFICATION_ID_ONGOING, miuiNotification)
                    Log.d(TAG, "小米动态岛 Live Update 通知已发送: $courseName, 剩余${remainingMinutes}分钟")
                    return
                }
            }

            val useLiveNotification = liveNotificationEnabled && canPostPromoted
            
            Log.d(TAG, "useLiveNotification: $useLiveNotification, channelId: $CHANNEL_ID_ONGOING")
            
            val builder = NotificationCompat.Builder(context, CHANNEL_ID_ONGOING)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(courseName)
                .setContentText("$courseLocation · 剩余 $remainingMinutes 分钟")
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOnlyAlertOnce(true)
                .setUsesChronometer(true)
                .setChronometerCountDown(true)
                .setWhen(endTime)
                .setRequestPromotedOngoing(useLiveNotification)

            if (useLiveNotification) {
                builder.setShortCriticalText("剩余 $remainingMinutes 分钟")
                Log.d(TAG, ">>> 实况通知已启用 <<<")
            } else {
                Log.w(TAG, "实况通知未启用: liveEnabled=$liveNotificationEnabled, canPost=$canPostPromoted")
            }
            
            if (remainingMinutes <= 0) {
                builder.setOngoing(false)
                builder.setRequestPromotedOngoing(false)
                builder.setAutoCancel(true)
                builder.setContentTitle("$courseName 已结束")
                builder.setContentText(courseLocation)
                Log.d(TAG, "课程已结束: $courseName")
            }

            notificationManager.notify(NOTIFICATION_ID_ONGOING, builder.build())
            Log.d(TAG, "通知已发送到系统, notificationId=$NOTIFICATION_ID_ONGOING")
        } else {
            val builder = NotificationCompat.Builder(context, CHANNEL_ID_ONGOING)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(courseName)
                .setContentText(courseLocation)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOnlyAlertOnce(true)
                .setUsesChronometer(true)
                .setChronometerCountDown(true)
                .setWhen(endTime)
                .setSubText("剩余 $remainingMinutes 分钟")

            notificationManager.notify(NOTIFICATION_ID_ONGOING, builder.build())
            Log.d(TAG, "实时通知已发送 (Android < 15): $courseName, 剩余${remainingMinutes}分钟")
        }
    }

    fun cancelReminderNotifications() {
        WorkManager.getInstance(context).cancelAllWorkByTag(WORK_NAME_REMINDER)
        notificationManager.cancel(NOTIFICATION_ID_REMINDER)
    }

    fun cancelOngoingNotification() {
        notificationManager.cancel(NOTIFICATION_ID_ONGOING)
        LiveUpdateService.stop(context)
    }
    
    fun startLiveUpdateService() {
        if (!LiveUpdateService.isServiceRunning()) {
            LiveUpdateService.start(context)
        }
    }

    data class CourseOngoingState(
        val courseName: String,
        val courseLocation: String,
        val remainingMinutes: Int
    )
}
