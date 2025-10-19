package com.example.myapplication.ui.theme

import android.os.Build
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.example.myapplication.R

// ─── Custom App Colors ─────────────────────────────────────────────────
private val PaperSurface = Color(0xFFF0E1C8)
private val TextColor     = Color.Black
private val AccentRed     = Color(0xFFB71C1C)

// ─── Dark Theme ───────────────────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary            = PergaminhoFundo,
    secondary          = PurpleGrey80,
    tertiary           = Pink80,
    background         = PaperSurface,
    surface            = PaperSurface,
    onPrimary          = Color.White,
    onSecondary        = Color.White,
    onTertiary         = Color.White,
    onBackground       = TextColor,
    onSurface          = TextColor,
    primaryContainer   = PaperSurface,
    onPrimaryContainer = TextColor
)

// ─── Light Theme ──────────────────────────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary            = AccentRed,
    secondary          = PurpleGrey40,
    tertiary           = Pink40,
    background         = PaperSurface,
    surface            = PaperSurface,
    onPrimary          = Color.White,
    onSecondary        = Color.White,
    onTertiary         = Color.White,
    onBackground       = TextColor,
    onSurface          = TextColor,
    primaryContainer   = PaperSurface,
    onPrimaryContainer = TextColor
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography
    ) {
        Box(Modifier.fillMaxSize()) {
            // camada de pergaminho
            Image(
                painter           = painterResource(R.drawable.bg_pergaminho),
                contentDescription = null,
                contentScale      = ContentScale.Crop,
                modifier          = Modifier.fillMaxSize()
            )
            // conteúdo por cima, transparent surface para respeitar cores do tema
            Surface(
                modifier = Modifier.fillMaxSize(),
                color    = Color.Transparent
            ) {
                content()
            }
        }
    }
}
