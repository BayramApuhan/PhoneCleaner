package com.bayramapuhan.phonecleaner.domain.model

import android.net.Uri

sealed interface CleanableItem {
    val key: String
    val name: String
    val sizeBytes: Long
    val category: Category

    enum class Category { PHOTO, VIDEO, AUDIO, APK, LARGE_FILE }

    data class Media(
        override val key: String,
        override val name: String,
        override val sizeBytes: Long,
        override val category: Category,
        val mediaId: Long,
        val uri: Uri,
    ) : CleanableItem

    data class Disk(
        override val key: String,
        override val name: String,
        override val sizeBytes: Long,
        override val category: Category,
        val path: String,
    ) : CleanableItem
}
