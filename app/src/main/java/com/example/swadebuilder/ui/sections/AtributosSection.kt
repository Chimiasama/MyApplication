package com.example.swadebuilder.ui.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.criacaoBasicaCongelada
import com.example.swadebuilder.ui.components.SectionHeader
import com.example.swadebuilder.listaAtributos
import com.example.swadebuilder.mapaAtributosDisplay
import com.example.swadebuilder.toDiceString

@OptIn(ExperimentalTextApi::class)
@Composable
fun AtributosContent(
    state: CriadorState,
    onOpenAtributosDetail: () -> Unit
) {
    // Agora travamos tudo com base na fase global
    val locked = state.criacaoBasicaCongelada

    // Usa a surface do tema
    val pergaminho = MaterialTheme.colorScheme.surfaceVariant

    val showLista = booleanResource(com.example.swadebuilder.R.bool.show_lista_completa)

    // ===== PONTOS BÔNUS (PC) =====
    val pcTotal  = state.pontosComplicacao
    val pcGastos = state.pontosComplicacaoGastos
    val pcLivres = (pcTotal - pcGastos).coerceAtLeast(0)
    val paUsados = state.cpPaStack.size
    // =============================

    // ===== LARGURA DINÂMICA PARA O VALOR (evitar esmagar "d12+4") =====
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val measureStyle = MaterialTheme.typography.bodyLarge

    val valorColWidthDp = androidx.compose.runtime.remember(state.superAtributoIncs, measureStyle) {
        val samples = listaAtributos.map { nome ->
            state.atributoRawComSupers(nome).toDiceString()
        }
        val maxPx = samples.maxOf { s ->
            textMeasurer.measure(text = s, style = measureStyle).size.width
        }
        // um pouco mais largo pra afastar os botões
        with(density) { (maxPx + 100).toDp() }
    }
    // ===================================================================

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(pergaminho, shape = RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        // cabeçalho
        SectionHeader(
            onHelpClick = null,
            centerText = "Pontos de Atributo: ${state.pontosAtributo} • Pontos Bônus: $pcLivres de $pcTotal",
            onListaCompletaClick = if (showLista) ({ onOpenAtributosDetail() }) else null,
            listaCompletaText = "Lista Completa"
        )

        Spacer(Modifier.height(4.dp))

        // Botões de usar / desfazer Pontos Bônus em Atributos
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val podeUsarPc = !locked && pcLivres >= 2
            val podeDesfazerPc = !locked && paUsados > 0

            TextButton(
                onClick = {
                    if (!podeUsarPc) return@TextButton

                    // Centraliza toda a lógica de gasto de PC em CriadorState
                    state.gastarPcParaAtributo()
                },
                enabled = podeUsarPc
            ) {
                Text("Usar PB em Atributos")
            }

            TextButton(
                onClick = {
                    if (!podeDesfazerPc) return@TextButton
                    state.cpPaStack.removeAt(state.cpPaStack.lastIndex)
                    state.pontosComplicacaoGastos =
                        (state.pontosComplicacaoGastos - 2).coerceAtLeast(0)
                    state.recalcularPontosAtributo()
                },
                enabled = podeDesfazerPc
            ) {
                Text("Desfazer uso de PB")
            }
        }

        if (pcTotal == 0) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Para ganhar Pontos Bônus, escolha Complicações na seção apropriada.",
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(Modifier.height(8.dp))

        // linhas dos atributos
        listaAtributos.forEach { nome ->
            val baseRaw = state.valoresAtributos[nome]!!.intValue

            // mínimo exigido (racial ou por vantagem)
            val minReq = maxOf(
                state.atributoMinRaw(nome),
                state.minAttrPorVantagem[nome] ?: 4
            )

            val maxRaw  = state.atributoMaxRaw(nome)
            val stack   = state.paCostStackPorAtributo.getValue(nome)

            // valor efetivo com supers (para exibição)
            val efetivoRaw = state.atributoRawComSupers(nome)

            // ===== NOVOS PASSOS: até d12 sobe de 2 em 2, depois de 1 em 1 =====
            val nextRaw = if (baseRaw < 12) baseRaw + 2 else baseRaw + 1
            val prevRaw = if (baseRaw <= 12) baseRaw - 2 else baseRaw - 1

            val canReduce   = !locked && stack.isNotEmpty() && (prevRaw >= minReq)
            val canIncrease = !locked && state.pontosAtributo > 0 && (nextRaw <= maxRaw)
            // =================================================================

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = mapaAtributosDisplay[nome] ?: nome,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // botão de diminuir
                IconButton(
                    onClick = {
                        if (prevRaw < minReq) return@IconButton
                        stack.removeAt(stack.lastIndex)
                        state.valoresAtributos[nome]!!.intValue = prevRaw
                        state.pontosAtributo++
                        state.recalcularPontosAtributo()
                    },
                    enabled = canReduce,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(4.dp)
                ) {
                    Icon(
                        Icons.Default.Remove,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // valor (com supers) – agora com mais espaço
                Text(
                    text = efetivoRaw.toDiceString(),
                    modifier = Modifier.width(valorColWidthDp),
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    textAlign = TextAlign.Center
                )

                // botão de aumentar
                IconButton(
                    onClick = {
                        if (nextRaw > maxRaw || state.pontosAtributo <= 0) return@IconButton
                        stack.add(1)
                        state.valoresAtributos[nome]!!.intValue = nextRaw
                        state.pontosAtributo--
                        state.recalcularPontosAtributo()
                    },
                    enabled = canIncrease,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(4.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}