package com.duoschedule.ui.settings

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.duoschedule.data.importexport.ImportResult
import com.duoschedule.data.importexport.ImportPreviewData
import com.duoschedule.data.model.PersonType
import com.duoschedule.ui.settings.components.*
import com.duoschedule.ui.theme.*
import com.duoschedule.ui.theme.LocalBackdrop
import com.duoschedule.ui.theme.LiquidGlassColors
import com.duoschedule.ui.theme.GlassBottomSheetDefaults
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.platform.LocalDensity
import com.kyant.capsule.ContinuousRoundedRectangle
import com.duoschedule.util.FilePickerUtils
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataManagementScreen(
    onNavigateBack: () -> Unit,
    onNavigateToImportPreview: (ImportPreviewData) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val labelsPrimary = getLabelsVibrantPrimary()
    val scope = rememberCoroutineScope()
    
    var isLoading by remember { mutableStateOf(false) }
    var loadingMessage by remember { mutableStateOf("") }
    var showImportResultDialog by remember { mutableStateOf(false) }
    var importResult by remember { mutableStateOf<ImportResult?>(null) }

    val csvImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            scope.launch {
                isLoading = true
                loadingMessage = "正在解析 CSV 文件..."
                val result = viewModel.importFromCsv(context, uri)
                importResult = result
                isLoading = false
                
                if (result.success && result.courses.isNotEmpty()) {
                    onNavigateToImportPreview(ImportPreviewData(
                        courses = result.courses,
                        fileType = result.fileType,
                        settingsA = result.settingsA,
                        settingsB = result.settingsB,
                        personAName = result.personAName,
                        personBName = result.personBName
                    ))
                } else {
                    showImportResultDialog = true
                }
            }
        }
    }

    val wakeupImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            scope.launch {
                isLoading = true
                loadingMessage = "正在解析 Wakeup 文件..."
                val result = viewModel.importFromWakeup(context, uri, PersonType.PERSON_B)
                importResult = result
                isLoading = false
                
                if (result.success && result.courses.isNotEmpty()) {
                    onNavigateToImportPreview(ImportPreviewData(
                        courses = result.courses
                    ))
                } else {
                    showImportResultDialog = true
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "数据管理",
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
        val deviceInfo = remember { FilePickerUtils.getDeviceInfo() }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.iOS26.groupSpacing)
        ) {
            Spacer(modifier = Modifier.height(Spacing.sm))
            
            if (deviceInfo.isMiuiDevice) {
                val backdrop = LocalBackdrop.current ?: emptyBackdrop()
                val darkTheme = LocalDarkTheme.current
                val density = LocalDensity.current
                val labelsPrimary = getLabelsVibrantPrimary()
                val labelsSecondary = getLabelsVibrantSecondary()

                val layer1Tint = if (darkTheme) {
                    LiquidGlassColors.BottomSheet.Dark.Layer1_Tint
                } else {
                    LiquidGlassColors.BottomSheet.Light.Layer1_Tint
                }

                val layer1Alpha = if (darkTheme) {
                    LiquidGlassColors.BottomSheet.Dark.Layer1_Alpha
                } else {
                    LiquidGlassColors.BottomSheet.Light.Layer1_Alpha
                }

                val layer2Base = if (darkTheme) {
                    LiquidGlassColors.BottomSheet.Dark.Layer2_Base
                } else {
                    LiquidGlassColors.BottomSheet.Light.Layer2_Base
                }

                val glassEffect = if (darkTheme) {
                    LiquidGlassColors.BottomSheet.Dark.GlassEffect
                } else {
                    LiquidGlassColors.BottomSheet.Light.GlassEffect
                }

                val accentColor = if (deviceInfo.isHyperOS) IOSColors.Green else IOSColors.Orange

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg)
                        .drawBackdrop(
                            backdrop = backdrop,
                            shape = { ContinuousRoundedRectangle(16.dp) },
                            effects = {
                                vibrancy()
                                blur(with(density) { GlassBottomSheetDefaults.BlurRadius.toPx() })
                                lens(
                                    refractionHeight = with(density) { GlassBottomSheetDefaults.LensRefractionHeight.toPx() },
                                    refractionAmount = with(density) { GlassBottomSheetDefaults.LensRefractionAmount.toPx() },
                                    chromaticAberration = true
                                )
                            },
                            onDrawSurface = {
                                drawRect(accentColor, blendMode = BlendMode.Hue)
                                drawRect(accentColor.copy(alpha = 0.1f))
                            }
                        )
                        .padding(Spacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = if (deviceInfo.isHyperOS) {
                            Icons.Outlined.CheckCircle
                        } else {
                            Icons.Outlined.Info
                        },
                        contentDescription = null,
                        tint = accentColor
                    )
                    Column {
                        Text(
                            text = if (deviceInfo.isHyperOS) {
                                "小米文件选择器"
                            } else {
                                "系统文件选择器"
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = labelsPrimary
                        )
                        Text(
                            text = if (deviceInfo.isHyperOS) {
                                "HyperOS ${deviceInfo.miuiVersion} · 支持最新文件选择控件"
                            } else if (deviceInfo.isMiuiDevice) {
                                "MIUI ${deviceInfo.miuiVersion} · 升级至 HyperOS 可获得更好体验"
                            } else {
                                "Android ${deviceInfo.androidVersion}"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = labelsSecondary
                        )
                    }
                }
            }

            SettingsSection(title = "数据导出") {
                SettingsNavigationRow(
                    title = "导出课表数据",
                    subtitle = "将课表数据导出为 CSV 文件",
                    icon = Icons.Outlined.FileUpload,
                    iconBackgroundColor = IOSColors.Blue,
                    onClick = {
                        scope.launch {
                            isLoading = true
                            loadingMessage = "正在生成导出文件..."
                            
                            val fileName = "duoschedule_export_${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))}.csv"
                            val result = viewModel.exportToCacheFile(context, fileName)
                            
                            isLoading = false
                            
                            if (result.isSuccess) {
                                val file = result.getOrNull()
                                if (file != null) {
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        file
                                    )
                                    
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/csv"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    
                                    val chooserIntent = Intent.createChooser(shareIntent, "导出课表数据").apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    
                                    context.startActivity(chooserIntent)
                                }
                            } else {
                                Toast.makeText(context, "导出失败: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            }

            SettingsSection(title = "数据导入") {
                SettingsNavigationRow(
                    title = "从 CSV 文件导入",
                    subtitle = "从 CSV 文件导入课表数据",
                    icon = Icons.Outlined.FileDownload,
                    iconBackgroundColor = IOSColors.Green,
                    onClick = {
                        csvImportLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "*/*"))
                    }
                )
                
                Separator(modifier = Modifier.padding(horizontal = Spacing.lg))
                
                SettingsNavigationRow(
                    title = "从 Wakeup 课程表导入",
                    subtitle = "导入 Wakeup 课程表的数据",
                    icon = Icons.Outlined.CloudDownload,
                    iconBackgroundColor = IOSColors.Orange,
                    onClick = {
                        wakeupImportLauncher.launch(arrayOf("application/json", "*/*"))
                    }
                )
                
                Separator(modifier = Modifier.padding(horizontal = Spacing.lg))
                
                SettingsNavigationRow(
                    title = "从教务系统导入",
                    subtitle = "连接教务系统获取课表",
                    icon = Icons.Outlined.School,
                    iconBackgroundColor = IOSColors.Purple,
                    onClick = {
                        Toast.makeText(context, "功能开发中", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            SettingsSection(title = "模板下载") {
                SettingsNavigationRow(
                    title = "下载 CSV 模板",
                    subtitle = "下载课表导入模板文件",
                    icon = Icons.Outlined.Description,
                    iconBackgroundColor = IOSColors.Gray,
                    onClick = {
                        scope.launch {
                            isLoading = true
                            loadingMessage = "正在生成模板..."
                            
                            try {
                                val fileName = "duoschedule_template.csv"
                                val cacheDir = File(context.cacheDir, "export")
                                if (!cacheDir.exists()) cacheDir.mkdirs()
                                val file = File(cacheDir, fileName)
                                
                                file.writeText("课程名称（必填）,星期(1-7),开始节次,结束节次,教室地点,上课老师,周次\n示例课程,1,1,2,教学楼101,张老师,1-16\n高等数学,2,3,4,A201,李老师,1-16\n")
                                
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    file
                                )
                                
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/csv"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                
                                val chooserIntent = Intent.createChooser(shareIntent, "分享模板文件").apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                
                                context.startActivity(chooserIntent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "生成失败: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                            
                            isLoading = false
                        }
                    }
                )
            }

            SettingsFooter(
                text = "导出的 CSV 文件可用于备份或迁移数据。导入时请确保文件格式正确。"
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }

    if (isLoading) {
        IOSLoadingDialog(
            message = loadingMessage,
            onDismiss = { }
        )
    }

    if (showImportResultDialog && importResult != null) {
        if (importResult!!.success && importResult!!.importedCount > 0) {
            IOSSuccessDialog(
                title = "导入成功",
                message = "成功解析 ${importResult!!.importedCount} 门课程",
                onDismiss = { showImportResultDialog = false },
                dismissText = "确定"
            )
        } else {
            IOSErrorDialog(
                title = "导入失败",
                message = if (importResult!!.errors.isEmpty()) "无法解析文件内容" else null,
                errors = importResult!!.errors,
                onDismiss = { showImportResultDialog = false }
            )
        }
    }
}
