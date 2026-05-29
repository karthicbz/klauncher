package com.karthicbz.klauncher

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.karthicbz.klauncher.util.AppIconFetcher
import com.karthicbz.klauncher.util.AppIconKeyer
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class KLauncherApp : Application(), ImageLoaderFactory {

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(AppIconFetcher.Factory(this@KLauncherApp))
                add(AppIconKeyer())
            }
            // In-memory cache: up to 20% of available heap for app icons
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.20)
                    .build()
            }
            // Disk cache: up to 64 MB for app icons, cached between sessions
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("icon_cache"))
                    .maxSizeBytes(64L * 1024 * 1024) // 64 MB
                    .build()
            }
            // Never stall the main thread waiting for icons
            .respectCacheHeaders(false)
            .build()
    }
}
