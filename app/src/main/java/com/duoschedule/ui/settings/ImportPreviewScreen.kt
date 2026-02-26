package com.duoschedule.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.duoschedule.data.importexport.CourseImportData
import com.duoschedule.data.model.PersonType
import com.duoschedule.ui.theme.getDialogBackgroundColor
import kotlinx.coroutines.launch

data class ImportPreviewItem(
    val data: CourseImportData,
    val isSelected: Boolean = true,
    val hasConflict: Boolean = false,
    val conflictReason: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportPreviewScreen(
    courses: List<CourseImportData>,
    onConfirm: (selectedCourses: List<CourseImportData>, targetPerson: PersonType, mergeMode: Boolean) -> Unit,
    onDismiss: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    onImportSuccess: ((PersonType) -> Unit)? = null
) {
    val previewItems = remember(courses) {
        mutableStateListOf(
            *courses.map { ImportPreviewItem(data = it) }.toTypedArray()
        )
    }
    
    var selectedTarget by remember { mutableStateOf<PersonType?>(null) }
    var mergeMode by remember { mutableStateOf(true) }
    var showTargetDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var importedTarget by remember { mutableStateOf<PersonType?>(null) }
    var importedCount by remember { mutableStateOf(0) }
    var isImporting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    val conflictCount = previewItems.count { it.hasConflict }
    val selectedCount = previewItems.count { it.isSelected }
    val totalCount = previewItems.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("导入预览") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { showTargetDialog = true },
                        enabled = selectedCount > 0
                    ) {
                        Text("确认导入")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 小米风格统计卡片
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("共 $totalCount 门课程")
                        Text(
                            "已选择 $selectedCount 门",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    if (conflictCount > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                "$conflictCount 门课程存在时间冲突",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { 
                                previewItems.forEachIndexed { index, _ ->
                                    previewItems[index] = previewItems[index].copy(isSelected = true)
                                }
                            },
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text("全选")
                        }
                        OutlinedButton(
                            onClick = { 
                                previewItems.forEachIndexed { index, _ ->
                                    previewItems[index] = previewItems[index].copy(isSelected = false)
                                }
                            },
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text("取消全选")
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(previewItems) { item ->
                    ImportPreviewItemCard(
                        item = item,
                        onToggleSelection = {
                            val index = previewItems.indexOf(item)
                            if (index >= 0) {
                                previewItems[index] = item.copy(isSelected = !item.isSelected)
                            }
                        }
                    )
                }
            }
        }
    }

    if (showTargetDialog) {
        AlertDialog(
            onDismissRequest = { showTargetDialog = false },
            containerColor = getDialogBackgroundColor(),
            shape = MaterialTheme.shapes.large,
            title = { Text("选择导入目标") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("请选择要将课程导入到哪个课表：")
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedTarget == PersonType.PERSON_A,
                            onClick = { selectedTarget = PersonType.PERSON_A },
                            label = { Text("Ta的课表") }
                        )
                        FilterChip(
                            selected = selectedTarget == PersonType.PERSON_B,
                            onClick = { selectedTarget = PersonType.PERSON_B },
                            label = { Text("我的课表") }
                        )
                    }
                    
                    Divider()
                    
                    Text("冲突处理方式：")
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = mergeMode,
                            onClick = { mergeMode = true },
                            label = { Text("合并（保留现有）") }
                        )
                        FilterChip(
                            selected = !mergeMode,
                            onClick = { mergeMode = false },
                            label = { Text("覆盖现有数据") }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedTarget?.let { target ->
                            val selectedCourses = previewItems
                                .filter { it.isSelected }
                                .map { it.data.copy(personType = target) }
                            
                            scope.launch {
                                isImporting = true
                                val result = viewModel.saveImportedCourses(selectedCourses, target, mergeMode)
                                isImporting = false
                                
                                if (result.success) {
                                    importedTarget = target
                                    importedCount = result.importedCount
                                    showTargetDialog = false
                                    showSuccessDialog = true
                                    onConfirm(selectedCourses, target, mergeMode)
                                }
                            }
                        }
                    },
                    enabled = selectedTarget != null && !isImporting
                ) {
                    if (isImporting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("确认导入")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showTargetDialog = false },
                    enabled = !isImporting
                ) {
                    Text("取消")
                }
            }
        )
    }
    
    if (showSuccessDialog && importedTarget != null) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                onDismiss()
            },
            containerColor = getDialogBackgroundColor(),
            shape = MaterialTheme.shapes.large,
            title = { Text("导入成功") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("成功导入 $importedCount 门课程到${if (importedTarget == PersonType.PERSON_A) "Ta的课表" else "我的课表"}")
                }
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = {
                        showSuccessDialog = false
                        onDismiss()
                    }) {
                        Text("返回主页")
                    }
                    FilledTonalButton(onClick = {
                        showSuccessDialog = false
                        onImportSuccess?.invoke(importedTarget!!)
                        onDismiss()
                    }) {
                        Text("查看课表")
                    }
                }
            }
        )
    }
}

@Composable
private fun ImportPreviewItemCard(
    item: ImportPreviewItem,
    onToggleSelection: () -> Unit
) {
    // 小米风格卡片
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = item.isSelected,
                onValueChange = { onToggleSelection() }
            ),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (item.hasConflict) {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isSelected,
                onCheckedChange = { onToggleSelection() }
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = item.data.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    if (item.hasConflict) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "冲突",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = getDayOfWeekString(item.data.dayOfWeek),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${String.format("%02d", item.data.startHour)}:${String.format("%02d", item.data.startMinute)}-${String.format("%02d", item.data.endHour)}:${String.format("%02d", item.data.endMinute)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (item.data.location.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = item.data.location,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                val weekText = when (item.data.weekType) {
                    com.duoschedule.data.model.WeekType.ALL -> "${item.data.startWeek}-${item.data.endWeek}周"
                    com.duoschedule.data.model.WeekType.ODD -> "单周"
                    com.duoschedule.data.model.WeekType.EVEN -> "双周"
                    com.duoschedule.data.model.WeekType.CUSTOM -> item.data.customWeeks.ifEmpty { "自定义周次" }
                }
                Text(
                    text = weekText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (item.hasConflict && item.conflictReason.isNotEmpty()) {
                    Text(
                        text = item.conflictReason,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

private fun getDayOfWeekString(dayOfWeek: Int): String {
    return when (dayOfWeek) {
        1 -> "周一"
        2 -> "周二"
        3 -> "周三"
        4 -> "周四"
        5 -> "周五"
        6 -> "周六"
        7 -> "周日"
        else -> "未知"
    }
}
