package com.duoschedule.notification

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class NotificationDebugLog(
    val timestamp: Long = System.currentTimeMillis(),
    val type: LogType,
    val result: LogResult,
    val message: String,
    val params: Map<String, String> = emptyMap()
) {
    enum class LogType {
        REMINDER,
        ONGOING,
        SHORT_ONGOING,
        CANCEL_ALL
    }
    
    enum class LogResult {
        SUCCESS,
        FAILURE
    }
    
    val formattedTime: String
        get() = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
    
    val formattedDetails: String
        get() {
            val sb = StringBuilder()
            sb.append("[$formattedTime] ")
            sb.append(type.name)
            sb.append(": ")
            sb.append(if (result == LogResult.SUCCESS) "成功" else "失败")
            if (message.isNotEmpty()) {
                sb.append(" - ")
                sb.append(message)
            }
            if (params.isNotEmpty()) {
                sb.append("\n参数: ")
                params.forEach { (key, value) ->
                    sb.append("$key=$value, ")
                }
                sb.setLength(sb.length - 2)
            }
            return sb.toString()
        }
}

object NotificationDebugLogger {
    private const val MAX_LOG_SIZE = 50
    private val _logs = mutableListOf<NotificationDebugLog>()
    val logs: List<NotificationDebugLog> get() = _logs.toList()
    
    private val listeners = mutableListOf<() -> Unit>()
    
    fun addListener(listener: () -> Unit) {
        listeners.add(listener)
    }
    
    fun removeListener(listener: () -> Unit) {
        listeners.remove(listener)
    }
    
    private fun notifyListeners() {
        listeners.forEach { it.invoke() }
    }
    
    @Synchronized
    fun log(log: NotificationDebugLog) {
        _logs.add(0, log)
        if (_logs.size > MAX_LOG_SIZE) {
            _logs.removeAt(_logs.size - 1)
        }
        notifyListeners()
    }
    
    @Synchronized
    fun clear() {
        _logs.clear()
        notifyListeners()
    }
    
    fun getLogsText(): String {
        return if (_logs.isEmpty()) {
            "暂无日志记录"
        } else {
            _logs.joinToString("\n\n") { it.formattedDetails }
        }
    }
}
