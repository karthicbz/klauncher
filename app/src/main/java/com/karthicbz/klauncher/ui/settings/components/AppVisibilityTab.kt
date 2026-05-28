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
            "Toggle switches to hide or show installed applications on the home screen.",
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
                                        colors = ClickableSurfaceDefaults.colors(
                                            containerColor = MaterialTheme.colorScheme.surface
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(app.label, style = MaterialTheme.typography.bodyLarge)
                                                Text(
                                                    app.packageName,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                                )
                                            }
                                            Checkbox(
                                                checked = !app.isHidden,
                                                onCheckedChange = null // Click handled by Surface
                                            )
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
