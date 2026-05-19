package com.duoschedule.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.duoschedule.data.model.Course

@Database(
    entities = [Course::class],
    version = 7,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun courseDao(): CourseDao

    companion object {
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("UPDATE courses SET personType = 'PERSON_TEMP' WHERE personType = 'PERSON_A'")
                db.execSQL("UPDATE courses SET personType = 'PERSON_A' WHERE personType = 'PERSON_B'")
                db.execSQL("UPDATE courses SET personType = 'PERSON_B' WHERE personType = 'PERSON_TEMP'")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE courses ADD COLUMN isCustomTime INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_courses_dayOfWeek_personType_startHour_startMinute` ON `courses` (`dayOfWeek`, `personType`, `startHour`, `startMinute`)")
            }
        }
    }
}
