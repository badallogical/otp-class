package com.harekrishna.otpClasses.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
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

// Light Theme Color Scheme
val OrangeLightColorScheme = lightColorScheme(
    primary = Color(0xFFFF5722), // Bright orange
    onPrimary = Color.White, // Text on primary color should be white
    secondary = Color(0xFFFFAB91),
    onSecondary = Color.Black, // Text on secondary color should be black
    background = Color(0xFFF5F5F5), // Light background color
    onBackground = Color.Black, // Text on light background should be black
    surface = Color.White, // White surface color
    onSurface = Color.Black, // Text on white surface should be black
)

// Dark Theme Color Scheme
val OrangeDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF5722), // Bright orange
    onPrimary = Color.White, // Text on primary color should be white
    secondary = Color(0xFFFFAB91),
    onSecondary = Color.White, // Text on secondary should be white for contrast
    background = Color(0xFF121212), // Very dark background
    onBackground = Color.White, // Text on dark background should be white
    surface = Color(0xFF1E1E1E), // Dark surface color
    onSurface = Color.White, // Text on dark surface should be white

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


