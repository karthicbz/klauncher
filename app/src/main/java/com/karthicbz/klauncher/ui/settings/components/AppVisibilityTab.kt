package com.karthicbz.klauncher.ui.settings.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import com.karthicbz.klauncher.data.model.AppInfo
import com.karthicbz.klauncher.data.model.CategoryEntity
import com.karthicbz.klauncher.ui.settings.SettingsViewModel

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun AppVisibilityTab(
    categoriesWithAllApps: Map<CategoryEntity, List<AppInfo>>,
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        Text("App Visibility", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Toggle to hide or show apps on the home screen.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.weight(1f)
        ) {
            categoriesWithAllApps.forEach { (category, apps) ->
                if (apps.isNotEmpty()) {
                    item {
                        Column {
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                apps.forEach { app ->
                                    Surface(
                                        onClick = {
                                            viewModel.setAppHidden(app.packageName, !app.isHidden)
                                        },
                                        shape = ClickableSurfaceDefaults.shape(MaterialTheme.shapes.medium),
                                        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f),
                                        colors = ClickableSurfaceDefaults.colors(
                                            containerColor = MaterialTheme.colorScheme.surface,
                                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(
                                                horizontal = 16.dp, vertical = 12.dp
                                            ),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(app.label, style = MaterialTheme.typography.bodyLarge)
                                                Text(
                                                    app.packageName,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                                )
                                            }
                                            // Flat pill toggle — replaces non-TV Checkbox
                                            VisibilityPill(visible = !app.isHidden)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Flat coloured pill showing Visible/Hidden — zero Material3 dependencies. */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun VisibilityPill(visible: Boolean) {
    val bg = if (visible) MaterialTheme.colorScheme.primary
             else MaterialTheme.colorScheme.surfaceVariant
    val fg = if (visible) MaterialTheme.colorScheme.onPrimary
             else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

    Surface(
        onClick = {},
        shape = ClickableSurfaceDefaults.shape(
            androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
        ),
        colors = ClickableSurfaceDefaults.colors(containerColor = bg, focusedContainerColor = bg)
    ) {
        Text(
            text = if (visible) "Visible" else "Hidden",
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}
