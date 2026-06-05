package com.duoschedule.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.content.pm.PackageManager
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.Binder
import android.util.Log
import com.duoschedule.data.local.AppDatabase
import com.duoschedule.data.local.SettingsDataStore
import com.duoschedule.data.model.PersonType
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class ScheduleContentProvider : ContentProvider() {

    companion object {
        private const val TAG = "ScheduleContentProvider"
        private const val AUTHORITY = "com.duoschedule.provider"
        
        private const val CODE_TODAY_COURSES = 1
        private const val CODE_TOMORROW_COURSES = 2
        private const val CODE_INIT_STATUS = 3
        private const val CODE_VIVO_INTENT = 4
        private const val CODE_MAML_SCHEDULE = 10
        private const val CODE_MAML_MY_COURSES = 11
        private const val CODE_MAML_TA_COURSES = 12
        private const val CODE_MAML_FREE_TIME = 13
        
        private val URI_MATCHER = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, "today", CODE_TODAY_COURSES)
            addURI(AUTHORITY, "tomorrow", CODE_TOMORROW_COURSES)
            addURI(AUTHORITY, "init", CODE_INIT_STATUS)
            addURI(AUTHORITY, "vivo_intent", CODE_VIVO_INTENT)
            addURI(AUTHORITY, "maml/schedule", CODE_MAML_SCHEDULE)
            addURI(AUTHORITY, "maml/my_courses", CODE_MAML_MY_COURSES)
            addURI(AUTHORITY, "maml/ta_courses", CODE_MAML_TA_COURSES)
            addURI(AUTHORITY, "maml/free_time", CODE_MAML_FREE_TIME)
        }
        
        private val ALLOWED_PACKAGES = setOf(
            "com.wakeup.schedule.card",
            "com.wakeup.hm",
            "com.wakeup.schedule.honorcard",
            "com.hihonor.quickengine",
            "com.coloros.assistantscreen",
            "com.android.launcher",
            "com.oplus.metis",
            "com.oplus.pantanal.ums",
            "com.vivo.aiengine",
            "com.miui.personalassistant",
            "com.miui.home",
            "com.duoschedule"
        )
    }

    interface ProviderDependencies {
        val database: AppDatabase
        val settingsDataStore: SettingsDataStore
    }

    override fun onCreate(): Boolean {
        return true
    }

    override fun getType(uri: Uri): String? {
        return "vnd.android.cursor.item/vnd.com.duoschedule.schedule"
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        val context = context ?: return null
        
        if (!isCallingPackageAllowed(context)) {
            Log.w(TAG, "Unauthorized access attempt from: ${getCallingPackage(context)}")
            return createErrorCursor(2001, "Unauthorized")
        }

        val dependencies = try {
            EntryPointAccessors.fromApplication(
                context.applicationContext,
                ProviderDependencies::class.java
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get dependencies", e)
            return createErrorCursor(2002, "Internal error")
        }

        return runBlocking {
            try {
                when (URI_MATCHER.match(uri)) {
                    CODE_TODAY_COURSES -> queryCourses(dependencies, false)
                    CODE_TOMORROW_COURSES -> queryCourses(dependencies, true)
                    CODE_INIT_STATUS -> queryInitStatus(dependencies)
                    CODE_VIVO_INTENT -> queryVivoIntent(dependencies)
                    CODE_MAML_SCHEDULE -> queryMamlSchedule(dependencies)
                    CODE_MAML_MY_COURSES -> queryMamlMyCourses(dependencies)
                    CODE_MAML_TA_COURSES -> queryMamlTaCourses(dependencies)
                    CODE_MAML_FREE_TIME -> queryMamlFreeTime(dependencies)
                    else -> {
                        Log.w(TAG, "Unknown URI: $uri")
                        null
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Query failed", e)
                createErrorCursor(2003, e.message ?: "Unknown error")
            }
        }
    }

    private suspend fun queryCourses(dependencies: ProviderDependencies, isTomorrow: Boolean): Cursor {
        val database = dependencies.database
        val settingsDataStore = dependencies.settingsDataStore
        
        val currentWeekA = settingsDataStore.getCurrentWeek(PersonType.PERSON_A).first()
        val currentWeekB = settingsDataStore.getCurrentWeek(PersonType.PERSON_B).first()
        
        val targetDate = if (isTomorrow) {
            LocalDate.now().plusDays(1)
        } else {
            LocalDate.now()
        }
        val dayOfWeek = targetDate.dayOfWeek.value
        
        val currentTime = LocalDateTime.now()
        val currentTimeStr = String.format("%02d:%02d", currentTime.hour, currentTime.minute)
        
        val allCourses = database.courseDao().getCoursesForDaySync(dayOfWeek, PersonType.PERSON_A) +
                         database.courseDao().getCoursesForDaySync(dayOfWeek, PersonType.PERSON_B)
        
        val courses = allCourses.filter { course ->
            val currentWeek = if (course.personType == PersonType.PERSON_A) currentWeekA else currentWeekB
            course.isInWeek(currentWeek) && (isTomorrow || course.getEndTimeString() > currentTimeStr)
        }
        
        val cursor = MatrixCursor(arrayOf("code", "data"))
        val jsonData = buildCoursesJson(courses, targetDate)
        cursor.addRow(arrayOf<Any>(0, jsonData))
        return cursor
    }

    private fun buildCoursesJson(courses: List<com.duoschedule.data.model.Course>, targetDate: LocalDate): String {
        val sb = StringBuilder()
        sb.append("{\"courses\":[")
        
        courses.forEachIndexed { index, course ->
            if (index > 0) sb.append(",")
            
            val startDateTime = targetDate.atTime(course.startHour, course.startMinute)
            val endDateTime = targetDate.atTime(course.endHour, course.endMinute)
            
            val startTimestamp = startDateTime.atZone(ZoneId.systemDefault()).toEpochSecond()
            val endTimestamp = endDateTime.atZone(ZoneId.systemDefault()).toEpochSecond()
            
            sb.append("{")
            sb.append("\"id\":${course.id},")
            sb.append("\"courseName\":\"${escapeJson(course.name)}\",")
            sb.append("\"room\":\"${escapeJson(course.location)}\",")
            sb.append("\"teacher\":\"${escapeJson(course.teacher)}\",")
            sb.append("\"startTime\":\"${course.getStartTimeString()}\",")
            sb.append("\"endTime\":\"${course.getEndTimeString()}\",")
            sb.append("\"startTimestamp\":$startTimestamp,")
            sb.append("\"endTimestamp\":$endTimestamp,")
            sb.append("\"personType\":\"${course.personType.name}\"")
            sb.append("}")
        }
        
        sb.append("]}")
        return sb.toString()
    }

    private suspend fun queryInitStatus(dependencies: ProviderDependencies): Cursor {
        val database = dependencies.database
        val courseCount = database.courseDao().getCourseCountByPerson(PersonType.PERSON_A).first() +
                          database.courseDao().getCourseCountByPerson(PersonType.PERSON_B).first()
        
        val hasInit = courseCount > 0
        
        val cursor = MatrixCursor(arrayOf("code", "data"))
        cursor.addRow(arrayOf<Any>(0, "{\"has_init\":$hasInit}"))
        return cursor
    }

    private suspend fun queryVivoIntent(dependencies: ProviderDependencies): Cursor {
        val cursor = MatrixCursor(arrayOf("code", "data"))
        val jsonData = "{\"action\":\"查看课程表\",\"target\":{\"package\":\"com.duoschedule\"}}"
        cursor.addRow(arrayOf<Any>(0, jsonData))
        return cursor
    }

    private suspend fun queryMamlSchedule(dependencies: ProviderDependencies): Cursor {
        val database = dependencies.database
        val settingsDataStore = dependencies.settingsDataStore

        val currentWeekA = settingsDataStore.getCurrentWeek(PersonType.PERSON_A).first()
        val currentWeekB = settingsDataStore.getCurrentWeek(PersonType.PERSON_B).first()
        val personAName = settingsDataStore.personAName.first()
        val personBName = settingsDataStore.personBName.first()

        val dayOfWeek = LocalDate.now().dayOfWeek.value
        val currentHour = LocalDateTime.now().hour
        val currentMinute = LocalDateTime.now().minute

        val personACourses = database.courseDao().getCoursesForDaySync(dayOfWeek, PersonType.PERSON_A)
            .filter { it.isInWeek(currentWeekA) }
        val personBCourses = database.courseDao().getCoursesForDaySync(dayOfWeek, PersonType.PERSON_B)
            .filter { it.isInWeek(currentWeekB) }

        val currentCourseA = personACourses.find { it.isOngoing(currentHour, currentMinute, currentWeekA) }
        val currentCourseB = personBCourses.find { it.isOngoing(currentHour, currentMinute, currentWeekB) }

        val cursor = MatrixCursor(arrayOf(
            "week", "person_a_name", "person_a_course", "person_a_time", "person_a_remaining",
            "person_b_name", "person_b_course", "person_b_time", "person_b_remaining"
        ))

        val personACourseName = currentCourseA?.name ?: "无课"
        val personATime = if (currentCourseA != null) {
            val remaining = currentCourseA.getRemainingMinutes(currentHour, currentMinute)
            val periodText = if (currentCourseA.isCustomTime) {
                currentCourseA.getTimeString()
            } else if (currentCourseA.startPeriod == currentCourseA.endPeriod) {
                "第${currentCourseA.startPeriod}节"
            } else {
                "第${currentCourseA.startPeriod}-${currentCourseA.endPeriod}节"
            }
            "$periodText · 剩余${remaining}分钟"
        } else {
            ""
        }
        val personARemaining = currentCourseA?.getRemainingMinutes(currentHour, currentMinute) ?: 0

        val personBCourseName = currentCourseB?.name ?: "无课"
        val personBTime = if (currentCourseB != null) {
            val remaining = currentCourseB.getRemainingMinutes(currentHour, currentMinute)
            val periodText = if (currentCourseB.isCustomTime) {
                currentCourseB.getTimeString()
            } else if (currentCourseB.startPeriod == currentCourseB.endPeriod) {
                "第${currentCourseB.startPeriod}节"
            } else {
                "第${currentCourseB.startPeriod}-${currentCourseB.endPeriod}节"
            }
            "$periodText · 剩余${remaining}分钟"
        } else {
            ""
        }
        val personBRemaining = currentCourseB?.getRemainingMinutes(currentHour, currentMinute) ?: 0

        cursor.addRow(arrayOf<Any>(
            currentWeekA, personAName, personACourseName, personATime, personARemaining,
            personBName, personBCourseName, personBTime, personBRemaining
        ))
        return cursor
    }

    private suspend fun queryMamlMyCourses(dependencies: ProviderDependencies): Cursor {
        return queryMamlPersonCourses(dependencies, PersonType.PERSON_A)
    }

    private suspend fun queryMamlTaCourses(dependencies: ProviderDependencies): Cursor {
        return queryMamlPersonCourses(dependencies, PersonType.PERSON_B)
    }

    private suspend fun queryMamlPersonCourses(dependencies: ProviderDependencies, personType: PersonType): Cursor {
        val database = dependencies.database
        val settingsDataStore = dependencies.settingsDataStore

        val currentWeek = settingsDataStore.getCurrentWeek(personType).first()
        val personName = if (personType == PersonType.PERSON_A) {
            settingsDataStore.personAName.first()
        } else {
            settingsDataStore.personBName.first()
        }

        val dayOfWeek = LocalDate.now().dayOfWeek.value
        val currentHour = LocalDateTime.now().hour
        val currentMinute = LocalDateTime.now().minute

        val courses = database.courseDao().getCoursesForDaySync(dayOfWeek, personType)
            .filter { it.isInWeek(currentWeek) }
            .sortedBy { it.startHour * 60 + it.startMinute }
            .take(4)

        val cursor = MatrixCursor(arrayOf(
            "week", "person_name",
            "course_1_name", "course_1_location", "course_1_period", "course_1_ended",
            "course_2_name", "course_2_location", "course_2_period", "course_2_ended",
            "course_3_name", "course_3_location", "course_3_period", "course_3_ended",
            "course_4_name", "course_4_location", "course_4_period", "course_4_ended"
        ))

        val row = mutableListOf<Any>(currentWeek, personName)
        for (i in 0 until 4) {
            val course = courses.getOrNull(i)
            if (course != null) {
                val periodText = if (course.isCustomTime) {
                    course.getTimeString()
                } else if (course.startPeriod == course.endPeriod) {
                    "第${course.startPeriod}节"
                } else {
                    "第${course.startPeriod}-${course.endPeriod}节"
                }
                val ended = if (course.hasEnded(currentHour, currentMinute)) 1 else 0
                row.add(course.name)
                row.add(course.location)
                row.add(periodText)
                row.add(ended)
            } else {
                row.add("")
                row.add("")
                row.add("")
                row.add(0)
            }
        }

        cursor.addRow(row.toTypedArray())
        return cursor
    }

    private suspend fun queryMamlFreeTime(dependencies: ProviderDependencies): Cursor {
        val database = dependencies.database
        val settingsDataStore = dependencies.settingsDataStore

        val currentWeekA = settingsDataStore.getCurrentWeek(PersonType.PERSON_A).first()
        val currentWeekB = settingsDataStore.getCurrentWeek(PersonType.PERSON_B).first()

        val dayOfWeek = LocalDate.now().dayOfWeek.value
        val currentHour = LocalDateTime.now().hour
        val currentMinute = LocalDateTime.now().minute

        val personACourses = database.courseDao().getCoursesForDaySync(dayOfWeek, PersonType.PERSON_A)
            .filter { it.isInWeek(currentWeekA) }
        val personBCourses = database.courseDao().getCoursesForDaySync(dayOfWeek, PersonType.PERSON_B)
            .filter { it.isInWeek(currentWeekB) }

        val freeTimeSlots = calculateFreeTimeSlots(personACourses, personBCourses, currentHour, currentMinute)

        val cursor = MatrixCursor(arrayOf("free_time_text", "free_time_label"))
        if (freeTimeSlots.isNotEmpty()) {
            cursor.addRow(arrayOf<Any>(freeTimeSlots.first(), "空闲"))
        } else {
            cursor.addRow(arrayOf<Any>("无空闲时间", ""))
        }
        return cursor
    }

    private fun calculateFreeTimeSlots(
        personACourses: List<com.duoschedule.data.model.Course>,
        personBCourses: List<com.duoschedule.data.model.Course>,
        currentHour: Int,
        currentMinute: Int
    ): List<String> {
        val freeSlots = mutableListOf<String>()
        val currentMinutes = currentHour * 60 + currentMinute

        val allPeriods = listOf(
            Pair(8 * 60, 9 * 60 + 40),
            Pair(10 * 60, 11 * 60 + 40),
            Pair(14 * 60, 15 * 60 + 40),
            Pair(16 * 60, 17 * 60 + 40),
            Pair(19 * 60, 20 * 60 + 40)
        )

        for ((start, end) in allPeriods) {
            if (start < currentMinutes) continue

            val aHasCourse = personACourses.any { course ->
                val courseStart = course.startHour * 60 + course.startMinute
                val courseEnd = course.endHour * 60 + course.endMinute
                start < courseEnd && end > courseStart
            }

            val bHasCourse = personBCourses.any { course ->
                val courseStart = course.startHour * 60 + course.startMinute
                val courseEnd = course.endHour * 60 + course.endMinute
                start < courseEnd && end > courseStart
            }

            if (!aHasCourse && !bHasCourse) {
                val startHour = start / 60
                val startMin = start % 60
                val endHour = end / 60
                val endMin = end % 60
                freeSlots.add(String.format("%02d:%02d-%02d:%02d", startHour, startMin, endHour, endMin))
            }
        }

        return freeSlots
    }

    private fun createErrorCursor(code: Int, message: String): Cursor {
        val cursor = MatrixCursor(arrayOf("code", "data"))
        cursor.addRow(arrayOf<Any>(code, "{\"error\":\"$message\"}"))
        return cursor
    }

    private fun escapeJson(str: String): String {
        return str.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    private fun isCallingPackageAllowed(context: Context): Boolean {
        val callingPackage = getCallingPackage(context) ?: return false
        return ALLOWED_PACKAGES.any { allowed ->
            callingPackage == allowed || context.packageName == callingPackage
        }
    }

    private fun getCallingPackage(context: Context): String? {
        val callingPackage = callingPackage ?: return null
        
        try {
            val packagesForUid = context.packageManager.getPackagesForUid(Binder.getCallingUid())
            return packagesForUid?.firstOrNull()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get calling package", e)
            return callingPackage
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
}
