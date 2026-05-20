package com.duoschedule.ui.update

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.duoschedule.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateScreen(
    onNavigateBack: () -> Unit,
    viewModel: UpdateViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.checkForUpdate(context)
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text("检查更新") },
                navigationIcon = {
                    if (uiState !is UpdateUiState.ReadyToInstall &&
                        uiState !is UpdateUiState.Downloading &&
                        (uiState !is UpdateUiState.UpdateAvailable || !(uiState as UpdateUiState.UpdateAvailable).isForceUpdate)
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "返回"
                            )
                        }
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
                .padding(top = paddingValues.calculateTopPadding())
                .padding(horizontal = Spacing.lg)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(Spacing.xxl))

            when (val state = uiState) {
                is UpdateUiState.Idle -> IdleContent()
                is UpdateUiState.Checking -> CheckingContent()
                is UpdateUiState.UpdateAvailable -> UpdateAvailableContent(
                    info = state.info,
                    isForceUpdate = state.isForceUpdate,
                    onDownload = { viewModel.startDownload(context) },
                    onSkip = { viewModel.skipVersion() }
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
                is UpdateUiState.NoUpdate -> NoUpdateContent()
                is UpdateUiState.Error -> ErrorContent(
                    message = state.message,
                    onRetry = { viewModel.checkForUpdate(context) }
                )
            }

            Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}

@Composable
private fun IdleContent() {
    Icon(
        imageVector = Icons.Filled.SystemUpdate,
        contentDescription = null,
        modifier = Modifier.size(64.dp),
        tint = getLabelsVibrantSecondary()
    )
    Spacer(modifier = Modifier.height(Spacing.lg))
    Text(
        text = "正在准备检查更新...",
        style = MaterialTheme.typography.bodyLarge,
        color = getLabelsVibrantSecondary()
    )
}

@Composable
private fun CheckingContent() {
    CircularProgressIndicator(
        modifier = Modifier.size(48.dp),
        color = BrandColors.Primary
    )
    Spacer(modifier = Modifier.height(Spacing.lg))
    Text(
        text = "正在检查更新...",
        style = MaterialTheme.typography.bodyLarge,
        color = getLabelsVibrantSecondary()
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

    Icon(
        imageVector = Icons.Filled.SystemUpdate,
        contentDescription = null,
        modifier = Modifier.size(64.dp),
        tint = BrandColors.Primary
    )

    Spacer(modifier = Modifier.height(Spacing.lg))

    Text(
        text = "发现新版本 v${info.latestVersion}",
        style = MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.Bold
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

    Spacer(modifier = Modifier.height(Spacing.lg))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = getFillsVibrantPrimary()
        ),
        shape = RoundedCornerShape(BorderRadius.iOS26.container)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg)
        ) {
            Text(
                text = "更新内容",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = labelsPrimary
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
            Text(
                text = info.releaseNotes,
                style = MaterialTheme.typography.bodyMedium,
                color = labelsSecondary
            )
        }
    }

    Spacer(modifier = Modifier.height(Spacing.xl))

    Button(
        onClick = onDownload,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(BorderRadius.iOS26.container)
    ) {
        Text("立即更新")
    }

    if (!isForceUpdate) {
        Spacer(modifier = Modifier.height(Spacing.md))
        OutlinedButton(
            onClick = onSkip,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(BorderRadius.iOS26.container)
        ) {
            Text("跳过此版本")
        }
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

    Icon(
        imageVector = Icons.Filled.SystemUpdate,
        contentDescription = null,
        modifier = Modifier.size(64.dp),
        tint = BrandColors.Primary
    )

    Spacer(modifier = Modifier.height(Spacing.lg))

    Text(
        text = "正在下载更新...",
        style = MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.Bold
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

    Spacer(modifier = Modifier.height(Spacing.xl))

    OutlinedButton(
        onClick = onCancel,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(BorderRadius.iOS26.container)
    ) {
        Text("取消下载")
    }
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
        modifier = Modifier.size(64.dp),
        tint = IOSColors.Green
    )

    Spacer(modifier = Modifier.height(Spacing.lg))

    Text(
        text = "下载完成",
        style = MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.Bold
        ),
        color = labelsPrimary
    )

    Spacer(modifier = Modifier.height(Spacing.xs))

    Text(
        text = "点击安装以完成更新",
        style = MaterialTheme.typography.bodyMedium,
        color = labelsSecondary
    )

    Spacer(modifier = Modifier.height(Spacing.xl))

    Button(
        onClick = onInstall,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(BorderRadius.iOS26.container)
    ) {
        Text("安装更新")
    }
}

@Composable
private fun NoUpdateContent() {
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()

    Icon(
        imageVector = Icons.Filled.CheckCircle,
        contentDescription = null,
        modifier = Modifier.size(64.dp),
        tint = IOSColors.Green
    )

    Spacer(modifier = Modifier.height(Spacing.lg))

    Text(
        text = "已是最新版本",
        style = MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.Bold
        ),
        color = labelsPrimary
    )

    Spacer(modifier = Modifier.height(Spacing.xs))

    Text(
        text = "当前没有可用的更新",
        style = MaterialTheme.typography.bodyMedium,
        color = labelsSecondary
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
        modifier = Modifier.size(64.dp),
        tint = IOSColors.Red
    )

    Spacer(modifier = Modifier.height(Spacing.lg))

    Text(
        text = "检查更新失败",
        style = MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.Bold
        ),
        color = labelsPrimary
    )

    Spacer(modifier = Modifier.height(Spacing.xs))

    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color = labelsSecondary
    )

    Spacer(modifier = Modifier.height(Spacing.xl))

    Button(
        onClick = onRetry,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(BorderRadius.iOS26.container)
    ) {
        Text("重试")
    }
}

private fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        .coerceIn(0, units.size - 1)
    return String.format("%.1f %s", bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}
