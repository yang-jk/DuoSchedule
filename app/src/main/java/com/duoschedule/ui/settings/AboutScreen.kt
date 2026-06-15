package com.duoschedule.ui.settings

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import com.duoschedule.ui.theme.GlassSymbolIconButton
import com.duoschedule.ui.theme.GlassSymbolButtonStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.duoschedule.data.model.AppThemeMode
import com.duoschedule.ui.theme.LocalAppThemeMode
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.duoschedule.R
import com.duoschedule.ui.theme.BgEffectBackground
import com.duoschedule.ui.theme.BrandColors
import com.duoschedule.ui.theme.IOSColors
import com.duoschedule.ui.theme.getLabelsVibrantPrimary
import com.duoschedule.ui.theme.getLabelsVibrantSecondary
import com.duoschedule.ui.update.UpdateDialog
import com.duoschedule.ui.update.UpdateUiState
import com.duoschedule.ui.update.UpdateViewModel
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import com.duoschedule.ui.theme.ContinuousShapes
import com.duoschedule.ui.theme.LocalDarkTheme
import com.duoschedule.ui.theme.BlurredBar
import com.kyant.backdrop.backdrops.layerBackdrop as kyantLayerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop as kyantRememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.layerBackdrop
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import androidx.compose.material3.MaterialTheme

