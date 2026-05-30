package com.karthicbz.klauncher.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

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

    private val _unsplashAccessKey = MutableStateFlow(prefs.getString("unsplash_access_key", null))
    val unsplashAccessKey: StateFlow<String?> = _unsplashAccessKey.asStateFlow()

    private val _unsplashTopicId = MutableStateFlow(prefs.getString("unsplash_topic_id", null))
    val unsplashTopicId: StateFlow<String?> = _unsplashTopicId.asStateFlow()

    private val _unsplashAutoUpdate = MutableStateFlow(prefs.getBoolean("unsplash_auto_update", false))
    val unsplashAutoUpdate: StateFlow<Boolean> = _unsplashAutoUpdate.asStateFlow()

    fun setLocation(lat: Float, lon: Float) {
        _latitude.value = lat
        _longitude.value = lon
        prefs.edit().putFloat("latitude", lat).putFloat("longitude", lon).apply()
    }

    fun setWallpaperColor(hex: String?) {
        _wallpaperColor.value = hex
        _wallpaperImageUrl.value = null
        prefs.edit().putString("wallpaper_color", hex).remove("wallpaper_image_url").apply()
    }

    fun setWallpaperImageUrl(url: String?) {
        _wallpaperImageUrl.value = url
        if (url != null) _wallpaperColor.value = null
        prefs.edit().putString("wallpaper_image_url", url).apply {
            if (url != null) remove("wallpaper_color")
            apply()
        }
    }

    fun setUnsplashAccessKey(key: String?) {
        _unsplashAccessKey.value = key
        prefs.edit().putString("unsplash_access_key", key).apply()
    }

    fun setUnsplashTopicId(topicId: String?) {
        _unsplashTopicId.value = topicId
        prefs.edit().putString("unsplash_topic_id", topicId).apply()
    }

    fun setUnsplashAutoUpdate(enabled: Boolean) {
        _unsplashAutoUpdate.value = enabled
        prefs.edit().putBoolean("unsplash_auto_update", enabled).apply()
    }
}
