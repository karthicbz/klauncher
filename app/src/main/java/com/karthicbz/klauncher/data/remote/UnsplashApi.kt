package com.karthicbz.klauncher.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.URL

private val json = Json { ignoreUnknownKeys = true }

object UnsplashApi {
    private const val BASE = "https://api.unsplash.com"

    suspend fun getTopics(accessKey: String): List<UnsplashTopic> {
        val text = URL("$BASE/topics?per_page=20").openConnection().apply {
            setRequestProperty("Authorization", "Client-ID $accessKey")
        }.let { it.getInputStream().reader().readText() }
        return json.decodeFromString<List<UnsplashTopic>>(text)
    }

    suspend fun getRandomPhoto(accessKey: String, topicId: String? = null): UnsplashPhoto? {
        val url = buildString {
            append("$BASE/photos/random?count=1")
            if (topicId != null) append("&topics=$topicId")
        }
        val text = URL(url).openConnection().apply {
            setRequestProperty("Authorization", "Client-ID $accessKey")
        }.let { it.getInputStream().reader().readText() }
        val photos = json.decodeFromString<List<UnsplashPhoto>>(text)
        return photos.firstOrNull()
    }

    suspend fun searchPhotos(accessKey: String, query: String, perPage: Int = 20): List<UnsplashPhoto> {
        val text = URL("$BASE/search/photos?query=${java.net.URLEncoder.encode(query, "UTF-8")}&per_page=$perPage").openConnection().apply {
            setRequestProperty("Authorization", "Client-ID $accessKey")
        }.let { it.getInputStream().reader().readText() }
        val result = json.decodeFromString<UnsplashSearchResult>(text)
        return result.results
    }
}

@Serializable
data class UnsplashTopic(
    val id: String,
    val title: String,
    @SerialName("cover_photo") val coverPhoto: UnsplashPhoto? = null
)

@Serializable
data class UnsplashPhoto(
    val id: String,
    val urls: UnsplashUrls,
    @SerialName("alt_description") val altDescription: String? = null,
    val user: UnsplashUser? = null
)

@Serializable
data class UnsplashUrls(
    val raw: String,
    val full: String,
    val regular: String,
    val small: String,
    val thumb: String
)

@Serializable
data class UnsplashUser(val name: String)

@Serializable
data class UnsplashSearchResult(
    val results: List<UnsplashPhoto>,
    val total: Int
)
