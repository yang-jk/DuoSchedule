package com.duoschedule.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import com.kyant.capsule.ContinuousRoundedRectangle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.duoschedule.data.importexport.CourseImportData
import com.duoschedule.data.importexport.CsvFileType
import com.duoschedule.data.importexport.ImportPreviewData
import com.duoschedule.data.importexport.ScheduleSettingsExport
import com.duoschedule.data.model.PersonType
import com.duoschedule.ui.theme.LiquidGlassButton
import com.duoschedule.ui.theme.LiquidGlassButtonStyle
import com.duoschedule.ui.theme.LiquidToggle
import com.duoschedule.ui.theme.GlassSymbolIconButton
import com.duoschedule.ui.theme.GlassSymbolButtonStyle
import com.duoschedule.ui.theme.SegmentedControl
import com.duoschedule.ui.theme.SegmentOption
import com.duoschedule.ui.theme.ComponentSize
import com.duoschedule.ui.theme.LocalDarkTheme
import com.duoschedule.ui.theme.LocalBackdrop
import com.duoschedule.ui.theme.getDialogBackgroundColor
import com.duoschedule.ui.theme.getLabelsVibrantPrimary
import com.duoschedule.ui.theme.getLabelsVibrantSecondary
import com.duoschedule.ui.theme.Spacing
import com.duoschedule.ui.theme.BrandColors
import com.duoschedule.ui.theme.GlassCard
import com.duoschedule.ui.theme.LiquidGlassColors
import com.duoschedule.ui.theme.GlassBottomSheetDefaults
import com.duoschedule.ui.settings.components.IOSConfirmDialog
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.platform.LocalDensity
import com.duoschedule.ui.settings.components.IOSSuccessDialog
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class ImportPreviewItem(
    val data: CourseImportData,
    val isSelected: Boolean = true,
    val hasConflict: Boolean = false,
    val conflictReason: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportPreviewScreen(
    courses: List<CourseImportData>,
    onConfirm: (selectedCourses: List<CourseImportData>, targetPerson: PersonType, mergeMode: Boolean) -> Unit,
    onDismiss: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    onImportSuccess: ((PersonType) -> Unit)? = null,
    onNavigateToHome: (() -> Unit)? = null,
    importData: ImportPreviewData? = null
) {
    val actualImportData = importData ?: ImportPreviewData(courses = courses)
    val isAppExport = actualImportData.fileType == CsvFileType.APP_EXPORT
    
    val previewItems = remember {
        mutableStateListOf<ImportPreviewItem>()
    }
    
    var selectedTarget by remember { mutableStateOf<PersonType?>(PersonType.PERSON_B) }
    var mergeMode by remember { mutableStateOf(true) }
    var importSettings by remember { mutableStateOf(isAppExport) }
    var showTargetDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var importedTarget by remember { mutableStateOf<PersonType?>(null) }
    var importedCount by remember { mutableIntStateOf(0) }
    var isImporting by remember { mutableStateOf(false) }
    
    var personATarget by remember { mutableStateOf<PersonType?>(null) }
    var personBTarget by remember { mutableStateOf<PersonType?>(null) }
    
    val scope = rememberCoroutineScope()
    
    val conflictCount = previewItems.count { it.hasConflict }
    val selectedCount = previewItems.count { it.isSelected }
    val totalCount = previewItems.size
    
    val coursesForPersonA by remember {
        derivedStateOf {
            previewItems.filter { it.data.personType == PersonType.PERSON_A }
        }
    }
    val coursesForPersonB by remember {
        derivedStateOf {
            previewItems.filter { it.data.personType == PersonType.PERSON_B }
        }
    }
    
    LaunchedEffect(actualImportData.courses, selectedTarget, isAppExport) {
        previewItems.clear()
        
        val existingCoursesA = viewModel.getExistingCourses(PersonType.PERSON_A)
        val existingCoursesB = viewModel.getExistingCourses(PersonType.PERSON_B)
        
        val items = actualImportData.courses.map { importData ->
            val targetPersonType = if (isAppExport) {
                importData.personType
            } else {
                selectedTarget ?: PersonType.PERSON_B
            }
            
            val existingCourses = if (targetPersonType == PersonType.PERSON_A) {
                existingCoursesA
            } else {
                existingCoursesB
            }
            
            val hasConflict = existingCourses.any { existing ->
                importData.hasConflictWith(existing)
            }
            
            val conflictReason = if (hasConflict) {
                val conflictCourse = existingCourses.first { existing ->
                    importData.hasConflictWith(existing)
                }
                "与${conflictCourse.name}时间冲突"
            } else ""
            
            ImportPreviewItem(
                data = importData,
                isSelected = true,
                hasConflict = hasConflict,
                conflictReason = conflictReason
            )
        }
        
        previewItems.addAll(items)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(if (isAppExport) "导入预览（应用导出）" else "导入预览（模板）") 
                },
                navigationIcon = {
                    GlassSymbolIconButton(
                        onClick = onDismiss,
                        style = GlassSymbolButtonStyle.NonTinted,
                        size = ComponentSize.LiquidGlassButton.TopAppBarIconButtonSize,
                        contentPadding = PaddingValues(start = Spacing.sm)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回", tint = getLabelsVibrantPrimary())
                    }
                },
                actions = {
                    LiquidGlassButton(
                        onClick = { 
                            if (isAppExport) {
                                showTargetDialog = true
                            } else {
                                showTargetDialog = true
                            }
                        },
                        text = "确认导入",
                        enabled = selectedCount > 0,
                        style = LiquidGlassButtonStyle.Tinted
                    )
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
                .padding(paddingValues)
        ) {
            if (isAppExport) {
                AppExportPreviewContent(
                    personAName = actualImportData.personAName,
                    personBName = actualImportData.personBName,
                    settingsA = actualImportData.settingsA,
                    settingsB = actualImportData.settingsB,
                    coursesForPersonA = coursesForPersonA,
                    coursesForPersonB = coursesForPersonB,
                    importSettings = importSettings,
                    onImportSettingsChange = { importSettings = it },
                    previewItems = previewItems,
                    totalCount = totalCount,
                    selectedCount = selectedCount,
                    conflictCount = conflictCount
                )
            } else {
                TemplatePreviewContent(
                    previewItems = previewItems,
                    totalCount = totalCount,
                    selectedCount = selectedCount,
                    conflictCount = conflictCount
                )
            }
        }
    }

    if (showTargetDialog) {
        if (isAppExport) {
            AppExportConfirmDialog(
                personAName = actualImportData.personAName,
                personBName = actualImportData.personBName,
                coursesForPersonA = coursesForPersonA,
                coursesForPersonB = coursesForPersonB,
                importSettings = importSettings,
                mergeMode = mergeMode,
                isImporting = isImporting,
                personATarget = personATarget,
                personBTarget = personBTarget,
                onImportSettingsChange = { importSettings = it },
                onMergeModeChange = { mergeMode = it },
                onPersonATargetChange = { personATarget = it },
                onPersonBTargetChange = { personBTarget = it },
                onConfirm = {
                    scope.launch {
                        isImporting = true
                        
                        val selectedCourses = previewItems
                            .filter { it.isSelected }
                            .map { item ->
                                val targetPerson = when (item.data.personType) {
                                    PersonType.PERSON_A -> personATarget ?: PersonType.PERSON_A
                                    PersonType.PERSON_B -> personBTarget ?: PersonType.PERSON_B
                                }
                                item.data.copy(personType = targetPerson)
                            }
                        
                        val result = viewModel.saveImportedCoursesWithSettings(
                            courses = selectedCourses,
                            mergeMode = mergeMode,
                            importSettings = importSettings,
                            settingsA = actualImportData.settingsA,
                            settingsB = actualImportData.settingsB,
                            personAName = actualImportData.personAName,
                            personBName = actualImportData.personBName
                        )
                        
                        isImporting = false
                        
                        if (result.success) {
                            importedCount = result.importedCount
                            importedTarget = PersonType.PERSON_B
                            showTargetDialog = false
                            showSuccessDialog = true
                            onConfirm(selectedCourses, PersonType.PERSON_A, mergeMode)
                        }
                    }
                },
                onDismiss = { showTargetDialog = false }
            )
        } else {
            TemplateConfirmDialog(
                selectedTarget = selectedTarget,
                mergeMode = mergeMode,
                isImporting = isImporting,
                onTargetChange = { selectedTarget = it },
                onMergeModeChange = { mergeMode = it },
                onConfirm = {
                    selectedTarget?.let { target ->
                        scope.launch {
                            isImporting = true
                            val selectedCourses = previewItems
                                .filter { it.isSelected }
                                .map { it.data.copy(personType = target) }
                            
                            val result = viewModel.saveImportedCourses(selectedCourses, target, mergeMode)
                            isImporting = false
                            
                            if (result.success) {
                                importedTarget = target
                                importedCount = result.importedCount
                                showTargetDialog = false
                                showSuccessDialog = true
                                onConfirm(selectedCourses, target, mergeMode)
                            }
                        }
                    }
                },
                onDismiss = { showTargetDialog = false }
            )
        }
    }
    
    if (showSuccessDialog) {
        SuccessDialog(
            importedCount = importedCount,
            importedTarget = importedTarget,
            isAppExport = isAppExport,
            onViewSchedule = {
                showSuccessDialog = false
                importedTarget?.let { onImportSuccess?.invoke(it) }
            },
            onNavigateToHome = {
                showSuccessDialog = false
                onNavigateToHome?.invoke() ?: onDismiss()
            }
        )
    }
}

