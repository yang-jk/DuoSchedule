package com.duoschedule.di

import android.content.Context
import androidx.room.Room
import com.duoschedule.data.local.AppDatabase
import com.duoschedule.data.local.CourseDao
import com.duoschedule.data.local.RepeatRuleDao
import com.duoschedule.data.local.SettingsDataStore
import com.duoschedule.data.local.TodoDao
import com.duoschedule.data.local.TodoTagDao
import com.duoschedule.provider.ScheduleContentProvider
import com.duoschedule.widget.WidgetDependencies
import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "duo_schedule_database"
        )
            .addMigrations(
                AppDatabase.MIGRATION_4_5,
                AppDatabase.MIGRATION_5_6,
                AppDatabase.MIGRATION_6_7,
                AppDatabase.MIGRATION_7_8,
                AppDatabase.MIGRATION_8_9
            )
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    @Provides
    fun provideCourseDao(database: AppDatabase): CourseDao {
        return database.courseDao()
    }

    @Provides
    fun provideTodoDao(database: AppDatabase): TodoDao {
        return database.todoDao()
    }

    @Provides
    fun provideTodoTagDao(database: AppDatabase): TodoTagDao {
        return database.todoTagDao()
    }

    @Provides
    fun provideRepeatRuleDao(database: AppDatabase): RepeatRuleDao {
        return database.repeatRuleDao()
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetDependenciesModule : WidgetDependencies

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ProviderDependenciesModule : ScheduleContentProvider.ProviderDependencies
