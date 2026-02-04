package com.example.swadebuilder.ui.theme

import androidx.annotation.DrawableRes
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

data class AppThemeData(
    val lightColors: ColorScheme,
    val darkColors: ColorScheme,
    val typography: Typography,
    @DrawableRes val backgroundDrawable: Int? = null,
    val cardBorderColor: Color? = null // PASSO 2: Borda para efeito Wireframe
)

// ─── Default Theme (Old School Paper) ────────────────────────────────────────
val DefaultThemeData = AppThemeData(
    lightColors = lightColorScheme(
        primary = OldSchoolRed,
        onPrimary = Color.White,
        primaryContainer = OldSchoolRed.copy(alpha = 0.12f),
        onPrimaryContainer = OldSchoolInk,
        secondary = OldSchoolGold,
        onSecondary = OldSchoolInk,
        secondaryContainer = OldSchoolGold.copy(alpha = 0.18f),
        onSecondaryContainer = OldSchoolInk,
        tertiary = OldSchoolInkSoft,
        onTertiary = Color.White,
        background = OldSchoolPaper,
        onBackground = OldSchoolInk,
        surface = OldSchoolSurface,
        onSurface = OldSchoolInk,
        surfaceVariant = OldSchoolSurfaceVariant,
        onSurfaceVariant = OldSchoolInkSoft,
        outline = OldSchoolOutline,
        outlineVariant = OldSchoolOutline.copy(alpha = 0.6f),
        errorContainer = Color(0xFFD32F2F).copy(alpha = 0.12f),
        onErrorContainer = OldSchoolInk
    ),
    darkColors = darkColorScheme(
        primary = OldSchoolRed,
        onPrimary = Color.White,
        primaryContainer = OldSchoolRed.copy(alpha = 0.2f),
        onPrimaryContainer = OldSchoolPaper,
        secondary = OldSchoolGold,
        onSecondary = OldSchoolDarkBg,
        secondaryContainer = OldSchoolGold.copy(alpha = 0.2f),
        onSecondaryContainer = OldSchoolPaper,
        tertiary = OldSchoolInkSoft,
        onTertiary = OldSchoolPaper,
        background = OldSchoolDarkBg,
        onBackground = OldSchoolPaper,
        surface = OldSchoolDarkSurface,
        onSurface = OldSchoolPaper,
        surfaceVariant = OldSchoolDarkVariant,
        onSurfaceVariant = OldSchoolPaper.copy(alpha = 0.9f),
        outline = OldSchoolOutline.copy(alpha = 0.5f),
        outlineVariant = OldSchoolOutline.copy(alpha = 0.35f),
        errorContainer = Color(0xFFD32F2F).copy(alpha = 0.22f),
        onErrorContainer = OldSchoolPaper
    ),
    typography = DefaultTypography
)

// ─── Medieval Theme ──────────────────────────────────────────────────────────
val MedievalThemeData = AppThemeData(
    lightColors = lightColorScheme(
        primary = MedGold,
        onPrimary = Color.White,
        primaryContainer = MedGold.copy(alpha = 0.18f),
        onPrimaryContainer = MedInk,
        secondary = MedInk,
        onSecondary = MedPaper,
        secondaryContainer = MedInk.copy(alpha = 0.1f),
        onSecondaryContainer = MedInk,
        tertiary = MedGold.copy(alpha = 0.8f),
        onTertiary = MedPaper,
        background = MedPaper,
        onBackground = MedInk,
        surface = MedPaper.copy(alpha = 0.98f),
        onSurface = MedInk,
        surfaceVariant = MedPaper.copy(alpha = 0.9f),
        onSurfaceVariant = MedInk.copy(alpha = 0.85f),
        outline = MedGold.copy(alpha = 0.45f),
        outlineVariant = MedGold.copy(alpha = 0.25f),
        errorContainer = MedPaper.copy(alpha = 0.85f),
        onErrorContainer = MedInk
    ),
    darkColors = darkColorScheme(
        primary = MedGold,
        onPrimary = Color.White,
        primaryContainer = MedGold.copy(alpha = 0.25f),
        onPrimaryContainer = MedPaper,
        secondary = MedPaper,
        onSecondary = MedInk,
        secondaryContainer = MedInk.copy(alpha = 0.6f),
        onSecondaryContainer = MedPaper,
        tertiary = MedGold.copy(alpha = 0.9f),
        onTertiary = MedInk,
        background = MedInk,
        onBackground = MedPaper,
        surface = Color(0xFF2B1E1A),
        onSurface = MedPaper,
        surfaceVariant = Color(0xFF3A2A24),
        onSurfaceVariant = MedPaper.copy(alpha = 0.9f),
        outline = MedGold.copy(alpha = 0.4f),
        outlineVariant = MedGold.copy(alpha = 0.25f),
        errorContainer = Color(0xFF3A1E1E),
        onErrorContainer = MedPaper
    ),
    typography = MedievalTypography
)

