package com.bayramapuhan.phonecleaner.domain.model

data class MemoryInfo(
    val totalBytes: Long,
    val availableBytes: Long,
    val thresholdBytes: Long,
    val lowMemory: Boolean,
) {
    val usedBytes: Long get() = totalBytes - availableBytes
    val usedFraction: Float get() = if (totalBytes > 0) usedBytes.toFloat() / totalBytes else 0f
}
