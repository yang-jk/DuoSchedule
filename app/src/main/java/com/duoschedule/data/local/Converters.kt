package com.duoschedule.data.local

import androidx.room.TypeConverter
import com.duoschedule.data.model.PersonType
import com.duoschedule.data.model.Priority
import com.duoschedule.data.model.RepeatFrequency
import com.duoschedule.data.model.TodoStatus
import com.duoschedule.data.model.WeekType

class Converters {
    @TypeConverter
    fun fromPersonType(value: PersonType): String {
        return value.name
    }

    @TypeConverter
    fun toPersonType(value: String): PersonType {
        return try {
            PersonType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            PersonType.PERSON_A
        }
    }

    @TypeConverter
    fun fromWeekType(value: WeekType): String {
        return value.name
    }

    @TypeConverter
    fun toWeekType(value: String): WeekType {
        return try {
            WeekType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            WeekType.ALL
        }
    }

    @TypeConverter
    fun fromPriority(value: Priority): String {
        return value.name
    }

    @TypeConverter
    fun toPriority(value: String): Priority {
        return try {
            Priority.valueOf(value)
        } catch (e: IllegalArgumentException) {
            Priority.MEDIUM
        }
    }

    @TypeConverter
    fun fromTodoStatus(value: TodoStatus): String {
        return value.name
    }

    @TypeConverter
    fun toTodoStatus(value: String): TodoStatus {
        return try {
            TodoStatus.valueOf(value)
        } catch (e: IllegalArgumentException) {
            TodoStatus.PENDING
        }
    }

    @TypeConverter
    fun fromRepeatFrequency(value: RepeatFrequency): String {
        return value.name
    }

    @TypeConverter
    fun toRepeatFrequency(value: String): RepeatFrequency {
        return try {
            RepeatFrequency.valueOf(value)
        } catch (e: IllegalArgumentException) {
            RepeatFrequency.DAILY
        }
    }
}
