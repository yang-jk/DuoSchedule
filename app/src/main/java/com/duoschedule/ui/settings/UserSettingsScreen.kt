package com.duoschedule.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.duoschedule.data.model.PersonType
import com.duoschedule.ui.settings.components.*
import com.duoschedule.ui.theme.*
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val personAName by viewModel.personAName.collectAsState()
    val personBName by viewModel.personBName.collectAsState()

    var showPersonADialog by remember { mutableStateOf(false) }
    var showPersonBDialog by remember { mutableStateOf(false) }

    val labelsPrimary = getLabelsVibrantPrimary()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "用户设置",
                        color = labelsPrimary,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    ) 
                },
                navigationIcon = {
                    GlassSymbolIconButton(
                        onClick = onNavigateBack,
                        style = GlassSymbolButtonStyle.NonTinted,
                        size = ComponentSize.LiquidGlassButton.TopAppBarIconButtonSize,
                        contentPadding = PaddingValues(start = Spacing.sm)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "返回",
                            tint = labelsPrimary
                        )
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
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(Spacing.iOS26.groupSpacing)
        ) {
            Spacer(modifier = Modifier.height(Spacing.sm))

            SettingsSection(title = "人员名称") {
                SettingsNavigationRow(
                    title = "我的名称",
                    subtitle = "设置自己的显示名称",
                    icon = Icons.Outlined.Person,
                    iconBackgroundColor = BrandColors.PersonB,
                    value = personBName,
                    onClick = { showPersonBDialog = true }
                )
                
                Separator(modifier = Modifier.padding(horizontal = Spacing.lg))
                
                SettingsNavigationRow(
                    title = "Ta的名称",
                    subtitle = "设置对方的显示名称",
                    icon = Icons.Outlined.Person,
                    iconBackgroundColor = BrandColors.PersonA,
                    value = personAName,
                    onClick = { showPersonADialog = true }
                )
            }

            SettingsFooter(
                text = "这些名称将显示在课表和主页中，用于区分两个人的课程。"
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }

    if (showPersonADialog) {
        val backdrop = LocalBackdrop.current ?: emptyBackdrop()
        TextInputAlert(
            backdrop = backdrop,
            title = "Ta的名称",
            label = "请输入Ta的名称",
            initialValue = personAName,
            onDismiss = { showPersonADialog = false },
            onConfirm = { 
                viewModel.setPersonName(PersonType.PERSON_A, it)
                showPersonADialog = false
            },
            placeholder = "例如：小明"
        )
    }

    if (showPersonBDialog) {
        val backdrop = LocalBackdrop.current ?: emptyBackdrop()
        TextInputAlert(
            backdrop = backdrop,
            title = "我的名称",
            label = "请输入我的名称",
            initialValue = personBName,
            onDismiss = { showPersonBDialog = false },
            onConfirm = { 
                viewModel.setPersonName(PersonType.PERSON_B, it)
                showPersonBDialog = false
            },
            placeholder = "例如：小红"
        )
    }
}
