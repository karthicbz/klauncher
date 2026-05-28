package com.karthicbz.klauncher.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "apps",
    primaryKeys = ["packageName", "categoryId"],
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("categoryId")]
)
data class AppEntity(
    val packageName: String,
    val categoryId: Long,
    val position: Int,
    val isHidden: Boolean = false
)
