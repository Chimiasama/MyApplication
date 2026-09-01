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
import com.example.swadebuilder.toDiceString
import com.example.swadebuilder.ui.components.SectionHeader
import com.example.swadebuilder.calcularPericiaRules
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.semAcentos
import com.example.swadebuilder.util.toFancyTitleCase

@OptIn(ExperimentalTextApi::class)
@Composable
fun AtributosContent(
    state: CriadorState,
    listaAtributos: List<String>,
    mapaAtributosDisplay: Map<String, String>,
    mapaAtributosDescricao: Map<String, String>,
    onUserFeedback: () -> Unit
) {
    LocalContext.current
    val allowLongTexts = booleanResource(R.bool.enable_long_texts)
    val detalhesExpandidos = remember { mutableStateMapOf<String, Boolean>() }

    val locked = state.criacaoBasicaCongelada && !state.attributeAdvancementInProgress

    val pergaminho = MaterialTheme.colorScheme.surfaceVariant
    val onSurface = MaterialTheme.colorScheme.onSurface

    val pcTotal  = state.pontosComplicacao
    val pcGastos = state.pontosComplicacaoGastos
    val pcLivres = (pcTotal - pcGastos).coerceAtLeast(0)

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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(pergaminho, shape = RoundedCornerShape(12.dp))
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        // Updated Header to show both Attribute and Skill Points
        if (!state.modoLivre) {
            SectionHeader(
                onHelpClick = null,
                centerText = "Atributos: ${state.pontosAtributo}${if (!locked && pcLivres >= 2) " (+${pcLivres / 2} via PB)" else ""}  |  Perícias: ${state.pontosPericia}${if (!locked && pcLivres >= 1) " (+${pcLivres} via PB)" else ""}",
                onListaCompletaClick = null,
                listaCompletaText = ""
            )
        } else {
            Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.height(4.dp))

        listaAtributos.forEach { nome ->
            val baseRaw = state.valoresAtributos[nome]!!.intValue

            val minReq = maxOf(
                state.atributoMinRaw(nome),
                state.minAttrPorVantagem[nome] ?: 4
            )

            val maxRaw  = state.atributoMaxRawNaCriacao(nome)
            val stack   = state.paCostStackPorAtributo.getValue(nome)

            val efetivoRaw = state.atributoRawComSupers(nome)

            val nextRaw = if (baseRaw < 12) baseRaw + 2 else baseRaw + 1
            val prevRaw = if (baseRaw <= 12) baseRaw - 2 else baseRaw - 1

            val allowedByRule = !state.isAttributeRankLimitReached() || state.isAttributeFreeForMonster(nome)

            // Updated logic: allow increase if points > 0 OR if we have enough BP to auto-buy
            val canIncrease = if (state.modoLivre) true else !locked && (state.pontosAtributo > 0 || pcLivres >= 2) && (nextRaw <= maxRaw) && allowedByRule

            val canReduce = run {
                val baseCanReduce = !locked && stack.isNotEmpty() && (prevRaw >= minReq)
                if (!baseCanReduce) {
                    false
                } else {
                    if (state.attributeAdvancementInProgress) {
                        val beforeSize = state.attributeStacksBeforeAdvancement?.get(nome) ?: 0
                        stack.size > beforeSize
                    } else {
                        true
                    }
                }
            }


            val rawDisplayName = mapaAtributosDisplay[nome] ?: nome
            val displayName = rawDisplayName.toFancyTitleCase()
            val descKey = rawDisplayName.uppercase().semAcentos()
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

                            // Auto-Refund BP if we have a surplus and are using BP
                            // We need to loop because one click might free up a point that allows refunding MULTIPLE BPs if logic was different,
                            // but for Attributes 1 AP = 2 BP.
                            if (state.pontosAtributo > 0 && state.cpPaStack.isNotEmpty()) {
                                state.devolverPcDeAtributo()
                            }

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
                            if (!state.modoLivre && nextRaw > maxRaw) return@IconButton

                            if (!state.modoLivre && state.pontosAtributo <= 0) {
                                // Auto-spend BP
                                if (!state.gastarPcParaAtributo()) return@IconButton
                            }

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

                val periciasDoAtributo = remember(state.listaPericias, nome) {
                    state.periciasComIdiomas().filter { per ->
                        per.atributo.keyify() == nome.keyify()
                    }.distinctBy { it.nome }
                }

                Spacer(Modifier.height(2.dp))
                TextButton(
                    onClick = {
                        val current = detalhesExpandidos[descKey] ?: false
                        detalhesExpandidos[descKey] = !current
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        if (detalhesExpandidos[descKey] == true) "Ocultar Perícias" else "Ver Perícias (${periciasDoAtributo.size})",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                AnimatedVisibility(visible = detalhesExpandidos[descKey] == true) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, start = 8.dp, end = 8.dp, bottom = 4.dp),
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp)
                    ) {
                        if (periciasDoAtributo.isEmpty()) {
                            Text(
                                text = "Nenhuma perícia vinculada a este atributo.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            periciasDoAtributo.forEach { per ->
                                val reg = state.calcularPericiaRules(per, state.idosoBonusSp > 0, locked)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = per.nome.toFancyTitleCase(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = {
                                            state.decreasePericia(per)
                                            onUserFeedback()
                                        },
                                        enabled = reg.canDecrease,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "Diminuir", modifier = Modifier.fillMaxSize())
                                    }
                                    Text(
                                        text = if (reg.displayRaw == 0) "-" else reg.displayRaw.toDiceString(),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.width(48.dp),
                                        textAlign = TextAlign.Center
                                    )
                                    IconButton(
                                        onClick = {
                                            if (!state.modoLivre && state.pontosPericia < reg.cost && pcLivres >= (reg.cost - state.pontosPericia)) {
                                                repeat(reg.cost - state.pontosPericia) { state.gastarPcParaPericia() }
                                            }
                                            state.increasePericiaFromAdvancement(per, reg.cost, null)
                                            onUserFeedback()
                                        },
                                        enabled = reg.canIncrease || (pcLivres >= reg.cost && reg.nextRaw <= reg.capRaw),
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Aumentar", modifier = Modifier.fillMaxSize())
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
