package com.duoschedule.data.update

import android.content.Context
import com.duoschedule.util.AppLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApkDownloader @Inject constructor() {
    companion object {
        private const val TAG = "ApkDownloader"
        private const val APK_FILE_NAME = "update.apk"
        private const val CONNECT_TIMEOUT_SECONDS = 15L
        private const val READ_TIMEOUT_SECONDS = 30L
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    private var currentCall: okhttp3.Call? = null

    suspend fun downloadApk(
        context: Context,
        downloadUrl: String,
        onProgress: (percent: Int, downloadedBytes: Long, totalBytes: Long) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val apkDir = File(context.getExternalFilesDir("apk"), ".").apply {
                if (!exists()) mkdirs()
            }
            cleanupOldApk(apkDir)

            val apkFile = File(apkDir, APK_FILE_NAME)

            val request = Request.Builder().url(downloadUrl).build()
            val call = client.newCall(request)
            currentCall = call

            val response = call.execute()
            if (!response.isSuccessful) {
                throw Exception("下载失败: HTTP ${response.code}")
            }

            val body = response.body ?: throw Exception("下载响应体为空")
            val contentLength = body.contentLength()

            body.byteStream().use { input ->
                apkFile.outputStream().use { output ->
                    val buffer = ByteArray(8192)
                    var downloadedBytes = 0L
                    var lastReportPercent = -1

                    while (true) {
                        val read = input.read(buffer)
                        if (read == -1) break
                        output.write(buffer, 0, read)
                        downloadedBytes += read

                        if (contentLength > 0) {
                            val percent = (downloadedBytes * 100 / contentLength).toInt()
                            if (percent != lastReportPercent) {
                                lastReportPercent = percent
                                withContext(Dispatchers.Main) {
                                    onProgress(percent, downloadedBytes, contentLength)
                                }
                            }
                        }
                    }
                }
            }

            currentCall = null

            if (apkFile.length() == 0L) {
                apkFile.delete()
                throw Exception("下载的APK文件为空")
            }

            AppLog.i(TAG, "APK下载完成: ${apkFile.absolutePath}, 大小: ${apkFile.length()}")
            Result.success(apkFile)
        } catch (e: Exception) {
            currentCall = null
            AppLog.e(TAG, "APK下载失败: ${e.message}")
            Result.failure(e)
        }
    }

    fun cancelDownload() {
        currentCall?.cancel()
        currentCall = null
        AppLog.i(TAG, "下载已取消")
    }

    private fun cleanupOldApk(apkDir: File) {
        val oldFile = File(apkDir, APK_FILE_NAME)
        if (oldFile.exists()) {
            oldFile.delete()
            AppLog.i(TAG, "已清理旧APK文件")
        }
    }

    fun getDownloadedApk(context: Context): File? {
        val apkFile = File(context.getExternalFilesDir("apk"), APK_FILE_NAME)
        return if (apkFile.exists() && apkFile.length() > 0) apkFile else null
    }
}
