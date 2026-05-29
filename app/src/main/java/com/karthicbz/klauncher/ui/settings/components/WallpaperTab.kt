package com.karthicbz.klauncher.ui.settings.components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun WallpaperTab(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Column(modifier = modifier.fillMaxSize()) {
        Text("Wallpaper", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Choose a solid background colour or open the system wallpaper picker.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Solid colour presets — flat boxes, no overdraw
        Text(
            "Solid Colour Backgrounds",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        val solidColors = listOf(
            "#000000" to "OLED Black",
            "#0D0D0D" to "Near Black",
            "#1A1A2E" to "Deep Navy",
            "#16213E" to "Midnight Blue",
            "#1C1C1C" to "Charcoal",
            "#2C2C2C" to "Dark Grey",
            "#0F2027" to "Dark Teal",
            "#1B1B2F" to "Deep Purple"
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            solidColors.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { (hex, label) ->
                        ColorPresetCard(
                            hex = hex,
                            label = label,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Fill remaining space if odd count
                    if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // System wallpaper picker fallback
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
                } catch (_: Exception) { /* device doesn't support wallpaper */ }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun ColorPresetCard(hex: String, label: String, modifier: Modifier = Modifier) {
    val color = runCatching { Color(android.graphics.Color.parseColor(hex)) }
        .getOrDefault(Color.Black)

    Surface(
        onClick = { /* future: apply solid colour as background via WallpaperManager */ },
        shape = ClickableSurfaceDefaults.shape(MaterialTheme.shapes.medium),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(color, MaterialTheme.shapes.small)
            )
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
