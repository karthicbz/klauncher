package com.karthicbz.klauncher.ui.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun HomeHeader(onSettingsClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
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
