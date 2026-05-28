package com.karthicbz.klauncher.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.karthicbz.klauncher.data.db.CategoryDao
import com.karthicbz.klauncher.data.model.CategoryEntity
import com.karthicbz.klauncher.data.model.AppInfo
import com.karthicbz.klauncher.repository.AppRepository
import com.karthicbz.klauncher.repository.ThemeRepository
import com.karthicbz.klauncher.ui.theme.ThemeConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val categoryDao: CategoryDao,
    private val themeRepository: ThemeRepository,
    private val appRepository: AppRepository
) : ViewModel() {

    val categories: StateFlow<List<CategoryEntity>> = categoryDao.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categoriesWithAllApps: StateFlow<Map<CategoryEntity, List<AppInfo>>> = appRepository.getCategoriesWithAllApps()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val currentTheme = themeRepository.currentTheme
    val builtInThemes = themeRepository.getBuiltInThemes()

    fun addCategory(name: String) {
        viewModelScope.launch {
            val maxPos = categoryDao.getMaxPosition() ?: -1
            categoryDao.insertCategory(CategoryEntity(name = name, position = maxPos + 1))
        }
    }

    fun renameCategory(category: CategoryEntity, newName: String) {
        viewModelScope.launch {
            categoryDao.updateCategory(category.copy(name = newName))
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            categoryDao.deleteCategory(category)
        }
    }

    fun reorderCategory(category: CategoryEntity, moveUp: Boolean) {
        viewModelScope.launch {
            val categoriesList = categoryDao.getAllCategories().first().toMutableList()
            val index = categoriesList.indexOfFirst { it.id == category.id }
            if (index != -1) {
                val targetIndex = if (moveUp) index - 1 else index + 1
                if (targetIndex in 0 until categoriesList.size) {
                    val temp = categoriesList[index]
                    categoriesList[index] = categoriesList[targetIndex]
                    categoriesList[targetIndex] = temp
                    
                    categoriesList.forEachIndexed { idx, cat ->
                        categoryDao.updateCategory(cat.copy(position = idx))
                    }
                }
            }
        }
    }

    fun setAppHidden(packageName: String, isHidden: Boolean) {
        viewModelScope.launch {
            appRepository.setAppVisibility(packageName, isHidden)
        }
    }

    fun selectTheme(theme: ThemeConfig) {
        themeRepository.applyTheme(theme)
    }

    fun importThemeFromJson(json: String): Result<ThemeConfig> {
        return themeRepository.importThemeFromJson(json)
    }

    fun importThemeFromUrl(url: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = themeRepository.importThemeFromUrl(url)
            if (result.isSuccess) {
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }
}
