package com.bayramapuhan.phonecleaner.ui.screens.photos

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.bayramapuhan.phonecleaner.R
import com.bayramapuhan.phonecleaner.domain.model.Photo
import com.bayramapuhan.phonecleaner.ui.components.ConfirmDeleteDialog
import com.bayramapuhan.phonecleaner.util.Permissions
import com.bayramapuhan.phonecleaner.util.formatSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotosScreen(
    onBack: () -> Unit,
    vm: PhotosViewModel = hiltViewModel(),
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
        if (hasPermission) vm.loadPhotos()
    }

    LaunchedEffect(Unit) {
        if (hasPermission) vm.loadPhotos()
    }

    LaunchedEffect(Unit) {
        vm.events.collect { event ->
            when (event) {
                is PhotosEvent.LaunchDelete -> deleteLauncher.launch(
                    IntentSenderRequest.Builder(event.intentSender).build(),
                )
                PhotosEvent.DeletedDirectly -> snackbarHostState.showSnackbar(deletedMsg.format(0, "0 B"))
                PhotosEvent.DeleteFailed -> snackbarHostState.showSnackbar(failedMsg)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.photos_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            if (!hasPermission) {
                PermissionPrompt(onGrant = { permissionLauncher.launch(Permissions.mediaPermissions()) })
                return@Scaffold
            }

            TabRow(selectedTabIndex = state.tab.ordinal) {
                Tab(
                    selected = state.tab == PhotosTab.ALL,
                    onClick = { vm.selectTab(PhotosTab.ALL) },
                    text = { Text("${stringResource(R.string.photos_tab_all)} (${state.photos.size})") },
                )
                Tab(
                    selected = state.tab == PhotosTab.DUPLICATES,
                    onClick = { vm.selectTab(PhotosTab.DUPLICATES) },
                    text = { Text(stringResource(R.string.photos_tab_duplicates)) },
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                when {
                    state.loading -> CenteredLoading(stringResource(R.string.photos_loading))
                    state.tab == PhotosTab.ALL -> AllPhotosGrid(state, onToggle = vm::toggleSelected)
                    state.tab == PhotosTab.DUPLICATES -> when {
                        state.scanning -> ScanProgress(state.scanProgress, state.scanTotal)
                        state.duplicateGroups.isEmpty() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(stringResource(R.string.photos_no_duplicates))
                        }
                        else -> DuplicatesList(
                            groups = state.duplicateGroups,
                            selectedIds = state.selectedIds,
                            onToggle = vm::toggleSelected,
                        )
                    }
                }
            }

            if (state.selectedCount > 0) {
                Button(
                    onClick = { showConfirm = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("${stringResource(R.string.action_delete)} (${state.selectedCount} · ${state.selectedTotalBytes.formatSize()})")
                }
            }
        }
    }
}

@Composable
private fun CenteredLoading(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(Modifier.height(8.dp))
            Text(text)
        }
    }
}

@Composable
private fun ScanProgress(current: Int, total: Int) {
    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.photos_scanning_dupes), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { if (total > 0) current.toFloat() / total else 0f },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            Text("$current / $total", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun AllPhotosGrid(state: PhotosUiState, onToggle: (Long) -> Unit) {
    if (state.photos.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.photos_empty))
        }
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        items(state.photos, key = { it.id }) { photo ->
            PhotoTile(
                photo = photo,
                selected = photo.id in state.selectedIds,
                onClick = { onToggle(photo.id) },
            )
        }
    }
}

@Composable
private fun DuplicatesList(
    groups: List<List<Photo>>,
    selectedIds: Set<Long>,
    onToggle: (Long) -> Unit,
) {
    LazyColumn(contentPadding = PaddingValues(16.dp)) {
        items(groups, key = { it.first().id }) { group ->
            DuplicateGroupCard(group, selectedIds, onToggle)
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DuplicateGroupCard(
    group: List<Photo>,
    selectedIds: Set<Long>,
    onToggle: (Long) -> Unit,
) {
    Column {
        Text(
            stringResource(R.string.photos_group_label, group.size, group.sumOf { it.sizeBytes }.formatSize()),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.height(220.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(group, key = { it.id }) { photo ->
                val isFirst = photo == group.first()
                PhotoTile(
                    photo = photo,
                    selected = photo.id in selectedIds,
                    keepBadge = isFirst,
                    onClick = { onToggle(photo.id) },
                )
            }
        }
    }
}

@Composable
private fun PhotoTile(
    photo: Photo,
    selected: Boolean,
    keepBadge: Boolean = false,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
    ) {
        AsyncImage(
            model = photo.uri,
            contentDescription = photo.displayName,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        if (selected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f)),
            )
            Box(
                modifier = Modifier
                    .padding(6.dp)
                    .size(22.dp)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
        if (keepBadge && !selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
                    .size(22.dp)
                    .background(MaterialTheme.colorScheme.tertiary, RoundedCornerShape(50)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Star, contentDescription = stringResource(R.string.photos_keep), tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun PermissionPrompt(onGrant: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(stringResource(R.string.perm_required_title), style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.perm_storage_msg))
        Spacer(Modifier.height(16.dp))
        Button(onClick = onGrant) { Text(stringResource(R.string.perm_grant)) }
    }
}
