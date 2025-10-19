package com.example.myapplication.ui.sections

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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.CriadorState
import com.example.myapplication.SectionCard
import com.example.myapplication.SectionHeader
import com.example.myapplication.listaComplicacoes
import com.example.myapplication.util.keyify
import com.example.myapplication.util.semAcentos
import kotlinx.serialization.json.JsonPrimitive

@ExperimentalMaterial3Api
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun ComplicacoesSection(
    state: CriadorState,
    onOpenComplicacoesDetail: () -> Unit
) {
    val locked = state.progresso > 0
    var expCompSection by rememberSaveable { mutableStateOf(false) }
    var expCompMenu    by rememberSaveable { mutableStateOf(false) }
    var showHelp       by rememberSaveable { mutableStateOf(false) }
    var showGastarPcDialog by rememberSaveable { mutableStateOf(false) }
    var allocAtributo by rememberSaveable { mutableIntStateOf(0) }
    var allocPericia  by rememberSaveable { mutableIntStateOf(0) }
    var allocVantagem by rememberSaveable { mutableIntStateOf(0) }
    var allocRecursos by rememberSaveable { mutableIntStateOf(0) }
    var isRefundMode  by rememberSaveable { mutableStateOf(false) }

    // Chaves automáticas (desvantagens raciais)
    val autoBaseKeys = state.desvantagensAutomaticas
        .map { it.uppercase().semAcentos().substringBefore("(").trim() }
        .toSet()

    SectionCard(
        title    = "Complicações",
        expanded = expCompSection,
        onToggle = { expCompSection = !expCompSection },
        icon     = Icons.Default.Warning
    ) {
        // 1. Cabeçalho usando SectionHeader
        SectionHeader(
            onHelpClick          = { showHelp = true },
            centerText           = "Gastar/Devolver pontos: " +
                    "${(state.pontosComplicacao - state.pontosComplicacaoGastos).coerceAtLeast(0)}",
            onCenterClick        = if (!locked) ({ showGastarPcDialog = true }) else null,
            onListaCompletaClick = onOpenComplicacoesDetail
        )

        Spacer(Modifier.height(8.dp))

        // 2. Diálogo de ajuda
        if (showHelp) {
            AlertDialog(
                onDismissRequest = { showHelp = false },
                title            = { Text("Como funciona") },
                text             = {
                    Text(
                        "Escolha Complicações para ganhar Pontos de Complicação (PC).\n" +
                                "Cada Complicação Menor vale 1 PC, cada complicação Maior vale 2 pontos\n" +
                                "Você pode obter no máximo 4 pontos.\n" +
                                "Para usar os pontos ganhos, clique no botão Gastar/Devolver pontos."
                    )
                },
                confirmButton    = {
                    TextButton(onClick = { showHelp = false }) {
                        Text("OK")
                    }
                }
            )
        }

        // 3. Diálogo de gastar ou devolver PC
        if (showGastarPcDialog) {
            AlertDialog(
                onDismissRequest = { showGastarPcDialog = false },
                title            = {
                    Row {
                        TextButton(onClick = { isRefundMode = false }) { Text("Gastar") }
                        Spacer(Modifier.width(8.dp))
                        TextButton(onClick = { isRefundMode = true }) { Text("Devolver") }
                    }
                },
                text             = {
                    Column {
                        val remainingPc = state.pontosComplicacao - state.pontosComplicacaoGastos

                        if (!isRefundMode) {
                            // Atributo (2 PC)
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Atributo (2 PC)", Modifier.weight(1f))
                                IconButton(
                                    onClick = { if (!locked && allocAtributo > 0) allocAtributo-- },
                                    enabled = !locked && allocAtributo > 0
                                ) { Icon(Icons.Default.Remove, contentDescription = null) }
                                Text("$allocAtributo")
                                IconButton(
                                    onClick = {
                                        if (!locked &&
                                            remainingPc - (allocAtributo * 2 + allocPericia + allocVantagem * 2 + allocRecursos) >= 2
                                        ) allocAtributo++
                                    },
                                    enabled = !locked &&
                                            remainingPc - (allocAtributo * 2 + allocPericia + allocVantagem * 2 + allocRecursos) >= 2
                                ) { Icon(Icons.Default.Add, contentDescription = null) }
                            }
                            // Perícia (1 PC)
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Perícia (1 PC)", Modifier.weight(1f))
                                IconButton(
                                    onClick = { if (!locked && allocPericia > 0) allocPericia-- },
                                    enabled = !locked && allocPericia > 0
                                ) { Icon(Icons.Default.Remove, contentDescription = null) }
                                Text("$allocPericia")
                                IconButton(
                                    onClick = {
                                        if (!locked &&
                                            remainingPc - (allocAtributo * 2 + allocPericia + allocVantagem * 2 + allocRecursos) >= 1
                                        ) allocPericia++
                                    },
                                    enabled = !locked &&
                                            remainingPc - (allocAtributo * 2 + allocPericia + allocVantagem * 2 + allocRecursos) >= 1
                                ) { Icon(Icons.Default.Add, contentDescription = null) }
                            }
                            // Recursos ×2 (1 PC)
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Recursos ×2 (1 PC)", Modifier.weight(1f))
                                IconButton(
                                    onClick = { if (!locked && allocRecursos > 0) allocRecursos-- },
                                    enabled = !locked && allocRecursos > 0
                                ) { Icon(Icons.Default.Remove, contentDescription = null) }
                                Text("$allocRecursos")
                                IconButton(
                                    onClick = {
                                        if (!locked && allocRecursos < 1 &&
                                            remainingPc - (allocAtributo * 2 + allocPericia + allocVantagem * 2 + allocRecursos) >= 1
                                        ) allocRecursos++
                                    },
                                    enabled = !locked && allocRecursos < 1 &&
                                            remainingPc - (allocAtributo * 2 + allocPericia + allocVantagem * 2 + allocRecursos) >= 1
                                ) { Icon(Icons.Default.Add, contentDescription = null) }
                            }
                            // Vantagem (2 PC)
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Vantagem (2 PC)", Modifier.weight(1f))
                                IconButton(
                                    onClick = { if (!locked && allocVantagem > 0) allocVantagem-- },
                                    enabled = !locked && allocVantagem > 0
                                ) { Icon(Icons.Default.Remove, contentDescription = null) }
                                Text("$allocVantagem")
                                IconButton(
                                    onClick = {
                                        if (!locked &&
                                            remainingPc - (allocAtributo * 2 + allocPericia + allocVantagem * 2 + allocRecursos) >= 2
                                        ) allocVantagem++
                                    },
                                    enabled = !locked &&
                                            remainingPc - (allocAtributo * 2 + allocPericia + allocVantagem * 2 + allocRecursos) >= 2
                                ) { Icon(Icons.Default.Add, contentDescription = null) }
                            }

                        } else {
                            // MODO DEVOLVER
                            Text("Devolver Pontos de Complicação", fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            Text("PA via CP: ${state.cpPaStack.size}")
                            Text("SP via CP: ${state.cpSpStack.size}")
                            Text("PV via CP: ${state.cpPvStack.size}")
                            Text("$$ via CP: ${state.cpRecursosStack.size}")
                            Spacer(Modifier.height(12.dp))

                            Column {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Button(
                                        onClick = {
                                            if (!locked && state.cpPaStack.isNotEmpty()) {
                                                state.cpPaStack.removeAt(state.cpPaStack.lastIndex)
                                                state.pontosComplicacaoGastos -= 2
                                                state.recalcularPontosAtributo()
                                                val lastAttr = state.paCostStackPorAtributo
                                                    .entries
                                                    .firstOrNull { it.value.isNotEmpty() }
                                                    ?.key
                                                if (lastAttr != null) {
                                                    state.paCostStackPorAtributo[lastAttr]!!
                                                        .removeAt(state.paCostStackPorAtributo[lastAttr]!!.lastIndex)
                                                    state.valoresAtributos[lastAttr]!!.intValue -= 2
                                                } else {
                                                    state.pontosAtributo++
                                                }
                                                state.recalcularPontosAtributo()
                                            }
                                        },
                                        enabled = !locked && state.cpPaStack.isNotEmpty()
                                    ) { Text("- PA") }

                                    Button(
                                        onClick = {
                                            if (!locked && state.cpSpStack.isNotEmpty()) {
                                                state.cpSpStack.removeAt(state.cpSpStack.lastIndex)
                                                state.pontosComplicacaoGastos -= 1
                                                state.syncFromCPRefund(sp = true)
                                            }
                                        },
                                        enabled = !locked && state.cpSpStack.isNotEmpty()
                                    ) { Text("- SP") }
                                }

                                Spacer(Modifier.height(8.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Button(
                                        onClick = {
                                            if (!locked && state.cpPvStack.isNotEmpty()) {
                                                state.cpPvStack.removeAt(state.cpPvStack.lastIndex)
                                                state.pontosComplicacaoGastos -= 2
                                                if (state.pontosVantagem > 0) {
                                                    state.pontosVantagem--
                                                } else if (state.vantagensSelecionadas.isNotEmpty()) {
                                                    state.vantagensSelecionadas.removeAt(state.vantagensSelecionadas.lastIndex)
                                                }
                                            }
                                        },
                                        enabled = !locked && state.cpPvStack.isNotEmpty()
                                    ) { Text("- PV") }

                                    Button(
                                        onClick = {
                                            if (!locked && state.cpRecursosStack.isNotEmpty()) {
                                                state.cpRecursosStack.removeAt(state.cpRecursosStack.lastIndex)
                                                state.pontosComplicacaoGastos -= 1
                                                if (state.dinheiro >= 500) {
                                                    state.dinheiro -= 500
                                                } else {
                                                    while (state.dinheiro < 500 && state.equipamentosComprados.isNotEmpty()) {
                                                        val equipamento = state.equipamentosComprados.removeAt(state.equipamentosComprados.lastIndex)
                                                        val custo = (equipamento.custo as? JsonPrimitive)
                                                            ?.content
                                                            ?.toIntOrNull()
                                                            ?: 0
                                                        state.dinheiro += custo
                                                    }
                                                    if (state.dinheiro >= 500) {
                                                        state.dinheiro -= 500
                                                    }
                                                }
                                            }
                                        },
                                        enabled = !locked && state.cpRecursosStack.isNotEmpty()
                                    ) { Text("- $$") }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (!isRefundMode) {
                            repeat(allocAtributo) { state.cpPaStack.add("PA") }
                            repeat(allocVantagem) { state.cpPvStack.add(Unit) }
                            repeat(allocPericia) { state.cpSpStack.add(Unit) }
                            repeat(allocRecursos) {
                                state.cpRecursosStack.add(Unit)
                                state.dinheiro += 500
                            }
                            state.pontosComplicacaoGastos += allocAtributo * 2 +
                                    allocPericia +
                                    allocVantagem * 2 +
                                    allocRecursos
                            state.pontosAtributo += allocAtributo
                            state.pontosVantagem += allocVantagem
                            allocAtributo = 0
                            allocPericia  = 0
                            allocVantagem = 0
                            allocRecursos = 0
                        }
                        showGastarPcDialog = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showGastarPcDialog = false }) { Text("Cancelar") }
                }
            )
        }

        Spacer(Modifier.height(8.dp))

        // 4. Chips das complicações já selecionadas
        if (!showGastarPcDialog) {
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

                        AssistChip(
                            onClick = {
                                if (!locked && canRemove) {
                                    when (comp.id) {
                                        "idoso" -> {
                                            state.complicacoesSelecionadas.remove(comp)
                                            state.idosoBonusSp = 0
                                            state.syncFromCPRefund(sp = true)
                                        }
                                        "jovem" -> {
                                            val pequComp = listaComplicacoes.first { it.id == "pequeno" }
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
                                }
                            },
                            enabled     = canRemove,
                            label       = { Text("${comp.name} ($tipo)") },
                            leadingIcon = { Icon(Icons.Default.Close, contentDescription = null) }
                        )
                    }
            }

            Spacer(Modifier.height(8.dp))

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
                    val pequComp = listaComplicacoes.first { it.id == "pequeno" }

                    // Filtra para remover automáticas e só traz “SUPER” se ativo
                    listaComplicacoes
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
}

@Composable
fun TransparentOutlinedReadOnlyField(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value         = text,
        onValueChange = {},  // read‐only
        enabled       = enabled,
        readOnly      = true,
        textStyle     = LocalTextStyle.current.copy(color = Color.Black),
        singleLine    = true,
        modifier      = modifier,
        decorationBox = { inner ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color.Transparent, shape = MaterialTheme.shapes.small)
                    .border(
                        width = 1.dp,
                        color = if (enabled) Color.Black else Color.Gray,
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clickable(enabled = enabled, onClick = onClick)
            ) {
                inner()  // o texto
                Spacer(Modifier.weight(1f))
                Icon(
                    imageVector        = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint               = if (enabled) Color.Black else Color.Gray
                )
            }
        }
    )
}
