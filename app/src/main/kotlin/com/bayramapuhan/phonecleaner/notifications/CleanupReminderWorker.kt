package com.bayramapuhan.phonecleaner.notifications

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.bayramapuhan.phonecleaner.data.preferences.AppPreferences
import com.bayramapuhan.phonecleaner.data.repository.StorageRepository
import com.bayramapuhan.phonecleaner.util.formatSize
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class CleanupReminderWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WorkerDeps {
        fun appPreferences(): AppPreferences
        fun storageRepository(): StorageRepository
    }

    override suspend fun doWork(): Result {
        val deps = EntryPointAccessors.fromApplication(applicationContext, WorkerDeps::class.java)
        if (!deps.appPreferences().notificationsEnabled.first()) return Result.success()

        return runCatching {
            val info = deps.storageRepository().load()
            val total = info.totalBytes.coerceAtLeast(1)
            val freeRatio = info.freeBytes.toDouble() / total
            if (freeRatio < 0.10) {
                NotificationHelper.showStorageLow(applicationContext, info.freeBytes.formatSize())
            }
            Result.success()
        }.getOrElse { Result.retry() }
    }

    companion object {
        const val UNIQUE_NAME = "cleanup_reminder_daily"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()
            val request = PeriodicWorkRequestBuilder<CleanupReminderWorker>(1, TimeUnit.DAYS)
                .setConstraints(constraints)
                .setInitialDelay(6, TimeUnit.HOURS)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(UNIQUE_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_NAME)
        }
    }
}
