package com.bayramapuhan.phonecleaner.domain.model

data class FileItem(
    val path: String,
    val name: String,
    val sizeBytes: Long,
    val lastModified: Long,
    val mimeType: String? = null,
)
