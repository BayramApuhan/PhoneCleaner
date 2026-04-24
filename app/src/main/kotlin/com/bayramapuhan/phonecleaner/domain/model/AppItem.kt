package com.bayramapuhan.phonecleaner.domain.model

data class AppItem(
    val packageName: String,
    val label: String,
    val versionName: String?,
    val sizeBytes: Long,
    val isSystem: Boolean,
    val installedAt: Long,
)
