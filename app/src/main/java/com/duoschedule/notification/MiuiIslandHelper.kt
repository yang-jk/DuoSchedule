package com.duoschedule.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import com.duoschedule.R
import org.json.JSONObject

object MiuiIslandHelper {
    private const val TAG = "MiuiIslandHelper"
    
    const val MIUI_FOCUS_ISLAND_PROPERTY = "miui.focus.islandProperty"
    const val MIUI_FOCUS_ISLAND_BIG_AREA = "miui.focus.bigIslandArea"
    const val MIUI_FOCUS_ISLAND_SMALL_AREA = "miui.focus.smallIslandArea"
    const val MIUI_FOCUS_ISLAND_TICKER_DATA = "miui.focus.tickerData"
    
    const val ISLAND_PROPERTY_APP = 2
    
    const val IMAGE_TYPE_RESOURCE = 3
    const val IMAGE_TYPE_ICON = 1
    
    const val TEXT_TYPE_TITLE = 2
    const val TEXT_TYPE_TITLE_WITH_ICON = 1
    
    private const val PIC_LIVE_UPDATE = "miui.focus.pic_liveupdate"
    
    fun buildTickerData(
        leftTitle: String = "",
        rightTitle: String
    ): String {
        val tickerData = JSONObject().apply {
            put("islandProperty", ISLAND_PROPERTY_APP)
            
            val bigIslandArea = JSONObject().apply {
                val imageTextInfoLeft = JSONObject().apply {
                    put("type", TEXT_TYPE_TITLE_WITH_ICON)
                    put("textInfo", JSONObject().apply {
                        put("title", leftTitle)
                    })
                    put("picInfo", JSONObject().apply {
                        put("type", IMAGE_TYPE_RESOURCE)
                        put("pic", PIC_LIVE_UPDATE)
                    })
                }
                
                val imageTextInfoRight = JSONObject().apply {
                    put("type", TEXT_TYPE_TITLE)
                    put("textInfo", JSONObject().apply {
                        put("title", rightTitle)
                    })
                }
                
                put("imageTextInfoLeft", imageTextInfoLeft)
                put("imageTextInfoRight", imageTextInfoRight)
            }
            
            val smallIslandArea = JSONObject().apply {
                put("picInfo", JSONObject().apply {
                    put("type", IMAGE_TYPE_RESOURCE)
                    put("pic", PIC_LIVE_UPDATE)
                })
            }
            
            put("bigIslandArea", bigIslandArea)
            put("smallIslandArea", smallIslandArea)
        }
        
        return tickerData.toString()
    }
    
    fun buildProgressTickerData(
        courseName: String,
        location: String,
        remainingMinutes: Int
    ): String {
        val rightTitle = if (remainingMinutes <= 0) {
            "即将结束"
        } else {
            "$location · 剩余 ${remainingMinutes}分钟"
        }
        
        return buildTickerData(
            leftTitle = courseName,
            rightTitle = rightTitle
        )
    }
    
    fun buildReminderTickerData(
        courseName: String,
        location: String,
        advanceMinutes: Int
    ): String {
        val rightTitle = "$location · 还有${advanceMinutes}分钟上课"
        
        return buildTickerData(
            leftTitle = courseName,
            rightTitle = rightTitle
        )
    }
    
    fun applyMiuiIslandExtras(
        builder: Notification.Builder,
        tickerData: String
    ): Notification.Builder {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val extras = Bundle().apply {
                    putString(MIUI_FOCUS_ISLAND_TICKER_DATA, tickerData)
                }
                builder.addExtras(extras)
                Log.d(TAG, "已应用小米动态岛 extras: $tickerData")
            } catch (e: Exception) {
                Log.e(TAG, "应用小米动态岛 extras 失败", e)
            }
        }
        return builder
    }
    
    fun createMiuiIslandNotification(
        context: Context,
        channelId: String,
        courseName: String,
        location: String,
        remainingMinutes: Int,
        pendingIntent: PendingIntent?,
        notificationId: Int
    ): Notification? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return null
        }
        
        return try {
            val tickerData = buildProgressTickerData(
                courseName = courseName,
                location = location,
                remainingMinutes = remainingMinutes
            )
            
            val endTime = System.currentTimeMillis() + remainingMinutes * 60 * 1000L
            
            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(courseName)
                .setContentText("$location · 剩余 $remainingMinutes 分钟")
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOnlyAlertOnce(true)
                .setUsesChronometer(true)
                .setChronometerCountDown(true)
                .setWhen(endTime)
            
            pendingIntent?.let { builder.setContentIntent(it) }
            
            if (Build.VERSION.SDK_INT >= 36) {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                val canPostPromoted = notificationManager.canPostPromotedNotifications()
                Log.d(TAG, "小米设备实况通知检查: canPostPromoted=$canPostPromoted")
                
                if (canPostPromoted) {
                    builder.setRequestPromotedOngoing(true)
                    builder.setShortCriticalText("剩余 $remainingMinutes 分钟")
                    Log.d(TAG, ">>> 小米设备实况通知已启用 <<<")
                }
            }
            
            val notification = builder.build()
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    val extras = Bundle().apply {
                        putString(MIUI_FOCUS_ISLAND_TICKER_DATA, tickerData)
                    }
                    notification.extras.putAll(extras)
                    Log.d(TAG, "已应用小米动态岛 extras: $tickerData")
                } catch (e: Exception) {
                    Log.e(TAG, "应用小米动态岛 extras 失败", e)
                }
            }
            
            notification
        } catch (e: Exception) {
            Log.e(TAG, "创建小米动态岛通知失败", e)
            null
        }
    }
    
    fun createMiuiReminderNotification(
        context: Context,
        channelId: String,
        courseName: String,
        location: String,
        advanceMinutes: Int,
        pendingIntent: PendingIntent?
    ): Notification? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return null
        }
        
        return try {
            val tickerData = buildReminderTickerData(
                courseName = courseName,
                location = location,
                advanceMinutes = advanceMinutes
            )
            
            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("即将上课")
                .setContentText("$courseName · $location · 还有 $advanceMinutes 分钟")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
            
            pendingIntent?.let { builder.setContentIntent(it) }
            
            val notification = builder.build()
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    val extras = Bundle().apply {
                        putString(MIUI_FOCUS_ISLAND_TICKER_DATA, tickerData)
                    }
                    notification.extras.putAll(extras)
                    Log.d(TAG, "已应用小米动态岛提醒 extras: $tickerData")
                } catch (e: Exception) {
                    Log.e(TAG, "应用小米动态岛提醒 extras 失败", e)
                }
            }
            
            notification
        } catch (e: Exception) {
            Log.e(TAG, "创建小米动态岛提醒通知失败", e)
            null
        }
    }
    
    fun isMiuiDevice(): Boolean {
        return try {
            val propClass = Class.forName("android.os.SystemProperties")
            val getMethod = propClass.getDeclaredMethod("get", String::class.java)
            val version = getMethod.invoke(null, "ro.miui.ui.version.name")
            version != null && version.toString().isNotEmpty()
        } catch (ex: Exception) {
            false
        }
    }
}
