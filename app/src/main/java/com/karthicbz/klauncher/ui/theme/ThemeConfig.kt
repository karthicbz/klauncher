package com.karthicbz.klauncher.ui.theme

import kotlinx.serialization.Serializable

@Serializable
data class ThemeConfig(
    val name: String,
    val version: Int = 1,
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

fun ThemeConfig.validate(): ThemeConfig {
    val hexRegex = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{8})$".toRegex()
    
    fun validateColor(color: String, default: String): String {
        return if (hexRegex.matches(color)) color else default
    }

    val defaultColors = DefaultThemeConfig.colors
    val validatedColors = ThemeColors(
        background = validateColor(colors.background, defaultColors.background),
        surface = validateColor(colors.surface, defaultColors.surface),
        onSurface = validateColor(colors.onSurface, defaultColors.onSurface),
        primary = validateColor(colors.primary, defaultColors.primary),
        onPrimary = validateColor(colors.onPrimary, defaultColors.onPrimary),
        accent = validateColor(colors.accent, defaultColors.accent),
        focusHighlight = validateColor(colors.focusHighlight, defaultColors.focusHighlight)
    )

    val validatedShapes = ThemeShapes(
        cardCornerRadius = if (shapes.cardCornerRadius in 0..50) shapes.cardCornerRadius else DefaultThemeConfig.shapes.cardCornerRadius,
        iconShape = if (shapes.iconShape in listOf("circle", "rounded", "square")) shapes.iconShape else DefaultThemeConfig.shapes.iconShape
    )

    val validatedSpacing = ThemeSpacing(
        padding = if (spacing.padding in 0..100) spacing.padding else DefaultThemeConfig.spacing.padding,
        gridGap = if (spacing.gridGap in 0..100) spacing.gridGap else DefaultThemeConfig.spacing.gridGap
    )

    return ThemeConfig(
        name = if (name.isNotBlank()) name else "Unnamed Theme",
        version = version,
        colors = validatedColors,
        shapes = validatedShapes,
        spacing = validatedSpacing
    )
}

