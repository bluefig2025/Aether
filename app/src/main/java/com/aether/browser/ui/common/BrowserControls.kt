package com.aether.browser.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.aether.browser.UiCommand
import com.aether.browser.core.model.BrowserTabState
import kotlinx.coroutines.flow.Flow

@Composable
fun AddressField(
    tab: BrowserTabState,
    commands: Flow<UiCommand>,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = OutlinedTextFieldDefaults.shape,
) {
    val focusRequester = remember { FocusRequester() }
    var focused by remember { mutableStateOf(false) }
    var value by remember(tab.id) { mutableStateOf(TextFieldValue(tab.url)) }

    LaunchedEffect(tab.url, focused) {
        if (!focused) value = TextFieldValue(tab.url)
    }
    LaunchedEffect(commands, tab.id) {
        commands.collect { command ->
            if (command == UiCommand.FocusAddressBar) {
                value = value.copy(selection = TextRange(0, value.text.length))
                focusRequester.requestFocus()
            }
        }
    }

    OutlinedTextField(
        value = value,
        onValueChange = { value = it },
        modifier = modifier
            .focusRequester(focusRequester)
            .onFocusChanged { focused = it.isFocused },
        singleLine = true,
        shape = shape,
        placeholder = { androidx.compose.material3.Text("Search or enter address") },
        leadingIcon = {
            if (tab.faviconUrl != null && tab.internalPage == null) {
                AsyncImage(
                    model = tab.faviconUrl,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            } else {
                Icon(
                    if (value.text.isBlank()) Icons.Default.Search else Icons.Default.Language,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            }
        },
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Go),
        keyboardActions = androidx.compose.foundation.text.KeyboardActions(onGo = {
            onNavigate(value.text)
        }),
    )
}

@Composable
fun Favicon(url: String?, size: Dp = 18.dp) {
    Box(Modifier.size(size)) {
        if (url != null) {
            AsyncImage(model = url, contentDescription = null, modifier = Modifier.size(size))
        } else {
            Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(size))
        }
    }
}
