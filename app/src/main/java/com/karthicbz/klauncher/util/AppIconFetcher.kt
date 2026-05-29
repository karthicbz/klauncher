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

/**
 * Regex that matches a valid Android package name (e.g. "com.example.app").
 * Used to avoid the Coil fetcher intercepting actual HTTP/file URLs.
 */
private val PACKAGE_NAME_REGEX = "^[a-zA-Z][a-zA-Z0-9_]*(\\.[a-zA-Z][a-zA-Z0-9_]*)+$".toRegex()

private fun String.isPackageName(): Boolean =
    !startsWith("http") && !startsWith("/") && PACKAGE_NAME_REGEX.matches(this)

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
            // Only handle strings that look like valid Android package names
            return if (data.isPackageName()) AppIconFetcher(context, data) else null
        }
    }
}

class AppIconKeyer : Keyer<String> {
    override fun key(data: String, options: Options): String? {
        // Only produce a cache key for package name strings, not URLs
        return if (data.isPackageName()) "pkg:$data" else null
    }
}
