package com.citra.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Cores primárias ────────────────────────────────────────────
private val CitraRed   = Color(0xFFCC0000)
private val CitraDark  = Color(0xFF1A1A2E)
private val CitraAccent= Color(0xFF0077CC)

private val DarkColorScheme = darkColorScheme(
    primary         = CitraAccent,
    onPrimary       = Color.White,
    primaryContainer= Color(0xFF003A6B),
    secondary       = CitraRed,
    background      = Color(0xFF0D0D1A),
    surface         = Color(0xFF16213E),
    surfaceVariant  = Color(0xFF1E2A40),
    onBackground    = Color(0xFFEEEEEE),
    onSurface       = Color(0xFFEEEEEE),
)

private val LightColorScheme = lightColorScheme(
    primary         = CitraAccent,
    onPrimary       = Color.White,
    primaryContainer= Color(0xFFD4E8FF),
    secondary       = CitraRed,
    background      = Color(0xFFF5F5F5),
    surface         = Color.White,
    surfaceVariant  = Color(0xFFECECEC),
    onBackground    = Color(0xFF1A1A1A),
    onSurface       = Color(0xFF1A1A1A),
)

@Composable
fun CitraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography(),
        content     = content
    )
}
