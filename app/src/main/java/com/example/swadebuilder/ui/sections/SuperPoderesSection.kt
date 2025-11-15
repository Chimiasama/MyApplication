package com.example.swadebuilder.ui.sections

import android.os.Build
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Checkbox
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.R
import com.example.swadebuilder.SectionCard
import com.example.swadebuilder.SectionHeader
import com.example.swadebuilder.SuperPoder
import com.example.swadebuilder.model.CriadorViewModel
import com.example.swadebuilder.model.PowerEffect
import com.example.swadebuilder.ui.dialogs.SuperAtributosPickerDialog
import kotlin.math.roundToInt

// ==========================================================
// DIALOG: Compra de Superpoder (base discreta + modificadores)
// ==========================================================
@Composable
fun BuySuperPowerDialog(
    poder: SuperPoder,
    pontosDisponiveis: Int,
    limitePorPoder: Int,
    onConfirm: (baseCost: Int, totalCost: Int) -> Unit,
    onDismiss: () -> Unit
) {
    // ---------- parse discreto do custoBase ----------
    fun parseCustoBaseOptions(raw: String?): List<Int> {
        if (raw.isNullOrBlank()) return listOf(1)
        val s = raw.trim()

        // faixa com EN DASH “a–b”
        val enDash = '–'
        if (s.contains(enDash)) {
            val parts = s.split(enDash).map { it.trim() }
            val a = parts.getOrNull(0)?.toIntOrNull()
            val b = parts.getOrNull(1)?.toIntOrNull()
            if (a != null && b != null) {
                val start = minOf(a, b)
                val end   = maxOf(a, b)
                return (start..end).toList()
            }
        }

        // lista “x/y/z/...”
        if (s.contains('/')) {
            return s.split('/')
                .mapNotNull { it.trim().toIntOrNull() }
                .distinct()
                .sorted()
        }

        // valor único
        return s.toIntOrNull()?.let { listOf(it) } ?: listOf(1)
    }

    // opções declaradas no JSON para este poder
    val baseOptionsAll = remember(poder.custoBase) { parseCustoBaseOptions(poder.custoBase) }
    val baseMinDeclarado = baseOptionsAll.minOrNull() ?: 1
    val baseMaxDeclarado = baseOptionsAll.maxOrNull() ?: baseMinDeclarado

    // ---------- modificadores ----------
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

    // ---------- CAP: por poder + pool disponível ----------
    val totalCap = minOf(limitePorPoder, pontosDisponiveis) // teto do TOTAL (base + mods)

    // reduza as opções declaradas pelo teto (considerando custo dos mods)
    val capParaBase = (totalCap - modCost).coerceAtLeast(baseMinDeclarado)
    val allowedBaseOptions = baseOptionsAll
        .filter { it in baseMinDeclarado..minOf(baseMaxDeclarado, capParaBase) }
        .ifEmpty { listOf(baseMinDeclarado.coerceAtMost(capParaBase)) }

    // ---------- Slider por ÍNDICE das opções discretas ----------
    var baseIdx by rememberSaveable(allowedBaseOptions) { mutableIntStateOf(0) }
    val baseCost = allowedBaseOptions.getOrElse(baseIdx) { allowedBaseOptions.last() }

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
                Text("Custo base: $baseCost")

                Slider(
                    value = baseIdx.toFloat(),
                    onValueChange = { novo ->
                        val idx = novo.roundToInt().coerceIn(0, allowedBaseOptions.lastIndex)
                        baseIdx = idx
                    },
                    valueRange = 0f..allowedBaseOptions.lastIndex.toFloat(),
                    steps = (allowedBaseOptions.size - 1).coLeastOne() - 1,
                    modifier = Modifier.fillMaxWidth()
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
            val totalAtual = baseCost + modCost
            val podeConfirmar = totalAtual in allowedBaseOptions.first()..totalCap
            TextButton(
                enabled = podeConfirmar,
                onClick = { onConfirm(baseCost, totalAtual) }
            ) { Text("Comprar ($totalAtual)") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

// helpers locais para steps/slider
private fun Int.coLeastOne(): Int = if (this < 1) 1 else this
private fun Int.coLeastZero(): Int = if (this < 0) 0 else this

// ==========================================================
// SECTION: Lista e compra de Superpoderes (com picker 2:1)
// ==========================================================
@Composable
fun SuperPoderesSection(
    state: CriadorState,
    listaSuperPoderes: List<SuperPoder>,
    expanded: Boolean,
    viewModel: CriadorViewModel = viewModel()
) {
    if (!expanded) return

    var poderParaComprar by remember { mutableStateOf<SuperPoder?>(null) }
    val nivelAtual = state.superNivelCampanha ?: 1
    // criação básica precisa estar pronta
    val supersLiberados = state.creationComplete()
    // se já gastou qualquer ponto de super, o nível trava
    val jaInvestiuSupers = state.superPontosDisponiveis < state.superPontosTotais
    val podeEditarNivel = supersLiberados && !jaInvestiuSupers

    var showNivelDialog by rememberSaveable { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // função helper: aplicar o nível (igual ao que o slider fazia)
    fun aplicarNivelSuper(novoNivel: Int) {
        val nivel = novoNivel.coerceIn(1, 5)
        state.superNivelCampanha = nivel

        val total  = 15 * nivel           // 15 pts por nível
        val limite = 5  * nivel           // 1/3 do total

        state.superPontosTotais   = total
        state.superLimite         = limite
        state.superLimitePorPoder = limite

        // recalcula gastos pelos poderes (mais robusto)
        val gastos = state.gastosPorPoder.values.sum()
        state.superPontosDisponiveis = (total - gastos).coerceAtLeast(0)
    }

    LaunchedEffect(Unit) {
        // Inicialização mínima se veio “cru” do load
        if ((state.superNivelCampanha ?: 0) == 0 || state.superPontosTotais == 0) {
            val v = state.vantagensSelecionadas.firstOrNull {
                it.nome.equals("Superpoderes", ignoreCase = true)
            }
            if (v != null) {
                aplicarNivelSuper(1)
            }
        }
    }

    Column(Modifier.fillMaxWidth().padding(8.dp)) {
        // 1) chips dos poderes comprados
        if (state.superPoderesComprados.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement   = Arrangement.spacedBy(4.dp)
            ) {
                state.superPoderesComprados.forEach { p ->
                    AssistChip(
                        onClick = { state.removerSuperPoder(p) },
                        label = { Text("${p.nome} (+${p.custo})") },
                        leadingIcon = { Icon(Icons.Filled.Close, contentDescription = "Remover") }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        // 2) seleção de nível (no lugar do slider)
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
                    text = "Nível atual: $nivelAtual",
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Pontos: ${state.superPontosTotais} • Limite por poder: ${state.superLimite}",
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
        } else if (jaInvestiuSupers) {
            Text(
                "Para alterar o nível, devolva todos os pontos de superpoder já gastos.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(Modifier.height(4.dp))
        Text("Pontos disponíveis: ${state.superPontosDisponiveis}")
        Text("Limite de superpoderes: ${state.superLimite}")

        Spacer(Modifier.height(8.dp))

        // 3) lista rolável de superpoderes para comprar
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 220.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
            items(listaSuperPoderes, key = { it.nome }) { poder ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        // clique só funciona se supersLiberados == true
                        .clickable(enabled = supersLiberados) { poderParaComprar = poder }
                        .padding(vertical = 6.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(poder.nome, Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                    Icon(Icons.Filled.FlashOn, contentDescription = "Comprar")
                }
                HorizontalDivider()
            }
        }
    }

    // 4) diálogo de escolha de nível
    if (showNivelDialog) {
        AlertDialog(
            onDismissRequest = { showNivelDialog = false },
            title = { Text("Escolher nível de Superpoderes") },
            text = {
                Column(Modifier.fillMaxWidth()) {
                    (1..5).forEach { nivel ->
                        val total  = 15 * nivel
                        val limite = 5  * nivel
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

    // 5) diálogo de compra (base/mods) + casos especiais
    var showSuperAttrPicker by rememberSaveable { mutableStateOf(false) }
    var poolSuperAttr by rememberSaveable { mutableIntStateOf(0) }

    poderParaComprar?.let { poder ->
        // Se por algum motivo o estado mudar e os supers forem bloqueados,
        // garantimos que o diálogo não abre / é fechado.
        if (!supersLiberados) {
            poderParaComprar = null
        } else {
            BuySuperPowerDialog(
                poder = poder,
                pontosDisponiveis = state.superPontosDisponiveis,
                limitePorPoder = state.superLimitePorPoder,
                onConfirm = { baseCost, custoTotal ->
                    val nome = poder.nome.trim().uppercase()
                    when {
                        nome == "APARAR" -> {
                            viewModel.tentarInvestirSuper(
                                poderId = "sp_aparar",
                                custo   = custoTotal,
                                efeito  = PowerEffect.BonusAparar(baseCost)
                            )
                        }

                        nome == "ARMADURA" -> {
                            viewModel.tentarInvestirSuper(
                                poderId = "sp_armor",
                                custo   = custoTotal,
                                efeito  = PowerEffect.BonusArmadura(custoTotal * 2)
                            )
                        }

                        nome == "RESISTÊNCIA" || nome == "RESISTENCIA" -> {
                            viewModel.tentarInvestirSuper(
                                poderId = "sp_res",
                                custo   = custoTotal,
                                efeito  = PowerEffect.BonusResistencia(custoTotal)
                            )
                        }

                        // Super Atributo: custo vira pool/2 em steps
                        nome == "SUPERATRIBUTO" || nome == "SUPER ATRIBUTO" -> {
                            poolSuperAttr = custoTotal / 2
                            showSuperAttrPicker = true
                        }

                        else -> {
                            // fallback simples (apenas registrar gasto com id do poder)
                            state.comprarSuperPoder(poder.nome, custoTotal)
                        }
                    }
                    poderParaComprar = null
                },
                onDismiss = { poderParaComprar = null }
            )
        }
    }

    // 6) picker de SuperAtributos (2:1)
    if (showSuperAttrPicker) {
        SuperAtributosPickerDialog(
            state = state,
            poolInicial = poolSuperAttr,
            onConfirmDistribuicao = { mapa ->
                // mapa: attrKey -> stepsSolicitados
                mapa.forEach { (attrKey, stepsSolicitados) ->
                    val poderId = "sp_attr_${attrKey.uppercase()}"

                    // custo pretendido (2:1)
                    val custoPretendido = stepsSolicitados * 2

                    // respeita limite por poder e saldo disponível
                    val gastoAtual     = state.gastosPorPoder[poderId] ?: 0
                    val limitePorPoder = viewModel.perPowerLimit(poderId)
                    val restoPorPoder  = (limitePorPoder - gastoAtual).coerceAtLeast(0)
                    val restoDePool    = state.superPontosDisponiveis.coerceAtLeast(0)

                    val custoAplicavel  = minOf(custoPretendido, restoPorPoder, restoDePool)
                    val stepsAplicaveis = (custoAplicavel / 2).coerceAtLeast(0)

                    if (stepsAplicaveis > 0) {
                        // aplica overlay de superatributo
                        val atual = state.superAtributoIncs[attrKey.uppercase()] ?: 0
                        state.superAtributoIncs[attrKey.uppercase()] = atual + stepsAplicaveis

                        // registra gasto e recalcula saldo
                        state.registrarGastoDePoder(poderId, stepsAplicaveis * 2)
                    }
                }

                // recálculo rápido do saldo de SP
                state.superPontosDisponiveis =
                    (state.superPontosTotais - state.gastosPorPoder.values.sum()).coerceAtLeast(0)

                showSuperAttrPicker = false
            },
            onDismiss = { showSuperAttrPicker = false }
        )
    }
}

// ==========================================================
// CONTENT: Card com título/ícone/cabeçalho e a Section acima
// ==========================================================
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun SuperPoderesContent(
    state: CriadorState,
    listaSuperPoderes: List<SuperPoder>,
    expanded: Boolean,
    onToggle: () -> Unit,
    onOpenSuperPoderesDetail: () -> Unit,
    onHelpClick: () -> Unit
) {
    SectionCard(
        title    = "Superpoderes",
        expanded = expanded,
        onToggle = onToggle,
        icon     = Icons.Filled.FlashOn
    ) {
        val showLista = booleanResource(R.bool.show_lista_completa)

        SectionHeader(
            onHelpClick          = onHelpClick,
            centerText           = "Pontos de Super: ${state.superPontosDisponiveis}",
            onCenterClick        = onToggle,
            onListaCompletaClick = if (showLista) onOpenSuperPoderesDetail else null,
            listaCompletaText    = "Lista Completa"
        )

        SuperPoderesSection(
            state             = state,
            listaSuperPoderes = listaSuperPoderes,
            expanded          = expanded
        )
    }
}
