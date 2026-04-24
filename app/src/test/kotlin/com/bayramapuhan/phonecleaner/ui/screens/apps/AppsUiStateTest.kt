package com.bayramapuhan.phonecleaner.ui.screens.apps

import com.bayramapuhan.phonecleaner.domain.model.AppItem
import org.junit.Assert.assertEquals
import org.junit.Test

class AppsUiStateTest {
    private val apps = listOf(
        AppItem("com.foo.bar", "Foo Browser", "1.0", 50_000_000L, false, 0L),
        AppItem("net.example.player", "Music Player", "2.3", 80_000_000L, false, 0L),
        AppItem("com.foo.gallery", "Gallery", "1.2", 30_000_000L, false, 0L),
    )

    @Test
    fun `empty query returns all apps`() {
        val state = AppsUiState(apps = apps)
        assertEquals(3, state.visibleApps.size)
    }

    @Test
    fun `query matches label`() {
        val state = AppsUiState(apps = apps, query = "music")
        assertEquals(1, state.visibleApps.size)
        assertEquals("Music Player", state.visibleApps.first().label)
    }

    @Test
    fun `query matches package name`() {
        val state = AppsUiState(apps = apps, query = "com.foo")
        assertEquals(2, state.visibleApps.size)
    }

    @Test
    fun `query is case-insensitive`() {
        val state = AppsUiState(apps = apps, query = "GALLERY")
        assertEquals(1, state.visibleApps.size)
    }
}
