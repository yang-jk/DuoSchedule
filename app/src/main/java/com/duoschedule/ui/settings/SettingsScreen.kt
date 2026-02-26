package com.duoschedule.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToUserSettings: () -> Unit,
    onNavigateToScheduleSettings: () -> Unit,
    onNavigateToDisplaySettings: () -> Unit,
    onNavigateToDataManagement: () -> Unit,
    onNavigateToNotificationSettings: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val personAName by viewModel.personAName.collectAsState()
    val personBName by viewModel.personBName.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // 小米风格设置卡片
            SettingsItemCard(
                title = "用户设置",
                subtitle = "设置我和${personAName}的名称",
                icon = Icons.Default.Person,
                onClick = onNavigateToUserSettings
            )

            SettingsItemCard(
                title = "课表设置",
                subtitle = "${personBName}和${personAName}的课表参数配置",
                icon = Icons.Default.Schedule,
                onClick = onNavigateToScheduleSettings
            )

            SettingsItemCard(
                title = "显示设置",
                subtitle = "控制课表显示方式",
                icon = Icons.Default.DisplaySettings,
                onClick = onNavigateToDisplaySettings
            )

            SettingsItemCard(
                title = "通知设置",
                subtitle = "课前提醒和岛通知配置",
                icon = Icons.Default.Notifications,
                onClick = onNavigateToNotificationSettings
            )

            SettingsItemCard(
                title = "数据管理",
                subtitle = "导入导出课表数据",
                icon = Icons.Default.DataUsage,
                onClick = onNavigateToDataManagement
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "双人课程表 v1.0.0",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun SettingsItemCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    // 小米风格设置卡片：纯白背景，12dp圆角，无阴影
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图标容器 - 小米风格
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(44.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "进入",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
