package com.aether.browser.ui.desktop

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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

/** Pointer-first browser chrome for large and freely resizable Android desktop windows. */
@Composable
fun DesktopBrowser(
    state: BrowserState,
    session: GeckoSession?,
    commands: Flow<UiCommand>,
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
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
            Row(
                Modifier.fillMaxWidth().height(42.dp).padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    Modifier.width(112.dp).padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    Icon(Icons.Default.Explore, null, Modifier.size(19.dp), tint = MaterialTheme.colorScheme.primary)
                    Text("Aether", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                }
                Row(
                    Modifier.weight(1f).horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    state.tabs.forEach { item ->
                        DesktopTab(
                            tab = item,
                            selected = item.id == state.selectedTabId,
                            onSelect = { onSelectTab(item.id) },
                            onClose = { onCloseTab(item.id) },
                        )
                    }
                }
                IconButton(onClick = onNewTab, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Default.Add, "New tab", Modifier.size(19.dp))
                }
            }
        }

        Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
            Row(
                Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                DesktopToolbarButton(onBack, tab.canGoBack, "Back") {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, Modifier.size(19.dp))
                }
                DesktopToolbarButton(onForward, tab.canGoForward, "Forward") {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.size(19.dp))
                }
                DesktopToolbarButton(if (tab.isLoading) onStop else onReload, true, if (tab.isLoading) "Stop" else "Reload") {
                    Icon(if (tab.isLoading) Icons.Default.Close else Icons.Default.Refresh, null, Modifier.size(19.dp))
                }
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    AddressField(
                        tab = tab,
                        commands = commands,
                        onNavigate = onNavigate,
                        modifier = Modifier.fillMaxWidth().widthIn(max = 900.dp),
                        shape = RoundedCornerShape(13.dp),
                    )
                }
                BrowserOverflowMenu(
                    tabCount = state.tabs.size,
                    onNewTab = onNewTab,
                    onShowTabs = onShowTabs,
                    onShowVersion = onShowVersion,
                    onShowSettings = onShowSettings,
                    modifier = Modifier.size(48.dp),
                )
            }
        }

        if (tab.isLoading) {
            LinearProgressIndicator(
                progress = { tab.loadingProgress / 100f },
                modifier = Modifier.fillMaxWidth().height(2.dp),
            )
        } else {
            HorizontalDivider()
        }
        BrowserContent(tab, session, LayoutMode.DESKTOP, Modifier.weight(1f).fillMaxWidth())
    }
}

@Composable
private fun DesktopTab(
    tab: BrowserTabState,
    selected: Boolean,
    onSelect: () -> Unit,
    onClose: () -> Unit,
) {
    val background = if (selected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceContainer
    Row(
        Modifier
            .widthIn(min = 150.dp, max = 220.dp)
            .height(34.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(background)
            .clickable(onClick = onSelect)
            .padding(start = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Favicon(tab.faviconUrl, 16.dp)
        Text(
            tab.title,
            Modifier.weight(1f).padding(horizontal = 8.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelMedium,
        )
        IconButton(onClick = onClose, modifier = Modifier.size(30.dp)) {
            Icon(Icons.Default.Close, "Close ${tab.title}", Modifier.size(15.dp))
        }
    }
}

@Composable
private fun DesktopToolbarButton(
    onClick: () -> Unit,
    enabled: Boolean,
    description: String,
    icon: @Composable () -> Unit,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(38.dp).semantics { contentDescription = description },
    ) {
        Box(Modifier.size(20.dp), contentAlignment = Alignment.Center) { icon() }
    }
}
