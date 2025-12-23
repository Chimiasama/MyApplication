package com.example.swadebuilder.ui.theme

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.core.view.WindowCompat

val LocalAppThemeData = staticCompositionLocalOf { DefaultThemeData }

@Composable
fun SWADEbuilderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    appTheme: AppTheme = AppTheme.DEFAULT,
    content: @Composable () -> Unit
) {
    val themeData = AllThemes[appTheme] ?: DefaultThemeData
    val colorScheme = if (darkTheme) themeData.darkColors else themeData.lightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()

            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalAppThemeData provides themeData) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = themeData.typography
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = colorScheme.background
            ) {
                val backgroundModifier = if (themeData.backgroundDrawable != null) {
                    Modifier.paint(
                        painter = painterResource(id = themeData.backgroundDrawable),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Modifier.background(colorScheme.background)
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(backgroundModifier)
                ) {
                    content()
                }
            }
        }
    }
}
