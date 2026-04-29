package com.duoschedule.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.duoschedule.ui.settings.components.*
import com.duoschedule.ui.theme.*

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
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsTertiary = getLabelsVibrantTertiary()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.iOS26.groupSpacing)
        ) {
            Spacer(modifier = Modifier.height(Spacing.sm))

            Text(
                text = "设置",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = labelsPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = Spacing.lg,
                        end = Spacing.lg,
                        top = Spacing.lg,
                        bottom = Spacing.xs
                    )
            )

            SettingsSection {
                SettingsNavigationRow(
                    title = "用户设置",
                    subtitle = "设置我和${personAName}的名称",
                    icon = Icons.Outlined.Person,
                    iconBackgroundColor = IOSColors.Blue,
                    onClick = onNavigateToUserSettings
                )
                
                Separator(modifier = Modifier.padding(horizontal = Spacing.lg))
                
                SettingsNavigationRow(
                    title = "课表设置",
                    subtitle = "${personBName}和${personAName}的课表参数配置",
                    icon = Icons.Outlined.Schedule,
                    iconBackgroundColor = IOSColors.Orange,
                    onClick = onNavigateToScheduleSettings
                )
            }

            SettingsSection {
                SettingsNavigationRow(
                    title = "显示设置",
                    subtitle = "控制课表显示方式",
                    icon = Icons.Outlined.DisplaySettings,
                    iconBackgroundColor = IOSColors.Indigo,
                    onClick = onNavigateToDisplaySettings
                )
                
                Separator(modifier = Modifier.padding(horizontal = Spacing.lg))
                
                SettingsNavigationRow(
                    title = "通知设置",
                    subtitle = "课前提醒和岛通知配置",
                    icon = Icons.Outlined.Notifications,
                    iconBackgroundColor = IOSColors.Red,
                    onClick = onNavigateToNotificationSettings
                )
                
                Separator(modifier = Modifier.padding(horizontal = Spacing.lg))
                
                SettingsNavigationRow(
                    title = "数据管理",
                    subtitle = "导入导出课表数据",
                    icon = Icons.Outlined.DataUsage,
                    iconBackgroundColor = IOSColors.Green,
                    onClick = onNavigateToDataManagement
                )
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
