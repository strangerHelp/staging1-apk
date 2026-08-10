import os

path = 'app/src/main/java/com/strangerhelp/app/ui/theme/Color.kt'
with open(path, 'w') as f:
    f.write("""package com.strangerhelp.app.ui.theme

import androidx.compose.ui.graphics.Color

val Primary = Color(0xFF101828)        // Deep Ink Navy
val OnPrimary = Color(0xFFFFFFFF)
val Saffron = Color(0xFFF5A623)        // Marigold Saffron
val OnSaffron = Color(0xFF101828)
val BackgroundLight = Color(0xFFFAF9F6) // Warm Off-White Paper
val Cyan = Color(0xFF50E3C2)
val CyanDeep = Color(0xFF2A9D8F)       // Muted Teal - verified badges
val Link = Color(0xFF0070F3)           // Blue links
val Error = Color(0xFFEE0000)
val Warning = Color(0xFFF5A623)

val Surface = Color(0xFFFFFFFF)
val SurfaceVariant = Color(0xFFF0F0F0)
val Hairline = Color(0xFFEBEBEB)       // Borders

val Body = Color(0xFF4D4D4D)           // Body text
val Muted = Color(0xFF666666)          // Muted text (WCAG AA)

// Dark mode
val DarkSurface = Color(0xFF1A1A1A)
val DarkBackground = Color(0xFF121212)
val DarkOnSurface = Color(0xFFF2F2F2)
val DarkVariant = Color(0xFF2A2A2A)
""")

path = 'app/src/main/java/com/strangerhelp/app/ui/theme/Theme.kt'
with open(path, 'w') as f:
    f.write("""package com.strangerhelp.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

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
    outline = Hairline,
    outlineVariant = Hairline,
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
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}
""")
