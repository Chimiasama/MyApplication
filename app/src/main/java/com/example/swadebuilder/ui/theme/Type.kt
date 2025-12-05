package com.example.swadebuilder.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

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

// W.I.P. - Using default font until .ttf files can be added.
val MedievalTypography = DefaultTypography /*Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily(androidx.compose.ui.text.font.Font(R.font.medieval_font)),
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)*/

// W.I.P. - Using default font until .ttf files can be added.
val MatrixTypography = DefaultTypography /*Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily(androidx.compose.ui.text.font.Font(R.font.matrix_font)),
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)*/

// W.I.P. - Using default font until .ttf files can be added.
val CyberpunkTypography = DefaultTypography /*Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily(androidx.compose.ui.text.font.Font(R.font.cyberpunk_font)),
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)*/

// W.I.P. - Using default font until .ttf files can be added.
val PrideTypography = DefaultTypography /*Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily(androidx.compose.ui.text.font.Font(R.font.pride_font)),
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)*/
