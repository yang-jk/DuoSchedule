package com.duoschedule.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Badge
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
import com.duoschedule.ui.settings.components.*
import com.duoschedule.ui.theme.*
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import dev.chrisbanes.haze.hazeSource
import top.yukonga.miuix.kmp.theme.MiuixTheme

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
    val hazeState = rememberHazeState()
    val scrollState = rememberScrollState()
    val scrollProgress by remember { derivedStateOf { (scrollState.value.toFloat() / 300f).coerceIn(0f, 1f) } }
    val blurActive = scrollProgress >= 0.5f
    val barColor = if (blurActive) Color.Transparent else if (scrollProgress >= 0.5f) MiuixTheme.colorScheme.surface else Color.Transparent

    CompositionLocalProvider(LocalHazeState provides hazeState) {
    Scaffold(
        topBar = {
            BlurredBar(null, blurActive) {
                SmallTopAppBar(
                    title = "更新日志",
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
    ) { innerPadding ->
        Box(modifier = Modifier.hazeSource(hazeState)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(innerPadding)
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
        }
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
