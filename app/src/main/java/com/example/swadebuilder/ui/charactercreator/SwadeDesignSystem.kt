package com.example.swadebuilder.ui.charactercreator

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

@Immutable
data class SwadeDesignTokens(
    val panelShape: Shape,
    val buttonShape: Shape,
    val headerTextStyle: TextStyle,
    val bodyTextStyle: TextStyle
)

object SwadeDesignSystem {
    val tokens = SwadeDesignTokens(
        panelShape = RoundedCornerShape(12.dp),
        buttonShape = RoundedCornerShape(8.dp),
        headerTextStyle = TextStyle(fontFamily = FontFamily.Serif),
        bodyTextStyle = TextStyle(fontFamily = FontFamily.Serif)
    )

    @Composable
    fun panelBorder(): BorderStroke = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
}
