package com.karthicbz.klauncher.ui.settings.components

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
    var showDeleteDialog by remember { mutableStateOf<CategoryEntity?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Manage Categories", style = MaterialTheme.typography.headlineMedium)
            Surface(
                onClick = { showAddDialog = true },
                shape = ClickableSurfaceDefaults.shape(MaterialTheme.shapes.medium),
                scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    focusedContainerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("+ Add Category", modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(categories) { category ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = MaterialTheme.shapes.medium
                        )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            onClick = { showRenameDialog = category },
                            shape = ClickableSurfaceDefaults.shape(MaterialTheme.shapes.small),
                            colors = ClickableSurfaceDefaults.colors(
                                containerColor = Color.Transparent,
                                focusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(4.dp)) {
                                Text(category.name, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = if (category.isSystem) "System" else "User",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SettingsIconButton("▲") { viewModel.reorderCategory(category, moveUp = true) }
                            SettingsIconButton("▼") { viewModel.reorderCategory(category, moveUp = false) }
                            SettingsTextButton("Rename") { showRenameDialog = category }
                            if (!category.isSystem) {
                            SettingsTextButton(
                                label = "Delete",
                                containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                                focusedContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
                            ) { showDeleteDialog = category }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add category dialog — TV overlay with real text input
    if (showAddDialog) {
        TvInputDialog(
            title = "New Category",
            placeholder = "Category name…",
            confirmLabel = "Create",
            onConfirm = { name ->
                if (name.isNotBlank()) viewModel.addCategory(name.trim())
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    // Delete confirmation dialog
    if (showDeleteDialog != null) {
        val cat = showDeleteDialog!!
        TvConfirmDialog(
            title = "Delete \"${cat.name}\"?",
            message = "Apps in this category will be moved to the default category.",
            confirmLabel = "Delete",
            onConfirm = {
                viewModel.deleteCategory(cat)
                showDeleteDialog = null
            },
            onDismiss = { showDeleteDialog = null }
        )
    }

    // Rename dialog
    if (showRenameDialog != null) {
        val cat = showRenameDialog!!
        TvInputDialog(
            title = "Rename \"${cat.name}\"",
            placeholder = cat.name,
            confirmLabel = "Rename",
            onConfirm = { name ->
                if (name.isNotBlank()) viewModel.renameCategory(cat, name.trim())
                showRenameDialog = null
            },
            onDismiss = { showRenameDialog = null }
        )
    }
}

// ---------------------------------------------------------------------------
// Shared TV-native dialog with BasicTextField (no Material3 AlertDialog dep)
// ---------------------------------------------------------------------------

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun TvConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.72f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(420.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.large
                )
                .padding(28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(title, style = MaterialTheme.typography.headlineSmall)
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
            ) {
                SettingsTextButton("Cancel") { onDismiss() }
                SettingsTextButton(
                    label = confirmLabel,
                    containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                    focusedContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
                ) { onConfirm() }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun TvInputDialog(
    title: String,
    placeholder: String,
    confirmLabel: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.72f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(420.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.large
                )
                .padding(28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
                Text(title, style = MaterialTheme.typography.headlineSmall)

                // Text input field
                BasicTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.focusRequester(focusRequester),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
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
                            if (text.isEmpty()) {
                                Text(
                                    placeholder,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            }
                            inner()
                        }
                    }
                )

                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                ) {
                    SettingsTextButton("Cancel") { onDismiss() }
                    SettingsTextButton(
                        label = confirmLabel,
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        focusedContainerColor = MaterialTheme.colorScheme.primary
                    ) { onConfirm(text) }
                }
        }
    }
}

// ---------------------------------------------------------------------------
// Small shared button components
// ---------------------------------------------------------------------------

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
internal fun SettingsTextButton(
    label: String,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surface,
    focusedContainerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(MaterialTheme.shapes.small),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = containerColor,
            focusedContainerColor = focusedContainerColor,
            contentColor = contentColor,
            focusedContentColor = contentColor
        )
    ) {
        Text(label, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp))
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun SettingsIconButton(icon: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(MaterialTheme.shapes.small),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.1f),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Text(icon, modifier = Modifier.padding(8.dp))
    }
}
