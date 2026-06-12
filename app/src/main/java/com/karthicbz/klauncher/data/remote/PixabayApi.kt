package com.karthicbz.klauncher.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

object PixabayApi {
    private const val BASE = "https://pixabay.com/api"

    suspend fun getWallpaper(apiKey: String, category: String): String? {
        val url = URL("$BASE/?key=$apiKey&category=$category&image_type=photo&per_page=20&safesearch=true")
        val connection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = 10000
        connection.readTimeout = 10000
        connection.instanceFollowRedirects = true
        return try {
            connection.connect()
            val text = connection.inputStream.bufferedReader().readText()
            val result = json.decodeFromString<PixabayResponse>(text)
            result.hits.randomOrNull()?.largeImageURL
        } finally {
            connection.disconnect()
        }
    }

    val categories = listOf(
        "backgrounds", "fashion", "nature", "science", "education",
        "feelings", "health", "people", "religion", "places",
        "animals", "industry", "computer", "food", "sports",
        "transportation", "travel", "buildings", "business", "music"
    )
}

@Serializable
data class PixabayResponse(
    val total: Int,
    val totalHits: Int,
    val hits: List<PixabayImage>
)

@Serializable
data class PixabayImage(
    val id: Long,
    @SerialName("largeImageURL") val largeImageURL: String,
    @SerialName("webformatURL") val webformatURL: String,
    val tags: String? = null,
    val user: String? = null
)
