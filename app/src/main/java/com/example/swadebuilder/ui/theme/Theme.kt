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

@Composable
fun SWADEbuilderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // ✅ segue o tema do sistema
    dynamicColor: Boolean = false,              // ✅ usa tuas cores por padrão
    content: @Composable () -> Unit
) {
    val colorScheme =
        if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Se algum dia quiser usar Material You, é só passar dynamicColor = true
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        } else {
            if (darkTheme) DarkColorScheme else LightColorScheme
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
