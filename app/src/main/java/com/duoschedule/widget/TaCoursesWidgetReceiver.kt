package com.duoschedule.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
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

class TaCoursesWidgetReceiver : AppWidgetProvider() {

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
        Log.d("TaCoursesWidget", "onReceive: action=${intent.action}")
        
        when (intent.action) {
            ACTION_UPDATE_WIDGET,
            ACTION_MIUI_APPWIDGET_UPDATE,
            ACTION_MIUI_EXPOSURE,
            AppWidgetManager.ACTION_APPWIDGET_UPDATE -> {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = ComponentName(context, TaCoursesWidgetReceiver::class.java)
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
                
                val personType = PersonType.PERSON_A
                val personColor = R.color.person_a_color
                
                val currentWeek = settingsDataStore.getCurrentWeek(personType).first()
                val personName = settingsDataStore.personAName.first()
                
                val dayOfWeek = LocalDate.now().dayOfWeek.value
                val currentTime = LocalTime.now()
                val currentHour = currentTime.hour
                val currentMinute = currentTime.minute
                
                val allCourses = courseDao.getCoursesByDay(dayOfWeek).first()
                
                val courses = allCourses.filter { 
                    it.personType == personType && it.isInWeek(currentWeek) 
                }.sortedBy { it.startHour * 60 + it.startMinute }
                
                val views = RemoteViews(context.packageName, R.layout.widget_today_courses)
                
                views.setTextViewText(R.id.widget_title, "${personName}的今日课程")
                views.setTextViewText(R.id.widget_week, "第${currentWeek}周")
                views.setInt(R.id.person_indicator, "setBackgroundResource", personColor)
                
                if (courses.isEmpty()) {
                    views.setViewVisibility(R.id.courses_container, View.GONE)
                    views.setViewVisibility(R.id.empty_text, View.VISIBLE)
                    views.setTextViewText(R.id.empty_text, context.getString(R.string.no_courses))
                } else {
                    views.setViewVisibility(R.id.courses_container, View.VISIBLE)
                    views.setViewVisibility(R.id.empty_text, View.GONE)
                    
                    views.removeAllViews(R.id.courses_container)
                    
                    val maxCourses = minOf(courses.size, 4)
                    for (i in 0 until maxCourses) {
                        val course = courses[i]
                        val hasEnded = course.hasEnded(currentHour, currentMinute)
                        
                        val courseView = RemoteViews(context.packageName, R.layout.widget_course_item)
                        
                        val periodText = if (course.startPeriod == course.endPeriod) {
                            "第${course.startPeriod}节"
                        } else {
                            "第${course.startPeriod}-${course.endPeriod}节"
                        }
                        
                        courseView.setTextViewText(R.id.course_period, periodText)
                        courseView.setTextViewText(R.id.course_name, course.name)
                        courseView.setTextViewText(R.id.course_location, course.location.ifEmpty { "--" })
                        
                        if (hasEnded) {
                            courseView.setInt(R.id.course_period, "setTextColor", R.color.course_ended)
                            courseView.setInt(R.id.course_name, "setTextColor", R.color.course_ended)
                            courseView.setInt(R.id.course_location, "setTextColor", R.color.course_ended)
                        } else {
                            courseView.setInt(R.id.course_period, "setTextColor", personColor)
                        }
                        
                        views.addView(R.id.courses_container, courseView)
                    }
                }
                
                val intent = Intent(context, MainActivity::class.java).apply {
                    putExtra("target_route", "schedule_a")
                }
                
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    WIDGET_REQUEST_CODE,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(android.R.id.background, pendingIntent)
                
                MiuiWidgetHelper.updateMiuiWidgetStatusBeforeUpdate(
                    appWidgetManager,
                    appWidgetId,
                    MiuiWidgetHelper.EVENT_CODE_COURSE_INFO
                )
                
                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (e: Exception) {
                Log.e("TaCoursesWidget", "Failed to update widget", e)
            }
        }
    }

    companion object {
        const val ACTION_UPDATE_WIDGET = "com.duoschedule.action.UPDATE_TA_COURSES_WIDGET"
        const val ACTION_MIUI_APPWIDGET_UPDATE = "miui.appwidget.action.APPWIDGET_UPDATE"
        const val ACTION_MIUI_EXPOSURE = "miui.appwidget.action.EXPOSURE"
        const val WIDGET_REQUEST_CODE = 2002

        fun updateAllWidgets(context: Context) {
            val intent = Intent(context, TaCoursesWidgetReceiver::class.java).apply {
                action = ACTION_UPDATE_WIDGET
            }
            context.sendBroadcast(intent)
        }
    }
}
