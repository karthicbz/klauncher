package com.karthicbz.klauncher.di

import android.content.Context
import androidx.room.Room
import com.karthicbz.klauncher.data.db.AppDao
import com.karthicbz.klauncher.data.db.AppDatabase
import com.karthicbz.klauncher.data.db.CategoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "klauncher.db"
        ).build()
    }

    @Provides
    fun provideAppDao(database: AppDatabase): AppDao = database.appDao()

    @Provides
    fun provideCategoryDao(database: AppDatabase): CategoryDao = database.categoryDao()
}
