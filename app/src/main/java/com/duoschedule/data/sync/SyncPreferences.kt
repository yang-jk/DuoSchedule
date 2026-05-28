package com.duoschedule.data.sync

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.syncDataStore: DataStore<Preferences> by preferencesDataStore(name = "sync_settings")

@Singleton
class SyncPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val WEBDAV_URL = stringPreferencesKey("webdav_url")
        private val WEBDAV_USERNAME = stringPreferencesKey("webdav_username")
        private val WEBDAV_PASSWORD = stringPreferencesKey("webdav_password")
        private val ROOM_ID = stringPreferencesKey("room_id")
        private val DEVICE_ID = stringPreferencesKey("device_id")
        private val LAST_SYNC_VERSION = intPreferencesKey("last_sync_version")
        private val LAST_SYNC_TIME = longPreferencesKey("last_sync_time")
        private val SYNC_ENABLED = stringPreferencesKey("sync_enabled")
    }

    val syncConfig: Flow<SyncConfig?> = context.syncDataStore.data.map { prefs ->
        val url = prefs[WEBDAV_URL]
        val user = prefs[WEBDAV_USERNAME]
        val pass = prefs[WEBDAV_PASSWORD]
        val room = prefs[ROOM_ID]
        val device = prefs[DEVICE_ID]
        if (url != null && user != null && pass != null && room != null && device != null) {
            SyncConfig(url, user, pass, room, device)
        } else null
    }

    val syncEnabled: Flow<Boolean> = context.syncDataStore.data.map { prefs ->
        prefs[SYNC_ENABLED] == "true"
    }

    val lastSyncVersion: Flow<Int> = context.syncDataStore.data.map { prefs ->
        prefs[LAST_SYNC_VERSION] ?: 0
    }

    val lastSyncTime: Flow<Long> = context.syncDataStore.data.map { prefs ->
        prefs[LAST_SYNC_TIME] ?: 0L
    }

    suspend fun saveSyncConfig(config: SyncConfig) {
        context.syncDataStore.edit { prefs ->
            prefs[WEBDAV_URL] = config.webDavUrl
            prefs[WEBDAV_USERNAME] = config.username
            prefs[WEBDAV_PASSWORD] = config.password
            prefs[ROOM_ID] = config.roomId
            prefs[DEVICE_ID] = config.deviceId
        }
    }

    suspend fun setSyncEnabled(enabled: Boolean) {
        context.syncDataStore.edit { prefs ->
            prefs[SYNC_ENABLED] = if (enabled) "true" else "false"
        }
    }

    suspend fun updateLastSyncVersion(version: Int) {
        context.syncDataStore.edit { prefs ->
            prefs[LAST_SYNC_VERSION] = version
        }
    }

    suspend fun updateLastSyncTime(time: Long) {
        context.syncDataStore.edit { prefs ->
            prefs[LAST_SYNC_TIME] = time
        }
    }

    suspend fun getSyncConfigSync(): SyncConfig? {
        return syncConfig.first()
    }

    suspend fun clearSyncConfig() {
        context.syncDataStore.edit { prefs ->
            prefs.remove(WEBDAV_URL)
            prefs.remove(WEBDAV_USERNAME)
            prefs.remove(WEBDAV_PASSWORD)
            prefs.remove(ROOM_ID)
            prefs.remove(DEVICE_ID)
            prefs.remove(LAST_SYNC_VERSION)
            prefs.remove(LAST_SYNC_TIME)
            prefs[SYNC_ENABLED] = "false"
        }
    }
}
