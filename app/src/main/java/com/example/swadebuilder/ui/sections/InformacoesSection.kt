package com.example.swadebuilder.ui.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.SectionCard
import com.example.swadebuilder.SectionHeader
import com.example.swadebuilder.model.StorageUtils
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.valorAparar
import com.example.swadebuilder.valorArmaduraEfetiva
import com.example.swadebuilder.valorMovimentacao
import com.example.swadebuilder.valorResistenciaFinal
import com.example.swadebuilder.valorTamanho
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonPrimitive
import kotlin.math.roundToInt

@Composable
fun InformacoesSection(
    state: CriadorState,
    onUseProgress: () -> Unit
) {
    val context = LocalContext.current

    // Gera um nome padrão "Nome 1", "Nome 2", ... se estiver em branco
    val nomesSalvos = remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }

    LaunchedEffect(Unit) {
        nomesSalvos.value = withContext(Dispatchers.IO) {
            StorageUtils.listarPersonagens(context)
        }
    }

    LaunchedEffect(nomesSalvos.value) {
        if (state.nomePersonagem.isBlank()) {
            var idx = 1
            while (nomesSalvos.value.any { it.first == "Nome $idx" }) {
                idx++
            }
            state.nomePersonagem = "Nome $idx"
        }
    }

    var expanded by rememberSaveable { mutableStateOf(false) }
    var showProgressDialog by rememberSaveable { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    SectionCard(
        title = "Informações",
        expanded = expanded,
        onToggle = { expanded = !expanded },
        icon = Icons.Default.Person
    ) {
        Column(Modifier.padding(8.dp)) {

            // 1) Cabeçalho no padrão das outras seções (com botão de ajuda)
            SectionHeader(
                onHelpClick = null,
                centerText = "Estágio: ${state.estagioAtual().nome}",
                onCenterClick = null,
                onListaCompletaClick = null,
                listaCompletaText = ""
            )

            Spacer(Modifier.height(8.dp))

            // 2) Nome do personagem
            OutlinedTextField(
                value = state.nomePersonagem,
                onValueChange = { state.nomePersonagem = it },
                label = { Text("Nome do Personagem") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                textStyle = MaterialTheme.typography.bodyLarge,
                colors = TextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
                )
            )

            val arcanoVersions: List<String> = state.vantagensSelecionadas
                .filter { it.id == "antecedente_arcano" }
                .mapNotNull { it.choice?.keyify() }

            val poderesOk = if (arcanoVersions.isEmpty()) {
                true
            } else {
                arcanoVersions.all { ver ->
                    val slots = state.poderSlotsPorArcano[ver]
                    slots != null && slots.isNotEmpty() && slots.all { it != null }
                }
            }

            val supersTerminados =
                (state.superPontosTotais > 0) && (state.superPontosDisponiveis == 0)
            val podeAbrirProgressos = state.emProgresso || supersTerminados
            val podeUsarProgresso =
                supersTerminados && (state.progressosDisponiveis > 0) && (state.pontosVantagem == 0)

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { showProgressDialog = true },
                    enabled = podeAbrirProgressos && poderesOk
                ) {
                    Text("Progressos: ${state.progresso}")
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        state.emProgresso = true
                        if (state.progressosDisponiveis > 0 && state.pontosVantagem == 0) {
                            onUseProgress()
                        }
                    },
                    enabled = podeUsarProgresso && poderesOk
                ) {
                    Text("Usar Progresso (${state.progressosDisponiveis})")
                }
            }

            // 4) Desfazer tudo
            if (state.progresso > 0) {
                TextButton(onClick = {
                    state.progresso = 0
                    state.progressosDisponiveis = 0
                    state.emProgresso = false
                }) {
                    Text("Desfazer Progresso")
                }
            }

            // 5) Parâmetros extras (Movim., Aparar, Resistência, Tamanho)
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CircleStat(value = state.valorMovimentacao().toString(), label = "Movim.")
                    CircleStat(value = state.valorAparar().toString(), label = "Aparar")
                }
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val temArmaduraDeEquip =
                        state.equipamentosComprados.any { it.armadura != null }
                    val bonusSemArmadura =
                        if (state.heroisSemArmadura && !temArmaduraDeEquip) 2 else 0

                    val baseFinal = state.valorResistenciaFinal()
                    val armaduraEfetiva = state.valorArmaduraEfetiva()
                    val total = baseFinal + armaduraEfetiva + bonusSemArmadura

                    CircleStat(
                        value = if ((armaduraEfetiva + bonusSemArmadura) != 0) "$baseFinal($total)" else "$baseFinal",
                        label = "Resistência"
                    )

                    val tam = state.valorTamanho()
                    CircleStat(
                        value = if (tam > 0) "+$tam" else tam.toString(),
                        label = "Tamanho"
                    )
                }
            }

            // 6) Diálogo para ajustar valor de progresso
            val spentOnCreation = state.progresso - state.progressosDisponiveis
            var tempProgresso by rememberSaveable { mutableIntStateOf(state.progresso) }
            if (showProgressDialog) {
                AlertDialog(
                    onDismissRequest = { showProgressDialog = false },
                    title = { Text("Defina o Progresso (0–50)") },
                    text = {
                        Column {
                            Slider(
                                value = tempProgresso.toFloat(),
                                onValueChange = { new ->
                                    tempProgresso = new.roundToInt()
                                        .coerceIn(spentOnCreation, 50)
                                },
                                valueRange = spentOnCreation.toFloat()..50f,
                                steps = 50,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "XP atual: $tempProgresso",
                                Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            state.progresso = tempProgresso
                            state.progressosDisponiveis = tempProgresso - spentOnCreation
                            state.emProgresso = true
                            showProgressDialog = false
                        }) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showProgressDialog = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            // 7) Peso carregado automático e alertas de sobrecarga
            val totalWeight = state.equipamentosComprados
                .mapNotNull { item ->
                    (item.peso as? JsonPrimitive)
                        ?.content
                        ?.replace(",", ".")
                        ?.toFloatOrNull()
                }
                .sum()

            val hasMusculoso = state.vantagensSelecionadas.any {
                it.nome.keyify() == "musculoso"
            }
            val hasSoldado = state.vantagensSelecionadas.any {
                it.nome.keyify() == "soldado"
            }
            val bonusCapacity = (if (hasMusculoso) 10f else 0f) +
                    (if (hasSoldado) 10f else 0f)

            val strengthRaw = state.valoresAtributos["FORCA"]!!.intValue
            val baseLimit = ((strengthRaw - 2) / 2) * 10f
            val limit = baseLimit + bonusCapacity

            Text(
                text = "Peso Total: ${"%.1f".format(totalWeight)} / ${"%.1f".format(limit)}",
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                textAlign = TextAlign.Center
            )

            val ratio = if (limit > 0f) totalWeight / limit else Float.POSITIVE_INFINITY
            val warning = when {
                ratio >= 4f -> "Impossível carregar tanto peso, remova itens ou aumente a força."
                ratio >= 3f -> "Peso extremo! Penalidades severas de sobrecarga."
                ratio > 1f -> "Peso excedido! Você está sobrecarregado."
                else -> ""
            }
            if (warning.isNotEmpty()) {
                Text(
                    text = warning,
                    color = Color.Red,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
