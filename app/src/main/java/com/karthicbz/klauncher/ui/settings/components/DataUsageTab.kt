package com.karthicbz.klauncher.ui.settings.components

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.*
import com.karthicbz.klauncher.data.model.AppDailyUsage
import com.karthicbz.klauncher.data.model.AppWeekTotal
import com.karthicbz.klauncher.data.model.DataUsageUiState
import com.karthicbz.klauncher.data.model.DayUsage
import com.karthicbz.klauncher.ui.settings.DataUsageViewModel

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun DataUsageTab(
    viewModel: DataUsageViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.checkAndLoad()
    }

    when (val state = uiState) {
        DataUsageUiState.PermissionRequired -> PermissionGate(
            onGrantClick = {
                context.startActivity(
                    Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                )
            },
            onRefreshClick = { viewModel.checkAndLoad() }
        )

        DataUsageUiState.Loading -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Loading data usage…", style = MaterialTheme.typography.bodyLarge)
        }

        is DataUsageUiState.Error -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Error: ${state.message}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        is DataUsageUiState.Success -> DataUsageContent(state = state, viewModel = viewModel)
    }
}

// ── Permission gate ────────────────────────────────────────────────────────────

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun PermissionGate(onGrantClick: () -> Unit, onRefreshClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Usage Access Required",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "To show per-app data usage, grant Usage Access permission in system settings.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 480.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Surface(
                onClick = onGrantClick,
                shape = ClickableSurfaceDefaults.shape(MaterialTheme.shapes.medium),
                scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    focusedContainerColor = MaterialTheme.colorScheme.primary,
                    focusedContentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    "Open Usage Access Settings",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )
            }
            Surface(
                onClick = onRefreshClick,
                shape = ClickableSurfaceDefaults.shape(MaterialTheme.shapes.medium),
                scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    focusedContainerColor = MaterialTheme.colorScheme.primary,
                    focusedContentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    "I've Granted It — Refresh",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )
            }
        }
    }
}

// ── Main content ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun DataUsageContent(
    state: DataUsageUiState.Success,
    viewModel: DataUsageViewModel
) {
    val weekTabs = buildList {
        add("This Week")
        state.previousWeeks.forEach { add(it.label) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Week selector
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            weekTabs.forEachIndexed { index, label ->
                val isSelected = state.selectedWeekIndex == index
                Surface(
                    onClick = { viewModel.selectWeek(index) },
                    shape = ClickableSurfaceDefaults.shape(MaterialTheme.shapes.small),
                    scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = if (isSelected)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)
                        else MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        focusedContainerColor = MaterialTheme.colorScheme.primary,
                        focusedContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // Two-panel layout: app list (left) + chart (right)
        Row(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f, fill = false),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // App list
            Box(modifier = Modifier.width(300.dp).fillMaxHeight()) {
                if (state.selectedWeekIndex == 0) {
                    AppListCurrentWeek(
                        apps = state.currentWeek,
                        selectedPackage = state.selectedAppPackage,
                        onSelectApp = { viewModel.selectApp(it) }
                    )
                } else {
                    val week = state.previousWeeks.getOrNull(state.selectedWeekIndex - 1)
                    AppListPreviousWeek(
                        apps = week?.apps.orEmpty(),
                        selectedPackage = state.selectedAppPackage,
                        onSelectApp = { viewModel.selectApp(it) }
                    )
                }
            }

            // Chart panel
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.shapes.medium
                    )
                    .padding(20.dp)
            ) {
                val selectedPkg = state.selectedAppPackage
                if (selectedPkg == null) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "Select an app to view its chart",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                } else if (state.selectedWeekIndex == 0) {
                    val app = state.currentWeek.firstOrNull { it.packageName == selectedPkg }
                    if (app != null) {
                        DailyBarChart(appName = app.appName, days = app.dailyBytes)
                    }
                } else {
                    val weeklyTotals = viewModel.getWeeklyTotals(selectedPkg)
                    val appName = state.previousWeeks
                        .flatMap { it.apps }
                        .firstOrNull { it.packageName == selectedPkg }?.appName
                        ?: state.currentWeek.firstOrNull { it.packageName == selectedPkg }?.appName
                        ?: selectedPkg
                    WeeklyComparisonChart(appName = appName, weeks = weeklyTotals)
                }
            }
        }
    }
}

