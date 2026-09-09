package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = RoyalBlueLight,
    onPrimary = Color.White,
    primaryContainer = RoyalBluePrimary,
    onPrimaryContainer = Color.White,
    secondary = GoldVibrant,
    onSecondary = Color.Black,
    secondaryContainer = Navy800,
    onSecondaryContainer = GoldLight,
    tertiary = EmeraldSuccess,
    background = Navy900,
    onBackground = TextPrimaryOnDark,
    surface = Navy800,
    onSurface = TextPrimaryOnDark,
    surfaceVariant = Navy700,
    onSurfaceVariant = TextSecondaryOnDark,
    error = RoseError,
    outline = Navy700
)

private val LightColorScheme = lightColorScheme(
    primary = RoyalBluePrimary,
    onPrimary = Color.White,
    primaryContainer = RoyalBlueSoft,
    onPrimaryContainer = RoyalBluePrimary,
    secondary = GoldAccent,
    onSecondary = Color.White,
    secondaryContainer = GoldLight,
    onSecondaryContainer = GoldAccent,
    tertiary = EmeraldSuccess,
    background = SlateBackground,
    onBackground = TextPrimary,
    surface = SlateSurface,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = TextSecondary,
    error = RoseError,
    outline = SlateBorder
)

@Composable
fun SmartGroupTheme(
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
