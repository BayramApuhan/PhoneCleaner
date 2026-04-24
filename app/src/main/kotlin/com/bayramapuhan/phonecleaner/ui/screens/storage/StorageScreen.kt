package com.bayramapuhan.phonecleaner.ui.screens.storage

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bayramapuhan.phonecleaner.R
import com.bayramapuhan.phonecleaner.domain.model.CategoryType
import com.bayramapuhan.phonecleaner.domain.model.StorageCategory
import com.bayramapuhan.phonecleaner.domain.model.StorageInfo
import com.bayramapuhan.phonecleaner.util.formatSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageScreen(
    onBack: () -> Unit,
    vm: StorageViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.storage_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.loading,
            onRefresh = { vm.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when {
                state.loading && state.info == null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                state.error != null && state.info == null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Hata: ${state.error}")
                }
                state.info != null -> StorageContent(state.info!!)
            }
        }
    }
}

@Composable
private fun StorageContent(info: StorageInfo) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        SummaryCard(info)
        Spacer(Modifier.height(24.dp))
        Text(
            "Kategoriler",
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(8.dp))
        info.categories.forEach { cat ->
            CategoryRow(cat, info.totalBytes)
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SummaryCard(info: StorageInfo) {
    val usedFraction = if (info.totalBytes > 0) info.usedBytes.toFloat() / info.totalBytes else 0f
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(20.dp))
            .padding(20.dp),
    ) {
        Text(
            info.usedBytes.formatSize(),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            "${stringResource(R.string.storage_used)} / ${info.totalBytes.formatSize()} ${stringResource(R.string.storage_total).lowercase()}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(12.dp))
        LinearProgressIndicator(
            progress = { usedFraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp),
        )
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${stringResource(R.string.storage_free)}: ${info.freeBytes.formatSize()}", style = MaterialTheme.typography.labelMedium)
            Text("${(usedFraction * 100).toInt()}%", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun CategoryRow(cat: StorageCategory, total: Long) {
    val fraction = if (total > 0) cat.sizeBytes.toFloat() / total else 0f
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(cat.type.color(), RoundedCornerShape(3.dp)),
        )
        Spacer(Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(cat.type.label(), style = MaterialTheme.typography.bodyMedium)
                Text(cat.sizeBytes.formatSize(), style = MaterialTheme.typography.labelMedium)
            }
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { fraction.coerceAtMost(1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = cat.type.color(),
            )
        }
    }
}

@Composable
private fun CategoryType.label(): String = stringResource(
    when (this) {
        CategoryType.IMAGES -> R.string.storage_category_images
        CategoryType.VIDEOS -> R.string.storage_category_videos
        CategoryType.AUDIO -> R.string.storage_category_audio
        CategoryType.APPS -> R.string.storage_category_apps
        CategoryType.OTHER -> R.string.storage_category_other
    },
)

private fun CategoryType.color(): Color = when (this) {
    CategoryType.IMAGES -> Color(0xFF06B6D4)
    CategoryType.VIDEOS -> Color(0xFF8B5CF6)
    CategoryType.AUDIO -> Color(0xFFF59E0B)
    CategoryType.APPS -> Color(0xFF10B981)
    CategoryType.OTHER -> Color(0xFF94A3B8)
}
