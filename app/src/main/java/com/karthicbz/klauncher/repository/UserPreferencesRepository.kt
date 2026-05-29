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

    fun setLocation(lat: Float, lon: Float) {
        _latitude.value = lat
        _longitude.value = lon
        prefs.edit().putFloat("latitude", lat).putFloat("longitude", lon).apply()
    }
}
