package com.bayramapuhan.phonecleaner.data.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.bayramapuhan.phonecleaner.domain.model.AppItem
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    suspend fun loadInstalledApps(includeSystem: Boolean = false): List<AppItem> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val infos = runCatching { pm.getInstalledPackages(PackageManager.GET_META_DATA) }
            .getOrDefault(emptyList())
        infos.mapNotNull { pkg ->
            val app = pkg.applicationInfo ?: return@mapNotNull null
            val isSystem = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            if (!includeSystem && isSystem) return@mapNotNull null
            AppItem(
                packageName = pkg.packageName,
                label = app.loadLabel(pm).toString(),
                versionName = pkg.versionName,
                sizeBytes = runCatching { File(app.sourceDir).length() }.getOrDefault(0L),
                isSystem = isSystem,
                installedAt = pkg.firstInstallTime,
            )
        }.sortedByDescending { it.sizeBytes }
    }
}
