package com.karthicbz.klauncher.ui.settings.components

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import com.karthicbz.klauncher.repository.WallpaperSource
import com.karthicbz.klauncher.ui.settings.SettingsViewModel

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun WallpaperTab(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentWallpaper by viewModel.wallpaperColor.collectAsState()
    val currentImageWallpaper by viewModel.wallpaperImageUrl.collectAsState()
    val wallpaperSource by viewModel.wallpaperSource.collectAsState()
    val pixabayCategory by viewModel.pixabayCategory.collectAsState()
    val pixabayCategories = viewModel.pixabayCategories
    val isLoading by viewModel.isLoadingWallpaper.collectAsState()
    val status by viewModel.wallpaperStatus.collectAsState()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.setWallpaperImageUrl(it.toString(), WallpaperSource.LOCAL_IMAGE)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Text("Wallpaper", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Choose a solid colour, daily Bing wallpaper, Pixabay, or a local image.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            // ── Solid colours ──
            item {
                Text(
                    "Solid Colour Backgrounds",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            item {
                val solidColors = listOf(
                    null to "None (Theme Default)",
                    "#000000" to "OLED Black", "#0D0D0D" to "Near Black",
                    "#1A1A2E" to "Deep Navy", "#16213E" to "Midnight Blue",
                    "#1C1C1C" to "Charcoal", "#2C2C2C" to "Dark Grey",
                    "#0F2027" to "Dark Teal", "#1B1B2F" to "Deep Purple"
                )
                SolidColorGrid(
                    colors = solidColors,
                    selected = currentWallpaper,
                    onSelect = { viewModel.setWallpaperColor(it) }
                )
            }

            // ── System wallpaper picker ──
            item {
                SettingsTextButton(
                    label = "Open System Wallpaper Picker",
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    focusedContainerColor = MaterialTheme.colorScheme.primary
                ) {
                    try {
                        context.startActivity(
                            Intent(android.app.WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                        )
                    } catch (_: Exception) {
                        try {
                            context.startActivity(
                                Intent(Intent.ACTION_SET_WALLPAPER).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                            )
                        } catch (_: Exception) { }
                    }
                }
            }

            // ── Pick from local disk ──
            item {
                Text(
                    "Local Image",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )
            }
            item {
                SettingsTextButton(
                    label = "Pick Image from Device Storage",
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    focusedContainerColor = MaterialTheme.colorScheme.primary
                ) {
                    filePickerLauncher.launch(arrayOf("image/*"))
                }
            }

            // ── Bing Daily Wallpaper ──
            item {
                Text(
                    "Bing Daily Wallpaper",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SettingsTextButton(
                            label = if (wallpaperSource == WallpaperSource.BING) "Bing: ON" else "Bing: OFF",
                            containerColor = if (wallpaperSource == WallpaperSource.BING)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            focusedContainerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ) {
                            if (wallpaperSource == WallpaperSource.BING) {
                                viewModel.setWallpaperSource(WallpaperSource.NONE)
                            } else {
                                viewModel.fetchBingWallpaper()
                            }
                        }
                        if (wallpaperSource == WallpaperSource.BING) {
                            SettingsTextButton(
                                label = if (isLoading) "Refreshing…" else "Refresh Now",
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                focusedContainerColor = MaterialTheme.colorScheme.primary
                            ) { viewModel.fetchBingWallpaper() }
                        }
                    }
                    Text(
                        "Fetches the daily Bing homepage image automatically.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            // ── Pixabay ──
            item {
                Text(
                    "Pixabay",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Category",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        pixabayCategories.forEach { cat ->
                            Surface(
                                onClick = { viewModel.setPixabayCategory(cat) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = ClickableSurfaceDefaults.shape(MaterialTheme.shapes.small),
                                colors = ClickableSurfaceDefaults.colors(
                                    containerColor = if (pixabayCategory == cat)
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                                    else MaterialTheme.colorScheme.surface,
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurface,
                                    focusedContentColor = MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Text(
                                    cat.replaceFirstChar { it.uppercase() },
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SettingsTextButton(
                            label = if (isLoading) "Applying…" else "Apply Pixabay Wallpaper",
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            focusedContainerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.primary
                        ) { viewModel.fetchPixabayWallpaper(pixabayCategory) }
                        if (wallpaperSource == WallpaperSource.PIXABAY) {
                            SettingsTextButton(
                                label = "Refresh Now",
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                focusedContainerColor = MaterialTheme.colorScheme.primary
                            ) { viewModel.fetchPixabayWallpaper(pixabayCategory) }
                        }
                    }
                    Text(
                        "Picks a random photo from your chosen category. Updates daily on app launch.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            // Status feedback
            if (status != null) {
                item {
                    Text(
                        text = status!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (status!!.startsWith("✓") || status!!.contains("applied", ignoreCase = true))
                            MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error
                    )
                }
            }

            // Clear image wallpaper
            if (currentImageWallpaper != null) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    SettingsTextButton(
                        label = "Clear Image Wallpaper",
                        containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                        focusedContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.4f),
                        contentColor = MaterialTheme.colorScheme.error
                    ) { viewModel.setWallpaperImageUrl(null, WallpaperSource.NONE) }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun SolidColorGrid(
    colors: List<Pair<String?, String>>,
    selected: String?,
    onSelect: (String?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        colors.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { (hex, label) ->
                    ColorPresetCard(
                        hex = hex,
                        label = label,
                        isSelected = selected == hex,
                        onClick = { onSelect(hex) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun ColorPresetCard(
    hex: String?,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = if (hex != null) {
        runCatching { androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(hex)) }
            .getOrDefault(androidx.compose.ui.graphics.Color.Black)
    } else androidx.compose.ui.graphics.Color.Transparent

    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(MaterialTheme.shapes.medium),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                else MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        if (hex != null) color else MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.shapes.small
                    )
            )
            Text(label, style = MaterialTheme.typography.bodySmall)
        }
    }
}
