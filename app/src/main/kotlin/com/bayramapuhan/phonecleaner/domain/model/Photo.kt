package com.bayramapuhan.phonecleaner.domain.model

import android.net.Uri

data class Photo(
    val id: Long,
    val uri: Uri,
    val displayName: String,
    val sizeBytes: Long,
    val dateAdded: Long,
)
