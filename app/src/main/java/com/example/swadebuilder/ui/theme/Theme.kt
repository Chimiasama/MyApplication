package com.example.swadebuilder.ui.theme

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// ─── Cores base ──────────────────────────────────────────────────────────────
private val PaperSurface    = Color(0xFFF0E1C8)          // pergaminho (tema claro)
private val PaperSurfaceAlt = Color(0xFFE6D5B4)
private val AccentRed       = Color(0xFFB71C1C)

// Paleta dark
private val DarkBackground     = Color(0xFF101015)
private val DarkSurface        = Color(0xFF18171C)
private val DarkSurfaceVariant = Color(0xFF26252B)
private val DarkOn             = Color(0xFFEDE0D0)

// ─── Dark Theme ──────────────────────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary            = AccentRed,
    secondary          = PurpleGrey80,
    tertiary           = Pink80,
    background         = DarkBackground,
    surface            = DarkSurface,
    surfaceVariant     = DarkSurfaceVariant,
    onPrimary          = Color.White,
    onSecondary        = Color.White,
    onTertiary         = Color.White,
    onBackground       = DarkOn,
    onSurface          = DarkOn,
    primaryContainer   = DarkSurfaceVariant,
    onPrimaryContainer = DarkOn
)

// ─── Light Theme ─────────────────────────────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary            = AccentRed,
    secondary          = PurpleGrey40,
    tertiary           = Pink40,
    background         = PaperSurface,
    surface            = PaperSurface,
    surfaceVariant     = PaperSurfaceAlt,
    onPrimary          = Color.White,
    onSecondary        = Color.White,
    onTertiary         = Color.White,
    onBackground       = Color.Black,
    onSurface          = Color.Black,
    primaryContainer   = PaperSurfaceAlt,
    onPrimaryContainer = Color.Black
)

// ─── Medieval Theme ──────────────────────────────────────────────────────────
private val MedievalLightColorScheme = lightColorScheme(
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
)

private val MedievalDarkColorScheme = darkColorScheme(
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
)

// ─── Cyberpunk Theme ─────────────────────────────────────────────────────────
private val CyberpunkLightColorScheme = lightColorScheme(
    primary = CyberpunkPink,
    secondary = CyberpunkCyan,
    tertiary = CyberpunkPink,
    background = CyberpunkDark,
    surface = CyberpunkBlack,
    surfaceVariant = Color(0xFF2C001E),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = CyberpunkCyan,
    onSurface = CyberpunkCyan,
    primaryContainer = CyberpunkPink,
    onPrimaryContainer = Color.Black
)

private val CyberpunkDarkColorScheme = darkColorScheme(
    primary = CyberpunkCyan,
    secondary = CyberpunkPink,
    tertiary = CyberpunkCyan,
    background = CyberpunkBlack,
    surface = CyberpunkDark,
    surfaceVariant = Color(0xFF2C2C2C),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = CyberpunkCyan,
    onSurface = CyberpunkCyan,
    primaryContainer = CyberpunkDark,
    onPrimaryContainer = CyberpunkCyan
)


// ─── WW2 Theme ───────────────────────────────────────────────────────────────
private val WW2LightColorScheme = lightColorScheme(
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
)

private val WW2DarkColorScheme = darkColorScheme(
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
)

// ─── Horror Theme ────────────────────────────────────────────────────────────
private val HorrorLightColorScheme = lightColorScheme(
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
)

private val HorrorDarkColorScheme = darkColorScheme(
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
)

// ─── Sci-Fi Theme ────────────────────────────────────────────────────────────
private val SciFiLightColorScheme = lightColorScheme(
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
)

private val SciFiDarkColorScheme = darkColorScheme(
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
)

@Composable
fun SWADEbuilderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    appTheme: AppTheme = AppTheme.DEFAULT,
    content: @Composable () -> Unit
) {
    val colorScheme = when (appTheme) {
        AppTheme.DEFAULT -> if (darkTheme) DarkColorScheme else LightColorScheme
        AppTheme.MEDIEVAL -> if (darkTheme) MedievalDarkColorScheme else MedievalLightColorScheme
        AppTheme.CYBERPUNK -> if (darkTheme) CyberpunkDarkColorScheme else CyberpunkLightColorScheme
        AppTheme.WW2 -> if (darkTheme) WW2DarkColorScheme else WW2LightColorScheme
        AppTheme.HORROR -> if (darkTheme) HorrorDarkColorScheme else HorrorLightColorScheme
        AppTheme.SCIFI -> if (darkTheme) SciFiDarkColorScheme else SciFiLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography
    ) {
        // fundo usando o background do tema
        Surface(
            modifier = Modifier.fillMaxSize(),
            color    = colorScheme.background
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colorScheme.background)
            ) {
                content()
            }
        }
    }
}
