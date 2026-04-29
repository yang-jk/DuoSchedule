package com.duoschedule.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.duoschedule.data.model.PersonType
import com.duoschedule.ui.settings.components.*
import com.duoschedule.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
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
    
    val labelsPrimary = getLabelsVibrantPrimary()
    
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
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "${personName}的时间设置",
                        color = labelsPrimary,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    ) 
                },
                navigationIcon = {
                    GlassSymbolIconButton(
                        onClick = onNavigateBack,
                        style = GlassSymbolButtonStyle.NonTinted,
                        size = ComponentSize.LiquidGlassButton.TopAppBarIconButtonSize,
                        contentPadding = PaddingValues(start = Spacing.sm)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "返回",
                            tint = labelsPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (hasChanges) {
                BottomAppBar(
                    containerColor = MaterialTheme.colorScheme.background,
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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(Spacing.iOS26.groupSpacing)
        ) {
            Spacer(modifier = Modifier.height(Spacing.sm))

            SettingsFooter(
                text = "点击时间可编辑每节课的开始和结束时间。"
            )

            SettingsCard {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
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

            Spacer(modifier = Modifier.weight(1f))
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
