package com.duoschedule.data.sync

import org.junit.Assert.*
import org.junit.Test

class SyncCodeGeneratorTest {

    @Test
    fun generate_and_parse_roundTrip() {
        val config = SyncConfig(
            webDavUrl = "https://dav.jianguoyun.com/dav/",
            username = "test@example.com",
            password = "app-password-123",
            roomId = "a3f2b8c1-d4e5-6f7a-8b9c-0d1e2f3a4b5c",
            deviceId = "device-abc12345"
        )
        val syncCode = SyncCodeGenerator.generate(config)
        assertTrue(syncCode.startsWith("DSYNC:"))

        val parsed = SyncCodeGenerator.parse(syncCode)
        assertTrue(parsed.isSuccess)
        val parsedConfig = parsed.getOrThrow()
        assertEquals(config.webDavUrl, parsedConfig.webDavUrl)
        assertEquals(config.username, parsedConfig.username)
        assertEquals(config.password, parsedConfig.password)
        assertEquals(config.roomId, parsedConfig.roomId)
    }

    @Test
    fun parse_invalidPrefix_returnsFailure() {
        val result = SyncCodeGenerator.parse("INVALID:abc123")
        assertTrue(result.isFailure)
    }

    @Test
    fun parse_garbageData_returnsFailure() {
        val result = SyncCodeGenerator.parse("DSYNC:not-valid-base64!!!")
        assertTrue(result.isFailure)
    }

    @Test
    fun isValidSyncCode_valid_returnsTrue() {
        assertTrue(SyncCodeGenerator.isValidSyncCode("DSYNC:abc123"))
    }

    @Test
    fun isValidSyncCode_invalid_returnsFalse() {
        assertFalse(SyncCodeGenerator.isValidSyncCode("abc123"))
    }

    @Test
    fun generateDeviceId_hasCorrectPrefix() {
        val deviceId = SyncCodeGenerator.generateDeviceId()
        assertTrue(deviceId.startsWith("device-"))
        assertTrue(deviceId.length > 7)
    }

    @Test
    fun generateRoomId_isValidUUID() {
        val roomId = SyncCodeGenerator.generateRoomId()
        assertTrue(roomId.matches(Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")))
    }
}
