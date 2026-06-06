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
import com.duoschedule.data.model.AppThemeMode
import com.duoschedule.data.sync.*
import com.duoschedule.ui.settings.components.*
import com.duoschedule.ui.theme.getRoundedCorner
import com.duoschedule.ui.theme.*
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
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.window.WindowDialog
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import top.yukonga.miuix.kmp.theme.MiuixTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import top.yukonga.miuix.kmp.basic.Surface
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
    val appThemeMode = LocalAppThemeMode.current

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
                            if (appThemeMode == AppThemeMode.MIUIX) {
                                listOf("坚果云" to 0, "数据胶囊" to 1, "自定义" to 2).forEach { (label, idx) ->
                                    Surface(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(16.dp),
                                        color = if (selectedProvider == idx) MiuixTheme.colorScheme.primary.copy(alpha = 0.12f) else MiuixTheme.colorScheme.surfaceContainer,
                                        onClick = {
                                            selectedProvider = idx
                                            webDavUrl = when (idx) {
                                                0 -> "https://dav.jianguoyun.com/dav/"
                                                1 -> "https://dbox.cstcloud.cn/dav/"
                                                else -> ""
                                            }
                                        }
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            top.yukonga.miuix.kmp.basic.Text(
                                                text = label,
                                                color = if (selectedProvider == idx) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onBackground,
                                                fontWeight = if (selectedProvider == idx) FontWeight.SemiBold else FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                            } else {
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
                                if (appThemeMode == AppThemeMode.MIUIX) {
                                    TextField(
                                        value = webDavUrl,
                                        onValueChange = { webDavUrl = it },
                                        label = "WebDAV 地址",
                                        useLabelAsPlaceholder = true,
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                } else {
                                    GlassTextField(
                                        value = webDavUrl,
                                        onValueChange = { webDavUrl = it },
                                        placeholder = "https://example.com/dav/",
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
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
                            if (appThemeMode == AppThemeMode.MIUIX) {
                                TextField(
                                    value = username,
                                    onValueChange = { username = it },
                                    label = "用户名",
                                    useLabelAsPlaceholder = true,
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                GlassTextField(
                                    value = username,
                                    onValueChange = { username = it },
                                    placeholder = "WebDAV 账号",
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
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
                            if (appThemeMode == AppThemeMode.MIUIX) {
                                TextField(
                                    value = password,
                                    onValueChange = { password = it },
                                    label = "WebDAV 密码/应用密码",
                                    useLabelAsPlaceholder = true,
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                GlassTextField(
                                    value = password,
                                    onValueChange = { password = it },
                                    placeholder = "WebDAV 密码/应用密码",
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(Spacing.xs))

                        if (appThemeMode == AppThemeMode.MIUIX) {
                            Button(
                                onClick = {
                                    val url = when (selectedProvider) {
                                        0 -> "https://dav.jianguoyun.com/dav/"
                                        1 -> "https://dbox.cstcloud.cn/dav/"
                                        else -> webDavUrl
                                    }
                                    viewModel.createRoom(url, username, password)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColorsPrimary(),
                                enabled = username.isNotBlank() && password.isNotBlank() && (selectedProvider != 2 || webDavUrl.isNotBlank())
                            ) {
                                top.yukonga.miuix.kmp.basic.Text("创建房间")
                            }
                        } else {
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
                            if (appThemeMode == AppThemeMode.MIUIX) {
                                TextField(
                                    value = syncCodeInput,
                                    onValueChange = { syncCodeInput = it },
                                    label = "输入对方分享的同步码",
                                    useLabelAsPlaceholder = true,
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                GlassTextField(
                                    value = syncCodeInput,
                                    onValueChange = { syncCodeInput = it },
                                    placeholder = "输入对方分享的同步码",
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(Spacing.xs))

                        if (appThemeMode == AppThemeMode.MIUIX) {
                            Button(
                                onClick = { viewModel.joinRoom(syncCodeInput) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColorsPrimary(),
                                enabled = syncCodeInput.isNotBlank() && viewModel.isValidSyncCode(syncCodeInput)
                            ) {
                                top.yukonga.miuix.kmp.basic.Text("加入房间")
                            }
                        } else {
                            LiquidGlassButton(
                                text = "加入房间",
                                onClick = { viewModel.joinRoom(syncCodeInput) },
                                style = LiquidGlassButtonStyle.Tinted,
                                enabled = syncCodeInput.isNotBlank() && viewModel.isValidSyncCode(syncCodeInput),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
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

                        if (appThemeMode == AppThemeMode.MIUIX) {
                            Button(
                                onClick = remember { { viewModel.copySyncCode() } },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColorsPrimary()
                            ) {
                                top.yukonga.miuix.kmp.basic.Text("复制同步码")
                            }
                        } else {
                            LiquidGlassButton(
                                text = "复制同步码",
                                onClick = remember { { viewModel.copySyncCode() } },
                                style = LiquidGlassButtonStyle.Tinted,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    SettingsSection(title = "同步操作") {
                        if (appThemeMode == AppThemeMode.MIUIX) {
                            Button(
                                onClick = remember { { viewModel.syncNow() } },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColorsPrimary()
                            ) {
                                top.yukonga.miuix.kmp.basic.Text("手动同步")
                            }
                        } else {
                            LiquidGlassButton(
                                text = "手动同步",
                                onClick = remember { { viewModel.syncNow() } },
                                style = LiquidGlassButtonStyle.Tinted,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(Spacing.sm))

                        if (appThemeMode == AppThemeMode.MIUIX) {
                            Button(
                                onClick = { showLeaveConfirm = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors()
                            ) {
                                top.yukonga.miuix.kmp.basic.Text("离开房间")
                            }
                        } else {
                            LiquidGlassButton(
                                text = "离开房间",
                                onClick = { showLeaveConfirm = true },
                                style = LiquidGlassButtonStyle.NonTinted,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    SettingsFooter(text = "离开房间后将清除本地同步配置，不会删除本地课表数据")
                }

                Spacer(modifier = Modifier.height(Spacing.xl))
                Spacer(modifier = Modifier.height(LiquidBottomTabsSpec.Height + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()))
            }
        }
    }

    if (isCreating) {
        if (appThemeMode == AppThemeMode.MIUIX) {
            WindowDialog(
                show = true,
                title = "请稍候",
                onDismissRequest = {}
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    top.yukonga.miuix.kmp.basic.CircularProgressIndicator(
                        modifier = Modifier.size(24.dp)
                    )
                    top.yukonga.miuix.kmp.basic.Text("正在创建房间...")
                }
            }
        } else {
            GlassLoadingDialog(
                backdrop = backdrop,
                message = "正在创建房间...",
                onDismiss = {}
            )
        }
    }

    if (isJoining) {
        if (appThemeMode == AppThemeMode.MIUIX) {
            WindowDialog(
                show = true,
                title = "请稍候",
                onDismissRequest = {}
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    top.yukonga.miuix.kmp.basic.CircularProgressIndicator(
                        modifier = Modifier.size(24.dp)
                    )
                    top.yukonga.miuix.kmp.basic.Text("正在加入房间...")
                }
            }
        } else {
            GlassLoadingDialog(
                backdrop = backdrop,
                message = "正在加入房间...",
                onDismiss = {}
            )
        }
    }

    if (isSyncing) {
        if (appThemeMode == AppThemeMode.MIUIX) {
            WindowDialog(
                show = true,
                title = "请稍候",
                onDismissRequest = {}
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    top.yukonga.miuix.kmp.basic.CircularProgressIndicator(
                        modifier = Modifier.size(24.dp)
                    )
                    top.yukonga.miuix.kmp.basic.Text("正在同步...")
                }
            }
        } else {
            GlassLoadingDialog(
                backdrop = backdrop,
                message = "正在同步...",
                onDismiss = {}
            )
        }
    }

    if (showLeaveConfirm) {
        if (appThemeMode == AppThemeMode.MIUIX) {
            WindowDialog(
                show = true,
                title = "离开房间",
                onDismissRequest = { showLeaveConfirm = false }
            ) {
                top.yukonga.miuix.kmp.basic.Text(
                    text = "确定要离开当前同步房间吗？离开后将清除同步配置。",
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
                Spacer(modifier = Modifier.height(Spacing.lg))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    Button(
                        onClick = { showLeaveConfirm = false },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors()
                    ) {
                        top.yukonga.miuix.kmp.basic.Text("取消")
                    }
                    Button(
                        onClick = {
                            showLeaveConfirm = false
                            viewModel.leaveRoom()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColorsPrimary()
                    ) {
                        top.yukonga.miuix.kmp.basic.Text("离开")
                    }
                }
            }
        } else {
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
