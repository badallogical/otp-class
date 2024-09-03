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

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

val OrangeLightColorScheme = lightColorScheme(
    primary = Color(0xFFFF5722), // Orange color
    secondary = Color(0xFFFFAB91),
    background = Color(0xFFF5F5F5)
)

val OrangeDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF5722), // Orange color
    secondary = Color(0xFFFFAB91),
    background = Color(0xFFF5F5F5)
)

@Composable
fun Otp_class_appTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = remember(darkTheme, dynamicColor) {
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {

                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            darkTheme -> OrangeDarkColorScheme
            else -> OrangeLightColorScheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Ensure you have a Typography definition or use the default
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