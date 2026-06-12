package com.karthicbz.klauncher.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

enum class WallpaperSource(val key: String) {
    NONE("none"),
    SOLID_COLOR("color"),
    LOCAL_IMAGE("local"),
    BING("bing"),
    PIXABAY("pixabay")
}

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    private val _latitude = MutableStateFlow(prefs.getFloat("latitude", 0f))
    val latitude: StateFlow<Float> = _latitude.asStateFlow()

    private val _longitude = MutableStateFlow(prefs.getFloat("longitude", 0f))
    val longitude: StateFlow<Float> = _longitude.asStateFlow()

    private val _wallpaperColor = MutableStateFlow(prefs.getString("wallpaper_color", null))
    val wallpaperColor: StateFlow<String?> = _wallpaperColor.asStateFlow()

    private val _wallpaperImageUrl = MutableStateFlow(prefs.getString("wallpaper_image_url", null))
    val wallpaperImageUrl: StateFlow<String?> = _wallpaperImageUrl.asStateFlow()

    private val _wallpaperSource = MutableStateFlow(
        prefs.getString("wallpaper_source", WallpaperSource.NONE.key)?.let { key ->
            WallpaperSource.entries.firstOrNull { it.key == key }
        } ?: WallpaperSource.NONE
    )
    val wallpaperSource: StateFlow<WallpaperSource> = _wallpaperSource.asStateFlow()

    private val _pixabayCategory = MutableStateFlow(prefs.getString("pixabay_category", "nature"))
    val pixabayCategory: StateFlow<String> = _pixabayCategory.asStateFlow()

    fun setLocation(lat: Float, lon: Float) {
        _latitude.value = lat
        _longitude.value = lon
        prefs.edit().putFloat("latitude", lat).putFloat("longitude", lon).apply()
    }

    fun setWallpaperColor(hex: String?) {
        _wallpaperColor.value = hex
        _wallpaperImageUrl.value = null
        _wallpaperSource.value = if (hex != null) WallpaperSource.SOLID_COLOR else WallpaperSource.NONE
        prefs.edit()
            .putString("wallpaper_color", hex)
            .remove("wallpaper_image_url")
            .putString("wallpaper_source", _wallpaperSource.value.key)
            .apply()
    }

    fun setWallpaperImageUrl(url: String?, source: WallpaperSource = WallpaperSource.LOCAL_IMAGE) {
        _wallpaperImageUrl.value = url
        _wallpaperSource.value = if (url != null) source else WallpaperSource.NONE
        if (url != null) _wallpaperColor.value = null
        prefs.edit().putString("wallpaper_image_url", url).apply {
            if (url != null) remove("wallpaper_color")
            putString("wallpaper_source", _wallpaperSource.value.key)
            apply()
        }
    }

    fun setWallpaperSource(source: WallpaperSource) {
        _wallpaperSource.value = source
        prefs.edit().putString("wallpaper_source", source.key).apply()
        if (source == WallpaperSource.NONE) {
            _wallpaperImageUrl.value = null
            _wallpaperColor.value = null
            prefs.edit().remove("wallpaper_image_url").remove("wallpaper_color").apply()
        }
    }

    fun setPixabayCategory(category: String) {
        _pixabayCategory.value = category
        prefs.edit().putString("pixabay_category", category).apply()
    }
}
