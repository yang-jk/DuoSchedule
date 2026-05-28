package com.duoschedule.ui.update

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
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
import com.duoschedule.ui.theme.BrandColors
import com.duoschedule.ui.theme.BorderRadius
import com.duoschedule.ui.theme.IOSColors
import com.duoschedule.ui.theme.LocalBackdrop
import com.duoschedule.ui.theme.LocalDarkTheme
import com.duoschedule.ui.theme.LiquidGlassButton
import com.duoschedule.ui.theme.LiquidGlassButtonStyle
import com.duoschedule.ui.theme.Spacing
import com.duoschedule.ui.theme.getFillsVibrantPrimary
import com.duoschedule.ui.theme.getLabelsVibrantPrimary
import com.duoschedule.ui.theme.getLabelsVibrantSecondary
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.highlight.Highlight
import com.kyant.capsule.ContinuousRoundedRectangle

private val LocalDialogBackdrop = compositionLocalOf<Backdrop?> { null }

@Composable
fun UpdateDialog(
    onDismiss: () -> Unit,
    viewModel: UpdateViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val backdrop = LocalBackdrop.current ?: emptyBackdrop()

    val isForceUpdate = when (val state = uiState) {
        is UpdateUiState.UpdateAvailable -> state.isForceUpdate
        is UpdateUiState.ReadyToInstall -> state.isForceUpdate
        else -> false
    }

    LaunchedEffect(Unit) {
        viewModel.checkForUpdate(context)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = !isForceUpdate,
            dismissOnClickOutside = !isForceUpdate,
            usePlatformDefaultWidth = false
        )
    ) {
        val darkTheme = LocalDarkTheme.current
        val containerColor = if (darkTheme) {
            Color(0xFF121212).copy(alpha = 0.4f)
        } else {
            Color(0xFFFAFAFA).copy(alpha = 0.6f)
        }
        val dialogBackdrop = rememberLayerBackdrop()

        Box(
            modifier = Modifier
                .width(300.dp)
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { ContinuousRoundedRectangle(24.dp) },
                    effects = {
                        colorControls(
                            brightness = if (darkTheme) 0f else 0.2f,
                            saturation = 1.5f
                        )
                        blur(if (darkTheme) 8.dp.toPx() else 16.dp.toPx())
                        lens(24.dp.toPx(), 48.dp.toPx(), depthEffect = true)
                    },
                    highlight = { Highlight.Plain },
                    exportedBackdrop = dialogBackdrop,
                    onDrawSurface = { drawRect(containerColor) }
                )
        ) {
            CompositionLocalProvider(
                LocalDialogBackdrop provides dialogBackdrop
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 176.dp)
                        .padding(Spacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    when (val state = uiState) {
                        is UpdateUiState.Idle -> {}
                        is UpdateUiState.Checking -> CheckingContent()
                        is UpdateUiState.UpdateAvailable -> UpdateAvailableContent(
                            info = state.info,
                            isForceUpdate = state.isForceUpdate,
                            onDownload = { viewModel.startDownload(context) },
                            onSkip = {
                                viewModel.skipVersion()
                                onDismiss()
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
                        is UpdateUiState.NoUpdate -> NoUpdateContent(
                            onDismiss = onDismiss
                        )
                        is UpdateUiState.Error -> ErrorContent(
                            message = state.message,
                            onRetry = { viewModel.checkForUpdate(context) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CheckingContent() {
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()

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

@Composable
private fun UpdateAvailableContent(
    info: com.duoschedule.data.update.UpdateInfo,
    isForceUpdate: Boolean,
    onDownload: () -> Unit,
    onSkip: () -> Unit
) {
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()
    val darkTheme = LocalDarkTheme.current

    Icon(
        imageVector = Icons.Filled.SystemUpdate,
        contentDescription = null,
        modifier = Modifier.size(40.dp),
        tint = BrandColors.Primary
    )

    Spacer(modifier = Modifier.height(Spacing.md))

    Text(
        text = "发现新版本 v${info.latestVersion}",
        style = MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.SemiBold
        ),
        color = labelsPrimary
    )

    if (isForceUpdate) {
        Spacer(modifier = Modifier.height(Spacing.xs))
        Text(
            text = "此版本为重要更新，需要更新后才能继续使用",
            style = MaterialTheme.typography.bodySmall,
            color = IOSColors.Red
        )
    }

    Spacer(modifier = Modifier.height(Spacing.md))

    val optionBackground = if (darkTheme) {
        Color(0x29EBEBF5)
    } else {
        Color(0x29787880)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 150.dp)
            .clip(ContinuousRoundedRectangle(BorderRadius.iOS26.container))
            .background(optionBackground)
            .padding(Spacing.sm)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = info.releaseNotes,
            style = MaterialTheme.typography.bodyMedium,
            color = labelsSecondary
        )
    }

    Spacer(modifier = Modifier.height(Spacing.lg))

    LiquidGlassButton(
        onClick = onDownload,
        text = "立即更新",
        style = LiquidGlassButtonStyle.Tinted,
        modifier = Modifier.fillMaxWidth()
    )

    if (!isForceUpdate) {
        Spacer(modifier = Modifier.height(Spacing.sm))
        LiquidGlassButton(
            onClick = onSkip,
            text = "跳过此版本",
            style = LiquidGlassButtonStyle.NonTinted,
            modifier = Modifier.fillMaxWidth()
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
            trackColor = getFillsVibrantPrimary(),
        )
    } else {
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = BrandColors.Primary,
            trackColor = getFillsVibrantPrimary(),
        )
    }

    Spacer(modifier = Modifier.height(Spacing.sm))

    Text(
        text = if (percent >= 0) "$percent%  ·  ${formatFileSize(downloadedBytes)} / ${formatFileSize(totalBytes)}" else "${formatFileSize(downloadedBytes)} 已下载",
        style = MaterialTheme.typography.bodySmall,
        color = labelsSecondary
    )

    Spacer(modifier = Modifier.height(Spacing.lg))

    LiquidGlassButton(
        onClick = onCancel,
        text = "取消下载",
        style = LiquidGlassButtonStyle.NonTinted,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ReadyToInstallContent(
    isForceUpdate: Boolean,
    onInstall: () -> Unit
) {
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()

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

    LiquidGlassButton(
        onClick = onInstall,
        text = "安装更新",
        style = LiquidGlassButtonStyle.Tinted,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun NoUpdateContent(
    onDismiss: () -> Unit
) {
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()

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

    LiquidGlassButton(
        onClick = onDismiss,
        text = "好的",
        style = LiquidGlassButtonStyle.Tinted,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()

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

    LiquidGlassButton(
        onClick = onRetry,
        text = "重试",
        style = LiquidGlassButtonStyle.Tinted,
        modifier = Modifier.fillMaxWidth()
    )
}

private fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        .coerceIn(0, units.size - 1)
    return String.format("%.1f %s", bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}
