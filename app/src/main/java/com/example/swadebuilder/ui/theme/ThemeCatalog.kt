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

// ─── Default Theme (Old School Paper) ────────────────────────────────────────
// Refined to match user screenshots: Beige background, Deep Red primary, Dark text.
val DefaultThemeData = AppThemeData(
    lightColors = lightColorScheme(
        primary = OldSchoolRed,
        onPrimary = Color.White,
        secondary = OldSchoolInk,
        tertiary = OldSchoolRed,
        background = OldSchoolPaper,
        onBackground = OldSchoolInk,
        surface = OldSchoolPaper, // Cards share the background color (visually separated by borders/shadows)
        onSurface = OldSchoolInk,
        surfaceVariant = OldSchoolPaper // Ensure variants also respect the beige tone unless specifically overridden
    ),
    darkColors = darkColorScheme(
        // In Dark Mode for this theme, we might just invert or keep it "Paper" style but dimmer?
        // User screenshots are Light mode. For Dark mode consistency with "Old School",
        // let's use the inverted Ink/Paper logic established previously.
        primary = OldSchoolRed,
        onPrimary = Color.White,
        secondary = OldSchoolPaper,
        tertiary = OldSchoolRed,
        background = OldSchoolInk,
        onBackground = OldSchoolPaper,
        surface = OldSchoolInk,
        onSurface = OldSchoolPaper
    ),
    typography = DefaultTypography // Standard Sans-Serif as seen in screenshots
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

// ─── Cyberpunk Theme (Matrix Style) ──────────────────────────────────────────
val CyberpunkThemeData = AppThemeData(
    // Matrix style is inherently dark. We map Light to a high-contrast version or same.
    lightColors = lightColorScheme(
        primary = CyberMatrixGreen,
        onPrimary = Color.Black,
        secondary = CyberMatrixDark,
        tertiary = CyberMatrixGreen,
        background = CyberMatrixBg,
        onBackground = CyberMatrixText,
        surface = CyberMatrixBg,
        onSurface = CyberMatrixText
    ),
    darkColors = darkColorScheme(
        primary = CyberMatrixGreen,
        onPrimary = Color.Black,
        secondary = CyberMatrixDark,
        tertiary = CyberMatrixGreen,
        background = CyberMatrixBg,
        onBackground = CyberMatrixText,
        surface = CyberMatrixBg,
        onSurface = CyberMatrixText
    ),
    typography = CyberpunkTypography
)

// ─── Sci-Fi Theme ────────────────────────────────────────────────────────────
val SciFiThemeData = AppThemeData(
    lightColors = lightColorScheme(
        primary = SciFiHolo,
        onPrimary = Color.Black,
        background = SciFiBlueDeep,
        surface = SciFiBlueDeep,
        onBackground = SciFiSilver,
        onSurface = SciFiSilver
    ),
    darkColors = darkColorScheme(
        primary = SciFiHolo,
        onPrimary = Color.Black,
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
        onPrimary = Color.White,
        background = HorrorBg,
        onBackground = HorrorBone,
        surface = HorrorBg,
        onSurface = HorrorBone
    ),
    darkColors = darkColorScheme(
        primary = HorrorBlood,
        onPrimary = Color.White,
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
        onPrimary = Color.White,
        background = WW2Paper,
        surface = WW2Khaki,
        onSurface = Color.Black,
        onBackground = Color.Black
    ),
    darkColors = darkColorScheme(
        primary = WW2Stamp,
        onPrimary = Color.White,
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
        onPrimary = Color.Black,
        secondary = HalloPurple,
        background = HalloBlack,
        onBackground = HalloOrange, // High contrast
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
    typography = HorrorTypography // Using HorrorTypography as requested in Step 2 for Halloween
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
