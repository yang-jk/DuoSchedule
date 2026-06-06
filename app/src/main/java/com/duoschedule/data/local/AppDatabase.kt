package com.duoschedule.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.duoschedule.data.model.Course
import com.duoschedule.data.model.RepeatRule
import com.duoschedule.data.model.Todo
import com.duoschedule.data.model.TodoTag

@Database(
    entities = [Course::class, Todo::class, TodoTag::class, RepeatRule::class],
    version = 9,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun courseDao(): CourseDao
    abstract fun todoDao(): TodoDao
    abstract fun todoTagDao(): TodoTagDao
    abstract fun repeatRuleDao(): RepeatRuleDao

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

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE courses ADD COLUMN syncId TEXT NOT NULL DEFAULT ''")
                db.execSQL("UPDATE courses SET syncId = 'legacy-' || id WHERE syncId = ''")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_courses_syncId` ON `courses` (`syncId`)")
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 创建 todos 表
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `todos` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `syncId` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `personType` TEXT NOT NULL,
                        `date` INTEGER NOT NULL,
                        `startHour` INTEGER NOT NULL,
                        `startMinute` INTEGER NOT NULL,
                        `endHour` INTEGER NOT NULL,
                        `endMinute` INTEGER NOT NULL,
                        `priority` TEXT NOT NULL,
                        `status` TEXT NOT NULL,
                        `tags` TEXT NOT NULL,
                        `linkedCourseSyncId` TEXT,
                        `repeatRuleId` TEXT,
                        `completedAt` INTEGER
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_todos_personType` ON `todos` (`personType`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_todos_date` ON `todos` (`date`)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_todos_syncId` ON `todos` (`syncId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_todos_personType_date` ON `todos` (`personType`, `date`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_todos_repeatRuleId` ON `todos` (`repeatRuleId`)")

                // 创建 todo_tags 表
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `todo_tags` (
                        `id` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `color` INTEGER NOT NULL,
                        `isPreset` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())

                // 创建 repeat_rules 表
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `repeat_rules` (
                        `id` TEXT NOT NULL,
                        `frequency` TEXT NOT NULL,
                        `interval` INTEGER NOT NULL,
                        `daysOfWeek` TEXT NOT NULL,
                        `customDates` TEXT NOT NULL,
                        `endDate` INTEGER,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
            }
        }
    }
}
