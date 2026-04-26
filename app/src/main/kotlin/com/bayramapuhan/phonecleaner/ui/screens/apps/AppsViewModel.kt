package com.bayramapuhan.phonecleaner.ui.screens.apps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bayramapuhan.phonecleaner.data.preferences.AppPreferences
import com.bayramapuhan.phonecleaner.data.repository.AppRepository
import com.bayramapuhan.phonecleaner.domain.model.AppItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AppSort { SIZE, NAME }

data class AppsUiState(
    val loading: Boolean = false,
    val apps: List<AppItem> = emptyList(),
    val sort: AppSort = AppSort.SIZE,
    val query: String = "",
) {
    val visibleApps: List<AppItem>
        get() = if (query.isBlank()) apps
        else apps.filter {
            it.label.contains(query, ignoreCase = true) ||
                it.packageName.contains(query, ignoreCase = true)
        }
}

@HiltViewModel
class AppsViewModel @Inject constructor(
    private val repo: AppRepository,
    private val prefs: AppPreferences,
) : ViewModel() {
    private val _state = MutableStateFlow(AppsUiState(loading = true))
    val state: StateFlow<AppsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            prefs.hideSystemApps.collect { _ -> load() }
        }
    }

    fun load() = doLoad(initialDelayMs = 0)

    /**
     * Pull-to-refresh and post-uninstall reload. The 500ms delay gives the
     * PackageManager time to finish propagating the uninstall before we
     * query getInstalledPackages — otherwise the freshly removed app can
     * still appear in the list for one or two seconds on some OEMs.
     */
    fun refresh() = doLoad(initialDelayMs = 500)

    private fun doLoad(initialDelayMs: Long) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            if (initialDelayMs > 0) delay(initialDelayMs)
            val hideSystem = prefs.hideSystemApps.first()
            val list = runCatching { repo.loadInstalledApps(includeSystem = !hideSystem) }
                .getOrDefault(emptyList())
            _state.update { it.copy(loading = false, apps = sorted(list, it.sort)) }
        }
    }

    fun setSort(sort: AppSort) {
        _state.update { it.copy(sort = sort, apps = sorted(it.apps, sort)) }
    }

    fun setQuery(q: String) = _state.update { it.copy(query = q) }

    private fun sorted(list: List<AppItem>, sort: AppSort): List<AppItem> = when (sort) {
        AppSort.SIZE -> list.sortedByDescending { it.sizeBytes }
        AppSort.NAME -> list.sortedBy { it.label.lowercase() }
    }
}
