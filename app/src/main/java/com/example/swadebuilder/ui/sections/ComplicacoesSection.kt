@file:Suppress("DEPRECATION")

package com.example.swadebuilder.ui.sections

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.SectionCard
import com.example.swadebuilder.SectionHeader
import com.example.swadebuilder.criacaoBasicaCongelada
import com.example.swadebuilder.listaComplicacoes
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.semAcentos

@ExperimentalMaterial3Api
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun ComplicacoesSection(
    state: CriadorState,
    expanded: Boolean,
    onToggle: () -> Unit,
    onOpenComplicacoesDetail: () -> Unit
) {
    val locked = state.criacaoBasicaCongelada

    var showPcInUseDialog by rememberSaveable { mutableStateOf(false) }

    val autoBaseKeys = state.desvantagensAutomaticas
        .map { it.uppercase().semAcentos().substringBefore("(").trim() }
        .toSet()

    val showLista = booleanResource(com.example.swadebuilder.R.bool.show_lista_completa)

    // ✅ NOVO: origens ativas por enquanto:
    // BASICO sempre, SUPER só se modoSuperComplicacoes estiver ligado.
    // No futuro, basta adicionar flags e incluir novas origens aqui.
    val origensAtivas: Set<String> = buildSet {
        add("BASICO")
        if (state.modoSuperComplicacoes) add("SUPER")
        // Futuro:
        // if (state.modoHorrorComplicacoes) add("HORROR")
        // if (state.modoFantasiaComplicacoes) add("FANTASIA")
        // if (state.modoSciFiComplicacoes) add("SCIFI")
    }

    val complicacoesFiltradas = listaComplicacoes.filter { comp ->
        comp.origem.uppercase().semAcentos().trim() in origensAtivas
    }

    SectionCard(
        title    = "Complicações",
        expanded = expanded,
        onToggle = onToggle,
        icon     = Icons.Default.Warning
    ) {
        val totalPc = state.pontosComplicacao
        val usadosPc = state.pontosComplicacaoGastos
        val livresPc = (totalPc - usadosPc).coerceAtLeast(0)

        SectionHeader(
            onHelpClick          = null,
            centerText           = "Pontos Complicação: livres $livresPc / $totalPc",
            onCenterClick        = null,
            onListaCompletaClick = if (showLista) onOpenComplicacoesDetail else null,
            listaCompletaText    = "Lista Completa"
        )

        Spacer(Modifier.height(4.dp))

        if (totalPc == 0) {
            Text(
                "Escolha Complicações para ganhar Pontos Bônus de Complicação.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Text(
                "Use seus Pontos Bônus de Complicação nas seções de Atributos, Perícias, Vantagens ou Equipamento.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(8.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement   = Arrangement.spacedBy(8.dp),
            modifier = Modifier.alpha(if (locked) 0.3f else 1f)
        ) {
            state.complicacoesSelecionadas
                .filterValues { it != null }
                .forEach { (comp, tipo) ->
                    val isAuto = comp.id.keyify() in autoBaseKeys
                    val isYoungAuto = comp.id == "pequeno" && state.jovemAutoPequeno
                    val cost = if (tipo == "Maior") 2 else 1
                    val canRemove = !locked &&
                            !isAuto &&
                            !isYoungAuto &&
                            state.pontosComplicacaoGastos <= state.pontosComplicacao - cost

                    val isClickable = !locked && !isAuto && !isYoungAuto

                    AssistChip(
                        onClick = {
                            if (!isClickable) return@AssistChip

                            if (canRemove) {
                                when (comp.id) {
                                    "idoso" -> {
                                        state.complicacoesSelecionadas.remove(comp)
                                        state.idosoBonusSp = 0
                                        state.syncFromCPRefund(sp = true)
                                    }
                                    "jovem" -> {
                                        val pequComp = complicacoesFiltradas.first { it.id == "pequeno" }
                                        state.removeYoung(pequComp)
                                        state.complicacoesSelecionadas.remove(comp)
                                        state.applyYoungMinor()
                                    }
                                    "pobreza" -> {
                                        state.complicacoesSelecionadas.remove(comp)
                                        state.dinheiro += 250
                                    }
                                    "obeso" -> {
                                        state.complicacoesSelecionadas.remove(comp)
                                        state.obesoBonusSize = 0
                                        state.obesoMalusMov = 0
                                    }
                                    else -> {
                                        state.complicacoesSelecionadas.remove(comp)
                                    }
                                }
                            } else {
                                showPcInUseDialog = true
                            }
                        },
                        enabled     = isClickable,
                        label       = { Text("${comp.name} ($tipo)") },
                        leadingIcon = { Icon(Icons.Default.Close, contentDescription = null) }
                    )
                }
        }

        Spacer(Modifier.height(8.dp))

        if (showPcInUseDialog) {
            val paCount  = state.cpPaStack.size
            val spCount  = state.cpSpStack.size
            val pvCount  = state.cpPvStack.size
            val recCount = state.cpRecursosStack.size
            val totalUsados = paCount + spCount + pvCount + recCount

            AlertDialog(
                onDismissRequest = { showPcInUseDialog = false },
                confirmButton = {
                    TextButton(onClick = { showPcInUseDialog = false }) {
                        Text("OK")
                    }
                },
                title = { Text("Não é possível remover esta Complicação") },
                text = {
                    Column {
                        Text("Você já converteu Pontos Bônus de Criação vindos de Complicações em:")
                        Spacer(Modifier.height(8.dp))

                        if (totalUsados == 0) {
                            Text("Nenhum Ponto Bônus está em uso no momento.")
                        } else {
                            if (paCount > 0) Text("• $paCount em Atributos adicionais")
                            if (spCount > 0) Text("• $spCount em passos extras de Perícia")
                            if (pvCount > 0) Text("• $pvCount em Vantagens adicionais")
                            if (recCount > 0) Text("• $recCount em Recursos extras (500$ cada)")

                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Para remover esta Complicação, primeiro desfaça alguns " +
                                        "desses Pontos Bônus nas seções de Atributos, Perícias, " +
                                        "Vantagens ou Equipamento.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            )
        }

        Spacer(Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 260.dp)
                .alpha(if (locked) 0.3f else 1f)
        ) {
            Text(
                "Lista de complicações disponíveis:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(4.dp))

            val pequComp = complicacoesFiltradas.first { it.id == "pequeno" }

            val listaParaMostrar = complicacoesFiltradas
                .filter { comp ->
                    comp.id.keyify() !in autoBaseKeys
                }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(listaParaMostrar) { comp ->
                    val cur    = state.complicacoesSelecionadas[comp]
                    val sevRaw = comp.severity.lowercase().trim()
                    val menorOnly = sevRaw.contains("menor") && !sevRaw.contains("maior")
                    val maiorOnly = sevRaw.contains("maior") && !sevRaw.contains("menor")
                    val ambos     = sevRaw.contains("menor") && sevRaw.contains("maior")

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = comp.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(Modifier.width(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (menorOnly || ambos) {
                                val enabledMenor = !locked && cur == null
                                TextButton(
                                    onClick = {
                                        if (!enabledMenor) return@TextButton
                                        when (comp.id) {
                                            "jovem" -> {
                                                state.complicacoesSelecionadas[comp] = "Menor"
                                                state.applyYoungMinor()
                                            }
                                            "obeso" -> {
                                                state.complicacoesSelecionadas[comp] = "Menor"
                                                state.obesoBonusSize = 1
                                                state.obesoMalusMov = 1
                                            }
                                            else -> {
                                                state.complicacoesSelecionadas[comp] = "Menor"
                                            }
                                        }
                                    },
                                    enabled = enabledMenor
                                ) {
                                    Text("Menor")
                                }
                            }

                            if (maiorOnly || ambos) {
                                val enabledMaior = !locked && (
                                        (maiorOnly && cur == null) ||
                                                (ambos && cur == "Menor")
                                        )
                                TextButton(
                                    onClick = {
                                        if (!enabledMaior) return@TextButton
                                        when (comp.id) {
                                            "idoso" -> {
                                                state.complicacoesSelecionadas[comp] = "Maior"
                                                state.idosoBonusSp = 5
                                                state.rebuildAllPericiaStacks()
                                            }
                                            "jovem" -> {
                                                state.complicacoesSelecionadas[comp] = "Maior"
                                                state.applyYoungMajor(pequComp)
                                            }
                                            "obeso" -> {
                                                state.complicacoesSelecionadas[comp] = "Maior"
                                                state.obesoBonusSize = 1
                                                state.obesoMalusMov = 1
                                            }
                                            else -> {
                                                state.complicacoesSelecionadas[comp] = "Maior"
                                            }
                                        }
                                    },
                                    enabled = enabledMaior
                                ) {
                                    Text("Maior")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransparentOutlinedReadOnlyField(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme

    BasicTextField(
        value         = text,
        onValueChange = {},
        enabled       = enabled,
        readOnly      = true,
        textStyle     = LocalTextStyle.current.copy(color = colors.onSurface),
        singleLine    = true,
        modifier      = modifier,
        decorationBox = { inner ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color.Transparent, shape = MaterialTheme.shapes.small)
                    .border(
                        width = 1.dp,
                        color = if (enabled)
                            colors.outline.copy(alpha = 0.8f)
                        else
                            colors.outline.copy(alpha = 0.5f),
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clickable(enabled = enabled, onClick = onClick)
            ) {
                inner()
                Spacer(Modifier.weight(1f))
                Icon(
                    imageVector        = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = if (enabled)
                        colors.onSurface
                    else
                        colors.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    )
}
