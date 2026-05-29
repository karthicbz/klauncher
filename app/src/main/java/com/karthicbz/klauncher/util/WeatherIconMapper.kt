package com.karthicbz.klauncher.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

object WeatherIconMapper {
    fun getEmojiForCode(code: Int): String {
        return when (code) {
            0 -> "☀️"
            1, 2 -> "🌤️"
            3 -> "☁️"
            45, 48 -> "🌫️"
            51, 53, 55 -> "🌦️"
            61, 63, 65 -> "🌧️"
            71, 73, 75 -> "🌨️"
            80, 81, 82 -> "🌦️"
            95, 96, 99 -> "⛈️"
            else -> "☁️"
        }
    }
}
