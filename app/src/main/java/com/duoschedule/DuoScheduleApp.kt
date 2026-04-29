package com.duoschedule

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Configuration
import com.duoschedule.data.local.AppDatabase
import com.duoschedule.data.local.SettingsDataStore
import com.duoschedule.data.model.PersonType
import com.duoschedule.data.repository.CourseRepository
import com.duoschedule.notification.CourseNotificationManager
import com.duoschedule.notification.NotificationRescheduleWorker
import com.duoschedule.notification.RingerModeManager
import com.duoschedule.util.ComposeWarmup
import com.duoschedule.util.PerformanceMonitor
import com.duoschedule.widget.TimeChangeReceiver
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class DuoScheduleApp : Application(), Configuration.Provider {
    
    @Inject
    lateinit var database: AppDatabase
    
    @Inject
    lateinit var settingsDataStore: SettingsDataStore
    
    @Inject
    lateinit var repository: CourseRepository
    
    @Inject
    lateinit var notificationManager: CourseNotificationManager
    
    @Inject
    lateinit var ringerModeManager: RingerModeManager
    
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var timeChangeReceiver: TimeChangeReceiver? = null
    private var lifecycleObserver: AppLifecycleObserver? = null

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        PerformanceMonitor.recordAppStart()
        super.onCreate()
        
        Log.i("DuoScheduleApp", "=== App Started ===")
        Log.i("DuoScheduleApp", "Time: ${java.time.LocalDateTime.now()}")
        
        ComposeWarmup.warmup(this, applicationScope)
        
        applicationScope.launch {
            preloadViewModels()
        }
        
        try {
            notificationManager.createNotificationChannels()
            Log.i("DuoScheduleApp", "Notification channels created")
            
            timeChangeReceiver = TimeChangeReceiver.register(this)
        } catch (e: Exception) {
            Log.e("DuoScheduleApp", "Failed to initialize components", e)
        }
        
        lifecycleObserver = AppLifecycleObserver()
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver!!)
        
        NotificationRescheduleWorker.schedule(this)
        NotificationRescheduleWorker.scheduleQuickCheck(this)
        Log.i("DuoScheduleApp", "NotificationRescheduleWorker scheduled")
        
        applicationScope.launch {
            PerformanceMonitor.startTrace("database_preload")
            try {
                database.courseDao().getAllCourses()
            } catch (e: Exception) {
                Log.e("DuoScheduleApp", "Failed to preload database", e)
            }
            PerformanceMonitor.endTrace("database_preload")
            
            preloadSettings()
            
            updateCurrentWeekIfNeeded()
            
            checkAndRestoreRingerMode()
            
            rescheduleNotifications("app_start")
        }
    }
    
    private fun checkAndRestoreRingerMode() {
        if (!ringerModeManager.hasNotificationPolicyAccess()) {
            Log.d("DuoScheduleApp", "没有勿扰模式权限，跳过铃声检查")
            return
        }
        
        if (ringerModeManager.isAutoSilentActive()) {
            val endTime = ringerModeManager.getAutoSilentEndTime()
            val now = System.currentTimeMillis()
            
            if (now >= endTime) {
                Log.i("DuoScheduleApp", "启动时检测到静音已过期，恢复铃声")
                ringerModeManager.restoreRingerMode()
                ringerModeManager.clearAutoSilentState()
            } else {
                val remainingMinutes = (endTime - now) / 60000
                Log.i("DuoScheduleApp", "自动静音未过期，剩余 $remainingMinutes 分钟")
            }
        } else {
            Log.d("DuoScheduleApp", "自动静音未激活")
        }
    }
    
    private suspend fun rescheduleNotifications(reason: String) {
        try {
            Log.i("DuoScheduleApp", "Rescheduling notifications (reason: $reason)...")
            notificationManager.scheduleReminderNotifications()
            Log.i("DuoScheduleApp", "Notifications rescheduled (reason: $reason)")
        } catch (e: Exception) {
            Log.e("DuoScheduleApp", "Failed to reschedule notifications (reason: $reason)", e)
        }
    }
    
    private inner class AppLifecycleObserver : DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) {
            Log.i("DuoScheduleApp", "App entered foreground")
            applicationScope.launch {
                updateCurrentWeekIfNeeded()
                checkAndRestoreRingerMode()
                val hasOngoingCourse = notificationManager.checkAndShowOngoingNotification()
                Log.i("DuoScheduleApp", "当前课程检查结果: hasOngoingCourse=$hasOngoingCourse")
                rescheduleNotifications("enter_foreground")
            }
        }
        
        override fun onStop(owner: LifecycleOwner) {
            Log.i("DuoScheduleApp", "App entered background")
            NotificationRescheduleWorker.schedule(this@DuoScheduleApp)
            Log.i("DuoScheduleApp", "NotificationRescheduleWorker re-scheduled on background")
        }
    }
    
    private suspend fun preloadSettings() {
        PerformanceMonitor.startTrace("settings_preload")
        try {
            kotlinx.coroutines.coroutineScope {
                listOf(
                    async { settingsDataStore.personAName.first() },
                    async { settingsDataStore.personBName.first() },
                    async { settingsDataStore.getSemesterStartDate(PersonType.PERSON_A).first() },
                    async { settingsDataStore.getSemesterStartDate(PersonType.PERSON_B).first() },
                    async { settingsDataStore.getTotalWeeks(PersonType.PERSON_A).first() },
                    async { settingsDataStore.getTotalWeeks(PersonType.PERSON_B).first() },
                    async { settingsDataStore.getCurrentWeek(PersonType.PERSON_A).first() },
                    async { settingsDataStore.getCurrentWeek(PersonType.PERSON_B).first() },
                    async { settingsDataStore.getTotalPeriods(PersonType.PERSON_A).first() },
                    async { settingsDataStore.getTotalPeriods(PersonType.PERSON_B).first() },
                    async { settingsDataStore.getPeriodTimes(PersonType.PERSON_A).first() },
                    async { settingsDataStore.getPeriodTimes(PersonType.PERSON_B).first() },
                    async { settingsDataStore.showSaturday.first() },
                    async { settingsDataStore.showSunday.first() },
                    async { settingsDataStore.showDashedBorder.first() },
                    async { settingsDataStore.themeMode.first() }
                ).awaitAll()
            }
            Log.i("DuoScheduleApp", "Settings preloaded")
        } catch (e: Exception) {
            Log.e("DuoScheduleApp", "Failed to preload settings", e)
        }
        PerformanceMonitor.endTrace("settings_preload")
    }
    
    private suspend fun updateCurrentWeekIfNeeded() {
        try {
            val personAStartDate = settingsDataStore.getSemesterStartDate(PersonType.PERSON_A).first()
            val personBStartDate = settingsDataStore.getSemesterStartDate(PersonType.PERSON_B).first()
            val personATotalWeeks = settingsDataStore.getTotalWeeks(PersonType.PERSON_A).first()
            val personBTotalWeeks = settingsDataStore.getTotalWeeks(PersonType.PERSON_B).first()
            val personACurrentWeek = settingsDataStore.getCurrentWeek(PersonType.PERSON_A).first()
            val personBCurrentWeek = settingsDataStore.getCurrentWeek(PersonType.PERSON_B).first()
            
            Log.i("DuoScheduleApp", "Ta开学日期: $personAStartDate, 总周数: $personATotalWeeks, 当前周: $personACurrentWeek")
            Log.i("DuoScheduleApp", "我开学日期: $personBStartDate, 总周数: $personBTotalWeeks, 当前周: $personBCurrentWeek")
            Log.i("DuoScheduleApp", "今天日期: ${java.time.LocalDate.now()}")
            
            val calculatedWeekA = settingsDataStore.calculateCurrentWeek(personAStartDate, personATotalWeeks)
            val calculatedWeekB = settingsDataStore.calculateCurrentWeek(personBStartDate, personBTotalWeeks)
            
            Log.i("DuoScheduleApp", "计算得到Ta周次: $calculatedWeekA, 我周次: $calculatedWeekB")
            
            if (calculatedWeekA != personACurrentWeek) {
                Log.i("DuoScheduleApp", "自动更新Ta的当前周次: $personACurrentWeek -> $calculatedWeekA")
                settingsDataStore.setCurrentWeek(PersonType.PERSON_A, calculatedWeekA)
            }
            
            if (calculatedWeekB != personBCurrentWeek) {
                Log.i("DuoScheduleApp", "自动更新我的当前周次: $personBCurrentWeek -> $calculatedWeekB")
                settingsDataStore.setCurrentWeek(PersonType.PERSON_B, calculatedWeekB)
            }
        } catch (e: Exception) {
            Log.e("DuoScheduleApp", "Failed to update current week", e)
        }
    }
    
    private suspend fun preloadViewModels() {
        PerformanceMonitor.startTrace("viewmodel_preload")
        try {
            Log.i("DuoScheduleApp", "Preloading ViewModels and database...")
            
            database.courseDao().getAllCourses()
            Log.i("DuoScheduleApp", "Database preloaded")
            
            preloadSettings()
            
            kotlinx.coroutines.coroutineScope {
                listOf(
                    async { repository.getCoursesByPerson(PersonType.PERSON_A).first() },
                    async { repository.getCoursesByPerson(PersonType.PERSON_B).first() },
                    async { repository.getPersonAName().first() },
                    async { repository.getPersonBName().first() },
                    async { repository.getCurrentWeek(PersonType.PERSON_A).first() },
                    async { repository.getCurrentWeek(PersonType.PERSON_B).first() },
                    async { repository.getTotalWeeks(PersonType.PERSON_A).first() },
                    async { repository.getTotalWeeks(PersonType.PERSON_B).first() },
                    async { repository.getTotalPeriods(PersonType.PERSON_A).first() },
                    async { repository.getTotalPeriods(PersonType.PERSON_B).first() },
                    async { repository.getPeriodTimes(PersonType.PERSON_A).first() },
                    async { repository.getPeriodTimes(PersonType.PERSON_B).first() },
                    async { repository.getTodayCourseDisplayMode().first() },
                    async { repository.getShowSaturday().first() },
                    async { repository.getShowSunday().first() }
                ).awaitAll()
            }
            Log.i("DuoScheduleApp", "Repository data preloaded")
            
            Log.i("DuoScheduleApp", "ViewModels preloaded")
        } catch (e: Exception) {
            Log.e("DuoScheduleApp", "Failed to preload ViewModels", e)
        }
        PerformanceMonitor.endTrace("viewmodel_preload")
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
