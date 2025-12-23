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
        secondary = OldSchoolInk,
        tertiary = OldSchoolRed,
        background = OldSchoolPaper,
        onBackground = OldSchoolInk,
        surface = OldSchoolPaper,
        onSurface = OldSchoolInk,
        surfaceVariant = OldSchoolPaper
    ),
    darkColors = darkColorScheme(
        primary = OldSchoolRed,
        onPrimary = Color.White,
        secondary = OldSchoolPaper,
        tertiary = OldSchoolRed,
        background = OldSchoolInk,
        onBackground = OldSchoolPaper,
        surface = OldSchoolInk,
        onSurface = OldSchoolPaper
    ),
    typography = DefaultTypography
)

// ─── Medieval Theme ──────────────────────────────────────────────────────────
val MedievalThemeData = AppThemeData(
    lightColors = lightColorScheme(
        primary = MedGold,
        onPrimary = Color.White,
        background = MedPaper,
        onBackground = MedInk,
        surface = MedPaper,
        onSurface = MedInk,
        surfaceVariant = Color(0xFFF5EEDC),
        onSurfaceVariant = MedInk
    ),
    darkColors = darkColorScheme(
        primary = MedGold,
        onPrimary = Color.White,
        background = MedInk,
        onBackground = MedPaper,
        surface = MedInk,
        onSurface = MedPaper,
        surfaceVariant = Color(0xFFF5EEDC),
        onSurfaceVariant = MedInk
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
        surfaceVariant = Color(0xFF1A1A1A), // HOTFIX 2: Cards must be black to match white text
        onSurfaceVariant = Color(0xFFCCCCCC) // HOTFIX: Cinza claro
    ),
    darkColors = darkColorScheme(
        primary = CyberMatrixGreen,
        onPrimary = Color.Black,
        secondary = CyberMatrixDark,
        tertiary = CyberMatrixGreen,
        background = CyberMatrixBg,
        onBackground = Color.White, // HOTFIX: Texto branco
        surface = CyberMatrixBg,
        onSurface = Color.White, // HOTFIX: Texto branco
        surfaceVariant = Color(0xFF1A1A1A), // HOTFIX 2: Cards must be black to match white text
        onSurfaceVariant = Color(0xFFCCCCCC) // HOTFIX: Cinza claro
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
        onSurfaceVariant = Color(0xFFB0BEC5)
    ),
    darkColors = darkColorScheme(
        primary = SciFiHolo,
        onPrimary = Color.Black,
        background = SciFiBlueDeep,
        surface = SciFiBlueDeep,
        onBackground = SciFiSilver,
        onSurface = SciFiSilver,
        onSurfaceVariant = Color(0xFFB0BEC5)
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
        onSurfaceVariant = Color(0xFFEBEBD3)
    ),
    darkColors = darkColorScheme(
        primary = HorrorBlood,
        onPrimary = Color.White,
        background = HorrorBg,
        onBackground = HorrorBone,
        surface = HorrorBg,
        onSurface = HorrorBone,
        surfaceVariant = Color(0xFF1C1010),
        onSurfaceVariant = Color(0xFFEBEBD3)
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
        onSurfaceVariant = Color.Black
    ),
    darkColors = darkColorScheme(
        primary = WW2Stamp,
        onPrimary = Color.White,
        background = WW2Olive,
        surface = Color.Black,
        onSurface = WW2Khaki,
        onBackground = WW2Khaki,
        surfaceVariant = WW2Khaki,
        onSurfaceVariant = Color.Black
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
        onSurface = HalloOrange
    ),
    darkColors = darkColorScheme(
        primary = HalloOrange,
        onPrimary = Color.Black,
        secondary = HalloPurple,
        background = HalloBlack,
        onBackground = HalloOrange,
        surface = HalloBlack,
        onSurface = HalloOrange
    ),
    typography = HorrorTypography
)

// ─── Pride Theme ─────────────────────────────────────────────────────────────
val PrideThemeData = AppThemeData(
    lightColors = lightColorScheme(
        primary = PrideViolet,
        onPrimary = Color.White,
        secondary = PrideRed,
        tertiary = PrideGreen,
        background = Color.White,
        surface = Color.White,
        surfaceVariant = PrideYellow.copy(alpha = 0.2f),
        onBackground = Color.Black,
        onSurface = Color.Black
    ),
    darkColors = darkColorScheme(
        primary = PrideOrange,
        onPrimary = Color.Black,
        secondary = PrideBlue,
        tertiary = PrideGreen,
        background = PrideDarkBg,
        surface = PrideDarkBg,
        onSurface = Color.White,
        onBackground = Color.White
    ),
    typography = PrideTypography
)

val AllThemes = mapOf(
    AppTheme.DEFAULT to DefaultThemeData,
    AppTheme.MEDIEVAL to MedievalThemeData,
    AppTheme.CYBERPUNK to CyberpunkThemeData,
    AppTheme.WW2 to WW2ThemeData,
    AppTheme.HORROR to HorrorThemeData,
    AppTheme.SCIFI to SciFiThemeData,
    AppTheme.PRIDE to PrideThemeData,
    AppTheme.HALLOWEEN to HalloweenThemeData
)
