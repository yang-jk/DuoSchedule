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
import com.duoschedule.notification.NotificationTestHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: CourseRepository,
    val testHelper: NotificationTestHelper
) : ViewModel() {

    val personAName: StateFlow<String> = repository.getPersonAName()
        .stateIn(viewModelScope, SharingStarted.Lazily, "Ta")

    val personBName: StateFlow<String> = repository.getPersonBName()
        .stateIn(viewModelScope, SharingStarted.Lazily, "我")

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
        repository.getTotalPeriods(personType)
            .stateIn(viewModelScope, SharingStarted.Lazily, 12)

    fun getPeriodTimes(personType: PersonType): StateFlow<List<String>> = 
        repository.getPeriodTimes(personType)
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val showNonCurrentWeekCourses: StateFlow<Boolean> = repository.getShowNonCurrentWeekCourses()
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val showSaturday: StateFlow<Boolean> = repository.getShowSaturday()
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    val showSunday: StateFlow<Boolean> = repository.getShowSunday()
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val courseCellHeight: StateFlow<Int> = repository.getCourseCellHeight()
        .stateIn(viewModelScope, SharingStarted.Lazily, 60)

    val notificationEnabled: StateFlow<Boolean> = repository.getNotificationEnabled()
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    val notificationAdvanceTime: StateFlow<Int> = repository.getNotificationAdvanceTime()
        .stateIn(viewModelScope, SharingStarted.Lazily, 10)

    val islandDisplayMode: StateFlow<String> = repository.getIslandDisplayMode()
        .stateIn(viewModelScope, SharingStarted.Lazily, "BOTH")

    val liveNotificationEnabled: StateFlow<Boolean> = repository.getLiveNotificationEnabled()
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    val todayCourseDisplayMode: StateFlow<TodayCourseDisplayMode> = repository.getTodayCourseDisplayMode()
        .stateIn(viewModelScope, SharingStarted.Lazily, TodayCourseDisplayMode.BOTH)

    val themeMode: StateFlow<ThemeMode> = repository.getThemeMode()
        .stateIn(viewModelScope, SharingStarted.Lazily, ThemeMode.FOLLOW_SYSTEM)

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

    fun setIslandDisplayMode(mode: String) {
        viewModelScope.launch {
            repository.setIslandDisplayMode(mode)
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

    fun calculateCurrentWeek(startDate: LocalDate, totalWeeks: Int): Int {
        return repository.calculateCurrentWeek(startDate, totalWeeks)
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
                    semesterStartDate = getSemesterStartDate(PersonType.PERSON_A).value.toEpochDay(),
                    totalWeeks = getTotalWeeks(PersonType.PERSON_A).value,
                    currentWeek = getCurrentWeek(PersonType.PERSON_A).value,
                    totalPeriods = getTotalPeriods(PersonType.PERSON_A).value,
                    periodTimes = getPeriodTimes(PersonType.PERSON_A).value
                )
            } else null
            
            val settingsB = if (exportScope == ExportScope.ALL_WITH_SETTINGS || exportScope == ExportScope.PERSON_B_ONLY) {
                ScheduleSettingsExport(
                    semesterStartDate = getSemesterStartDate(PersonType.PERSON_B).value.toEpochDay(),
                    totalWeeks = getTotalWeeks(PersonType.PERSON_B).value,
                    currentWeek = getCurrentWeek(PersonType.PERSON_B).value,
                    totalPeriods = getTotalPeriods(PersonType.PERSON_B).value,
                    periodTimes = getPeriodTimes(PersonType.PERSON_B).value
                )
            } else null
            
            CsvExporter.exportToCsv(
                context = context,
                uri = uri,
                courses = courses,
                settingsA = settingsA,
                settingsB = settingsB,
                personAName = personAName.value,
                personBName = personBName.value
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
        val periodTimes = getPeriodTimes(PersonType.PERSON_B).first()
        return CsvExporter.importFromCsv(context, uri, targetPerson, periodTimes)
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
            
            for (importData in courses) {
                val course = importData.copy(personType = targetPerson).toCourse()
                
                val hasConflict = existingCourses.any { existing ->
                    importData.hasConflictWith(existing)
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
}
