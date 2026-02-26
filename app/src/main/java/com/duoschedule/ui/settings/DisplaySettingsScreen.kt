package com.duoschedule.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.duoschedule.data.model.ThemeMode
import com.duoschedule.data.model.TodayCourseDisplayMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplaySettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val showNonCurrentWeekCourses by viewModel.showNonCurrentWeekCourses.collectAsState()
    val showSaturday by viewModel.showSaturday.collectAsState()
    val showSunday by viewModel.showSunday.collectAsState()
    val todayCourseDisplayMode by viewModel.todayCourseDisplayMode.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    
    var showDisplayModeDialog by remember { mutableStateOf(false) }
    var showThemeModeDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("显示设置") },
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
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "主题设置",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DarkMode,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("深色模式")
                            }
                            Text(
                                "选择应用的显示主题",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 28.dp)
                            )
                        }
                        Text(
                            text = when (themeMode) {
                                ThemeMode.FOLLOW_SYSTEM -> "跟随系统"
                                ThemeMode.LIGHT -> "浅色模式"
                                ThemeMode.DARK -> "深色模式"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    
                    TextButton(
                        onClick = { showThemeModeDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("选择主题模式")
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "主页显示设置",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("今日课程显示模式")
                            Text(
                                "选择下方课程列表显示的课表（当前课程始终显示两人）",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = when (todayCourseDisplayMode) {
                                TodayCourseDisplayMode.SELF_ONLY -> "仅我的课表"
                                TodayCourseDisplayMode.TA_ONLY -> "仅Ta的课表"
                                TodayCourseDisplayMode.BOTH -> "都显示"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    
                    TextButton(
                        onClick = { showDisplayModeDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("选择显示模式")
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "课表外观设置",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("显示周六")
                            Text(
                                "在课表中显示周六列",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = showSaturday,
                            onCheckedChange = { viewModel.setShowSaturday(it) }
                        )
                    }

                    Divider(modifier = Modifier.padding(vertical = 4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("显示周日")
                            Text(
                                "在课表中显示周日列",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = showSunday,
                            onCheckedChange = { viewModel.setShowSunday(it) }
                        )
                    }

                    Divider(modifier = Modifier.padding(vertical = 4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("显示非本周课程")
                            Text(
                                "开启后显示所有课程，非本周课程以灰色显示",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = showNonCurrentWeekCourses,
                            onCheckedChange = { viewModel.setShowNonCurrentWeekCourses(it) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }

    if (showDisplayModeDialog) {
        TodayCourseDisplayModeDialog(
            currentMode = todayCourseDisplayMode,
            onDismiss = { showDisplayModeDialog = false },
            onConfirm = { mode ->
                viewModel.setTodayCourseDisplayMode(mode)
                showDisplayModeDialog = false
            }
        )
    }

    if (showThemeModeDialog) {
        ThemeModeDialog(
            currentMode = themeMode,
            onDismiss = { showThemeModeDialog = false },
            onConfirm = { mode ->
                viewModel.setThemeMode(mode)
                showThemeModeDialog = false
            }
        )
    }
}

@Composable
private fun TodayCourseDisplayModeDialog(
    currentMode: TodayCourseDisplayMode,
    onDismiss: () -> Unit,
    onConfirm: (TodayCourseDisplayMode) -> Unit
) {
    var selectedMode by remember { mutableStateOf(currentMode) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("今日课程显示模式") },
        text = {
            Column(modifier = Modifier.selectableGroup()) {
                TodayCourseDisplayMode.values().forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (mode == selectedMode),
                                onClick = { selectedMode = mode },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (mode == selectedMode),
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = when (mode) {
                                    TodayCourseDisplayMode.SELF_ONLY -> "仅显示我的课表"
                                    TodayCourseDisplayMode.TA_ONLY -> "仅显示Ta的课表"
                                    TodayCourseDisplayMode.BOTH -> "都显示"
                                }
                            )
                            Text(
                                text = when (mode) {
                                    TodayCourseDisplayMode.SELF_ONLY -> "主页只显示我的课程信息"
                                    TodayCourseDisplayMode.TA_ONLY -> "主页只显示Ta的课程信息"
                                    TodayCourseDisplayMode.BOTH -> "主页同时显示两人的课程信息"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedMode) }) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun ThemeModeDialog(
    currentMode: ThemeMode,
    onDismiss: () -> Unit,
    onConfirm: (ThemeMode) -> Unit
) {
    var selectedMode by remember { mutableStateOf(currentMode) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("深色模式") },
        text = {
            Column(modifier = Modifier.selectableGroup()) {
                ThemeMode.values().forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (mode == selectedMode),
                                onClick = { selectedMode = mode },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (mode == selectedMode),
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = when (mode) {
                                    ThemeMode.FOLLOW_SYSTEM -> "跟随系统"
                                    ThemeMode.LIGHT -> "浅色模式"
                                    ThemeMode.DARK -> "深色模式"
                                }
                            )
                            Text(
                                text = when (mode) {
                                    ThemeMode.FOLLOW_SYSTEM -> "应用主题跟随系统设置自动切换"
                                    ThemeMode.LIGHT -> "应用始终使用浅色主题"
                                    ThemeMode.DARK -> "应用始终使用深色主题"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedMode) }) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
