package com.duoschedule.data.model

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Locale

object CourseWeekCache {
    private val cache = mutableMapOf<Int, Set<Int>>()
    fun get(id: Int): Set<Int>? = cache[id]
    fun put(id: Int, weeks: Set<Int>) { cache[id] = weeks }
    fun clear(id: Int) { cache.remove(id) }
}

@Immutable
@Entity(
    tableName = "courses",
    indices = [
        Index(value = ["personType"]),
        Index(value = ["dayOfWeek"]),
        Index(value = ["startWeek", "endWeek"]),
        Index(value = ["personType", "dayOfWeek"]),
        Index(value = ["dayOfWeek", "personType", "startHour", "startMinute"])
    ]
)
data class Course(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val location: String = "",
    val teacher: String = "",
    val dayOfWeek: Int,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val weekType: WeekType,
    val startWeek: Int = 1,
    val endWeek: Int = 16,
    val customWeeks: String = "",
    val personType: PersonType,
    val startPeriod: Int = 1,
    val endPeriod: Int = 1,
    val isCustomTime: Boolean = false
) {
    fun getStartTimeString(): String {
        return String.format(Locale.ROOT, "%02d:%02d", startHour, startMinute)
    }

    fun getEndTimeString(): String {
        return String.format(Locale.ROOT, "%02d:%02d", endHour, endMinute)
    }

    fun getTimeString(): String {
        return "${getStartTimeString()}-${getEndTimeString()}"
    }

    fun isOngoing(currentHour: Int, currentMinute: Int, currentWeek: Int): Boolean {
        if (!isInWeek(currentWeek)) return false
        
        val startMinutes = startHour * 60 + startMinute
        val endMinutes = endHour * 60 + endMinute
        val currentMinutes = currentHour * 60 + currentMinute
        
        return currentMinutes in startMinutes until endMinutes
    }

    fun isInWeek(currentWeek: Int): Boolean {
        return when (weekType) {
            WeekType.ALL -> currentWeek in startWeek..endWeek
            WeekType.ODD -> currentWeek % 2 == 1 && currentWeek in startWeek..endWeek
            WeekType.EVEN -> currentWeek % 2 == 0 && currentWeek in startWeek..endWeek
            WeekType.CUSTOM -> {
                if (customWeeks.isEmpty()) return false
                val weeks = CourseWeekCache.get(id.toInt()) ?: run {
                    customWeeks.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet().also {
                        CourseWeekCache.put(id.toInt(), it)
                    }
                }
                currentWeek in weeks
            }
        }
    }

    fun getActiveWeeks(): Set<Int> {
        val weekRange = startWeek..endWeek
        return when (weekType) {
            WeekType.ALL -> weekRange.toSet()
            WeekType.ODD -> weekRange.filter { it % 2 == 1 }.toSet()
            WeekType.EVEN -> weekRange.filter { it % 2 == 0 }.toSet()
            WeekType.CUSTOM -> {
                if (customWeeks.isNotEmpty()) {
                    customWeeks.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
                } else {
                    emptySet()
                }
            }
        }
    }

    fun getRemainingMinutes(currentHour: Int, currentMinute: Int): Int {
        val endMinutes = endHour * 60 + endMinute
        val currentMinutes = currentHour * 60 + currentMinute
        return (endMinutes - currentMinutes).coerceAtLeast(0)
    }

    fun hasEnded(currentHour: Int, currentMinute: Int): Boolean {
        val endMinutes = endHour * 60 + endMinute
        val currentMinutes = currentHour * 60 + currentMinute
        return currentMinutes >= endMinutes
    }

    val duration: Int
        get() = ((endHour * 60 + endMinute) - (startHour * 60 + startMinute)).coerceAtLeast(0)
}
