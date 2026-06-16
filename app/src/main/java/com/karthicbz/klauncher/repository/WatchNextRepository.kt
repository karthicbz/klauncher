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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchNextRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _programs = MutableStateFlow<List<WatchNextProgram>>(emptyList())

    private val watchNextUri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        TvContract.WatchNextPrograms.CONTENT_URI
    } else {
        null
    }

    init {
        val uri = watchNextUri
        if (uri != null) {
            refresh()
            val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean) {
                    refresh()
                }
            }
            try {
                context.contentResolver.registerContentObserver(uri, true, observer)
            } catch (_: Exception) {
            }
        }
    }

    fun getWatchNextPrograms(): Flow<List<WatchNextProgram>> = _programs.onStart {
        refresh()
    }

    fun refresh() {
        scope.launch {
            _programs.value = queryWatchNext()
        }
    }

    @Suppress("DEPRECATION")
    private fun queryWatchNext(): List<WatchNextProgram> {
        val programs = mutableListOf<WatchNextProgram>()
        val uri = watchNextUri ?: return emptyList()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return emptyList()

        val projection = buildList {
            add(TvContract.WatchNextPrograms._ID)
            add(TvContract.WatchNextPrograms.COLUMN_PACKAGE_NAME)
            add(TvContract.WatchNextPrograms.COLUMN_TITLE)
            add(TvContract.WatchNextPrograms.COLUMN_SHORT_DESCRIPTION)
            add(TvContract.WatchNextPrograms.COLUMN_POSTER_ART_URI)
            // COLUMN_LAST_ENGAGEMENT_TIME_UTC_MILLIS was added in API 28
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                add(TvContract.WatchNextPrograms.COLUMN_LAST_ENGAGEMENT_TIME_UTC_MILLIS)
            }
            add(TvContract.WatchNextPrograms.COLUMN_LAST_PLAYBACK_POSITION_MILLIS)
            add(TvContract.WatchNextPrograms.COLUMN_DURATION_MILLIS)
        }.toTypedArray()

        try {
            val sortOrder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                "${TvContract.WatchNextPrograms.COLUMN_LAST_ENGAGEMENT_TIME_UTC_MILLIS} DESC"
            } else null
            context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                val idIdx = cursor.getColumnIndexOrThrow(TvContract.WatchNextPrograms._ID)
                val pkgIdx = cursor.getColumnIndexOrThrow(TvContract.WatchNextPrograms.COLUMN_PACKAGE_NAME)
                val titleIdx = cursor.getColumnIndexOrThrow(TvContract.WatchNextPrograms.COLUMN_TITLE)
                val descIdx = cursor.getColumnIndexOrThrow(TvContract.WatchNextPrograms.COLUMN_SHORT_DESCRIPTION)
                val posterIdx = cursor.getColumnIndexOrThrow(TvContract.WatchNextPrograms.COLUMN_POSTER_ART_URI)
                // COLUMN_LAST_ENGAGEMENT_TIME_UTC_MILLIS added in API 28; fall back to -1 on API 26-27
                val timeIdx = cursor.getColumnIndex(TvContract.WatchNextPrograms.COLUMN_LAST_ENGAGEMENT_TIME_UTC_MILLIS)
                val posIdx = cursor.getColumnIndexOrThrow(TvContract.WatchNextPrograms.COLUMN_LAST_PLAYBACK_POSITION_MILLIS)
                val durIdx = cursor.getColumnIndexOrThrow(TvContract.WatchNextPrograms.COLUMN_DURATION_MILLIS)

                while (cursor.moveToNext()) {
                    val positionMs = cursor.getLong(posIdx)
                    val durationMs = cursor.getLong(durIdx)
                    val progress = if (durationMs > 0L) {
                        ((positionMs.toFloat() / durationMs) * 100).toInt().coerceIn(0, 100)
                    } else 0

                    programs.add(
                        WatchNextProgram(
                            id = cursor.getLong(idIdx),
                            packageName = cursor.getString(pkgIdx) ?: "",
                            title = cursor.getString(titleIdx),
                            description = cursor.getString(descIdx),
                            posterArtUri = cursor.getString(posterIdx),
                            progress = progress,
                            lastEngagementTime = if (timeIdx >= 0) cursor.getLong(timeIdx) else 0L
                        )
                    )
                }
            }
        } catch (_: Exception) {
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
            } catch (_: Exception) {
            }
        }
    }
}
