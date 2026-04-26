package com.bayramapuhan.phonecleaner.ui.drawer

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

data class DrawerUiState(
    val notificationsEnabled: Boolean = true,
    val passwordEnabled: Boolean = false,
    val biometricEnabled: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val language: String = "system",
    val hasPasswordSet: Boolean = false,
)

@HiltViewModel
class DrawerViewModel @Inject constructor(
    private val prefs: AppPreferences,
) : ViewModel() {
    val state: StateFlow<DrawerUiState> = combine(
        prefs.notificationsEnabled,
        prefs.passwordEnabled,
        prefs.biometricEnabled,
        prefs.themeMode,
        combine(prefs.language, prefs.passwordHash) { lang, hash -> lang to (hash != null) },
    ) { notif, pwd, bio, theme, langAndHash ->
        DrawerUiState(
            notificationsEnabled = notif,
            passwordEnabled = pwd,
            biometricEnabled = bio,
            themeMode = theme,
            language = langAndHash.first,
            hasPasswordSet = langAndHash.second,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DrawerUiState())

    fun setNotifications(enabled: Boolean) = viewModelScope.launch {
        prefs.setNotificationsEnabled(enabled)
    }

    fun setPasswordEnabled(enabled: Boolean) = viewModelScope.launch {
        prefs.setPasswordEnabled(enabled)
        if (!enabled) {
            prefs.setPasswordHash(null)
            prefs.setBiometricEnabled(false)
        }
    }

    fun setBiometric(enabled: Boolean) = viewModelScope.launch {
        prefs.setBiometricEnabled(enabled)
    }
}
