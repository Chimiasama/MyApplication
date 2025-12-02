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

// ─── Cores base ──────────────────────────────────────────────────────────────
private val PaperSurface = Color(0xFFF0E1C8)          // pergaminho (tema claro)
private val PaperSurfaceAlt = Color(0xFFE6D5B4)
private val AccentRed = Color(0xFFB71C1C)

// Paleta dark
private val DarkBackground = Color(0xFF101015)
private val DarkSurface = Color(0xFF18171C)
private val DarkSurfaceVariant = Color(0xFF26252B)
private val DarkOn = Color(0xFFEDE0D0)

// ─── Default Theme ───────────────────────────────────────────────────────────
val DefaultThemeData = AppThemeData(
    lightColors = lightColorScheme(
        primary = AccentRed,
        secondary = PurpleGrey40,
        tertiary = Pink40,
        background = PaperSurface,
        surface = PaperSurface,
        surfaceVariant = PaperSurfaceAlt,
        onPrimary = Color.White,
        onSecondary = Color.White,
        onTertiary = Color.White,
        onBackground = Color.Black,
        onSurface = Color.Black,
        primaryContainer = PaperSurfaceAlt,
        onPrimaryContainer = Color.Black
    ),
    darkColors = darkColorScheme(
        primary = AccentRed,
        secondary = PurpleGrey80,
        tertiary = Pink80,
        background = DarkBackground,
        surface = DarkSurface,
        surfaceVariant = DarkSurfaceVariant,
        onPrimary = Color.White,
        onSecondary = Color.White,
        onTertiary = Color.White,
        onBackground = DarkOn,
        onSurface = DarkOn,
        primaryContainer = DarkSurfaceVariant,
        onPrimaryContainer = DarkOn
    ),
    typography = DefaultTypography
)

// ─── Medieval Theme ──────────────────────────────────────────────────────────
val MedievalThemeData = AppThemeData(
    lightColors = lightColorScheme(
        primary = MedievalBrown,
        secondary = MedievalAccent,
        tertiary = Pink40,
        background = MedievalParchment,
        surface = MedievalParchment,
        surfaceVariant = Color(0xFFE0D9D3),
        onPrimary = Color.White,
        onSecondary = Color.White,
        onTertiary = Color.White,
        onBackground = MedievalBrown,
        onSurface = MedievalBrown,
        primaryContainer = MedievalAccent,
        onPrimaryContainer = MedievalParchment
    ),
    darkColors = darkColorScheme(
        primary = MedievalAccent,
        secondary = MedievalParchment,
        tertiary = Pink80,
        background = MedievalDark,
        surface = MedievalBrown,
        surfaceVariant = Color(0xFF3A2A26),
        onPrimary = Color.Black,
        onSecondary = Color.Black,
        onTertiary = Color.Black,
        onBackground = MedievalParchment,
        onSurface = MedievalParchment,
        primaryContainer = MedievalBrown,
        onPrimaryContainer = MedievalParchment
    ),
    typography = MedievalTypography
)

// ─── Cyberpunk (Matrix) Theme ────────────────────────────────────────────────
val CyberpunkThemeData = AppThemeData(
    lightColors = lightColorScheme(
        primary = MatrixGreen,
        secondary = Color.Gray,
        tertiary = MatrixGreen,
        background = MatrixBlack,
        surface = Color(0xFF0A0A0A),
        surfaceVariant = Color(0xFF1A1A1A),
        onPrimary = MatrixBlack,
        onSecondary = MatrixBlack,
        onTertiary = MatrixBlack,
        onBackground = MatrixGreen,
        onSurface = MatrixGreen,
        primaryContainer = Color(0xFF003B07),
        onPrimaryContainer = MatrixGreen
    ),
    darkColors = darkColorScheme(
        primary = MatrixGreen,
        secondary = Color.DarkGray,
        tertiary = MatrixGreen,
        background = MatrixBlack,
        surface = Color(0xFF0A0A0A),
        surfaceVariant = Color(0xFF1A1A1A),
        onPrimary = MatrixBlack,
        onSecondary = MatrixBlack,
        onTertiary = MatrixBlack,
        onBackground = MatrixGreen,
        onSurface = MatrixGreen,
        primaryContainer = Color(0xFF003B07),
        onPrimaryContainer = MatrixGreen
    ),
    typography = MatrixTypography
)

// ─── WW2 Theme ───────────────────────────────────────────────────────────────
val WW2ThemeData = AppThemeData(
    lightColors = lightColorScheme(
        primary = WW2Olive,
        secondary = WW2Gray,
        tertiary = Pink40,
        background = WW2Khaki,
        surface = Color(0xFFE6DDB5),
        surfaceVariant = Color(0xFFD9CEA1),
        onPrimary = Color.White,
        onSecondary = Color.White,
        onTertiary = Color.White,
        onBackground = Color.Black,
        onSurface = Color.Black,
        primaryContainer = WW2Olive,
        onPrimaryContainer = WW2Khaki
    ),
    darkColors = darkColorScheme(
        primary = WW2Khaki,
        secondary = WW2Gray,
        tertiary = Pink80,
        background = WW2Dark,
        surface = Color(0xFF424242),
        surfaceVariant = Color(0xFF535353),
        onPrimary = Color.Black,
        onSecondary = Color.Black,
        onTertiary = Color.Black,
        onBackground = WW2Khaki,
        onSurface = WW2Khaki,
        primaryContainer = WW2Dark,
        onPrimaryContainer = WW2Khaki
    ),
    typography = DefaultTypography
)

