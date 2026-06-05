package com.duoschedule.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import com.kyant.capsule.ContinuousRoundedRectangle
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.duoschedule.data.model.AppThemeMode
import com.duoschedule.data.model.Course
import com.duoschedule.data.model.PersonType
import com.duoschedule.data.model.TodayCourseDisplayMode
import com.duoschedule.ui.edit.CoursePreviewBottomSheet
import com.duoschedule.ui.main.components.*
import com.duoschedule.ui.theme.*
import com.duoschedule.ui.theme.LocalAppThemeMode
import com.duoschedule.ui.theme.LiquidGlassButton
import com.duoschedule.ui.theme.LiquidGlassButtonStyle
import dev.chrisbanes.haze.rememberHazeState
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop as kyantLayerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop as kyantRememberLayerBackdrop
import dev.chrisbanes.haze.hazeSource
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.window.WindowDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.basic.TabRow
import top.yukonga.miuix.kmp.basic.TabRowDefaults
import androidx.compose.foundation.shape.RoundedCornerShape
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToEdit: (Long?, Int?, Int?, PersonType?) -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val singleModeEnabled by viewModel.singleModeEnabled.collectAsState()
    val personACurrentCourse by viewModel.personACurrentCourse.collectAsState()
    val personBCurrentCourse by viewModel.personBCurrentCourse.collectAsState()

    var selectedCourse by remember { mutableStateOf<Course?>(null) }
    var selectedCoursePersonType by remember { mutableStateOf<PersonType?>(null) }
    val previewSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showPreview by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(selectedCourse) {
        if (selectedCourse != null) {
            showPreview = true
        }
    }

    val scrollState = rememberScrollState()

    val hazeState = rememberHazeState()

    val today = remember { LocalDate.now() }

    val contentBackdrop = kyantRememberLayerBackdrop()

    Scaffold(
        topBar = {}
    ) { innerPadding ->
        Box(modifier = Modifier.hazeSource(hazeState).kyantLayerBackdrop(contentBackdrop)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .statusBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                HeaderSection(
                    viewModel = viewModel,
                    singleModeEnabled = singleModeEnabled
                )

                Spacer(modifier = Modifier.height(Spacing.xl - Spacing.md))

                CurrentCourseCard(
                    personAState = personACurrentCourse,
                    personBState = personBCurrentCourse,
                    singleModeEnabled = singleModeEnabled,
                    modifier = Modifier.padding(horizontal = Spacing.lg)
                )

                Spacer(modifier = Modifier.height(Spacing.lg - Spacing.md))

                TodayScheduleSection(
                    viewModel = viewModel,
                    singleModeEnabled = singleModeEnabled,
                    onCourseClick = { course, personType ->
                        selectedCourse = course
                        selectedCoursePersonType = personType
                    }
                )

                Spacer(modifier = Modifier.height(LiquidBottomTabsSpec.Height + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()))
            }
        }
    }

    if (showPreview && selectedCourse != null) {
        CoursePreviewBottomSheet(
            course = selectedCourse!!,
            onDismiss = {
                showPreview = false
                selectedCourse = null
                selectedCoursePersonType = null
            },
            onEdit = {
                showPreview = false
                val course = selectedCourse
                val personType = selectedCoursePersonType
                selectedCourse = null
                selectedCoursePersonType = null
                course?.let { onNavigateToEdit(it.id, null, null, personType) }
            },
            onDelete = {
                showDeleteConfirm = true
            },
            sheetState = previewSheetState
        )
    }

    if (showDeleteConfirm && selectedCourse != null) {
        val appThemeMode = LocalAppThemeMode.current

        if (appThemeMode == AppThemeMode.MIUIX) {
            WindowDialog(
                show = true,
                title = "删除课程",
                onDismissRequest = { showDeleteConfirm = false }
            ) {
                top.yukonga.miuix.kmp.basic.Text(
                    text = "确定要删除「${selectedCourse?.name}」吗？",
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
                Spacer(modifier = Modifier.height(Spacing.md))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    Button(
                        onClick = { showDeleteConfirm = false },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors()
                    ) {
                        top.yukonga.miuix.kmp.basic.Text("取消")
                    }
                    Button(
                        onClick = {
                            viewModel.deleteCourse(selectedCourse!!.id)
                            showDeleteConfirm = false
                            showPreview = false
                            selectedCourse = null
                            selectedCoursePersonType = null
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColorsPrimary()
                    ) {
                        top.yukonga.miuix.kmp.basic.Text("删除")
                    }
                }
            }
        } else {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                containerColor = getDialogBackgroundColor(),
                shape = MaterialTheme.shapes.large,
                title = { Text("删除课程") },
                text = { Text("确定要删除「${selectedCourse?.name}」吗？") },
                confirmButton = {
                    LiquidGlassButton(
                        onClick = {
                            viewModel.deleteCourse(selectedCourse!!.id)
                            showDeleteConfirm = false
                            showPreview = false
                            selectedCourse = null
                            selectedCoursePersonType = null
                        },
                        text = "删除",
                        style = LiquidGlassButtonStyle.Tinted
                    )
                },
                dismissButton = {
                    LiquidGlassButton(
                        onClick = { showDeleteConfirm = false },
                        text = "取消",
                        style = LiquidGlassButtonStyle.NonTinted
                    )
                }
            )
        }
    }
}

