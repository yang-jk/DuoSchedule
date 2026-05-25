package com.duoschedule.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.duoschedule.data.model.PersonType
import com.duoschedule.ui.settings.components.*
import com.duoschedule.ui.theme.*
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import dev.chrisbanes.haze.hazeSource
import top.yukonga.miuix.kmp.theme.MiuixTheme
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.rememberModalBottomSheetState

@Composable
fun PeriodTimesSettingsScreen(
    personType: PersonType,
    personName: String,
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val totalPeriods by viewModel.getTotalPeriods(personType).collectAsState()
    val periodTimes by viewModel.getPeriodTimes(personType).collectAsState()
    
    var editedTimes by remember { mutableStateOf<List<String>>(emptyList()) }
    var hasChanges by remember { mutableStateOf(false) }
    var editingIndex by remember { mutableStateOf<Int?>(null) }
    var lastTotalPeriods by remember { mutableStateOf<Int?>(null) }
    
    val timePickerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    LaunchedEffect(periodTimes, totalPeriods) {
        val shouldReinitialize = lastTotalPeriods != null && lastTotalPeriods != totalPeriods
        
        if (editedTimes.isEmpty() || shouldReinitialize || !hasChanges) {
            val defaultTimes = generateDefaultTimes(totalPeriods)
            editedTimes = if (periodTimes.isEmpty()) {
                defaultTimes
            } else {
                val existingTimes = periodTimes.take(totalPeriods)
                val missingCount = totalPeriods - existingTimes.size
                if (missingCount > 0) {
                    existingTimes + defaultTimes.drop(existingTimes.size).take(missingCount)
                } else {
                    existingTimes
                }
            }
        }
        lastTotalPeriods = totalPeriods
    }
    
    val hazeState = rememberHazeState()
    val lazyListState = rememberLazyListState()
    val scrollOffset by remember {
        derivedStateOf {
            if (lazyListState.firstVisibleItemIndex > 0) {
                lazyListState.firstVisibleItemScrollOffset + 1
            } else {
                lazyListState.firstVisibleItemScrollOffset
            }
        }
    }
    val scrollProgress by remember { derivedStateOf { (scrollOffset.toFloat() / 300f).coerceIn(0f, 1f) } }
    val blurActive = scrollProgress >= 0.5f
    val barColor = if (blurActive) Color.Transparent else if (scrollProgress >= 0.5f) MiuixTheme.colorScheme.surface else Color.Transparent

    CompositionLocalProvider(LocalHazeState provides hazeState) {
    Scaffold(
        topBar = {
            BlurredBar(null, blurActive) {
                SmallTopAppBar(
                    title = "${personName}的时间设置",
                    scrollBehavior = MiuixScrollBehavior(),
                    color = barColor,
                    titleColor = MiuixTheme.colorScheme.onSurface.copy(alpha = ((scrollProgress - 0.35f) / 0.65f).coerceIn(0f, 1f)),
                    defaultWindowInsetsPadding = false,
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Outlined.ArrowBack, contentDescription = "返回")
                        }
                    },
                )
            }
        },
        bottomBar = {
            if (hasChanges) {
                BottomAppBar(
                    containerColor = MaterialTheme.colorScheme.background,
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    LiquidGlassButton(
                        text = "保存",
                        onClick = {
                            viewModel.setPeriodTimes(personType, editedTimes)
                            onNavigateBack()
                        },
                        modifier = Modifier.padding(horizontal = Spacing.lg),
                        style = LiquidGlassButtonStyle.Tinted
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.hazeSource(hazeState)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(Spacing.iOS26.groupSpacing)
            ) {
            Spacer(modifier = Modifier.height(Spacing.sm))

            SettingsFooter(
                text = "点击时间可编辑每节课的开始和结束时间。"
            )

            SettingsCard {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    state = lazyListState,
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    itemsIndexed(editedTimes) { index, time ->
                        PeriodTimeItem(
                            periodIndex = index,
                            time = time,
                            onClick = {
                                editingIndex = index
                            }
                        )
                        
                        if (index < editedTimes.size - 1) {
                            Separator(modifier = Modifier.padding(horizontal = Spacing.lg))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
            Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
    }
    
    editingIndex?.let { index ->
        TimeRangeBottomSheet(
            periodIndex = index,
            initialValue = editedTimes.getOrElse(index) { "08:00-08:45" },
            onDismiss = { editingIndex = null },
            onConfirm = { newTime ->
                val newTimes = editedTimes.toMutableList()
                if (index < newTimes.size) {
                    newTimes[index] = newTime
                } else {
                    while (newTimes.size <= index) {
                        newTimes.add("08:00-08:45")
                    }
                    newTimes[index] = newTime
                }
                editedTimes = newTimes
                hasChanges = true
                editingIndex = null
            },
            sheetState = timePickerSheetState
        )
    }
}

@Composable
private fun PeriodTimeItem(
    periodIndex: Int,
    time: String,
    onClick: () -> Unit
) {
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()
    
    SettingsNavigationRow(
        title = "第${periodIndex + 1}节",
        icon = Icons.Outlined.Schedule,
        iconBackgroundColor = IOSColors.Blue,
        value = time.ifEmpty { "未设置" },
        onClick = onClick
    )
}

private fun generateDefaultTimes(totalPeriods: Int): List<String> {
    val times = mutableListOf<String>()
    var currentHour = 8
    var currentMinute = 0
    
    for (i in 0 until totalPeriods) {
        val startHour = currentHour
        val startMinute = currentMinute
        
        currentMinute += 45
        if (currentMinute >= 60) {
            currentHour++
            currentMinute -= 60
        }
        
        val endHour = currentHour
        val endMinute = currentMinute
        
        val startTime = "${startHour.toString().padStart(2, '0')}:${startMinute.toString().padStart(2, '0')}"
        val endTime = "${endHour.toString().padStart(2, '0')}:${endMinute.toString().padStart(2, '0')}"
        times.add("$startTime-$endTime")
        
        currentMinute += 10
        if (currentMinute >= 60) {
            currentHour++
            currentMinute -= 60
        }
    }
    
    return times
}
