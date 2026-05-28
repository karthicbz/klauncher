package com.karthicbz.klauncher.ui.theme

import kotlinx.serialization.Serializable

@Serializable
data class ThemeConfig(
    val name: String,
    val colors: ThemeColors,
    val shapes: ThemeShapes,
    val spacing: ThemeSpacing
)

@Serializable
data class ThemeColors(
    val background: String,
    val surface: String,
    val onSurface: String,
    val primary: String,
    val onPrimary: String,
    val accent: String,
    val focusHighlight: String
)

@Serializable
data class ThemeShapes(
    val cardCornerRadius: Int,
    val iconShape: String // "circle", "rounded", "square"
)

@Serializable
data class ThemeSpacing(
    val padding: Int,
    val gridGap: Int
)

val DefaultThemeConfig = ThemeConfig(
    name = "OLED Dark",
    colors = ThemeColors(
        background = "#000000",
        surface = "#121212",
        onSurface = "#FFFFFF",
        primary = "#BB86FC",
        onPrimary = "#000000",
        accent = "#03DAC6",
        focusHighlight = "#FFFFFF"
    ),
    shapes = ThemeShapes(
        cardCornerRadius = 12,
        iconShape = "rounded"
    ),
    spacing = ThemeSpacing(
        padding = 24,
        gridGap = 16
    )
)
