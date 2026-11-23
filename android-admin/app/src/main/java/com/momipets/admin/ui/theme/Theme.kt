package com.momipets.admin.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val ForestDark = Color(0xFF123524)
val ForestMid = Color(0xFF1B4332)
val ForestTint = Color(0xFFE6EFE9)
val CreamCanvas = Color(0xFFFAF8F3)
val CreamSurface = Color(0xFFF2EFE7)
val CrispWhite = Color(0xFFFFFFFF)
val InkPrimary = Color(0xFF14261C)
val InkMuted = Color(0xFF4A5D52)
val WarmAmber = Color(0xFFB7791F)
val AlertRed = Color(0xFFB91C1C)

private val MomiAdminColors = lightColorScheme(
    primary = ForestDark,
    onPrimary = CreamCanvas,
    primaryContainer = ForestTint,
    onPrimaryContainer = ForestDark,
    secondary = ForestMid,
    onSecondary = CrispWhite,
    background = CreamCanvas,
    onBackground = InkPrimary,
    surface = CrispWhite,
    onSurface = InkPrimary,
    surfaceVariant = CreamSurface,
    onSurfaceVariant = InkMuted,
    error = AlertRed,
    onError = CrispWhite,
    outline = Color(0x2214261C)
)

@Composable
fun MomiPetsAdminTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MomiAdminColors,
        content = content
    )
}
