package com.duoschedule.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.duoschedule.ui.settings.components.*
import com.duoschedule.ui.theme.*
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

enum class ChangelogType {
    FEATURE, BUGFIX, BREAKING
}

data class ChangelogEntry(
    val version: String,
    val date: String,
    val type: ChangelogType,
    val summary: String
)

val changelogEntries = listOf(
    ChangelogEntry(
        version = "3.2.0",
        date = "2026-05-20",
        type = ChangelogType.FEATURE,
        summary = "新增关于页面，包含更新日志、用户协议和隐私政策、开源致谢；预测式返回开关移至外观与显示分组"
    ),
    ChangelogEntry(
        version = "3.1.1",
        date = "2026-05-20",
        type = ChangelogType.BUGFIX,
        summary = "修复应用内更新下载和安装功能多个严重问题"
    ),
    ChangelogEntry(
        version = "3.1.0",
        date = "2026-05-20",
        type = ChangelogType.FEATURE,
        summary = "重新梳理 Gitee 仓库与自动打包 APK 工作流"
    ),
    ChangelogEntry(
        version = "3.0.0",
        date = "2026-05-20",
        type = ChangelogType.BREAKING,
        summary = "统一版本号计算规则 + 修复 GitHub Actions 构建签名错误"
    ),
    ChangelogEntry(
        version = "1.16.3",
        date = "2026-05-19",
        type = ChangelogType.BUGFIX,
        summary = "修复应用内更新安装 APK 崩溃 + 优化更新日志格式"
    ),
    ChangelogEntry(
        version = "1.16.0",
        date = "2026-05-19",
        type = ChangelogType.FEATURE,
        summary = "重写 GitHub Actions 发布工作流与版本号计算规则"
    ),
    ChangelogEntry(
        version = "1.15.0",
        date = "2026-05-19",
        type = ChangelogType.FEATURE,
        summary = "GitHub Actions CI/CD 自动打包发布"
    )
)

@Composable
fun ChangelogScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text("更新日志") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        val scrollBackdrop = rememberLayerBackdrop()
        val scrollState = rememberScrollState()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .layerBackdrop(scrollBackdrop)
            ) {
                SettingsSection(title = null) {
                    changelogEntries.forEachIndexed { index, entry ->
                        ChangelogRow(
                            version = entry.version,
                            date = entry.date,
                            type = entry.type,
                            summary = entry.summary
                        )
                        if (index < changelogEntries.size - 1) {
                            Separator(modifier = Modifier.padding(horizontal = Spacing.lg))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(LiquidBottomTabsSpec.Height + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp))
            }

            ScrollTopBlurOverlay(backdrop = scrollBackdrop, scrollOffset = scrollState.value)
        }
    }
}

@Composable
fun ChangelogRow(
    version: String,
    date: String,
    type: ChangelogType,
    summary: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.md)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Text(
                text = version,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = getLabelsVibrantPrimary()
            )

            Spacer(modifier = Modifier.width(8.dp))

            Badge(
                containerColor = when (type) {
                    ChangelogType.FEATURE -> IOSColors.Blue
                    ChangelogType.BUGFIX -> IOSColors.Orange
                    ChangelogType.BREAKING -> IOSColors.Red
                },
                contentColor = Color.White
            ) {
                Text(
                    text = when (type) {
                        ChangelogType.FEATURE -> "功能增强"
                        ChangelogType.BUGFIX -> "Bug修复"
                        ChangelogType.BREAKING -> "重大变更"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }

        Text(
            text = date,
            style = MaterialTheme.typography.bodySmall,
            color = getLabelsVibrantSecondary(),
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Text(
            text = summary,
            style = MaterialTheme.typography.bodyMedium,
            color = getLabelsVibrantPrimary(),
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
        )
    }
}
