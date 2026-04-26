package com.bayramapuhan.phonecleaner.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.bayramapuhan.phonecleaner.domain.model.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "phonecleaner_prefs")

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val LARGE_FILE_THRESHOLD_MB = intPreferencesKey("large_file_threshold_mb")
        val HIDE_SYSTEM_APPS = booleanPreferencesKey("hide_system_apps")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val LANGUAGE = stringPreferencesKey("language")
        val PASSWORD_ENABLED = booleanPreferencesKey("password_enabled")
        val PASSWORD_HASH = stringPreferencesKey("password_hash")
        val RECOVERY_EMAIL = stringPreferencesKey("recovery_email")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        runCatching { ThemeMode.valueOf(prefs[Keys.THEME_MODE] ?: ThemeMode.SYSTEM.name) }
            .getOrDefault(ThemeMode.SYSTEM)
    }

    val largeFileThresholdMb: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.LARGE_FILE_THRESHOLD_MB] ?: 50
    }

    val hideSystemApps: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.HIDE_SYSTEM_APPS] ?: true
    }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.NOTIFICATIONS_ENABLED] ?: true
    }

    val language: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.LANGUAGE] ?: "system"
    }

    val passwordEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.PASSWORD_ENABLED] ?: false
    }

    val passwordHash: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[Keys.PASSWORD_HASH]
    }

    val recoveryEmail: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.RECOVERY_EMAIL] ?: ""
    }

    val biometricEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.BIOMETRIC_ENABLED] ?: false
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setLargeFileThresholdMb(mb: Int) {
        context.dataStore.edit { it[Keys.LARGE_FILE_THRESHOLD_MB] = mb.coerceIn(1, 5000) }
    }

    suspend fun setHideSystemApps(hide: Boolean) {
        context.dataStore.edit { it[Keys.HIDE_SYSTEM_APPS] = hide }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setLanguage(code: String) {
        context.dataStore.edit { it[Keys.LANGUAGE] = code }
    }

    suspend fun setPasswordEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.PASSWORD_ENABLED] = enabled }
    }

    suspend fun setPasswordHash(hash: String?) {
        context.dataStore.edit {
            if (hash == null) it.remove(Keys.PASSWORD_HASH) else it[Keys.PASSWORD_HASH] = hash
        }
    }

    suspend fun setRecoveryEmail(email: String) {
        context.dataStore.edit { it[Keys.RECOVERY_EMAIL] = email }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.BIOMETRIC_ENABLED] = enabled }
    }
}
