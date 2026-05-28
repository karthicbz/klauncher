package com.karthicbz.klauncher.repository

import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.media.tv.TvContract
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.karthicbz.klauncher.data.model.WatchNextProgram
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchNextRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Watch Next programs are available from API 26 (Android 8.0)
    // We use the system URI directly to avoid dependency issues with compat libraries
    private val watchNextUri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        TvContract.WatchNextPrograms.CONTENT_URI
    } else {
        null
    }

    fun getWatchNextPrograms(): Flow<List<WatchNextProgram>> = callbackFlow {
        val uri = watchNextUri
        if (uri == null) {
            trySend(emptyList())
            awaitClose()
            return@callbackFlow
        }

        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                trySend(queryWatchNext())
            }
        }

        try {
            context.contentResolver.registerContentObserver(uri, true, observer)
            trySend(queryWatchNext())
        } catch (e: Exception) {
            trySend(emptyList())
        }

        awaitClose {
            context.contentResolver.unregisterContentObserver(observer)
        }
    }.flowOn(Dispatchers.IO)

    private fun queryWatchNext(): List<WatchNextProgram> {
        val programs = mutableListOf<WatchNextProgram>()
        val uri = watchNextUri ?: return emptyList()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return emptyList()

        val projection = arrayOf(
            TvContract.WatchNextPrograms._ID,
            TvContract.WatchNextPrograms.COLUMN_PACKAGE_NAME,
            TvContract.WatchNextPrograms.COLUMN_TITLE,
            TvContract.WatchNextPrograms.COLUMN_SHORT_DESCRIPTION,
            TvContract.WatchNextPrograms.COLUMN_POSTER_ART_URI,
            TvContract.WatchNextPrograms.COLUMN_LAST_ENGAGEMENT_TIME_UTC_MILLIS
        )

        try {
            context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                "${TvContract.WatchNextPrograms.COLUMN_LAST_ENGAGEMENT_TIME_UTC_MILLIS} DESC"
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    programs.add(
                        WatchNextProgram(
                            id = cursor.getLong(0),
                            packageName = cursor.getString(1) ?: "",
                            title = cursor.getString(2),
                            description = cursor.getString(3),
                            posterArtUri = cursor.getString(4),
                            progress = 0,
                            lastEngagementTime = cursor.getLong(5)
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Log failure or handle gracefully
        }
        return programs
    }

    fun launchWatchNextProgram(programId: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val uri = TvContract.buildWatchNextProgramUri(programId)
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                // Fallback: search for package or log failure
            }
        }
    }
}
