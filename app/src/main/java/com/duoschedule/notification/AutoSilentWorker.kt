package com.duoschedule.notification

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.duoschedule.data.local.CourseDao
import com.duoschedule.data.local.SettingsDataStore
import com.duoschedule.data.model.PersonType
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

@HiltWorker
class AutoSilentWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val ringerModeManager: RingerModeManager,
    private val settingsDataStore: SettingsDataStore,
    private val courseDao: CourseDao
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "AutoSilentWorker"
        const val WORK_NAME_SILENT_START = "auto_silent_start"
        const val WORK_NAME_SILENT_END = "auto_silent_end"
        const val EXTRA_ACTION = "action"
        const val EXTRA_COURSE_ID = "course_id"
        const val ACTION_START_SILENT = "start_silent"
        const val ACTION_END_SILENT = "end_silent"
        const val ACTION_CHECK_CONTINUOUS = "check_continuous"

        fun scheduleSilentStart(context: Context, courseId: Long, delayMinutes: Long) {
            if (delayMinutes <= 0) return
            
            val workData = androidx.work.workDataOf(
                EXTRA_ACTION to ACTION_START_SILENT,
                EXTRA_COURSE_ID to courseId
            )
            
            val workRequest = OneTimeWorkRequestBuilder<AutoSilentWorker>()
                .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
                .setInputData(workData)
                .build()
            
            WorkManager.getInstance(context).enqueueUniqueWork(
                "${WORK_NAME_SILENT_START}_$courseId",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
            Log.d(TAG, "已调度静音开始任务: courseId=$courseId, delay=$delayMinutes 分钟")
        }

        fun scheduleSilentEnd(context: Context, courseId: Long, delayMinutes: Long) {
            if (delayMinutes <= 0) return
            
            val workData = androidx.work.workDataOf(
                EXTRA_ACTION to ACTION_END_SILENT,
                EXTRA_COURSE_ID to courseId
            )
            
            val workRequest = OneTimeWorkRequestBuilder<AutoSilentWorker>()
                .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
                .setInputData(workData)
                .build()
            
            WorkManager.getInstance(context).enqueueUniqueWork(
                "${WORK_NAME_SILENT_END}_$courseId",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
            Log.d(TAG, "已调度静音结束任务: courseId=$courseId, delay=$delayMinutes 分钟")
        }

        fun cancelAllSilentTasks(context: Context) {
            WorkManager.getInstance(context).cancelAllWorkByTag(WORK_NAME_SILENT_START)
            WorkManager.getInstance(context).cancelAllWorkByTag(WORK_NAME_SILENT_END)
            Log.d(TAG, "已取消所有静音任务")
        }
    }

    override suspend fun doWork(): Result {
        val action = inputData.getString(EXTRA_ACTION) ?: return Result.failure()
        val courseId = inputData.getLong(EXTRA_COURSE_ID, 0)
        
        Log.d(TAG, "执行任务: action=$action, courseId=$courseId")
        
        val autoSilentEnabled = settingsDataStore.getAutoSilentEnabled()
        if (!autoSilentEnabled) {
            Log.d(TAG, "自动静音功能已关闭，跳过任务")
            return Result.success()
        }
        
        if (!ringerModeManager.hasNotificationPolicyAccess()) {
            Log.w(TAG, "没有勿扰模式权限，跳过任务")
            return Result.success()
        }
        
        when (action) {
            ACTION_START_SILENT -> {
                val silentModeType = settingsDataStore.getAutoSilentModeType()
                val success = ringerModeManager.setSilentMode(silentModeType)
                if (success) {
                    Log.d(TAG, "已开启静音模式: $silentModeType")
                }
            }
            ACTION_END_SILENT -> {
                val hasContinuousCourse = checkForContinuousCourse(courseId)
                if (hasContinuousCourse) {
                    Log.d(TAG, "检测到连续课程，保持静音状态")
                } else {
                    ringerModeManager.restoreRingerMode()
                    Log.d(TAG, "已恢复铃声模式")
                }
            }
            ACTION_CHECK_CONTINUOUS -> {
                ringerModeManager.restoreRingerMode()
                Log.d(TAG, "检查完成，已恢复铃声模式")
            }
        }
        
        return Result.success()
    }

    private suspend fun checkForContinuousCourse(endedCourseId: Long): Boolean {
        val endedCourse = courseDao.getCourseById(endedCourseId) ?: return false
        
        val today = LocalDate.now()
        val currentWeek = settingsDataStore.getCurrentWeek(PersonType.PERSON_B).first()
        
        val todayCourses = courseDao.getCoursesForDaySync(
            dayOfWeek = today.dayOfWeek.value,
            personType = PersonType.PERSON_B
        ).filter { it.isInWeek(currentWeek) }
        
        val endedEndTime = endedCourse.endHour * 60 + endedCourse.endMinute
        
        for (course in todayCourses) {
            if (course.id == endedCourseId) continue
            
            val courseStartTime = course.startHour * 60 + course.startMinute
            
            if (courseStartTime - endedEndTime in 0..10) {
                val now = LocalTime.now()
                val courseStart = LocalTime.of(course.startHour, course.startMinute)
                val courseEnd = LocalTime.of(course.endHour, course.endMinute)
                
                if (now.isBefore(courseEnd)) {
                    val delayMinutes = ChronoUnit.MINUTES.between(now, courseEnd)
                    scheduleSilentEnd(context, course.id, delayMinutes)
                    Log.d(TAG, "发现连续课程: ${course.name}, 将在 $delayMinutes 分钟后恢复")
                    return true
                }
            }
        }
        
        return false
    }
}
