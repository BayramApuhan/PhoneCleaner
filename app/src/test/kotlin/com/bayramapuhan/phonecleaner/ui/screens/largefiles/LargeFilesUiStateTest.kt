package com.bayramapuhan.phonecleaner.ui.screens.largefiles

import com.bayramapuhan.phonecleaner.domain.model.FileItem
import org.junit.Assert.assertEquals
import org.junit.Test

class LargeFilesUiStateTest {
    private val files = listOf(
        FileItem("/sd/photo.jpg", "photo.jpg", 1_000_000L, 0L),
        FileItem("/sd/video.mp4", "video.mp4", 500_000_000L, 0L),
        FileItem("/sd/Documents/Holiday.pdf", "Holiday.pdf", 5_000_000L, 0L),
    )

    @Test
    fun `empty query returns all files`() {
        val state = LargeFilesUiState(files = files)
        assertEquals(3, state.visibleFiles.size)
    }

    @Test
    fun `query filters by name case-insensitively`() {
        val state = LargeFilesUiState(files = files, query = "VIDEO")
        assertEquals(1, state.visibleFiles.size)
        assertEquals("video.mp4", state.visibleFiles.first().name)
    }

    @Test
    fun `query with no match returns empty`() {
        val state = LargeFilesUiState(files = files, query = "music")
        assertEquals(0, state.visibleFiles.size)
    }

    @Test
    fun `selectedTotalBytes sums only selected files`() {
        val state = LargeFilesUiState(
            files = files,
            selected = setOf("/sd/photo.jpg", "/sd/video.mp4"),
        )
        assertEquals(501_000_000L, state.selectedTotalBytes)
    }

    @Test
    fun `selectedTotalBytes ignores unknown paths`() {
        val state = LargeFilesUiState(
            files = files,
            selected = setOf("/sd/missing.bin"),
        )
        assertEquals(0L, state.selectedTotalBytes)
    }
}
