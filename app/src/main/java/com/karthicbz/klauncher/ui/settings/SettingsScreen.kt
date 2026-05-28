package com.karthicbz.klauncher.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.tv.material3.*

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val categories by viewModel.categories.collectAsState()
    val currentTheme by viewModel.currentTheme.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(48.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.displayMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            item {
                SettingsSectionHeader("Theming")
            }
            item {
                SettingsClickableItem(
                    title = "Current Theme: ${currentTheme.name}",
                    description = "Change the look and feel of the launcher",
                    onClick = { /* Open Theme Picker */ }
                )
            }

            item {
                SettingsSectionHeader("Categories")
            }
            items(categories) { category ->
                SettingsClickableItem(
                    title = category.name,
                    description = if (category.isSystem) "System Category" else "User Category",
                    onClick = { /* Edit Category */ }
                )
            }
            item {
                Button(onClick = { viewModel.addCategory("New Category") }) {
                    Text("Add Category")
                }
            }

            item {
                SettingsSectionHeader("About")
            }
            item {
                SettingsItem(
                    title = "Version",
                    description = "1.0.0"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBack) {
            Text("Back to Home")
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SettingsClickableItem(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
        shape = ClickableSurfaceDefaults.shape(MaterialTheme.shapes.medium),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SettingsItem(
    title: String,
    description: String
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}
