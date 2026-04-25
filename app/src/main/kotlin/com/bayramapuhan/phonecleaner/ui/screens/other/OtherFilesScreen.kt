package com.bayramapuhan.phonecleaner.ui.screens.other

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bayramapuhan.phonecleaner.R
import com.bayramapuhan.phonecleaner.ui.components.ConfirmDeleteDialog
import com.bayramapuhan.phonecleaner.ui.components.FileLeadingIcon
import com.bayramapuhan.phonecleaner.util.Permissions
import com.bayramapuhan.phonecleaner.util.formatSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtherFilesScreen(
    onBack: () -> Unit,
    vm: OtherFilesViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showConfirm by remember { mutableStateOf(false) }

    val deletedMsg = stringResource(R.string.snackbar_deleted)
    val failedMsg = stringResource(R.string.snackbar_delete_failed)

    LaunchedEffect(Unit) {
        if (Permissions.hasAllFilesAccess()) vm.scan()
    }

    LaunchedEffect(Unit) {
        vm.events.collect { event ->
            when (event) {
                is OtherFilesEvent.Deleted -> snackbarHostState.showSnackbar(
                    deletedMsg.format(event.result.deletedCount, event.result.bytesFreed.formatSize()),
                )
                OtherFilesEvent.DeleteFailed -> snackbarHostState.showSnackbar(failedMsg)
            }
        }
    }

    if (showConfirm && state.selected.isNotEmpty()) {
        ConfirmDeleteDialog(
            count = state.selected.size,
            totalBytes = state.selectedTotalBytes,
            onConfirm = {
                showConfirm = false
                vm.deleteSelected()
            },
            onDismiss = { showConfirm = false },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.other_files_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { vm.scan() }) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            if (!Permissions.hasAllFilesAccess()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(top = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(stringResource(R.string.perm_required_title), style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.perm_all_files_msg))
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { Permissions.openAllFilesAccessSettings(context) }) {
                        Text(stringResource(R.string.perm_open_settings))
                    }
                }
                return@Scaffold
            }

            OutlinedTextField(
                value = state.query,
                onValueChange = vm::setQuery,
                placeholder = { Text(stringResource(R.string.search_placeholder)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = if (state.query.isNotEmpty()) {
                    {
                        IconButton(onClick = { vm.setQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = null)
                        }
                    }
                } else null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            )

            Spacer(Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { vm.selectAll() }, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.action_select_all))
                }
                OutlinedButton(onClick = { vm.clearSelection() }, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.action_clear_selection))
                }
            }

            Spacer(Modifier.height(8.dp))

            Box(modifier = Modifier.weight(1f)) {
                when {
                    state.loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                    state.visibleFiles.isEmpty() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.other_files_empty))
                    }
                    else -> LazyColumn {
                        items(state.visibleFiles, key = { it.path }) { file ->
                            ListItem(
                                modifier = Modifier.clickable { vm.toggleSelect(file.path) },
                                leadingContent = {
                                    FileLeadingIcon(
                                        path = file.path,
                                        modifier = Modifier.size(44.dp),
                                    )
                                },
                                headlineContent = { Text(file.name, maxLines = 1) },
                                supportingContent = { Text(file.sizeBytes.formatSize()) },
                                trailingContent = {
                                    Checkbox(
                                        checked = state.selected.contains(file.path),
                                        onCheckedChange = { vm.toggleSelect(file.path) },
                                    )
                                },
                            )
                        }
                    }
                }
            }

            if (state.selected.isNotEmpty()) {
                Button(
                    onClick = { showConfirm = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("${stringResource(R.string.action_delete)} (${state.selected.size} · ${state.selectedTotalBytes.formatSize()})")
                }
            }
        }
    }
}
