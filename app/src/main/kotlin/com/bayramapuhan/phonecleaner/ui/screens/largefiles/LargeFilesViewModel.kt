package com.bayramapuhan.phonecleaner.ui.screens.largefiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bayramapuhan.phonecleaner.data.repository.FileRepository
import com.bayramapuhan.phonecleaner.domain.model.FileItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LargeFilesUiState(
    val loading: Boolean = false,
    val files: List<FileItem> = emptyList(),
    val selected: Set<String> = emptySet(),
    val thresholdMb: Int = 50,
    val error: String? = null,
)

@HiltViewModel
class LargeFilesViewModel @Inject constructor(
    private val repo: FileRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(LargeFilesUiState())
    val state: StateFlow<LargeFilesUiState> = _state.asStateFlow()

    fun scan() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null, selected = emptySet()) }
            val threshold = _state.value.thresholdMb.toLong() * 1024L * 1024L
            runCatching { repo.findLargeFiles(threshold) }
                .onSuccess { result -> _state.update { it.copy(loading = false, files = result) } }
                .onFailure { e -> _state.update { it.copy(loading = false, error = e.message) } }
        }
    }

    fun toggleSelect(path: String) {
        _state.update {
            val sel = it.selected.toMutableSet()
            if (!sel.add(path)) sel.remove(path)
            it.copy(selected = sel)
        }
    }

    fun selectAll() = _state.update { it.copy(selected = it.files.map { f -> f.path }.toSet()) }
    fun clearSelection() = _state.update { it.copy(selected = emptySet()) }

    fun deleteSelected() {
        viewModelScope.launch {
            val targets = _state.value.selected.toList()
            repo.delete(targets)
            scan()
        }
    }

    fun setThreshold(mb: Int) = _state.update { it.copy(thresholdMb = mb) }
}
