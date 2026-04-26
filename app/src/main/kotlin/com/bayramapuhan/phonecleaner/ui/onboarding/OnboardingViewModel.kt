package com.bayramapuhan.phonecleaner.ui.onboarding

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
class OnboardingViewModel @Inject constructor(
    private val prefs: AppPreferences,
) : ViewModel() {

    val completed: StateFlow<Boolean?> = prefs.onboardingCompleted
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun complete() = viewModelScope.launch { prefs.setOnboardingCompleted(true) }
}
