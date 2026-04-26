package com.bayramapuhan.phonecleaner.ui.drawer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bayramapuhan.phonecleaner.R
import com.bayramapuhan.phonecleaner.domain.model.ThemeMode

@Composable
fun DrawerContent(
    onClose: () -> Unit,
    onNavigateAppearance: () -> Unit,
    onNavigateLanguage: () -> Unit,
    onNavigateChangePassword: () -> Unit,
    onNavigateRecoveryEmail: () -> Unit,
    onNavigateAbout: () -> Unit,
    onNavigateFaq: () -> Unit,
    onNavigateFeedback: () -> Unit,
    vm: DrawerViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()
    var proDialog by remember { mutableStateOf(false) }
    var restoreDialog by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val deniedMsg = stringResource(R.string.notif_permission_denied)
    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            vm.setNotifications(true)
        } else {
            Toast.makeText(context, deniedMsg, Toast.LENGTH_LONG).show()
        }
    }
    val onToggleNotifications: (Boolean) -> Unit = { target ->
        if (!target) {
            vm.setNotifications(false)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
            if (granted) vm.setNotifications(true)
            else notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            vm.setNotifications(true)
        }
    }

    ModalDrawerSheet {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(vertical = 12.dp),
        ) {
            DrawerHeader(onClose = onClose)
            ProCard(
                onMoreInfo = { proDialog = true },
                onRestore = { restoreDialog = true },
            )

            Spacer(Modifier.height(8.dp))
            DrawerSection(stringResource(R.string.drawer_section_general)) {
                DrawerSwitchRow(
                    icon = Icons.Default.Notifications,
                    label = stringResource(R.string.drawer_notifications),
                    checked = state.notificationsEnabled,
                    onCheckedChange = onToggleNotifications,
                )
                DrawerLinkRow(
                    icon = Icons.Default.Palette,
                    label = stringResource(R.string.drawer_appearance),
                    sublabel = state.themeMode.label(),
                    onClick = onNavigateAppearance,
                )
                DrawerLinkRow(
                    icon = Icons.Default.Language,
                    label = stringResource(R.string.drawer_language),
                    sublabel = languageLabel(state.language),
                    onClick = onNavigateLanguage,
                )
            }

            DrawerSection(stringResource(R.string.drawer_section_private)) {
                DrawerSwitchRow(
                    icon = Icons.Default.Lock,
                    label = stringResource(R.string.drawer_password),
                    checked = state.passwordEnabled,
                    onCheckedChange = { enable ->
                        if (enable) {
                            onNavigateChangePassword()
                        } else {
                            vm.setPasswordEnabled(false)
                        }
                    },
                )
                if (state.passwordEnabled) {
                    DrawerLinkRow(
                        icon = Icons.Default.Key,
                        label = stringResource(R.string.drawer_change_password),
                        onClick = onNavigateChangePassword,
                    )
                    DrawerLinkRow(
                        icon = Icons.Default.Email,
                        label = stringResource(R.string.drawer_recovery_email),
                        onClick = onNavigateRecoveryEmail,
                    )
                    DrawerSwitchRow(
                        icon = Icons.Default.Fingerprint,
                        label = stringResource(R.string.drawer_biometric),
                        checked = state.biometricEnabled,
                        enabled = state.hasPasswordSet,
                        onCheckedChange = vm::setBiometric,
                    )
                }
            }

            DrawerSection(stringResource(R.string.drawer_section_about)) {
                DrawerLinkRow(
                    icon = Icons.Default.Star,
                    label = stringResource(R.string.drawer_rate),
                    onClick = {
                        val pkg = context.packageName.removeSuffix(".debug")
                        runCatching { uriHandler.openUri("market://details?id=$pkg") }
                            .onFailure { uriHandler.openUri("https://play.google.com/store/apps/details?id=$pkg") }
                    },
                )
                DrawerLinkRow(
                    icon = Icons.AutoMirrored.Filled.HelpOutline,
                    label = stringResource(R.string.drawer_faq),
                    onClick = onNavigateFaq,
                )
                DrawerLinkRow(
                    icon = Icons.Default.Email,
                    label = stringResource(R.string.drawer_feedback),
                    onClick = onNavigateFeedback,
                )
                DrawerLinkRow(
                    icon = Icons.Default.Info,
                    label = stringResource(R.string.settings_about),
                    onClick = onNavigateAbout,
                )
            }
        }
    }

    if (proDialog) {
        AlertDialog(
            onDismissRequest = { proDialog = false },
            title = { Text(stringResource(R.string.pro_dialog_title)) },
            text = { Text(stringResource(R.string.pro_dialog_body)) },
            confirmButton = {
                TextButton(onClick = { proDialog = false }) { Text(stringResource(android.R.string.ok)) }
            },
        )
    }
    if (restoreDialog) {
        AlertDialog(
            onDismissRequest = { restoreDialog = false },
            title = { Text(stringResource(R.string.pro_restore_dialog_title)) },
            text = { Text(stringResource(R.string.pro_restore_dialog_body)) },
            confirmButton = {
                TextButton(onClick = { restoreDialog = false }) { Text(stringResource(android.R.string.ok)) }
            },
        )
    }
}

@Composable
private fun DrawerHeader(onClose: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClose)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.CleaningServices,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(Modifier.size(12.dp))
        Text(
            stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onClose) {
            Icon(
                Icons.Default.Close,
                contentDescription = stringResource(R.string.drawer_close),
            )
        }
    }
}

@Composable
private fun ProCard(onMoreInfo: () -> Unit, onRestore: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(16.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.WorkspacePremium,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    stringResource(R.string.drawer_pro_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(R.string.drawer_pro_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onMoreInfo,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                ) { Text(stringResource(R.string.drawer_pro_more_info)) }
                OutlinedButton(
                    onClick = onRestore,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                ) { Text(stringResource(R.string.drawer_pro_restore)) }
            }
        }
    }
}

@Composable
private fun DrawerSection(title: String, content: @Composable () -> Unit) {
    Spacer(Modifier.height(16.dp))
    Text(
        title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 20.dp, bottom = 4.dp),
    )
    content()
}

@Composable
private fun DrawerLinkRow(
    icon: ImageVector,
    label: String,
    sublabel: String? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.size(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            if (sublabel != null) {
                Text(
                    sublabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun DrawerSwitchRow(
    icon: ImageVector,
    label: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else Color.Gray,
        )
        Spacer(Modifier.size(16.dp))
        Text(
            label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}

@Composable
private fun ThemeMode.label(): String = stringResource(
    when (this) {
        ThemeMode.SYSTEM -> R.string.theme_system
        ThemeMode.LIGHT -> R.string.theme_light
        ThemeMode.DARK -> R.string.theme_dark
    },
)

@Composable
private fun languageLabel(code: String): String = stringResource(
    when (code) {
        "tr" -> R.string.language_tr
        "en" -> R.string.language_en
        else -> R.string.language_system
    },
)
