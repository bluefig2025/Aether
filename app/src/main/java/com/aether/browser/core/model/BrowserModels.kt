package com.aether.browser.core.model

import java.util.UUID

enum class InterfaceMode { AUTOMATIC, PHONE, TABLET, DESKTOP }

enum class LayoutMode { COMPACT, MEDIUM, EXPANDED, DESKTOP }

enum class InternalPage(val uri: String, val implemented: Boolean = false) {
    NEW_TAB("aether://newtab", true),
    HISTORY("aether://history"),
    BOOKMARKS("aether://bookmarks"),
    DOWNLOADS("aether://downloads"),
    SETTINGS("aether://settings"),
    VERSION("aether://version", true),
    FLAGS("aether://flags");

    companion object {
        fun from(uri: String): InternalPage? = entries.firstOrNull { it.uri.equals(uri, true) }
    }
}

data class BrowserTabState(
    val id: String = UUID.randomUUID().toString(),
    val url: String = InternalPage.NEW_TAB.uri,
    val title: String = "New tab",
    val faviconUrl: String? = null,
    val isLoading: Boolean = false,
    val loadingProgress: Int = 0,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val errorMessage: String? = null,
    val internalPage: InternalPage? = InternalPage.NEW_TAB,
)

data class BrowserState(
    val tabs: List<BrowserTabState> = emptyList(),
    val selectedTabId: String? = null,
) {
    val selectedTab: BrowserTabState?
        get() = tabs.firstOrNull { it.id == selectedTabId }
}

fun resolveLayoutMode(interfaceMode: InterfaceMode, widthDp: Int): LayoutMode = when (interfaceMode) {
    InterfaceMode.PHONE -> LayoutMode.COMPACT
    InterfaceMode.TABLET -> LayoutMode.EXPANDED
    InterfaceMode.DESKTOP -> LayoutMode.DESKTOP
    InterfaceMode.AUTOMATIC -> when {
        widthDp < 600 -> LayoutMode.COMPACT
        widthDp < 840 -> LayoutMode.MEDIUM
        widthDp < 1_000 -> LayoutMode.EXPANDED
        else -> LayoutMode.DESKTOP
    }
}
