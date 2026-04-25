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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bayramapuhan.phonecleaner.R
import com.bayramapuhan.phonecleaner.domain.model.StorageCategory
import com.bayramapuhan.phonecleaner.domain.model.StorageInfo
import com.bayramapuhan.phonecleaner.ui.components.DonutChart
import com.bayramapuhan.phonecleaner.ui.components.DonutSegment
import com.bayramapuhan.phonecleaner.ui.components.color
import com.bayramapuhan.phonecleaner.ui.components.label
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
            modifier = Modifier.fillMaxSize().padding(padding),
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
    val scroll = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(16.dp),
    ) {
        DonutSummaryCard(info)
        Spacer(Modifier.height(20.dp))
        Text(
            stringResource(R.string.storage_categories_label),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 4.dp),
        )
        Spacer(Modifier.height(12.dp))
        info.categories.forEach { cat ->
            CategoryRow(cat, info.totalBytes)
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun DonutSummaryCard(info: StorageInfo) {
    val usedFraction = if (info.totalBytes > 0) info.usedBytes.toFloat() / info.totalBytes else 0f
    val percent = (usedFraction * 100).toInt()
    val segments = remember(info) {
        info.categories.map { DonutSegment(it.sizeBytes.toFloat(), it.type.color()) } +
            DonutSegment(info.freeBytes.toFloat(), androidx.compose.ui.graphics.Color(0xFF334155))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            DonutChart(
                segments = segments,
                modifier = Modifier.size(220.dp),
                strokeWidth = 28.dp,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "$percent%",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        stringResource(R.string.storage_used),
                        style = MaterialTheme.typography.labelMedium,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                StatColumn(
                    label = stringResource(R.string.storage_used),
                    value = info.usedBytes.formatSize(),
                )
                StatColumn(
                    label = stringResource(R.string.storage_free),
                    value = info.freeBytes.formatSize(),
                )
                StatColumn(
                    label = stringResource(R.string.storage_total),
                    value = info.totalBytes.formatSize(),
                )
            }
        }
    }
}

@Composable
private fun StatColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CategoryRow(cat: StorageCategory, total: Long) {
    val fraction = if (total > 0) cat.sizeBytes.toFloat() / total else 0f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(cat.type.color().copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(cat.type.color()),
            )
        }
        Spacer(Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(cat.type.label(), style = MaterialTheme.typography.titleMedium)
                Text(
                    cat.sizeBytes.formatSize(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { fraction.coerceAtMost(1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = cat.type.color(),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    }
}
