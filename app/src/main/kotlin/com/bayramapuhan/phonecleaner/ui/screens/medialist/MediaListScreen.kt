package com.bayramapuhan.phonecleaner.ui.screens.medialist

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bayramapuhan.phonecleaner.R
import com.bayramapuhan.phonecleaner.ui.components.ConfirmDeleteDialog
import com.bayramapuhan.phonecleaner.util.Permissions
import com.bayramapuhan.phonecleaner.util.formatSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaListScreen(
    onBack: () -> Unit,
    vm: MediaListViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showConfirm by remember { mutableStateOf(false) }
    var hasPermission by remember { mutableStateOf(Permissions.hasMediaRead(context)) }

    val deletedMsg = stringResource(R.string.snackbar_deleted)
    val failedMsg = stringResource(R.string.snackbar_delete_failed)

    val deleteLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) vm.onDeletionConfirmed()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { granted ->
        hasPermission = granted.values.all { it }
        if (hasPermission) vm.load()
    }

    LaunchedEffect(Unit) {
        if (hasPermission) vm.load()
    }

    LaunchedEffect(Unit) {
        vm.events.collect { event ->
            when (event) {
                is MediaListEvent.LaunchDelete -> deleteLauncher.launch(
                    IntentSenderRequest.Builder(event.intentSender).build(),
                )
                MediaListEvent.DeletedDirectly -> snackbarHostState.showSnackbar(deletedMsg.format(0, "0 B"))
                MediaListEvent.DeleteFailed -> snackbarHostState.showSnackbar(failedMsg)
            }
        }
    }

    if (showConfirm && state.selectedCount > 0) {
        ConfirmDeleteDialog(
            count = state.selectedCount,
            totalBytes = state.selectedTotalBytes,
            onConfirm = {
                showConfirm = false
                vm.deleteSelected()
            },
            onDismiss = { showConfirm = false },
        )
    }

    val titleRes = when (state.type) {
        MediaListType.VIDEOS -> R.string.medialist_videos_title
        MediaListType.AUDIO -> R.string.medialist_audio_title
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(titleRes)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = 16.dp)
        ) {
            if (!hasPermission) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(top = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(stringResource(R.string.perm_required_title))
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.perm_storage_msg))
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { permissionLauncher.launch(Permissions.mediaPermissions()) }) {
                        Text(stringResource(R.string.perm_grant))
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

            Box(modifier = Modifier.weight(1f)) {
                when {
                    state.loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                    state.visible.isEmpty() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.medialist_empty))
                    }
                    else -> LazyColumn {
                        items(state.visible, key = { it.id }) { item ->
                            ListItem(
                                modifier = Modifier.clickable { vm.toggleSelect(item.id) },
                                leadingContent = {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(typeColor(state.type).copy(alpha = 0.18f)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            when (state.type) {
                                                MediaListType.VIDEOS -> Icons.Default.PlayCircle
                                                MediaListType.AUDIO -> Icons.Default.Audiotrack
                                            },
                                            contentDescription = null,
                                            tint = typeColor(state.type),
                                        )
                                    }
                                },
                                headlineContent = { Text(item.displayName, maxLines = 1) },
                                supportingContent = { Text(item.sizeBytes.formatSize()) },
                                trailingContent = {
                                    Checkbox(
                                        checked = item.id in state.selected,
                                        onCheckedChange = { vm.toggleSelect(item.id) },
                                    )
                                },
                            )
                        }
                    }
                }
            }

            if (state.selectedCount > 0) {
                Button(
                    onClick = { showConfirm = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("${stringResource(R.string.action_delete)} (${state.selectedCount} · ${state.selectedTotalBytes.formatSize()})")
                }
            }
        }
    }
}

private fun typeColor(type: MediaListType): Color = when (type) {
    MediaListType.VIDEOS -> Color(0xFF8B5CF6)
    MediaListType.AUDIO -> Color(0xFFF59E0B)
}
