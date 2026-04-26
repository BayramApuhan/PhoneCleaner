package com.bayramapuhan.phonecleaner.ui.lock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.bayramapuhan.phonecleaner.R
import kotlinx.coroutines.launch

@Composable
fun LockScreen(
    onUnlocked: () -> Unit,
    vm: LockViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val scope = rememberCoroutineScope()

    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var biometricAttempted by remember { mutableStateOf(false) }

    val biometricTitle = stringResource(R.string.lock_biometric_title)
    val biometricSubtitle = stringResource(R.string.lock_biometric_subtitle)
    val wrongPwd = stringResource(R.string.lock_wrong_password)

    fun tryBiometric() {
        val act = activity ?: return
        if (!state.biometricEnabled) return
        if (!isBiometricAvailable(act)) return
        showBiometricPrompt(
            activity = act,
            title = biometricTitle,
            subtitle = biometricSubtitle,
            onSuccess = onUnlocked,
            onFailure = { msg ->
                if (msg != null) error = msg.toString()
            },
        )
    }

    LaunchedEffect(state.biometricEnabled, state.initializing) {
        if (!state.initializing && state.biometricEnabled && !biometricAttempted) {
            biometricAttempted = true
            tryBiometric()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .padding(8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                stringResource(R.string.lock_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    error = null
                },
                placeholder = { Text(stringResource(R.string.lock_password_placeholder)) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                isError = error != null,
                supportingText = error?.let { msg -> { Text(msg) } },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    scope.launch {
                        if (vm.verify(password)) {
                            onUnlocked()
                        } else {
                            error = wrongPwd
                            password = ""
                        }
                    }
                },
                enabled = password.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.lock_unlock)) }

            if (state.biometricEnabled && activity != null && isBiometricAvailable(activity)) {
                Spacer(Modifier.height(24.dp))
                IconButton(onClick = { tryBiometric() }) {
                    Icon(
                        Icons.Default.Fingerprint,
                        contentDescription = stringResource(R.string.lock_use_biometric),
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    stringResource(R.string.lock_use_biometric),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
