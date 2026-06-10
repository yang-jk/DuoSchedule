package com.duoschedule.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList

enum class LogLevel(val label: String, val emoji: String) {
    VERBOSE("VERBOSE", "📋"),
    DEBUG("DEBUG", "🐛"),
    INFO("INFO", "ℹ️"),
    WARN("WARN", "⚠️"),
    ERROR("ERROR", "❌")
}

data class AppLogEntry(
    val timestamp: Long = System.currentTimeMillis(),
    val level: LogLevel,
    val tag: String,
    val message: String,
    val throwable: Throwable? = null
) {
    val formattedTime: String
        get() = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date(timestamp))

    val formattedDate: String
        get() = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date(timestamp))

    val formattedDetails: String
        get() {
            val sb = StringBuilder()
            sb.append("[$formattedDate] ")
            sb.append("${level.label}/$tag: ")
            sb.append(message)
            if (throwable != null) {
                sb.append("\n${throwable.stackTraceToString()}")
            }
            return sb.toString()
        }
}

object AppLogger {
    private const val MAX_LOG_SIZE = 200
    private val _logs = CopyOnWriteArrayList<AppLogEntry>()
    val logs: List<AppLogEntry> get() = _logs.toList()

    private val listeners = CopyOnWriteArrayList<() -> Unit>()

    fun addListener(listener: () -> Unit) { listeners.add(listener) }
    fun removeListener(listener: () -> Unit) { listeners.remove(listener) }
    private fun notifyListeners() { listeners.forEach { it.invoke() } }

    fun v(tag: String, message: String) = addLog(AppLogEntry(level = LogLevel.VERBOSE, tag = tag, message = message))
    fun d(tag: String, message: String) = addLog(AppLogEntry(level = LogLevel.DEBUG, tag = tag, message = message))
    fun i(tag: String, message: String) = addLog(AppLogEntry(level = LogLevel.INFO, tag = tag, message = message))
    fun w(tag: String, message: String, throwable: Throwable? = null) = addLog(AppLogEntry(level = LogLevel.WARN, tag = tag, message = message, throwable = throwable))
    fun e(tag: String, message: String, throwable: Throwable? = null) = addLog(AppLogEntry(level = LogLevel.ERROR, tag = tag, message = message, throwable = throwable))

    private fun addLog(entry: AppLogEntry) {
        _logs.add(0, entry)
        if (_logs.size > MAX_LOG_SIZE) _logs.removeAt(_logs.size - 1)
        notifyListeners()
        // 同时输出到 Android Logcat
        val tr = entry.throwable
        when (entry.level) {
            LogLevel.VERBOSE -> android.util.Log.v(entry.tag, entry.message, tr)
            LogLevel.DEBUG -> android.util.Log.d(entry.tag, entry.message, tr)
            LogLevel.INFO -> android.util.Log.i(entry.tag, entry.message, tr)
            LogLevel.WARN -> android.util.Log.w(entry.tag, entry.message, tr)
            LogLevel.ERROR -> android.util.Log.e(entry.tag, entry.message, tr)
        }
    }

    fun clear() { _logs.clear(); notifyListeners() }

    fun getLogsText(filter: LogLevel? = null): String {
        val filtered = if (filter != null) _logs.filter { it.level == filter } else _logs
        return if (filtered.isEmpty()) "暂无日志记录" else filtered.joinToString("\n\n") { it.formattedDetails }
    }
}
