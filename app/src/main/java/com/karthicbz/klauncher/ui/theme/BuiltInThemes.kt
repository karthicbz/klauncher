package com.karthicbz.klauncher.ui.theme

val OLED_DARK = DefaultThemeConfig

val NORD = ThemeConfig(
    name = "Nord",
    colors = ThemeColors(
        background = "#2E3440",
        surface = "#3B4252",
        onSurface = "#ECEFF4",
        primary = "#88C0D0",
        onPrimary = "#2E3440",
        accent = "#81A1C1",
        focusHighlight = "#EBCB8B"
    ),
    shapes = ThemeShapes(
        cardCornerRadius = 8,
        iconShape = "rounded"
    ),
    spacing = ThemeSpacing(
        padding = 24,
        gridGap = 16
    )
)

val CATPPUCCIN = ThemeConfig(
    name = "Catppuccin",
    colors = ThemeColors(
        background = "#1E1E2E",
        surface = "#313244",
        onSurface = "#CDD6F4",
        primary = "#CBA6F7",
        onPrimary = "#11111B",
        accent = "#F5E0DC",
        focusHighlight = "#F9E2AF"
    ),
    shapes = ThemeShapes(
        cardCornerRadius = 16,
        iconShape = "circle"
    ),
    spacing = ThemeSpacing(
        padding = 24,
        gridGap = 20
    )
)

val BuiltInThemes = listOf(OLED_DARK, NORD, CATPPUCCIN)
