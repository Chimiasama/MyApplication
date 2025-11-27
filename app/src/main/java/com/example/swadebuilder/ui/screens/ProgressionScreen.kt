package com.example.swadebuilder.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.buildSummaryLines
import com.example.swadebuilder.toMeuPersonagem
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.KeyboardType
import com.example.swadebuilder.model.CriadorViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import com.example.swadebuilder.RadioButtonRow
import com.example.swadebuilder.listaDeEstagios
import com.example.swadebuilder.model.Complicacao
import com.example.swadebuilder.Pericia
import com.example.swadebuilder.listaAtributos
import com.example.swadebuilder.listaPericias
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults

@Composable
fun ProgressionScreen(state: CriadorState) {
    val summaryLines = remember(state) {
        buildSummaryLines(state.toMeuPersonagem())
    }

    LazyColumn(modifier = Modifier.padding(16.dp)) {
        item {
            Text("Ficha do Personagem", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
        }

        val sections = summaryLines.joinToString("\n").split("\n\n")
        items(sections) { section ->
            val lines = section.lines()
            if (lines.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = lines.first(),
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        lines.drop(1).forEach { line ->
                            Text(text = line)
                        }
                    }
                }
            }
        }

        item {
            ProgressionSlotsSection(state = state)
        }
    }
}

@Composable
fun ProgressionSlotDialog(
    stageName: String,
    onDismiss: () -> Unit,
    viewModel: CriadorViewModel,
    state: CriadorState
) {
    var selectedOption by remember { mutableStateOf("") }
    var periciaAlta by remember { mutableStateOf<Pericia?>(null) }
    var periciaBaixa1 by remember { mutableStateOf<Pericia?>(null) }
    var periciaBaixa2 by remember { mutableStateOf<Pericia?>(null) }
    var atributo by remember { mutableStateOf<String?>(null) }
    var complicacao by remember { mutableStateOf<Complicacao?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Gastar Progresso ($stageName)") },
        text = {
            Column {
                RadioButtonRow("Comprar Vantagem", selectedOption == "vantagem") {
                    selectedOption = "vantagem"
                }
                RadioButtonRow("Aumentar Perícia ≥ Atributo", selectedOption == "pericia_alta") {
                    selectedOption = "pericia_alta"
                }
                if (selectedOption == "pericia_alta") {
                    PericiaSelector(
                        pericias = listaPericias.filter { state.rawTotal(it) >= state.valoresAtributos[it.atributo]!!.intValue },
                        selected = periciaAlta,
                        onSelect = { periciaAlta = it }
                    )
                }
                RadioButtonRow("Aumentar 2 Perícias < Atributo", selectedOption == "pericias_baixas") {
                    selectedOption = "pericias_baixas"
                }
                if (selectedOption == "pericias_baixas") {
                    PericiaSelector(
                        pericias = listaPericias.filter { state.rawTotal(it) < state.valoresAtributos[it.atributo]!!.intValue },
                        selected = periciaBaixa1,
                        onSelect = { periciaBaixa1 = it }
                    )
                    PericiaSelector(
                        pericias = listaPericias.filter { state.rawTotal(it) < state.valoresAtributos[it.atributo]!!.intValue && it != periciaBaixa1 },
                        selected = periciaBaixa2,
                        onSelect = { periciaBaixa2 = it }
                    )
                }
                RadioButtonRow("Aumentar Atributo", selectedOption == "atributo") {
                    selectedOption = "atributo"
                }
                if (selectedOption == "atributo") {
                    AtributoSelector(
                        atributos = listaAtributos,
                        selected = atributo,
                        onSelect = { atributo = it }
                    )
                }
                RadioButtonRow("Remover Complicação", selectedOption == "complicacao") {
                    selectedOption = "complicacao"
                }
                if (selectedOption == "complicacao") {
                    ComplicacaoSelector(
                        complicacoes = state.complicacoesSelecionadas.keys.toList(),
                        selected = complicacao,
                        onSelect = { complicacao = it }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    when (selectedOption) {
                        "vantagem" -> viewModel.gastarProgressoEmVantagem(stageName)
                        "pericia_alta" -> periciaAlta?.let { viewModel.gastarProgressoEmPericiaAlta(it) }
                        "pericias_baixas" -> if (periciaBaixa1 != null && periciaBaixa2 != null) {
                            viewModel.gastarProgressoEmPericiasBaixas(periciaBaixa1!!, periciaBaixa2!!)
                        }
                        "atributo" -> atributo?.let { viewModel.gastarProgressoEmAtributo(stageName, it) }
                        "complicacao" -> complicacao?.let { viewModel.gastarProgressoEmComplicacao(it) }
                    }
                    onDismiss()
                }
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AtributoSelector(
    atributos: List<String>,
    selected: String?,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selected ?: "Selecione o Atributo",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                .fillMaxWidth()
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            atributos.forEach { atributo ->
                DropdownMenuItem(
                    text = { Text(atributo) },
                    onClick = {
                        onSelect(atributo)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PericiaSelector(
    pericias: List<Pericia>,
    selected: Pericia?,
    onSelect: (Pericia) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selected?.nome ?: "Selecione a Perícia",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                .fillMaxWidth()
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            pericias.forEach { pericia ->
                DropdownMenuItem(
                    text = { Text(pericia.nome) },
                    onClick = {
                        onSelect(pericia)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplicacaoSelector(
    complicacoes: List<Complicacao>,
    selected: Complicacao?,
    onSelect: (Complicacao) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selected?.name ?: "Selecione a Complicação",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                .fillMaxWidth()
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            complicacoes.forEach { complicacao ->
                DropdownMenuItem(
                    text = { Text(complicacao.name) },
                    onClick = {
                        onSelect(complicacao)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ProgressionSlotsSection(state: CriadorState, viewModel: CriadorViewModel = viewModel()) {
    var xp by remember { mutableStateOf(state.progresso.toString()) }
    var showDialog by remember { mutableStateOf(false) }
    var selectedStage by remember { mutableStateOf("") }

    if (showDialog) {
        ProgressionSlotDialog(
            stageName = selectedStage,
            onDismiss = { showDialog = false },
            viewModel = viewModel
        )
    }

    Column {
        Text("Progressão", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = xp,
            onValueChange = {
                xp = it
                state.progresso = it.toIntOrNull() ?: 0
            },
            label = { Text("Total de XP") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        val advances = state.progresso
        val stageCounts = listOf(3, 4, 4, 4, advances - 15)

        Column {
            var advanceCount = 0
            listaDeEstagios.forEachIndexed { index, stage ->
                if (advanceCount < advances) {
                    Text(stage.nome, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                repeat(stageCounts[index]) {
                    if (advanceCount < advances) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .clickable {
                                    selectedStage = stage.nome
                                    showDialog = true
                                },
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Text(
                                text = "Slot de avanço ${advanceCount + 1}",
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                        advanceCount++
                    }
                }
                if (advanceCount < advances) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
