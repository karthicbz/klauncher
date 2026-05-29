package com.karthicbz.klauncher.ui.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import com.karthicbz.klauncher.ui.settings.SettingsViewModel
import com.karthicbz.klauncher.ui.theme.ThemeConfig

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ThemesTab(
    builtInThemes: List<ThemeConfig>,
    currentTheme: ThemeConfig,
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    var showImportDialog by remember { mutableStateOf(false) }
    var importStatusMessage by remember { mutableStateOf<String?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Select Theme", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Pick a built-in theme or import a community JSON theme.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            SettingsTextButton("Import Theme") { showImportDialog = true }
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(builtInThemes) { theme ->
                val isActive = theme.name == currentTheme.name
                Surface(
                    onClick = { viewModel.selectTheme(theme) },
                    scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = if (isActive)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        else MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isActive)
                            MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface
                    ),
                    shape = ClickableSurfaceDefaults.shape(MaterialTheme.shapes.medium),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(theme.name, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Colour swatches — flat boxes, no blur
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ColorSwatch(theme.colors.background)
                            ColorSwatch(theme.colors.surface)
                            ColorSwatch(theme.colors.primary)
                            ColorSwatch(theme.colors.accent)
                        }

                        if (isActive) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "✓ Active",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }

    // Import dialog — TV-native overlay, no AlertDialog
    if (showImportDialog) {
        TvImportDialog(
            statusMessage = importStatusMessage,
            onImportUrl = { url ->
                viewModel.importThemeFromUrl(
                    url = url,
                    onSuccess = { importStatusMessage = "Theme imported successfully!" },
                    onError = { error -> importStatusMessage = "Import failed: $error" }
                )
            },
            onImportJson = { json ->
                val result = viewModel.importThemeFromJson(json)
                importStatusMessage = if (result.isSuccess) {
                    "Theme validated and applied!"
                } else {
                    "Validation failed: ${result.exceptionOrNull()?.message}"
                }
            },
            onDismiss = {
                showImportDialog = false
                importStatusMessage = null
            }
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun TvImportDialog(
    statusMessage: String?,
    onImportUrl: (String) -> Unit,
    onImportJson: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var urlText by remember { mutableStateOf("") }
    var jsonText by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.72f)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            onClick = { /* consume */ },
            modifier = Modifier.width(520.dp),
            shape = ClickableSurfaceDefaults.shape(MaterialTheme.shapes.large),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Import Community Theme", style = MaterialTheme.typography.headlineSmall)
                Text(
                    "Paste a URL to a raw JSON theme file, or paste JSON directly.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                // URL input
                Text("From URL:", style = MaterialTheme.typography.labelMedium)
                BasicTextField(
                    value = urlText,
                    onValueChange = { urlText = it },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
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
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            if (urlText.isEmpty()) {
                                Text(
                                    "https://raw.githubusercontent.com/…/theme.json",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                                )
                            }
                            inner()
                        }
                    }
                )
                SettingsTextButton(
                    label = "Download & Apply",
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    focusedContainerColor = MaterialTheme.colorScheme.primary
                ) { if (urlText.isNotBlank()) onImportUrl(urlText.trim()) }

                Spacer(modifier = Modifier.height(4.dp))

                // JSON paste input
                Text("Paste raw JSON:", style = MaterialTheme.typography.labelMedium)
                BasicTextField(
                    value = jsonText,
                    onValueChange = { jsonText = it },
                    textStyle = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    maxLines = 4,
                    decorationBox = { inner ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    MaterialTheme.shapes.small
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            if (jsonText.isEmpty()) {
                                Text(
                                    "{ \"name\": \"My Theme\", … }",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                                )
                            }
                            inner()
                        }
                    }
                )
                SettingsTextButton(
                    label = "Validate & Apply",
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    focusedContainerColor = MaterialTheme.colorScheme.primary
                ) { if (jsonText.isNotBlank()) onImportJson(jsonText.trim()) }

                // Status feedback
                if (statusMessage != null) {
                    Text(
                        text = statusMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (statusMessage.startsWith("✓") || statusMessage.contains("success", ignoreCase = true))
                            MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error
                    )
                }

                SettingsTextButton("Close") { onDismiss() }
            }
        }
    }
}

@Composable
private fun ColorSwatch(hexColor: String) {
    val color = try {
        Color(android.graphics.Color.parseColor(hexColor))
    } catch (_: Exception) {
        Color.Gray
    }
    Box(
        modifier = Modifier
            .size(18.dp)
            .background(color, androidx.compose.foundation.shape.CircleShape)
    )
}
