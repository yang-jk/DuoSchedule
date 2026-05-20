package com.duoschedule.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.duoschedule.ui.settings.components.*
import com.duoschedule.ui.theme.*
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.capsule.ContinuousRoundedRectangle

@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit,
    onNavigateToChangelog: () -> Unit,
    onNavigateToUpdate: () -> Unit,
    onNavigateToLegal: () -> Unit,
    onNavigateToAcknowledgments: () -> Unit
) {
    val context = LocalContext.current
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    val versionName = packageInfo.versionName ?: "3.2.0"
    val versionCode = packageInfo.versionCode

    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text("关于") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        val scrollBackdrop = rememberLayerBackdrop()
        val scrollState = rememberScrollState()
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .layerBackdrop(scrollBackdrop),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(ContinuousRoundedRectangle(BorderRadius.iOS26.container)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(BrandColors.Primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(44.dp),
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "DuoSchedule",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = labelsPrimary
                )

                Text(
                    text = "版本 $versionName ($versionCode)",
                    style = MaterialTheme.typography.bodySmall,
                    color = labelsSecondary
                )

                Spacer(modifier = Modifier.height(24.dp))

                SettingsSection(title = null) {
                    SettingsNavigationRow(
                        title = "更新日志",
                        subtitle = "查看版本更新记录",
                        icon = Icons.Outlined.History,
                        iconBackgroundColor = IOSColors.Blue,
                        onClick = onNavigateToChangelog
                    )

                    Separator(modifier = Modifier.padding(horizontal = Spacing.lg))

                    SettingsNavigationRow(
                        title = "检查更新",
                        subtitle = "查看是否有新版本",
                        icon = Icons.Outlined.SystemUpdate,
                        iconBackgroundColor = IOSColors.Green,
                        onClick = onNavigateToUpdate
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.iOS26.groupSpacing))

                SettingsSection(title = null) {
                    SettingsNavigationRow(
                        title = "用户协议和隐私政策",
                        icon = Icons.Outlined.Description,
                        iconBackgroundColor = IOSColors.Teal,
                        onClick = onNavigateToLegal
                    )

                    Separator(modifier = Modifier.padding(horizontal = Spacing.lg))

                    SettingsNavigationRow(
                        title = "开源致谢",
                        subtitle = "感谢开源社区",
                        icon = Icons.Outlined.Favorite,
                        iconBackgroundColor = IOSColors.Pink,
                        onClick = onNavigateToAcknowledgments
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.xl))

                Text(
                    text = "© 2026 DuoSchedule Contributors\nMIT License",
                    style = MaterialTheme.typography.bodySmall,
                    color = getLabelsVibrantTertiary(),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = LiquidBottomTabsSpec.Height + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp)
                )
            }

            ScrollTopBlurOverlay(backdrop = scrollBackdrop, scrollOffset = scrollState.value)
        }
    }
}
