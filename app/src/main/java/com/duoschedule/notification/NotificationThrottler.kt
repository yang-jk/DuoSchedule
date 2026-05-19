package com.duoschedule.notification

import android.util.Log
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

object NotificationThrottler {
    private const val TAG = "NotificationThrottler"
    private const val MIN_UPDATE_INTERVAL_MS = 500L

    private val lastNotifyTime = AtomicLong(0L)
    private val lastCourseName = AtomicReference("")
    private val lastRemainingMinutes = AtomicReference(-1)

    fun shouldNotify(courseName: String, remainingMinutes: Int): Boolean {
        val currentTime = System.currentTimeMillis()

        if (courseName != lastCourseName.get()) {
            Log.d(TAG, "课程名变化: ${lastCourseName.get()} -> $courseName, 允许更新")
            updateState(currentTime, courseName, remainingMinutes)
            return true
        }

        if (remainingMinutes != lastRemainingMinutes.get()) {
            val timeSinceLastNotify = currentTime - lastNotifyTime.get()
            Log.d(TAG, "剩余时间变化: ${lastRemainingMinutes.get()} -> $remainingMinutes, 距上次: ${timeSinceLastNotify}ms")
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
        lastNotifyTime.set(time)
        lastCourseName.set(courseName)
        lastRemainingMinutes.set(remainingMinutes)
        Log.d(TAG, "状态已更新: courseName=$courseName, remainingMinutes=$remainingMinutes")
    }

    fun reset() {
        lastNotifyTime.set(0L)
        lastCourseName.set("")
        lastRemainingMinutes.set(-1)
        Log.d(TAG, "状态已重置")
    }
}
