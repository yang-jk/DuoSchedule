package com.duoschedule.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Separator(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    color: Color? = null
) {
    val darkTheme = LocalDarkTheme.current
    val dividerColor = color ?: if (darkTheme) {
        SeparatorsDark.NonOpaque
    } else {
        SeparatorsLight.NonOpaque
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness)
            .background(dividerColor)
    )
}
