package com.bayramapuhan.phonecleaner.ui.screens.apk

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

data class ApkUiState(
    val loading: Boolean = false,
    val files: List<FileItem> = emptyList(),
    val selected: Set<String> = emptySet(),
) {
    val selectedTotalBytes: Long
        get() = files.filter { it.path in selected }.sumOf { it.sizeBytes }
}

sealed interface ApkEvent {
    data class Deleted(val result: DeleteResult) : ApkEvent
    data object DeleteFailed : ApkEvent
}

@HiltViewModel
class ApkViewModel @Inject constructor(
    private val repo: FileRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(ApkUiState())
    val state: StateFlow<ApkUiState> = _state.asStateFlow()

    private val _events = Channel<ApkEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

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
            val targets = _state.value.selected.toList()
            if (targets.isEmpty()) return@launch
            val result = runCatching { repo.delete(targets) }.getOrNull()
            if (result == null) {
                _events.send(ApkEvent.DeleteFailed)
            } else {
                _events.send(ApkEvent.Deleted(result))
                scan()
            }
        }
    }
}
