package com.duoschedule.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.duoschedule.data.local.CourseDao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationManager: CourseNotificationManager

    @Inject
    lateinit var courseDao: CourseDao

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                Log.d(TAG, "Received: ${intent.action}, rescheduling notifications")

                NotificationDebugLogger.log(NotificationDebugLog(
                    type = NotificationDebugLog.LogType.ALARM_TRIGGERED,
                    result = NotificationDebugLog.LogResult.SUCCESS,
                    message = "${intent.action} 重新调度触发"
                ))

                val pendingResult = goAsync()

                CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                    try {
                        val hasCourses = courseDao.getAllCoursesSync().isNotEmpty()
                        if (!hasCourses) {
                            Log.i(TAG, "No courses found, skipping notification reschedule")
                            BootReceiverManager.updateBootReceiverEnabled(context, courseDao)
                            return@launch
                        }

                        notificationManager.scheduleReminderNotifications()
                        Log.d(TAG, "Notifications rescheduled after ${intent.action}")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to reschedule notifications after ${intent.action}", e)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}
