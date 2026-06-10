package com.duoschedule.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.work.*
import com.duoschedule.MainActivity
import com.duoschedule.data.model.PersonType
import com.duoschedule.util.AppLogger
import com.duoschedule.util.SafeConverters
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "AlarmScheduler"
    }

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    data class AlarmInfo(
        val requestCode: Int,
        val type: String,
        val courseName: String,
        val triggerTime: Long,
        val isExact: Boolean
    )

    private val scheduledAlarms = mutableMapOf<Int, AlarmInfo>()

    fun getScheduledAlarms(): List<AlarmInfo> = scheduledAlarms.values.toList()

    private fun recordAlarm(requestCode: Int, type: String, courseName: String, triggerTime: Long, isExact: Boolean) {
        scheduledAlarms[requestCode] = AlarmInfo(requestCode, type, courseName, triggerTime, isExact)
    }

    fun clearScheduledAlarmsRecord() {
        scheduledAlarms.clear()
    }

    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    private fun setAlarmWithFallback(type: Int, triggerMillis: Long, operation: PendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(type, triggerMillis, operation)
            } else {
                alarmManager.setAndAllowWhileIdle(type, triggerMillis, operation)
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(type, triggerMillis, operation)
        } else {
            alarmManager.setExact(type, triggerMillis, operation)
        }
    }

    fun scheduleReminderWithAlarm(course: com.duoschedule.data.model.Course, reminderTime: LocalTime, advanceMinutes: Int) {
        val reminderDateTime = LocalDateTime.of(LocalDate.now(), reminderTime)
        val triggerTime = reminderDateTime.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000

        AppLogger.d(TAG, "scheduleReminderWithAlarm: ${course.name}")
        AppLogger.d(TAG, "  reminderDateTime: $reminderDateTime")
        AppLogger.d(TAG, "  triggerTime(ms): $triggerTime")
        AppLogger.d(TAG, "  currentTime(ms): ${System.currentTimeMillis()}")

        if (triggerTime <= System.currentTimeMillis()) {
            AppLogger.d(TAG, "  触发时间已过，跳过")
            return
        }

        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            action = CourseNotificationManager.ACTION_REMINDER_ALARM
            putExtra(CourseNotificationManager.EXTRA_COURSE_ID, course.id)
            putExtra("course_name", course.name)
            putExtra("course_location", course.location)
            putExtra("start_hour", course.startHour)
            putExtra("start_minute", course.startMinute)
            putExtra("advance_minutes", advanceMinutes)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            SafeConverters.safeRequestCode(course.id),
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
                    recordAlarm(SafeConverters.safeRequestCode(course.id), "reminder", course.name, triggerTime, true)
                    AppLogger.d(TAG, "  AlarmClock已设置: ${course.name} at $reminderTime")
                    NotificationDebugLogger.log(NotificationDebugLog(
                        type = NotificationDebugLog.LogType.ALARM_SCHEDULED,
                        result = NotificationDebugLog.LogResult.SUCCESS,
                        message = "课前提醒闹钟已设置",
                        params = mapOf("courseName" to course.name, "triggerTime" to triggerTime.toString())
                    ))
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                    recordAlarm(SafeConverters.safeRequestCode(course.id), "reminder", course.name, triggerTime, false)
                    AppLogger.d(TAG, "  非精确闹钟已设置(无精确闹钟权限): ${course.name} at $reminderTime")
                    NotificationDebugLogger.log(NotificationDebugLog(
                        type = NotificationDebugLog.LogType.ALARM_SCHEDULED,
                        result = NotificationDebugLog.LogResult.SUCCESS,
                        message = "课前提醒闹钟已设置",
                        params = mapOf("courseName" to course.name, "triggerTime" to triggerTime.toString())
                    ))
                }
            } else {
                scheduleAlarmClock(
                    triggerTime = triggerTime,
                    pendingIntent = pendingIntent,
                    label = "课前提醒: ${course.name}"
                )
                recordAlarm(SafeConverters.safeRequestCode(course.id), "reminder", course.name, triggerTime, true)
                AppLogger.d(TAG, "  AlarmClock已设置: ${course.name} at $reminderTime")
                NotificationDebugLogger.log(NotificationDebugLog(
                    type = NotificationDebugLog.LogType.ALARM_SCHEDULED,
                    result = NotificationDebugLog.LogResult.SUCCESS,
                    message = "课前提醒闹钟已设置",
                    params = mapOf("courseName" to course.name, "triggerTime" to triggerTime.toString())
                ))
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "  无法设置闹钟: ${e.message}")
            NotificationDebugLogger.log(NotificationDebugLog(
                type = NotificationDebugLog.LogType.ALARM_SCHEDULED,
                result = NotificationDebugLog.LogResult.FAILURE,
                message = "课前提醒闹钟设置失败",
                params = mapOf("courseName" to course.name, "error" to (e.message ?: ""))
            ))
            scheduleReminderWork(course, reminderTime, advanceMinutes)
        }
    }

    fun scheduleOngoingCourseAlarm(course: com.duoschedule.data.model.Course) {
        val courseStartTime = SafeConverters.safeLocalTimeOf(course.startHour, course.startMinute) ?: return
        val courseStartDateTime = LocalDateTime.of(LocalDate.now(), courseStartTime)
        val triggerTime = courseStartDateTime.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000

        AppLogger.d(TAG, "scheduleOngoingCourseAlarm: ${course.name}")
        AppLogger.d(TAG, "  courseStartTime: $courseStartTime")
        AppLogger.d(TAG, "  triggerTime(ms): $triggerTime")
        AppLogger.d(TAG, "  currentTime(ms): ${System.currentTimeMillis()}")

        if (triggerTime <= System.currentTimeMillis()) {
            AppLogger.d(TAG, "  触发时间已过，跳过")
            return
        }

        val intent = Intent(context, OngoingCourseReceiver::class.java).apply {
            action = OngoingCourseReceiver.ACTION_COURSE_START
            putExtra(OngoingCourseReceiver.EXTRA_COURSE_NAME, course.name)
            putExtra(OngoingCourseReceiver.EXTRA_COURSE_LOCATION, course.location)
            putExtra(OngoingCourseReceiver.EXTRA_DURATION, course.duration)
            putExtra(OngoingCourseReceiver.EXTRA_END_HOUR, course.endHour)
            putExtra(OngoingCourseReceiver.EXTRA_END_MINUTE, course.endMinute)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            SafeConverters.safeRequestCode(course.id, 10000),
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
                    recordAlarm(SafeConverters.safeRequestCode(course.id, 10000), "ongoing", course.name, triggerTime, true)
                    AppLogger.d(TAG, "  课程开始AlarmClock已设置: ${course.name} at $courseStartTime")
                    NotificationDebugLogger.log(NotificationDebugLog(
                        type = NotificationDebugLog.LogType.ALARM_SCHEDULED,
                        result = NotificationDebugLog.LogResult.SUCCESS,
                        message = "课程开始闹钟已设置",
                        params = mapOf("courseName" to course.name, "triggerTime" to triggerTime.toString())
                    ))
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                    recordAlarm(SafeConverters.safeRequestCode(course.id, 10000), "ongoing", course.name, triggerTime, false)
                    AppLogger.d(TAG, "  课程开始闹钟已设置(非精确-无权限): ${course.name} at $courseStartTime")
                    NotificationDebugLogger.log(NotificationDebugLog(
                        type = NotificationDebugLog.LogType.ALARM_SCHEDULED,
                        result = NotificationDebugLog.LogResult.SUCCESS,
                        message = "课程开始闹钟已设置",
                        params = mapOf("courseName" to course.name, "triggerTime" to triggerTime.toString())
                    ))
                }
            } else {
                scheduleAlarmClock(
                    triggerTime = triggerTime,
                    pendingIntent = pendingIntent,
                    label = "课程开始: ${course.name}"
                )
                recordAlarm(SafeConverters.safeRequestCode(course.id, 10000), "ongoing", course.name, triggerTime, true)
                AppLogger.d(TAG, "  课程开始AlarmClock已设置: ${course.name} at $courseStartTime")
                NotificationDebugLogger.log(NotificationDebugLog(
                    type = NotificationDebugLog.LogType.ALARM_SCHEDULED,
                    result = NotificationDebugLog.LogResult.SUCCESS,
                    message = "课程开始闹钟已设置",
                    params = mapOf("courseName" to course.name, "triggerTime" to triggerTime.toString())
                ))
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "  无法设置课程开始闹钟: ${e.message}")
            NotificationDebugLogger.log(NotificationDebugLog(
                type = NotificationDebugLog.LogType.ALARM_SCHEDULED,
                result = NotificationDebugLog.LogResult.FAILURE,
                message = "课程开始闹钟设置失败",
                params = mapOf("courseName" to course.name, "error" to (e.message ?: ""))
            ))
        }
    }

    fun schedulePreStartServiceAlarm(course: com.duoschedule.data.model.Course, preStartTime: LocalTime) {
        val preStartDateTime = LocalDateTime.of(LocalDate.now(), preStartTime)
        val triggerTime = preStartDateTime.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000

        AppLogger.d(TAG, "schedulePreStartServiceAlarm: ${course.name}")
        AppLogger.d(TAG, "  preStartTime: $preStartTime")
        AppLogger.d(TAG, "  triggerTime(ms): $triggerTime")

        if (triggerTime <= System.currentTimeMillis()) {
            AppLogger.d(TAG, "  触发时间已过，跳过")
            return
        }

        val intent = Intent(context, PreStartServiceReceiver::class.java).apply {
            action = PreStartServiceReceiver.ACTION_PRE_START
            putExtra(LiveUpdateService.EXTRA_COURSE_NAME, course.name)
            putExtra(LiveUpdateService.EXTRA_COURSE_LOCATION, course.location)
            putExtra(LiveUpdateService.EXTRA_END_HOUR, course.endHour)
            putExtra(LiveUpdateService.EXTRA_END_MINUTE, course.endMinute)
            putExtra(LiveUpdateService.EXTRA_TOTAL_MINUTES, course.duration)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            SafeConverters.safeRequestCode(course.id, 60000),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        try {
            setAlarmWithFallback(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
            recordAlarm(SafeConverters.safeRequestCode(course.id, 60000), "pre_start", course.name, triggerTime, canScheduleExactAlarms())
            AppLogger.i(TAG, "  预启动服务闹钟已设置: ${course.name} at $preStartTime")
            NotificationDebugLogger.log(NotificationDebugLog(
                type = NotificationDebugLog.LogType.ALARM_SCHEDULED,
                result = NotificationDebugLog.LogResult.SUCCESS,
                message = "预启动服务闹钟已设置",
                params = mapOf("courseName" to course.name, "triggerTime" to triggerTime.toString())
            ))
        } catch (e: Exception) {
            Log.e(TAG, "  无法设置预启动服务闹钟: ${e.message}")
            NotificationDebugLogger.log(NotificationDebugLog(
                type = NotificationDebugLog.LogType.ALARM_SCHEDULED,
                result = NotificationDebugLog.LogResult.FAILURE,
                message = "预启动服务闹钟设置失败",
                params = mapOf("courseName" to course.name, "error" to (e.message ?: ""))
            ))
        }
    }

    fun schedulePreCheckAlarm(course: com.duoschedule.data.model.Course, preCheckTime: LocalTime) {
        val preCheckDateTime = LocalDateTime.of(LocalDate.now(), preCheckTime)
        val triggerTime = preCheckDateTime.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000

        AppLogger.d(TAG, "schedulePreCheckAlarm: ${course.name}")
        AppLogger.d(TAG, "  preCheckTime: $preCheckTime")
        AppLogger.d(TAG, "  triggerTime(ms): $triggerTime")
        AppLogger.d(TAG, "  currentTime(ms): ${System.currentTimeMillis()}")

        if (triggerTime <= System.currentTimeMillis()) {
            AppLogger.d(TAG, "  触发时间已过，跳过")
            return
        }

        val intent = Intent(context, DailyRescheduleReceiver::class.java).apply {
            action = CourseNotificationManager.ACTION_DAILY_RESCHEDULE
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            SafeConverters.safeRequestCode(course.id, 50000),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        try {
            setAlarmWithFallback(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
            recordAlarm(SafeConverters.safeRequestCode(course.id, 50000), "pre_check", course.name, triggerTime, canScheduleExactAlarms())
            AppLogger.i(TAG, "  课前检查闹钟已设置：${course.name} at $preCheckTime")
            NotificationDebugLogger.log(NotificationDebugLog(
                type = NotificationDebugLog.LogType.ALARM_SCHEDULED,
                result = NotificationDebugLog.LogResult.SUCCESS,
                message = "课前检查闹钟已设置",
                params = mapOf("courseName" to course.name, "triggerTime" to triggerTime.toString())
            ))
        } catch (e: Exception) {
            Log.e(TAG, "  无法设置课前检查闹钟：${e.message}")
            NotificationDebugLogger.log(NotificationDebugLog(
                type = NotificationDebugLog.LogType.ALARM_SCHEDULED,
                result = NotificationDebugLog.LogResult.FAILURE,
                message = "课前检查闹钟设置失败",
                params = mapOf("courseName" to course.name, "error" to (e.message ?: ""))
            ))
        }
    }

    fun scheduleSilentStartAlarm(course: com.duoschedule.data.model.Course, triggerDateTime: LocalDateTime, endTimeMillis: Long = 0L) {
        val triggerTime = triggerDateTime.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000

        if (triggerTime <= System.currentTimeMillis()) {
            val intent = Intent(context, SilentModeReceiver::class.java).apply {
                action = SilentModeReceiver.ACTION_SILENT_START
                putExtra(SilentModeReceiver.EXTRA_COURSE_ID, course.id)
                putExtra(SilentModeReceiver.EXTRA_COURSE_NAME, course.name)
                putExtra(SilentModeReceiver.EXTRA_END_TIME, endTimeMillis)
            }
            context.sendBroadcast(intent)
            return
        }

        AppLogger.d(TAG, "scheduleSilentStartAlarm: ${course.name}")
        AppLogger.d(TAG, "  triggerDateTime: $triggerDateTime")
        AppLogger.d(TAG, "  triggerTime: $triggerTime")
        AppLogger.d(TAG, "  currentTime: ${System.currentTimeMillis()}")
        AppLogger.d(TAG, "  endTimeMillis: $endTimeMillis")

        val intent = Intent(context, SilentModeReceiver::class.java).apply {
            action = SilentModeReceiver.ACTION_SILENT_START
            putExtra(SilentModeReceiver.EXTRA_COURSE_ID, course.id)
            putExtra(SilentModeReceiver.EXTRA_COURSE_NAME, course.name)
            putExtra(SilentModeReceiver.EXTRA_END_TIME, endTimeMillis)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            SafeConverters.safeRequestCode(course.id, 20000),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        try {
            setAlarmWithFallback(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
            recordAlarm(SafeConverters.safeRequestCode(course.id, 20000), "silent_start", course.name, triggerTime, canScheduleExactAlarms())
            AppLogger.i(TAG, "  静音开始闹钟已设置: ${course.name}")
            NotificationDebugLogger.log(NotificationDebugLog(
                type = NotificationDebugLog.LogType.ALARM_SCHEDULED,
                result = NotificationDebugLog.LogResult.SUCCESS,
                message = "静音开始闹钟已设置",
                params = mapOf("courseName" to course.name, "triggerTime" to triggerTime.toString())
            ))
        } catch (e: SecurityException) {
            Log.e(TAG, "  无法设置静音开始闹钟: ${e.message}")
            NotificationDebugLogger.log(NotificationDebugLog(
                type = NotificationDebugLog.LogType.ALARM_SCHEDULED,
                result = NotificationDebugLog.LogResult.FAILURE,
                message = "静音开始闹钟设置失败",
                params = mapOf("courseName" to course.name, "error" to (e.message ?: ""))
            ))
        }
    }

    fun scheduleSilentEndAlarm(course: com.duoschedule.data.model.Course, triggerDateTime: LocalDateTime) {
        val triggerTime = triggerDateTime.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000
        if (triggerTime <= System.currentTimeMillis()) {
            AppLogger.d(TAG, "scheduleSilentEndAlarm: 触发时间已过，发送即时广播: ${course.name}")
            val intent = Intent(context, SilentModeReceiver::class.java).apply {
                action = SilentModeReceiver.ACTION_SILENT_END
                putExtra(SilentModeReceiver.EXTRA_COURSE_ID, course.id)
                putExtra(SilentModeReceiver.EXTRA_COURSE_NAME, course.name)
            }
            context.sendBroadcast(intent)
            return
        }

        AppLogger.d(TAG, "scheduleSilentEndAlarm: ${course.name}")
        AppLogger.d(TAG, "  triggerDateTime: $triggerDateTime")
        AppLogger.d(TAG, "  triggerTime: $triggerTime")

        val intent = Intent(context, SilentModeReceiver::class.java).apply {
            action = SilentModeReceiver.ACTION_SILENT_END
            putExtra(SilentModeReceiver.EXTRA_COURSE_ID, course.id)
            putExtra(SilentModeReceiver.EXTRA_COURSE_NAME, course.name)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            SafeConverters.safeRequestCode(course.id, 30000),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        try {
            setAlarmWithFallback(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
            recordAlarm(SafeConverters.safeRequestCode(course.id, 30000), "silent_end", course.name, triggerTime, canScheduleExactAlarms())
            AppLogger.i(TAG, "  静音结束闹钟已设置: ${course.name}")
            NotificationDebugLogger.log(NotificationDebugLog(
                type = NotificationDebugLog.LogType.ALARM_SCHEDULED,
                result = NotificationDebugLog.LogResult.SUCCESS,
                message = "静音结束闹钟已设置",
                params = mapOf("courseName" to course.name, "triggerTime" to triggerTime.toString())
            ))
        } catch (e: SecurityException) {
            Log.e(TAG, "  无法设置静音结束闹钟: ${e.message}")
            NotificationDebugLogger.log(NotificationDebugLog(
                type = NotificationDebugLog.LogType.ALARM_SCHEDULED,
                result = NotificationDebugLog.LogResult.FAILURE,
                message = "静音结束闹钟设置失败",
                params = mapOf("courseName" to course.name, "error" to (e.message ?: ""))
            ))
        }
    }

    fun scheduleDailyReschedule() {
        val tomorrow = LocalDate.now().plusDays(1)
        val midnight = LocalDateTime.of(tomorrow, LocalTime.MIDNIGHT)
        val triggerTime = midnight.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000

        val intent = Intent(context, DailyRescheduleReceiver::class.java).apply {
            action = CourseNotificationManager.ACTION_DAILY_RESCHEDULE
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            9999,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        try {
            setAlarmWithFallback(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
            recordAlarm(9999, "daily_reschedule", "", triggerTime, canScheduleExactAlarms())
            AppLogger.d(TAG, "每日重新调度任务已设置: $midnight")
            NotificationDebugLogger.log(NotificationDebugLog(
                type = NotificationDebugLog.LogType.ALARM_SCHEDULED,
                result = NotificationDebugLog.LogResult.SUCCESS,
                message = "每日重新调度闹钟已设置",
                params = mapOf("triggerTime" to triggerTime.toString())
            ))
        } catch (e: SecurityException) {
            Log.e(TAG, "设置每日重新调度任务失败: ${e.message}")
            NotificationDebugLogger.log(NotificationDebugLog(
                type = NotificationDebugLog.LogType.ALARM_SCHEDULED,
                result = NotificationDebugLog.LogResult.FAILURE,
                message = "每日重新调度闹钟设置失败",
                params = mapOf("error" to (e.message ?: ""))
            ))
        }
    }

    fun cancelAlarmsForCourse(courseId: Long) {
        val requestCodes = listOf(
            SafeConverters.safeRequestCode(courseId, 0),
            SafeConverters.safeRequestCode(courseId, 10000),
            SafeConverters.safeRequestCode(courseId, 20000),
            SafeConverters.safeRequestCode(courseId, 30000),
            SafeConverters.safeRequestCode(courseId, 50000),
            SafeConverters.safeRequestCode(courseId, 60000)
        )
        for (requestCode in requestCodes) {
            val intents = listOf(
                Intent(context, ReminderAlarmReceiver::class.java),
                Intent(context, OngoingCourseReceiver::class.java),
                Intent(context, SilentModeReceiver::class.java),
                Intent(context, DailyRescheduleReceiver::class.java),
                Intent(context, PreStartServiceReceiver::class.java)
            )
            for (intent in intents) {
                val pendingIntent = PendingIntent.getBroadcast(
                    context, requestCode, intent,
                    PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                )
                pendingIntent?.let { alarmManager.cancel(it) }
            }
        }
        WorkManager.getInstance(context).cancelUniqueWork("${CourseNotificationManager.WORK_NAME_REMINDER}_$courseId")
        NotificationDebugLogger.log(NotificationDebugLog(
            type = NotificationDebugLog.LogType.ALARM_CANCELLED,
            result = NotificationDebugLog.LogResult.SUCCESS,
            message = "已取消课程闹钟",
            params = mapOf("courseId" to courseId.toString())
        ))
    }

    fun cancelAllSilentAlarms() {
        val silentAlarmEntries = scheduledAlarms.entries
            .filter { it.value.type == "silent_start" || it.value.type == "silent_end" || it.value.type == "silent_end_test" }

        for (entry in silentAlarmEntries) {
            val intent = Intent(context, SilentModeReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, entry.key, intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            pendingIntent?.let { alarmManager.cancel(it) }
            scheduledAlarms.remove(entry.key)
        }

        AppLogger.d(TAG, "cancelAllSilentAlarms: 已取消 ${silentAlarmEntries.size} 个静音闹钟")
        NotificationDebugLogger.log(NotificationDebugLog(
            type = NotificationDebugLog.LogType.ALARM_CANCELLED,
            result = NotificationDebugLog.LogResult.SUCCESS,
            message = "已取消所有静音闹钟",
            params = mapOf("count" to silentAlarmEntries.size.toString())
        ))
    }

    fun scheduleSilentEndAlarmForTest(courseId: Long, courseName: String, triggerTimeMillis: Long) {
        val intent = Intent(context, SilentModeReceiver::class.java).apply {
            action = SilentModeReceiver.ACTION_SILENT_END
            putExtra(SilentModeReceiver.EXTRA_COURSE_ID, courseId)
            putExtra(SilentModeReceiver.EXTRA_COURSE_NAME, courseName)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            SafeConverters.safeRequestCode(courseId, 30000),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        try {
            setAlarmWithFallback(
                AlarmManager.RTC_WAKEUP,
                triggerTimeMillis,
                pendingIntent
            )
            recordAlarm(SafeConverters.safeRequestCode(courseId, 30000), "silent_end_test", courseName, triggerTimeMillis, canScheduleExactAlarms())
            AppLogger.i(TAG, "测试静音结束闹钟已设置: $courseName, ${((triggerTimeMillis - System.currentTimeMillis()) / 1000)}秒后触发")
            NotificationDebugLogger.log(NotificationDebugLog(
                type = NotificationDebugLog.LogType.ALARM_SCHEDULED,
                result = NotificationDebugLog.LogResult.SUCCESS,
                message = "测试静音结束闹钟已设置",
                params = mapOf("courseName" to courseName, "triggerTime" to triggerTimeMillis.toString())
            ))
        } catch (e: SecurityException) {
            Log.e(TAG, "无法设置测试静音结束闹钟: ${e.message}")
            NotificationDebugLogger.log(NotificationDebugLog(
                type = NotificationDebugLog.LogType.ALARM_SCHEDULED,
                result = NotificationDebugLog.LogResult.FAILURE,
                message = "测试静音结束闹钟设置失败",
                params = mapOf("courseName" to courseName, "error" to (e.message ?: ""))
            ))
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
        AppLogger.d(TAG, "AlarmClock scheduled: $label at $triggerTime")
    }

    private fun scheduleReminderWork(course: com.duoschedule.data.model.Course, reminderTime: LocalTime, advanceMinutes: Int) {
        val currentTime = LocalTime.now()
        val delayMinutes = java.time.Duration.between(currentTime, reminderTime).toMinutes()

        if (delayMinutes <= 0) return

        val workData = workDataOf(
            "course_id" to course.id,
            "course_name" to course.name,
            "course_location" to course.location,
            "person_type" to course.personType.name,
            "start_hour" to course.startHour,
            "start_minute" to course.startMinute,
            "advance_minutes" to advanceMinutes
        )

        val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
            .setInputData(workData)
            .addTag(CourseNotificationManager.WORK_NAME_REMINDER)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "${CourseNotificationManager.WORK_NAME_REMINDER}_${course.id}",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
        AppLogger.d(TAG, "  WorkManager任务已设置: ${course.name}, delay=$delayMinutes 分钟")
    }
}