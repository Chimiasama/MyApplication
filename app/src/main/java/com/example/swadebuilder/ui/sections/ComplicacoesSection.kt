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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
    onOpenComplicacoesDetail: () -> Unit
) {
    val locked = state.criacaoBasicaCongelada

    var expCompSection by rememberSaveable { mutableStateOf(false) }
    var expCompMenu    by rememberSaveable { mutableStateOf(false) }
    var showPcInUseDialog by rememberSaveable { mutableStateOf(false) }

    val autoBaseKeys = state.desvantagensAutomaticas
        .map { it.uppercase().semAcentos().substringBefore("(").trim() }
        .toSet()

    val showLista = booleanResource(com.example.swadebuilder.R.bool.show_lista_completa)

    val complicacoesFiltradas = if (state.modoSuperComplicacoes) {
        listaComplicacoes
    } else {
        listaComplicacoes.filter { it.origem.equals("BASICO", ignoreCase = true) }
    }

    SectionCard(
        title    = "Complicações",
        expanded = expCompSection,
        onToggle = { expCompSection = !expCompSection },
        icon     = Icons.Default.Warning
    ) {
        val totalPc = state.pontosComplicacao
        val usadosPc = state.pontosComplicacaoGastos
        val livresPc = (totalPc - usadosPc).coerceAtLeast(0)

        SectionHeader(
            onHelpClick          = null,
            centerText           = "Pontos Bônus: livres $livresPc / $totalPc",
            onCenterClick        = null,
            onListaCompletaClick = if (showLista) onOpenComplicacoesDetail else null,
            listaCompletaText    = "Lista Completa"
        )

        Spacer(Modifier.height(4.dp))

        if (totalPc == 0) {
            Text(
                "Escolha Complicações abaixo para ganhar Pontos Bônus de Criação.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Text(
                "Use seus Pontos Bônus de Criação nas seções de Atributos, Perícias, Vantagens ou Equipamento.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(8.dp))

        // 4. Chips das complicações já selecionadas
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
                                    // Não pode remover porque os Pontos Bônus já foram gastos
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

            // 5. Dropdown para escolher novas complicações
            ExposedDropdownMenuBox(
                expanded         = expCompMenu,
                onExpandedChange = { if (!locked) expCompMenu = !expCompMenu },
                modifier         = Modifier.alpha(if (locked) .3f else 1f)
            ) {
                TransparentOutlinedReadOnlyField(
                    text     = if (state.complicacoesSelecionadas.values.any { it != null })
                        "Selecionadas" else "Escolher…",
                    enabled  = !locked,
                    onClick  = { expCompMenu = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )

                ExposedDropdownMenu(
                    expanded         = expCompMenu,
                    onDismissRequest = { expCompMenu = false },
                    modifier         = Modifier.heightIn(max = 300.dp)
                ) {
                    // usa a lista filtrada para achar "pequeno"
                    val pequComp = complicacoesFiltradas.first { it.id == "pequeno" }

                    // Filtra para remover automáticas e só traz “SUPER” se ativo
                    complicacoesFiltradas
                        .filter { comp ->
                            comp.id.keyify() !in autoBaseKeys &&
                                    (state.modoSuperComplicacoes || !comp.origem.equals("SUPER", ignoreCase = true))
                        }
                        .forEach { comp ->
                            val cur    = state.complicacoesSelecionadas[comp]
                            val sevRaw = comp.severity.lowercase().trim()
                            val menorOnly = sevRaw.contains("menor") && !sevRaw.contains("maior")
                            val maiorOnly = sevRaw.contains("maior") && !sevRaw.contains("menor")
                            val ambos     = sevRaw.contains("menor") && sevRaw.contains("maior")

                            when {
                                maiorOnly -> {
                                    if (cur == null) {
                                        DropdownMenuItem(
                                            text = { Text("${comp.name} (Maior)") },
                                            onClick = {
                                                if (locked) return@DropdownMenuItem
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
                                            enabled = !locked
                                        )
                                    }
                                }
                                menorOnly -> {
                                    if (cur == null) {
                                        DropdownMenuItem(
                                            text = { Text("${comp.name} (Menor)") },
                                            onClick = {
                                                if (locked) return@DropdownMenuItem
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
                                            enabled = !locked
                                        )
                                    }
                                }
                                ambos -> {
                                    if (cur == null) {
                                        DropdownMenuItem(
                                            text = { Text("${comp.name} (Menor)") },
                                            onClick = {
                                                if (locked) return@DropdownMenuItem
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
                                            enabled = !locked
                                        )
                                    }
                                    if (cur == "Menor") {
                                        DropdownMenuItem(
                                            text = { Text("${comp.name} (Maior)") },
                                            onClick = {
                                                if (locked) return@DropdownMenuItem
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
                                            enabled = !locked
                                        )
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
        onValueChange = {},  // read‐only
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
                inner()  // texto
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

