package com.bayramapuhan.phonecleaner.data.repository

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.bayramapuhan.phonecleaner.domain.model.MemoryInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoryRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun load(): MemoryInfo {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val info = ActivityManager.MemoryInfo()
        am.getMemoryInfo(info)
        return MemoryInfo(
            totalBytes = info.totalMem,
            availableBytes = info.availMem,
            thresholdBytes = info.threshold,
            lowMemory = info.lowMemory,
        )
    }

    fun openSystemAppList(): Boolean {
        val intent = Intent(Settings.ACTION_APPLICATION_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return runCatching { context.startActivity(intent) }.isSuccess
    }
}
