package com.bayramapuhan.phonecleaner.ui.screens.storage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bayramapuhan.phonecleaner.data.repository.StorageRepository
import com.bayramapuhan.phonecleaner.domain.model.StorageInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StorageUiState(
    val loading: Boolean = false,
    val info: StorageInfo? = null,
    val error: String? = null,
)

@HiltViewModel
class StorageViewModel @Inject constructor(
    private val repo: StorageRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(StorageUiState(loading = true))
    val state: StateFlow<StorageUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _state.value = StorageUiState(loading = true)
            runCatching { repo.load() }
                .onSuccess { _state.value = StorageUiState(info = it) }
                .onFailure { _state.value = StorageUiState(error = it.message ?: "Hata") }
        }
    }
}
