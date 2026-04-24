package com.bayramapuhan.phonecleaner.ui.screens.largefiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bayramapuhan.phonecleaner.data.preferences.AppPreferences
import com.bayramapuhan.phonecleaner.data.repository.FileRepository
import com.bayramapuhan.phonecleaner.domain.model.DeleteResult
import com.bayramapuhan.phonecleaner.domain.model.FileItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LargeFilesUiState(
    val loading: Boolean = false,
    val files: List<FileItem> = emptyList(),
    val selected: Set<String> = emptySet(),
    val thresholdMb: Int = 50,
    val query: String = "",
    val error: String? = null,
) {
    val visibleFiles: List<FileItem>
        get() = if (query.isBlank()) files
        else files.filter { it.name.contains(query, ignoreCase = true) }

    val selectedTotalBytes: Long
        get() = files.filter { it.path in selected }.sumOf { it.sizeBytes }
}

sealed interface LargeFilesEvent {
    data class Deleted(val result: DeleteResult) : LargeFilesEvent
    data object DeleteFailed : LargeFilesEvent
}

@HiltViewModel
class LargeFilesViewModel @Inject constructor(
    private val repo: FileRepository,
    private val prefs: AppPreferences,
) : ViewModel() {
    private val _state = MutableStateFlow(LargeFilesUiState())
    val state: StateFlow<LargeFilesUiState> = _state.asStateFlow()

    private val _events = Channel<LargeFilesEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            prefs.largeFileThresholdMb.collect { mb ->
                _state.update { it.copy(thresholdMb = mb) }
            }
        }
    }

    fun scan() {
        viewModelScope.launch {
            val mb = prefs.largeFileThresholdMb.first()
            _state.update { it.copy(loading = true, error = null, selected = emptySet(), thresholdMb = mb) }
            val threshold = mb.toLong() * 1024L * 1024L
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

    fun selectAll() = _state.update { it.copy(selected = it.visibleFiles.map { f -> f.path }.toSet()) }
    fun clearSelection() = _state.update { it.copy(selected = emptySet()) }

    fun setQuery(q: String) = _state.update { it.copy(query = q) }

    fun deleteSelected() {
        viewModelScope.launch {
            val targets = _state.value.selected.toList()
            if (targets.isEmpty()) return@launch
            val result = runCatching { repo.delete(targets) }.getOrNull()
            if (result == null) {
                _events.send(LargeFilesEvent.DeleteFailed)
            } else {
                _events.send(LargeFilesEvent.Deleted(result))
                scan()
            }
        }
    }
}
