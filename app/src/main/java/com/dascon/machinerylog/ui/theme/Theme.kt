package com.dascon.machinerylog.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DscplBlue = Color(0xFF1E3A8A)
val DscplBlueDark = Color(0xFF0B1B4D)
val DscplBlueMid = Color(0xFF1D4ED8)
val DscplRed = Color(0xFFE11D48)
val DscplInk = Color(0xFF0F172A)
val DscplMuted = Color(0xFF64748B)
val DscplMist = Color(0xFFF1F5F9)
val DscplIvory = Color(0xFFFCFCFE)
val DscplLine = Color(0xFFE2E8F0)

private val LightColors = lightColorScheme(
    primary = DscplBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBE4FF),
    onPrimaryContainer = DscplBlueDark,
    secondary = DscplRed,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE4E9),
    onSecondaryContainer = Color(0xFF9F1239),
    tertiary = DscplBlueMid,
    background = DscplIvory,
    onBackground = DscplInk,
    surface = Color.White,
    onSurface = DscplInk,
    surfaceVariant = DscplMist,
    onSurfaceVariant = DscplMuted,
    outline = DscplLine,
    error = Color(0xFFB42318),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF93C5FD),
    onPrimary = DscplBlueDark,
    secondary = Color(0xFFFB7185),
    onSecondary = Color.White,
    background = Color(0xFF060B18),
    onBackground = Color(0xFFF1F5F9),
    surface = Color(0xFF0F172A),
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF334155),
)

@Composable
fun DasconTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = DasconTypography,
        content = content,
    )
}
