package com.duoschedule.data.sync

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class SyncManagerTest {

    @Test
    fun parseCloudData_parsesCorrectly() {
        val json = JSONObject().apply {
            put("roomId", "test-room-id")
            put("version", 5)
            put("lastModified", "2026-05-28T10:30:00Z")
            put("lastModifiedBy", "device-abc12345")
            put("courses", JSONArray().apply {
                put(JSONObject().apply {
                    put("id", 1L)
                    put("name", "高等数学")
                    put("location", "A101")
                    put("teacher", "张老师")
                    put("dayOfWeek", 1)
                    put("startHour", 8)
                    put("startMinute", 0)
                    put("endHour", 9)
                    put("endMinute", 40)
                    put("weekType", "ALL")
                    put("startWeek", 1)
                    put("endWeek", 16)
                    put("customWeeks", "")
                    put("personType", "PERSON_A")
                    put("startPeriod", 1)
                    put("endPeriod", 2)
                    put("isCustomTime", false)
                })
            })
            put("settingsA", JSONObject().apply {
                put("semesterStartDate", 20045L)
                put("totalWeeks", 16)
                put("currentWeek", 10)
                put("totalPeriods", 10)
                put("periodTimes", JSONArray().apply {
                    put("08:00-08:45")
                    put("08:55-09:40")
                })
            })
            put("personAName", "我")
            put("personBName", "Ta")
        }

        val parser = SyncManagerParserHelper()
        val data = parser.parseCloudData(json)

        assertEquals("test-room-id", data.roomId)
        assertEquals(5, data.version)
        assertEquals(1, data.courses.size)
        assertEquals("高等数学", data.courses[0].name)
        assertNotNull(data.settingsA)
        assertEquals(16, data.settingsA!!.totalWeeks)
        assertEquals(2, data.settingsA!!.periodTimes.size)
        assertNull(data.settingsB)
    }

    @Test
    fun parseCloudData_emptyCourses_returnsEmptyList() {
        val json = JSONObject().apply {
            put("roomId", "test-room")
            put("version", 0)
            put("lastModified", "")
            put("lastModifiedBy", "")
            put("courses", JSONArray())
            put("personAName", "我")
            put("personBName", "Ta")
        }

        val parser = SyncManagerParserHelper()
        val data = parser.parseCloudData(json)

        assertEquals(0, data.courses.size)
    }

    @Test
    fun parseSettings_parsesCorrectly() {
        val json = JSONObject().apply {
            put("semesterStartDate", 20045L)
            put("totalWeeks", 18)
            put("currentWeek", 5)
            put("totalPeriods", 12)
            put("periodTimes", JSONArray().apply {
                put("08:00-08:45")
                put("08:55-09:40")
                put("10:00-10:45")
            })
        }

        val parser = SyncManagerParserHelper()
        val settings = parser.parseSettings(json)

        assertEquals(20045L, settings.semesterStartDate)
        assertEquals(18, settings.totalWeeks)
        assertEquals(5, settings.currentWeek)
        assertEquals(12, settings.totalPeriods)
        assertEquals(3, settings.periodTimes.size)
        assertEquals("08:00-08:45", settings.periodTimes[0])
    }
}

class SyncManagerParserHelper {
    fun parseCloudData(json: JSONObject): CloudData {
        val coursesArray = json.optJSONArray("courses") ?: JSONArray()
        val courses = mutableListOf<CloudCourse>()
        for (i in 0 until coursesArray.length()) {
            val courseJson = coursesArray.getJSONObject(i)
            courses.add(CloudCourse(
                id = courseJson.optLong("id", 0),
                name = courseJson.optString("name", ""),
                location = courseJson.optString("location", ""),
                teacher = courseJson.optString("teacher", ""),
                dayOfWeek = courseJson.optInt("dayOfWeek", 1),
                startHour = courseJson.optInt("startHour", 8),
                startMinute = courseJson.optInt("startMinute", 0),
                endHour = courseJson.optInt("endHour", 9),
                endMinute = courseJson.optInt("endMinute", 40),
                weekType = courseJson.optString("weekType", "ALL"),
                startWeek = courseJson.optInt("startWeek", 1),
                endWeek = courseJson.optInt("endWeek", 16),
                customWeeks = courseJson.optString("customWeeks", ""),
                personType = courseJson.optString("personType", "PERSON_A"),
                startPeriod = courseJson.optInt("startPeriod", 1),
                endPeriod = courseJson.optInt("endPeriod", 1),
                isCustomTime = courseJson.optBoolean("isCustomTime", false)
            ))
        }
        return CloudData(
            roomId = json.optString("roomId", ""),
            version = json.optInt("version", 0),
            lastModified = json.optString("lastModified", ""),
            lastModifiedBy = json.optString("lastModifiedBy", ""),
            courses = courses,
            settingsA = json.optJSONObject("settingsA")?.let { parseSettings(it) },
            settingsB = json.optJSONObject("settingsB")?.let { parseSettings(it) },
            personAName = json.optString("personAName", "我"),
            personBName = json.optString("personBName", "Ta")
        )
    }

    fun parseSettings(json: JSONObject): CloudSettings {
        val periodTimesArray = json.optJSONArray("periodTimes") ?: JSONArray()
        val periodTimes = mutableListOf<String>()
        for (i in 0 until periodTimesArray.length()) {
            periodTimes.add(periodTimesArray.getString(i))
        }
        return CloudSettings(
            semesterStartDate = json.optLong("semesterStartDate", 0),
            totalWeeks = json.optInt("totalWeeks", 16),
            currentWeek = json.optInt("currentWeek", 1),
            totalPeriods = json.optInt("totalPeriods", 10),
            periodTimes = periodTimes
        )
    }
}
