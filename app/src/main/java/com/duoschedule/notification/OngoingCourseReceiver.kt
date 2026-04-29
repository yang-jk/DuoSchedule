package com.duoschedule.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.duoschedule.DuoScheduleApp

class OngoingCourseReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "=== OngoingCourseReceiver.onReceive ===")
        Log.d(TAG, "action: ${intent.action}")
        Log.d(TAG, "expected action: $ACTION_COURSE_START")
        
        if (intent.action == ACTION_COURSE_START) {
            val courseName = intent.getStringExtra(EXTRA_COURSE_NAME)
            val courseLocation = intent.getStringExtra(EXTRA_COURSE_LOCATION) ?: ""
            val duration = intent.getIntExtra(EXTRA_DURATION, 45)
            val endHour = intent.getIntExtra(EXTRA_END_HOUR, -1)
            val endMinute = intent.getIntExtra(EXTRA_END_MINUTE, -1)

            Log.d(TAG, "Course start broadcast triggered: $courseName, duration: $duration min, end: $endHour:$endMinute")

            if (courseName.isNullOrEmpty()) {
                Log.e(TAG, "Course name is empty, skip")
                return
            }

            LiveUpdateService.start(
                context = context,
                courseName = courseName,
                courseLocation = courseLocation,
                remainingMinutes = duration,
                endHour = endHour,
                endMinute = endMinute
            )
            
            Log.d(TAG, "LiveUpdateService started")
        } else {
            Log.w(TAG, "Unknown action: ${intent.action}")
        }
    }

    companion object {
        private const val TAG = "OngoingCourseReceiver"
        const val ACTION_COURSE_START = "com.duoschedule.action.COURSE_START"
        const val EXTRA_COURSE_NAME = "course_name"
        const val EXTRA_COURSE_LOCATION = "course_location"
        const val EXTRA_DURATION = "duration"
        const val EXTRA_END_HOUR = "end_hour"
        const val EXTRA_END_MINUTE = "end_minute"
    }
}
