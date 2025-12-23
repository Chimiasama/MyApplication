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

fun createTypography(
    fontFamily: FontFamily,
    lineHeightFactor: Float = 0f, // Additive now
    letterSpacingFactor: Float = 0f, // Additive now
    isHacker: Boolean = false,
    isCyberpunkTitle: Boolean = false
): Typography {
    val default = DefaultTypography

    // Helper to conditionally apply styles
    fun fontWeight(original: FontWeight): FontWeight {
        return if (isHacker) FontWeight.Normal
               else if (isCyberpunkTitle && original >= FontWeight.Medium) FontWeight.ExtraBold
               else original
    }

    // Specific logic for titleLarge in Cyberpunk
    fun fontWeightTitleLarge(original: FontWeight): FontWeight {
        return if (isHacker) FontWeight.Normal
               else if (isCyberpunkTitle) FontWeight.ExtraBold
               else original
    }

    fun letterSpacing(original: Double): Double {
        return if (isHacker) 0.0 else original + letterSpacingFactor.toDouble()
    }

    fun lineHeight(original: Double): Double {
        return original + lineHeightFactor.toDouble()
    }

    return Typography(
        displayLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = fontWeight(FontWeight.Normal),
            fontSize = 57.sp,
            lineHeight = lineHeight(64.0).sp,
            letterSpacing = letterSpacing(-0.25).sp
        ),
        displayMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = fontWeight(FontWeight.Normal),
            fontSize = 45.sp,
            lineHeight = lineHeight(52.0).sp,
            letterSpacing = letterSpacing(0.0).sp
        ),
        displaySmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = fontWeight(FontWeight.Normal),
            fontSize = 36.sp,
            lineHeight = lineHeight(44.0).sp,
            letterSpacing = letterSpacing(0.0).sp
        ),
        headlineLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = fontWeight(FontWeight.Normal),
            fontSize = 32.sp,
            lineHeight = lineHeight(40.0).sp,
            letterSpacing = letterSpacing(0.0).sp
        ),
        headlineMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = fontWeight(FontWeight.Normal),
            fontSize = 28.sp,
            lineHeight = lineHeight(36.0).sp,
            letterSpacing = letterSpacing(0.0).sp
        ),
        headlineSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = fontWeight(FontWeight.Normal),
            fontSize = 24.sp,
            lineHeight = lineHeight(32.0).sp,
            letterSpacing = letterSpacing(0.0).sp
        ),
        titleLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = fontWeightTitleLarge(FontWeight.Normal),
            fontSize = 22.sp,
            lineHeight = lineHeight(28.0).sp,
            letterSpacing = letterSpacing(0.0).sp
        ),
        titleMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = fontWeight(FontWeight.Medium),
            fontSize = 16.sp,
            lineHeight = lineHeight(24.0).sp,
            letterSpacing = letterSpacing(0.15).sp
        ),
        titleSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = fontWeight(FontWeight.Medium),
            fontSize = 14.sp,
            lineHeight = lineHeight(20.0).sp,
            letterSpacing = letterSpacing(0.1).sp
        ),
        bodyLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = fontWeight(FontWeight.Normal),
            fontSize = 16.sp,
            lineHeight = lineHeight(24.0).sp,
            letterSpacing = letterSpacing(0.5).sp
        ),
        bodyMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = fontWeight(FontWeight.Normal),
            fontSize = 14.sp,
            lineHeight = lineHeight(20.0).sp,
            letterSpacing = letterSpacing(0.25).sp
        ),
        bodySmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = fontWeight(FontWeight.Normal),
            fontSize = 12.sp,
            lineHeight = lineHeight(16.0).sp,
            letterSpacing = letterSpacing(0.4).sp
        ),
        labelLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = fontWeight(FontWeight.Medium),
            fontSize = 14.sp,
            lineHeight = lineHeight(20.0).sp,
            letterSpacing = letterSpacing(0.1).sp
        ),
        labelMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = fontWeight(FontWeight.Medium),
            fontSize = 12.sp,
            lineHeight = lineHeight(16.0).sp,
            letterSpacing = letterSpacing(0.5).sp
        ),
        labelSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = fontWeight(FontWeight.Medium),
            fontSize = 11.sp,
            lineHeight = lineHeight(16.0).sp,
            letterSpacing = letterSpacing(0.5).sp
        )
    )
}

val SciFiTypography = createTypography(OrbitronFontFamily)

val CyberpunkTypography = createTypography(OrbitronFontFamily, isCyberpunkTitle = true)

val MedievalTypography = createTypography(MedievalFontFamily, lineHeightFactor = 2.0f) // +2sp

val HorrorTypography = createTypography(HorrorFontFamily, letterSpacingFactor = 0.2f) // +0.2sp

val MatrixTypography = createTypography(MatrixFontFamily, isHacker = true)

val PrideTypography = DefaultTypography
