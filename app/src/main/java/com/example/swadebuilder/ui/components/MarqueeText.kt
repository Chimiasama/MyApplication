package com.example.swadebuilder.ui.components

import androidx.compose.foundation.basicMarquee
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow

/**
 * Substitui um [Text] de uma linha só que, em espaços estreitos, empilharia (ou cortaria mal).
 * Em vez de quebrar linha, o texto "anda" uma vez só revelando o restante e depois assenta
 * mostrando só o que cabe. TextOverflow.Clip (não Ellipsis) é obrigatório aqui: o marquee
 * precisa medir o texto inteiro pra saber quanto rolar — com Ellipsis o texto já chegaria
 * truncado e nada rolaria.
 */
@Composable
fun MarqueeText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified
) {
    Text(
        text = text,
        modifier = modifier.basicMarquee(iterations = 1),
        style = style,
        color = color,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Clip
    )
}