// ─── Cyberpunk Theme (Matrix Style) ──────────────────────────────────────────
// PASSO 2: Fundo preto e Borda Wireframe
// HOTFIX: Texto branco forçado para legibilidade contra fundo preto
// HOTFIX 2: surfaceVariant set to Black to fix card background contrast with white text
val CyberpunkThemeData = AppThemeData(
    lightColors = lightColorScheme(
        primary = CyberMatrixGreen,
        onPrimary = Color.Black,
        secondary = CyberMatrixDark,
        tertiary = Color(0xFF00E5FF),
        background = CyberMatrixBg,
        onBackground = CyberMatrixText,
        surface = Color(0xFF050505),
        onSurface = CyberMatrixText,
        surfaceVariant = Color(0xFF0E0E0E),
        onSurfaceVariant = Color(0xFF7CFF9B),
        outline = CyberMatrixGreen.copy(alpha = 0.6f),
        outlineVariant = CyberMatrixGreen.copy(alpha = 0.35f),
        errorContainer = Color(0xFF3A0000),
        onErrorContainer = Color(0xFFFFCFCF)
    ),
    darkColors = darkColorScheme(
        primary = CyberMatrixGreen,
        onPrimary = Color.Black,
        secondary = CyberMatrixDark,
        tertiary = Color(0xFF00E5FF),
        background = Color(0xFF000000),
        onBackground = CyberMatrixText,
        surface = Color(0xFF060606),
        onSurface = CyberMatrixText,
        surfaceVariant = Color(0xFF141414),
        onSurfaceVariant = Color(0xFF7CFF9B),
        outline = CyberMatrixGreen.copy(alpha = 0.6f),
        outlineVariant = CyberMatrixGreen.copy(alpha = 0.35f),
        errorContainer = Color(0xFF3A0000),
        onErrorContainer = Color(0xFFFFCFCF)
    ),
    typography = CyberpunkTypography,
    cardBorderColor = CyberMatrixGreen // Efeito Wireframe
)

// ─── Sci-Fi Theme ────────────────────────────────────────────────────────────
val SciFiThemeData = AppThemeData(
    lightColors = lightColorScheme(
        primary = SciFiHolo,
        onPrimary = Color.Black,
        secondary = Color(0xFF6FD8FF),
        onSecondary = Color(0xFF0C141C),
        tertiary = SciFiSilver,
        onTertiary = Color(0xFF0C141C),
        background = SciFiBlueDeep,
        surface = Color(0xFF0C131E),
        onBackground = SciFiSilver,
        onSurface = SciFiSilver,
        surfaceVariant = Color(0xFF131B26),
        onSurfaceVariant = Color(0xFFB0BEC5),
        outline = SciFiHolo.copy(alpha = 0.5f),
        outlineVariant = SciFiSilver.copy(alpha = 0.3f),
        errorContainer = Color(0xFF2A0E0E),
        onErrorContainer = Color(0xFFFFCDD2)
    ),
    darkColors = darkColorScheme(
        primary = SciFiHolo,
        onPrimary = Color.Black,
        secondary = Color(0xFF6FD8FF),
        onSecondary = Color(0xFF051018),
        tertiary = SciFiSilver,
        onTertiary = Color(0xFF051018),
        background = Color(0xFF04070D),
        surface = Color(0xFF0A0F18),
        onBackground = Color(0xFFE6F0F4),
        onSurface = Color(0xFFE6F0F4),
        surfaceVariant = Color(0xFF151C27),
        onSurfaceVariant = Color(0xFFC0CCD2),
        outline = SciFiHolo.copy(alpha = 0.45f),
        outlineVariant = SciFiSilver.copy(alpha = 0.25f),
        errorContainer = Color(0xFF2A0E0E),
        onErrorContainer = Color(0xFFFFCDD2)
    ),
    typography = SciFiTypography,
    cardBorderColor = SciFiHolo.copy(alpha = 0.5f)
)

