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
    @DrawableRes val backgroundDrawable: Int? = null
)

// ─── Default Theme ───────────────────────────────────────────────────────────
val DefaultThemeData = AppThemeData(
    lightColors = lightColorScheme(
        primary = PurpleGrey40,
        secondary = PurpleGrey40,
        tertiary = Pink40
    ),
    darkColors = darkColorScheme(
        primary = PurpleGrey80,
        secondary = PurpleGrey80,
        tertiary = Pink80
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
        onSurface = MedInk
    ),
    darkColors = darkColorScheme(
        primary = MedGold,
        onPrimary = Color.White,
        background = MedInk,
        onBackground = MedPaper,
        surface = MedInk,
        onSurface = MedPaper
    ),
    typography = MedievalTypography
)

// ─── Cyberpunk Theme ─────────────────────────────────────────────────────────
val CyberpunkThemeData = AppThemeData(
    lightColors = lightColorScheme(
        primary = CyberPink,
        secondary = CyberCyan,
        tertiary = CyberPink,
        background = CyberBg,
        surface = CyberSurface,
        onSurface = Color.White // or lighter Cyan if needed, but White provides good contrast
    ),
    darkColors = darkColorScheme(
        primary = CyberPink,
        secondary = CyberCyan,
        tertiary = CyberPink,
        background = CyberBg,
        surface = CyberSurface,
        onSurface = Color.White
    ),
    typography = CyberpunkTypography
)

// ─── Sci-Fi Theme ────────────────────────────────────────────────────────────
val SciFiThemeData = AppThemeData(
    lightColors = lightColorScheme(
        primary = SciFiHolo,
        background = SciFiBlueDeep,
        surface = SciFiBlueDeep, // Assuming consistent clean look
        onBackground = SciFiSilver,
        onSurface = SciFiSilver
    ),
    darkColors = darkColorScheme(
        primary = SciFiHolo,
        background = SciFiBlueDeep,
        surface = SciFiBlueDeep,
        onBackground = SciFiSilver,
        onSurface = SciFiSilver
    ),
    typography = SciFiTypography
)

// ─── Horror Theme ────────────────────────────────────────────────────────────
val HorrorThemeData = AppThemeData(
    lightColors = lightColorScheme(
        primary = HorrorBlood,
        background = HorrorBg,
        onBackground = HorrorBone,
        surface = HorrorBg,
        onSurface = HorrorBone
    ),
    darkColors = darkColorScheme(
        primary = HorrorBlood,
        background = HorrorBg,
        onBackground = HorrorBone,
        surface = HorrorBg,
        onSurface = HorrorBone
    ),
    typography = HorrorTypography
)

// ─── WW2 Theme ───────────────────────────────────────────────────────────────
val WW2ThemeData = AppThemeData(
    lightColors = lightColorScheme(
        primary = WW2Stamp,
        background = WW2Paper,
        surface = WW2Khaki,
        onSurface = Color.Black,
        onBackground = Color.Black
    ),
    darkColors = darkColorScheme(
        primary = WW2Stamp,
        background = WW2Olive,
        surface = Color.Black,
        onSurface = WW2Khaki,
        onBackground = WW2Khaki
    ),
    typography = WW2Typography
)

// ─── Halloween Theme ─────────────────────────────────────────────────────────
val HalloweenThemeData = AppThemeData(
    lightColors = lightColorScheme(
        primary = HalloOrange,
        secondary = HalloPurple,
        background = HalloBlack,
        onBackground = HalloOrange, // High contrast
        surface = HalloBlack,
        onSurface = HalloOrange
    ),
    darkColors = darkColorScheme(
        primary = HalloOrange,
        secondary = HalloPurple,
        background = HalloBlack,
        onBackground = HalloOrange,
        surface = HalloBlack,
        onSurface = HalloOrange
    ),
    typography = HorrorTypography // Using HorrorTypography as requested in Step 2 for Halloween
)

// ─── Pride Theme ─────────────────────────────────────────────────────────────
val PrideThemeData = AppThemeData(
    lightColors = lightColorScheme(
        primary = PrideViolet,
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
