package com.example.swadebuilder.ui.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Substitui um [Text] de uma linha só que, em colunas estreitas (ex.: valores de dado como
 * "d12+1", "d12+2"), quebraria em duas linhas ("d12+" numa linha e "1" na debaixo) em vez de
 * cortar. Em vez disso, encolhe a fonte em passos a cada recomposição até caber numa linha só
 * (ou até atingir [minFontSize]), sem nunca quebrar linha.
 */
@Composable
fun AutoSizeText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    minFontSize: TextUnit = 9.sp
) {
    val baseStyle = if (fontWeight != null) style.copy(fontWeight = fontWeight) else style
    var resolvedStyle by remember(text, baseStyle) { mutableStateOf(baseStyle) }

    Text(
        text = text,
        modifier = modifier,
        style = resolvedStyle,
        color = color,
        textAlign = textAlign,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Clip,
        onTextLayout = { result ->
            val currentSize = resolvedStyle.fontSize
            if (result.didOverflowWidth && currentSize > minFontSize) {
                val nextSize = (currentSize.value * 0.9f).sp
                resolvedStyle = resolvedStyle.copy(fontSize = if (nextSize < minFontSize) minFontSize else nextSize)
            }
        }
    )
}
