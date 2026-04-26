package com.bayramapuhan.phonecleaner.ui.screens.apps

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bayramapuhan.phonecleaner.R
import com.bayramapuhan.phonecleaner.ui.components.AppIcon
import com.bayramapuhan.phonecleaner.util.formatSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsScreen(
    onBack: () -> Unit,
    vm: AppsViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()
    val uninstallLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { vm.load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.apps_title)) },
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
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
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

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = state.sort == AppSort.SIZE,
                    onClick = { vm.setSort(AppSort.SIZE) },
                    label = { Text(stringResource(R.string.apps_sort_size)) },
                )
                FilterChip(
                    selected = state.sort == AppSort.NAME,
                    onClick = { vm.setSort(AppSort.NAME) },
                    label = { Text(stringResource(R.string.apps_sort_name)) },
                )
            }

            when {
                state.loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(8.dp))
                        Text(stringResource(R.string.apps_loading))
                    }
                }
                else -> LazyColumn {
                    items(state.visibleApps, key = { it.packageName }) { app ->
                        ListItem(
                            leadingContent = {
                                AppIcon(packageName = app.packageName, modifier = Modifier.size(40.dp))
                            },
                            headlineContent = { Text(app.label) },
                            supportingContent = { Text("${app.packageName} · ${app.sizeBytes.formatSize()}") },
                            trailingContent = {
                                TextButton(onClick = {
                                    val intent = Intent(
                                        Intent.ACTION_DELETE,
                                        Uri.parse("package:${app.packageName}"),
                                    )
                                    uninstallLauncher.launch(intent)
                                }) {
                                    Text(stringResource(R.string.apps_uninstall))
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}
