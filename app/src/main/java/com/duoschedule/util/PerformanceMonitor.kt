package com.duoschedule.util

import android.os.SystemClock
import android.util.Log
import java.util.concurrent.ConcurrentHashMap

object PerformanceMonitor {
    private const val TAG = "PerformanceMonitor"
    private const val SLOW_THRESHOLD_MS = 16L
    
    private val startTimes = ConcurrentHashMap<String, Long>()
    private val startupMetrics = mutableMapOf<String, Long>()
    
    private var appStartTime: Long = 0
    private var isInitialized = false
    
    fun recordAppStart() {
        appStartTime = SystemClock.uptimeMillis()
        Log.d(TAG, "App start recorded at: $appStartTime")
    }
    
    fun recordStartupComplete() {
        val currentTime = SystemClock.uptimeMillis()
        val startupTime = currentTime - appStartTime
        startupMetrics["cold_startup"] = startupTime
        Log.d(TAG, "Cold startup time: ${startupTime}ms")
        isInitialized = true
    }
    
    fun startTrace(traceName: String) {
        startTimes[traceName] = SystemClock.uptimeMillis()
    }
    
    fun endTrace(traceName: String): Long {
        val startTime = startTimes.remove(traceName) ?: return 0
        val duration = SystemClock.uptimeMillis() - startTime
        
        if (duration > SLOW_THRESHOLD_MS) {
            Log.w(TAG, "Slow trace '$traceName': ${duration}ms")
        } else {
            Log.d(TAG, "Trace '$traceName': ${duration}ms")
        }
        
        return duration
    }
    
    fun recordFrameTime(frameTimeMs: Long) {
        if (frameTimeMs > 16) {
            Log.w(TAG, "Frame drop detected: ${frameTimeMs}ms (${frameTimeMs / 16} frames dropped)")
        }
    }
    
    fun recordDatabaseQuery(queryName: String, durationMs: Long) {
        if (durationMs > 50) {
            Log.w(TAG, "Slow database query '$queryName': ${durationMs}ms")
        } else {
            Log.d(TAG, "Database query '$queryName': ${durationMs}ms")
        }
    }
    
    fun getStartupMetrics(): Map<String, Long> = startupMetrics.toMap()
    
    fun clearMetrics() {
        startTimes.clear()
        startupMetrics.clear()
        isInitialized = false
    }
}
