package com.duoschedule.notification

import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.util.Log
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

enum class SilentModeType(val displayName: String, val value: String) {
    SILENT("静音模式", "SILENT"),
    VIBRATE("振动模式", "VIBRATE"),
    DO_NOT_DISTURB("勿扰模式", "DO_NOT_DISTURB")
}

@Singleton
class RingerModeManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "RingerModeManager"
        private const val PREF_NAME = "ringer_mode_prefs"
        private const val KEY_SAVED_RINGER_MODE = "saved_ringer_mode"
        private const val KEY_SAVED_INTERRUPTION_FILTER = "saved_interruption_filter"
        private const val KEY_IS_AUTO_SILENT_ACTIVE = "is_auto_silent_active"
        private const val KEY_AUTO_SILENT_END_TIME = "auto_silent_end_time"
        private const val KEY_AUTO_SILENT_COURSE_ID = "auto_silent_course_id"
    }

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun hasNotificationPolicyAccess(): Boolean {
        return notificationManager.isNotificationPolicyAccessGranted
    }

    fun getCurrentRingerMode(): Int {
        return audioManager.ringerMode
    }

    fun saveCurrentRingerMode() {
        val currentMode = audioManager.ringerMode
        prefs.edit {
            putInt(KEY_SAVED_RINGER_MODE, currentMode)
            putInt(KEY_SAVED_INTERRUPTION_FILTER, notificationManager.currentInterruptionFilter)
        }
        
        Log.d(TAG, "已保存当前铃声模式: $currentMode")
    }

    fun restoreRingerMode() {
        if (!hasNotificationPolicyAccess()) {
            Log.w(TAG, "没有勿扰模式权限，无法恢复铃声模式")
            return
        }

        val savedMode = prefs.getInt(KEY_SAVED_RINGER_MODE, AudioManager.RINGER_MODE_NORMAL)
        
        try {
            audioManager.ringerMode = savedMode
            Log.d(TAG, "已恢复铃声模式: $savedMode")
            
            val savedFilter = prefs.getInt(KEY_SAVED_INTERRUPTION_FILTER, NotificationManager.INTERRUPTION_FILTER_ALL)
            notificationManager.setInterruptionFilter(savedFilter)
            Log.d(TAG, "已恢复勿扰过滤器: $savedFilter")
        } catch (e: SecurityException) {
            Log.e(TAG, "恢复铃声模式失败: ${e.message}")
        }
    }

    fun setSilentMode(modeType: SilentModeType): Boolean {
        if (!hasNotificationPolicyAccess()) {
            Log.w(TAG, "没有勿扰模式权限，无法设置静音模式")
            return false
        }

        try {
            saveCurrentRingerMode()
            
            when (modeType) {
                SilentModeType.SILENT -> {
                    audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
                    Log.d(TAG, "已设置为静音模式")
                }
                SilentModeType.VIBRATE -> {
                    audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                    Log.d(TAG, "已设置为振动模式")
                }
                SilentModeType.DO_NOT_DISTURB -> {
                    audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
                    notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
                    Log.d(TAG, "已设置为勿扰模式")
                }
            }
            return true
        } catch (e: SecurityException) {
            Log.e(TAG, "设置静音模式失败: ${e.message}")
            return false
        }
    }

    fun getSavedRingerMode(): Int {
        return prefs.getInt(KEY_SAVED_RINGER_MODE, AudioManager.RINGER_MODE_NORMAL)
    }

    fun clearSavedRingerMode() {
        prefs.edit {
            remove(KEY_SAVED_RINGER_MODE)
            remove(KEY_SAVED_INTERRUPTION_FILTER)
        }
    }

    fun setAutoSilentActive(courseId: Long, endTimeMillis: Long) {
        prefs.edit {
            putBoolean(KEY_IS_AUTO_SILENT_ACTIVE, true)
            putLong(KEY_AUTO_SILENT_END_TIME, endTimeMillis)
            putLong(KEY_AUTO_SILENT_COURSE_ID, courseId)
        }
        Log.d(TAG, "已记录自动静音状态: courseId=$courseId, endTime=$endTimeMillis")
    }

    fun isAutoSilentActive(): Boolean = prefs.getBoolean(KEY_IS_AUTO_SILENT_ACTIVE, false)

    fun getAutoSilentEndTime(): Long = prefs.getLong(KEY_AUTO_SILENT_END_TIME, 0)

    fun getAutoSilentCourseId(): Long = prefs.getLong(KEY_AUTO_SILENT_COURSE_ID, 0)

    fun clearAutoSilentState() {
        prefs.edit {
            putBoolean(KEY_IS_AUTO_SILENT_ACTIVE, false)
            remove(KEY_AUTO_SILENT_END_TIME)
            remove(KEY_AUTO_SILENT_COURSE_ID)
        }
        Log.d(TAG, "已清除自动静音状态")
    }

    fun checkAndRestoreIfNeeded(): Boolean {
        if (!isAutoSilentActive()) {
            Log.d(TAG, "自动静音未激活，无需检查")
            return false
        }

        val endTime = getAutoSilentEndTime()
        val now = System.currentTimeMillis()

        if (now >= endTime) {
            Log.i(TAG, "检测到静音已过期，恢复铃声")
            restoreRingerMode()
            clearAutoSilentState()
            return true
        } else {
            Log.d(TAG, "静音未过期，剩余 ${((endTime - now) / 60000)} 分钟")
            return false
        }
    }
}
