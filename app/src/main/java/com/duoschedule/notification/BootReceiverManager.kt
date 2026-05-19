package com.duoschedule.notification

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.duoschedule.data.local.CourseDao

object BootReceiverManager {

    private const val TAG = "BootReceiverManager"

    suspend fun updateBootReceiverEnabled(context: Context, courseDao: CourseDao) {
        val hasCourses = courseDao.getAllCoursesSync().isNotEmpty()
        val currentState = context.packageManager.getComponentEnabledSetting(
            ComponentName(context, BootReceiver::class.java)
        )
        val newState = if (hasCourses) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        }

        if (currentState == newState) {
            Log.d(TAG, "BootReceiver 状态未变: ${if (hasCourses) "已启用(有课)" else "已禁用(无课)"}")
            return
        }

        context.packageManager.setComponentEnabledSetting(
            ComponentName(context, BootReceiver::class.java),
            newState,
            PackageManager.DONT_KILL_APP
        )

        Log.i(TAG, "BootReceiver 状态已更新: ${if (hasCourses) "启用(有课)" else "禁用(无课)"}")
    }

    fun isBootReceiverEnabled(context: Context): Boolean {
        val state = context.packageManager.getComponentEnabledSetting(
            ComponentName(context, BootReceiver::class.java)
        )
        return state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED ||
               state == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
    }
}
