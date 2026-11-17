package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

enum class ThemePalette(
    val key: String,
    val displayName: String,
    val previewPrimary: Color,
    val previewSecondary: Color
) {
    OCEAN("OCEAN", "Okyanus Mavisi", Color(0xFF0B6E99), Color(0xFF00A896)),
    EMERALD("EMERALD", "Zümrüt Yeşili", Color(0xFF0F766E), Color(0xFF10B981)),
    ROYAL("ROYAL", "Kraliyet Moru", Color(0xFF5B21B6), Color(0xFFEC4899)),
    SUNSET("SUNSET", "Gün Batımı", Color(0xFFC2410C), Color(0xFFF59E0B)),
    RUBY("RUBY", "Yakut Kırmızısı", Color(0xFFBE123C), Color(0xFFFB7185)),
    INDIGO("INDIGO", "Gece İndigosu", Color(0xFF1E3A8A), Color(0xFF38BDF8));

    companion object {
        fun fromKey(key: String?): ThemePalette =
            entries.firstOrNull { it.key == key } ?: OCEAN
    }
}

enum class DarkModePref(val key: String, val displayName: String) {
    SYSTEM("SYSTEM", "Sistem"),
    LIGHT("LIGHT", "Açık Mod"),
    DARK("DARK", "Koyu Mod");

    companion object {
        fun fromKey(key: String?): DarkModePref =
            entries.firstOrNull { it.key == key } ?: SYSTEM
    }
}

// Semantic status colors shared across charts & badges
val StatusSuccess = Color(0xFF16A34A)
val StatusSuccessContainer = Color(0xFFDCFCE7)
val StatusWarning = Color(0xFFD97706)
val StatusWarningContainer = Color(0xFFFEF3C7)
val StatusError = Color(0xFFDC2626)
val StatusErrorContainer = Color(0xFFFEE2E2)
val StatusInfo = Color(0xFF0284C7)

val ChartColors = listOf(
    Color(0xFF0B6E99),
    Color(0xFF10B981),
    Color(0xFFF59E0B),
    Color(0xFF8B5CF6),
    Color(0xFFEC4899),
    Color(0xFFEF4444),
    Color(0xFF06B6D4),
    Color(0xFF84CC16)
)

