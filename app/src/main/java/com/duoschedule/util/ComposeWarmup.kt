package com.duoschedule.util

import android.content.Context
import android.util.Log
import android.view.Choreographer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

                Choreographer.getInstance().postFrameCallback {
                    composeView.disposeComposition()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Warmup composition failed: ${e.message}")
            }
        }
    }
}

@Composable
private fun WarmupContent() {
    MaterialTheme {
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
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