// ─── Horror Theme ────────────────────────────────────────────────────────────
// PASSO 4: Ajuste de contraste
val HorrorThemeData = AppThemeData(
    lightColors = lightColorScheme(
        primary = HorrorBlood, // Vermelho apenas para destaques
        onPrimary = Color.White,
        secondary = Color(0xFF4B1A1A),
        onSecondary = HorrorBone,
        tertiary = Color(0xFF9E7B5B),
        onTertiary = Color(0xFF1A0F0F),
        background = HorrorBg,
        onBackground = HorrorBone,
        surface = Color(0xFF120B0B),
        onSurface = HorrorBone,
        surfaceVariant = Color(0xFF1E1212),
        onSurfaceVariant = Color(0xFFE6E0D4),
        outline = HorrorBlood.copy(alpha = 0.55f),
        outlineVariant = HorrorBlood.copy(alpha = 0.3f),
        errorContainer = Color(0xFF3A1212),
        onErrorContainer = HorrorBone
    ),
    darkColors = darkColorScheme(
        primary = HorrorBlood,
        onPrimary = Color.White,
        secondary = Color(0xFF4B1A1A),
        onSecondary = HorrorBone,
        tertiary = Color(0xFF9E7B5B),
        onTertiary = Color(0xFF1A0F0F),
        background = Color(0xFF050505),
        onBackground = Color(0xFFEBEBD3),
        surface = Color(0xFF0C0A0A),
        onSurface = Color(0xFFEBEBD3),
        surfaceVariant = Color(0xFF1B1212),
        onSurfaceVariant = Color(0xFFBDB7AA),
        outline = HorrorBlood.copy(alpha = 0.5f),
        outlineVariant = HorrorBlood.copy(alpha = 0.28f),
        errorContainer = Color(0xFF3A1212),
        onErrorContainer = HorrorBone
    ),
    typography = HorrorTypography
)

// ─── WW2 Theme ───────────────────────────────────────────────────────────────
val WW2ThemeData = AppThemeData(
    lightColors = lightColorScheme(
        primary = WW2Stamp,
        onPrimary = Color.White,
        primaryContainer = WW2Khaki.copy(alpha = 0.5f),
        onPrimaryContainer = Color.Black,
        secondary = WW2Olive,
        onSecondary = Color.White,
        secondaryContainer = WW2Olive.copy(alpha = 0.2f),
        onSecondaryContainer = Color.Black,
        tertiary = WW2Paper,
        onTertiary = Color.Black,
        background = WW2Paper,
        surface = WW2Khaki,
        onSurface = Color.Black,
        onBackground = Color.Black,
        surfaceVariant = WW2Khaki.copy(alpha = 0.8f),
        onSurfaceVariant = Color.Black,
        outline = WW2Stamp.copy(alpha = 0.4f),
        outlineVariant = WW2Stamp.copy(alpha = 0.25f),
        errorContainer = WW2Olive.copy(alpha = 0.3f),
        onErrorContainer = Color.Black
    ),
    darkColors = darkColorScheme(
        primary = WW2Paper,
        onPrimary = Color.Black,
        primaryContainer = WW2Stamp.copy(alpha = 0.4f),
        onPrimaryContainer = WW2Paper,
        secondary = WW2Khaki,
        onSecondary = Color.Black,
        secondaryContainer = WW2Stamp.copy(alpha = 0.35f),
        onSecondaryContainer = WW2Paper,
        tertiary = WW2Paper,
        onTertiary = Color.Black,
        background = WW2Olive,
        surface = Color(0xFF1B1D16),
        onSurface = WW2Khaki,
        onBackground = WW2Khaki,
        surfaceVariant = Color(0xFF2B2F25),
        onSurfaceVariant = WW2Khaki,
        outline = WW2Paper.copy(alpha = 0.35f),
        outlineVariant = WW2Paper.copy(alpha = 0.2f),
        errorContainer = Color(0xFF2A1F00),
        onErrorContainer = WW2Khaki
    ),
    typography = WW2Typography
)

