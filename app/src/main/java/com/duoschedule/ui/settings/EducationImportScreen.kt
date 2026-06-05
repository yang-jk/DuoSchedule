package com.duoschedule.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.duoschedule.data.importexport.ImportPreviewData
import com.duoschedule.data.importexport.SupportedSchools
import com.duoschedule.data.importexport.ZhengfangSchoolAdapter
import com.duoschedule.data.importexport.QiangzhiSchoolAdapter
import com.duoschedule.data.model.AppThemeMode
import com.duoschedule.ui.settings.components.*
import com.duoschedule.ui.theme.*
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.theme.MiuixTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EducationImportScreen(
    onNavigateBack: () -> Unit,
    onNavigateToImportPreview: (ImportPreviewData) -> Unit,
    onNavigateToEducationWebView: (String) -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val labelsPrimary = getLabelsVibrantPrimary()
    val appThemeMode = LocalAppThemeMode.current

    val schools = remember {
        SupportedSchools.register(ZhengfangSchoolAdapter())
        SupportedSchools.register(QiangzhiSchoolAdapter())
        SupportedSchools.getSchools()
    }

    if (appThemeMode == AppThemeMode.MIUIX) {
        Scaffold(
            topBar = {
                SmallTopAppBar(
                    title = "从教务系统导入",
                    scrollBehavior = MiuixScrollBehavior(),
                    color = androidx.compose.ui.graphics.Color.Transparent,
                    titleColor = MiuixTheme.colorScheme.onSurface,
                    defaultWindowInsetsPadding = false,
                    navigationIcon = {
                        top.yukonga.miuix.kmp.basic.IconButton(onClick = {
                            onNavigateBack()
                        }) {
                            Icon(
                                Icons.Default.ChevronLeft,
                                contentDescription = "返回",
                                tint = MiuixTheme.colorScheme.onSurface
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding())
                    .verticalScroll(rememberScrollState())
            ) {
                // School selection
                SettingsSection(title = "选择学校") {
                    schools.forEach { school ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onNavigateToEducationWebView(school.id)
                                }
                                .padding(horizontal = Spacing.lg, vertical = Spacing.md)
                        ) {
                            top.yukonga.miuix.kmp.basic.Text(
                                text = school.name,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                                color = MiuixTheme.colorScheme.onBackground
                            )
                            if (school.vpnHint.isNotEmpty()) {
                                top.yukonga.miuix.kmp.basic.Text(
                                    text = school.vpnHint,
                                    color = BrandColors.Primary,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        if (school != schools.last()) {
                            Separator(modifier = Modifier.padding(horizontal = Spacing.lg))
                        }
                    }
                }

                Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
            }
        }
    } else {
        // Material3 theme
        Scaffold(
            contentWindowInsets = WindowInsets(0),
            topBar = {
                TopAppBar(
                    title = { Text("从教务系统导入") },
                    navigationIcon = {
                        GlassSymbolIconButton(
                            onClick = {
                                onNavigateBack()
                            },
                            style = GlassSymbolButtonStyle.NonTinted,
                            buttonSize = ComponentSize.LiquidGlassButton.TopAppBarIconButtonSize,
                            contentPadding = PaddingValues(start = Spacing.sm)
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "返回", tint = getLabelsVibrantPrimary())
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
                    .verticalScroll(rememberScrollState())
            ) {
                // School selection
                SettingsSection(title = "选择学校") {
                    schools.forEach { school ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onNavigateToEducationWebView(school.id)
                                }
                                .padding(horizontal = Spacing.lg, vertical = Spacing.md)
                        ) {
                            Text(
                                text = school.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                                color = labelsPrimary
                            )
                            if (school.vpnHint.isNotEmpty()) {
                                Text(
                                    text = school.vpnHint,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = BrandColors.Primary
                                )
                            }
                        }
                        if (school != schools.last()) {
                            Separator(modifier = Modifier.padding(horizontal = Spacing.lg))
                        }
                    }
                }

                Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
            }
        }
    }
}
