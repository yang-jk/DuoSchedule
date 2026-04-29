package com.duoschedule.notification

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import com.duoschedule.MainActivity
import com.duoschedule.R
import com.duoschedule.data.local.CourseDao
import com.duoschedule.data.local.SettingsDataStore
import com.duoschedule.data.model.PersonType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

@AndroidEntryPoint
class LiveUpdateService : Service() {

    companion object {
        private const val TAG = "LiveUpdateService"
        const val NOTIFICATION_ID = 1002
        
        const val ACTION_START = "com.duoschedule.action.START_LIVE_UPDATE"
        const val ACTION_STOP = "com.duoschedule.action.STOP_LIVE_UPDATE"
        const val ACTION_UPDATE = "com.duoschedule.action.UPDATE_LIVE_UPDATE"
        const val ACTION_PRE_START = "com.duoschedule.action.PRE_START_LIVE_UPDATE"
        
        const val EXTRA_COURSE_NAME = "course_name"
        const val EXTRA_COURSE_LOCATION = "course_location"
        const val EXTRA_REMAINING_MINUTES = "remaining_minutes"
        const val EXTRA_END_HOUR = "end_hour"
        const val EXTRA_END_MINUTE = "end_minute"
        const val EXTRA_IS_PRE_START = "is_pre_start"
        
        private var isRunning = false
        private var wakeLock: PowerManager.WakeLock? = null
        
        fun isServiceRunning(): Boolean = isRunning
        
        fun start(context: Context) {
            val intent = Intent(context, LiveUpdateService::class.java).apply {
                action = ACTION_START
            }
            context.startForegroundService(intent)
        }
        
        fun start(
            context: Context,
            courseName: String,
            courseLocation: String,
            remainingMinutes: Int,
            endHour: Int = -1,
            endMinute: Int = -1
        ) {
            val intent = Intent(context, LiveUpdateService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_COURSE_NAME, courseName)
                putExtra(EXTRA_COURSE_LOCATION, courseLocation)
                putExtra(EXTRA_REMAINING_MINUTES, remainingMinutes)
                putExtra(EXTRA_END_HOUR, endHour)
                putExtra(EXTRA_END_MINUTE, endMinute)
            }
            context.startForegroundService(intent)
        }
        
        fun preStart(context: Context) {
            val intent = Intent(context, LiveUpdateService::class.java).apply {
                action = ACTION_PRE_START
            }
            context.startForegroundService(intent)
        }
        
