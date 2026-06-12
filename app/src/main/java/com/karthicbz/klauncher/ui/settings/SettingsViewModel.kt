package com.karthicbz.klauncher.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.karthicbz.klauncher.data.db.CategoryDao
import com.karthicbz.klauncher.data.model.CategoryEntity
import com.karthicbz.klauncher.data.model.AppInfo
import com.karthicbz.klauncher.data.remote.BingApi
import com.karthicbz.klauncher.data.remote.PixabayApi
import com.karthicbz.klauncher.repository.AppRepository
import com.karthicbz.klauncher.repository.ThemeRepository
import com.karthicbz.klauncher.repository.UserPreferencesRepository
import com.karthicbz.klauncher.repository.WallpaperSource
import com.karthicbz.klauncher.ui.theme.ThemeConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    val wallpaperSource = userPreferencesRepository.wallpaperSource
    val pixabayCategory = userPreferencesRepository.pixabayCategory
    val pixabayCategories = PixabayApi.categories

    private val _isLoadingWallpaper = MutableStateFlow(false)
    val isLoadingWallpaper: StateFlow<Boolean> = _isLoadingWallpaper

    private val _wallpaperStatus = MutableStateFlow<String?>(null)
    val wallpaperStatus: StateFlow<String?> = _wallpaperStatus

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

    fun setWallpaperImageUrl(url: String?, source: WallpaperSource = WallpaperSource.LOCAL_IMAGE) {
        userPreferencesRepository.setWallpaperImageUrl(url, source)
    }

    fun setWallpaperSource(source: WallpaperSource) {
        userPreferencesRepository.setWallpaperSource(source)
    }

    fun setPixabayCategory(category: String) {
        userPreferencesRepository.setPixabayCategory(category)
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

    fun fetchBingWallpaper() {
        viewModelScope.launch {
            _isLoadingWallpaper.value = true
            _wallpaperStatus.value = null
            try {
                val url = withContext(Dispatchers.IO) { BingApi.getDailyWallpaper() }
                if (url != null) {
                    userPreferencesRepository.setWallpaperImageUrl(url, WallpaperSource.BING)
                    _wallpaperStatus.value = "Bing wallpaper applied!"
                } else {
                    _wallpaperStatus.value = "Failed to fetch Bing wallpaper"
                }
            } catch (e: Exception) {
                _wallpaperStatus.value = "Error: ${e.message}"
            }
            _isLoadingWallpaper.value = false
        }
    }

    fun fetchPixabayWallpaper(category: String) {
        viewModelScope.launch {
            _isLoadingWallpaper.value = true
            _wallpaperStatus.value = null
            try {
                val url = withContext(Dispatchers.IO) {
                    PixabayApi.getWallpaper(PIXABAY_API_KEY, category)
                }
                if (url != null) {
                    userPreferencesRepository.setWallpaperImageUrl(url, WallpaperSource.PIXABAY)
                    _wallpaperStatus.value = "Pixabay wallpaper applied!"
                } else {
                    _wallpaperStatus.value = "No images found for this category"
                }
            } catch (e: Exception) {
                _wallpaperStatus.value = "Error: ${e.message}"
            }
            _isLoadingWallpaper.value = false
        }
    }

    companion object {
        private const val PIXABAY_API_KEY = "PIXABAY_API_KEY_PLACEHOLDER"
    }
}
