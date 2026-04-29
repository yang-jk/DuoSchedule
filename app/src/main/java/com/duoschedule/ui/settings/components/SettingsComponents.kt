package com.duoschedule.ui.settings.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import com.kyant.capsule.ContinuousRoundedRectangle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.duoschedule.ui.theme.*
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight

object SettingsDefaults {
    val CardShape = ContinuousRoundedRectangle(BorderRadius.iOS26.container)
    val ItemVerticalPadding = 12.dp
    val ItemHorizontalPadding = 16.dp
    val SectionSpacing = 24.dp
    val GroupSpacing = 35.dp
    val IconSize = 22.dp
    val IconBackgroundSize = 29.dp
}

@Composable
fun SettingsSection(
    title: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = labelsSecondary,
                modifier = Modifier.padding(
                    start = Spacing.iOS26.compactLG,
                    bottom = Spacing.xs
                )
            )
        }
        
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(
                horizontal = SettingsDefaults.ItemHorizontalPadding,
                vertical = Spacing.sm
            )
        ) {
            content()
        }
    }
}

@Composable
fun SettingsCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg),
        contentPadding = PaddingValues(
            horizontal = SettingsDefaults.ItemHorizontalPadding,
            vertical = Spacing.sm
        ),
        content = content
    )
}

@Composable
fun SettingsRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    iconBackgroundColor: Color? = null,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(AnimationDuration.Micro, easing = FastOutSlowInEasing),
        label = "row_scale"
    )
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            )
            .padding(vertical = SettingsDefaults.ItemVerticalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null && iconBackgroundColor != null) {
            Box(
                modifier = Modifier
                    .size(SettingsDefaults.IconBackgroundSize)
                    .clip(ContinuousRoundedRectangle(BorderRadius.iOS26.icon))
                    .background(iconBackgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(SettingsDefaults.IconSize),
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(Spacing.md))
        }
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = labelsPrimary
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = labelsSecondary
                )
            }
        }
        
        trailing?.invoke()
    }
}

@Composable
fun SettingsNavigationRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    iconBackgroundColor: Color? = null,
    value: String? = null,
    onClick: () -> Unit
) {
    val labelsSecondary = getLabelsVibrantSecondary()
    
    SettingsRow(
        title = title,
        subtitle = subtitle,
        icon = icon,
        iconBackgroundColor = iconBackgroundColor,
        modifier = modifier,
        onClick = onClick,
        trailing = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (value != null) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = BrandColors.Primary
                    )
                    Spacer(modifier = Modifier.width(Spacing.xs))
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = labelsSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    )
}

@Composable
fun SettingsToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    iconBackgroundColor: Color? = null,
    enabled: Boolean = true
) {
    SettingsRow(
        title = title,
        subtitle = subtitle,
        icon = icon,
        iconBackgroundColor = iconBackgroundColor,
        modifier = modifier,
        onClick = { if (enabled) onCheckedChange(!checked) },
        trailing = {
            IOSSwitch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled
            )
        }
    )
}

@Composable
fun IOSSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val backdrop = LocalBackdrop.current ?: emptyBackdrop()
    
    LiquidToggle(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        backdrop = backdrop,
        enabled = enabled
    )
}

@Composable
fun SettingsValueRow(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    SettingsNavigationRow(
        title = title,
        subtitle = subtitle,
        value = value,
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
fun SettingsHeader(
    text: String,
    modifier: Modifier = Modifier
) {
    val labelsSecondary = getLabelsVibrantSecondary()
    
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall.copy(
            fontWeight = FontWeight.SemiBold
        ),
        color = labelsSecondary,
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = Spacing.lg + Spacing.iOS26.compactLG,
                top = Spacing.sm,
                bottom = Spacing.xs
            )
    )
}

@Composable
fun SettingsFooter(
    text: String,
    modifier: Modifier = Modifier
) {
    val labelsTertiary = getLabelsVibrantTertiary()
    
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = labelsTertiary,
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = Spacing.lg + Spacing.iOS26.compactLG,
                vertical = Spacing.xs
            )
    )
}

@Composable
fun SettingsMenuRow(
    title: String,
    selectedOption: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    iconBackgroundColor: Color? = null
) {
    var showDialog by remember { mutableStateOf(false) }
    val backdrop = LocalBackdrop.current ?: emptyBackdrop()
    
    SettingsNavigationRow(
        title = title,
        subtitle = subtitle,
        icon = icon,
        iconBackgroundColor = iconBackgroundColor,
        value = selectedOption,
        onClick = { showDialog = true },
        modifier = modifier
    )
    
    if (showDialog) {
        SettingsOptionDialog(
            title = title,
            options = options,
            selectedOption = selectedOption,
            backdrop = backdrop,
            onDismiss = { showDialog = false },
            onConfirm = { option ->
                onOptionSelected(option)
                showDialog = false
            }
        )
    }
}

@Composable
fun <T> SettingsOptionDialog(
    title: String,
    options: List<T>,
    selectedOption: T,
    backdrop: Backdrop,
    optionLabel: (T) -> String = { it.toString() },
    optionDescription: ((T) -> String)? = null,
    onDismiss: () -> Unit,
    onConfirm: (T) -> Unit
) {
    var currentSelection by remember { mutableStateOf(selectedOption) }
    val darkTheme = LocalDarkTheme.current
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()
    
    val containerColor = if (darkTheme) {
        Color(0xFF121212).copy(alpha = 0.4f)
    } else {
        Color(0xFFFAFAFA).copy(alpha = 0.6f)
    }
    
    val optionBackground = if (darkTheme) {
        Color(0x29EBEBF5)
    } else {
        Color(0x29787880)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .width(300.dp)
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { ContinuousRoundedRectangle(24.dp) },
                    effects = {
                        colorControls(
                            brightness = if (darkTheme) 0f else 0.2f,
                            saturation = 1.5f
                        )
                        blur(if (darkTheme) 8.dp.toPx() else 16.dp.toPx())
                        lens(24.dp.toPx(), 48.dp.toPx(), depthEffect = true)
                    },
                    highlight = { Highlight.Plain },
                    onDrawSurface = { drawRect(containerColor) }
                )
        ) {
            Column(
                modifier = Modifier.padding(Spacing.lg)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = labelsPrimary
                )
                
                Spacer(modifier = Modifier.height(Spacing.md))
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(ContinuousRoundedRectangle(BorderRadius.iOS26.container))
                        .background(optionBackground)
                        .padding(vertical = Spacing.sm)
                ) {
                    options.forEachIndexed { index, option ->
                        val isSelected = option == currentSelection
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { currentSelection = option }
                                .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = optionLabel(option),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                ),
                                color = if (isSelected) BrandColors.Primary else labelsPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = BrandColors.Primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        
                        if (index < options.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = Spacing.md),
                                color = if (darkTheme) Color(0x33EBEBF5) else Color(0x33787880),
                                thickness = 0.5.dp
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(Spacing.lg))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    LiquidGlassButton(
                        onClick = onDismiss,
                        text = "取消",
                        style = LiquidGlassButtonStyle.NonTinted,
                        modifier = Modifier.weight(1f)
                    )
                    
                    LiquidGlassButton(
                        onClick = { onConfirm(currentSelection) },
                        text = "确定",
                        style = LiquidGlassButtonStyle.Tinted,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
