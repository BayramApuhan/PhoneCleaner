package com.bayramapuhan.phonecleaner.domain.model

data class StorageInfo(
    val totalBytes: Long,
    val freeBytes: Long,
    val categories: List<StorageCategory>,
) {
    val usedBytes: Long get() = totalBytes - freeBytes
}

data class StorageCategory(
    val type: CategoryType,
    val sizeBytes: Long,
)

enum class CategoryType { IMAGES, VIDEOS, AUDIO, APPS, OTHER }
