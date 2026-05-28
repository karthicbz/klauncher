package com.karthicbz.klauncher.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.tv.material3.*
import com.karthicbz.klauncher.ui.home.components.AppCard
import com.karthicbz.klauncher.ui.home.components.WatchNextCard
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
                    onSettingsClick = onNavigateToSettings
                )
            }
            is HomeUiState.Error -> {
                ErrorScreen(state.message)
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun HomeContent(
    state: HomeUiState.Success,
    onAppClick: (String) -> Unit,
    onProgramClick: (Long) -> Unit,
    onSettingsClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Top Header
        HomeHeader(onSettingsClick)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Watch Next Row
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

            // App Categories
            state.categoriesWithApps.forEach { (category, apps) ->
                if (apps.isNotEmpty()) {
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
                                    AppCard(
                                        app = app,
                                        onClick = { onAppClick(app.packageName) }
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

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun HomeHeader(onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Time
        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        Text(
            text = currentTime,
            style = MaterialTheme.typography.headlineMedium
        )

        // Settings Icon
        IconButton(onClick = onSettingsClick) {
            Text("Settings") 
        }
    }
}

@Composable
private fun LoadingScreen() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(48.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        repeat(3) {
            item {
                Column {
                    Box(modifier = Modifier.size(200.dp, 30.dp).surfacePlaceholder())
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(5) {
                            Box(modifier = Modifier.size(160.dp, 90.dp).surfacePlaceholder())
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun Modifier.surfacePlaceholder(): Modifier = this.then(
    Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), MaterialTheme.shapes.medium)
)

@Composable
private fun ErrorScreen(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Error: $message", color = MaterialTheme.colorScheme.error)
    }
}
