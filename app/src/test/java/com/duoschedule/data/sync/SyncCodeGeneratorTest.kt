package com.duoschedule.data.sync

import org.junit.Assert.*
import org.junit.Test

class SyncCodeGeneratorTest {

    @Test
    fun generateRoomCode_isSixDigits() {
        val code = SyncCodeGenerator.generateRoomCode()
        assertEquals(6, code.length)
        assertTrue(code.matches(Regex("\\d{6}")))
    }

    @Test
    fun generateRoomCode_isInRange() {
        repeat(100) {
            val code = SyncCodeGenerator.generateRoomCode()
            val value = code.toInt()
            assertTrue("Room code should be >= 100000, got $value", value >= 100000)
            assertTrue("Room code should be <= 999999, got $value", value <= 999999)
        }
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

    @Test
    fun generateInviteCode_startsPrefix() {
        val config = SyncConfig(
            webDavUrl = "https://dav.jianguoyun.com/dav/",
            username = "test@example.com",
            password = "testpassword",
            roomId = "room-123",
            deviceId = "device-abc"
        )
        val inviteCode = SyncCodeGenerator.generateInviteCode(config)
        assertTrue(inviteCode.startsWith("DS1:"))
    }

    @Test
    fun decodeInviteCode_roundTrip() {
        val config = SyncConfig(
            webDavUrl = "https://dav.jianguoyun.com/dav/",
            username = "test@example.com",
            password = "testpassword123",
            roomId = "550e8400-e29b-41d4-a716-446655440000",
            deviceId = "device-abc"
        )
        val inviteCode = SyncCodeGenerator.generateInviteCode(config)
        val decoded = SyncCodeGenerator.decodeInviteCode(inviteCode)
        assertNotNull(decoded)
        assertEquals(config.webDavUrl, decoded!!.webDavUrl)
        assertEquals(config.username, decoded.username)
        assertEquals(config.password, decoded.password)
        assertEquals(config.roomId, decoded.roomId)
        // deviceId is newly generated, not preserved
        assertTrue(decoded.deviceId.startsWith("device-"))
    }

    @Test
    fun decodeInviteCode_invalidPrefix() {
        assertNull(SyncCodeGenerator.decodeInviteCode("INVALID:abc"))
    }

    @Test
    fun decodeInviteCode_emptyAfterPrefix() {
        assertNull(SyncCodeGenerator.decodeInviteCode("DS1:"))
    }

    @Test
    fun decodeInviteCode_invalidBase64() {
        assertNull(SyncCodeGenerator.decodeInviteCode("DS1:!!!notbase64!!!"))
    }

    @Test
    fun decodeInviteCode_corruptedData() {
        // Valid Base64 but not valid encrypted data
        assertNull(SyncCodeGenerator.decodeInviteCode("DS1:AAAABBBBCCCCDDDD"))
    }

    @Test
    fun generateInviteCode_differentConfigsProduceDifferentCodes() {
        val config1 = SyncConfig(
            webDavUrl = "https://dav.jianguoyun.com/dav/",
            username = "user1@example.com",
            password = "pass1",
            roomId = "room-1",
            deviceId = "device-1"
        )
        val config2 = SyncConfig(
            webDavUrl = "https://dav.jianguoyun.com/dav/",
            username = "user2@example.com",
            password = "pass2",
            roomId = "room-2",
            deviceId = "device-2"
        )
        val code1 = SyncCodeGenerator.generateInviteCode(config1)
        val code2 = SyncCodeGenerator.generateInviteCode(config2)
        assertNotEquals(code1, code2)
    }

    @Test
    fun generateInviteCode_sameConfigProducesDifferentCodesDueToRandomIV() {
        val config = SyncConfig(
            webDavUrl = "https://dav.jianguoyun.com/dav/",
            username = "test@example.com",
            password = "testpassword",
            roomId = "room-123",
            deviceId = "device-abc"
        )
        val code1 = SyncCodeGenerator.generateInviteCode(config)
        val code2 = SyncCodeGenerator.generateInviteCode(config)
        // Different IVs produce different codes, but both decode to same config
        assertNotEquals(code1, code2)
        val decoded1 = SyncCodeGenerator.decodeInviteCode(code1)
        val decoded2 = SyncCodeGenerator.decodeInviteCode(code2)
        assertEquals(decoded1!!.webDavUrl, decoded2!!.webDavUrl)
        assertEquals(decoded1.username, decoded2.username)
        assertEquals(decoded1.password, decoded2.password)
        assertEquals(decoded1.roomId, decoded2.roomId)
    }

    @Test
    fun decodeInviteCode_specialCharactersInPassword() {
        val config = SyncConfig(
            webDavUrl = "https://dav.jianguoyun.com/dav/",
            username = "test+special@example.com",
            password = "p@ss=w0rd/中文!#$%",
            roomId = "room-special",
            deviceId = "device-1"
        )
        val inviteCode = SyncCodeGenerator.generateInviteCode(config)
        val decoded = SyncCodeGenerator.decodeInviteCode(inviteCode)
        assertNotNull(decoded)
        assertEquals(config.password, decoded!!.password)
        assertEquals(config.username, decoded.username)
    }
}
