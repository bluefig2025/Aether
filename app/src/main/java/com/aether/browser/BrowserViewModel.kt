package com.aether.browser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aether.browser.core.model.BrowserState
import com.aether.browser.core.model.InterfaceMode
import com.aether.browser.core.settings.SettingsRepository
import com.aether.browser.core.tabs.TabManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.mozilla.geckoview.GeckoSession

sealed interface UiCommand {
    data object FocusAddressBar : UiCommand
}

class BrowserViewModel(
    private val tabs: TabManager,
    private val settings: SettingsRepository,
) : ViewModel() {
    val browserState: StateFlow<BrowserState> = tabs.state
    val interfaceMode: StateFlow<InterfaceMode> = settings.interfaceMode.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        InterfaceMode.AUTOMATIC,
    )

    private val _commands = MutableSharedFlow<UiCommand>(extraBufferCapacity = 4)
    val commands = _commands.asSharedFlow()

    fun selectedSession(): GeckoSession? = tabs.selectedSession()
    fun navigate(input: String) = tabs.navigate(input)
    fun newTab() = tabs.newTab()
    fun closeTab(id: String) = tabs.closeTab(id)
    fun selectTab(id: String) = tabs.selectTab(id)
    fun back() = tabs.goBack()
    fun forward() = tabs.goForward()
    fun reload() = tabs.reload()
    fun stop() = tabs.stop()
    fun focusAddressBar() { _commands.tryEmit(UiCommand.FocusAddressBar) }
    fun setInterfaceMode(mode: InterfaceMode) {
        viewModelScope.launch { settings.setInterfaceMode(mode) }
    }
}
