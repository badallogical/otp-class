package com.harekrishna.otpClasses.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}
val primary = Color(0xFFFF5722)

// Light Theme Color Scheme
val OrangeLightColorScheme = lightColorScheme(
    primary = Saffron,
    onPrimary = Color.White,
    secondary = SaffronLight,
    onSecondary = SaffronDark,
    tertiary = GreenSuccess,
    onTertiary = Color.White,
    background = SurfaceWhite,       // Using your custom warm white
    onBackground = TextPrimary,      // Using your custom dark text
    surface = CardBg, // Your CardBg map perfectly to surface
    onSurface = TextPrimary,         // Main text on cards
    surfaceTint = GreenLight,
    surfaceVariant = SaffronLight,   // Nice subtle tint for alternate containers
    onSurfaceVariant = TextSecondary // Muted text for subtitles
)

// Dark Theme Color Scheme
val OrangeDarkColorScheme = darkColorScheme(
    primary = Saffron,
    onPrimary = Color.White,
    secondary = SaffronLight,
    onSecondary = SaffronDark,
    tertiary = GreenSuccess,
    onTertiary = Color.White,
    background = Color(0xFF121212),
    onBackground = Color.White,
    surface = Color(0xFF1E1E1E),     // Darker grey for dark mode cards
    onSurface = Color.White,
    surfaceTint = GreenLight,
    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = Color(0xFFC1BFBF) // Muted grey for dark mode subtitles
)

@Composable
fun Otp_class_appTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val isDarkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (isDarkTheme) {
        OrangeDarkColorScheme
    } else {
        OrangeLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}


