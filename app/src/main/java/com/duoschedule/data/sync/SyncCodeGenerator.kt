package com.duoschedule.data.sync

import kotlin.random.Random

object SyncCodeGenerator {

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
}
