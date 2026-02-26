package com.duoschedule.data.importexport

import android.content.Context
import android.net.Uri
import com.duoschedule.data.model.Course
import com.duoschedule.data.model.PersonType
import com.duoschedule.data.model.WeekType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.charset.Charset
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object CsvExporter {
    private const val CSV_HEADER = "课程名称,星期,开始时间,结束时间,地点,周次类型,开始周,结束周,自定义周次,所属人"
    private const val TEMPLATE_HEADER = "课程名称（必填）,星期(1-7),开始节次,结束节次,教室地点,上课老师,周次"
    private const val SETTINGS_HEADER = "# 课表设置"
    private const val PERSON_A_MARKER = "# Ta的课表设置"
    private const val PERSON_B_MARKER = "# 我的课表设置"
    private const val PERSON_NAMES_MARKER = "# 用户名称"

    suspend fun exportToCsv(
        context: Context,
        uri: Uri,
        courses: List<Course>,
        settingsA: ScheduleSettingsExport?,
        settingsB: ScheduleSettingsExport?,
        personAName: String,
        personBName: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream, "UTF-8").use { writer ->
                    writer.append("\uFEFF")
                    
                    writer.append("# 双人课程表导出文件\n")
                    writer.append("# 导出时间: ${DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(java.time.LocalDateTime.now())}\n")
                    writer.append("# 版本: 2.0\n\n")
                    
                    writer.append("$PERSON_NAMES_MARKER\n")
                    writer.append("Ta的名称,$personAName\n")
                    writer.append("我的名称,$personBName\n\n")
                    
                    if (settingsA != null) {
                        writer.append("$PERSON_A_MARKER\n")
                        writer.append("开学时间,${LocalDate.ofEpochDay(settingsA.semesterStartDate)}\n")
                        writer.append("学期总周数,${settingsA.totalWeeks}\n")
                        writer.append("当前周次,${settingsA.currentWeek}\n")
                        writer.append("每天节数,${settingsA.totalPeriods}\n")
                        writer.append("课程时间,${settingsA.periodTimes.joinToString(";")}\n\n")
                    }
                    
                    if (settingsB != null) {
                        writer.append("$PERSON_B_MARKER\n")
                        writer.append("开学时间,${LocalDate.ofEpochDay(settingsB.semesterStartDate)}\n")
                        writer.append("学期总周数,${settingsB.totalWeeks}\n")
                        writer.append("当前周次,${settingsB.currentWeek}\n")
                        writer.append("每天节数,${settingsB.totalPeriods}\n")
                        writer.append("课程时间,${settingsB.periodTimes.joinToString(";")}\n\n")
                    }
                    
                    writer.append("# 课程数据\n")
                    writer.append("$CSV_HEADER\n")
                    
                    courses.forEach { course ->
                        val weekTypeStr = when (course.weekType) {
                            WeekType.ALL -> "每周"
                            WeekType.ODD -> "单周"
                            WeekType.EVEN -> "双周"
                            WeekType.CUSTOM -> "自定义"
                        }
                        val personStr = if (course.personType == PersonType.PERSON_A) "Ta" else "我"
                        val dayStr = getDayOfWeekString(course.dayOfWeek)
                        
                        writer.append("${escapeCsvField(course.name)},")
                        writer.append("$dayStr,")
                        writer.append("${course.getStartTimeString()},")
                        writer.append("${course.getEndTimeString()},")
                        writer.append("${escapeCsvField(course.location)},")
                        writer.append("$weekTypeStr,")
                        writer.append("${course.startWeek},")
                        writer.append("${course.endWeek},")
                        writer.append("${escapeCsvField(course.customWeeks)},")
                        writer.append("$personStr\n")
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importFromCsv(
        context: Context,
        uri: Uri,
        targetPerson: PersonType? = null,
        periodTimes: List<String> = emptyList()
    ): ImportResult = withContext(Dispatchers.IO) {
        try {
            val courses = mutableListOf<CourseImportData>()
            val errors = mutableListOf<String>()
            var lineNumber = 0
            var currentSection = ""
            var personAName = "Ta"
            var personBName = "我"
            val settingsA = mutableMapOf<String, String>()
            val settingsB = mutableMapOf<String, String>()
            var isTemplateFormat = false

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val bytes = inputStream.readBytes()
                val charset = detectCharset(bytes)
                
                val content = try {
                    String(bytes, charset)
                } catch (e: Exception) {
                    String(bytes, Charsets.UTF_8)
                }
                
                val reader = BufferedReader(content.reader())
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    lineNumber++
                    val lineStr = line!!.trim()
                    
                    if (lineStr.isEmpty() || lineStr.startsWith("#")) {
                        when {
                            lineStr.contains("Ta的课表设置") -> currentSection = "settingsA"
                            lineStr.contains("我的课表设置") -> currentSection = "settingsB"
                            lineStr.contains("课程数据") -> currentSection = "courses"
                            lineStr.contains("用户名称") -> currentSection = "names"
                        }
                        continue
                    }
                    
                    if (lineStr == CSV_HEADER) {
                        isTemplateFormat = false
                        continue
                    }
                    if (lineStr == TEMPLATE_HEADER || lineStr.contains("开始节次")) {
                        isTemplateFormat = true
                        continue
                    }
                    
                    when (currentSection) {
                        "names" -> {
                            val parts = parseCsvLine(lineStr)
                            if (parts.size >= 2) {
                                when (parts[0]) {
                                    "Ta的名称" -> personAName = parts[1]
                                    "我的名称" -> personBName = parts[1]
                                }
                            }
                        }
                        "settingsA", "settingsB" -> {
                            val parts = parseCsvLine(lineStr)
                            if (parts.size >= 2) {
                                val settings = if (currentSection == "settingsA") settingsA else settingsB
                                settings[parts[0]] = parts[1]
                            }
                        }
                        "courses", "" -> {
                            val result = if (isTemplateFormat) {
                                parseTemplateLine(lineStr, lineNumber, targetPerson, periodTimes)
                            } else {
                                parseCourseLine(lineStr, lineNumber, targetPerson)
                            }
                            if (result.first != null) {
                                courses.add(result.first!!)
                            } else if (result.second.isNotEmpty()) {
                                errors.add(result.second)
                            }
                        }
                    }
                }
            }

            if (courses.isEmpty() && errors.isEmpty()) {
                ImportResult(
                    success = false,
                    errors = listOf("文件格式错误，请对照模板检查")
                )
            } else if (courses.isEmpty() && errors.isNotEmpty()) {
                ImportResult(
                    success = false,
                    errors = listOf("文件格式错误，请对照模板检查")
                )
            } else {
                ImportResult(
                    success = true,
                    importedCount = courses.size,
                    failedCount = errors.size,
                    errors = errors,
                    courses = courses
                )
            }
        } catch (e: Exception) {
            ImportResult(
                success = false,
                errors = listOf("文件格式错误，请对照模板检查")
            )
        }
    }
    
    private fun detectCharset(bytes: ByteArray): Charset {
        if (bytes.size >= 3 && bytes[0] == 0xEF.toByte() && bytes[1] == 0xBB.toByte() && bytes[2] == 0xBF.toByte()) {
            return Charsets.UTF_8
        }
        
        if (bytes.size >= 2) {
            val b0 = bytes[0].toInt() and 0xFF
            val b1 = bytes[1].toInt() and 0xFF
            
            if (b0 == 0xFE && b1 == 0xFF) {
                return Charsets.UTF_16BE
            }
            if (b0 == 0xFF && b1 == 0xFE) {
                return Charsets.UTF_16LE
            }
        }
        
        val sampleSize = minOf(4096, bytes.size)
        val sample = String(bytes, 0, sampleSize, Charsets.UTF_8)
        
        var hasUtf8Error = false
        try {
            val decoder = Charsets.UTF_8.newDecoder()
            decoder.decode(java.nio.ByteBuffer.wrap(bytes))
        } catch (e: Exception) {
            hasUtf8Error = true
        }
        
        if (!hasUtf8Error) {
            return Charsets.UTF_8
        }
        
        try {
            val gbkContent = String(bytes, charset("GBK"))
            val reencoded = gbkContent.toByteArray(charset("GBK"))
            if (reencoded.contentEquals(bytes)) {
                return charset("GBK")
            }
        } catch (e: Exception) {
        }
        
        try {
            val gb2312Content = String(bytes, charset("GB2312"))
            val reencoded = gb2312Content.toByteArray(charset("GB2312"))
            if (reencoded.contentEquals(bytes)) {
                return charset("GB2312")
            }
        } catch (e: Exception) {
        }
        
        return Charsets.UTF_8
    }

    private fun parseCourseLine(
        line: String,
        lineNumber: Int,
        targetPerson: PersonType?
    ): Pair<CourseImportData?, String> {
        return try {
            val parts = parseCsvLine(line)
            if (parts.size < 10) {
                return Pair(null, "第${lineNumber}行: 字段数量不足")
            }

            val name = parts[0]
            if (name.isBlank()) {
                return Pair(null, "第${lineNumber}行: 课程名称不能为空")
            }

            val dayOfWeek = parseDayOfWeek(parts[1])
            if (dayOfWeek == -1) {
                return Pair(null, "第${lineNumber}行: 无效的星期: ${parts[1]}")
            }

            val startTime = parseTime(parts[2])
            val endTime = parseTime(parts[3])
            if (startTime == null || endTime == null) {
                return Pair(null, "第${lineNumber}行: 无效的时间格式")
            }

            val location = parts[4]
            val weekType = parseWeekType(parts[5])
            val startWeek = parts[6].toIntOrNull() ?: 1
            val endWeek = parts[7].toIntOrNull() ?: 16
            val customWeeks = parts[8]
            
            val personType = targetPerson ?: parsePersonType(parts[9])

            Pair(
                CourseImportData(
                    name = name,
                    location = location,
                    dayOfWeek = dayOfWeek,
                    startHour = startTime.first,
                    startMinute = startTime.second,
                    endHour = endTime.first,
                    endMinute = endTime.second,
                    weekType = weekType,
                    startWeek = startWeek,
                    endWeek = endWeek,
                    customWeeks = customWeeks,
                    personType = personType
                ),
                ""
            )
        } catch (e: Exception) {
            Pair(null, "第${lineNumber}行: 解析错误 - ${e.message}")
        }
    }

    private fun parseTemplateLine(
        line: String,
        lineNumber: Int,
        targetPerson: PersonType?,
        periodTimes: List<String>
    ): Pair<CourseImportData?, String> {
        return try {
            val parts = parseCsvLine(line)
            if (parts.size < 4) {
                return Pair(null, "第${lineNumber}行: 字段数量不足")
            }

            val name = parts[0]
            if (name.isBlank()) {
                return Pair(null, "第${lineNumber}行: 课程名称不能为空")
            }

            val dayOfWeek = parseDayOfWeek(parts[1])
            if (dayOfWeek == -1) {
                return Pair(null, "第${lineNumber}行: 无效的星期: ${parts[1]}")
            }

            val startPeriod = parts[2].toIntOrNull()
            val endPeriod = parts[3].toIntOrNull()
            if (startPeriod == null || endPeriod == null) {
                return Pair(null, "第${lineNumber}行: 无效的节次格式")
            }

            val location = if (parts.size > 4) parts[4] else ""
            val teacher = if (parts.size > 5) parts[5] else ""
            val weekStr = if (parts.size > 6) parts[6] else "1-16"

            val (startTime, endTime) = getPeriodTimeRange(startPeriod, endPeriod, periodTimes)

            val (weekType, startWeek, endWeek, customWeeks) = parseWeekString(weekStr)

            Pair(
                CourseImportData(
                    name = name,
                    location = location,
                    dayOfWeek = dayOfWeek,
                    startHour = startTime.first,
                    startMinute = startTime.second,
                    endHour = endTime.first,
                    endMinute = endTime.second,
                    weekType = weekType,
                    startWeek = startWeek,
                    endWeek = endWeek,
                    customWeeks = customWeeks,
                    personType = targetPerson ?: PersonType.PERSON_B
                ),
                ""
            )
        } catch (e: Exception) {
            Pair(null, "第${lineNumber}行: 解析错误 - ${e.message}")
        }
    }

    private fun getPeriodTimeRange(
        startPeriod: Int,
        endPeriod: Int,
        periodTimes: List<String>
    ): Pair<Pair<Int, Int>, Pair<Int, Int>> {
        if (periodTimes.isEmpty()) {
            val defaultStartHour = 8
            val defaultStartMinute = 0
            val defaultDuration = 45
            val breakDuration = 10
            
            val startMinutes = (startPeriod - 1) * (defaultDuration + breakDuration) + defaultStartHour * 60
            val endMinutes = startMinutes + (endPeriod - startPeriod + 1) * defaultDuration + (endPeriod - startPeriod) * breakDuration
            
            return Pair(
                Pair(startMinutes / 60, startMinutes % 60),
                Pair(endMinutes / 60, endMinutes % 60)
            )
        }

        val startIndex = (startPeriod - 1).coerceIn(0, periodTimes.size - 1)
        val endIndex = (endPeriod - 1).coerceIn(0, periodTimes.size - 1)

        val startTime = parseTime(periodTimes[startIndex].split("-").firstOrNull() ?: "08:00")
        val endTime = parseTime(periodTimes[endIndex].split("-").lastOrNull() ?: "08:45")

        return Pair(
            startTime ?: Pair(8, 0),
            endTime ?: Pair(8, 45)
        )
    }

    private fun parseWeekString(weekStr: String): Tuple4<WeekType, Int, Int, String> {
        val trimmed = weekStr.trim()
        
        when {
            trimmed == "单周" -> return Tuple4(WeekType.ODD, 1, 16, "")
            trimmed == "双周" -> return Tuple4(WeekType.EVEN, 1, 16, "")
            trimmed.isEmpty() -> return Tuple4(WeekType.ALL, 1, 16, "")
        }

        if (trimmed.contains("-")) {
            val rangeParts = trimmed.split("-")
            if (rangeParts.size == 2) {
                val start = rangeParts[0].trim().toIntOrNull() ?: 1
                val end = rangeParts[1].trim().toIntOrNull() ?: 16
                return Tuple4(WeekType.ALL, start, end, "")
            }
        }

        if (trimmed.contains(",")) {
            val weeks = trimmed.split(",").mapNotNull { it.trim().toIntOrNull() }
            if (weeks.isNotEmpty()) {
                return Tuple4(WeekType.CUSTOM, weeks.minOrNull() ?: 1, weeks.maxOrNull() ?: 16, weeks.joinToString(","))
            }
        }

        return Tuple4(WeekType.ALL, 1, 16, "")
    }

    private data class Tuple4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false
        
        for (char in line) {
            when {
                char == '"' && !inQuotes -> inQuotes = true
                char == '"' && inQuotes -> inQuotes = false
                char == ',' && !inQuotes -> {
                    result.add(current.toString().trim())
                    current = StringBuilder()
                }
                else -> current.append(char)
            }
        }
        result.add(current.toString().trim())
        
        return result
    }

    private fun escapeCsvField(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }

    private fun getDayOfWeekString(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            1 -> "周一"
            2 -> "周二"
            3 -> "周三"
            4 -> "周四"
            5 -> "周五"
            6 -> "周六"
            7 -> "周日"
            else -> "未知"
        }
    }

    private fun parseDayOfWeek(value: String): Int {
        return when (value.trim()) {
            "周一", "星期一", "一", "1" -> 1
            "周二", "星期二", "二", "2" -> 2
            "周三", "星期三", "三", "3" -> 3
            "周四", "星期四", "四", "4" -> 4
            "周五", "星期五", "五", "5" -> 5
            "周六", "星期六", "六", "6" -> 6
            "周日", "星期日", "日", "七", "7" -> 7
            else -> -1
        }
    }

    private fun parseTime(value: String): Pair<Int, Int>? {
        return try {
            val parts = value.trim().split(":")
            if (parts.size == 2) {
                Pair(parts[0].toInt(), parts[1].toInt())
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private fun parseWeekType(value: String): WeekType {
        return when (value.trim()) {
            "单周" -> WeekType.ODD
            "双周" -> WeekType.EVEN
            "自定义" -> WeekType.CUSTOM
            else -> WeekType.ALL
        }
    }

    private fun parsePersonType(value: String): PersonType {
        return when (value.trim()) {
            "Ta", "TA", "ta", "A", "a", "人员A" -> PersonType.PERSON_A
            else -> PersonType.PERSON_B
        }
    }
}
