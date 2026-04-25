package com.bayramapuhan.phonecleaner.ui.screens.memory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bayramapuhan.phonecleaner.data.repository.MemoryRepository
import com.bayramapuhan.phonecleaner.domain.model.MemoryInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MemoryUiState(
    val info: MemoryInfo? = null,
)

@HiltViewModel
class MemoryViewModel @Inject constructor(
    private val repo: MemoryRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(MemoryUiState())
    val state: StateFlow<MemoryUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _state.value = MemoryUiState(info = repo.load())
        }
    }

    fun openSystemAppList() {
        repo.openSystemAppList()
    }
}
