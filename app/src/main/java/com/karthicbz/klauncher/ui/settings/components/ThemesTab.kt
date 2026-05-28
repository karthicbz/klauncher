package com.karthicbz.klauncher.ui.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
                Text("Pick a pre-configured styling layout or load a community JSON theme.", style = MaterialTheme.typography.bodySmall)
            }
            Button(onClick = { showImportDialog = true }) {
                Text("Import Theme")
            }
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
                        containerColor = if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
                        contentColor = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        focusedContentColor = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = ClickableSurfaceDefaults.shape(MaterialTheme.shapes.medium),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(theme.name, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.size(16.dp).background(Color(android.graphics.Color.parseColor(theme.colors.background)), MaterialTheme.shapes.extraSmall))
                            Box(modifier = Modifier.size(16.dp).background(Color(android.graphics.Color.parseColor(theme.colors.surface)), MaterialTheme.shapes.extraSmall))
                            Box(modifier = Modifier.size(16.dp).background(Color(android.graphics.Color.parseColor(theme.colors.primary)), MaterialTheme.shapes.extraSmall))
                            Box(modifier = Modifier.size(16.dp).background(Color(android.graphics.Color.parseColor(theme.colors.accent)), MaterialTheme.shapes.extraSmall))
                        }
                        if (isActive) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Active Theme", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }

    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = {
                showImportDialog = false
                importStatusMessage = null
            },
            title = { Text("Import Community Theme") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Import raw styling JSON from community github or paste raw files. Choose from preset examples:",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Button(
                        onClick = {
                            val presetUrl = "https://raw.githubusercontent.com/karthicbz/klauncher-themes/main/Nord.json"
                            viewModel.importThemeFromUrl(
                                url = presetUrl,
                                onSuccess = {
                                    importStatusMessage = "Successfully imported Nord theme!"
                                },
                                onError = { error ->
                                    importStatusMessage = "Error importing theme: $error (Preset fallback initiated)"
                                    viewModel.importThemeFromJson("""
                                        {
                                            "name": "Nord Raw",
                                            "colors": {
                                                "background": "#2E3440",
                                                "surface": "#3B4252",
                                                "onSurface": "#ECEFF4",
                                                "primary": "#88C0D0",
                                                "onPrimary": "#2E3440",
                                                "accent": "#81A1C1",
                                                "focusHighlight": "#EBCB8B"
                                            },
                                            "shapes": { "cardCornerRadius": 8, "iconShape": "rounded" },
                                            "spacing": { "padding": 24, "gridGap": 16 }
                                        }
                                    """.trimIndent())
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Download Nord (Remote Preset)")
                    }

                    Button(
                        onClick = {
                            val rawJson = """
                                {
                                    "name": "Matrix OLED",
                                    "colors": {
                                        "background": "#000000",
                                        "surface": "#111111",
                                        "onSurface": "#33FF33",
                                        "primary": "#00FF00",
                                        "onPrimary": "#000000",
                                        "accent": "#008800",
                                        "focusHighlight": "#33FF33"
                                    },
                                    "shapes": { "cardCornerRadius": 0, "iconShape": "square" },
                                    "spacing": { "padding": 20, "gridGap": 12 }
                                }
                            """.trimIndent()
                            val result = viewModel.importThemeFromJson(rawJson)
                            if (result.isSuccess) {
                                importStatusMessage = "Successfully validated and imported Matrix OLED!"
                            } else {
                                importStatusMessage = "Validation failed: ${result.exceptionOrNull()?.message}"
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Import Matrix OLED (Raw JSON)")
                    }

                    if (importStatusMessage != null) {
                        Text(
                            text = importStatusMessage!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {}
        )
    }
}
