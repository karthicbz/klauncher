package com.karthicbz.klauncher.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.*
import com.karthicbz.klauncher.ui.settings.components.*

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val categories by viewModel.categories.collectAsState()
    val categoriesWithAllApps by viewModel.categoriesWithAllApps.collectAsState()
    val currentTheme by viewModel.currentTheme.collectAsState()
    val builtInThemes = viewModel.builtInThemes

    var selectedTab by remember { mutableStateOf(0) }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(48.dp),
        horizontalArrangement = Arrangement.spacedBy(48.dp)
    ) {
        // Left Column: Tab Menu
        Column(
            modifier = Modifier
                .width(260.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            val tabs = listOf(
                "Categories",
                "App Visibility",
                "Themes & Styles",
                "About & Sideload"
            )

            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTab == index
                Surface(
                    onClick = { selectedTab = index },
                    scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        focusedContentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = ClickableSurfaceDefaults.shape(MaterialTheme.shapes.medium),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back to Home")
            }
        }

        // Right Column: Detail Content Box
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), MaterialTheme.shapes.large)
                .padding(32.dp)
        ) {
            when (selectedTab) {
                0 -> CategoriesTab(categories, viewModel)
                1 -> AppVisibilityTab(categoriesWithAllApps, viewModel)
                2 -> ThemesTab(builtInThemes, currentTheme, viewModel)
                3 -> AboutTab()
            }
        }
    }
}
