package com.strangerhelp.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = SurfaceVariant,
    onPrimaryContainer = Primary,
    secondary = CyanDeep,
    onSecondary = OnPrimary,
    secondaryContainer = Cyan.copy(alpha = 0.15f),
    onSecondaryContainer = CyanDeep,
    error = Error,
    surface = Surface,
    onSurface = Primary,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = Body,
    outline = Hairline,
    outlineVariant = Hairline,
    background = Surface,
    onBackground = Primary
)

private val DarkColors = darkColorScheme(
    primary = CyanDeep,
    onPrimary = Primary,
    primaryContainer = DarkVariant,
    onPrimaryContainer = DarkOnSurface,
    secondary = Cyan,
    onSecondary = Primary,
    secondaryContainer = CyanDeep.copy(alpha = 0.15f),
    onSecondaryContainer = Cyan,
    error = Color(0xFFEF4444),
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkVariant,
    onSurfaceVariant = Color(0xFFD1D5DB),
    outline = Color(0xFF374151),
    outlineVariant = Color(0xFF4B5563),
    background = DarkSurface,
    onBackground = DarkOnSurface
)

@Composable
fun StrangerHelpTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}
