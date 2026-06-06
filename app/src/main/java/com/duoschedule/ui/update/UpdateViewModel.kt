package com.duoschedule.ui.update

import android.content.Context
import android.content.pm.PackageInfo
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duoschedule.data.local.SettingsDataStore
import com.duoschedule.data.update.ApkDownloader
import com.duoschedule.data.update.ApkInstaller
import com.duoschedule.data.update.AppUpdateChecker
import com.duoschedule.data.update.UpdateInfo
import com.duoschedule.data.update.UpdateStatus
import com.duoschedule.util.AppLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

sealed class UpdateUiState {
    data object Idle : UpdateUiState()
    data object Checking : UpdateUiState()
    data class UpdateAvailable(val info: UpdateInfo, val isForceUpdate: Boolean) : UpdateUiState()
    data class Downloading(val percent: Int, val downloadedBytes: Long, val totalBytes: Long) : UpdateUiState()
    data class ReadyToInstall(val info: UpdateInfo, val isForceUpdate: Boolean) : UpdateUiState()
    data object NoUpdate : UpdateUiState()
    data class Error(val message: String) : UpdateUiState()
}

@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val updateChecker: AppUpdateChecker,
    private val apkDownloader: ApkDownloader,
    private val apkInstaller: ApkInstaller,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    companion object {
        private const val TAG = "UpdateViewModel"
    }

    private val _uiState = MutableStateFlow<UpdateUiState>(UpdateUiState.Idle)
    val uiState: StateFlow<UpdateUiState> = _uiState

    private var currentUpdateInfo: UpdateInfo? = null
    private var isForceUpdate = false

    fun checkForUpdate(context: Context) {
        viewModelScope.launch {
            _uiState.value = UpdateUiState.Checking
            val result = updateChecker.checkForUpdate(context)
            result.fold(
                onSuccess = { (info, status) ->
                    currentUpdateInfo = info
                    isForceUpdate = status == UpdateStatus.FORCE_UPDATE
                    when (status) {
                        UpdateStatus.NO_UPDATE -> {
                            _uiState.value = UpdateUiState.NoUpdate
                        }
                        UpdateStatus.OPTIONAL_UPDATE -> {
                            val downloadedApk = apkDownloader.getDownloadedApk(context)
                            if (downloadedApk != null && isApkVersionMatch(context, downloadedApk, info.latestVersionCode)) {
                                _uiState.value = UpdateUiState.ReadyToInstall(info, false)
                            } else {
                                if (downloadedApk != null) {
                                    downloadedApk.delete()
                                    AppLog.i(TAG, "已删除版本不匹配的缓存APK")
                                }
                                _uiState.value = UpdateUiState.UpdateAvailable(info, false)
                            }
                        }
                        UpdateStatus.FORCE_UPDATE -> {
                            val downloadedApk = apkDownloader.getDownloadedApk(context)
                            if (downloadedApk != null && isApkVersionMatch(context, downloadedApk, info.latestVersionCode)) {
                                _uiState.value = UpdateUiState.ReadyToInstall(info, true)
                            } else {
                                if (downloadedApk != null) {
                                    downloadedApk.delete()
                                    AppLog.i(TAG, "已删除版本不匹配的缓存APK")
                                }
                                _uiState.value = UpdateUiState.UpdateAvailable(info, true)
                            }
                        }
                    }
                },
                onFailure = { e ->
                    _uiState.value = UpdateUiState.Error(e.message ?: "检查更新失败")
                }
            )
        }
    }

    fun startDownload(context: Context) {
        val info = currentUpdateInfo ?: return
        viewModelScope.launch {
            _uiState.value = UpdateUiState.Downloading(0, 0, 0)
            val result = apkDownloader.downloadApk(
                context = context,
                downloadUrl = info.downloadUrl,
                onProgress = { percent, downloaded, total ->
                    _uiState.value = UpdateUiState.Downloading(percent, downloaded, total)
                }
            )
            result.fold(
                onSuccess = {
                    _uiState.value = UpdateUiState.ReadyToInstall(info, isForceUpdate)
                },
                onFailure = { e ->
                    _uiState.value = UpdateUiState.Error(e.message ?: "下载失败")
                }
            )
        }
    }

    fun installApk(context: Context) {
        val apkFile = apkDownloader.getDownloadedApk(context)
        if (apkFile != null) {
            if (apkInstaller.canRequestInstall(context)) {
                val result = apkInstaller.installApk(context, apkFile)
                result.fold(
                    onSuccess = {
                        AppLog.i(TAG, "安装意图已发送")
                    },
                    onFailure = { e ->
                        _uiState.value = UpdateUiState.Error(e.message ?: "安装失败")
                    }
                )
            } else {
                apkInstaller.openInstallPermissionSettings(context)
            }
        } else {
            AppLog.e(TAG, "APK文件不存在，无法安装")
            _uiState.value = UpdateUiState.Error("APK文件不存在，请重新下载")
        }
    }

    fun canRequestInstall(context: Context): Boolean {
        return apkInstaller.canRequestInstall(context)
    }

    fun skipVersion() {
        val info = currentUpdateInfo ?: return
        viewModelScope.launch {
            settingsDataStore.setSkippedVersionCode(info.latestVersionCode)
            _uiState.value = UpdateUiState.Idle
        }
    }

    fun cancelDownload() {
        apkDownloader.cancelDownload()
        val info = currentUpdateInfo
        if (info != null) {
            _uiState.value = UpdateUiState.UpdateAvailable(info, isForceUpdate)
        } else {
            _uiState.value = UpdateUiState.Idle
        }
    }

    private fun isApkVersionMatch(context: Context, apkFile: File, expectedVersionCode: Int): Boolean {
        return try {
            val packageInfo: PackageInfo = context.packageManager
                .getPackageArchiveInfo(apkFile.absolutePath, 0)
                ?: return false
            val apkVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode
            }
            AppLog.i(TAG, "缓存APK版本号: $apkVersionCode, 期望版本号: $expectedVersionCode")
            apkVersionCode == expectedVersionCode
        } catch (e: Exception) {
            AppLog.e(TAG, "读取缓存APK版本号失败: ${e.message}")
            false
        }
    }
}
