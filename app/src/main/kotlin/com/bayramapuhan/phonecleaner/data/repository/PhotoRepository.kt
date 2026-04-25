package com.bayramapuhan.phonecleaner.data.repository

import android.content.ContentUris
import android.content.Context
import android.content.IntentSender
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.bayramapuhan.phonecleaner.domain.model.Photo
import com.bayramapuhan.phonecleaner.util.PhotoHash
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    suspend fun loadAllPhotos(): List<Photo> = loadFromCollection(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    suspend fun loadAllVideos(): List<Photo> = loadFromCollection(MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
    suspend fun loadAllAudio(): List<Photo> = loadFromCollection(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)

    private suspend fun loadFromCollection(collection: Uri): List<Photo> = withContext(Dispatchers.IO) {
        val list = mutableListOf<Photo>()
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.DATE_ADDED,
        )
        runCatching {
            context.contentResolver.query(
                collection,
                projection,
                null,
                null,
                "${MediaStore.MediaColumns.DATE_ADDED} DESC",
            )
        }.getOrNull()?.use { c ->
            val idCol = c.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val nameCol = c.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
            val sizeCol = c.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
            val dateCol = c.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED)
            while (c.moveToNext()) {
                val id = c.getLong(idCol)
                list += Photo(
                    id = id,
                    uri = ContentUris.withAppendedId(collection, id),
                    displayName = c.getString(nameCol) ?: "",
                    sizeBytes = c.getLong(sizeCol),
                    dateAdded = c.getLong(dateCol),
                )
            }
        }
        list
    }

    suspend fun computeHashes(
        photos: List<Photo>,
        onProgress: (current: Int, total: Int) -> Unit = { _, _ -> },
    ): Map<Long, Long> = withContext(Dispatchers.IO) {
        val result = mutableMapOf<Long, Long>()
        photos.forEachIndexed { index, photo ->
            computeHash(photo.uri)?.let { result[photo.id] = it }
            if (index % 10 == 0) onProgress(index, photos.size)
        }
        onProgress(photos.size, photos.size)
        result
    }

    private fun computeHash(uri: Uri): Long? = runCatching {
        context.contentResolver.openInputStream(uri)?.use { stream ->
            val opts = BitmapFactory.Options().apply { inSampleSize = 8 }
            val bitmap = BitmapFactory.decodeStream(stream, null, opts)
            bitmap?.let {
                val hash = PhotoHash.compute(it)
                it.recycle()
                hash
            }
        }
    }.getOrNull()

    fun groupDuplicates(hashes: Map<Long, Long>, threshold: Int = 10): List<List<Long>> {
        val groups = mutableListOf<List<Long>>()
        val visited = mutableSetOf<Long>()
        val entries = hashes.entries.toList()
        for ((idA, hashA) in entries) {
            if (idA in visited) continue
            val group = mutableListOf(idA)
            visited += idA
            for ((idB, hashB) in entries) {
                if (idB in visited) continue
                if (PhotoHash.hammingDistance(hashA, hashB) <= threshold) {
                    group += idB
                    visited += idB
                }
            }
            if (group.size > 1) groups += group
        }
        return groups
    }

    fun deletePhotosIntent(uris: List<Uri>): IntentSender? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            MediaStore.createDeleteRequest(context.contentResolver, uris).intentSender
        } else {
            uris.forEach { runCatching { context.contentResolver.delete(it, null, null) } }
            null
        }
    }
}
