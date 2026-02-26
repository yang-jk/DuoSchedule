package com.duoschedule.ui.settings

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.duoschedule.data.importexport.CourseImportData
import com.duoschedule.data.importexport.EducationSystemCredentials
import com.duoschedule.data.importexport.ImportResult
import com.duoschedule.data.model.PersonType
import com.duoschedule.ui.theme.getDialogBackgroundColor
import com.duoschedule.util.TemplateUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataManagementScreen(
    onNavigateBack: () -> Unit,
    onNavigateToImportPreview: (List<CourseImportData>) -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showEducationSystemDialog by remember { mutableStateOf(false) }
    var importResult by remember { mutableStateOf<ImportResult?>(null) }
    var showResultDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var loadingMessage by remember { mutableStateOf("") }
    
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let {
            scope.launch {
                isLoading = true
                loadingMessage = "正在导出..."
                val result = viewModel.exportToCsv(context, uri)
                isLoading = false
                if (result.isSuccess) {
                    importResult = ImportResult(success = true, errors = listOf("导出成功！"))
                } else {
                    importResult = ImportResult(
                        success = false, 
                        errors = listOf("导出失败: ${result.exceptionOrNull()?.message}")
                    )
                }
                showResultDialog = true
            }
        }
    }
    
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            try {
                context.contentResolver.takePersistableUriPermission(it, takeFlags)
            } catch (e: SecurityException) {
                // 某些设备可能不支持持久化权限，忽略此错误
            }
            scope.launch {
                isLoading = true
                loadingMessage = "正在解析文件..."
                val result = viewModel.importFromCsv(context, uri)
                isLoading = false
                importResult = result
                if (result.success && result.courses.isNotEmpty()) {
                    onNavigateToImportPreview(result.courses)
                } else {
                    showResultDialog = true
                }
            }
        }
    }
    
    val wakeupImportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            try {
                context.contentResolver.takePersistableUriPermission(it, takeFlags)
            } catch (e: SecurityException) {
                // 某些设备可能不支持持久化权限，忽略此错误
            }
            scope.launch {
                isLoading = true
                loadingMessage = "正在解析Wakeup文件..."
                val result = viewModel.importFromWakeup(context, uri, PersonType.PERSON_B)
                isLoading = false
                importResult = result
                if (result.success && result.courses.isNotEmpty()) {
                    onNavigateToImportPreview(result.courses)
                } else {
                    showResultDialog = true
                }
            }
        }
    }
    
    val templateSaveLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let {
            scope.launch {
                isLoading = true
                loadingMessage = "正在保存模板..."
                val result = TemplateUtils.saveTemplateToUri(context, uri)
                isLoading = false
                if (result.isSuccess) {
                    Toast.makeText(context, "模板已保存", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "保存失败: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("数据管理") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
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
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 数据导出卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "数据导出",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    OutlinedButton(
                        onClick = { showExportDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("导出课表数据")
                    }
                }
            }

            // 数据导入卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "数据导入",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    OutlinedButton(
                        onClick = { 
                            importLauncher.launch(arrayOf("*/*"))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("从CSV文件导入")
                    }
                    
                    OutlinedButton(
                        onClick = { 
                            wakeupImportLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("从Wakeup课程表导入")
                    }
                    
                    OutlinedButton(
                        onClick = { showEducationSystemDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(Icons.Default.School, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("从教务系统导入")
                    }
                }
            }

            // 模板下载卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "CSV 导入模板",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Text(
                        text = "下载模板文件，按格式填写后导入课表数据。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { 
                                templateSaveLauncher.launch("课程表导入模板.csv")
                            },
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("保存到本地")
                        }
                        
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    isLoading = true
                                    loadingMessage = "正在准备分享..."
                                    try {
                                        val content = TemplateUtils.getTemplateContent(context)
                                        val file = java.io.File(context.cacheDir, "课程表导入模板.csv")
                                        java.io.FileOutputStream(file).use { fos ->
                                            fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
                                            fos.write(content.toByteArray(Charsets.UTF_8))
                                        }
                                        val uri = androidx.core.content.FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            file
                                        )
                                        val shareIntent = TemplateUtils.createShareIntent(context, uri)
                                        context.startActivity(shareIntent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "分享失败: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                    isLoading = false
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("分享")
                        }
                    }
                }
            }

            // 数据备份卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "数据备份",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Text(
                        text = "导入前会自动备份当前数据，如导入失败可恢复。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }

    if (showExportDialog) {
        ExportScopeDialog(
            onDismiss = { showExportDialog = false },
            onConfirm = { exportScope ->
                showExportDialog = false
                val fileName = "双人课程表_${java.time.LocalDate.now()}.csv"
                try {
                    exportLauncher.launch(fileName)
                } catch (e: Exception) {
                    Toast.makeText(context, "打开文件选择器失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    if (showEducationSystemDialog) {
        EducationSystemImportDialog(
            onDismiss = { showEducationSystemDialog = false },
            onImport = { credentials, targetPerson ->
                showEducationSystemDialog = false
                scope.launch {
                    isLoading = true
                    loadingMessage = "正在连接教务系统..."
                    val result = viewModel.importFromEducationSystem(credentials, targetPerson)
                    isLoading = false
                    when {
                        result.needCaptcha -> {
                            importResult = ImportResult(
                                success = false,
                                errors = listOf("需要输入验证码，请稍后重试")
                            )
                            showResultDialog = true
                        }
                        result.success && result.courses.isNotEmpty() -> {
                            onNavigateToImportPreview(result.courses)
                        }
                        else -> {
                            importResult = ImportResult(
                                success = false,
                                errors = result.errors.ifEmpty { listOf("导入失败") }
                            )
                            showResultDialog = true
                        }
                    }
                }
            }
        )
    }

    if (showResultDialog && importResult != null) {
        AlertDialog(
            onDismissRequest = { 
                showResultDialog = false
                importResult = null
            },
            containerColor = getDialogBackgroundColor(),
            title = { 
                Text(if (importResult!!.success) "导入结果" else "导入失败") 
            },
            text = {
                Column {
                    if (importResult!!.success && importResult!!.importedCount > 0) {
                        Text("成功解析 ${importResult!!.importedCount} 门课程")
                        if (importResult!!.failedCount > 0) {
                            Text("失败 ${importResult!!.failedCount} 门")
                        }
                    }
                    importResult!!.errors.forEach { error ->
                        Text(
                            text = error,
                            color = if (importResult!!.success) 
                                MaterialTheme.colorScheme.onSurface 
                            else 
                                MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { 
                    showResultDialog = false
                    importResult = null
                }) {
                    Text("确定")
                }
            }
        )
    }

    if (isLoading) {
        Dialog(onDismissRequest = { }) {
            Card(
                modifier = Modifier.padding(16.dp),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = getDialogBackgroundColor()
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text(loadingMessage)
                }
            }
        }
    }
}

@Composable
private fun ExportScopeDialog(
    onDismiss: () -> Unit,
    onConfirm: (com.duoschedule.data.importexport.ExportScope) -> Unit
) {
    var selectedScope by remember {
        mutableStateOf(com.duoschedule.data.importexport.ExportScope.ALL_WITH_SETTINGS)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = getDialogBackgroundColor(),
        shape = MaterialTheme.shapes.large,
        title = { Text("选择导出范围") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    com.duoschedule.data.importexport.ExportScope.PERSON_A_ONLY to "仅Ta的课表",
                    com.duoschedule.data.importexport.ExportScope.PERSON_B_ONLY to "仅我的课表",
                    com.duoschedule.data.importexport.ExportScope.BOTH to "双人课表",
                    com.duoschedule.data.importexport.ExportScope.ALL_WITH_SETTINGS to "全部数据（含设置）"
                ).forEach { (scope, label) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedScope == scope,
                            onClick = { selectedScope = scope }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(label)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedScope) }) {
                Text("导出")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EducationSystemImportDialog(
    onDismiss: () -> Unit,
    onImport: (EducationSystemCredentials, PersonType) -> Unit
) {
    var baseUrl by remember { mutableStateOf("") }
    var studentId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var captcha by remember { mutableStateOf("") }
    var selectedPerson by remember { mutableStateOf(PersonType.PERSON_B) }
    var showPassword by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = getDialogBackgroundColor(),
        shape = MaterialTheme.shapes.large,
        title = { Text("从教务系统导入") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = baseUrl,
                    onValueChange = { baseUrl = it },
                    label = { Text("教务系统网址") },
                    placeholder = { Text("如: http://jwxt.example.edu.cn") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )
                
                OutlinedTextField(
                    value = studentId,
                    onValueChange = { studentId = it },
                    label = { Text("学号") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )
                
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("密码") },
                    singleLine = true,
                    visualTransformation = if (showPassword) 
                        androidx.compose.ui.text.input.VisualTransformation.None 
                    else 
                        androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )
                
                OutlinedTextField(
                    value = captcha,
                    onValueChange = { captcha = it },
                    label = { Text("验证码（如有）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )
                
                Divider()
                
                Text("导入到：", style = MaterialTheme.typography.bodyMedium)
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedPerson == PersonType.PERSON_A,
                        onClick = { selectedPerson = PersonType.PERSON_A },
                        label = { Text("Ta的课表") }
                    )
                    FilterChip(
                        selected = selectedPerson == PersonType.PERSON_B,
                        onClick = { selectedPerson = PersonType.PERSON_B },
                        label = { Text("我的课表") }
                    )
                }
                
                Text(
                    text = "提示：账号密码仅用于获取课表，不会被保存。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onImport(
                        EducationSystemCredentials(
                            baseUrl = baseUrl,
                            studentId = studentId,
                            password = password,
                            captcha = captcha
                        ),
                        selectedPerson
                    )
                },
                enabled = baseUrl.isNotBlank() && studentId.isNotBlank() && password.isNotBlank()
            ) {
                Text("导入")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