// ─── Halloween Theme ─────────────────────────────────────────────────────────
val HalloweenThemeData = AppThemeData(
    lightColors = lightColorScheme(
        primary = HalloOrange,
        onPrimary = Color.Black,
        secondary = HalloPurple,
        onSecondary = Color.White,
        tertiary = Color(0xFFFFD54F),
        onTertiary = Color.Black,
        background = Color(0xFF0D0A0F),
        onBackground = Color(0xFFFFC37A),
        surface = Color(0xFF131017),
        onSurface = Color(0xFFFFC37A),
        surfaceVariant = Color(0xFF1C1624),
        onSurfaceVariant = Color(0xFFF3C48C),
        outline = HalloOrange.copy(alpha = 0.5f),
        outlineVariant = HalloPurple.copy(alpha = 0.35f),
        errorContainer = Color(0xFF2A0C0C),
        onErrorContainer = Color(0xFFFFC37A)
    ),
    darkColors = darkColorScheme(
        primary = HalloOrange,
        onPrimary = Color.Black,
        secondary = HalloPurple,
        onSecondary = Color.White,
        tertiary = Color(0xFFFFD54F),
        onTertiary = Color.Black,
        background = Color(0xFF060407),
        onBackground = Color(0xFFFFE0B2),
        surface = Color(0xFF0F0C12),
        onSurface = Color(0xFFFFE0B2),
        surfaceVariant = Color(0xFF1A1320),
        onSurfaceVariant = Color(0xFFE0C4A0),
        outline = HalloOrange.copy(alpha = 0.5f),
        outlineVariant = HalloPurple.copy(alpha = 0.35f),
        errorContainer = Color(0xFF2A0C0C),
        onErrorContainer = Color(0xFFFFE0B2)
    ),
    typography = HorrorTypography
)

// ─── Minimalist Theme ────────────────────────────────────────────────────────
val MinimalistThemeData = AppThemeData(
    lightColors = lightColorScheme(
        primary = MinimalistLightPrimary,
        onPrimary = Color.White,
        secondary = Color(0xFF3D3D3D),
        onSecondary = Color.White,
        tertiary = MinimalistLightGreen,
        onTertiary = MinimalistLightText,
        background = MinimalistLightBg,
        surface = MinimalistLightSurface,
        onBackground = MinimalistLightText,
        onSurface = MinimalistLightText,
        surfaceVariant = Color(0xFFEDEDED),
        onSurfaceVariant = MinimalistLightText.copy(alpha = 0.85f),
        outline = Color(0xFFBDBDBD),
        outlineVariant = Color(0xFFE0E0E0),
        errorContainer = MinimalistLightYellow,
        onErrorContainer = MinimalistLightText
    ),
    darkColors = darkColorScheme(
        primary = MinimalistDarkPrimary,
        onPrimary = Color.Black,
        secondary = Color(0xFFD6D6D6),
        onSecondary = Color.Black,
        tertiary = MinimalistDarkGreen,
        onTertiary = Color.White,
        background = MinimalistDarkBg,
        surface = MinimalistDarkSurface,
        onBackground = MinimalistDarkText,
        onSurface = MinimalistDarkText,
        surfaceVariant = Color(0xFF2A2A2A),
        onSurfaceVariant = MinimalistDarkText.copy(alpha = 0.9f),
        outline = Color(0xFF424242),
        outlineVariant = Color(0xFF333333),
        errorContainer = MinimalistDarkYellow,
        onErrorContainer = Color.Black
    ),
    typography = MinimalistTypography
)

val AllThemes = mapOf(
    AppTheme.DEFAULT to DefaultThemeData,
    AppTheme.MEDIEVAL to MedievalThemeData,
    AppTheme.CYBERPUNK to CyberpunkThemeData,
    AppTheme.WW2 to WW2ThemeData,
    AppTheme.HORROR to HorrorThemeData,
    AppTheme.SCIFI to SciFiThemeData,
    AppTheme.MINIMALIST to MinimalistThemeData,
    AppTheme.HALLOWEEN to HalloweenThemeData
)
