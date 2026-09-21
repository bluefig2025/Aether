package com.aether.browser.ui.compact

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterNone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aether.browser.UiCommand
import com.aether.browser.core.model.BrowserState
import com.aether.browser.core.model.LayoutMode
import com.aether.browser.ui.common.AddressField
import com.aether.browser.ui.common.BrowserContent
import com.aether.browser.ui.common.BrowserOverflowMenu
import kotlinx.coroutines.flow.Flow
import org.mozilla.geckoview.GeckoSession

@Composable
fun CompactBrowser(
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
    onShowTabs: () -> Unit,
    onShowVersion: () -> Unit,
    onShowSettings: () -> Unit,
) {
    val tab = state.selectedTab ?: return
    // Edge-to-edge windows do not resize Compose content for the IME automatically. Applying the
    // IME inset at the compact-layout boundary keeps the complete bottom chrome above the keyboard
    // while allowing the page viewport to take the remaining space.
    Column(Modifier.fillMaxSize().imePadding()) {
        BrowserContent(tab, session, layoutMode, Modifier.weight(1f).fillMaxWidth())
        if (tab.isLoading) {
            LinearProgressIndicator(
                progress = { tab.loadingProgress / 100f },
                modifier = Modifier.fillMaxWidth(),
            )
        } else HorizontalDivider()
        Surface(tonalElevation = 3.dp) {
            Column(Modifier.padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 6.dp)) {
                AddressField(
                    tab = tab,
                    commands = commands,
                    onNavigate = onNavigate,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                )
                Row(
                    Modifier.fillMaxWidth().padding(top = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack, enabled = tab.canGoBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                    IconButton(onClick = onForward, enabled = tab.canGoForward) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, "Forward")
                    }
                    IconButton(onClick = if (tab.isLoading) onStop else onReload) {
                        Icon(if (tab.isLoading) Icons.Default.Close else Icons.Default.Refresh, if (tab.isLoading) "Stop" else "Reload")
                    }
                    FilledTonalIconButton(onClick = onShowTabs) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FilterNone, "Tabs")
                            Text(state.tabs.size.toString(), Modifier.padding(start = 2.dp))
                        }
                    }
                    BrowserOverflowMenu(
                        tabCount = state.tabs.size,
                        onNewTab = onNewTab,
                        onShowTabs = onShowTabs,
                        onShowVersion = onShowVersion,
                        onShowSettings = onShowSettings,
                    )
                }
            }
        }
    }
}
