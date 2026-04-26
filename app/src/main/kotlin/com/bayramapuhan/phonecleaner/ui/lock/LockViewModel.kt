package com.bayramapuhan.phonecleaner.ui.lock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bayramapuhan.phonecleaner.data.preferences.AppPreferences
import com.bayramapuhan.phonecleaner.util.PasswordHasher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class LockUiState(
    val initializing: Boolean = true,
    val lockRequired: Boolean = false,
    val biometricEnabled: Boolean = false,
)

@HiltViewModel
class LockViewModel @Inject constructor(
    private val prefs: AppPreferences,
) : ViewModel() {

    val state: StateFlow<LockUiState> = combine(
        prefs.passwordEnabled,
        prefs.passwordHash,
        prefs.biometricEnabled,
    ) { enabled, hash, bio ->
        val lockRequired = enabled && hash != null
        LockUiState(
            initializing = false,
            lockRequired = lockRequired,
            biometricEnabled = lockRequired && bio,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, LockUiState())

    suspend fun verify(password: String): Boolean {
        val hash = prefs.passwordHash.first() ?: return false
        return PasswordHasher.verify(password.toCharArray(), hash)
    }
}
