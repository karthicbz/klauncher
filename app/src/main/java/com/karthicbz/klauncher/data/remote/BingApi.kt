package com.karthicbz.klauncher.data.remote

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

object BingApi {
    private const val BING_URL = "https://www.bing.com/HPImageArchive.aspx?format=js&idx=0&n=1&mkt=en-US"

    suspend fun getDailyWallpaper(): String? {
        val request = Request.Builder().url(BING_URL).build()
        val response = client.newCall(request).execute()
        val text = response.body?.string() ?: return null
        val result = json.decodeFromString<BingResponse>(text)
        val urlPath = result.images.firstOrNull()?.url ?: return null
        return "https://www.bing.com$urlPath"
    }
}

@Serializable
data class BingResponse(
    val images: List<BingImage>
)

@Serializable
data class BingImage(
    val url: String,
    val title: String? = null,
    val copyright: String? = null
)
