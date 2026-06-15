package com.duoschedule.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import com.duoschedule.ui.theme.GlassSymbolIconButton
import com.duoschedule.ui.theme.GlassSymbolButtonStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duoschedule.data.model.AppThemeMode
import com.duoschedule.ui.theme.*
import com.kyant.backdrop.backdrops.layerBackdrop as kyantLayerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop as kyantRememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import top.yukonga.miuix.kmp.theme.MiuixTheme

enum class ChangelogType {
    FEATURE, ADDITION, BUGFIX, BREAKING
}

data class ChangelogItem(
    val type: ChangelogType,
    val summary: String
)

data class ChangelogEntry(
    val version: String,
    val date: String,
    val items: List<ChangelogItem>
)

@Composable
fun ChangelogScreen(
    onNavigateBack: () -> Unit
) {
    val hazeState = rememberHazeState()
    val scrollState = rememberScrollState()

    val contentBackdrop = kyantRememberLayerBackdrop()
    val backgroundColor = MaterialTheme.colorScheme.surface
    val miuixBackdrop = rememberLayerBackdrop {
        drawRect(backgroundColor)
        drawContent()
    }

    Scaffold(
        topBar = {
            BlurredBar(hazeState, backdrop = miuixBackdrop, contentBackdrop = contentBackdrop) {
                SmallTopAppBar(
                    title = "更新日志",
                    scrollBehavior = MiuixScrollBehavior(),
                    color = Color.Transparent,
                    titleColor = MiuixTheme.colorScheme.onSurface,
                    defaultWindowInsetsPadding = false,
                    navigationIcon = {
                        val appThemeMode = LocalAppThemeMode.current
                        if (appThemeMode == AppThemeMode.MIUIX) {
                            top.yukonga.miuix.kmp.basic.IconButton(onClick = onNavigateBack) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = "返回", tint = MiuixTheme.colorScheme.onSurface)
                            }
                        } else {
                            GlassSymbolIconButton(onClick = onNavigateBack, style = GlassSymbolButtonStyle.NonTinted) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = "返回")
                            }
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        Box(modifier = Modifier.hazeSource(hazeState).kyantLayerBackdrop(contentBackdrop).layerBackdrop(miuixBackdrop)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(
                        top = innerPadding.calculateTopPadding(),
                        bottom = innerPadding.calculateBottomPadding()
                    )
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                changelogEntries.forEachIndexed { index, entry ->
                    ChangelogVersionCard(
                        entry = entry,
                        isLast = index == changelogEntries.size - 1
                    )
                }

                Spacer(modifier = Modifier.height(LiquidBottomTabsSpec.Height + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp))
            }
        }
    }
}

@Composable
private fun ChangelogVersionCard(
    entry: ChangelogEntry,
    isLast: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    val miuixTextStyles = MiuixTheme.textStyles
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()

    top.yukonga.miuix.kmp.basic.Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = if (isLast) 0.dp else 12.dp),
        colors = top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors(),
        cornerRadius = 16.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = { expanded = !expanded })
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = entry.version,
                            color = labelsPrimary,
                            fontWeight = FontWeight.SemiBold,
                            style = miuixTextStyles.body1,
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = entry.date,
                        color = labelsSecondary,
                        style = miuixTextStyles.footnote1
                    )
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = labelsSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                            .height(0.5.dp)
                            .background(labelsSecondary.copy(alpha = 0.1f))
                    )

                    entry.items.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            val badgeColor = when (item.type) {
                                ChangelogType.FEATURE -> Color(0xFF4CAF50)
                                ChangelogType.ADDITION -> IOSColors.Blue
                                ChangelogType.BUGFIX -> Color(0xFF9E9E9E)
                                ChangelogType.BREAKING -> Color(0xFFF44336)
                            }
                            val badgeText = when (item.type) {
                                ChangelogType.FEATURE -> "优化"
                                ChangelogType.ADDITION -> "新增"
                                ChangelogType.BUGFIX -> "修复"
                                ChangelogType.BREAKING -> "重大"
                            }

                            Box(
                                modifier = Modifier
                                    .padding(top = 2.dp, end = 8.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(badgeColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = badgeText,
                                    fontWeight = FontWeight.Medium,
                                    color = badgeColor,
                                    style = miuixTextStyles.footnote2
                                )
                            }

                            Text(
                                text = item.summary,
                                color = labelsSecondary,
                                modifier = Modifier.weight(1f),
                                lineHeight = 20.sp,
                                style = miuixTextStyles.body2
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChangelogPageTag(
    text: String,
    color: Color
) {
    val miuixTextStyles = MiuixTheme.textStyles
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 5.dp, vertical = 1.dp)
    ) {
        Text(
            text = text,
            style = miuixTextStyles.footnote2,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}
