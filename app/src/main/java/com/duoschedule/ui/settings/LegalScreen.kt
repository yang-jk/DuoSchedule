package com.duoschedule.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.Icon
import com.duoschedule.ui.theme.GlassSymbolIconButton
import com.duoschedule.ui.theme.GlassSymbolButtonStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.duoschedule.ui.theme.*
import com.kyant.backdrop.backdrops.layerBackdrop as kyantLayerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop as kyantRememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.layerBackdrop
import com.kyant.capsule.ContinuousRoundedRectangle
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun LegalScreen(
    onNavigateBack: () -> Unit
) {
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
                    title = "法律信息",
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
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "用户协议",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = getLabelsVibrantSecondary(),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Text(
                    text = "欢迎使用 DuoSchedule（以下简称\u201c本应用\u201d）。请您在使用本应用前仔细阅读以下条款。下载、安装或以任何方式使用本应用，即表示您已充分阅读、理解并同意接受本协议的全部条款。如果您不同意本协议的任何条款，请立即停止使用本应用。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = getLabelsVibrantPrimary(),
                    lineHeight = 1.6.em,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Text(
                    text = "本协议最后更新日期：2026 年 5 月 28 日",
                    style = MaterialTheme.typography.bodySmall,
                    color = getLabelsVibrantTertiary(),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LegalSection(
                    title = "1. 服务说明",
                    content = "本应用是一款双人课程表管理工具，主要用于帮助用户记录和管理课程安排。主要功能包括：添加、编辑、删除课程信息；按人员分类展示课表；课程提醒通知；数据导入与导出；设备间课表同步（可选功能）。"
                )

                LegalSection(
                    title = "2. 开源许可",
                    content = "本应用的源代码依据 MIT License 开源发布。您可以在遵守 MIT License 条款的前提下，自由地使用、复制、修改、合并、出版发行、再许可及/或销售本应用的副本。完整的许可文本请参见应用内\u201c开源致谢\u201d页面或项目源代码仓库。"
                )

                LegalSection(
                    title = "3. 用户义务",
                    content = "您在使用本应用时应遵守以下义务：（1）遵守中华人民共和国法律法规及其他适用法律；（2）不得利用本应用从事任何违法违规活动，包括但不限于侵犯他人隐私、传播恶意软件等；（3）您对自己输入和管理的课程数据的内容和准确性负责；（4）不得以任何方式干扰本应用的正常运行，包括但不限于反向工程、反编译或试图提取源代码（基于 MIT 许可的开源代码仓库除外）。"
                )

                LegalSection(
                    title = "4. 知识产权",
                    content = "本应用的源代码依据 MIT License 开源，不限制对代码的使用、修改和分发。但本应用的名称\u201cDuoSchedule\u201d、图标、品牌标识等商标性元素不属于 MIT License 授权范围，未经明确书面许可，他人不得将这些元素用于商业推广或冒充本应用。"
                )

                LegalSection(
                    title = "5. 免责声明",
                    content = "本应用按\u201c原样\u201d（AS IS）提供，不附带任何形式的明示或暗示的担保，包括但不限于对适销性、特定用途适用性及不侵权的担保。开发者不保证本应用的运行不受干扰或不含错误，也不保证任何缺陷会被修正。"
                )

                LegalSection(
                    title = "6. 责任限制",
                    content = "在适用法律允许的最大范围内，开发者对因使用或无法使用本应用所产生的任何直接、间接、附带、特殊、惩罚性或结果性损害（包括但不限于数据丢失、课程提醒遗漏导致的损失、业务中断等）不承担任何责任，即使开发者已被告知此类损害的可能性。"
                )

                LegalSection(
                    title = "7. 协议修改",
                    content = "开发者保留随时修改本协议的权利。协议修改后，更新后的条款将在本页面发布并标注最后更新日期。重大变更可能会通过应用内通知或其他合理方式提示您。您在协议修改后继续使用本应用，即视为您同意接受修改后的协议。建议您定期查阅本协议。"
                )

                LegalSection(
                    title = "8. 法律适用与争议解决",
                    content = "本协议的订立、执行、解释及争议解决均适用中华人民共和国法律。因本协议引起的或与本协议有关的任何争议，双方应首先友好协商解决；协商不成的，任何一方均有权向开发者所在地有管辖权的人民法院提起诉讼。"
                )

                LegalSection(
                    title = "9. 联系方式",
                    content = "如您对本协议有任何疑问、意见或建议，请通过以下方式联系我们：\n• GitHub Issues：https://github.com/yang-jk/duoschedule/issues\n我们将在合理时间内予以回复。"
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "隐私政策",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = getLabelsVibrantSecondary(),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Text(
                    text = "本隐私政策旨在向您说明本应用如何收集、使用、存储和保护您的信息。我们深知个人信息对您的重要性，并承诺以最高标准保护您的隐私。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = getLabelsVibrantPrimary(),
                    lineHeight = 1.6.em,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Text(
                    text = "本政策最后更新日期：2026 年 5 月 28 日",
                    style = MaterialTheme.typography.bodySmall,
                    color = getLabelsVibrantTertiary(),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LegalSection(
                    title = "1. 信息收集原则",
                    content = "本应用坚持数据最小化原则。所有课程数据（包括课程名称、上课时间、教室、任课教师等）和个人设置（主题偏好、提醒设置等）均存储在您的设备本地存储空间中（Android Room 数据库与 DataStore）。本应用不设置任何后台服务器，因此您的数据不会（也无法）被上传到任何远程服务器。"
                )

                LegalSection(
                    title = "2. 我们不收集的信息",
                    content = "本应用明确不会收集以下任何信息：\n• 个人身份信息（姓名、身份证号、手机号码、电子邮箱等）\n• 设备标识符（IMEI、MAC 地址、Android ID 等）\n• 精确或粗略地理位置信息\n• 联系人、通话记录、短信内容\n• 使用行为分析数据或崩溃日志\n• 任何形式的生物识别信息"
                )

                LegalSectionWithSubSections(
                    title = "3. 权限使用说明",
                    content = "本应用申请以下系统权限，均仅用于实现核心功能，不会用于其他目的：",
                    subSections = listOf(
                        LegalSubItem("网络访问权限", "用于以下功能：（1）从 GitHub/Gitee 检查应用更新版本；（2）从教务系统网页导入课程数据（需您主动操作）；（3）通过 WebDAV 协议实现设备间课表同步（需您主动配置）。上述网络操作均不会传输您的个人信息。"),
                        LegalSubItem("通知权限", "用于在您设定的时间发送课程提醒通知。本应用的通知功能完全在设备本地运行，提醒内容基于本地存储的课程数据生成。"),
                        LegalSubItem("精确闹钟权限", "用于确保课程提醒在您设定的精确时间触发。Android 系统要求此权限才能设置精确时间的闹钟。"),
                        LegalSubItem("开机自启动权限", "用于在设备重启后自动恢复已设置的课程提醒闹钟，确保您不会因设备重启而错过课程提醒。"),
                        LegalSubItem("前台服务权限", "用于维持提醒服务的正常运行，确保课程提醒能够准时触发。此服务仅在后台运行，不会收集或传输任何数据。"),
                        LegalSubItem("免打扰权限", "用于在您开启\u201c上课免打扰\u201d功能时，自动将设备切换至静音或振动模式，下课后自动恢复。此功能完全在设备本地执行。"),
                        LegalSubItem("安装应用权限", "仅用于应用内更新功能，在您主动确认更新后下载并安装新版本 APK。本应用不会自动安装任何第三方应用。")
                    )
                )

                LegalSection(
                    title = "4. 数据使用方式",
                    content = "本地存储的课程数据仅用于以下目的：（1）在应用界面展示课程表；（2）根据您的设置发送课程提醒通知；（3）向桌面小组件提供课程数据；（4）执行您主动触发的数据导出操作；（5）执行您主动配置的设备间同步。除此之外，数据不会被用于任何其他目的。"
                )

                LegalSection(
                    title = "5. 数据存储与安全",
                    content = "所有数据存储在您的设备本地，受到 Android 系统沙盒机制的保护，其他应用无法访问本应用的数据目录。建议您定期通过应用内的导出功能备份数据。如果您卸载本应用，所有本地数据将被一并删除且不可恢复。本应用不提供云端备份功能，开发者无法恢复您已删除或丢失的数据。"
                )

                LegalSectionWithSubSections(
                    title = "6. 第三方服务",
                    content = "本应用在以下有限情况下涉及第三方服务：",
                    subSections = listOf(
                        LegalSubItem("应用更新检查", "通过 jsDelivr CDN 访问存储在 GitHub 上的更新信息 JSON 文件。此过程仅请求版本号、更新日志等公开信息，不传输任何用户数据。备选更新源为 Gitee。"),
                        LegalSubItem("WebDAV 同步（可选功能）", "如果您主动启用并配置了 WebDAV 同步功能，课程数据将加密后通过 HTTPS 传输至您指定的 WebDAV 服务器（如坚果云等）。WebDAV 服务器的隐私政策由相应服务提供商制定，建议您在使用前阅读其隐私条款。本应用不会在未经您明确授权的情况下启用此功能。"),
                        LegalSubItem("教务系统导入（可选功能）", "如果您主动使用教务系统导入功能，本应用将通过 HTTPS 访问您指定的教务系统网页。您的登录凭据仅在您设备的本地 WebView 中处理，不会经由任何第三方服务器中转或存储。")
                    )
                )

                LegalSection(
                    title = "7. 您的权利",
                    content = "根据《中华人民共和国个人信息保护法》及其他相关法律法规，您对您的数据享有以下权利：\n• 查阅权：您可以在应用内随时查看所有已存储的课程数据\n• 更正权：您可以在应用内随时编辑、修改任何课程数据\n• 删除权：您可以在应用内随时删除任何课程数据，或通过卸载应用删除全部数据\n• 导出权：您可以通过应用内的导出功能将课程数据导出为 CSV 文件\n• 撤回同意权：您可以在系统设置中随时撤销已授予的任何权限"
                )

                LegalSection(
                    title = "8. 未成年人保护",
                    content = "本应用是一款课程管理工具，不包含任何不适合未成年人的内容。如果您是未满 14 周岁的儿童，请在监护人的陪同下阅读本隐私政策，并在获得监护人同意后使用本应用。如果您是未成年人的监护人，请确保被监护人在您的指导和同意下使用本应用。"
                )

                LegalSection(
                    title = "9. 政策更新",
                    content = "我们可能会不时更新本隐私政策。更新后的政策将在本页面发布，并标注最后更新日期。对于重大变更，我们可能会通过应用内通知或其他适当方式提示您。建议您定期查阅本政策，以了解我们如何保护您的隐私。"
                )

                LegalSection(
                    title = "10. 联系方式",
                    content = "如果您对本隐私政策有任何疑问、意见或投诉，或者希望行使您的数据权利，请通过以下方式联系我们：\n• GitHub Issues：https://github.com/yang-jk/duoschedule/issues\n我们将在合理时间内予以回复和处理。"
                )

                Spacer(modifier = Modifier.height(LiquidBottomTabsSpec.Height + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp))
            }
        }
    }
}

private data class LegalSubItem(
    val title: String,
    val content: String
)

@Composable
private fun LegalSection(
    title: String,
    content: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        colors = CardDefaults.defaultColors()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = getLabelsVibrantPrimary(),
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = getLabelsVibrantSecondary(),
                lineHeight = 1.6.em
            )
        }
    }
}

@Composable
private fun LegalSectionWithSubSections(
    title: String,
    content: String,
    subSections: List<LegalSubItem>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        colors = CardDefaults.defaultColors()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = getLabelsVibrantPrimary(),
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = getLabelsVibrantSecondary(),
                lineHeight = 1.6.em,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            subSections.forEachIndexed { index, subItem ->
                LegalSubSection(
                    title = subItem.title,
                    content = subItem.content
                )
                if (index < subSections.size - 1) {
                    Separator(
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = 0.5.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun LegalSubSection(
    title: String,
    content: String
) {
    Row(
        modifier = Modifier.padding(bottom = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(IntrinsicSize.Max)
                .padding(vertical = 2.dp)
                .clip(ContinuousRoundedRectangle(1.5.dp))
                .background(getLabelsVibrantPrimary().copy(alpha = 0.3f))
        )
        Spacer(modifier = Modifier.width(9.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = getLabelsVibrantSecondary(),
                modifier = Modifier.padding(bottom = 2.dp)
            )
            Text(
                text = content,
                style = MaterialTheme.typography.bodySmall,
                color = getLabelsVibrantTertiary(),
                lineHeight = 1.5.em
            )
        }
    }
}
