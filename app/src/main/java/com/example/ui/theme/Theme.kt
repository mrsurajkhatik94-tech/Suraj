package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = BrandGreenLight,
    secondary = BrandOrangeLight,
    tertiary = LightGreenAccent,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = Color(0xFF111A14),
    onSecondary = Color(0xFF111A14),
    onBackground = Color.White,
    onSurface = Color.White,
    error = ErrorColor
)

private val LightColorScheme = lightColorScheme(
    primary = BrandGreen,
    secondary = BrandOrange,
    tertiary = BrandDarkGreen,
    background = BrandLightBackground,
    surface = BrandCreamSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = BrandDarkGreen,
    onSurface = BrandDarkGreen,
    error = ErrorColor,
    primaryContainer = LightGreenAccent,
    secondaryContainer = LightOrangeAccent
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
