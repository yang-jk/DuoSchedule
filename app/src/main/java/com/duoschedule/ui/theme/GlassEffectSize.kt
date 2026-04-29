package com.duoschedule.ui.theme

import androidx.compose.ui.unit.dp

enum class GlassEffectSize(
    val blurRadius: androidx.compose.ui.unit.Dp,
    val cornerRadius: androidx.compose.ui.unit.Dp,
    val shadowElevation: androidx.compose.ui.unit.Dp
) {
    SMALL(
        blurRadius = 8.dp,
        cornerRadius = 16.dp,
        shadowElevation = 8.dp
    ),
    MEDIUM(
        blurRadius = 12.dp,
        cornerRadius = 24.dp,
        shadowElevation = 16.dp
    ),
    LARGE(
        blurRadius = 16.dp,
        cornerRadius = 32.dp,
        shadowElevation = 24.dp
    ),
    CARD(
        blurRadius = 12.dp,
        cornerRadius = 24.dp,
        shadowElevation = 20.dp
    ),
    BUTTON(
        blurRadius = 8.dp,
        cornerRadius = 16.dp,
        shadowElevation = 12.dp
    ),
    NAV_BAR(
        blurRadius = 16.dp,
        cornerRadius = 34.dp,
        shadowElevation = 24.dp
    ),
    BOTTOM_SHEET(
        blurRadius = 16.dp,
        cornerRadius = 24.dp,
        shadowElevation = 32.dp
    ),
    DIALOG(
        blurRadius = 16.dp,
        cornerRadius = 28.dp,
        shadowElevation = 32.dp
    ),
    CHIP(
        blurRadius = 4.dp,
        cornerRadius = 12.dp,
        shadowElevation = 4.dp
    ),
    INPUT_FIELD(
        blurRadius = 8.dp,
        cornerRadius = 16.dp,
        shadowElevation = 8.dp
    )
}
