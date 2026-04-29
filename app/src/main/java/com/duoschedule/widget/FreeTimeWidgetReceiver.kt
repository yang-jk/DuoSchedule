package com.duoschedule.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import com.duoschedule.MainActivity
import com.duoschedule.R
import com.duoschedule.data.local.AppDatabase
import com.duoschedule.data.local.SettingsDataStore
import com.duoschedule.data.model.PersonType
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

class FreeTimeWidgetReceiver : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("FreeTimeWidget", "onReceive: action=${intent.action}")
        
        when (intent.action) {
            ACTION_UPDATE_WIDGET,
            ACTION_MIUI_APPWIDGET_UPDATE,
            ACTION_MIUI_EXPOSURE,
            AppWidgetManager.ACTION_APPWIDGET_UPDATE -> {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = ComponentName(context, FreeTimeWidgetReceiver::class.java)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
                
                for (appWidgetId in appWidgetIds) {
                    updateWidget(context, appWidgetManager, appWidgetId)
                }
            }
            else -> super.onReceive(context, intent)
        }
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val appContext = context.applicationContext
                val dependencies = EntryPointAccessors.fromApplication(
                    appContext,
                    WidgetDependencies::class.java
                )
                
                val courseDao = dependencies.database.courseDao()
                val settingsDataStore = dependencies.settingsDataStore
                
                val currentWeekA = settingsDataStore.getCurrentWeek(PersonType.PERSON_A).first()
                val currentWeekB = settingsDataStore.getCurrentWeek(PersonType.PERSON_B).first()
                
                val dayOfWeek = LocalDate.now().dayOfWeek.value
                val currentTime = LocalTime.now()
                val currentHour = currentTime.hour
                val currentMinute = currentTime.minute
                
                val allCourses = courseDao.getCoursesByDay(dayOfWeek).first()
                
                val personACourses = allCourses.filter { 
                    it.personType == PersonType.PERSON_A && it.isInWeek(currentWeekA) 
                }
                val personBCourses = allCourses.filter { 
                    it.personType == PersonType.PERSON_B && it.isInWeek(currentWeekB) 
                }
                
                val freeTimeSlots = calculateFreeTimeSlots(
                    personACourses, personBCourses, currentHour, currentMinute
                )
                
                val views = RemoteViews(context.packageName, R.layout.widget_free_time)
                
                if (freeTimeSlots.isNotEmpty()) {
                    val nearestSlot = freeTimeSlots.first()
                    views.setTextViewText(R.id.free_time_text, nearestSlot)
                    views.setTextViewText(R.id.free_time_label, "空闲")
                } else {
                    views.setTextViewText(R.id.free_time_text, context.getString(R.string.no_free_time))
                    views.setTextViewText(R.id.free_time_label, "")
                }
                
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    WIDGET_REQUEST_CODE,
                    Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(android.R.id.background, pendingIntent)
                
                MiuiWidgetHelper.updateMiuiWidgetStatusBeforeUpdate(
                    appWidgetManager,
                    appWidgetId,
                    MiuiWidgetHelper.EVENT_CODE_FREE_TIME
                )
                
                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (e: Exception) {
                Log.e("FreeTimeWidget", "Failed to update widget", e)
            }
        }
    }

    private fun calculateFreeTimeSlots(
        personACourses: List<com.duoschedule.data.model.Course>,
        personBCourses: List<com.duoschedule.data.model.Course>,
        currentHour: Int,
        currentMinute: Int
    ): List<String> {
        val freeSlots = mutableListOf<String>()
        val currentMinutes = currentHour * 60 + currentMinute
        
        val allPeriods = listOf(
            Pair(8 * 60, 9 * 60 + 40),
            Pair(10 * 60, 11 * 60 + 40),
            Pair(14 * 60, 15 * 60 + 40),
            Pair(16 * 60, 17 * 60 + 40),
            Pair(19 * 60, 20 * 60 + 40)
        )
        
        for ((start, end) in allPeriods) {
            if (start < currentMinutes) continue
            
            val aHasCourse = personACourses.any { course ->
                val courseStart = course.startHour * 60 + course.startMinute
                val courseEnd = course.endHour * 60 + course.endMinute
                start < courseEnd && end > courseStart
            }
            
            val bHasCourse = personBCourses.any { course ->
                val courseStart = course.startHour * 60 + course.startMinute
                val courseEnd = course.endHour * 60 + course.endMinute
                start < courseEnd && end > courseStart
            }
            
            if (!aHasCourse && !bHasCourse) {
                val startHour = start / 60
                val startMin = start % 60
                val endHour = end / 60
                val endMin = end % 60
                freeSlots.add(String.format("%02d:%02d-%02d:%02d", startHour, startMin, endHour, endMin))
            }
        }
        
        return freeSlots
    }

    companion object {
        const val ACTION_UPDATE_WIDGET = "com.duoschedule.action.UPDATE_FREE_TIME_WIDGET"
        const val ACTION_MIUI_APPWIDGET_UPDATE = "miui.appwidget.action.APPWIDGET_UPDATE"
        const val ACTION_MIUI_EXPOSURE = "miui.appwidget.action.EXPOSURE"
        const val WIDGET_REQUEST_CODE = 1001

        fun updateAllWidgets(context: Context) {
            val intent = Intent(context, FreeTimeWidgetReceiver::class.java).apply {
                action = ACTION_UPDATE_WIDGET
            }
            context.sendBroadcast(intent)
        }
    }
}