@Composable
private fun AppExportPreviewContent(
    personAName: String?,
    personBName: String?,
    settingsA: ScheduleSettingsExport?,
    settingsB: ScheduleSettingsExport?,
    coursesForPersonA: List<ImportPreviewItem>,
    coursesForPersonB: List<ImportPreviewItem>,
    importSettings: Boolean,
    onImportSettingsChange: (Boolean) -> Unit,
    previewItems: MutableList<ImportPreviewItem>,
    totalCount: Int,
    selectedCount: Int,
    conflictCount: Int,
    backdrop: Backdrop = LocalBackdrop.current ?: emptyBackdrop()
) {
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

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
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
                            drawRect(BrandColors.Primary, blendMode = BlendMode.Hue)
                            drawRect(BrandColors.Primary.copy(alpha = 0.08f))
                        }
                    )
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = BrandColors.Primary
                    )
                    Text(
                        "检测到应用导出文件，导入时请确认课表分配",
                        style = MaterialTheme.typography.bodyMedium,
                        color = BrandColors.Primary
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("同时导入设置信息", style = MaterialTheme.typography.bodyMedium, color = labelsPrimary)
                    LiquidToggle(
                        checked = importSettings,
                        onCheckedChange = onImportSettingsChange,
                        backdrop = backdrop
                    )
                }
            }
        }
        
        if (importSettings && (settingsA != null || settingsB != null)) {
            item {
                SettingsPreviewCard(
                    personAName = personAName,
                    personBName = personBName,
                    settingsA = settingsA,
                    settingsB = settingsB
                )
            }
        }
        
        if (coursesForPersonA.isNotEmpty()) {
            item {
                Text(
                    text = "${personAName ?: "Ta"}的课程 (${coursesForPersonA.size}门)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            items(coursesForPersonA) { item ->
                ImportPreviewItemCard(
                    item = item,
                    onToggleSelection = {
                        val index = previewItems.indexOf(item)
                        if (index >= 0) {
                            previewItems[index] = item.copy(isSelected = !item.isSelected)
                        }
                    }
                )
            }
        }
        
        if (coursesForPersonB.isNotEmpty()) {
            item {
                Text(
                    text = "${personBName ?: "我"}的课程 (${coursesForPersonB.size}门)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            items(coursesForPersonB) { item ->
                ImportPreviewItemCard(
                    item = item,
                    onToggleSelection = {
                        val index = previewItems.indexOf(item)
                        if (index >= 0) {
                            previewItems[index] = item.copy(isSelected = !item.isSelected)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SettingsPreviewCard(
    personAName: String?,
    personBName: String?,
    settingsA: ScheduleSettingsExport?,
    settingsB: ScheduleSettingsExport?,
    backdrop: Backdrop = LocalBackdrop.current ?: emptyBackdrop()
) {
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
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
                    drawRect(layer1Tint.copy(alpha = layer1Alpha))
                    drawRect(layer2Base, blendMode = BlendMode.ColorDodge)
                    drawRect(glassEffect)
                }
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "将要导入的设置",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = labelsPrimary
        )
        
        if (settingsA != null) {
            SettingsItemPreview(
                name = personAName ?: "Ta",
                settings = settingsA
            )
        }
        
        if (settingsB != null) {
            SettingsItemPreview(
                name = personBName ?: "我",
                settings = settingsB
            )
        }
    }
}

@Composable
private fun SettingsItemPreview(
    name: String,
    settings: ScheduleSettingsExport
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            "$name 的设置",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )
        
        val semesterStart = try {
            LocalDate.ofEpochDay(settings.semesterStartDate).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        } catch (e: Exception) {
            "未知"
        }
        
        Text(
            "开学时间: $semesterStart | 总周数: ${settings.totalWeeks}周 | 当前: 第${settings.currentWeek}周",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TemplatePreviewContent(
    previewItems: MutableList<ImportPreviewItem>,
    totalCount: Int,
    selectedCount: Int,
    conflictCount: Int,
    backdrop: Backdrop = LocalBackdrop.current ?: emptyBackdrop()
) {
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

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
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
                        drawRect(layer1Tint.copy(alpha = layer1Alpha))
                        drawRect(layer2Base, blendMode = BlendMode.ColorDodge)
                        drawRect(glassEffect)
                    }
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("共 $totalCount 门课程", color = labelsPrimary)
                Text(
                    "已选择 $selectedCount 门",
                    color = BrandColors.Primary
                )
            }
            
            if (conflictCount > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        "$conflictCount 门课程存在时间冲突",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LiquidGlassButton(
                    text = "全选",
                    onClick = { 
                        previewItems.forEachIndexed { index, _ ->
                            previewItems[index] = previewItems[index].copy(isSelected = true)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    style = LiquidGlassButtonStyle.NonTinted
                )
                LiquidGlassButton(
                    text = "取消全选",
                    onClick = { 
                        previewItems.forEachIndexed { index, _ ->
                            previewItems[index] = previewItems[index].copy(isSelected = false)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    style = LiquidGlassButtonStyle.NonTinted
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(previewItems) { item ->
                ImportPreviewItemCard(
                    item = item,
                    backdrop = backdrop,
                    onToggleSelection = {
                        val index = previewItems.indexOf(item)
                        if (index >= 0) {
                            previewItems[index] = item.copy(isSelected = !item.isSelected)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun AppExportConfirmDialog(
    personAName: String?,
    personBName: String?,
    coursesForPersonA: List<ImportPreviewItem>,
    coursesForPersonB: List<ImportPreviewItem>,
    importSettings: Boolean,
    mergeMode: Boolean,
    isImporting: Boolean,
    personATarget: PersonType?,
    personBTarget: PersonType?,
    onImportSettingsChange: (Boolean) -> Unit,
    onMergeModeChange: (Boolean) -> Unit,
    onPersonATargetChange: (PersonType) -> Unit,
    onPersonBTargetChange: (PersonType) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()
    val darkTheme = LocalDarkTheme.current
    
    val mergeOptions = listOf(
        SegmentOption(true, "合并（保留现有）"),
        SegmentOption(false, "覆盖现有数据")
    )
    
    val targetOptions = listOf(
        SegmentOption(PersonType.PERSON_A, "Ta的课表"),
        SegmentOption(PersonType.PERSON_B, "我的课表")
    )
    
    val isAssignmentValid = personATarget != null && personBTarget != null && personATarget != personBTarget

    IOSConfirmDialog(
        title = "确认导入",
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        confirmText = if (isImporting) "导入中..." else "确认导入",
        confirmEnabled = !isImporting && isAssignmentValid,
        dismissText = "取消"
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            Text(
                text = "请确认课表分配：",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = labelsPrimary
            )
            
            if (coursesForPersonA.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${personAName ?: "Ta"}的课表",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF2196F3),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${coursesForPersonA.count { it.isSelected }}门课程",
                            style = MaterialTheme.typography.bodySmall,
                            color = labelsSecondary
                        )
                    }
                    SegmentedControl(
                        options = targetOptions,
                        selectedOption = personATarget ?: PersonType.PERSON_A,
                        onOptionSelected = onPersonATargetChange,
                        modifier = Modifier.width(180.dp)
                    )
                }
            }
            
            if (coursesForPersonB.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${personBName ?: "我"}的课表",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFFFC107),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${coursesForPersonB.count { it.isSelected }}门课程",
                            style = MaterialTheme.typography.bodySmall,
                            color = labelsSecondary
                        )
                    }
                    SegmentedControl(
                        options = targetOptions,
                        selectedOption = personBTarget ?: PersonType.PERSON_B,
                        onOptionSelected = onPersonBTargetChange,
                        modifier = Modifier.width(180.dp)
                    )
                }
            }
            
            if (personATarget != null && personBTarget != null && personATarget == personBTarget) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "两份课表不能分配给同一个人",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(Spacing.xs))
            
            if (importSettings) {
                Text(
                    text = "• 同时导入设置信息",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BrandColors.Primary
                )
            }
            
            Spacer(modifier = Modifier.height(Spacing.sm))
            
            Text(
                text = "冲突处理方式：",
                style = MaterialTheme.typography.bodyMedium,
                color = labelsPrimary
            )
            
            SegmentedControl(
                options = mergeOptions,
                selectedOption = mergeMode,
                onOptionSelected = onMergeModeChange,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun TemplateConfirmDialog(
    selectedTarget: PersonType?,
    mergeMode: Boolean,
    isImporting: Boolean,
    onTargetChange: (PersonType) -> Unit,
    onMergeModeChange: (Boolean) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()
    val darkTheme = LocalDarkTheme.current
    
    val targetOptions = listOf(
        SegmentOption(PersonType.PERSON_A, "Ta的课表"),
        SegmentOption(PersonType.PERSON_B, "我的课表")
    )
    
    val mergeOptions = listOf(
        SegmentOption(true, "合并（保留现有）"),
        SegmentOption(false, "覆盖现有数据")
    )

    IOSConfirmDialog(
        title = "选择导入目标",
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        confirmText = if (isImporting) "导入中..." else "确认导入",
        confirmEnabled = selectedTarget != null && !isImporting,
        dismissText = "取消"
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Text(
                text = "请选择要将课程导入到哪个课表：",
                style = MaterialTheme.typography.bodyMedium,
                color = labelsPrimary
            )
            
            SegmentedControl(
                options = targetOptions,
                selectedOption = selectedTarget ?: PersonType.PERSON_B,
                onOptionSelected = onTargetChange,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(Spacing.xs))
            
            Text(
                text = "冲突处理方式：",
                style = MaterialTheme.typography.bodyMedium,
                color = labelsPrimary
            )
            
            SegmentedControl(
                options = mergeOptions,
                selectedOption = mergeMode,
                onOptionSelected = onMergeModeChange,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SuccessDialog(
    importedCount: Int,
    importedTarget: PersonType?,
    isAppExport: Boolean,
    onViewSchedule: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val message = if (isAppExport) {
        "成功导入 $importedCount 门课程"
    } else {
        "成功导入 $importedCount 门课程到${if (importedTarget == PersonType.PERSON_A) "Ta的课表" else "我的课表"}"
    }
    
    IOSSuccessDialog(
        title = "导入成功",
        message = message,
        onDismiss = onNavigateToHome,
        onAction = onViewSchedule,
        actionText = "查看课表",
        dismissText = "返回主页"
    )
}

@Composable
private fun ImportPreviewItemCard(
    item: ImportPreviewItem,
    onToggleSelection: () -> Unit,
    backdrop: Backdrop = LocalBackdrop.current ?: emptyBackdrop()
) {
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

    val errorColor = MaterialTheme.colorScheme.error

    Row(
        modifier = Modifier
            .fillMaxWidth()
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
                    if (item.hasConflict) {
                        drawRect(errorColor, blendMode = BlendMode.Hue)
                        drawRect(errorColor.copy(alpha = 0.15f))
                    } else {
                        drawRect(layer1Tint.copy(alpha = layer1Alpha))
                        drawRect(layer2Base, blendMode = BlendMode.ColorDodge)
                        drawRect(glassEffect)
                    }
                }
            )
            .toggleable(
                value = item.isSelected,
                onValueChange = { onToggleSelection() }
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = item.isSelected,
            onCheckedChange = { onToggleSelection() }
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.data.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = labelsPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                if (item.hasConflict) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "冲突",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = getDayOfWeekString(item.data.dayOfWeek),
                    style = MaterialTheme.typography.bodyMedium,
                    color = labelsSecondary
                )
                Text(
                    text = getPeriodDisplayText(item.data.startPeriod, item.data.endPeriod),
                    style = MaterialTheme.typography.bodyMedium,
                    color = labelsSecondary
                )
            }
            
            if (item.data.location.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = labelsSecondary
                    )
                    Text(
                        text = item.data.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = labelsSecondary
                    )
                }
            }
            
            val weekText = when (item.data.weekType) {
                com.duoschedule.data.model.WeekType.ALL -> "${item.data.startWeek}-${item.data.endWeek}周"
                com.duoschedule.data.model.WeekType.ODD -> "单周"
                com.duoschedule.data.model.WeekType.EVEN -> "双周"
                com.duoschedule.data.model.WeekType.CUSTOM -> item.data.customWeeks.ifEmpty { "自定义周次" }
            }
            Text(
                text = weekText,
                style = MaterialTheme.typography.bodySmall,
                color = labelsSecondary
            )
            
            if (item.hasConflict && item.conflictReason.isNotEmpty()) {
                Text(
                    text = item.conflictReason,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

private fun getDayOfWeekString(dayOfWeek: Int): String {
    return when (dayOfWeek) {
        1 -> "周一"
        2 -> "周二"
        3 -> "周三"
        4 -> "周四"
        5 -> "周五"
        6 -> "周六"
        7 -> "周日"
        else -> "未知"
    }
}

private fun getPeriodDisplayText(startPeriod: Int, endPeriod: Int): String {
    return if (startPeriod == endPeriod) {
        "第${startPeriod}节"
    } else {
        "第${startPeriod}-${endPeriod}节"
    }
}
