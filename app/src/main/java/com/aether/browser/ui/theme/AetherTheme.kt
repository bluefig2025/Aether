package com.aether.browser.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF315FAF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD8E5FF),
    onPrimaryContainer = Color(0xFF0A2F64),
    background = Color(0xFFF7F9FC),
    surface = Color(0xFFFDFDFF),
    surfaceContainer = Color(0xFFF0F3F9),
    surfaceContainerHigh = Color(0xFFE7EBF3),
    outline = Color(0xFF747985),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA9C7FF),
    onPrimary = Color(0xFF07305F),
    primaryContainer = Color(0xFF244A86),
    onPrimaryContainer = Color(0xFFD8E5FF),
    background = Color(0xFF0D1015),
    surface = Color(0xFF111318),
    surfaceContainer = Color(0xFF1B1D23),
    surfaceContainerHigh = Color(0xFF25272E),
    outline = Color(0xFF8D919B),
)

@Composable
fun AetherTheme(content: @Composable () -> Unit) {
    // A consistent palette gives browser chrome its own identity across phones, tablets, and
    // desktop environments while still following the system light/dark preference.
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}
