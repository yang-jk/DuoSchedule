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
import java.util.UUID
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

    private data class ProfileMapping(
        val myProfileId: String,
        val partnerProfileId: String?
    ) {
        fun profileIdFor(personType: PersonType): String? {
            return if (personType == PersonType.PERSON_A) myProfileId else partnerProfileId
        }

        fun personTypeFor(profileId: String): PersonType? {
            return when (profileId) {
                myProfileId -> PersonType.PERSON_A
                partnerProfileId -> PersonType.PERSON_B
                else -> null
            }
        }
    }

    suspend fun createRoom(config: SyncConfig): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val testResult = webDavClient.testConnection(config)
                if (testResult.isFailure) {
                    return@withContext Result.failure(testResult.exceptionOrNull() ?: Exception("Connection test failed"))
                }

                val dirs = listOf("duoschedule/", "duoschedule/sync/", "duoschedule/sync/${config.roomId}/")
                for (dir in dirs) {
                    val result = webDavClient.ensureDirectory(config, dir)
                    if (result.isFailure) {
                        return@withContext Result.failure(result.exceptionOrNull() ?: Exception("Create directory failed"))
                    }
                }

                val mapping = ensureProfileMapping(config)
                val profiles = getLocalProfiles(mapping)
                val meta = JSONObject().apply {
                    put("roomId", config.roomId)
                    put("createdAt", Instant.now().toString())
                    put("createdBy", config.deviceId)
                    put("members", JSONArray().apply { put(config.deviceId) })
                    put("currentVersion", 0)
                    put("schemaVersion", CLOUD_SCHEMA_VERSION)
                    put("profiles", profilesToJson(profiles))
                }
                val metaResult = webDavClient.uploadJson(config, webDavClient.getMetaPath(config.roomId), meta)
                if (metaResult.isFailure) {
                    return@withContext Result.failure(metaResult.exceptionOrNull() ?: Exception("Upload metadata failed"))
                }

                syncPreferences.saveSyncConfig(config)
                syncPreferences.setSyncEnabled(true)
                syncPreferences.updateLastSyncVersion(0)

                Result.success(SyncCodeGenerator.generate(config, mapping.myProfileId, mapping.partnerProfileId))
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
                    return@withContext Result.failure(parseResult.exceptionOrNull() ?: Exception("Invalid sync code"))
                }

                val config = parseResult.getOrThrow()

                val testResult = webDavClient.testConnection(config)
                if (testResult.isFailure) {
                    return@withContext Result.failure(testResult.exceptionOrNull() ?: Exception("Connection test failed"))
                }

                val metaResult = webDavClient.downloadJson(config, webDavClient.getMetaPath(config.roomId))
                if (metaResult.isFailure) {
                    val ex = metaResult.exceptionOrNull()
                    if (ex?.message == "NOT_FOUND") {
                        return@withContext Result.failure(Exception("Room not found"))
                    }
                    return@withContext Result.failure(ex ?: Exception("Download metadata failed"))
                }

                val metaJson = metaResult.getOrThrow()
                val mapping = determineJoinMapping(config, metaJson)
                val profiles = mergeProfiles(parseProfiles(metaJson.optJSONArray("profiles")), getLocalProfiles(mapping))

                metaJson.put("schemaVersion", CLOUD_SCHEMA_VERSION)
                metaJson.put("members", addUnique(metaJson.optJSONArray("members") ?: JSONArray(), config.deviceId))
                metaJson.put("profiles", profilesToJson(profiles))

                val updateResult = webDavClient.uploadJson(config, webDavClient.getMetaPath(config.roomId), metaJson)
                if (updateResult.isFailure) {
                    Log.w(TAG, "Failed to update meta members, continuing anyway")
                }

                syncPreferences.saveSyncConfig(config)
                syncPreferences.saveProfileMapping(mapping.myProfileId, mapping.partnerProfileId)
                syncPreferences.setSyncEnabled(true)

                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "joinRoom failed", e)
                Result.failure(e)
            }
        }
    }

    suspend fun getSyncCode(): String? {
        val config = syncPreferences.getSyncConfigSync() ?: return null
        val mapping = ensureProfileMapping(config)
        return SyncCodeGenerator.generate(config, mapping.myProfileId, mapping.partnerProfileId)
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
                    return@withContext SyncResult.Error(metaResult.exceptionOrNull()?.message ?: "Download metadata failed")
                }

                val metaJson = metaResult.getOrThrow()
                val cloudVersion = metaJson.optInt("currentVersion", 0)
                val localLastSyncVersion = syncPreferences.lastSyncVersion.first()

                val dataResult = webDavClient.downloadJson(config, webDavClient.getDataPath(config.roomId))
                if (dataResult.isFailure) {
                    val ex = dataResult.exceptionOrNull()
                    if (ex?.message == "NOT_FOUND") {
                        return@withContext pushLocalToCloud(config, metaJson, localLastSyncVersion = localLastSyncVersion)
                    }
                    syncStatus.value = SyncStatus(state = SyncState.ERROR, errorMessage = ex?.message)
                    return@withContext SyncResult.Error(ex?.message ?: "Download cloud data failed")
                }

                val cloudDataJson = dataResult.getOrThrow()
                val cloudData = parseCloudData(cloudDataJson)
                val mapping = ensureProfileMapping(config, metaJson, cloudData)

                val localCourses = courseDao.getAllCoursesSync()
                val localDiffers = localDataDiffersFromCloud(localCourses, cloudData, mapping)

                if (!localDiffers) {
                    syncPreferences.updateLastSyncVersion(cloudVersion)
                    syncPreferences.updateLastSyncTime(System.currentTimeMillis())
                    syncStatus.value = SyncStatus(
                        state = SyncState.SYNCED,
                        lastSyncVersion = cloudVersion,
                        lastSyncTime = System.currentTimeMillis()
                    )
                    return@withContext SyncResult.NoChanges
                }

                if (cloudVersion == localLastSyncVersion) {
                    return@withContext pushLocalToCloud(config, metaJson, cloudData, localLastSyncVersion)
                }

                val conflicts = detectConflicts(cloudData, mapping)
                if (conflicts.isNotEmpty()) {
                    syncStatus.value = SyncStatus(state = SyncState.CONFLICT, lastSyncVersion = localLastSyncVersion)
                    return@withContext SyncResult.Conflict(
                        localVersion = localLastSyncVersion,
                        cloudVersion = cloudVersion,
                        conflictItems = conflicts
                    )
                }

                val mergedCourses = if (localLastSyncVersion == 0) {
                    smartMergeForFirstSync(localCourses, cloudData, mapping)
                } else {
                    mergeCourses(localCourses, cloudData, mapping)
                }
                val mergedData = buildCloudDataJson(config, mergedCourses, cloudData, mapping)

                val uploadResult = webDavClient.uploadJson(config, webDavClient.getDataPath(config.roomId), mergedData)
                if (uploadResult.isFailure) {
                    syncStatus.value = SyncStatus(state = SyncState.ERROR, errorMessage = uploadResult.exceptionOrNull()?.message)
                    return@withContext SyncResult.Error(uploadResult.exceptionOrNull()?.message ?: "Upload merged data failed")
                }

                val newVersion = metaJson.optInt("currentVersion", 0) + 1
                metaJson.put("currentVersion", newVersion)
                metaJson.put("schemaVersion", CLOUD_SCHEMA_VERSION)
                metaJson.put("profiles", profilesToJson(mergeProfiles(cloudData.profiles, getLocalProfiles(mapping))))
                webDavClient.uploadJson(config, webDavClient.getMetaPath(config.roomId), metaJson)

                applyCloudData(parseCloudData(mergedData), mapping)

                syncPreferences.updateLastSyncVersion(newVersion)
                syncPreferences.updateLastSyncTime(System.currentTimeMillis())
                syncStatus.value = SyncStatus(
                    state = SyncState.SYNCED,
                    lastSyncVersion = newVersion,
                    lastSyncTime = System.currentTimeMillis()
                )

                SyncResult.Success(pulledCourses = mergedCourses.size, pushedCourses = mergedCourses.size)
            } catch (e: Exception) {
                Log.e(TAG, "sync failed", e)
                syncStatus.value = SyncStatus(state = SyncState.ERROR, errorMessage = e.message)
                SyncResult.Error(e.message ?: "Sync failed", e)
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
                    return@withContext SyncResult.Error(metaResult.exceptionOrNull()?.message ?: "Download metadata failed")
                }

                val metaJson = metaResult.getOrThrow()
                val cloudData = webDavClient.downloadJson(config, webDavClient.getDataPath(config.roomId))
                    .getOrNull()
                    ?.let { parseCloudData(it) }
                val localLastSyncVersion = syncPreferences.lastSyncVersion.first()
                pushLocalToCloud(config, metaJson, cloudData, localLastSyncVersion)
            } catch (e: Exception) {
                Log.e(TAG, "pushChanges failed", e)
                syncStatus.value = SyncStatus(state = SyncState.ERROR, errorMessage = e.message)
                SyncResult.Error(e.message ?: "Push failed", e)
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
                    return@withContext SyncResult.Error(dataResult.exceptionOrNull()?.message ?: "Download cloud data failed")
                }

                val metaResult = webDavClient.downloadJson(config, webDavClient.getMetaPath(config.roomId))
                val metaJson = metaResult.getOrNull()
                val cloudData = parseCloudData(dataResult.getOrThrow())
                val mapping = ensureProfileMapping(config, metaJson, cloudData)
                val localCourses = courseDao.getAllCoursesSync()

                val cloudCourseMap = cloudData.courses.associateBy { it.syncId }
                val localCourseMap = localCourses.associateBy { it.syncId }
                val merged = linkedMapOf<String, CloudCourse>()

                for (course in cloudData.courses) {
                    if (!resolution.resolutions.containsKey(course.syncId)) {
                        merged[course.syncId] = course
                    }
                }

                for (course in localCourses) {
                    if (!resolution.resolutions.containsKey(course.syncId) && !merged.containsKey(course.syncId)) {
                        mapping.profileIdFor(course.personType)?.let { ownerProfileId ->
                            merged[course.syncId] = course.toCloudCourse(ownerProfileId)
                        }
                    }
                }

                for ((courseKey, choice) in resolution.resolutions) {
                    when (choice) {
                        ConflictChoice.KEEP_LOCAL -> {
                            localCourseMap[courseKey]?.let { course ->
                                mapping.profileIdFor(course.personType)?.let { ownerProfileId ->
                                    merged[course.syncId] = course.toCloudCourse(ownerProfileId)
                                }
                            }
                        }

                        ConflictChoice.KEEP_CLOUD -> {
                            cloudCourseMap[courseKey]?.let { merged[it.syncId] = it }
                        }

                        ConflictChoice.KEEP_BOTH -> {
                            localCourseMap[courseKey]?.let { course ->
                                mapping.profileIdFor(course.personType)?.let { ownerProfileId ->
                                    merged[course.syncId] = course.toCloudCourse(ownerProfileId)
                                }
                            }
                            cloudCourseMap[courseKey]?.let { cloud ->
                                val copied = cloud.copy(id = 0, syncId = UUID.randomUUID().toString())
                                merged[copied.syncId] = copied
                            }
                        }
                    }
                }

                val mergedData = buildCloudDataJson(config, merged.values.toList(), cloudData, mapping)
                val uploadResult = webDavClient.uploadJson(config, webDavClient.getDataPath(config.roomId), mergedData)
                if (uploadResult.isFailure) {
                    return@withContext SyncResult.Error(uploadResult.exceptionOrNull()?.message ?: "Upload merged data failed")
                }

                if (metaJson != null) {
                    val newVersion = metaJson.optInt("currentVersion", 0) + 1
                    metaJson.put("currentVersion", newVersion)
                    metaJson.put("schemaVersion", CLOUD_SCHEMA_VERSION)
                    metaJson.put("profiles", profilesToJson(mergeProfiles(cloudData.profiles, getLocalProfiles(mapping))))
                    webDavClient.uploadJson(config, webDavClient.getMetaPath(config.roomId), metaJson)
                    syncPreferences.updateLastSyncVersion(newVersion)
                }

                applyCloudData(parseCloudData(mergedData), mapping)

                syncPreferences.updateLastSyncTime(System.currentTimeMillis())
                syncStatus.value = SyncStatus(state = SyncState.SYNCED)

                SyncResult.Success(pulledCourses = merged.size, pushedCourses = merged.size)
            } catch (e: Exception) {
                Log.e(TAG, "resolveConflicts failed", e)
                SyncResult.Error(e.message ?: "Resolve conflict failed", e)
            }
        }
    }

    suspend fun leaveRoom(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val config = syncPreferences.getSyncConfigSync()
                if (config != null) {
                    webDavClient.deleteFile(config, webDavClient.getDataPath(config.roomId))
                    webDavClient.deleteFile(config, webDavClient.getMetaPath(config.roomId))
                    webDavClient.deleteFile(config, webDavClient.getRoomPath(config.roomId))
                }
                syncPreferences.clearSyncConfig()
                syncStatus.value = SyncStatus(state = SyncState.DISABLED)
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "leaveRoom failed", e)
                syncPreferences.clearSyncConfig()
                syncStatus.value = SyncStatus(state = SyncState.DISABLED)
                Result.success(Unit)
            }
        }
    }

    private suspend fun pushLocalToCloud(
        config: SyncConfig,
        metaJson: JSONObject,
        existingCloudData: CloudData? = null,
        localLastSyncVersion: Int = 0
    ): SyncResult {
        return try {
            val mapping = ensureProfileMapping(config, metaJson, existingCloudData)
            val localCourses = courseDao.getAllCoursesSync()

            val cloudCourses = if (localLastSyncVersion == 0 && existingCloudData != null) {
                smartMergeForFirstSync(localCourses, existingCloudData, mapping)
            } else {
                val myProfileCloudCourses = localCourses
                    .filter { mapping.profileIdFor(it.personType) == mapping.myProfileId }
                    .mapNotNull { course -> course.toCloudCourse(mapping.myProfileId) }

                val partnerProfileCloudCourses = existingCloudData?.courses.orEmpty()
                    .filter { it.ownerProfileId == mapping.partnerProfileId }

                val preservedCloudCourses = existingCloudData?.courses.orEmpty().filter { course ->
                    mapping.personTypeFor(course.ownerProfileId) == null
                }

                preservedCloudCourses + partnerProfileCloudCourses + myProfileCloudCourses
            }

            val dataJson = buildCloudDataJson(config, cloudCourses, existingCloudData, mapping)

            val uploadResult = webDavClient.uploadJson(config, webDavClient.getDataPath(config.roomId), dataJson)
            if (uploadResult.isFailure) {
                syncStatus.value = SyncStatus(state = SyncState.ERROR, errorMessage = uploadResult.exceptionOrNull()?.message)
                return SyncResult.Error(uploadResult.exceptionOrNull()?.message ?: "Upload data failed")
            }

            val newVersion = metaJson.optInt("currentVersion", 0) + 1
            metaJson.put("currentVersion", newVersion)
            metaJson.put("schemaVersion", CLOUD_SCHEMA_VERSION)
            metaJson.put("profiles", profilesToJson(mergeProfiles(existingCloudData?.profiles.orEmpty(), getLocalProfiles(mapping))))
            webDavClient.uploadJson(config, webDavClient.getMetaPath(config.roomId), metaJson)

            syncPreferences.updateLastSyncVersion(newVersion)
            syncPreferences.updateLastSyncTime(System.currentTimeMillis())
            syncStatus.value = SyncStatus(
                state = SyncState.SYNCED,
                lastSyncVersion = newVersion,
                lastSyncTime = System.currentTimeMillis()
            )

            SyncResult.Success(pushedCourses = cloudCourses.size, pushedSettings = true)
        } catch (e: Exception) {
            Log.e(TAG, "pushLocalToCloud failed", e)
            syncStatus.value = SyncStatus(state = SyncState.ERROR, errorMessage = e.message)
            SyncResult.Error(e.message ?: "Push failed", e)
        }
    }

    private suspend fun applyCloudData(cloudData: CloudData, mapping: ProfileMapping): SyncResult {
        var pulledCourses = 0
        var pulledSettings = false

        try {
            val existingCourses = courseDao.getAllCoursesSync()
            val existingBySyncId = existingCourses.associateBy { it.syncId }
            val selectedCloudCourses = cloudData.courses.mapNotNull { cloudCourse ->
                mapping.personTypeFor(cloudCourse.ownerProfileId)?.let { personType -> cloudCourse to personType }
            }
            val cloudSyncIds = selectedCloudCourses.map { it.first.syncId }.toSet()

            for (course in existingCourses) {
                if (!cloudSyncIds.contains(course.syncId)) {
                    courseDao.deleteCourseById(course.id)
                }
            }

            for ((cloudCourse, personType) in selectedCloudCourses) {
                val existing = existingBySyncId[cloudCourse.syncId]
                val course = cloudCourse.toCourse(personType).copy(
                    id = existing?.id ?: 0,
                    syncId = cloudCourse.syncId
                )
                if (existing != null) {
                    courseDao.updateCourse(course)
                } else {
                    courseDao.insertCourse(course)
                }
                pulledCourses++
            }

            val settingsByProfile = cloudData.profileSettings.ifEmpty {
                buildMap {
                    cloudData.settingsA?.let { put(LEGACY_PERSON_A_PROFILE_ID, it) }
                    cloudData.settingsB?.let { put(LEGACY_PERSON_B_PROFILE_ID, it) }
                }
            }
            settingsByProfile[mapping.myProfileId]?.let { settings ->
                applySettings(PersonType.PERSON_A, settings)
                pulledSettings = true
            }
            mapping.partnerProfileId?.let { partnerProfileId ->
                settingsByProfile[partnerProfileId]?.let { settings ->
                    applySettings(PersonType.PERSON_B, settings)
                    pulledSettings = true
                }
            }

            val profiles = cloudData.profiles.ifEmpty {
                listOf(
                    CloudProfile(LEGACY_PERSON_A_PROFILE_ID, cloudData.personAName),
                    CloudProfile(LEGACY_PERSON_B_PROFILE_ID, cloudData.personBName)
                )
            }
            profiles.firstOrNull { it.id == mapping.myProfileId }?.name?.takeIf { it.isNotBlank() }?.let {
                settingsDataStore.setPersonName(PersonType.PERSON_A, it)
            }
            mapping.partnerProfileId?.let { partnerProfileId ->
                profiles.firstOrNull { it.id == partnerProfileId }?.name?.takeIf { it.isNotBlank() }?.let {
                    settingsDataStore.setPersonName(PersonType.PERSON_B, it)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "applyCloudData failed", e)
            return SyncResult.Error(e.message ?: "Apply cloud data failed")
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

    private suspend fun localDataDiffersFromCloud(
        localCourses: List<Course>,
        cloudData: CloudData,
        mapping: ProfileMapping
    ): Boolean {
        val selectedCloudCourses = cloudData.courses.mapNotNull { cloudCourse ->
            mapping.personTypeFor(cloudCourse.ownerProfileId)?.let { personType -> cloudCourse to personType }
        }
        if (localCourses.size != selectedCloudCourses.size) return true

        val cloudCourseMap = selectedCloudCourses.associateBy { it.first.syncId }
        for (local in localCourses) {
            val cloud = cloudCourseMap[local.syncId] ?: return true
            if (!courseContentEquals(local, cloud.first, cloud.second)) return true
        }
        return false
    }

    private fun mergeCourses(
        localCourses: List<Course>,
        cloudData: CloudData,
        mapping: ProfileMapping
    ): List<CloudCourse> {
        val merged = linkedMapOf<String, CloudCourse>()
        for (course in cloudData.courses) {
            merged[course.syncId] = course
        }
        for (course in localCourses) {
            mapping.profileIdFor(course.personType)?.let { ownerProfileId ->
                merged[course.syncId] = course.toCloudCourse(ownerProfileId)
            }
        }
        return merged.values.toList()
    }

    private suspend fun detectConflicts(cloudData: CloudData, mapping: ProfileMapping): List<ConflictItem> {
        val localCourses = courseDao.getAllCoursesSync()
        val localMap = localCourses.associateBy { it.syncId }
        val selectedCloudCourses = cloudData.courses.mapNotNull { cloudCourse ->
            mapping.personTypeFor(cloudCourse.ownerProfileId)?.let { personType -> cloudCourse to personType }
        }
        val conflicts = mutableListOf<ConflictItem>()

        for ((cloud, personType) in selectedCloudCourses) {
            val local = localMap[cloud.syncId]
            if (local != null && !courseContentEquals(local, cloud, personType)) {
                conflicts.add(
                    ConflictItem(
                        courseName = local.name,
                        localVersion = mapping.profileIdFor(local.personType)?.let { local.toCloudCourse(it) },
                        cloudVersion = cloud,
                        conflictType = ConflictType.BOTH_MODIFIED,
                        courseKey = cloud.syncId
                    )
                )
            }
        }
        return conflicts
    }

    private fun courseContentEquals(local: Course, cloud: CloudCourse, cloudPersonType: PersonType): Boolean {
        return local.name == cloud.name &&
            local.location == cloud.location &&
            local.teacher == cloud.teacher &&
            local.dayOfWeek == cloud.dayOfWeek &&
            local.startHour == cloud.startHour &&
            local.startMinute == cloud.startMinute &&
            local.endHour == cloud.endHour &&
            local.endMinute == cloud.endMinute &&
            local.weekType.name == cloud.weekType &&
            local.startWeek == cloud.startWeek &&
            local.endWeek == cloud.endWeek &&
            local.customWeeks == cloud.customWeeks &&
            local.personType == cloudPersonType &&
            local.startPeriod == cloud.startPeriod &&
            local.endPeriod == cloud.endPeriod &&
            local.isCustomTime == cloud.isCustomTime
    }

    private fun contentMatchKey(course: CloudCourse): String {
        return "${course.name}|${course.dayOfWeek}|${course.startHour}|${course.startMinute}|" +
            "${course.endHour}|${course.endMinute}|${course.location}|${course.teacher}|" +
            "${course.weekType}|${course.startWeek}|${course.endWeek}|${course.customWeeks}|" +
            "${course.startPeriod}|${course.endPeriod}|${course.isCustomTime}"
    }

    private suspend fun smartMergeForFirstSync(
        localCourses: List<Course>,
        cloudData: CloudData,
        mapping: ProfileMapping
    ): List<CloudCourse> {
        val cloudByProfile = cloudData.courses.groupBy { it.ownerProfileId }
        val merged = mutableListOf<CloudCourse>()
        val matchedCloudSyncIds = mutableSetOf<String>()

        for (personType in listOf(PersonType.PERSON_A, PersonType.PERSON_B)) {
            val profileId = mapping.profileIdFor(personType) ?: continue
            val localForPerson = localCourses.filter { it.personType == personType }
            val cloudForProfile = cloudByProfile[profileId].orEmpty()

            val cloudKeyMap = mutableMapOf<String, CloudCourse>()
            for (cloudCourse in cloudForProfile) {
                cloudKeyMap[contentMatchKey(cloudCourse)] = cloudCourse
            }

            for (localCourse in localForPerson) {
                val localCloud = localCourse.toCloudCourse(profileId)
                val key = contentMatchKey(localCloud)
                val matchedCloud = cloudKeyMap.remove(key)
                if (matchedCloud != null) {
                    matchedCloudSyncIds.add(matchedCloud.syncId)
                    courseDao.updateCourse(localCourse.copy(syncId = matchedCloud.syncId))
                    merged.add(matchedCloud)
                } else {
                    merged.add(localCloud)
                }
            }

            for (remainingCloud in cloudKeyMap.values) {
                merged.add(remainingCloud)
            }
        }

        for ((profileId, courses) in cloudByProfile) {
            if (profileId != mapping.myProfileId && profileId != mapping.partnerProfileId) {
                for (course in courses) {
                    if (!matchedCloudSyncIds.contains(course.syncId)) {
                        merged.add(course)
                    }
                }
            }
        }

        return merged
    }

    private suspend fun buildCloudDataJson(
        config: SyncConfig,
        courses: List<CloudCourse>,
        existingCloudData: CloudData?,
        mapping: ProfileMapping
    ): JSONObject {
        val settingsA = getCloudSettings(PersonType.PERSON_A)
        val settingsB = getCloudSettings(PersonType.PERSON_B)
        val personAName = settingsDataStore.personAName.first()
        val personBName = settingsDataStore.personBName.first()
        val localProfiles = getLocalProfiles(mapping)
        val profiles = mergeProfiles(existingCloudData?.profiles.orEmpty(), localProfiles)
        val profileSettings = linkedMapOf<String, CloudSettings>()
        existingCloudData?.profileSettings?.let { profileSettings.putAll(it) }
        profileSettings[mapping.myProfileId] = settingsA
        mapping.partnerProfileId?.let { profileSettings[it] = settingsB }

        return buildCloudDataJson(
            config = config,
            courses = courses,
            profiles = profiles,
            profileSettings = profileSettings,
            settingsA = settingsA,
            settingsB = settingsB,
            personAName = personAName,
            personBName = personBName
        )
    }

    private fun buildCloudDataJson(
        config: SyncConfig,
        courses: List<CloudCourse>,
        profiles: List<CloudProfile>,
        profileSettings: Map<String, CloudSettings>,
        settingsA: CloudSettings? = null,
        settingsB: CloudSettings? = null,
        personAName: String = "Me",
        personBName: String = "Ta"
    ): JSONObject {
        return JSONObject().apply {
            put("schemaVersion", CLOUD_SCHEMA_VERSION)
            put("roomId", config.roomId)
            put("version", 0)
            put("lastModified", Instant.now().toString())
            put("lastModifiedBy", config.deviceId)
            put("profiles", profilesToJson(profiles))
            put("profileSettings", JSONObject().apply {
                for ((profileId, settings) in profileSettings) {
                    put(profileId, settingsToJson(settings))
                }
            })
            put("courses", JSONArray().apply {
                for (course in courses) {
                    val syncId = course.syncId.ifBlank { "legacy-${course.id}" }
                    put(JSONObject().apply {
                        put("id", course.id)
                        put("courseUuid", syncId)
                        put("syncId", syncId)
                        put("ownerProfileId", course.ownerProfileId)
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
        val schemaVersion = json.optInt(
            "schemaVersion",
            if (json.has("profiles") || json.has("profileSettings")) CLOUD_SCHEMA_VERSION else 1
        )
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
            courses.add(
                CloudCourse(
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
                )
            )
        }

        val profileSettings = linkedMapOf<String, CloudSettings>()
        json.optJSONObject("profileSettings")?.let { settingsJson ->
            val keys = settingsJson.keys()
            while (keys.hasNext()) {
                val profileId = keys.next()
                profileSettings[profileId] = parseSettings(settingsJson.getJSONObject(profileId))
            }
        }

        val personAName = json.optString("personAName", "Me")
        val personBName = json.optString("personBName", "Ta")
        val parsedProfiles = parseProfiles(json.optJSONArray("profiles"))
        val profiles = if (parsedProfiles.isNotEmpty()) {
            parsedProfiles
        } else {
            listOf(
                CloudProfile(LEGACY_PERSON_A_PROFILE_ID, personAName),
                CloudProfile(LEGACY_PERSON_B_PROFILE_ID, personBName)
            )
        }

        return CloudData(
            roomId = json.optString("roomId", ""),
            version = json.optInt("version", 0),
            lastModified = json.optString("lastModified", ""),
            lastModifiedBy = json.optString("lastModifiedBy", ""),
            courses = courses,
            settingsA = json.optJSONObject("settingsA")?.let { parseSettings(it) },
            settingsB = json.optJSONObject("settingsB")?.let { parseSettings(it) },
            personAName = personAName,
            personBName = personBName,
            schemaVersion = schemaVersion,
            profiles = profiles,
            profileSettings = profileSettings
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

    private suspend fun ensureProfileMapping(
        config: SyncConfig,
        metaJson: JSONObject? = null,
        cloudData: CloudData? = null
    ): ProfileMapping {
        val existingMyProfileId = syncPreferences.getMyProfileIdSync()
        val existingPartnerProfileId = syncPreferences.getPartnerProfileIdSync()
        if (!existingMyProfileId.isNullOrBlank() && !existingPartnerProfileId.isNullOrBlank()) {
            return ProfileMapping(existingMyProfileId, existingPartnerProfileId)
        }

        val mapping = when {
            !config.inviteReceiverProfileId.isNullOrBlank() -> {
                ProfileMapping(config.inviteReceiverProfileId, config.inviteSenderProfileId)
            }

            cloudData != null && cloudData.schemaVersion <= 1 -> {
                val isCreator = metaJson?.optString("createdBy") == config.deviceId
                if (isCreator) {
                    ProfileMapping(LEGACY_PERSON_A_PROFILE_ID, LEGACY_PERSON_B_PROFILE_ID)
                } else {
                    ProfileMapping(LEGACY_PERSON_B_PROFILE_ID, LEGACY_PERSON_A_PROFILE_ID)
                }
            }

            else -> {
                ProfileMapping(
                    existingMyProfileId ?: SyncCodeGenerator.generateProfileId(),
                    existingPartnerProfileId ?: SyncCodeGenerator.generateProfileId()
                )
            }
        }

        syncPreferences.saveProfileMapping(mapping.myProfileId, mapping.partnerProfileId)
        return mapping
    }

    private suspend fun determineJoinMapping(config: SyncConfig, metaJson: JSONObject): ProfileMapping {
        if (!config.inviteReceiverProfileId.isNullOrBlank()) {
            return ProfileMapping(config.inviteReceiverProfileId, config.inviteSenderProfileId)
        }

        val profiles = parseProfiles(metaJson.optJSONArray("profiles"))
        val creatorProfileId = profiles.firstOrNull()?.id
        val myProfileId = SyncCodeGenerator.generateProfileId()
        return ProfileMapping(myProfileId, creatorProfileId)
    }

    private suspend fun getLocalProfiles(mapping: ProfileMapping): List<CloudProfile> {
        val personAName = settingsDataStore.personAName.first().ifBlank { "Me" }
        val personBName = settingsDataStore.personBName.first().ifBlank { "Ta" }
        return buildList {
            add(CloudProfile(mapping.myProfileId, personAName))
            mapping.partnerProfileId?.let { add(CloudProfile(it, personBName)) }
        }
    }

    private fun parseProfiles(array: JSONArray?): List<CloudProfile> {
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

    private fun profilesToJson(profiles: List<CloudProfile>): JSONArray {
        return JSONArray().apply {
            for (profile in profiles) {
                put(JSONObject().apply {
                    put("id", profile.id)
                    put("name", profile.name)
                })
            }
        }
    }

    private fun mergeProfiles(existing: List<CloudProfile>, local: List<CloudProfile>): List<CloudProfile> {
        val merged = linkedMapOf<String, CloudProfile>()
        for (profile in existing) merged[profile.id] = profile
        for (profile in local) merged[profile.id] = profile
        return merged.values.toList()
    }

    private fun addUnique(array: JSONArray, value: String): JSONArray {
        for (i in 0 until array.length()) {
            if (array.optString(i) == value) return array
        }
        array.put(value)
        return array
    }

    private fun legacyProfileIdFor(personType: String): String {
        return if (personType == PersonType.PERSON_B.name) {
            LEGACY_PERSON_B_PROFILE_ID
        } else {
            LEGACY_PERSON_A_PROFILE_ID
        }
    }

    companion object {
        private const val CLOUD_SCHEMA_VERSION = 2
        private const val LEGACY_PERSON_A_PROFILE_ID = "legacy-person-a"
        private const val LEGACY_PERSON_B_PROFILE_ID = "legacy-person-b"
    }
}
