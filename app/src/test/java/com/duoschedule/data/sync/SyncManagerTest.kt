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

    @Test
    fun parseCloudData_withTodos_parsesCorrectly() {
        val json = JSONObject().apply {
            put("roomId", "room-todo")
            put("version", 1)
            put("lastModified", "")
            put("lastModifiedBy", "")
            put("courses", JSONArray())
            put("todos", JSONArray().apply {
                put(JSONObject().apply {
                    put("syncId", "todo-sync-1")
                    put("ownerProfileId", "profile-a")
                    put("title", "完成作业")
                    put("description", "数学第三章")
                    put("date", 20050L)
                    put("startHour", 14)
                    put("startMinute", 0)
                    put("endHour", 16)
                    put("endMinute", 30)
                    put("priority", "HIGH")
                    put("status", "PENDING")
                    put("tags", "学习,重要")
                    put("linkedCourseSyncId", "course-sync-1")
                    put("repeatRuleId", JSONObject.NULL)
                    put("completedAt", JSONObject.NULL)
                })
            })
            put("personAName", "我")
            put("personBName", "Ta")
        }

        val parser = SyncManagerParserHelper()
        val data = parser.parseCloudData(json)

        assertEquals(1, data.todos.size)
        val todo = data.todos[0]
        assertEquals("todo-sync-1", todo.syncId)
        assertEquals("profile-a", todo.ownerProfileId)
        assertEquals("完成作业", todo.title)
        assertEquals("数学第三章", todo.description)
        assertEquals(20050L, todo.date)
        assertEquals(14, todo.startHour)
        assertEquals(0, todo.startMinute)
        assertEquals(16, todo.endHour)
        assertEquals(30, todo.endMinute)
        assertEquals("HIGH", todo.priority)
        assertEquals("PENDING", todo.status)
        assertEquals("学习,重要", todo.tags)
        assertEquals("course-sync-1", todo.linkedCourseSyncId)
        assertNull(todo.repeatRuleId)
        assertNull(todo.completedAt)
    }

    @Test
    fun parseCloudData_withTodoTags_parsesCorrectly() {
        val json = JSONObject().apply {
            put("roomId", "room-tags")
            put("version", 1)
            put("lastModified", "")
            put("lastModifiedBy", "")
            put("courses", JSONArray())
            put("todoTags", JSONArray().apply {
                put(JSONObject().apply {
                    put("id", "tag-1")
                    put("name", "学习")
                    put("color", 0xFF5722)
                    put("isPreset", true)
                })
                put(JSONObject().apply {
                    put("id", "tag-2")
                    put("name", "生活")
                    put("color", 0x4CAF50)
                    put("isPreset", false)
                })
            })
            put("personAName", "我")
            put("personBName", "Ta")
        }

        val parser = SyncManagerParserHelper()
        val data = parser.parseCloudData(json)

        assertEquals(2, data.todoTags.size)
        assertEquals("tag-1", data.todoTags[0].id)
        assertEquals("学习", data.todoTags[0].name)
        assertEquals(0xFF5722, data.todoTags[0].color)
        assertTrue(data.todoTags[0].isPreset)
        assertEquals("tag-2", data.todoTags[1].id)
        assertEquals("生活", data.todoTags[1].name)
        assertEquals(0x4CAF50, data.todoTags[1].color)
        assertFalse(data.todoTags[1].isPreset)
    }

    @Test
    fun parseCloudData_withRepeatRules_parsesCorrectly() {
        val json = JSONObject().apply {
            put("roomId", "room-repeat")
            put("version", 1)
            put("lastModified", "")
            put("lastModifiedBy", "")
            put("courses", JSONArray())
            put("repeatRules", JSONArray().apply {
                put(JSONObject().apply {
                    put("id", "rule-1")
                    put("frequency", "WEEKLY")
                    put("interval", 2)
                    put("daysOfWeek", "1,3,5")
                    put("customDates", "")
                    put("endDate", 20100L)
                })
            })
            put("personAName", "我")
            put("personBName", "Ta")
        }

        val parser = SyncManagerParserHelper()
        val data = parser.parseCloudData(json)

        assertEquals(1, data.repeatRules.size)
        val rule = data.repeatRules[0]
        assertEquals("rule-1", rule.id)
        assertEquals("WEEKLY", rule.frequency)
        assertEquals(2, rule.interval)
        assertEquals("1,3,5", rule.daysOfWeek)
        assertEquals("", rule.customDates)
        assertEquals(20100L, rule.endDate)
    }

    @Test
    fun parseCloudData_missingTodoFields_returnsEmptyLists() {
        val json = JSONObject().apply {
            put("roomId", "room-minimal")
            put("version", 0)
            put("lastModified", "")
            put("lastModifiedBy", "")
            put("courses", JSONArray())
            put("personAName", "我")
            put("personBName", "Ta")
        }

        val parser = SyncManagerParserHelper()
        val data = parser.parseCloudData(json)

        assertEquals(0, data.todos.size)
        assertEquals(0, data.todoTags.size)
        assertEquals(0, data.repeatRules.size)
    }

    @Test
    fun parseCloudData_withProfiles_parsesCorrectly() {
        val json = JSONObject().apply {
            put("roomId", "room-profiles")
            put("version", 1)
            put("lastModified", "")
            put("lastModifiedBy", "")
            put("courses", JSONArray())
            put("profiles", JSONArray().apply {
                put(JSONObject().apply {
                    put("id", "profile-abc")
                    put("name", "小明")
                })
                put(JSONObject().apply {
                    put("id", "profile-xyz")
                    put("name", "小红")
                })
            })
            put("personAName", "我")
            put("personBName", "Ta")
        }

        val parser = SyncManagerParserHelper()
        val data = parser.parseCloudData(json)

        assertEquals(2, data.profiles.size)
        assertEquals("profile-abc", data.profiles[0].id)
        assertEquals("小明", data.profiles[0].name)
        assertEquals("profile-xyz", data.profiles[1].id)
        assertEquals("小红", data.profiles[1].name)
    }

    @Test
    fun parseCloudData_withProfileSettings_parsesCorrectly() {
        val json = JSONObject().apply {
            put("roomId", "room-psettings")
            put("version", 1)
            put("lastModified", "")
            put("lastModifiedBy", "")
            put("courses", JSONArray())
            put("profileSettings", JSONObject().apply {
                put("profile-a", JSONObject().apply {
                    put("semesterStartDate", 20045L)
                    put("totalWeeks", 16)
                    put("currentWeek", 5)
                    put("totalPeriods", 10)
                    put("periodTimes", JSONArray().apply {
                        put("08:00-08:45")
                    })
                })
                put("profile-b", JSONObject().apply {
                    put("semesterStartDate", 20050L)
                    put("totalWeeks", 18)
                    put("currentWeek", 3)
                    put("totalPeriods", 12)
                    put("periodTimes", JSONArray().apply {
                        put("09:00-09:45")
                    })
                })
            })
            put("personAName", "我")
            put("personBName", "Ta")
        }

        val parser = SyncManagerParserHelper()
        val data = parser.parseCloudData(json)

        assertEquals(2, data.profileSettings.size)
        val settingsA = data.profileSettings["profile-a"]
        assertNotNull(settingsA)
        assertEquals(20045L, settingsA!!.semesterStartDate)
        assertEquals(16, settingsA.totalWeeks)
        assertEquals(5, settingsA.currentWeek)
        assertEquals(10, settingsA.totalPeriods)
        assertEquals(1, settingsA.periodTimes.size)

        val settingsB = data.profileSettings["profile-b"]
        assertNotNull(settingsB)
        assertEquals(20050L, settingsB!!.semesterStartDate)
        assertEquals(18, settingsB.totalWeeks)
        assertEquals(3, settingsB.currentWeek)
        assertEquals(12, settingsB.totalPeriods)
    }

    @Test
    fun parseCloudData_courseWithOwnerProfileId_parsesCorrectly() {
        val json = JSONObject().apply {
            put("roomId", "room-owner")
            put("version", 1)
            put("lastModified", "")
            put("lastModifiedBy", "")
            put("courses", JSONArray().apply {
                put(JSONObject().apply {
                    put("id", 10L)
                    put("name", "线性代数")
                    put("location", "B202")
                    put("teacher", "李老师")
                    put("dayOfWeek", 2)
                    put("startHour", 10)
                    put("startMinute", 0)
                    put("endHour", 11)
                    put("endMinute", 40)
                    put("weekType", "ALL")
                    put("startWeek", 1)
                    put("endWeek", 16)
                    put("customWeeks", "")
                    put("personType", "PERSON_A")
                    put("startPeriod", 3)
                    put("endPeriod", 4)
                    put("isCustomTime", false)
                    put("syncId", "course-uuid-10")
                    put("ownerProfileId", "profile-abc-123")
                })
            })
            put("personAName", "我")
            put("personBName", "Ta")
        }

        val parser = SyncManagerParserHelper()
        val data = parser.parseCloudData(json)

        assertEquals(1, data.courses.size)
        val course = data.courses[0]
        assertEquals("course-uuid-10", course.syncId)
        assertEquals("profile-abc-123", course.ownerProfileId)
        assertEquals("线性代数", course.name)
    }

    @Test
    fun parseCloudData_courseWithoutOwnerProfileId_usesLegacyMapping() {
        val json = JSONObject().apply {
            put("roomId", "room-legacy")
            put("version", 1)
            put("lastModified", "")
            put("lastModifiedBy", "")
            put("courses", JSONArray().apply {
                put(JSONObject().apply {
                    put("id", 20L)
                    put("name", "大学物理")
                    put("location", "C303")
                    put("teacher", "王老师")
                    put("dayOfWeek", 3)
                    put("startHour", 14)
                    put("startMinute", 0)
                    put("endHour", 15)
                    put("endMinute", 40)
                    put("weekType", "ODD")
                    put("startWeek", 1)
                    put("endWeek", 16)
                    put("customWeeks", "")
                    put("personType", "PERSON_B")
                    put("startPeriod", 5)
                    put("endPeriod", 6)
                    put("isCustomTime", false)
                })
            })
            put("personAName", "我")
            put("personBName", "Ta")
        }

        val parser = SyncManagerParserHelper()
        val data = parser.parseCloudData(json)

        assertEquals(1, data.courses.size)
        val course = data.courses[0]
        assertEquals("legacy-person-b", course.ownerProfileId)
        assertEquals("PERSON_B", course.personType)
        assertEquals("大学物理", course.name)
    }
}

