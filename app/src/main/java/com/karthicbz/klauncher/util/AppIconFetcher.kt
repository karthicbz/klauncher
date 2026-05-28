package com.karthicbz.klauncher.util

import android.content.Context
import android.content.pm.PackageManager
import coil.ImageLoader
import coil.decode.DataSource
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.key.Keyer
import coil.request.Options
import coil.request.ImageRequest

class AppIconFetcher(
    private val context: Context,
    private val packageName: String
) : Fetcher {

    override suspend fun fetch(): FetchResult? {
        val packageManager = context.packageManager
        return try {
            val icon = packageManager.getApplicationIcon(packageName)
            DrawableResult(
                drawable = icon,
                isSampled = false,
                dataSource = DataSource.DISK
            )
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    class Factory(private val context: Context) : Fetcher.Factory<String> {
        override fun create(data: String, options: Options, imageLoader: ImageLoader): Fetcher? {
            // We only handle strings that look like package names and if they are prefixed or identified as such
            // For simplicity, let's assume if it doesn't start with http or / it's a package name in our specific context
            if (data.contains(".")) {
                return AppIconFetcher(context, data)
            }
            return null
        }
    }
}

class AppIconKeyer : Keyer<String> {
    override fun key(data: String, options: Options): String? {
        return data
    }
}
