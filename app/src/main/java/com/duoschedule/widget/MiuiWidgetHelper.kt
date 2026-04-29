package com.duoschedule.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log

object MiuiWidgetHelper {
    
    private const val TAG = "MiuiWidgetHelper"
    
    private const val MIUI_WIDGET_AUTHORITY = "com.miui.personalassistant.widget.external"
    
    private const val KEY_MIUI_WIDGET_SUPPORTED = "isMiuiWidgetSupported"
    private const val KEY_MIUI_WIDGET_DETAIL_SUPPORTED = "isMiuiWidgetDetailPageSupported"
    
    const val KEY_MIUI_WIDGET_EVENT_CODE = "miuiWidgetEventCode"
    const val KEY_MIUI_WIDGET_TIMESTAMP = "miuiWidgetTimestamp"
    
    const val EVENT_CODE_COURSE_INFO = "info1"
    const val EVENT_CODE_FREE_TIME = "state1"
    const val EVENT_CODE_OTHER = "other"
    
    @WorkerThread
    fun isMiuiWidgetSupported(context: Context): Boolean {
        val uri = Uri.parse("content://$MIUI_WIDGET_AUTHORITY")
        return try {
            val bundle = context.contentResolver.call(uri, "isMiuiWidgetSupported", null, null)
            bundle?.getBoolean(KEY_MIUI_WIDGET_SUPPORTED, false) ?: false
        } catch (e: Exception) {
            Log.w(TAG, "Failed to check MIUI Widget support", e)
            false
        }
    }
    
    @WorkerThread
    fun isMiuiWidgetDetailPageSupported(context: Context): Boolean {
        val uri = Uri.parse("content://$MIUI_WIDGET_AUTHORITY")
        return try {
            val bundle = context.contentResolver.call(uri, "isMiuiWidgetDetailPageSupported", null, null)
            bundle?.getBoolean(KEY_MIUI_WIDGET_DETAIL_SUPPORTED, false) ?: false
        } catch (e: Exception) {
            Log.w(TAG, "Failed to check MIUI Widget detail page support", e)
            false
        }
    }
    
    fun updateMiuiWidgetStatus(
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        eventCode: String
    ) {
        try {
            val options = Bundle().apply {
                putString(KEY_MIUI_WIDGET_EVENT_CODE, eventCode)
                putString(KEY_MIUI_WIDGET_TIMESTAMP, System.currentTimeMillis().toString())
            }
            appWidgetManager.updateAppWidgetOptions(appWidgetId, options)
            Log.d(TAG, "Updated MIUI Widget status: widgetId=$appWidgetId, eventCode=$eventCode")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update MIUI Widget status", e)
        }
    }
    
    fun updateMiuiWidgetStatusBeforeUpdate(
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        eventCode: String
    ) {
        val options = Bundle().apply {
            putString(KEY_MIUI_WIDGET_EVENT_CODE, eventCode)
            putString(KEY_MIUI_WIDGET_TIMESTAMP, System.currentTimeMillis().toString())
        }
        appWidgetManager.updateAppWidgetOptions(appWidgetId, options)
    }
}

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
private annotation class WorkerThread
