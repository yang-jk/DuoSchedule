package com.duoschedule.data.sync

import android.util.Log
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebDavClient @Inject constructor() {

    private val TAG = "WebDavClient"

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val xmlMediaType = "application/xml; charset=utf-8".toMediaType()

    private val propfindBody = """<?xml version="1.0" encoding="utf-8"?>
        |<propfind xmlns="DAV:">
        |<prop>
        |<resourcetype/>
        |</prop>
        |</propfind>""".trimMargin().toRequestBody(xmlMediaType)

    fun testConnection(config: SyncConfig): Result<Unit> {
        return try {
            val request = Request.Builder()
                .url(config.webDavUrl.trimEnd('/'))
                .header("Authorization", Credentials.basic(config.username, config.password))
                .header("Depth", "0")
                .method("PROPFIND", propfindBody)
                .build()
            val response = client.newCall(request).execute()
            response.close()
            if (response.isSuccessful || response.code == 207) {
                Result.success(Unit)
            } else if (response.code == 401) {
                Result.failure(IOException("认证失败，请检查账号密码"))
            } else if (response.code == 403) {
                Result.failure(IOException("无访问权限，请检查账号是否对该目录有读写权限"))
            } else if (response.code == 404) {
                Result.failure(IOException("路径不存在，请检查 WebDAV 地址"))
            } else {
                Result.failure(IOException("连接失败: HTTP ${response.code}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "testConnection failed", e)
            Result.failure(IOException("网络连接失败: ${e.message}"))
        }
    }

    fun ensureDirectory(config: SyncConfig, path: String): Result<Unit> {
        val fullPath = buildPath(config.webDavUrl, path)
        val request = Request.Builder()
            .url(fullPath)
            .header("Authorization", Credentials.basic(config.username, config.password))
            .method("MKCOL", null)
            .build()
        return try {
            val response = client.newCall(request).execute()
            response.close()
            if (response.isSuccessful || response.code == 405) {
                Result.success(Unit)
            } else {
                Result.failure(IOException("创建目录失败: HTTP ${response.code}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "ensureDirectory failed for $path", e)
            Result.failure(IOException("创建目录失败: ${e.message}"))
        }
    }

    fun uploadJson(config: SyncConfig, path: String, json: JSONObject): Result<Unit> {
        val fullPath = buildPath(config.webDavUrl, path)
        val body = json.toString().toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url(fullPath)
            .header("Authorization", Credentials.basic(config.username, config.password))
            .put(body)
            .build()
        return try {
            val response = client.newCall(request).execute()
            response.close()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(IOException("上传失败: HTTP ${response.code}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "uploadJson failed for $path", e)
            Result.failure(IOException("上传失败: ${e.message}"))
        }
    }

    fun downloadJson(config: SyncConfig, path: String): Result<JSONObject> {
        val fullPath = buildPath(config.webDavUrl, path)
        val request = buildGetRequest(config, fullPath)
        return try {
            val response = client.newCall(request).execute()
            try {
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (body != null) {
                        Result.success(JSONObject(body))
                    } else {
                        Result.failure(IOException("下载失败: 响应为空"))
                    }
                } else if (response.code == 404) {
                    Result.failure(IOException("NOT_FOUND"))
                } else {
                    Result.failure(IOException("下载失败: HTTP ${response.code}"))
                }
            } finally {
                response.close()
            }
        } catch (e: Exception) {
            Log.e(TAG, "downloadJson failed for $path", e)
            Result.failure(IOException("下载失败: ${e.message}"))
        }
    }

    fun deleteFile(config: SyncConfig, path: String): Result<Unit> {
        val fullPath = buildPath(config.webDavUrl, path)
        val request = Request.Builder()
            .url(fullPath)
            .header("Authorization", Credentials.basic(config.username, config.password))
            .delete()
            .build()
        return try {
            val response = client.newCall(request).execute()
            response.close()
            if (response.isSuccessful || response.code == 404) {
                Result.success(Unit)
            } else {
                Result.failure(IOException("删除失败: HTTP ${response.code}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "deleteFile failed for $path", e)
            Result.failure(IOException("删除失败: ${e.message}"))
        }
    }

    fun fileExists(config: SyncConfig, path: String): Result<Boolean> {
        val fullPath = buildPath(config.webDavUrl, path)
        val request = Request.Builder()
            .url(fullPath)
            .header("Authorization", Credentials.basic(config.username, config.password))
            .head()
            .build()
        return try {
            val response = client.newCall(request).execute()
            response.close()
            Result.success(response.isSuccessful)
        } catch (e: Exception) {
            Result.failure(IOException("检查文件失败: ${e.message}"))
        }
    }

    private fun buildGetRequest(config: SyncConfig, url: String): Request {
        return Request.Builder()
            .url(url)
            .header("Authorization", Credentials.basic(config.username, config.password))
            .get()
            .build()
    }

    private fun buildPath(baseUrl: String, path: String): String {
        val base = baseUrl.trimEnd('/')
        val cleanPath = path.trimStart('/')
        return "$base/$cleanPath"
    }

    fun getRoomPath(roomId: String): String = "duoschedule/sync/$roomId/"

    fun getDataPath(roomId: String): String = "duoschedule/sync/$roomId/data.json"

    fun getMetaPath(roomId: String): String = "duoschedule/sync/$roomId/meta.json"
}
