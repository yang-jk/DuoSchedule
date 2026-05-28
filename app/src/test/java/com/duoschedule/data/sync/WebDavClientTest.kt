package com.duoschedule.data.sync

import org.junit.Assert.*
import org.junit.Test

class WebDavClientTest {

    private val client = WebDavClient()

    @Test
    fun getRoomPath_returnsCorrectPath() {
        val roomId = "a3f2b8c1-d4e5-6f7a-8b9c-0d1e2f3a4b5c"
        assertEquals("duoschedule/sync/$roomId/", client.getRoomPath(roomId))
    }

    @Test
    fun getDataPath_returnsCorrectPath() {
        val roomId = "a3f2b8c1-d4e5-6f7a-8b9c-0d1e2f3a4b5c"
        assertEquals("duoschedule/sync/$roomId/data.json", client.getDataPath(roomId))
    }

    @Test
    fun getMetaPath_returnsCorrectPath() {
        val roomId = "a3f2b8c1-d4e5-6f7a-8b9c-0d1e2f3a4b5c"
        assertEquals("duoschedule/sync/$roomId/meta.json", client.getMetaPath(roomId))
    }
}
