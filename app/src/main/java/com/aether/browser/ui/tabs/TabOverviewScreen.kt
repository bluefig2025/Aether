package com.aether.browser.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aether.browser.core.model.BrowserState
import com.aether.browser.core.model.BrowserTabState
import com.aether.browser.core.model.InternalPage
import com.aether.browser.ui.common.Favicon
import java.net.URI

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabOverviewScreen(
    state: BrowserState,
    onDismiss: () -> Unit,
    onNewTab: () -> Unit,
    onSelect: (String) -> Unit,
    onClose: (String) -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Tabs", fontWeight = FontWeight.SemiBold)
                        Text(
                            "${state.tabs.size} open",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = { TextButton(onClick = onDismiss) { Text("Done") } },
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Aether tabs",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    FilledIconButton(onClick = onNewTab) { Icon(Icons.Default.Add, "New tab") }
                }
            }
        },
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 168.dp),
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(state.tabs, key = { it.id }) { tab ->
                TabPreview(
                    tab = tab,
                    selected = tab.id == state.selectedTabId,
                    onSelect = { onSelect(tab.id) },
                    onClose = { onClose(tab.id) },
                )
            }
        }
    }
}

@Composable
private fun TabPreview(tab: BrowserTabState, selected: Boolean, onSelect: () -> Unit, onClose: () -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    val border = if (selected) Modifier.border(3.dp, MaterialTheme.colorScheme.primary, shape) else Modifier
    Surface(
        modifier = Modifier.fillMaxWidth().aspectRatio(0.72f).then(border).clip(shape).clickable(onClick = onSelect),
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = if (selected) 8.dp else 2.dp,
    ) {
        Column {
            Row(
                Modifier.fillMaxWidth().padding(start = 12.dp, top = 8.dp, bottom = 7.dp, end = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Favicon(tab.faviconUrl, 17.dp)
                Text(
                    tab.title,
                    Modifier.weight(1f).padding(horizontal = 8.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelLarge,
                )
                IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Close, "Close ${tab.title}", Modifier.size(17.dp))
                }
            }
            Box(
                Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                        if (tab.internalPage == InternalPage.NEW_TAB) {
                            Icon(
                                Icons.Default.Explore,
                                null,
                                Modifier.padding(14.dp).size(30.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        } else {
                            Box(Modifier.padding(14.dp).size(30.dp), contentAlignment = Alignment.Center) {
                                Favicon(tab.faviconUrl, 28.dp)
                            }
                        }
                    }
                    Text(
                        previewLabel(tab),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        tab.url,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

private fun previewLabel(tab: BrowserTabState): String = when (tab.internalPage) {
    InternalPage.NEW_TAB -> "A quieter way onto the web"
    InternalPage.VERSION -> "About Aether"
    null -> runCatching { URI(tab.url).host?.removePrefix("www.") }.getOrNull() ?: tab.title
    else -> tab.title
}
