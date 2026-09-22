package com.aether.browser.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aether.browser.BrowserViewModel
import com.aether.browser.core.model.LayoutMode
import com.aether.browser.core.model.resolveLayoutMode
import com.aether.browser.ui.compact.CompactBrowser
import com.aether.browser.ui.desktop.DesktopBrowser
import com.aether.browser.ui.expanded.ExpandedBrowser
import com.aether.browser.ui.settings.SettingsScreen
import com.aether.browser.ui.tabs.TabOverviewScreen
import com.aether.browser.ui.theme.AetherTheme

private enum class AppSurface { BROWSER, TABS, SETTINGS }

@Composable
fun AetherApp(
    viewModel: BrowserViewModel,
    onNavigate: (String) -> Unit = viewModel::navigate,
) {
    val state by viewModel.browserState.collectAsStateWithLifecycle()
    val interfaceMode by viewModel.interfaceMode.collectAsStateWithLifecycle()
    // This is the current activity window width, so freeform desktop resizing updates the UI.
    val layoutMode = resolveLayoutMode(interfaceMode, LocalConfiguration.current.screenWidthDp)
    var surface by rememberSaveable { mutableStateOf(AppSurface.BROWSER) }
    val selected = state.selectedTab

    BackHandler(enabled = surface != AppSurface.BROWSER) { surface = AppSurface.BROWSER }
    BackHandler(enabled = surface == AppSurface.BROWSER && selected?.canGoBack == true, onBack = viewModel::back)

    fun showVersion() {
        surface = AppSurface.BROWSER
        viewModel.navigate("aether://version")
    }

    AetherTheme {
        when (surface) {
            AppSurface.TABS -> TabOverviewScreen(
                state = state,
                onDismiss = { surface = AppSurface.BROWSER },
                onNewTab = {
                    viewModel.newTab()
                    surface = AppSurface.BROWSER
                    viewModel.focusAddressBar()
                },
                onSelect = {
                    viewModel.selectTab(it)
                    surface = AppSurface.BROWSER
                },
                onClose = viewModel::closeTab,
            )

            AppSurface.SETTINGS -> SettingsScreen(
                current = interfaceMode,
                onSelect = viewModel::setInterfaceMode,
                onBack = { surface = AppSurface.BROWSER },
                onShowVersion = ::showVersion,
            )

            AppSurface.BROWSER -> when (layoutMode) {
                LayoutMode.COMPACT -> CompactBrowser(
                    state = state,
                    session = viewModel.selectedSession(),
                    commands = viewModel.commands,
                    layoutMode = layoutMode,
                    onNavigate = onNavigate,
                    onBack = viewModel::back,
                    onForward = viewModel::forward,
                    onReload = viewModel::reload,
                    onStop = viewModel::stop,
                    onNewTab = {
                        viewModel.newTab()
                        viewModel.focusAddressBar()
                    },
                    onShowTabs = { surface = AppSurface.TABS },
                    onShowVersion = ::showVersion,
                    onShowSettings = { surface = AppSurface.SETTINGS },
                )

                LayoutMode.DESKTOP -> DesktopBrowser(
                    state = state,
                    session = viewModel.selectedSession(),
                    commands = viewModel.commands,
                    onNavigate = onNavigate,
                    onBack = viewModel::back,
                    onForward = viewModel::forward,
                    onReload = viewModel::reload,
                    onStop = viewModel::stop,
                    onNewTab = viewModel::newTab,
                    onSelectTab = viewModel::selectTab,
                    onCloseTab = viewModel::closeTab,
                    onShowTabs = { surface = AppSurface.TABS },
                    onShowVersion = ::showVersion,
                    onShowSettings = { surface = AppSurface.SETTINGS },
                )

                LayoutMode.MEDIUM, LayoutMode.EXPANDED -> ExpandedBrowser(
                    state = state,
                    session = viewModel.selectedSession(),
                    commands = viewModel.commands,
                    layoutMode = layoutMode,
                    onNavigate = onNavigate,
                    onBack = viewModel::back,
                    onForward = viewModel::forward,
                    onReload = viewModel::reload,
                    onStop = viewModel::stop,
                    onNewTab = viewModel::newTab,
                    onSelectTab = viewModel::selectTab,
                    onCloseTab = viewModel::closeTab,
                    onShowTabs = { surface = AppSurface.TABS },
                    onShowVersion = ::showVersion,
                    onShowSettings = { surface = AppSurface.SETTINGS },
                )
            }
        }
    }
}
