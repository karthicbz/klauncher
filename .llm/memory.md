# Session Memory

## Goal
- Fix bugs in an Android TV launcher: duplicate headings, invisible dialog buttons, focus traps, category management, and app-to-category assignment.

## Constraints & Preferences
- Android TV (D-pad navigation, `androidx.tv.material3`)
- Kotlin + Jetpack Compose
- System categories (`isSystem=true`) must not be deletable

## Progress
### Done
- Removed hardcoded "Apps" heading (`HomeScreen.kt`) to eliminate duplicate with default category section header.
- Changed letter button colors in `AlphabetPickerOverlay` (unfocused `surface`→`surfaceVariant`, focused `primary.copy(alpha=0.2f)`→solid `primary` with `onPrimary` text).
- Replaced focus-trapping `Surface(onClick={consume})` dialog containers with non-clickable `Column` + `Modifier.background` in: `AlphabetPickerOverlay`, `CategoryPickerOverlay`, `AppContextMenu`, `TvConfirmDialog`, `TvInputDialog`.
- Removed `.filter { apps.isNotEmpty() || category.isSystem }` in `HomeScreen.kt` so empty user-created categories show on the homepage.
- Added `isSystem` guard in `SettingsViewModel.deleteCategory()` so system categories cannot be deleted.
- Added `TvConfirmDialog` with confirmation step before category deletion.
- Restructured `CategoriesTab.kt` category rows: outer `Surface(onClick=rename)` replaced with non-clickable `Column` + separate clickable `Surface` for rename, making ▲/▼/Rename/Delete buttons independently focusable.
- Added `Key.Menu` / `Key.MediaTopMenu` handler in `AppCard.kt` as alternative to long-press for opening context menu.
- Replaced `SpaceBetween` with `End` alignment in alphabet filter row (heading removed, only buttons remain).
- Fixed TvInputDialog Column close brace indentation in `CategoriesTab.kt`.
- Long-press detection for context menu: checks `isLongPress` on `KeyEventType.KeyUp` for `DirectionCenter`/`Enter`. Handles `Key.Menu` on `KeyDown` as alternative. Removed `Key.MediaTopMenu` (not a standard remote key).
- Updated `.llm/memory.md` with all changes.
- Fixed focus trap in `TvImportDialog` (`ThemesTab.kt`): replaced `Surface(onClick={consume})` with non-clickable `Column` + `Modifier.background` — the same fix applied to the other 5 dialogs but was missed in the import dialog.
- Fixed theme active indicator (`ThemesTab.kt:56`): changed `theme.name == currentTheme.name` to `theme == currentTheme` so imported themes with the same name as a built-in don't incorrectly show as "✓ Active".
- Fixed wallpaper image hidden by opaque `Surface` (`MainActivity.kt:63-66`): added conditional `color = if (wallpaperImageUrl != null) Color.Transparent else MaterialTheme.colorScheme.surface` so wallpaper shows through when set.
- Mapped `accent`→`secondary` and `focusHighlight`→`tertiary` in `darkColorScheme` (`Theme.kt:34-35`): accent and focus highlight colors were defined in `ThemeConfig` and shown as swatches in the theme picker but never applied to the color scheme.
- Fixed long-press on `AppCard` (`AppCard.kt`): moved key event handling from outer `Box` (which never received events because the focused `Surface` consumed them) to `onPreviewKeyEvent` on the `Surface`'s own modifier chain, so the long-press timer actually starts on `KeyDown` and can prevent `onClick` on `KeyUp`.
- Replaced Unsplash wallpaper with Bing Daily + Pixabay: removed `UnsplashApi.kt`, created `BingApi.kt` and `PixabayApi.kt`, added `WallpaperSource` enum to `UserPreferencesRepository.kt`, rewrote `WallpaperTab.kt` with Bing toggle and Pixabay category selector.
- Added explicit `SSLHandshakeException` catch in `SettingsViewModel.kt` so users get a friendly "check device date/time" message instead of a raw exception.

