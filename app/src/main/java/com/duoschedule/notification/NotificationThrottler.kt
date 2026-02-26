package com.duoschedule.notification

import android.util.Log

object NotificationThrottler {
    private const val TAG = "NotificationThrottler"
    private const val MIN_UPDATE_INTERVAL_MS = 500L
    
    private var lastNotifyTime = 0L
    private var lastCourseName = ""
    private var lastRemainingMinutes = -1
    
    fun shouldNotify(courseName: String, remainingMinutes: Int): Boolean {
        val currentTime = System.currentTimeMillis()
        
        if (courseName != lastCourseName) {
            Log.d(TAG, "课程名变化: $lastCourseName -> $courseName, 允许更新")
            updateState(currentTime, courseName, remainingMinutes)
            return true
        }
        
        if (remainingMinutes != lastRemainingMinutes) {
            val timeSinceLastNotify = currentTime - lastNotifyTime
            Log.d(TAG, "剩余时间变化: $lastRemainingMinutes -> $remainingMinutes, 距上次: ${timeSinceLastNotify}ms")
            if (timeSinceLastNotify >= MIN_UPDATE_INTERVAL_MS) {
                Log.d(TAG, "允许更新")
                updateState(currentTime, courseName, remainingMinutes)
                return true
            } else {
                Log.d(TAG, "节流中，跳过更新")
            }
        } else {
            Log.d(TAG, "剩余时间相同: $remainingMinutes，跳过更新")
        }
        
        return false
    }
    
    private fun updateState(time: Long, courseName: String, remainingMinutes: Int) {
        lastNotifyTime = time
        lastCourseName = courseName
        lastRemainingMinutes = remainingMinutes
        Log.d(TAG, "状态已更新: courseName=$courseName, remainingMinutes=$remainingMinutes")
    }
    
    fun reset() {
        lastNotifyTime = 0L
        lastCourseName = ""
        lastRemainingMinutes = -1
        Log.d(TAG, "状态已重置")
    }
}
