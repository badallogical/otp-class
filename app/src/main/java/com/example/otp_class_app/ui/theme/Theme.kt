package com.example.otp_class_app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview

// Light Theme Color Scheme
val OrangeLightColorScheme = lightColorScheme(
    primary = Color(0xFFFF5722), // Bright orange
    onPrimary = Color.White, // Text on primary color should be white
    secondary = Color(0xFFFFAB91),
    onSecondary = Color.Black, // Text on secondary color should be black
    background = Color(0xFFF5F5F5), // Light background color
    onBackground = Color.Black, // Text on light background should be black
    surface = Color.White, // White surface color
    onSurface = Color.Black // Text on white surface should be black
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
    onSurface = Color.White // Text on dark surface should be white
)

@Composable
fun Otp_class_appTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Use system theme or force dark mode
    content: @Composable () -> Unit
) {
    // Choose between light and dark color schemes based on the theme
    val colorScheme = if (darkTheme) {
        OrangeDarkColorScheme
    } else {
        OrangeLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Use your defined typography or the default
        content = content
    )
}


@Preview(showBackground = true)
@Composable
fun LightThemePreview() {
    Otp_class_appTheme(darkTheme = false) {
        // Preview light theme
    }
}

@Preview(showBackground = true)
@Composable
fun DarkThemePreview() {
    Otp_class_appTheme(darkTheme = true) {
        // Preview dark theme
    }
}