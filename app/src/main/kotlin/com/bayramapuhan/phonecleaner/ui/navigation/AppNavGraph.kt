package com.bayramapuhan.phonecleaner.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bayramapuhan.phonecleaner.ui.screens.apk.ApkScreen
import com.bayramapuhan.phonecleaner.ui.screens.apps.AppsScreen
import com.bayramapuhan.phonecleaner.ui.screens.home.HomeScreen
import com.bayramapuhan.phonecleaner.ui.screens.largefiles.LargeFilesScreen
import com.bayramapuhan.phonecleaner.ui.screens.settings.SettingsScreen
import com.bayramapuhan.phonecleaner.ui.screens.storage.StorageScreen

object Routes {
    const val HOME = "home"
    const val STORAGE = "storage"
    const val LARGE_FILES = "large_files"
    const val APPS = "apps"
    const val APK = "apk"
    const val SETTINGS = "settings"
}

@Composable
fun AppNavGraph() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onOpenStorage = { nav.navigate(Routes.STORAGE) },
                onOpenLargeFiles = { nav.navigate(Routes.LARGE_FILES) },
                onOpenApps = { nav.navigate(Routes.APPS) },
                onOpenApk = { nav.navigate(Routes.APK) },
                onOpenSettings = { nav.navigate(Routes.SETTINGS) },
            )
        }
        composable(Routes.STORAGE) { StorageScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.LARGE_FILES) { LargeFilesScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.APPS) { AppsScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.APK) { ApkScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.SETTINGS) { SettingsScreen(onBack = { nav.popBackStack() }) }
    }
}
