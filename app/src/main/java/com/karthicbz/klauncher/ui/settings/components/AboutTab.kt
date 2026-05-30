package com.karthicbz.klauncher.ui.settings.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun AboutTab(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize()) {
        Text("About klauncher", style = MaterialTheme.typography.headlineMedium)
        Text("Version 1.0.0 (API 22+ Support)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            item {
                Surface(
                    onClick = { /* No-op */ },
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        focusedContentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = ClickableSurfaceDefaults.shape(MaterialTheme.shapes.medium),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Description", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "klauncher is a premium, lightweight, fully customizable Jetpack Compose-based TV launcher. " +
                            "It has been meticulously optimized for old TV units, stick devices (Fire TV / Fire Stick), and standard Android TV set-top boxes.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            item {
                Surface(
                    onClick = { /* No-op */ },
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        focusedContentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = ClickableSurfaceDefaults.shape(MaterialTheme.shapes.medium),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Fire TV / Fire Stick Sideload Instructions", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "1. Go to Settings -> My Fire TV -> Developer Options -> Turn on 'ADB Debugging'.\n" +
                            "2. Sideload the APK using Downloader or ADB from your computer:\n" +
                            "   adb install klauncher.apk\n" +
                            "3. To replace default launcher completely without root, run via ADB shell:\n" +
                            "   adb shell cmd package set-home-activity com.karthicbz.klauncher/.MainActivity\n" +
                            "4. Press home key to launch!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}
