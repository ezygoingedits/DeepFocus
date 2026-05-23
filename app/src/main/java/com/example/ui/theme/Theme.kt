package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ObsidianDarkColorScheme = darkColorScheme(
    primary = PrimaryGreen,
    onPrimary = DarkBg,
    secondary = TextMuted,
    onSecondary = TextPrimary,
    background = DarkBg,
    onBackground = TextPrimary,
    surface = CardBg,
    onSurface = TextPrimary,
    surfaceVariant = ActiveGreenBg,
    onSurfaceVariant = TextSecondary,
    outline = BorderMuted,
    error = AccentFailure
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark mode to meet the primary design requirements
    dynamicColor: Boolean = false, // Force disabled for cohesive visual compliance
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = ObsidianDarkColorScheme,
        typography = Typography,
        content = content
    )
}
