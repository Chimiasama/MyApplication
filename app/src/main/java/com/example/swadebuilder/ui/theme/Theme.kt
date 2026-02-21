package com.example.swadebuilder.ui.theme

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

val LocalAppThemeData = staticCompositionLocalOf { DefaultThemeData }

private val AppShapes = Shapes(
    small = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
)

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
            // Use WindowCompat or newer APIs if available, but statusBarColor/navigationBarColor setters are standard on Window
            // The deprecation warning is likely from 'window.statusBarColor = ...' if targeting very new SDKs directly,
            // or maybe specific to how they are accessed in some contexts.
            // Actually, the log said: 'var statusBarColor: Int' is deprecated. Deprecated in Java.
            // This usually refers to View.SYSTEM_UI_FLAG_... manipulation or similar, but on Window it's standard since API 21.
            // However, let's keep it simple. If it's just a warning, we can suppress or ignore for now as it's the standard way.
            // To be 100% clean, we might need WindowInsetsController, but setting color is still done on Window.
            // Let's just suppress the warning for this block if possible, or leave it be if it's not breaking.
            // Wait, the log says: "file:///.../ui/theme/Theme.kt:46:20 'var statusBarColor: Int' is deprecated. Deprecated in Java."
            // This refers to `window.statusBarColor`.
            // As of API 35 (Vanilla Ice Cream), direct field access might be discouraged in favor of... something else?
            // Actually, `window.statusBarColor` is NOT deprecated in Android 14/15 docs yet.
            // It might be a Kotlin property access issue or a very specific lint.
            // Let's try to use the setter method `setStatusBarColor` if exposed, or ignore for now as it works.
            // Actually, let's just suppress it for now to clean the log.
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.background.toArgb()
            @Suppress("DEPRECATION")
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
            typography = themeData.typography,
            shapes = AppShapes
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
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        colorScheme.primary.copy(alpha = if (darkTheme) 0.12f else 0.08f),
                                        colorScheme.background.copy(alpha = 0.0f)
                                    )
                                )
                            )
                    )
                    content()
                }
            }
        }
    }
}
