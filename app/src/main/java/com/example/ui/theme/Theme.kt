package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CyberDarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color(0xFF04101A),
    primaryContainer = Color(0xFF0A3644),
    onPrimaryContainer = Color(0xFFBAF0FF),
    secondary = NeonPurple,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF381559),
    onSecondaryContainer = Color(0xFFEADBFF),
    tertiary = NeonPink,
    onTertiary = Color.White,
    background = CyberNavyDark,
    onBackground = TextPrimary,
    surface = CyberNavySurface,
    onSurface = TextPrimary,
    surfaceVariant = CyberNavyCard,
    onSurfaceVariant = TextSecondary,
    outline = CyberNavyBorder,
    error = VerdictFakeRed,
    onError = Color.White
)

private val CyberLightColorScheme = lightColorScheme(
    primary = NeonCyanDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = NeonPurpleDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF3E8FF),
    onSecondaryContainer = Color(0xFF6B21A8),
    tertiary = NeonPink,
    onTertiary = Color.White,
    background = CyberLightBg,
    onBackground = TextPrimaryLight,
    surface = CyberLightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = CyberLightCard,
    onSurfaceVariant = TextSecondaryLight,
    outline = CyberLightBorder,
    error = VerdictFakeRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) CyberDarkColorScheme else CyberLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
