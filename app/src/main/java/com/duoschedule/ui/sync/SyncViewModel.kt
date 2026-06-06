package com.duoschedule.ui.sync

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duoschedule.data.sync.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SyncViewModel @Inject constructor(
    private val syncManager: SyncManager,
    private val syncPreferences: SyncPreferences,
    private val application: Application
) : ViewModel() {

    val syncStatus: StateFlow<SyncStatus> = syncManager.syncStatus
        .stateIn(viewModelScope, SharingStarted.Lazily, SyncStatus())

    val syncEnabled: StateFlow<Boolean> = syncPreferences.syncEnabled
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val syncConfig: StateFlow<SyncConfig?> = syncPreferences.syncConfig
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val lastSyncTime: StateFlow<Long> = syncPreferences.lastSyncTime
        .stateIn(viewModelScope, SharingStarted.Lazily, 0L)

    private val _isCreating = MutableStateFlow(false)
    val isCreating: StateFlow<Boolean> = _isCreating.asStateFlow()

    private val _isJoining = MutableStateFlow(false)
    val isJoining: StateFlow<Boolean> = _isJoining.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _roomCode = MutableStateFlow("")
    val roomCode: StateFlow<String> = _roomCode.asStateFlow()

    private val _pendingJoinInfo = MutableStateFlow<JoinRoomInfo?>(null)
    val pendingJoinInfo: StateFlow<JoinRoomInfo?> = _pendingJoinInfo.asStateFlow()

    init {
        viewModelScope.launch {
            _roomCode.value = syncManager.getRoomCode().orEmpty()
        }
    }

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _conflictResult = MutableStateFlow<SyncResult.Conflict?>(null)
    val conflictResult: StateFlow<SyncResult.Conflict?> = _conflictResult.asStateFlow()

    fun createRoom(username: String, password: String) {
        viewModelScope.launch {
            _isCreating.value = true
            val config = SyncConfig(
                webDavUrl = "https://dav.jianguoyun.com/dav/",
                username = username,
                password = password,
                roomId = SyncCodeGenerator.generateRoomId(),
                deviceId = SyncCodeGenerator.generateDeviceId()
            )
            val result = syncManager.createRoom(config)
            _isCreating.value = false
            if (result.isSuccess) {
                _roomCode.value = result.getOrThrow()
                _message.value = "房间创建成功"
            } else {
                _message.value = result.exceptionOrNull()?.message ?: "创建房间失败"
            }
        }
    }

    fun joinRoom(roomCode: String) {
        viewModelScope.launch {
            _isJoining.value = true
            val result = syncManager.joinRoom(roomCode.trim())
            _isJoining.value = false
            if (result.isSuccess) {
                _pendingJoinInfo.value = result.getOrThrow()
            } else {
                _message.value = result.exceptionOrNull()?.message ?: "加入房间失败"
            }
        }
    }

    fun confirmJoinRoom(selectedProfileId: String) {
        val joinInfo = _pendingJoinInfo.value ?: return
        viewModelScope.launch {
            _isJoining.value = true
            val result = syncManager.joinRoomWithRoleSelection(joinInfo, selectedProfileId)
            _isJoining.value = false
            _pendingJoinInfo.value = null
            if (result.isSuccess) {
                _roomCode.value = joinInfo.roomCode
                _message.value = "加入房间成功"
            } else {
                _message.value = result.exceptionOrNull()?.message ?: "加入房间失败"
            }
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            _isSyncing.value = true
            val result = syncManager.sync()
            _isSyncing.value = false
            when (result) {
                is SyncResult.Success -> _message.value = "同步成功"
                is SyncResult.NoChanges -> _message.value = "已是最新"
                is SyncResult.Conflict -> {
                    _conflictResult.value = result
                    _message.value = "检测到冲突"
                }
                is SyncResult.Error -> _message.value = result.message
                is SyncResult.NotConfigured -> _message.value = "未配置同步"
            }
        }
    }

    fun resolveConflicts(resolution: ConflictResolution) {
        viewModelScope.launch {
            val result = syncManager.resolveConflicts(resolution)
            _conflictResult.value = null
            when (result) {
                is SyncResult.Success -> _message.value = "冲突已解决"
                is SyncResult.Error -> _message.value = result.message
                else -> {}
            }
        }
    }

    fun dismissConflict() {
        _conflictResult.value = null
    }

    fun leaveRoom() {
        viewModelScope.launch {
            syncManager.leaveRoom()
            _roomCode.value = ""
            _message.value = "已离开房间"
        }
    }

    fun copyRoomCode() {
        val code = _roomCode.value
        if (code.isNotEmpty()) {
            val clipboard = application.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("房间码", code))
            _message.value = "房间码已复制"
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
