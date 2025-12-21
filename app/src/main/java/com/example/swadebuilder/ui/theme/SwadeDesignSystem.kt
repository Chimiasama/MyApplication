package com.example.swadebuilder.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape

@Immutable
data class SwadeColors(
    val primary: Color,
    val onPrimary: Color,
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val highlight: Color, // For selected items
    val border: Color
)

@Immutable
data class SwadeTypography(
    val header: TextStyle,
    val subHeader: TextStyle,
    val body: TextStyle,
    val label: TextStyle, // Serif for sidebar
    val statValue: TextStyle
)

@Immutable
data class SwadeShapes(
    val card: Shape,
    val button: Shape,
    val input: Shape
)

@Immutable
data class SwadeDimensions(
    val paddingSmall: Dp = 4.dp,
    val paddingMedium: Dp = 8.dp,
    val paddingLarge: Dp = 16.dp,
    val borderWidth: Dp = 2.dp
)

object SwadeDesignSystem {
    val colors: SwadeColors
        @Composable
        get() = LocalSwadeColors.current

    val typography: SwadeTypography
        @Composable
        get() = LocalSwadeTypography.current

    val shapes: SwadeShapes
        @Composable
        get() = LocalSwadeShapes.current

    val dimensions: SwadeDimensions
        @Composable
        get() = LocalSwadeDimensions.current
}

// Default values (Fallbacks)
val DefaultSwadeColors = SwadeColors(
    primary = Color(0xFF6200EE),
    onPrimary = Color.White,
    background = Color(0xFF121212), // Dark theme by default as per "Savage" vibe
    onBackground = Color.White,
    surface = Color(0xFF1E1E1E),
    onSurface = Color.White,
    highlight = Color(0xFF8B0000), // Dark Red for selection
    border = Color(0xFF333333)
)

val DefaultSwadeTypography = SwadeTypography(
    header = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp
    ),
    subHeader = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp
    ),
    body = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),
    label = TextStyle(
        fontFamily = FontFamily.Serif, // Requested serif for sidebar
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp
    ),
    statValue = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp
    )
)

val DefaultSwadeShapes = SwadeShapes(
    card = RoundedCornerShape(4.dp),
    button = RoundedCornerShape(4.dp),
    input = RectangleShape
)

val LocalSwadeColors = staticCompositionLocalOf { DefaultSwadeColors }
val LocalSwadeTypography = staticCompositionLocalOf { DefaultSwadeTypography }
val LocalSwadeShapes = staticCompositionLocalOf { DefaultSwadeShapes }
val LocalSwadeDimensions = staticCompositionLocalOf { SwadeDimensions() }

@Composable
fun SwadeTheme(
    colors: SwadeColors = DefaultSwadeColors,
    typography: SwadeTypography = DefaultSwadeTypography,
    shapes: SwadeShapes = DefaultSwadeShapes,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalSwadeColors provides colors,
        LocalSwadeTypography provides typography,
        LocalSwadeShapes provides shapes,
        LocalSwadeDimensions provides SwadeDimensions(),
        content = content
    )
}
