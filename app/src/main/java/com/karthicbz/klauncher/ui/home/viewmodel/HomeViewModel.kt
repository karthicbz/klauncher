package com.karthicbz.klauncher.ui.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.karthicbz.klauncher.data.remote.CurrentWeather
import com.karthicbz.klauncher.data.repository.WeatherRepository
import com.karthicbz.klauncher.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _weather = MutableStateFlow<CurrentWeather?>(null)
    val weather: StateFlow<CurrentWeather?> = _weather.asStateFlow()

    init {
        startWeatherUpdates()
    }

    private fun startWeatherUpdates() {
        viewModelScope.launch {
            combine(
                userPreferencesRepository.latitude,
                userPreferencesRepository.longitude
            ) { lat, lon ->
                lat to lon
            }.collectLatest { (lat, lon) ->
                if (lat != 0f || lon != 0f) {
                    updateWeather(lat.toDouble(), lon.toDouble())
                }
            }
        }
    }

    suspend fun updateWeather(lat: Double, lon: Double) {
        weatherRepository.getCurrentWeather(lat, lon).collect {
            _weather.value = it
        }
    }

    fun refreshWeather() {
        viewModelScope.launch {
            val lat = userPreferencesRepository.latitude.value
            val lon = userPreferencesRepository.longitude.value
            if (lat != 0f || lon != 0f) {
                updateWeather(lat.toDouble(), lon.toDouble())
            }
        }
    }
}
