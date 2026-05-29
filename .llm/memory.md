# KLauncher — Project Memory

## Overview
**KLauncher** is a lightweight Android TV launcher built entirely with Jetpack Compose. Optimized for old TV units, Fire TV / Fire Stick devices, and Android TV set-top boxes. Replaces the default TV home screen with a customizable, D-pad navigable interface.

- **Root package:** `com.karthicbz.klauncher`
- **Git:** main branch, no tests yet.

---

## Tech Stack

| Category | Technology | Version |
|---|---|---|
| Language | Kotlin | 2.0.21 |
| Build | Gradle (Kotlin DSL) + Version Catalog | AGP 8.7.3 |
| UI | Jetpack Compose (BOM) | 2024.12.01 |
| TV UI | `androidx.tv:tv-material` | 1.0.0 |
| Navigation | Navigation Compose | 2.8.5 |
| DI | Dagger Hilt | 2.52 |
| Database | Room (KSP) | 2.6.1 |
| Networking | Retrofit 2 + kotlinx-serialization-converter | 2.11.0 |
| HTTP | OkHttp (logging interceptor) | 4.12.0 |
| Serialization | kotlinx-serialization-json | 1.7.3 |
| Image Loading | Coil (Compose) | 2.7.0 |
| Icons Extended | `androidx.compose.material:material-icons-extended` | (BOM-managed) |
| Coroutines | kotlinx-coroutines-android | 1.9.0 |
| TV Provider | `androidx.tvprovider` | 1.1.0 |
| Min/Target SDK | minSdk=23, targetSdk=36, compileSdk=36 | |

---

## Architecture: MVVM + Repository

```
Activity (@AndroidEntryPoint)
  └── NavHost (HomeScreen | SettingsScreen)
        ├── HomeViewModel #1 (@HiltViewModel, in ui/home/) — apps + watch next
        │     ├── AppRepository (singleton)
        │     └── WatchNextRepository (singleton)
        ├── HomeViewModel #2 (@HiltViewModel, in ui/home/viewmodel/) — weather
        │     ├── WeatherRepository (singleton)
        │     └── UserPreferencesRepository (singleton)
        └── SettingsViewModel (@HiltViewModel)
              ├── CategoryDao
              ├── ThemeRepository (singleton)
              ├── AppRepository (singleton)
              └── UserPreferencesRepository (singleton)
```

- **Single Activity**, two NavHost destinations.
- **Sealed UiState**: `Loading | Success | Error` for home screen.
- **No UseCases** — logic in ViewModels + Repositories.
- **DAOs** return `Flow<List<T>>` for reactive updates.
- **Theme via CompositionLocal** wrapping `MaterialTheme`.
- **Broadcast Receivers** are `@AndroidEntryPoint` with injected `AppRepository`.

---

## Feature Details

### Home Screen
- App grid: `LazyColumn` of `LazyRow`s, grouped by user-defined category.
- Loading skeleton with shimmer animation.
- Live clock (HH:mm) + date (EEE, d MMM), updated every second.
- Weather display: temperature + WMO weather icon (from Open-Meteo).
- "Continue Watching" row from Android TV's Watch Next ContentProvider.
- App launching via Leanback intent, fallback to standard launcher intent.

### App Management
- Auto-scans both `CATEGORY_LEANBACK_LAUNCHER` and `CATEGORY_LAUNCHER` intents, deduplicates by packageName.
- Reordering: D-pad Left/Right within category, Up/Down moves to adjacent category. Border highlight on reorder item.
- Hide/unhide apps from context menu.
- Context menu: Reorder, Hide, App Info, Uninstall, Cancel.
- `PackageChangeReceiver` refreshes on install/uninstall/update.
- `BootReceiver` refreshes on reboot.

### Categories
- Default "Apps" category auto-created on first scan.
- CRUD: Add, Rename, Delete (system only), Reorder.
- Position-based ordering in Room, system categories protected from deletion.

### Themes & Styling
- 5 built-in themes: OLED Dark (default), OLED Black, Light, Nord, Catppuccino.
- Custom theme import from URL (raw JSON) or paste raw JSON.
- Validation: color hex regex, bounds on shapes/spacing.
- Persisted as JSON string in SharedPreferences.
- Configurable: card corner radius, icon shape (circle/rounded/square), padding, grid gap.
- Active theme checkmark in settings.

### Weather (Open-Meteo)
- Free, no API key needed. Endpoint: `api.open-meteo.com/v1/forecast`.
- Coordinates stored in SharedPreferences via `UserPreferencesRepository`.
- `WeatherRepository` fetches `CurrentWeather` (temperature, weathercode, windspeed, winddirection, time).
- `WeatherIconMapper` maps WMO codes to Material icons.
- Weather refreshes when lat/lon change (combined flow + `collectLatest`).

