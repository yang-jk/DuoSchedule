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

    private val _syncCode = MutableStateFlow("")
    val syncCode: StateFlow<String> = _syncCode.asStateFlow()

    init {
        viewModelScope.launch {
            _syncCode.value = syncManager.getSyncCode().orEmpty()
        }
    }

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _conflictResult = MutableStateFlow<SyncResult.Conflict?>(null)
    val conflictResult: StateFlow<SyncResult.Conflict?> = _conflictResult.asStateFlow()

    fun createRoom(webDavUrl: String, username: String, password: String) {
        viewModelScope.launch {
            _isCreating.value = true
            val config = SyncConfig(
                webDavUrl = webDavUrl,
                username = username,
                password = password,
                roomId = SyncCodeGenerator.generateRoomId(),
                deviceId = SyncCodeGenerator.generateDeviceId()
            )
            val result = syncManager.createRoom(config)
            _isCreating.value = false
            if (result.isSuccess) {
                _syncCode.value = result.getOrThrow()
                _message.value = "房间创建成功"
            } else {
                _message.value = result.exceptionOrNull()?.message ?: "创建房间失败"
            }
        }
    }

    fun joinRoom(syncCodeInput: String) {
        viewModelScope.launch {
            _isJoining.value = true
            val result = syncManager.joinRoom(syncCodeInput.trim())
            _isJoining.value = false
            if (result.isSuccess) {
                _syncCode.value = syncManager.getSyncCode().orEmpty()
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
            _syncCode.value = ""
            _message.value = "已离开房间"
        }
    }

    fun copySyncCode() {
        val code = _syncCode.value
        if (code.isNotEmpty()) {
            val clipboard = application.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("同步码", code))
            _message.value = "同步码已复制"
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    fun isValidSyncCode(text: String): Boolean {
        return SyncCodeGenerator.isValidSyncCode(text.trim())
    }
}
