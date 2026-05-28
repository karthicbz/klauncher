package com.karthicbz.klauncher

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
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
            .build()
    }
}
