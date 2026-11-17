package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun BasariPusulasiTheme(
    palette: ThemePalette = ThemePalette.OCEAN,
    darkModePref: DarkModePref = DarkModePref.SYSTEM,
    content: @Composable () -> Unit
) {
    val isDark = when (darkModePref) {
        DarkModePref.SYSTEM -> isSystemInDarkTheme()
        DarkModePref.LIGHT -> false
        DarkModePref.DARK -> true
    }

    val colorScheme = if (isDark) darkSchemeFor(palette) else lightSchemeFor(palette)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
