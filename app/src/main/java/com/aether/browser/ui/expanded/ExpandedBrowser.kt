package com.aether.browser.ui.expanded

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aether.browser.UiCommand
import com.aether.browser.core.model.BrowserState
import com.aether.browser.core.model.BrowserTabState
import com.aether.browser.core.model.LayoutMode
import com.aether.browser.ui.common.AddressField
import com.aether.browser.ui.common.BrowserContent
import com.aether.browser.ui.common.BrowserOverflowMenu
import com.aether.browser.ui.common.Favicon
import kotlinx.coroutines.flow.Flow
import org.mozilla.geckoview.GeckoSession

@Composable
fun ExpandedBrowser(
    state: BrowserState,
    session: GeckoSession?,
    commands: Flow<UiCommand>,
    layoutMode: LayoutMode,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onReload: () -> Unit,
    onStop: () -> Unit,
    onNewTab: () -> Unit,
    onSelectTab: (String) -> Unit,
    onCloseTab: (String) -> Unit,
    onShowTabs: () -> Unit,
    onShowVersion: () -> Unit,
    onShowSettings: () -> Unit,
) {
    val tab = state.selectedTab ?: return
    Column(Modifier.fillMaxSize()) {
        Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
            Row(
                Modifier.fillMaxWidth().height(46.dp).horizontalScroll(rememberScrollState()).padding(start = 8.dp, top = 6.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                state.tabs.forEach { item ->
                    BrowserTab(
                        tab = item,
                        selected = item.id == state.selectedTabId,
                        onSelect = { onSelectTab(item.id) },
                        onClose = { onCloseTab(item.id) },
                    )
                }
                IconButton(onClick = onNewTab) { Icon(Icons.Default.Add, "New tab") }
            }
        }
        Surface(tonalElevation = 2.dp) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                IconButton(onClick = onBack, enabled = tab.canGoBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                IconButton(onClick = onForward, enabled = tab.canGoForward) { Icon(Icons.AutoMirrored.Filled.ArrowForward, "Forward") }
                IconButton(onClick = if (tab.isLoading) onStop else onReload) {
                    Icon(if (tab.isLoading) Icons.Default.Close else Icons.Default.Refresh, if (tab.isLoading) "Stop" else "Reload")
                }
                AddressField(
                    tab = tab,
                    commands = commands,
                    onNavigate = onNavigate,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                )
                BrowserOverflowMenu(
                    tabCount = state.tabs.size,
                    onNewTab = onNewTab,
                    onShowTabs = onShowTabs,
                    onShowVersion = onShowVersion,
                    onShowSettings = onShowSettings,
                )
            }
        }
        if (tab.isLoading) {
            LinearProgressIndicator(progress = { tab.loadingProgress / 100f }, modifier = Modifier.fillMaxWidth())
        } else HorizontalDivider()
        BrowserContent(tab, session, layoutMode, Modifier.weight(1f).fillMaxWidth())
    }
}

@Composable
private fun BrowserTab(
    tab: BrowserTabState,
    selected: Boolean,
    onSelect: () -> Unit,
    onClose: () -> Unit,
) {
    val color = if (selected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceContainerHigh
    Row(
        Modifier
            .widthIn(min = 130.dp, max = 230.dp)
            .height(40.dp)
            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            .background(color)
            .clickable(onClick = onSelect)
            .padding(start = 12.dp, top = 9.dp, bottom = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Favicon(tab.faviconUrl)
        Text(
            tab.title,
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelLarge,
        )
        IconButton(onClick = onClose, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.Close, "Close ${tab.title}", Modifier.size(17.dp))
        }
    }
}
