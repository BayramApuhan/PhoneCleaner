package com.bayramapuhan.phonecleaner.ui.screens.quickclean

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bayramapuhan.phonecleaner.R

private data class QuickCleanTile(
    val titleRes: Int,
    val descRes: Int,
    val icon: ImageVector,
    val accent: Color,
    val onClick: () -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickCleanScreen(
    onBack: () -> Unit,
    onOpenDuplicates: () -> Unit,
    onOpenLargeFiles: () -> Unit,
    onOpenApk: () -> Unit,
    onOpenApps: () -> Unit,
    onOpenVideos: () -> Unit,
    onOpenAudio: () -> Unit,
    onOpenPhotos: () -> Unit,
) {
    val tiles = listOf(
        QuickCleanTile(
            R.string.qc_duplicates,
            R.string.qc_duplicates_desc,
            Icons.Default.ContentCopy,
            Color(0xFFEC4899),
            onOpenDuplicates,
        ),
        QuickCleanTile(
            R.string.qc_large_files,
            R.string.qc_large_files_desc,
            Icons.Default.CleaningServices,
            Color(0xFFF97316),
            onOpenLargeFiles,
        ),
        QuickCleanTile(
            R.string.qc_apk,
            R.string.qc_apk_desc,
            Icons.Default.FolderZip,
            Color(0xFF14B8A6),
            onOpenApk,
        ),
        QuickCleanTile(
            R.string.qc_apps,
            R.string.qc_apps_desc,
            Icons.Default.Apps,
            Color(0xFF10B981),
            onOpenApps,
        ),
        QuickCleanTile(
            R.string.qc_photos,
            R.string.qc_photos_desc,
            Icons.Default.PhotoLibrary,
            Color(0xFF06B6D4),
            onOpenPhotos,
        ),
        QuickCleanTile(
            R.string.qc_videos,
            R.string.qc_videos_desc,
            Icons.Default.Movie,
            Color(0xFF8B5CF6),
            onOpenVideos,
        ),
        QuickCleanTile(
            R.string.qc_audio,
            R.string.qc_audio_desc,
            Icons.Default.Audiotrack,
            Color(0xFFF59E0B),
            onOpenAudio,
        ),
    )

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
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Text(
                stringResource(R.string.qc_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(tiles) { tile -> QuickCleanCard(tile) }
            }
        }
    }
}

@Composable
private fun QuickCleanCard(tile: QuickCleanTile) {
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
                stringResource(tile.titleRes),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(tile.descRes),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
