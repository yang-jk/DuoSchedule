package com.duoschedule.ui.update

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Badge
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.duoschedule.data.model.AppThemeMode
import com.duoschedule.data.update.UpdateInfo
import com.duoschedule.ui.settings.ChangelogType
import com.duoschedule.ui.theme.BrandColors
import com.duoschedule.ui.theme.IOSColors
import com.duoschedule.ui.theme.LocalAppThemeMode
import com.duoschedule.ui.theme.LocalDarkTheme
import com.duoschedule.ui.theme.Spacing
import com.duoschedule.ui.theme.getLabelsVibrantPrimary
import com.duoschedule.ui.theme.getLabelsVibrantSecondary
import top.yukonga.miuix.kmp.basic.Button as MiuixButton
import top.yukonga.miuix.kmp.basic.ButtonDefaults as MiuixButtonDefaults
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.util.Locale

@Composable
private fun MiuixTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    MiuixButton(
        onClick = onClick,
        modifier = modifier,
        colors = MiuixButtonDefaults.buttonColors()
    ) {
        Text(text = text)
    }
}

@Composable
private fun MiuixFilledButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    MiuixButton(
        onClick = onClick,
        modifier = modifier,
        colors = MiuixButtonDefaults.buttonColorsPrimary()
    ) {
        Text(text = text, color = Color.White)
    }
}

@Composable
fun UpdateDialog(
    onDismiss: () -> Unit,
    viewModel: UpdateViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    val isForceUpdate = when (val state = uiState) {
        is UpdateUiState.UpdateAvailable -> state.isForceUpdate
        is UpdateUiState.ReadyToInstall -> state.isForceUpdate
        else -> false
    }

    val appThemeMode = LocalAppThemeMode.current
    val darkTheme = LocalDarkTheme.current

    val cardBackground = if (darkTheme) {
        Color(0xFF1E1E1E)
    } else {
        Color(0xFFFFFFFF)
    }
    val cardShape = RoundedCornerShape(20.dp)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = !isForceUpdate,
            dismissOnClickOutside = !isForceUpdate,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .fillMaxWidth()
                .clip(cardShape)
                .background(cardBackground)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is UpdateUiState.Idle -> {}
                is UpdateUiState.Checking -> {}
                is UpdateUiState.NoUpdate -> {}
                is UpdateUiState.Error -> {}
                is UpdateUiState.UpdateAvailable -> UpdateAvailableContent(
                    info = state.info,
                    isForceUpdate = state.isForceUpdate,
                    appThemeMode = appThemeMode,
                    darkTheme = darkTheme,
                    onDownload = { viewModel.startDownload(context) },
                    onSkip = {
                        viewModel.skipVersion()
                        onDismiss()
                    },
                    onBrowserDownload = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(state.info.downloadUrl))
                        context.startActivity(intent)
                    }
                )
                is UpdateUiState.Downloading -> DownloadingContent(
                    percent = state.percent,
                    downloadedBytes = state.downloadedBytes,
                    totalBytes = state.totalBytes,
                    onCancel = { viewModel.cancelDownload() }
                )
                is UpdateUiState.ReadyToInstall -> ReadyToInstallContent(
                    isForceUpdate = state.isForceUpdate,
                    onInstall = { viewModel.installApk(context) }
                )
            }
        }
    }
}

@Composable
private fun CheckingContent() {
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 176.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(40.dp),
            strokeWidth = 3.dp,
            color = BrandColors.Primary
        )

        Spacer(modifier = Modifier.height(Spacing.md))

        Text(
            text = "正在检查更新",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = labelsPrimary
        )

        Spacer(modifier = Modifier.height(Spacing.xs))

        Text(
            text = "请稍候...",
            style = MaterialTheme.typography.bodyMedium,
            color = labelsSecondary
        )
    }
}

