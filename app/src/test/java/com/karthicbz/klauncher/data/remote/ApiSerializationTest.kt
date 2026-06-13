package com.karthicbz.klauncher.data.remote

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ApiSerializationTest {

    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    @Test
    fun bingResponse_parsesFullResponse() {
        val raw = """
        {
            "images": [{
                "url": "/th?id=OHR.Sunrise_ROW1234567890_1920x1080.jpg",
                "title": "Sunrise",
                "copyright": "Sunrise (© Example)"
            }]
        }
        """.trimIndent()

        val result = json.decodeFromString<BingResponse>(raw)
        assertEquals(1, result.images.size)
        assertEquals("/th?id=OHR.Sunrise_ROW1234567890_1920x1080.jpg", result.images[0].url)
        assertEquals("Sunrise", result.images[0].title)
        assertEquals("Sunrise (© Example)", result.images[0].copyright)
    }

    @Test
    fun bingResponse_handlesEmptyImages() {
        val raw = """{"images": []}""".trimIndent()
        val result = json.decodeFromString<BingResponse>(raw)
        assertEquals(0, result.images.size)
    }

    @Test
    fun bingResponse_handlesMissingOptionalFields() {
        val raw = """{"images": [{"url": "/photo.jpg"}]}""".trimIndent()
        val result = json.decodeFromString<BingResponse>(raw)
        assertEquals(1, result.images.size)
        assertNotNull(result.images[0].url)
    }

    @Test
    fun pixabayResponse_parsesFullResponse() {
        val raw = """
        {
            "total": 100,
            "totalHits": 50,
            "hits": [
                {
                    "id": 12345,
                    "largeImageURL": "https://pixabay.com/large/photo.jpg",
                    "webformatURL": "https://pixabay.com/web/photo.jpg",
                    "tags": "nature, forest",
                    "user": "photographer"
                }
            ]
        }
        """.trimIndent()

        val result = json.decodeFromString<PixabayResponse>(raw)
        assertEquals(100, result.total)
        assertEquals(50, result.totalHits)
        assertEquals(1, result.hits.size)
        assertEquals("https://pixabay.com/large/photo.jpg", result.hits[0].largeImageURL)
        assertEquals("https://pixabay.com/web/photo.jpg", result.hits[0].webformatURL)
        assertEquals("nature, forest", result.hits[0].tags)
        assertEquals("photographer", result.hits[0].user)
    }

    @Test
    fun pixabayResponse_handlesEmptyHits() {
        val raw = """{"total": 0, "totalHits": 0, "hits": []}""".trimIndent()
        val result = json.decodeFromString<PixabayResponse>(raw)
        assertEquals(0, result.hits.size)
    }

    @Test
    fun pixabayResponse_handlesMissingOptionalFields() {
        val raw = """
        {
            "total": 1,
            "totalHits": 1,
            "hits": [{"id": 1, "largeImageURL": "https://example.com/img.jpg", "webformatURL": "https://example.com/img_small.jpg"}]
        }
        """.trimIndent()

        val result = json.decodeFromString<PixabayResponse>(raw)
        assertEquals(1, result.hits.size)
        assertEquals("https://example.com/img.jpg", result.hits[0].largeImageURL)
    }

    @Test
    fun pixabayCategories_containsExpectedValues() {
        assert(PixabayApi.categories.contains("nature"))
        assert(PixabayApi.categories.contains("backgrounds"))
        assert(PixabayApi.categories.contains("animals"))
        assertEquals(20, PixabayApi.categories.size)
    }

    @Test
    fun bingWallpaperUrl_constructsCorrectly() {
        val raw = """{"images": [{"url": "/th?id=OHR.Test_1920x1080.jpg"}]}""".trimIndent()
        val result = json.decodeFromString<BingResponse>(raw)
        val urlPath = result.images.firstOrNull()?.url ?: return
        val fullUrl = "https://www.bing.com$urlPath"
        assertEquals("https://www.bing.com/th?id=OHR.Test_1920x1080.jpg", fullUrl)
    }
}
