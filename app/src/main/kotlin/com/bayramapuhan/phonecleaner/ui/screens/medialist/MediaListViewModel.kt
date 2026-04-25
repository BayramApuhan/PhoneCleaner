package com.bayramapuhan.phonecleaner.ui.screens.medialist

import android.content.IntentSender
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bayramapuhan.phonecleaner.data.repository.PhotoRepository
import com.bayramapuhan.phonecleaner.domain.model.Photo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MediaListUiState(
    val type: MediaListType = MediaListType.VIDEOS,
    val loading: Boolean = false,
    val items: List<Photo> = emptyList(),
    val selected: Set<Long> = emptySet(),
    val query: String = "",
) {
    val visible: List<Photo>
        get() = if (query.isBlank()) items
        else items.filter { it.displayName.contains(query, ignoreCase = true) }

    val selectedTotalBytes: Long
        get() = items.filter { it.id in selected }.sumOf { it.sizeBytes }

    val selectedCount: Int get() = selected.size
}

sealed interface MediaListEvent {
    data class LaunchDelete(val intentSender: IntentSender) : MediaListEvent
    data object DeletedDirectly : MediaListEvent
    data object DeleteFailed : MediaListEvent
}

@HiltViewModel
class MediaListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repo: PhotoRepository,
) : ViewModel() {

    private val type: MediaListType = MediaListType.valueOf(
        savedStateHandle.get<String>("type")?.uppercase() ?: MediaListType.VIDEOS.name,
    )

    private val _state = MutableStateFlow(MediaListUiState(type = type))
    val state: StateFlow<MediaListUiState> = _state.asStateFlow()

    private val _events = Channel<MediaListEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, selected = emptySet()) }
            val items = runCatching {
                when (type) {
                    MediaListType.VIDEOS -> repo.loadAllVideos()
                    MediaListType.AUDIO -> repo.loadAllAudio()
                }
            }.getOrDefault(emptyList())
            _state.update { it.copy(loading = false, items = items) }
        }
    }

    fun toggleSelect(id: Long) {
        _state.update {
            val sel = it.selected.toMutableSet()
            if (!sel.add(id)) sel.remove(id)
            it.copy(selected = sel)
        }
    }

    fun setQuery(q: String) = _state.update { it.copy(query = q) }
    fun clearSelection() = _state.update { it.copy(selected = emptySet()) }

    fun deleteSelected() {
        viewModelScope.launch {
            val ids = _state.value.selected
            if (ids.isEmpty()) return@launch
            val uris = _state.value.items.filter { it.id in ids }.map { it.uri }
            val intentSender = runCatching { repo.deletePhotosIntent(uris) }.getOrNull()
            if (intentSender != null) {
                _events.send(MediaListEvent.LaunchDelete(intentSender))
            } else {
                _events.send(MediaListEvent.DeletedDirectly)
                load()
            }
        }
    }

    fun onDeletionConfirmed() {
        load()
    }
}
