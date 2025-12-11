package com.example.swadebuilder.ui.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.ui.components.SectionCard
import com.example.swadebuilder.ui.components.SectionHeader
import com.example.swadebuilder.util.keyify
import kotlinx.serialization.json.JsonPrimitive
import kotlin.math.roundToInt

@Composable
fun InformacoesSection(
    state: CriadorState,
    expanded: Boolean,
    onToggle: () -> Unit,
    onUseProgress: () -> Unit
) {
    if (state.nomePersonagem.isBlank()) {
        state.nomePersonagem = "Nome 1"
    }

    var showProgressDialog by rememberSaveable { mutableStateOf(false) }
    var showMoneyDialog by rememberSaveable { mutableStateOf(false) }
    var dinheiroInput by rememberSaveable { mutableStateOf(state.dinheiro.toString()) }
    val focusManager = LocalFocusManager.current

    SectionCard(
        title = "Informações",
        expanded = expanded,
        onToggle = onToggle,
        icon = Icons.Default.Person
    ) {
        Column(Modifier.padding(8.dp)) {

            SectionHeader(
                onHelpClick = null,
                centerText = "Estágio: ${state.estagioAtual().nome}",
                onCenterClick = null,
                onListaCompletaClick = null,
                listaCompletaText = ""
            )

            Spacer(Modifier.height(8.dp))

            if (state.emProgresso) {
                TextButton(onClick = {
                    dinheiroInput = state.dinheiro.toString()
                    showMoneyDialog = true
                }) {
                    Text("Editar dinheiro")
                }
                Spacer(Modifier.height(4.dp))
            }

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


            val movimento = state.valorMovimentacao()
            val aparar = state.valorAparar()

            val temArmaduraDeEquip =
                state.equipamentosComprados.any { it.armadura != null }
            val bonusSemArmadura =
                if (state.heroisSemArmadura && !temArmaduraDeEquip) 2 else 0

            val baseResFinal = state.valorResistenciaFinal()
            val armaduraEfetiva = state.valorArmaduraEfetiva()
            val totalRes = baseResFinal + armaduraEfetiva + bonusSemArmadura
            val resistenciaTexto =
                if ((armaduraEfetiva + bonusSemArmadura) != 0)
                    "$baseResFinal ($totalRes)"
                else
                    baseResFinal.toString()

            val tamanho = state.valorTamanho()
            val tamanhoTexto = if (tamanho > 0) "+$tamanho" else tamanho.toString()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        StatItem("Movimentação", movimento.toString())
                        Spacer(modifier = Modifier.height(8.dp))
                        StatItem("Resistência", resistenciaTexto)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        StatItem("Aparar", aparar.toString())
                        Spacer(modifier = Modifier.height(8.dp))
                        StatItem("Tamanho", tamanhoTexto)
                    }
                }
            }

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

        if (showMoneyDialog) {
            AlertDialog(
                onDismissRequest = { showMoneyDialog = false },
                title = { Text("Editar dinheiro") },
                text = {
                    OutlinedTextField(
                        value = dinheiroInput,
                        onValueChange = { novo ->
                            dinheiroInput = novo.filter { it.isDigit() || it == '-' }
                        },
                        label = { Text("Valor em dinheiro") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        val novoValor = dinheiroInput.toIntOrNull()
                        if (novoValor != null) {
                            state.dinheiro = novoValor
                        }
                        showMoneyDialog = false
                    }) {
                        Text("Salvar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showMoneyDialog = false }) { Text("Cancelar") }
                }
            )
        }

            val totalWeight = state.equipamentosComprados
                .mapNotNull { item ->
                    (item.peso as? JsonPrimitive)
                        ?.content
                        ?.replace(",", ".")
                        ?.toFloatOrNull()
                }
                .sum()

            val hasMusculoso = state.vantagensSelecionadas.any {
                it.nome.keyify() == "MUSCULOSO"
            }
            val hasSoldado = state.vantagensSelecionadas.any {
                it.nome.keyify() == "SOLDADO"
            }
            val bonusCapacity = if (hasMusculoso) 10f else 0f

            val strengthRaw = state.valoresAtributos["FORCA"]!!.intValue
            val effectiveStrength = if (hasSoldado && state.soldadoCargaAtivo) {
                if (strengthRaw < 12) strengthRaw + 2 else strengthRaw + 1
            } else {
                strengthRaw
            }
            val baseLimit = ((effectiveStrength - 2) / 2) * 10f
            val limit = baseLimit + bonusCapacity

            Text(
                text = "Peso Total: ${"%.1f".format(totalWeight)} / ${"%.1f".format(limit)}",
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                textAlign = TextAlign.Center
            )

            if (hasSoldado) {
                AssistChip(
                    onClick = { state.soldadoCargaAtivo = !state.soldadoCargaAtivo },
                    label = {
                        Text(
                            if (state.soldadoCargaAtivo) "Bônus Soldado ativo" else "Bônus Soldado inativo"
                        )
                    }
                )
            }

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

@Composable
fun StatItem(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center
        )
    }
}
