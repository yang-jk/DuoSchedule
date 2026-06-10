package com.duoschedule.ui.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duoschedule.data.model.AppThemeMode
import com.duoschedule.ui.settings.components.*
import com.duoschedule.ui.theme.*
import com.duoschedule.util.AppLogEntry
import com.duoschedule.util.AppLogger
import com.duoschedule.util.LogLevel
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop as kyantLayerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop as kyantRememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.basic.TabRowWithContour
import top.yukonga.miuix.kmp.window.WindowDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

data class LogLevelOption(val level: LogLevel?, val label: String)

@Composable
fun LogViewerScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val appThemeMode = LocalAppThemeMode.current
    val backdrop = LocalBackdrop.current ?: emptyBackdrop()

    val logLevelOptions = remember {
        listOf(
            LogLevelOption(null, "全部"),
            LogLevelOption(LogLevel.VERBOSE, "VERBOSE"),
            LogLevelOption(LogLevel.DEBUG, "DEBUG"),
            LogLevelOption(LogLevel.INFO, "INFO"),
            LogLevelOption(LogLevel.WARN, "WARN"),
            LogLevelOption(LogLevel.ERROR, "ERROR")
        )
    }

    var selectedLevelOption by remember { mutableStateOf(logLevelOptions.first()) }
    var logs by remember { mutableStateOf(AppLogger.logs) }

    // 监听日志变化
    LaunchedEffect(Unit) {
        val listener: () -> Unit = { logs = AppLogger.logs }
        AppLogger.addListener(listener)
        logs = AppLogger.logs
    }

    DisposableEffect(Unit) {
        val listener: () -> Unit = { logs = AppLogger.logs }
        AppLogger.addListener(listener)
        onDispose {
            AppLogger.removeListener(listener)
        }
    }

    val filteredLogs = remember(logs, selectedLevelOption) {
        if (selectedLevelOption.level == null) logs else logs.filter { it.level == selectedLevelOption.level }
    }

    val hazeState = rememberHazeState()
    val scrollState = rememberScrollState()

    val contentBackdrop = kyantRememberLayerBackdrop()
    val backgroundColor = MaterialTheme.colorScheme.surface
    val miuixBackdrop = rememberLayerBackdrop {
        drawRect(backgroundColor)
        drawContent()
    }

    val blurEnabled = scrollState.value > 0

    Scaffold(
        topBar = {
            BlurredBar(hazeState, backdrop = miuixBackdrop, enabled = blurEnabled) {
                SmallTopAppBar(
                    title = "查看日志",
                    scrollBehavior = MiuixScrollBehavior(),
                    color = Color.Transparent,
                    titleColor = MiuixTheme.colorScheme.onSurface,
                    defaultWindowInsetsPadding = false,
                    navigationIcon = {
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
                    .padding(top = innerPadding.calculateTopPadding(), bottom = innerPadding.calculateBottomPadding()),
                verticalArrangement = Arrangement.spacedBy(Spacing.iOS26.groupSpacing)
            ) {
                // 级别过滤
                if (appThemeMode == AppThemeMode.MIUIX) {
                    TabRowWithContour(
                        tabs = logLevelOptions.map { it.label },
                        selectedTabIndex = logLevelOptions.indexOf(selectedLevelOption),
                        onTabSelected = { index ->
                            selectedLevelOption = logLevelOptions[index]
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.lg)
                    )
                } else {
                    SegmentedControl(
                        options = logLevelOptions.map { SegmentOption(it, it.label) },
                        selectedOption = selectedLevelOption,
                        onOptionSelected = { selectedLevelOption = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.lg)
                    )
                }

                // 日志列表
                if (filteredLogs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Spacing.xl),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无日志记录",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    SettingsSection(title = "日志列表 (${filteredLogs.size})") {
                        Column(
                            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs),
                            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                        ) {
                            filteredLogs.take(100).forEach { log ->
                                LogItemView(log = log, appThemeMode = appThemeMode)
                            }
                            if (filteredLogs.size > 100) {
                                Text(
                                    text = "... 还有 ${filteredLogs.size - 100} 条日志未显示",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = Spacing.xs)
                                )
                            }
                        }
                    }
                }

                // 底部操作按钮
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    if (appThemeMode == AppThemeMode.MIUIX) {
                        Button(
                            onClick = {
                                val text = AppLogger.getLogsText(selectedLevelOption.level)
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("应用日志", text))
                                Toast.makeText(context, "日志已复制", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColorsPrimary()
                        ) {
                            top.yukonga.miuix.kmp.basic.Text("复制日志")
                        }
                        Button(
                            onClick = {
                                AppLogger.clear()
                                logs = AppLogger.logs
                                Toast.makeText(context, "日志已清空", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors()
                        ) {
                            top.yukonga.miuix.kmp.basic.Text("清空日志")
                        }
                    } else {
                        LiquidGlassButton(
                            text = "复制日志",
                            onClick = {
                                val text = AppLogger.getLogsText(selectedLevelOption.level)
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("应用日志", text))
                                Toast.makeText(context, "日志已复制", Toast.LENGTH_SHORT).show()
                            },
                            style = LiquidGlassButtonStyle.Tinted,
                            modifier = Modifier.weight(1f)
                        )
                        LiquidGlassButton(
                            text = "清空日志",
                            onClick = {
                                AppLogger.clear()
                                logs = AppLogger.logs
                                Toast.makeText(context, "日志已清空", Toast.LENGTH_SHORT).show()
                            },
                            style = LiquidGlassButtonStyle.NonTinted,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.xl))
                Spacer(modifier = Modifier.height(LiquidBottomTabsSpec.Height + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()))
            }
        }
    }
}

@Composable
private fun LogItemView(
    log: AppLogEntry,
    appThemeMode: AppThemeMode
) {
    val levelColor = when (log.level) {
        LogLevel.VERBOSE -> Color(0xFF9E9E9E)
        LogLevel.DEBUG -> Color(0xFF2196F3)
        LogLevel.INFO -> Color(0xFF4CAF50)
        LogLevel.WARN -> Color(0xFFFF9800)
        LogLevel.ERROR -> Color(0xFFF44336)
    }

    if (appThemeMode == AppThemeMode.MIUIX) {
        Surface(
            shape = RoundedCornerShape(BorderRadius.md),
            color = MiuixTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(Spacing.sm)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    Text(
                        text = log.formattedTime,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = levelColor.copy(alpha = 0.15f),
                        modifier = Modifier
                    ) {
                        Text(
                            text = log.level.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            ),
                            color = levelColor,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                    Text(
                        text = log.tag,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MiuixTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = log.message,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MiuixTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    } else {
        val labelsPrimary = getLabelsVibrantPrimary()
        val labelsSecondary = getLabelsVibrantSecondary()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                Text(
                    text = log.formattedTime,
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = labelsSecondary
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = levelColor.copy(alpha = 0.15f),
                    modifier = Modifier
                ) {
                    Text(
                        text = log.level.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        ),
                        color = levelColor,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
                Text(
                    text = log.tag,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = labelsPrimary
                )
            }
            Text(
                text = log.message,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = labelsPrimary,
                modifier = Modifier.padding(top = 1.dp)
            )
        }
    }
}
