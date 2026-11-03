package com.dascon.machinerylog.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Always light — site use needs high contrast outdoors / on phone dark mode. */
val DscplBlue = Color(0xFF1E3A8A)
val DscplBlueDark = Color(0xFF0B1B4D)
val DscplBlueMid = Color(0xFF1D4ED8)
val DscplRed = Color(0xFFC8102E)
val DscplInk = Color(0xFF0F172A)
val DscplMuted = Color(0xFF475569)
val DscplMist = Color(0xFFF1F5F9)
val DscplIvory = Color(0xFFF5F7FB)
val DscplLine = Color(0xFFCBD5E1)

private val LightColors = lightColorScheme(
    primary = DscplBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBE4FF),
    onPrimaryContainer = DscplBlueDark,
    secondary = DscplRed,
    onSecondary = Color.White,
    background = DscplIvory,
    onBackground = DscplInk,
    surface = Color.White,
    onSurface = DscplInk,
    surfaceVariant = DscplMist,
    onSurfaceVariant = DscplMuted,
    outline = DscplLine,
    error = Color(0xFFB42318),
)

@Composable
fun DasconTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = DasconTypography,
        content = content,
    )
}
