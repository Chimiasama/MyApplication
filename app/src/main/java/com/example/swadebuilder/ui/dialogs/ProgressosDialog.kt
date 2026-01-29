package com.example.swadebuilder.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.Pericia
import com.example.swadebuilder.R
import com.example.swadebuilder.dynamicStageCaps
import com.example.swadebuilder.listaAtributos
import com.example.swadebuilder.listaDeEstagios
import com.example.swadebuilder.listaVantagens
import com.example.swadebuilder.mapaAtributosDisplay
import com.example.swadebuilder.model.AdvancementAction
import com.example.swadebuilder.model.Complicacao
import com.example.swadebuilder.model.HindranceChangeType
import com.example.swadebuilder.model.MENSAGEM_EXCLUSIVIDADE_CLASSE
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.model.classeExclusivaBloqueada
import com.example.swadebuilder.periciaStartRaw
import com.example.swadebuilder.stageForSlot
import com.example.swadebuilder.stageIndexForSlot
import com.example.swadebuilder.ui.components.ExpandableSearchFilter
import com.example.swadebuilder.ui.components.RadioButtonRow
import com.example.swadebuilder.ui.theme.LocalAppThemeData
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.semAcentos
import com.example.swadebuilder.util.toSentenceCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProgressosDialog(
    state: CriadorState,
    slotIndex: Int,
    onDismiss: () -> Unit,
    onStartSkillAdvancement: (Int, String) -> Unit,
    onStartAttributeAdvancement: (Int, String, Boolean) -> Unit,
    onReserveLegendaryAttribute: (Int, String) -> Unit,
    onPurchaseAdvantage: (Int, String, Vantagem) -> Unit,
    onPurchaseAttribute: (Int, String, String, Boolean) -> Unit,
    onIncreaseSkill: (Pericia) -> Unit,
    onFinishSkillAdvancement: () -> Unit
) {
    // Snackbar para mensagens temporárias (substitui showTempError/tempErrorMsg)
    val snackHost = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    fun showSnack(msg: String) = scope.launch { snackHost.showSnackbar(message = msg) }

    var escolheu by rememberSaveable { mutableStateOf<String?>(null) }
    var compAction by rememberSaveable { mutableStateOf<String?>(null) }
    var compReserveExp by rememberSaveable { mutableStateOf(false) }
    var compReserveSelected by rememberSaveable { mutableStateOf<Complicacao?>(null) }
    var compReservedRemovalExp by rememberSaveable { mutableStateOf(false) }
    var compReservedRemovalSelected by rememberSaveable { mutableStateOf<Complicacao?>(null) }
    var compReduceExp by rememberSaveable { mutableStateOf(false) }
    var compReduceSelected by rememberSaveable { mutableStateOf<Complicacao?>(null) }
    var compMinorExp by rememberSaveable { mutableStateOf(false) }
    var compMinorSelected by rememberSaveable { mutableStateOf<Complicacao?>(null) }

    // NEW STATES FOR SELECTION
    var showAdvSelection by rememberSaveable { mutableStateOf(false) }
    var showAttrSelection by rememberSaveable { mutableStateOf(false) }
    var showSkillSelection by rememberSaveable { mutableStateOf(false) }
    var skillSearchQuery by rememberSaveable { mutableStateOf("") }

    var pendingAdv by rememberSaveable { mutableStateOf<Vantagem?>(null) }
    var showPendingChoice by rememberSaveable { mutableStateOf(false) }
    var advSelectedStageIndex by rememberSaveable { mutableIntStateOf(-1) }
    val detalhesExpandidos = remember { mutableStateMapOf<String, Boolean>() }

    val stages = listaDeEstagios
    val stageIndex = stageIndexForSlot(slotIndex)
    var selectedTab by rememberSaveable { mutableIntStateOf(stageIndex) }

    val idParaNome = remember(listaVantagens) {
        listaVantagens.associate { it.id to it.nomeExibicao.toSentenceCase() }
    }

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
            (remainingBaseAttrs > 0 || canUseReservation || state.modoMonstroAtivo)
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
                    val per = state.periciasComIdiomas().firstOrNull { it.nome.equals(perNome, ignoreCase = true) }
                    per != null && state.rawTotal(per) >= minRaw
                }
                if (!atendeUma) return false
            } else {
                val falhaAlguma = perMin.any { (perNome, minRaw) ->
                    val per = state.periciasComIdiomas().firstOrNull { it.nome.equals(perNome, ignoreCase = true) }
                        ?: return@any true
                    state.rawTotal(per) < minRaw
                }
                if (falhaAlguma) return false
            }
        }
        val perMinOpc = v.requisitos.periciaMinOpcional
        if (perMinOpc.isNotEmpty()) {
            val ok = perMinOpc.any { (perNome, minRaw) ->
                val per = state.periciasComIdiomas().firstOrNull { it.nome.equals(perNome, ignoreCase = true) }
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

    fun bloquearExclusividadeClasse(vant: Vantagem): Boolean {
        return if (state.vantagensSelecionadas.classeExclusivaBloqueada(vant)) {
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
                            showAdvSelection = true
                        }
                        "Aumentar Perícia" -> {
                            onStartSkillAdvancement(slotIndex, est.nome)
                            showSkillSelection = true
                        }
                        "Atributo" -> {
                            if (canBuyAttr) {
                                showAttrSelection = true
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
                    "Atributo" -> canBuyAttr
                    "ReservaLendario" -> canReserveLegendary
                    else -> true
                }
            ) { Text("Confirmar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )

    if (showAttrSelection) {
        val consumeReservation = remainingBaseAttrs <= 0
        AlertDialog(
            onDismissRequest = { showAttrSelection = false },
            title = { Text("Selecione o Atributo") },
            text = {
                LazyColumn {
                    items(listaAtributos) { attrKey ->
                        val label = mapaAtributosDisplay[attrKey] ?: attrKey
                        val currentVal = state.valoresAtributos[attrKey]?.intValue ?: 4

                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onPurchaseAttribute(slotIndex, est.nome, attrKey, consumeReservation)
                                }
                                .padding(vertical = 12.dp)
                        ) {
                            Text("$label (d$currentVal → d${if (currentVal < 12) currentVal + 2 else currentVal + 1})")
                        }
                        HorizontalDivider()
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAttrSelection = false }) { Text("Cancelar") }
            }
        )
    }

    if (showSkillSelection) {
        val spRemaining = state.spFromProgress

        AlertDialog(
            onDismissRequest = {
                // If user dismisses, we finish/commit whatever they bought so far
                // or cancel? Usually commit if they bought something.
                if (spRemaining < 2) {
                    onFinishSkillAdvancement()
                } else {
                    // Cancel logic logic should be in viewmodel, but we can just close
                    // Actually, if we close without finishing, state remains in "progress".
                    // Safest to call finish which cleans up.
                    onFinishSkillAdvancement()
                }
            },
            title = {
                Column {
                    Text("Aumentar Perícias")
                    Text("Pontos disponíveis: $spRemaining", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            },
            text = {
                Column {
                    ExpandableSearchFilter(
                        query = skillSearchQuery,
                        onQueryChange = { skillSearchQuery = it },
                        isExpanded = true,
                        onExpandedChange = {},
                        placeholder = "Buscar perícia..."
                    )
                    Spacer(Modifier.height(8.dp))

                    val filteredSkills = state.periciasComIdiomas().filter {
                        it.nome.contains(skillSearchQuery, ignoreCase = true)
                    }.sortedBy { it.nome }

                    LazyColumn(modifier = Modifier.fillMaxHeight(0.6f)) {
                        items(filteredSkills) { per ->
                            val current = state.rawTotal(per)
                            val attrVal = state.valoresAtributos[per.atributo]?.intValue ?: 4
                            val cost = if (current >= attrVal) 2 else 1
                            val canBuy = spRemaining >= cost && current < 12 // Hard cap at d12 usually, but allowing legendaries is complex.
                            // Standard limit is often d12. Let's assume standard rules.

                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .alpha(if (canBuy) 1f else 0.5f)
                                    .clickable(enabled = canBuy) {
                                        onIncreaseSkill(per)
                                        if (state.spFromProgress <= 0) {
                                            onFinishSkillAdvancement()
                                        }
                                    }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(per.nome)
                                    Text("Atual: d$current | Custo: $cost SP", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            HorizontalDivider()
                        }
                    }
                }
            },
            confirmButton = {
                // Only show confirm if they are done or want to stop early
                TextButton(onClick = { onFinishSkillAdvancement() }) {
                    Text("Concluir")
                }
            }
        )
    }

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
                            DialogVantagemItem(
                                vant = vant,
                                state = state,
                                locked = false,
                                allowLongTexts = true,
                                showOfficialNames = state.modoOficialAtivo,
                                idParaNome = idParaNome,
                                detalhesExpandidos = detalhesExpandidos,
                                onSelect = {
                                    val qtdJaTemClick = state.vantagensSelecionadas.count {
                                        it.nome.equals(vant.nome, ignoreCase = true)
                                    }
                                    when (val maxEff = maxEffectiveSelections(vant)) {
                                        null -> {}
                                        else -> if (qtdJaTemClick >= maxEff) {
                                            showSnack("Você já atingiu o limite para ${vant.nome}.")
                                            return@DialogVantagemItem
                                        }
                                    }
                                    if (!state.podeSelecionar(vant) || !strictRequirementsOk(vant, estIndex)) {
                                        showSnack("Você não cumpre os requisitos (ou já atingiu o limite) para ${vant.nome}.")
                                        return@DialogVantagemItem
                                    }
                                    if (bloquearExclusividadeClasse(vant)) {
                                        return@DialogVantagemItem
                                    }
                                    if (state.progressosDisponiveis < 1) {
                                        showSnack("Você não tem progressos suficientes.")
                                        return@DialogVantagemItem
                                    }

                                    // Use local logic for choices or pass to main callback
                                    if (vant.requiresChoice) {
                                        pendingAdv = vant
                                        advSelectedStageIndex = estIndex
                                        showPendingChoice = true
                                    } else {
                                        onPurchaseAdvantage(slotIndex, estSel.nome, vant)
                                    }
                                },
                                onError = { showSnack(it) }
                            )
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
                val maxedSkills = state.periciasComIdiomas()
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

                        onPurchaseAdvantage(slotIndex, estSel.nome, vant.copy(choice = choice))

                        showPendingChoice = false
                        showAdvSelection = false
                        pendingAdv = null
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

                            onPurchaseAdvantage(slotIndex, estSel.nome, vant.copy(choice = choice))

                            showPendingChoice = false
                            showAdvSelection = false
                            pendingAdv = null
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

                        onPurchaseAdvantage(slotIndex, estSel.nome, vant.copy(choice = choice))

                        showPendingChoice = false
                        showAdvSelection = false
                        pendingAdv = null
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

// Extracted reusable item component
@Composable
private fun DialogVantagemItem(
    vant: Vantagem,
    state: CriadorState,
    locked: Boolean,
    allowLongTexts: Boolean,
    showOfficialNames: Boolean,
    idParaNome: Map<String, String>,
    detalhesExpandidos: MutableMap<String, Boolean>,
    onSelect: () -> Unit,
    onError: (String) -> Unit
) {
    val themeData = LocalAppThemeData.current

    val reqList = buildList {
        listaDeEstagios.firstOrNull {
            it.nome.equals(vant.requisitos.estagio, true)
        }?.let { add(it.nome) }

        vant.requisitos.atributoMin.forEach { (a, m) ->
            add("$a d$m")
        }
        vant.requisitos.periciaMin.forEach { (p, m) ->
            add("$p d$m")
        }

        if (vant.requisitos.periciaMinOpcional.isNotEmpty()) {
            add(
                vant.requisitos.periciaMinOpcional.entries.joinToString(" ou ") {
                    "${it.key} d${it.value}"
                }
            )
        }

        vant.requisitos.vantagensPrevias.forEach { prevId ->
            val legivel = idParaNome[prevId]
                ?: prevId.replace('_', ' ').replace('-', ' ').toSentenceCase()
            add("Pré-requisito: $legivel")
        }

        if (vant.requisitos.observacoes.isNotBlank()) {
            add(vant.requisitos.observacoes)
        }
        if (vant.nome.trim().removeSuffix(":").keyify() == "profissional") {
            add(
                "Traço no teto máximo: escolha entre " +
                        state.maxedTraits.joinToString()
            )
        }
    }

    val jaTem = state.vantagensSelecionadas.any { it.id == vant.id }
    val requisitosOk = state.podeSelecionar(vant)
    val bloqueioClasse = if (state.vantagensSelecionadas.classeExclusivaBloqueada(vant)) {
        "Requer Multiclasse"
    } else null

    val statusText = when {
        jaTem -> "Já selecionada"
        bloqueioClasse != null -> bloqueioClasse
        requisitosOk -> "Requisitos OK"
        else -> "Requisitos pendentes"
    }
    val statusColor = when {
        jaTem -> MaterialTheme.colorScheme.tertiary
        bloqueioClasse != null -> MaterialTheme.colorScheme.error
        requisitosOk -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.error
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(enabled = !locked) {
                if (!locked) {
                    val conflitoMsg = state.mensagemConflitoParaVantagem(vant)

                    val isPathfinderFree = state.pathfinderSlotAvailable && state.isPathfinderEligible(vant)

                    when {
                        // Check class blocking specifically for error message
                        state.vantagensSelecionadas.classeExclusivaBloqueada(vant) -> onError("Requer a vantagem Multiclasse para possuir duas classes")
                        conflitoMsg != null -> onError(conflitoMsg)
                        !state.podeSelecionar(vant) -> onError("Faltam requisitos para '${vant.nomeExibicao}'")
                        else -> onSelect()
                    }
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = when {
                jaTem -> MaterialTheme.colorScheme.tertiaryContainer
                requisitosOk && bloqueioClasse == null -> MaterialTheme.colorScheme.surfaceVariant
                else -> MaterialTheme.colorScheme.errorContainer
            }
        ),
        border = themeData.cardBorderColor?.let { androidx.compose.foundation.BorderStroke(1.dp, it) }
    ) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        if (showOfficialNames && !vant.originalName.isNullOrBlank()) vant.originalName!!.toSentenceCase() else vant.nomeExibicao.toSentenceCase(),
                        style = MaterialTheme.typography.titleSmall
                    )
                }

                Text(
                    statusText,
                    style = MaterialTheme.typography.labelMedium,
                    color = statusColor
                )
            }

            Spacer(Modifier.size(6.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (vant.descricao.isNotBlank() && vant.vinculadoPericia) {
                    AssistChip(
                        onClick = {},
                        label = { Text("Opções especiais") }
                    )
                }
            }

            if (reqList.isNotEmpty()) {
                Spacer(Modifier.size(4.dp))
                Text(
                    "Requisitos:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                reqList.forEach { req ->
                    Text(
                        "• $req",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (requisitosOk || jaTem) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            val canShowDetails = allowLongTexts && vant.descricao.isNotBlank()
            if (canShowDetails) {
                Spacer(Modifier.size(8.dp))
                TextButton(
                    onClick = {
                        val current = detalhesExpandidos[vant.id] ?: false
                        detalhesExpandidos[vant.id] = !current
                    },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        if (detalhesExpandidos[vant.id] == true) "Ocultar detalhes" else "Ver detalhes",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                AnimatedVisibility(visible = detalhesExpandidos[vant.id] == true) {
                    Text(
                        text = if (showOfficialNames && !vant.originalDescription.isNullOrBlank()) vant.originalDescription.trim() else vant.descricao.trim(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