// ── App list composables ───────────────────────────────────────────────────────

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun AppListCurrentWeek(
    apps: List<AppDailyUsage>,
    selectedPackage: String?,
    onSelectApp: (String) -> Unit
) {
    if (apps.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "No data for this week",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        items(apps, key = { it.packageName }) { app ->
            AppUsageRow(
                name = app.appName,
                bytes = app.weekTotalBytes,
                isSelected = app.packageName == selectedPackage,
                onClick = { onSelectApp(app.packageName) }
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun AppListPreviousWeek(
    apps: List<AppWeekTotal>,
    selectedPackage: String?,
    onSelectApp: (String) -> Unit
) {
    if (apps.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "No data for this period",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        items(apps, key = { it.packageName }) { app ->
            AppUsageRow(
                name = app.appName,
                bytes = app.totalBytes,
                isSelected = app.packageName == selectedPackage,
                onClick = { onSelectApp(app.packageName) }
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun AppUsageRow(
    name: String,
    bytes: Long,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(MaterialTheme.shapes.small),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.03f),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
            else Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            focusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
            focusedContentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = formatBytes(bytes),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// ── Bar charts ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun DailyBarChart(appName: String, days: List<DayUsage>) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = appName,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "Daily usage — this week",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        BarChart(
            bars = days.map { day ->
                BarEntry(
                    label = day.label,
                    bytes = day.bytes,
                    highlight = day.isToday
                )
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun WeeklyComparisonChart(appName: String, weeks: List<Pair<String, Long>>) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = appName,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "Weekly comparison — last 4 weeks",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        BarChart(
            bars = weeks.map { (label, bytes) ->
                BarEntry(label = label, bytes = bytes, highlight = label == "This Week")
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

// ── Generic bar chart ──────────────────────────────────────────────────────────

private data class BarEntry(val label: String, val bytes: Long, val highlight: Boolean = false)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun BarChart(bars: List<BarEntry>, modifier: Modifier = Modifier) {
    val maxBytes = bars.maxOfOrNull { it.bytes }.takeIf { it != null && it > 0 } ?: 1L
    val barColor = MaterialTheme.colorScheme.primary
    val dimColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
    val labelColor = MaterialTheme.colorScheme.onSurface
    val zeroColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)

    BoxWithConstraints(modifier = modifier) {
        val chartHeight = maxHeight - 56.dp  // reserve space for labels + values

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            bars.forEach { bar ->
                val fraction = if (maxBytes > 0) bar.bytes.toFloat() / maxBytes else 0f
                val barHeight = (chartHeight * fraction).coerceAtLeast(2.dp)
                val color = when {
                    bar.bytes == 0L -> zeroColor
                    bar.highlight  -> barColor
                    else           -> dimColor
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.weight(1f)
                ) {
                    // Value label above bar
                    Text(
                        text = if (bar.bytes > 0) formatBytes(bar.bytes) else "—",
                        style = MaterialTheme.typography.labelSmall,
                        color = labelColor.copy(alpha = 0.75f),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    // Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.65f)
                            .height(barHeight)
                            .background(
                                color = color,
                                shape = MaterialTheme.shapes.small
                            )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    // Day/week label
                    Text(
                        text = bar.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (bar.highlight) barColor else labelColor.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// ── Utilities ──────────────────────────────────────────────────────────────────

internal fun formatBytes(bytes: Long): String = when {
    bytes <= 0 -> "0 B"
    bytes < 1_024 -> "$bytes B"
    bytes < 1_024 * 1_024 -> "${bytes / 1_024} KB"
    bytes < 1_024L * 1_024 * 1_024 -> "${"%.1f".format(bytes / (1_024.0 * 1_024))} MB"
    else -> "${"%.2f".format(bytes / (1_024.0 * 1_024 * 1_024))} GB"
}
