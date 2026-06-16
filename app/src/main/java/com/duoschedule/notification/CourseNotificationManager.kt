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
    private val ringerModeManager: RingerModeManager,
    private val alarmScheduler: AlarmScheduler
) {
    companion object {
        private const val TAG = "CourseNotification"
        const val CHANNEL_ID_REMINDER = "course_reminder_channel"
        const val CHANNEL_ID_ONGOING = "course_ongoing_channel"
        const val CHANNEL_ID_LIVE = "course_live_channel"
        
        const val NOTIFICATION_ID_REMINDER = 1001
        const val NOTIFICATION_ID_ONGOING = 1002
        const val REMINDER_NOTIFICATION_ID_BASE = 2000
        
        const val WORK_NAME_REMINDER = "course_reminder_work"
        
        const val ACTION_REMINDER_ALARM = "com.duoschedule.action.REMINDER_ALARM"
        const val ACTION_DAILY_RESCHEDULE = "com.duoschedule.action.DAILY_RESCHEDULE"
        const val EXTRA_COURSE_ID = "course_id"
    }

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val activeReminderNotificationIds = mutableSetOf<Int>()

    private fun canPostPromotedNotifications(): Boolean {
        return PromotedNotificationBuilder.canPostPromotedNotifications(context)
    }

    private fun canScheduleExactAlarms(): Boolean = alarmScheduler.canScheduleExactAlarms()

    private fun hasNotificationPermission(): Boolean {
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
            cancelOngoingNotification()
            Log.i(TAG, "通知已禁用，取消所有通知")
            return
        }

        if (!hasNotificationPermission()) {
            Log.w(TAG, "没有通知权限，无法调度通知")
            cancelReminderNotifications()
            cancelOngoingNotification()
            return
        }

        val advanceMinutes = settingsDataStore.getNotificationAdvanceTime()
        Log.d(TAG, "提前提醒时间: $advanceMinutes 分钟")
        
        val canScheduleExact = canScheduleExactAlarms()
        Log.d(TAG, "精确闹钟权限: $canScheduleExact")
        
        val today = LocalDate.now()
        val currentWeekA = settingsDataStore.getCurrentWeek(PersonType.PERSON_A).first()
        Log.d(TAG, "当前周(PersonA): $currentWeekA")

        val personACourses = courseDao.getCoursesForDaySync(
            dayOfWeek = today.dayOfWeek.value,
            personType = PersonType.PERSON_A
        )
        
        Log.d(TAG, "今日PersonA课程总数: ${personACourses.size}")
        personACourses.forEach { course ->
            Log.d(TAG, "  - ${course.name}: 周${course.dayOfWeek}, ${course.startHour}:${course.startMinute}, 周次${course.startWeek}-${course.endWeek}, isInWeek=${course.isInWeek(currentWeekA)}")
        }
        
        val filteredCourses = personACourses.filter { it.isInWeek(currentWeekA) }
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
            if (course.duration <= 0) continue
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
        alarmScheduler.scheduleOngoingCourseAlarm(course)
    }
    
    private fun schedulePreStartServiceAlarm(course: Course, preStartTime: LocalTime) {
        alarmScheduler.schedulePreStartServiceAlarm(course, preStartTime)
    }
    
    private fun schedulePreCheckAlarm(course: Course, preCheckTime: LocalTime) {
        alarmScheduler.schedulePreCheckAlarm(course, preCheckTime)
    }

    suspend fun scheduleAutoSilentTasks() {
        Log.i(TAG, "========== 开始调度自动静音任务 ==========")
        
        alarmScheduler.cancelAllSilentAlarms()

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
        val currentWeekA = settingsDataStore.getCurrentWeek(PersonType.PERSON_A).first()
        Log.d(TAG, "当前日期: $today, 星期${today.dayOfWeek.value}")
        Log.d(TAG, "当前周(PersonA): $currentWeekA")

        val personACourses = courseDao.getCoursesForDaySync(
            dayOfWeek = today.dayOfWeek.value,
            personType = PersonType.PERSON_A
        ).filter { it.isInWeek(currentWeekA) }
        
        Log.d(TAG, "今日课程数: ${personACourses.size}")

        val currentTime = LocalTime.now()
        Log.d(TAG, "当前时间: $currentTime")
        
        val activeAndUpcomingCourses = personACourses
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
            if (course.duration <= 0) continue
            val courseStartTime = LocalTime.of(course.startHour, course.startMinute)
            val courseEndTime = LocalTime.of(course.endHour, course.endMinute)
            val silentStartTime = courseStartTime.minusMinutes(advanceTime.toLong())
            
            val silentStartDateTime = LocalDateTime.of(today, silentStartTime)
            val courseEndDateTime = LocalDateTime.of(today, courseEndTime)
            val endTimeMillis = courseEndDateTime.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000
            
            val delayToSilentStart = java.time.Duration.between(currentTime, silentStartTime).toMinutes()
            val delayToEnd = java.time.Duration.between(currentTime, courseEndTime).toMinutes()
            
            Log.d(TAG, "----------")
            Log.d(TAG, "课程: ${course.name}")
            Log.d(TAG, "  课程开始时间: $courseStartTime")
            Log.d(TAG, "  静音开始时间: $silentStartTime (提前 ${advanceTime} 分钟)")
            Log.d(TAG, "  延迟到静音开始: $delayToSilentStart 分钟")
            Log.d(TAG, "  课程结束时间: $courseEndTime, 延迟: $delayToEnd 分钟")
            Log.d(TAG, "  结束时间戳: $endTimeMillis")
            
            if (delayToSilentStart <= 0 && delayToEnd <= 0) {
                Log.d(TAG, "  静音开始时间和课程结束时间都已过，跳过")
                continue
            }
            
            if (delayToSilentStart > 0) {
                scheduleSilentStartAlarm(course, silentStartDateTime, endTimeMillis)
                scheduleSilentEndAlarm(course, courseEndDateTime)
                Log.d(TAG, "  静音开始闹钟已调度，${delayToSilentStart}分钟后触发")
            } else {
                Log.d(TAG, "  静音开始时间已过，立即触发静音")
                scheduleSilentStartAlarm(course, silentStartDateTime, endTimeMillis)
                scheduleSilentEndAlarm(course, courseEndDateTime)
            }
        }
        
        Log.i(TAG, "========== 自动静音任务调度完成 ==========")
    }
    
    private fun scheduleSilentStartAlarm(course: Course, triggerDateTime: LocalDateTime, endTimeMillis: Long = 0L) {
        alarmScheduler.scheduleSilentStartAlarm(course, triggerDateTime, endTimeMillis)
    }
    
    private fun scheduleSilentEndAlarm(course: Course, triggerDateTime: LocalDateTime) {
        alarmScheduler.scheduleSilentEndAlarm(course, triggerDateTime)
    }

    private fun scheduleReminderWithAlarm(course: Course, reminderTime: LocalTime, advanceMinutes: Int) {
        alarmScheduler.scheduleReminderWithAlarm(course, reminderTime, advanceMinutes)
    }

    private fun scheduleDailyReschedule() {
        alarmScheduler.scheduleDailyReschedule()
    }

    suspend fun showReminderNotification(
        courseId: Long,
        courseName: String,
        courseLocation: String,
        startHour: Int,
        startMinute: Int,
        advanceMinutes: Int
    ) {
        Log.d(TAG, "=== showReminderNotification ===")
        Log.d(TAG, "courseId: $courseId")
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

        val notificationId = REMINDER_NOTIFICATION_ID_BASE + courseId.toInt()
        activeReminderNotificationIds.add(notificationId)
        notificationManager.notify(notificationId, notification)
        Log.d(TAG, "Reminder notification sent: $courseName, id=$notificationId")
        NotificationDebugLogger.log(NotificationDebugLog(
            type = NotificationDebugLog.LogType.REMINDER,
            result = NotificationDebugLog.LogResult.SUCCESS,
            message = "课前提醒通知已发送",
            params = mapOf("courseName" to courseName, "startHour" to startHour.toString(), "startMinute" to startMinute.toString())
        ))
    }

    suspend fun showOngoingNotification(
        courseName: String,
        courseLocation: String,
        remainingMinutes: Int,
        totalMinutes: Int
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
            totalMinutes = totalMinutes,
            pendingIntent = pendingIntent,
            liveNotificationEnabled = liveNotificationEnabled
        )

        notificationManager.notify(NOTIFICATION_ID_ONGOING, notification)
        Log.d(TAG, "通知已发送: $courseName, notificationId=$NOTIFICATION_ID_ONGOING")
        NotificationDebugLogger.log(NotificationDebugLog(
            type = NotificationDebugLog.LogType.ONGOING,
            result = NotificationDebugLog.LogResult.SUCCESS,
            message = "上课中通知已发送",
            params = mapOf("courseName" to courseName, "remainingMinutes" to remainingMinutes.toString())
        ))
    }

    fun cancelReminderNotifications() {
        WorkManager.getInstance(context).cancelAllWorkByTag(WORK_NAME_REMINDER)
        for (id in activeReminderNotificationIds) {
            notificationManager.cancel(id)
        }
        activeReminderNotificationIds.clear()
        NotificationDebugLogger.log(NotificationDebugLog(
            type = NotificationDebugLog.LogType.CANCEL_ALL,
            result = NotificationDebugLog.LogResult.SUCCESS,
            message = "已取消所有提醒通知"
        ))
    }

    fun cancelOngoingNotification() {
        notificationManager.cancel(NOTIFICATION_ID_ONGOING)
        LiveUpdateService.stop(context)
    }
    
    fun startLiveUpdateService() {
        if (!LiveUpdateService.isServiceRunning(context)) {
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
        val currentWeekA = settingsDataStore.getCurrentWeek(PersonType.PERSON_A).first()
        val currentTime = LocalTime.now()
        
        Log.d(TAG, "当前日期: $today, 星期${today.dayOfWeek.value}")
        Log.d(TAG, "当前周(PersonA): $currentWeekA")
        
        val personACourses = courseDao.getCoursesForDaySync(
            dayOfWeek = today.dayOfWeek.value,
            personType = PersonType.PERSON_A
        ).filter { it.isInWeek(currentWeekA) }
        
        Log.d(TAG, "今日课程数: ${personACourses.size}")
        
        for (course in personACourses) {
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
                    courseLocation = course.location,
                    remainingMinutes = remainingMinutes,
                    endHour = course.endHour,
                    endMinute = course.endMinute,
                    totalMinutes = course.duration
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
