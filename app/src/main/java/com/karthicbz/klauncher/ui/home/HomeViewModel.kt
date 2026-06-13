package com.karthicbz.klauncher.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.karthicbz.klauncher.repository.AppRepository
import com.karthicbz.klauncher.repository.WatchNextRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val appRepository: AppRepository,
    private val watchNextRepository: WatchNextRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Tracks whether the first PackageManager scan has finished.
    // Without this flag, an empty DB would show a spinner forever.
    private val _refreshDone = MutableStateFlow(false)

    init {
        observeData()
        refreshApps()
    }

    private fun observeData() {
        combine(
            appRepository.getCategoriesWithApps(),
            watchNextRepository.getWatchNextPrograms(),
            _refreshDone
        ) { categoriesWithApps, watchNextPrograms, refreshDone ->
            when {
                // Still scanning — show skeleton
                !refreshDone -> HomeUiState.Loading
                // Scan done, emit success even if lists are empty (user has no visible apps)
                else -> HomeUiState.Success(
                    watchNextPrograms = watchNextPrograms,
                    categoriesWithApps = categoriesWithApps
                )
            }
        }.onEach { state ->
            _uiState.value = state
        }.catch { e ->
            _uiState.value = HomeUiState.Error(e.message ?: "Unknown error")
        }.launchIn(viewModelScope)
    }

    fun refreshApps() {
        viewModelScope.launch {
            try {
                appRepository.refreshApps()
            } finally {
                // Mark refresh done whether it succeeded or failed,
                // so the UI never gets stuck on the loading skeleton.
                _refreshDone.value = true
            }
        }
    }

    fun launchApp(packageName: String) {
        appRepository.launchApp(packageName)
    }

    fun refreshWatchNext() {
        watchNextRepository.refresh()
    }

    fun launchWatchNextProgram(programId: Long) {
        watchNextRepository.launchWatchNextProgram(programId)
    }

    fun setAppHidden(packageName: String, isHidden: Boolean) {
        viewModelScope.launch {
            appRepository.setAppVisibility(packageName, isHidden)
        }
    }

    fun reorderApp(categoryId: Long, packageName: String, fromPosition: Int, toPosition: Int) {
        viewModelScope.launch {
            appRepository.reorderApp(categoryId, packageName, fromPosition, toPosition)
        }
    }

    fun moveAppToCategory(packageName: String, oldCategoryId: Long, newCategoryId: Long) {
        viewModelScope.launch {
            appRepository.moveAppToCategory(packageName, oldCategoryId, newCategoryId)
        }
    }
}
