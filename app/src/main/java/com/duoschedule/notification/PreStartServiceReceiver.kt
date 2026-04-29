package com.duoschedule.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class PreStartServiceReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "=== PreStartServiceReceiver.onReceive ===")
        Log.d(TAG, "action: ${intent.action}")
        
        if (intent.action == ACTION_PRE_START) {
            Log.i(TAG, "预启动前台服务，确保课程开始时服务已就绪")
            
            if (!LiveUpdateService.isServiceRunning()) {
                LiveUpdateService.preStart(context)
                Log.i(TAG, "LiveUpdateService 预启动成功")
            } else {
                Log.d(TAG, "LiveUpdateService 已在运行中")
            }
        }
    }

    companion object {
        private const val TAG = "PreStartServiceReceiver"
        const val ACTION_PRE_START = "com.duoschedule.action.PRE_START_SERVICE"
    }
}
