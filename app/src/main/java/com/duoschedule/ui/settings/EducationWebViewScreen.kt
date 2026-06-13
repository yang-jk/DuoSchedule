package com.duoschedule.ui.settings

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.duoschedule.data.importexport.ImportPreviewData
import com.duoschedule.data.importexport.SchoolInfo
import com.duoschedule.data.importexport.SupportedSchools
import com.duoschedule.data.model.AppThemeMode
import com.duoschedule.data.model.PersonType
import com.duoschedule.ui.settings.components.*
import com.duoschedule.ui.theme.*
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.theme.MiuixTheme

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EducationWebViewScreen(
    schoolId: String,
    onNavigateBack: () -> Unit,
    onNavigateToImportPreview: (ImportPreviewData) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val appThemeMode = LocalAppThemeMode.current
    val labelsPrimary = getLabelsVibrantPrimary()

    val adapter = remember(schoolId) {
        SupportedSchools.register(com.duoschedule.data.importexport.ZhengfangSchoolAdapter())
        SupportedSchools.register(com.duoschedule.data.importexport.QiangzhiSchoolAdapter())
        SupportedSchools.getAdapter(schoolId)
    }
    val schoolInfo = adapter?.schoolInfo

    var webView by remember { mutableStateOf<WebView?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var currentUrl by remember { mutableStateOf(schoolInfo?.loginUrl ?: "") }
    var isFetchingSchedule by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var canFetchSchedule by remember { mutableStateOf(false) }

    // URL text field state - lifted out of webViewContent lambda
    var urlTextFieldValue by remember { mutableStateOf(schoolInfo?.loginUrl ?: "") }

    // Sync text field when WebView navigates
    LaunchedEffect(currentUrl) {
        urlTextFieldValue = currentUrl
    }

    // Check if current URL matches schedule page
    fun isScheduleUrl(url: String): Boolean {
        val schedulePath = schoolInfo?.scheduleUrlPath ?: return false
        return url.contains(schedulePath, ignoreCase = true)
    }

    // Extract HTML from WebView and parse schedule
    fun fetchScheduleFromWebView(wv: WebView) {
        isFetchingSchedule = true
        errorMessage = ""
        wv.evaluateJavascript("document.documentElement.outerHTML") { html ->
            if (html != null) {
                val cleanHtml = html
                    .removeSurrounding("\"")
                    .replace("\\u003C", "<")
                    .replace("\\u003E", ">")
                    .replace("\\/", "/")
                    .replace("\\n", "\n")
                    .replace("\\t", "\t")
                    .replace("\\\"", "\"")
                    .replace("&amp;", "&")
                    .replace("&lt;", "<")
                    .replace("&gt;", ">")

                scope.launch {
                    val courses = adapter?.parseScheduleHtml(cleanHtml, PersonType.PERSON_A) ?: emptyList()
                    isFetchingSchedule = false

                    if (courses.isNotEmpty()) {
                        onNavigateToImportPreview(ImportPreviewData(courses = courses))
                    } else {
                        errorMessage = "未检测到课表数据，请确保已打开课表页面"
                    }
                }
            } else {
                isFetchingSchedule = false
                errorMessage = "获取页面内容失败"
            }
        }
    }

    if (schoolInfo == null) {
        // School not found
        if (appThemeMode == AppThemeMode.MIUIX) {
            Scaffold(
                topBar = {
                    SmallTopAppBar(
                        title = "从教务系统导入",
                        scrollBehavior = MiuixScrollBehavior(),
                        color = Color.Transparent,
                        titleColor = MiuixTheme.colorScheme.onSurface,
                        defaultWindowInsetsPadding = false,
                        navigationIcon = {
                            top.yukonga.miuix.kmp.basic.IconButton(onClick = onNavigateBack) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = "返回", tint = MiuixTheme.colorScheme.onSurface)
                            }
                        }
                    )
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    top.yukonga.miuix.kmp.basic.Text("未找到学校信息", color = MiuixTheme.colorScheme.onSurface)
                }
            }
        } else {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("从教务系统导入") },
                        navigationIcon = {
                            GlassSymbolIconButton(
                                onClick = onNavigateBack,
                                style = GlassSymbolButtonStyle.NonTinted,
                                buttonSize = ComponentSize.LiquidGlassButton.TopAppBarIconButtonSize,
                                contentPadding = PaddingValues(start = Spacing.sm)
                            ) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = "返回", tint = labelsPrimary)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                    )
                },
                containerColor = MaterialTheme.colorScheme.background
            ) { paddingValues ->
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("未找到学校信息")
                }
            }
        }
        return
    }

    // Address bar composable - defined at top level, not inside a lambda
    val addressBar: @Composable () -> Unit = {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (appThemeMode == AppThemeMode.MIUIX) {
                top.yukonga.miuix.kmp.basic.TextField(
                    value = urlTextFieldValue,
                    onValueChange = { urlTextFieldValue = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    label = "输入网址",
                    useLabelAsPlaceholder = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(
                        onGo = {
                            var url = urlTextFieldValue.trim()
                            if (url.isNotEmpty()) {
                                if (!url.contains("://")) {
                                    url = "https://$url"
                                }
                                webView?.loadUrl(url)
                                currentUrl = url
                            }
                        }
                    )
                )
            } else {
                OutlinedTextField(
                    value = urlTextFieldValue,
                    onValueChange = { urlTextFieldValue = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    placeholder = {
                        Text(
                            "输入网址",
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    textStyle = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(
                        onGo = {
                            var url = urlTextFieldValue.trim()
                            if (url.isNotEmpty()) {
                                if (!url.contains("://")) {
                                    url = "https://$url"
                                }
                                webView?.loadUrl(url)
                                currentUrl = url
                            }
                        }
                    )
                )
            }

            // Refresh button
            if (appThemeMode == AppThemeMode.MIUIX) {
                top.yukonga.miuix.kmp.basic.IconButton(onClick = { webView?.reload() }) {
                    if (isLoading) {
                        top.yukonga.miuix.kmp.basic.CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "刷新",
                            tint = MiuixTheme.colorScheme.onSurface
                        )
                    }
                }
            } else {
                GlassSymbolIconButton(onClick = { webView?.reload() }, style = GlassSymbolButtonStyle.NonTinted) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "刷新",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // WebView + bottom bar composable - needs ColumnScope for weight
    val webViewAndBottomBar: @Composable ColumnScope.() -> Unit = {
        Box(modifier = Modifier.weight(1f)) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.allowFileAccess = false
                        settings.allowContentAccess = false
                        settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                                val url = request.url.toString()
                                currentUrl = url
                                canFetchSchedule = isScheduleUrl(url)

                                // Auto-detect schedule page and fetch
                                if (isScheduleUrl(url)) {
                                    view.postDelayed({
                                        fetchScheduleFromWebView(view)
                                    }, 1500)
                                }
                                return false
                            }

                            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                                isLoading = true
                                currentUrl = url
                                canFetchSchedule = isScheduleUrl(url)
                            }

                            override fun onPageFinished(view: WebView, url: String) {
                                isLoading = false
                                currentUrl = url
                                canFetchSchedule = isScheduleUrl(url)
                            }

                            override fun onReceivedError(view: WebView?, request: android.webkit.WebResourceRequest?, error: android.webkit.WebResourceError?) {
                                super.onReceivedError(view, request, error)
                                if (request?.isForMainFrame == true) {
                                    isLoading = false
                                }
                            }
                        }

                        webChromeClient = WebChromeClient()

                        loadUrl(schoolInfo.loginUrl)
                        webView = this
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Loading indicator
            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                )
            }
        }

        // Bottom bar with fetch button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            if (errorMessage.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (isFetchingSchedule) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Text("正在解析课表...", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (canFetchSchedule) "已检测到课表页面" else "请在网页中登录并打开课表页面",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (canFetchSchedule) BrandColors.Primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                webView?.let { fetchScheduleFromWebView(it) }
                            },
                            enabled = canFetchSchedule && !isFetchingSchedule
                        ) {
                            Text("获取课表")
                        }
                    }
                }
            }
        }
    }

    if (appThemeMode == AppThemeMode.MIUIX) {
        Scaffold(
            topBar = {
                Column {
                    SmallTopAppBar(
                        title = schoolInfo.name,
                        scrollBehavior = MiuixScrollBehavior(),
                        color = Color.Transparent,
                        titleColor = MiuixTheme.colorScheme.onSurface,
                        defaultWindowInsetsPadding = false,
                        navigationIcon = {
                            top.yukonga.miuix.kmp.basic.IconButton(onClick = onNavigateBack) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = "返回", tint = MiuixTheme.colorScheme.onSurface)
                            }
                        }
                    )
                    addressBar()
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding())
            ) {
                webViewAndBottomBar()
            }
        }
    } else {
        Scaffold(
            contentWindowInsets = WindowInsets(0),
            topBar = {
                Column {
                    TopAppBar(
                        title = { Text(schoolInfo.name) },
                        navigationIcon = {
                            GlassSymbolIconButton(
                                onClick = onNavigateBack,
                                style = GlassSymbolButtonStyle.NonTinted,
                                buttonSize = ComponentSize.LiquidGlassButton.TopAppBarIconButtonSize,
                                contentPadding = PaddingValues(start = Spacing.sm)
                            ) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = "返回", tint = labelsPrimary)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                    )
                    addressBar()
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding())
            ) {
                webViewAndBottomBar()
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            webView?.destroy()
        }
    }
}