### Wallpaper
- 8 solid dark colour presets.
- System wallpaper picker (falls back from `ACTION_LIVE_WALLPAPER_CHOOSER` to `ACTION_SET_WALLPAPER`).

### Settings (6 Tabs)
1. **Categories** — Add/rename/delete/reorder categories.
2. **App Visibility** — Toggle hidden/visible per app, grouped by category.
3. **Themes & Styles** — Built-in theme grid + import dialog (URL or raw JSON).
4. **Weather** — Latitude/longitude text inputs + Save button. Save action calls `viewModel.setLocation(lat, lon)`.
5. **Wallpaper** — Solid colour presets + system wallpaper picker.
6. **About** — App version + Fire TV sideload instructions.

### TV-Optimized UX
- All surfaces use `androidx.tv.material3` clickable surfaces.
- `android.hardware.touchscreen` set to `required=false`.
- `LEANBACK_LAUNCHER` category in intent-filter.
- Flat black overlays (no blur for low-end devices).
- 10-foot text readability.
- Auto-focus first app card via `FocusRequester`.
- Custom overlay dialogs (no Material3 AlertDialog).
- 16:9 aspect ratio cards.

### Android Manifest Highlights
- Registers as HOME screen replacement (can be default launcher).
- `android:persistent="true"` on application tag.
- `singleInstance` launch mode.
- Permissions: INTERNET, RECEIVE_BOOT_COMPLETED, READ_EPG_DATA, READ_TV_LISTINGS, QUERY_ALL_PACKAGES.

---

## Key Files

```
app/src/main/java/com/karthicbz/klauncher/
├── KLauncherApp.kt                       # @HiltAndroidApp, Coil ImageLoaderFactory
├── MainActivity.kt                       # @AndroidEntryPoint, NavHost
├── data/
│   ├── db/      (AppDatabase, AppDao, CategoryDao, ThemeDao)
│   ├── model/   (AppEntity, CategoryEntity, ThemeEntity, AppInfo, WatchNextProgram)
│   ├── remote/  (WeatherApiService — Retrofit interface + DTOs)
│   └── repository/ (WeatherRepository)
├── di/          (DatabaseModule, NetworkModule)
├── receiver/    (BootReceiver, PackageChangeReceiver)
├── repository/  (AppRepository, ThemeRepository, UserPreferencesRepository, WatchNextRepository)
├── ui/
│   ├── home/            (HomeScreen, HomeUiState, HomeViewModel — apps)
│   │   ├── components/  (AppCard, AppContextMenu, ErrorScreen, HomeHeader, LoadingScreen, WatchNextCard)
│   │   └── viewmodel/   (HomeViewModel — weather)
│   ├── navigation/      (Screens sealed class)
│   ├── settings/        (SettingsScreen, SettingsViewModel)
│   │   └── components/  (AboutTab, AppVisibilityTab, CategoriesTab, ThemesTab, WallpaperTab, WeatherSettingsTab)
│   └── theme/           (Theme, ThemeConfig, BuiltInThemes, Type)
└── util/        (AppIconFetcher — Coil, WeatherIconMapper — WMO codes)
```

---

## Recent Work (this agent session)
- Added weather display using Open-Meteo.
- Added Retrofit + OkHttp + converter-kotlinx-serialization to version catalog and build file.
- Created `NetworkModule` (Hilt module for Json + Retrofit `WeatherApiService`).
- Created `WeatherApiService` (Retrofit interface), `WeatherRepository`, `UserPreferencesRepository` (lat/lon).
- Created `WeatherIconMapper` (WMO code → Material icon).
- Created `WeatherSettingsTab` in settings with lat/lon inputs.
- Updated `SettingsViewModel` to inject `UserPreferencesRepository` and expose `setLocation()`.
- Updated `SettingsScreen` to add Weather tab.
- Created `HomeViewModel` (in `ui/home/viewmodel/`) for weather state.
- Updated `HomeHeader` to accept `weather: CurrentWeather?` and display icon + temp next to clock.
- Updated `HomeScreen` to wire weather from a separate `HomeViewModel` (weather) into `HomeHeader`.
- Fixed version catalog key conflict (`retrofit` vs `retrofit-serialization-converter` → renamed to `retrofit-core`).
- Added `material-icons-extended` dependency for weather icons.
- Fixed `HomeViewModel` class name collision (two ViewModels with same name) by using import alias.
- Added Toast feedback on weather location save, input validation, and pre-filled lat/lon fields from saved values.
- Fixed `OutlinedTextField` text colors to respect theme's `onSurface` color (fixes visibility in Catppuccin and other themes).
