package com.example.swadebuilder.ui.sections

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.R
import com.example.swadebuilder.criacaoBasicaCongelada
import com.example.swadebuilder.listaAtributos
import com.example.swadebuilder.mapaAtributosDescricao
import com.example.swadebuilder.mapaAtributosDisplay
import com.example.swadebuilder.toDiceString
import com.example.swadebuilder.ui.components.PbWalletBanner
import com.example.swadebuilder.ui.components.ResourceControlRow
import com.example.swadebuilder.ui.components.SectionCard
import com.example.swadebuilder.ui.components.SectionHeader
import com.example.swadebuilder.util.semAcentos

@OptIn(ExperimentalTextApi::class)
@Composable
fun AtributosSection(
    state: CriadorState,
    onUserFeedback: () -> Unit
) {
    LocalContext.current
    val allowLongTexts = booleanResource(R.bool.enable_long_texts)
    val usePbWalletRedesign = booleanResource(R.bool.enable_pb_wallet_redesign)
    val detalhesExpandidos = remember { mutableStateMapOf<String, Boolean>() }

    listaAtributos

    val locked = state.criacaoBasicaCongelada && !state.attributeAdvancementInProgress

    val pergaminho = MaterialTheme.colorScheme.surfaceVariant
    val onSurface = MaterialTheme.colorScheme.onSurface

    val pcTotal  = state.pontosComplicacao
    val pcGastos = state.pontosComplicacaoGastos
    val pcLivres = (pcTotal - pcGastos).coerceAtLeast(0)
    val paUsados = state.cpPaStack.size

    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val measureStyle = MaterialTheme.typography.bodyLarge

    val valorColWidthDp = androidx.compose.runtime.remember(state.superInvestments, measureStyle) {
        val samples = listaAtributos.map { nome ->
            state.atributoRawComSupers(nome).toDiceString()
        }
        val maxPx = samples.maxOf { s ->
            textMeasurer.measure(text = s, style = measureStyle).size.width
        }
        with(density) { (maxPx + 100).toDp() }
    }

    SectionCard(
        title = "Atributos",
        icon = Icons.Default.FitnessCenter,
        showHeader = false
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(pergaminho, shape = RoundedCornerShape(12.dp))
                .verticalScroll(rememberScrollState())
                .padding(12.dp)
        ) {
            SectionHeader(
                onHelpClick = null,
                centerText = "Pontos de Atributo: ${state.pontosAtributo}",
                onListaCompletaClick = null,
                listaCompletaText = ""
            )

            Spacer(Modifier.height(4.dp))

            if (!state.emProgresso) {
                if (usePbWalletRedesign) {
                    PbWalletBanner(
                        pcTotal = pcTotal,
                        pcLivres = pcLivres,
                        spendLabel = "Usar PB em Atributos",
                        refundLabel = "Desfazer uso de PB",
                        spendEnabled = !locked && pcLivres >= 2,
                        refundEnabled = !locked && paUsados > 0,
                        onSpend = { state.gastarPcParaAtributo() },
                        onRefund = {
                            state.cpPaStack.removeAt(state.cpPaStack.lastIndex)
                            state.pontosComplicacaoGastos =
                                (state.pontosComplicacaoGastos - 2).coerceAtLeast(0)
                            state.recalcularPontosAtributo()
                        }
                    )
                } else {
                    ResourceControlRow(
                        labelAdd = "Usar PB em Atributos",
                        labelRemove = "Desfazer uso de PB",
                        canAdd = !locked && pcLivres >= 2,
                        canRemove = !locked && paUsados > 0,
                        onAdd = { state.gastarPcParaAtributo() },
                        onRemove = {
                            state.cpPaStack.removeAt(state.cpPaStack.lastIndex)
                            state.pontosComplicacaoGastos =
                                (state.pontosComplicacaoGastos - 2).coerceAtLeast(0)
                            state.recalcularPontosAtributo()
                        }
                    )
                }

                Spacer(Modifier.height(8.dp))
            }

            listaAtributos.forEach { nome ->
                val baseRaw = state.valoresAtributos[nome]!!.intValue

                val minReq = maxOf(
                    state.atributoMinRaw(nome),
                    state.minAttrPorVantagem[nome] ?: 4
                )

                val maxRaw  = state.atributoMaxRaw(nome)
                val stack   = state.paCostStackPorAtributo.getValue(nome)

                val efetivoRaw = state.atributoRawComSupers(nome)

                val nextRaw = if (baseRaw < 12) baseRaw + 2 else baseRaw + 1
                val prevRaw = if (baseRaw <= 12) baseRaw - 2 else baseRaw - 1

                val allowedByRule = !state.isAttributeRankLimitReached() || state.isAttributeFreeForMonster(nome)
                val canIncrease = !locked && state.pontosAtributo > 0 && (nextRaw <= maxRaw) && allowedByRule

                val canReduce = run {
                    val baseCanReduce = !locked && stack.isNotEmpty() && (prevRaw >= minReq)
                    if (!baseCanReduce) {
                        false
                    } else {
                        if (state.attributeAdvancementInProgress) {
                            // Durante o avanço, só pode reduzir se a pilha atual for maior
                            // do que era ANTES de começar a gastar o ponto.
                            val beforeSize = state.attributeStacksBeforeAdvancement?.get(nome) ?: 0
                            stack.size > beforeSize
                        } else {
                            // Comportamento normal fora do avanço
                            true
                        }
                    }
                }


                val displayName = mapaAtributosDisplay[nome] ?: nome
                val descKey = displayName.uppercase().semAcentos()
                val descricao = mapaAtributosDescricao[descKey].orEmpty()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = displayName,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        IconButton(
                            onClick = {
                                if (prevRaw < minReq) return@IconButton
                                stack.removeAt(stack.lastIndex)
                                state.valoresAtributos[nome]!!.intValue = prevRaw
                                state.pontosAtributo++
                                state.recalcularPontosAtributo()
                                onUserFeedback()
                            },
                            enabled = canReduce,
                            modifier = Modifier
                                .size(32.dp)
                                .padding(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Remove,
                                contentDescription = "Diminuir ${mapaAtributosDisplay[nome]}",
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Text(
                            text = efetivoRaw.toDiceString(),
                            modifier = Modifier.width(valorColWidthDp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Clip,
                            textAlign = TextAlign.Center
                        )

                        IconButton(
                            onClick = {
                                if (nextRaw > maxRaw || state.pontosAtributo <= 0) return@IconButton
                                stack.add(1)
                                state.valoresAtributos[nome]!!.intValue = nextRaw
                                state.pontosAtributo--
                                state.recalcularPontosAtributo()
                                onUserFeedback()
                            },
                            enabled = canIncrease,
                            modifier = Modifier
                                .size(32.dp)
                                .padding(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Aumentar ${mapaAtributosDisplay[nome]}",
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(Modifier.width(4.dp))
                    }

                    if (allowLongTexts && descricao.isNotBlank()) {
                        Spacer(Modifier.height(2.dp))
                        TextButton(
                            onClick = {
                                val current = detalhesExpandidos[descKey] ?: false
                                detalhesExpandidos[descKey] = !current
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                if (detalhesExpandidos[descKey] == true) "Ocultar detalhes" else "Ver detalhes",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        AnimatedVisibility(visible = detalhesExpandidos[descKey] == true) {
                            Text(
                                text = descricao,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
