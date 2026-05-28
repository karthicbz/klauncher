package com.karthicbz.klauncher.repository

import android.content.Context
import com.karthicbz.klauncher.ui.theme.BuiltInThemes
import com.karthicbz.klauncher.ui.theme.DefaultThemeConfig
import com.karthicbz.klauncher.ui.theme.ThemeConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
    
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
    }

    private val _currentTheme = MutableStateFlow(loadTheme())
    val currentTheme: StateFlow<ThemeConfig> = _currentTheme.asStateFlow()

    private fun loadTheme(): ThemeConfig {
        val themeJson = prefs.getString("current_theme_json", null)
        return if (themeJson != null) {
            try {
                json.decodeFromString<ThemeConfig>(themeJson)
            } catch (e: Exception) {
                DefaultThemeConfig
            }
        } else {
            DefaultThemeConfig
        }
    }

    fun applyTheme(config: ThemeConfig) {
        _currentTheme.value = config
        prefs.edit().putString("current_theme_json", json.encodeToString(config)).apply()
    }

    fun getBuiltInThemes(): List<ThemeConfig> = BuiltInThemes

    fun resetToDefault() {
        applyTheme(DefaultThemeConfig)
    }
}
