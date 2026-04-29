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
import com.duoschedule.notification.SilentModeType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
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
        private val SHOW_DASHED_BORDER = booleanPreferencesKey("show_dashed_border")
        
        private val NOTIFICATION_ENABLED = booleanPreferencesKey("notification_enabled")
        private val NOTIFICATION_ADVANCE_TIME = intPreferencesKey("notification_advance_time")
        private val LIVE_NOTIFICATION_ENABLED = booleanPreferencesKey("live_notification_enabled")
        private val TODAY_COURSE_DISPLAY_MODE = stringPreferencesKey("today_course_display_mode")
        private val THEME_MODE = stringPreferencesKey("theme_mode")
        
        private val AUTO_SILENT_ENABLED = booleanPreferencesKey("auto_silent_enabled")
        private val AUTO_SILENT_MODE_TYPE = stringPreferencesKey("auto_silent_mode_type")
        private val AUTO_SILENT_ADVANCE_TIME = intPreferencesKey("auto_silent_advance_time")
        
        private val COURSE_NAME_FONT_SIZE = intPreferencesKey("course_name_font_size")
        private val COURSE_LOCATION_FONT_SIZE = intPreferencesKey("course_location_font_size")

        private val DEFAULT_PERIOD_TIMES = listOf(
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

    val showDashedBorder: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SHOW_DASHED_BORDER] ?: true
    }

    val notificationEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[NOTIFICATION_ENABLED] ?: true
    }

    val notificationAdvanceTime: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[NOTIFICATION_ADVANCE_TIME] ?: 10
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

    val autoSilentEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[AUTO_SILENT_ENABLED] ?: true
    }

    val autoSilentModeType: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[AUTO_SILENT_MODE_TYPE] ?: "VIBRATE"
    }

    val autoSilentAdvanceTime: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[AUTO_SILENT_ADVANCE_TIME] ?: 5
    }

    val courseNameFontSize: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[COURSE_NAME_FONT_SIZE] ?: 12
    }

    val courseLocationFontSize: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[COURSE_LOCATION_FONT_SIZE] ?: 11
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

    suspend fun setShowDashedBorder(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SHOW_DASHED_BORDER] = show
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

    suspend fun getAutoSilentEnabled(): Boolean {
        return context.dataStore.data.map { preferences ->
            preferences[AUTO_SILENT_ENABLED] ?: true
        }.first()
    }

    suspend fun getAutoSilentModeType(): SilentModeType {
        return context.dataStore.data.map { preferences ->
            val mode = preferences[AUTO_SILENT_MODE_TYPE] ?: "VIBRATE"
            try {
                SilentModeType.valueOf(mode)
            } catch (e: Exception) {
                SilentModeType.VIBRATE
            }
        }.first()
    }

    suspend fun setAutoSilentEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_SILENT_ENABLED] = enabled
        }
    }

    suspend fun setAutoSilentModeType(modeType: SilentModeType) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_SILENT_MODE_TYPE] = modeType.name
        }
    }

    suspend fun getAutoSilentAdvanceTime(): Int {
        return context.dataStore.data.map { preferences ->
            preferences[AUTO_SILENT_ADVANCE_TIME] ?: 5
        }.first()
    }

    suspend fun setAutoSilentAdvanceTime(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_SILENT_ADVANCE_TIME] = minutes
        }
    }

    suspend fun setCourseNameFontSize(size: Int) {
        context.dataStore.edit { preferences ->
            preferences[COURSE_NAME_FONT_SIZE] = size
        }
    }

    suspend fun setCourseLocationFontSize(size: Int) {
        context.dataStore.edit { preferences ->
            preferences[COURSE_LOCATION_FONT_SIZE] = size
        }
    }

    fun calculateCurrentWeek(startDate: LocalDate, totalWeeks: Int): Int {
        // 找到开学日期所在周的周一，作为第一周的起点
        val firstWeekMonday = startDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        // 计算从第一个周一到今天经过了多少天
        val days = ChronoUnit.DAYS.between(firstWeekMonday, LocalDate.now())
        // 计算当前周次（至少为 1，最多为 totalWeeks）
        return ((days / 7) + 1).toInt().coerceIn(1, totalWeeks)
    }

    fun getWeekDates(semesterStartDate: LocalDate, currentWeek: Int): List<LocalDate> {
        val firstWeekMonday = semesterStartDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val currentWeekMonday = firstWeekMonday.plusWeeks((currentWeek - 1).toLong())
        return (0..6).map { currentWeekMonday.plusDays(it.toLong()) }
    }
}
