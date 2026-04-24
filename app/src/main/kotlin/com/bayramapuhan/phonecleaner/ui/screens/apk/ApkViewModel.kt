package com.bayramapuhan.phonecleaner.ui.screens.apk

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

data class ApkUiState(
    val loading: Boolean = false,
    val files: List<FileItem> = emptyList(),
    val selected: Set<String> = emptySet(),
)

@HiltViewModel
class ApkViewModel @Inject constructor(
    private val repo: FileRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(ApkUiState())
    val state: StateFlow<ApkUiState> = _state.asStateFlow()

    fun scan() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, selected = emptySet()) }
            val files = runCatching { repo.findApks() }.getOrDefault(emptyList())
            _state.update { it.copy(loading = false, files = files) }
        }
    }

    fun toggleSelect(path: String) {
        _state.update {
            val sel = it.selected.toMutableSet()
            if (!sel.add(path)) sel.remove(path)
            it.copy(selected = sel)
        }
    }

    fun deleteSelected() {
        viewModelScope.launch {
            repo.delete(_state.value.selected.toList())
            scan()
        }
    }
}
