package com.karthicbz.klauncher.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

private val client = OkHttpClient.Builder()
    .connectTimeout(10, TimeUnit.SECONDS)
    .readTimeout(10, TimeUnit.SECONDS)
    .followRedirects(true)
    .followSslRedirects(true)
    .build()

object PixabayApi {
    private const val BASE = "https://pixabay.com/api"

    suspend fun getWallpaper(apiKey: String, category: String): String? {
        val url = "$BASE/?key=$apiKey&category=$category&image_type=photo&per_page=20&safesearch=true"
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        val text = response.body?.string() ?: return null
        val result = json.decodeFromString<PixabayResponse>(text)
        return result.hits.randomOrNull()?.largeImageURL
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
