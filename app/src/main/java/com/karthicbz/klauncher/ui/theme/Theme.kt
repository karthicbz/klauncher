package com.karthicbz.klauncher.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.*

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

private val LocalThemeConfig = staticCompositionLocalOf { DefaultThemeConfig }

object KLauncherTheme {
    val config: ThemeConfig
        @Composable
        @ReadOnlyComposable
        get() = LocalThemeConfig.current
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun KlauncherTheme(
    config: ThemeConfig = DefaultThemeConfig,
    content: @Composable () -> Unit
) {
    val bgRgb = android.graphics.Color.parseColor(config.colors.background)
    val bgR = android.graphics.Color.red(bgRgb) / 255.0
    val bgG = android.graphics.Color.green(bgRgb) / 255.0
    val bgB = android.graphics.Color.blue(bgRgb) / 255.0
    val isLightTheme = (0.2126 * bgR + 0.7152 * bgG + 0.0722 * bgB) > 0.5

    val themeColors = arrayOf(
        Color(android.graphics.Color.parseColor(config.colors.background)),
        Color(android.graphics.Color.parseColor(config.colors.surface)),
        Color(android.graphics.Color.parseColor(config.colors.onSurface)),
        Color(android.graphics.Color.parseColor(config.colors.primary)),
        Color(android.graphics.Color.parseColor(config.colors.onPrimary)),
        Color(android.graphics.Color.parseColor(config.colors.accent)),
        Color(android.graphics.Color.parseColor(config.colors.focusHighlight))
    )

    val colorScheme = if (isLightTheme) {
        lightColorScheme(
            background = themeColors[0],
            surface = themeColors[1],
            onSurface = themeColors[2],
            primary = themeColors[3],
            onPrimary = themeColors[4],
            secondary = themeColors[5],
            tertiary = themeColors[6]
        )
    } else {
        darkColorScheme(
            background = themeColors[0],
            surface = themeColors[1],
            onSurface = themeColors[2],
            primary = themeColors[3],
            onPrimary = themeColors[4],
            secondary = themeColors[5],
            tertiary = themeColors[6]
        )
    }

    val shapes = Shapes(
        medium = RoundedCornerShape(config.shapes.cardCornerRadius.dp),
        large = RoundedCornerShape((config.shapes.cardCornerRadius * 1.5).toInt().dp)
    )

    CompositionLocalProvider(LocalThemeConfig provides config) {
        MaterialTheme(
            colorScheme = colorScheme,
            shapes = shapes,
            content = content
        )
    }
}
