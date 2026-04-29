package com.duoschedule.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import com.kyant.capsule.ContinuousRoundedRectangle
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.duoschedule.data.model.Course
import com.duoschedule.data.model.PersonType
import com.duoschedule.data.model.TodayCourseDisplayMode
import com.duoschedule.ui.edit.CoursePreviewBottomSheet
import com.duoschedule.ui.main.components.*
import com.duoschedule.ui.theme.*
import com.duoschedule.ui.theme.LiquidGlassButton
import com.duoschedule.ui.theme.LiquidGlassButtonStyle
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToEdit: (Long?, Int?, Int?, PersonType?) -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val personACurrentCourse by viewModel.personACurrentCourse.collectAsState()
    val personBCurrentCourse by viewModel.personBCurrentCourse.collectAsState()
    val personATodayCourses by viewModel.personATodayCourses.collectAsState()
    val personBTodayCourses by viewModel.personBTodayCourses.collectAsState()
    val freeTimeSlots by viewModel.freeTimeSlots.collectAsState()
    val personACurrentWeek by viewModel.personACurrentWeek.collectAsState()
    val personBCurrentWeek by viewModel.personBCurrentWeek.collectAsState()
    val personAName by viewModel.personAName.collectAsState()
    val personBName by viewModel.personBName.collectAsState()
    val displayMode by viewModel.todayCourseDisplayMode.collectAsState()
    val personAPeriodTimes by viewModel.personAPeriodTimes.collectAsState()
    val personBPeriodTimes by viewModel.personBPeriodTimes.collectAsState()

    val currentTime = remember { mutableStateOf(LocalTime.now()) }
    val today = remember { LocalDate.now() }

    LaunchedEffect(Unit) {
        while (true) {
            try {
                viewModel.updateTime()
                currentTime.value = LocalTime.now()
            } catch (e: Exception) {
                android.util.Log.e("MainScreen", "Error updating time", e)
            }
            kotlinx.coroutines.delay(60000)
        }
    }
    val currentHour = currentTime.value.hour
    val currentMinute = currentTime.value.minute

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

    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()
    val labelsTertiary = getLabelsVibrantTertiary()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
            HeaderSection(
                today = today,
                personAName = personAName,
                personBName = personBName,
                personACurrentWeek = personACurrentWeek,
                personBCurrentWeek = personBCurrentWeek
            )

            CurrentCourseSection(
                personAState = personACurrentCourse,
                personBState = personBCurrentCourse
            )

            FreeTimeSection(
                freeTimeSlots = freeTimeSlots
            )

            TodayScheduleSection(
                personATodayCourses = personATodayCourses,
                personBTodayCourses = personBTodayCourses,
                displayMode = displayMode,
                currentHour = currentHour,
                currentMinute = currentMinute,
                personAPeriodTimes = personAPeriodTimes,
                personBPeriodTimes = personBPeriodTimes,
                personAName = personAName,
                personBName = personBName,
                onDisplayModeChange = { viewModel.setTodayCourseDisplayMode(it) },
                onCourseClick = { course, personType ->
                    selectedCourse = course
                    selectedCoursePersonType = personType
                }
            )
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

@Composable
private fun HeaderSection(
    today: LocalDate,
    personAName: String,
    personBName: String,
    personACurrentWeek: Int,
    personBCurrentWeek: Int
) {
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()
    val labelsTertiary = getLabelsVibrantTertiary()
    val darkTheme = LocalDarkTheme.current
    
    val dateFormatter = DateTimeFormatter.ofPattern("M月d日 EEEE")
    val dateText = today.format(dateFormatter)
    
    val greeting = remember {
        val hour = LocalTime.now().hour
        when {
            hour < 6 -> "夜深了"
            hour < 12 -> "早上好"
            hour < 14 -> "中午好"
            hour < 18 -> "下午好"
            else -> "晚上好"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg)
            .padding(top = Spacing.md)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.titleMedium,
                    color = labelsSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = labelsPrimary
                )
            }
            
            Row(
                modifier = Modifier
                    .clip(ContinuousRoundedRectangle(BorderRadius.iOS26.small))
                    .background(
                        if (darkTheme) Color.White.copy(alpha = 0.08f)
                        else Color.Black.copy(alpha = 0.04f)
                    )
                    .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
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
                    color = labelsSecondary
                )
                Text(
                    text = "|",
                    style = MaterialTheme.typography.labelSmall,
                    color = labelsTertiary
                )
                Text(
                    text = "${personAName}第${personACurrentWeek}周",
                    style = MaterialTheme.typography.labelSmall,
                    color = labelsSecondary
                )
            }
        }
    }
}

