package com.bayramapuhan.phonecleaner.ui.screens.recovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bayramapuhan.phonecleaner.data.preferences.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecoveryEmailViewModel @Inject constructor(
    private val prefs: AppPreferences,
) : ViewModel() {
    val email: StateFlow<String> = prefs.recoveryEmail
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    fun save(value: String) = viewModelScope.launch {
        prefs.setRecoveryEmail(value.trim())
    }
}
