package com.duoschedule.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.duoschedule.data.model.PersonType
import com.duoschedule.data.model.ThemeMode
import com.duoschedule.data.model.TodayCourseDisplayMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val PERSON_A_NAME = stringPreferencesKey("person_a_name")
        private val PERSON_B_NAME = stringPreferencesKey("person_b_name")
        
        private val PERSON_A_SEMESTER_START_DATE = longPreferencesKey("person_a_semester_start_date")
        private val PERSON_B_SEMESTER_START_DATE = longPreferencesKey("person_b_semester_start_date")
        
        private val PERSON_A_TOTAL_WEEKS = intPreferencesKey("person_a_total_weeks")
        private val PERSON_B_TOTAL_WEEKS = intPreferencesKey("person_b_total_weeks")
        
        private val PERSON_A_CURRENT_WEEK = intPreferencesKey("person_a_current_week")
        private val PERSON_B_CURRENT_WEEK = intPreferencesKey("person_b_current_week")
        
        private val PERSON_A_TOTAL_PERIODS = intPreferencesKey("person_a_total_periods")
        private val PERSON_B_TOTAL_PERIODS = intPreferencesKey("person_b_total_periods")
        
        private val PERSON_A_PERIOD_TIMES = stringPreferencesKey("person_a_period_times")
        private val PERSON_B_PERIOD_TIMES = stringPreferencesKey("person_b_period_times")
        
        private val SHOW_NON_CURRENT_WEEK_COURSES = booleanPreferencesKey("show_non_current_week_courses")
        private val SHOW_SATURDAY = booleanPreferencesKey("show_saturday")
        private val SHOW_SUNDAY = booleanPreferencesKey("show_sunday")
        private val COURSE_CELL_HEIGHT = intPreferencesKey("course_cell_height")
        
        private val NOTIFICATION_ENABLED = booleanPreferencesKey("notification_enabled")
        private val NOTIFICATION_ADVANCE_TIME = intPreferencesKey("notification_advance_time")
        private val ISLAND_DISPLAY_MODE = stringPreferencesKey("island_display_mode")
        private val LIVE_NOTIFICATION_ENABLED = booleanPreferencesKey("live_notification_enabled")
        private val TODAY_COURSE_DISPLAY_MODE = stringPreferencesKey("today_course_display_mode")
        private val THEME_MODE = stringPreferencesKey("theme_mode")

        private val DEFAULT_PERIOD_TIMES = listOf(
            "08:00-08:45",
            "08:55-09:40",
            "10:00-10:45",
            "10:55-11:40",
            "14:00-14:45"
        )
    }

    val personAName: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PERSON_A_NAME] ?: "Ta"
    }

    val personBName: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PERSON_B_NAME] ?: "我"
    }

    fun getSemesterStartDate(personType: PersonType): Flow<LocalDate> = context.dataStore.data.map { preferences ->
        val key = if (personType == PersonType.PERSON_A) PERSON_A_SEMESTER_START_DATE else PERSON_B_SEMESTER_START_DATE
        preferences[key]?.let { LocalDate.ofEpochDay(it) } ?: LocalDate.now()
    }

    fun getTotalWeeks(personType: PersonType): Flow<Int> = context.dataStore.data.map { preferences ->
        val key = if (personType == PersonType.PERSON_A) PERSON_A_TOTAL_WEEKS else PERSON_B_TOTAL_WEEKS
        preferences[key] ?: 16
    }

    fun getCurrentWeek(personType: PersonType): Flow<Int> = context.dataStore.data.map { preferences ->
        val key = if (personType == PersonType.PERSON_A) PERSON_A_CURRENT_WEEK else PERSON_B_CURRENT_WEEK
        preferences[key] ?: 1
    }

    fun getTotalPeriods(personType: PersonType): Flow<Int> = context.dataStore.data.map { preferences ->
        val key = if (personType == PersonType.PERSON_A) PERSON_A_TOTAL_PERIODS else PERSON_B_TOTAL_PERIODS
        preferences[key] ?: 5
    }

    fun getPeriodTimes(personType: PersonType): Flow<List<String>> = context.dataStore.data.map { preferences ->
        val key = if (personType == PersonType.PERSON_A) PERSON_A_PERIOD_TIMES else PERSON_B_PERIOD_TIMES
        preferences[key]?.split("|") ?: DEFAULT_PERIOD_TIMES
    }

    val showNonCurrentWeekCourses: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SHOW_NON_CURRENT_WEEK_COURSES] ?: false
    }

    val showSaturday: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SHOW_SATURDAY] ?: true
    }

    val showSunday: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SHOW_SUNDAY] ?: false
    }

    val courseCellHeight: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[COURSE_CELL_HEIGHT] ?: 60
    }

    val notificationEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[NOTIFICATION_ENABLED] ?: true
    }

    val notificationAdvanceTime: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[NOTIFICATION_ADVANCE_TIME] ?: 10
    }

    val islandDisplayMode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[ISLAND_DISPLAY_MODE] ?: "BOTH"
    }

    val liveNotificationEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[LIVE_NOTIFICATION_ENABLED] ?: true
    }

    val todayCourseDisplayMode: Flow<TodayCourseDisplayMode> = context.dataStore.data.map { preferences ->
        val mode = preferences[TODAY_COURSE_DISPLAY_MODE] ?: "BOTH"
        try {
            TodayCourseDisplayMode.valueOf(mode)
        } catch (e: Exception) {
            TodayCourseDisplayMode.BOTH
        }
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        val mode = preferences[THEME_MODE] ?: "FOLLOW_SYSTEM"
        try {
            ThemeMode.valueOf(mode)
        } catch (e: Exception) {
            ThemeMode.FOLLOW_SYSTEM
        }
    }

    suspend fun getNotificationEnabled(): Boolean {
        return context.dataStore.data.map { preferences ->
            preferences[NOTIFICATION_ENABLED] ?: true
        }.first()
    }

    suspend fun getNotificationAdvanceTime(): Int {
        return context.dataStore.data.map { preferences ->
            preferences[NOTIFICATION_ADVANCE_TIME] ?: 10
        }.first()
    }

    suspend fun getIslandDisplayMode(): com.duoschedule.notification.IslandDisplayMode {
        return context.dataStore.data.map { preferences ->
            val mode = preferences[ISLAND_DISPLAY_MODE] ?: "BOTH"
            try {
                com.duoschedule.notification.IslandDisplayMode.valueOf(mode)
            } catch (e: Exception) {
                com.duoschedule.notification.IslandDisplayMode.BOTH
            }
        }.first()
    }

    suspend fun getLiveNotificationEnabled(): Boolean {
        return context.dataStore.data.map { preferences ->
            preferences[LIVE_NOTIFICATION_ENABLED] ?: true
        }.first()
    }

    suspend fun setPersonName(personType: PersonType, name: String) {
        context.dataStore.edit { preferences ->
            val key = if (personType == PersonType.PERSON_A) PERSON_A_NAME else PERSON_B_NAME
            preferences[key] = name
        }
    }

    suspend fun setSemesterStartDate(personType: PersonType, date: LocalDate) {
        context.dataStore.edit { preferences ->
            val key = if (personType == PersonType.PERSON_A) PERSON_A_SEMESTER_START_DATE else PERSON_B_SEMESTER_START_DATE
            preferences[key] = date.toEpochDay()
        }
    }

    suspend fun setTotalWeeks(personType: PersonType, weeks: Int) {
        context.dataStore.edit { preferences ->
            val key = if (personType == PersonType.PERSON_A) PERSON_A_TOTAL_WEEKS else PERSON_B_TOTAL_WEEKS
            preferences[key] = weeks
        }
    }

    suspend fun setCurrentWeek(personType: PersonType, week: Int) {
        context.dataStore.edit { preferences ->
            val key = if (personType == PersonType.PERSON_A) PERSON_A_CURRENT_WEEK else PERSON_B_CURRENT_WEEK
            preferences[key] = week
        }
    }

    suspend fun setTotalPeriods(personType: PersonType, periods: Int) {
        context.dataStore.edit { preferences ->
            val key = if (personType == PersonType.PERSON_A) PERSON_A_TOTAL_PERIODS else PERSON_B_TOTAL_PERIODS
            preferences[key] = periods
        }
    }

    suspend fun setPeriodTimes(personType: PersonType, times: List<String>) {
        context.dataStore.edit { preferences ->
            val key = if (personType == PersonType.PERSON_A) PERSON_A_PERIOD_TIMES else PERSON_B_PERIOD_TIMES
            preferences[key] = times.joinToString("|")
        }
    }

    suspend fun setShowNonCurrentWeekCourses(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SHOW_NON_CURRENT_WEEK_COURSES] = show
        }
    }

    suspend fun setShowSaturday(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SHOW_SATURDAY] = show
        }
    }

    suspend fun setShowSunday(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SHOW_SUNDAY] = show
        }
    }

    suspend fun setCourseCellHeight(height: Int) {
        context.dataStore.edit { preferences ->
            preferences[COURSE_CELL_HEIGHT] = height
        }
    }

    suspend fun setNotificationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATION_ENABLED] = enabled
        }
    }

    suspend fun setNotificationAdvanceTime(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATION_ADVANCE_TIME] = minutes
        }
    }

    suspend fun setIslandDisplayMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[ISLAND_DISPLAY_MODE] = mode
        }
    }

    suspend fun setLiveNotificationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[LIVE_NOTIFICATION_ENABLED] = enabled
        }
    }

    suspend fun setTodayCourseDisplayMode(mode: TodayCourseDisplayMode) {
        context.dataStore.edit { preferences ->
            preferences[TODAY_COURSE_DISPLAY_MODE] = mode.name
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode.name
        }
    }

    fun calculateCurrentWeek(startDate: LocalDate, totalWeeks: Int): Int {
        val days = ChronoUnit.DAYS.between(startDate, LocalDate.now())
        return ((days / 7) + 1).toInt().coerceIn(1, totalWeeks)
    }

    fun getWeekDates(semesterStartDate: LocalDate, currentWeek: Int): List<LocalDate> {
        val weekStart = semesterStartDate.plusWeeks((currentWeek - 1).toLong())
        return (0..6).map { weekStart.plusDays(it.toLong()) }
    }
}
