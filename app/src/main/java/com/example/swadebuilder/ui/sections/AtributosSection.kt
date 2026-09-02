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
import androidx.compose.foundation.clickable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.R
import com.example.swadebuilder.toDiceString
import com.example.swadebuilder.ui.components.SectionHeader
import com.example.swadebuilder.atributoBaseParaPericia
import com.example.swadebuilder.calcularPericiaRules
import com.example.swadebuilder.model.Pericia
import com.example.swadebuilder.util.AppPreferences
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.semAcentos
import com.example.swadebuilder.util.toFancyTitleCase


@Composable
fun AttributeCarouselPopoverDialog(
    attrName: String,
    minRaw: Int,
    maxRaw: Int,
    currentRaw: Int,
    availablePa: Int? = null,
    onSelectRaw: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val steps = remember(minRaw, maxRaw) {
        val list = mutableListOf<Int>()
        var v = maxOf(4, minRaw)
        while (v <= minOf(12, maxRaw)) {
            list.add(v)
            v += 2
        }
        list
    }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(attrName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
                Text(
                    "Selecione o dado do atributo (cada passo custa 1 Ponto de Atributo ou 2 PB):",
                    style = MaterialTheme.typography.bodySmall
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    steps.forEach { targetRaw ->
                        val cost = (targetRaw - minRaw) / 2
                        val stepsFromCurrent = if (targetRaw > currentRaw) (targetRaw - currentRaw) / 2 else 0
                        val canAfford = availablePa == null || targetRaw <= currentRaw || stepsFromCurrent <= availablePa

                        val isSelected = targetRaw == currentRaw
                        val containerColor = when {
                            !canAfford -> MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.4f)
                            isSelected -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.surfaceContainerHigh
                        }

                        androidx.compose.material3.OutlinedCard(
                            onClick = {
                                if (canAfford) {
                                    onSelectRaw(targetRaw)
                                    onDismiss()
                                }
                            },
                            enabled = canAfford,
                            modifier = Modifier.weight(1f),
                            colors = androidx.compose.material3.CardDefaults.outlinedCardColors(
                                containerColor = containerColor,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.3f)
                            ),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else androidx.compose.material3.CardDefaults.outlinedCardBorder()
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                            ) {
                                Text(
                                    text = targetRaw.toDiceString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (canAfford) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                )
                                Text(
                                    text = if (cost == 0) "base" else if (cost == 1) "1 pt" else "$cost pts",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Fechar") }
        }
    )
}

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

    val showSteppers = state.modoSelecaoPericia != AppPreferences.ModoSelecaoPericia.CARROSSEL_POPOVER

    var attributePopoverTarget by remember { mutableStateOf<String?>(null) }
    var skillPopoverTarget by remember { mutableStateOf<Pericia?>(null) }

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

                    if (showSteppers) {
                        IconButton(
                            onClick = {
                                if (prevRaw < minReq) return@IconButton
                                stack.removeAt(stack.lastIndex)
                                state.valoresAtributos[nome]!!.intValue = prevRaw
                                state.pontosAtributo++
                                state.recalcularPontosAtributo()

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
                    }

                    Text(
                        text = efetivoRaw.toDiceString(),
                        modifier = Modifier
                            .width(valorColWidthDp)
                            .clickable {
                                attributePopoverTarget = nome
                            },
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
                        textAlign = TextAlign.Center
                    )

                    if (showSteppers) {
                        IconButton(
                            onClick = {
                                if (!state.modoLivre && nextRaw > maxRaw) return@IconButton

                                if (!state.modoLivre && state.pontosAtributo <= 0) {
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
                    }

                    Spacer(Modifier.width(4.dp))
                }

                val periciasDoAtributo = remember(state.listaPericias, state.vantagensSelecionadas.size, nome) {
                    state.periciasComIdiomas().filter { per ->
                        state.atributoBaseParaPericia(per).keyify() == nome.keyify()
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
                                    val isBasica = state.isPericiaBasicaEfetiva(per)
                                    Text(
                                        text = buildAnnotatedString {
                                            val displayName = per.nome.toFancyTitleCase()
                                            if (isBasica) {
                                                withStyle(
                                                    SpanStyle(
                                                        color = Color.Red,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                ) {
                                                    append("✯ $displayName")
                                                }
                                            } else {
                                                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                                    append(displayName)
                                                }
                                            }
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (showSteppers) {
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
                                    }
                                    Text(
                                        text = when {
                                            reg.displayRaw > 0 -> reg.displayRaw.toDiceString()
                                            isBasica -> "d4"
                                            else -> "-"
                                        },
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .width(48.dp)
                                            .clickable {
                                                skillPopoverTarget = per
                                            },
                                        textAlign = TextAlign.Center
                                    )
                                    if (showSteppers) {
                                        val canAffordWithBP = (state.pontosPericia + pcLivres) >= reg.cost
                                        IconButton(
                                            onClick = {
                                                if (!state.modoLivre && state.pontosPericia < reg.cost && pcLivres >= (reg.cost - state.pontosPericia)) {
                                                    repeat(reg.cost - state.pontosPericia) { state.gastarPcParaPericia() }
                                                }
                                                state.increasePericiaFromAdvancement(per, reg.cost, null)
                                                onUserFeedback()
                                            },
                                            enabled = reg.canIncrease || (canAffordWithBP && reg.nextRaw <= reg.capRaw),
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

    if (skillPopoverTarget != null) {
        val per = skillPopoverTarget!!
        val startRaw = state.periciaStartRaw(state.ancestralidade, per)
        val baseAttr = state.atributoBaseParaPericia(per)
        val attrName = state.mapaAtributosDisplay[baseAttr] ?: baseAttr
        val attrRaw = state.valoresAtributos[baseAttr]?.intValue ?: 4
        val currentRaw = state.rawTotal(per)
        val idosoActive = state.idosoBonusSp > 0

        SkillCarouselPopoverDialog(
            skillName = per.nome,
            startRaw = startRaw,
            attrName = attrName,
            attrRaw = attrRaw,
            currentRaw = currentRaw,
            availableSp = if (state.modoLivre) null else (state.pontosPericia + pcLivres),
            onSelectRaw = { targetRaw: Int ->
                if (targetRaw > currentRaw) {
                    val stepsToAdd = dieStepsCount(currentRaw, targetRaw)
                    repeat(stepsToAdd) {
                        val reg = state.calcularPericiaRules(per, idosoActive, locked)
                        if (!state.modoLivre && state.pontosPericia < reg.cost) {
                            val missing = reg.cost - state.pontosPericia
                            val currentPcLivres = (state.pontosComplicacao - state.pontosComplicacaoGastos).coerceAtLeast(0)
                            if (currentPcLivres >= missing) {
                                repeat(missing) { state.gastarPcParaPericia() }
                            } else {
                                return@repeat
                            }
                        }
                        state.increasePericiaFromAdvancement(per, reg.cost, null)
                    }
                } else if (targetRaw < currentRaw) {
                    val stepsToRemove = dieStepsCount(targetRaw, currentRaw)
                    repeat(stepsToRemove) {
                        state.decreasePericia(per)
                    }
                }
                onUserFeedback()
            },
            onDismiss = { skillPopoverTarget = null }
        )
    }

    if (attributePopoverTarget != null) {
        val nome = attributePopoverTarget!!
        val baseRaw = state.valoresAtributos[nome]!!.intValue
        val minReq = maxOf(state.atributoMinRaw(nome), state.minAttrPorVantagem[nome] ?: 4)
        val maxRaw = state.atributoMaxRawNaCriacao(nome)

        AttributeCarouselPopoverDialog(
            attrName = mapaAtributosDisplay[nome] ?: nome,
            minRaw = minReq,
            maxRaw = maxRaw,
            currentRaw = baseRaw,
            availablePa = if (state.modoLivre) null else (state.pontosAtributo + pcLivres / 2),
            onSelectRaw = { targetRaw ->
                val stack = state.paCostStackPorAtributo.getValue(nome)
                if (targetRaw > baseRaw) {
                    val stepsToAdd = (targetRaw - baseRaw) / 2
                    repeat(stepsToAdd) {
                        val currentBase = state.valoresAtributos[nome]!!.intValue
                        val nextR = if (currentBase < 12) currentBase + 2 else currentBase + 1
                        if (!state.modoLivre && state.pontosAtributo <= 0) {
                            if (!state.gastarPcParaAtributo()) return@repeat
                        }
                        stack.add(1)
                        state.valoresAtributos[nome]!!.intValue = nextR
                        state.pontosAtributo--
                        state.recalcularPontosAtributo()
                    }
                } else if (targetRaw < baseRaw) {
                    val stepsToRemove = (baseRaw - targetRaw) / 2
                    repeat(stepsToRemove) {
                        if (stack.isNotEmpty()) {
                            stack.removeAt(stack.lastIndex)
                            val currentBase = state.valoresAtributos[nome]!!.intValue
                            val prevR = if (currentBase <= 12) currentBase - 2 else currentBase - 1
                            state.valoresAtributos[nome]!!.intValue = prevR
                            state.pontosAtributo++
                            state.recalcularPontosAtributo()
                        }
                    }
                }
                onUserFeedback()
            },
            onDismiss = { attributePopoverTarget = null }
        )
    }
}
