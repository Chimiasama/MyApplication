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
        primaryContainer = Color(0xFFFFDAD6), // Lighter Red for container
        onPrimaryContainer = Color(0xFF410002), // Dark Red for text on container
        secondary = OldSchoolInk,
        tertiary = OldSchoolRed,
        background = OldSchoolPaper,
        onBackground = OldSchoolInk,
        surface = OldSchoolPaper,
        onSurface = OldSchoolInk,
        surfaceVariant = OldSchoolPaper,
        errorContainer = Color(0xFFD32F2F).copy(alpha = 0.1f), // Reddish fade
        onErrorContainer = OldSchoolInk
    ),
    darkColors = darkColorScheme(
        primary = OldSchoolRed,
        onPrimary = Color.White,
        primaryContainer = Color(0xFF93000A), // Darker Red for container
        onPrimaryContainer = Color(0xFFFFDAD6), // Light Red for text
        secondary = OldSchoolPaper,
        tertiary = OldSchoolRed,
        background = OldSchoolInk,
        onBackground = OldSchoolPaper,
        surface = OldSchoolInk,
        onSurface = OldSchoolPaper,
        errorContainer = Color(0xFFD32F2F).copy(alpha = 0.2f),
        onErrorContainer = OldSchoolPaper
    ),
    typography = DefaultTypography
)

// ─── Medieval Theme ──────────────────────────────────────────────────────────
val MedievalThemeData = AppThemeData(
    lightColors = lightColorScheme(
        primary = MedGold,
        onPrimary = Color.White,
        primaryContainer = Color(0xFFEBE0D0), // Slightly darker paper/gold blend? Or just standard M3 default? Setting a safe one.
        onPrimaryContainer = MedInk,
        background = MedPaper,
        onBackground = MedInk,
        surface = MedPaper,
        onSurface = MedInk,
        surfaceVariant = Color(0xFFF5EEDC),
        onSurfaceVariant = MedInk,
        errorContainer = MedPaper.copy(alpha = 0.9f), // Keep it paper-like but distinct? Or perhaps a faded red ink
        onErrorContainer = MedInk
    ),
    darkColors = darkColorScheme(
        primary = MedGold,
        onPrimary = Color.White,
        background = MedInk,
        onBackground = MedPaper,
        surface = MedInk,
        onSurface = MedPaper,
        surfaceVariant = Color(0xFF5D4037), // Dark Brown (contrasts with MedPaper text)
        onSurfaceVariant = MedPaper,
        errorContainer = MedInk,
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
        tertiary = CyberMatrixGreen,
        background = CyberMatrixBg, // Preto
        onBackground = Color.White, // HOTFIX: Texto branco
        surface = CyberMatrixBg, // Preto (sem cinza)
        onSurface = Color.White, // HOTFIX: Texto branco
        surfaceVariant = Color(0xFF121212), // HOTFIX 2: Cards must be black to match white text
        onSurfaceVariant = Color(0xFF00E5FF), // HOTFIX: Ciano para labels
        errorContainer = Color(0xFF2C0000), // Dark red matrix
        onErrorContainer = Color(0xFFFF0000)
    ),
    darkColors = darkColorScheme(
        primary = CyberMatrixGreen,
        onPrimary = Color.Black,
        secondary = CyberMatrixDark,
        tertiary = CyberMatrixGreen,
        background = Color(0xFF000000), // Preto absoluto
        onBackground = Color.White, // Texto branco
        surface = Color(0xFF050505), // Quase preto
        onSurface = Color.White, // Texto branco
        surfaceVariant = Color(0xFF121212), // Header escuro
        onSurfaceVariant = Color(0xFF00E5FF), // Ciano para brilhar
        errorContainer = Color(0xFF2C0000), // Dark red matrix
        onErrorContainer = Color(0xFFFF0000)
    ),
    typography = CyberpunkTypography,
    cardBorderColor = CyberMatrixGreen // Efeito Wireframe
)

