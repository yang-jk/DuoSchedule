package com.duoschedule.notification

import android.Manifest
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
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
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CourseNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val courseDao: CourseDao,
    private val settingsDataStore: SettingsDataStore,
    private val ringerModeManager: RingerModeManager
) {
    companion object {
        private const val TAG = "CourseNotification"
        const val CHANNEL_ID_REMINDER = "course_reminder_channel"
        const val CHANNEL_ID_ONGOING = "course_ongoing_channel"
        const val CHANNEL_ID_LIVE = "course_live_channel"
        
        const val NOTIFICATION_ID_REMINDER = 1001
        const val NOTIFICATION_ID_ONGOING = 1002
        
        const val WORK_NAME_REMINDER = "course_reminder_work"
        const val WORK_NAME_ONGOING = "course_ongoing_work"
        
        const val ACTION_REMINDER_ALARM = "com.duoschedule.action.REMINDER_ALARM"
        const val ACTION_DAILY_RESCHEDULE = "com.duoschedule.action.DAILY_RESCHEDULE"
        const val EXTRA_COURSE_ID = "course_id"
    }

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun canPostPromotedNotifications(): Boolean {
        return PromotedNotificationBuilder.canPostPromotedNotifications(context)
    }

    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun createNotificationChannels() {
        PromotedNotificationBuilder.createNotificationChannels(context)
        Log.d(TAG, "通知渠道已创建")
    }

    suspend fun scheduleReminderNotifications() {
        Log.i(TAG, "========== 开始调度通知 ==========")
        Log.i(TAG, "当前时间: ${LocalTime.now()}")
        Log.i(TAG, "当前日期: ${LocalDate.now()}, 星期${LocalDate.now().dayOfWeek.value}")
        
        val isEnabled = settingsDataStore.getNotificationEnabled()
        Log.i(TAG, "通知开关: $isEnabled")
        
        if (!isEnabled) {
            cancelReminderNotifications()
            cancelOngoingNotifications()
            Log.i(TAG, "通知已禁用，取消所有通知")
            return
        }

        if (!hasNotificationPermission()) {
            Log.w(TAG, "没有通知权限，无法调度通知")
            cancelReminderNotifications()
            cancelOngoingNotifications()
            return
        }

        val advanceMinutes = settingsDataStore.getNotificationAdvanceTime()
        Log.d(TAG, "提前提醒时间: $advanceMinutes 分钟")
        
        val canScheduleExact = canScheduleExactAlarms()
        Log.d(TAG, "精确闹钟权限: $canScheduleExact")
        
        val today = LocalDate.now()
        val currentWeekB = settingsDataStore.getCurrentWeek(PersonType.PERSON_B).first()
        Log.d(TAG, "当前周(PersonB): $currentWeekB")

        val personBCourses = courseDao.getCoursesForDaySync(
            dayOfWeek = today.dayOfWeek.value,
            personType = PersonType.PERSON_B
        )
        
        Log.d(TAG, "今日PersonB课程总数: ${personBCourses.size}")
        personBCourses.forEach { course ->
            Log.d(TAG, "  - ${course.name}: 周${course.dayOfWeek}, ${course.startHour}:${course.startMinute}, 周次${course.startWeek}-${course.endWeek}, isInWeek=${course.isInWeek(currentWeekB)}")
        }
        
        val filteredCourses = personBCourses.filter { it.isInWeek(currentWeekB) }
        Log.d(TAG, "本周有效课程数: ${filteredCourses.size}")

        val currentTime = LocalTime.now()
        val upcomingCourses = filteredCourses
            .filter { course ->
                val courseStartTime = LocalTime.of(course.startHour, course.startMinute)
                courseStartTime.isAfter(currentTime)
            }
            .sortedBy { it.startHour * 60 + it.startMinute }
        
        Log.d(TAG, "即将开始的课程数: ${upcomingCourses.size}")
        upcomingCourses.forEach { course ->
            Log.d(TAG, "  即将开始: ${course.name} at ${course.startHour}:${course.startMinute}")
        }

        for (course in upcomingCourses) {
            val courseStartTime = LocalTime.of(course.startHour, course.startMinute)
            val reminderTime = courseStartTime.minusMinutes(advanceMinutes.toLong())
            
            Log.d(TAG, "----------")
            Log.d(TAG, "课程：${course.name}")
            Log.d(TAG, "  开始时间：$courseStartTime")
            Log.d(TAG, "  提醒时间：$reminderTime")
            Log.d(TAG, "  当前时间：$currentTime")
            
            if (reminderTime.isAfter(currentTime)) {
                scheduleReminderWithAlarm(course, reminderTime, advanceMinutes)
            } else {
                Log.d(TAG, "  提醒时间已过，跳过")
            }
            
            if (courseStartTime.isAfter(currentTime)) {
                scheduleOngoingCourseAlarm(course)
                
                val preStartTime = courseStartTime.minusMinutes(5)
                if (preStartTime.isAfter(currentTime)) {
                    schedulePreStartServiceAlarm(course, preStartTime)
                }
            }
            
            val preCheckTime = courseStartTime.minusMinutes(30)
            if (preCheckTime.isAfter(currentTime)) {
                schedulePreCheckAlarm(course, preCheckTime)
            }
        }

        scheduleAutoSilentTasks()
        scheduleDailyReschedule()
        
        Log.i(TAG, "========== 通知调度完成 ==========")
        Log.i(TAG, "共调度 ${upcomingCourses.size} 个课程的提醒")
    }
    
    private fun scheduleOngoingCourseAlarm(course: Course) {
        val courseStartTime = LocalTime.of(course.startHour, course.startMinute)
        val courseStartDateTime = LocalDateTime.of(LocalDate.now(), courseStartTime)
        val triggerTime = courseStartDateTime.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000

        Log.d(TAG, "scheduleOngoingCourseAlarm: ${course.name}")
        Log.d(TAG, "  courseStartTime: $courseStartTime")
        Log.d(TAG, "  triggerTime(ms): $triggerTime")
        Log.d(TAG, "  currentTime(ms): ${System.currentTimeMillis()}")

        if (triggerTime <= System.currentTimeMillis()) {
            Log.d(TAG, "  触发时间已过，跳过")
            return
        }

        val intent = Intent(context, OngoingCourseReceiver::class.java).apply {
            action = OngoingCourseReceiver.ACTION_COURSE_START
            putExtra(OngoingCourseReceiver.EXTRA_COURSE_NAME, course.name)
            putExtra(OngoingCourseReceiver.EXTRA_COURSE_LOCATION, course.location ?: "")
            putExtra(OngoingCourseReceiver.EXTRA_DURATION, course.duration)
            putExtra(OngoingCourseReceiver.EXTRA_END_HOUR, course.endHour)
            putExtra(OngoingCourseReceiver.EXTRA_END_MINUTE, course.endMinute)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            course.id.hashCode() + 10000,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (canScheduleExactAlarms()) {
                    scheduleAlarmClock(
                        triggerTime = triggerTime,
                        pendingIntent = pendingIntent,
                        label = "课程开始: ${course.name}"
                    )
                    Log.d(TAG, "  课程开始AlarmClock已设置: ${course.name} at $courseStartTime")
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                    Log.d(TAG, "  课程开始闹钟已设置(非精确-无权限): ${course.name} at $courseStartTime")
                }
            } else {
                scheduleAlarmClock(
                    triggerTime = triggerTime,
                    pendingIntent = pendingIntent,
                    label = "课程开始: ${course.name}"
                )
                Log.d(TAG, "  课程开始AlarmClock已设置: ${course.name} at $courseStartTime")
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "  无法设置课程开始闹钟: ${e.message}")
        }
    }
    
    private fun schedulePreStartServiceAlarm(course: Course, preStartTime: LocalTime) {
        val preStartDateTime = LocalDateTime.of(LocalDate.now(), preStartTime)
        val triggerTime = preStartDateTime.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000

        Log.d(TAG, "schedulePreStartServiceAlarm: ${course.name}")
        Log.d(TAG, "  preStartTime: $preStartTime")
        Log.d(TAG, "  triggerTime(ms): $triggerTime")

        if (triggerTime <= System.currentTimeMillis()) {
            Log.d(TAG, "  触发时间已过，跳过")
            return
        }

        val intent = Intent(context, PreStartServiceReceiver::class.java).apply {
            action = PreStartServiceReceiver.ACTION_PRE_START
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            course.id.hashCode() + 60000,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                    Log.i(TAG, "  预启动服务闹钟已设置(精确): ${course.name} at $preStartTime")
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                    Log.i(TAG, "  预启动服务闹钟已设置(非精确): ${course.name} at $preStartTime")
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
                Log.i(TAG, "  预启动服务闹钟已设置: ${course.name} at $preStartTime")
            }
        } catch (e: Exception) {
            Log.e(TAG, "  无法设置预启动服务闹钟: ${e.message}")
        }
    }
    
    private fun cancelOngoingNotifications() {
        notificationManager.cancel(NOTIFICATION_ID_ONGOING)
        LiveUpdateService.stop(context)
        Log.d(TAG, "已取消上课中通知")
    }
    
    private fun schedulePreCheckAlarm(course: Course, preCheckTime: LocalTime) {
        val preCheckDateTime = LocalDateTime.of(LocalDate.now(), preCheckTime)
        val triggerTime = preCheckDateTime.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000

        Log.d(TAG, "schedulePreCheckAlarm: ${course.name}")
        Log.d(TAG, "  preCheckTime: $preCheckTime")
        Log.d(TAG, "  triggerTime(ms): $triggerTime")
        Log.d(TAG, "  currentTime(ms): ${System.currentTimeMillis()}")

        if (triggerTime <= System.currentTimeMillis()) {
            Log.d(TAG, "  触发时间已过，跳过")
            return
        }

        val intent = Intent(context, DailyRescheduleReceiver::class.java).apply {
            action = ACTION_DAILY_RESCHEDULE
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            course.id.hashCode() + 50000,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
            Log.i(TAG, "  课前检查闹钟已设置：${course.name} at $preCheckTime")
        } catch (e: Exception) {
            Log.e(TAG, "  无法设置课前检查闹钟：${e.message}")
        }
    }

    suspend fun scheduleAutoSilentTasks() {
        Log.i(TAG, "========== 开始调度自动静音任务 ==========")
        
        val autoSilentEnabled = settingsDataStore.getAutoSilentEnabled()
        Log.d(TAG, "自动静音开关: $autoSilentEnabled")
        
        if (!autoSilentEnabled) {
            Log.d(TAG, "自动静音功能已关闭，跳过调度")
            return
        }

        if (!ringerModeManager.hasNotificationPolicyAccess()) {
            Log.w(TAG, "没有勿扰模式权限，无法调度自动静音任务")
            return
        }

        val advanceTime = settingsDataStore.getAutoSilentAdvanceTime()
        Log.d(TAG, "静音提前时间: $advanceTime 分钟")

        val today = LocalDate.now()
        val currentWeekB = settingsDataStore.getCurrentWeek(PersonType.PERSON_B).first()
        Log.d(TAG, "当前日期: $today, 星期${today.dayOfWeek.value}")
        Log.d(TAG, "当前周(PersonB): $currentWeekB")

        val personBCourses = courseDao.getCoursesForDaySync(
            dayOfWeek = today.dayOfWeek.value,
            personType = PersonType.PERSON_B
        ).filter { it.isInWeek(currentWeekB) }
        
        Log.d(TAG, "今日课程数: ${personBCourses.size}")

        val currentTime = LocalTime.now()
        Log.d(TAG, "当前时间: $currentTime")
        
        val activeAndUpcomingCourses = personBCourses
            .filter { course ->
                val courseEndTime = LocalTime.of(course.endHour, course.endMinute)
                courseEndTime.isAfter(currentTime)
            }
            .sortedBy { it.startHour * 60 + it.startMinute }

        Log.d(TAG, "进行中及即将开始的课程数: ${activeAndUpcomingCourses.size}")
        
        for (course in activeAndUpcomingCourses) {
            Log.d(TAG, "  - ${course.name}: ${course.startHour}:${course.startMinute} - ${course.endHour}:${course.endMinute}")
        }

        for (course in activeAndUpcomingCourses) {
            val courseStartTime = LocalTime.of(course.startHour, course.startMinute)
            val courseEndTime = LocalTime.of(course.endHour, course.endMinute)
            val silentStartTime = courseStartTime.minusMinutes(advanceTime.toLong())
            
            val delayToSilentStart = java.time.Duration.between(currentTime, silentStartTime).toMinutes()
            val delayToCourseStart = java.time.Duration.between(currentTime, courseStartTime).toMinutes()
            val delayToEnd = java.time.Duration.between(currentTime, courseEndTime).toMinutes()
            
            val courseEndDateTime = LocalDateTime.of(today, courseEndTime)
            val endTimeMillis = courseEndDateTime.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000
            
            Log.d(TAG, "----------")
            Log.d(TAG, "课程: ${course.name}")
            Log.d(TAG, "  课程开始时间: $courseStartTime")
            Log.d(TAG, "  静音开始时间: $silentStartTime (提前 ${advanceTime} 分钟)")
            Log.d(TAG, "  延迟到静音开始: $delayToSilentStart 分钟")
            Log.d(TAG, "  延迟到课程开始: $delayToCourseStart 分钟")
            Log.d(TAG, "  课程结束时间: $courseEndTime, 延迟: $delayToEnd 分钟")
            Log.d(TAG, "  结束时间戳: $endTimeMillis")
            
            if (delayToEnd <= 0) {
                Log.d(TAG, "  课程已结束，跳过")
                continue
            }
            
            if (delayToSilentStart > 0) {
                scheduleSilentStartAlarm(course, delayToSilentStart, endTimeMillis)
                scheduleSilentEndAlarm(course, delayToEnd)
                Log.d(TAG, "  静音开始闹钟已调度，${delayToSilentStart}分钟后触发")
            } else {
                Log.d(TAG, "  静音开始时间已过，立即触发静音")
                scheduleSilentStartAlarm(course, 0, endTimeMillis)
                scheduleSilentEndAlarm(course, delayToEnd)
            }
        }
        
        Log.i(TAG, "========== 自动静音任务调度完成 ==========")
    }
    
    private fun scheduleSilentStartAlarm(course: Course, delayMinutes: Long, endTimeMillis: Long = 0L) {
        if (delayMinutes < 0) {
            Log.w(TAG, "scheduleSilentStartAlarm: delayMinutes < 0, 跳过")
            return
        }
        
        if (delayMinutes == 0L) {
            Log.i(TAG, "scheduleSilentStartAlarm: 立即触发静音 - ${course.name}")
            val intent = Intent(context, SilentModeReceiver::class.java).apply {
                action = SilentModeReceiver.ACTION_SILENT_START
                putExtra(SilentModeReceiver.EXTRA_COURSE_ID, course.id)
                putExtra(SilentModeReceiver.EXTRA_COURSE_NAME, course.name)
                putExtra(SilentModeReceiver.EXTRA_END_TIME, endTimeMillis)
            }
            context.sendBroadcast(intent)
            return
        }
        
        val triggerTime = System.currentTimeMillis() + delayMinutes * 60 * 1000
        
        Log.d(TAG, "scheduleSilentStartAlarm: ${course.name}")
        Log.d(TAG, "  delayMinutes: $delayMinutes")
        Log.d(TAG, "  triggerTime: $triggerTime")
        Log.d(TAG, "  currentTime: ${System.currentTimeMillis()}")
        Log.d(TAG, "  endTimeMillis: $endTimeMillis")

        val intent = Intent(context, SilentModeReceiver::class.java).apply {
            action = SilentModeReceiver.ACTION_SILENT_START
            putExtra(SilentModeReceiver.EXTRA_COURSE_ID, course.id)
            putExtra(SilentModeReceiver.EXTRA_COURSE_NAME, course.name)
            putExtra(SilentModeReceiver.EXTRA_END_TIME, endTimeMillis)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            course.id.hashCode() + 20000,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                    Log.i(TAG, "  静音开始闹钟已设置(精确): ${course.name}")
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                    Log.i(TAG, "  静音开始闹钟已设置(非精确): ${course.name}")
                }
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
                Log.i(TAG, "  静音开始闹钟已设置: ${course.name}")
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "  无法设置静音开始闹钟: ${e.message}")
        }
    }
    
    private fun scheduleSilentEndAlarm(course: Course, delayMinutes: Long) {
        val triggerTime = System.currentTimeMillis() + delayMinutes * 60 * 1000
        
        Log.d(TAG, "scheduleSilentEndAlarm: ${course.name}")
        Log.d(TAG, "  delayMinutes: $delayMinutes")
        Log.d(TAG, "  triggerTime: $triggerTime")

        val intent = Intent(context, SilentModeReceiver::class.java).apply {
            action = SilentModeReceiver.ACTION_SILENT_END
            putExtra(SilentModeReceiver.EXTRA_COURSE_ID, course.id)
            putExtra(SilentModeReceiver.EXTRA_COURSE_NAME, course.name)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            course.id.hashCode() + 30000,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                    Log.i(TAG, "  静音结束闹钟已设置(精确): ${course.name}")
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                    Log.i(TAG, "  静音结束闹钟已设置(非精确): ${course.name}")
                }
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
                Log.i(TAG, "  静音结束闹钟已设置: ${course.name}")
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "  无法设置静音结束闹钟: ${e.message}")
        }
    }

    private fun scheduleReminderWithAlarm(course: Course, reminderTime: LocalTime, advanceMinutes: Int) {
        val reminderDateTime = LocalDateTime.of(LocalDate.now(), reminderTime)
        val triggerTime = reminderDateTime.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000

        Log.d(TAG, "scheduleReminderWithAlarm: ${course.name}")
        Log.d(TAG, "  reminderDateTime: $reminderDateTime")
        Log.d(TAG, "  triggerTime(ms): $triggerTime")
        Log.d(TAG, "  currentTime(ms): ${System.currentTimeMillis()}")

        if (triggerTime <= System.currentTimeMillis()) {
            Log.d(TAG, "  触发时间已过，跳过")
            return
        }

        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            action = ACTION_REMINDER_ALARM
            putExtra(EXTRA_COURSE_ID, course.id)
            putExtra("course_name", course.name)
            putExtra("course_location", course.location ?: "")
            putExtra("start_hour", course.startHour)
            putExtra("start_minute", course.startMinute)
            putExtra("advance_minutes", advanceMinutes)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            course.id.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (canScheduleExactAlarms()) {
                    scheduleAlarmClock(
                        triggerTime = triggerTime,
                        pendingIntent = pendingIntent,
                        label = "课前提醒: ${course.name}"
                    )
                    Log.d(TAG, "  AlarmClock已设置: ${course.name} at $reminderTime")
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                    Log.d(TAG, "  非精确闹钟已设置(无精确闹钟权限): ${course.name} at $reminderTime")
                }
            } else {
                scheduleAlarmClock(
                    triggerTime = triggerTime,
                    pendingIntent = pendingIntent,
                    label = "课前提醒: ${course.name}"
                )
                Log.d(TAG, "  AlarmClock已设置: ${course.name} at $reminderTime")
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "  无法设置闹钟: ${e.message}")
            scheduleReminderWork(course, reminderTime, advanceMinutes)
        }
    }
    
    private fun scheduleAlarmClock(triggerTime: Long, pendingIntent: PendingIntent, label: String) {
        val showIntent = Intent(context, MainActivity::class.java)
        val showPendingIntent = PendingIntent.getActivity(
            context, 0, showIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val alarmClockInfo = AlarmManager.AlarmClockInfo(
            triggerTime,
            showPendingIntent
        )
        
        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
        Log.d(TAG, "AlarmClock scheduled: $label at $triggerTime")
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
        Log.d(TAG, "  WorkManager任务已设置: ${course.name}, delay=$delayMinutes 分钟")
    }

    private fun scheduleDailyReschedule() {
        val tomorrow = LocalDate.now().plusDays(1)
        val midnight = LocalDateTime.of(tomorrow, LocalTime.MIDNIGHT)
        val triggerTime = midnight.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000

        val intent = Intent(context, DailyRescheduleReceiver::class.java).apply {
            action = ACTION_DAILY_RESCHEDULE
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            9999,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
            Log.d(TAG, "每日重新调度任务已设置: $midnight")
        } catch (e: Exception) {
            Log.e(TAG, "设置每日重新调度任务失败", e)
        }
    }

    suspend fun showReminderNotification(
        courseName: String,
        courseLocation: String,
        startHour: Int,
        startMinute: Int,
        advanceMinutes: Int
    ) {
        Log.d(TAG, "=== showReminderNotification ===")
        Log.d(TAG, "courseName: $courseName")
        Log.d(TAG, "courseLocation: $courseLocation")
        Log.d(TAG, "startHour: $startHour, startMinute: $startMinute")
        Log.d(TAG, "advanceMinutes: $advanceMinutes")
        
        if (!hasNotificationPermission()) {
            Log.w(TAG, "没有通知权限，无法发送通知")
            return
        }
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = PromotedNotificationBuilder.buildReminderNotification(
            context = context,
            courseName = courseName,
            location = courseLocation,
            startHour = startHour,
            startMinute = startMinute,
            advanceMinutes = advanceMinutes,
            pendingIntent = pendingIntent
        )

        notificationManager.notify(NOTIFICATION_ID_REMINDER, notification)
        Log.d(TAG, "Reminder notification sent: $courseName, id=$NOTIFICATION_ID_REMINDER")
    }

    suspend fun showOngoingNotification(
        courseName: String,
        courseLocation: String,
        remainingMinutes: Int
    ) {
        val liveNotificationEnabled = settingsDataStore.getLiveNotificationEnabled()
        val canPostPromoted = canPostPromotedNotifications()
        
        Log.d(TAG, "=== showOngoingNotification ===")
        Log.d(TAG, "SDK版本: ${Build.VERSION.SDK_INT}")
        Log.d(TAG, "liveNotificationEnabled: $liveNotificationEnabled")
        Log.d(TAG, "canPostPromotedNotifications: $canPostPromoted")
        Log.d(TAG, "课程: $courseName, 剩余: ${remainingMinutes}分钟")

        if (!hasNotificationPermission()) {
            Log.w(TAG, "没有通知权限，无法发送通知")
            return
        }

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

        val notification = PromotedNotificationBuilder.buildOngoingNotification(
            context = context,
            courseName = courseName,
            location = courseLocation,
            remainingMinutes = remainingMinutes,
            totalMinutes = 45,
            pendingIntent = pendingIntent,
            liveNotificationEnabled = liveNotificationEnabled
        )

        notificationManager.notify(NOTIFICATION_ID_ONGOING, notification)
        Log.d(TAG, "通知已发送: $courseName, notificationId=$NOTIFICATION_ID_ONGOING")
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
    
    suspend fun checkAndShowOngoingNotification(): Boolean {
        Log.i(TAG, "========== 检查当前课程状态 ==========")
        Log.i(TAG, "当前时间: ${LocalTime.now()}")
        
        if (!hasNotificationPermission()) {
            Log.w(TAG, "没有通知权限，跳过检查")
            return false
        }
        
        val isEnabled = settingsDataStore.getNotificationEnabled()
        if (!isEnabled) {
            Log.w(TAG, "通知已禁用，跳过检查")
            return false
        }
        
        val today = LocalDate.now()
        val currentWeekB = settingsDataStore.getCurrentWeek(PersonType.PERSON_B).first()
        val currentTime = LocalTime.now()
        
        Log.d(TAG, "当前日期: $today, 星期${today.dayOfWeek.value}")
        Log.d(TAG, "当前周(PersonB): $currentWeekB")
        
        val personBCourses = courseDao.getCoursesForDaySync(
            dayOfWeek = today.dayOfWeek.value,
            personType = PersonType.PERSON_B
        ).filter { it.isInWeek(currentWeekB) }
        
        Log.d(TAG, "今日课程数: ${personBCourses.size}")
        
        for (course in personBCourses) {
            val courseStartTime = LocalTime.of(course.startHour, course.startMinute)
            val courseEndTime = LocalTime.of(course.endHour, course.endMinute)
            
            Log.d(TAG, "检查课程: ${course.name}, 时间: $courseStartTime - $courseEndTime")
            
            if (currentTime.isAfter(courseStartTime) && currentTime.isBefore(courseEndTime)) {
                val remainingMinutes = java.time.Duration.between(currentTime, courseEndTime).toMinutes().toInt()
                
                Log.i(TAG, "发现正在进行的课程: ${course.name}")
                Log.i(TAG, "剩余时间: $remainingMinutes 分钟")
                
                NotificationThrottler.reset()
                
                LiveUpdateService.start(
                    context = context,
                    courseName = course.name,
                    courseLocation = course.location ?: "",
                    remainingMinutes = remainingMinutes,
                    endHour = course.endHour,
                    endMinute = course.endMinute
                )
                
                Log.i(TAG, "LiveUpdateService 已启动")
                Log.i(TAG, "========== 当前课程检查完成 ==========")
                return true
            }
        }
        
        Log.i(TAG, "没有正在进行的课程")
        Log.i(TAG, "========== 当前课程检查完成 ==========")
        return false
    }

    data class CourseOngoingState(
        val courseName: String,
        val courseLocation: String,
        val remainingMinutes: Int
    )
}
