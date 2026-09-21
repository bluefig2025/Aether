package com.aether.browser.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterNone
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** An overflow menu anchored to its own button, so compact layouts can place it above the bar. */
@Composable
fun BrowserOverflowMenu(
    tabCount: Int,
    onNewTab: () -> Unit,
    onShowTabs: () -> Unit,
    onShowVersion: () -> Unit,
    onShowSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier) {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.MoreVert, "Menu")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(264.dp),
        ) {
            Text(
                "Aether",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text("New tab") },
                leadingIcon = { Icon(Icons.Default.Add, null) },
                trailingIcon = { MenuHint("Ctrl + T") },
                onClick = { expanded = false; onNewTab() },
            )
            DropdownMenuItem(
                text = { Text("View tabs") },
                leadingIcon = { Icon(Icons.Default.FilterNone, null) },
                trailingIcon = { MenuHint(tabCount.toString()) },
                onClick = { expanded = false; onShowTabs() },
            )
            HorizontalDivider(Modifier.padding(vertical = 4.dp))
            DropdownMenuItem(
                text = { Text("Settings") },
                leadingIcon = { Icon(Icons.Default.Settings, null) },
                onClick = { expanded = false; onShowSettings() },
            )
            DropdownMenuItem(
                text = { Text("About Aether") },
                leadingIcon = { Icon(Icons.Default.Info, null) },
                onClick = { expanded = false; onShowVersion() },
            )
        }
    }
}

@Composable
private fun MenuHint(value: String) {
    Text(
        value,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
