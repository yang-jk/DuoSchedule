package com.duoschedule.ui.settings

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.duoschedule.data.importexport.ImportResult
import com.duoschedule.data.importexport.ImportPreviewData
import com.duoschedule.data.model.AppThemeMode
import com.duoschedule.data.model.PersonType
import com.duoschedule.ui.settings.components.*
import com.duoschedule.ui.theme.*
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop as kyantLayerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop as kyantRememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.layerBackdrop
import com.kyant.backdrop.drawBackdrop

import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.platform.LocalDensity
import com.kyant.capsule.ContinuousRoundedRectangle
import com.duoschedule.util.FilePickerUtils
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.window.WindowDialog
import androidx.compose.foundation.shape.RoundedCornerShape
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun DataManagementScreen(
    onNavigateBack: () -> Unit,
    onNavigateToImportPreview: (ImportPreviewData) -> Unit,
    onNavigateToEducationImport: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()
    val scope = rememberCoroutineScope()
    val appThemeMode = LocalAppThemeMode.current
    
    var isLoading by remember { mutableStateOf(false) }
    var loadingMessage by remember { mutableStateOf("") }
    var showImportResultDialog by remember { mutableStateOf(false) }
    var importResult by remember { mutableStateOf<ImportResult?>(null) }
    var backupFiles by remember { mutableStateOf<List<File>>(emptyList()) }

    LaunchedEffect(Unit) {
        backupFiles = viewModel.getBackupFiles()
    }

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
                        personBName = result.personBName,
                        exportVersion = result.exportVersion
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
            BlurredBar(hazeState, backdrop = miuixBackdrop, enabled = blurEnabled, contentBackdrop = contentBackdrop) {
                SmallTopAppBar(
                    title = "数据管理",
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
        val deviceInfo = remember { FilePickerUtils.getDeviceInfo() }

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
            if (deviceInfo.isMiuiDevice) {
                val accentColor = if (deviceInfo.isHyperOS) IOSColors.Green else IOSColors.Orange
                if (appThemeMode == AppThemeMode.MIUIX) {
                    Surface(
                        shape = RoundedCornerShape(BorderRadius.lg),
                        color = MiuixTheme.colorScheme.surfaceContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.lg)
                    ) {
                        Row(
                            modifier = Modifier.padding(Spacing.md),
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
                                top.yukonga.miuix.kmp.basic.Text(
                                    text = if (deviceInfo.isHyperOS) {
                                        "小米文件选择器"
                                    } else {
                                        "系统文件选择器"
                                    },
                                    fontWeight = FontWeight.Medium
                                )
                                top.yukonga.miuix.kmp.basic.Text(
                                    text = if (deviceInfo.isHyperOS) {
                                        "HyperOS ${deviceInfo.miuiVersion} · 支持最新文件选择控件"
                                    } else if (deviceInfo.isMiuiDevice) {
                                        "MIUI ${deviceInfo.miuiVersion} · 升级至 HyperOS 可获得更好体验"
                                    } else {
                                        "Android ${deviceInfo.androidVersion}"
                                    },
                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                )
                            }
                        }
                    }
                } else {
                    val miuiBackdrop = LocalBackdrop.current ?: emptyBackdrop()
                    val darkTheme = LocalDarkTheme.current
                    val density = LocalDensity.current

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

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.lg)
                            .drawBackdrop(
                                backdrop = miuiBackdrop,
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
                        onNavigateToEducationImport()
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

            SettingsSection(title = "备份管理") {
                if (backupFiles.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.lg, vertical = Spacing.sm)
                    ) {
                        Text(
                            text = "暂无备份（导入课程时自动创建）",
                            style = MaterialTheme.typography.bodySmall,
                            color = labelsSecondary
                        )
                    }
                } else {
                    backupFiles.forEach { file ->
                        val displayName = try {
                            val regex = Regex("backup_pre_import_(\\d{4})(\\d{2})(\\d{2})_(\\d{2})(\\d{2})(\\d{2})\\.csv")
                            val match = regex.find(file.name)
                            if (match != null) {
                                "${match.groupValues[1]}-${match.groupValues[2]}-${match.groupValues[3]} ${match.groupValues[4]}:${match.groupValues[5]}:${match.groupValues[6]}"
                            } else file.name
                        } catch (e: Exception) { file.name }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Spacing.lg, vertical = Spacing.xs),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = labelsPrimary
                                )
                                Text(
                                    text = "${file.length() / 1024}KB",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = labelsSecondary
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                                if (appThemeMode == AppThemeMode.MIUIX) {
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                isLoading = true
                                                loadingMessage = "正在读取备份..."
                                                val uri = FileProvider.getUriForFile(
                                                    context,
                                                    "${context.packageName}.fileprovider",
                                                    file
                                                )
                                                val result = viewModel.importFromCsv(context, uri)
                                                isLoading = false
                                                if (result.success && result.courses.isNotEmpty()) {
                                                    onNavigateToImportPreview(ImportPreviewData(
                                                        courses = result.courses,
                                                        fileType = result.fileType,
                                                        settingsA = result.settingsA,
                                                        settingsB = result.settingsB,
                                                        personAName = result.personAName,
                                                        personBName = result.personBName,
                                                        exportVersion = result.exportVersion
                                                    ))
                                                } else {
                                                    showImportResultDialog = true
                                                    importResult = result
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColorsPrimary()
                                    ) {
                                        top.yukonga.miuix.kmp.basic.Text("恢复")
                                    }
                                    Button(
                                        onClick = {
                                            viewModel.deleteBackupFile(file.name)
                                            backupFiles = viewModel.getBackupFiles()
                                        },
                                        colors = ButtonDefaults.buttonColors()
                                    ) {
                                        top.yukonga.miuix.kmp.basic.Text("删除")
                                    }
                                } else {
                                    LiquidGlassButton(
                                        text = "恢复",
                                        onClick = {
                                            scope.launch {
                                                isLoading = true
                                                loadingMessage = "正在读取备份..."
                                                val uri = FileProvider.getUriForFile(
                                                    context,
                                                    "${context.packageName}.fileprovider",
                                                    file
                                                )
                                                val result = viewModel.importFromCsv(context, uri)
                                                isLoading = false
                                                if (result.success && result.courses.isNotEmpty()) {
                                                    onNavigateToImportPreview(ImportPreviewData(
                                                        courses = result.courses,
                                                        fileType = result.fileType,
                                                        settingsA = result.settingsA,
                                                        settingsB = result.settingsB,
                                                        personAName = result.personAName,
                                                        personBName = result.personBName,
                                                        exportVersion = result.exportVersion
                                                    ))
                                                } else {
                                                    showImportResultDialog = true
                                                    importResult = result
                                                }
                                            }
                                        },
                                        style = LiquidGlassButtonStyle.Tinted
                                    )
                                    LiquidGlassButton(
                                        text = "删除",
                                        onClick = {
                                            viewModel.deleteBackupFile(file.name)
                                            backupFiles = viewModel.getBackupFiles()
                                        },
                                        style = LiquidGlassButtonStyle.NonTinted
                                    )
                                }
                            }
                        }
                        if (file != backupFiles.last()) {
                            Separator(modifier = Modifier.padding(horizontal = Spacing.lg))
                        }
                    }
                }
            }

            SettingsFooter(
                text = "导出的 CSV 文件可用于备份或迁移数据。导入时请确保文件格式正确。"
            )

            Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
            Spacer(modifier = Modifier.weight(1f))
            }
        }
    }

    if (isLoading) {
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
                    top.yukonga.miuix.kmp.basic.Text(loadingMessage)
                }
            }
        } else {
            IOSLoadingDialog(
                message = loadingMessage,
                onDismiss = { }
            )
        }
    }

    if (showImportResultDialog && importResult != null) {
        if (importResult!!.success && importResult!!.importedCount > 0) {
            if (appThemeMode == AppThemeMode.MIUIX) {
                WindowDialog(
                    show = true,
                    title = "导入成功",
                    onDismissRequest = { showImportResultDialog = false }
                ) {
                    top.yukonga.miuix.kmp.basic.Text(
                        text = "成功解析 ${importResult!!.importedCount} 门课程",
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                    )
                    Spacer(modifier = Modifier.height(Spacing.lg))
                    Button(
                        onClick = { showImportResultDialog = false },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColorsPrimary()
                    ) {
                        top.yukonga.miuix.kmp.basic.Text("确定")
                    }
                }
            } else {
                IOSSuccessDialog(
                    title = "导入成功",
                    message = "成功解析 ${importResult!!.importedCount} 门课程",
                    onDismiss = { showImportResultDialog = false },
                    dismissText = "确定"
                )
            }
        } else {
            if (appThemeMode == AppThemeMode.MIUIX) {
                WindowDialog(
                    show = true,
                    title = "导入失败",
                    onDismissRequest = { showImportResultDialog = false }
                ) {
                    val errorMsg = if (importResult!!.errors.isEmpty()) "无法解析文件内容" else importResult!!.errors.take(5).joinToString("\n") { "• $it" }
                    top.yukonga.miuix.kmp.basic.Text(
                        text = errorMsg,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                    )
                    Spacer(modifier = Modifier.height(Spacing.lg))
                    Button(
                        onClick = { showImportResultDialog = false },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColorsPrimary()
                    ) {
                        top.yukonga.miuix.kmp.basic.Text("确定")
                    }
                }
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
}
