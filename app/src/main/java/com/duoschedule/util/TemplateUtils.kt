package com.duoschedule.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

object TemplateUtils {
    
    const val TEMPLATE_FILE_NAME = "course_template.csv"
    
    suspend fun getTemplateContent(context: Context): String = withContext(Dispatchers.IO) {
        context.assets.open(TEMPLATE_FILE_NAME).use { inputStream ->
            BufferedReader(InputStreamReader(inputStream, "UTF-8")).use { reader ->
                reader.readText()
            }
        }
    }
    
    suspend fun saveTemplateToUri(
        context: Context,
        uri: Uri
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val content = getTemplateContent(context)
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(0xEF)
                outputStream.write(0xBB)
                outputStream.write(0xBF)
                outputStream.write(content.toByteArray(Charsets.UTF_8))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun createShareIntent(context: Context, uri: Uri): Intent {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return Intent.createChooser(shareIntent, "分享 CSV 模板")
    }
}
