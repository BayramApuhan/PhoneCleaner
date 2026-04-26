package com.bayramapuhan.phonecleaner.ui.lock

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

private const val ALLOWED = BiometricManager.Authenticators.BIOMETRIC_WEAK or
    BiometricManager.Authenticators.DEVICE_CREDENTIAL

fun isBiometricAvailable(context: Context): Boolean {
    val mgr = BiometricManager.from(context)
    return mgr.canAuthenticate(ALLOWED) == BiometricManager.BIOMETRIC_SUCCESS
}

fun showBiometricPrompt(
    activity: FragmentActivity,
    title: String,
    subtitle: String,
    onSuccess: () -> Unit,
    onFailure: (CharSequence?) -> Unit,
) {
    val executor = ContextCompat.getMainExecutor(activity)
    val prompt = BiometricPrompt(
        activity,
        executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                    errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON
                ) {
                    onFailure(errString)
                } else {
                    onFailure(null)
                }
            }
        },
    )
    val info = BiometricPrompt.PromptInfo.Builder()
        .setTitle(title)
        .setSubtitle(subtitle)
        .setAllowedAuthenticators(ALLOWED)
        .build()
    prompt.authenticate(info)
}
