package com.example.swadebuilder.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource

val LocalAppThemeData = staticCompositionLocalOf { DefaultThemeData }

@Composable
fun SWADEbuilderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    appTheme: AppTheme = AppTheme.DEFAULT,
    content: @Composable () -> Unit
) {
    val themeData = AllThemes[appTheme] ?: DefaultThemeData
    val colorScheme = if (darkTheme) themeData.darkColors else themeData.lightColors

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
