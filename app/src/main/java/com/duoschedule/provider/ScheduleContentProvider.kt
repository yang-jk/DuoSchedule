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
        
        private val URI_MATCHER = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, "today", CODE_TODAY_COURSES)
            addURI(AUTHORITY, "tomorrow", CODE_TOMORROW_COURSES)
            addURI(AUTHORITY, "init", CODE_INIT_STATUS)
            addURI(AUTHORITY, "vivo_intent", CODE_VIVO_INTENT)
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
