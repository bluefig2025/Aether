package com.aether.browser.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class LayoutModeTest {
    @Test fun `automatic follows standard window breakpoints`() {
        assertEquals(LayoutMode.COMPACT, resolveLayoutMode(InterfaceMode.AUTOMATIC, 599))
        assertEquals(LayoutMode.MEDIUM, resolveLayoutMode(InterfaceMode.AUTOMATIC, 600))
        assertEquals(LayoutMode.EXPANDED, resolveLayoutMode(InterfaceMode.AUTOMATIC, 840))
        assertEquals(LayoutMode.EXPANDED, resolveLayoutMode(InterfaceMode.AUTOMATIC, 999))
        assertEquals(LayoutMode.DESKTOP, resolveLayoutMode(InterfaceMode.AUTOMATIC, 1000))
    }

    @Test fun `forced modes ignore window width`() {
        assertEquals(LayoutMode.COMPACT, resolveLayoutMode(InterfaceMode.PHONE, 1200))
        assertEquals(LayoutMode.EXPANDED, resolveLayoutMode(InterfaceMode.TABLET, 320))
        assertEquals(LayoutMode.DESKTOP, resolveLayoutMode(InterfaceMode.DESKTOP, 320))
    }
}
