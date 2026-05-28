package com.karthicbz.klauncher.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.karthicbz.klauncher.data.model.AppEntity
import com.karthicbz.klauncher.data.model.CategoryEntity
import com.karthicbz.klauncher.data.model.ThemeEntity

@Database(
    entities = [AppEntity::class, CategoryEntity::class, ThemeEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
    abstract fun categoryDao(): CategoryDao
}
