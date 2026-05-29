package com.karthicbz.klauncher.data.repository

import com.karthicbz.klauncher.data.remote.CurrentWeather
import com.karthicbz.klauncher.data.remote.WeatherApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor(
    private val weatherApiService: WeatherApiService
) {
    fun getCurrentWeather(lat: Double, lon: Double): Flow<CurrentWeather?> = flow {
        try {
            val response = weatherApiService.getCurrentWeather(lat, lon)
            emit(response.current_weather)
        } catch (e: Exception) {
            emit(null)
        }
    }
}
