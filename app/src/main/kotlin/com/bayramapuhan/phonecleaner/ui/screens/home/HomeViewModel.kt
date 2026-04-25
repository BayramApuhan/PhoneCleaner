package com.bayramapuhan.phonecleaner.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bayramapuhan.phonecleaner.data.repository.StorageRepository
import com.bayramapuhan.phonecleaner.domain.model.StorageInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val loading: Boolean = false,
    val storage: StorageInfo? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val storageRepo: StorageRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(HomeUiState(loading = true))
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            val info = runCatching { storageRepo.load() }.getOrNull()
            _state.update { it.copy(loading = false, storage = info) }
        }
    }
}
