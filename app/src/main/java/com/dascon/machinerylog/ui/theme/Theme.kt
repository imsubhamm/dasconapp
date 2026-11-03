package com.dascon.machinerylog.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Official DSCPL brand colours (web Logo / ERP). */
val DscplBlue = Color(0xFF1E3A8A)      // blue-900
val DscplBlueDark = Color(0xFF172554)
val DscplRed = Color(0xFFE11D48)       // rose-600
val DscplInk = Color(0xFF0F172A)
val DscplMuted = Color(0xFF64748B)
val DscplSurface = Color(0xFFF8FAFC)

private val LightColors = lightColorScheme(
    primary = DscplBlue,
    onPrimary = Color.White,
    secondary = DscplRed,
    onSecondary = Color.White,
    tertiary = DscplBlueDark,
    background = DscplSurface,
    onBackground = DscplInk,
    surface = Color.White,
    onSurface = DscplInk,
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = DscplMuted,
    error = Color(0xFFB42318),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF93C5FD),
    onPrimary = DscplBlueDark,
    secondary = Color(0xFFFB7185),
    onSecondary = Color.White,
    background = Color(0xFF020617),
    onBackground = Color(0xFFF1F5F9),
    surface = Color(0xFF0F172A),
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFF94A3B8),
)

@Composable
fun DasconTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content,
    )
}
