package com.bayramapuhan.phonecleaner.data.repository

import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import android.os.StatFs
import android.provider.MediaStore
import com.bayramapuhan.phonecleaner.domain.model.CategoryType
import com.bayramapuhan.phonecleaner.domain.model.StorageCategory
import com.bayramapuhan.phonecleaner.domain.model.StorageInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    suspend fun load(): StorageInfo = withContext(Dispatchers.IO) {
        val stat = StatFs(Environment.getDataDirectory().path)
        val total = stat.blockCountLong * stat.blockSizeLong
        val free = stat.availableBlocksLong * stat.blockSizeLong

        val images = sumMediaSize(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.Images.Media.SIZE)
        val videos = sumMediaSize(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, MediaStore.Video.Media.SIZE)
        val audio = sumMediaSize(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MediaStore.Audio.Media.SIZE)
        val apps = sumInstalledAppsSize()

        val categorized = images + videos + audio + apps
        val other = (total - free - categorized).coerceAtLeast(0)

        StorageInfo(
            totalBytes = total,
            freeBytes = free,
            categories = listOf(
                StorageCategory(CategoryType.IMAGES, images),
                StorageCategory(CategoryType.VIDEOS, videos),
                StorageCategory(CategoryType.AUDIO, audio),
                StorageCategory(CategoryType.APPS, apps),
                StorageCategory(CategoryType.OTHER, other),
            ),
        )
    }

    private fun sumMediaSize(uri: android.net.Uri, sizeColumn: String): Long {
        val projection = arrayOf(sizeColumn)
        return runCatching {
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                val idx = cursor.getColumnIndexOrThrow(sizeColumn)
                var total = 0L
                while (cursor.moveToNext()) total += cursor.getLong(idx)
                total
            } ?: 0L
        }.getOrDefault(0L)
    }

    private fun sumInstalledAppsSize(): Long {
        val pm = context.packageManager
        val flags = PackageManager.GET_META_DATA
        val apps = runCatching { pm.getInstalledApplications(flags) }.getOrDefault(emptyList())
        return apps.sumOf { info ->
            runCatching { File(info.sourceDir).length() }.getOrDefault(0L)
        }
    }
}
