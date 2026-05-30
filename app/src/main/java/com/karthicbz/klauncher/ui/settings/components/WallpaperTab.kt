package com.karthicbz.klauncher.ui.settings.components

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
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
    val unsplashKey by viewModel.unsplashAccessKey.collectAsState()
    val unsplashTopics by viewModel.unsplashTopics.collectAsState()
    val unsplashTopicId by viewModel.unsplashTopicId.collectAsState()
    val unsplashAutoUpdate by viewModel.unsplashAutoUpdate.collectAsState()
    val searchResults by viewModel.unsplashSearchResults.collectAsState()
    val isLoading by viewModel.isLoadingUnsplash.collectAsState()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.setWallpaperImageUrl(it.toString())
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Text("Wallpaper", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Choose a solid background colour or open the system wallpaper picker.",
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

            // ── Unsplash ──
            item {
                Text(
                    "Unsplash",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )
            }

            // Unsplash access key
            item {
                val keyFocus = remember { FocusRequester() }
                var keyInput by remember { mutableStateOf(unsplashKey.orEmpty()) }
                Column {
                    Text(
                        "Access Key",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    BasicTextField(
                        value = keyInput,
                        onValueChange = { keyInput = it },
                        modifier = Modifier.focusRequester(keyFocus),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        decorationBox = { inner ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        MaterialTheme.shapes.small
                                    )
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                if (keyInput.isEmpty()) {
                                    Text(
                                        "e.g. abc123...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                    )
                                }
                                inner()
                            }
                        }
                    )
                    LaunchedEffect(Unit) { keyFocus.requestFocus() }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SettingsTextButton("Save Key") {
                            viewModel.setUnsplashAccessKey(keyInput.ifBlank { null })
                        }
                        if (unsplashKey != null) {
                            SettingsTextButton(
                                label = "Load Topics",
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                focusedContainerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.primary
                            ) { viewModel.fetchUnsplashTopics() }
                        }
                    }
                }
            }

            // Unsplash topics / categories
            if (unsplashTopics.isNotEmpty()) {
                item {
                    Text(
                        "Categories",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        unsplashTopics.forEach { topic ->
                            Surface(
                                onClick = { viewModel.setUnsplashTopicId(topic.id) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = ClickableSurfaceDefaults.shape(MaterialTheme.shapes.small),
                                colors = ClickableSurfaceDefaults.colors(
                                    containerColor = if (unsplashTopicId == topic.id)
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                                    else MaterialTheme.colorScheme.surface,
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurface,
                                    focusedContentColor = MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Text(
                                    topic.title,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Unsplash random / auto-update
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SettingsTextButton(
                        label = if (isLoading) "Loading…" else "Get Random",
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        focusedContainerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.primary
                    ) { viewModel.fetchRandomUnsplashPhoto(unsplashTopicId) }

                    SettingsTextButton(
                        label = if (unsplashAutoUpdate) "Auto-Update: ON" else "Auto-Update: OFF",
                        containerColor = if (unsplashAutoUpdate)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        focusedContainerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ) { viewModel.setUnsplashAutoUpdate(!unsplashAutoUpdate) }
                }
            }

            // Unsplash search
            item {
                var query by remember { mutableStateOf("") }
                val searchFocus = remember { FocusRequester() }
                Text(
                    "Search",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.focusRequester(searchFocus),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { inner ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    MaterialTheme.shapes.small
                                )
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            if (query.isEmpty()) {
                                Text(
                                    "Search Unsplash…",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            }
                            inner()
                        }
                    }
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SettingsTextButton("Search") { viewModel.searchUnsplash(query) }
                    if (searchResults.isNotEmpty()) {
                        SettingsTextButton(
                            label = "Clear Results",
                            containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                            focusedContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.4f),
                            contentColor = MaterialTheme.colorScheme.error
                        ) { viewModel.clearUnsplashSearch() }
                    }
                }
            }

            // Unsplash search results
            if (searchResults.isNotEmpty()) {
                item {
                    Text(
                        "Results",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                    )
                }
                items(searchResults.size) { index ->
                    val photo = searchResults[index]
                    Surface(
                        onClick = { viewModel.setWallpaperImageUrl(photo.urls.full) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = ClickableSurfaceDefaults.shape(MaterialTheme.shapes.small),
                        colors = ClickableSurfaceDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                photo.altDescription ?: "Untitled",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                "Apply",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
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
                    ) { viewModel.setWallpaperImageUrl(null) }
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
        runCatching { Color(android.graphics.Color.parseColor(hex)) }
            .getOrDefault(Color.Black)
    } else Color.Transparent

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
