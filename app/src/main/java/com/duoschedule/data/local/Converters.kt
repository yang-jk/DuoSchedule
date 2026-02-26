package com.duoschedule.data.local

import androidx.room.TypeConverter
import com.duoschedule.data.model.PersonType
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
}
