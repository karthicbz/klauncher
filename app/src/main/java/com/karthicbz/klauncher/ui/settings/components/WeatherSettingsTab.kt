package com.karthicbz.klauncher.ui.settings.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import com.karthicbz.klauncher.ui.settings.SettingsViewModel

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun WeatherSettingsTab(viewModel: SettingsViewModel) {
    var lat by remember { mutableStateOf("") }
    var lon by remember { mutableStateOf("") }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Weather Settings",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Text("Enter your coordinates to enable weather display on home screen")

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = lat,
                onValueChange = { lat = it },
                label = { Text("Latitude") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = lon,
                onValueChange = { lon = it },
                label = { Text("Longitude") },
                modifier = Modifier.weight(1f)
            )
        }

        Surface(
            onClick = {
                val latVal = lat.toFloatOrNull() ?: 0f
                val lonVal = lon.toFloatOrNull() ?: 0f
                viewModel.setLocation(latVal, lonVal)
            },
            modifier = Modifier.width(120.dp)
        ) {
            Text("Save", modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
        }
    }
}
