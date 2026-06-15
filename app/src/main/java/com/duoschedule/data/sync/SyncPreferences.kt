package com.duoschedule.data.sync

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
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
        private val MY_PROFILE_ID = stringPreferencesKey("my_profile_id")
        private val PARTNER_PROFILE_ID = stringPreferencesKey("partner_profile_id")
        private val LAST_SYNC_VERSION = longPreferencesKey("last_sync_version")
        private val LAST_SYNC_TIME = longPreferencesKey("last_sync_time")
        private val ROOM_CODE = stringPreferencesKey("room_code")
        private val INVITE_CODE = stringPreferencesKey("invite_code")
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

    val lastSyncVersion: Flow<Long> = context.syncDataStore.data.map { prefs ->
        prefs[LAST_SYNC_VERSION] ?: 0L
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

    suspend fun updateLastSyncVersion(version: Long) {
        context.syncDataStore.edit { prefs ->
            prefs[LAST_SYNC_VERSION] = version
        }
    }

    suspend fun updateLastSyncTime(time: Long) {
        context.syncDataStore.edit { prefs ->
            prefs[LAST_SYNC_TIME] = time
        }
    }

    suspend fun getMyProfileIdSync(): String? {
        return context.syncDataStore.data.map { prefs -> prefs[MY_PROFILE_ID] }.first()
    }

    suspend fun getPartnerProfileIdSync(): String? {
        return context.syncDataStore.data.map { prefs -> prefs[PARTNER_PROFILE_ID] }.first()
    }

    val roomCode: Flow<String?> = context.syncDataStore.data.map { prefs ->
        prefs[ROOM_CODE]
    }

    suspend fun saveRoomCode(code: String) {
        context.syncDataStore.edit { prefs ->
            prefs[ROOM_CODE] = code
        }
    }

    suspend fun getRoomCodeSync(): String? {
        return context.syncDataStore.data.map { prefs -> prefs[ROOM_CODE] }.first()
    }

    val inviteCode: Flow<String?> = context.syncDataStore.data.map { prefs ->
        prefs[INVITE_CODE]
    }

    suspend fun saveInviteCode(code: String) {
        context.syncDataStore.edit { prefs ->
            prefs[INVITE_CODE] = code
        }
    }

    suspend fun getInviteCodeSync(): String? {
        return context.syncDataStore.data.map { prefs -> prefs[INVITE_CODE] }.first()
    }

    suspend fun saveProfileMapping(myProfileId: String, partnerProfileId: String?) {
        context.syncDataStore.edit { prefs ->
            prefs[MY_PROFILE_ID] = myProfileId
            if (partnerProfileId.isNullOrBlank()) {
                prefs.remove(PARTNER_PROFILE_ID)
            } else {
                prefs[PARTNER_PROFILE_ID] = partnerProfileId
            }
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
            prefs.remove(MY_PROFILE_ID)
            prefs.remove(PARTNER_PROFILE_ID)
            prefs.remove(LAST_SYNC_VERSION)
            prefs.remove(LAST_SYNC_TIME)
            prefs.remove(ROOM_CODE)
            prefs.remove(INVITE_CODE)
            prefs[SYNC_ENABLED] = "false"
        }
    }
}
