package com.bayramapuhan.phonecleaner

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.VideoFrameDecoder
import com.bayramapuhan.phonecleaner.data.preferences.AppPreferences
import com.bayramapuhan.phonecleaner.notifications.CleanupReminderWorker
import com.bayramapuhan.phonecleaner.notifications.NotificationHelper
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@HiltAndroidApp
class PhoneCleanerApp : Application(), ImageLoaderFactory {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface PrefsEntryPoint {
        fun appPreferences(): AppPreferences
    }

    override fun onCreate() {
        super.onCreate()
        val prefs = EntryPointAccessors.fromApplication(this, PrefsEntryPoint::class.java)
            .appPreferences()
        val code = runBlocking { prefs.language.first() }
        applyLocale(code)

        NotificationHelper.ensureChannels(this)
        val notificationsOn = runBlocking { prefs.notificationsEnabled.first() }
        if (notificationsOn) {
            CleanupReminderWorker.schedule(this)
        } else {
            CleanupReminderWorker.cancel(this)
        }
    }

    override fun newImageLoader(): ImageLoader = ImageLoader.Builder(this)
        .components { add(VideoFrameDecoder.Factory()) }
        .crossfade(true)
        .build()

    companion object {
        fun applyLocale(code: String) {
            val tags = if (code == "system" || code.isBlank()) "" else code
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tags))
        }
    }
}
