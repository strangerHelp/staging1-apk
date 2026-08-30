package com.strangerhelp.app.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = SurfaceVariant,
    onPrimaryContainer = Primary,
    secondary = Saffron,
    onSecondary = OnSaffron,
    secondaryContainer = Saffron.copy(alpha = 0.15f),
    onSecondaryContainer = Saffron,
    error = Error,
    surface = Surface,
    onSurface = Primary,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = Body,
    outline = Color(0xFF737373),
    outlineVariant = Color(0xFF8C8C8C),
    background = BackgroundLight,
    onBackground = Primary
)

private val DarkColors = darkColorScheme(
    primary = Saffron,
    onPrimary = Primary,
    primaryContainer = DarkVariant,
    onPrimaryContainer = DarkOnSurface,
    secondary = CyanDeep,
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
    background = DarkBackground,
    onBackground = DarkOnSurface
)

@Composable
fun StrangerHelpTheme(
    darkTheme: Boolean = false, // Forced white/light theme
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}

val MaterialTheme.defaultBorder: BorderStroke
    @Composable
    get() = BorderStroke(1.dp, colorScheme.outline)

fun Modifier.themeBorder(shape: Shape): Modifier = composed {
    this.border(MaterialTheme.defaultBorder, shape)
}
