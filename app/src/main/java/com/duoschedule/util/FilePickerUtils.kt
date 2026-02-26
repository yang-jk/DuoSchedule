package com.duoschedule.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build

object FilePickerUtils {
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

    fun isFilePickerSupported(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
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
}
