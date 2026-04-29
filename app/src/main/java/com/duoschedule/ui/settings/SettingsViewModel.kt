package com.duoschedule.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duoschedule.data.importexport.*
import com.duoschedule.data.model.Course
import com.duoschedule.data.model.PersonType
import com.duoschedule.data.model.ThemeMode
import com.duoschedule.data.model.TodayCourseDisplayMode
import com.duoschedule.data.repository.CourseRepository
import com.duoschedule.notification.CourseNotificationManager
import com.duoschedule.notification.SilentModeType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: CourseRepository,
    private val notificationManager: CourseNotificationManager
) : ViewModel() {

    val personAName: StateFlow<String> = repository.getPersonAName()
        .stateIn(viewModelScope, SharingStarted.Lazily, "Ta")

    val personBName: StateFlow<String> = repository.getPersonBName()
        .stateIn(viewModelScope, SharingStarted.Lazily, "我")

    val personASemesterStart: StateFlow<LocalDate> = repository.getSemesterStartDate(PersonType.PERSON_A)
        .stateIn(viewModelScope, SharingStarted.Lazily, LocalDate.now())

    val personBSemesterStart: StateFlow<LocalDate> = repository.getSemesterStartDate(PersonType.PERSON_B)
        .stateIn(viewModelScope, SharingStarted.Lazily, LocalDate.now())

    val personATotalWeeks: StateFlow<Int> = repository.getTotalWeeks(PersonType.PERSON_A)
        .stateIn(viewModelScope, SharingStarted.Lazily, 16)

    val personBTotalWeeks: StateFlow<Int> = repository.getTotalWeeks(PersonType.PERSON_B)
        .stateIn(viewModelScope, SharingStarted.Lazily, 16)

    val personACurrentWeek: StateFlow<Int> = repository.getCurrentWeek(PersonType.PERSON_A)
        .stateIn(viewModelScope, SharingStarted.Lazily, 1)

    val personBCurrentWeek: StateFlow<Int> = repository.getCurrentWeek(PersonType.PERSON_B)
        .stateIn(viewModelScope, SharingStarted.Lazily, 1)

    val personAPeriodsPerDay: StateFlow<Int> = repository.getTotalPeriods(PersonType.PERSON_A)
        .stateIn(viewModelScope, SharingStarted.Lazily, 5)

    val personBPeriodsPerDay: StateFlow<Int> = repository.getTotalPeriods(PersonType.PERSON_B)
        .stateIn(viewModelScope, SharingStarted.Lazily, 5)

    fun getSemesterStartDate(personType: PersonType): StateFlow<LocalDate> = 
        repository.getSemesterStartDate(personType)
            .stateIn(viewModelScope, SharingStarted.Lazily, LocalDate.now())

    fun getTotalWeeks(personType: PersonType): StateFlow<Int> = 
        repository.getTotalWeeks(personType)
            .stateIn(viewModelScope, SharingStarted.Lazily, 16)

    fun getCurrentWeek(personType: PersonType): StateFlow<Int> = 
        repository.getCurrentWeek(personType)
            .stateIn(viewModelScope, SharingStarted.Lazily, 1)

    fun getTotalPeriods(personType: PersonType): StateFlow<Int> = 
        if (personType == PersonType.PERSON_A) personAPeriodsPerDay else personBPeriodsPerDay

    private val personAPeriodTimesFlow: StateFlow<List<String>> = repository.getPeriodTimes(PersonType.PERSON_A)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val personBPeriodTimesFlow: StateFlow<List<String>> = repository.getPeriodTimes(PersonType.PERSON_B)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun getPeriodTimes(personType: PersonType): StateFlow<List<String>> = 
        if (personType == PersonType.PERSON_A) personAPeriodTimesFlow else personBPeriodTimesFlow

    val showNonCurrentWeek: StateFlow<Boolean> = repository.getShowNonCurrentWeekCourses()
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val showSaturday: StateFlow<Boolean> = repository.getShowSaturday()
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    val showSunday: StateFlow<Boolean> = repository.getShowSunday()
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val courseCellHeight: StateFlow<Int> = repository.getCourseCellHeight()
        .stateIn(viewModelScope, SharingStarted.Lazily, 60)

    val notificationEnabled: StateFlow<Boolean> = repository.getNotificationEnabled()
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    val reminderMinutesBefore: StateFlow<Int> = repository.getNotificationAdvanceTime()
        .stateIn(viewModelScope, SharingStarted.Lazily, 10)

    val liveNotificationEnabled: StateFlow<Boolean> = repository.getLiveNotificationEnabled()
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    val todayCourseDisplayMode: StateFlow<TodayCourseDisplayMode> = repository.getTodayCourseDisplayMode()
        .stateIn(viewModelScope, SharingStarted.Lazily, TodayCourseDisplayMode.BOTH)

    val themeMode: StateFlow<ThemeMode> = repository.getThemeMode()
        .stateIn(viewModelScope, SharingStarted.Lazily, ThemeMode.FOLLOW_SYSTEM)

    val showDashedBorder: StateFlow<Boolean> = repository.getShowDashedBorder()
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    val autoSilentEnabled: StateFlow<Boolean> = repository.getAutoSilentEnabled()
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    val autoSilentModeType: StateFlow<String> = repository.getAutoSilentModeType()
        .stateIn(viewModelScope, SharingStarted.Lazily, "VIBRATE")

    val autoSilentAdvanceTime: StateFlow<Int> = repository.getAutoSilentAdvanceTime()
        .stateIn(viewModelScope, SharingStarted.Lazily, 5)

    val courseNameFontSize: StateFlow<Int> = repository.getCourseNameFontSize()
        .stateIn(viewModelScope, SharingStarted.Lazily, 12)

    val courseLocationFontSize: StateFlow<Int> = repository.getCourseLocationFontSize()
        .stateIn(viewModelScope, SharingStarted.Lazily, 11)

    fun setSemesterStartDate(personType: PersonType, date: LocalDate) {
        viewModelScope.launch {
            repository.setSemesterStartDate(personType, date)
        }
    }

    fun setTotalWeeks(personType: PersonType, weeks: Int) {
        viewModelScope.launch {
            repository.setTotalWeeks(personType, weeks)
        }
    }

    fun setCurrentWeek(personType: PersonType, week: Int) {
        viewModelScope.launch {
            repository.setCurrentWeek(personType, week)
        }
    }

    fun setTotalPeriods(personType: PersonType, periods: Int) {
        viewModelScope.launch {
            repository.setTotalPeriods(personType, periods)
        }
    }

    fun setPeriodTimes(personType: PersonType, times: List<String>) {
        viewModelScope.launch {
            repository.setPeriodTimes(personType, times)
            repository.updateCourseTimesForPerson(personType, times)
        }
    }

    fun setPersonName(personType: PersonType, name: String) {
        viewModelScope.launch {
            repository.setPersonName(personType, name)
        }
    }

    fun setShowNonCurrentWeekCourses(show: Boolean) {
        viewModelScope.launch {
            repository.setShowNonCurrentWeekCourses(show)
        }
    }

    fun setShowSaturday(show: Boolean) {
        viewModelScope.launch {
            repository.setShowSaturday(show)
        }
    }

    fun setShowSunday(show: Boolean) {
        viewModelScope.launch {
            repository.setShowSunday(show)
        }
    }

    fun setCourseCellHeight(height: Int) {
        viewModelScope.launch {
            repository.setCourseCellHeight(height)
        }
    }

    fun setNotificationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setNotificationEnabled(enabled)
        }
    }

    fun setNotificationAdvanceTime(minutes: Int) {
        viewModelScope.launch {
            repository.setNotificationAdvanceTime(minutes)
        }
    }

    fun setLiveNotificationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setLiveNotificationEnabled(enabled)
        }
    }

    fun setTodayCourseDisplayMode(mode: TodayCourseDisplayMode) {
        viewModelScope.launch {
            repository.setTodayCourseDisplayMode(mode)
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            repository.setThemeMode(mode)
        }
    }

    fun setShowDashedBorder(show: Boolean) {
        viewModelScope.launch {
            repository.setShowDashedBorder(show)
        }
    }

    fun setAutoSilentEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setAutoSilentEnabled(enabled)
        }
    }

    fun setAutoSilentModeType(modeType: SilentModeType) {
        viewModelScope.launch {
            repository.setAutoSilentModeType(modeType)
        }
    }

    fun setAutoSilentAdvanceTime(minutes: Int) {
        viewModelScope.launch {
            repository.setAutoSilentAdvanceTime(minutes)
        }
    }

    fun setCourseNameFontSize(size: Int) {
        viewModelScope.launch {
            repository.setCourseNameFontSize(size)
        }
    }

    fun setCourseLocationFontSize(size: Int) {
        viewModelScope.launch {
            repository.setCourseLocationFontSize(size)
        }
    }

    fun setPersonSemesterStart(personType: PersonType, date: LocalDate) {
        viewModelScope.launch {
            repository.setSemesterStartDate(personType, date)
            val totalWeeks = repository.getTotalWeeks(personType).first()
            val calculatedWeek = repository.calculateCurrentWeek(date, totalWeeks)
            repository.setCurrentWeek(personType, calculatedWeek)
        }
    }

    fun setPersonTotalWeeks(personType: PersonType, weeks: Int) {
        viewModelScope.launch {
            repository.setTotalWeeks(personType, weeks)
            val startDate = repository.getSemesterStartDate(personType).first()
            val calculatedWeek = repository.calculateCurrentWeek(startDate, weeks)
            repository.setCurrentWeek(personType, calculatedWeek)
        }
    }

    fun setPersonCurrentWeek(personType: PersonType, week: Int) {
        viewModelScope.launch {
            repository.setCurrentWeek(personType, week)
        }
    }

    fun setPersonPeriodsPerDay(personType: PersonType, periods: Int) {
        viewModelScope.launch {
            repository.setTotalPeriods(personType, periods)
        }
    }

    fun setReminderMinutesBefore(minutes: Int) {
        viewModelScope.launch {
            repository.setNotificationAdvanceTime(minutes)
        }
    }

    fun requestNotificationPermission(context: Context) {
        // Permission request is handled by the Activity
    }

    fun openNotificationSettings(context: Context) {
        val intent = android.content.Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
        }
        context.startActivity(intent)
    }

    suspend fun exportToCacheFile(
        context: Context,
        fileName: String,
        exportScope: ExportScope = ExportScope.ALL_WITH_SETTINGS
    ): Result<java.io.File> {
        return try {
            val allCourses = repository.getAllCourses().first()
            val courses = when (exportScope) {
                ExportScope.PERSON_A_ONLY -> allCourses.filter { it.personType == PersonType.PERSON_A }
                ExportScope.PERSON_B_ONLY -> allCourses.filter { it.personType == PersonType.PERSON_B }
                else -> allCourses
            }
            
            val settingsA = if (exportScope == ExportScope.ALL_WITH_SETTINGS || exportScope == ExportScope.PERSON_A_ONLY) {
                ScheduleSettingsExport(
                    semesterStartDate = repository.getSemesterStartDate(PersonType.PERSON_A).first().toEpochDay(),
                    totalWeeks = repository.getTotalWeeks(PersonType.PERSON_A).first(),
                    currentWeek = repository.getCurrentWeek(PersonType.PERSON_A).first(),
                    totalPeriods = repository.getTotalPeriods(PersonType.PERSON_A).first(),
                    periodTimes = repository.getPeriodTimes(PersonType.PERSON_A).first()
                )
            } else null
            
            val settingsB = if (exportScope == ExportScope.ALL_WITH_SETTINGS || exportScope == ExportScope.PERSON_B_ONLY) {
                ScheduleSettingsExport(
                    semesterStartDate = repository.getSemesterStartDate(PersonType.PERSON_B).first().toEpochDay(),
                    totalWeeks = repository.getTotalWeeks(PersonType.PERSON_B).first(),
                    currentWeek = repository.getCurrentWeek(PersonType.PERSON_B).first(),
                    totalPeriods = repository.getTotalPeriods(PersonType.PERSON_B).first(),
                    periodTimes = repository.getPeriodTimes(PersonType.PERSON_B).first()
                )
            } else null
            
            val personANameValue = repository.getPersonAName().first()
            val personBNameValue = repository.getPersonBName().first()
            
            val cacheDir = java.io.File(context.cacheDir, "export")
            if (!cacheDir.exists()) cacheDir.mkdirs()
            val file = java.io.File(cacheDir, fileName)
            
            CsvExporter.exportToFile(
                file = file,
                courses = courses,
                settingsA = settingsA,
                settingsB = settingsB,
                personAName = personANameValue,
                personBName = personBNameValue
            )
            
            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun exportToCsv(
        context: Context,
        uri: Uri,
        exportScope: ExportScope = ExportScope.ALL_WITH_SETTINGS
    ): Result<Unit> {
        return try {
            val allCourses = repository.getAllCourses().first()
            val courses = when (exportScope) {
                ExportScope.PERSON_A_ONLY -> allCourses.filter { it.personType == PersonType.PERSON_A }
                ExportScope.PERSON_B_ONLY -> allCourses.filter { it.personType == PersonType.PERSON_B }
                else -> allCourses
            }
            
            val settingsA = if (exportScope == ExportScope.ALL_WITH_SETTINGS || exportScope == ExportScope.PERSON_A_ONLY) {
                ScheduleSettingsExport(
                    semesterStartDate = repository.getSemesterStartDate(PersonType.PERSON_A).first().toEpochDay(),
                    totalWeeks = repository.getTotalWeeks(PersonType.PERSON_A).first(),
                    currentWeek = repository.getCurrentWeek(PersonType.PERSON_A).first(),
                    totalPeriods = repository.getTotalPeriods(PersonType.PERSON_A).first(),
                    periodTimes = repository.getPeriodTimes(PersonType.PERSON_A).first()
                )
            } else null
            
            val settingsB = if (exportScope == ExportScope.ALL_WITH_SETTINGS || exportScope == ExportScope.PERSON_B_ONLY) {
                ScheduleSettingsExport(
                    semesterStartDate = repository.getSemesterStartDate(PersonType.PERSON_B).first().toEpochDay(),
                    totalWeeks = repository.getTotalWeeks(PersonType.PERSON_B).first(),
                    currentWeek = repository.getCurrentWeek(PersonType.PERSON_B).first(),
                    totalPeriods = repository.getTotalPeriods(PersonType.PERSON_B).first(),
                    periodTimes = repository.getPeriodTimes(PersonType.PERSON_B).first()
                )
            } else null
            
            val personANameValue = repository.getPersonAName().first()
            val personBNameValue = repository.getPersonBName().first()
            
            CsvExporter.exportToCsv(
                context = context,
                uri = uri,
                courses = courses,
                settingsA = settingsA,
                settingsB = settingsB,
                personAName = personANameValue,
                personBName = personBNameValue
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importFromCsv(
        context: Context,
        uri: Uri,
        targetPerson: PersonType? = null
    ): ImportResult {
        val effectivePerson = targetPerson ?: PersonType.PERSON_B
        val periodTimes = getPeriodTimes(effectivePerson).first()
        val totalWeeks = getTotalWeeks(effectivePerson).first()
        return CsvExporter.importFromCsv(context, uri, targetPerson, periodTimes, totalWeeks)
    }

    suspend fun importFromWakeup(
        context: Context,
        uri: Uri,
        targetPerson: PersonType
    ): ImportResult {
        return WakeupImporter.importFromWakeup(context, uri, targetPerson)
    }

    suspend fun importFromEducationSystem(
        credentials: EducationSystemCredentials,
        targetPerson: PersonType
    ): EducationSystemResult {
        return EducationSystemImporter.loginAndGetCourses(credentials, targetPerson)
    }

    suspend fun saveImportedCourses(
        courses: List<CourseImportData>,
        targetPerson: PersonType,
        mergeMode: Boolean
    ): ImportResult {
        return try {
            if (!mergeMode) {
                repository.deleteCoursesByPerson(targetPerson)
            }
            
            var importedCount = 0
            var conflictCount = 0
            val errors = mutableListOf<String>()
            
            val existingCourses = repository.getCoursesByPerson(targetPerson).first()
            val periodTimes = repository.getPeriodTimes(targetPerson).first()
            val currentTotalPeriods = repository.getTotalPeriods(targetPerson).first()
            
            val maxPeriodInImport = courses.map { it.endPeriod }.maxOrNull() ?: 1
            if (maxPeriodInImport > currentTotalPeriods) {
                repository.setTotalPeriods(targetPerson, maxPeriodInImport)
            }
            
            for (importData in courses) {
                val courseWithTime = recalculateTimeFromPeriod(importData, periodTimes, targetPerson)
                val course = courseWithTime.toCourse()
                
                val hasConflict = existingCourses.any { existing ->
                    courseWithTime.hasConflictWith(existing)
                }
                
                if (hasConflict && mergeMode) {
                    conflictCount++
                    errors.add("${importData.name} 时间冲突，已跳过")
                } else {
                    repository.insertCourse(course)
                    importedCount++
                }
            }
            
            ImportResult(
                success = true,
                importedCount = importedCount,
                conflictCount = conflictCount,
                errors = errors
            )
        } catch (e: Exception) {
            ImportResult(
                success = false,
                errors = listOf("保存失败: ${e.message}")
            )
        }
    }

    suspend fun saveImportedCoursesWithSettings(
        courses: List<CourseImportData>,
        mergeMode: Boolean,
        importSettings: Boolean,
        settingsA: ScheduleSettingsExport?,
        settingsB: ScheduleSettingsExport?,
        personAName: String?,
        personBName: String?
    ): ImportResult {
        return try {
            if (!mergeMode) {
                repository.deleteCoursesByPerson(PersonType.PERSON_A)
                repository.deleteCoursesByPerson(PersonType.PERSON_B)
            }
            
            if (importSettings) {
                personAName?.let { repository.setPersonName(PersonType.PERSON_A, it) }
                personBName?.let { repository.setPersonName(PersonType.PERSON_B, it) }
                
                settingsA?.let { settings ->
                    repository.setSemesterStartDate(PersonType.PERSON_A, java.time.LocalDate.ofEpochDay(settings.semesterStartDate))
                    repository.setTotalWeeks(PersonType.PERSON_A, settings.totalWeeks)
                    repository.setCurrentWeek(PersonType.PERSON_A, settings.currentWeek)
                    repository.setTotalPeriods(PersonType.PERSON_A, settings.totalPeriods)
                    if (settings.periodTimes.isNotEmpty()) {
                        repository.setPeriodTimes(PersonType.PERSON_A, settings.periodTimes)
                    }
                }
                
                settingsB?.let { settings ->
                    repository.setSemesterStartDate(PersonType.PERSON_B, java.time.LocalDate.ofEpochDay(settings.semesterStartDate))
                    repository.setTotalWeeks(PersonType.PERSON_B, settings.totalWeeks)
                    repository.setCurrentWeek(PersonType.PERSON_B, settings.currentWeek)
                    repository.setTotalPeriods(PersonType.PERSON_B, settings.totalPeriods)
                    if (settings.periodTimes.isNotEmpty()) {
                        repository.setPeriodTimes(PersonType.PERSON_B, settings.periodTimes)
                    }
                }
            }
            
            val coursesForPersonA = courses.filter { it.personType == PersonType.PERSON_A }
            val coursesForPersonB = courses.filter { it.personType == PersonType.PERSON_B }
            
            if (!importSettings) {
                val maxPeriodA = coursesForPersonA.map { it.endPeriod }.maxOrNull() ?: 0
                val maxPeriodB = coursesForPersonB.map { it.endPeriod }.maxOrNull() ?: 0
                
                val currentTotalPeriodsA = repository.getTotalPeriods(PersonType.PERSON_A).first()
                val currentTotalPeriodsB = repository.getTotalPeriods(PersonType.PERSON_B).first()
                
                if (maxPeriodA > currentTotalPeriodsA) {
                    repository.setTotalPeriods(PersonType.PERSON_A, maxPeriodA)
                }
                if (maxPeriodB > currentTotalPeriodsB) {
                    repository.setTotalPeriods(PersonType.PERSON_B, maxPeriodB)
                }
            }
            
            var importedCount = 0
            var conflictCount = 0
            val errors = mutableListOf<String>()
            
            val existingCoursesA = repository.getCoursesByPerson(PersonType.PERSON_A).first()
            val existingCoursesB = repository.getCoursesByPerson(PersonType.PERSON_B).first()
            val periodTimesA = repository.getPeriodTimes(PersonType.PERSON_A).first()
            val periodTimesB = repository.getPeriodTimes(PersonType.PERSON_B).first()
            
            for (importData in courses) {
                val (existingCourses, periodTimes) = if (importData.personType == PersonType.PERSON_A) {
                    existingCoursesA to periodTimesA
                } else {
                    existingCoursesB to periodTimesB
                }
                
                val courseWithTime = recalculateTimeFromPeriod(importData, periodTimes, importData.personType)
                val course = courseWithTime.toCourse()
                
                val hasConflict = existingCourses.any { existing ->
                    courseWithTime.hasConflictWith(existing)
                }
                
                if (hasConflict && mergeMode) {
                    conflictCount++
                    errors.add("${importData.name} 时间冲突，已跳过")
                } else {
                    repository.insertCourse(course)
                    importedCount++
                }
            }
            
            ImportResult(
                success = true,
                importedCount = importedCount,
                conflictCount = conflictCount,
                errors = errors
            )
        } catch (e: Exception) {
            ImportResult(
                success = false,
                errors = listOf("保存失败: ${e.message}")
            )
        }
    }

    fun testNotification() {
        viewModelScope.launch {
            android.util.Log.i("SettingsViewModel", "=== 测试通知按钮被点击 ===")
            val now = java.time.LocalTime.now()
            notificationManager.showReminderNotification(
                courseName = "测试课程",
                courseLocation = "测试教室",
                startHour = now.hour,
                startMinute = now.minute + 5,
                advanceMinutes = 5
            )
            android.util.Log.i("SettingsViewModel", "测试通知已发送")
        }
    }

    fun rescheduleNotifications() {
        viewModelScope.launch {
            android.util.Log.i("SettingsViewModel", "=== 手动重新调度通知 ===")
            val hasOngoingCourse = notificationManager.checkAndShowOngoingNotification()
            android.util.Log.i("SettingsViewModel", "当前课程检查结果: hasOngoingCourse=$hasOngoingCourse")
            notificationManager.scheduleReminderNotifications()
            android.util.Log.i("SettingsViewModel", "通知调度完成")
            _rescheduleResult.value = if (hasOngoingCourse) {
                "已重新调度，当前课程通知已显示"
            } else {
                "已重新调度通知"
            }
        }
    }
    
    private val _rescheduleResult = MutableStateFlow<String?>(null)
    val rescheduleResult: StateFlow<String?> = _rescheduleResult
    
    fun clearRescheduleResult() {
        _rescheduleResult.value = null
    }
    
    fun rescheduleAutoSilent() {
        viewModelScope.launch {
            android.util.Log.i("SettingsViewModel", "=== 手动重新调度自动静音 ===")
            notificationManager.scheduleAutoSilentTasks()
            android.util.Log.i("SettingsViewModel", "自动静音调度完成")
        }
    }

    fun testOngoingNotification(context: Context) {
        viewModelScope.launch {
            android.util.Log.i("SettingsViewModel", "=== 测试上课中通知按钮被点击 ===")
            notificationManager.createNotificationChannels()
            com.duoschedule.notification.LiveUpdateService.start(
                context = context,
                courseName = "测试课程 - 上课中",
                courseLocation = "测试教室 -101",
                remainingMinutes = 45
            )
            android.util.Log.i("SettingsViewModel", "上课中通知服务已启动")
        }
    }
    
    suspend fun getExistingCourses(personType: PersonType) = repository.getCoursesByPerson(personType).first()
    
    private fun recalculateTimeFromPeriod(
        importData: CourseImportData,
        periodTimes: List<String>,
        targetPerson: PersonType
    ): CourseImportData {
        if (importData.startPeriod <= 0 || importData.endPeriod <= 0) {
            return importData.copy(personType = targetPerson)
        }
        
        val (startTime, endTime) = getPeriodTimeRange(importData.startPeriod, importData.endPeriod, periodTimes)
        
        return importData.copy(
            personType = targetPerson,
            startHour = startTime.first,
            startMinute = startTime.second,
            endHour = endTime.first,
            endMinute = endTime.second
        )
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

        val startTime = parseTimeFromStr(effectivePeriodTimes[startIndex].split("-").firstOrNull() ?: "08:00")
        val endTime = parseTimeFromStr(effectivePeriodTimes[endIndex].split("-").lastOrNull() ?: "08:45")

        return Pair(
            startTime ?: Pair(8, 0),
            endTime ?: Pair(8, 45)
        )
    }
    
    private fun parseTimeFromStr(timeStr: String): Pair<Int, Int>? {
        val parts = timeStr.split(":")
        if (parts.size != 2) return null
        val hour = parts[0].toIntOrNull() ?: return null
        val minute = parts[1].toIntOrNull() ?: return null
        return Pair(hour, minute)
    }
}