        fun stop(context: Context) {
            val intent = Intent(context, LiveUpdateService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
        
        fun update(
            context: Context,
            courseName: String,
            courseLocation: String,
            remainingMinutes: Int
        ) {
            val intent = Intent(context, LiveUpdateService::class.java).apply {
                action = ACTION_UPDATE
                putExtra(EXTRA_COURSE_NAME, courseName)
                putExtra(EXTRA_COURSE_LOCATION, courseLocation)
                putExtra(EXTRA_REMAINING_MINUTES, remainingMinutes)
            }
            context.startService(intent)
        }
    }

    @Inject
    lateinit var courseDao: CourseDao
    
    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    private val binder = LocalBinder()
    private var serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var updateJob: Job? = null
    
    private var currentCourseName: String = ""
    private var currentCourseLocation: String = ""
    private var currentRemainingMinutes: Int = 0
    private var currentCourseEndTime: LocalTime? = null
    private var isPreStartMode: Boolean = false
    
    private lateinit var notificationManager: NotificationManager

    inner class LocalBinder : Binder() {
        fun getService(): LiveUpdateService = this@LiveUpdateService
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        PromotedNotificationBuilder.createNotificationChannels(this)
        isRunning = true
        
        acquireWakeLock()
        
        Log.d(TAG, "服务已创建")
    }
    
    private fun acquireWakeLock() {
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "DuoSchedule::LiveUpdateService"
            )
            wakeLock?.acquire(10 * 60 * 1000L)
            Log.d(TAG, "WakeLock acquired")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to acquire WakeLock", e)
        }
    }
    
    private fun releaseWakeLock() {
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                    Log.d(TAG, "WakeLock released")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to release WakeLock", e)
        }
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PRE_START -> {
                isPreStartMode = true
                Log.d(TAG, "预启动模式: 启动前台服务保持进程存活")
                startForegroundService()
                startAutoUpdate()
            }
            ACTION_START -> {
                isPreStartMode = false
                intent.getStringExtra(EXTRA_COURSE_NAME)?.let { currentCourseName = it }
                intent.getStringExtra(EXTRA_COURSE_LOCATION)?.let { currentCourseLocation = it }
                currentRemainingMinutes = intent.getIntExtra(EXTRA_REMAINING_MINUTES, 0)
                
                val endHour = intent.getIntExtra(EXTRA_END_HOUR, -1)
                val endMinute = intent.getIntExtra(EXTRA_END_MINUTE, -1)
                if (endHour >= 0 && endMinute >= 0) {
                    currentCourseEndTime = LocalTime.of(endHour, endMinute)
                }
                
                Log.d(TAG, "服务启动: $currentCourseName, 剩余 $currentRemainingMinutes 分钟, 结束时间: $currentCourseEndTime")
                
                startForegroundService()
                startAutoUpdate()
                
                if (currentCourseName.isNotEmpty()) {
                    updateNotification()
                }
            }
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            ACTION_UPDATE -> {
                intent.getStringExtra(EXTRA_COURSE_NAME)?.let { currentCourseName = it }
                intent.getStringExtra(EXTRA_COURSE_LOCATION)?.let { currentCourseLocation = it }
                currentRemainingMinutes = intent.getIntExtra(EXTRA_REMAINING_MINUTES, 0)
                updateNotification()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        updateJob?.cancel()
        serviceScope.cancel()
        isRunning = false
        releaseWakeLock()
        Log.d(TAG, "服务已销毁")
        super.onDestroy()
    }

    private fun startForegroundService() {
        val notification = buildNotification()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        Log.d(TAG, "前台服务已启动")
    }

    private fun startAutoUpdate() {
        updateJob?.cancel()
        updateJob = serviceScope.launch {
            while (isActive) {
                delay(60_000)
                refreshCourseState()
                if (!isPreStartMode) {
                    updateNotification()
                }
            }
        }
    }

    private suspend fun refreshCourseState() {
        try {
            val today = LocalDate.now()
            val currentTime = LocalTime.now()
            val currentWeekB = settingsDataStore.getCurrentWeek(PersonType.PERSON_B).first()

            val personBCourses = courseDao.getCoursesForDaySync(
                dayOfWeek = today.dayOfWeek.value,
                personType = PersonType.PERSON_B
            ).filter { it.isInWeek(currentWeekB) }

            var foundOngoingCourse = false
            
            for (course in personBCourses) {
                val courseStartTime = LocalTime.of(course.startHour, course.startMinute)
                val courseEndTime = LocalTime.of(course.endHour, course.endMinute)
                
                if (currentTime.isAfter(courseStartTime) && currentTime.isBefore(courseEndTime)) {
                    val remainingMinutes = java.time.Duration.between(currentTime, courseEndTime).toMinutes().toInt()
                    
                    currentCourseName = course.name
                    currentCourseLocation = course.location ?: ""
                    currentRemainingMinutes = remainingMinutes
                    currentCourseEndTime = courseEndTime
                    isPreStartMode = false
                    
                    foundOngoingCourse = true
                    Log.d(TAG, "刷新课程状态: ${course.name}, 剩余${remainingMinutes}分钟")
                    break
                }
            }
            
            if (!foundOngoingCourse && currentCourseName.isNotEmpty()) {
                Log.d(TAG, "课程已结束，停止服务: $currentCourseName")
                currentCourseName = ""
                currentCourseLocation = ""
                currentRemainingMinutes = 0
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        } catch (e: Exception) {
            Log.e(TAG, "刷新课程状态失败", e)
        }
    }

    private fun updateNotification() {
        if (currentCourseName.isEmpty()) return
        
        val notification = buildNotification()
        notificationManager.notify(NOTIFICATION_ID, notification)
        Log.d(TAG, "通知已更新: $currentCourseName, 剩余${currentRemainingMinutes}分钟")
    }

    private fun buildNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val liveNotificationEnabled = runBlocking { settingsDataStore.getLiveNotificationEnabled() }

        Log.d(TAG, "=== LiveUpdateService 实况通知调试 ===")
        Log.d(TAG, "SDK版本: ${Build.VERSION.SDK_INT}")
        Log.d(TAG, "liveNotificationEnabled: $liveNotificationEnabled")
        Log.d(TAG, "课程: $currentCourseName, 剩余: ${currentRemainingMinutes}分钟")

        if (isPreStartMode || currentCourseName.isEmpty()) {
            return PromotedNotificationBuilder.buildWaitingNotification(
                context = this,
                pendingIntent = pendingIntent
            )
        }

        return PromotedNotificationBuilder.buildOngoingNotification(
            context = this,
            courseName = currentCourseName,
            location = currentCourseLocation,
            remainingMinutes = currentRemainingMinutes,
            totalMinutes = 45,
            pendingIntent = pendingIntent,
            liveNotificationEnabled = liveNotificationEnabled,
            courseEndTime = currentCourseEndTime
        )
    }
}
