package com.duoschedule.data.sync

import android.util.Log
import com.duoschedule.data.local.CourseDao
import com.duoschedule.data.local.SettingsDataStore
import com.duoschedule.data.local.TodoDao
import com.duoschedule.data.model.Course
import com.duoschedule.data.model.PersonType
import com.duoschedule.data.model.Todo
import com.duoschedule.util.AppLogger
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
    private val todoDao: TodoDao,
    private val settingsDataStore: SettingsDataStore
) {
    private val TAG = "SyncManager"
    private val syncMutex = Mutex()

    val syncStatus = MutableStateFlow(SyncStatus())

    suspend fun testConnection(username: String, password: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val config = SyncConfig(
                    webDavUrl = "https://dav.jianguoyun.com/dav/",
                    username = username,
                    password = password,
                    roomId = "",
                    deviceId = ""
                )
                val result = webDavClient.testConnection(config)
                if (result.isSuccess) {
                    "连接成功"
                } else {
                    "连接失败: ${result.exceptionOrNull()?.message ?: "未知错误"}"
                }
            } catch (e: Exception) {
                "连接失败: ${e.message}"
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

                val roomCode = SyncCodeGenerator.generateRoomCode()
                val inviteCode = SyncCodeGenerator.generateInviteCode(config)
                val mapping = ensureProfileMapping(config)
                val profiles = getLocalProfiles(mapping)
                val meta = JSONObject().apply {
                    put("roomId", config.roomId)
                    put("roomCode", roomCode)
                    put("createdAt", Instant.now().toString())
                    put("createdBy", config.deviceId)
                    put("members", JSONArray().apply { put(config.deviceId) })
                    put("currentVersion", 0L)
                    put("schemaVersion", CLOUD_SCHEMA_VERSION)
                    put("profiles", profilesToJson(profiles))
                }
                val metaResult = webDavClient.uploadJson(config, webDavClient.getMetaPath(config.roomId), meta)
                if (metaResult.isFailure) {
                    return@withContext Result.failure(metaResult.exceptionOrNull() ?: Exception("Upload metadata failed"))
                }

                val localCourses = courseDao.getAllCoursesSync().map { it.toCloudCourse(mapping.myProfileId) }
                val localTodos = todoDao.getAllTodosSync().map { it.toCloudTodo(mapping.myProfileId) }
                val initialData = buildCloudDataJson(config, localCourses, localTodos, null, mapping)
                val dataResult = webDavClient.uploadJson(config, webDavClient.getDataPath(config.roomId), initialData)
                if (dataResult.isFailure) {
                    return@withContext Result.failure(dataResult.exceptionOrNull() ?: Exception("Upload initial data failed"))
                }

                syncPreferences.saveSyncConfig(config)
                syncPreferences.setSyncEnabled(true)
                syncPreferences.updateLastSyncVersion(0L)
                syncPreferences.saveRoomCode(roomCode)
                syncPreferences.saveInviteCode(inviteCode)

                AppLogger.i("Sync", "创建房间成功: roomCode=$roomCode, inviteCode=$inviteCode")
                Result.success(inviteCode)
            } catch (e: Exception) {
                Log.e(TAG, "createRoom failed", e)
                AppLogger.e("Sync", "创建房间失败", e)
                Result.failure(e)
            }
        }
    }

    suspend fun joinRoom(inviteCode: String): Result<JoinRoomInfo> {
        return withContext(Dispatchers.IO) {
            try {
                val config = SyncCodeGenerator.decodeInviteCode(inviteCode.trim())
                if (config == null) {
                    return@withContext Result.failure(Exception("邀请码无效"))
                }

                val testResult = webDavClient.testConnection(config)
                if (testResult.isFailure) {
                    return@withContext Result.failure(Exception("连接失败，请检查邀请码是否正确"))
                }

                val findResult = findRoomByConfig(config)
                if (findResult.isFailure) {
                    return@withContext Result.failure(findResult.exceptionOrNull() ?: Exception("房间不存在或已过期"))
                }
                Result.success(findResult.getOrThrow())
            } catch (e: Exception) {
                Log.e(TAG, "joinRoom failed", e)
                AppLogger.e("Sync", "加入房间失败", e)
                Result.failure(e)
            }
        }
    }

    suspend fun joinRoomWithRoleSelection(
        joinInfo: JoinRoomInfo,
        selectedProfileId: String
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val config = joinInfo.config

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

                // 根据用户选择确定 ProfileMapping
                val myProfileId: String
                val partnerProfileId: String
                if (selectedProfileId == joinInfo.profileA.id) {
                    myProfileId = joinInfo.profileA.id
                    partnerProfileId = joinInfo.profileB.id
                } else {
                    myProfileId = joinInfo.profileB.id
                    partnerProfileId = joinInfo.profileA.id
                }
                val mapping = ProfileMapping(myProfileId, partnerProfileId)

                // 下载云端数据用于合并
                val dataResult = webDavClient.downloadJson(config, webDavClient.getDataPath(config.roomId))
                val existingCloudData = dataResult.getOrNull()?.let { parseCloudData(it) }

                // 合并数据：首次同步使用 smartMerge
                val localCourses = courseDao.getAllCoursesSync()
                val localTodos = todoDao.getAllTodosSync()
                val mergedCourses = if (existingCloudData != null) {
                    smartMergeForFirstSync(localCourses, existingCloudData, mapping)
                } else {
                    localCourses.mapNotNull { course ->
                        mapping.profileIdFor(course.personType)?.let { course.toCloudCourse(it) }
                    }
                }
                val mergedTodos = if (existingCloudData != null) {
                    smartMergeTodosForFirstSync(localTodos, existingCloudData, mapping)
                } else {
                    localTodos.mapNotNull { todo ->
                        mapping.profileIdFor(todo.personType)?.let { todo.toCloudTodo(it) }
                    }
                }

                val profiles = mergeProfiles(
                    parseProfiles(metaJson.optJSONArray("profiles")),
                    getLocalProfiles(mapping)
                )
                metaJson.put("schemaVersion", CLOUD_SCHEMA_VERSION)
                metaJson.put("members", addUnique(metaJson.optJSONArray("members") ?: JSONArray(), config.deviceId))
                metaJson.put("profiles", profilesToJson(profiles))

                val updateResult = webDavClient.uploadJson(config, webDavClient.getMetaPath(config.roomId), metaJson)
                if (updateResult.isFailure) {
                    Log.w(TAG, "Failed to update meta members, continuing anyway")
                }

                // 上传合并后的数据
                backupCloudData(config)
                if (existingCloudData != null) {
                    val mergedData = buildCloudDataJson(config, mergedCourses, mergedTodos, existingCloudData, mapping)
                    webDavClient.uploadJson(config, webDavClient.getDataPath(config.roomId), mergedData)
                } else {
                    val dataJson = buildCloudDataJson(config, mergedCourses, mergedTodos, null, mapping)
                    webDavClient.uploadJson(config, webDavClient.getDataPath(config.roomId), dataJson)
                }

                // 应用合并后的数据到本地
                val appliedData = if (existingCloudData != null) {
                    parseCloudData(buildCloudDataJson(config, mergedCourses, mergedTodos, existingCloudData, mapping))
                } else {
                    parseCloudData(buildCloudDataJson(config, mergedCourses, mergedTodos, null, mapping))
                }
                applyCloudData(appliedData, mapping)
                applyCloudTodos(appliedData, mapping)

                syncPreferences.saveSyncConfig(config)
                syncPreferences.saveProfileMapping(mapping.myProfileId, mapping.partnerProfileId)
                syncPreferences.setSyncEnabled(true)
                syncPreferences.saveRoomCode(joinInfo.roomCode)
                syncPreferences.saveInviteCode(SyncCodeGenerator.generateInviteCode(config))

                AppLogger.i("Sync", "加入房间成功: roomCode=${joinInfo.roomCode}")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "joinRoomWithRoleSelection failed", e)
                AppLogger.e("Sync", "加入房间（选择角色）失败", e)
                Result.failure(e)
            }
        }
    }

    /**
     * 通过邀请码解码的 config 直接查找房间
     * 下载 meta.json 获取房间信息
     */
    private suspend fun findRoomByConfig(config: SyncConfig): Result<JoinRoomInfo> {
        return withContext(Dispatchers.IO) {
            try {
                val metaPath = webDavClient.getMetaPath(config.roomId)
                val metaResult = webDavClient.downloadJson(config, metaPath)
                if (metaResult.isFailure) {
                    val ex = metaResult.exceptionOrNull()
                    if (ex?.message == "NOT_FOUND") {
                        return@withContext Result.failure(Exception("房间不存在或已过期"))
                    }
                    return@withContext Result.failure(ex ?: Exception("获取房间信息失败"))
                }

                val metaJson = metaResult.getOrThrow()
                val roomCode = metaJson.optString("roomCode", "")
                val profiles = parseProfiles(metaJson.optJSONArray("profiles"))
                val personAName = metaJson.optString("personAName", "Me")
                val personBName = metaJson.optString("personBName", "Ta")

                val profileA = profiles.getOrElse(0) { CloudProfile(SyncCodeGenerator.generateProfileId(), personAName) }
                val profileB = profiles.getOrElse(1) { CloudProfile(SyncCodeGenerator.generateProfileId(), personBName) }

                Result.success(
                    JoinRoomInfo(
                        roomCode = roomCode,
                        config = config,
                        profileA = profileA,
                        profileB = profileB,
                        personAName = profileA.name.ifBlank { personAName },
                        personBName = profileB.name.ifBlank { personBName }
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "findRoomByConfig failed", e)
                Result.failure(e)
            }
        }
    }

    suspend fun getRoomCode(): String? {
        return syncPreferences.getInviteCodeSync() ?: syncPreferences.getRoomCodeSync()
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

            AppLogger.i("Sync", "开始同步...")

            return@withContext try {
                val metaResult = webDavClient.downloadJson(config, webDavClient.getMetaPath(config.roomId))
                if (metaResult.isFailure) {
                    syncStatus.value = SyncStatus(state = SyncState.ERROR, errorMessage = metaResult.exceptionOrNull()?.message)
                    return@withContext SyncResult.Error(metaResult.exceptionOrNull()?.message ?: "Download metadata failed")
                }

                val metaJson = metaResult.getOrThrow()
                val cloudVersion = metaJson.optLong("currentVersion", 0L)
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
                val localTodos = todoDao.getAllTodosSync()
                val courseDiffers = localDataDiffersFromCloud(localCourses, cloudData, mapping)
                val todoDiffers = todoDataDiffersFromCloud(localTodos, cloudData, mapping)

                // 课程和 Todo 都无差异时才返回 NoChanges
                if (!courseDiffers && !todoDiffers) {
                    syncPreferences.updateLastSyncVersion(cloudVersion)
                    syncPreferences.updateLastSyncTime(System.currentTimeMillis())
                    syncStatus.value = SyncStatus(
                        state = SyncState.SYNCED,
                        lastSyncVersion = cloudVersion,
                        lastSyncTime = System.currentTimeMillis()
                    )
                    AppLogger.i("Sync", "同步完成: 无变更")
                    return@withContext SyncResult.NoChanges
                }

                if (cloudVersion == localLastSyncVersion) {
                    return@withContext pushLocalToCloud(config, metaJson, cloudData, localLastSyncVersion)
                }

                // 课程和 Todo 冲突分别检测，合并返回
                val courseConflicts = detectConflicts(cloudData, mapping)
                val todoConflicts = detectTodoConflicts(cloudData, mapping)
                val allConflicts = courseConflicts + todoConflicts
                if (allConflicts.isNotEmpty()) {
                    syncStatus.value = SyncStatus(state = SyncState.CONFLICT, lastSyncVersion = localLastSyncVersion)
                    AppLogger.w("Sync", "同步冲突: ${allConflicts.size} 个冲突项")
                    return@withContext SyncResult.Conflict(
                        localVersion = localLastSyncVersion,
                        cloudVersion = cloudVersion,
                        conflictItems = allConflicts
                    )
                }

                val mergedCourses = if (localLastSyncVersion == 0L) {
                    smartMergeForFirstSync(localCourses, cloudData, mapping)
                } else {
                    mergeCourses(localCourses, cloudData, mapping)
                }
                val mergedTodos = if (localLastSyncVersion == 0L) {
                    smartMergeTodosForFirstSync(localTodos, cloudData, mapping)
                } else {
                    mergeTodos(localTodos, cloudData, mapping)
                }
                val mergedData = buildCloudDataJson(config, mergedCourses, mergedTodos, cloudData, mapping)

                backupCloudData(config)
                val uploadResult = webDavClient.uploadJson(config, webDavClient.getDataPath(config.roomId), mergedData)
                if (uploadResult.isFailure) {
                    syncStatus.value = SyncStatus(state = SyncState.ERROR, errorMessage = uploadResult.exceptionOrNull()?.message)
                    return@withContext SyncResult.Error(uploadResult.exceptionOrNull()?.message ?: "Upload merged data failed")
                }

                val newVersion = System.currentTimeMillis()
                metaJson.put("currentVersion", newVersion)
                metaJson.put("schemaVersion", CLOUD_SCHEMA_VERSION)
                metaJson.put("profiles", profilesToJson(mergeProfiles(cloudData.profiles, getLocalProfiles(mapping))))
                webDavClient.uploadJson(config, webDavClient.getMetaPath(config.roomId), metaJson)

                val parsedMergedData = parseCloudData(mergedData)
                applyCloudData(parsedMergedData, mapping)
                applyCloudTodos(parsedMergedData, mapping)

                syncPreferences.updateLastSyncVersion(newVersion)
                syncPreferences.updateLastSyncTime(System.currentTimeMillis())
                syncStatus.value = SyncStatus(
                    state = SyncState.SYNCED,
                    lastSyncVersion = newVersion,
                    lastSyncTime = System.currentTimeMillis()
                )

                AppLogger.i("Sync", "同步完成: 合并 ${mergedCourses.size} 门课程, ${mergedTodos.size} 个待办")
                SyncResult.Success(pulledCourses = mergedCourses.size, pushedCourses = mergedCourses.size)
            } catch (e: Exception) {
                Log.e(TAG, "sync failed", e)
                syncStatus.value = SyncStatus(state = SyncState.ERROR, errorMessage = e.message)
                AppLogger.e("Sync", "同步失败", e)
                SyncResult.Error(e.message ?: "Sync failed", e)
            }
        }
    }

    suspend fun pushChanges(): SyncResult = syncMutex.withLock {
        withContext(Dispatchers.IO) {
            val config = syncPreferences.getSyncConfigSync()
            if (config == null) return@withContext SyncResult.NotConfigured

            syncStatus.value = SyncStatus(state = SyncState.SYNCING)

            AppLogger.i("Sync", "开始推送本地变更...")

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
                AppLogger.e("Sync", "推送变更失败", e)
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

                // ===== 课程冲突解决 =====
                val localCourses = courseDao.getAllCoursesSync()
                val cloudCourseMap = cloudData.courses.associateBy { it.syncId }
                val localCourseMap = localCourses.associateBy { it.syncId }
                val mergedCourses = linkedMapOf<String, CloudCourse>()

                for (course in cloudData.courses) {
                    if (!resolution.resolutions.containsKey(course.syncId)) {
                        mergedCourses[course.syncId] = course
                    }
                }

                for (course in localCourses) {
                    if (!resolution.resolutions.containsKey(course.syncId) && !mergedCourses.containsKey(course.syncId)) {
                        mapping.profileIdFor(course.personType)?.let { ownerProfileId ->
                            mergedCourses[course.syncId] = course.toCloudCourse(ownerProfileId)
                        }
                    }
                }

                for ((courseKey, choice) in resolution.resolutions) {
                    when (choice) {
                        ConflictChoice.KEEP_LOCAL -> {
                            localCourseMap[courseKey]?.let { course ->
                                mapping.profileIdFor(course.personType)?.let { ownerProfileId ->
                                    mergedCourses[course.syncId] = course.toCloudCourse(ownerProfileId)
                                }
                            }
                        }

                        ConflictChoice.KEEP_CLOUD -> {
                            cloudCourseMap[courseKey]?.let { mergedCourses[it.syncId] = it }
                        }

                        ConflictChoice.KEEP_BOTH -> {
                            localCourseMap[courseKey]?.let { course ->
                                mapping.profileIdFor(course.personType)?.let { ownerProfileId ->
                                    mergedCourses[course.syncId] = course.toCloudCourse(ownerProfileId)
                                }
                            }
                            cloudCourseMap[courseKey]?.let { cloud ->
                                val copied = cloud.copy(id = 0, syncId = UUID.randomUUID().toString())
                                mergedCourses[copied.syncId] = copied
                            }
                        }
                    }
                }

                // ===== Todo 冲突解决 =====
                val localTodos = todoDao.getAllTodosSync()
                val cloudTodoMap = cloudData.todos.associateBy { it.syncId }
                val localTodoMap = localTodos.associateBy { it.syncId }
                val mergedTodos = linkedMapOf<String, CloudTodo>()

                for (todo in cloudData.todos) {
                    if (!resolution.resolutions.containsKey(todo.syncId)) {
                        mergedTodos[todo.syncId] = todo
                    }
                }

                for (todo in localTodos) {
                    if (!resolution.resolutions.containsKey(todo.syncId) && !mergedTodos.containsKey(todo.syncId)) {
                        mapping.profileIdFor(todo.personType)?.let { ownerProfileId ->
                            mergedTodos[todo.syncId] = todo.toCloudTodo(ownerProfileId)
                        }
                    }
                }

                for ((todoKey, choice) in resolution.resolutions) {
                    when (choice) {
                        ConflictChoice.KEEP_LOCAL -> {
                            localTodoMap[todoKey]?.let { todo ->
                                mapping.profileIdFor(todo.personType)?.let { ownerProfileId ->
                                    mergedTodos[todo.syncId] = todo.toCloudTodo(ownerProfileId)
                                }
                            }
                        }

                        ConflictChoice.KEEP_CLOUD -> {
                            cloudTodoMap[todoKey]?.let { mergedTodos[it.syncId] = it }
                        }

                        ConflictChoice.KEEP_BOTH -> {
                            localTodoMap[todoKey]?.let { todo ->
                                mapping.profileIdFor(todo.personType)?.let { ownerProfileId ->
                                    mergedTodos[todo.syncId] = todo.toCloudTodo(ownerProfileId)
                                }
                            }
                            cloudTodoMap[todoKey]?.let { cloud ->
                                val copied = cloud.copy(syncId = UUID.randomUUID().toString())
                                mergedTodos[copied.syncId] = copied
                            }
                        }
                    }
                }

                val mergedData = buildCloudDataJson(
                    config, mergedCourses.values.toList(), mergedTodos.values.toList(), cloudData, mapping
                )
                backupCloudData(config)
                val uploadResult = webDavClient.uploadJson(config, webDavClient.getDataPath(config.roomId), mergedData)
                if (uploadResult.isFailure) {
                    return@withContext SyncResult.Error(uploadResult.exceptionOrNull()?.message ?: "Upload merged data failed")
                }

                if (metaJson != null) {
                    val newVersion = System.currentTimeMillis()
                    metaJson.put("currentVersion", newVersion)
                    metaJson.put("schemaVersion", CLOUD_SCHEMA_VERSION)
                    metaJson.put("profiles", profilesToJson(mergeProfiles(cloudData.profiles, getLocalProfiles(mapping))))
                    webDavClient.uploadJson(config, webDavClient.getMetaPath(config.roomId), metaJson)
                    syncPreferences.updateLastSyncVersion(newVersion)
                }

                val parsedMergedData = parseCloudData(mergedData)
                applyCloudData(parsedMergedData, mapping)
                applyCloudTodos(parsedMergedData, mapping)

                syncPreferences.updateLastSyncTime(System.currentTimeMillis())
                syncStatus.value = SyncStatus(state = SyncState.SYNCED)

                SyncResult.Success(pulledCourses = mergedCourses.size, pushedCourses = mergedCourses.size)
            } catch (e: Exception) {
                Log.e(TAG, "resolveConflicts failed", e)
                SyncResult.Error(e.message ?: "Resolve conflict failed", e)
            }
        }
    }

    suspend fun restoreFromBackup(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val config = syncPreferences.getSyncConfigSync() ?: return@withContext Result.failure(Exception("未配置同步"))
                val backupResult = webDavClient.downloadJson(config, webDavClient.getBackupPath(config.roomId))
                if (backupResult.isFailure) {
                    return@withContext Result.failure(Exception("备份文件不存在"))
                }
                val backupJson = backupResult.getOrThrow()
                val uploadResult = webDavClient.uploadJson(config, webDavClient.getDataPath(config.roomId), backupJson)
                if (uploadResult.isFailure) {
                    return@withContext Result.failure(Exception("恢复失败"))
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
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

    private suspend fun backupCloudData(config: SyncConfig) {
        try {
            val dataResult = webDavClient.downloadJson(config, webDavClient.getDataPath(config.roomId))
            if (dataResult.isSuccess) {
                val dataJson = dataResult.getOrThrow()
                webDavClient.uploadJson(config, webDavClient.getBackupPath(config.roomId), dataJson)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Backup cloud data failed, continuing anyway", e)
        }
    }

    private suspend fun pushLocalToCloud(
        config: SyncConfig,
        metaJson: JSONObject,
        existingCloudData: CloudData? = null,
        localLastSyncVersion: Long = 0L
    ): SyncResult {
        return try {
            val mapping = ensureProfileMapping(config, metaJson, existingCloudData)
            val localCourses = courseDao.getAllCoursesSync()
            val localTodos = todoDao.getAllTodosSync()

            val cloudCourses = if (localLastSyncVersion == 0L && existingCloudData != null) {
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

            val cloudTodos = pushLocalTodosToCloud(localTodos, existingCloudData, mapping)

            val dataJson = buildCloudDataJson(config, cloudCourses, cloudTodos, existingCloudData, mapping)

            backupCloudData(config)
            val uploadResult = webDavClient.uploadJson(config, webDavClient.getDataPath(config.roomId), dataJson)
            if (uploadResult.isFailure) {
                syncStatus.value = SyncStatus(state = SyncState.ERROR, errorMessage = uploadResult.exceptionOrNull()?.message)
                return SyncResult.Error(uploadResult.exceptionOrNull()?.message ?: "Upload data failed")
            }

            val newVersion = System.currentTimeMillis()
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

    // ========== Todo 同步方法 ==========

    /**
     * 将本地 Todo 推送到云端
     * 与课程推送逻辑一致：只替换 myProfileId 的 CloudTodo，保留 partnerProfileId 和未知 profile 的 CloudTodo
     */
    private fun pushLocalTodosToCloud(
        localTodos: List<Todo>,
        existingCloudData: CloudData?,
        mapping: ProfileMapping
    ): List<CloudTodo> {
        // 将本地 PERSON_A 的 Todo 转为 CloudTodo，ownerProfileId = myProfileId
        val myProfileCloudTodos = localTodos
            .filter { mapping.profileIdFor(it.personType) == mapping.myProfileId }
            .map { todo -> todo.toCloudTodo(mapping.myProfileId) }

        // 保留 partnerProfileId 的 CloudTodo（对方的 Todo 不动）
        val partnerProfileCloudTodos = existingCloudData?.todos.orEmpty()
            .filter { it.ownerProfileId == mapping.partnerProfileId }

        // 保留无法映射到本地 personType 的 CloudTodo（未知 profile 的数据保留）
        val preservedCloudTodos = existingCloudData?.todos.orEmpty().filter { todo ->
            mapping.personTypeFor(todo.ownerProfileId) == null
        }

        return preservedCloudTodos + partnerProfileCloudTodos + myProfileCloudTodos
    }

    /**
     * 将云端 Todo 应用到本地数据库
     * 与课程 applyCloudData 逻辑一致：按 ownerProfileId 映射 personType，删除本地不在云端的，upsert 云端数据
     */
    private suspend fun applyCloudTodos(cloudData: CloudData, mapping: ProfileMapping) {
        try {
            val existingTodos = todoDao.getAllTodosSync()
            val existingBySyncId = existingTodos.associateBy { it.syncId }

            // 按 ownerProfileId 筛选可映射的 CloudTodo，映射为 personType
            val selectedCloudTodos = cloudData.todos.mapNotNull { cloudTodo ->
                mapping.personTypeFor(cloudTodo.ownerProfileId)?.let { personType -> cloudTodo to personType }
            }
            val cloudSyncIds = selectedCloudTodos.map { it.first.syncId }.toSet()

            // 删除本地不在云端列表中的 Todo（按 syncId 匹配）
            for (todo in existingTodos) {
                if (!cloudSyncIds.contains(todo.syncId)) {
                    todoDao.deleteTodoById(todo.id)
                }
            }

            // 更新或插入云端 Todo 到本地数据库
            for ((cloudTodo, personType) in selectedCloudTodos) {
                val existing = existingBySyncId[cloudTodo.syncId]
                val todo = cloudTodo.toTodo(personType).copy(
                    id = existing?.id ?: 0,
                    syncId = cloudTodo.syncId
                )
                if (existing != null) {
                    todoDao.updateTodo(todo)
                } else {
                    todoDao.insertTodo(todo)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "applyCloudTodos failed", e)
        }
    }

    /**
     * 比较本地 Todo 与云端 Todo 是否有差异
     * 与课程 localDataDiffersFromCloud 逻辑一致
     */
    private fun todoDataDiffersFromCloud(
        localTodos: List<Todo>,
        cloudData: CloudData,
        mapping: ProfileMapping
    ): Boolean {
        val selectedCloudTodos = cloudData.todos.mapNotNull { cloudTodo ->
            mapping.personTypeFor(cloudTodo.ownerProfileId)?.let { personType -> cloudTodo to personType }
        }
        if (localTodos.size != selectedCloudTodos.size) return true

        val cloudTodoMap = selectedCloudTodos.associateBy { it.first.syncId }
        for (local in localTodos) {
            val cloud = cloudTodoMap[local.syncId] ?: return true
            if (!todoContentEquals(local, cloud.first, cloud.second)) return true
        }
        return false
    }

    /**
     * 检测 Todo 冲突
     * 与课程 detectConflicts 逻辑一致：遍历云端 Todo，查找本地有相同 syncId 但内容不同的
     */
    private suspend fun detectTodoConflicts(
        cloudData: CloudData,
        mapping: ProfileMapping
    ): List<ConflictItem> {
        val localTodos = todoDao.getAllTodosSync()
        val localMap = localTodos.associateBy { it.syncId }
        val selectedCloudTodos = cloudData.todos.mapNotNull { cloudTodo ->
            mapping.personTypeFor(cloudTodo.ownerProfileId)?.let { personType -> cloudTodo to personType }
        }
        val conflicts = mutableListOf<ConflictItem>()

        for ((cloud, personType) in selectedCloudTodos) {
            val local = localMap[cloud.syncId]
            if (local != null && !todoContentEquals(local, cloud, personType)) {
                conflicts.add(
                    ConflictItem(
                        courseName = local.title,
                        localVersion = null,
                        cloudVersion = null,
                        conflictType = ConflictType.BOTH_MODIFIED,
                        courseKey = cloud.syncId,
                        localTodoVersion = mapping.profileIdFor(local.personType)?.let { local.toCloudTodo(it) },
                        cloudTodoVersion = cloud
                    )
                )
            }
        }
        return conflicts
    }

    /**
     * 合并 Todo：以云端为基础，用本地 Todo 覆盖同 syncId 的云端 Todo，添加本地独有的 Todo
     * 与课程 mergeCourses 逻辑一致
     */
    private fun mergeTodos(
        localTodos: List<Todo>,
        cloudData: CloudData,
        mapping: ProfileMapping
    ): List<CloudTodo> {
        val merged = linkedMapOf<String, CloudTodo>()
        for (todo in cloudData.todos) {
            merged[todo.syncId] = todo
        }
        for (todo in localTodos) {
            mapping.profileIdFor(todo.personType)?.let { ownerProfileId ->
                merged[todo.syncId] = todo.toCloudTodo(ownerProfileId)
            }
        }
        return merged.values.toList()
    }

    /**
     * 首次同步智能合并 Todo
     * 与课程 smartMergeForFirstSync 逻辑一致，使用 contentMatchKey: title|date|startHour|startMinute|endHour|endMinute
     */
    private suspend fun smartMergeTodosForFirstSync(
        localTodos: List<Todo>,
        cloudData: CloudData,
        mapping: ProfileMapping
    ): List<CloudTodo> {
        // 将本地 Todo 按 personType 转为 CloudTodo
        val localCloudTodos = localTodos.mapNotNull { todo ->
            mapping.profileIdFor(todo.personType)?.let { ownerProfileId ->
                todo.toCloudTodo(ownerProfileId)
            }
        }

        val result = SmartMergeTodoHelper.smartMergeTodosLogic(
            localTodos = localCloudTodos,
            cloudTodos = cloudData.todos,
            myProfileId = mapping.myProfileId,
            partnerProfileId = mapping.partnerProfileId
        )

        // 更新本地数据库中匹配成功的 Todo 的 syncId
        // 构建 contentMatchKey → 新 syncId 的映射（仅匹配成功的）
        val localKeyToOriginalSyncId = localCloudTodos.associateBy {
            SmartMergeTodoHelper.todoContentMatchKey(it) to it.ownerProfileId
        }
        for (matchedSyncId in result.matchedCloudSyncIds) {
            val mergedTodo = result.mergedTodos.find { it.syncId == matchedSyncId } ?: continue
            val key = SmartMergeTodoHelper.todoContentMatchKey(mergedTodo) to mergedTodo.ownerProfileId
            val originalCloudTodo = localKeyToOriginalSyncId[key] ?: continue
            // 找到对应的本地 Todo，更新 syncId
            val localTodo = localTodos.find { it.syncId == originalCloudTodo.syncId }
            if (localTodo != null && localTodo.syncId != matchedSyncId) {
                todoDao.updateTodo(localTodo.copy(syncId = matchedSyncId))
            }
        }

        return result.mergedTodos
    }

    /**
     * 比较本地 Todo 与云端 CloudTodo 的内容是否一致
     */
    private fun todoContentEquals(local: Todo, cloud: CloudTodo, cloudPersonType: PersonType): Boolean {
        return SyncComparatorHelper.todoContentEquals(local, cloud, cloudPersonType)
    }

    /**
     * Todo 内容匹配键，用于首次同步智能合并
     */
    private fun todoContentMatchKey(todo: CloudTodo): String {
        return SmartMergeTodoHelper.todoContentMatchKey(todo)
    }

    // ========== 课程同步方法（原有） ==========

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
        settingsDataStore.setTotalPeriods(personType, settings.totalPeriods)
        settingsDataStore.setPeriodTimes(personType, settings.periodTimes)
    }

    private suspend fun getCloudSettings(personType: PersonType): CloudSettings {
        val startDate = settingsDataStore.getSemesterStartDate(personType).first()
        val totalWeeks = settingsDataStore.getTotalWeeks(personType).first()
        val currentWeek = settingsDataStore.calculateCurrentWeek(startDate, totalWeeks)
        return CloudSettings(
            semesterStartDate = startDate.toEpochDay(),
            totalWeeks = totalWeeks,
            currentWeek = currentWeek,
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
        return SyncComparatorHelper.courseContentEquals(local, cloud, cloudPersonType)
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

    // ========== JSON 序列化/反序列化 ==========

    private suspend fun buildCloudDataJson(
        config: SyncConfig,
        courses: List<CloudCourse>,
        todos: List<CloudTodo>,
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

        return buildCloudDataJson(
            config = config,
            courses = courses,
            todos = todos,
            todoTags = existingCloudData?.todoTags.orEmpty(),
            repeatRules = existingCloudData?.repeatRules.orEmpty(),
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
        todos: List<CloudTodo>,
        todoTags: List<CloudTodoTag>,
        repeatRules: List<CloudRepeatRule>,
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
            put("version", System.currentTimeMillis())
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
            // Todo 数据（schema v3 新增）
            put("todos", JSONArray().apply {
                for (todo in todos) {
                    put(JSONObject().apply {
                        put("syncId", todo.syncId)
                        put("ownerProfileId", todo.ownerProfileId)
                        put("title", todo.title)
                        put("description", todo.description)
                        put("date", todo.date)
                        put("startHour", todo.startHour)
                        put("startMinute", todo.startMinute)
                        put("endHour", todo.endHour)
                        put("endMinute", todo.endMinute)
                        put("priority", todo.priority)
                        put("status", todo.status)
                        put("tags", todo.tags)
                        put("linkedCourseSyncId", todo.linkedCourseSyncId ?: JSONObject.NULL)
                        put("repeatRuleId", todo.repeatRuleId ?: JSONObject.NULL)
                        put("completedAt", todo.completedAt ?: JSONObject.NULL)
                    })
                }
            })
            // Todo 标签（schema v3 新增）
            put("todoTags", JSONArray().apply {
                for (tag in todoTags) {
                    put(JSONObject().apply {
                        put("id", tag.id)
                        put("name", tag.name)
                        put("color", tag.color)
                        put("isPreset", tag.isPreset)
                    })
                }
            })
            // 重复规则（schema v3 新增）
            put("repeatRules", JSONArray().apply {
                for (rule in repeatRules) {
                    put(JSONObject().apply {
                        put("id", rule.id)
                        put("frequency", rule.frequency)
                        put("interval", rule.interval)
                        put("daysOfWeek", rule.daysOfWeek)
                        put("customDates", rule.customDates)
                        put("endDate", rule.endDate ?: JSONObject.NULL)
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

        // 解析 Todo 数据（schema v3 新增，向后兼容：缺失时使用空数组）
        val todosArray = json.optJSONArray("todos") ?: JSONArray()
        val todos = mutableListOf<CloudTodo>()
        for (i in 0 until todosArray.length()) {
            val todoJson = todosArray.getJSONObject(i)
            todos.add(
                CloudTodo(
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
                )
            )
        }

        // 解析 Todo 标签（schema v3 新增）
        val todoTagsArray = json.optJSONArray("todoTags") ?: JSONArray()
        val todoTags = mutableListOf<CloudTodoTag>()
        for (i in 0 until todoTagsArray.length()) {
            val tagJson = todoTagsArray.getJSONObject(i)
            todoTags.add(
                CloudTodoTag(
                    id = tagJson.optString("id", ""),
                    name = tagJson.optString("name", ""),
                    color = tagJson.optLong("color", 0),
                    isPreset = tagJson.optBoolean("isPreset", false)
                )
            )
        }

        // 解析重复规则（schema v3 新增）
        val repeatRulesArray = json.optJSONArray("repeatRules") ?: JSONArray()
        val repeatRules = mutableListOf<CloudRepeatRule>()
        for (i in 0 until repeatRulesArray.length()) {
            val ruleJson = repeatRulesArray.getJSONObject(i)
            repeatRules.add(
                CloudRepeatRule(
                    id = ruleJson.optString("id", ""),
                    frequency = ruleJson.optString("frequency", "DAILY"),
                    interval = ruleJson.optInt("interval", 1),
                    daysOfWeek = ruleJson.optString("daysOfWeek", ""),
                    customDates = ruleJson.optString("customDates", ""),
                    endDate = if (ruleJson.isNull("endDate")) null else ruleJson.optLong("endDate")
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
            version = json.optLong("version", 0L),
            lastModified = json.optString("lastModified", ""),
            lastModifiedBy = json.optString("lastModifiedBy", ""),
            courses = courses,
            settingsA = json.optJSONObject("settingsA")?.let { parseSettings(it) },
            settingsB = json.optJSONObject("settingsB")?.let { parseSettings(it) },
            personAName = personAName,
            personBName = personBName,
            schemaVersion = schemaVersion,
            profiles = profiles,
            profileSettings = profileSettings,
            todos = todos,
            todoTags = todoTags,
            repeatRules = repeatRules
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
        private const val CLOUD_SCHEMA_VERSION = 3
        private const val LEGACY_PERSON_A_PROFILE_ID = "legacy-person-a"
        private const val LEGACY_PERSON_B_PROFILE_ID = "legacy-person-b"
    }
}
