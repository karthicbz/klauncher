package com.karthicbz.klauncher.data.remote

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

object BingApi {
    private const val BING_URL = "https://www.bing.com/HPImageArchive.aspx?format=js&idx=0&n=1&mkt=en-US"

    suspend fun getDailyWallpaper(): String? {
        val connection = URL(BING_URL).openConnection() as HttpURLConnection
        connection.connectTimeout = 10000
        connection.readTimeout = 10000
        connection.instanceFollowRedirects = true
        return try {
            connection.connect()
            val text = connection.inputStream.bufferedReader().readText()
            val result = json.decodeFromString<BingResponse>(text)
            val urlPath = result.images.firstOrNull()?.url ?: return null
            "https://www.bing.com$urlPath"
        } finally {
            connection.disconnect()
        }
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