@Composable
private fun HeaderSection(
    viewModel: MainViewModel,
    singleModeEnabled: Boolean = false
) {
    val personAName by viewModel.personAName.collectAsState()
    val personBName by viewModel.personBName.collectAsState()
    val personACurrentWeek by viewModel.personACurrentWeek.collectAsState()
    val personBCurrentWeek by viewModel.personBCurrentWeek.collectAsState()

    val today = remember { LocalDate.now() }
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()
    val labelsTertiary = getLabelsVibrantTertiary()
    val darkTheme = LocalDarkTheme.current
    val appThemeMode = LocalAppThemeMode.current
    
    val dateFormatter = DateTimeFormatter.ofPattern("M月d日 EEEE")
    val dateText = today.format(dateFormatter)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg)
            .padding(top = Spacing.sm)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = dateText,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = labelsPrimary,
                maxLines = 1
            )
            
            val chipModifier = if (appThemeMode == AppThemeMode.MIUIX) {
                Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MiuixTheme.colorScheme.surfaceContainer.copy(alpha = 0.7f))
            } else {
                Modifier
                    .clip(ContinuousRoundedRectangle(BorderRadius.iOS26.small))
                    .background(
                        if (darkTheme) Color.White.copy(alpha = 0.08f)
                        else Color.Black.copy(alpha = 0.04f)
                    )
            }
            Column(
                modifier = chipModifier
                    .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                        tint = labelsSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${personAName}第${personACurrentWeek}周",
                        style = MaterialTheme.typography.labelSmall,
                        color = labelsSecondary,
                        maxLines = 1
                    )
                }
                if (!singleModeEnabled) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarMonth,
                            contentDescription = null,
                            tint = labelsSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${personBName}第${personBCurrentWeek}周",
                            style = MaterialTheme.typography.labelSmall,
                            color = labelsSecondary,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodayScheduleSection(
    viewModel: MainViewModel,
    singleModeEnabled: Boolean = false,
    onCourseClick: (Course, PersonType) -> Unit
) {
    val personATodayCourses by viewModel.personATodayCourses.collectAsState()
    val personBTodayCourses by viewModel.personBTodayCourses.collectAsState()
    val displayMode by viewModel.todayCourseDisplayMode.collectAsState()
    val currentHour by viewModel.currentHour.collectAsState()
    val currentMinute by viewModel.currentMinute.collectAsState()
    val personAPeriodTimes by viewModel.personAPeriodTimes.collectAsState()
    val personBPeriodTimes by viewModel.personBPeriodTimes.collectAsState()
    val personAName by viewModel.personAName.collectAsState()
    val personBName by viewModel.personBName.collectAsState()
    val freeTimeSlots by viewModel.freeTimeSlots.collectAsState()

    val effectiveDisplayMode = if (singleModeEnabled) TodayCourseDisplayMode.SELF_ONLY else displayMode
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()
    val labelsTertiary = getLabelsVibrantTertiary()
    val darkTheme = LocalDarkTheme.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Spacing.xs),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "今日课程",
                style = MaterialTheme.typography.titleSmall,
                color = labelsPrimary,
                fontWeight = FontWeight.SemiBold
            )
            
            Text(
                text = if (singleModeEnabled) "${personATodayCourses.size} 节" else "${personATodayCourses.size + personBTodayCourses.size} 节",
                style = MaterialTheme.typography.labelMedium,
                color = labelsTertiary
            )
        }

        if (!singleModeEnabled) {
            val appThemeMode = LocalAppThemeMode.current

            if (appThemeMode == AppThemeMode.MIUIX) {
                val tabs = listOf(
                    personAName.ifEmpty { "我" },
                    personBName.ifEmpty { "Ta" },
                    "全部"
                )
                val selectedTabIndex = when (displayMode) {
                    TodayCourseDisplayMode.SELF_ONLY -> 0
                    TodayCourseDisplayMode.TA_ONLY -> 1
                    TodayCourseDisplayMode.BOTH -> 2
                }
                TabRow(
                    tabs = tabs,
                    selectedTabIndex = selectedTabIndex,
                    onTabSelected = { index ->
                        val mode = when (index) {
                            0 -> TodayCourseDisplayMode.SELF_ONLY
                            1 -> TodayCourseDisplayMode.TA_ONLY
                            else -> TodayCourseDisplayMode.BOTH
                        }
                        viewModel.setTodayCourseDisplayMode(mode)
                    },
                    modifier = Modifier.padding(bottom = Spacing.sm)
                )
            } else {
                val displayModeOptions = listOf(
                    SegmentOption(TodayCourseDisplayMode.SELF_ONLY, personAName.ifEmpty { "我" }),
                    SegmentOption(TodayCourseDisplayMode.TA_ONLY, personBName.ifEmpty { "Ta" }),
                    SegmentOption(TodayCourseDisplayMode.BOTH, "全部")
                )

                SegmentedControl(
                    options = displayModeOptions,
                    selectedOption = displayMode,
                    onOptionSelected = { viewModel.setTodayCourseDisplayMode(it) },
                    modifier = Modifier.padding(bottom = Spacing.sm)
                )
            }
        }

        if (personATodayCourses.isEmpty() && (singleModeEnabled || personBTodayCourses.isEmpty())) {
            EmptyScheduleCard()
        } else {
            ScheduleList(
                personACourses = personATodayCourses,
                personBCourses = personBTodayCourses,
                displayMode = effectiveDisplayMode,
                currentHour = currentHour,
                currentMinute = currentMinute,
                periodTimesA = personAPeriodTimes,
                periodTimesB = personBPeriodTimes,
                personAName = personAName,
                personBName = personBName,
                freeTimeSlots = if (!singleModeEnabled) freeTimeSlots else emptyList(),
                onCourseClick = onCourseClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun EmptyScheduleCard(
    backdrop: Backdrop = LocalBackdrop.current ?: emptyBackdrop()
) {
    val appThemeMode = LocalAppThemeMode.current
    val darkTheme = LocalDarkTheme.current
    val labelsSecondary = getLabelsVibrantSecondary()
    val labelsTertiary = getLabelsVibrantTertiary()

    if (appThemeMode == AppThemeMode.MIUIX) {
        Surface(
            color = MiuixTheme.colorScheme.surfaceContainerHigh,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(vertical = Spacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocalCafe,
                    contentDescription = null,
                    tint = labelsSecondary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(Spacing.sm))
                Text(
                    text = "今日无课",
                    style = MaterialTheme.typography.titleMedium,
                    color = labelsSecondary
                )
                Text(
                    text = "享受你的自由时光",
                    style = MaterialTheme.typography.bodySmall,
                    color = labelsTertiary
                )
            }
        }
        return
    }

    val density = LocalDensity.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .drawBackdrop(
                backdrop = backdrop,
                shape = { ContinuousRoundedRectangle(BorderRadius.iOS26.large) },
                effects = {
                    vibrancy()
                    blur(with(density) { 4.dp.toPx() })
                    lens(
                        refractionHeight = with(density) { 8.dp.toPx() },
                        refractionAmount = with(density) { 16.dp.toPx() },
                        chromaticAberration = true
                    )
                },
                onDrawSurface = {
                    drawRect(
                        if (darkTheme) LiquidGlassColors.BottomSheet.Dark.Layer1_Tint.copy(alpha = 0.5f)
                        else LiquidGlassColors.BottomSheet.Light.Layer1_Tint.copy(alpha = 0.55f)
                    )
                    drawRect(
                        if (darkTheme) LiquidGlassColors.BottomSheet.Dark.Layer2_Base
                        else LiquidGlassColors.BottomSheet.Light.Layer2_Base,
                        blendMode = BlendMode.ColorDodge
                    )
                    drawRect(
                        if (darkTheme) LiquidGlassColors.BottomSheet.Dark.GlassEffect
                        else LiquidGlassColors.BottomSheet.Light.GlassEffect
                    )
                }
            )
            .padding(vertical = Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.LocalCafe,
            contentDescription = null,
            tint = labelsSecondary,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(Spacing.sm))
        Text(
            text = "今日无课",
            style = MaterialTheme.typography.titleMedium,
            color = labelsSecondary
        )
        Text(
            text = "享受你的自由时光",
            style = MaterialTheme.typography.bodySmall,
            color = labelsTertiary
        )
    }
}
