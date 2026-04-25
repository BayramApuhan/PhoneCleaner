package com.bayramapuhan.phonecleaner.ui.screens.other

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bayramapuhan.phonecleaner.data.repository.FileRepository
import com.bayramapuhan.phonecleaner.domain.model.DeleteResult
import com.bayramapuhan.phonecleaner.domain.model.FileItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OtherFilesUiState(
    val loading: Boolean = false,
    val files: List<FileItem> = emptyList(),
    val selected: Set<String> = emptySet(),
    val query: String = "",
) {
    val visibleFiles: List<FileItem>
        get() = if (query.isBlank()) files
        else files.filter { it.name.contains(query, ignoreCase = true) }

    val selectedTotalBytes: Long
        get() = files.filter { it.path in selected }.sumOf { it.sizeBytes }
}

sealed interface OtherFilesEvent {
    data class Deleted(val result: DeleteResult) : OtherFilesEvent
    data object DeleteFailed : OtherFilesEvent
}

@HiltViewModel
class OtherFilesViewModel @Inject constructor(
    private val repo: FileRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(OtherFilesUiState())
    val state: StateFlow<OtherFilesUiState> = _state.asStateFlow()

    private val _events = Channel<OtherFilesEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun scan() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, selected = emptySet()) }
            val files = runCatching { repo.findOtherFiles() }.getOrDefault(emptyList())
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

    fun selectAll() = _state.update { it.copy(selected = it.visibleFiles.map { f -> f.path }.toSet()) }
    fun clearSelection() = _state.update { it.copy(selected = emptySet()) }

    fun setQuery(q: String) = _state.update { it.copy(query = q) }

    fun deleteSelected() {
        viewModelScope.launch {
            val targets = _state.value.selected.toList()
            if (targets.isEmpty()) return@launch
            val result = runCatching { repo.delete(targets) }.getOrNull()
            if (result == null) {
                _events.send(OtherFilesEvent.DeleteFailed)
            } else {
                _events.send(OtherFilesEvent.Deleted(result))
                scan()
            }
        }
    }
}
