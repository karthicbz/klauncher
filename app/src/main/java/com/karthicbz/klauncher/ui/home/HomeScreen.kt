package com.karthicbz.klauncher.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
//import androidx.tv.material3.TvLazyRow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

import androidx.tv.material3.*
import com.karthicbz.klauncher.data.model.AppInfo
import com.karthicbz.klauncher.ui.home.components.*
import com.karthicbz.klauncher.ui.home.viewmodel.HomeViewModel as WeatherViewModel

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val weatherViewModel: WeatherViewModel = hiltViewModel()

    var reorderingAppPackage by remember { mutableStateOf<String?>(null) }
    var showMenuForApp by remember { mutableStateOf<AppInfo?>(null) }
    var showCategoryPickerForApp by remember { mutableStateOf<AppInfo?>(null) }
    var alphabetFilter by remember { mutableStateOf<Char?>(null) }
    var showAlphabetPicker by remember { mutableStateOf(false) }

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
                onShowMenuForApp = { showMenuForApp = it },
                weatherViewModel = weatherViewModel,
                alphabetFilter = alphabetFilter,
                onAlphabetFilterChange = { alphabetFilter = it },
                onShowAlphabetPicker = { showAlphabetPicker = it }
            )
            is HomeUiState.Error -> ErrorScreen(state.message)
        }

        // Context menu overlaid on top — flat backdrop, D-pad navigable
        if (showMenuForApp != null) {
            AppContextMenu(
                app = showMenuForApp!!,
                onDismissRequest = { showMenuForApp = null },
                onReorderClick = { reorderingAppPackage = showMenuForApp!!.packageName },
                onHideClick = { viewModel.setAppHidden(showMenuForApp!!.packageName, true) },
                onAddToCategoryClick = { showCategoryPickerForApp = showMenuForApp }
            )
        }

        // Alphabet filter picker overlay
        if (showAlphabetPicker) {
            AlphabetPickerOverlay(
                selectedLetter = alphabetFilter,
                onSelectLetter = { letter ->
                    alphabetFilter = letter
                    showAlphabetPicker = false
                },
                onDismiss = { showAlphabetPicker = false }
            )
        }

        // Category picker overlay
        if (showCategoryPickerForApp != null) {
            val app = showCategoryPickerForApp!!
            val state = uiState
            if (state is HomeUiState.Success) {
                CategoryPickerOverlay(
                    app = app,
                    categories = state.categoriesWithApps.keys.toList(),
                    onSelectCategory = { category ->
                        viewModel.moveAppToCategory(app.packageName, app.categoryId, category.id)
                        showCategoryPickerForApp = null
                    },
                    onDismiss = { showCategoryPickerForApp = null }
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun CategoryPickerOverlay(
    app: AppInfo,
    categories: List<com.karthicbz.klauncher.data.model.CategoryEntity>,
    onSelectCategory: (com.karthicbz.klauncher.data.model.CategoryEntity) -> Unit,
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
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
                Text(
                    text = "Move \"${app.label}\" to…",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                categories.forEach { category ->
                    Surface(
                        onClick = { onSelectCategory(category) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = ClickableSurfaceDefaults.shape(MaterialTheme.shapes.medium),
                        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.03f),
                        colors = ClickableSurfaceDefaults.colors(
                            containerColor = if (category.id == app.categoryId)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                            else MaterialTheme.colorScheme.surface,
                            focusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            focusedContentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = ClickableSurfaceDefaults.shape(MaterialTheme.shapes.medium),
                    scale = ClickableSurfaceDefaults.scale(focusedScale = 1.03f),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                    )
                ) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .fillMaxWidth()
                            .wrapContentWidth(Alignment.CenterHorizontally)
                    )
                }
        }
    }
}

private val ALPHABETS = ('A'..'Z').toList()

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
    onShowMenuForApp: (AppInfo?) -> Unit,
    weatherViewModel: com.karthicbz.klauncher.ui.home.viewmodel.HomeViewModel,
    alphabetFilter: Char?,
    onAlphabetFilterChange: (Char?) -> Unit,
    onShowAlphabetPicker: (Boolean) -> Unit
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
        val weather by weatherViewModel.weather.collectAsState(initial = null)
        HomeHeader(
            onSettingsClick = onSettingsClick,
            weather = weather
        )

        // Alphabet filter row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (alphabetFilter != null) {
                Surface(
                    onClick = { onAlphabetFilterChange(null) },
                    shape = ClickableSurfaceDefaults.shape(MaterialTheme.shapes.small),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                        focusedContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
                    )
                ) {
                    Text(
                        text = "Clear Filter: ${alphabetFilter}",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
            Surface(
                onClick = { onShowAlphabetPicker(true) },
                shape = ClickableSurfaceDefaults.shape(MaterialTheme.shapes.small),
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    focusedContainerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = if (alphabetFilter != null) "Filter: $alphabetFilter" else "A-Z",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        // Filter apps by selected alphabet
        val filteredCategories = state.categoriesWithApps.mapValues { (_, apps) ->
            if (alphabetFilter != null) {
                apps.filter { it.label.firstOrNull()?.uppercaseChar() == alphabetFilter }
            } else apps
        }

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
                            contentPadding = PaddingValues(horizontal = 12.dp),
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
            filteredCategories.entries.forEachIndexed { categoryIndex, (category, apps) ->
                    item {
                        Column(modifier = Modifier.padding(horizontal = 48.dp)) {
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 12.dp),
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

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun AlphabetPickerOverlay(
    selectedLetter: Char?,
    onSelectLetter: (Char) -> Unit,
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
                .width(520.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.large
                )
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Filter by Alphabet",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            val rows = ALPHABETS.chunked(7)
            rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                ) {
                    row.forEach { letter ->
                        Surface(
                            onClick = { onSelectLetter(letter) },
                            shape = ClickableSurfaceDefaults.shape(MaterialTheme.shapes.small),
                            colors = ClickableSurfaceDefaults.colors(
                                containerColor = if (selectedLetter == letter)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                focusedContainerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                focusedContentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(
                                text = letter.toString(),
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier
                                    .padding(12.dp)
                                    .width(32.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = ClickableSurfaceDefaults.shape(MaterialTheme.shapes.medium),
                scale = ClickableSurfaceDefaults.scale(focusedScale = 1.03f),
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                )
            ) {
                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .fillMaxWidth()
                        .wrapContentWidth(Alignment.CenterHorizontally)
                )
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
