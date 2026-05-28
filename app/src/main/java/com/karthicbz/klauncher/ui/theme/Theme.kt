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
    val colorScheme = darkColorScheme(
        background = Color(android.graphics.Color.parseColor(config.colors.background)),
        surface = Color(android.graphics.Color.parseColor(config.colors.surface)),
        onSurface = Color(android.graphics.Color.parseColor(config.colors.onSurface)),
        primary = Color(android.graphics.Color.parseColor(config.colors.primary)),
        onPrimary = Color(android.graphics.Color.parseColor(config.colors.onPrimary))
    )

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
