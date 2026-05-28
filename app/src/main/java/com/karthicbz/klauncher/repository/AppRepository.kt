package com.karthicbz.klauncher.repository

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.karthicbz.klauncher.data.db.AppDao
import com.karthicbz.klauncher.data.db.CategoryDao
import com.karthicbz.klauncher.data.model.AppEntity
import com.karthicbz.klauncher.data.model.AppInfo
import com.karthicbz.klauncher.data.model.CategoryEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appDao: AppDao,
    private val categoryDao: CategoryDao
) {
    private val packageManager: PackageManager = context.packageManager

    fun getCategoriesWithApps(): Flow<Map<CategoryEntity, List<AppInfo>>> {
        return categoryDao.getAllCategories().flatMapLatest { categories ->
            if (categories.isEmpty()) return@flatMapLatest flowOf(emptyMap())
            
            val flows = categories.map { category ->
                appDao.getAppsForCategory(category.id).map { entities ->
                    category to entities.mapNotNull { entity ->
                        getAppInfo(entity.packageName, entity.categoryId)
                    }
                }
            }
            
            combine(flows) { it.toMap() }
        }
    }

    suspend fun refreshApps() = withContext(Dispatchers.IO) {
        val installedApps = getInstalledTvApps()
        var categories = categoryDao.getAllCategories().first()
        
        if (categories.isEmpty()) {
            val defaultId = categoryDao.insertCategory(
                CategoryEntity(name = "Apps", position = 0, isSystem = true)
            )
            categories = listOf(CategoryEntity(id = defaultId, name = "Apps", position = 0, isSystem = true))
        }

        val defaultCategoryId = categories.first().id
        val existingApps = categories.flatMap { appDao.getAppsForCategory(it.id).first() }
        val existingPackageNames = existingApps.map { it.packageName }.toSet()
        val installedPackageNames = installedApps.map { it.packageName }.toSet()

        // Remove uninstalled apps
        existingApps.forEach { app ->
            if (app.packageName !in installedPackageNames) {
                appDao.deleteApp(app.packageName)
            }
        }

        // Add new apps to default category
        val newApps = installedApps.filter { it.packageName !in existingPackageNames }
        if (newApps.isNotEmpty()) {
            val maxPos = (existingApps.filter { it.categoryId == defaultCategoryId }.maxOfOrNull { it.position } ?: -1) + 1
            val appEntities = newApps.mapIndexed { index, info ->
                AppEntity(info.packageName, defaultCategoryId, maxPos + index)
            }
            appDao.insertApps(appEntities)
        }
    }

    private fun getInstalledTvApps(): List<AppInfo> {
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        }
        val resolveInfos = packageManager.queryIntentActivities(intent, 0)
        
        // Also get normal launcher apps for devices without Leanback support or for mixed apps
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val mainResolveInfos = packageManager.queryIntentActivities(mainIntent, 0)
        
        val allApps = (resolveInfos + mainResolveInfos).distinctBy { it.activityInfo.packageName }
        
        return allApps.map {
            AppInfo(
                packageName = it.activityInfo.packageName,
                label = it.loadLabel(packageManager).toString(),
                categoryId = 0
            )
        }.filter { it.packageName != context.packageName }
    }

    private fun getAppInfo(packageName: String, categoryId: Long): AppInfo? {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            AppInfo(
                packageName = packageName,
                label = packageManager.getApplicationLabel(appInfo).toString(),
                categoryId = categoryId
            )
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    fun launchApp(packageName: String) {
        val intent = packageManager.getLeanbackLaunchIntentForPackage(packageName)
            ?: packageManager.getLaunchIntentForPackage(packageName)
        intent?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(it)
        }
    }
}
