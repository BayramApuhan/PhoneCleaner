package com.bayramapuhan.phonecleaner.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class FileSizeTest {
    @Test
    fun `zero or negative returns zero bytes`() {
        Locale.setDefault(Locale.US)
        assertEquals("0 B", 0L.formatSize())
        assertEquals("0 B", (-100L).formatSize())
    }

    @Test
    fun `bytes under one KB stay in B`() {
        Locale.setDefault(Locale.US)
        assertTrue(1L.formatSize().endsWith(" B"))
        assertTrue(1023L.formatSize().endsWith(" B"))
    }

    @Test
    fun `kilobytes formatted with one decimal`() {
        Locale.setDefault(Locale.US)
        assertEquals("1.0 KB", 1024L.formatSize())
        assertEquals("1.5 KB", 1536L.formatSize())
    }

    @Test
    fun `megabytes scale correctly`() {
        Locale.setDefault(Locale.US)
        assertEquals("1.0 MB", (1024L * 1024L).formatSize())
        assertEquals("100.0 MB", (100L * 1024L * 1024L).formatSize())
    }

    @Test
    fun `gigabytes and terabytes`() {
        Locale.setDefault(Locale.US)
        assertEquals("1.0 GB", (1024L * 1024L * 1024L).formatSize())
        assertEquals("2.5 TB", (2560L * 1024L * 1024L * 1024L).formatSize())
    }
}
