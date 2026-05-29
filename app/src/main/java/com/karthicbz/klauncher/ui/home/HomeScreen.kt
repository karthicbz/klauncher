package com.karthicbz.klauncher.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
//import androidx.tv.material3.TvLazyRow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
            is HomeUiState.Loading -> LoadingScreen()
            is HomeUiState.Success -> HomeContent(
                state = state,
                onAppClick = { viewModel.launchApp(it) },
                onProgramClick = { viewModel.launchWatchNextProgram(it) },
                onSettingsClick = onNavigateToSettings,
                viewModel = viewModel,
                reorderingAppPackage = reorderingAppPackage,
                onReorderAppPackageChange = { reorderingAppPackage = it },
                onShowMenuForApp = { showMenuForApp = it }
            )
            is HomeUiState.Error -> ErrorScreen(state.message)
        }

        // Context menu overlaid on top — flat backdrop, D-pad navigable
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
    // Focus request for the very first app card so D-pad works on first frame
    val firstCardFocus = remember { FocusRequester() }
    var focusRequested by remember { mutableStateOf(false) }

    val categories = state.categoriesWithApps

    LaunchedEffect(categories) {
        if (!focusRequested && categories.isNotEmpty()) {
            try {
                firstCardFocus.requestFocus()
                focusRequested = true
            } catch (_: Exception) {
                // Composable may not be attached yet — will retry on next emission
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        HomeHeader(onSettingsClick)

        // LazyColumn with TV Surface cards — D-pad focus handled by TV Material Surface
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Continue Watching row
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

            // Category rows
            categories.entries.forEachIndexed { categoryIndex, (category, apps) ->
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
                                    // Attach focus requester to the first card of the first category
                                    val isFirstCard =
                                        categoryIndex == 0 && apps.indexOf(app) == 0

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
                                        modifier = Modifier
                                            .then(
                                                if (isFirstCard) Modifier.focusRequester(firstCardFocus)
                                                else Modifier
                                            )
                                            .then(
                                                if (isReordering) {
                                                    Modifier
                                                        .border(
                                                            BorderStroke(3.dp, MaterialTheme.colorScheme.primary),
                                                            MaterialTheme.shapes.medium
                                                        )
                                                        .onKeyEvent { keyEvent ->
                                                            if (keyEvent.type == KeyEventType.KeyDown) {
                                                                handleReorderKey(
                                                                    keyEvent = keyEvent,
                                                                    app = app,
                                                                    apps = apps,
                                                                    category = category,
                                                                    state = state,
                                                                    viewModel = viewModel,
                                                                    onReorderAppPackageChange = onReorderAppPackageChange
                                                                )
                                                            } else false
                                                        }
                                                } else Modifier
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

/**
 * Handles D-pad key events during app reorder mode.
 * Left/Right reorders within the category; Up/Down moves to adjacent category.
 * Center/Enter/Back exits reorder mode.
 */
@OptIn(ExperimentalTvMaterial3Api::class)
private fun handleReorderKey(
    keyEvent: KeyEvent,
    app: AppInfo,
    apps: List<AppInfo>,
    category: com.karthicbz.klauncher.data.model.CategoryEntity,
    state: HomeUiState.Success,
    viewModel: HomeViewModel,
    onReorderAppPackageChange: (String?) -> Unit
): Boolean {
    val currentIndex = apps.indexOf(app)
    val categoriesList = state.categoriesWithApps.keys.toList()
    val currentCategoryIndex = categoriesList.indexOf(category)

    return when (keyEvent.key) {
        Key.DirectionLeft -> {
            if (currentIndex > 0) {
                viewModel.reorderApp(category.id, app.packageName, currentIndex, currentIndex - 1)
            }
            true
        }
        Key.DirectionRight -> {
            if (currentIndex < apps.size - 1) {
                viewModel.reorderApp(category.id, app.packageName, currentIndex, currentIndex + 1)
            }
            true
        }
        Key.DirectionUp -> {
            if (currentCategoryIndex > 0) {
                viewModel.moveAppToCategory(
                    app.packageName,
                    category.id,
                    categoriesList[currentCategoryIndex - 1].id
                )
            }
            true
        }
        Key.DirectionDown -> {
            if (currentCategoryIndex < categoriesList.size - 1) {
                viewModel.moveAppToCategory(
                    app.packageName,
                    category.id,
                    categoriesList[currentCategoryIndex + 1].id
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
}
