package com.duoschedule.ui.sync

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import com.duoschedule.ui.theme.GlassSymbolIconButton
import com.duoschedule.ui.theme.GlassSymbolButtonStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.duoschedule.data.sync.*
import com.duoschedule.ui.settings.components.*
import com.duoschedule.ui.theme.*
import com.kyant.backdrop.backdrops.emptyBackdrop
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SyncSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SyncViewModel = hiltViewModel()
) {
    val syncEnabled by viewModel.syncEnabled.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val syncConfig by viewModel.syncConfig.collectAsState()
    val lastSyncTime by viewModel.lastSyncTime.collectAsState()
    val isCreating by viewModel.isCreating.collectAsState()
    val isJoining by viewModel.isJoining.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val syncCode by viewModel.syncCode.collectAsState()
    val message by viewModel.message.collectAsState()
    val conflictResult by viewModel.conflictResult.collectAsState()

    val context = LocalContext.current
    val backdrop = LocalBackdrop.current ?: emptyBackdrop()

    var selectedProvider by remember { mutableIntStateOf(0) }
    var webDavUrl by remember { mutableStateOf("https://dav.jianguoyun.com/dav/") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var syncCodeInput by remember { mutableStateOf("") }
    var showLeaveConfirm by remember { mutableStateOf(false) }

    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()

    val hazeState = rememberHazeState()
    val scrollState = rememberScrollState()

    val contentBackdrop = kyantRememberLayerBackdrop()
    val backgroundColor = MaterialTheme.colorScheme.surface
    val miuixBackdrop = rememberLayerBackdrop {
        drawRect(backgroundColor)
        drawContent()
    }

    LaunchedEffect(message) {
        if (message != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    val blurEnabled = scrollState.value > 0

    Scaffold(
        topBar = {
            BlurredBar(hazeState, backdrop = miuixBackdrop, enabled = blurEnabled) {
                SmallTopAppBar(
                    title = "课表同步",
                    scrollBehavior = MiuixScrollBehavior(),
                    color = Color.Transparent,
                    titleColor = MiuixTheme.colorScheme.onSurface,
                    defaultWindowInsetsPadding = false,
                    navigationIcon = {
                        GlassSymbolIconButton(onClick = onNavigateBack, style = GlassSymbolButtonStyle.NonTinted) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "返回")
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
                    .padding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(Spacing.iOS26.groupSpacing)
            ) {
                SettingsSection(title = "同步状态") {
                    SettingsRow(
                        title = "当前状态",
                        subtitle = getSyncStateLabel(syncStatus.state),
                        icon = Icons.Outlined.Sync,
                        iconBackgroundColor = when (syncStatus.state) {
                            SyncState.SYNCED -> IOSColors.Green
                            SyncState.SYNCING -> IOSColors.Orange
                            SyncState.ERROR -> IOSColors.Red
                            SyncState.CONFLICT -> IOSColors.Orange
                            else -> IOSColors.Gray
                        },
                        trailing = {
                            Text(
                                text = getSyncStateIndicator(syncStatus.state),
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                    )

                    if (syncEnabled && lastSyncTime > 0L) {
                        Separator(modifier = Modifier.padding(horizontal = Spacing.lg))

                        SettingsRow(
                            title = "上次同步",
                            subtitle = formatSyncTime(lastSyncTime),
                            icon = Icons.Outlined.Schedule,
                            iconBackgroundColor = IOSColors.Blue
                        )
                    }
                }

                if (!syncEnabled) {
                    SettingsSection(title = "创建房间") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = Spacing.sm),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                        ) {
                            GlassSelectableChip(
                                selected = selectedProvider == 0,
                                onClick = {
                                    selectedProvider = 0
                                    webDavUrl = "https://dav.jianguoyun.com/dav/"
                                },
                                label = "坚果云",
                                selectedColor = IOSColors.Blue,
                                modifier = Modifier.weight(1f)
                            )
                            GlassSelectableChip(
                                selected = selectedProvider == 1,
                                onClick = {
                                    selectedProvider = 1
                                    webDavUrl = "https://dbox.cstcloud.cn/dav/"
                                },
                                label = "数据胶囊",
                                selectedColor = IOSColors.Green,
                                modifier = Modifier.weight(1f)
                            )
                            GlassSelectableChip(
                                selected = selectedProvider == 2,
                                onClick = {
                                    selectedProvider = 2
                                    webDavUrl = ""
                                },
                                label = "自定义",
                                selectedColor = IOSColors.Indigo,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (selectedProvider == 2) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = Spacing.xs)
                            ) {
                                Text(
                                    text = "WebDAV 地址",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = labelsSecondary,
                                    modifier = Modifier.padding(bottom = Spacing.xs)
                                )
                                GlassTextField(
                                    value = webDavUrl,
                                    onValueChange = { webDavUrl = it },
                                    placeholder = "https://example.com/dav/",
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        Separator(modifier = Modifier.padding(horizontal = Spacing.lg))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = Spacing.xs)
                        ) {
                            Text(
                                text = "用户名",
                                style = MaterialTheme.typography.bodySmall,
                                color = labelsSecondary,
                                modifier = Modifier.padding(bottom = Spacing.xs)
                            )
                            GlassTextField(
                                value = username,
                                onValueChange = { username = it },
                                placeholder = "WebDAV 账号",
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = Spacing.xs)
                        ) {
                            Text(
                                text = "密码",
                                style = MaterialTheme.typography.bodySmall,
                                color = labelsSecondary,
                                modifier = Modifier.padding(bottom = Spacing.xs)
                            )
                            GlassTextField(
                                value = password,
                                onValueChange = { password = it },
                                placeholder = "WebDAV 密码/应用密码",
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(Spacing.xs))

                        LiquidGlassButton(
                            text = "创建房间",
                            onClick = {
                                val url = when (selectedProvider) {
                                    0 -> "https://dav.jianguoyun.com/dav/"
                                    1 -> "https://dbox.cstcloud.cn/dav/"
                                    else -> webDavUrl
                                }
                                viewModel.createRoom(url, username, password)
                            },
                            style = LiquidGlassButtonStyle.Tinted,
                            enabled = username.isNotBlank() && password.isNotBlank() && (selectedProvider != 2 || webDavUrl.isNotBlank()),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    SettingsSection(title = "加入房间") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = Spacing.xs)
                        ) {
                            Text(
                                text = "同步码",
                                style = MaterialTheme.typography.bodySmall,
                                color = labelsSecondary,
                                modifier = Modifier.padding(bottom = Spacing.xs)
                            )
                            GlassTextField(
                                value = syncCodeInput,
                                onValueChange = { syncCodeInput = it },
                                placeholder = "输入对方分享的同步码",
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(Spacing.xs))

                        LiquidGlassButton(
                            text = "加入房间",
                            onClick = { viewModel.joinRoom(syncCodeInput) },
                            style = LiquidGlassButtonStyle.Tinted,
                            enabled = syncCodeInput.isNotBlank() && viewModel.isValidSyncCode(syncCodeInput),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    SettingsFooter(text = "通过 WebDAV 云存储实现双人课表同步，创建房间后分享同步码给对方即可")
                } else {
                    SettingsSection(title = "同步码") {
                        SettingsRow(
                            title = "同步码",
                            subtitle = if (syncCode.isNotEmpty()) syncCode else syncConfig?.roomId ?: "",
                            icon = Icons.Outlined.QrCode,
                            iconBackgroundColor = IOSColors.Indigo
                        )

                        Spacer(modifier = Modifier.height(Spacing.xs))

                        LiquidGlassButton(
                            text = "复制同步码",
                            onClick = remember { { viewModel.copySyncCode() } },
                            style = LiquidGlassButtonStyle.Tinted,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    SettingsSection(title = "同步操作") {
                        LiquidGlassButton(
                            text = "手动同步",
                            onClick = remember { { viewModel.syncNow() } },
                            style = LiquidGlassButtonStyle.Tinted,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(Spacing.sm))

                        LiquidGlassButton(
                            text = "离开房间",
                            onClick = { showLeaveConfirm = true },
                            style = LiquidGlassButtonStyle.NonTinted,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    SettingsFooter(text = "离开房间后将清除本地同步配置，不会删除本地课表数据")
                }

                Spacer(modifier = Modifier.height(Spacing.xl))
                Spacer(modifier = Modifier.height(LiquidBottomTabsSpec.Height + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()))
            }
        }
    }

    if (isCreating) {
        GlassLoadingDialog(
            backdrop = backdrop,
            message = "正在创建房间...",
            onDismiss = {}
        )
    }

    if (isJoining) {
        GlassLoadingDialog(
            backdrop = backdrop,
            message = "正在加入房间...",
            onDismiss = {}
        )
    }

    if (isSyncing) {
        GlassLoadingDialog(
            backdrop = backdrop,
            message = "正在同步...",
            onDismiss = {}
        )
    }

    if (showLeaveConfirm) {
        GlassConfirmDialog(
            backdrop = backdrop,
            title = "离开房间",
            message = "确定要离开当前同步房间吗？离开后将清除同步配置。",
            confirmText = "离开",
            dismissText = "取消",
            isDestructive = true,
            onConfirm = {
                showLeaveConfirm = false
                viewModel.leaveRoom()
            },
            onDismiss = { showLeaveConfirm = false }
        )
    }

    if (conflictResult != null) {
        ConflictResolutionDialog(
            conflictItems = conflictResult!!.conflictItems,
            onResolve = { resolution ->
                viewModel.resolveConflicts(resolution)
            },
            onDismiss = {
                viewModel.dismissConflict()
            }
        )
    }
}

private fun getSyncStateLabel(state: SyncState): String {
    return when (state) {
        SyncState.IDLE -> "空闲"
        SyncState.SYNCING -> "同步中"
        SyncState.SYNCED -> "已同步"
        SyncState.ERROR -> "同步失败"
        SyncState.CONFLICT -> "冲突"
        SyncState.DISABLED -> "未启用"
    }
}

private fun getSyncStateIndicator(state: SyncState): String {
    return when (state) {
        SyncState.SYNCED -> "🟢"
        SyncState.SYNCING -> "🟡"
        SyncState.ERROR -> "🔴"
        SyncState.CONFLICT -> "🟡"
        SyncState.IDLE -> "⚪"
        SyncState.DISABLED -> "⚪"
    }
}

private fun formatSyncTime(timestamp: Long): String {
    if (timestamp <= 0L) return "从未同步"
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
