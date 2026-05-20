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
