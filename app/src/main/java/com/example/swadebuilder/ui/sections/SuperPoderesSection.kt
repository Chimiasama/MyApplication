@file:OptIn(ExperimentalLayoutApi::class)

package com.example.swadebuilder.ui.sections

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.R
import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.CriadorViewModel
import com.example.swadebuilder.model.Pericia
import com.example.swadebuilder.model.PowerEffect
import com.example.swadebuilder.model.SuperInvestment
import com.example.swadebuilder.model.SuperPoder
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.ui.components.ExpandableSearchFilter
import com.example.swadebuilder.ui.components.SectionCard
import com.example.swadebuilder.ui.components.SectionHeader
import com.example.swadebuilder.ui.dialogs.SuperAtributosPickerDialog
import com.example.swadebuilder.ui.dialogs.SuperPericiasPickerDialog
import com.example.swadebuilder.ui.dialogs.SuperVantagensPickerDialog
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.semAcentos
import com.example.swadebuilder.util.toFancyTitleCase
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

@Composable
fun BuySuperPowerDialog(
    poder: SuperPoder,
    pontosDisponiveis: Int,
    limitePorPoder: Int,
    onConfirm: (baseCost: Int, totalCost: Int, modifiers: Map<String, Int>) -> Unit,
    onDismiss: () -> Unit,
) {
    fun parseCustoBaseOptions(raw: String?): List<Int> {
        if (raw.isNullOrBlank()) return listOf(1)
        val s = raw.trim()

        val enDash = '–'
        if (s.contains(enDash)) {
            val parts = s.split(enDash).map { it.trim() }
            val a = parts.getOrNull(0)?.toIntOrNull()
            val b = parts.getOrNull(1)?.toIntOrNull()
            if ((a != null) && (b != null)) {
                val start = minOf(a, b)
                val end = maxOf(a, b)
                return (start..end).toList()
            }
        }

        if (s.contains('/')) {
            return s.split('/')
                .asSequence()
                .mapNotNull { it.trim().toIntOrNull() }
                .distinct()
                .sorted()
                .toList()
        }

        return s.toIntOrNull()?.let { listOf(it) } ?: listOf(1)
    }

    val baseOptionsAll = remember(poder.custoBase) { parseCustoBaseOptions(poder.custoBase) }
    val baseMinDeclarado = baseOptionsAll.minOrNull() ?: 1
    val baseMaxDeclarado = baseOptionsAll.maxOrNull() ?: baseMinDeclarado

    data class ModState(
        val name: String,
        val options: List<Int>,
        val included: MutableState<Boolean>,
        val selected: MutableState<Int>,
        val isNegative: Boolean
    )

    val modStates = remember(poder.modificadores) {
        poder.modificadores.orEmpty().map { modObj ->
            val fullName = modObj.substringBefore(":").trim()
            val cleanName = fullName.replace(Regex("\\s*\\([+-]?\\d+(/\\d+)*\\)\\s*$"), "")
            val paren = Regex("\\(([^)]*)\\)").find(fullName)?.groupValues?.get(1).orEmpty()
            val opts = paren.split("/")
                .mapNotNull { it.trim().removePrefix("+").toIntOrNull() }
                .takeIf { it.isNotEmpty() } ?: listOf(0)
            val isNeg = opts.all { it < 0 } || paren.contains("-")
            ModState(
                name = cleanName,
                options = opts,
                included = mutableStateOf(value = false),
                selected = mutableIntStateOf(value = opts.first()),
                isNegative = isNeg
            )
        }
    }

    val modPositivesCost by remember(modStates) {
        androidx.compose.runtime.derivedStateOf {
            modStates.asSequence().filter { it.included.value && !it.isNegative }.sumOf { it.selected.value }
        }
    }

    val modLimitationsCost by remember(modStates) {
        androidx.compose.runtime.derivedStateOf {
            modStates.asSequence().filter { it.included.value && it.isNegative }.sumOf { it.selected.value }
        }
    }

    val modCost = modPositivesCost + modLimitationsCost

    val totalCap = minOf(limitePorPoder, pontosDisponiveis)

    val capParaBase = (totalCap - modCost).coerceAtLeast(baseMinDeclarado)
    val allowedBaseOptions = baseOptionsAll
        .filter { it in (baseMinDeclarado..minOf(baseMaxDeclarado, capParaBase)) }
        .ifEmpty { listOf(baseMinDeclarado.coerceAtMost(capParaBase)) }

    val minAllowed = allowedBaseOptions.first()
    val maxAllowed = allowedBaseOptions.last()
    val isLongRange = (allowedBaseOptions.size > 7) ||
            ((maxAllowed - minAllowed) > 10)

    var baseIdx by rememberSaveable(poder.nome) { mutableIntStateOf(0) }
    val baseCost = allowedBaseOptions.getOrElse(baseIdx) { allowedBaseOptions.last() }

    LaunchedEffect(baseCost) {
        if ((poder.nome.keyify() == "VELOCIDADE") && (baseCost < 13)) {
            modStates.forEach { mod ->
                if ((mod.name.keyify() == "TENSAO SUPERFICIAL") || (mod.name.keyify() == "TENSAO_SUPERFICIAL")) {
                    mod.included.value = false
                }
            }
        }
    }

    val totalAtualRaw = baseCost + modCost
    val totalAtual = totalAtualRaw.coerceAtLeast(1)

    val podeConfirmar =
        (baseCost in allowedBaseOptions) && (totalAtual in 1..totalCap)

    var isDescExpanded by rememberSaveable { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Comprar “${poder.nome}”") },
        text = {
            val scroll = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp)
                    .verticalScroll(scroll)
                    .padding(4.dp),
            ) {
                poder.descricao?.takeIf { it.isNotBlank() }?.let { descText ->
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isDescExpanded = !isDescExpanded }
                            .padding(bottom = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "📖 Descrição do Poder",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Icon(
                                    imageVector = Icons.Filled.ExpandMore,
                                    contentDescription = if (isDescExpanded) "Recolher" else "Expandir",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            AnimatedVisibility(visible = isDescExpanded) {
                                Column(modifier = Modifier.padding(top = 4.dp)) {
                                    Text(
                                        text = descText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
                if (allowedBaseOptions.size > 1) {
                    if (!isLongRange) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            allowedBaseOptions.forEachIndexed { idx, opt ->
                                FilterChip(
                                    selected = (idx == baseIdx),
                                    onClick = { baseIdx = idx },
                                    label = { Text("$opt SP") },
                                )
                            }
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Custo base: $baseCost SP",
                                modifier = Modifier.weight(1f),
                                fontWeight = FontWeight.SemiBold
                            )
                            TextButton(
                                onClick = { if (baseIdx > 0) baseIdx-- },
                                enabled = baseIdx > 0
                            ) { Text("−") }
                            TextButton(
                                onClick = { if (baseIdx < allowedBaseOptions.lastIndex) baseIdx++ },
                                enabled = baseIdx < allowedBaseOptions.lastIndex
                            ) { Text("+") }
                        }
                        Text(
                            text = "Mín: $minAllowed • Máx: $maxAllowed",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                val positiveMods = modStates.filter { !it.isNegative }
                val negativeMods = modStates.filter { it.isNegative }

                if (positiveMods.isNotEmpty()) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                "🟢 Adicionais Positivos (+)",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(4.dp))
                            positiveMods.forEach { mod ->
                                val isTensaoSuperficial = poder.nome.keyify() == "VELOCIDADE" &&
                                        (mod.name.keyify() == "TENSAO SUPERFICIAL" || mod.name.keyify() == "TENSAO_SUPERFICIAL")
                                val isModEnabled = !isTensaoSuperficial || baseCost >= 13

                                Column(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                                    val rowTitle = if (mod.options.size == 1) {
                                        val optVal = mod.options.first()
                                        val valStr = if (optVal > 0) "+$optVal" else "$optVal"
                                        "${mod.name} ($valStr SP)"
                                    } else {
                                        mod.name
                                    }
                                    com.example.swadebuilder.ui.components.SelectableItemRow(
                                        title = rowTitle,
                                        selected = mod.included.value,
                                        onClick = { mod.included.value = !mod.included.value },
                                        mode = com.example.swadebuilder.ui.components.SelectionMode.MULTIPLA,
                                        enabled = isModEnabled
                                    )
                                    if (mod.included.value) {
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.padding(start = 24.dp, top = 2.dp)
                                        ) {
                                            mod.options.forEach { opt ->
                                                FilterChip(
                                                    selected = mod.selected.value == opt,
                                                    onClick = {
                                                        val outros = modStates.filter { it.included.value && it != mod }.sumOf { it.selected.value }
                                                        if (baseCost + outros + opt <= totalCap) {
                                                            mod.selected.value = opt
                                                        }
                                                    },
                                                    label = { Text("+$opt SP") }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (negativeMods.isNotEmpty()) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                "🔴 Limitações (-)",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(Modifier.height(4.dp))
                            negativeMods.forEach { mod ->
                                Column(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                                    val rowTitle = if (mod.options.size == 1) {
                                        val optVal = mod.options.first()
                                        val valStr = if (optVal > 0) "-$optVal" else "$optVal"
                                        "${mod.name} ($valStr SP)"
                                    } else {
                                        mod.name
                                    }
                                    com.example.swadebuilder.ui.components.SelectableItemRow(
                                        title = rowTitle,
                                        selected = mod.included.value,
                                        onClick = { mod.included.value = !mod.included.value },
                                        mode = com.example.swadebuilder.ui.components.SelectionMode.MULTIPLA
                                    )
                                    if (mod.included.value && mod.options.size > 1) {
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.padding(start = 24.dp, top = 2.dp)
                                        ) {
                                            mod.options.forEach { opt ->
                                                FilterChip(
                                                    selected = mod.selected.value == opt,
                                                    onClick = { mod.selected.value = opt },
                                                    label = { Text("$opt SP") }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Custo Base:", style = MaterialTheme.typography.bodySmall)
                            Text("$baseCost SP", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        if (modPositivesCost > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Adicionais (+):", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                Text("+$modPositivesCost SP", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        if (modLimitationsCost < 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Limitações (-):", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                                Text("$modLimitationsCost SP", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("TOTAL DO PODER:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text("$totalAtual / $totalCap SP", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = if (totalAtual > totalCap) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = podeConfirmar,
                onClick = {
                    val mods = modStates
                        .asSequence()
                        .filter { it.included.value }
                        .associateBy({ it.name }) { it.selected.value }
                    onConfirm(baseCost, totalAtual, mods)
                }
            ) { Text("Comprar ($totalAtual SP)") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

private fun Int.coLeastZero(): Int = if (this < 0) 0 else this

private enum class SuperCategory(val label: String, val icon: String) {
    TODOS("Todos", ""),
    COMBATE("Combate", "⚔️"),
    DEFESA("Defesa", "🛡️"),
    MOVIMENTO("Movimento", "🏃"),
    ATRIBUTOS("Atributos", "📊"),
    UTILITARIOS("Utilitários", "✨");

    companion object {
        fun fromPowerName(nome: String): SuperCategory {
            val key = nome.keyify()
            return when {
                key in listOf("ATAQUE_CORPO_A_CORPO", "ATAQUE_DE_LONGA_DISTANCIA", "APARAR", "ATORDOAR", "CAMPO_DE_DANO", "DECOMPOR", "EMPURRAR", "ENREDAR", "PRECISAO_MORTAL", "TERREMOTO", "VENENO") -> COMBATE
                key in listOf("ARMADURA", "CAMPO_DE_FORCA", "ESCUDO_MENTAL", "ESQUIVA", "INTANGIBILIDADE", "INVISIBILIDADE", "RESISTENCIA", "RESISTENCIA_AMBIENTAL", "ROBUSTO", "ABSORCAO", "ANULAR", "DESTEMIDO", "CEGAR", "OBSCURACER", "SEM_ORGAOS_VITAIS") -> DEFESA
                key in listOf("ALCANCE", "ANDAR_NAS_PAREDES", "BALANCAR", "CAVAR", "ENCOLHIMENTO", "ESPACIAL", "MOVIMENTACAO", "SALTO", "TELEPORTE", "VELOCIDADE", "VOO") -> MOVIMENTO
                key in listOf("AUMENTAR_REDUZIR_CARACTERISTICA", "BONUS_DE_PERICIA", "SUPERATRIBUTO", "SUPERPERICIA", "SUPERVANTAGEM", "CRESCIMENTO", "REFLEXOS_APRIMORADOS", "SENTIDOS_APRIMORADOS", "MEMBROS_EXTRAS", "SUPER_ATRIBUTO", "SUPER_PERICIA", "SUPER_VANTAGEM") -> ATRIBUTOS
                else -> UTILITARIOS
            }
        }
    }
}

private fun Vantagem.bloqueadaComoSuperVantagem(): Boolean {
    if (grupoId?.equals("antecedente_arcano", ignoreCase = true) == true) return true
    if (id.contains("antecedente_arcano", ignoreCase = true)) return true

    return requisitos.vantagensPrevias.any { req ->
        req.equals("antecedente_arcano", ignoreCase = true) ||
                req.contains("antecedente_arcano", ignoreCase = true)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuperPoderesSection(
    state: CriadorState,
    listaSuperPoderes: List<SuperPoder>,
    viewModel: CriadorViewModel = viewModel(),
    allAdvantages: List<Vantagem>,
    onShowMessage: (String) -> Unit
) {
    var poderParaComprar by remember { mutableStateOf<SuperPoder?>(null) }

    val listState = rememberLazyListState()

    LaunchedEffect(state.superPoderEmFoco) {
        val foco = state.superPoderEmFoco
        if (!foco.isNullOrBlank()) {
            val index = listaSuperPoderes.indexOfFirst { it.nome == foco }
            if (index >= 0) {
                listState.scrollToItem(index)
            }
        }
    }

    val nivelAtual = state.superNivelCampanha

    val supersLiberados = state.baseCreationComplete()

    val jaInvestiuSupers = state.superPontosDisponiveis < state.superPontosTotais
    val podeEditarNivel = supersLiberados && !jaInvestiuSupers

    val nivelDefinido = (nivelAtual != null && state.superPontosTotais > 0)

    val podeComprarSupers = supersLiberados && nivelDefinido

    var showNivelDialog by rememberSaveable { mutableStateOf(value = false) }

    fun aplicarNivelSuper(novoNivel: Int) {
        if (novoNivel <= 0) {
            state.superNivelCampanha = null
            state.superPontosTotais = 0
            state.superPontosDisponiveis = 0
            state.superLimite = 0
            state.superLimitePorPoder = 0
            state.limiteDePoderDaCampanha = 0
            state.faseSupersAtiva = false

            return
        }

        val total = 15 * novoNivel
        val limite = 5 * novoNivel

        state.superNivelCampanha = novoNivel
        state.superPontosTotais = total
        state.superLimite = limite
        state.superLimitePorPoder = limite
        state.limiteDePoderDaCampanha = limite

        val gastos = state.gastosPorPoder.values.sum()
        state.superPontosDisponiveis = (total - gastos).coerceAtLeast(0)

        state.faseSupersAtiva = true
    }

    val temOMelhorQueHa = state.oMelhorQueHaSelecionada

    var showSuperAttrPicker by rememberSaveable { mutableStateOf(value = false) }
    var poolSuperAttr by rememberSaveable { mutableIntStateOf(0) }

    var showSuperPericiaPicker by rememberSaveable { mutableStateOf(value = false) }
    var poolSuperPericia by rememberSaveable { mutableIntStateOf(0) }

    var showSuperVantPicker by rememberSaveable { mutableStateOf(value = false) }
    var poolSuperVant by rememberSaveable { mutableIntStateOf(0) }

    var showBonusPericiaPicker by rememberSaveable { mutableStateOf(false) }
    var bonusPericiaBaseCost by rememberSaveable { mutableIntStateOf(0) }
    var bonusPericiaTotalCost by rememberSaveable { mutableIntStateOf(0) }
    var bonusPericiaNivel by rememberSaveable { mutableIntStateOf(0) }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        if (state.superInvestments.isNotEmpty()) {
            val genericosAgrupados = state.superInvestments
                .asSequence()
                .filter { it.effect is PowerEffect.Generico }
                .groupBy { it.powerId }

            val uniqueInvestments = mutableListOf<Pair<SuperInvestment, Int>>()
            val emittedIds = mutableSetOf<String>()

            state.superInvestments.forEach { investment ->
                when (investment.effect) {
                    is PowerEffect.Generico -> {
                        if (emittedIds.add(investment.powerId)) {
                            val listaMesmoPoder = genericosAgrupados[investment.powerId].orEmpty()
                            val custoSomado = listaMesmoPoder.sumOf { it.cost }
                            uniqueInvestments.add(investment to custoSomado)
                        }
                    }
                    else -> {
                        uniqueInvestments.add(investment to investment.cost)
                    }
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Meus Poderes (${uniqueInvestments.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(4.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp)
                ) {
                    items(uniqueInvestments, key = { it.first.id }) { pair ->
                        val (investment, cost) = pair
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            tonalElevation = 1.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = investment.displayName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Surface(
                                            shape = MaterialTheme.shapes.extraSmall,
                                            color = MaterialTheme.colorScheme.primaryContainer
                                        ) {
                                            Text(
                                                text = "$cost SP",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    if (investment.modifiers.isNotEmpty()) {
                                        val modSummary = investment.modifiers.entries.joinToString(", ") { (modName, modVal) ->
                                            val sign = if (modVal > 0) "+$modVal" else "$modVal"
                                            "$modName ($sign)"
                                        }
                                        Text(
                                            text = modSummary,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Spacer(Modifier.width(8.dp))
                                IconButton(
                                    onClick = {
                                        if (investment.effect is PowerEffect.Generico) {
                                            val listaMesmoPoder = genericosAgrupados[investment.powerId].orEmpty()
                                            listaMesmoPoder.forEach { inv ->
                                                viewModel.desfazerInvestimentoSuper(inv)
                                                state.removerSuperPoder(inv, desfazerNoLedger = false)
                                            }
                                        } else {
                                            val r = viewModel.desfazerInvestimentoSuper(investment)
                                            if (r.ok) {
                                                state.removerSuperPoder(investment, desfazerNoLedger = false)
                                            } else {
                                                onShowMessage(r.mensagem)
                                            }
                                        }
                                    },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Remover ${investment.displayName}",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val levelLabel = if (!supersLiberados) "🔒 Nível" else if (nivelAtual == null) "⚡ Nível: –" else "⚡ Nível $nivelAtual"
            AssistChip(
                onClick = { if (podeEditarNivel) showNivelDialog = true },
                enabled = podeEditarNivel,
                label = {
                    Text(
                        text = levelLabel,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            )

            if (nivelDefinido) {
                val pontosGastos = (state.superPontosTotais - state.superPontosDisponiveis).coerceAtLeast(0)
                val progress = if (state.superPontosTotais > 0) {
                    (pontosGastos.toFloat() / state.superPontosTotais.toFloat()).coerceIn(0f, 1f)
                } else 0f

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$pontosGastos / ${state.superPontosTotais} SP",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Máx: ${state.superLimite} SP",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp),
                        color = if (progress >= 1f) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                }
            } else {
                Text(
                    text = if (!supersLiberados) "Conclua a criação base" else "Selecione um nível",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        if (temOMelhorQueHa && nivelDefinido) {
            Spacer(Modifier.height(8.dp))

            if (state.poderFavoritoId == null) {
                Text(
                    "Defina o poder vinculado à vantagem O Melhor Que Há",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        var searchQuery by rememberSaveable { mutableStateOf("") }
        var isSearchExpanded by rememberSaveable { mutableStateOf(value = false) }
        var selectedCategory by rememberSaveable { mutableStateOf(SuperCategory.TODOS) }

        val filteredList = remember(listaSuperPoderes, searchQuery, selectedCategory) {
            listaSuperPoderes.filter { poder ->
                val matchesCategory = selectedCategory == SuperCategory.TODOS ||
                        SuperCategory.fromPowerName(poder.nome) == selectedCategory
                val matchesQuery = searchQuery.isBlank() ||
                        poder.nome.semAcentos().contains(searchQuery.semAcentos(), ignoreCase = true) ||
                        (poder.descricao?.semAcentos()?.contains(searchQuery.semAcentos(), ignoreCase = true) == true)
                matchesCategory && matchesQuery
            }
        }

        ExpandableSearchFilter(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            isExpanded = isSearchExpanded,
            onExpandedChange = { isSearchExpanded = it },
            onClear = { searchQuery = "" },
            placeholder = "Pesquisar Superpoderes..."
        )

        Spacer(Modifier.height(4.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp)
        ) {
            items(SuperCategory.entries.toTypedArray()) { cat ->
                val count = remember(listaSuperPoderes, searchQuery, cat) {
                    listaSuperPoderes.count { poder ->
                        val matchesCat = cat == SuperCategory.TODOS || SuperCategory.fromPowerName(poder.nome) == cat
                        val matchesQ = searchQuery.isBlank() ||
                                poder.nome.semAcentos().contains(searchQuery.semAcentos(), ignoreCase = true) ||
                                (poder.descricao?.semAcentos()?.contains(searchQuery.semAcentos(), ignoreCase = true) == true)
                        matchesCat && matchesQ
                    }
                }
                val labelText = if (cat.icon.isEmpty()) "${cat.label} ($count)" else "${cat.icon} ${cat.label} ($count)"
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    label = { Text(labelText, style = MaterialTheme.typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors()
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        val allowLongTexts = booleanResource(R.bool.enable_long_texts)
        val detalhesExpandidos = remember { mutableStateMapOf<String, Boolean>() }

        val themeData = com.example.swadebuilder.ui.theme.LocalAppThemeData.current

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
            items(filteredList, key = { it.nome }) { poder ->
                val manifestacoesList = remember(poder.nome, poder.manifestacoes) {
                    when (val m = poder.manifestacoes) {
                        is JsonArray -> m.mapNotNull { (it as? JsonPrimitive)?.contentOrNull }
                        is JsonPrimitive -> listOfNotNull(m.contentOrNull)
                        else -> emptyList()
                    }.asSequence()
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                        .distinct()
                        .toList()
                }

                val showDetails = allowLongTexts && (
                        poder.descricao?.isNotBlank() == true ||
                                (poder.modificadores?.isNotEmpty() == true) ||
                                manifestacoesList.isNotEmpty()
                        )
                val expanded = detalhesExpandidos[poder.nome] == true

                val poderKey = "sp_${poder.nome.keyify()}"
                val gastoNestePoder = state.gastosPorPoder[poderKey] ?: 0
                val jaInvestido = gastoNestePoder > 0

                val statusText = when {
                    jaInvestido -> "+$gastoNestePoder SP"
                    podeComprarSupers -> "Selecionar"
                    else -> "Bloqueado"
                }

                val statusColor = when {
                    jaInvestido -> MaterialTheme.colorScheme.tertiary
                    podeComprarSupers -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.error
                }

                androidx.compose.material3.Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable(enabled = podeComprarSupers) {
                            if (podeComprarSupers) {
                                val nomeUpper = poder.nome.trim().uppercase()
                                when (nomeUpper) {
                                    "SUPERATRIBUTO", "SUPER ATRIBUTO" -> {
                                        val gastoAtual = state.gastosPorPoder["sp_superatributo"] ?: 0
                                        val limiteInd = viewModel.perPowerLimit("sp_superatributo")
                                        val restoInd = (limiteInd - gastoAtual).coerceAtLeast(0)
                                        val maxSp = minOf(restoInd, state.superPontosDisponiveis)
                                        poolSuperAttr = maxSp / 2
                                        if (poolSuperAttr > 0) {
                                            showSuperAttrPicker = true
                                        } else {
                                            onShowMessage("Limite ou pontos insuficientes para Superatributo.")
                                        }
                                    }
                                    "SUPERPERÍCIA", "SUPER PERÍCIA", "SUPERPERICIA", "SUPER PERICIA" -> {
                                        val gastoAtual = state.gastosPorPoder["sp_superpericia"] ?: 0
                                        val limiteInd = viewModel.perPowerLimit("sp_superpericia")
                                        val restoInd = (limiteInd - gastoAtual).coerceAtLeast(0)
                                        val maxSp = minOf(restoInd, state.superPontosDisponiveis)
                                        poolSuperPericia = maxSp
                                        if (poolSuperPericia > 0) {
                                            showSuperPericiaPicker = true
                                        } else {
                                            onShowMessage("Limite ou pontos insuficientes para Superperícia.")
                                        }
                                    }
                                    "SUPERVANTAGEM", "SUPER VANTAGEM" -> {
                                        val gastoAtual = state.gastosPorPoder["sp_supervantagem"] ?: 0
                                        val limiteInd = viewModel.perPowerLimit("sp_supervantagem")
                                        val restoInd = (limiteInd - gastoAtual).coerceAtLeast(0)
                                        val maxSp = minOf(restoInd, state.superPontosDisponiveis)
                                        poolSuperVant = maxSp / 2
                                        if (poolSuperVant > 0) {
                                            showSuperVantPicker = true
                                        } else {
                                            onShowMessage("Limite ou pontos insuficientes para Supervantagem.")
                                        }
                                    }
                                    else -> {
                                        poderParaComprar = poder
                                    }
                                }
                            }
                        },
                    colors = androidx.compose.material3.CardDefaults.cardColors(
                        containerColor = if (jaInvestido) {
                            MaterialTheme.colorScheme.tertiaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                    border = themeData.cardBorderColor?.let { BorderStroke(1.dp, it) }
                ) {
                    Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                poder.nome.toFancyTitleCase(),
                                Modifier.weight(1f),
                                style = MaterialTheme.typography.titleSmall
                            )

                            if (temOMelhorQueHa) {
                                val favoritoAtual = state.poderFavoritoId
                                val temFavorito = favoritoAtual != null

                                val isFavoriteLocked = favoritoAtual?.let { favId ->
                                    (state.gastosPorPoder[favId] ?: 0) > 0
                                } ?: false

                                val poderId = "sp_${poder.nome.keyify()}"
                                val isFav = favoritoAtual == poderId

                                val isBloqueado = poder.nome.keyify() in listOf("ARMADURA", "RESISTENCIA")

                                val showStarForThis = !isBloqueado && (!temFavorito || isFav)

                                if (showStarForThis) {
                                    IconButton(
                                        onClick = {
                                            viewModel.definirPoderFavorecido(if (isFav) null else poderId)
                                        },
                                        enabled = !isFavoriteLocked
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.StarBorder,
                                            contentDescription = if (isFav)
                                                "Desmarcar como poder favorito"
                                            else
                                                "Marcar como poder favorito",
                                            tint = if (isFavoriteLocked)
                                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                            else if (isFav)
                                                Color.Red
                                            else
                                                MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.width(8.dp))

                            Text(
                                statusText,
                                style = MaterialTheme.typography.labelMedium,
                                color = statusColor
                            )
                        }
                    }
                }
            }
        }
    }

    if (showNivelDialog) {
        AlertDialog(
            onDismissRequest = { showNivelDialog = false },
            title = { Text("Escolher nível de Superpoderes") },
            text = {
                Column(Modifier.fillMaxWidth()) {

                    TextButton(
                        onClick = {
                            aplicarNivelSuper(0)
                            showNivelDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.fillMaxWidth()) {
                            Text(
                                text = "Nível 0 – voltar à criação",
                                fontWeight = if (nivelAtual == null)
                                    FontWeight.Bold else FontWeight.Normal
                            )
                            Text(
                                text = "Zera os pontos de superpoder e desbloqueia Atributos, Perícias, Ancestralidades, etc.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    HorizontalDivider()

                    (1..5).forEach { nivel ->
                        val total = 15 * nivel
                        val limite = 5 * nivel
                        TextButton(
                            onClick = {
                                aplicarNivelSuper(nivel)
                                showNivelDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.fillMaxWidth()) {
                                Text(
                                    text = "Nível $nivel",
                                    fontWeight = if (nivel == nivelAtual)
                                        FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    text = "Pontos: $total • Limite por poder: $limite",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showNivelDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    poderParaComprar?.let { poder ->

        if (!supersLiberados) {
            poderParaComprar = null
        } else {
            val nomeUpper = poder.nome.trim().uppercase()
            val poderIdEspecifico = when (nomeUpper) {
                "ARMADURA" -> "sp_armor"
                "RESISTÊNCIA", "RESISTENCIA" -> "sp_res"
                else -> null
            }

            val saldoSp = state.superPontosDisponiveis
            state.superLimitePorPoder

            val limiteParaDialog: Int = when {

                poderIdEspecifico != null -> {
                    val gastoAtualNeste = state.gastosPorPoder[poderIdEspecifico] ?: 0
                    val limiteIndividual = viewModel.perPowerLimit(poderIdEspecifico)
                    val restoIndividual = (limiteIndividual - gastoAtualNeste).coerceAtLeast(0)

                    val gastoArmor = state.gastosPorPoder["sp_armor"] ?: 0
                    val gastoRes = state.gastosPorPoder["sp_res"] ?: 0
                    val gastoCompartilhado = gastoArmor + gastoRes

                    val restoCompartilhado =
                        (state.limiteDePoderDaCampanha - gastoCompartilhado).coerceAtLeast(0)

                    minOf(restoIndividual, restoCompartilhado, saldoSp)
                }

                nomeUpper == "APARAR" -> {
                    val gastoAtual = state.gastosPorPoder["sp_aparar"] ?: 0
                    val limiteInd = viewModel.perPowerLimit("sp_aparar")
                    val restoInd = (limiteInd - gastoAtual).coerceAtLeast(0)
                    minOf(restoInd, saldoSp)
                }

                nomeUpper == "MOVIMENTAÇÃO" || nomeUpper == "MOVIMENTACAO" -> {
                    val gastoAtual = state.gastosPorPoder["sp_movimentacao"] ?: 0
                    val limiteInd = viewModel.perPowerLimit("sp_movimentacao")
                    val restoInd = (limiteInd - gastoAtual).coerceAtLeast(0)
                    minOf(restoInd, saldoSp)
                }

                nomeUpper == "SUPERATRIBUTO" || nomeUpper == "SUPER ATRIBUTO" -> {
                    val gastoAtual = state.gastosPorPoder["sp_superatributo"] ?: 0
                    val limiteInd = viewModel.perPowerLimit("sp_superatributo")
                    val restoInd = (limiteInd - gastoAtual).coerceAtLeast(0)
                    minOf(restoInd, saldoSp)
                }

                nomeUpper == "SUPERPERÍCIA" || nomeUpper == "SUPER PERÍCIA" ||
                        nomeUpper == "SUPERPERICIA" || nomeUpper == "SUPER PERICIA" -> {
                    val gastoAtual = state.gastosPorPoder["sp_superpericia"] ?: 0
                    val limiteInd = viewModel.perPowerLimit("sp_superpericia")
                    val restoInd = (limiteInd - gastoAtual).coerceAtLeast(0)
                    minOf(restoInd, saldoSp)
                }

                nomeUpper == "SUPERVANTAGEM" || nomeUpper == "SUPER VANTAGEM" -> {
                    val gastoAtual = state.gastosPorPoder["sp_supervantagem"] ?: 0
                    val limiteInd = viewModel.perPowerLimit("sp_supervantagem")
                    val restoInd = (limiteInd - gastoAtual).coerceAtLeast(0)
                    minOf(restoInd, saldoSp)
                }

                nomeUpper == "BÔNUS DE PERÍCIA" ||
                        nomeUpper == "BÔNUS DE PERICIA" ||
                        nomeUpper == "BONUS DE PERÍCIA" ||
                        nomeUpper == "BONUS DE PERICIA" -> {
                    val limiteIndividual = viewModel.perPowerLimit("sp_bonus_pericia")
                    minOf(limiteIndividual, saldoSp)
                }

                else -> {
                    val poderIdGenerico = "sp_${poder.nome.keyify()}"
                    val gastoAtual = state.gastosPorPoder[poderIdGenerico] ?: 0
                    val limiteInd = viewModel.perPowerLimit(poderIdGenerico)
                    val restoInd = (limiteInd - gastoAtual).coerceAtLeast(0)
                    minOf(restoInd, saldoSp)
                }
            }

            BuySuperPowerDialog(
                poder = poder,
                pontosDisponiveis = saldoSp,
                limitePorPoder = limiteParaDialog,
                onConfirm = { baseCost, custoTotal, modifiers ->
                    val nome = poder.nome.trim().uppercase()
                    var result: com.example.swadebuilder.model.InvestResult? = null

                    when (nome) {
                        "APARAR" -> {
                            result = viewModel.tentarInvestirSuper(
                                SuperInvestment(
                                    powerId = "sp_aparar",
                                    displayName = "Aparar",
                                    cost = custoTotal,
                                    baseCost = baseCost,
                                    effect = PowerEffect.BonusAparar(baseCost),
                                    modifiers = modifiers
                                )
                            )
                        }
                        "MOVIMENTAÇÃO", "MOVIMENTACAO" -> {
                            result = viewModel.tentarInvestirSuper(
                                SuperInvestment(
                                    powerId = "sp_movimentacao",
                                    displayName = "Movimentação",
                                    cost = custoTotal,
                                    baseCost = baseCost,
                                    effect = PowerEffect.BonusMovimentacao(baseCost),
                                    modifiers = modifiers
                                )
                            )
                        }
                        "ARMADURA" -> {
                            result = viewModel.tentarInvestirSuper(
                                SuperInvestment(
                                    powerId = "sp_armor",
                                    displayName = "Armadura",
                                    cost = custoTotal,
                                    baseCost = baseCost,
                                    effect = PowerEffect.BonusArmadura(baseCost * 2),
                                    modifiers = modifiers
                                )
                            )
                        }
                        "RESISTÊNCIA", "RESISTENCIA" -> {
                            result = viewModel.tentarInvestirSuper(
                                SuperInvestment(
                                    powerId = "sp_res",
                                    displayName = "Resistência",
                                    cost = custoTotal,
                                    baseCost = baseCost,
                                    effect = PowerEffect.BonusResistencia(baseCost),
                                    modifiers = modifiers
                                )
                            )
                        }
                        "SUPERATRIBUTO", "SUPER ATRIBUTO" -> {
                            poolSuperAttr = baseCost / 2
                            showSuperAttrPicker = true
                        }
                        "SUPERPERÍCIA",
                        "SUPER PERÍCIA",
                        "SUPERPERICIA",
                        "SUPER PERICIA" -> {
                            poolSuperPericia = baseCost
                            if (poolSuperPericia > 0) {
                                showSuperPericiaPicker = true
                            } else {
                                onShowMessage("Limite deste poder já foi atingido.")
                            }
                        }
                        "SUPERVANTAGEM", "SUPER VANTAGEM" -> {
                            poolSuperVant = baseCost / 2
                            if (poolSuperVant > 0) {
                                showSuperVantPicker = true
                            }
                        }
                        "BÔNUS DE PERÍCIA",
                        "BÔNUS DE PERICIA",
                        "BONUS DE PERÍCIA",
                        "BONUS DE PERICIA" -> {
                            bonusPericiaBaseCost = baseCost
                            bonusPericiaTotalCost = custoTotal
                            bonusPericiaNivel = if (baseCost >= 4) 2 else 1
                            showBonusPericiaPicker = true
                        }
                        else -> {
                            result = viewModel.tentarInvestirSuper(
                                SuperInvestment(
                                    powerId = "sp_${poder.nome.keyify()}",
                                    displayName = poder.nome,
                                    cost = custoTotal,
                                    baseCost = baseCost,
                                    effect = PowerEffect.Generico(poder.nome),
                                    modifiers = modifiers
                                )
                            )
                        }
                    }

                    if (result != null) {
                        if (result.ok) {
                            poderParaComprar = null
                        } else {
                            onShowMessage(result.mensagem)
                        }
                    } else {
                        poderParaComprar = null
                    }
                }
            ) { poderParaComprar = null }
        }
    }

    if (showSuperAttrPicker) {
        SuperAtributosPickerDialog(
            state = state,
            poolInicial = poolSuperAttr,
            onConfirmDistribuicao = { mapa ->
                mapa.forEach { (attrKey, stepsSolicitados) ->
                    val poderIdPai = "sp_superatributo"

                    val custoPretendido = stepsSolicitados * 2

                    val gastoAtual = state.gastosPorPoder[poderIdPai] ?: 0
                    val limitePorPoder = viewModel.perPowerLimit(poderIdPai)
                    val restoPorPoder = (limitePorPoder - gastoAtual).coerceAtLeast(0)
                    val restoDePool = state.superPontosDisponiveis.coerceAtLeast(0)

                    val custoAplicavel = minOf(custoPretendido, restoPorPoder, restoDePool)
                    val stepsAplicaveis = (custoAplicavel / 2).coerceAtLeast(0)

                    if (stepsAplicaveis > 0 && custoAplicavel > 0) {
                        viewModel.tentarInvestirSuper(
                            SuperInvestment(
                                powerId = poderIdPai,
                                displayName = "Superatributo: $attrKey",
                                cost = custoAplicavel,
                                baseCost = stepsAplicaveis,
                                effect = PowerEffect.SuperAtributo(attrKey.uppercase(), stepsAplicaveis)
                            )
                        )
                    }
                }

                showSuperAttrPicker = false
            },
            onDismiss = { showSuperAttrPicker = false }
        )
    }

    if (showSuperPericiaPicker) {
        SuperPericiasPickerDialog(
            state = state,
            poolInicial = poolSuperPericia,
            onConfirmDistribuicao = { mapa ->

                val poderIdPai = "sp_superpericia"

                mapa.forEach { (periciaKey, stepsSolicitados) ->

                    val custoPretendido = stepsSolicitados

                    val gastoAtual = state.gastosPorPoder[poderIdPai] ?: 0
                    val limitePorPoder = viewModel.perPowerLimit(poderIdPai)
                    val restoPorPoder = (limitePorPoder - gastoAtual).coerceAtLeast(0)
                    val restoDePool = state.superPontosDisponiveis.coerceAtLeast(0)

                    val custoAplicavel = minOf(custoPretendido, restoPorPoder, restoDePool)
                    val stepsAplicaveis = custoAplicavel.coerceAtLeast(0)

                    if (stepsAplicaveis > 0 && custoAplicavel > 0) {
                        viewModel.tentarInvestirSuper(
                            SuperInvestment(
                                powerId = poderIdPai,
                                displayName = "Superperícia: $periciaKey",
                                cost = custoAplicavel,
                                baseCost = stepsAplicaveis,
                                effect = PowerEffect.SuperPericia(periciaKey, stepsAplicaveis)
                            )
                        )
                    }
                }

                showSuperPericiaPicker = false
            },
            onDismiss = { showSuperPericiaPicker = false }
        )
    }

    if (showSuperVantPicker) {
        val vantagensDisponiveis: List<Vantagem> = allAdvantages.filter { v ->
            v.categoria != Categoria.LENDARIAS &&
                    !v.bloqueadaComoSuperVantagem() &&
                    state.vantagensSelecionadas.none { it.id == v.id }
        }

        SuperVantagensPickerDialog(
            poolInicial = poolSuperVant,
            vantagensDisponiveis = vantagensDisponiveis,
            onConfirm = { selecionadas ->
                val custoPorVantagem = 2

                selecionadas.forEach { v ->
                    val poderIdPai = "sp_supervantagem"

                    val gastoAtual = state.gastosPorPoder[poderIdPai] ?: 0
                    val limitePorPoder = viewModel.perPowerLimit(poderIdPai)
                    val restoPorPoder = (limitePorPoder - gastoAtual).coerceAtLeast(0)
                    val restoDePool = state.superPontosDisponiveis.coerceAtLeast(0)

                    val custoAplicavel = minOf(custoPorVantagem, restoPorPoder, restoDePool)

                    if (custoAplicavel == custoPorVantagem) {
                        viewModel.tentarInvestirSuper(
                            SuperInvestment(
                                powerId = poderIdPai,
                                displayName = "Supervantagem: ${v.nome}",
                                cost = custoAplicavel,
                                baseCost = 1,
                                effect = PowerEffect.SuperVantagem(v.id)
                            )
                        )
                    }
                }

                showSuperVantPicker = false
            },
            onDismiss = { showSuperVantPicker = false }
        )
    }

    if (showBonusPericiaPicker) {
        var selectedPericia by remember { mutableStateOf<Pericia?>(null) }

        AlertDialog(
            onDismissRequest = { showBonusPericiaPicker = false },
            title = { Text("Escolher perícia para Bônus de Perícia") },
            text = {
                val scroll = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 260.dp)
                        .verticalScroll(scroll)
                ) {
                    Text(
                        "Escolha a perícia que recebe o bônus. " +
                                "Este poder não altera automaticamente a ficha, " +
                                "serve apenas como lembrete.\n\n" +
                                "Se a perícia já tiver um Bônus de Perícia, " +
                                "ele será substituído pela nova versão."
                    )
                    Spacer(Modifier.height(8.dp))

                    state.listaPericias.forEach { per ->
                        com.example.swadebuilder.ui.components.SelectableItemRow(
                            title = per.nome,
                            selected = selectedPericia == per,
                            onClick = { selectedPericia = per },
                            modifier = Modifier.padding(vertical = 2.dp),
                            mode = com.example.swadebuilder.ui.components.SelectionMode.UNICA
                        )
                    }
                }
            },
            confirmButton = {
                val per = selectedPericia
                val gastosTotais = state.gastosPorPoder["sp_bonus_pericia"] ?: 0
                val limiteIndividual = viewModel.perPowerLimit("sp_bonus_pericia")
                val custoNovo = bonusPericiaTotalCost

                val podeConfirmar = if (per == null) {
                    false
                } else {
                    val existente = state.superInvestments.firstOrNull { inv ->
                        inv.powerId == "sp_bonus_pericia" &&
                                inv.displayName.contains("em ${per.nome}")
                    }
                    if (existente != null) {
                        val custoAtual = existente.cost
                        val gastosSemEsta = gastosTotais - custoAtual
                        val novoTotalGastos = gastosSemEsta + custoNovo
                        val custoExtra = (custoNovo - custoAtual).coerceAtLeast(0)
                        novoTotalGastos <= limiteIndividual &&
                                custoExtra <= state.superPontosDisponiveis
                    } else {
                        val novoTotalGastos = gastosTotais + custoNovo
                        novoTotalGastos <= limiteIndividual &&
                                custoNovo <= state.superPontosDisponiveis
                    }
                }

                TextButton(
                    enabled = podeConfirmar,
                    onClick = {
                        val pericia = selectedPericia ?: return@TextButton
                        val poderId = "sp_bonus_pericia"
                        val custoNovoLocal = bonusPericiaTotalCost

                        val existente = state.superInvestments.firstOrNull { inv ->
                            inv.powerId == poderId &&
                                    inv.displayName.contains("em ${pericia.nome}")
                        }

                        if (existente != null) {
                            viewModel.desfazerInvestimentoSuper(existente)
                            state.removerSuperPoder(existente, desfazerNoLedger = false)
                        }

                        val nivel = bonusPericiaNivel.coerceAtLeast(1)
                        val textoNivel = if (nivel == 1) "+1" else "+$nivel"
                        val nomeChip = "Bônus de Perícia ($textoNivel em ${pericia.nome})"

                        viewModel.tentarInvestirSuper(
                            SuperInvestment(
                                powerId = poderId,
                                displayName = nomeChip,
                                cost = custoNovoLocal,
                                baseCost = bonusPericiaBaseCost,
                                effect = PowerEffect.Generico(nomeChip)
                            )
                        )
                        showBonusPericiaPicker = false
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBonusPericiaPicker = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}


@Composable
fun SuperPoderesContent(
    state: CriadorState,
    listaSuperPoderes: List<SuperPoder>,
    allAdvantages: List<Vantagem>,
    onShowMessage: (String) -> Unit
) {
    SectionCard(
        title = "Superpoderes",
        icon = Icons.Filled.FlashOn,
        showHeader = false
    ) {
        SuperPoderesSection(
            state = state,
            listaSuperPoderes = listaSuperPoderes,
            allAdvantages = allAdvantages,
            onShowMessage = onShowMessage
        )
    }
}
