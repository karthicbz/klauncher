package com.karthicbz.klauncher.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.*
import com.karthicbz.klauncher.data.model.AppInfo
import com.karthicbz.klauncher.ui.home.components.*

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var reorderingAppPackage by remember { mutableStateOf<String?>(null) }
    var showMenuForApp by remember { mutableStateOf<AppInfo?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is HomeUiState.Loading -> {
                LoadingScreen()
            }
            is HomeUiState.Success -> {
                HomeContent(
                    state = state,
                    onAppClick = { viewModel.launchApp(it) },
                    onProgramClick = { viewModel.launchWatchNextProgram(it) },
                    onSettingsClick = onNavigateToSettings,
                    viewModel = viewModel,
                    reorderingAppPackage = reorderingAppPackage,
                    onReorderAppPackageChange = { reorderingAppPackage = it },
                    onShowMenuForApp = { showMenuForApp = it }
                )
            }
            is HomeUiState.Error -> {
                ErrorScreen(state.message)
            }
        }

        if (showMenuForApp != null) {
            AppContextMenu(
                app = showMenuForApp!!,
                onDismissRequest = { showMenuForApp = null },
                onReorderClick = { reorderingAppPackage = showMenuForApp!!.packageName },
                onHideClick = { viewModel.setAppHidden(showMenuForApp!!.packageName, true) }
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun HomeContent(
    state: HomeUiState.Success,
    onAppClick: (String) -> Unit,
    onProgramClick: (Long) -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: HomeViewModel,
    reorderingAppPackage: String?,
    onReorderAppPackageChange: (String?) -> Unit,
    onShowMenuForApp: (AppInfo?) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        HomeHeader(onSettingsClick)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Watch Next / Continue Watching Row
            if (state.watchNextPrograms.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 48.dp)) {
                        Text(
                            text = "Continue Watching",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(state.watchNextPrograms) { program ->
                                WatchNextCard(
                                    program = program,
                                    onClick = { onProgramClick(program.id) }
                                )
                            }
                        }
                    }
                }
            }

            // Categories list
            state.categoriesWithApps.forEach { (category, apps) ->
                if (apps.isNotEmpty() || category.isSystem) {
                    item {
                        Column(modifier = Modifier.padding(horizontal = 48.dp)) {
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(apps) { app ->
                                    val isReordering = reorderingAppPackage == app.packageName
                                    AppCard(
                                        app = app,
                                        onClick = {
                                            if (reorderingAppPackage != null) {
                                                onReorderAppPackageChange(null)
                                            } else {
                                                onAppClick(app.packageName)
                                            }
                                        },
                                        onLongClick = {
                                            if (reorderingAppPackage == null) {
                                                onShowMenuForApp(app)
                                            }
                                        },
                                        modifier = Modifier.then(
                                            if (isReordering) {
                                                Modifier
                                                    .border(
                                                        BorderStroke(
                                                            3.dp,
                                                            MaterialTheme.colorScheme.primary
                                                        ),
                                                        MaterialTheme.shapes.medium
                                                    )
                                                    .onKeyEvent { keyEvent ->
                                                        if (keyEvent.type == KeyEventType.KeyDown) {
                                                            when (keyEvent.key) {
                                                                Key.DirectionLeft -> {
                                                                    val currentIndex = apps.indexOf(app)
                                                                    if (currentIndex > 0) {
                                                                        viewModel.reorderApp(
                                                                            category.id,
                                                                            app.packageName,
                                                                            currentIndex,
                                                                            currentIndex - 1
                                                                        )
                                                                    }
                                                                    true
                                                                }
                                                                Key.DirectionRight -> {
                                                                    val currentIndex = apps.indexOf(app)
                                                                    if (currentIndex < apps.size - 1) {
                                                                        viewModel.reorderApp(
                                                                            category.id,
                                                                            app.packageName,
                                                                            currentIndex,
                                                                            currentIndex + 1
                                                                        )
                                                                    }
                                                                    true
                                                                }
                                                                Key.DirectionUp -> {
                                                                    val categoriesList = state.categoriesWithApps.keys.toList()
                                                                    val currentCategoryIndex = categoriesList.indexOf(category)
                                                                    if (currentCategoryIndex > 0) {
                                                                        val targetCategory = categoriesList[currentCategoryIndex - 1]
                                                                        viewModel.moveAppToCategory(
                                                                            app.packageName,
                                                                            category.id,
                                                                            targetCategory.id
                                                                        )
                                                                    }
                                                                    true
                                                                }
                                                                Key.DirectionDown -> {
                                                                    val categoriesList = state.categoriesWithApps.keys.toList()
                                                                    val currentCategoryIndex = categoriesList.indexOf(category)
                                                                    if (currentCategoryIndex < categoriesList.size - 1) {
                                                                        val targetCategory = categoriesList[currentCategoryIndex + 1]
                                                                        viewModel.moveAppToCategory(
                                                                            app.packageName,
                                                                            category.id,
                                                                            targetCategory.id
                                                                        )
                                                                    }
                                                                    true
                                                                }
                                                                Key.DirectionCenter, Key.Enter, Key.Back -> {
                                                                    onReorderAppPackageChange(null)
                                                                    true
                                                                }
                                                                else -> false
                                                            }
                                                        } else false
                                                    }
                                            } else {
                                                Modifier
                                            }
                                        )
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
