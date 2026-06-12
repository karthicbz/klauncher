# KLauncher

A lightweight, customizable Android TV launcher built entirely with Jetpack Compose. Optimized for old TV units, Fire TV / Fire Stick devices, and Android TV set-top boxes.

## Features

- **App Grid** — Apps grouped by customizable categories, D-pad navigable
- **App Management** — Auto-scan installed apps, reorder within/between categories, hide apps
- **Categories** — Create, rename, delete, and reorder app categories
- **Live Clock** — Real-time clock with date display on home screen
- **Weather** — Current temperature + icon from Open-Meteo (free, no API key) based on your coordinates
- **Continue Watching** — "Watch Next" row from Android TV's ContentProvider
- **Themes** — 5 built-in themes (OLED Dark, OLED Black, Light, Nord, Catppuccin) plus custom theme import from URL or JSON
- **Wallpaper** — Solid colour presets, system wallpaper picker, or Unsplash integration
- **App Context Menu** — Long-press OK/Enter or press Menu key per-app actions (move to category, hide)
- **TV-Optimized** — Full D-pad navigation, no touchscreen required, focus-trap-free dialogs, flat overlays for performance, 10-foot readability

## Tech Stack

| Category | Technology |
|---|---|
| Language | Kotlin 2.0.21 |
| UI | Jetpack Compose + TV Material |
| DI | Dagger Hilt |
| Database | Room (KSP) |
| Networking | Retrofit 2 + kotlinx-serialization |
| Image Loading | Coil |
| Architecture | MVVM + Repository |

## Setup

1. Clone the repo
2. Open in Android Studio
3. Sync Gradle and run on an Android TV device or emulator (API 23+)

## Configuration

Go to **Settings → Weather** to set your latitude/longitude for weather display. The app uses Open-Meteo which requires no API key.
