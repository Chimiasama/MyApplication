package com.example.swadebuilder.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.swadebuilder.R

// Set of Material typography styles to start with
val DefaultTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)

val OrbitronFontFamily = FontFamily(
    Font(R.font.orbitron_regular, FontWeight.Normal),
    Font(R.font.orbitron_medium, FontWeight.Medium),
    Font(R.font.orbitron_semi_bold, FontWeight.SemiBold),
    Font(R.font.orbitron_bold, FontWeight.Bold)
)

val MedievalFontFamily = FontFamily(
    Font(R.font.medieval_font)
)

val HorrorFontFamily = FontFamily(
    Font(R.font.horror_font)
)

val MatrixFontFamily = FontFamily(
    Font(R.font.matrix_font)
)

val SciFiTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = OrbitronFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    titleLarge = TextStyle(
        fontFamily = OrbitronFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = OrbitronFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)

val CyberpunkTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = OrbitronFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    titleLarge = TextStyle(
        fontFamily = OrbitronFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = OrbitronFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)

val MedievalTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = MedievalFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 66.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = MedievalFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = MedievalFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.5.sp
    )
)

val HorrorTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = HorrorFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = 0.2.sp
    ),
    titleLarge = TextStyle(
        fontFamily = HorrorFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.2.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = HorrorFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.2.sp
    )
)

val MatrixTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = MatrixFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = MatrixFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = MatrixFontFamily,
        fontWeight = FontWeight.Normal, // Hacker style: Normal
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp // Hacker style: 0.sp
    )
)

val PrideTypography = DefaultTypography
