package com.aether.browser.core.tabs

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TabRegistryTest {
    @Test fun `new tabs are selected by default`() {
        val tabs = TabRegistry()
        tabs.add("one")
        tabs.add("two")
        assertEquals(listOf("one", "two"), tabs.ids())
        assertEquals("two", tabs.selectedId)
    }

    @Test fun `select rejects an unknown tab`() {
        val tabs = TabRegistry()
        tabs.add("one")
        assertFalse(tabs.select("missing"))
        assertTrue(tabs.select("one"))
    }

    @Test fun `closing selected tab prefers its right neighbor then left`() {
        val tabs = TabRegistry()
        tabs.add("one")
        tabs.add("two")
        tabs.add("three")
        tabs.select("two")
        assertEquals("three", tabs.remove("two"))
        assertEquals("one", tabs.remove("three"))
    }

    @Test fun `closing background tab preserves selection`() {
        val tabs = TabRegistry()
        tabs.add("one")
        tabs.add("two")
        tabs.remove("one")
        assertEquals("two", tabs.selectedId)
    }
}