fun lightSchemeFor(palette: ThemePalette): ColorScheme = when (palette) {
    ThemePalette.OCEAN -> lightColorScheme(
        primary = Color(0xFF0B6E99),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFD1EFFF),
        onPrimaryContainer = Color(0xFF00263A),
        secondary = Color(0xFF00897B),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFCCFBF1),
        onSecondaryContainer = Color(0xFF002B25),
        tertiary = Color(0xFFF59E0B),
        onTertiary = Color(0xFF281800),
        tertiaryContainer = Color(0xFFFEF3C7),
        onTertiaryContainer = Color(0xFF3E2700),
        background = Color(0xFFF6F9FC),
        onBackground = Color(0xFF0F172A),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF0F172A),
        surfaceVariant = Color(0xFFE7EFF7),
        onSurfaceVariant = Color(0xFF475569),
        outline = Color(0xFFCBD5E1)
    )
    ThemePalette.EMERALD -> lightColorScheme(
        primary = Color(0xFF0F766E),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFCCFBF1),
        onPrimaryContainer = Color(0xFF042F2E),
        secondary = Color(0xFF059669),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFD1FAE5),
        onSecondaryContainer = Color(0xFF022C22),
        tertiary = Color(0xFFD97706),
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFFEF3C7),
        onTertiaryContainer = Color(0xFF451A03),
        background = Color(0xFFF4FBF8),
        onBackground = Color(0xFF0F1F1C),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF0F1F1C),
        surfaceVariant = Color(0xFFE2F2EC),
        onSurfaceVariant = Color(0xFF3D5A52),
        outline = Color(0xFFBDD7CE)
    )
    ThemePalette.ROYAL -> lightColorScheme(
        primary = Color(0xFF5B21B6),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFEDE9FE),
        onPrimaryContainer = Color(0xFF2E1065),
        secondary = Color(0xFFDB2777),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFFCE7F3),
        onSecondaryContainer = Color(0xFF500724),
        tertiary = Color(0xFF0284C7),
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFE0F2FE),
        onTertiaryContainer = Color(0xFF082F49),
        background = Color(0xFFF8F7FC),
        onBackground = Color(0xFF1B1528),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF1B1528),
        surfaceVariant = Color(0xFFECE8F7),
        onSurfaceVariant = Color(0xFF524A68),
        outline = Color(0xFFD4CCE8)
    )
    ThemePalette.SUNSET -> lightColorScheme(
        primary = Color(0xFFC2410C),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFFFEDD5),
        onPrimaryContainer = Color(0xFF431407),
        secondary = Color(0xFFD97706),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFFEF3C7),
        onSecondaryContainer = Color(0xFF451A03),
        tertiary = Color(0xFF0F766E),
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFCCFBF1),
        onTertiaryContainer = Color(0xFF042F2E),
        background = Color(0xFFFFFBF7),
        onBackground = Color(0xFF24160E),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF24160E),
        surfaceVariant = Color(0xFFF9ECE2),
        onSurfaceVariant = Color(0xFF6B4F3F),
        outline = Color(0xFFEAD4C4)
    )
    ThemePalette.RUBY -> lightColorScheme(
        primary = Color(0xFFBE123C),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFFFE4E6),
        onPrimaryContainer = Color(0xFF4C0519),
        secondary = Color(0xFF9333EA),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFF3E8FF),
        onSecondaryContainer = Color(0xFF3B0764),
        tertiary = Color(0xFF0284C7),
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFE0F2FE),
        onTertiaryContainer = Color(0xFF082F49),
        background = Color(0xFFFFF8F9),
        onBackground = Color(0xFF241016),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF241016),
        surfaceVariant = Color(0xFFF9E5E9),
        onSurfaceVariant = Color(0xFF6A454E),
        outline = Color(0xFFE8C8D0)
    )
    ThemePalette.INDIGO -> lightColorScheme(
        primary = Color(0xFF1E3A8A),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFDBEAFE),
        onPrimaryContainer = Color(0xFF172554),
        secondary = Color(0xFF0284C7),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFE0F2FE),
        onSecondaryContainer = Color(0xFF082F49),
        tertiary = Color(0xFFF59E0B),
        onTertiary = Color(0xFF281800),
        tertiaryContainer = Color(0xFFFEF3C7),
        onTertiaryContainer = Color(0xFF451A03),
        background = Color(0xFFF5F7FB),
        onBackground = Color(0xFF0F172A),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF0F172A),
        surfaceVariant = Color(0xFFE5EAF5),
        onSurfaceVariant = Color(0xFF475569),
        outline = Color(0xFFCBD5E1)
    )
}

