package com.bayramapuhan.phonecleaner.domain.model

data class DeleteResult(
    val deletedCount: Int,
    val bytesFreed: Long,
)
