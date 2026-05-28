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

    init {
        observeData()
        refreshApps()
    }

    private fun observeData() {
        combine(
            appRepository.getCategoriesWithApps(),
            watchNextRepository.getWatchNextPrograms()
        ) { categoriesWithApps, watchNextPrograms ->
            if (categoriesWithApps.isEmpty()) {
                HomeUiState.Loading
            } else {
                HomeUiState.Success(
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
            appRepository.refreshApps()
        }
    }

    fun launchApp(packageName: String) {
        appRepository.launchApp(packageName)
    }

    fun launchWatchNextProgram(programId: Long) {
        watchNextRepository.launchWatchNextProgram(programId)
    }
}
