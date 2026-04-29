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

interface WidgetDependencies {
    val database: AppDatabase
    val settingsDataStore: SettingsDataStore
}

class ScheduleWidgetReceiverMIUI : AppWidgetProvider() {

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        Log.d("ScheduleWidgetMIUI", "Widget enabled")
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        Log.d("ScheduleWidgetMIUI", "Widget disabled")
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        Log.d("ScheduleWidgetMIUI", "onUpdate: widgetIds=${appWidgetIds.toList()}")
        
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("ScheduleWidgetMIUI", "onReceive: action=${intent.action}")
        
        when (intent.action) {
            ACTION_UPDATE_WIDGET,
            ACTION_MIUI_APPWIDGET_UPDATE,
            ACTION_MIUI_EXPOSURE,
            AppWidgetManager.ACTION_APPWIDGET_UPDATE -> {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = ComponentName(context, ScheduleWidgetReceiverMIUI::class.java)
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
        Log.d("ScheduleWidgetMIUI", "updateWidget: widgetId=$appWidgetId")
        
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
                val personAName = settingsDataStore.personAName.first()
                val personBName = settingsDataStore.personBName.first()
                
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
                
                val currentCourseA = personACourses.find { 
                    it.isOngoing(currentHour, currentMinute, currentWeekA) 
                }
                val currentCourseB = personBCourses.find { 
                    it.isOngoing(currentHour, currentMinute, currentWeekB) 
                }
                
                val views = RemoteViews(context.packageName, R.layout.schedule_widget)
                
                views.setTextViewText(R.id.widget_week, "第${currentWeekA}周")
                
                views.setTextViewText(R.id.person_a_label, personAName)
                views.setTextViewText(
                    R.id.person_a_course,
                    currentCourseA?.name ?: context.getString(R.string.no_class)
                )
                views.setTextViewText(
                    R.id.person_a_time,
                    if (currentCourseA != null) {
                        val remaining = currentCourseA.getRemainingMinutes(currentHour, currentMinute)
                        val periodText = if (currentCourseA.startPeriod == currentCourseA.endPeriod) {
                            "第${currentCourseA.startPeriod}节"
                        } else {
                            "第${currentCourseA.startPeriod}-${currentCourseA.endPeriod}节"
                        }
                        "$periodText · 剩余${remaining}分钟"
                    } else {
                        ""
                    }
                )
                
                views.setTextViewText(R.id.person_b_label, personBName)
                views.setTextViewText(
                    R.id.person_b_course,
                    currentCourseB?.name ?: context.getString(R.string.no_class)
                )
                views.setTextViewText(
                    R.id.person_b_time,
                    if (currentCourseB != null) {
                        val remaining = currentCourseB.getRemainingMinutes(currentHour, currentMinute)
                        val periodText = if (currentCourseB.startPeriod == currentCourseB.endPeriod) {
                            "第${currentCourseB.startPeriod}节"
                        } else {
                            "第${currentCourseB.startPeriod}-${currentCourseB.endPeriod}节"
                        }
                        "$periodText · 剩余${remaining}分钟"
                    } else {
                        ""
                    }
                )
                
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(android.R.id.background, pendingIntent)
                
                MiuiWidgetHelper.updateMiuiWidgetStatusBeforeUpdate(
                    appWidgetManager,
                    appWidgetId,
                    MiuiWidgetHelper.EVENT_CODE_COURSE_INFO
                )
                
                appWidgetManager.updateAppWidget(appWidgetId, views)
                Log.d("ScheduleWidgetMIUI", "Widget updated successfully: widgetId=$appWidgetId")
            } catch (e: Exception) {
                Log.e("ScheduleWidgetMIUI", "Failed to update widget", e)
                
                try {
                    val errorViews = RemoteViews(context.packageName, R.layout.schedule_widget)
                    errorViews.setTextViewText(R.id.widget_week, "")
                    errorViews.setTextViewText(R.id.person_a_label, "Ta")
                    errorViews.setTextViewText(R.id.person_a_course, "加载中...")
                    errorViews.setTextViewText(R.id.person_a_time, "")
                    errorViews.setTextViewText(R.id.person_b_label, "我")
                    errorViews.setTextViewText(R.id.person_b_course, "加载中...")
                    errorViews.setTextViewText(R.id.person_b_time, "")
                    
                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        0,
                        Intent(context, MainActivity::class.java),
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    errorViews.setOnClickPendingIntent(android.R.id.background, pendingIntent)
                    
                    appWidgetManager.updateAppWidget(appWidgetId, errorViews)
                } catch (e2: Exception) {
                    Log.e("ScheduleWidgetMIUI", "Failed to show error state", e2)
                }
            }
        }
    }

    companion object {
        const val ACTION_UPDATE_WIDGET = "com.duoschedule.action.UPDATE_WIDGET"
        const val ACTION_MIUI_APPWIDGET_UPDATE = "miui.appwidget.action.APPWIDGET_UPDATE"
        const val ACTION_MIUI_EXPOSURE = "miui.appwidget.action.EXPOSURE"

        fun updateAllWidgets(context: Context) {
            val intent = Intent(context, ScheduleWidgetReceiverMIUI::class.java).apply {
                action = ACTION_UPDATE_WIDGET
            }
            context.sendBroadcast(intent)
        }
    }
}
