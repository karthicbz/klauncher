package com.karthicbz.klauncher.ui.settings.components

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme as Material3Theme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
//import androidx.compose.platform.LocalContext
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import com.karthicbz.klauncher.ui.settings.SettingsViewModel

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun WeatherSettingsTab(viewModel: SettingsViewModel) {
    val context = LocalContext.current
    val savedLat by viewModel.latitude.collectAsState()
    val savedLon by viewModel.longitude.collectAsState()
    var lat by remember(savedLat) { mutableStateOf(if (savedLat != 0f) savedLat.toString() else "") }
    var lon by remember(savedLon) { mutableStateOf(if (savedLon != 0f) savedLon.toString() else "") }

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
                textStyle = TextStyle(color = Material3Theme.colorScheme.onSurface),
                colors = TextFieldDefaults.colors(
                    unfocusedTextColor = Material3Theme.colorScheme.onSurface,
                    focusedTextColor = Material3Theme.colorScheme.onSurface,
                    unfocusedLabelColor = Material3Theme.colorScheme.onSurface.copy(alpha = 0.6f),
                    focusedLabelColor = Material3Theme.colorScheme.onSurface.copy(alpha = 0.8f),
                    cursorColor = Material3Theme.colorScheme.primary,
                    focusedIndicatorColor = Material3Theme.colorScheme.primary,
                    unfocusedIndicatorColor = Material3Theme.colorScheme.onSurface.copy(alpha = 0.3f)
                ),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = lon,
                onValueChange = { lon = it },
                label = { Text("Longitude") },
                textStyle = TextStyle(color = Material3Theme.colorScheme.onSurface),
                colors = TextFieldDefaults.colors(
                    unfocusedTextColor = Material3Theme.colorScheme.onSurface,
                    focusedTextColor = Material3Theme.colorScheme.onSurface,
                    unfocusedLabelColor = Material3Theme.colorScheme.onSurface.copy(alpha = 0.6f),
                    focusedLabelColor = Material3Theme.colorScheme.onSurface.copy(alpha = 0.8f),
                    cursorColor = Material3Theme.colorScheme.primary,
                    focusedIndicatorColor = Material3Theme.colorScheme.primary,
                    unfocusedIndicatorColor = Material3Theme.colorScheme.onSurface.copy(alpha = 0.3f)
                ),
                modifier = Modifier.weight(1f)
            )
        }

        Surface(
            onClick = {
                val latVal = lat.toFloatOrNull()
                val lonVal = lon.toFloatOrNull()
                if (latVal != null && lonVal != null) {
                    viewModel.setLocation(latVal, lonVal)
                    Toast.makeText(context, "Location saved", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Enter valid coordinates", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.width(120.dp)
        ) {
            Text("Save", modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
        }
    }
}
