package com.aiagram.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AIagramColorScheme = darkColorScheme(
    primary = Gold,
    onPrimary = OnPrimary,
    primaryContainer = GoldDeep,
    onPrimaryContainer = Black,
    secondary = GoldDark,
    onSecondary = OnPrimary,
    secondaryContainer = SurfaceVariant,
    onSecondaryContainer = OnBackground,
    tertiary = GoldLight,
    onTertiary = Black,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceMuted,
    error = Error,
    onError = Black,
    outline = Divider,
    outlineVariant = SurfaceVariant,
    scrim = Color(0xCC000000),
)

@Composable
fun AIagramTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AIagramColorScheme,
        typography = AIagramTypography,
        content = content
    )
}
