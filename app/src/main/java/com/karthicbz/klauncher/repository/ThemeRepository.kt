package com.karthicbz.klauncher.repository

import android.content.Context
import com.karthicbz.klauncher.ui.theme.BuiltInThemes
import com.karthicbz.klauncher.ui.theme.DefaultThemeConfig
import com.karthicbz.klauncher.ui.theme.ThemeConfig
import com.karthicbz.klauncher.ui.theme.validate
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URL
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

    fun importThemeFromJson(jsonString: String): Result<ThemeConfig> {
        return try {
            val config = json.decodeFromString<ThemeConfig>(jsonString)
            val validated = config.validate()
            applyTheme(validated)
            Result.success(validated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importThemeFromUrl(urlString: String): Result<ThemeConfig> = withContext(Dispatchers.IO) {
        try {
            val jsonString = URL(urlString).readText()
            val config = json.decodeFromString<ThemeConfig>(jsonString)
            val validated = config.validate()
            applyTheme(validated)
            Result.success(validated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
