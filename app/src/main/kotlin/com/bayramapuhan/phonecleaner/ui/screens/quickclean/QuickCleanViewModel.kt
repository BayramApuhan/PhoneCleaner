package com.bayramapuhan.phonecleaner.ui.screens.quickclean

import android.content.IntentSender
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bayramapuhan.phonecleaner.data.repository.CleanableRepository
import com.bayramapuhan.phonecleaner.data.repository.FileRepository
import com.bayramapuhan.phonecleaner.data.repository.PhotoRepository
import com.bayramapuhan.phonecleaner.domain.model.CleanableItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuickCleanState(
    val loading: Boolean = false,
    val deleting: Boolean = false,
    val items: List<CleanableItem> = emptyList(),
    val selected: Set<String> = emptySet(),
) {
    val selectedItems: List<CleanableItem>
        get() = items.filter { it.key in selected }

    val selectedTotalBytes: Long
        get() = selectedItems.sumOf { it.sizeBytes }
}

sealed interface QuickCleanEvent {
    data class LaunchMediaDelete(val intentSender: IntentSender, val pendingDiskPaths: List<String>) : QuickCleanEvent
    data class Deleted(val count: Int, val freed: Long) : QuickCleanEvent
    data object DeleteFailed : QuickCleanEvent
}

@HiltViewModel
class QuickCleanViewModel @Inject constructor(
    private val cleanable: CleanableRepository,
    private val photos: PhotoRepository,
    private val files: FileRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(QuickCleanState())
    val state: StateFlow<QuickCleanState> = _state.asStateFlow()

    private val _events = Channel<QuickCleanEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, selected = emptySet()) }
            val list = runCatching { cleanable.loadAll() }.getOrDefault(emptyList())
            _state.update { it.copy(loading = false, items = list) }
        }
    }

    fun toggleSelect(key: String) {
        _state.update {
            val sel = it.selected.toMutableSet()
            if (!sel.add(key)) sel.remove(key)
            it.copy(selected = sel)
        }
    }

    fun clearSelection() = _state.update { it.copy(selected = emptySet()) }

    fun deleteSelected() {
        viewModelScope.launch {
            val targets = _state.value.selectedItems
            if (targets.isEmpty()) return@launch
            _state.update { it.copy(deleting = true) }

            val media = targets.filterIsInstance<CleanableItem.Media>()
            val disk = targets.filterIsInstance<CleanableItem.Disk>()

            if (media.isNotEmpty()) {
                val sender = runCatching { photos.deletePhotosIntent(media.map { it.uri }) }.getOrNull()
                if (sender != null) {
                    _events.send(QuickCleanEvent.LaunchMediaDelete(sender, disk.map { it.path }))
                    return@launch
                }
            }
            // Either no media or pre-R direct delete already happened
            finalizeDelete(diskPaths = disk.map { it.path }, mediaCount = media.size, mediaFreed = media.sumOf { it.sizeBytes })
        }
    }

    fun onMediaDeletionConfirmed(pendingDiskPaths: List<String>) {
        viewModelScope.launch {
            val freedMedia = _state.value.selectedItems.filterIsInstance<CleanableItem.Media>().sumOf { it.sizeBytes }
            val mediaCount = _state.value.selectedItems.filterIsInstance<CleanableItem.Media>().size
            finalizeDelete(pendingDiskPaths, mediaCount, freedMedia)
        }
    }

    fun onMediaDeletionCancelled() {
        _state.update { it.copy(deleting = false) }
    }

    private suspend fun finalizeDelete(diskPaths: List<String>, mediaCount: Int, mediaFreed: Long) {
        val diskResult = if (diskPaths.isNotEmpty()) {
            runCatching { files.delete(diskPaths) }.getOrNull()
        } else null

        val totalCount = mediaCount + (diskResult?.deletedCount ?: 0)
        val totalFreed = mediaFreed + (diskResult?.bytesFreed ?: 0L)

        _state.update { it.copy(deleting = false, selected = emptySet()) }
        if (totalCount > 0) {
            _events.send(QuickCleanEvent.Deleted(totalCount, totalFreed))
            load()
        } else {
            _events.send(QuickCleanEvent.DeleteFailed)
        }
    }
}
