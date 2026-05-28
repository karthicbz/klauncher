package com.karthicbz.klauncher.data.db

import androidx.room.*
import com.karthicbz.klauncher.data.model.AppEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM apps WHERE categoryId = :categoryId ORDER BY position ASC")
    fun getAppsForCategory(categoryId: Long): Flow<List<AppEntity>>

    @Query("SELECT * FROM apps WHERE categoryId = :categoryId AND isHidden = 0 ORDER BY position ASC")
    fun getVisibleAppsForCategory(categoryId: Long): Flow<List<AppEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApps(apps: List<AppEntity>)

    @Update
    suspend fun updateApps(apps: List<AppEntity>)

    @Query("DELETE FROM apps WHERE packageName = :packageName")
    suspend fun deleteApp(packageName: String)

    @Query("DELETE FROM apps WHERE packageName = :packageName AND categoryId = :categoryId")
    suspend fun deleteAppFromCategory(packageName: String, categoryId: Long)

    @Query("UPDATE apps SET categoryId = :newCategoryId, position = :newPosition WHERE packageName = :packageName AND categoryId = :oldCategoryId")
    suspend fun moveApp(packageName: String, oldCategoryId: Long, newCategoryId: Long, newPosition: Int)

    @Query("UPDATE apps SET isHidden = :hidden WHERE packageName = :packageName")
    suspend fun setAppHidden(packageName: String, hidden: Boolean)
}
