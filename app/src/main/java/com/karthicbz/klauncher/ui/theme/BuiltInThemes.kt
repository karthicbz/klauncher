package com.karthicbz.klauncher.ui.theme

// OLED Dark — pure black, easy on OLED screens (default)
val OLED_DARK = DefaultThemeConfig

// OLED Black — slightly lighter surface than OLED Dark for readability
val OLED_BLACK = ThemeConfig(
    name = "OLED Black",
    version = 1,
    colors = ThemeColors(
        background = "#000000",
        surface = "#0A0A0A",
        onSurface = "#E0E0E0",
        primary = "#FFFFFF",
        onPrimary = "#000000",
        accent = "#AAAAAA",
        focusHighlight = "#FFFFFF"
    ),
    shapes = ThemeShapes(cardCornerRadius = 8, iconShape = "square"),
    spacing = ThemeSpacing(padding = 24, gridGap = 12)
)

// Light — bright theme for well-lit rooms
val LIGHT = ThemeConfig(
    name = "Light",
    version = 1,
    colors = ThemeColors(
        background = "#F2F2F2",
        surface = "#FFFFFF",
        onSurface = "#1A1A1A",
        primary = "#1565C0",
        onPrimary = "#FFFFFF",
        accent = "#0288D1",
        focusHighlight = "#1565C0"
    ),
    shapes = ThemeShapes(cardCornerRadius = 12, iconShape = "rounded"),
    spacing = ThemeSpacing(padding = 24, gridGap = 16)
)

// Nord — cool blue-grey palette
val NORD = ThemeConfig(
    name = "Nord",
    version = 1,
    colors = ThemeColors(
        background = "#2E3440",
        surface = "#3B4252",
        onSurface = "#ECEFF4",
        primary = "#88C0D0",
        onPrimary = "#2E3440",
        accent = "#81A1C1",
        focusHighlight = "#EBCB8B"
    ),
    shapes = ThemeShapes(cardCornerRadius = 8, iconShape = "rounded"),
    spacing = ThemeSpacing(padding = 24, gridGap = 16)
)

// Catppuccin Mocha
val CATPPUCCIN = ThemeConfig(
    name = "Catppuccin",
    version = 1,
    colors = ThemeColors(
        background = "#1E1E2E",
        surface = "#313244",
        onSurface = "#CDD6F4",
        primary = "#CBA6F7",
        onPrimary = "#11111B",
        accent = "#F5E0DC",
        focusHighlight = "#F9E2AF"
    ),
    shapes = ThemeShapes(cardCornerRadius = 16, iconShape = "circle"),
    spacing = ThemeSpacing(padding = 24, gridGap = 20)
)

val BuiltInThemes = listOf(OLED_DARK, OLED_BLACK, LIGHT, NORD, CATPPUCCIN)
