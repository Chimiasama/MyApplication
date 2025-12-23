package com.example.swadebuilder.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.swadebuilder.R

// Font Families
val OrbitronFamily = FontFamily(
    Font(R.font.orbitron_regular, FontWeight.Normal),
    Font(R.font.orbitron_medium, FontWeight.Medium),
    Font(R.font.orbitron_semi_bold, FontWeight.SemiBold),
    Font(R.font.orbitron_bold, FontWeight.Bold)
)

val MedievalFamily = FontFamily(
    Font(R.font.medieval_font)
)

val HorrorFamily = FontFamily(
    Font(R.font.horror_font)
)

val MatrixFamily = FontFamily(
    Font(R.font.matrix_font)
)

val StencilFamily = FontFamily.Monospace

// Helper to create Typography based on base settings
private fun createTypography(
    fontFamily: FontFamily,
    letterSpacingAdd: Double = 0.0,
    lineHeightAdd: Double = 0.0
): Typography {
    val default = Typography()
    return Typography(
        displayLarge = default.displayLarge.copy(fontFamily = fontFamily, letterSpacing = default.displayLarge.letterSpacing.value.plus(letterSpacingAdd).sp, lineHeight = default.displayLarge.lineHeight.value.plus(lineHeightAdd).sp),
        displayMedium = default.displayMedium.copy(fontFamily = fontFamily, letterSpacing = default.displayMedium.letterSpacing.value.plus(letterSpacingAdd).sp, lineHeight = default.displayMedium.lineHeight.value.plus(lineHeightAdd).sp),
        displaySmall = default.displaySmall.copy(fontFamily = fontFamily, letterSpacing = default.displaySmall.letterSpacing.value.plus(letterSpacingAdd).sp, lineHeight = default.displaySmall.lineHeight.value.plus(lineHeightAdd).sp),
        headlineLarge = default.headlineLarge.copy(fontFamily = fontFamily, letterSpacing = default.headlineLarge.letterSpacing.value.plus(letterSpacingAdd).sp, lineHeight = default.headlineLarge.lineHeight.value.plus(lineHeightAdd).sp),
        headlineMedium = default.headlineMedium.copy(fontFamily = fontFamily, letterSpacing = default.headlineMedium.letterSpacing.value.plus(letterSpacingAdd).sp, lineHeight = default.headlineMedium.lineHeight.value.plus(lineHeightAdd).sp),
        headlineSmall = default.headlineSmall.copy(fontFamily = fontFamily, letterSpacing = default.headlineSmall.letterSpacing.value.plus(letterSpacingAdd).sp, lineHeight = default.headlineSmall.lineHeight.value.plus(lineHeightAdd).sp),
        titleLarge = default.titleLarge.copy(fontFamily = fontFamily, letterSpacing = default.titleLarge.letterSpacing.value.plus(letterSpacingAdd).sp, lineHeight = default.titleLarge.lineHeight.value.plus(lineHeightAdd).sp),
        titleMedium = default.titleMedium.copy(fontFamily = fontFamily, letterSpacing = default.titleMedium.letterSpacing.value.plus(letterSpacingAdd).sp, lineHeight = default.titleMedium.lineHeight.value.plus(lineHeightAdd).sp),
        titleSmall = default.titleSmall.copy(fontFamily = fontFamily, letterSpacing = default.titleSmall.letterSpacing.value.plus(letterSpacingAdd).sp, lineHeight = default.titleSmall.lineHeight.value.plus(lineHeightAdd).sp),
        bodyLarge = default.bodyLarge.copy(fontFamily = fontFamily, letterSpacing = default.bodyLarge.letterSpacing.value.plus(letterSpacingAdd).sp, lineHeight = default.bodyLarge.lineHeight.value.plus(lineHeightAdd).sp),
        bodyMedium = default.bodyMedium.copy(fontFamily = fontFamily, letterSpacing = default.bodyMedium.letterSpacing.value.plus(letterSpacingAdd).sp, lineHeight = default.bodyMedium.lineHeight.value.plus(lineHeightAdd).sp),
        bodySmall = default.bodySmall.copy(fontFamily = fontFamily, letterSpacing = default.bodySmall.letterSpacing.value.plus(letterSpacingAdd).sp, lineHeight = default.bodySmall.lineHeight.value.plus(lineHeightAdd).sp),
        labelLarge = default.labelLarge.copy(fontFamily = fontFamily, letterSpacing = default.labelLarge.letterSpacing.value.plus(letterSpacingAdd).sp, lineHeight = default.labelLarge.lineHeight.value.plus(lineHeightAdd).sp),
        labelMedium = default.labelMedium.copy(fontFamily = fontFamily, letterSpacing = default.labelMedium.letterSpacing.value.plus(letterSpacingAdd).sp, lineHeight = default.labelMedium.lineHeight.value.plus(lineHeightAdd).sp),
        labelSmall = default.labelSmall.copy(fontFamily = fontFamily, letterSpacing = default.labelSmall.letterSpacing.value.plus(letterSpacingAdd).sp, lineHeight = default.labelSmall.lineHeight.value.plus(lineHeightAdd).sp)
    )
}

val DefaultTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)

val SciFiTypography = createTypography(OrbitronFamily, letterSpacingAdd = 0.1)
val CyberpunkTypography = createTypography(OrbitronFamily, letterSpacingAdd = 0.1)

val MedievalTypography = createTypography(MedievalFamily, lineHeightAdd = 2.0)

val HorrorTypography = createTypography(HorrorFamily, letterSpacingAdd = 0.2)

val WW2Typography = createTypography(StencilFamily)

val PrideTypography = Typography(
    displayLarge = DefaultTypography.displayLarge.copy(fontWeight = FontWeight.Medium),
    displayMedium = DefaultTypography.displayMedium.copy(fontWeight = FontWeight.Medium),
    displaySmall = DefaultTypography.displaySmall.copy(fontWeight = FontWeight.Medium),
    headlineLarge = DefaultTypography.headlineLarge.copy(fontWeight = FontWeight.Medium),
    headlineMedium = DefaultTypography.headlineMedium.copy(fontWeight = FontWeight.Medium),
    headlineSmall = DefaultTypography.headlineSmall.copy(fontWeight = FontWeight.Medium),
    titleLarge = DefaultTypography.titleLarge.copy(fontWeight = FontWeight.Medium),
    titleMedium = DefaultTypography.titleMedium.copy(fontWeight = FontWeight.Medium),
    titleSmall = DefaultTypography.titleSmall.copy(fontWeight = FontWeight.Medium),
    bodyLarge = DefaultTypography.bodyLarge.copy(fontWeight = FontWeight.Medium),
    bodyMedium = DefaultTypography.bodyMedium.copy(fontWeight = FontWeight.Medium),
    bodySmall = DefaultTypography.bodySmall.copy(fontWeight = FontWeight.Medium),
    labelLarge = DefaultTypography.labelLarge.copy(fontWeight = FontWeight.Medium),
    labelMedium = DefaultTypography.labelMedium.copy(fontWeight = FontWeight.Medium),
    labelSmall = DefaultTypography.labelSmall.copy(fontWeight = FontWeight.Medium)
)
