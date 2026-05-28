package com.karthicbz.klauncher.ui.settings.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import com.karthicbz.klauncher.data.model.CategoryEntity
import com.karthicbz.klauncher.ui.settings.SettingsViewModel

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun CategoriesTab(
    categories: List<CategoryEntity>,
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf<CategoryEntity?>(null) }
    var renameValue by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Manage Categories", style = MaterialTheme.typography.headlineMedium)
            Button(onClick = { showAddDialog = true }) {
                Text("Add Category")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(categories) { category ->
                Surface(
                    onClick = { /* Select */ },
                    shape = ClickableSurfaceDefaults.shape(MaterialTheme.shapes.medium),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(category.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = if (category.isSystem) "System Category" else "User Category",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(onClick = { viewModel.reorderCategory(category, true) }) {
                                Text("▲")
                            }
                            IconButton(onClick = { viewModel.reorderCategory(category, false) }) {
                                Text("▼")
                            }
                            Button(onClick = {
                                renameValue = category.name
                                showRenameDialog = category
                            }) {
                                Text("Rename")
                            }
                            if (!category.isSystem) {
                                Button(
                                    onClick = { viewModel.deleteCategory(category) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                                        contentColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text("Delete")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Category") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter a unique category name:")
                    Button(onClick = {
                        viewModel.addCategory("New Category ${categories.size + 1}")
                        showAddDialog = false
                    }) {
                        Text("Create Predefined Category")
                    }
                }
            },
            confirmButton = {}
        )
    }

    if (showRenameDialog != null) {
        val category = showRenameDialog!!
        AlertDialog(
            onDismissRequest = { showRenameDialog = null },
            title = { Text("Rename Category") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Select a rename preset:")
                    val presets = listOf("Favorites", "Media", "Games", "Tools", "Streaming", "Utilities")
                    presets.forEach { presetName ->
                        Button(
                            onClick = {
                                viewModel.renameCategory(category, presetName)
                                showRenameDialog = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Change to \"$presetName\"")
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}
