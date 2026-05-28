package com.karthicbz.klauncher.ui.home

import com.karthicbz.klauncher.data.model.AppInfo
import com.karthicbz.klauncher.data.model.CategoryEntity
import com.karthicbz.klauncher.data.model.WatchNextProgram

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(
        val watchNextPrograms: List<WatchNextProgram> = emptyList(),
        val categoriesWithApps: Map<CategoryEntity, List<AppInfo>> = emptyMap()
    ) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}
