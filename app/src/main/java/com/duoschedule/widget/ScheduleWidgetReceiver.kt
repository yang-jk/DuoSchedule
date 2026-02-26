package com.duoschedule.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import androidx.hilt.work.HiltWorkerFactory
import com.duoschedule.MainActivity
import com.duoschedule.R
import com.duoschedule.data.local.AppDatabase
import com.duoschedule.data.local.SettingsDataStore
import com.duoschedule.data.model.PersonType
import com.duoschedule.notification.CourseNotificationManager
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

interface WidgetDependencies {
    val database: AppDatabase
    val settingsDataStore: SettingsDataStore
}

class ScheduleWidgetReceiver : AppWidgetProvider() {

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
        super.onReceive(context, intent)
        
        when (intent.action) {
            ACTION_UPDATE_WIDGET,
            AppWidgetManager.ACTION_APPWIDGET_UPDATE -> {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = ComponentName(context, ScheduleWidgetReceiver::class.java)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
                
                for (appWidgetId in appWidgetIds) {
                    updateWidget(context, appWidgetManager, appWidgetId)
                }
            }
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
                        context.getString(R.string.remaining_time, remaining)
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
                        context.getString(R.string.remaining_time, remaining)
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
                
                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (e: Exception) {
                Log.e("ScheduleWidgetReceiver", "Failed to update widget", e)
            }
        }
    }

    companion object {
        const val ACTION_UPDATE_WIDGET = "com.duoschedule.action.UPDATE_WIDGET"

        fun updateAllWidgets(context: Context) {
            val intent = Intent(context, ScheduleWidgetReceiver::class.java).apply {
                action = ACTION_UPDATE_WIDGET
            }
            context.sendBroadcast(intent)
        }
    }
}
