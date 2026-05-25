package com.duoschedule.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.duoschedule.ui.settings.components.*
import com.duoschedule.ui.theme.*
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop

@Composable
fun LegalScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text("法律信息") },
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
        val backgroundColor = MaterialTheme.colorScheme.background
        val scrollBackdrop = rememberLayerBackdrop {
            drawRect(backgroundColor)
            drawContent()
        }
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
                    .layerBackdrop(scrollBackdrop)
            ) {
                SettingsSection(title = "用户协议") {
                    Column(modifier = Modifier.padding(horizontal = Spacing.lg)) {
                        Text(
                            text = "欢迎使用 DuoSchedule（以下简称\"本应用\"）。使用本应用即表示您同意以下条款：",
                            style = MaterialTheme.typography.bodyMedium,
                            color = getLabelsVibrantPrimary(),
                            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
                            modifier = Modifier.padding(bottom = Spacing.md)
                        )

                        LegalSection(
                            title = "1. 开源许可",
                            content = "本应用基于 MIT License 开源发布。您可以在 MIT License 条款下自由使用、修改和分发本应用。"
                        )

                        LegalSection(
                            title = "2. 使用规范",
                            content = "本应用仅供个人课表管理使用，不得用于任何违法用途。用户应对自己输入的数据负责。"
                        )

                        LegalSection(
                            title = "3. 免责声明",
                            content = "本应用按\"原样\"提供，不作任何明示或暗示的保证。开发者不对因使用本应用造成的任何损失承担责任。"
                        )
                    }
                }

                SettingsSection(title = "隐私政策") {
                    Column(modifier = Modifier.padding(horizontal = Spacing.lg)) {
                        LegalSection(
                            title = "1. 数据存储",
                            content = "所有课程数据和个人设置均存储在您的设备本地（Room 数据库 + DataStore），不会上传到任何服务器。"
                        )

                        LegalSection(
                            title = "2. 数据收集",
                            content = "本应用不收集、不传输任何个人身份数据、位置信息或使用行为数据。"
                        )

                        LegalSection(
                            title = "3. 网络访问",
                            content = "本应用仅在以下情况访问网络：检查应用更新（访问 GitHub/Gitee API）、从教务系统导入课程数据。这些操作不会传输您的个人信息。"
                        )

                        LegalSection(
                            title = "4. 通知权限",
                            content = "课前提醒通知功能需要通知权限，该权限仅用于发送课程提醒，不会用于其他目的。"
                        )

                        LegalSection(
                            title = "5. 第三方库",
                            content = "本应用使用的第三方开源库列表请参见\"开源致谢\"页面。"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(LiquidBottomTabsSpec.Height + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp))
            }

            ScrollTopBlurOverlay(backdrop = scrollBackdrop, scrollOffset = scrollState.value)
        }
    }
}

@Composable
fun LegalSection(
    title: String,
    content: String
) {
    Column(modifier = Modifier.padding(bottom = Spacing.md)) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = getLabelsVibrantPrimary(),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = getLabelsVibrantSecondary(),
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
        )
    }
}
