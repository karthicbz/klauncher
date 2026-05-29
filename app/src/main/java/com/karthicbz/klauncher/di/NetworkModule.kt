package com.karthicbz.klauncher.di

import com.karthicbz.klauncher.data.remote.WeatherApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
//import com.squareup.retrofit2.Retrofit
//import com.squareup.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json {
        return Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }

    @Provides
    @Singleton
    fun provideWeatherApiService(json: Json): WeatherApiService {
        return Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(WeatherApiService::class.java)
    }
}
