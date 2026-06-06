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
    fun generateRoomCode_multipleCallsReturnDifferentValues() {
        val codes = (1..50).map { SyncCodeGenerator.generateRoomCode() }
        val uniqueCodes = codes.toSet()
        assertTrue("Multiple calls should generate different codes", uniqueCodes.size > 1)
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
