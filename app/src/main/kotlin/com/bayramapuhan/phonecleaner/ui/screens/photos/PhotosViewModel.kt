package com.bayramapuhan.phonecleaner.ui.screens.photos

import android.content.IntentSender
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

enum class PhotosTab { ALL, DUPLICATES }

data class PhotosUiState(
    val tab: PhotosTab = PhotosTab.ALL,
    val loading: Boolean = false,
    val photos: List<Photo> = emptyList(),
    val selectedIds: Set<Long> = emptySet(),
    val scanning: Boolean = false,
    val scanProgress: Int = 0,
    val scanTotal: Int = 0,
    val duplicateGroups: List<List<Photo>> = emptyList(),
) {
    val selectedTotalBytes: Long
        get() = (photos + duplicateGroups.flatten())
            .distinctBy { it.id }
            .filter { it.id in selectedIds }
            .sumOf { it.sizeBytes }

    val selectedCount: Int get() = selectedIds.size
}

sealed interface PhotosEvent {
    data class LaunchDelete(val intentSender: IntentSender) : PhotosEvent
    data object DeletedDirectly : PhotosEvent
    data object DeleteFailed : PhotosEvent
}

@HiltViewModel
class PhotosViewModel @Inject constructor(
    private val repo: PhotoRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(PhotosUiState())
    val state: StateFlow<PhotosUiState> = _state.asStateFlow()

    private val _events = Channel<PhotosEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun loadPhotos() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, selectedIds = emptySet(), duplicateGroups = emptyList()) }
            val photos = runCatching { repo.loadAllPhotos() }.getOrDefault(emptyList())
            _state.update { it.copy(loading = false, photos = photos) }
        }
    }

    fun selectTab(tab: PhotosTab) {
        _state.update { it.copy(tab = tab) }
        if (tab == PhotosTab.DUPLICATES && _state.value.duplicateGroups.isEmpty() && !_state.value.scanning) {
            findDuplicates()
        }
    }

    fun findDuplicates() {
        viewModelScope.launch {
            val photos = _state.value.photos
            if (photos.isEmpty()) return@launch
            _state.update { it.copy(scanning = true, scanProgress = 0, scanTotal = photos.size) }
            val hashes = repo.computeHashes(photos) { current, total ->
                _state.update { it.copy(scanProgress = current, scanTotal = total) }
            }
            val groupIds = repo.groupDuplicates(hashes)
            val photoMap = photos.associateBy { it.id }
            val groups = groupIds.mapNotNull { ids ->
                ids.mapNotNull { photoMap[it] }
                    .sortedByDescending { it.sizeBytes }
                    .takeIf { it.size > 1 }
            }
            // Auto-suggest deletion: select all except largest in each group
            val autoSelected = groups.flatMap { it.drop(1).map { p -> p.id } }.toSet()
            _state.update {
                it.copy(
                    scanning = false,
                    duplicateGroups = groups,
                    selectedIds = autoSelected,
                )
            }
        }
    }

    fun toggleSelected(id: Long) {
        _state.update {
            val sel = it.selectedIds.toMutableSet()
            if (!sel.add(id)) sel.remove(id)
            it.copy(selectedIds = sel)
        }
    }

    fun clearSelection() = _state.update { it.copy(selectedIds = emptySet()) }

    fun deleteSelected() {
        viewModelScope.launch {
            val ids = _state.value.selectedIds
            if (ids.isEmpty()) return@launch
            val all = _state.value.photos + _state.value.duplicateGroups.flatten()
            val uris = all.distinctBy { it.id }.filter { it.id in ids }.map { it.uri }
            val intentSender = runCatching { repo.deletePhotosIntent(uris) }.getOrNull()
            if (intentSender != null) {
                _events.send(PhotosEvent.LaunchDelete(intentSender))
            } else {
                _events.send(PhotosEvent.DeletedDirectly)
                loadPhotos()
            }
        }
    }

    fun onDeletionConfirmed() {
        loadPhotos()
    }
}
