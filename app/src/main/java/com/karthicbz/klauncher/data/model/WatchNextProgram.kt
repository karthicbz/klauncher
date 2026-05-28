package com.karthicbz.klauncher.data.model

data class WatchNextProgram(
    val id: Long,
    val packageName: String,
    val title: String?,
    val description: String?,
    val posterArtUri: String?,
    val progress: Int,
    val lastEngagementTime: Long
)
