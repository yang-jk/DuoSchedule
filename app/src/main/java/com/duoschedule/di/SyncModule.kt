package com.duoschedule.di

import com.duoschedule.data.local.CourseDao
import com.duoschedule.data.local.SettingsDataStore
import com.duoschedule.data.local.TodoDao
import com.duoschedule.data.sync.SyncManager
import com.duoschedule.data.sync.SyncPreferences
import com.duoschedule.data.sync.WebDavClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SyncModule {

    @Provides
    @Singleton
    fun provideWebDavClient(): WebDavClient {
        return WebDavClient()
    }

    @Provides
    @Singleton
    fun provideSyncManager(
        webDavClient: WebDavClient,
        syncPreferences: SyncPreferences,
        courseDao: CourseDao,
        todoDao: TodoDao,
        settingsDataStore: SettingsDataStore
    ): SyncManager {
        return SyncManager(webDavClient, syncPreferences, courseDao, todoDao, settingsDataStore)
    }
}
