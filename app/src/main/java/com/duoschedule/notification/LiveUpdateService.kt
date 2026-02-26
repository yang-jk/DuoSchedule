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
import android.util.Log
import androidx.core.app.NotificationCompat
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
        
        const val EXTRA_COURSE_NAME = "course_name"
        const val EXTRA_COURSE_LOCATION = "course_location"
        const val EXTRA_REMAINING_MINUTES = "remaining_minutes"
        
        private var isRunning = false
        
        fun isServiceRunning(): Boolean = isRunning
        
        fun start(context: Context) {
            val intent = Intent(context, LiveUpdateService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
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
    
    private lateinit var notificationManager: NotificationManager

    inner class LocalBinder : Binder() {
        fun getService(): LiveUpdateService = this@LiveUpdateService
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        isRunning = true
        Log.d(TAG, "服务已创建")
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                startForegroundService()
                startAutoUpdate()
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
                updateNotification()
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

            for (course in personBCourses) {
                val courseStartTime = LocalTime.of(course.startHour, course.startMinute)
                val courseEndTime = courseStartTime.plusMinutes(course.duration.toLong())
                
                if (currentTime.isAfter(courseStartTime) && currentTime.isBefore(courseEndTime)) {
                    val remainingMinutes = java.time.Duration.between(currentTime, courseEndTime).toMinutes().toInt()
                    
                    currentCourseName = course.name
                    currentCourseLocation = course.location ?: ""
                    currentRemainingMinutes = remainingMinutes
                    
                    Log.d(TAG, "刷新课程状态: ${course.name}, 剩余${remainingMinutes}分钟")
                    break
                }
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

        val endTime = System.currentTimeMillis() + currentRemainingMinutes * 60 * 1000L

        if (Build.VERSION.SDK_INT >= 36) {
            val canPostPromoted = notificationManager.canPostPromotedNotifications()
            val liveEnabled = runBlocking { settingsDataStore.getLiveNotificationEnabled() }
            
            Log.d(TAG, "=== LiveUpdateService 实况通知调试 ===")
            Log.d(TAG, "canPostPromotedNotifications: $canPostPromoted")
            Log.d(TAG, "liveNotificationEnabled: $liveEnabled")
            Log.d(TAG, "课程: $currentCourseName, 剩余: ${currentRemainingMinutes}分钟")

            val miuiNotification = MiuiIslandHelper.createMiuiIslandNotification(
                context = this,
                channelId = CourseNotificationManager.CHANNEL_ID_ONGOING,
                courseName = currentCourseName,
                location = currentCourseLocation,
                remainingMinutes = currentRemainingMinutes,
                pendingIntent = pendingIntent,
                notificationId = NOTIFICATION_ID
            )
            
            if (miuiNotification != null) {
                Log.d(TAG, "使用小米动态岛通知")
                return miuiNotification
            }

            val useLiveNotification = liveEnabled && canPostPromoted
            
            val builder = NotificationCompat.Builder(this, CourseNotificationManager.CHANNEL_ID_ONGOING)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(currentCourseName)
                .setContentText("$currentCourseLocation · 剩余 $currentRemainingMinutes 分钟")
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
                builder.setShortCriticalText("剩余 $currentRemainingMinutes 分钟")
                Log.d(TAG, ">>> LiveUpdateService 实况通知已启用 <<<")
            } else {
                Log.w(TAG, "实况通知未启用: liveEnabled=$liveEnabled, canPost=$canPostPromoted")
            }

            return builder.build()
        } else {
            return NotificationCompat.Builder(this, CourseNotificationManager.CHANNEL_ID_ONGOING)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(currentCourseName)
                .setContentText(currentCourseLocation)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOnlyAlertOnce(true)
                .setUsesChronometer(true)
                .setChronometerCountDown(true)
                .setWhen(endTime)
                .setSubText("剩余 $currentRemainingMinutes 分钟")
                .build()
        }
    }
}
