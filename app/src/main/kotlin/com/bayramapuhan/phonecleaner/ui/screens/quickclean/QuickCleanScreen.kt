package com.bayramapuhan.phonecleaner.ui.screens.quickclean

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.bayramapuhan.phonecleaner.R
import com.bayramapuhan.phonecleaner.domain.model.CleanableItem
import com.bayramapuhan.phonecleaner.ui.components.ConfirmDeleteDialog
import com.bayramapuhan.phonecleaner.ui.components.DeletingOverlay
import com.bayramapuhan.phonecleaner.ui.components.FileLeadingIcon
import com.bayramapuhan.phonecleaner.ui.components.MediaThumb
import com.bayramapuhan.phonecleaner.util.Permissions
import com.bayramapuhan.phonecleaner.util.formatSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickCleanScreen(
    onBack: () -> Unit,
    vm: QuickCleanViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var showConfirm by remember { mutableStateOf(false) }
    var pendingDiskAfterMedia by remember { mutableStateOf<List<String>>(emptyList()) }

    val deletedFmt = stringResource(R.string.snackbar_deleted)
    val failedMsg = stringResource(R.string.snackbar_delete_failed)

    val mediaDeleteLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            vm.onMediaDeletionConfirmed(pendingDiskAfterMedia)
        } else {
            vm.onMediaDeletionCancelled()
        }
        pendingDiskAfterMedia = emptyList()
    }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var permTick by remember { mutableIntStateOf(0) }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) permTick++
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val hasMedia = remember(permTick) { Permissions.hasMediaRead(context) }
    val hasAllFiles = remember(permTick) { Permissions.hasAllFilesAccess() }

    val mediaPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permTick++ }

    LaunchedEffect(permTick, hasMedia, hasAllFiles) {
        if (hasMedia || hasAllFiles) vm.load()
    }

    LaunchedEffect(Unit) {
        vm.events.collect { event ->
            when (event) {
                is QuickCleanEvent.LaunchMediaDelete -> {
                    pendingDiskAfterMedia = event.pendingDiskPaths
                    mediaDeleteLauncher.launch(IntentSenderRequest.Builder(event.intentSender).build())
                }
                is QuickCleanEvent.Deleted -> snackbar.showSnackbar(
                    deletedFmt.format(event.count, event.freed.formatSize()),
                )
                QuickCleanEvent.DeleteFailed -> snackbar.showSnackbar(failedMsg)
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

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.qc_title)) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        }
                    },
                )
            },
            snackbarHost = { SnackbarHost(snackbar) },
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                PullToRefreshBox(
                    isRefreshing = state.loading,
                    onRefresh = { vm.load() },
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                ) {
                when {
                    state.loading && state.items.isEmpty() -> Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(Modifier.height(8.dp))
                            Text(stringResource(R.string.qc_scanning))
                        }
                    }
                    state.items.isEmpty() && (!hasMedia || !hasAllFiles) -> Box(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        PermissionMissingPrompt(
                            hasMedia = hasMedia,
                            hasAllFiles = hasAllFiles,
                            onGrantMedia = { mediaPermLauncher.launch(Permissions.mediaPermissions()) },
                            onGrantAllFiles = { Permissions.openAllFilesAccessSettings(context) },
                        )
                    }
                    state.items.isEmpty() -> Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(stringResource(R.string.qc_empty))
                    }
                    else -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            stringResource(
                                R.string.qc_count_total,
                                state.items.size,
                                state.items.sumOf { it.sizeBytes }.formatSize(),
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        )
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            contentPadding = PaddingValues(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(state.items, key = { it.key }) { item ->
                                CleanableCell(
                                    item = item,
                                    selected = item.key in state.selected,
                                    onClick = { vm.toggleSelect(item.key) },
                                )
                            }
                        }
                    }
                    }
                }
                }

                if (state.selected.isNotEmpty()) {
                    Button(
                        onClick = { showConfirm = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            stringResource(
                                R.string.qc_delete_btn,
                                state.selected.size,
                                state.selectedTotalBytes.formatSize(),
                            ),
                        )
                    }
                }
            }
        }

        DeletingOverlay(visible = state.deleting)
    }
}

@Composable
private fun PermissionMissingPrompt(
    hasMedia: Boolean,
    hasAllFiles: Boolean,
    onGrantMedia: () -> Unit,
    onGrantAllFiles: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            stringResource(R.string.qc_perm_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.qc_perm_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(20.dp))
        if (!hasMedia) {
            PermissionRow(
                icon = Icons.Default.PhotoLibrary,
                accent = Color(0xFFEC4899),
                title = stringResource(R.string.onboarding_media_title),
                onGrant = onGrantMedia,
            )
            Spacer(Modifier.height(10.dp))
        }
        if (!hasAllFiles) {
            PermissionRow(
                icon = Icons.Default.FolderOpen,
                accent = Color(0xFFF97316),
                title = stringResource(R.string.onboarding_allfiles_title),
                onGrant = onGrantAllFiles,
            )
        }
    }
}

@Composable
private fun PermissionRow(
    icon: ImageVector,
    accent: Color,
    title: String,
    onGrant: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(accent),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = Color.White)
        }
        Spacer(Modifier.size(12.dp))
        Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        OutlinedButton(onClick = onGrant) { Text(stringResource(R.string.onboarding_grant)) }
    }
}

@Composable
private fun CleanableCell(
    item: CleanableItem,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
    ) {
        when (item) {
            is CleanableItem.Media -> {
                MediaThumb(
                    uri = item.uri,
                    isVideo = item.category == CleanableItem.Category.VIDEO,
                    modifier = Modifier.fillMaxSize(),
                )
                if (item.category == CleanableItem.Category.VIDEO) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(32.dp)
                            .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                            .padding(4.dp),
                    )
                }
            }
            is CleanableItem.Disk -> {
                FileLeadingIcon(path = item.path, modifier = Modifier.fillMaxSize())
            }
        }

        // bottom info bar
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.55f))
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                item.sizeBytes.formatSize(),
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            Text(
                item.category.label(),
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 9.sp,
            )
        }

        // selection indicator
        if (selected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp),
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.35f)),
            )
        }
    }
}

@Composable
private fun CleanableItem.Category.label(): String = stringResource(
    when (this) {
        CleanableItem.Category.PHOTO -> R.string.qc_cat_photo
        CleanableItem.Category.VIDEO -> R.string.qc_cat_video
        CleanableItem.Category.AUDIO -> R.string.qc_cat_audio
        CleanableItem.Category.APK -> R.string.qc_cat_apk
        CleanableItem.Category.LARGE_FILE -> R.string.qc_cat_file
    },
)
