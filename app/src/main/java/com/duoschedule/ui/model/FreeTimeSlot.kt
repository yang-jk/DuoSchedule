package com.duoschedule.ui.model

import java.util.Locale

data class FreeTimeSlot(
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int
) {
    fun getStartTimeString(): String = String.format(Locale.ROOT, "%02d:%02d", startHour, startMinute)
    
    fun getEndTimeString(): String = String.format(Locale.ROOT, "%02d:%02d", endHour, endMinute)
    
    fun getTimeString(): String = "${getStartTimeString()}-${getEndTimeString()}"
    
    val durationMinutes: Int
        get() {
            val startMinutes = startHour * 60 + startMinute
            val endMinutes = endHour * 60 + endMinute
            return endMinutes - startMinutes
        }
    
    fun getDurationString(): String {
        val minutes = durationMinutes
        val hours = minutes / 60
        val mins = minutes % 60
        return when {
            hours > 0 && mins > 0 -> "${hours}小时${mins}分钟"
            hours > 0 -> "${hours}小时"
            else -> "${mins}分钟"
        }
    }
}
