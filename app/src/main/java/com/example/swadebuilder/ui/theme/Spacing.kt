package com.example.swadebuilder.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Escala de espaçamento (gaps/padding) do app. Antes disso cada tela escolhia seu próprio
 * valor de Spacer/padding ad-hoc (6dp, 10dp, 14dp, 18dp...), sem relação nenhuma entre telas.
 * Usar só estes seis valores dá um ritmo vertical/horizontal consistente. Não cobre tamanho
 * de ícone, largura de borda ou raio de canto — esses continuam específicos de cada componente.
 */
object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
}
