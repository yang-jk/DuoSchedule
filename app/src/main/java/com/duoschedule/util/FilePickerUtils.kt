package com.duoschedule.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build

object FilePickerUtils {
    
    private const val HYPEROS_VERSION_CODE = 816
    
    fun isFilePickerAvailable(context: Context, intent: Intent): Boolean {
        return try {
            val resolveInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.resolveActivity(
                    intent,
                    PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.resolveActivity(intent, PackageManager.MATCH_ALL)
            }
            resolveInfo != null
        } catch (e: Exception) {
            true
        }
    }

    fun isFilePickerSupported(): Boolean = true
    
    private fun getSystemProperty(key: String): String? {
        return try {
            val clazz = Class.forName("android.os.SystemProperties")
            val method = clazz.getMethod("get", String::class.java)
            method.invoke(null, key) as? String
        } catch (e: Exception) {
            null
        }
    }
    
    private fun getSystemPropertyInt(key: String): Int {
        return try {
            val value = getSystemProperty(key)
            value?.filter { it.isDigit() }?.toIntOrNull() ?: 0
        } catch (e: Exception) {
            0
        }
    }

    fun isMiuiDevice(): Boolean {
        val version = getSystemProperty("ro.miui.ui.version.name")
        return !version.isNullOrBlank()
    }
    
    fun getMiuiVersionName(): String? {
        return getSystemProperty("ro.miui.ui.version.name")
    }
    
    fun getMiuiVersionCode(): Int {
        val code = getSystemPropertyInt("ro.miui.ui.version.code")
        if (code > 0) return code
        
        val name = getSystemProperty("ro.miui.ui.version.name")
        if (!name.isNullOrBlank()) {
            val digits = name.filter { it.isDigit() }
            return digits.toIntOrNull() ?: 0
        }
        return 0
    }
    
    fun isHyperOS(): Boolean {
        return getMiuiVersionCode() >= HYPEROS_VERSION_CODE
    }
    
    fun isHyperOS3OrAbove(): Boolean {
        return isMiuiDevice() && isHyperOS()
    }
    
    fun supportsMiuiFilePicker(): Boolean {
        return isMiuiDevice() && isHyperOS()
    }

    fun createOpenDocumentIntent(
        mimeTypes: Array<String>,
        allowMultiple: Boolean = false
    ): Intent {
        return Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, allowMultiple)
        }
    }

    fun createOpenCsvIntent(): Intent {
        return Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("text/csv", "text/comma-separated-values", "text/plain", "application/csv"))
        }
    }

    fun createOpenJsonIntent(): Intent {
        return Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/json", "text/plain"))
        }
    }

    fun createOpenDocumentIntent(mimeType: String): Intent {
        return Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = mimeType
        }
    }

    fun createCreateDocumentIntent(mimeType: String, fileName: String): Intent {
        return Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = mimeType
            putExtra(Intent.EXTRA_TITLE, fileName)
        }
    }
    
    fun getDeviceInfo(): DeviceInfo {
        val versionName = getMiuiVersionName()
        val versionCode = getMiuiVersionCode()
        val isMiui = !versionName.isNullOrBlank()
        val isHyperOS = isMiui && versionCode >= HYPEROS_VERSION_CODE
        
        return DeviceInfo(
            isMiuiDevice = isMiui,
            miuiVersion = versionName,
            miuiVersionCode = versionCode,
            isHyperOS = isHyperOS,
            androidVersion = Build.VERSION.SDK_INT,
            supportsMiuiFilePicker = isHyperOS
        )
    }
    
    data class DeviceInfo(
        val isMiuiDevice: Boolean,
        val miuiVersion: String?,
        val miuiVersionCode: Int,
        val isHyperOS: Boolean,
        val androidVersion: Int,
        val supportsMiuiFilePicker: Boolean
    )
}
