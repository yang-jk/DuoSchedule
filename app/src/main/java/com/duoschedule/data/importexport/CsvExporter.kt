package com.duoschedule.data.importexport

import android.content.Context
import android.net.Uri
import com.duoschedule.data.model.Course
import com.duoschedule.data.model.PersonType
import com.duoschedule.data.model.WeekType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.charset.Charset
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object CsvExporter {
    private const val CSV_HEADER = "课程名称,星期,开始时间,结束时间,地点,教师,周次类型,开始周,结束周,自定义周次,所属人"
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
                    writer.write("\uFEFF")
                    writeExportContent(writer, courses, settingsA, settingsB, personAName, personBName)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun exportToFile(
        file: File,
        courses: List<Course>,
        settingsA: ScheduleSettingsExport?,
        settingsB: ScheduleSettingsExport?,
        personAName: String,
        personBName: String
    ) {
        file.bufferedWriter(Charsets.UTF_8).use { writer ->
            writer.write("\uFEFF")
            writeExportContent(writer, courses, settingsA, settingsB, personAName, personBName)
        }
    }

    private fun writeExportContent(
        writer: java.io.Writer,
        courses: List<Course>,
        settingsA: ScheduleSettingsExport?,
        settingsB: ScheduleSettingsExport?,
        personAName: String,
        personBName: String
    ) {
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
            val personStr = if (course.personType == PersonType.PERSON_A) personAName else personBName
            val dayStr = getDayOfWeekString(course.dayOfWeek)
            
            writer.append("${escapeCsvField(course.name)},")
            writer.append("$dayStr,")
            writer.append("${course.getStartTimeString()},")
            writer.append("${course.getEndTimeString()},")
            writer.append("${escapeCsvField(course.location)},")
            writer.append("${escapeCsvField(course.teacher)},")
            writer.append("$weekTypeStr,")
            writer.append("${course.startWeek},")
            writer.append("${course.endWeek},")
            writer.append("${escapeCsvField(course.customWeeks)},")
            writer.append("$personStr\n")
        }
    }

    suspend fun importFromCsv(
        context: Context,
        uri: Uri,
        targetPerson: PersonType? = null,
        periodTimes: List<String> = emptyList(),
        totalWeeks: Int = 16
    ): ImportResult = withContext(Dispatchers.IO) {
        try {
            val courses = mutableListOf<CourseImportData>()
            val errors = mutableListOf<String>()
            var lineNumber = 0
            var currentSection = ""
            var personAName: String? = null
            var personBName: String? = null
            val settingsAMap = mutableMapOf<String, String>()
            val settingsBMap = mutableMapOf<String, String>()
            var isTemplateFormat = false
            var isAppExport = false

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val bytes = inputStream.readBytes()
                val charset = detectCharset(bytes)
                
                var content = try {
                    String(bytes, charset)
                } catch (e: Exception) {
                    String(bytes, Charsets.UTF_8)
                }
                
                if (content.startsWith("\uFEFF")) {
                    content = content.substring(1)
                }
                
                val reader = BufferedReader(content.reader())
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    lineNumber++
                    val lineStr = line!!.trim()
                    
                    if (lineStr.isEmpty() || lineStr.startsWith("#")) {
                        when {
                            lineStr.contains("双人课程表导出文件") -> isAppExport = true
                            lineStr.contains("版本:") -> isAppExport = true
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
                                val settings = if (currentSection == "settingsA") settingsAMap else settingsBMap
                                settings[parts[0]] = parts[1]
                            }
                        }
                        "courses", "" -> {
                            val result = if (isTemplateFormat) {
                                parseTemplateLine(lineStr, lineNumber, targetPerson, periodTimes, totalWeeks)
                            } else {
                                parseCourseLine(lineStr, lineNumber, targetPerson, personAName, personBName)
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
                val fileType = if (isAppExport) CsvFileType.APP_EXPORT else CsvFileType.TEMPLATE
                
                val settingsA = if (isAppExport && settingsAMap.isNotEmpty()) {
                    ScheduleSettingsExport(
                        semesterStartDate = settingsAMap["开学时间"]?.let { 
                            LocalDate.parse(it).toEpochDay() 
                        } ?: LocalDate.now().toEpochDay(),
                        totalWeeks = settingsAMap["学期总周数"]?.toIntOrNull() ?: 16,
                        currentWeek = settingsAMap["当前周次"]?.toIntOrNull() ?: 1,
                        totalPeriods = settingsAMap["每天节数"]?.toIntOrNull() ?: 5,
                        periodTimes = settingsAMap["课程时间"]?.split(";")?.filter { it.isNotBlank() } ?: emptyList()
                    )
                } else null
                
                val settingsB = if (isAppExport && settingsBMap.isNotEmpty()) {
                    ScheduleSettingsExport(
                        semesterStartDate = settingsBMap["开学时间"]?.let { 
                            LocalDate.parse(it).toEpochDay() 
                        } ?: LocalDate.now().toEpochDay(),
                        totalWeeks = settingsBMap["学期总周数"]?.toIntOrNull() ?: 16,
                        currentWeek = settingsBMap["当前周次"]?.toIntOrNull() ?: 1,
                        totalPeriods = settingsBMap["每天节数"]?.toIntOrNull() ?: 5,
                        periodTimes = settingsBMap["课程时间"]?.split(";")?.filter { it.isNotBlank() } ?: emptyList()
                    )
                } else null
                
                ImportResult(
                    success = true,
                    importedCount = courses.size,
                    failedCount = errors.size,
                    errors = errors,
                    courses = courses,
                    fileType = fileType,
                    settingsA = settingsA,
                    settingsB = settingsB,
                    personAName = personAName,
                    personBName = personBName
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
        targetPerson: PersonType?,
        personAName: String? = null,
        personBName: String? = null
    ): Pair<CourseImportData?, String> {
        return try {
            val parts = parseCsvLine(line)
            if (parts.size < 11) {
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
            val teacher = parts[5]
            val weekType = parseWeekType(parts[6])
            val startWeek = parts[7].toIntOrNull() ?: 1
            val endWeek = parts[8].toIntOrNull() ?: 16
            val customWeeks = parts[9]
            
            val personType = targetPerson ?: parsePersonType(parts[10], personAName, personBName)

            Pair(
                CourseImportData(
                    name = name,
                    location = location,
                    teacher = teacher,
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
        periodTimes: List<String>,
        totalWeeks: Int
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

            val (weekType, startWeek, endWeek, customWeeks) = parseWeekString(weekStr, totalWeeks)

            Pair(
                CourseImportData(
                    name = name,
                    location = location,
                    teacher = teacher,
                    dayOfWeek = dayOfWeek,
                    startHour = startTime.first,
                    startMinute = startTime.second,
                    endHour = endTime.first,
                    endMinute = endTime.second,
                    weekType = weekType,
                    startWeek = startWeek,
                    endWeek = endWeek,
                    customWeeks = customWeeks,
                    personType = targetPerson ?: PersonType.PERSON_B,
                    startPeriod = startPeriod,
                    endPeriod = endPeriod
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
        val defaultPeriodTimes = listOf(
            "08:00-08:45",
            "08:55-09:40",
            "10:00-10:45",
            "10:55-11:40",
            "14:00-14:45",
            "14:55-15:40",
            "16:00-16:45",
            "16:55-17:40",
            "19:00-19:45",
            "19:55-20:40"
        )
        
        val effectivePeriodTimes = if (periodTimes.isEmpty()) {
            defaultPeriodTimes
        } else {
            val maxPeriod = maxOf(startPeriod, endPeriod)
            if (periodTimes.size < maxPeriod) {
                val extended = periodTimes.toMutableList()
                for (i in periodTimes.size until maxPeriod) {
                    extended.add(defaultPeriodTimes.getOrElse(i) { "08:00-08:45" })
                }
                extended
            } else {
                periodTimes
            }
        }
        
        val startIndex = (startPeriod - 1).coerceIn(0, effectivePeriodTimes.size - 1)
        val endIndex = (endPeriod - 1).coerceIn(0, effectivePeriodTimes.size - 1)

        val startTime = parseTime(effectivePeriodTimes[startIndex].split("-").firstOrNull() ?: "08:00")
        val endTime = parseTime(effectivePeriodTimes[endIndex].split("-").lastOrNull() ?: "08:45")

        return Pair(
            startTime ?: Pair(8, 0),
            endTime ?: Pair(8, 45)
        )
    }

    private fun parseWeekString(weekStr: String, totalWeeks: Int = 16): Tuple4<WeekType, Int, Int, String> {
        var trimmed = weekStr.trim()
        
        if (trimmed.contains("月") && trimmed.contains("日")) {
            return Tuple4(WeekType.ALL, 1, totalWeeks, "")
        }
        
        trimmed = trimmed.replace("周", "").replace("星期", "").trim()
        
        when {
            trimmed == "单" -> return Tuple4(WeekType.ODD, 1, totalWeeks, "")
            trimmed == "双" -> return Tuple4(WeekType.EVEN, 1, totalWeeks, "")
            trimmed.isEmpty() -> return Tuple4(WeekType.ALL, 1, totalWeeks, "")
        }

        val allWeeks = mutableListOf<Int>()
        
        val segments = trimmed.split("、", ",", "，")
        
        for (segment in segments) {
            val seg = segment.trim()
            if (seg.isEmpty()) continue
            
            if (seg.contains("-") || seg.contains("—") || seg.contains("~") || seg.contains("～")) {
                val rangeParts = seg.split("-", "—", "~", "～")
                if (rangeParts.size >= 2) {
                    val start = rangeParts[0].trim().toIntOrNull()
                    val end = rangeParts[1].trim().toIntOrNull()
                    if (start != null && end != null && start <= end) {
                        allWeeks.addAll(start..end)
                    }
                }
            } else {
                val weekNum = seg.toIntOrNull()
                if (weekNum != null && weekNum > 0) {
                    allWeeks.add(weekNum)
                }
            }
        }
        
        if (allWeeks.isNotEmpty()) {
            val uniqueWeeks = allWeeks.distinct().sorted()
            val minWeek = uniqueWeeks.first()
            val maxWeek = uniqueWeeks.last()
            
            val isContinuous = uniqueWeeks == (minWeek..maxWeek).toList()
            
            return if (isContinuous && uniqueWeeks.size > 1) {
                Tuple4(WeekType.ALL, minWeek, maxWeek, "")
            } else if (uniqueWeeks.size == 1) {
                Tuple4(WeekType.ALL, minWeek, minWeek, "")
            } else {
                Tuple4(WeekType.CUSTOM, minWeek, maxWeek, uniqueWeeks.joinToString(","))
            }
        }

        if (trimmed.contains("-")) {
            val rangeParts = trimmed.split("-")
            if (rangeParts.size == 2) {
                val start = rangeParts[0].trim().toIntOrNull()
                val end = rangeParts[1].trim().toIntOrNull()
                if (start != null && end != null && start <= end) {
                    return Tuple4(WeekType.ALL, start, end, "")
                }
            }
        }

        if (trimmed.contains(",")) {
            val weeks = trimmed.split(",").mapNotNull { it.trim().toIntOrNull() }
            if (weeks.isNotEmpty()) {
                return Tuple4(WeekType.CUSTOM, weeks.minOrNull()!!, weeks.maxOrNull()!!, weeks.joinToString(","))
            }
        }

        val singleWeek = trimmed.toIntOrNull()
        if (singleWeek != null) {
            return Tuple4(WeekType.ALL, singleWeek, singleWeek, "")
        }

        return Tuple4(WeekType.ALL, 1, totalWeeks, "")
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

    private fun parsePersonType(value: String, personAName: String? = null, personBName: String? = null): PersonType {
        val trimmed = value.trim()
        if (personAName != null && trimmed == personAName) {
            return PersonType.PERSON_A
        }
        if (personBName != null && trimmed == personBName) {
            return PersonType.PERSON_B
        }
        return when (trimmed) {
            "Ta", "TA", "ta", "A", "a", "人员A" -> PersonType.PERSON_A
            else -> PersonType.PERSON_B
        }
    }
}
