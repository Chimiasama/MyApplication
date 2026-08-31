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

// Token único de "ênfase" (rótulo em negrito dentro de um item — custo, estágio, nome
// selecionado etc.). Antes disso cada tela recriava a mesma coisa na mão, quase sempre como
// `MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)` — encontrado ~91
// vezes no app, cada recriação livre pra divergir em tamanho/peso. Usar
// `MaterialTheme.typography.emphasis` em vez disso.
val Typography.emphasis: TextStyle
    get() = titleMedium.copy(fontWeight = FontWeight.SemiBold)

// Helper to create Typography
// SEPARAÇÃO HIERÁRQUICA: headingFontFamily para Títulos, bodyFontFamily para Leitura.
private fun createTypography(
    headingFontFamily: FontFamily,
    bodyFontFamily: FontFamily = FontFamily.Default,
    letterSpacingAdd: Double = 0.0,
    lineHeightAdd: Double = 0.0,
    headingFontWeight: FontWeight = FontWeight.Normal // Allow forcing Bold for headers
): Typography {
    val default = Typography()
    return Typography(
        // TITLES / DISPLAY (Decorativo)
        displayLarge = default.displayLarge.copy(fontFamily = headingFontFamily, fontWeight = headingFontWeight, letterSpacing = default.displayLarge.letterSpacing.value.plus(letterSpacingAdd).sp, lineHeight = default.displayLarge.lineHeight.value.plus(lineHeightAdd).sp),
        displayMedium = default.displayMedium.copy(fontFamily = headingFontFamily, fontWeight = headingFontWeight, letterSpacing = default.displayMedium.letterSpacing.value.plus(letterSpacingAdd).sp, lineHeight = default.displayMedium.lineHeight.value.plus(lineHeightAdd).sp),
        displaySmall = default.displaySmall.copy(fontFamily = headingFontFamily, fontWeight = headingFontWeight, letterSpacing = default.displaySmall.letterSpacing.value.plus(letterSpacingAdd).sp, lineHeight = default.displaySmall.lineHeight.value.plus(lineHeightAdd).sp),
        headlineLarge = default.headlineLarge.copy(fontFamily = headingFontFamily, fontWeight = headingFontWeight, letterSpacing = default.headlineLarge.letterSpacing.value.plus(letterSpacingAdd).sp, lineHeight = default.headlineLarge.lineHeight.value.plus(lineHeightAdd).sp),
        headlineMedium = default.headlineMedium.copy(fontFamily = headingFontFamily, fontWeight = headingFontWeight, letterSpacing = default.headlineMedium.letterSpacing.value.plus(letterSpacingAdd).sp, lineHeight = default.headlineMedium.lineHeight.value.plus(lineHeightAdd).sp),
        headlineSmall = default.headlineSmall.copy(fontFamily = headingFontFamily, fontWeight = headingFontWeight, letterSpacing = default.headlineSmall.letterSpacing.value.plus(letterSpacingAdd).sp, lineHeight = default.headlineSmall.lineHeight.value.plus(lineHeightAdd).sp),
        titleLarge = default.titleLarge.copy(fontFamily = headingFontFamily, fontWeight = headingFontWeight, letterSpacing = default.titleLarge.letterSpacing.value.plus(letterSpacingAdd).sp, lineHeight = default.titleLarge.lineHeight.value.plus(lineHeightAdd).sp),
        titleMedium = default.titleMedium.copy(fontFamily = headingFontFamily, fontWeight = headingFontWeight, letterSpacing = default.titleMedium.letterSpacing.value.plus(letterSpacingAdd).sp, lineHeight = default.titleMedium.lineHeight.value.plus(lineHeightAdd).sp),
        titleSmall = default.titleSmall.copy(fontFamily = headingFontFamily, fontWeight = headingFontWeight, letterSpacing = default.titleSmall.letterSpacing.value.plus(letterSpacingAdd).sp, lineHeight = default.titleSmall.lineHeight.value.plus(lineHeightAdd).sp),

        // BODY / LABEL (Legível - Default, Serif ou Mono)
        bodyLarge = default.bodyLarge.copy(fontFamily = bodyFontFamily, letterSpacing = default.bodyLarge.letterSpacing.value.plus(letterSpacingAdd).sp, lineHeight = default.bodyLarge.lineHeight.value.plus(lineHeightAdd).sp),
        bodyMedium = default.bodyMedium.copy(fontFamily = bodyFontFamily, letterSpacing = default.bodyMedium.letterSpacing.value.plus(letterSpacingAdd).sp, lineHeight = default.bodyMedium.lineHeight.value.plus(lineHeightAdd).sp),
        bodySmall = default.bodySmall.copy(fontFamily = bodyFontFamily, letterSpacing = default.bodySmall.letterSpacing.value.plus(letterSpacingAdd).sp, lineHeight = default.bodySmall.lineHeight.value.plus(lineHeightAdd).sp),
        labelLarge = default.labelLarge.copy(fontFamily = bodyFontFamily, letterSpacing = default.labelLarge.letterSpacing.value.plus(letterSpacingAdd).sp, lineHeight = default.labelLarge.lineHeight.value.plus(lineHeightAdd).sp),
        labelMedium = default.labelMedium.copy(fontFamily = bodyFontFamily, letterSpacing = default.labelMedium.letterSpacing.value.plus(letterSpacingAdd).sp, lineHeight = default.labelMedium.lineHeight.value.plus(lineHeightAdd).sp),
        labelSmall = default.labelSmall.copy(fontFamily = bodyFontFamily, letterSpacing = default.labelSmall.letterSpacing.value.plus(letterSpacingAdd).sp, lineHeight = default.labelSmall.lineHeight.value.plus(lineHeightAdd).sp)
    )
}

val DefaultTypography = Typography(
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.15.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.5.sp
    )
)

// SCIFI / CYBERPUNK: Headers = Orbitron, Body = Monospace
val SciFiTypography = createTypography(
    headingFontFamily = OrbitronFamily,
    bodyFontFamily = FontFamily.Monospace,
    letterSpacingAdd = 0.1
)
val CyberpunkTypography = createTypography(
    headingFontFamily = OrbitronFamily,
    bodyFontFamily = FontFamily.Monospace,
    letterSpacingAdd = 0.1
)

// MEDIEVAL: Headers = Medieval (BOLD forced), Body = Serif (Legible)
val MedievalTypography = createTypography(
    headingFontFamily = MedievalFamily,
    bodyFontFamily = FontFamily.Serif,
    lineHeightAdd = 2.0,
    headingFontWeight = FontWeight.Bold // Passo 1.3: Force Bold
)

// HORROR: Headers = Horror, Body = Serif
val HorrorTypography = createTypography(
    headingFontFamily = HorrorFamily,
    bodyFontFamily = FontFamily.Serif,
    letterSpacingAdd = 0.2
)

// WW2: Headers = Stencil, Body = SansSerif (Default)
val WW2Typography = createTypography(
    headingFontFamily = StencilFamily,
    bodyFontFamily = FontFamily.Default
)

// Minimalist Typography uses Default (SansSerif) but ensures a clean look
val MinimalistTypography = DefaultTypography
