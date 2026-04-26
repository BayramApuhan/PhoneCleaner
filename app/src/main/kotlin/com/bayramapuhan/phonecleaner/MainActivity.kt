package com.bayramapuhan.phonecleaner

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.bayramapuhan.phonecleaner.data.preferences.AppPreferences
import com.bayramapuhan.phonecleaner.domain.model.ThemeMode
import com.bayramapuhan.phonecleaner.ui.lock.LockScreen
import com.bayramapuhan.phonecleaner.ui.lock.LockViewModel
import com.bayramapuhan.phonecleaner.ui.navigation.AppNavGraph
import com.bayramapuhan.phonecleaner.ui.onboarding.OnboardingScreen
import com.bayramapuhan.phonecleaner.ui.onboarding.OnboardingViewModel
import com.bayramapuhan.phonecleaner.ui.theme.PhoneCleanerTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject lateinit var prefs: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by prefs.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            PhoneCleanerTheme(themeMode = themeMode) {
                AppRoot()
            }
        }
    }
}

@Composable
private fun AppRoot(
    lockVm: LockViewModel = hiltViewModel(),
    onboardingVm: OnboardingViewModel = hiltViewModel(),
) {
    val lockState by lockVm.state.collectAsState()
    val onboardingDone by onboardingVm.completed.collectAsState()
    var unlocked by rememberSaveable { mutableStateOf(false) }
    var lockSnapshot by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(lockState.initializing) {
        if (!lockState.initializing && lockSnapshot == null) {
            lockSnapshot = lockState.lockRequired
        }
    }

    when {
        lockSnapshot == null || onboardingDone == null -> Unit
        lockSnapshot == true && !unlocked -> LockScreen(onUnlocked = { unlocked = true })
        onboardingDone == false -> OnboardingScreen(onComplete = { onboardingVm.complete() })
        else -> AppNavGraph()
    }
}
