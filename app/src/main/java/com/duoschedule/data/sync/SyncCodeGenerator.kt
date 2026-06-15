package com.duoschedule.data.sync

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import org.json.JSONObject
import kotlin.random.Random

object SyncCodeGenerator {

    private const val INVITE_CODE_PREFIX = "DS1:"
    private const val AES_ALGORITHM = "AES/CBC/PKCS5Padding"
    private val ENCRYPTION_KEY = byteArrayOf(
        0x44, 0x75, 0x6F, 0x53, 0x63, 0x68, 0x65, 0x64,
        0x75, 0x6C, 0x65, 0x53, 0x79, 0x6E, 0x63, 0x4B
    ) // "DuoScheduleSyncK" - 16 bytes AES key

    fun generateRoomCode(): String {
        return (Random.nextInt(100000, 1000000)).toString()
    }

    fun generateDeviceId(): String {
        return "device-${java.util.UUID.randomUUID().toString().take(8)}"
    }

    fun generateRoomId(): String {
        return java.util.UUID.randomUUID().toString()
    }

    fun generateProfileId(): String {
        return "profile-${java.util.UUID.randomUUID().toString().take(12)}"
    }

    /**
     * 生成邀请码，将 WebDAV 连接信息编码为可分享的字符串
     * 格式: DS1:<Base64(IV + AES加密的JSON)>
     * JSON 包含: webDavUrl, username, password, roomId
     */
    fun generateInviteCode(config: SyncConfig): String {
        val json = JSONObject().apply {
            put("u", config.webDavUrl)
            put("n", config.username)
            put("p", config.password)
            put("r", config.roomId)
        }
        val plaintext = json.toString().toByteArray(Charsets.UTF_8)
        val encrypted = aesEncrypt(plaintext)
        val encoded = Base64.encodeToString(encrypted, Base64.NO_WRAP or Base64.URL_SAFE)
        return "$INVITE_CODE_PREFIX$encoded"
    }

    /**
     * 解码邀请码，返回包含 WebDAV 连接信息的 SyncConfig
     * @return 解码成功返回 SyncConfig，失败返回 null
     */
    fun decodeInviteCode(inviteCode: String): SyncConfig? {
        if (!inviteCode.startsWith(INVITE_CODE_PREFIX)) return null
        val encoded = inviteCode.removePrefix(INVITE_CODE_PREFIX)
        if (encoded.isBlank()) return null
        return try {
            val encrypted = Base64.decode(encoded, Base64.NO_WRAP or Base64.URL_SAFE)
            val decrypted = aesDecrypt(encrypted)
            val json = JSONObject(String(decrypted, Charsets.UTF_8))
            SyncConfig(
                webDavUrl = json.optString("u"),
                username = json.optString("n"),
                password = json.optString("p"),
                roomId = json.optString("r"),
                deviceId = generateDeviceId()
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun aesEncrypt(plaintext: ByteArray): ByteArray {
        val iv = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val keySpec = SecretKeySpec(ENCRYPTION_KEY, "AES")
        val ivSpec = IvParameterSpec(iv)
        val cipher = Cipher.getInstance(AES_ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
        val encrypted = cipher.doFinal(plaintext)
        return iv + encrypted
    }

    private fun aesDecrypt(data: ByteArray): ByteArray {
        val iv = data.copyOfRange(0, 16)
        val encrypted = data.copyOfRange(16, data.size)
        val keySpec = SecretKeySpec(ENCRYPTION_KEY, "AES")
        val ivSpec = IvParameterSpec(iv)
        val cipher = Cipher.getInstance(AES_ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
        return cipher.doFinal(encrypted)
    }
}
