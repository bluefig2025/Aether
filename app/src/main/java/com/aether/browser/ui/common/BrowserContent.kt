package com.aether.browser.ui.common

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.aether.browser.BuildConfig
import com.aether.browser.core.model.BrowserTabState
import com.aether.browser.core.model.InternalPage
import com.aether.browser.core.model.LayoutMode
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView

@Composable
fun BrowserContent(
    tab: BrowserTabState,
    session: GeckoSession?,
    layoutMode: LayoutMode,
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
        when {
            tab.internalPage != null -> InternalPageContent(tab.internalPage, layoutMode)
            tab.url.startsWith("aether://") -> MessagePage("Unknown page", tab.errorMessage.orEmpty())
            session != null -> GeckoSessionView(session, Modifier.fillMaxSize())
            else -> MessagePage("Browser unavailable", "A web session could not be created.")
        }

        if (!tab.errorMessage.isNullOrBlank() && !tab.url.startsWith("aether://")) {
            Card(
                modifier = Modifier.align(Alignment.TopCenter).padding(12.dp),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(tab.errorMessage, Modifier.padding(horizontal = 16.dp, vertical = 10.dp))
            }
        }
    }
}

@Composable
private fun GeckoSessionView(session: GeckoSession, modifier: Modifier = Modifier) {
    androidx.compose.ui.viewinterop.AndroidView(
        factory = { context ->
            GeckoView(context).apply {
                setSession(session)
                tag = session
            }
        },
        update = { view ->
            if (view.tag !== session) {
                view.releaseSession()
                view.setSession(session)
                view.tag = session
            }
        },
        onRelease = { view ->
            // The core owns sessions. A view only releases its display attachment, so resizing,
            // switching tabs, and activity recreation never destroys browsing state.
            view.releaseSession()
            view.tag = null
        },
        modifier = modifier,
    )
}

@Composable
private fun InternalPageContent(page: InternalPage, layoutMode: LayoutMode) {
    when (page) {
        InternalPage.NEW_TAB -> NewTabPage()
        InternalPage.VERSION -> VersionPage(layoutMode)
        else -> MessagePage(
            page.name.lowercase().replaceFirstChar(Char::uppercase),
            "This Aether page is reserved for a future milestone.",
        )
    }
}

@Composable
private fun NewTabPage() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            Modifier.widthIn(max = 560.dp).padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(28.dp),
            ) {
                Icon(
                    Icons.Default.Explore,
                    contentDescription = null,
                    modifier = Modifier.padding(18.dp).size(48.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Text("Aether", style = MaterialTheme.typography.displaySmall)
            Text(
                "A quieter way onto the web",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                "Press Ctrl + L to search or enter an address",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
    }
}

@Composable
private fun VersionPage(layoutMode: LayoutMode) {
    val configuration = LocalConfiguration.current
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Surface(
            modifier = Modifier.widthIn(max = 560.dp).padding(24.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = 1.dp,
        ) {
            Column(
                Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(Icons.Default.Info, contentDescription = null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                Text("Aether Browser", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.size(20.dp))
                InfoLine("Aether", BuildConfig.VERSION_NAME)
                InfoLine("GeckoView", BuildConfig.GECKOVIEW_VERSION)
                InfoLine("Android", "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
                InfoLine("Device", "${Build.MANUFACTURER} ${Build.MODEL}")
                InfoLine("Window", "${configuration.screenWidthDp} × ${configuration.screenHeightDp} dp")
                InfoLine("Interface", layoutMode.name.lowercase().replaceFirstChar(Char::uppercase))
                Spacer(Modifier.size(16.dp))
                Text(
                    "This information stays on your device.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(110.dp))
        Text(value, modifier = Modifier.width(240.dp))
    }
}

@Composable
private fun MessagePage(title: String, message: String) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.size(8.dp))
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
