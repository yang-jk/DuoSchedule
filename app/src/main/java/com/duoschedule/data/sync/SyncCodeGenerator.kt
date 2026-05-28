package com.duoschedule.data.sync

import org.json.JSONObject
import java.util.Base64

object SyncCodeGenerator {

    private const val PREFIX = "DSYNC:"
    private const val KEY_VERSION = "v"
    private const val KEY_URL = "url"
    private const val KEY_USER = "user"
    private const val KEY_PASS = "pass"
    private const val KEY_ROOM = "room"
    private const val CURRENT_VERSION = 1

    fun generate(config: SyncConfig): String {
        val json = JSONObject().apply {
            put(KEY_VERSION, CURRENT_VERSION)
            put(KEY_URL, config.webDavUrl)
            put(KEY_USER, config.username)
            put(KEY_PASS, config.password)
            put(KEY_ROOM, config.roomId)
        }
        val encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(json.toString().toByteArray(Charsets.UTF_8))
        return "$PREFIX$encoded"
    }

    fun parse(syncCode: String): Result<SyncConfig> {
        return try {
            if (!syncCode.startsWith(PREFIX)) {
                return Result.failure(IllegalArgumentException("无效的同步码格式"))
            }
            val encoded = syncCode.removePrefix(PREFIX)
            val decoded = String(Base64.getUrlDecoder().decode(encoded), Charsets.UTF_8)
            val json = JSONObject(decoded)

            val version = json.optInt(KEY_VERSION, 0)
            if (version != CURRENT_VERSION) {
                return Result.failure(IllegalArgumentException("不支持的同步码版本: $version"))
            }

            val url = json.getString(KEY_URL)
            val user = json.getString(KEY_USER)
            val pass = json.getString(KEY_PASS)
            val room = json.getString(KEY_ROOM)

            if (url.isBlank() || user.isBlank() || pass.isBlank() || room.isBlank()) {
                return Result.failure(IllegalArgumentException("同步码内容不完整"))
            }

            val deviceId = generateDeviceId()
            Result.success(SyncConfig(webDavUrl = url, username = user, password = pass, roomId = room, deviceId = deviceId))
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException("解析同步码失败: ${e.message}"))
        }
    }

    fun generateDeviceId(): String {
        return "device-${java.util.UUID.randomUUID().toString().take(8)}"
    }

    fun generateRoomId(): String {
        return java.util.UUID.randomUUID().toString()
    }

    fun isValidSyncCode(text: String): Boolean {
        return text.startsWith(PREFIX)
    }
}
