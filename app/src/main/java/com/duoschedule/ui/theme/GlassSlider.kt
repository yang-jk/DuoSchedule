package com.duoschedule.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberCombinedBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.lens

@Composable
fun GlassSlider(
    backdrop: Backdrop,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier
            .padding(horizontal = 24f.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.CenterStart
    ) {
        val trackBackdrop = rememberLayerBackdrop()

        // track
        Box(
            Modifier
                .layerBackdrop(trackBackdrop)
                .background(Color(0xFF0088FF), CircleShape)
                .height(6f.dp)
                .fillMaxWidth()
        )

        // thumb
        Box(
            Modifier
                .offset(x = maxWidth / 2f - 28f.dp)
                .drawBackdrop(
                    // We want to draw both of `backdrop` and `trackBackdrop`
                    backdrop = rememberCombinedBackdrop(backdrop, trackBackdrop),
                    shape = { CircleShape },
                    effects = {
                        lens(
                            refractionHeight = 12f.dp.toPx(),
                            refractionAmount = 16f.dp.toPx(),
                            chromaticAberration = true
                        )
                    }
                )
                .size(56f.dp, 32f.dp)
        )
    }
}