package com.duoschedule.ui.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.blur.BlurColors
import top.yukonga.miuix.kmp.blur.BlurDefaults
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.textureBlur

/**
 * 分层渐变模糊组件
 *
 * 参考 ABlur (https://github.com/xujunhao940/ABlur) 的算法思路：
 * 通过堆叠 N 层 [textureBlur] 模糊切片，每层使用不同的模糊半径和垂直渐变 alpha 遮罩，
 * 实现模糊半径沿 Y 轴从 [baseBlur]（顶部）渐进到 0（底部）的真实渐变模糊效果。
 *
 * 与"均匀模糊 + 渐变色遮罩"的伪渐变方案不同，本方案下底层模糊强度沿轴向真实变化，
 * 呈现 iOS 控制中心 / macOS Sonoma 风格的分层渐变模糊。
 *
 * 本组件以 [BoxScope] 扩展形式使用，内部通过 [matchParentSize] 的独立 Box 渲染每层模糊，
 * 遮罩仅作用于模糊层，不影响兄弟内容（如标题、返回按钮等）。
 *
 * 各层遮罩采用"从顶部渐变衰减"策略，相邻层自然重叠，避免硬边界导致的阶梯感：
 * - Layer 0（blur = baseBlur）：遮罩从 y=0 渐变衰减到 0 于 y=endAt/N
 * - Layer 1（blur = baseBlur×(N-1)/N）：遮罩从 y=0 渐变衰减到 0 于 y=2×endAt/N
 * - ...
 * - Layer N-1（blur = baseBlur/N）：遮罩从 y=0 渐变衰减到 0 于 y=endAt
 *
 * @param layers 模糊切片层数（越多越平滑，性能越低），默认 4
 * @param baseBlur 顶部最大模糊半径（px），默认 120f
 * @param endAt 模糊衰减到 0 的相对位置（0.0–1.0），默认 1.0f
 * @param flip 反转模糊方向（false=顶强底弱，适配顶栏；true=底强顶弱，预留给底栏等场景）
 * @param backdrop 模糊源 Backdrop
 * @param blurColors 模糊颜色配置，默认 BlurDefaults.blurColors(saturation = 1.3f)
 */
@Composable
fun BoxScope.LayeredGradientBlur(
    layers: Int = 5,
    baseBlur: Float = 120f,
    endAt: Float = 1.0f,
    flip: Boolean = false,
    backdrop: Backdrop,
    blurColors: BlurColors = BlurDefaults.blurColors(saturation = 1.3f),
) {
    val shaderSupported = remember { isRuntimeShaderSupported() }
    if (!shaderSupported) {
        // 不支持 RuntimeShader 的设备降级为单层均匀模糊
        Box(
            modifier = Modifier
                .matchParentSize()
                .textureBlur(
                    backdrop = backdrop,
                    shape = RoundedCornerShape(0.dp),
                    blurRadiusX = baseBlur * 0.4f,
                    blurRadiusY = baseBlur,
                    noiseCoefficient = 0f,
                    colors = blurColors,
                )
        )
        return
    }

    val n = layers.coerceAtLeast(1)
    for (i in 0 until n) {
        // 第 i 层模糊半径：i=0 时为 baseBlur（最强），i=n-1 时为 baseBlur/n（最弱）
        val blurRadius = baseBlur * (n - i) / n
        // 第 i 层遮罩衰减终点：越强的层衰减越快（靠近顶部消失），越弱的层延伸越远
        val fadeEnd = endAt * (i + 1) / n

        val colorStops = if (!flip) {
            // 顶强底弱：从顶部 alpha=1 渐变衰减到 0 于 fadeEnd
            arrayOf(
                0.0f to Color.Black,
                fadeEnd to Color.Transparent,
                1.0f to Color.Transparent,
            )
        } else {
            // 底强顶弱：从底部 alpha=1 渐变衰减到 0
            arrayOf(
                0.0f to Color.Transparent,
                (1.0f - fadeEnd) to Color.Transparent,
                1.0f to Color.Black,
            )
        }

        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .textureBlur(
                    backdrop = backdrop,
                    shape = RoundedCornerShape(0.dp),
                    blurRadiusX = baseBlur * 0.4f,
                    blurRadiusY = blurRadius,
                    noiseCoefficient = 0f,
                    colors = blurColors,
                )
                .drawWithContent {
                    drawContent()
                    drawRect(
                        brush = Brush.verticalGradient(colorStops = colorStops),
                        blendMode = BlendMode.DstIn,
                    )
                }
        )
    }
}
