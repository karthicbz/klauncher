package com.karthicbz.klauncher.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.karthicbz.klauncher.data.db.CategoryDao
import com.karthicbz.klauncher.data.model.CategoryEntity
import com.karthicbz.klauncher.repository.ThemeRepository
import com.karthicbz.klauncher.ui.theme.ThemeConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val categoryDao: CategoryDao,
    private val themeRepository: ThemeRepository
) : ViewModel() {

    val categories: StateFlow<List<CategoryEntity>> = categoryDao.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentTheme = themeRepository.currentTheme
    val builtInThemes = themeRepository.getBuiltInThemes()

    fun addCategory(name: String) {
        viewModelScope.launch {
            val maxPos = categoryDao.getMaxPosition() ?: -1
            categoryDao.insertCategory(CategoryEntity(name = name, position = maxPos + 1))
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            categoryDao.deleteCategory(category)
        }
    }

    fun selectTheme(theme: ThemeConfig) {
        themeRepository.applyTheme(theme)
    }

    fun updateThemeFromJson(json: String) {
        // Implement parsing if needed, or rely on ThemeRepository.applyTheme(String) 
        // if we add that back for custom JSON imports.
    }
}
