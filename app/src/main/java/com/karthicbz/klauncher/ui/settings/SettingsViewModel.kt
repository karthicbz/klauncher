package com.karthicbz.klauncher.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.karthicbz.klauncher.data.db.CategoryDao
import com.karthicbz.klauncher.data.model.CategoryEntity
import com.karthicbz.klauncher.data.model.AppInfo
import com.karthicbz.klauncher.repository.AppRepository
import com.karthicbz.klauncher.repository.ThemeRepository
import com.karthicbz.klauncher.repository.UserPreferencesRepository
import com.karthicbz.klauncher.ui.theme.ThemeConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
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
    private val appRepository: AppRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val categories: StateFlow<List<CategoryEntity>> = categoryDao.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categoriesWithAllApps: StateFlow<Map<CategoryEntity, List<AppInfo>>> = appRepository.getCategoriesWithAllApps()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val currentTheme = themeRepository.currentTheme
    val builtInThemes = themeRepository.getBuiltInThemes()
    val latitude = userPreferencesRepository.latitude
    val longitude = userPreferencesRepository.longitude
    val wallpaperColor = userPreferencesRepository.wallpaperColor
    val wallpaperImageUrl = userPreferencesRepository.wallpaperImageUrl
    val unsplashAccessKey = userPreferencesRepository.unsplashAccessKey
    val unsplashTopicId = userPreferencesRepository.unsplashTopicId
    val unsplashAutoUpdate = userPreferencesRepository.unsplashAutoUpdate

    private val _unsplashTopics = MutableStateFlow<List<com.karthicbz.klauncher.data.remote.UnsplashTopic>>(emptyList())
    val unsplashTopics: StateFlow<List<com.karthicbz.klauncher.data.remote.UnsplashTopic>> = _unsplashTopics

    private val _unsplashSearchResults = MutableStateFlow<List<com.karthicbz.klauncher.data.remote.UnsplashPhoto>>(emptyList())
    val unsplashSearchResults: StateFlow<List<com.karthicbz.klauncher.data.remote.UnsplashPhoto>> = _unsplashSearchResults

    private val _isLoadingUnsplash = MutableStateFlow(false)
    val isLoadingUnsplash: StateFlow<Boolean> = _isLoadingUnsplash

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
            if (!category.isSystem) {
                categoryDao.deleteCategory(category)
            }
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

    fun setWallpaperColor(hex: String?) {
        userPreferencesRepository.setWallpaperColor(hex)
    }

    fun setWallpaperImageUrl(url: String?) {
        userPreferencesRepository.setWallpaperImageUrl(url)
    }

    fun setUnsplashAccessKey(key: String?) {
        userPreferencesRepository.setUnsplashAccessKey(key)
    }

    fun setUnsplashTopicId(topicId: String?) {
        userPreferencesRepository.setUnsplashTopicId(topicId)
    }

    fun setUnsplashAutoUpdate(enabled: Boolean) {
        userPreferencesRepository.setUnsplashAutoUpdate(enabled)
    }

    fun fetchUnsplashTopics() {
        viewModelScope.launch {
            val key = unsplashAccessKey.value ?: return@launch
            _isLoadingUnsplash.value = true
            try {
                _unsplashTopics.value = com.karthicbz.klauncher.data.remote.UnsplashApi.getTopics(key)
            } catch (_: Exception) { }
            _isLoadingUnsplash.value = false
        }
    }

    fun fetchRandomUnsplashPhoto(topicId: String? = null) {
        viewModelScope.launch {
            val key = unsplashAccessKey.value ?: return@launch
            _isLoadingUnsplash.value = true
            try {
                val photo = com.karthicbz.klauncher.data.remote.UnsplashApi.getRandomPhoto(key, topicId)
                photo?.let { setWallpaperImageUrl(it.urls.full) }
            } catch (_: Exception) { }
            _isLoadingUnsplash.value = false
        }
    }

    fun searchUnsplash(query: String) {
        viewModelScope.launch {
            val key = unsplashAccessKey.value ?: return@launch
            if (query.isBlank()) {
                _unsplashSearchResults.value = emptyList()
                return@launch
            }
            _isLoadingUnsplash.value = true
            try {
                _unsplashSearchResults.value = com.karthicbz.klauncher.data.remote.UnsplashApi.searchPhotos(key, query)
            } catch (_: Exception) { }
            _isLoadingUnsplash.value = false
        }
    }

    fun clearUnsplashSearch() {
        _unsplashSearchResults.value = emptyList()
    }

    fun setLocation(lat: Float, lon: Float) {
        userPreferencesRepository.setLocation(lat, lon)
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
