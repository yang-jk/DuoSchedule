package com.duoschedule.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
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
import java.time.LocalDateTime
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
    }

    @Inject
    lateinit var settingsDataStore: SettingsDataStore
    
    @Inject
    lateinit var courseDao: CourseDao
    
    @Inject
    lateinit var ringerModeManager: RingerModeManager

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

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
                    return@launch
                }
                
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val hasPolicyAccess = notificationManager.isNotificationPolicyAccessGranted
                Log.d(TAG, "勿扰模式权限: $hasPolicyAccess")
                
                if (!hasPolicyAccess) {
                    Log.w(TAG, "没有勿扰模式权限，跳过")
                    return@launch
                }
                
                when (intent.action) {
                    ACTION_SILENT_START -> {
                        val courseId = intent.getLongExtra(EXTRA_COURSE_ID, 0)
                        val endTime = intent.getLongExtra(EXTRA_END_TIME, 0L)
                        val silentModeType = settingsDataStore.getAutoSilentModeType()
                        Log.d(TAG, "静音模式类型: $silentModeType")
                        Log.d(TAG, "课程ID: $courseId, 结束时间: $endTime")
                        ringerModeManager.setSilentMode(silentModeType)
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
                            ringerModeManager.restoreRingerMode()
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
        val currentWeek = settingsDataStore.getCurrentWeek(PersonType.PERSON_A).first()
        
        val todayCourses = courseDao.getCoursesForDaySync(
            dayOfWeek = today.dayOfWeek.value,
            personType = PersonType.PERSON_A
        ).filter { it.isInWeek(currentWeek) }
        
        Log.d(TAG, "今日课程数: ${todayCourses.size}")
        
        val endedEndTime = endedCourse.endHour * 60 + endedCourse.endMinute
        val now = LocalTime.now()
        
        val otherCourses = todayCourses
            .filter { it.id != endedCourseId }
            .sortedBy { it.startHour * 60 + it.startMinute }
        
        var chainEndTime = endedEndTime
        var latestCourse: com.duoschedule.data.model.Course? = null
        
        for (course in otherCourses) {
            val startMin = course.startHour * 60 + course.startMinute
            val endMin = course.endHour * 60 + course.endMinute
            
            if (startMin - chainEndTime <= 10 && endMin > chainEndTime) {
                chainEndTime = endMin
                if (now.isBefore(LocalTime.of(course.endHour, course.endMinute))) {
                    latestCourse = course
                }
            }
        }
        
        if (latestCourse != null) {
            val delayMinutes = ChronoUnit.MINUTES.between(now, LocalTime.of(latestCourse.endHour, latestCourse.endMinute))
            Log.d(TAG, "发现连续课程: ${latestCourse.name}, ${delayMinutes}分钟后结束")
            scheduleSilentEndAlarm(context, latestCourse, delayMinutes)
            return true
        }
        
        return false
    }
    
    private fun scheduleSilentEndAlarm(context: Context, course: com.duoschedule.data.model.Course, delayMinutes: Long) {
        val triggerDateTime = LocalDateTime.now().plusMinutes(delayMinutes)
        val triggerTimeMillis = triggerDateTime.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000

        Log.d(TAG, "scheduleSilentEndAlarm: ${course.name}")
        Log.d(TAG, "  delayMinutes: $delayMinutes")
        Log.d(TAG, "  triggerDateTime: $triggerDateTime")

        alarmScheduler.scheduleSilentEndAlarm(course, triggerDateTime)
        ringerModeManager.setAutoSilentActive(course.id, triggerTimeMillis)
    }
    
}
