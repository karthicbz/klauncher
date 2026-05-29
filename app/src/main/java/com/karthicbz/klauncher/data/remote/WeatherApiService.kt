package com.karthicbz.klauncher.data.remote

//import com.squareup.retrofit2.http.GET
//import com.squareup.retrofit2.http.Queryimport com.squareup.retrofit2.http.GET
//import com.squareup.retrofit2.http.Query
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

@Serializable
data class WeatherResponse(
    val current_weather: CurrentWeather
)

@Serializable
data class CurrentWeather(
    val temperature: Double,
    val weathercode: Int,
    val windspeed: Double,
    val winddirection: Int,
//    val interval_start: java.util.Date? = null,
//    val interval_end: java.util.Date? = null,
    val time: String
)

interface WeatherApiService {
    @GET("v1/forecast")
    suspend fun getCurrentWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current_weather") currentOnly: Boolean = true
    ): WeatherResponse
}
