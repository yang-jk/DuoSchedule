package com.duoschedule.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.duoschedule.data.model.PersonType
import com.duoschedule.ui.settings.components.TimeRangeInputDialog

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
    
    var editedTimes by remember(periodTimes, totalPeriods) { 
        mutableStateOf(
            if (periodTimes.isEmpty()) {
                (1..totalPeriods).map { "08:00-08:45" }
            } else {
                periodTimes.take(totalPeriods)
            }
        )
    }
    
    var hasChanges by remember { mutableStateOf(false) }
    var editingIndex by remember { mutableStateOf<Int?>(null) }
    
    val defaultTimes = remember(totalPeriods) {
        generateDefaultTimes(totalPeriods)
    }
    
    LaunchedEffect(periodTimes, totalPeriods) {
        editedTimes = if (periodTimes.isEmpty()) {
            defaultTimes
        } else {
            periodTimes.take(totalPeriods)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${personName}的时间设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.background,
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = {
                        viewModel.setPeriodTimes(personType, editedTimes)
                        onNavigateBack()
                    },
                    enabled = hasChanges,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("保存")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // 小米风格说明卡片
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "每节课时间设置",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "点击时间可编辑，格式: HH:mm-HH:mm",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                itemsIndexed(editedTimes) { index, time ->
                    PeriodTimeItem(
                        periodIndex = index,
                        time = time,
                        onClick = {
                            editingIndex = index
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
        }
    }
    
    editingIndex?.let { index ->
        TimeRangeInputDialog(
            title = "编辑时间",
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
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PeriodTimeItem(
    periodIndex: Int,
    time: String,
    onClick: () -> Unit
) {
    // 小米风格列表项
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "第${periodIndex + 1}节",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = time.ifEmpty { "未设置" },
                style = MaterialTheme.typography.bodyLarge,
                color = if (time.isEmpty()) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
        }
    }
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
