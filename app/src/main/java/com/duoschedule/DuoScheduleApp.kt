package com.duoschedule

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.duoschedule.data.local.AppDatabase
import com.duoschedule.notification.CourseNotificationManager
import com.duoschedule.util.PerformanceMonitor
import com.duoschedule.widget.TimeChangeReceiver
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class DuoScheduleApp : Application(), Configuration.Provider {
    
    @Inject
    lateinit var database: AppDatabase
    
    @Inject
    lateinit var notificationManager: CourseNotificationManager
    
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var timeChangeReceiver: TimeChangeReceiver? = null

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        PerformanceMonitor.recordAppStart()
        super.onCreate()
        
        try {
            notificationManager.createNotificationChannels()
            
            timeChangeReceiver = TimeChangeReceiver.register(this)
        } catch (e: Exception) {
            Log.e("DuoScheduleApp", "Failed to initialize components", e)
        }
        
        applicationScope.launch {
            PerformanceMonitor.startTrace("database_preload")
            try {
                database.courseDao().getAllCourses()
            } catch (e: Exception) {
                Log.e("DuoScheduleApp", "Failed to preload database", e)
            }
            PerformanceMonitor.endTrace("database_preload")
            
            try {
                notificationManager.scheduleReminderNotifications()
            } catch (e: Exception) {
                Log.e("DuoScheduleApp", "Failed to schedule notifications", e)
            }
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        try {
            timeChangeReceiver?.let {
                unregisterReceiver(it)
            }
        } catch (e: Exception) {
            Log.e("DuoScheduleApp", "Failed to unregister receiver", e)
        }
    }
}