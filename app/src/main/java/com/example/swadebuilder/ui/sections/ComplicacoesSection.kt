package com.example.swadebuilder.ui.sections

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CollapsibleSection
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.listaComplicacoes
import com.example.swadebuilder.model.Complicacao
import com.example.swadebuilder.ui.components.PbWalletBanner
import com.example.swadebuilder.ui.components.SectionHeader
import com.example.swadebuilder.util.keyify

data class CompFilter(
    val origens: Set<String> = emptySet(),
    val tipos: Set<String> = emptySet()
) {
    fun isEmpty() = origens.isEmpty() && tipos.isEmpty()
    fun totalSelections() = origens.size + tipos.size
}

@Composable
fun CompFilterDialog(
    allOrigens: List<String>,
    current: CompFilter,
    onChange: (CompFilter) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filtros de Complicações") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Origem", fontWeight = FontWeight.Bold)
                allOrigens.forEach { o ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = o in current.origens,
                            onCheckedChange = {
                                val s = current.origens.toMutableSet()
                                if (it) s += o else s -= o
                                onChange(current.copy(origens = s))
                            }
                        )
                        Spacer(Modifier.size(4.dp))
                        Text(o)
                    }
                }
                Spacer(Modifier.size(8.dp))

                Text("Gravidade", fontWeight = FontWeight.Bold)
                listOf("MENOR", "MAIOR").forEach { tipo ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = tipo in current.tipos,
                            onCheckedChange = {
                                val s = current.tipos.toMutableSet()
                                if (it) s += tipo else s -= tipo
                                onChange(current.copy(tipos = s))
                            }
                        )
                        Spacer(Modifier.size(4.dp))
                        Text(if (tipo == "MENOR") "Menor" else "Maior")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("OK") }
        }
    )
}

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun ComplicacoesSection(
    state: CriadorState,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    val pcTotal = state.pontosComplicacao
    val pcGastos = state.pontosComplicacaoGastos
    val pcLivres = (pcTotal - pcGastos).coerceAtLeast(0)

    val locked = state.criacaoBasicaCongeladaComXp
    val allowLongTexts = booleanResource(com.example.swadebuilder.R.bool.enable_long_texts)
    val detalhesExpandidos = remember { mutableStateMapOf<String, Boolean>() }

    var filter by remember { mutableStateOf(CompFilter()) }
    var showFilterDialog by rememberSaveable { mutableStateOf(false) }

    val origensDisponiveis = remember(state.compendioFantasiaAtivo, state.compendioTrilhadorAtivo, state.compendioDeadlandsAtivo) {
        buildSet {
            add("BASICO")
            if (state.compendioFantasiaAtivo) add("FANTASIA")
            if (state.compendioTrilhadorAtivo) add("FANTASIA_TRILHADOR")
            if (state.compendioDeadlandsAtivo) add("DEADLANDS")
        }.toList()
    }

    // Filtra apenas complicações das origens ativas
    val listaComplicacoesAtivas = remember(listaComplicacoes, state.compendioFantasiaAtivo, state.compendioTrilhadorAtivo, state.compendioDeadlandsAtivo) {
        listaComplicacoes.filter { comp ->
            val origem = (comp.origem ?: "BASICO").uppercase()
            origem == "BASICO" ||
            (origem == "FANTASIA" && state.compendioFantasiaAtivo) ||
            (origem == "FANTASIA_TRILHADOR" && state.compendioTrilhadorAtivo) ||
            (origem == "DEADLANDS" && state.compendioDeadlandsAtivo)
        }
    }

    SectionCard(
        title = "Complicações",
        expanded = expanded,
        onToggle = onToggle,
        icon = Icons.Default.Warning
    ) {
        SectionHeader(
            onHelpClick = null,
            centerText = "Pontos de Complicação (PC): $pcTotal",
            onListaCompletaClick = null,
            listaCompletaText = ""
        )

        Spacer(Modifier.size(4.dp))

        if (!locked) {
            PbWalletBanner(
                pcTotal = pcTotal,
                pcLivres = pcLivres,
                spendLabel = "",
                refundLabel = "",
                spendEnabled = false,
                refundEnabled = false,
                onSpend = {},
                onRefund = {}
            )
            Spacer(Modifier.size(8.dp))
        }

        Text(
            text = if (filter.isEmpty()) "Filtrar complicações"
            else "Filtros (${filter.totalSelections()})",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clickable { showFilterDialog = true }
        )

        if (showFilterDialog) {
            val allOrigens = listaComplicacoesAtivas
                .map { (it.origem ?: "BASICO").uppercase() }
                .distinct()
                .sorted()

            CompFilterDialog(
                allOrigens = allOrigens,
                current = filter,
                onChange = { filter = it },
                onDismiss = { showFilterDialog = false }
            )
        }

        val listaFiltrada = listaComplicacoesAtivas.filter { comp ->
            val o = (comp.origem ?: "BASICO").uppercase()
            if (filter.origens.isNotEmpty() && o !in filter.origens) return@filter false

            if (filter.tipos.isNotEmpty()) {
                val tiposComp = mutableListOf<String>()
                if (comp.severity.equals("Menor", ignoreCase = true) || comp.severity.equals("Ambas", ignoreCase = true)) {
                    tiposComp.add("MENOR")
                }
                if (comp.severity.equals("Maior", ignoreCase = true) || comp.severity.equals("Ambas", ignoreCase = true)) {
                    tiposComp.add("MAIOR")
                }
                if (filter.tipos.intersect(tiposComp).isEmpty()) return@filter false
            }
            true
        }

        Spacer(Modifier.size(8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 300.dp)
                .padding(start = 8.dp, bottom = 8.dp)
        ) {
            items(listaFiltrada, key = { it.id }) { comp ->
                val autoMenor = state.desvantagensAutomaticas.any {
                    it.substringBefore("(").trim().keyify() == comp.id.keyify()
                            && it.contains("Menor", ignoreCase = true)
                }
                val autoMaior = state.desvantagensAutomaticas.any {
                    it.substringBefore("(").trim().keyify() == comp.id.keyify()
                            && it.contains("Maior", ignoreCase = true)
                }
                val isAuto = autoMenor || autoMaior

                val currentSelection = state.complicacoesSelecionadas[comp]
                val isSelected = currentSelection != null

                // Verifica se essa complicação foi "reservada" (removida) com XP
                // Se foi reservada, ela conta como "removida", mas talvez o usuário queira vê-la?
                // Na lógica atual, se foi removida, ela sai de `state.complicacoesSelecionadas`.
                // Então ela aparece como não selecionada, mas o usuário não deveria poder selecioná-la de novo
                // se já usou XP pra tirar?
                // Savage Worlds permite recomprar? Em tese, se você "cura" uma complicação, ela sumiu.
                // Mas se quiser pegar de novo, é escolha do jogador.
                // Vou manter o comportamento padrão.

                val cardColor = if (isSelected || isAuto) {
                    MaterialTheme.colorScheme.tertiaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                if (state.modoOficialAtivo && !comp.originalName.isNullOrBlank()) comp.originalName else comp.name,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.weight(1f)
                            )
                            if (isAuto) {
                                Text(
                                    "(Racial/Auto)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        if (!isAuto) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (comp.severity.equals("Menor", ignoreCase = true) || comp.severity.equals("Ambas", ignoreCase = true)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(
                                            selected = (currentSelection == "Menor"),
                                            onClick = {
                                                if (!locked) {
                                                    if (currentSelection == "Menor") {
                                                        state.complicacoesSelecionadas.remove(comp)
                                                    } else {
                                                        state.complicacoesSelecionadas[comp] = "Menor"
                                                    }
                                                    state.rebuildAllPericiaStacks()
                                                }
                                            },
                                            enabled = !locked
                                        )
                                        Text("Menor", style = MaterialTheme.typography.bodySmall)
                                    }
                                    Spacer(Modifier.size(8.dp))
                                }

                                if (comp.severity.equals("Maior", ignoreCase = true) || comp.severity.equals("Ambas", ignoreCase = true)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(
                                            selected = (currentSelection == "Maior"),
                                            onClick = {
                                                if (!locked) {
                                                    if (currentSelection == "Maior") {
                                                        state.complicacoesSelecionadas.remove(comp)
                                                    } else {
                                                        state.complicacoesSelecionadas[comp] = "Maior"
                                                    }
                                                    state.rebuildAllPericiaStacks()
                                                }
                                            },
                                            enabled = !locked
                                        )
                                        Text("Maior", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }

                        if (allowLongTexts && comp.description.isNotBlank()) {
                            TextButton(
                                onClick = {
                                    val current = detalhesExpandidos[comp.id] ?: false
                                    detalhesExpandidos[comp.id] = !current
                                },
                                contentPadding = PaddingValues(horizontal = 0.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    if (detalhesExpandidos[comp.id] == true) "Ocultar detalhes" else "Ver detalhes",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                            AnimatedVisibility(visible = detalhesExpandidos[comp.id] == true) {
                                Text(
                                    if (state.modoOficialAtivo && !comp.originalDescription.isNullOrBlank()) comp.originalDescription else comp.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
