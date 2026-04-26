package com.bayramapuhan.phonecleaner.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bayramapuhan.phonecleaner.R
import com.bayramapuhan.phonecleaner.domain.model.StorageInfo
import com.bayramapuhan.phonecleaner.ui.components.color
import com.bayramapuhan.phonecleaner.ui.components.label
import com.bayramapuhan.phonecleaner.ui.drawer.DrawerContent
import com.bayramapuhan.phonecleaner.util.formatSize
import kotlinx.coroutines.launch

private data class FeatureTile(
    val title: Int,
    val desc: Int,
    val icon: ImageVector,
    val accent: Color,
    val onClick: () -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenStorage: () -> Unit,
    onOpenPhotos: () -> Unit,
    onOpenLargeFiles: () -> Unit,
    onOpenApps: () -> Unit,
    onOpenApk: () -> Unit,
    onOpenMemory: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenQuickClean: () -> Unit,
    onOpenAppearance: () -> Unit,
    onOpenLanguage: () -> Unit,
    onOpenChangePassword: () -> Unit,
    onOpenRecoveryEmail: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenFaq: () -> Unit,
    onOpenFeedback: () -> Unit,
    vm: HomeViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()
    val tiles = listOf(
        FeatureTile(R.string.feature_storage, R.string.feature_storage_desc, Icons.Default.Storage, Color(0xFF0EA5E9), onOpenStorage),
        FeatureTile(R.string.feature_photos, R.string.feature_photos_desc, Icons.Default.PhotoLibrary, Color(0xFFEC4899), onOpenPhotos),
        FeatureTile(R.string.feature_large_files, R.string.feature_large_files_desc, Icons.Default.CleaningServices, Color(0xFFF97316), onOpenLargeFiles),
        FeatureTile(R.string.feature_apps, R.string.feature_apps_desc, Icons.Default.Apps, Color(0xFF10B981), onOpenApps),
        FeatureTile(R.string.feature_apk, R.string.feature_apk_desc, Icons.Default.FolderZip, Color(0xFF14B8A6), onOpenApk),
        FeatureTile(R.string.feature_memory, R.string.feature_memory_desc, Icons.Default.Memory, Color(0xFF8B5CF6), onOpenMemory),
    )

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val closeDrawerThen: (() -> Unit) -> Unit = { action ->
        scope.launch { drawerState.close() }
        action()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                onNavigateAppearance = { closeDrawerThen(onOpenAppearance) },
                onNavigateLanguage = { closeDrawerThen(onOpenLanguage) },
                onNavigateChangePassword = { closeDrawerThen(onOpenChangePassword) },
                onNavigateRecoveryEmail = { closeDrawerThen(onOpenRecoveryEmail) },
                onNavigateAbout = { closeDrawerThen(onOpenAbout) },
                onNavigateFaq = { closeDrawerThen(onOpenFaq) },
                onNavigateFeedback = { closeDrawerThen(onOpenFeedback) },
            )
        },
    ) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.home_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.drawer_open))
                    }
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings_title))
                    }
                },
            )
        },
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                HeroStorageCard(
                    storage = state.storage,
                    onQuickClean = onOpenQuickClean,
                )
            }
            items(tiles) { tile -> FeatureCard(tile) }
        }
    }
    }
}

@Composable
private fun HeroStorageCard(storage: StorageInfo?, onQuickClean: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                stringResource(R.string.home_free_space).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                letterSpacing = 1.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                if (storage != null) storage.freeBytes.formatSize() else "—",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                if (storage != null) stringResource(R.string.home_total_space, storage.totalBytes.formatSize()) else "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(20.dp))

            if (storage != null) {
                SegmentedStorageBar(storage)
                Spacer(Modifier.height(16.dp))
                StorageLegend(storage)
                Spacer(Modifier.height(20.dp))
            }

            Button(
                onClick = onQuickClean,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(vertical = 14.dp),
            ) {
                Text(
                    stringResource(R.string.home_quick_clean),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun SegmentedStorageBar(info: StorageInfo) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        info.categories.forEach { cat ->
            val fraction = if (info.totalBytes > 0) cat.sizeBytes.toFloat() / info.totalBytes else 0f
            if (fraction > 0.001f) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(fraction)
                        .background(cat.type.color()),
                )
            }
        }
        val freeFraction = if (info.totalBytes > 0) info.freeBytes.toFloat() / info.totalBytes else 1f
        if (freeFraction > 0.001f) {
            Box(modifier = Modifier.fillMaxHeight().weight(freeFraction))
        }
    }
}

@Composable
private fun StorageLegend(info: StorageInfo) {
    val items = info.categories
        .filter { it.sizeBytes > 0 }
        .sortedByDescending { it.sizeBytes }
        .take(3)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items.forEach { cat ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(cat.type.color()),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    cat.type.label(),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    cat.sizeBytes.formatSize(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun FeatureCard(tile: FeatureTile) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = tile.onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(tile.accent),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = tile.icon,
                    contentDescription = null,
                    tint = Color.White,
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(tile.title),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(tile.desc),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
