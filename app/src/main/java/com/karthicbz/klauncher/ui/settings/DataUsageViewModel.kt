package com.karthicbz.klauncher.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.karthicbz.klauncher.data.model.DataUsageUiState
import com.karthicbz.klauncher.repository.DataUsageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class DataUsageViewModel @Inject constructor(
    private val repository: DataUsageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DataUsageUiState>(DataUsageUiState.Loading)
    val uiState: StateFlow<DataUsageUiState> = _uiState.asStateFlow()

    fun checkAndLoad() {
        if (!repository.isPermissionGranted()) {
            _uiState.value = DataUsageUiState.PermissionRequired
            return
        }
        load()
    }

    fun load() {
        _uiState.value = DataUsageUiState.Loading
        viewModelScope.launch {
            try {
                val current = withContext(Dispatchers.IO) {
                    repository.getCurrentWeekDailyUsage()
                }
                val previous = withContext(Dispatchers.IO) {
                    repository.getPreviousWeeksSummaries()
                }
                val prev = _uiState.value
                val selectedApp = (prev as? DataUsageUiState.Success)?.selectedAppPackage
                    ?: current.firstOrNull()?.packageName
                _uiState.value = DataUsageUiState.Success(
                    currentWeek = current,
                    previousWeeks = previous,
                    selectedAppPackage = selectedApp
                )
            } catch (e: Exception) {
                _uiState.value = DataUsageUiState.Error(e.message ?: "Failed to load data")
            }
        }
    }

    fun selectWeek(index: Int) {
        val s = _uiState.value as? DataUsageUiState.Success ?: return
        _uiState.value = s.copy(selectedWeekIndex = index)
    }

    fun selectApp(packageName: String) {
        val s = _uiState.value as? DataUsageUiState.Success ?: return
        _uiState.value = s.copy(selectedAppPackage = packageName)
    }

    fun getWeeklyTotals(packageName: String): List<Pair<String, Long>> =
        repository.getWeeklyTotalsForApp(packageName)
}