### In Progress
- *(none)*

### Blocked
- *(none)*

## Key Decisions
- Remove `.filter {}` entirely from homepage categories instead of just widening it — empty user categories must be visible so users know they exist and can move apps into them.
- Make dialog containers non-clickable (`Column`+`background`) instead of `Surface(onClick=consume)` to eliminate D-pad focus traps while preserving visual style.
- Use confirmation dialog for category deletion rather than immediate delete.
- Add `Key.Menu` handler in addition to long-press for context menu, since long-press is unreliable on some TV remotes.

## Next Steps
- Build and test the app.
- Verify categories display, deletion with confirmation, D-pad navigation through all dialogs and overlays, and context menu via Menu button.

## Critical Context
- The "Move to Category" flow: navigate to app → long-press OK/Enter or press Menu button → context menu → "Move to Category" → pick destination category.
- System categories (`isSystem=true`) are the default "Apps" category; user-created categories have `isSystem=false`.
- All dialog overlays (`AlphabetPickerOverlay`, `CategoryPickerOverlay`, `AppContextMenu`, `TvConfirmDialog`, `TvInputDialog`, `TvImportDialog`) had the same focus-trap pattern (`Surface(onClick={consume})`) and were all fixed.

## Relevant Files
- `app/src/main/java/com/karthicbz/klauncher/ui/home/HomeScreen.kt`: Removed duplicate "Apps" heading, removed category filter, fixed `AlphabetPickerOverlay` and `CategoryPickerOverlay` focus traps, updated letter button colors.
- `app/src/main/java/com/karthicbz/klauncher/ui/home/components/AppCard.kt`: Added `Key.Menu`/`Key.MediaTopMenu` handler for context menu.
- `app/src/main/java/com/karthicbz/klauncher/ui/home/components/AppContextMenu.kt`: Fixed focus trap (removed clickable outer `Surface`).
- `app/src/main/java/com/karthicbz/klauncher/ui/settings/SettingsViewModel.kt`: Added `isSystem` guard in `deleteCategory()`.
- `app/src/main/java/com/karthicbz/klauncher/ui/settings/components/CategoriesTab.kt`: Restructured rows for focusable buttons, added `TvConfirmDialog`, fixed dialog focus traps and TvInputDialog Column close brace.
- `app/src/main/java/com/karthicbz/klauncher/ui/settings/components/ThemesTab.kt`: Fixed focus trap in `TvImportDialog`, fixed theme active indicator comparison (`theme == currentTheme` instead of `name == name`).
- `app/src/main/java/com/karthicbz/klauncher/MainActivity.kt`: Made `Surface` transparent when wallpaper is set so the image is visible.
- `app/src/main/java/com/karthicbz/klauncher/ui/theme/Theme.kt`: Mapped `accent` → `secondary` and `focusHighlight` → `tertiary` in the color scheme (previously unused).
- `app/src/main/java/com/karthicbz/klauncher/data/remote/PixabayApi.kt`: New — Pixabay API client for category-based wallpaper fetching.
- `app/src/main/java/com/karthicbz/klauncher/data/remote/BingApi.kt`: New — Bing daily wallpaper API client.
- `app/src/main/java/com/karthicbz/klauncher/repository/UserPreferencesRepository.kt`: Removed Unsplash prefs, added `WallpaperSource` enum, `pixabayCategory`, and source-tracking.
- `app/src/main/java/com/karthicbz/klauncher/ui/settings/SettingsViewModel.kt`: Removed all Unsplash methods, added `fetchBingWallpaper()` and `fetchPixabayWallpaper()`.
- `app/src/main/java/com/karthicbz/klauncher/ui/settings/components/WallpaperTab.kt`: Replaced Unsplash section with Bing daily toggle and Pixabay category selector.
- `.llm/memory.md`: Updated with all changes.
