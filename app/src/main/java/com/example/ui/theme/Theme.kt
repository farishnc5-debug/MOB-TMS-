package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class ThemeMode(val displayName: String, val description: String) {
    SYSTEM("As Per System", "Follow your Android device system setting automatically"),
    DARK("Dark Mode", "Deep dark BI canvas with high-contrast glowing elements"),
    LIGHT("Light Mode", "Crisp bright clean interface with ultra-legible dark typography")
}

enum class LayoutThemeStyle(val displayName: String, val description: String) {
    NUDGE_NEO_BI("Nudge BI Neon Glass", "Modern glass BI dashboard with electric blue, cyan & glowing accents"),
    OBSIDIAN_EXECUTIVE("Obsidian Gold Executive", "Deep obsidian canvas with Saudi gold and sapphire accents"),
    LUMINA_ENTERPRISE("Lumina Clean Enterprise", "High-contrast corporate enterprise theme with royal blue & emerald")
}

// 1. NUDGE NEO BI SCHEMES (Inspired by Power BI / Nudge BI Attachments)
private val NudgeNeoDarkScheme = darkColorScheme(
    primary = Color(0xFF3B82F6), // Electric Blue
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFBFDBFE),
    secondary = Color(0xFF06B6D4), // Neon Cyan
    onSecondary = Color(0xFF001F28),
    secondaryContainer = Color(0xFF164E63),
    onSecondaryContainer = Color(0xFFCFFAFE),
    tertiary = Color(0xFF10B981), // Emerald Green
    onTertiary = Color(0xFF002114),
    tertiaryContainer = Color(0xFF064E3B),
    onTertiaryContainer = Color(0xFFA7F3D0),
    background = Color(0xFF0B0F19), // Deep Dark BI Canvas
    onBackground = Color(0xFFFFFFFF), // High Contrast White Text
    surface = Color(0xFF131C2E), // Sleek Card Container
    onSurface = Color(0xFFFFFFFF), // Crisp White Text
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFFF1F5F9), // Ultra Legible Off-White Text
    outline = Color(0xFF334155),
    outlineVariant = Color(0xFF475569)
)

private val NudgeNeoLightScheme = lightColorScheme(
    primary = Color(0xFF2563EB),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary = Color(0xFF0284C7),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF075985),
    tertiary = Color(0xFF059669),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFD1FAE5),
    onTertiaryContainer = Color(0xFF065F46),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A), // High Contrast Deep Navy Text
    surface = Color(0xFFFFFFFF), // Pure White Card
    onSurface = Color(0xFF0F172A), // Pure Dark Navy Bold Text
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF1E293B),
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0)
)

// 2. OBSIDIAN EXECUTIVE SCHEMES
private val ObsidianDarkScheme = darkColorScheme(
    primary = Color(0xFFF59E0B), // Saudi Gold
    onPrimary = Color(0xFF1E1200),
    primaryContainer = Color(0xFF3D2700),
    onPrimaryContainer = Color(0xFFFDE68A),
    secondary = Color(0xFF3B82F6),
    onSecondary = Color(0xFF001A40),
    secondaryContainer = Color(0xFF1E3A8A),
    onSecondaryContainer = Color(0xFFDBEAFE),
    tertiary = Color(0xFF10B981),
    onTertiary = Color(0xFF002214),
    tertiaryContainer = Color(0xFF064E3B),
    onTertiaryContainer = Color(0xFFD1FAE5),
    background = Color(0xFF050811),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF0D1322),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF172033),
    onSurfaceVariant = Color(0xFFF1F5F9),
    outline = Color(0xFF334155),
    outlineVariant = Color(0xFF475569)
)

private val ObsidianLightScheme = lightColorScheme(
    primary = Color(0xFFD97706),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFEF3C7),
    onPrimaryContainer = Color(0xFF78350F),
    secondary = Color(0xFF1D4ED8),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFDBEAFE),
    onSecondaryContainer = Color(0xFF1E40AF),
    tertiary = Color(0xFF047857),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFD1FAE5),
    onTertiaryContainer = Color(0xFF065F46),
    background = Color(0xFFF1F5F9),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF1E293B),
    outline = Color(0xFF94A3B8),
    outlineVariant = Color(0xFFCBD5E1)
)

// 3. LUMINA ENTERPRISE SCHEMES
private val LuminaLightScheme = lightColorScheme(
    primary = Color(0xFF1E40AF),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary = Color(0xFFD97706),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF78350F),
    tertiary = Color(0xFF059669),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFD1FAE5),
    onTertiaryContainer = Color(0xFF065F46),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF1E293B),
    outline = Color(0xFF94A3B8),
    outlineVariant = Color(0xFFCBD5E1)
)

private val LuminaDarkScheme = darkColorScheme(
    primary = Color(0xFF60A5FA),
    onPrimary = Color(0xFF0F172A),
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFBFDBFE),
    secondary = Color(0xFFFBBF24),
    onSecondary = Color(0xFF0F172A),
    secondaryContainer = Color(0xFF78350F),
    onSecondaryContainer = Color(0xFFFEF3C7),
    tertiary = Color(0xFF34D399),
    onTertiary = Color(0xFF0F172A),
    tertiaryContainer = Color(0xFF065F46),
    onTertiaryContainer = Color(0xFFD1FAE5),
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFF1F5F9),
    outline = Color(0xFF475569),
    outlineVariant = Color(0xFF64748B)
)

@Composable
fun MyApplicationTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    themeStyle: LayoutThemeStyle = LayoutThemeStyle.NUDGE_NEO_BI,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemDark
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val colorScheme = if (isDark) {
        when (themeStyle) {
            LayoutThemeStyle.NUDGE_NEO_BI -> NudgeNeoDarkScheme
            LayoutThemeStyle.OBSIDIAN_EXECUTIVE -> ObsidianDarkScheme
            LayoutThemeStyle.LUMINA_ENTERPRISE -> LuminaDarkScheme
        }
    } else {
        when (themeStyle) {
            LayoutThemeStyle.NUDGE_NEO_BI -> NudgeNeoLightScheme
            LayoutThemeStyle.OBSIDIAN_EXECUTIVE -> ObsidianLightScheme
            LayoutThemeStyle.LUMINA_ENTERPRISE -> LuminaLightScheme
        }
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}



