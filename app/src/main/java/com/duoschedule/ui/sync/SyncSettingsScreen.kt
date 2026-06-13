package com.duoschedule.ui.sync

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.outlined.*
import com.duoschedule.ui.theme.GlassSymbolIconButton
import com.duoschedule.ui.theme.GlassSymbolButtonStyle
import androidx.compose.material3.Icon
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
    val isTestingConnection by viewModel.isTestingConnection.collectAsState()
    val testConnectionResult by viewModel.testConnectionResult.collectAsState()
    val roomCode by viewModel.roomCode.collectAsState()
    val message by viewModel.message.collectAsState()
    val conflictResult by viewModel.conflictResult.collectAsState()
    val pendingJoinInfo by viewModel.pendingJoinInfo.collectAsState()

    val context = LocalContext.current
    val backdrop = LocalBackdrop.current ?: emptyBackdrop()
    val appThemeMode = LocalAppThemeMode.current

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var roomCodeInput by remember { mutableStateOf("") }
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

    LaunchedEffect(testConnectionResult) {
        if (testConnectionResult != null) {
            Toast.makeText(context, testConnectionResult, Toast.LENGTH_SHORT).show()
            viewModel.clearTestResult()
        }
    }

    val blurEnabled = scrollState.value > 0

    Scaffold(
        topBar = {
            BlurredBar(hazeState, backdrop = miuixBackdrop, enabled = blurEnabled, contentBackdrop = contentBackdrop) {
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
                verticalArrangement = Arrangement.spacedBy(
                    if (appThemeMode == AppThemeMode.MIUIX) Spacing.lg else Spacing.iOS26.groupSpacing
                )
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
                                    placeholder = "坚果云账号",
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
                                    label = "坚果云应用密码",
                                    useLabelAsPlaceholder = true,
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                GlassTextField(
                                    value = password,
                                    onValueChange = { password = it },
                                    placeholder = "坚果云应用密码",
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(Spacing.xs))

                        if (appThemeMode == AppThemeMode.MIUIX) {
                            Button(
                                onClick = { viewModel.testConnection(username, password) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(),
                                enabled = username.isNotBlank() && password.isNotBlank()
                            ) {
                                top.yukonga.miuix.kmp.basic.Text("测试连接")
                            }
                        } else {
                            LiquidGlassButton(
                                text = "测试连接",
                                onClick = { viewModel.testConnection(username, password) },
                                style = LiquidGlassButtonStyle.NonTinted,
                                enabled = username.isNotBlank() && password.isNotBlank(),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(Spacing.xs))

                        if (appThemeMode == AppThemeMode.MIUIX) {
                            Button(
                                onClick = { viewModel.createRoom(username, password) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColorsPrimary(),
                                enabled = username.isNotBlank() && password.isNotBlank()
                            ) {
                                top.yukonga.miuix.kmp.basic.Text("创建房间")
                            }
                        } else {
                            LiquidGlassButton(
                                text = "创建房间",
                                onClick = { viewModel.createRoom(username, password) },
                                style = LiquidGlassButtonStyle.Tinted,
                                enabled = username.isNotBlank() && password.isNotBlank(),
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
                                text = "房间码",
                                style = MaterialTheme.typography.bodySmall,
                                color = labelsSecondary,
                                modifier = Modifier.padding(bottom = Spacing.xs)
                            )
                            if (appThemeMode == AppThemeMode.MIUIX) {
                                TextField(
                                    value = roomCodeInput,
                                    onValueChange = { roomCodeInput = it },
                                    label = "输入6位房间码",
                                    useLabelAsPlaceholder = true,
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                GlassTextField(
                                    value = roomCodeInput,
                                    onValueChange = { roomCodeInput = it },
                                    placeholder = "输入6位房间码",
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(Spacing.xs))

                        val roomCodeValid = roomCodeInput.length == 6 && roomCodeInput.all { it.isDigit() }

                        if (appThemeMode == AppThemeMode.MIUIX) {
                            Button(
                                onClick = { viewModel.joinRoom(roomCodeInput) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColorsPrimary(),
                                enabled = roomCodeValid
                            ) {
                                top.yukonga.miuix.kmp.basic.Text("加入房间")
                            }
                        } else {
                            LiquidGlassButton(
                                text = "加入房间",
                                onClick = { viewModel.joinRoom(roomCodeInput) },
                                style = LiquidGlassButtonStyle.Tinted,
                                enabled = roomCodeValid,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    SettingsFooter(text = "创建房间后分享房间码给对方，对方输入房间码即可加入")
                } else {
                    SettingsSection(title = "房间码") {
                        SettingsRow(
                            title = "房间码",
                            subtitle = roomCode,
                            icon = Icons.Outlined.QrCode,
                            iconBackgroundColor = IOSColors.Indigo
                        )

                        Spacer(modifier = Modifier.height(Spacing.xs))

                        if (appThemeMode == AppThemeMode.MIUIX) {
                            Button(
                                onClick = remember { { viewModel.copyRoomCode() } },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColorsPrimary()
                            ) {
                                top.yukonga.miuix.kmp.basic.Text("复制房间码")
                            }
                        } else {
                            LiquidGlassButton(
                                text = "复制房间码",
                                onClick = remember { { viewModel.copyRoomCode() } },
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

    if (isTestingConnection) {
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
                    top.yukonga.miuix.kmp.basic.Text("正在测试连接...")
                }
            }
        } else {
            GlassLoadingDialog(
                backdrop = backdrop,
                message = "正在测试连接...",
                onDismiss = {}
            )
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

    // 名字选择弹窗
    if (pendingJoinInfo != null) {
        val info = pendingJoinInfo!!
        val sameName = info.personAName == info.personBName
        val nameALabel = if (sameName) "用户A" else info.personAName
        val nameBLabel = if (sameName) "用户B" else info.personBName

        if (appThemeMode == AppThemeMode.MIUIX) {
            WindowDialog(
                show = true,
                title = "选择你的名字",
                onDismissRequest = {}
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MiuixTheme.colorScheme.surfaceContainer,
                        onClick = { viewModel.confirmJoinRoom(info.profileA.id) }
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            top.yukonga.miuix.kmp.basic.Text(
                                text = nameALabel,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MiuixTheme.colorScheme.surfaceContainer,
                        onClick = { viewModel.confirmJoinRoom(info.profileB.id) }
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            top.yukonga.miuix.kmp.basic.Text(
                                text = nameBLabel,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        } else {
            GlassConfirmDialog(
                backdrop = backdrop,
                title = "选择你的名字",
                confirmText = nameALabel,
                dismissText = nameBLabel,
                onConfirm = { viewModel.confirmJoinRoom(info.profileA.id) },
                onDismiss = { viewModel.confirmJoinRoom(info.profileB.id) }
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