class SyncManagerParserHelper {

    companion object {
        private const val LEGACY_PERSON_A_PROFILE_ID = "legacy-person-a"
        private const val LEGACY_PERSON_B_PROFILE_ID = "legacy-person-b"
    }

    fun parseCloudData(json: JSONObject): CloudData {
        val coursesArray = json.optJSONArray("courses") ?: JSONArray()
        val courses = mutableListOf<CloudCourse>()
        for (i in 0 until coursesArray.length()) {
            val courseJson = coursesArray.getJSONObject(i)
            val id = courseJson.optLong("id", 0)
            val personType = courseJson.optString("personType", "PERSON_A")
            val syncId = courseJson.optString("courseUuid")
                .ifBlank { courseJson.optString("syncId") }
                .ifBlank { "legacy-$id" }
            val ownerProfileId = courseJson.optString("ownerProfileId").ifBlank {
                legacyProfileIdFor(personType)
            }
            courses.add(CloudCourse(
                id = id,
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
                personType = personType,
                startPeriod = courseJson.optInt("startPeriod", 1),
                endPeriod = courseJson.optInt("endPeriod", 1),
                isCustomTime = courseJson.optBoolean("isCustomTime", false),
                syncId = syncId,
                ownerProfileId = ownerProfileId
            ))
        }

        // 解析 Todo 数据
        val todosArray = json.optJSONArray("todos") ?: JSONArray()
        val todos = mutableListOf<CloudTodo>()
        for (i in 0 until todosArray.length()) {
            val todoJson = todosArray.getJSONObject(i)
            todos.add(CloudTodo(
                syncId = todoJson.optString("syncId", ""),
                ownerProfileId = todoJson.optString("ownerProfileId", ""),
                title = todoJson.optString("title", ""),
                description = todoJson.optString("description", ""),
                date = todoJson.optLong("date", 0),
                startHour = todoJson.optInt("startHour", -1),
                startMinute = todoJson.optInt("startMinute", -1),
                endHour = todoJson.optInt("endHour", -1),
                endMinute = todoJson.optInt("endMinute", -1),
                priority = todoJson.optString("priority", "MEDIUM"),
                status = todoJson.optString("status", "PENDING"),
                tags = todoJson.optString("tags", ""),
                linkedCourseSyncId = if (todoJson.isNull("linkedCourseSyncId")) null else todoJson.optString("linkedCourseSyncId"),
                repeatRuleId = if (todoJson.isNull("repeatRuleId")) null else todoJson.optString("repeatRuleId"),
                completedAt = if (todoJson.isNull("completedAt")) null else todoJson.optLong("completedAt")
            ))
        }

        // 解析 Todo 标签
        val todoTagsArray = json.optJSONArray("todoTags") ?: JSONArray()
        val todoTags = mutableListOf<CloudTodoTag>()
        for (i in 0 until todoTagsArray.length()) {
            val tagJson = todoTagsArray.getJSONObject(i)
            todoTags.add(CloudTodoTag(
                id = tagJson.optString("id", ""),
                name = tagJson.optString("name", ""),
                color = tagJson.optLong("color", 0),
                isPreset = tagJson.optBoolean("isPreset", false)
            ))
        }

        // 解析重复规则
        val repeatRulesArray = json.optJSONArray("repeatRules") ?: JSONArray()
        val repeatRules = mutableListOf<CloudRepeatRule>()
        for (i in 0 until repeatRulesArray.length()) {
            val ruleJson = repeatRulesArray.getJSONObject(i)
            repeatRules.add(CloudRepeatRule(
                id = ruleJson.optString("id", ""),
                frequency = ruleJson.optString("frequency", "DAILY"),
                interval = ruleJson.optInt("interval", 1),
                daysOfWeek = ruleJson.optString("daysOfWeek", ""),
                customDates = ruleJson.optString("customDates", ""),
                endDate = if (ruleJson.isNull("endDate")) null else ruleJson.optLong("endDate")
            ))
        }

        // 解析 profileSettings
        val profileSettings = linkedMapOf<String, CloudSettings>()
        json.optJSONObject("profileSettings")?.let { settingsJson ->
            val keys = settingsJson.keys()
            while (keys.hasNext()) {
                val profileId = keys.next()
                profileSettings[profileId] = parseSettings(settingsJson.getJSONObject(profileId))
            }
        }

        // 解析 profiles
        val profilesArray = json.optJSONArray("profiles")
        val profiles = parseProfiles(profilesArray)

        return CloudData(
            roomId = json.optString("roomId", ""),
            version = json.optLong("version", 0L),
            lastModified = json.optString("lastModified", ""),
            lastModifiedBy = json.optString("lastModifiedBy", ""),
            courses = courses,
            settingsA = json.optJSONObject("settingsA")?.let { parseSettings(it) },
            settingsB = json.optJSONObject("settingsB")?.let { parseSettings(it) },
            personAName = json.optString("personAName", "我"),
            personBName = json.optString("personBName", "Ta"),
            todos = todos,
            todoTags = todoTags,
            repeatRules = repeatRules,
            profiles = profiles,
            profileSettings = profileSettings
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

    fun parseProfiles(array: JSONArray?): List<CloudProfile> {
        if (array == null) return emptyList()
        val profiles = mutableListOf<CloudProfile>()
        for (i in 0 until array.length()) {
            val json = array.getJSONObject(i)
            val id = json.optString("id")
            if (id.isNotBlank()) {
                profiles.add(CloudProfile(id, json.optString("name", "")))
            }
        }
        return profiles
    }

    private fun legacyProfileIdFor(personType: String): String {
        return if (personType == "PERSON_B") {
            LEGACY_PERSON_B_PROFILE_ID
        } else {
            LEGACY_PERSON_A_PROFILE_ID
        }
    }
}