// ─── Horror Theme ────────────────────────────────────────────────────────────
val HorrorThemeData = AppThemeData(
    lightColors = lightColorScheme(
        primary = HorrorRed,
        secondary = HorrorGray,
        tertiary = Pink40,
        background = Color.White,
        surface = Color(0xFFFAFAFA),
        surfaceVariant = Color(0xFFF0F0F0),
        onPrimary = Color.White,
        onSecondary = Color.White,
        onTertiary = Color.White,
        onBackground = HorrorBlack,
        onSurface = HorrorBlack,
        primaryContainer = HorrorRed,
        onPrimaryContainer = Color.White
    ),
    darkColors = darkColorScheme(
        primary = HorrorRed,
        secondary = HorrorGray,
        tertiary = Pink80,
        background = HorrorBlack,
        surface = HorrorDark,
        surfaceVariant = HorrorGray,
        onPrimary = Color.White,
        onSecondary = Color.White,
        onTertiary = Color.White,
        onBackground = Color.White,
        onSurface = Color.White,
        primaryContainer = HorrorDark,
        onPrimaryContainer = Color.White
    ),
    typography = DefaultTypography
)

// ─── Sci-Fi Theme ────────────────────────────────────────────────────────────
val SciFiThemeData = AppThemeData(
    lightColors = lightColorScheme(
        primary = SciFiBlue,
        secondary = SciFiAccent,
        tertiary = Pink40,
        background = SciFiSilver,
        surface = Color.White,
        surfaceVariant = Color(0xFFF5F5F5),
        onPrimary = Color.White,
        onSecondary = Color.Black,
        onTertiary = Color.Black,
        onBackground = Color.Black,
        onSurface = Color.Black,
        primaryContainer = SciFiBlue,
        onPrimaryContainer = Color.White
    ),
    darkColors = darkColorScheme(
        primary = SciFiAccent,
        secondary = SciFiBlue,
        tertiary = Pink80,
        background = SciFiDark,
        surface = Color(0xFF1A1A3A),
        surfaceVariant = Color(0xFF2A2A4A),
        onPrimary = Color.Black,
        onSecondary = Color.White,
        onTertiary = Color.Black,
        onBackground = SciFiSilver,
        onSurface = SciFiSilver,
        primaryContainer = SciFiDark,
        onPrimaryContainer = SciFiAccent
    ),
    typography = DefaultTypography
)

// ─── Pride Theme ─────────────────────────────────────────────────────────────
val PrideThemeData = AppThemeData(
    lightColors = lightColorScheme(
        primary = PrideViolet,
        secondary = PrideBlue,
        tertiary = PrideGreen,
        background = Color.White,
        surface = Color.White,
        surfaceVariant = PrideYellow,
        onPrimary = Color.White,
        onSecondary = Color.White,
        onTertiary = Color.White,
        onBackground = Color.Black,
        onSurface = Color.Black,
        primaryContainer = PrideOrange,
        onPrimaryContainer = Color.Black
    ),
    darkColors = darkColorScheme(
        primary = PrideYellow,
        secondary = PrideOrange,
        tertiary = PrideRed,
        background = Color.Black,
        surface = Color(0xFF121212),
        surfaceVariant = PrideViolet,
        onPrimary = Color.Black,
        onSecondary = Color.Black,
        onTertiary = Color.Black,
        onBackground = Color.White,
        onSurface = Color.White,
        primaryContainer = PrideBlue,
        onPrimaryContainer = Color.White
    ),
    typography = PrideTypography,
    // backgroundDrawable = com.example.swadebuilder.R.drawable.pride_background // CRASH FIX: Temporarily disabled
)

// ─── Halloween Theme ─────────────────────────────────────────────────────────
val HalloweenThemeData = AppThemeData(
    lightColors = lightColorScheme(
        primary = HalloweenOrange,
        secondary = HalloweenPurple,
        tertiary = Color.Yellow,
        background = HalloweenBlack,
        surface = Color(0xFF1E1E1E),
        surfaceVariant = Color(0xFF333333),
        onPrimary = Color.Black,
        onSecondary = Color.White,
        onTertiary = Color.Black,
        onBackground = HalloweenOrange,
        onSurface = Color.White,
        primaryContainer = HalloweenPurple,
        onPrimaryContainer = Color.White
    ),
    darkColors = darkColorScheme(
        primary = HalloweenOrange,
        secondary = HalloweenPurple,
        tertiary = Color.Yellow,
        background = HalloweenBlack,
        surface = Color(0xFF1E1E1E),
        surfaceVariant = Color(0xFF333333),
        onPrimary = Color.Black,
        onSecondary = Color.White,
        onTertiary = Color.Black,
        onBackground = HalloweenOrange,
        onSurface = Color.White,
        primaryContainer = HalloweenPurple,
        onPrimaryContainer = Color.White
    ),
    typography = DefaultTypography
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
