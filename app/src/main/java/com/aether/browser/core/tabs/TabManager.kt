package com.aether.browser.core.tabs

import com.aether.browser.core.engine.GeckoEngine
import com.aether.browser.core.engine.SitePermissionController
import com.aether.browser.core.model.BrowserState
import com.aether.browser.core.model.BrowserTabState
import com.aether.browser.core.model.InternalPage
import com.aether.browser.core.navigation.AddressInterpreter
import com.aether.browser.core.navigation.AddressTarget
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.WebRequestError
import java.net.URI
import java.util.UUID

class TabManager(
    private val engine: GeckoEngine,
    private val addressInterpreter: AddressInterpreter = AddressInterpreter(),
    private val permissionController: SitePermissionController = SitePermissionController(),
) {
    private data class Record(
        val session: GeckoSession,
        var state: BrowserTabState,
        var returnFromInternal: BrowserTabState? = null,
    )

    private val registry = TabRegistry()
    private val records = linkedMapOf<String, Record>()
    private val _state = MutableStateFlow(BrowserState())
    val state: StateFlow<BrowserState> = _state.asStateFlow()

    init {
        newTab()
    }

    fun selectedSession(): GeckoSession? = registry.selectedId?.let { records[it]?.session }

    fun newTab(initialInput: String = InternalPage.NEW_TAB.uri): String {
        val id = UUID.randomUUID().toString()
        val session = engine.createSession()
        val record = Record(session, BrowserTabState(id = id))
        records[id] = record
        registry.add(id)
        attachDelegates(id, session)
        publish()
        if (!initialInput.equals(InternalPage.NEW_TAB.uri, true)) navigate(id, initialInput)
        return id
    }

    fun closeTab(id: String) {
        records.remove(id)?.session?.close()
        registry.remove(id)
        if (records.isEmpty()) {
            newTab()
        } else {
            publish()
        }
    }

    fun selectTab(id: String) {
        if (registry.select(id)) publish()
    }

    fun navigate(input: String) {
        registry.selectedId?.let { navigate(it, input) }
    }

    private fun navigate(id: String, input: String) {
        val record = records[id] ?: return
        when (val target = addressInterpreter.interpret(input)) {
            is AddressTarget.Internal -> {
                val page = InternalPage.from(target.url)
                if (page != null) {
                    if (record.state.internalPage == null) record.returnFromInternal = record.state
                    record.state = record.state.copy(
                        url = page.uri,
                        title = internalTitle(page),
                        internalPage = page,
                        faviconUrl = null,
                        isLoading = false,
                        canGoBack = record.returnFromInternal != null,
                        canGoForward = false,
                        errorMessage = null,
                    )
                } else {
                    record.state = record.state.copy(
                        url = target.url,
                        title = "Unknown Aether page",
                        internalPage = null,
                        isLoading = false,
                        errorMessage = "This internal Aether page does not exist.",
                    )
                }
                publish()
            }

            is AddressTarget.Web -> {
                record.returnFromInternal = null
                record.state = record.state.copy(
                    url = target.url,
                    internalPage = null,
                    isLoading = true,
                    loadingProgress = 0,
                    errorMessage = null,
                )
                publish()
                record.session.load(
                    GeckoSession.Loader().uri(target.url).originalInput(input.trim()),
                )
            }
        }
    }

    fun goBack() = selectedRecord()?.let { record ->
        val returnState = record.returnFromInternal
        if (record.state.internalPage != null && returnState != null) {
            record.state = returnState
            record.returnFromInternal = null
            publish()
        } else if (record.state.canGoBack) {
            record.session.goBack()
        }
    }
    fun goForward() = selectedRecord()?.let { if (it.state.canGoForward) it.session.goForward() }
    fun reload() = selectedRecord()?.session?.reload()
    fun stop() = selectedRecord()?.session?.stop()

    private fun selectedRecord(): Record? = registry.selectedId?.let(records::get)

    private fun attachDelegates(id: String, session: GeckoSession) {
        session.permissionDelegate = permissionController
        session.progressDelegate = object : GeckoSession.ProgressDelegate {
            override fun onPageStart(session: GeckoSession, url: String) {
                update(id) {
                    if (it.internalPage != null) it
                    else it.copy(url = url, isLoading = true, loadingProgress = 0, errorMessage = null)
                }
            }

            override fun onProgressChange(session: GeckoSession, progress: Int) {
                update(id) { if (it.internalPage != null) it else it.copy(loadingProgress = progress) }
            }

            override fun onPageStop(session: GeckoSession, success: Boolean) {
                update(id) {
                    if (it.internalPage != null) it else it.copy(
                        isLoading = false,
                        loadingProgress = if (success) 100 else it.loadingProgress,
                        errorMessage = if (success) null else it.errorMessage ?: "The page could not be loaded.",
                    )
                }
            }
        }
        session.contentDelegate = object : GeckoSession.ContentDelegate {
            override fun onTitleChange(session: GeckoSession, title: String?) {
                if (!title.isNullOrBlank()) update(id) {
                    if (it.internalPage != null) it else it.copy(title = title)
                }
            }

            override fun onCloseRequest(session: GeckoSession) = closeTab(id)

            override fun onCrash(session: GeckoSession) {
                update(id) { it.copy(isLoading = false, errorMessage = "The web content process stopped.") }
            }
        }
        session.navigationDelegate = object : GeckoSession.NavigationDelegate {
            override fun onLocationChange(
                session: GeckoSession,
                url: String?,
                perms: List<GeckoSession.PermissionDelegate.ContentPermission>,
                hasUserGesture: Boolean,
            ) {
                if (url != null) update(id) {
                    // A newly opened GeckoSession reports about:blank even while its tab is
                    // displaying a native aether:// page. Background engine callbacks must not
                    // replace native-page state; web navigation clears internalPage before load.
                    if (it.internalPage != null) it
                    else it.copy(url = url, faviconUrl = faviconFor(url))
                }
            }

            override fun onCanGoBack(session: GeckoSession, canGoBack: Boolean) {
                update(id) { if (it.internalPage != null) it else it.copy(canGoBack = canGoBack) }
            }

            override fun onCanGoForward(session: GeckoSession, canGoForward: Boolean) {
                update(id) { if (it.internalPage != null) it else it.copy(canGoForward = canGoForward) }
            }

            override fun onLoadError(
                session: GeckoSession,
                uri: String?,
                error: WebRequestError,
            ) = null.also {
                update(id) {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Unable to load this page (error ${error.code}).",
                    )
                }
            }
        }
    }

    private fun update(id: String, transform: (BrowserTabState) -> BrowserTabState) {
        records[id]?.let { it.state = transform(it.state) }
        publish()
    }

    private fun publish() {
        _state.value = BrowserState(
            tabs = registry.ids().mapNotNull { records[it]?.state },
            selectedTabId = registry.selectedId,
        )
    }

    private fun faviconFor(url: String): String? = try {
        URI(url).let { uri ->
            if (uri.scheme in setOf("http", "https") && uri.host != null) {
                "${uri.scheme}://${uri.rawAuthority}/favicon.ico"
            } else null
        }
    } catch (_: Exception) {
        null
    }

    private fun internalTitle(page: InternalPage): String = when (page) {
        InternalPage.NEW_TAB -> "New tab"
        InternalPage.HISTORY -> "History"
        InternalPage.BOOKMARKS -> "Bookmarks"
        InternalPage.DOWNLOADS -> "Downloads"
        InternalPage.SETTINGS -> "Settings"
        InternalPage.VERSION -> "Aether version"
        InternalPage.FLAGS -> "Experiments"
    }
}
