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
- **Alphabet filter**: "A-Z" button opens an overlay with all alphabets on visible buttons. Selecting a letter filters all category apps to those starting with that letter. "Clear Filter" button when active.

### App Management
- Auto-scans both `CATEGORY_LEANBACK_LAUNCHER` and `CATEGORY_LAUNCHER` intents, deduplicates by packageName.
- Reordering: D-pad Left/Right within category, Up/Down moves to adjacent category. Border highlight on reorder item.
- Hide/unhide apps from context menu.
- Context menu: Reorder, Hide, Move to Category, App Info, Uninstall, Cancel.
- "Move to Category" opens category picker overlay to move app between categories.
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
- 8 solid dark colour presets + "None (Theme Default)" option.
- System wallpaper picker (falls back from `ACTION_LIVE_WALLPAPER_CHOOSER` to `ACTION_SET_WALLPAPER`).
- Wallpaper color is persisted separately from theme, applied as root background. Image wallpaper supported via Coil `AsyncImage`.
- **Unsplash integration**: API key input, load topic/category list, get random photo (respects selected category), search with result list, auto-update toggle.
- **Local file picker**: Pick image from device storage via `ActivityResultContracts.OpenDocument`.
- Image and color wallpapers are mutually exclusive — setting one clears the other.

### Settings (7 Tabs)
1. **Categories** — Add/rename/delete/reorder categories. Clicking a row opens rename dialog. Text field in dialogs auto-focused.
2. **App Visibility** — Toggle hidden/visible per app, grouped by category.
3. **Themes & Styles** — Built-in theme grid + import dialog (URL or raw JSON).
4. **Weather** — Latitude/longitude text inputs + Save button. Save action calls `viewModel.setLocation(lat, lon)`.
5. **Wallpaper** — Solid colour presets, Unsplash (key/topics/search/random), local file picker, system wallpaper picker.
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
│   ├── remote/  (WeatherApiService — Retrofit interface + DTOs, UnsplashApi — URL+serialization client)
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
- Fixed `BasicTextField` not focusable in `TvInputDialog` by adding `FocusRequester` + `LaunchedEffect`.
- Fixed category row `onClick` no-op — now opens rename dialog.
- Added "Move to Category" option to app context menu + `CategoryPickerOverlay` dialog.
- Added wallpaper color presets `onClick` — solid colors now apply as root background via `Modifier.background()`.
- Added `wallpaperColor` state to `UserPreferencesRepository` + `SettingsViewModel`.
- Applied wallpaper in `MainActivity.kt` (color via `Modifier.background`, image via Coil `AsyncImage`).
- Fixed About tab `Surface` content colors by adding explicit `contentColor`/`focusedContentColor`.
- **Unsplash wallpaper integration**:
  - Created `UnsplashApi.kt` (data/remote/) — `getTopics`, `getRandomPhoto`, `searchPhotos` via `java.net.URL` + kotlinx.serialization.
  - Added state flows: `wallpaperImageUrl`, `unsplashAccessKey`, `unsplashTopicId`, `unsplashAutoUpdate` in `UserPreferencesRepository`.
  - Added `SettingsViewModel` methods: `setUnsplashAccessKey`, `fetchUnsplashTopics`, `fetchRandomUnsplashPhoto`, `searchUnsplash`, etc.
  - Rewrote `WallpaperTab` with sections for solid colors, system picker, local file picker, Unsplash key input, topics list, random/auto-update, search.
- **Local file picker**: Uses `ActivityResultContracts.OpenDocument("image/*")` to pick images from device storage.
- **Home screen alphabet filter**: "A-Z" button opens an alphabet picker overlay. Filters app rows by first letter. "Clear Filter" button when active. Removed duplicate "Apps" heading to eliminate redundancy with the default category header.
- Fixed A-Z alphabet picker: letter buttons invisible (`surface`/`primary.copy(alpha=0.2f)` → `surfaceVariant`/solid `primary`). Fixed focus trap: outer `Surface(onClick={consume})` replaced with non-clickable `Column` so D-pad reaches letter buttons and Cancel.
