package com.bayramapuhan.phonecleaner.data.repository

import com.bayramapuhan.phonecleaner.data.preferences.AppPreferences
import com.bayramapuhan.phonecleaner.domain.model.CleanableItem
import com.bayramapuhan.phonecleaner.domain.model.FileItem
import com.bayramapuhan.phonecleaner.domain.model.Photo
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CleanableRepository @Inject constructor(
    private val photos: PhotoRepository,
    private val files: FileRepository,
    private val prefs: AppPreferences,
) {
    suspend fun loadAll(): List<CleanableItem> = coroutineScope {
        val thresholdMb = prefs.largeFileThresholdMb.first()
        val thresholdBytes = thresholdMb.toLong() * 1024L * 1024L

        val photosDeferred = async { runCatching { photos.loadAllPhotos() }.getOrDefault(emptyList()) }
        val videosDeferred = async { runCatching { photos.loadAllVideos() }.getOrDefault(emptyList()) }
        val audioDeferred = async { runCatching { photos.loadAllAudio() }.getOrDefault(emptyList()) }
        val apksDeferred = async { runCatching { files.findApks() }.getOrDefault(emptyList()) }
        val largeDeferred = async { runCatching { files.findLargeFiles(thresholdBytes) }.getOrDefault(emptyList()) }

        val combined = mutableListOf<CleanableItem>()
        photosDeferred.await().forEach { combined += it.toMedia(CleanableItem.Category.PHOTO) }
        videosDeferred.await().forEach { combined += it.toMedia(CleanableItem.Category.VIDEO) }
        audioDeferred.await().forEach { combined += it.toMedia(CleanableItem.Category.AUDIO) }
        apksDeferred.await().forEach { combined += it.toDisk(CleanableItem.Category.APK) }
        largeDeferred.await().forEach { combined += it.toDisk(CleanableItem.Category.LARGE_FILE) }

        combined.sortedByDescending { it.sizeBytes }
    }
}

private fun Photo.toMedia(category: CleanableItem.Category): CleanableItem.Media =
    CleanableItem.Media(
        key = "${category.name}-$id",
        name = displayName,
        sizeBytes = sizeBytes,
        category = category,
        mediaId = id,
        uri = uri,
    )

private fun FileItem.toDisk(category: CleanableItem.Category): CleanableItem.Disk =
    CleanableItem.Disk(
        key = "${category.name}-$path",
        name = name,
        sizeBytes = sizeBytes,
        category = category,
        path = path,
    )