fun darkSchemeFor(palette: ThemePalette): ColorScheme = when (palette) {
    ThemePalette.OCEAN -> darkColorScheme(
        primary = Color(0xFF56CCF2),
        onPrimary = Color(0xFF003247),
        primaryContainer = Color(0xFF004C69),
        onPrimaryContainer = Color(0xFFD1EFFF),
        secondary = Color(0xFF4FD1C5),
        onSecondary = Color(0xFF003832),
        secondaryContainer = Color(0xFF00524A),
        onSecondaryContainer = Color(0xFFCCFBF1),
        tertiary = Color(0xFFFBBF24),
        onTertiary = Color(0xFF3E2700),
        tertiaryContainer = Color(0xFF5A3B00),
        onTertiaryContainer = Color(0xFFFEF3C7),
        background = Color(0xFF0B131E),
        onBackground = Color(0xFFE8F1F8),
        surface = Color(0xFF121D2B),
        onSurface = Color(0xFFE8F1F8),
        surfaceVariant = Color(0xFF1C2B3D),
        onSurfaceVariant = Color(0xFF9FB4C9),
        outline = Color(0xFF2E4259)
    )
    ThemePalette.EMERALD -> darkColorScheme(
        primary = Color(0xFF34D399),
        onPrimary = Color(0xFF003824),
        primaryContainer = Color(0xFF065F46),
        onPrimaryContainer = Color(0xFFD1FAE5),
        secondary = Color(0xFF2DD4BF),
        onSecondary = Color(0xFF003731),
        secondaryContainer = Color(0xFF0F766E),
        onSecondaryContainer = Color(0xFFCCFBF1),
        tertiary = Color(0xFFFBBF24),
        onTertiary = Color(0xFF3E2700),
        background = Color(0xFF0A1613),
        onBackground = Color(0xFFE5F4EE),
        surface = Color(0xFF11211D),
        onSurface = Color(0xFFE5F4EE),
        surfaceVariant = Color(0xFF1A302B),
        onSurfaceVariant = Color(0xFF9AC0B5),
        outline = Color(0xFF2B4A42)
    )
    ThemePalette.ROYAL -> darkColorScheme(
        primary = Color(0xFFA78BFA),
        onPrimary = Color(0xFF2B1063),
        primaryContainer = Color(0xFF4C1D95),
        onPrimaryContainer = Color(0xFFEDE9FE),
        secondary = Color(0xFFF472B6),
        onSecondary = Color(0xFF4F0729),
        secondaryContainer = Color(0xFF831843),
        onSecondaryContainer = Color(0xFFFCE7F3),
        tertiary = Color(0xFF38BDF8),
        onTertiary = Color(0xFF00334C),
        background = Color(0xFF110D1C),
        onBackground = Color(0xFFEDE9F8),
        surface = Color(0xFF191428),
        onSurface = Color(0xFFEDE9F8),
        surfaceVariant = Color(0xFF251E3B),
        onSurfaceVariant = Color(0xFFB3A9CE),
        outline = Color(0xFF3B3159)
    )
    ThemePalette.SUNSET -> darkColorScheme(
        primary = Color(0xFFFB923C),
        onPrimary = Color(0xFF4A1800),
        primaryContainer = Color(0xFF9A3412),
        onPrimaryContainer = Color(0xFFFFEDD5),
        secondary = Color(0xFFFBBF24),
        onSecondary = Color(0xFF3E2700),
        secondaryContainer = Color(0xFF78350F),
        onSecondaryContainer = Color(0xFFFEF3C7),
        tertiary = Color(0xFF2DD4BF),
        onTertiary = Color(0xFF003731),
        background = Color(0xFF19100B),
        onBackground = Color(0xFFF8EDE6),
        surface = Color(0xFF231711),
        onSurface = Color(0xFFF8EDE6),
        surfaceVariant = Color(0xFF332219),
        onSurfaceVariant = Color(0xFFC9ADA0),
        outline = Color(0xFF4D3529)
    )
    ThemePalette.RUBY -> darkColorScheme(
        primary = Color(0xFFFB7185),
        onPrimary = Color(0xFF4C0519),
        primaryContainer = Color(0xFF881337),
        onPrimaryContainer = Color(0xFFFFE4E6),
        secondary = Color(0xFFC084FC),
        onSecondary = Color(0xFF3B0764),
        secondaryContainer = Color(0xFF6B21A8),
        onSecondaryContainer = Color(0xFFF3E8FF),
        tertiary = Color(0xFF38BDF8),
        onTertiary = Color(0xFF00334C),
        background = Color(0xFF180C11),
        onBackground = Color(0xFFF8E7EC),
        surface = Color(0xFF221218),
        onSurface = Color(0xFFF8E7EC),
        surfaceVariant = Color(0xFF321B24),
        onSurfaceVariant = Color(0xFFC9A1AE),
        outline = Color(0xFF4B2B37)
    )
    ThemePalette.INDIGO -> darkColorScheme(
        primary = Color(0xFF93C5FD),
        onPrimary = Color(0xFF0F265C),
        primaryContainer = Color(0xFF1E3A8A),
        onPrimaryContainer = Color(0xFFDBEAFE),
        secondary = Color(0xFF38BDF8),
        onSecondary = Color(0xFF00334C),
        secondaryContainer = Color(0xFF075985),
        onSecondaryContainer = Color(0xFFE0F2FE),
        tertiary = Color(0xFFFBBF24),
        onTertiary = Color(0xFF3E2700),
        background = Color(0xFF0A101E),
        onBackground = Color(0xFFE6ECF8),
        surface = Color(0xFF111A2E),
        onSurface = Color(0xFFE6ECF8),
        surfaceVariant = Color(0xFF1B2742),
        onSurfaceVariant = Color(0xFF9FB0D1),
        outline = Color(0xFF2D3E63)
    )
}
