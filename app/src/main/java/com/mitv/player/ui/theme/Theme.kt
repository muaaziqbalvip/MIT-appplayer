package com.mitv.player.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.mitv.player.domain.model.ThemeMode

// --- Dark Theme ---
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF00B4D8),
    onPrimary = Color(0xFF003545),
    primaryContainer = Color(0xFF004E63),
    onPrimaryContainer = Color(0xFFB8EAFF),
    secondary = Color(0xFF90CDF4),
    background = Color(0xFF0A0E1A),
    onBackground = Color(0xFFE1E7F0),
    surface = Color(0xFF121826),
    onSurface = Color(0xFFE1E7F0),
    surfaceVariant = Color(0xFF1E2A3A),
    error = Color(0xFFFF6B6B),
    outline = Color(0xFF2D3E50)
)

// --- Light Theme ---
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF006787),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBAEAFF),
    onPrimaryContainer = Color(0xFF001F2A),
    secondary = Color(0xFF0077B6),
    background = Color(0xFFF0F4F8),
    onBackground = Color(0xFF1A1C1E),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFDDE3EA),
    error = Color(0xFFD32F2F),
    outline = Color(0xFFB0BEC5)
)

// --- Premium Gold Theme ---
val GoldPrimary = Color(0xFFFFD700)
val GoldSecondary = Color(0xFFFFA500)
val GoldBackground = Color(0xFF0D0D0D)
val GoldSurface = Color(0xFF1A1400)
val GoldSurfaceVariant = Color(0xFF2A2000)
val GoldOnPrimary = Color(0xFF1A1400)
val GoldOnBackground = Color(0xFFFFF8E1)

private val GoldColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = GoldOnPrimary,
    primaryContainer = Color(0xFF3D2E00),
    onPrimaryContainer = Color(0xFFFFE57F),
    secondary = GoldSecondary,
    background = GoldBackground,
    onBackground = GoldOnBackground,
    surface = GoldSurface,
    onSurface = GoldOnBackground,
    surfaceVariant = GoldSurfaceVariant,
    error = Color(0xFFFF6B6B),
    outline = Color(0xFF5C4A00)
)

@Composable
fun MiTVTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode) {
        ThemeMode.DARK -> DarkColorScheme
        ThemeMode.LIGHT -> LightColorScheme
        ThemeMode.PREMIUM_GOLD -> GoldColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MiTVTypography,
        content = content
    )
}
