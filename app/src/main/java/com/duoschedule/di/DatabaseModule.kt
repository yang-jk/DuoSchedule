package com.duoschedule.di

import android.content.Context
import androidx.room.Room
import com.duoschedule.data.local.AppDatabase
import com.duoschedule.data.local.CourseDao
import com.duoschedule.data.local.SettingsDataStore
import com.duoschedule.widget.WidgetDependencies
import dagger.Module
import dagger.Provides
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
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideCourseDao(database: AppDatabase): CourseDao {
        return database.courseDao()
    }
}

@Module
@InstallIn(SingletonComponent::class)
interface WidgetDependenciesModule : WidgetDependencies
