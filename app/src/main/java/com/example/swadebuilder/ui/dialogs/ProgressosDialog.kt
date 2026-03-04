package com.example.swadebuilder.ui.dialogs

import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.model.AdvancementAction
import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.Complicacao
import com.example.swadebuilder.model.CriadorViewModel
import com.example.swadebuilder.model.Estagio
import com.example.swadebuilder.model.HindranceChangeType
import com.example.swadebuilder.model.MENSAGEM_EXCLUSIVIDADE_CLASSE
import com.example.swadebuilder.model.Pericia
import com.example.swadebuilder.model.RequirementValidator
import com.example.swadebuilder.model.VantFilter
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.model.canonicalOriginKey
import com.example.swadebuilder.model.classeExclusivaBloqueada
import com.example.swadebuilder.model.dynamicStageCaps
import com.example.swadebuilder.model.getActiveOrigins
import com.example.swadebuilder.model.isVantagemVisible
import com.example.swadebuilder.stageForSlot
import com.example.swadebuilder.stageIndexForSlot
import com.example.swadebuilder.toDiceString
import com.example.swadebuilder.ui.components.ExpandableSearchFilter
import com.example.swadebuilder.ui.components.RadioButtonRow
import com.example.swadebuilder.ui.sections.PoderesSection
import com.example.swadebuilder.ui.sections.VantFilterDialog
import com.example.swadebuilder.ui.theme.LocalAppThemeData
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.semAcentos
import com.example.swadebuilder.util.toSentenceCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProgressosDialog(
    state: CriadorState,
    viewModel: CriadorViewModel,
    onShowMessage: (String) -> Unit,
    slotIndex: Int,
    allAdvantages: List<Vantagem>,
    listaAtributos: List<String>,
    listaPericias: List<Pericia>,
    mapaAtributosDisplay: Map<String, String>,
    mapaPericias: Map<String, Pericia>,
    allEstagios: List<Estagio>,
    onDismiss: () -> Unit
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
    var showPowerSelection by rememberSaveable { mutableStateOf(false) }

    // FILTER STATES
    var advSearchQuery by rememberSaveable { mutableStateOf("") }
    var advSelectedCategories by rememberSaveable { mutableStateOf<Set<Categoria>>(emptySet()) }
    var advFilter by remember { mutableStateOf(VantFilter()) }
    var showFilterDialog by rememberSaveable { mutableStateOf(false) }
    var isSearchExpanded by rememberSaveable { mutableStateOf(false) }

    var pendingAdv by rememberSaveable { mutableStateOf<Vantagem?>(null) }
    var showPendingChoice by rememberSaveable { mutableStateOf(false) }
    var showMysticPowersSelection by rememberSaveable { mutableStateOf(false) }
    var pendingMysticPowersAdv by remember { mutableStateOf<Vantagem?>(null) }
    var advSelectedStageIndex by rememberSaveable { mutableIntStateOf(-1) }
    val detalhesExpandidos = remember { mutableStateMapOf<String, Boolean>() }

    val stages = allEstagios
    val stageIndex = stageIndexForSlot(slotIndex)
    var selectedTab by rememberSaveable { mutableIntStateOf(stageIndex) }

    val idParaNome = remember(allAdvantages) {
        allAdvantages.associate { it.id to it.nomeExibicao.toSentenceCase() }
    }

    // ── Cálculos de atributo via XP ────────────────────────────────────────────
    val est = allEstagios.firstOrNull { it.nome == state.stageNameForCurrentAdvancement } ?: stageForSlot(slotIndex)
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
    val hasReservedProgress = state.xpSlots.getOrNull(slotIndex) == true

    val canBuyAttr = creditsLeft > 0 && hasReservedProgress &&
            (remainingBaseAttrs > 0 || canUseReservation || state.modoMonstroAtivo)
    val canReserveLegendary = isLendarioStage &&
            totalAttrPurchases >= lendarioIndex && creditsLeft > 0 &&
            hasReservedProgress && state.legendaryAttrReservations == 0

    // ── Requisitos de vantagens (mesma lógica, sem logs) ──────────────────────
    fun strictRequirementsOk(v: Vantagem, estIndex: Int): Boolean {
        val reqEst = v.requisitos.estagio
        if (reqEst.isNotBlank()) {
            val reqIdx = stages.indexOfFirst { it.nome.equals(reqEst, ignoreCase = true) }
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
        // Reaproveita a validação central para manter comportamento idêntico
        // (inclui regras especiais como Ameaçador por complicações OR).
        if (!RequirementValidator.canSelect(v, state)) return false
        if (v.requisitos.exigeCS && !state.cartaSelvagem) return false
        return true
    }

    fun bloquearExclusividadeClasse(vant: Vantagem): Boolean {
        return if (state.vantagensSelecionadas.classeExclusivaBloqueada(vant, state.compendioPathfinderAtivo)) {
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
                            viewModel.startSkillAdvancement(slotIndex, est.nome)
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
                                viewModel.reserveLegendaryAttribute(slotIndex, est.nome)
                                onDismiss()
                            } else {
                                showSnack("Você precisa liberar reservas lendárias primeiro.")
                                return@TextButton
                            }
                        }
                        "Complicacao" -> {
                            fun ensureProgressAvailable(): Boolean {
                                if (!hasReservedProgress) {
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
                            state.stageNameForCurrentAdvancement = null
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
        AlertDialog(
            onDismissRequest = { showAttrSelection = false },
            title = { Text("Selecione o Atributo") },
            text = {
                LazyColumn {
                    items(listaAtributos) { attrKey ->
                        val label = mapaAtributosDisplay[attrKey] ?: attrKey
                        val currentVal = state.valoresAtributos[attrKey]?.intValue ?: 4
                        val maxVal = state.atributoMaxRaw(attrKey)

                        // Logic for Monster Mode: Physical attributes are not limited by rank
                        val isFree = state.isAttributeFreeForMonster(attrKey)
                        val limitReached = remainingBaseAttrs <= 0
                        val allowedByRule = !limitReached || isFree || canUseReservation

                        val canIncrease = (currentVal < maxVal) && allowedByRule

                        Row(
                            Modifier
                                .fillMaxWidth()
                                .alpha(if (canIncrease) 1f else 0.5f)
                                .clickable(enabled = canIncrease) {
                                    // Only consume reservation if we are restricted (limit reached & not free)
                                    val shouldConsumeReservation = limitReached && !isFree
                                    viewModel.startAttributeAdvancement(slotIndex, est.nome, shouldConsumeReservation)
                                    viewModel.increaseAttributeForAdvancement(attrKey)
                                    viewModel.finishAttributeAdvancement()
                                    onDismiss()
                                }
                                .padding(vertical = 12.dp)
                        ) {
                            val nextVal = if (currentVal < 12) currentVal + 2 else currentVal + 1
                            val currentValStr = currentVal.toDiceString()
                            val nextValStr = nextVal.toDiceString()

                            val text = when {
                                currentVal >= maxVal -> "$label ($currentValStr) - Máximo"
                                !allowedByRule -> "$label ($currentValStr) - Limite de Estágio"
                                else -> "$label ($currentValStr → $nextValStr)"
                            }
                            Text(text)
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
        val canCancelSkillAdvancement = state.skillsForCurrentAdvancement.isEmpty()
        var showSkillNoteDialog by rememberSaveable { mutableStateOf(false) }
        var skillNoteText by rememberSaveable { mutableStateOf("") }
        var skillNoteTarget by remember { mutableStateOf<Pericia?>(null) }

        AlertDialog(
            onDismissRequest = {
                if (canCancelSkillAdvancement) {
                    viewModel.cancelAdvancementInProgress()
                    onDismiss()
                } else if (spRemaining == 0) {
                    viewModel.finishSkillAdvancement()
                    onDismiss()
                } else {
                    showSnack("Use os pontos restantes ou desfaça os aumentos antes de cancelar.")
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
                    val activeOrigins = state.getActiveOrigins()
                    // Combine active/available skills with existing char skills to ensure new purchases are possible
                    val allSkills = remember(state.compendioFantasiaAtivo, state.compendioHorrorAtivo, state.compendioPathfinderAtivo) {
                        val available = listaPericias.filter { per ->
                            val origem = per.origem?.ifBlank { "BASICO" }
                            origem in activeOrigins
                        }
                        val existing = state.periciasComIdiomas()
                        // Prioritize existing instances to ensure correct point lookup
                        (existing + available).distinctBy { it.nome.keyify() }.sortedBy { it.nome }
                    }

                    LazyColumn(modifier = Modifier.fillMaxHeight(0.6f)) {
                        items(allSkills) { per ->
                            val current = state.rawTotal(per)
                            val attrVal = state.valoresAtributos[per.atributo]?.intValue ?: 4
                            val cost = if (current >= attrVal) 2 else 1
                            val canBuy = spRemaining >= cost && current < 12
                            val wasIncreased = state.skillsForCurrentAdvancement.contains(per.nome)

                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .alpha(if (canBuy || wasIncreased) 1f else 0.5f)
                                    .clickable(enabled = canBuy) {
                                        viewModel.increaseSkillForAdvancement(per)
                                    }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(per.nome)
                                    val nota = state.notasPericia[per.nome]
                                    if (!nota.isNullOrBlank()) {
                                        Text(
                                            text = "($nota)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.tertiary
                                        )
                                    }
                                    Text("Atual: ${current.toDiceString()} | Custo: $cost SP", style = MaterialTheme.typography.bodySmall)
                                }

                                val canEditNote = current > 0 && (
                                    state.isIdiomaPericia(per) ||
                                        state.isJutsuPericia(per) ||
                                        state.usarEspecializacoesDePericia
                                    )

                                if (canEditNote) {
                                    IconButton(
                                        onClick = {
                                            skillNoteTarget = per
                                            skillNoteText = state.notasPericia[per.nome] ?: ""
                                            showSkillNoteDialog = true
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Editar descrição da perícia",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                if (wasIncreased) {
                                    TextButton(
                                        onClick = { viewModel.decreaseSkillForAdvancement(per) }
                                    ) {
                                        Text("-", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            HorizontalDivider()
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.finishSkillAdvancement()
                        onDismiss()
                    },
                    enabled = spRemaining == 0
                ) {
                    Text("Concluir")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.cancelAdvancementInProgress()
                        onDismiss()
                    },
                    enabled = canCancelSkillAdvancement
                ) {
                    Text("Cancelar")
                }
            }
        )

        if (showSkillNoteDialog && skillNoteTarget != null) {
            val isIdiomaOuJutsu = state.isIdiomaPericia(skillNoteTarget!!) || state.isJutsuPericia(skillNoteTarget!!)
            AlertDialog(
                onDismissRequest = {
                    showSkillNoteDialog = false
                    skillNoteTarget = null
                },
                title = {
                    Text(if (isIdiomaOuJutsu) "Editar idioma" else "Editar especialização")
                },
                text = {
                    Column {
                        Text("Perícia: ${skillNoteTarget!!.nome}")
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = skillNoteText,
                            onValueChange = { skillNoteText = it },
                            label = {
                                Text(
                                    if (isIdiomaOuJutsu) {
                                        "Ex: Élfico, Espada, Desarmado..."
                                    } else {
                                        "Ex: Pistolas, Investigação Forense..."
                                    }
                                )
                            },
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val per = skillNoteTarget!!
                            val txt = skillNoteText.trim()
                            if (txt.isNotBlank()) {
                                state.notasPericia[per.nome] = txt
                            } else if (isIdiomaOuJutsu) {
                                state.notasPericia[per.nome] = state.idiomaDefaultLabel(per)
                            } else {
                                state.notasPericia.remove(per.nome)
                            }
                            showSkillNoteDialog = false
                            skillNoteTarget = null
                        }
                    ) { Text("Salvar") }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showSkillNoteDialog = false
                            skillNoteTarget = null
                        }
                    ) { Text("Cancelar") }
                }
            )
        }
    }

    if (showAdvSelection) {
        val estIndex = if (advSelectedStageIndex >= 0) advSelectedStageIndex else selectedTab
        val estSel   = stages[estIndex]
        val prevStageSpent = state.stageXpSpent.getValue(estSel.nome)
        val hasProfissional = state.vantagensSelecionadas.any { it.id == "profissional" }

        val candidatas = remember(allAdvantages, advSearchQuery, advSelectedCategories, advFilter, estIndex, hasProfissional) {
            // First, filter visibility (which accounts for module rules)
            val visibleRaw = allAdvantages.filter { state.isVantagemVisible(it, state.permiteMultiAntecedenteArcano) }
            val visible = normalizePathfinderArcaneEntriesForProgress(visibleRaw, allAdvantages, state)

            visible.filter { vant ->
                // Filters
                if (advSelectedCategories.isNotEmpty() && vant.categoria !in advSelectedCategories) return@filter false
                if (advSearchQuery.isNotBlank()) {
                    val q = advSearchQuery.semAcentos().lowercase()
                    val n = vant.nomeExibicao.semAcentos().lowercase()
                    val d = vant.descricao.semAcentos().lowercase()
                    val original = vant.nome.semAcentos().lowercase()
                    if (!n.contains(q) && !d.contains(q) && !original.contains(q)) return@filter false
                }

                // Advanced Filter
                if (!advFilter.isEmpty()) {
                    val vantOrigem = canonicalOriginKey(vant.origem)
                    if (advFilter.origens.isNotEmpty() && vantOrigem !in advFilter.origens.map(::canonicalOriginKey).toSet()) return@filter false
                    if (advFilter.estagios.isNotEmpty() && vant.requisitos.estagio !in advFilter.estagios) return@filter false
                    if (advFilter.atributos.isNotEmpty() && advFilter.atributos.intersect(vant.requisitos.atributoMin.keys).isEmpty()) return@filter false
                    if (advFilter.pericias.isNotEmpty()) {
                        val reqMin = vant.requisitos.periciaMin.keys
                        val reqOpt = vant.requisitos.periciaMinOpcional.keys
                        val vinc = if (vant.vinculadoPericia) vant.choiceOptions else emptyList()
                        if (advFilter.pericias.intersect(reqMin + reqOpt + vinc).isEmpty()) return@filter false
                    }
                }

                if (vant.id == "especialista" && !hasProfissional) return@filter false

                val podeAgora = state.podeSelecionar(vant)
                val strictOk  = strictRequirementsOk(vant, estIndex)
                val qtdJaTem = state.vantagensSelecionadas.count { it.nome.equals(vant.nome, ignoreCase = true) }
                val repeticaoOk = when (val maxEff = maxEffectiveSelections(vant)) { null -> true; else -> qtdJaTem < maxEff }
                val stageOk = vant.requisitos.estagio.let { req ->
                    val reqIdx = stages.indexOfFirst { it.nome.equals(req, ignoreCase = true) }
                    reqIdx == -1 || reqIdx <= estIndex
                }
                val requiresChoice = vant.requiresChoice
                val validChoicesCount = if (requiresChoice) validChoiceOptionsFor(vant, state).size else 0
                val choiceOk = !requiresChoice || validChoicesCount > 0
                val temProgresso = hasReservedProgress

                podeAgora && strictOk && repeticaoOk && stageOk && choiceOk && temProgresso
            }
        }

        val candidatasPorCategoria = remember(candidatas) {
            candidatas.groupBy { it.categoria }
        }
        val expandedAdvCategories = remember { mutableStateMapOf<Categoria, Boolean>() }
        LaunchedEffect(candidatasPorCategoria.keys) {
            candidatasPorCategoria.keys.forEach { cat ->
                if (expandedAdvCategories[cat] == null) {
                    expandedAdvCategories[cat] = true
                }
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
                        .fillMaxHeight(0.8f)
                        .fillMaxWidth()
                ) {
                    // Search & Filters Header
                    ExpandableSearchFilter(
                        query = advSearchQuery,
                        onQueryChange = { advSearchQuery = it },
                        isExpanded = isSearchExpanded,
                        onExpandedChange = { isSearchExpanded = it },
                        onClear = {
                            advSearchQuery = ""
                            advSelectedCategories = emptySet()
                            advFilter = VantFilter()
                        },
                        placeholder = "Pesquisar Vantagens..."
                    ) {
                         Spacer(Modifier.height(8.dp))

                        // Calculate active categories based on visible items - moved outside LazyRow
                        val activeCategories = remember(allAdvantages) {
                            normalizePathfinderArcaneEntriesForProgress(
                                allAdvantages.filter { state.isVantagemVisible(it, state.permiteMultiAntecedenteArcano) },
                                allAdvantages,
                                state
                            )
                                .map { it.categoria }
                                .toSet()
                        }

                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item(key = "advanced_filters") {
                                FilterChip(
                                    selected = !advFilter.isEmpty(),
                                    onClick = { showFilterDialog = true },
                                    label = { Text("Filtros Avançados${if(!advFilter.isEmpty()) " (!)" else ""}") }
                                )
                            }

                            items(
                                items = Categoria.entries.toTypedArray(),
                                key = { it.name }
                            ) { cat ->
                                if (state.modoSupers && cat == Categoria.PODER) return@items
                                if (cat !in activeCategories) return@items

                                FilterChip(
                                    selected = cat in advSelectedCategories,
                                    onClick = {
                                        advSelectedCategories = if (cat in advSelectedCategories) advSelectedCategories - cat else advSelectedCategories + cat
                                    },
                                    label = {
                                        val label = if (cat.name == "LIDERANCA") "Liderança" else cat.name.toSentenceCase()
                                        Text(label)
                                    }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    LazyColumn {
                        Categoria.entries.forEach { cat ->
                            val listaCategoria = candidatasPorCategoria[cat] ?: return@forEach
                            val expanded = expandedAdvCategories[cat] ?: true

                            item(key = "adv_cat_header_${cat.name}") {
                                TextButton(
                                    onClick = { expandedAdvCategories[cat] = !expanded },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (cat.name == "LIDERANCA") "Liderança" else cat.name.toSentenceCase(),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Icon(
                                            imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                            contentDescription = if (expanded) "Recolher ${cat.name}" else "Expandir ${cat.name}"
                                        )
                                    }
                                }
                            }

                            if (expanded) {
                                items(
                                    items = listaCategoria,
                                    key = { vant -> "${vant.id}_${vant.nome}_${vant.origem}" }
                                ) { vant ->
                                    DialogVantagemItem(
                                        vant = vant,
                                        state = state,
                                        locked = false,
                                        allowLongTexts = true,
                                        showOfficialNames = state.modoOficialAtivo,
                                        idParaNome = idParaNome,
                                        detalhesExpandidos = detalhesExpandidos,
                                        stages = stages,
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
                                            if (!hasReservedProgress) {
                                                showSnack("Você não tem progressos suficientes.")
                                                return@DialogVantagemItem
                                            }

                                            if (vant.id == "poderes_misticos" || vant.id == "poderes_misticos_anjo" || vant.id == "poderes_misticos_demonio" || vant.id == "poderes_misticos_mumia") {
                                                pendingMysticPowersAdv = vant
                                                advSelectedStageIndex = estIndex
                                                showMysticPowersSelection = true
                                            } else if (vant.requiresChoice || vant.vinculadoPericia || vant.id == "arma_predileta_aprimorada") {
                                                pendingAdv = vant
                                                advSelectedStageIndex = estIndex
                                                showPendingChoice = true
                                            } else {
                                                viewModel.startAdvantageAdvancement(slotIndex, estSel.nome)
                                                viewModel.selectAdvantageForAdvancement(vant)

                                                val requiresPowerFlow =
                                                    vant.id == "novos_poderes" || vant.id.startsWith("antecedente_arcano")

                                                if (requiresPowerFlow && (state.arcanoCompraPendente() || state.mostrandoPoderesProgresso)) {
                                                    showAdvSelection = false
                                                    showPowerSelection = true
                                                } else {
                                                    viewModel.finishAdvantageAdvancement()
                                                    onDismiss()
                                                }
                                            }
                                        },
                                        onError = { showSnack(it) }
                                    )
                                }
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

    if (showFilterDialog) {
        val allEstagiosNames = stages.map { it.nome }
        val allAtributos = mapaAtributosDisplay.values.toList()
        val allPericias = listaPericias.map { it.nome }

        VantFilterDialog(
            allEstagios = allEstagiosNames,
            allAtributos = allAtributos,
            allPericias = allPericias,
            current = advFilter,
            onChange = { advFilter = it },
            onDismiss = { showFilterDialog = false }
        )
    }

    if (showPowerSelection) {
        AlertDialog(
            onDismissRequest = {
                viewModel.cancelAdvancementInProgress()
                showPowerSelection = false
                onDismiss()
            },
            title = { Text("Selecione os Poderes") },
            text = {
                Column(Modifier.fillMaxHeight(0.8f)) {
                    PoderesSection(state = state, arcanoInfoMap = state.arcanoInfo, onShowMessage = onShowMessage)
                }
            },
            confirmButton = {
                 Button(onClick = {
                     viewModel.finishAdvantageAdvancement()
                     showPowerSelection = false
                     onDismiss()
                 }) {
                     Text("Confirmar")
                 }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.cancelAdvancementInProgress()
                    showPowerSelection = false
                    onDismiss()
                }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showMysticPowersSelection && pendingMysticPowersAdv != null) {
        val vant = pendingMysticPowersAdv!!
        val isAnjoMysticPowers = vant.id == "poderes_misticos_anjo"
        val isDemonioMysticPowers = vant.id == "poderes_misticos_demonio"
        val isMumiaMysticPowers = vant.id == "poderes_misticos_mumia"
        val options = if (isAnjoMysticPowers) {
            listOf(
                "Arauto" to "Adivinhação, Aumentar/Reduzir Característica, Cura, Vidência",
                "Morte" to "Aumentar/Reduzir Característica (si mesmo), Deflexão, Ferir, Proteção (si mesmo)"
            )
        } else {
            if (isDemonioMysticPowers) {
                listOf(
                    "Invocador" to "Conjurar aliado, conjurar demônio, proteção, zumbi",
                    "Possessor" to "Aumentar/Reduzir Característica, fantoche, maldição, pesadelos",
                    "Sedutor" to "Aumentar/Reduzir Característica, disfarce, empatia, leitura de mente",
                    "Trapaceiro" to "Disfarce, deflexão (si mesmo), horrores ilusórios, medo"
                )
            } else if (isMumiaMysticPowers) {
                listOf(
                    "Arquiteto" to "Barreira, Detectar/Ocultar Arcano, Telecinese, Trancar/Destrancar",
                    "Régio" to "Explosão, Rajada, Rancor"
                )
            } else {
                listOf(
                    "Bárbaro" to "Força d8+",
                    "Guerreiro" to "Lutar d8+",
                    "Ladrão" to "Ladinagem d8+",
                    "Monge" to "Atletismo d8+",
                    "Paladino" to "Espírito d8+",
                    "Patrulheiro" to "Sobrevivência d8+"
                )
            }
        }
        var selectedClass by rememberSaveable { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = {
                showMysticPowersSelection = false
                pendingMysticPowersAdv = null
            },
            title = { Text(if (isAnjoMysticPowers) "Poderes Místicos (Anjo): Escolha o Pacote" else if (isDemonioMysticPowers) "Poderes Místicos (Demônio): Escolha o Pacote" else if (isMumiaMysticPowers) "Poderes Místicos (Múmia): Escolha o Pacote" else "Poderes Místicos: Escolha a Classe") },
            text = {
                Column {
                    Text(if (isAnjoMysticPowers) "Escolha o pacote de poderes para o anjo:" else if (isDemonioMysticPowers) "Escolha o pacote de poderes para o demônio:" else if (isMumiaMysticPowers) "Escolha o pacote de poderes para a múmia:" else "Escolha a classe para definir seus poderes e requisitos:")
                    Spacer(Modifier.size(8.dp))
                    options.forEach { (opcao, requisito) ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { selectedClass = opcao }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (selectedClass == opcao),
                                onClick = { selectedClass = opcao }
                            )
                            Spacer(Modifier.size(8.dp))
                            Column {
                                Text(opcao, fontWeight = FontWeight.Bold)
                                Text(requisito, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = (selectedClass != null),
                    onClick = {
                        val choice = selectedClass!!
                        // Validate specific requirements
                        var reqMet = false
                        var failMsg = ""

                        // Helper to find the active instance of a skill to ensure we check the correct point pool
                        fun getSkillTotal(nameKey: String): Int {
                            val activePer = state.periciasComIdiomas().firstOrNull { it.nome.keyify() == nameKey }
                                ?: mapaPericias[nameKey]
                            return activePer?.let { state.rawTotal(it) } ?: 0
                        }

                        when (choice) {
                            "Bárbaro" -> {
                                val str = state.valoresAtributos["FORCA"]?.intValue ?: 4
                                if (str >= 8) reqMet = true
                                else failMsg = "Requer Força d8+"
                            }
                            "Guerreiro" -> {
                                val lut = getSkillTotal("LUTAR")
                                if (lut >= 8) reqMet = true
                                else failMsg = "Requer Lutar d8+"
                            }
                            "Ladrão" -> {
                                val lad = getSkillTotal("LADINAGEM")
                                if (lad >= 8) reqMet = true
                                else failMsg = "Requer Ladinagem d8+"
                            }
                            "Monge" -> {
                                val atl = getSkillTotal("ATLETISMO")
                                if (atl >= 8) reqMet = true
                                else failMsg = "Requer Atletismo d8+"
                            }
                            "Paladino" -> {
                                val esp = state.valoresAtributos["ESPIRITO"]?.intValue ?: 4
                                if (esp >= 8) reqMet = true
                                else failMsg = "Requer Espírito d8+"
                            }
                            "Patrulheiro" -> {
                                val sob = getSkillTotal("SOBREVIVENCIA")
                                if (sob >= 8) reqMet = true
                                else failMsg = "Requer Sobrevivência d8+"
                            }
                            else -> reqMet = isAnjoMysticPowers || isDemonioMysticPowers || isMumiaMysticPowers
                        }

                        if (!reqMet) {
                            showSnack(failMsg)
                            return@TextButton
                        }

                        val estIndexFinal = advSelectedStageIndex.takeIf { it >= 0 } ?: selectedTab
                        val estSel = stages[estIndexFinal]

                        if (!state.podeSelecionar(vant) || !strictRequirementsOk(vant, estIndexFinal)) {
                            showSnack("Você não cumpre os requisitos gerais para ${vant.nome}.")
                            return@TextButton
                        }

                        val vantChoice = vant.copy(choice = choice)
                        viewModel.startAdvantageAdvancement(slotIndex, estSel.nome)
                        viewModel.selectAdvantageForAdvancement(vantChoice)

                        // Místico doesn't open power selection, so just finish
                        viewModel.finishAdvantageAdvancement()
                        showMysticPowersSelection = false
                        showAdvSelection = false
                        pendingMysticPowersAdv = null
                        onDismiss()
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showMysticPowersSelection = false
                        pendingMysticPowersAdv = null
                    }
                ) { Text("Cancelar") }
            }
        )
    }

    if (showPendingChoice && pendingAdv != null) {
        state.identifyMaxedTraits()
        val vant = pendingAdv!!
        val key = vant.nome.keyify()
        val estSel = stages[advSelectedStageIndex.takeIf { it >= 0 } ?: selectedTab]

        fun finishWithChoice(choice: String) {
            val estIndexFinal = advSelectedStageIndex.takeIf { it >= 0 } ?: selectedTab
            if (!state.podeSelecionar(vant) || !strictRequirementsOk(vant, estIndexFinal)) {
                showSnack("Você não cumpre os requisitos (ou já atingiu o limite) para ${vant.nome}.")
                return
            }
            if (bloquearExclusividadeClasse(vant)) {
                return
            }

            val vantChoice = vant.copy(choice = choice)
            viewModel.startAdvantageAdvancement(slotIndex, estSel.nome)
            viewModel.selectAdvantageForAdvancement(vantChoice)

            val requiresPowerFlow =
                vantChoice.id == "novos_poderes" || vantChoice.id.startsWith("antecedente_arcano")

            if (requiresPowerFlow && (state.arcanoCompraPendente() || state.mostrandoPoderesProgresso)) {
                showPendingChoice = false
                showAdvSelection = false
                pendingAdv = null
                showPowerSelection = true
            } else {
                viewModel.finishAdvantageAdvancement()
                showPendingChoice = false
                showAdvSelection = false
                pendingAdv = null
                onDismiss()
            }
        }

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
                    onConfirm = { finishWithChoice(it) },
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
                        onConfirm = { finishWithChoice(it) },
                        onDismiss = {
                            showPendingChoice = false
                            pendingAdv = null
                        }
                    )
                }
            }

            "ANTECEDENTE ARCANO" -> {
                if (state.compendioPathfinderAtivo) {
                    val opcoesPathfinder = remember(state.compendioPathfinderAtivo) {
                        val map = mapOf(
                            "Magia" to "antecedente_arcano_magia_pf",
                            "Milagres" to "antecedente_arcano_milagres_pf"
                        )
                        map.mapNotNull { (label, id) ->
                            val v = allAdvantages.firstOrNull { it.id == id }
                            if (v != null) label to v else null
                        }
                    }

                    if (opcoesPathfinder.isNotEmpty()) {
                        ChoiceDialog(
                            options = opcoesPathfinder.map { it.first },
                            onConfirm = { choiceLabel ->
                                val specificEdge = opcoesPathfinder.firstOrNull { it.first == choiceLabel }?.second
                                if (specificEdge != null) {
                                    val estIndexFinal = advSelectedStageIndex.takeIf { it >= 0 } ?: selectedTab
                                    val estSel = stages[estIndexFinal]

                                    if (!state.podeSelecionar(specificEdge) || !strictRequirementsOk(specificEdge, estIndexFinal)) {
                                        showSnack("Você não cumpre os requisitos para ${specificEdge.nome}.")
                                        return@ChoiceDialog
                                    }
                                    if (bloquearExclusividadeClasse(specificEdge)) {
                                        return@ChoiceDialog
                                    }

                                    viewModel.startAdvantageAdvancement(slotIndex, estSel.nome)
                                    viewModel.selectAdvantageForAdvancement(specificEdge)

                                    if (state.arcanoCompraPendente() || state.mostrandoPoderesProgresso) {
                                        showPendingChoice = false
                                        showAdvSelection = false
                                        pendingAdv = null
                                        showPowerSelection = true
                                    } else {
                                        viewModel.finishAdvantageAdvancement()
                                        showPendingChoice = false
                                        showAdvSelection = false
                                        pendingAdv = null
                                        onDismiss()
                                    }
                                }
                            },
                            onDismiss = {
                                showPendingChoice = false
                                pendingAdv = null
                            }
                        )
                    } else {
                        ChoiceDialog(
                            options = validChoiceOptionsFor(vant, state),
                            onConfirm = { finishWithChoice(it) },
                            onDismiss = {
                                showPendingChoice = false
                                pendingAdv = null
                            }
                        )
                    }
                } else if (state.compendioFantasiaAtivo || state.compendioHorrorAtivo) {
                    val opcoesArcano = remember(state.compendioFantasiaAtivo, state.compendioHorrorAtivo) {
                        allAdvantages
                            .filter {
                                val isAb = it.id.startsWith("antecedente_arcano_")
                                val isSrc = (state.compendioFantasiaAtivo && (it.origem.equals("FANTASIA", ignoreCase = true) || it.origem.equals("BASICO", ignoreCase = true))) ||
                                        (state.compendioHorrorAtivo && it.origem.equals("HORROR", ignoreCase = true))
                                isAb && isSrc
                            }
                            .map { v ->
                                val nameInParens = Regex("\\((.*?)\\)").find(v.nome)?.groupValues?.get(1)
                                val baseName = nameInParens?.toSentenceCase()
                                    ?: v.subtipoArcano?.toSentenceCase()
                                    ?: v.nome.removePrefix("ANTECEDENTE ARCANO ").replace("(", "").replace(")", "").trim().toSentenceCase()

                                val reqs = if (v.requisitos.atributoMin.isNotEmpty()) {
                                    val r = v.requisitos.atributoMin.entries.joinToString(", ") { "${it.key} d${it.value}" }
                                    " ($r)"
                                } else ""

                                "$baseName$reqs" to v
                            }
                            .sortedBy { it.first }
                    }

                    if (opcoesArcano.isNotEmpty()) {
                        ChoiceDialog(
                            options = opcoesArcano.map { it.first },
                            onConfirm = { choiceLabel ->
                                val specificEdge = opcoesArcano.firstOrNull { it.first == choiceLabel }?.second
                                if (specificEdge != null) {
                                    val estIndexFinal = advSelectedStageIndex.takeIf { it >= 0 } ?: selectedTab
                                    val estSel = stages[estIndexFinal]

                                    if (!state.podeSelecionar(specificEdge) || !strictRequirementsOk(specificEdge, estIndexFinal)) {
                                        showSnack("Você não cumpre os requisitos para ${specificEdge.nome}.")
                                        return@ChoiceDialog
                                    }
                                    if (bloquearExclusividadeClasse(specificEdge)) {
                                        return@ChoiceDialog
                                    }

                                    viewModel.startAdvantageAdvancement(slotIndex, estSel.nome)
                                    viewModel.selectAdvantageForAdvancement(specificEdge)

                                    if (state.arcanoCompraPendente() || state.mostrandoPoderesProgresso) {
                                        showPendingChoice = false
                                        showAdvSelection = false
                                        pendingAdv = null
                                        showPowerSelection = true
                                    } else {
                                        viewModel.finishAdvantageAdvancement()
                                        showPendingChoice = false
                                        showAdvSelection = false
                                        pendingAdv = null
                                        onDismiss()
                                    }
                                }
                            },
                            onDismiss = {
                                showPendingChoice = false
                                pendingAdv = null
                            }
                        )
                    } else {
                        ChoiceDialog(
                            options = validChoiceOptionsFor(vant, state),
                            onConfirm = { finishWithChoice(it) },
                            onDismiss = {
                                showPendingChoice = false
                                pendingAdv = null
                            }
                        )
                    }
                } else {
                    ChoiceDialog(
                        options = validChoiceOptionsFor(vant, state),
                        onConfirm = { finishWithChoice(it) },
                        onDismiss = {
                            showPendingChoice = false
                            pendingAdv = null
                        }
                    )
                }
            }

            else -> {
                ChoiceDialog(
                    options = validChoiceOptionsFor(vant, state),
                    onConfirm = { finishWithChoice(it) },
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

private fun normalizePathfinderArcaneEntriesForProgress(
    visible: List<Vantagem>,
    allAdvantages: List<Vantagem>,
    state: CriadorState
): List<Vantagem> {
    if (!state.compendioPathfinderAtivo || state.permiteMultiAntecedenteArcano) return visible

    val pfSpecificIds = setOf("antecedente_arcano_magia_pf", "antecedente_arcano_milagres_pf")
    val withoutPfSpecific = visible.filterNot { it.id in pfSpecificIds }

    if (withoutPfSpecific.any { it.id == "antecedente_arcano" }) {
        return withoutPfSpecific
    }

    val genericArcane = allAdvantages.firstOrNull { it.id == "antecedente_arcano" }
    if (genericArcane == null) return withoutPfSpecific

    // Mantém a ordenação original da fonte (allAdvantages) para não empurrar
    // "Antecedente Arcano" para o fim da lista ao normalizar as entradas PF.
    val normalized = withoutPfSpecific + genericArcane
    return allAdvantages.filter { sourceItem ->
        normalized.any { normalizedItem ->
            normalizedItem.id == sourceItem.id &&
                normalizedItem.nome == sourceItem.nome &&
                normalizedItem.origem == sourceItem.origem
        }
    }
}

private fun validChoiceOptionsFor(v: Vantagem, state: CriadorState): List<String> {
    return when (v.id) {
        "arma_predileta" -> {
            state.periciasComIdiomas()
                .filter { per ->
                    val nome = per.nome
                    val isAllowed =
                        nome.equals("Atirar", ignoreCase = true) ||
                            nome.equals("Atletismo", ignoreCase = true) ||
                            nome.equals("Lutar", ignoreCase = true)

                    isAllowed && state.rawTotal(per) >= 8
                }
                .map { it.nome }
        }
        "arma_predileta_aprimorada" -> {
            state.vantagensSelecionadas
                .filter { it.id == "arma_predileta" && !it.choice.isNullOrBlank() }
                .mapNotNull { it.choice }
                .distinct()
        }
        "discipulo_artes_marciais" -> {
            state.vantagensSelecionadas
                .filter { it.id == "estudante_artes_marciais" && !it.choice.isNullOrBlank() }
                .mapNotNull { it.choice }
                .ifEmpty { v.choiceOptions }
        }
        "mestre_artes_marciais" -> {
            val fromDiscipulo = state.vantagensSelecionadas
                .filter { it.id == "discipulo_artes_marciais" && !it.choice.isNullOrBlank() }
                .mapNotNull { it.choice }
            if (fromDiscipulo.isNotEmpty()) {
                fromDiscipulo
            } else {
                state.vantagensSelecionadas
                    .filter { it.id == "estudante_artes_marciais" && !it.choice.isNullOrBlank() }
                    .mapNotNull { it.choice }
                    .ifEmpty { v.choiceOptions }
            }
        }
        else -> v.choiceOptions
    }
}

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
    stages: List<Estagio>,
    onSelect: () -> Unit,
    onError: (String) -> Unit
) {
    val themeData = LocalAppThemeData.current

    val reqList = buildList {
        stages.firstOrNull {
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
    val bloqueioClasse = if (state.vantagensSelecionadas.classeExclusivaBloqueada(vant, state.compendioPathfinderAtivo)) {
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

                    when {
                        // Check class blocking specifically for error message
                        state.vantagensSelecionadas.classeExclusivaBloqueada(vant, state.compendioPathfinderAtivo) -> onError("Requer a vantagem Multiclasse para possuir duas classes")
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
                        if (showOfficialNames && !vant.originalName.isNullOrBlank()) vant.originalName.toSentenceCase() else vant.nomeExibicao.toSentenceCase(),
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
                if (vant.descricao.isNotBlank() && vant.vinculadoPericia &&
                    vant.id !in setOf("arma_predileta", "arma_predileta_aprimorada")
                ) {
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
