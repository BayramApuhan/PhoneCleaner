package com.bayramapuhan.phonecleaner.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bayramapuhan.phonecleaner.data.preferences.AppPreferences
import com.bayramapuhan.phonecleaner.domain.model.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val thresholdMb: Int = 50,
    val hideSystemApps: Boolean = true,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: AppPreferences,
) : ViewModel() {
    val state: StateFlow<SettingsUiState> = combine(
        prefs.themeMode,
        prefs.largeFileThresholdMb,
        prefs.hideSystemApps,
    ) { theme, threshold, hideSystem ->
        SettingsUiState(theme, threshold, hideSystem)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun setTheme(mode: ThemeMode) = viewModelScope.launch { prefs.setThemeMode(mode) }
    fun setThreshold(mb: Int) = viewModelScope.launch { prefs.setLargeFileThresholdMb(mb) }
    fun setHideSystemApps(hide: Boolean) = viewModelScope.launch { prefs.setHideSystemApps(hide) }
}