@Composable
private fun UpdateAvailableContent(
    info: UpdateInfo,
    isForceUpdate: Boolean,
    appThemeMode: AppThemeMode,
    darkTheme: Boolean,
    onDownload: () -> Unit,
    onSkip: () -> Unit,
    onBrowserDownload: () -> Unit
) {
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "发现新版本 ",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = labelsPrimary
        )

        Text(
            text = "v${info.latestVersion}",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = IOSColors.Blue
        )

        Spacer(modifier = Modifier.height(Spacing.xs))

        val subtitleParts = mutableListOf<String>()
        if (info.size.isNotBlank()) subtitleParts.add("大小：${info.size}")
        if (info.date.isNotBlank()) subtitleParts.add(info.date)
        val subtitle = subtitleParts.joinToString(" · ")

        if (subtitle.isNotBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = labelsSecondary
            )
        }

        if (isForceUpdate) {
            Spacer(modifier = Modifier.height(Spacing.xs))
            Text(
                text = "此版本为重要更新，需要更新后才能继续使用",
                style = MaterialTheme.typography.bodySmall,
                color = IOSColors.Red
            )
        }

        Spacer(modifier = Modifier.height(Spacing.md))

        Text(
            text = "更新内容",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = labelsPrimary
        )

        Spacer(modifier = Modifier.height(Spacing.xs))

        val changelogBg = if (darkTheme) Color(0xFF2A2A2A) else Color(0xFFF5F5F5)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 180.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(changelogBg)
                .padding(Spacing.md)
                .verticalScroll(rememberScrollState())
        ) {
            val lines = info.releaseNotes.lines().filter { it.isNotBlank() }
            if (lines.isNotEmpty()) {
                lines.forEach { line ->
                    ChangelogLineItem(line = line, appThemeMode = appThemeMode)
                }
            } else {
                Text(
                    text = info.releaseNotes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = labelsSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.lg))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (appThemeMode == AppThemeMode.MIUIX) {
                MiuixTextButton(
                    text = "稍后",
                    onClick = onSkip,
                    modifier = Modifier.weight(1f)
                )
                MiuixFilledButton(
                    text = "下载",
                    onClick = onDownload,
                    modifier = Modifier.weight(1f)
                )
            } else {
                androidx.compose.material3.TextButton(
                    onClick = onSkip,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("稍后", color = labelsSecondary)
                }
                androidx.compose.material3.Button(
                    onClick = onDownload,
                    modifier = Modifier.weight(1f),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = IOSColors.Blue,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("下载")
                }
            }
        }

        if (!isForceUpdate) {
            Spacer(modifier = Modifier.height(Spacing.sm))
            Text(
                text = "浏览器下载",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = IOSColors.Blue,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable(onClick = onBrowserDownload)
                    .padding(vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun ChangelogLineItem(
    line: String,
    appThemeMode: AppThemeMode
) {
    val labelsSecondary = getLabelsVibrantSecondary()
    val trimmed = line.trim()

    val (badgeText, badgeColor, contentText) = when {
        trimmed.startsWith("[优化]") || trimmed.startsWith("【优化】") -> {
            Triple("优化", Color(0xFF4CAF50), trimmed.substringAfter("[优化]").substringAfter("【优化】").trim())
        }
        trimmed.startsWith("[新增]") || trimmed.startsWith("【新增】") -> {
            Triple("新增", IOSColors.Blue, trimmed.substringAfter("[新增]").substringAfter("【新增】").trim())
        }
        trimmed.startsWith("[修复]") || trimmed.startsWith("【修复】") -> {
            Triple("修复", Color(0xFF9E9E9E), trimmed.substringAfter("[修复]").substringAfter("【修复】").trim())
        }
        trimmed.startsWith("[重大]") || trimmed.startsWith("【重大】") -> {
            Triple("重大", Color(0xFFF44336), trimmed.substringAfter("[重大]").substringAfter("【重大】").trim())
        }
        else -> Triple(null, Color.Transparent, trimmed)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        if (badgeText != null) {
            if (appThemeMode == AppThemeMode.MIUIX) {
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp, end = 6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(badgeColor.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = badgeColor
                    )
                }
            } else {
                Badge(
                    containerColor = badgeColor,
                    contentColor = Color.White,
                    modifier = Modifier.padding(top = 2.dp, end = 6.dp)
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .padding(top = 6.dp, end = 8.dp)
                    .size(5.dp)
                    .background(labelsSecondary.copy(alpha = 0.5f), RoundedCornerShape(50))
            )
        }

        Text(
            text = contentText,
            style = MaterialTheme.typography.bodyMedium,
            color = labelsSecondary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun DownloadingContent(
    percent: Int,
    downloadedBytes: Long,
    totalBytes: Long,
    onCancel: () -> Unit
) {
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()
    val appThemeMode = LocalAppThemeMode.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "正在下载更新...",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = labelsPrimary
        )

        Spacer(modifier = Modifier.height(Spacing.lg))

        if (percent >= 0) {
            LinearProgressIndicator(
                progress = { percent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = BrandColors.Primary,
                trackColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant,
            )
        } else {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = BrandColors.Primary,
                trackColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant,
            )
        }

        Spacer(modifier = Modifier.height(Spacing.sm))

        Text(
            text = if (percent >= 0) "$percent%  ·  ${formatFileSize(downloadedBytes)} / ${formatFileSize(totalBytes)}" else "${formatFileSize(downloadedBytes)} 已下载",
            style = MaterialTheme.typography.bodySmall,
            color = labelsSecondary
        )

        Spacer(modifier = Modifier.height(Spacing.lg))

        if (appThemeMode == AppThemeMode.MIUIX) {
            MiuixTextButton(
                text = "取消下载",
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            androidx.compose.material3.TextButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("取消下载", color = labelsSecondary)
            }
        }
    }
}

@Composable
private fun ReadyToInstallContent(
    isForceUpdate: Boolean,
    onInstall: () -> Unit
) {
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()
    val appThemeMode = LocalAppThemeMode.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = IOSColors.Green
        )

        Spacer(modifier = Modifier.height(Spacing.md))

        Text(
            text = "下载完成",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = labelsPrimary
        )

        Spacer(modifier = Modifier.height(Spacing.xs))

        Text(
            text = "点击安装以完成更新",
            style = MaterialTheme.typography.bodyMedium,
            color = labelsSecondary
        )

        Spacer(modifier = Modifier.height(Spacing.lg))

        if (appThemeMode == AppThemeMode.MIUIX) {
            MiuixFilledButton(
                text = "安装更新",
                onClick = onInstall,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            androidx.compose.material3.Button(
                onClick = onInstall,
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = IOSColors.Blue,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("安装更新")
            }
        }
    }
}

@Composable
private fun NoUpdateContent(
    onDismiss: () -> Unit
) {
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()
    val appThemeMode = LocalAppThemeMode.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = IOSColors.Green
        )

        Spacer(modifier = Modifier.height(Spacing.md))

        Text(
            text = "已是最新版本",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = labelsPrimary
        )

        Spacer(modifier = Modifier.height(Spacing.xs))

        Text(
            text = "当前没有可用的更新",
            style = MaterialTheme.typography.bodyMedium,
            color = labelsSecondary
        )

        Spacer(modifier = Modifier.height(Spacing.lg))

        if (appThemeMode == AppThemeMode.MIUIX) {
            MiuixFilledButton(
                text = "好的",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            androidx.compose.material3.Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = IOSColors.Blue,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("好的")
            }
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()
    val appThemeMode = LocalAppThemeMode.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Filled.SystemUpdate,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = IOSColors.Red
        )

        Spacer(modifier = Modifier.height(Spacing.md))

        Text(
            text = "检查更新失败",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = labelsPrimary
        )

        Spacer(modifier = Modifier.height(Spacing.xs))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = labelsSecondary
        )

        Spacer(modifier = Modifier.height(Spacing.lg))

        if (appThemeMode == AppThemeMode.MIUIX) {
            MiuixFilledButton(
                text = "重试",
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            androidx.compose.material3.Button(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = IOSColors.Blue,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("重试")
            }
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        .coerceIn(0, units.size - 1)
    return String.format(Locale.US, "%.1f %s", bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}
