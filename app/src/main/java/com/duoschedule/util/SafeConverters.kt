package com.duoschedule.util

import android.content.Context
import android.content.Intent
import java.time.DateTimeException
import java.time.LocalTime

object SafeConverters {

    fun safeRequestCode(courseId: Long, offset: Int = 0): Int {
        return ((courseId xor (offset.toLong() shl 16)) % Int.MAX_VALUE.toLong()).toInt()
            .coerceIn(0, Int.MAX_VALUE)
    }

    fun safeLocalTimeOf(hour: Int, minute: Int): LocalTime? {
        return try {
            LocalTime.of(hour, minute)
        } catch (e: DateTimeException) {
            AppLog.e("SafeConverters", "Invalid time: $hour:$minute - ${e.message}")
            null
        }
    }

    fun safeDurationMinutesToInt(minutes: Long): Int {
        return if (minutes > Int.MAX_VALUE.toLong()) {
            Int.MAX_VALUE
        } else if (minutes < Int.MIN_VALUE.toLong()) {
            0
        } else {
            minutes.toInt()
        }
    }

    fun safeStartActivity(context: Context, intent: Intent) {
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            AppLog.e("SafeConverters", "Failed to start activity: ${e.message}")
        }
    }
}