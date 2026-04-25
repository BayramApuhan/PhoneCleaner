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

    suspend fun loadAllPhotos(): List<Photo> = withContext(Dispatchers.IO) {
        val list = mutableListOf<Photo>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.DATE_ADDED,
        )
        runCatching {
            context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                "${MediaStore.Images.Media.DATE_ADDED} DESC",
            )
        }.getOrNull()?.use { c ->
            val idCol = c.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameCol = c.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val sizeCol = c.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val dateCol = c.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            while (c.moveToNext()) {
                val id = c.getLong(idCol)
                list += Photo(
                    id = id,
                    uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id),
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
