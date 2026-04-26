package com.bayramapuhan.phonecleaner.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bayramapuhan.phonecleaner.domain.model.CategoryType
import com.bayramapuhan.phonecleaner.ui.screens.about.AboutScreen
import com.bayramapuhan.phonecleaner.ui.screens.apk.ApkScreen
import com.bayramapuhan.phonecleaner.ui.screens.appearance.AppearanceScreen
import com.bayramapuhan.phonecleaner.ui.screens.apps.AppsScreen
import com.bayramapuhan.phonecleaner.ui.screens.faq.FaqScreen
import com.bayramapuhan.phonecleaner.ui.screens.feedback.FeedbackScreen
import com.bayramapuhan.phonecleaner.ui.screens.home.HomeScreen
import com.bayramapuhan.phonecleaner.ui.screens.language.LanguageScreen
import com.bayramapuhan.phonecleaner.ui.screens.largefiles.LargeFilesScreen
import com.bayramapuhan.phonecleaner.ui.screens.medialist.MediaListScreen
import com.bayramapuhan.phonecleaner.ui.screens.memory.MemoryScreen
import com.bayramapuhan.phonecleaner.ui.screens.other.OtherFilesScreen
import com.bayramapuhan.phonecleaner.ui.screens.password.ChangePasswordScreen
import com.bayramapuhan.phonecleaner.ui.screens.photos.PhotosScreen
import com.bayramapuhan.phonecleaner.ui.screens.quickclean.QuickCleanScreen
import com.bayramapuhan.phonecleaner.ui.screens.recovery.RecoveryEmailScreen
import com.bayramapuhan.phonecleaner.ui.screens.settings.SettingsScreen
import com.bayramapuhan.phonecleaner.ui.screens.storage.StorageScreen

object Routes {
    const val HOME = "home"
    const val STORAGE = "storage"
    const val PHOTOS = "photos"
    const val MEDIA = "media/{type}"
    const val LARGE_FILES = "large_files"
    const val APPS = "apps"
    const val APK = "apk"
    const val OTHER = "other"
    const val MEMORY = "memory"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
    const val APPEARANCE = "appearance"
    const val LANGUAGE = "language"
    const val CHANGE_PASSWORD = "change_password"
    const val RECOVERY_EMAIL = "recovery_email"
    const val FAQ = "faq"
    const val FEEDBACK = "feedback"
    const val QUICK_CLEAN = "quick_clean"

    fun mediaRoute(type: String) = "media/$type"
}

@Composable
fun AppNavGraph() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onOpenStorage = { nav.navigate(Routes.STORAGE) },
                onOpenPhotos = { nav.navigate(Routes.PHOTOS) },
                onOpenLargeFiles = { nav.navigate(Routes.LARGE_FILES) },
                onOpenApps = { nav.navigate(Routes.APPS) },
                onOpenApk = { nav.navigate(Routes.APK) },
                onOpenMemory = { nav.navigate(Routes.MEMORY) },
                onOpenSettings = { nav.navigate(Routes.SETTINGS) },
                onOpenQuickClean = { nav.navigate(Routes.QUICK_CLEAN) },
                onOpenAppearance = { nav.navigate(Routes.APPEARANCE) },
                onOpenLanguage = { nav.navigate(Routes.LANGUAGE) },
                onOpenChangePassword = { nav.navigate(Routes.CHANGE_PASSWORD) },
                onOpenRecoveryEmail = { nav.navigate(Routes.RECOVERY_EMAIL) },
                onOpenAbout = { nav.navigate(Routes.ABOUT) },
                onOpenFaq = { nav.navigate(Routes.FAQ) },
                onOpenFeedback = { nav.navigate(Routes.FEEDBACK) },
            )
        }
        composable(Routes.QUICK_CLEAN) {
            QuickCleanScreen(
                onBack = { nav.popBackStack() },
                onOpenDuplicates = { nav.navigate(Routes.PHOTOS) },
                onOpenLargeFiles = { nav.navigate(Routes.LARGE_FILES) },
                onOpenApk = { nav.navigate(Routes.APK) },
                onOpenApps = { nav.navigate(Routes.APPS) },
                onOpenVideos = { nav.navigate(Routes.mediaRoute("videos")) },
                onOpenAudio = { nav.navigate(Routes.mediaRoute("audio")) },
                onOpenPhotos = { nav.navigate(Routes.PHOTOS) },
            )
        }
        composable(Routes.STORAGE) {
            StorageScreen(
                onBack = { nav.popBackStack() },
                onOpenCategory = { type ->
                    when (type) {
                        CategoryType.IMAGES -> nav.navigate(Routes.PHOTOS)
                        CategoryType.VIDEOS -> nav.navigate(Routes.mediaRoute("videos"))
                        CategoryType.AUDIO -> nav.navigate(Routes.mediaRoute("audio"))
                        CategoryType.APPS -> nav.navigate(Routes.APPS)
                        CategoryType.OTHER -> nav.navigate(Routes.OTHER)
                    }
                },
            )
        }
        composable(Routes.PHOTOS) { PhotosScreen(onBack = { nav.popBackStack() }) }
        composable(
            route = Routes.MEDIA,
            arguments = listOf(navArgument("type") { type = NavType.StringType }),
        ) {
            MediaListScreen(onBack = { nav.popBackStack() })
        }
        composable(Routes.LARGE_FILES) { LargeFilesScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.APPS) { AppsScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.APK) { ApkScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.OTHER) { OtherFilesScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.MEMORY) { MemoryScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { nav.popBackStack() },
                onOpenAbout = { nav.navigate(Routes.ABOUT) },
            )
        }
        composable(Routes.ABOUT) { AboutScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.APPEARANCE) { AppearanceScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.LANGUAGE) { LanguageScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.CHANGE_PASSWORD) {
            ChangePasswordScreen(
                onBack = { nav.popBackStack() },
                onSaved = { nav.popBackStack() },
            )
        }
        composable(Routes.RECOVERY_EMAIL) { RecoveryEmailScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.FAQ) { FaqScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.FEEDBACK) { FeedbackScreen(onBack = { nav.popBackStack() }) }
    }
}
