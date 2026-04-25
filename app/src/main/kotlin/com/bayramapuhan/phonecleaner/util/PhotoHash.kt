package com.bayramapuhan.phonecleaner.util

import android.graphics.Bitmap

object PhotoHash {

    fun compute(bitmap: Bitmap): Long {
        val scaled = Bitmap.createScaledBitmap(bitmap, 9, 8, true)
        var hash = 0L
        for (y in 0 until 8) {
            var prev = luminance(scaled.getPixel(0, y))
            for (x in 0 until 8) {
                val next = luminance(scaled.getPixel(x + 1, y))
                if (prev > next) hash = hash or (1L shl (y * 8 + x))
                prev = next
            }
        }
        if (scaled !== bitmap) scaled.recycle()
        return hash
    }

    fun hammingDistance(a: Long, b: Long): Int = (a xor b).countOneBits()

    private fun luminance(color: Int): Int {
        val r = (color shr 16) and 0xFF
        val g = (color shr 8) and 0xFF
        val b = color and 0xFF
        return (r * 299 + g * 587 + b * 114) / 1000
    }
}
