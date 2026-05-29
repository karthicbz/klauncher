package com.karthicbz.klauncher.ui.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun HomeHeader(onSettingsClick: () -> Unit, modifier: Modifier = Modifier) {
    // Tick every second so the clock is always accurate
    var currentTime by remember { mutableStateOf(formattedTime()) }
    var currentDate by remember { mutableStateOf(formattedDate()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1_000L)
            currentTime = formattedTime()
            currentDate = formattedDate()
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Date + Time block — large for 10-foot readability
        Column {
            Text(
                text = currentTime,
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                text = currentDate,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        // Settings button — D-pad focusable
        Surface(
            onClick = onSettingsClick,
            shape = ClickableSurfaceDefaults.shape(MaterialTheme.shapes.medium),
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.08f),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "⚙  Settings",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
            )
        }
    }
}

private fun formattedTime(): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

private fun formattedDate(): String =
    SimpleDateFormat("EEE, d MMM", Locale.getDefault()).format(Date())
