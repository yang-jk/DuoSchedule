package com.duoschedule.data.update

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import com.duoschedule.data.local.SettingsDataStore
import com.duoschedule.util.AppLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppUpdateChecker @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) {
    companion object {
        private const val TAG = "AppUpdateChecker"
        private const val UPDATE_JSON_URL =
            "https://cdn.jsdelivr.net/gh/yang-jk/duoschedule-update@update.json"
        private const val CONNECT_TIMEOUT_SECONDS = 10L
        private const val READ_TIMEOUT_SECONDS = 15L
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(CONNECT_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    suspend fun checkForUpdate(context: Context): Result<Pair<UpdateInfo, UpdateStatus>> {
        return try {
            val currentVersionCode = getCurrentVersionCode(context)
            AppLog.i(TAG, "当前版本号: $currentVersionCode")

            val updateInfo = fetchUpdateInfo()
            AppLog.i(TAG, "最新版本: ${updateInfo.latestVersion} (${updateInfo.latestVersionCode})")

            val status = determineUpdateStatus(currentVersionCode, updateInfo)
            AppLog.i(TAG, "更新状态: $status")

            when (status) {
                UpdateStatus.NO_UPDATE -> Result.success(Pair(updateInfo, status))
                UpdateStatus.FORCE_UPDATE -> Result.success(Pair(updateInfo, status))
                UpdateStatus.OPTIONAL_UPDATE -> {
                    val skippedVersionCode = settingsDataStore.skippedVersionCode.first()
                    if (currentVersionCode < updateInfo.latestVersionCode &&
                        skippedVersionCode == updateInfo.latestVersionCode
                    ) {
                        AppLog.i(TAG, "用户已跳过版本 ${updateInfo.latestVersionCode}")
                        Result.success(Pair(updateInfo, UpdateStatus.NO_UPDATE))
                    } else {
                        Result.success(Pair(updateInfo, status))
                    }
                }
            }
        } catch (e: UnknownHostException) {
            AppLog.e(TAG, "检查更新失败: 网络不可用，无法解析更新服务器地址")
            Result.failure(e)
        } catch (e: java.net.SocketTimeoutException) {
            AppLog.e(TAG, "检查更新失败: 连接超时")
            Result.failure(e)
        } catch (e: java.io.IOException) {
            AppLog.e(TAG, "检查更新失败: 网络错误 - ${e.message}")
            Result.failure(e)
        } catch (e: Exception) {
            AppLog.e(TAG, "检查更新失败: ${e.javaClass.simpleName} - ${e.message}")
            Result.failure(e)
        }
    }

    private suspend fun fetchUpdateInfo(): UpdateInfo = withContext(Dispatchers.IO) {
        val url = "$UPDATE_JSON_URL?t=${System.currentTimeMillis()}"
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("HTTP ${response.code}")
            }

            val body = response.body?.string()
                ?: throw Exception("响应体为空")

            parseUpdateInfo(body)
        }
    }

    private fun parseUpdateInfo(json: String): UpdateInfo {
        val cleaned = json.trimStart('\uFEFF')
        val obj = JSONObject(cleaned)
        return UpdateInfo(
            latestVersion = obj.getString("latestVersion"),
            latestVersionCode = obj.getInt("latestVersionCode"),
            minSupportedVersionCode = obj.optInt("minSupportedVersionCode", 1),
            downloadUrl = obj.getString("downloadUrl"),
            releaseNotes = obj.getString("releaseNotes"),
            forceUpdate = obj.optBoolean("forceUpdate", false)
        )
    }

    private fun determineUpdateStatus(
        currentVersionCode: Int,
        updateInfo: UpdateInfo
    ): UpdateStatus {
        return when {
            currentVersionCode < updateInfo.minSupportedVersionCode -> UpdateStatus.FORCE_UPDATE
            currentVersionCode < updateInfo.latestVersionCode -> {
                if (updateInfo.forceUpdate) UpdateStatus.FORCE_UPDATE
                else UpdateStatus.OPTIONAL_UPDATE
            }
            else -> UpdateStatus.NO_UPDATE
        }
    }

    private fun getCurrentVersionCode(context: Context): Int {
        return try {
            val packageInfo: PackageInfo = context.packageManager
                .getPackageInfo(context.packageName, 0)
            packageInfo.versionCode
        } catch (e: PackageManager.NameNotFoundException) {
            AppLog.e(TAG, "无法获取版本号: ${e.message}")
            -1
        }
    }

    fun getCurrentVersionName(context: Context): String {
        return try {
            val packageInfo: PackageInfo = context.packageManager
                .getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "未知"
        } catch (e: PackageManager.NameNotFoundException) {
            "未知"
        }
    }
}