@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit,
    onNavigateToChangelog: () -> Unit,
    onNavigateToLegal: () -> Unit,
    onNavigateToAcknowledgments: () -> Unit,
    viewModel: UpdateViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    val versionName = packageInfo.versionName ?: "3.3.1"
    val uiState by viewModel.uiState.collectAsState()
    var showUpdateDialog by remember { mutableStateOf(false) }
    val hazeState = rememberHazeState()
    val contentBackdrop = kyantRememberLayerBackdrop()
    val backgroundColor = MaterialTheme.colorScheme.surface
    val miuixBackdrop = rememberLayerBackdrop {
        drawRect(backgroundColor)
        drawContent()
    }

    // 监听状态变化：有更新弹窗，无更新Toast
    LaunchedEffect(uiState) {
        when (uiState) {
            is UpdateUiState.UpdateAvailable -> showUpdateDialog = true
            is UpdateUiState.ReadyToInstall -> showUpdateDialog = true
            is UpdateUiState.NoUpdate -> {
                showUpdateDialog = false
                Toast.makeText(context, "已是最新版本", Toast.LENGTH_SHORT).show()
                viewModel.resetToIdle()
            }
            is UpdateUiState.Error -> {
                showUpdateDialog = false
                Toast.makeText(context, "检查更新失败", Toast.LENGTH_SHORT).show()
                viewModel.resetToIdle()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            BlurredBar(hazeState, backdrop = miuixBackdrop, contentBackdrop = contentBackdrop) {
                SmallTopAppBar(
                    title = "关于",
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
            BgEffectBackground(
                dynamicBackground = true,
            ) {
            AboutContent(
                padding = innerPadding,
                versionName = versionName,
                isChecking = uiState is UpdateUiState.Checking,
                onNavigateToChangelog = onNavigateToChangelog,
                onNavigateToLegal = onNavigateToLegal,
                onNavigateToAcknowledgments = onNavigateToAcknowledgments,
                onCheckUpdate = { viewModel.checkForUpdate(context) },
            )
        }
        }
    }

    if (showUpdateDialog) {
        UpdateDialog(
            onDismiss = { showUpdateDialog = false },
            viewModel = viewModel
        )
    }
}

@Composable
private fun AboutContent(
    padding: PaddingValues,
    versionName: String,
    isChecking: Boolean,
    onNavigateToChangelog: () -> Unit,
    onNavigateToLegal: () -> Unit,
    onNavigateToAcknowledgments: () -> Unit,
    onCheckUpdate: () -> Unit,
) {
    val miuixTextStyles = MiuixTheme.textStyles

    val cutoutStart = WindowInsets.displayCutout.asPaddingValues()
        .calculateLeftPadding(LayoutDirection.Ltr)
    val cutoutEnd = WindowInsets.displayCutout.asPaddingValues()
        .calculateRightPadding(LayoutDirection.Ltr)

    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = padding.calculateTopPadding(),
                start = 16.dp + cutoutStart,
                end = 16.dp + cutoutEnd,
            )
    ) {
        // 可滚动的中间内容区域
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 50.dp, bottom = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(ContinuousShapes.iOS26.icon)
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_launcher_foreground),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Text(
                    text = "双人课程表",
                    color = labelsPrimary,
                    fontWeight = FontWeight.Bold,
                    style = miuixTextStyles.title2,
                    modifier = Modifier.padding(top = 20.dp, bottom = 6.dp)
                )

                Text(
                    text = versionName,
                    color = labelsSecondary,
                    style = miuixTextStyles.title4,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (isChecking) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.5.dp,
                            color = IOSColors.Blue
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "检查中...",
                            color = IOSColors.Blue,
                            style = miuixTextStyles.body1,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                } else {
                    Text(
                        text = "检查更新",
                        color = IOSColors.Blue,
                        style = miuixTextStyles.body1,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable(onClick = onCheckUpdate)
                    )
                }
            }

            // 更新日志卡片
            BlurCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onNavigateToChangelog)
                            .padding(start = 16.dp, top = 14.dp, end = 16.dp, bottom = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "更新日志",
                            color = labelsPrimary,
                            style = miuixTextStyles.title3,
                            fontWeight = FontWeight.Medium
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = labelsSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    val recentEntries = changelogEntries.take(3)
                    recentEntries.forEachIndexed { index, entry ->
                        AboutChangelogVersionItem(
                            entry = entry,
                            isLast = index == recentEntries.size - 1
                        )
                    }
                }
            }

            // 致谢卡片
            var ackExpanded by remember { mutableStateOf(false) }
            val ackContext = LocalContext.current
            BlurCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = { ackExpanded = !ackExpanded })
                            .padding(start = 16.dp, top = 14.dp, end = 16.dp, bottom = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "致谢",
                            color = labelsPrimary,
                            style = miuixTextStyles.title3,
                            fontWeight = FontWeight.Medium
                        )
                        Icon(
                            imageVector = if (ackExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = labelsSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    AnimatedVisibility(
                        visible = ackExpanded,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                        ) {
                            val ackLibs = listOf(
                                Triple("Jetpack Compose", "Google", "https://github.com/android/compose-samples"),
                                Triple("Kotlin", "JetBrains", "https://github.com/JetBrains/kotlin"),
                                Triple("Kotlin Coroutines", "JetBrains", "https://github.com/Kotlin/kotlinx.coroutines"),
                                Triple("Room", "Google", "https://github.com/android/architecture-components-samples"),
                                Triple("Hilt", "Google", "https://github.com/google/dagger"),
                                Triple("OkHttp", "Square", "https://github.com/square/okhttp"),
                                Triple("jsoup", "Jonathan Hedley", "https://github.com/jhy/jsoup"),
                                Triple("Miuix", "Yukonga", "https://github.com/miuix-kotlin-multiplatform/miuix"),
                                Triple("AndroidLiquidGlass", "kyant0", "https://github.com/kyant0/AndroidLiquidGlass"),
                                Triple("Backdrop", "kyant0", "https://github.com/kyant0/Backdrop"),
                                Triple("Haze", "Chris Banes", "https://github.com/nickkimk/haze"),
                                Triple("Shapes", "kyant0", "https://github.com/kyant0/Shapes"),
                                Triple("Capsule", "kyant0", "https://github.com/kyant0/Capsule"),
                                Triple("DataStore", "Google", "https://developer.android.com/topic/libraries/architecture/datastore"),
                                Triple("Navigation", "Google", "https://developer.android.com/guide/navigation"),
                                Triple("Glance", "Google", "https://developer.android.com/develop/ui/compose/glance"),
                            )
                            ackLibs.forEach { (libName, author, url) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = libName,
                                        color = IOSColors.Blue,
                                        style = miuixTextStyles.body1,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.clickable {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                            ackContext.startActivity(intent)
                                        }
                                    )
                                    Text(
                                        text = author,
                                        color = IOSColors.Blue,
                                        style = miuixTextStyles.body2,
                                        modifier = Modifier.clickable {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                            ackContext.startActivity(intent)
                                        }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "以及所有用户的支持与反馈",
                                color = labelsSecondary,
                                style = miuixTextStyles.footnote1,
                            )
                        }
                    }
                }
            }
            }
        }

        // Footer 固定在底部
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 32.dp,
                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 24.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "用户协议",
                    color = IOSColors.Blue,
                    style = miuixTextStyles.body2,
                    modifier = Modifier.clickable(onClick = onNavigateToLegal)
                )
                Text(
                    text = "隐私政策",
                    color = IOSColors.Blue,
                    style = miuixTextStyles.body2,
                    modifier = Modifier.clickable(onClick = onNavigateToLegal)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "© 2026 双人课程表",
                color = labelsSecondary.copy(alpha = 0.6f),
                style = miuixTextStyles.footnote2,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun BlurCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val darkTheme = LocalDarkTheme.current
    val containerColor = if (darkTheme) {
        Color(0x60121212)
    } else {
        Color(0x60FAFAFA)
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor)
    ) {
        content()
    }
}

@Composable
private fun AboutChangelogVersionItem(
    entry: ChangelogEntry,
    isLast: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    val miuixTextStyles = MiuixTheme.textStyles
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()

    Column(modifier = Modifier.fillMaxWidth()) {
        if (!isLast || expanded) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(0.5.dp)
                    .background(labelsSecondary.copy(alpha = 0.1f))
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = { expanded = !expanded })
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = entry.version,
                color = labelsPrimary,
                style = miuixTextStyles.title4,
                fontWeight = FontWeight.SemiBold,
            )

            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = labelsSecondary,
                modifier = Modifier.size(18.dp)
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
                    .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
            ) {
                entry.items.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
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
                                .padding(top = 2.dp, end = 6.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(badgeColor.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = badgeText,
                                style = miuixTextStyles.footnote2,
                                fontWeight = FontWeight.Medium,
                                color = badgeColor
                            )
                        }

                        Text(
                            text = item.summary,
                            style = miuixTextStyles.body2,
                            color = labelsSecondary,
                            modifier = Modifier.weight(1f),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}