// ─── Sci-Fi Theme ────────────────────────────────────────────────────────────
val SciFiThemeData = AppThemeData(
    lightColors = lightColorScheme(
        primary = SciFiHolo,
        onPrimary = Color.Black,
        background = SciFiBlueDeep,
        surface = SciFiBlueDeep,
        onBackground = SciFiSilver,
        onSurface = SciFiSilver,
        onSurfaceVariant = Color(0xFFB0BEC5),
        errorContainer = SciFiBlueDeep.copy(alpha = 0.8f),
        onErrorContainer = Color(0xFFFF5252)
    ),
    darkColors = darkColorScheme(
        primary = SciFiHolo,
        onPrimary = Color.Black,
        background = Color(0xFF050505), // Fundo muito escuro
        surface = Color(0xFF0A0A0A), // Card ligeiramente mais claro
        onBackground = Color.White,
        onSurface = Color.White,
        surfaceVariant = Color(0xFF1F1F1F), // Header cinza chumbo
        onSurfaceVariant = Color(0xFFB0B0B0), // Cinza claro
        errorContainer = Color(0xFF1A0000),
        onErrorContainer = Color(0xFFFF5252)
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
        background = HorrorBg,
        onBackground = HorrorBone, // Branco sujo para texto
        surface = HorrorBg,
        onSurface = HorrorBone,
        surfaceVariant = Color(0xFF1C1010),
        onSurfaceVariant = Color(0xFFEBEBD3),
        errorContainer = Color(0xFF2C0E0E), // Dried blood dark
        onErrorContainer = HorrorBone
    ),
    darkColors = darkColorScheme(
        primary = HorrorBlood,
        onPrimary = Color.White,
        background = Color(0xFF050505), // Fundo muito escuro
        onBackground = Color(0xFFEBEBD3), // Osso
        surface = Color(0xFF0A0A0A), // Card ligeiramente mais claro
        onSurface = Color(0xFFEBEBD3), // Osso
        surfaceVariant = Color(0xFF1F1F1F), // Header escuro
        onSurfaceVariant = Color(0xFFAAAAAA), // Cinza claro
        errorContainer = Color(0xFF2C0E0E),
        onErrorContainer = HorrorBone
    ),
    typography = HorrorTypography
)

// ─── WW2 Theme ───────────────────────────────────────────────────────────────
val WW2ThemeData = AppThemeData(
    lightColors = lightColorScheme(
        primary = WW2Stamp,
        onPrimary = Color.White,
        background = WW2Paper,
        surface = WW2Khaki,
        onSurface = Color.Black,
        onBackground = Color.Black,
        surfaceVariant = WW2Khaki,
        onSurfaceVariant = Color.Black,
        errorContainer = WW2Olive.copy(alpha = 0.3f),
        onErrorContainer = Color.Black
    ),
    darkColors = darkColorScheme(
        primary = WW2Paper,
        onPrimary = Color.Black,
        background = WW2Olive,
        surface = Color.Black,
        onSurface = WW2Khaki,
        onBackground = WW2Khaki,
        surfaceVariant = WW2Stamp, // Dark Cyan/Slate (contrasts with WW2Khaki text)
        onSurfaceVariant = WW2Khaki,
        errorContainer = Color(0xFF1F1F00),
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
        background = HalloBlack,
        onBackground = HalloOrange,
        surface = HalloBlack,
        onSurface = HalloOrange,
        errorContainer = Color(0xFF220000),
        onErrorContainer = HalloOrange
    ),
    darkColors = darkColorScheme(
        primary = HalloOrange,
        onPrimary = Color.Black,
        secondary = HalloPurple,
        background = Color(0xFF000000),
        onBackground = Color.White,
        surface = Color(0xFF050505),
        onSurface = Color.White,
        surfaceVariant = Color(0xFF1F1F1F),
        onSurfaceVariant = Color(0xFFB0B0B0),
        errorContainer = Color(0xFF220000),
        onErrorContainer = HalloOrange
    ),
    typography = HorrorTypography
)

// ─── Minimalist Theme ────────────────────────────────────────────────────────
val MinimalistThemeData = AppThemeData(
    lightColors = lightColorScheme(
        primary = MinimalistLightPrimary,
        onPrimary = Color.White,
        secondary = MinimalistLightPrimary,
        tertiary = MinimalistLightPrimary,
        background = MinimalistLightBg,
        surface = MinimalistLightSurface,
        onBackground = MinimalistLightText,
        onSurface = MinimalistLightText,
        surfaceVariant = MinimalistLightGreen, // Active elements background
        onSurfaceVariant = MinimalistLightText,
        errorContainer = MinimalistLightYellow, // Pending elements background (was black)
        onErrorContainer = MinimalistLightText // Text on pending elements (ensure readability)
    ),
    darkColors = darkColorScheme(
        primary = MinimalistDarkPrimary,
        onPrimary = Color.Black,
        secondary = MinimalistDarkPrimary,
        tertiary = MinimalistDarkPrimary,
        background = MinimalistDarkBg,
        surface = MinimalistDarkSurface,
        onBackground = MinimalistDarkText,
        onSurface = MinimalistDarkText,
        surfaceVariant = MinimalistDarkGreen,
        onSurfaceVariant = MinimalistDarkText,
        errorContainer = MinimalistDarkYellow,
        onErrorContainer = Color.Black // Ensure text is readable on yellow
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
