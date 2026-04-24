package com.bayramapuhan.phonecleaner.data.repository

import android.content.Context
import android.os.Environment
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

    suspend fun delete(paths: List<String>): Int = withContext(Dispatchers.IO) {
        paths.count { runCatching { File(it).delete() }.getOrDefault(false) }
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
}
