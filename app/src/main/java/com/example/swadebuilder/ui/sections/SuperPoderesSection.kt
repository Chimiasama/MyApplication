package com.example.swadebuilder.ui.sections

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.Pericia
import com.example.swadebuilder.R
import com.example.swadebuilder.model.Poder
import com.example.swadebuilder.listaPericias
import com.example.swadebuilder.listaVantagens
import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.CriadorViewModel
import com.example.swadebuilder.model.PowerEffect
import com.example.swadebuilder.model.SuperInvestment
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.ui.components.SectionCard
import com.example.swadebuilder.ui.components.SectionHeader
import com.example.swadebuilder.ui.components.SuperPoderListItem
import com.example.swadebuilder.ui.dialogs.SuperAtributosPickerDialog
import com.example.swadebuilder.ui.dialogs.SuperPericiasPickerDialog
import com.example.swadebuilder.ui.dialogs.SuperVantagensPickerDialog
import com.example.swadebuilder.util.keyify
import kotlin.math.roundToInt

@Composable
fun BuySuperPowerDialog(
    poder: Poder,
    pontosDisponiveis: Int,
    limitePorPoder: Int,
    onConfirm: (baseCost: Int, totalCost: Int, modifiers: Map<String, Int>) -> Unit,
    onDismiss: () -> Unit
) {
    fun parseCustoBaseOptions(raw: String?): List<Int> {
        if (raw.isNullOrBlank()) return listOf(1)
        val s = raw.trim()

        val enDash = '–'
        if (s.contains(enDash)) {
            val parts = s.split(enDash).map { it.trim() }
            val a = parts.getOrNull(0)?.toIntOrNull()
            val b = parts.getOrNull(1)?.toIntOrNull()
            if (a != null && b != null) {
                val start = minOf(a, b)
                val end = maxOf(a, b)
                return (start..end).toList()
            }
        }

        if (s.contains('/')) {
            return s.split('/')
                .mapNotNull { it.trim().toIntOrNull() }
                .distinct()
                .sorted()
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
        val selected: MutableState<Int>
    )

    val modStates = remember(poder.modificadores) {
        poder.modificadores.orEmpty().map { modObj ->
            val name = modObj.substringBefore(":").trim()
            val paren = Regex("\\(([^)]*)\\)").find(name)?.groupValues?.get(1).orEmpty()
            val opts = paren.split("/")
                .mapNotNull { it.trim().removePrefix("+").toIntOrNull() }
                .takeIf { it.isNotEmpty() } ?: listOf(0)
            ModState(
                name = name,
                options = opts,
                included = mutableStateOf(false),
                selected = mutableIntStateOf(opts.first())
            )
        }
    }

    val modCost by remember(modStates) {
        androidx.compose.runtime.derivedStateOf {
            modStates.filter { it.included.value }.sumOf { it.selected.value }
        }
    }

    val totalCap = minOf(limitePorPoder, pontosDisponiveis)

    val capParaBase = (totalCap - modCost).coerceAtLeast(baseMinDeclarado)
    val allowedBaseOptions = baseOptionsAll
        .filter { it in baseMinDeclarado..minOf(baseMaxDeclarado, capParaBase) }
        .ifEmpty { listOf(baseMinDeclarado.coerceAtMost(capParaBase)) }

    val minAllowed = allowedBaseOptions.first()
    val maxAllowed = allowedBaseOptions.last()
    val isLongRange = allowedBaseOptions.size > 7 ||
            (maxAllowed - minAllowed) > 10

    var baseIdx by rememberSaveable(allowedBaseOptions) { mutableIntStateOf(0) }
    val baseCost = allowedBaseOptions.getOrElse(baseIdx) { allowedBaseOptions.last() }

    val totalAtualRaw = baseCost + modCost
    val totalAtual = totalAtualRaw.coerceAtLeast(1)

    val podeConfirmar =
        (baseCost in allowedBaseOptions) && (totalAtual in 1..totalCap)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Comprar “${poder.nome}”") },
        text = {
            val scroll = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 280.dp)
                    .verticalScroll(scroll)
                    .padding(8.dp)
            ) {
                Text("Custo base:", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))

                when {
                    allowedBaseOptions.size == 1 -> {
                        Text("Custo fixo: ${allowedBaseOptions.first()} SP")
                    }
                    !isLongRange -> {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            allowedBaseOptions.forEachIndexed { idx, opt ->
                                FilterChip(
                                    selected = (idx == baseIdx),
                                    onClick = { baseIdx = idx },
                                    label = { Text("$opt SP") }
                                )
                            }
                        }
                    }
                    else -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Custo base: $baseCost SP",
                                modifier = Modifier.weight(1f)
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

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Total do poder: $totalAtual SP (máx: $totalCap)",
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(Modifier.height(8.dp))

                if (modStates.isNotEmpty()) {
                    Text("Modificadores:", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(4.dp))

                    modStates.forEach { mod ->
                        if (mod.options.size == 1) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { mod.included.value = !mod.included.value }
                                    .padding(vertical = 4.dp)
                            ) {
                                Checkbox(
                                    checked = mod.included.value,
                                    onCheckedChange = { mod.included.value = it }
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("${mod.name} (${mod.options.first()})")
                            }
                        } else {
                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = mod.included.value,
                                        onCheckedChange = { mod.included.value = it }
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(mod.name)
                                }
                                if (mod.included.value) {
                                    val sel = mod.selected.value
                                    Slider(
                                        value = sel.toFloat(),
                                        onValueChange = { novo ->
                                            val clamp = novo.roundToInt()
                                                .coerceIn(
                                                    mod.options.minOrNull() ?: 0,
                                                    mod.options.maxOrNull() ?: 0
                                                )
                                            val outros = modStates
                                                .filter { it.included.value && it != mod }
                                                .sumOf { it.selected.value }
                                            val futuroTotal =
                                                allowedBaseOptions[baseIdx] + outros + clamp
                                            if (futuroTotal <= totalCap) {
                                                mod.selected.value = clamp
                                            }
                                        },
                                        valueRange = (mod.options.minOrNull() ?: 0).toFloat()..
                                                (mod.options.maxOrNull() ?: 0).toFloat(),
                                        steps = (mod.options.size - 1).coLeastZero(),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
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
                        .filter { it.included.value }
                        .associate { it.name to it.selected.value }
                    onConfirm(baseCost, totalAtual, mods)
                }
            ) { Text("Comprar ($totalAtual)") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

private fun Int.coLeastZero(): Int = if (this < 0) 0 else this

private fun Vantagem.bloqueadaComoSuperVantagem(): Boolean {
    if (grupoId?.equals("antecedente_arcano", ignoreCase = true) == true) return true
    if (id.contains("antecedente_arcano", ignoreCase = true)) return true

    if (requisitos.vantagensPrevias.any { req ->
            req.equals("antecedente_arcano", ignoreCase = true) ||
                    req.contains("antecedente_arcano", ignoreCase = true)
        }
    ) {
        return true
    }

    return false
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuperPoderesSection(
    state: CriadorState,
    listaSuperPoderes: List<Poder>,
    expanded: Boolean,
    viewModel: CriadorViewModel = viewModel()
) {
    if (!expanded) return
    val context = LocalContext.current
    var poderParaComprar by remember { mutableStateOf<Poder?>(null) }

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

    var showNivelDialog by rememberSaveable { mutableStateOf(false) }

    booleanResource(R.bool.show_full_descriptions)

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


    Column(
        Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        if (state.superInvestments.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val genericosAgrupados = state.superInvestments
                    .filter { it.effect is PowerEffect.Generico }
                    .groupBy { it.powerId }

                val emittedIds = mutableSetOf<String>()

                state.superInvestments.forEach { investment ->
                    when (investment.effect) {
                        is PowerEffect.Generico -> {
                            if (emittedIds.add(investment.powerId)) {
                                val listaMesmoPoder = genericosAgrupados[investment.powerId].orEmpty()
                                val custoSomado = listaMesmoPoder.sumOf { it.cost }

                                AssistChip(
                                    onClick = {
                                        listaMesmoPoder.forEach { inv ->
                                            viewModel.desfazerInvestimentoSuper(inv)
                                            state.removerSuperPoder(inv, desfazerNoLedger = false)
                                        }
                                    },
                                    label = { Text("${investment.displayName} (+$custoSomado)") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = "Remover"
                                        )
                                    }
                                )
                            }
                        }
                        else -> {
                            AssistChip(
                                onClick = {
                                    val r = viewModel.desfazerInvestimentoSuper(investment)
                                    if (r.ok) {
                                        state.removerSuperPoder(investment, desfazerNoLedger = false)
                                    } else {
                                        Toast.makeText(context, r.mensagem, Toast.LENGTH_SHORT).show()
                                    }
                                },
                                label = { Text("${investment.displayName} (+${investment.cost})") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Remover"
                                    )
                                }
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        Text("Nível de Superpoderes")
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = podeEditarNivel) {
                    if (podeEditarNivel) showNivelDialog = true
                }
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = if (nivelAtual == null)
                        "Nível atual: –"
                    else
                        "Nível atual: $nivelAtual",
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (!nivelDefinido)
                        "Pontos: – • Limite por poder: –"
                    else
                        "Pontos: ${state.superPontosTotais} • Limite por poder: ${state.superLimite}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Icon(
                imageVector = Icons.Filled.ExpandMore,
                contentDescription = "Selecionar nível",
            )
        }

        if (!supersLiberados) {
            Text(
                "Termine a distribuição inicial do personagem para escolher o nível de superpoderes.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        } else {
            if (!nivelDefinido) {
                Text(
                    "Defina o nível de superpoderes para liberar a compra de poderes.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            } else if (jaInvestiuSupers) {
                Text(
                    "Para alterar o nível, devolva todos os pontos de superpoder já gastos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(Modifier.height(4.dp))
        Text("Pontos disponíveis: ${state.superPontosDisponiveis}")
        Text("Limite de superpoderes: ${state.superLimite}")

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

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 220.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
            items(listaSuperPoderes, key = { it.nome }) { poder ->
                val favoritoAtual = state.poderFavoritoId
                val temFavorito = favoritoAtual != null
                val isFavoriteLocked = favoritoAtual?.let { favId ->
                    (state.gastosPorPoder[favId] ?: 0) > 0
                } ?: false
                val poderId = "sp_${poder.nome.keyify()}"
                val isFav = favoritoAtual == poderId
                val isBloqueado = poder.nome.keyify() in listOf("ARMADURA", "RESISTENCIA")
                val showStarForThis = !isBloqueado && (!temFavorito || isFav)

                SuperPoderListItem(
                    superPoder = poder,
                    isClickable = podeComprarSupers,
                    onClick = {
                        if (podeComprarSupers) {
                            poderParaComprar = poder
                        }
                    },
                    isFavorite = isFav,
                    isFavoriteLocked = isFavoriteLocked,
                    onToggleFavorite = {
                        if (showStarForThis) {
                            viewModel.definirPoderFavorecido(if (isFav) null else poderId)
                        }
                    }
                )
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

    var showSuperAttrPicker by rememberSaveable { mutableStateOf(false) }
    var poolSuperAttr by rememberSaveable { mutableIntStateOf(0) }

    var showSuperPericiaPicker by rememberSaveable { mutableStateOf(false) }
    var poolSuperPericia by rememberSaveable { mutableIntStateOf(0) }

    var showSuperVantPicker by rememberSaveable { mutableStateOf(false) }
    var poolSuperVant by rememberSaveable { mutableIntStateOf(0) }

    var showBonusPericiaPicker by rememberSaveable { mutableStateOf(false) }
    var bonusPericiaBaseCost by rememberSaveable { mutableIntStateOf(0) }
    var bonusPericiaTotalCost by rememberSaveable { mutableIntStateOf(0) }
    var bonusPericiaNivel by rememberSaveable { mutableIntStateOf(0) }

    poderParaComprar?.let { poder ->
        val context2 = LocalContext.current

        if (!supersLiberados) {
            poderParaComprar = null
        } else {
            val nomeUpper = poder.nome.trim().uppercase()
            val poderIdEspecifico = when {
                nomeUpper == "ARMADURA" -> "sp_armor"
                nomeUpper == "RESISTÊNCIA" || nomeUpper == "RESISTENCIA" -> "sp_res"
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
                    when {
                        nome == "APARAR" -> {
                            viewModel.tentarInvestirSuper(
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
                        nome == "MOVIMENTAÇÃO" || nome == "MOVIMENTACAO" -> {
                            viewModel.tentarInvestirSuper(
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
                        nome == "ARMADURA" -> {
                            viewModel.tentarInvestirSuper(
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
                        nome == "RESISTÊNCIA" || nome == "RESISTENCIA" -> {
                            viewModel.tentarInvestirSuper(
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
                        nome == "SUPERATRIBUTO" || nome == "SUPER ATRIBUTO" -> {
                            poolSuperAttr = baseCost / 2
                            showSuperAttrPicker = true
                        }
                        nome == "SUPERPERÍCIA" || nome == "SUPER PERÍCIA" ||
                                nome == "SUPERPERICIA" || nome == "SUPER PERICIA" -> {
                            poolSuperPericia = baseCost
                            if (poolSuperPericia > 0) {
                                showSuperPericiaPicker = true
                            } else {
                                Toast.makeText(context2, "Limite deste poder já foi atingido.", Toast.LENGTH_SHORT).show()
                            }
                        }
                        nome == "SUPERVANTAGEM" || nome == "SUPER VANTAGEM" -> {
                            poolSuperVant = baseCost / 2
                            if (poolSuperVant > 0) {
                                showSuperVantPicker = true
                            }
                        }
                        nome == "BÔNUS DE PERÍCIA" ||
                                nome == "BÔNUS DE PERICIA" ||
                                nome == "BONUS DE PERÍCIA" ||
                                nome == "BONUS DE PERICIA" -> {
                            bonusPericiaBaseCost = baseCost
                            bonusPericiaTotalCost = custoTotal
                            bonusPericiaNivel = if (baseCost >= 4) 2 else 1
                            showBonusPericiaPicker = true
                        }
                        else -> {
                            viewModel.tentarInvestirSuper(
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
                    poderParaComprar = null
                },
                onDismiss = { poderParaComprar = null }
            )
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
        val vantagensDisponiveis: List<Vantagem> = listaVantagens.filter { v ->
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

                    listaPericias.forEach { per ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedPericia = per }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedPericia == per,
                                onCheckedChange = { checked ->
                                    if (checked) selectedPericia = per
                                }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(per.nome)
                        }
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

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun SuperPoderesContent(
    state: CriadorState,
    listaSuperPoderes: List<Poder>,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    SectionCard(
        title = "Superpoderes",
        expanded = expanded,
        onToggle = onToggle,
        icon = Icons.Filled.FlashOn
    ) {
        val showLista = booleanResource(R.bool.show_full_descriptions)

        SectionHeader(
            onHelpClick = null,
            centerText = "Pontos de Super: ${state.superPontosDisponiveis}",
            onCenterClick = onToggle,
            onListaCompletaClick = null,
            listaCompletaText = "Lista Completa"
        )

        SuperPoderesSection(
            state = state,
            listaSuperPoderes = listaSuperPoderes,
            expanded = expanded,
        )
    }
}
