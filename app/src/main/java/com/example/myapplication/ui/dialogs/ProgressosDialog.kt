package com.example.myapplication.ui.dialogs

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
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.CriadorState
import com.example.myapplication.Pericia
import com.example.myapplication.RadioButtonRow
import com.example.myapplication.listaAtributos
import com.example.myapplication.listaDeEstagios
import com.example.myapplication.listaPericias
import com.example.myapplication.listaVantagens
import com.example.myapplication.mapaAtributosDisplay
import com.example.myapplication.model.Complicacao
import com.example.myapplication.model.Vantagem
import com.example.myapplication.periciaStartRaw
import com.example.myapplication.util.keyify
import com.example.myapplication.util.semAcentos
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun ProgressosDialog(
    state: CriadorState,
    onDismiss: () -> Unit
) {
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
    var showTempError by rememberSaveable { mutableStateOf(false) }
    var tempErrorMsg by rememberSaveable { mutableStateOf("") }
    var advSelectedStageIndex by rememberSaveable { mutableIntStateOf(-1) }
    var advPrevStageSpent by rememberSaveable { mutableIntStateOf(0) }

    // configurações gerais de progresso
    val totalProgressLimit = 50
    val stages = listaDeEstagios
    val stageCaps = stages.mapIndexed { idx, st ->
        val prevMax = stages.getOrNull(idx - 1)?.maxProgress ?: 0
        if (idx < stages.lastIndex)
            st.maxProgress - prevMax
        else
            (totalProgressLimit - prevMax).coerceAtLeast(0)
    }
    val currentStageIndex = stageCaps
        .mapIndexed { i, cap -> i to cap }
        .firstOrNull { (i, cap) ->
            state.stageXpSpent.getValue(stages[i].nome) < cap
        }?.first
        ?: stages.lastIndex

    var selectedTab by rememberSaveable { mutableIntStateOf(currentStageIndex) }

    // ── Cálculos para compra de atributos via XP ────────────────────────────────
    val est = stages[selectedTab]
    val prevMaxGlobal = if (selectedTab > 0) stages[selectedTab - 1].maxProgress else 0
    val stageCap = if (selectedTab < stages.lastIndex)
        est.maxProgress - prevMaxGlobal
    else
        (totalProgressLimit - prevMaxGlobal).coerceAtLeast(0)
    val spentHere = state.stageXpSpent.getValue(est.nome)
    val creditsLeft = stageCap - spentHere

    // quantas compras de atributo já foram feitas até o estágio atual (incluindo ele)
    val boughtSoFar = stages
        .take(selectedTab + 1)
        .sumOf { state.comprasAttrPorEstagio.getValue(it.nome) }

    // máximo permitido: uma compra por estágio até o atual, ou ilimitado em Lendário
    val maxAllowed = if (est.nome == "Lendário") Int.MAX_VALUE else (selectedTab + 1)

    // custo de XP por compra: 1 normalmente, 2 em Lendário
    val costAttr = if (est.nome == "Lendário") 2 else 1

    // só pode comprar se houver XP suficiente neste estágio e ainda não tiver alcançado o limite
    val canBuyAttr = creditsLeft >= costAttr &&
            state.progressosDisponiveis >= costAttr &&
            boughtSoFar < maxAllowed

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            TabRow(selectedTabIndex = selectedTab) {
                stages.forEachIndexed { i, st ->
                    val enabled = i == currentStageIndex
                    Tab(
                        selected = (selectedTab == i),
                        onClick = { if (enabled) selectedTab = i },
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
                Spacer(Modifier.height(8.dp))

                Text("XP neste estágio: $spentHere / $stageCap")
                Spacer(Modifier.height(16.dp))

                RadioButtonRow("Vantagem", escolheu == "Vantagem") {
                    escolheu = "Vantagem"
                }
                if (escolheu == "Vantagem") Spacer(Modifier.height(12.dp))

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
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
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

                RadioButtonRow("2× Perícias < Atributo", escolheu == "PericiasBaixas") {
                    escolheu = "PericiasBaixas"
                }
                if (escolheu == "PericiasBaixas") {
                    // 1ª perícia
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
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .clickable { perBaixaExp1 = true }
                        )
                        ExposedDropdownMenu(
                            expanded = perBaixaExp1,
                            onDismissRequest = { perBaixaExp1 = false },
                            modifier = Modifier.heightIn(max = 200.dp)
                        ) {
                            listaPericias
                                .filter { per ->
                                    val startRaw = periciaStartRaw(state.ancestralidade, per)
                                    val baseIncs = state.baseIncsPorPericia.getValue(per)
                                    val extraIncs = if (per == perBaixa2) 2 else 1
                                    val totalIncs = baseIncs + extraIncs
                                    val extraStep = if (startRaw == 0 && totalIncs > 0) 2 else 0
                                    val newRaw = startRaw + 2 * totalIncs + extraStep

                                    state.rawTotal(per) < state.valoresAtributos[per.atributo]!!.intValue &&
                                            newRaw <= state.valoresAtributos[per.atributo]!!.intValue
                                }
                                .forEach { per ->
                                    DropdownMenuItem(
                                        text = { Text(per.nome) },
                                        onClick = {
                                            perBaixa1 = per
                                            perBaixaExp1 = false
                                        }
                                    )
                                }
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    // 2ª perícia
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
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .clickable { perBaixaExp2 = true }
                        )
                        ExposedDropdownMenu(
                            expanded = perBaixaExp2,
                            onDismissRequest = { perBaixaExp2 = false },
                            modifier = Modifier.heightIn(max = 200.dp)
                        ) {
                            listaPericias
                                .filter { per ->
                                    val startRaw = periciaStartRaw(state.ancestralidade, per)
                                    val baseIncs = state.baseIncsPorPericia.getValue(per)
                                    val extraIncs = if (per == perBaixa1) 2 else 1
                                    val totalIncs = baseIncs + extraIncs
                                    val extraStep = if (startRaw == 0 && totalIncs > 0) 2 else 0
                                    val newRaw = startRaw + 2 * totalIncs + extraStep

                                    state.rawTotal(per) < state.valoresAtributos[per.atributo]!!.intValue &&
                                            newRaw <= state.valoresAtributos[per.atributo]!!.intValue
                                }
                                .forEach { per ->
                                    DropdownMenuItem(
                                        text = { Text(per.nome) },
                                        onClick = {
                                            perBaixa2 = per
                                            perBaixaExp2 = false
                                        }
                                    )
                                }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }

                // ── Aumento de Atributo via XP ─────────────────────────────────────────

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
                                    tempErrorMsg = "Atributos lendários custam 2 progressos para adquirir"
                                    showTempError = true
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
                                tempErrorMsg = "Atributos lendários custam 2 progressos para adquirir"
                                showTempError = true
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
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
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

                // ── Remover Complicação ────────────────────────────────────────────────
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
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
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
            TextButton(
                onClick = {
                    when (escolheu) {
                        "Vantagem" -> {
                            advSelectedStageIndex = selectedTab
                            advPrevStageSpent = state.stageXpSpent.getValue(est.nome)
                            showAdvSelection = true
                            return@TextButton
                        }
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
                            listOfNotNull(perBaixa1, perBaixa2).forEach { per ->
                                state.baseIncsPorPericia[per] =
                                    state.baseIncsPorPericia.getValue(per) + 1
                                state.spCostStackPorPericia.getValue(per).add(0)
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
                                tempErrorMsg = "Atributos lendários custam 2 progressos para adquirir"
                                showTempError = true
                            }
                            return@TextButton
                        }
                        "Complicacao" -> {
                            compSelected?.let { comp ->
                                val nivelAtual = state.complicacoesSelecionadas[comp]!!
                                val isSomenteMaior = comp.severity.lowercase() == "maior"
                                val custo = when {
                                isSomenteMaior      -> 2
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
                    "PericiasBaixas" -> perBaixa1 != null && perBaixa2 != null
                    "Atributo"       -> canBuyAttr
                    else             -> true
                }
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )

    if (showAdvSelection) {
        val estSel = stages[advSelectedStageIndex]
        val prevStageSpent = state.stageXpSpent.getValue(estSel.nome)

        AlertDialog(
            onDismissRequest = {
                // desfaz a reserva de XP se o usuário cancelar
                state.stageXpSpent[estSel.nome] = prevStageSpent
                state.progressosDisponiveis =
                    (state.progresso - state.stageXpSpent.values.sum()).coerceAtLeast(0)
                showAdvSelection = false
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxHeight(0.6f)
                        .fillMaxWidth()
                ) {
                    items(listaVantagens.filter { v ->
                        // Garante que não repita vantagens já obtidas
                        if (v in state.vantagensSelecionadas) return@filter false

                        // Se tiver requisito de estágio, mostra só se for até o estágio atual;
                        // se não tiver requisito, mostra também
                        val reqOk = v.requisitos?.estagio?.let { req ->
                            val idxReq = listaDeEstagios.indexOfFirst { it.nome.contains(req, ignoreCase = true) }
                            idxReq == -1 || idxReq <= advSelectedStageIndex
                        } ?: true

                        reqOk
                    }) { vant ->
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    state.spendProgressAcrossStages(1)
                                    state.vantagensSelecionadas += vant
                                    state.checkFreeze()
                                    showAdvSelection = false
                                    onDismiss()
                                }
                                .padding(vertical = 8.dp, horizontal = 4.dp)
                        ) {
                            val pode = state.podeSelecionar(vant)
                            val textoCor = if (pode) colorScheme.onSurface else colorScheme.onSurfaceVariant
                            Text(
                                "${vant.nome} (${vant.requisitos?.estagio ?: "—"})",
                                color = textoCor
                            )
                            if (!pode) {
                                Text(
                                    "Requisitos não atendidos",
                                    fontSize = 10.sp,
                                    color = colorScheme.onSurfaceVariant
                                )
                            }
                            HorizontalDivider()
                        }
                    }

                }
            },
            confirmButton = { /* não precisa de botão Confirmar aqui */ },
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
                    onConfirm = { choice ->
                        state.spendProgressAcrossStages(1)
                        state.vantagensSelecionadas += vant.copy(choice = choice)
                        val choiceKey = choice.uppercase().semAcentos()
                        if (state.valoresAtributos.containsKey(choiceKey)) {
                            state.valoresAtributos[choiceKey]!!.intValue += 2
                        } else {
                            val per = listaPericias.first { it.nome == choice }
                            state.baseIncsPorPericia[per] =
                                state.baseIncsPorPericia.getValue(per) + 1
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
                        showTempError = true
                        tempErrorMsg = "Você precisa primeiro de Profissional em algum traço"
                        delay(2_000)
                        showTempError = false
                        showPendingChoice = false
                        pendingAdv = null
                    }
                } else {
                    ChoiceDialog(
                        options = profChoices,
                        onConfirm = { choice ->
                            state.spendProgressAcrossStages(1)
                            state.vantagensSelecionadas += vant.copy(choice = choice)
                            val choiceKey = choice.uppercase().semAcentos()
                            if (state.valoresAtributos.containsKey(choiceKey)) {
                                state.valoresAtributos[choiceKey]!!.intValue += 2
                            } else {
                                val per = listaPericias.first { it.nome == choice }
                                state.baseIncsPorPericia[per] =
                                    state.baseIncsPorPericia.getValue(per) + 1
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
                    onConfirm = { choice ->
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
