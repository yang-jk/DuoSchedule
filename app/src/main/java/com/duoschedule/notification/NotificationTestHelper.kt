package com.duoschedule.notification

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.duoschedule.data.model.PersonType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationTestHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val courseNotificationManager: CourseNotificationManager,
    private val settingsDataStore: com.duoschedule.data.local.SettingsDataStore
) {
    private var updateHandler: Handler? = null
    private var updateRunnable: Runnable? = null
    private var currentRemainingMinutes: Int = 0
    private var isUpdating: Boolean = false
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    companion object {
        private const val TAG = "NotificationTest"
        private const val UPDATE_INTERVAL_MS = 60_000L
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
    
    fun getCanPostPromoted(): Boolean {
        return courseNotificationManager.canPostPromotedNotifications()
    }
    
    fun isMiuiDevice(): Boolean {
        return MiuiIslandHelper.isMiuiDevice()
    }

    fun testReminderNotification(personType: PersonType = PersonType.PERSON_B): TestResult {
        Log.d(TAG, "testReminderNotification 被调用")
        
        if (!hasNotificationPermission()) {
            val log = NotificationDebugLog(
                type = NotificationDebugLog.LogType.REMINDER,
                result = NotificationDebugLog.LogResult.FAILURE,
                message = "未授予通知权限",
                params = mapOf("permission" to "POST_NOTIFICATIONS")
            )
            NotificationDebugLogger.log(log)
            return TestResult.Failure("请先授予通知权限")
        }
        
        val courseName = "高等数学"
        val location = "教学楼A-301"
        val startHour = 8
        val startMinute = 0
        val advanceMinutes = 5

        return try {
            coroutineScope.launch {
                val actualAdvanceMinutes = settingsDataStore.getNotificationAdvanceTime()
                courseNotificationManager.showReminderNotification(
                    courseName = courseName,
                    courseLocation = location,
                    startHour = startHour,
                    startMinute = startMinute,
                    advanceMinutes = actualAdvanceMinutes
                )
                Log.d(TAG, "提醒通知已发送, advanceMinutes=$actualAdvanceMinutes")
            }
            
            val log = NotificationDebugLog(
                type = NotificationDebugLog.LogType.REMINDER,
                result = NotificationDebugLog.LogResult.SUCCESS,
                message = "提醒通知已发送",
                params = mapOf(
                    "courseName" to courseName,
                    "location" to location,
                    "advanceMinutes" to advanceMinutes.toString()
                )
            )
            NotificationDebugLogger.log(log)
            TestResult.Success("提醒通知已发送")
        } catch (e: Exception) {
            Log.e(TAG, "提醒通知发送失败", e)
            val log = NotificationDebugLog(
                type = NotificationDebugLog.LogType.REMINDER,
                result = NotificationDebugLog.LogResult.FAILURE,
                message = e.message ?: "未知错误",
                params = emptyMap()
            )
            NotificationDebugLogger.log(log)
            TestResult.Failure("发送失败: ${e.message}")
        }
    }

    fun testOngoingNotification(
        personType: PersonType = PersonType.PERSON_B,
        remainingMinutes: Int = 45
    ): TestResult {
        Log.d(TAG, "testOngoingNotification 被调用, remainingMinutes=$remainingMinutes")
        
        if (!hasNotificationPermission()) {
            val log = NotificationDebugLog(
                type = NotificationDebugLog.LogType.ONGOING,
                result = NotificationDebugLog.LogResult.FAILURE,
                message = "未授予通知权限",
                params = mapOf("permission" to "POST_NOTIFICATIONS")
            )
            NotificationDebugLogger.log(log)
            return TestResult.Failure("请先授予通知权限")
        }
        
        return try {
            stopProgressUpdate()
            
            currentRemainingMinutes = remainingMinutes
            isUpdating = true
            
            sendOngoingNotification(currentRemainingMinutes)
            
            startProgressUpdate()
            
            val canPostPromoted = getCanPostPromoted()
            val log = NotificationDebugLog(
                type = NotificationDebugLog.LogType.ONGOING,
                result = NotificationDebugLog.LogResult.SUCCESS,
                message = "上课中通知已发送",
                params = mapOf(
                    "remainingMinutes" to remainingMinutes.toString(),
                    "canPostPromoted" to canPostPromoted.toString(),
                    "isMiui" to isMiuiDevice().toString()
                )
            )
            NotificationDebugLogger.log(log)
            TestResult.Success("上课中通知已发送 (${remainingMinutes}分钟)")
        } catch (e: Exception) {
            Log.e(TAG, "上课中通知发送失败", e)
            val log = NotificationDebugLog(
                type = NotificationDebugLog.LogType.ONGOING,
                result = NotificationDebugLog.LogResult.FAILURE,
                message = e.message ?: "未知错误",
                params = emptyMap()
            )
            NotificationDebugLogger.log(log)
            TestResult.Failure("发送失败: ${e.message}")
        }
    }
    
    private fun startProgressUpdate() {
        updateHandler = Handler(Looper.getMainLooper())
        updateRunnable = object : Runnable {
            override fun run() {
                if (!isUpdating || currentRemainingMinutes <= 0) {
                    Log.d(TAG, "进度更新结束, remainingMinutes=$currentRemainingMinutes")
                    stopProgressUpdate()
                    if (currentRemainingMinutes <= 0) {
                        courseNotificationManager.cancelOngoingNotification()
                        Log.d(TAG, "倒计时结束，通知已取消")
                    }
                    return
                }
                
                currentRemainingMinutes--
                sendOngoingNotification(currentRemainingMinutes)
                Log.d(TAG, "进度更新: 剩余${currentRemainingMinutes}分钟")
                
                updateHandler?.postDelayed(this, UPDATE_INTERVAL_MS)
            }
        }
        updateHandler?.postDelayed(updateRunnable!!, UPDATE_INTERVAL_MS)
        Log.d(TAG, "定时更新已启动，每分钟更新一次")
    }
    
    private fun stopProgressUpdate() {
        isUpdating = false
        updateRunnable?.let {
            updateHandler?.removeCallbacks(it)
        }
        updateHandler = null
        updateRunnable = null
        Log.d(TAG, "定时更新已停止")
    }
    
    private fun sendOngoingNotification(remainingMinutes: Int) {
        val courseName = "高等数学"
        val location = "教学楼A-301"
        
        Log.d(TAG, "sendOngoingNotification: courseName=$courseName, remainingMinutes=$remainingMinutes")

        coroutineScope.launch {
            courseNotificationManager.showOngoingNotification(
                courseName = courseName,
                courseLocation = location,
                remainingMinutes = remainingMinutes
            )
        }
    }

    fun testShortOngoingNotification(): TestResult {
        Log.d(TAG, "testShortOngoingNotification 被调用 (1分钟倒计时)")
        return testOngoingNotification(remainingMinutes = 1)
    }

    fun cancelAllTestNotifications(): TestResult {
        Log.d(TAG, "cancelAllTestNotifications 被调用")
        
        return try {
            stopProgressUpdate()
            courseNotificationManager.cancelReminderNotifications()
            courseNotificationManager.cancelOngoingNotification()
            Log.d(TAG, "所有测试通知已取消")
            
            val log = NotificationDebugLog(
                type = NotificationDebugLog.LogType.CANCEL_ALL,
                result = NotificationDebugLog.LogResult.SUCCESS,
                message = "所有测试通知已取消",
                params = emptyMap()
            )
            NotificationDebugLogger.log(log)
            TestResult.Success("所有测试通知已取消")
        } catch (e: Exception) {
            Log.e(TAG, "取消通知失败", e)
            val log = NotificationDebugLog(
                type = NotificationDebugLog.LogType.CANCEL_ALL,
                result = NotificationDebugLog.LogResult.FAILURE,
                message = e.message ?: "未知错误",
                params = emptyMap()
            )
            NotificationDebugLogger.log(log)
            TestResult.Failure("取消失败: ${e.message}")
        }
    }
}

sealed class TestResult {
    data class Success(val message: String) : TestResult()
    data class Failure(val message: String) : TestResult()
    
    val isSuccess: Boolean get() = this is Success
    val messageText: String get() = when (this) {
        is Success -> message
        is Failure -> message
    }
}
