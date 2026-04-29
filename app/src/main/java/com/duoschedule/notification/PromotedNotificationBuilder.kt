package com.duoschedule.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.duoschedule.R
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object PromotedNotificationBuilder {
    private const val TAG = "PromotedNotification"

    const val CHANNEL_ID_REMINDER = "course_reminder_channel"
    const val CHANNEL_ID_ONGOING = "course_ongoing_channel"
    const val CHANNEL_ID_LIVE = "course_live_channel"

    fun createNotificationChannels(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

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
            description = "课程进行中的实时状态"
            enableVibration(false)
            setSound(null, null)
            setShowBadge(true)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }

        val liveChannel = NotificationChannel(
            CHANNEL_ID_LIVE,
            "实况通知",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "在锁屏与状态栏展示更突出的课程状态"
            enableVibration(false)
            setSound(null, null)
            setShowBadge(true)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }

        notificationManager.createNotificationChannels(listOf(reminderChannel, ongoingChannel, liveChannel))
        Log.d(TAG, "Notification channels created")
    }

    fun canPostPromotedNotifications(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= 36) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.canPostPromotedNotifications()
        } else {
            false
        }
    }

    fun buildOngoingNotification(
        context: Context,
        courseName: String,
        location: String,
        remainingMinutes: Int,
        totalMinutes: Int,
        pendingIntent: PendingIntent?,
        liveNotificationEnabled: Boolean,
        style: PromotedNotificationStyle? = null,
        courseEndTime: LocalTime? = null
    ): Notification {
        val canPostPromoted = canPostPromotedNotifications(context)
        val useLiveNotification = liveNotificationEnabled && canPostPromoted && Build.VERSION.SDK_INT >= 36

        val channelId = if (useLiveNotification) CHANNEL_ID_LIVE else CHANNEL_ID_ONGOING
        
        val now = LocalTime.now()
        val (endTime, isEnded) = if (courseEndTime != null) {
            val durationMillis = java.time.Duration.between(now, courseEndTime).toMillis()
            if (durationMillis <= 0) {
                Pair(System.currentTimeMillis(), true)
            } else {
                Pair(System.currentTimeMillis() + durationMillis, false)
            }
        } else {
            Pair(System.currentTimeMillis() + remainingMinutes * 60 * 1000L, remainingMinutes <= 0)
        }

        val notificationStyle = style ?: PromotedNotificationStyle.createOngoingStyle(
            courseName = courseName,
            location = location,
            remainingMinutes = remainingMinutes,
            totalMinutes = totalMinutes
        )

        Log.d(TAG, "=== Build Ongoing Notification ===")
        Log.d(TAG, "SDK: ${Build.VERSION.SDK_INT}, required: 36")
        Log.d(TAG, "liveNotificationEnabled: $liveNotificationEnabled")
        Log.d(TAG, "canPostPromotedNotifications: $canPostPromoted")
        Log.d(TAG, "useLiveNotification: $useLiveNotification")
        Log.d(TAG, "Course: $courseName, remaining: ${remainingMinutes}min")

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(courseName)
            .setContentText("$location · ${remainingMinutes}m")
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setUsesChronometer(true)
            .setChronometerCountDown(true)
            .setWhen(endTime)
            .setStyle(createProgressStyle(notificationStyle))

        if (useLiveNotification) {
            builder.setRequestPromotedOngoing(true)
            builder.setShortCriticalText("${remainingMinutes}m")
            Log.d(TAG, ">>> Live notification enabled <<<")
        }

        if (isEnded) {
            builder.setOngoing(false)
            builder.setRequestPromotedOngoing(false)
            builder.setAutoCancel(true)
            builder.setContentTitle("$courseName ended")
            builder.setContentText(location)
            builder.setUsesChronometer(false)
            Log.d(TAG, "Course ended: $courseName")
        }

        return builder.build()
    }

    fun buildReminderNotification(
        context: Context,
        courseName: String,
        location: String,
        startHour: Int,
        startMinute: Int,
        advanceMinutes: Int,
        pendingIntent: PendingIntent?,
        style: PromotedNotificationStyle? = null
    ): Notification {
        val startTime = String.format(Locale.ROOT, "%02d:%02d", startHour, startMinute)

        Log.d(TAG, "=== Build Reminder Notification ===")
        Log.d(TAG, "Course: $courseName, $startTime, advance: $advanceMinutes min")

        return NotificationCompat.Builder(context, CHANNEL_ID_REMINDER)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(courseName)
            .setContentText("$location · $startTime")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$location\nStarts at $startTime, $advanceMinutes min")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun createProgressStyle(style: PromotedNotificationStyle): NotificationCompat.Style {
        return NotificationCompat.BigTextStyle()
            .setBigContentTitle(style.expandedState.baseInfo.title)
            .bigText("${style.expandedState.baseInfo.content} · ${style.expandedState.baseInfo.subTitle}")
    }

    fun buildServiceNotification(
        context: Context,
        contentText: String,
        pendingIntent: PendingIntent?
    ): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID_ONGOING)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Course Service")
            .setContentText(contentText)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
    
    fun buildWaitingNotification(
        context: Context,
        pendingIntent: PendingIntent?
    ): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID_ONGOING)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("等待课程开始")
            .setContentText("正在监控课程状态...")
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .build()
    }
}
