package com.bayramapuhan.phonecleaner.ui.screens.password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bayramapuhan.phonecleaner.data.preferences.AppPreferences
import com.bayramapuhan.phonecleaner.util.PasswordHasher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChangePasswordState(
    val hasExistingPassword: Boolean = false,
    val saving: Boolean = false,
)

sealed interface ChangePasswordEvent {
    data object Saved : ChangePasswordEvent
    data class Error(val reason: Reason) : ChangePasswordEvent
    enum class Reason { CURRENT_WRONG, MISMATCH, TOO_SHORT }
}

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val prefs: AppPreferences,
) : ViewModel() {

    val hasExistingPassword: StateFlow<Boolean> = prefs.passwordHash
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _events = MutableStateFlow<ChangePasswordEvent?>(null)
    val events: StateFlow<ChangePasswordEvent?> = _events.asStateFlow()

    fun consumeEvent() { _events.value = null }

    fun submit(current: String, new: String, confirm: String) = viewModelScope.launch {
        if (new.length < 4) {
            _events.value = ChangePasswordEvent.Error(ChangePasswordEvent.Reason.TOO_SHORT)
            return@launch
        }
        if (new != confirm) {
            _events.value = ChangePasswordEvent.Error(ChangePasswordEvent.Reason.MISMATCH)
            return@launch
        }
        val existing = prefs.passwordHash.first()
        if (existing != null) {
            val ok = PasswordHasher.verify(current.toCharArray(), existing)
            if (!ok) {
                _events.value = ChangePasswordEvent.Error(ChangePasswordEvent.Reason.CURRENT_WRONG)
                return@launch
            }
        }
        val newHash = PasswordHasher.hash(new.toCharArray())
        prefs.setPasswordHash(newHash)
        prefs.setPasswordEnabled(true)
        _events.value = ChangePasswordEvent.Saved
    }
}
