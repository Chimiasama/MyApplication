package com.example.swadebuilder.ui.dialogs

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.Pericia
import com.example.swadebuilder.RadioButtonRow
import com.example.swadebuilder.listaAtributos
import com.example.swadebuilder.listaDeEstagios
import com.example.swadebuilder.listaPericias
import com.example.swadebuilder.listaVantagens
import com.example.swadebuilder.mapaAtributosDisplay
import com.example.swadebuilder.model.Complicacao
import com.example.swadebuilder.model.EspecializacoesDto
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.periciaStartRaw
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.semAcentos
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressosDialog(
    state: CriadorState,
    onDismiss: () -> Unit
) {
    // Snackbar para mensagens temporárias (substitui showTempError/tempErrorMsg)
    val snackHost = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    fun showSnack(msg: String) = scope.launch { snackHost.showSnackbar(message = msg) }

    var escolheu by rememberSaveable { mutableStateOf<String?>(null) }
    var perAltaExp by rememberSaveable { mutableStateOf(false) }
    var perAltaSelected by rememberSaveable { mutableStateOf<Pericia?>(null) }
    var perBaixaExp1 by rememberSaveable { mutableStateOf(false) }
    var perBaixa1 by rememberSaveable { mutableStateOf<Pericia?>(null) }
    var perBaixaExp2 by rememberSaveable { mutableStateOf(false) }
    var perBaixa2 by rememberSaveable { mutableStateOf<Pericia?>(null) }
    var attrExp by rememberSaveable { mutableStateOf(false) }
    var attrSelected by rememberSaveable { mutableStateOf<String?>(null) }
    var compExp by rememberSaveable { mutableStateOf(false) }
    var compSelected by rememberSaveable { mutableStateOf<Complicacao?>(null) }
    var showAdvSelection by rememberSaveable { mutableStateOf(false) }
    var pendingAdv by rememberSaveable { mutableStateOf<Vantagem?>(null) }
    var showPendingChoice by rememberSaveable { mutableStateOf(false) }
    var advSelectedStageIndex by rememberSaveable { mutableIntStateOf(-1) }

    // Slots: perícia OU especialização (quando a regra estiver ON)
    var slot1IsSpec by rememberSaveable { mutableStateOf(false) }
    var slot2IsSpec by rememberSaveable { mutableStateOf(false) }
    var slot1SpecPerExp by rememberSaveable { mutableStateOf(false) }
    var slot2SpecPerExp by rememberSaveable { mutableStateOf(false) }
    var slot1SpecPer by rememberSaveable { mutableStateOf<Pericia?>(null) }
    var slot2SpecPer by rememberSaveable { mutableStateOf<Pericia?>(null) }
    var slot1SpecName by rememberSaveable { mutableStateOf("") }
    var slot2SpecName by rememberSaveable { mutableStateOf("") }

    // Especialização inicial para perícia nova (=0) quando a regra estiver ON
    var slot1NewPerSpecName by rememberSaveable { mutableStateOf("") }
    var slot2NewPerSpecName by rememberSaveable { mutableStateOf("") }

    // Configuração de estágios
    val totalProgressLimit = 50
    val stages = listaDeEstagios
    val stageCaps = stages.mapIndexed { idx, st ->
        val prevMax = stages.getOrNull(idx - 1)?.maxProgress ?: 0
        if (idx < stages.lastIndex) st.maxProgress - prevMax else (totalProgressLimit - prevMax).coerceAtLeast(0)
    }
    val currentStageIndex = stageCaps
        .mapIndexed { i, cap -> i to cap }
        .firstOrNull { (i, cap) -> state.stageXpSpent.getValue(stages[i].nome) < cap }
        ?.first ?: stages.lastIndex

    var selectedTab by rememberSaveable { mutableIntStateOf(currentStageIndex) }

    listaPericias.associateWith { 0 }.toMutableMap()

    // ── Cálculos de atributo via XP ────────────────────────────────────────────
    val est = stages[selectedTab]
    val prevMaxGlobal = if (selectedTab > 0) stages[selectedTab - 1].maxProgress else 0
    val stageCap = if (selectedTab < stages.lastIndex) est.maxProgress - prevMaxGlobal
    else (totalProgressLimit - prevMaxGlobal).coerceAtLeast(0)
    val spentHere = state.stageXpSpent.getValue(est.nome)
    val creditsLeft = stageCap - spentHere

    val boughtSoFar = stages.take(selectedTab + 1).sumOf { state.comprasAttrPorEstagio.getValue(it.nome) }
    val maxAllowed = if (est.nome == "Lendário") Int.MAX_VALUE else (selectedTab + 1)
    val costAttr = if (est.nome == "Lendário") 2 else 1
    val canBuyAttr = creditsLeft >= costAttr && state.progressosDisponiveis >= costAttr && boughtSoFar < maxAllowed
    val podeComprarVantagem = creditsLeft > 0 && state.progressosDisponiveis > 0

    // Helpers 2×
    fun possui(per: Pericia): Boolean = state.rawTotal(per) > 0
    fun sendoCompradaAgora(per: Pericia): Boolean =
        (!slot1IsSpec && perBaixa1 == per) || (!slot2IsSpec && perBaixa2 == per)

    fun podeAumentarAbaixo(per: Pericia): Boolean {
        val startRaw = periciaStartRaw(state.ancestralidade, per)
        val baseIncs = state.baseIncsPorPericia.getValue(per)
        val extraIncs =
            (if (!slot1IsSpec && perBaixa1 == per) 1 else 0) +
                    (if (!slot2IsSpec && perBaixa2 == per) 1 else 0)
        val totalIncs = baseIncs + extraIncs + 1
        val extraStep = if (startRaw == 0 && totalIncs > 0) 2 else 0
        val newRaw = startRaw + 2 * totalIncs + extraStep
        return state.rawTotal(per) < state.valoresAtributos[per.atributo]!!.intValue &&
                newRaw <= state.valoresAtributos[per.atributo]!!.intValue
    }

    fun deveMostrarSpecNesteSlot(slotEh1: Boolean, perDesteSlot: Pericia?): Boolean {
        if (!state.usarEspecializacoesDePericia) return false
        if (perDesteSlot == null) return false
        if (state.rawTotal(perDesteSlot) != 0) return false

        val outroJaInformou = if (slotEh1) {
            (!slot2IsSpec && perBaixa2 == perDesteSlot && slot2NewPerSpecName.trim().isNotEmpty()) ||
                    ( slot2IsSpec && slot2SpecPer == perDesteSlot && slot2SpecName.trim().isNotEmpty())
        } else {
            (!slot1IsSpec && perBaixa1 == perDesteSlot && slot1NewPerSpecName.trim().isNotEmpty()) ||
                    ( slot1IsSpec && slot1SpecPer == perDesteSlot && slot1SpecName.trim().isNotEmpty())
        }
        return !outroJaInformou
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                stages.forEachIndexed { i, st ->
                    val enabled = i == currentStageIndex
                    Tab(
                        selected = (selectedTab == i),
                        onClick  = { if (enabled) selectedTab = i },
                        enabled  = enabled,
                        text = {
                            Text(
                                st.nome.first().toString(),
                                color = if (enabled) Color.Unspecified else Color.Gray
                            )
                        }
                    )
                }
            }
        },
        text = {
            Column {
                // host para mensagens rápidas
                SnackbarHost(hostState = snackHost)

                Spacer(Modifier.height(8.dp))

                Text("XP neste estágio: $spentHere / $stageCap")
                Spacer(Modifier.height(16.dp))

                RadioButtonRow("Perícia ≥ Atributo", escolheu == "PericiaAlta") {
                    escolheu = "PericiaAlta"
                }
                if (escolheu == "PericiaAlta") {
                    ExposedDropdownMenuBox(
                        expanded = perAltaExp,
                        onExpandedChange = { perAltaExp = !perAltaExp }
                    ) {
                        OutlinedTextField(
                            value = perAltaSelected?.nome ?: "Escolha perícia…",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(perAltaExp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                .clickable { perAltaExp = true }
                        )
                        ExposedDropdownMenu(
                            expanded = perAltaExp,
                            onDismissRequest = { perAltaExp = false },
                            modifier = Modifier.heightIn(max = 200.dp)
                        ) {
                            listaPericias
                                .filter { per ->
                                    val curr = state.rawTotal(per)
                                    val baseAttrKey = state.atributoBaseParaPericia(per)
                                    val atrRaw = state.valoresAtributos[baseAttrKey]!!.intValue
                                    val capRaw = state.periciaCapRaw(per)
                                    curr in atrRaw until capRaw
                                }
                                .forEach { per ->
                                    DropdownMenuItem(
                                        text = { Text(per.nome) },
                                        onClick = {
                                            perAltaSelected = per
                                            perAltaExp = false
                                        }
                                    )
                                }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }

                // ===== 2× Perícias/Especializações < Atributo (texto dinâmico) =====
                val titulo2x = if (state.usarEspecializacoesDePericia)
                    "2× Perícias/Especializações < Atributo"
                else
                    "2× Perícias < Atributo"

                RadioButtonRow(titulo2x, escolheu == "PericiasBaixas") {
                    escolheu = "PericiasBaixas"
                }
                if (escolheu == "PericiasBaixas") {
                    if (!state.usarEspecializacoesDePericia) {
                        slot1IsSpec = false; slot2IsSpec = false
                        slot1SpecPer = null; slot2SpecPer = null
                        slot1SpecName = ""; slot2SpecName = ""
                        slot1NewPerSpecName = ""; slot2NewPerSpecName = ""
                    }

                    // ---------- SLOT 1 ----------
                    Text("Escolha 1")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = !slot1IsSpec, onClick = { slot1IsSpec = false })
                        Text("Perícia")
                        Spacer(Modifier.width(16.dp))
                        if (state.usarEspecializacoesDePericia) {
                            RadioButton(selected = slot1IsSpec, onClick = { slot1IsSpec = true })
                            Text("Especialização")
                        }
                    }

                    if (!slot1IsSpec) {
                        ExposedDropdownMenuBox(
                            expanded = perBaixaExp1,
                            onExpandedChange = { perBaixaExp1 = !perBaixaExp1 }
                        ) {
                            OutlinedTextField(
                                value = perBaixa1?.nome ?: "Perícia 1…",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(perBaixaExp1) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                    .clickable { perBaixaExp1 = true }
                            )
                            ExposedDropdownMenu(
                                expanded = perBaixaExp1,
                                onDismissRequest = { perBaixaExp1 = false },
                                modifier = Modifier.heightIn(max = 200.dp)
                            ) {
                                listaPericias.filter { per -> podeAumentarAbaixo(per) }.forEach { per ->
                                    DropdownMenuItem(
                                        text = { Text(per.nome) },
                                        onClick = {
                                            perBaixa1 = per
                                            if (state.rawTotal(per) == 0) slot1NewPerSpecName = ""
                                            perBaixaExp1 = false
                                        }
                                    )
                                }
                            }
                        }
                        if (deveMostrarSpecNesteSlot(true, perBaixa1)) {
                            Spacer(Modifier.height(6.dp))
                            OutlinedTextField(
                                value = slot1NewPerSpecName,
                                onValueChange = { slot1NewPerSpecName = it },
                                label = { Text("Especialização inicial (para ${perBaixa1!!.nome})") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    } else if (state.usarEspecializacoesDePericia) {
                        ExposedDropdownMenuBox(
                            expanded = slot1SpecPerExp,
                            onExpandedChange = { slot1SpecPerExp = !slot1SpecPerExp }
                        ) {
                            OutlinedTextField(
                                value = slot1SpecPer?.nome ?: "Perícia para a especialização…",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(slot1SpecPerExp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                    .clickable { slot1SpecPerExp = true }
                            )
                            ExposedDropdownMenu(
                                expanded = slot1SpecPerExp,
                                onDismissRequest = { slot1SpecPerExp = false },
                                modifier = Modifier.heightIn(max = 200.dp)
                            ) {
                                listaPericias
                                    .filter { per -> possui(per) || sendoCompradaAgora(per) }
                                    .forEach { per ->
                                        DropdownMenuItem(
                                            text = { Text(per.nome) },
                                            onClick = {
                                                slot1SpecPer = per
                                                slot1SpecPerExp = false
                                            }
                                        )
                                    }
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = slot1SpecName,
                            onValueChange = { slot1SpecName = it },
                            label = { Text("Nome da especialização (1)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // ---------- SLOT 2 ----------
                    Text("Escolha 2")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = !slot2IsSpec, onClick = { slot2IsSpec = false })
                        Text("Perícia")
                        Spacer(Modifier.width(16.dp))
                        if (state.usarEspecializacoesDePericia) {
                            RadioButton(selected = slot2IsSpec, onClick = { slot2IsSpec = true })
                            Text("Especialização")
                        }
                    }

                    if (!slot2IsSpec) {
                        ExposedDropdownMenuBox(
                            expanded = perBaixaExp2,
                            onExpandedChange = { perBaixaExp2 = !perBaixaExp2 }
                        ) {
                            OutlinedTextField(
                                value = perBaixa2?.nome ?: "Perícia 2…",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(perBaixaExp2) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                    .clickable { perBaixaExp2 = true }
                            )
                            ExposedDropdownMenu(
                                expanded = perBaixaExp2,
                                onDismissRequest = { perBaixaExp2 = false },
                                modifier = Modifier.heightIn(max = 200.dp)
                            ) {
                                listaPericias.filter { per -> podeAumentarAbaixo(per) }.forEach { per ->
                                    DropdownMenuItem(
                                        text = { Text(per.nome) },
                                        onClick = {
                                            perBaixa2 = per
                                            if (state.rawTotal(per) == 0) slot2NewPerSpecName = ""
                                            perBaixaExp2 = false
                                        }
                                    )
                                }
                            }
                        }
                        if (deveMostrarSpecNesteSlot(false, perBaixa2)) {
                            Spacer(Modifier.height(6.dp))
                            OutlinedTextField(
                                value = slot2NewPerSpecName,
                                onValueChange = { slot2NewPerSpecName = it },
                                label = { Text("Especialização inicial (para ${perBaixa2!!.nome})") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    } else if (state.usarEspecializacoesDePericia) {
                        ExposedDropdownMenuBox(
                            expanded = slot2SpecPerExp,
                            onExpandedChange = { slot2SpecPerExp = !slot2SpecPerExp }
                        ) {
                            OutlinedTextField(
                                value = slot2SpecPer?.nome ?: "Perícia para a especialização…",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(slot2SpecPerExp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                    .clickable { slot2SpecPerExp = true }
                            )
                            ExposedDropdownMenu(
                                expanded = slot2SpecPerExp,
                                onDismissRequest = { slot2SpecPerExp = false },
                                modifier = Modifier.heightIn(max = 200.dp)
                            ) {
                                listaPericias
                                    .filter { per -> possui(per) || sendoCompradaAgora(per) }
                                    .forEach { per ->
                                        DropdownMenuItem(
                                            text = { Text(per.nome) },
                                            onClick = {
                                                slot2SpecPer = per
                                                slot2SpecPerExp = false
                                            }
                                        )
                                    }
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = slot2SpecName,
                            onValueChange = { slot2SpecName = it },
                            label = { Text("Nome da especialização (2)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                }

                // ── Atributo via XP ────────────────────────────────────────────────
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(if (canBuyAttr) 1f else 0.3f)
                        .clickable(
                            enabled = state.progressosDisponiveis >= costAttr || est.nome == "Lendário"
                        ) {
                            when {
                                est.nome == "Lendário" && state.progressosDisponiveis < costAttr -> {
                                    showSnack("Atributos lendários custam 2 progressos para adquirir")
                                }
                                canBuyAttr -> escolheu = "Atributo"
                            }
                        }
                        .padding(vertical = 4.dp)
                ) {
                    RadioButton(
                        selected = (escolheu == "Atributo"),
                        onClick  = {
                            if (est.nome == "Lendário" && state.progressosDisponiveis < costAttr) {
                                showSnack("Atributos lendários custam 2 progressos para adquirir")
                            } else if (canBuyAttr) {
                                escolheu = "Atributo"
                            }
                        },
                        enabled  = canBuyAttr
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Atributo +1 dado")
                }

                if (escolheu == "Atributo") {
                    ExposedDropdownMenuBox(
                        expanded = attrExp,
                        onExpandedChange = { attrExp = !attrExp }
                    ) {
                        OutlinedTextField(
                            value = attrSelected ?: "Escolha atributo…",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(attrExp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                .clickable { attrExp = true }
                        )
                        ExposedDropdownMenu(
                            expanded = attrExp,
                            onDismissRequest = { attrExp = false },
                            modifier = Modifier.heightIn(max = 200.dp)
                        ) {
                            listaAtributos
                                .filter { nome ->
                                    val current = state.valoresAtributos[nome]!!.intValue
                                    val cap     = state.atributoMaxRaw(nome)
                                    current < cap
                                }
                                .forEach { a ->
                                    DropdownMenuItem(
                                        text = { Text(a) },
                                        onClick = {
                                            attrSelected = a
                                            attrExp = false
                                        }
                                    )
                                }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }

                // ── Vantagem via XP ───────────────────────────────────────────────
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(if (podeComprarVantagem) 1f else 0.3f)
                        .clickable(enabled = podeComprarVantagem) {
                            if (podeComprarVantagem) {
                                escolheu = "Vantagem"
                            }
                        }
                        .padding(vertical = 4.dp)
                ) {
                    RadioButton(
                        selected = (escolheu == "Vantagem"),
                        onClick = {
                            if (podeComprarVantagem) escolheu = "Vantagem"
                        },
                        enabled = podeComprarVantagem
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Comprar Vantagem")
                }

                if (escolheu == "Vantagem") {
                    Text(
                        "Selecione o progresso para adquirir uma Vantagem (custo 1 XP).",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
                    )
                }

                // ── Remover Complicação ───────────────────────────────────────────
                if (state.complicacoesSelecionadas.values.any { it != null }) {
                    RadioButtonRow("Remover Complicação", escolheu == "Complicacao") {
                        escolheu = "Complicacao"
                    }
                    if (escolheu == "Complicacao") {
                        ExposedDropdownMenuBox(
                            expanded = compExp,
                            onExpandedChange = { compExp = !compExp }
                        ) {
                            OutlinedTextField(
                                value         = compSelected?.id ?: "Escolha complicação…",
                                onValueChange = {},
                                readOnly      = true,
                                trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(compExp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                    .clickable { compExp = true }
                            )
                            ExposedDropdownMenu(
                                expanded = compExp,
                                onDismissRequest = { compExp = false },
                                modifier = Modifier.heightIn(max = 200.dp)
                            ) {
                                state.complicacoesSelecionadas
                                    .filterValues { it != null }
                                    .keys
                                    .forEach { c ->
                                        DropdownMenuItem(
                                            text = { Text("${c.id} (${state.complicacoesSelecionadas[c]})") },
                                            onClick = {
                                                compSelected = c
                                                compExp = false
                                            }
                                        )
                                    }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        },
        confirmButton = {
            val context = LocalContext.current
            TextButton(
                onClick = {
                    when (escolheu) {
                        "PericiaAlta" -> {
                            state.spendProgressAcrossStages(1)
                            perAltaSelected?.let { per ->
                                state.baseIncsPorPericia[per] =
                                    state.baseIncsPorPericia.getValue(per) + 1
                                state.spCostStackPorPericia.getValue(per).add(0)
                            }
                        }
                        "PericiasBaixas" -> {
                            state.spendProgressAcrossStages(1)

                            fun especNomePara(per: Pericia): String? {
                                if (!state.usarEspecializacoesDePericia) return null
                                val s1 = if (slot1IsSpec && slot1SpecPer == per) slot1SpecName.trim() else null
                                val s2 = if (slot2IsSpec && slot2SpecPer == per) slot2SpecName.trim() else null
                                val i1 = if (!slot1IsSpec && perBaixa1 == per) slot1NewPerSpecName.trim() else null
                                val i2 = if (!slot2IsSpec && perBaixa2 == per) slot2NewPerSpecName.trim() else null
                                return listOf(i1, i2, s1, s2).firstOrNull { !it.isNullOrEmpty() }
                            }

                            // SLOT 1
                            if (!slot1IsSpec) {
                                perBaixa1?.let { per ->
                                    val eraZero = state.rawTotal(per) == 0
                                    state.baseIncsPorPericia[per] =
                                        state.baseIncsPorPericia.getValue(per) + 1
                                    state.spCostStackPorPericia.getValue(per).add(0)

                                    if (eraZero && state.usarEspecializacoesDePericia) {
                                        especNomePara(per)?.let { nomeEsp ->
                                            val atuais = state.especializacoesPorPericia[per.nome]
                                                ?: EspecializacoesDto(principal = null, lista = emptyList())
                                            val principal = atuais.principal ?: nomeEsp
                                            val lista = (atuais.lista + nomeEsp + principal).distinct()
                                            state.especializacoesPorPericia[per.nome] =
                                                atuais.copy(principal = principal, lista = lista)
                                        }
                                    }
                                }
                            } else if (state.usarEspecializacoesDePericia) {
                                val nome = slot1SpecName.trim()
                                slot1SpecPer?.let { per ->
                                    if (nome.isNotEmpty()) {
                                        val atuais = state.especializacoesPorPericia[per.nome]
                                            ?: EspecializacoesDto(principal = null, lista = emptyList())
                                        val novas = (atuais.lista + nome).distinct()
                                        state.especializacoesPorPericia[per.nome] =
                                            atuais.copy(lista = novas)
                                    }
                                }
                            }

                            // SLOT 2
                            if (!slot2IsSpec) {
                                perBaixa2?.let { per ->
                                    val eraZero = state.rawTotal(per) == 0
                                    state.baseIncsPorPericia[per] =
                                        state.baseIncsPorPericia.getValue(per) + 1
                                    state.spCostStackPorPericia.getValue(per).add(0)

                                    if (eraZero && state.usarEspecializacoesDePericia) {
                                        especNomePara(per)?.let { nomeEsp ->
                                            val atuais = state.especializacoesPorPericia[per.nome]
                                                ?: EspecializacoesDto(principal = null, lista = emptyList())
                                            val principal = atuais.principal ?: nomeEsp
                                            val lista = (atuais.lista + nomeEsp + principal).distinct()
                                            state.especializacoesPorPericia[per.nome] =
                                                atuais.copy(principal = principal, lista = lista)
                                        }
                                    }
                                }
                            } else if (state.usarEspecializacoesDePericia) {
                                val nome = slot2SpecName.trim()
                                slot2SpecPer?.let { per ->
                                    if (nome.isNotEmpty()) {
                                        val atuais = state.especializacoesPorPericia[per.nome]
                                            ?: EspecializacoesDto(principal = null, lista = emptyList())
                                        val novas = (atuais.lista + nome).distinct()
                                        state.especializacoesPorPericia[per.nome] =
                                            atuais.copy(lista = novas)
                                    }
                                }
                            }
                        }
                        "Atributo" -> {
                            if (canBuyAttr) {
                                state.spendProgressAcrossStages(costAttr)
                                attrSelected?.let { nome ->
                                    val prev = state.comprasAttrPorEstagio[est.nome] ?: 0
                                    state.comprasAttrPorEstagio[est.nome] = prev + 1
                                    state.valoresAtributos[nome]!!.intValue += 2
                                }
                                onDismiss()
                            } else {
                                showSnack("Atributos lendários custam 2 progressos para adquirir")
                            }
                            return@TextButton
                        }
                        "Vantagem" -> {
                            if (!podeComprarVantagem) {
                                showSnack("Sem progressos disponíveis neste estágio.")
                                return@TextButton
                            }

                            advSelectedStageIndex = selectedTab
                            showAdvSelection = true
                            return@TextButton
                        }
                        "Complicacao" -> {
                            compSelected?.let { comp ->
                                val nivelAtual = state.complicacoesSelecionadas[comp]!!
                                val isSomenteMaior = comp.severity.lowercase() == "maior"
                                val custo = when {
                                    isSomenteMaior        -> 2
                                    nivelAtual == "Maior" -> 1
                                    nivelAtual == "Menor" -> 1
                                    else                   -> 0
                                }
                                state.spendProgressAcrossStages(custo)
                                if (isSomenteMaior) {
                                    state.complicacoesSelecionadas.remove(comp)
                                } else if (nivelAtual == "Maior") {
                                    state.complicacoesSelecionadas[comp] = "Menor"
                                } else {
                                    state.complicacoesSelecionadas.remove(comp)
                                }
                            }
                        }
                    }
                    onDismiss()
                },
                enabled = when (escolheu) {
                    "PericiasBaixas" -> {
                        if (!state.usarEspecializacoesDePericia) {
                            val slot1Ok = (!slot1IsSpec) && (perBaixa1 != null)
                            val slot2Ok = (!slot2IsSpec) && (perBaixa2 != null)
                            slot1Ok && slot2Ok && state.progressosDisponiveis >= 1
                        } else {
                            fun temSpecPara(per: Pericia?): Boolean {
                                if (per == null) return false
                                val m1 = slot1IsSpec && slot1SpecPer == per && slot1SpecName.trim().isNotEmpty()
                                val m2 = slot2IsSpec && slot2SpecPer == per && slot2SpecName.trim().isNotEmpty()
                                val i1 = (!slot1IsSpec && perBaixa1 == per && state.rawTotal(per) == 0 && slot1NewPerSpecName.trim().isNotEmpty())
                                val i2 = (!slot2IsSpec && perBaixa2 == per && state.rawTotal(per) == 0 && slot2NewPerSpecName.trim().isNotEmpty())
                                return m1 || m2 || i1 || i2
                            }
                            val slot1Ok = if (!slot1IsSpec) {
                                val p1 = perBaixa1
                                p1 != null && (state.rawTotal(p1) > 0 || temSpecPara(p1))
                            } else {
                                slot1SpecPer != null && slot1SpecName.trim().isNotEmpty()
                            }
                            val slot2Ok = if (!slot2IsSpec) {
                                val p2 = perBaixa2
                                p2 != null && (state.rawTotal(p2) > 0 || temSpecPara(p2))
                            } else {
                                slot2SpecPer != null && slot2SpecName.trim().isNotEmpty()
                            }
                            val nova1 = (!slot1IsSpec && perBaixa1 != null && state.rawTotal(perBaixa1!!) == 0)
                            val nova2 = (!slot2IsSpec && perBaixa2 != null && state.rawTotal(perBaixa2!!) == 0)
                            val exigeSpecOk =
                                (!nova1 && !nova2) ||
                                        (nova1 && temSpecPara(perBaixa1)) ||
                                        (nova2 && temSpecPara(perBaixa2)) ||
                                        (nova1 && nova2 && (temSpecPara(perBaixa1) || temSpecPara(perBaixa2)))
                            slot1Ok && slot2Ok && exigeSpecOk && state.progressosDisponiveis >= 1
                        }
                    }
                    "Atributo" -> canBuyAttr
                    "Vantagem" -> podeComprarVantagem
                    else -> true
                }
            ) { Text("Confirmar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )

    if (showAdvSelection) {
        val estIndex = if (advSelectedStageIndex >= 0) advSelectedStageIndex else selectedTab
        val estSel   = listaDeEstagios[estIndex]
        val prevStageSpent = state.stageXpSpent.getValue(estSel.nome)

        val idParaNome = remember { listaVantagens.associate { it.id to it.nome } }

        val candidatas = buildList {
            listaVantagens.forEach { v ->
                val reqEst = v.requisitos.estagio
                val reqIdx = listaDeEstagios.indexOfFirst { it.nome.equals(reqEst, ignoreCase = true) }

                val stageCompatible = (reqIdx == -1 || reqIdx <= estIndex)

                val qtdJaTem = state.vantagensSelecionadas.count { it.nome.equals(v.nome, ignoreCase = true) }
                val limitReached = when (val maxEff = maxEffectiveSelections(v)) {
                    null -> false
                    else -> qtdJaTem >= maxEff
                }

                val requiresChoice = v.requiresChoice
                val validChoicesCount = if (requiresChoice) validChoiceOptionsFor(v).size else 0
                val choiceOk = !requiresChoice || validChoicesCount > 0

                if (stageCompatible && choiceOk && !limitReached) {
                    add(v)
                }
            }
        }

        AlertDialog(
            onDismissRequest = {
                state.stageXpSpent[estSel.nome] = prevStageSpent
                state.progressosDisponiveis =
                    (state.progresso - state.stageXpSpent.values.sum()).coerceAtLeast(0)
                showAdvSelection = false
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxHeight(0.6f)
                        .fillMaxWidth()
                ) {
                    Text(
                        "Selecione uma Vantagem (${estSel.nome})",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyColumn {
                        items(candidatas) { vant ->
                            val atendeRequisitos = state.podeSelecionar(vant)

                            val listaRequisitosTexto = formatarRequisitosParaDialog(vant, idParaNome)

                            val qtdJaTem = state.vantagensSelecionadas.count {
                                it.nome.equals(vant.nome, ignoreCase = true)
                            }

                            val temProgresso = state.progressosDisponiveis >= 1
                            val habilitado = atendeRequisitos && temProgresso

                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = habilitado) {
                                        if (!atendeRequisitos) {
                                            showSnack("Requisitos não atendidos para ${vant.nome}.")
                                            return@clickable
                                        }
                                        if (!temProgresso) {
                                            showSnack("Sem progressos disponíveis.")
                                            return@clickable
                                        }

                                        state.spendProgressAcrossStages(1)

                                        if (vant.requiresChoice || vant.id == "profissional" || vant.id == "especialista") {
                                            pendingAdv = vant
                                            showPendingChoice = true
                                        } else {
                                            state.vantagensSelecionadas += vant
                                            state.checkFreeze()
                                            showAdvSelection = false
                                            onDismiss()
                                        }
                                    }
                                    .padding(vertical = 8.dp, horizontal = 4.dp)
                                    .alpha(if (habilitado) 1f else 0.5f)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = vant.nome,
                                        fontWeight = FontWeight.Bold,
                                        color = if (atendeRequisitos) Color.Unspecified else MaterialTheme.colorScheme.error
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "(${vant.requisitos.estagio.ifBlank { "Novato" }})",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }

                                if (listaRequisitosTexto.isNotEmpty()) {
                                    listaRequisitosTexto.forEach { req ->
                                        Text("• $req", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                if (qtdJaTem > 0) {
                                    Text("Já possui x$qtdJaTem", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                                }

                                if (!atendeRequisitos) {
                                    Text(
                                        "Requisitos não atendidos (Atributo, Perícia ou Vantagem Prévia)",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }

                                HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = {
                    state.stageXpSpent[estSel.nome] = prevStageSpent
                    state.progressosDisponiveis =
                        (state.progresso - state.stageXpSpent.values.sum()).coerceAtLeast(0)
                    showAdvSelection = false
                }) { Text("Cancelar") }
            }
        )
    }

    if (showPendingChoice && pendingAdv != null) {
        state.identifyMaxedTraits()
        val vant = pendingAdv!!
        val key = vant.nome.keyify()

        when (key) {
            "PROFISSIONAL" -> {
                val maxedAttrs = listaAtributos
                    .filter { a -> state.valoresAtributos[a]!!.intValue == state.atributoMaxRaw(a) }
                    .map { mapaAtributosDisplay[it] ?: it }
                val maxedSkills = listaPericias
                    .filter { p -> state.rawTotal(p) == state.periciaCapRaw(p) }
                    .map { it.nome }

                val options = buildList {
                    if (maxedAttrs.isNotEmpty()) {
                        add("── Atributos no teto ──")
                        addAll(maxedAttrs)
                    }
                    if (maxedSkills.isNotEmpty()) {
                        add("── Perícias no teto ──")
                        addAll(maxedSkills)
                    }
                    if (isEmpty()) add("Nenhum traço no teto")
                }

                ChoiceDialog(
                    options = options,
                    onConfirm = onConfirm@{ choice ->
                        if (!state.podeSelecionar(vant)) {

                            showSnack("Você não cumpre os requisitos (ou já atingiu o limite) para ${vant.nome}.")
                            return@onConfirm
                        }

                        state.spendProgressAcrossStages(1)
                        state.vantagensSelecionadas += vant.copy(choice = choice)

                        val choiceKey = choice.uppercase().semAcentos()
                        if (state.valoresAtributos.containsKey(choiceKey)) {
                            state.valoresAtributos[choiceKey]!!.intValue += 2
                        } else {
                            val per = listaPericias.first { it.nome == choice }
                            state.baseIncsPorPericia[per] = state.baseIncsPorPericia.getValue(per) + 1
                            state.spCostStackPorPericia[per]?.add(0)
                        }

                        state.checkFreeze()
                        showPendingChoice = false
                        showAdvSelection = false
                        pendingAdv = null
                        onDismiss()
                    },
                    onDismiss = {
                        showPendingChoice = false
                        pendingAdv = null
                    }
                )
            }

            "ESPECIALISTA" -> {
                val profChoices = state.vantagensSelecionadas
                    .filter { it.nome.keyify() == "PROFISSIONAL" && it.choice != null }
                    .map { it.choice!! }

                if (profChoices.isEmpty()) {
                    LaunchedEffect(vant) {
                        showSnack("Você precisa primeiro de Profissional em algum traço")
                        delay(2_000)
                        showPendingChoice = false
                        pendingAdv = null
                    }
                } else {
                    ChoiceDialog(
                        options = profChoices,
                        onConfirm = onConfirm@{ choice ->
                            advSelectedStageIndex.takeIf { it >= 0 } ?: selectedTab
                            if (!state.podeSelecionar(vant)) {
                                showSnack("Você não cumpre os requisitos (ou já atingiu o limite) para ${vant.nome}.")
                                return@onConfirm
                            }

                            state.spendProgressAcrossStages(1)
                            state.vantagensSelecionadas += vant.copy(choice = choice)

                            val choiceKey = choice.uppercase().semAcentos()
                            if (state.valoresAtributos.containsKey(choiceKey)) {
                                state.valoresAtributos[choiceKey]!!.intValue += 2
                            } else {
                                val per = listaPericias.first { it.nome == choice }
                                state.baseIncsPorPericia[per] = state.baseIncsPorPericia.getValue(per) + 1
                                state.spCostStackPorPericia[per]?.add(0)
                            }

                            state.checkFreeze()
                            showPendingChoice = false
                            showAdvSelection = false
                            pendingAdv = null
                            onDismiss()
                        },
                        onDismiss = {
                            showPendingChoice = false
                            pendingAdv = null
                        }
                    )
                }
            }

            else -> {
                ChoiceDialog(
                    options = vant.choiceOptions,
                    onConfirm = onConfirm@{ choice ->
                        if (!state.podeSelecionar(vant)) {
                            showSnack("Você não cumpre os requisitos (ou já atingiu o limite) para ${vant.nome}.")
                            return@onConfirm
                        }

                        state.spendProgressAcrossStages(1)
                        state.vantagensSelecionadas += vant.copy(choice = choice)
                        state.checkFreeze()
                        showPendingChoice = false
                        showAdvSelection = false
                        pendingAdv = null
                        onDismiss()
                    },
                    onDismiss = {
                        showPendingChoice = false
                        pendingAdv = null
                    }
                )
            }
        }
    }
}

private fun formatarRequisitosParaDialog(v: Vantagem, idParaNome: Map<String, String>): List<String> {
    val partes = mutableListOf<String>()

    if (v.requisitos.atributoMin.isNotEmpty()) {
        partes += "Atributos: " + v.requisitos.atributoMin
            .entries
            .joinToString { (nome, min) -> "$nome d$min" }
    }
    if (v.requisitos.periciaMin.isNotEmpty()) {
        partes += "Perícias: " + v.requisitos.periciaMin
            .entries
            .joinToString { (nome, min) -> "$nome d$min" }
    }
    if (v.requisitos.periciaMinOpcional.isNotEmpty()) {
        partes += "Perícias (opcional): " + v.requisitos.periciaMinOpcional
            .entries
            .joinToString { (nome, min) -> "$nome d$min" }
    }
    if (v.requisitos.vantagensPrevias.isNotEmpty()) {
        val legiveis = v.requisitos.vantagensPrevias.map { prevId ->
            idParaNome[prevId] ?: prevId.replace('_', ' ').uppercase()
        }
        partes += "Vantagens: ${legiveis.joinToString()}"
    }
    if (v.requisitos.exigeCS) {
        partes += "Requer Carta Selvagem"
    }

    return partes
}

private fun maxEffectiveSelections(v: Vantagem): Int? =
    if (v.maxSelections > 0) v.maxSelections else null

private fun validChoiceOptionsFor(v: Vantagem): List<String> = v.choiceOptions