@Composable
private fun CurrentCourseSection(
    personAState: com.duoschedule.ui.model.CurrentCourseState,
    personBState: com.duoschedule.ui.model.CurrentCourseState
) {
    val labelsPrimary = getLabelsVibrantPrimary()
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg)
    ) {
        Text(
            text = "当前状态",
            style = MaterialTheme.typography.titleSmall,
            color = labelsPrimary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = Spacing.sm)
        )
        
        CurrentCourseCard(
            personAState = personAState,
            personBState = personBState
        )
    }
}

@Composable
private fun FreeTimeSection(
    freeTimeSlots: List<com.duoschedule.ui.model.FreeTimeSlot>
) {
    val labelsPrimary = getLabelsVibrantPrimary()
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg)
    ) {
        Text(
            text = "共同空闲",
            style = MaterialTheme.typography.titleSmall,
            color = labelsPrimary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = Spacing.sm)
        )
        
        FreeTimeCard(freeTimeSlots = freeTimeSlots)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodayScheduleSection(
    personATodayCourses: List<Course>,
    personBTodayCourses: List<Course>,
    displayMode: TodayCourseDisplayMode,
    currentHour: Int,
    currentMinute: Int,
    personAPeriodTimes: List<String>,
    personBPeriodTimes: List<String>,
    personAName: String,
    personBName: String,
    onDisplayModeChange: (TodayCourseDisplayMode) -> Unit,
    onCourseClick: (Course, PersonType) -> Unit
) {
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
                .padding(bottom = Spacing.sm),
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
                text = "${personATodayCourses.size + personBTodayCourses.size} 节",
                style = MaterialTheme.typography.labelMedium,
                color = labelsTertiary
            )
        }

        val displayModeOptions = listOf(
            SegmentOption(TodayCourseDisplayMode.SELF_ONLY, personBName.ifEmpty { "我" }),
            SegmentOption(TodayCourseDisplayMode.TA_ONLY, personAName.ifEmpty { "Ta" }),
            SegmentOption(TodayCourseDisplayMode.BOTH, "全部")
        )

        SegmentedControl(
            options = displayModeOptions,
            selectedOption = displayMode,
            onOptionSelected = onDisplayModeChange,
            modifier = Modifier.padding(bottom = Spacing.sm)
        )

        if (personATodayCourses.isEmpty() && personBTodayCourses.isEmpty()) {
            EmptyScheduleCard()
        } else {
            ScheduleList(
                personACourses = personATodayCourses,
                personBCourses = personBTodayCourses,
                displayMode = displayMode,
                currentHour = currentHour,
                currentMinute = currentMinute,
                periodTimesA = personAPeriodTimes,
                periodTimesB = personBPeriodTimes,
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
    val darkTheme = LocalDarkTheme.current
    val density = LocalDensity.current
    val labelsSecondary = getLabelsVibrantSecondary()
    val labelsTertiary = getLabelsVibrantTertiary()

    val layer1Tint = if (darkTheme) {
        LiquidGlassColors.BottomSheet.Dark.Layer1_Tint
    } else {
        LiquidGlassColors.BottomSheet.Light.Layer1_Tint
    }

    val layer1Alpha = if (darkTheme) {
        LiquidGlassColors.BottomSheet.Dark.Layer1_Alpha
    } else {
        LiquidGlassColors.BottomSheet.Light.Layer1_Alpha
    }

    val layer2Base = if (darkTheme) {
        LiquidGlassColors.BottomSheet.Dark.Layer2_Base
    } else {
        LiquidGlassColors.BottomSheet.Light.Layer2_Base
    }

    val glassEffect = if (darkTheme) {
        LiquidGlassColors.BottomSheet.Dark.GlassEffect
    } else {
        LiquidGlassColors.BottomSheet.Light.GlassEffect
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .drawBackdrop(
                backdrop = backdrop,
                shape = { ContinuousRoundedRectangle(BorderRadius.iOS26.large) },
                effects = {
                    vibrancy()
                    blur(with(density) { GlassBottomSheetDefaults.BlurRadius.toPx() })
                    lens(
                        refractionHeight = with(density) { GlassBottomSheetDefaults.LensRefractionHeight.toPx() },
                        refractionAmount = with(density) { GlassBottomSheetDefaults.LensRefractionAmount.toPx() },
                        chromaticAberration = true
                    )
                },
                onDrawSurface = {
                    drawRect(layer1Tint.copy(alpha = layer1Alpha))
                    drawRect(layer2Base, blendMode = BlendMode.ColorDodge)
                    drawRect(glassEffect)
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
