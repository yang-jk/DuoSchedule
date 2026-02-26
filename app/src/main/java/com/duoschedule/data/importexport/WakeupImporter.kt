package com.duoschedule.data.importexport

import android.content.Context
import android.net.Uri
import com.duoschedule.data.model.PersonType
import com.duoschedule.data.model.WeekType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

object WakeupImporter {
    suspend fun importFromWakeup(
        context: Context,
        uri: Uri,
        targetPerson: PersonType
    ): ImportResult = withContext(Dispatchers.IO) {
        try {
            val courses = mutableListOf<CourseImportData>()
            val errors = mutableListOf<String>()

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val content = BufferedReader(InputStreamReader(inputStream, "UTF-8")).readText()
                
                val json = try {
                    JSONObject(content)
                } catch (e: Exception) {
                    return@withContext ImportResult(
                        success = false,
                        errors = listOf("不是有效的Wakeup备份文件格式")
                    )
                }

                if (json.has("courses")) {
                    val coursesArray = json.getJSONArray("courses")
                    for (i in 0 until coursesArray.length()) {
                        val courseJson = coursesArray.getJSONObject(i)
                        val result = parseWakeupCourse(courseJson, i + 1, targetPerson)
                        if (result.first != null) {
                            courses.add(result.first!!)
                        } else if (result.second.isNotEmpty()) {
                            errors.add(result.second)
                        }
                    }
                } else if (json.has("data")) {
                    val dataJson = json.getJSONObject("data")
                    if (dataJson.has("courses")) {
                        val coursesArray = dataJson.getJSONArray("courses")
                        for (i in 0 until coursesArray.length()) {
                            val courseJson = coursesArray.getJSONObject(i)
                            val result = parseWakeupCourse(courseJson, i + 1, targetPerson)
                            if (result.first != null) {
                                courses.add(result.first!!)
                            } else if (result.second.isNotEmpty()) {
                                errors.add(result.second)
                            }
                        }
                    }
                } else {
                    for (key in json.keys()) {
                        try {
                            val item = json.getJSONObject(key)
                            if (item.has("courseName") || item.has("name")) {
                                val result = parseWakeupCourse(item, courses.size + 1, targetPerson)
                                if (result.first != null) {
                                    courses.add(result.first!!)
                                }
                            }
                        } catch (e: Exception) {
                        }
                    }
                }
            }

            if (courses.isEmpty() && errors.isEmpty()) {
                ImportResult(
                    success = false,
                    errors = listOf("未找到课程数据")
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
                errors = listOf("读取Wakeup文件失败: ${e.message}")
            )
        }
    }

    private fun parseWakeupCourse(
        json: JSONObject,
        index: Int,
        targetPerson: PersonType
    ): Pair<CourseImportData?, String> {
        return try {
            val name = json.optString("courseName", json.optString("name", ""))
            if (name.isBlank()) {
                return Pair(null, "第${index}条: 课程名称为空")
            }

            val location = json.optString("location", json.optString("room", ""))
            
            val dayOfWeek = json.optInt("dayOfWeek", json.optInt("day", 1))
            val day = if (dayOfWeek in 1..7) dayOfWeek else 1

            val startTimeStr = json.optString("startTime", json.optString("start_time", "08:00"))
            val endTimeStr = json.optString("endTime", json.optString("end_time", "09:40"))
            
            val startTime = parseTime(startTimeStr)
            val endTime = parseTime(endTimeStr)

            val weekType = when (json.optInt("weekType", 0)) {
                1 -> WeekType.ODD
                2 -> WeekType.EVEN
                else -> WeekType.ALL
            }

            val startWeek = json.optInt("startWeek", json.optInt("start_week", 1))
            val endWeek = json.optInt("endWeek", json.optInt("end_week", 16))
            
            val customWeeks = json.optString("weeks", json.optString("customWeeks", ""))

            Pair(
                CourseImportData(
                    name = name,
                    location = location,
                    dayOfWeek = day,
                    startHour = startTime.first,
                    startMinute = startTime.second,
                    endHour = endTime.first,
                    endMinute = endTime.second,
                    weekType = weekType,
                    startWeek = startWeek,
                    endWeek = endWeek,
                    customWeeks = customWeeks,
                    personType = targetPerson
                ),
                ""
            )
        } catch (e: Exception) {
            Pair(null, "第${index}条: 解析错误 - ${e.message}")
        }
    }

    private fun parseTime(value: String): Pair<Int, Int> {
        return try {
            val parts = value.trim().split(":")
            if (parts.size >= 2) {
                Pair(parts[0].toInt(), parts[1].toInt())
            } else {
                Pair(8, 0)
            }
        } catch (e: Exception) {
            Pair(8, 0)
        }
    }
}
