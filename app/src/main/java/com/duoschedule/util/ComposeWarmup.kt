package com.duoschedule.util

import android.content.Context
import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.kyant.capsule.ContinuousRoundedRectangle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.duoschedule.ui.main.MainViewModel
import com.duoschedule.ui.schedule.ScheduleViewModel
import com.duoschedule.ui.settings.SettingsViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

object ComposeWarmup {
    private const val TAG = "ComposeWarmup"
    private var isWarmedUp = false
    private var isWarmingUp = false

    fun warmup(context: Context, scope: CoroutineScope) {
        if (isWarmedUp || isWarmingUp) return
        isWarmingUp = true
        
        scope.launch(Dispatchers.Default) {
            try {
                PerformanceMonitor.startTrace("compose_warmup")
                Log.i(TAG, "Starting Compose warmup...")
                
                delay(500)
                
                warmupComposeRuntime(context)
                
                isWarmedUp = true
                PerformanceMonitor.endTrace("compose_warmup")
                Log.i(TAG, "Compose warmup completed")
            } catch (e: Exception) {
                Log.e(TAG, "Compose warmup failed", e)
            } finally {
                isWarmingUp = false
            }
        }
    }

    private suspend fun warmupComposeRuntime(context: Context) {
        withContext(Dispatchers.Main) {
            try {
                val composeView = ComposeView(context)
                
                composeView.setContent {
                    WarmupContent()
                }
                
                delay(500)
                
                composeView.disposeComposition()
            } catch (e: Exception) {
                Log.w(TAG, "Warmup composition failed: ${e.message}")
            }
        }
    }
}

@Composable
private fun WarmupContent() {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val layoutDirection = LayoutDirection.Ltr
    
    CompositionLocalProvider(
        LocalLayoutDirection provides layoutDirection,
        LocalInspectionMode provides false
    ) {
        MaterialTheme {
            WarmupCommonComponents()
            WarmupTextComponents()
            WarmupStateComponents()
            WarmupAnimationComponents()
            WarmupLazyColumnComponents()
            WarmupCardComponents()
            WarmupBrushComponents()
            WarmupNavigationComponents()
            WarmupInteractionComponents()
            WarmupScheduleComponents()
            WarmupCourseComponents()
            WarmupScreenComponents()
        }
    }
}

@Composable
private fun WarmupCommonComponents() {
    Column(
        modifier = Modifier
            .size(1.dp)
            .padding(1.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        Row(
            modifier = Modifier.size(1.dp),
            horizontalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Spacer(modifier = Modifier.size(1.dp))
            Box(modifier = Modifier.size(1.dp))
        }
        
        repeat(3) { index ->
            Text(
                text = "Warmup $index",
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun WarmupTextComponents() {
    var state by remember { mutableStateOf("initial") }
    
    Column {
        Text(
            text = state,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "Title",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        
        Text(
            text = "Body text for warmup",
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        
        Text(
            text = "Label",
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            letterSpacing = (-0.1).sp
        )
        
        Text(
            text = "Centered",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "课程名称",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2
        )
    }
}

@Composable
private fun WarmupStateComponents() {
    var counter by remember { mutableIntStateOf(0) }
    var text by remember { mutableStateOf("") }
    var checked by remember { mutableStateOf(false) }
    var selected by remember { mutableIntStateOf(0) }
    
    val listState = remember { mutableStateListOf<String>() }
    val mapState = remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    
    counter++
    text = "warmup"
    checked = !checked
    selected = (selected + 1) % 4
    
    listState.add("item")
    mapState.value = mapOf("key" to 1)
    
    remember { mutableStateOf<List<Int>>(emptyList()) }
    remember { mutableStateOf<Set<String>>(emptySet()) }
    remember { mutableStateOf<List<com.duoschedule.data.model.Course>>(emptyList()) }
}

@Composable
private fun WarmupAnimationComponents() {
    val scope = rememberCoroutineScope()
    val animatable = remember { Animatable(0f) }
    val animatableOffset = remember { Animatable(0f) }
    val animatableScale = remember { Animatable(1f) }
    
    LaunchedEffect(Unit) {
        animatable.animateTo(
            targetValue = 1f,
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        )
    }
    
    Box(
        modifier = Modifier
            .size(1.dp)
            .scale(animatableScale.value)
    )
}

@Composable
private fun WarmupLazyColumnComponents() {
    val items = remember { List(10) { "Item $it" } }
    
    LazyColumn(
        modifier = Modifier
            .size(100.dp)
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        items(items) { item ->
            Text(
                text = item,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(2.dp)
            )
        }
    }
}

@Composable
private fun WarmupCardComponents() {
    val shape = ContinuousRoundedRectangle(12.dp)
    
    Card(
        modifier = Modifier.size(50.dp),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = Color.Blue.copy(alpha = 0.5f)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Card",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
    
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(ContinuousRoundedRectangle(8.dp))
            .background(Color.Gray.copy(alpha = 0.3f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { }
    )
}

@Composable
private fun WarmupBrushComponents() {
    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color.Blue.copy(alpha = 0.8f),
            Color.Blue.copy(alpha = 0.6f)
        )
    )
    
    val horizontalGradient = Brush.horizontalGradient(
        colors = listOf(
            Color.Red.copy(alpha = 0.5f),
            Color.Red.copy(alpha = 0.3f)
        )
    )
    
    Box(
        modifier = Modifier
            .size(30.dp)
            .background(gradient)
    )
    
    Box(
        modifier = Modifier
            .size(30.dp)
            .background(horizontalGradient)
    )
}

@Composable
private fun WarmupNavigationComponents() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "warmup"
    ) {
        composable("warmup") {
            Text("Warmup Screen", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun WarmupInteractionComponents() {
    val interactionSource = remember { MutableInteractionSource() }
    
    Box(
        modifier = Modifier
            .size(20.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { }
    )
}

@Composable
private fun WarmupScheduleComponents() {
    val items = remember { List(5) { "Period $it" } }
    
    LazyColumn(
        modifier = Modifier
            .size(200.dp)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(items) { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item,
                    style = MaterialTheme.typography.labelSmall
                )
                Box(
                    modifier = Modifier
                        .size(40.dp, 30.dp)
                        .clip(ContinuousRoundedRectangle(4.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                )
            }
        }
    }
}

@Composable
private fun WarmupCourseComponents() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(3) { index ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = ContinuousRoundedRectangle(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "课程名称 $index",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "教室位置",
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "08:00-08:45",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = "第${index + 1}节",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WarmupScreenComponents() {
    val items = remember { List(5) { "Item $it" } }
    
    LazyColumn(
        modifier = Modifier
            .size(300.dp)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items) { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = ContinuousRoundedRectangle(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "主页卡片标题",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "这是卡片的副标题或描述信息",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "时间信息",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "位置信息",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(ContinuousRoundedRectangle(12.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "课表网格头",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                repeat(5) { index ->
                    Text(
                        text = "周${index + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
