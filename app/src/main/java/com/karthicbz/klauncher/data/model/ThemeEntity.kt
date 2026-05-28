package com.karthicbz.klauncher.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "themes")
data class ThemeEntity(
    @PrimaryKey val id: String,
    val name: String,
    val jsonConfig: String,
    val isCustom: Boolean = false
)
