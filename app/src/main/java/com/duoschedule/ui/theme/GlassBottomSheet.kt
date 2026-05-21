package com.duoschedule.ui.theme

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import com.kyant.capsule.ContinuousRoundedRectangle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.highlight.Highlight

object GlassBottomSheetDefaults {
    val CornerRadiusTop = LiquidGlassColors.BottomSheet.CornerRadiusTop
    val CornerRadiusBottom = LiquidGlassColors.BottomSheet.CornerRadiusBottom
    val BlurRadius = LiquidGlassColors.BottomSheet.BlurRadius
    val LensRefractionHeight = 24.dp
    val LensRefractionAmount = 48.dp
    val ContentHorizontalPadding = 20.dp
    val ContentTopPadding = 28.dp
    val ContentBottomPadding = 32.dp
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassBottomSheet(
    onDismiss: () -> Unit,
    sheetState: SheetState,
    modifier: Modifier = Modifier,
    backdrop: Backdrop = LocalBackdrop.current ?: emptyBackdrop(),
    darkTheme: Boolean = LocalDarkTheme.current,
    enableDismissOnSwipe: Boolean = true,
    content: @Composable ColumnScope.(LayerBackdrop) -> Unit
) {
    val density = LocalDensity.current
    val bottomSheetBackdrop = rememberLayerBackdrop()

    val actualSheetState = if (enableDismissOnSwipe) {
        sheetState
    } else {
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
            confirmValueChange = { newValue ->
                newValue != SheetValue.Hidden
            }
        )
    }

    val deviceBottomCorner = getRoundedCornerBottom()
    val shape = ContinuousRoundedRectangle(
        topStart = GlassBottomSheetDefaults.CornerRadiusTop,
        topEnd = GlassBottomSheetDefaults.CornerRadiusTop,
        bottomStart = deviceBottomCorner,
        bottomEnd = deviceBottomCorner
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = actualSheetState,
        shape = shape,
        containerColor = Color.Transparent,
        scrimColor = if (darkTheme) Color.Black.copy(alpha = 0.48f) else Color.Black.copy(alpha = 0.32f),
        dragHandle = null,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        modifier = modifier
    ) {
        CompositionLocalProvider(LocalBackdrop provides bottomSheetBackdrop) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBackdrop(
                        backdrop = backdrop,
                        shape = { shape },
                        effects = {
                            glassCardEffects(darkTheme, density)
                        },
                        highlight = { Highlight.Plain },
                        exportedBackdrop = bottomSheetBackdrop,
                        onDrawSurface = {
                            glassCardSurfaceColors(darkTheme)
                        }
                    )
                    .navigationBarsPadding()
            ) {
                content(bottomSheetBackdrop)
            }
        }
    }
}
