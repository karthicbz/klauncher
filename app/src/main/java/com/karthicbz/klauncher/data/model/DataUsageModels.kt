package com.karthicbz.klauncher.data.model

data class DayUsage(
    val label: String,
    val bytes: Long,
    val isToday: Boolean = false
)

data class AppDailyUsage(
    val packageName: String,
    val appName: String,
    val weekTotalBytes: Long,
    val dailyBytes: List<DayUsage>
)

data class AppWeekTotal(
    val packageName: String,
    val appName: String,
    val totalBytes: Long
)

data class WeekSummary(
    val label: String,
    val startMs: Long,
    val endMs: Long,
    val apps: List<AppWeekTotal>
)

sealed class DataUsageUiState {
    object PermissionRequired : DataUsageUiState()
    object Loading : DataUsageUiState()
    data class Success(
        val currentWeek: List<AppDailyUsage>,
        val previousWeeks: List<WeekSummary>,
        val selectedWeekIndex: Int = 0,
        val selectedAppPackage: String? = null
    ) : DataUsageUiState()
    data class Error(val message: String) : DataUsageUiState()
}
