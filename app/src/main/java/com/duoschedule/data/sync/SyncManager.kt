package com.duoschedule.data.sync

import android.util.Log
import com.duoschedule.data.local.CourseDao
import com.duoschedule.data.local.SettingsDataStore
import com.duoschedule.data.model.Course
import com.duoschedule.data.model.PersonType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    private val webDavClient: WebDavClient,
    private val syncPreferences: SyncPreferences,
    private val courseDao: CourseDao,
    private val settingsDataStore: SettingsDataStore
) {
    private val TAG = "SyncManager"
    private val syncMutex = Mutex()

    val syncStatus = MutableStateFlow(SyncStatus())

    suspend fun createRoom(config: SyncConfig): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val testResult = webDavClient.testConnection(config)
                if (testResult.isFailure) {
                    return@withContext Result.failure(testResult.exceptionOrNull() ?: Exception("连接测试失败"))
                }

                val dirs = listOf("duoschedule/", "duoschedule/sync/", "duoschedule/sync/${config.roomId}/")
                for (dir in dirs) {
                    val result = webDavClient.ensureDirectory(config, dir)
                    if (result.isFailure) {
                        return@withContext Result.failure(result.exceptionOrNull() ?: Exception("创建目录失败"))
                    }
                }

                val meta = JSONObject().apply {
                    put("roomId", config.roomId)
                    put("createdAt", Instant.now().toString())
                    put("createdBy", config.deviceId)
                    put("members", JSONArray().apply { put(config.deviceId) })
                    put("currentVersion", 0)
                }
                val metaResult = webDavClient.uploadJson(config, webDavClient.getMetaPath(config.roomId), meta)
                if (metaResult.isFailure) {
                    return@withContext Result.failure(metaResult.exceptionOrNull() ?: Exception("上传元数据失败"))
                }

                syncPreferences.saveSyncConfig(config)
                syncPreferences.setSyncEnabled(true)
                syncPreferences.updateLastSyncVersion(0)

                val syncCode = SyncCodeGenerator.generate(config)
                Result.success(syncCode)
            } catch (e: Exception) {
                Log.e(TAG, "createRoom failed", e)
                Result.failure(e)
            }
        }
    }

    suspend fun joinRoom(syncCode: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val parseResult = SyncCodeGenerator.parse(syncCode)
                if (parseResult.isFailure) {
                    return@withContext Result.failure(parseResult.exceptionOrNull() ?: Exception("同步码解析失败"))
                }

                val config = parseResult.getOrThrow()

                val testResult = webDavClient.testConnection(config)
                if (testResult.isFailure) {
                    return@withContext Result.failure(testResult.exceptionOrNull() ?: Exception("连接测试失败"))
                }

                val metaResult = webDavClient.downloadJson(config, webDavClient.getMetaPath(config.roomId))
                if (metaResult.isFailure) {
                    val ex = metaResult.exceptionOrNull()
                    if (ex?.message == "NOT_FOUND") {
                        return@withContext Result.failure(Exception("房间不存在，请检查同步码是否正确"))
                    }
                    return@withContext Result.failure(ex ?: Exception("获取房间信息失败"))
                }

                val metaJson = metaResult.getOrThrow()
                val members = metaJson.optJSONArray("members") ?: JSONArray()
                members.put(config.deviceId)
                metaJson.put("members", members)

                val updateResult = webDavClient.uploadJson(config, webDavClient.getMetaPath(config.roomId), metaJson)
                if (updateResult.isFailure) {
                    Log.w(TAG, "Failed to update meta members, continuing anyway")
                }

                syncPreferences.saveSyncConfig(config)
                syncPreferences.setSyncEnabled(true)

                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "joinRoom failed", e)
                Result.failure(e)
            }
        }
    }

    suspend fun sync(): SyncResult = syncMutex.withLock {
        withContext(Dispatchers.IO) {
            val config = syncPreferences.getSyncConfigSync()
            if (config == null) {
                return@withContext SyncResult.NotConfigured
            }

            val enabled = syncPreferences.syncEnabled.first()
            if (!enabled) {
                return@withContext SyncResult.NotConfigured
            }

            syncStatus.value = SyncStatus(state = SyncState.SYNCING)

            return@withContext try {
                val metaResult = webDavClient.downloadJson(config, webDavClient.getMetaPath(config.roomId))
                if (metaResult.isFailure) {
                    syncStatus.value = SyncStatus(state = SyncState.ERROR, errorMessage = metaResult.exceptionOrNull()?.message)
                    return@withContext SyncResult.Error(metaResult.exceptionOrNull()?.message ?: "获取元数据失败")
                }

                val metaJson = metaResult.getOrThrow()
                val cloudVersion = metaJson.optInt("currentVersion", 0)
                val localLastSyncVersion = syncPreferences.lastSyncVersion.first()

                if (cloudVersion == localLastSyncVersion) {
                    syncStatus.value = SyncStatus(state = SyncState.SYNCED, lastSyncVersion = cloudVersion)
                    return@withContext SyncResult.NoChanges
                }

                val dataResult = webDavClient.downloadJson(config, webDavClient.getDataPath(config.roomId))
                if (dataResult.isFailure) {
                    val ex = dataResult.exceptionOrNull()
                    if (ex?.message == "NOT_FOUND") {
                        val pushResult = pushLocalToCloud(config, metaJson)
                        return@withContext pushResult
                    }
                    syncStatus.value = SyncStatus(state = SyncState.ERROR, errorMessage = ex?.message)
                    return@withContext SyncResult.Error(ex?.message ?: "获取云端数据失败")
                }

                val cloudDataJson = dataResult.getOrThrow()
                val cloudData = parseCloudData(cloudDataJson)

                val localHasChanges = hasLocalChangesSince(localLastSyncVersion)

                if (!localHasChanges) {
                    val pullResult = applyCloudData(cloudData)
                    syncPreferences.updateLastSyncVersion(cloudVersion)
                    syncPreferences.updateLastSyncTime(System.currentTimeMillis())
                    syncStatus.value = SyncStatus(state = SyncState.SYNCED, lastSyncVersion = cloudVersion, lastSyncTime = System.currentTimeMillis())
                    return@withContext pullResult
                }

                val lastModifiedBy = cloudDataJson.optString("lastModifiedBy", "")
                if (lastModifiedBy == config.deviceId) {
                    syncPreferences.updateLastSyncVersion(cloudVersion)
                    syncStatus.value = SyncStatus(state = SyncState.SYNCED, lastSyncVersion = cloudVersion)
                    return@withContext SyncResult.NoChanges
                }

                val conflicts = detectConflicts(cloudData)
                if (conflicts.isNotEmpty()) {
                    syncStatus.value = SyncStatus(state = SyncState.CONFLICT, lastSyncVersion = localLastSyncVersion)
                    return@withContext SyncResult.Conflict(
                        localVersion = localLastSyncVersion,
                        cloudVersion = cloudVersion,
                        conflictItems = conflicts
                    )
                }

                val pullResult = applyCloudData(cloudData)
                syncPreferences.updateLastSyncVersion(cloudVersion)
                syncPreferences.updateLastSyncTime(System.currentTimeMillis())
                syncStatus.value = SyncStatus(state = SyncState.SYNCED, lastSyncVersion = cloudVersion, lastSyncTime = System.currentTimeMillis())
                pullResult
            } catch (e: Exception) {
                Log.e(TAG, "sync failed", e)
                syncStatus.value = SyncStatus(state = SyncState.ERROR, errorMessage = e.message)
                SyncResult.Error(e.message ?: "同步失败", e)
            }
        }
    }

    suspend fun pushChanges(): SyncResult = syncMutex.withLock {
        withContext(Dispatchers.IO) {
            val config = syncPreferences.getSyncConfigSync()
            if (config == null) return@withContext SyncResult.NotConfigured

            syncStatus.value = SyncStatus(state = SyncState.SYNCING)

            return@withContext try {
                val metaResult = webDavClient.downloadJson(config, webDavClient.getMetaPath(config.roomId))
                if (metaResult.isFailure) {
                    syncStatus.value = SyncStatus(state = SyncState.ERROR, errorMessage = metaResult.exceptionOrNull()?.message)
                    return@withContext SyncResult.Error(metaResult.exceptionOrNull()?.message ?: "获取元数据失败")
                }

                val metaJson = metaResult.getOrThrow()
                val pushResult = pushLocalToCloud(config, metaJson)
                pushResult
            } catch (e: Exception) {
                Log.e(TAG, "pushChanges failed", e)
                syncStatus.value = SyncStatus(state = SyncState.ERROR, errorMessage = e.message)
                SyncResult.Error(e.message ?: "推送失败", e)
            }
        }
    }

    suspend fun resolveConflicts(resolution: ConflictResolution): SyncResult = syncMutex.withLock {
        withContext(Dispatchers.IO) {
            val config = syncPreferences.getSyncConfigSync()
            if (config == null) return@withContext SyncResult.NotConfigured

            return@withContext try {
                val dataResult = webDavClient.downloadJson(config, webDavClient.getDataPath(config.roomId))
                if (dataResult.isFailure) {
                    return@withContext SyncResult.Error(dataResult.exceptionOrNull()?.message ?: "获取云端数据失败")
                }

                val cloudDataJson = dataResult.getOrThrow()
                val cloudData = parseCloudData(cloudDataJson)
                val localCourses = courseDao.getAllCoursesSync()

                val mergedCourses = mutableListOf<CloudCourse>()
                val cloudCourseMap = cloudData.courses.associateBy { it.id }
                val localCourseMap = localCourses.associateBy { it.id }

                for ((courseId, choice) in resolution.resolutions) {
                    val id = courseId.toLongOrNull() ?: continue
                    when (choice) {
                        ConflictChoice.KEEP_LOCAL -> {
                            localCourseMap[id]?.let { mergedCourses.add(it.toCloudCourse()) }
                        }
                        ConflictChoice.KEEP_CLOUD -> {
                            cloudCourseMap[id]?.let { mergedCourses.add(it) }
                        }
                        ConflictChoice.KEEP_BOTH -> {
                            localCourseMap[id]?.let { mergedCourses.add(it.toCloudCourse()) }
                            cloudCourseMap[id]?.let { cloud ->
                                val newId = (localCourses.maxOfOrNull { it.id } ?: 0) + 1 + mergedCourses.size
                                mergedCourses.add(cloud.copy(id = newId))
                            }
                        }
                    }
                }

                val nonConflictLocal = localCourses.filter { course ->
                    resolution.resolutions.containsKey(course.id.toString()).not() &&
                        !cloudCourseMap.containsKey(course.id)
                }
                val nonConflictCloud = cloudData.courses.filter { course ->
                    resolution.resolutions.containsKey(course.id.toString()).not() &&
                        !localCourseMap.containsKey(course.id)
                }

                mergedCourses.addAll(nonConflictLocal.map { it.toCloudCourse() })
                mergedCourses.addAll(nonConflictCloud)

                val mergedData = buildCloudDataJson(config, mergedCourses)
                val uploadResult = webDavClient.uploadJson(config, webDavClient.getDataPath(config.roomId), mergedData)
                if (uploadResult.isFailure) {
                    return@withContext SyncResult.Error(uploadResult.exceptionOrNull()?.message ?: "上传合并数据失败")
                }

                val metaResult = webDavClient.downloadJson(config, webDavClient.getMetaPath(config.roomId))
                if (metaResult.isSuccess) {
                    val metaJson = metaResult.getOrThrow()
                    val newVersion = metaJson.optInt("currentVersion", 0) + 1
                    metaJson.put("currentVersion", newVersion)
                    webDavClient.uploadJson(config, webDavClient.getMetaPath(config.roomId), metaJson)
                    syncPreferences.updateLastSyncVersion(newVersion)
                }

                val finalData = parseCloudData(mergedData)
                applyCloudData(finalData)

                syncPreferences.updateLastSyncTime(System.currentTimeMillis())
                syncStatus.value = SyncStatus(state = SyncState.SYNCED)

                SyncResult.Success(pulledCourses = mergedCourses.size, pushedCourses = mergedCourses.size)
            } catch (e: Exception) {
                Log.e(TAG, "resolveConflicts failed", e)
                SyncResult.Error(e.message ?: "冲突解决失败", e)
            }
        }
    }

    suspend fun leaveRoom(): Result<Unit> {
        syncPreferences.clearSyncConfig()
        syncStatus.value = SyncStatus(state = SyncState.DISABLED)
        return Result.success(Unit)
    }

    private suspend fun pushLocalToCloud(config: SyncConfig, metaJson: JSONObject): SyncResult {
        return try {
            val courses = courseDao.getAllCoursesSync()
            val cloudCourses = courses.map { it.toCloudCourse() }

            val settingsA = getCloudSettings(PersonType.PERSON_A)
            val settingsB = getCloudSettings(PersonType.PERSON_B)
            val personAName = settingsDataStore.personAName.first()
            val personBName = settingsDataStore.personBName.first()

            val dataJson = buildCloudDataJson(config, cloudCourses, settingsA, settingsB, personAName, personBName)

            val uploadResult = webDavClient.uploadJson(config, webDavClient.getDataPath(config.roomId), dataJson)
            if (uploadResult.isFailure) {
                syncStatus.value = SyncStatus(state = SyncState.ERROR, errorMessage = uploadResult.exceptionOrNull()?.message)
                return SyncResult.Error(uploadResult.exceptionOrNull()?.message ?: "上传数据失败")
            }

            val newVersion = metaJson.optInt("currentVersion", 0) + 1
            metaJson.put("currentVersion", newVersion)
            webDavClient.uploadJson(config, webDavClient.getMetaPath(config.roomId), metaJson)

            syncPreferences.updateLastSyncVersion(newVersion)
            syncPreferences.updateLastSyncTime(System.currentTimeMillis())
            syncStatus.value = SyncStatus(state = SyncState.SYNCED, lastSyncVersion = newVersion, lastSyncTime = System.currentTimeMillis())

            SyncResult.Success(pushedCourses = cloudCourses.size)
        } catch (e: Exception) {
            Log.e(TAG, "pushLocalToCloud failed", e)
            syncStatus.value = SyncStatus(state = SyncState.ERROR, errorMessage = e.message)
            SyncResult.Error(e.message ?: "推送失败", e)
        }
    }

    private suspend fun applyCloudData(cloudData: CloudData): SyncResult {
        var pulledCourses = 0
        var pulledSettings = false

        try {
            val existingCourses = courseDao.getAllCoursesSync()
            val existingIds = existingCourses.map { it.id }.toSet()
            val cloudIds = cloudData.courses.map { it.id }.toSet()

            val toDelete = existingIds - cloudIds
            for (id in toDelete) {
                courseDao.deleteCourseById(id)
            }

            for (cloudCourse in cloudData.courses) {
                val course = cloudCourse.toCourse()
                if (existingIds.contains(course.id)) {
                    courseDao.updateCourse(course)
                } else {
                    courseDao.insertCourse(course)
                }
                pulledCourses++
            }

            cloudData.settingsA?.let { settings ->
                applySettings(PersonType.PERSON_A, settings)
                pulledSettings = true
            }
            cloudData.settingsB?.let { settings ->
                applySettings(PersonType.PERSON_B, settings)
                pulledSettings = true
            }

            if (cloudData.personAName.isNotBlank()) {
                settingsDataStore.setPersonName(PersonType.PERSON_A, cloudData.personAName)
            }
            if (cloudData.personBName.isNotBlank()) {
                settingsDataStore.setPersonName(PersonType.PERSON_B, cloudData.personBName)
            }
        } catch (e: Exception) {
            Log.e(TAG, "applyCloudData failed", e)
            return SyncResult.Error(e.message ?: "应用云端数据失败")
        }

        return SyncResult.Success(pulledCourses = pulledCourses, pulledSettings = pulledSettings)
    }

    private suspend fun applySettings(personType: PersonType, settings: CloudSettings) {
        settingsDataStore.setSemesterStartDate(personType, LocalDate.ofEpochDay(settings.semesterStartDate))
        settingsDataStore.setTotalWeeks(personType, settings.totalWeeks)
        settingsDataStore.setCurrentWeek(personType, settings.currentWeek)
        settingsDataStore.setTotalPeriods(personType, settings.totalPeriods)
        settingsDataStore.setPeriodTimes(personType, settings.periodTimes)
    }

    private suspend fun getCloudSettings(personType: PersonType): CloudSettings {
        return CloudSettings(
            semesterStartDate = settingsDataStore.getSemesterStartDate(personType).first().toEpochDay(),
            totalWeeks = settingsDataStore.getTotalWeeks(personType).first(),
            currentWeek = settingsDataStore.getCurrentWeek(personType).first(),
            totalPeriods = settingsDataStore.getTotalPeriods(personType).first(),
            periodTimes = settingsDataStore.getPeriodTimes(personType).first()
        )
    }

    private suspend fun hasLocalChangesSince(lastSyncVersion: Int): Boolean {
        if (lastSyncVersion == 0) return true
        val lastSyncTime = syncPreferences.lastSyncTime.first()
        if (lastSyncTime == 0L) return true
        return true
    }

    private suspend fun detectConflicts(cloudData: CloudData): List<ConflictItem> {
        val localCourses = courseDao.getAllCoursesSync()
        val localMap = localCourses.associateBy { it.id }
        val cloudMap = cloudData.courses.associateBy { it.id }

        val conflicts = mutableListOf<ConflictItem>()
        val allIds = localMap.keys + cloudMap.keys

        for (id in allIds) {
            val local = localMap[id]
            val cloud = cloudMap[id]
            when {
                local != null && cloud != null && local != cloud.toCourse() -> {
                    conflicts.add(ConflictItem(
                        courseName = local.name,
                        localVersion = local.toCloudCourse(),
                        cloudVersion = cloud,
                        conflictType = ConflictType.BOTH_MODIFIED
                    ))
                }
                local == null && cloud != null -> {
                    conflicts.add(ConflictItem(
                        courseName = cloud.name,
                        localVersion = null,
                        cloudVersion = cloud,
                        conflictType = ConflictType.LOCAL_DELETED_CLOUD_MODIFIED
                    ))
                }
                local != null && cloud == null -> {
                    conflicts.add(ConflictItem(
                        courseName = local.name,
                        localVersion = local.toCloudCourse(),
                        cloudVersion = null,
                        conflictType = ConflictType.LOCAL_MODIFIED_CLOUD_DELETED
                    ))
                }
            }
        }
        return conflicts
    }

    private fun buildCloudDataJson(
        config: SyncConfig,
        courses: List<CloudCourse>,
        settingsA: CloudSettings? = null,
        settingsB: CloudSettings? = null,
        personAName: String = "我",
        personBName: String = "Ta"
    ): JSONObject {
        return JSONObject().apply {
            put("roomId", config.roomId)
            put("version", 0)
            put("lastModified", Instant.now().toString())
            put("lastModifiedBy", config.deviceId)
            put("courses", JSONArray().apply {
                for (course in courses) {
                    put(JSONObject().apply {
                        put("id", course.id)
                        put("name", course.name)
                        put("location", course.location)
                        put("teacher", course.teacher)
                        put("dayOfWeek", course.dayOfWeek)
                        put("startHour", course.startHour)
                        put("startMinute", course.startMinute)
                        put("endHour", course.endHour)
                        put("endMinute", course.endMinute)
                        put("weekType", course.weekType)
                        put("startWeek", course.startWeek)
                        put("endWeek", course.endWeek)
                        put("customWeeks", course.customWeeks)
                        put("personType", course.personType)
                        put("startPeriod", course.startPeriod)
                        put("endPeriod", course.endPeriod)
                        put("isCustomTime", course.isCustomTime)
                    })
                }
            })
            put("settingsA", settingsA?.let { settingsToJson(it) })
            put("settingsB", settingsB?.let { settingsToJson(it) })
            put("personAName", personAName)
            put("personBName", personBName)
        }
    }

    private fun settingsToJson(settings: CloudSettings): JSONObject {
        return JSONObject().apply {
            put("semesterStartDate", settings.semesterStartDate)
            put("totalWeeks", settings.totalWeeks)
            put("currentWeek", settings.currentWeek)
            put("totalPeriods", settings.totalPeriods)
            put("periodTimes", JSONArray().apply {
                for (time in settings.periodTimes) {
                    put(time)
                }
            })
        }
    }

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

    private fun parseSettings(json: JSONObject): CloudSettings {
        val periodTimesArray = json.optJSONArray("periodTimes") ?: JSONArray()
        val periodTimes = mutableListOf<String>()
        for (i in 0 until periodTimesArray.length()) {
            periodTimes.add(periodTimesArray.getString(i))
        }
        return CloudSettings(
            semesterStartDate = json.optLong("semesterStartDate", LocalDate.now().toEpochDay()),
            totalWeeks = json.optInt("totalWeeks", 16),
            currentWeek = json.optInt("currentWeek", 1),
            totalPeriods = json.optInt("totalPeriods", 10),
            periodTimes = periodTimes
        )
    }
}
