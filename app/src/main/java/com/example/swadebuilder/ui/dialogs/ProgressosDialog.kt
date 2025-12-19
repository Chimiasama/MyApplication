package com.example.swadebuilder.ui.dialogs

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.Pericia
import com.example.swadebuilder.ui.components.RadioButtonRow
import com.example.swadebuilder.listaAtributos
import com.example.swadebuilder.listaDeEstagios
import com.example.swadebuilder.listaPericias
import com.example.swadebuilder.listaVantagens
import com.example.swadebuilder.mapaAtributosDisplay
import com.example.swadebuilder.dynamicStageCaps
import com.example.swadebuilder.model.AdvancementAction
import com.example.swadebuilder.model.Complicacao
import com.example.swadebuilder.model.HindranceChangeType
import com.example.swadebuilder.model.MENSAGEM_EXCLUSIVIDADE_CLASSE
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.periciaStartRaw
import com.example.swadebuilder.stageForSlot
import com.example.swadebuilder.stageIndexForSlot
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.semAcentos
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressosDialog(
    state: CriadorState,
    slotIndex: Int,
    onDismiss: () -> Unit,
    onStartSkillAdvancement: (Int, String) -> Unit,
    onStartAdvantageAdvancement: (Int, String) -> Unit,
    onStartAttributeAdvancement: (Int, String, Boolean) -> Unit,
    onReserveLegendaryAttribute: (Int, String) -> Unit
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
    var compAction by rememberSaveable { mutableStateOf<String?>(null) }
    var compReserveExp by rememberSaveable { mutableStateOf(false) }
    var compReserveSelected by rememberSaveable { mutableStateOf<Complicacao?>(null) }
    var compReservedRemovalExp by rememberSaveable { mutableStateOf(false) }
    var compReservedRemovalSelected by rememberSaveable { mutableStateOf<Complicacao?>(null) }
    var compReduceExp by rememberSaveable { mutableStateOf(false) }
    var compReduceSelected by rememberSaveable { mutableStateOf<Complicacao?>(null) }
    var compMinorExp by rememberSaveable { mutableStateOf(false) }
    var compMinorSelected by rememberSaveable { mutableStateOf<Complicacao?>(null) }
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

    val stages = listaDeEstagios
    val stageIndex = stageIndexForSlot(slotIndex)
    var selectedTab by rememberSaveable { mutableIntStateOf(stageIndex) }

    // ── Cálculos de atributo via XP ────────────────────────────────────────────
    val est = stageForSlot(slotIndex)
    val stageCap = dynamicStageCaps.getOrElse(stageIndex) { 0 }
    val spentHere = state.stageXpSpent.getValue(est.nome)
    val creditsLeft = stageCap - spentHere

    val lendarioIndex = stages.indexOfFirst { it.nome.equals("Lendário", ignoreCase = true) }
        .takeIf { it >= 0 } ?: stages.lastIndex
    val totalAttrPurchases = state.comprasAttrPorEstagio.values.sum()
    val baseAllowance = (stageIndex + 1).coerceAtMost(lendarioIndex)
    val remainingBaseAttrs = (baseAllowance - totalAttrPurchases).coerceAtLeast(0)
    val isLendarioStage = stageIndex == lendarioIndex
    val canUseReservation = isLendarioStage && state.legendaryAttrReservations > 0
    val needsReservation = isLendarioStage && remainingBaseAttrs <= 0 && !canUseReservation
    val canBuyAttr = creditsLeft > 0 && state.progressosDisponiveis >= 1 &&
            (remainingBaseAttrs > 0 || canUseReservation)
    val canReserveLegendary = isLendarioStage &&
            totalAttrPurchases >= lendarioIndex && creditsLeft > 0 &&
            state.progressosDisponiveis >= 1 && state.legendaryAttrReservations == 0

    // ── Requisitos de vantagens (mesma lógica, sem logs) ──────────────────────
    fun strictRequirementsOk(v: Vantagem, estIndex: Int): Boolean {
        val reqEst = v.requisitos.estagio
        if (reqEst.isNotBlank()) {
            val reqIdx = listaDeEstagios.indexOfFirst { it.nome.equals(reqEst, ignoreCase = true) }
            if (reqIdx != -1 && reqIdx > estIndex) return false
        }
        if (v.requisitos.atributoMin.any { (nome, min) ->
                val chaveNorm = nome.uppercase().semAcentos().trim()
                val attrKey = mapaAtributosDisplay.keys.firstOrNull {
                    it.equals(chaveNorm, ignoreCase = true)
                } ?: chaveNorm
                val valor = state.valoresAtributos[attrKey]?.intValue
                valor == null || valor < min
            }
        ) return false
        val perMin = v.requisitos.periciaMin
        if (perMin.isNotEmpty()) {
            if (v.vinculadoPericia) {
                val atendeUma = perMin.any { (perNome, minRaw) ->
                    val per = listaPericias.firstOrNull { it.nome.equals(perNome, ignoreCase = true) }
                    per != null && state.rawTotal(per) >= minRaw
                }
                if (!atendeUma) return false
            } else {
                val falhaAlguma = perMin.any { (perNome, minRaw) ->
                    val per = listaPericias.firstOrNull { it.nome.equals(perNome, ignoreCase = true) }
                        ?: return@any true
                    state.rawTotal(per) < minRaw
                }
                if (falhaAlguma) return false
            }
        }
        val perMinOpc = v.requisitos.periciaMinOpcional
        if (perMinOpc.isNotEmpty()) {
            val ok = perMinOpc.any { (perNome, minRaw) ->
                val per = listaPericias.firstOrNull { it.nome.equals(perNome, ignoreCase = true) }
                per != null && state.rawTotal(per) >= minRaw
            }
            if (!ok) return false
        }
        if (v.requisitos.vantagensPrevias.isNotEmpty()) {
            val tenhoTodas = v.requisitos.vantagensPrevias.all { req ->
                val reqNorm = req.uppercase().semAcentos().trim()
                state.vantagensSelecionadas.any { it.nome.uppercase().semAcentos().trim() == reqNorm }
            }
            if (!tenhoTodas) return false
        }
        if (v.requisitos.exigeCS && !state.cartaSelvagem) return false
        return true
    }

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

    fun bloquearExclusividadeClasse(vant: Vantagem): Boolean {
        return if (state.classeBloqueadaBuscatrilha(vant)) {
            showSnack(MENSAGEM_EXCLUSIVIDADE_CLASSE)
            true
        } else {
            false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                stages.forEachIndexed { i, st ->
                    val enabled = i == stageIndex
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

                RadioButtonRow("Comprar Vantagem", escolheu == "Comprar Vantagem") {
                    escolheu = "Comprar Vantagem"
                }

                RadioButtonRow("Aumentar Perícia", escolheu == "Aumentar Perícia") {
                    escolheu = "Aumentar Perícia"
                }

                // ── Atributo via XP ────────────────────────────────────────────────
                val attrLabel = when {
                    isLendarioStage && remainingBaseAttrs <= 0 && canUseReservation ->
                        "Aumentar atributo (usar reserva lendária)"

                    else -> "Aumentar atributo"
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(if (canBuyAttr) 1f else 0.3f)
                        .clickable(
                            enabled = canBuyAttr
                        ) {
                            when {
                                canBuyAttr -> escolheu = "Atributo"
                                needsReservation -> showSnack("Reserve um atributo lendário primeiro.")
                                else -> showSnack("Sem créditos suficientes para atributo.")
                            }
                        }
                        .padding(vertical = 4.dp)
                ) {
                    RadioButton(
                        selected = (escolheu == "Atributo"),
                        onClick  = {
                            if (canBuyAttr) {
                                escolheu = "Atributo"
                            }
                        },
                        enabled  = canBuyAttr
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(attrLabel)
                }

                if (canReserveLegendary) {
                    RadioButtonRow(
                        "Reservar atributo lendário (custa 1 XP)",
                        escolheu == "ReservaLendario"
                    ) {
                        escolheu = "ReservaLendario"
                    }
                    Text(
                        "Reservas ativas: ${state.legendaryAttrReservations}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Spacer(Modifier.height(8.dp))
                }

                // ── Remover / Reduzir Complicação ─────────────────────────────────
                if (state.complicacoesSelecionadas.values.any { it != null }) {
                    // Limpa reservas que não fazem mais sentido (complicação removida ou reduzida).
                    val reservasInvalidas = state.reservasComplicacaoMaior.keys.filterNot { id ->
                        state.complicacoesSelecionadas.any { (comp, nivel) ->
                            comp.id == id && nivel == "Maior"
                        }
                    }
                    reservasInvalidas.forEach { state.reservasComplicacaoMaior.remove(it) }

                    val complicacoesMaiores = state.complicacoesSelecionadas
                        .filterValues { it == "Maior" }
                        .keys
                    val complicacoesReservaveis = complicacoesMaiores.filter { comp ->
                        !state.reservasComplicacaoMaior.containsKey(comp.id)
                    }
                    val complicacoesReservadas = complicacoesMaiores.filter { comp ->
                        state.reservasComplicacaoMaior.containsKey(comp.id)
                    }
                    val complicacoesRedutiveis = complicacoesReservaveis.filter { comp ->
                        comp.severity.contains("menor", ignoreCase = true)
                    }
                    val complicacoesMenores = state.complicacoesSelecionadas
                        .filterValues { it == "Menor" }
                        .keys

                    if (complicacoesReservaveis.isNotEmpty() ||
                        complicacoesReservadas.isNotEmpty() ||
                        complicacoesRedutiveis.isNotEmpty() ||
                        complicacoesMenores.isNotEmpty()
                    ) {
                        RadioButtonRow("Gerenciar Complicação", escolheu == "Complicacao") {
                            escolheu = "Complicacao"
                        }

                        if (escolheu == "Complicacao") {
                            Spacer(Modifier.height(4.dp))

                            if (complicacoesReservadas.isNotEmpty()) {
                                RadioButtonRow(
                                    "Remover complicação Maior (usar reserva)",
                                    compAction == "RemoverReservada"
                                ) {
                                    compAction = "RemoverReservada"
                                }
                                if (compAction == "RemoverReservada") {
                                    ExposedDropdownMenuBox(
                                        expanded = compReservedRemovalExp,
                                        onExpandedChange = { compReservedRemovalExp = !compReservedRemovalExp }
                                    ) {
                                        OutlinedTextField(
                                            value = compReservedRemovalSelected?.name
                                                ?: "Escolha complicação reservada…",
                                            onValueChange = {},
                                            readOnly = true,
                                            trailingIcon = {
                                                ExposedDropdownMenuDefaults.TrailingIcon(compReservedRemovalExp)
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                                .clickable { compReservedRemovalExp = true }
                                        )
                                        ExposedDropdownMenu(
                                            expanded = compReservedRemovalExp,
                                            onDismissRequest = { compReservedRemovalExp = false },
                                            modifier = Modifier.heightIn(max = 200.dp)
                                        ) {
                                            complicacoesReservadas.forEach { c ->
                                                DropdownMenuItem(
                                                    text = { Text("${c.name} (Maior)") },
                                                    onClick = {
                                                        compReservedRemovalSelected = c
                                                        compReservedRemovalExp = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                    Spacer(Modifier.height(12.dp))
                                }
                            }

                            if (complicacoesReservaveis.isNotEmpty()) {
                                RadioButtonRow(
                                    "Reservar remoção de complicação Maior",
                                    compAction == "ReservarMaior"
                                ) { compAction = "ReservarMaior" }
                                if (compAction == "ReservarMaior") {
                                    ExposedDropdownMenuBox(
                                        expanded = compReserveExp,
                                        onExpandedChange = { compReserveExp = !compReserveExp }
                                    ) {
                                        OutlinedTextField(
                                            value = compReserveSelected?.name ?: "Escolha complicação Maior…",
                                            onValueChange = {},
                                            readOnly = true,
                                            trailingIcon = {
                                                ExposedDropdownMenuDefaults.TrailingIcon(compReserveExp)
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                                .clickable { compReserveExp = true }
                                        )
                                        ExposedDropdownMenu(
                                            expanded = compReserveExp,
                                            onDismissRequest = { compReserveExp = false },
                                            modifier = Modifier.heightIn(max = 200.dp)
                                        ) {
                                            complicacoesReservaveis.forEach { c ->
                                                DropdownMenuItem(
                                                    text = { Text("${c.name} (Maior)") },
                                                    onClick = {
                                                        compReserveSelected = c
                                                        compReserveExp = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                    Spacer(Modifier.height(12.dp))
                                }
                            }

                            if (complicacoesRedutiveis.isNotEmpty()) {
                                RadioButtonRow(
                                    "Reduzir complicação (Maior → Menor)",
                                    compAction == "Reduzir"
                                ) { compAction = "Reduzir" }
                                if (compAction == "Reduzir") {
                                    ExposedDropdownMenuBox(
                                        expanded = compReduceExp,
                                        onExpandedChange = { compReduceExp = !compReduceExp }
                                    ) {
                                        OutlinedTextField(
                                            value = compReduceSelected?.name ?: "Escolha complicação…",
                                            onValueChange = {},
                                            readOnly = true,
                                            trailingIcon = {
                                                ExposedDropdownMenuDefaults.TrailingIcon(compReduceExp)
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                                .clickable { compReduceExp = true }
                                        )
                                        ExposedDropdownMenu(
                                            expanded = compReduceExp,
                                            onDismissRequest = { compReduceExp = false },
                                            modifier = Modifier.heightIn(max = 200.dp)
                                        ) {
                                            complicacoesRedutiveis.forEach { c ->
                                                DropdownMenuItem(
                                                    text = { Text("${c.name} (Maior)") },
                                                    onClick = {
                                                        compReduceSelected = c
                                                        compReduceExp = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                    Spacer(Modifier.height(12.dp))
                                }
                            }

                            if (complicacoesMenores.isNotEmpty()) {
                                RadioButtonRow(
                                    "Remover complicação Menor",
                                    compAction == "RemoverMenor"
                                ) { compAction = "RemoverMenor" }
                                if (compAction == "RemoverMenor") {
                                    ExposedDropdownMenuBox(
                                        expanded = compMinorExp,
                                        onExpandedChange = { compMinorExp = !compMinorExp }
                                    ) {
                                        OutlinedTextField(
                                            value = compMinorSelected?.name ?: "Escolha complicação…",
                                            onValueChange = {},
                                            readOnly = true,
                                            trailingIcon = {
                                                ExposedDropdownMenuDefaults.TrailingIcon(compMinorExp)
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                                .clickable { compMinorExp = true }
                                        )
                                        ExposedDropdownMenu(
                                            expanded = compMinorExp,
                                            onDismissRequest = { compMinorExp = false },
                                            modifier = Modifier.heightIn(max = 200.dp)
                                        ) {
                                            complicacoesMenores.forEach { c ->
                                                DropdownMenuItem(
                                                    text = { Text("${c.name} (Menor)") },
                                                    onClick = {
                                                        compMinorSelected = c
                                                        compMinorExp = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                    Spacer(Modifier.height(12.dp))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    when (escolheu) {
                        "Comprar Vantagem" -> {
                            onStartAdvantageAdvancement(slotIndex, est.nome)
                            onDismiss()
                        }
                        "Aumentar Perícia" -> {
                            onStartSkillAdvancement(slotIndex, est.nome)
                            onDismiss()
                        }
                        "Atributo" -> {
                            if (canBuyAttr) {
                                val consumeReservation = remainingBaseAttrs <= 0
                                onStartAttributeAdvancement(slotIndex, est.nome, consumeReservation)
                                onDismiss()
                            } else if (needsReservation) {
                                showSnack("Reserve um atributo lendário primeiro.")
                                return@TextButton
                            }
                        }
                        "ReservaLendario" -> {
                            if (canReserveLegendary) {
                                onReserveLegendaryAttribute(slotIndex, est.nome)
                                onDismiss()
                            } else {
                                showSnack("Você precisa liberar reservas lendárias primeiro.")
                                return@TextButton
                            }
                        }
                        "Complicacao" -> {
                            fun ensureProgressAvailable(): Boolean {
                                if (state.progressosDisponiveis < 1) {
                                    showSnack("Você não tem progressos suficientes.")
                                    return false
                                }
                                return true
                            }

                            when (compAction) {
                                "ReservarMaior" -> {
                                    val comp = compReserveSelected
                                    if (comp == null) {
                                        showSnack("Escolha qual complicação Maior reservar.")
                                        return@TextButton
                                    }
                                    if (!ensureProgressAvailable()) return@TextButton

                                    val nivelAtual = state.complicacoesSelecionadas[comp]
                                    if (nivelAtual != "Maior") {
                                        showSnack("Apenas complicações Maiores podem ser reservadas.")
                                        return@TextButton
                                    }

                                    state.progresso += 1
                                    state.spendProgressAtStage(est.nome, 1)
                                    state.reservasComplicacaoMaior[comp.id] = true
                                    state.advancementHistory.add(
                                        AdvancementAction.RemoveHindrance(
                                            hindranceId = comp.id,
                                            changeType = HindranceChangeType.RESERVATION,
                                            previousLevel = nivelAtual,
                                            stageName = est.nome,
                                            progressCost = 1
                                        )
                                    )
                                }

                                "RemoverReservada" -> {
                                    val comp = compReservedRemovalSelected
                                    if (comp == null) {
                                        showSnack("Escolha qual complicação remover.")
                                        return@TextButton
                                    }
                                    if (!state.reservasComplicacaoMaior.containsKey(comp.id)) {
                                        showSnack("Use primeiro a reserva dessa complicação Maior.")
                                        return@TextButton
                                    }
                                    if (!ensureProgressAvailable()) return@TextButton

                                    val nivelAnterior = state.complicacoesSelecionadas[comp]
                                    state.progresso += 1
                                    state.spendProgressAtStage(est.nome, 1)
                                    state.reservasComplicacaoMaior.remove(comp.id)
                                    state.complicacoesSelecionadas.remove(comp)
                                    state.advancementHistory.add(
                                        AdvancementAction.RemoveHindrance(
                                            hindranceId = comp.id,
                                            changeType = HindranceChangeType.REMOVE,
                                            previousLevel = nivelAnterior,
                                            usedReservation = true,
                                            stageName = est.nome,
                                            progressCost = 1
                                        )
                                    )
                                }

                                "Reduzir" -> {
                                    val comp = compReduceSelected
                                    if (comp == null) {
                                        showSnack("Escolha qual complicação reduzir.")
                                        return@TextButton
                                    }
                                    if (!ensureProgressAvailable()) return@TextButton

                                    val nivelAnterior = state.complicacoesSelecionadas[comp]
                                    if (nivelAnterior != "Maior") {
                                        showSnack("Essa complicação não está como Maior.")
                                        return@TextButton
                                    }

                                    state.progresso += 1
                                    state.spendProgressAtStage(est.nome, 1)
                                    state.reservasComplicacaoMaior.remove(comp.id)
                                    state.complicacoesSelecionadas[comp] = "Menor"
                                    state.advancementHistory.add(
                                        AdvancementAction.RemoveHindrance(
                                            hindranceId = comp.id,
                                            changeType = HindranceChangeType.REDUCE_TO_MINOR,
                                            previousLevel = nivelAnterior,
                                            stageName = est.nome,
                                            progressCost = 1
                                        )
                                    )
                                }

                                "RemoverMenor" -> {
                                    val comp = compMinorSelected
                                    if (comp == null) {
                                        showSnack("Escolha qual complicação remover.")
                                        return@TextButton
                                    }
                                    if (!ensureProgressAvailable()) return@TextButton

                                    val nivelAnterior = state.complicacoesSelecionadas[comp]
                                    state.progresso += 1
                                    state.spendProgressAtStage(est.nome, 1)
                                    state.reservasComplicacaoMaior.remove(comp.id)
                                    state.complicacoesSelecionadas.remove(comp)
                                    state.advancementHistory.add(
                                        AdvancementAction.RemoveHindrance(
                                            hindranceId = comp.id,
                                            changeType = HindranceChangeType.REMOVE,
                                            previousLevel = nivelAnterior,
                                            stageName = est.nome,
                                            progressCost = 1
                                        )
                                    )
                                }

                                else -> {
                                    showSnack("Escolha como gastar o XP na complicação.")
                                    return@TextButton
                                }
                            }

                            state.xpSlots[slotIndex] = true
                            state.recomputeAvailableProgress()
                            onDismiss()
                        }
                    }
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
                    "ReservaLendario" -> canReserveLegendary
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

        val candidatas = buildList {
            listaVantagens.forEach { v ->
                val podeAgora = state.podeSelecionar(v)
                val strictOk  = strictRequirementsOk(v, estIndex)
                val qtdJaTem = state.vantagensSelecionadas.count { it.nome.equals(v.nome, ignoreCase = true) }
                val repeticaoOk = when (val maxEff = maxEffectiveSelections(v)) { null -> true; else -> qtdJaTem < maxEff }
                val stageOk = v.requisitos.estagio.let { req ->
                    val reqIdx = listaDeEstagios.indexOfFirst { it.nome.equals(req, ignoreCase = true) }
                    reqIdx == -1 || reqIdx <= estIndex
                }
                val requiresChoice = v.requiresChoice
                val validChoicesCount = if (requiresChoice) validChoiceOptionsFor(v).size else 0
                val choiceOk = !requiresChoice || validChoicesCount > 0
                val temProgresso = state.progressosDisponiveis >= 1
                val deveListar = podeAgora && strictOk && repeticaoOk && stageOk && choiceOk && temProgresso
                if (deveListar) add(v)
            }
        }

        AlertDialog(
            onDismissRequest = {
                state.stageXpSpent[estSel.nome] = prevStageSpent
                state.recomputeAvailableProgress()
                showAdvSelection = false
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxHeight(0.6f)
                        .fillMaxWidth()
                ) {
                    LazyColumn {
                        items(candidatas) { vant ->
                            val qtdJaTem = state.vantagensSelecionadas.count {
                                it.nome.equals(vant.nome, ignoreCase = true)
                            }
                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val qtdJaTemClick = state.vantagensSelecionadas.count {
                                            it.nome.equals(vant.nome, ignoreCase = true)
                                        }
                                        when (val maxEff = maxEffectiveSelections(vant)) {
                                            null -> {}
                                            else -> if (qtdJaTemClick >= maxEff) {
                                                showSnack("Você já atingiu o limite para ${vant.nome}.")
                                                return@clickable
                                            }
                                        }
                                        if (!state.podeSelecionar(vant) || !strictRequirementsOk(vant, estIndex)) {
                                            showSnack("Você não cumpre os requisitos (ou já atingiu o limite) para ${vant.nome}.")
                                            return@clickable
                                        }
                                        if (bloquearExclusividadeClasse(vant)) {
                                            return@clickable
                                        }
                                        if (state.progressosDisponiveis < 1) {
                                            showSnack("Você não tem progressos suficientes.")
                                            return@clickable
                                        }
                        state.spendProgressAtStage(estSel.nome, 1)
                        state.vantagensSelecionadas += vant
                        state.advancementHistory.add(
                            AdvancementAction.SpendOnAdvantage(
                                advantageId = vant.id,
                                stageName = estSel.nome
                            )
                        )
                                        state.xpSlots[slotIndex] = true
                                        state.recomputeAvailableProgress()
                                        state.checkFreeze()
                                        showAdvSelection = false
                                        onDismiss()
                                    }
                                    .padding(vertical = 8.dp, horizontal = 4.dp)
                            ) {
                                Text("${vant.nome} (${vant.requisitos.estagio.ifBlank { "—" }})")
                                if (qtdJaTem > 0) {
                                    Text("Já possui x$qtdJaTem", fontSize = 10.sp, color = Color.Gray)
                                }
                                HorizontalDivider()
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = {
                    state.stageXpSpent[estSel.nome] = prevStageSpent
                    state.recomputeAvailableProgress()
                    showAdvSelection = false
                }) { Text("Cancelar") }
            }
        )
    }

    if (showPendingChoice && pendingAdv != null) {
        state.identifyMaxedTraits()
        val vant = pendingAdv!!
        val key = vant.nome.keyify()
        val estSel = listaDeEstagios[advSelectedStageIndex.takeIf { it >= 0 } ?: selectedTab]

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
                        val estIndexFinal = advSelectedStageIndex.takeIf { it >= 0 } ?: selectedTab
                        if (!state.podeSelecionar(vant) || !strictRequirementsOk(vant, estIndexFinal)) {
                            showSnack("Você não cumpre os requisitos (ou já atingiu o limite) para ${vant.nome}.")
                            return@onConfirm
                        }
                        if (bloquearExclusividadeClasse(vant)) {
                            return@onConfirm
                        }

                        state.spendProgressAtStage(estSel.nome, 1)
                        state.vantagensSelecionadas += vant.copy(choice = choice)
                        state.advancementHistory.add(
                            AdvancementAction.SpendOnAdvantage(
                                advantageId = vant.id,
                                stageName = estSel.nome
                            )
                        )
                        state.xpSlots[slotIndex] = true
                        state.recomputeAvailableProgress()

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
                            val estIndexFinal = advSelectedStageIndex.takeIf { it >= 0 } ?: selectedTab
                            if (!state.podeSelecionar(vant) || !strictRequirementsOk(vant, estIndexFinal)) {
                                showSnack("Você não cumpre os requisitos (ou já atingiu o limite) para ${vant.nome}.")
                                return@onConfirm
                            }
                            if (bloquearExclusividadeClasse(vant)) {
                                return@onConfirm
                            }

                            state.spendProgressAtStage(estSel.nome, 1)
                            state.vantagensSelecionadas += vant.copy(choice = choice)
                            state.advancementHistory.add(
                                AdvancementAction.SpendOnAdvantage(
                                    advantageId = vant.id,
                                    stageName = estSel.nome
                                )
                            )
                            state.xpSlots[slotIndex] = true
                            state.recomputeAvailableProgress()

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
                        val estIndexFinal = advSelectedStageIndex.takeIf { it >= 0 } ?: selectedTab
                        if (!state.podeSelecionar(vant) || !strictRequirementsOk(vant, estIndexFinal)) {
                            showSnack("Você não cumpre os requisitos (ou já atingiu o limite) para ${vant.nome}.")
                            return@onConfirm
                        }
                        if (bloquearExclusividadeClasse(vant)) {
                            return@onConfirm
                        }

                        state.spendProgressAtStage(estSel.nome, 1)
                        state.vantagensSelecionadas += vant.copy(choice = choice)
                        state.advancementHistory.add(
                            AdvancementAction.SpendOnAdvantage(
                                advantageId = vant.id,
                                stageName = estSel.nome
                            )
                        )
                        state.xpSlots[slotIndex] = true
                        state.recomputeAvailableProgress()
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

// Helpers usados acima
private fun maxEffectiveSelections(v: Vantagem): Int? =
    if (v.maxSelections > 0) v.maxSelections else null

private fun validChoiceOptionsFor(v: Vantagem): List<String> = v.choiceOptions
