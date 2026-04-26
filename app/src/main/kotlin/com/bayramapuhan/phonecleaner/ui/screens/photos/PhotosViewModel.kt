package com.bayramapuhan.phonecleaner.ui.screens.photos

import android.content.IntentSender
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bayramapuhan.phonecleaner.data.repository.PhotoRepository
import com.bayramapuhan.phonecleaner.domain.model.Photo
import com.bayramapuhan.phonecleaner.util.PhotoHash
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PhotosUiState(
    val loading: Boolean = false,
    val scanning: Boolean = false,
    val scanProgress: Int = 0,
    val scanTotal: Int = 0,
    val duplicateGroups: List<List<Photo>> = emptyList(),
    val selectedIds: Set<Long> = emptySet(),
) {
    val selectedTotalBytes: Long
        get() = duplicateGroups.flatten()
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

    /**
     * One-shot pull-and-scan. Loads all photos, hashes them with the
     * existing perceptual-hash util, then groups by Hamming distance.
     * Algorithm: photos are pre-bucketed by file-size proximity (±10%)
     * before hashing pairs, dropping the obvious O(n²) cost on the
     * "everything against everything" hot loop. Within each bucket we
     * still use a Hamming threshold of 8 (slightly stricter than the
     * old 10) so near-identical edits stay grouped while genuinely
     * different shots stay apart. Selection auto-checks every photo
     * except the largest in each group — the safe default for
     * "delete the duplicates, keep the best copy".
     */
    fun refresh() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    loading = true,
                    selectedIds = emptySet(),
                    duplicateGroups = emptyList(),
                )
            }
            val photos = runCatching { repo.loadAllPhotos() }.getOrDefault(emptyList())
            if (photos.isEmpty()) {
                _state.update { it.copy(loading = false) }
                return@launch
            }

            _state.update {
                it.copy(
                    loading = false,
                    scanning = true,
                    scanProgress = 0,
                    scanTotal = photos.size,
                )
            }

            val hashes = repo.computeHashes(photos) { current, total ->
                _state.update { it.copy(scanProgress = current, scanTotal = total) }
            }

            val groups = bucketAndGroup(photos, hashes, hammingThreshold = 8)

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
            val uris = _state.value.duplicateGroups
                .flatten()
                .distinctBy { it.id }
                .filter { it.id in ids }
                .map { it.uri }
            val intentSender = runCatching { repo.deletePhotosIntent(uris) }.getOrNull()
            if (intentSender != null) {
                _events.send(PhotosEvent.LaunchDelete(intentSender))
            } else {
                _events.send(PhotosEvent.DeletedDirectly)
                refresh()
            }
        }
    }

    fun onDeletionConfirmed() {
        refresh()
    }

    /**
     * Pre-bucket photos so we only run perceptual-hash comparisons inside
     * size cohorts. Two photos whose byte sizes differ by more than ~10%
     * are extremely unlikely to be perceptual duplicates of each other,
     * and avoiding those pairs keeps the cost roughly linear in n on
     * realistic galleries.
     */
    private fun bucketAndGroup(
        photos: List<Photo>,
        hashes: Map<Long, Long>,
        hammingThreshold: Int,
    ): List<List<Photo>> {
        if (photos.isEmpty()) return emptyList()
        val photoMap = photos.associateBy { it.id }
        val sortedBySize = photos.filter { it.id in hashes }
            .sortedBy { it.sizeBytes }
        val visited = mutableSetOf<Long>()
        val groups = mutableListOf<List<Photo>>()

        for (anchor in sortedBySize) {
            if (anchor.id in visited) continue
            val anchorHash = hashes[anchor.id] ?: continue
            val sizeWindow = (anchor.sizeBytes / 10).coerceAtLeast(64L * 1024L) // ±10% or 64 KiB
            val groupIds = mutableListOf(anchor.id)
            visited += anchor.id
            for (other in sortedBySize) {
                if (other.id in visited) continue
                if (other.sizeBytes - anchor.sizeBytes > sizeWindow) break
                val otherHash = hashes[other.id] ?: continue
                if (PhotoHash.hammingDistance(anchorHash, otherHash) <= hammingThreshold) {
                    groupIds += other.id
                    visited += other.id
                }
            }
            if (groupIds.size > 1) {
                groups += groupIds.mapNotNull { photoMap[it] }
                    .sortedByDescending { it.sizeBytes }
            }
        }
        return groups
    }
}
