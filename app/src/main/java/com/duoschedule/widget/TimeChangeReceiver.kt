package com.duoschedule.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.duoschedule.DuoScheduleApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TimeChangeReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("TimeChangeReceiver", "Received: ${intent.action}")
        
        when (intent.action) {
            Intent.ACTION_TIME_TICK -> {
                ScheduleWidgetReceiverMIUI.updateAllWidgets(context)
                FreeTimeWidgetReceiver.updateAllWidgets(context)
                MyCoursesWidgetReceiver.updateAllWidgets(context)
                TaCoursesWidgetReceiver.updateAllWidgets(context)
            }
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> {
                ScheduleWidgetReceiverMIUI.updateAllWidgets(context)
                FreeTimeWidgetReceiver.updateAllWidgets(context)
                MyCoursesWidgetReceiver.updateAllWidgets(context)
                TaCoursesWidgetReceiver.updateAllWidgets(context)
                
                Log.i("TimeChangeReceiver", "Time/timezone changed, rescheduling notifications...")
                val app = context.applicationContext as DuoScheduleApp
                scope.launch {
                    try {
                        app.notificationManager.scheduleReminderNotifications()
                        Log.i("TimeChangeReceiver", "Notifications rescheduled successfully")
                    } catch (e: Exception) {
                        Log.e("TimeChangeReceiver", "Failed to reschedule notifications", e)
                    }
                }
            }
            Intent.ACTION_SCREEN_ON -> {
                Log.i("TimeChangeReceiver", "Screen turned on, checking ongoing course and rescheduling...")
                val app = context.applicationContext as DuoScheduleApp
                scope.launch {
                    try {
                        val hasOngoing = app.notificationManager.checkAndShowOngoingNotification()
                        Log.i("TimeChangeReceiver", "Screen on check result: hasOngoing=$hasOngoing")
                        
                        app.notificationManager.scheduleReminderNotifications()
                        Log.i("TimeChangeReceiver", "Notifications rescheduled on screen on")
                    } catch (e: Exception) {
                        Log.e("TimeChangeReceiver", "Failed to check ongoing course", e)
                    }
                }
            }
        }
    }

    companion object {
        fun register(context: Context): TimeChangeReceiver {
            val receiver = TimeChangeReceiver()
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_TIME_TICK)
                addAction(Intent.ACTION_TIME_CHANGED)
                addAction(Intent.ACTION_TIMEZONE_CHANGED)
                addAction(Intent.ACTION_SCREEN_ON)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.registerReceiver(
                    context,
                    receiver,
                    filter,
                    ContextCompat.RECEIVER_NOT_EXPORTED
                )
            } else {
                context.registerReceiver(receiver, filter)
            }
            return receiver
        }
    }
}
