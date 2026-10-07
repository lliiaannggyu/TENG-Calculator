package com.tengwear.jisuanqi.ui.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme

/**
 * Material 3 官方蓝色主题
 */
private val BlueScheme = ColorScheme(
    primary = md_primary,
    onPrimary = md_onPrimary,
    primaryContainer = md_primaryContainer,
    onPrimaryContainer = md_onPrimaryContainer,
    secondary = md_secondary,
    onSecondary = md_onSecondary,
    secondaryContainer = md_secondaryContainer,
    onSecondaryContainer = md_onSecondaryContainer,
    tertiary = md_tertiary,
    onTertiary = md_onTertiary,
    background = md_background,
    onBackground = md_onBackground,
    onSurface = md_onSurface,
    onSurfaceVariant = md_onSurfaceVariant,
    error = md_error,
    onError = md_onError
)

/**
 * themeId 参数保留兼容（MainActivity 里仍在传），但不再使用
 */
@Composable
fun JiSuanQiTheme(
    themeId: String = "material_blue",
    content: @Composable () -> Unit
) {
    @Suppress("UNUSED_EXPRESSION")
    themeId

    MaterialTheme(
        colorScheme = BlueScheme,
        typography = Typography,
        content = content
    )
}