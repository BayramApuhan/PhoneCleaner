package com.bayramapuhan.phonecleaner.util

import java.util.Locale
import kotlin.math.ln
import kotlin.math.pow

fun Long.formatSize(): String {
    if (this <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (ln(this.toDouble()) / ln(1024.0)).toInt().coerceAtMost(units.lastIndex)
    val value = this / 1024.0.pow(digitGroups.toDouble())
    return String.format(Locale.getDefault(), "%.1f %s", value, units[digitGroups])
}
