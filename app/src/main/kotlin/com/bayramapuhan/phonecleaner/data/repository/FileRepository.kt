package com.bayramapuhan.phonecleaner.data.repository

import android.content.Context
import android.os.Environment
import com.bayramapuhan.phonecleaner.domain.model.DeleteResult
import com.bayramapuhan.phonecleaner.domain.model.FileItem
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    suspend fun findLargeFiles(thresholdBytes: Long): List<FileItem> = withContext(Dispatchers.IO) {
        val root = Environment.getExternalStorageDirectory() ?: return@withContext emptyList()
        val results = mutableListOf<FileItem>()
        walk(root, thresholdBytes, results, depthLimit = 8)
        results.sortedByDescending { it.sizeBytes }
    }

    suspend fun findApks(): List<FileItem> = withContext(Dispatchers.IO) {
        val root = Environment.getExternalStorageDirectory() ?: return@withContext emptyList()
        val results = mutableListOf<FileItem>()
        walkApks(root, results, depthLimit = 8)
        results.sortedByDescending { it.sizeBytes }
    }

    suspend fun findOtherFiles(): List<FileItem> = withContext(Dispatchers.IO) {
        val root = Environment.getExternalStorageDirectory() ?: return@withContext emptyList()
        val results = mutableListOf<FileItem>()
        walkOther(root, results, depthLimit = 8)
        results.sortedByDescending { it.sizeBytes }
    }

    suspend fun delete(paths: List<String>): DeleteResult = withContext(Dispatchers.IO) {
        var count = 0
        var bytes = 0L
        paths.forEach { path ->
            val file = File(path)
            val size = runCatching { file.length() }.getOrDefault(0L)
            if (runCatching { file.delete() }.getOrDefault(false)) {
                count++
                bytes += size
            }
        }
        DeleteResult(count, bytes)
    }

    private fun walk(dir: File, threshold: Long, out: MutableList<FileItem>, depthLimit: Int) {
        if (depthLimit < 0) return
        val children = dir.listFiles() ?: return
        for (child in children) {
            if (child.isDirectory) {
                walk(child, threshold, out, depthLimit - 1)
            } else if (child.length() >= threshold) {
                out += FileItem(
                    path = child.absolutePath,
                    name = child.name,
                    sizeBytes = child.length(),
                    lastModified = child.lastModified(),
                )
            }
        }
    }

    private fun walkApks(dir: File, out: MutableList<FileItem>, depthLimit: Int) {
        if (depthLimit < 0) return
        val children = dir.listFiles() ?: return
        for (child in children) {
            if (child.isDirectory) {
                walkApks(child, out, depthLimit - 1)
            } else if (child.extension.equals("apk", ignoreCase = true)) {
                out += FileItem(
                    path = child.absolutePath,
                    name = child.name,
                    sizeBytes = child.length(),
                    lastModified = child.lastModified(),
                    mimeType = "application/vnd.android.package-archive",
                )
            }
        }
    }

    private fun walkOther(dir: File, out: MutableList<FileItem>, depthLimit: Int) {
        if (depthLimit < 0) return
        val children = dir.listFiles() ?: return
        for (child in children) {
            if (child.isDirectory) {
                walkOther(child, out, depthLimit - 1)
            } else {
                val ext = child.extension.lowercase()
                if (ext !in MEDIA_EXTENSIONS && child.length() > 0) {
                    out += FileItem(
                        path = child.absolutePath,
                        name = child.name,
                        sizeBytes = child.length(),
                        lastModified = child.lastModified(),
                    )
                }
            }
        }
    }

    companion object {
        private val MEDIA_EXTENSIONS = setOf(
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "heic", "heif", "raw",
            "mp4", "mkv", "avi", "mov", "webm", "3gp", "m4v",
            "mp3", "wav", "m4a", "flac", "aac", "ogg", "opus", "wma",
            "apk",
        )
    }
}
