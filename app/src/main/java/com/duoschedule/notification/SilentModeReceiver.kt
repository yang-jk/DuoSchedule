package com.duoschedule.notification

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.util.Log
import androidx.core.content.edit
import com.duoschedule.data.local.CourseDao
import com.duoschedule.data.local.SettingsDataStore
import com.duoschedule.data.model.PersonType
import com.duoschedule.notification.SilentModeType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@AndroidEntryPoint
class SilentModeReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "SilentModeReceiver"
        
        const val ACTION_SILENT_START = "com.duoschedule.action.SILENT_START"
        const val ACTION_SILENT_END = "com.duoschedule.action.SILENT_END"
        const val EXTRA_COURSE_ID = "course_id"
        const val EXTRA_COURSE_NAME = "course_name"
        const val EXTRA_END_TIME = "end_time"
        
        private const val PREF_NAME = "ringer_mode_prefs"
        private const val KEY_SAVED_RINGER_MODE = "saved_ringer_mode"
        private const val KEY_SAVED_INTERRUPTION_FILTER = "saved_interruption_filter"
    }

    @Inject
    lateinit var settingsDataStore: SettingsDataStore
    
    @Inject
    lateinit var courseDao: CourseDao
    
    @Inject
    lateinit var ringerModeManager: RingerModeManager

    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "========== SilentModeReceiver 收到广播 ==========")
        Log.i(TAG, "Action: ${intent.action}")
        Log.i(TAG, "时间: ${LocalTime.now()}")
        
        val pendingResult = goAsync()
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val autoSilentEnabled = settingsDataStore.getAutoSilentEnabled()
                Log.d(TAG, "自动静音开关: $autoSilentEnabled")
                
                if (!autoSilentEnabled) {
                    Log.d(TAG, "自动静音功能已关闭，跳过")
                    pendingResult.finish()
                    return@launch
                }
                
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val hasPolicyAccess = notificationManager.isNotificationPolicyAccessGranted
                Log.d(TAG, "勿扰模式权限: $hasPolicyAccess")
                
                if (!hasPolicyAccess) {
                    Log.w(TAG, "没有勿扰模式权限，跳过")
                    pendingResult.finish()
                    return@launch
                }
                
                when (intent.action) {
                    ACTION_SILENT_START -> {
                        val courseId = intent.getLongExtra(EXTRA_COURSE_ID, 0)
                        val endTime = intent.getLongExtra(EXTRA_END_TIME, 0L)
                        val silentModeType = settingsDataStore.getAutoSilentModeType()
                        Log.d(TAG, "静音模式类型: $silentModeType")
                        Log.d(TAG, "课程ID: $courseId, 结束时间: $endTime")
                        setSilentMode(context, silentModeType)
                        if (endTime > 0 && courseId > 0) {
                            ringerModeManager.setAutoSilentActive(courseId, endTime)
                        }
                    }
                    ACTION_SILENT_END -> {
                        val courseId = intent.getLongExtra(EXTRA_COURSE_ID, 0)
                        Log.d(TAG, "课程ID: $courseId")
                        
                        val hasContinuousCourse = checkForContinuousCourse(context, courseId)
                        if (hasContinuousCourse) {
                            Log.d(TAG, "检测到连续课程，保持静音状态")
                        } else {
                            restoreRingerMode(context)
                            ringerModeManager.clearAutoSilentState()
                            Log.d(TAG, "已恢复铃声模式并清除自动静音状态")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "处理静音广播失败", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
    
    private suspend fun checkForContinuousCourse(context: Context, endedCourseId: Long): Boolean {
        if (endedCourseId == 0L) return false
        
        val endedCourse = courseDao.getCourseById(endedCourseId)
        if (endedCourse == null) {
            Log.d(TAG, "未找到结束的课程: $endedCourseId")
            return false
        }
        
        val today = LocalDate.now()
        val currentWeek = settingsDataStore.getCurrentWeek(PersonType.PERSON_B).first()
        
        val todayCourses = courseDao.getCoursesForDaySync(
            dayOfWeek = today.dayOfWeek.value,
            personType = PersonType.PERSON_B
        ).filter { it.isInWeek(currentWeek) }
        
        Log.d(TAG, "今日课程数: ${todayCourses.size}")
        
        val endedEndTime = endedCourse.endHour * 60 + endedCourse.endMinute
        val now = LocalTime.now()
        
        for (course in todayCourses) {
            if (course.id == endedCourseId) continue
            
            val courseStartTime = course.startHour * 60 + course.startMinute
            val courseEndTime = LocalTime.of(course.endHour, course.endMinute)
            
            if (courseStartTime - endedEndTime in 0..10) {
                if (now.isBefore(courseEndTime)) {
                    val delayMinutes = ChronoUnit.MINUTES.between(now, courseEndTime)
                    Log.d(TAG, "发现连续课程: ${course.name}, ${delayMinutes}分钟后结束，重新调度静音结束闹钟")
                    
                    scheduleSilentEndAlarm(context, course, delayMinutes)
                    return true
                }
            }
        }
        
        return false
    }
    
    private fun scheduleSilentEndAlarm(context: Context, course: com.duoschedule.data.model.Course, delayMinutes: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerTime = System.currentTimeMillis() + delayMinutes * 60 * 1000
        
        Log.d(TAG, "scheduleSilentEndAlarm: ${course.name}")
        Log.d(TAG, "  delayMinutes: $delayMinutes")
        Log.d(TAG, "  triggerTime: $triggerTime")

        val intent = Intent(context, SilentModeReceiver::class.java).apply {
            action = ACTION_SILENT_END
            putExtra(EXTRA_COURSE_ID, course.id)
            putExtra(EXTRA_COURSE_NAME, course.name)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            course.id.hashCode() + 30000,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                    Log.i(TAG, "  连续课程静音结束闹钟已设置(精确): ${course.name}")
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                    Log.i(TAG, "  连续课程静音结束闹钟已设置(非精确): ${course.name}")
                }
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
                Log.i(TAG, "  连续课程静音结束闹钟已设置: ${course.name}")
            }
            
            ringerModeManager.setAutoSilentActive(course.id, triggerTime)
        } catch (e: SecurityException) {
            Log.e(TAG, "  无法设置连续课程静音结束闹钟: ${e.message}")
        }
    }
    
    private fun setSilentMode(context: Context, modeType: SilentModeType) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        saveCurrentRingerMode(context)
        
        Log.d(TAG, "当前铃声模式: ${audioManager.ringerMode}")
        
        when (modeType) {
            SilentModeType.SILENT -> {
                audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
                Log.i(TAG, "已设置为静音模式")
            }
            SilentModeType.VIBRATE -> {
                audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                Log.i(TAG, "已设置为振动模式")
            }
            SilentModeType.DO_NOT_DISTURB -> {
                audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
                Log.i(TAG, "已设置为勿扰模式")
            }
        }
        
        Log.d(TAG, "设置后铃声模式: ${audioManager.ringerMode}")
    }
    
    private fun saveCurrentRingerMode(context: Context) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        
        val currentMode = audioManager.ringerMode
        val currentFilter = notificationManager.currentInterruptionFilter
        prefs.edit {
            putInt(KEY_SAVED_RINGER_MODE, currentMode)
            putInt(KEY_SAVED_INTERRUPTION_FILTER, currentFilter)
        }
        Log.d(TAG, "已保存当前铃声模式: $currentMode, 勿扰过滤器: $currentFilter")
    }
    
    private fun restoreRingerMode(context: Context) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        
        val savedMode = prefs.getInt(KEY_SAVED_RINGER_MODE, AudioManager.RINGER_MODE_NORMAL)
        Log.d(TAG, "恢复铃声模式: $savedMode")
        
        try {
            audioManager.ringerMode = savedMode
            Log.i(TAG, "已恢复铃声模式: $savedMode")
            
            val savedFilter = prefs.getInt(KEY_SAVED_INTERRUPTION_FILTER, NotificationManager.INTERRUPTION_FILTER_ALL)
            notificationManager.setInterruptionFilter(savedFilter)
            Log.i(TAG, "已恢复勿扰过滤器: $savedFilter")
        } catch (e: SecurityException) {
            Log.e(TAG, "恢复铃声模式失败: ${e.message}")
        }
    }
}
