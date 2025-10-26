package com.example.swadebuilder.ui.sections

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CollapsibleSection
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.SectionHeader
import com.example.swadebuilder.arcanoInfo
import com.example.swadebuilder.listaDeEstagios
import com.example.swadebuilder.listaPericias
import com.example.swadebuilder.mapaAtributosDisplay
import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.Poder
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.ui.dialogs.ChoiceDialog
import com.example.swadebuilder.ui.dialogs.MultipleSelectionDialog
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.loadJsonAsset
import com.example.swadebuilder.util.semAcentos
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json


// modelo de filtro
data class VantFilter(
    val origens: Set<String> = emptySet(),
    val estagios: Set<String> = emptySet(),
    val atributos: Set<String> = emptySet(),
    val pericias: Set<String> = emptySet()
) {
    fun isEmpty() = origens.isEmpty() && estagios.isEmpty() && atributos.isEmpty() && pericias.isEmpty()
    fun totalSelections() = origens.size + estagios.size + atributos.size + pericias.size
}

@Composable
fun VantFilterDialog(
    allOrigens: List<String>,
    allEstagios: List<String>,
    allAtributos: List<String>,
    allPericias: List<String>,
    current: VantFilter,
    onChange: (VantFilter) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filtros de Vantagens") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)            // primeiro fixa o tamanho máximo…
                    .verticalScroll(rememberScrollState()) // …depois habilita o scroll interno
                    .padding(end = 8.dp)
            ) {
                Text("Origem", fontWeight = FontWeight.Bold)
                allOrigens.forEach { o ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = o in current.origens,
                            onCheckedChange = {
                                val s = current.origens.toMutableSet()
                                if (it) s += o else s -= o
                                onChange(current.copy(origens = s))
                            }
                        )
                        Spacer(Modifier.size(4.dp))
                        Text(o)
                    }
                }
                Spacer(Modifier.size(8.dp))
                Text("Estágio", fontWeight = FontWeight.Bold)
                allEstagios.forEach { e ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = e in current.estagios,
                            onCheckedChange = {
                                val s = current.estagios.toMutableSet()
                                if (it) s += e else s -= e
                                onChange(current.copy(estagios = s))
                            }
                        )
                        Spacer(Modifier.size(4.dp))
                        Text(e)
                    }
                }
                Spacer(Modifier.size(8.dp))
                Text("Atributos", fontWeight = FontWeight.Bold)
                allAtributos.forEach { a ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = a in current.atributos,
                            onCheckedChange = {
                                val s = current.atributos.toMutableSet()
                                if (it) s += a else s -= a
                                onChange(current.copy(atributos = s))
                            }
                        )
                        Spacer(Modifier.size(4.dp))
                        Text(a)
                    }
                }
                Spacer(Modifier.size(8.dp))
                Text("Perícias", fontWeight = FontWeight.Bold)
                allPericias.forEach { p ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = p in current.pericias,
                            onCheckedChange = {
                                val s = current.pericias.toMutableSet()
                                if (it) s += p else s -= p
                                onChange(current.copy(pericias = s))
                            }
                        )
                        Spacer(Modifier.size(4.dp))
                        Text(p)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VantagensContent(
    state: CriadorState,
    onOpenVantagensDetail: (String) -> Unit,
    onTogglePoderes: () -> Unit
) {
    val context = LocalContext.current

    // 1) carrega lista de vantagens
    val listaVantagens: List<Vantagem> = remember {
        val jsonString = context.assets.open("Vantagens.json")
            .bufferedReader().use { it.readText() }
        Json { ignoreUnknownKeys = true; explicitNulls = false }
            .decodeFromString(ListSerializer(Vantagem.serializer()), jsonString)
    }

    // Estados principais
    var showHelp            by rememberSaveable { mutableStateOf(false) }
    var filter              by remember          { mutableStateOf(VantFilter()) }    // <<== aqui
    var showFilterDialog    by rememberSaveable { mutableStateOf(false) }
    var tempErrorMsg        by remember          { mutableStateOf("") }
    var showTempError       by remember          { mutableStateOf(false) }
    var selectedReqs        by remember          { mutableStateOf<List<String>>(emptyList()) }
    var pendingVantagem     by remember          { mutableStateOf<Vantagem?>(null) }
    var showChoiceDialog    by rememberSaveable { mutableStateOf(false) }
    var pendingNovosPoderes by rememberSaveable { mutableStateOf<Vantagem?>(null) }
    var showNovosPoderesDialog by rememberSaveable { mutableStateOf(false) }
    var dialogMostrandoAntecedente by remember   { mutableStateOf<Vantagem?>(null) }
    var subOpcaoSelecionada by rememberSaveable { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val expandedMap = remember {
        Categoria.entries.associateWith { mutableStateOf(false) }
    }

    // agrupamentos e filtros básico/super
    val categoriasBy = remember { listaVantagens.groupBy { it.categoria } }
    val categoriasFiltradas = remember(state.modoSupers) {
        categoriasBy.mapValues { (_, v) ->
            if (state.modoSupers) v else v.filter { it.origem.equals("BASICO", true) }
        }
    }

    // contar iniciais para remoção
    var initialCount by rememberSaveable { mutableIntStateOf(0) }
    LaunchedEffect(state.emProgresso) {
        if (state.emProgresso) initialCount = state.vantagensSelecionadas.size
    }
    val locked = state.progresso > 0 && !state.emProgresso

    val showLista = booleanResource(com.example.swadebuilder.R.bool.show_lista_completa)


    Column(modifier = Modifier.fillMaxWidth()) {
        // cabeçalho
        SectionHeader(
            onHelpClick          = { showHelp = true },
            centerText           = "Pontos restantes: ${state.pontosVantagem}",
            onListaCompletaClick = if (showLista) ({ onOpenVantagensDetail("") }) else null,
            listaCompletaText    = "Lista Completa"
        )


        if (showHelp) {
            AlertDialog(
                onDismissRequest = { showHelp = false },
                title  = { Text("Como funciona") },
                text   = { Text("Toque nas vantagens para ver requisitos, ...\nUse filtro para refinar.") },
                confirmButton = {
                    TextButton(onClick = { showHelp = false }) { Text("OK") }
                }
            )
        }

        Spacer(Modifier.size(8.dp))

        // botão de filtro
        Text(
            text = if (filter.isEmpty()) "Filtrar vantagens" else "Filtros (${filter.totalSelections()})",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clickable { showFilterDialog = true }
        )

        // diálogo de filtro
        if (showFilterDialog) {
            val allOrigens = listaVantagens.map { it.origem.uppercase() }.distinct()
            val allEstagios = listaDeEstagios.map { it.nome }
            val allAtributos = mapaAtributosDisplay.values.toList()
            val requiredPericias = listaVantagens.flatMap { vant ->
                vant.requisitos.periciaMin.keys +
                        vant.requisitos.periciaMinOpcional.keys +
                        if (vant.vinculadoPericia) vant.choiceOptions else emptyList()
            }.distinct()
            val allPericias = listaPericias
                .map { it.nome }
                .filter { it in requiredPericias }
            VantFilterDialog(
                allOrigens, allEstagios, allAtributos, allPericias,
                current = filter,
                onChange = { filter = it },
                onDismiss = { showFilterDialog = false }
            )
        }

        Spacer(Modifier.size(8.dp))

        // chips de selecionadas...
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement   = Arrangement.spacedBy(8.dp)
        ) {
            state.vantagensSelecionadas.forEachIndexed { index, vant ->
                val isRacialFree = vant.nome.keyify() in state.vantagensAutomaticas.map { it.keyify() }
                val requiredByAnother = state.vantagensSelecionadas.any { other ->
                    other != vant && other.requisitos.vantagensPrevias.any { req ->
                        req.uppercase().semAcentos().trim() ==
                                vant.nome.uppercase().semAcentos().trim()
                    }
                }
                val canRemove = !locked
                        && index >= initialCount
                        && index >= state.frozenAdvCount
                        && !isRacialFree
                        && !requiredByAnother
                        && vant.nome != "Superpoderes"

                AssistChip(
                    onClick = {
                        if (canRemove) {
                            if (vant.nome.contains("Pontos de Poder", true)) {
                                state.removerPontosDePoder(vant)
                            } else {
                                state.removeVantagemDinheiro(vant)
                                state.vantagensSelecionadas.remove(vant)
                            }
                            state.pontosVantagem++
                            state.rebuildAllPericiaStacks()
                        }
                    },
                    enabled = canRemove,
                    label   = { Text(vant.choice?.let { "${vant.nome} ($it)" } ?: vant.nome) },
                    leadingIcon = { Icon(Icons.Default.Close, contentDescription = null) }
                )
            }
        }

        Spacer(Modifier.size(16.dp))

        Card(
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF4E6))
        ) {
            Column(Modifier.padding(8.dp)) {
                when {
                    showTempError -> Text(tempErrorMsg, color = MaterialTheme.colorScheme.error)
                    selectedReqs.isEmpty() -> Text(
                        "Selecione uma vantagem para ver requisitos",
                        style = MaterialTheme.typography.bodySmall
                    )
                    else -> selectedReqs.forEach { req ->
                        Text("• $req", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        Spacer(Modifier.size(16.dp))

        // seções por categoria
        categoriasFiltradas.forEach { (cat, lista) ->
            if (state.modoSupers && cat == Categoria.PODER) return@forEach
            val expanded = expandedMap.getValue(cat)
            CollapsibleSection(
                title    = cat.name,
                expanded = expanded.value,
                onToggle = { expanded.value = !expanded.value }
            ) {
                val scroll = rememberScrollState()
                Column(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                        .verticalScroll(scroll)
                        .padding(start = 8.dp, bottom = 8.dp)
                ) {
                    lista
                        // básico vs superpoderes
                        .filter { vant ->
                            if (!state.modoSupers) true
                            else vant.id != "antecedente_arcano" &&
                                    vant.requisitos.vantagensPrevias.isEmpty()
                        }
                        // especialista só com profissional
                        .filter { vant ->
                            vant.categoria == cat &&
                                    (vant.nome.keyify() != "especialista" ||
                                            state.vantagensSelecionadas.any {
                                                it.nome.keyify() == "profissional"
                                            })
                        }
                        // aplica filtros compostos
                        .filter { vant ->
                            // origem
                            if (filter.origens.isNotEmpty() &&
                                vant.origem.uppercase() !in filter.origens) return@filter false
                            // estágio
                            if (filter.estagios.isNotEmpty() &&
                                vant.requisitos.estagio !in filter.estagios) return@filter false
                            // atributos
                            if (filter.atributos.isNotEmpty() &&
                                filter.atributos.intersect(vant.requisitos.atributoMin.keys).isEmpty())
                                return@filter false
                            // perícias
                            if (filter.pericias.isNotEmpty()) {
                                val reqMin = vant.requisitos.periciaMin.keys
                                val reqOpt = vant.requisitos.periciaMinOpcional.keys
                                val vinc   = if (vant.vinculadoPericia) vant.choiceOptions else emptyList()
                                if (filter.pericias.intersect(reqMin + reqOpt + vinc).isEmpty())
                                    return@filter false
                            }
                            true
                        }
                        .forEach { vant ->
                            // monta requisitos...
                            val reqList = buildList {
                                listaDeEstagios.firstOrNull {
                                    it.nome.equals(vant.requisitos.estagio, true)
                                }?.let { add("Estágio ≥ ${it.nome}") }
                                vant.requisitos.atributoMin.forEach { (a,m) -> add("$a ≥ $m") }
                                vant.requisitos.periciaMin.forEach  { (p,m) -> add("$p ≥ $m") }
                                if (vant.requisitos.periciaMinOpcional.isNotEmpty()) {
                                    add(vant.requisitos.periciaMinOpcional.entries.joinToString(" ou ") {
                                        "${it.key} d${it.value}+"
                                    })
                                }
                                vant.requisitos.vantagensPrevias.forEach { add("Pré‐requisito: $it") }
                                if (vant.requisitos.exigeCS) add("Requer Carta Selvagem")
                                if (vant.nome.trim().removeSuffix(":").keyify() == "profissional") {
                                    add("Traço no teto máximo: escolha entre ${state.maxedTraits.joinToString()}")
                                }
                            }
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        enabled = !locked,
                                        onClick = { selectedReqs = reqList },
                                        onDoubleClick = {
                                            if (!locked) {
                                                when {
                                                    state.pontosVantagem <= 0 -> {
                                                        tempErrorMsg = "Sem PV disponível"
                                                        showTempError = true
                                                    }
                                                    !state.podeSelecionar(vant) -> {
                                                        tempErrorMsg = "Faltam requisitos para '${vant.nome}'"
                                                        showTempError = true
                                                    }
                                                    vant.vinculadoPericia -> {
                                                        pendingVantagem = vant
                                                        showChoiceDialog = true
                                                    }
                                                    vant.id == "antecedente_arcano" -> {
                                                        dialogMostrandoAntecedente = vant
                                                    }
                                                    vant.id == "novos_poderes" -> {
                                                        pendingNovosPoderes = vant
                                                        showNovosPoderesDialog = true
                                                    }
                                                    else -> {
                                                        if (vant.nome.contains("Pontos de Poder", true)) {
                                                            state.comprarPontoDePoder(vant)
                                                        } else {
                                                            state.applyVantagemDinheiro(vant)
                                                            state.vantagensSelecionadas += vant
                                                        }
                                                        state.pontosVantagem--
                                                        state.rebuildAllPericiaStacks()

                                                        // --- NOVO BLOCO: consome PV pendente vindo de XP ---
                                                        if (state.pvFromXpOutstanding > 0) {
                                                            state.pvFromXpOutstanding -= 1
                                                            if (state.pvFromXpOutstanding == 0) {
                                                                state.overrideStageForVantagem = null
                                                                state.openVantagensAfterGrant = false
                                                            }
                                                        }

                                                    }
                                                }
                                                scope.launch {
                                                    delay(2_000)
                                                    showTempError = false
                                                }
                                            }
                                        }
                                    )
                                    .alpha(if (!locked && state.podeSelecionar(vant)) 1f else 0.3f)
                                    .padding(vertical = 8.dp, horizontal = 4.dp)
                            ) {
                                Text(vant.nome, Modifier.weight(1f), fontWeight = FontWeight.Medium)
                                // depois
                                if (showLista) {
                                    Icon(
                                        Icons.Default.Visibility,
                                        contentDescription = "Detalhes",
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clickable { onOpenVantagensDetail(vant.nome) }
                                    )
                                }

                            }
                        }
                }
            }
            Spacer(Modifier.size(8.dp))
        }

        // ---------------------------------------------------------------
        // 10) Dialog “Escolha Antecedente Arcano” (Dom, Magia, Milagres, Psiônicos, Ciência)
        // ---------------------------------------------------------------
        if (dialogMostrandoAntecedente != null) {
            val vantOriginal = dialogMostrandoAntecedente!!
            val opcoesArcano: List<String> = vantOriginal.choiceOptions

            AlertDialog(
                onDismissRequest = {
                    dialogMostrandoAntecedente = null
                    subOpcaoSelecionada = null
                },
                title = { Text("Escolha o tipo de ${vantOriginal.nome}") },
                text = {
                    Column {
                        opcoesArcano.forEach { opcao ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { subOpcaoSelecionada = opcao }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                androidx.compose.material3.RadioButton(
                                    selected = (subOpcaoSelecionada == opcao),
                                    onClick = { subOpcaoSelecionada = opcao }
                                )
                                Spacer(Modifier.size(8.dp))
                                Text(opcao)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        enabled = (subOpcaoSelecionada != null),
                        onClick = {
                            // Copia a vantagem “antecedente_arcano” com a escolha em.choice
                            val novaVantagem = vantOriginal.copy(
                                choice = subOpcaoSelecionada
                            )

                            if (state.podeSelecionar(novaVantagem)) {
                                state.vantagensSelecionadas += novaVantagem
                                state.pontosVantagem--
                                state.rebuildAllPericiaStacks()
                            }
                            dialogMostrandoAntecedente = null
                            subOpcaoSelecionada = null
                        }
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        dialogMostrandoAntecedente = null
                        subOpcaoSelecionada = null
                    }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        // ---------------------------------------------------------------
        // 11) Diálogo ChoiceDialog genérico (casos “vinculado_pericia” ou outras escolhas)
        // ---------------------------------------------------------------
        if (showChoiceDialog && pendingVantagem != null) {
            state.identifyMaxedTraits()
            val vant = pendingVantagem!!
            val validOptions: List<String> = when {
                vant.nome.equals("ARMA PREDILETA", ignoreCase = true) -> {
                    listaPericias
                        .filter { per -> state.rawTotal(per) >= 8 }
                        .map { it.nome }
                }
                vant.nome.equals("ARMA PREDILETA APRIMORADA", ignoreCase = true) -> {
                    state.vantagensSelecionadas
                        .filter { it.nome.equals("ARMA PREDILETA", ignoreCase = true) && it.choice != null }
                        .mapNotNull { it.choice }
                        .distinct()
                }
                vant.nome.trim().removeSuffix(":").keyify() == "profissional" -> {
                    vant.choiceOptions.filter { it in state.maxedTraits }
                }
                vant.nome.keyify() == "especialista" -> {
                    state.vantagensSelecionadas
                        .filter { it.nome.keyify() == "profissional" && it.choice != null }
                        .mapNotNull { it.choice }
                }
                vant.maxSelections > 0 -> {
                    val used = state.vantagensSelecionadas
                        .filter { it.nome.equals(vant.nome, ignoreCase = true) && it.choice != null }
                        .mapNotNull { it.choice }
                    vant.choiceOptions.filter { it !in used }
                }
                else -> vant.choiceOptions
            }

            if (validOptions.isEmpty()) {
                LaunchedEffect(vant) {
                    tempErrorMsg = "Nenhuma opção disponível para escolher"
                    showTempError = true
                    delay(2_000)
                    showTempError = false
                    showChoiceDialog = false
                    pendingVantagem = null
                }
            } else {
                ChoiceDialog(
                    options   = validOptions,
                    onConfirm = { choice ->
                        state.vantagensSelecionadas += vant.copy(choice = choice)
                        state.pontosVantagem--
                        state.rebuildAllPericiaStacks()
                        showChoiceDialog = false
                        pendingVantagem = null
                    },
                    onDismiss = {
                        showChoiceDialog = false
                        pendingVantagem = null
                    }
                )
            }
        }

        // ---------------------------------------------------------------
        // 12) Único bloco: “MultipleSelectionDialog” para NOVOS PODERES (comprável infinitas vezes)
        // ---------------------------------------------------------------
        if (showNovosPoderesDialog && pendingNovosPoderes != null) {
            // Captura local não nulo para que o compilador faça smart cast:
            val vant = pendingNovosPoderes!!

            // 1) Carrega lista completa de poderes do JSON “poderes.json”
            val allPoderes: List<Poder> =
                LocalContext.current.loadJsonAsset("poderes.json")

            // 2) Filtra apenas poderes cujo estágio seja ≤ estágio atual do personagem
            val curEst = listaPericias.indexOfFirst { it.nome == state.estagioAtual().nome }
            val disponiveis = allPoderes
                .filter { poder ->
                    listaPericias.indexOfFirst { it.nome == poder.estagio } <= curEst
                }
                .map { it.nome }
                .filter { nome -> nome !in state.poderesSelecionados }

            MultipleSelectionDialog(
                title = "Escolha 2 novos poderes",
                options = disponiveis,
                maxSelections = 2,
                onConfirm = { escolhas ->
                    // ----------------------------------------------------------
                    // Aqui extraímos qual “Antecedente Arcano” o jogador já comprou:
                    // Procuramos dentro de state.vantagensSelecionadas o Vantagem
                    // cujo id == "antecedente_arcano" e pegamos.choice (Dom, Magia etc).
                    // ----------------------------------------------------------
                    val escolhidoArcano = state.vantagensSelecionadas
                        .firstOrNull { it.id == "antecedente_arcano" }
                        ?.choice
                        ?: "" // Se der algo errado, ficará vazio, mas só irá funcionar se antecedente_arcano já estiver comprado.

                    // Normalizamos o texto para gerar a mesma chave que existe em arcanoInfo.json
                    // (ex: "DOM", "MAGIA", "MILAGRES", "PSIONICOS", "CIENCIAESTRANHA", etc.)
                    val versionKey = escolhidoArcano
                        .uppercase()
                        .semAcentos()
                        .trim()

                    // ----------------------------------------------------------
                    // 3) Obtém quantos slots iniciais esse Arcano deveria ter (de arcanoInfo.json)
                    // ----------------------------------------------------------
                    val initialSlots = arcanoInfo[versionKey]?.first ?: 0

                    // ----------------------------------------------------------
                    // 4) Garante que exista uma lista de slots no estado para esse versionKey
                    // ----------------------------------------------------------
                    val slots = state.poderSlotsPorArcano.getOrPut(versionKey) {
                        mutableStateListOf<String?>().apply {
                            repeat(initialSlots) { add(null) }
                        }
                    }

                    // ----------------------------------------------------------
                    // 5) Preenche cada slot vazio com um dos poderes escolhidos
                    // ----------------------------------------------------------
                    escolhas.forEach { poder ->
                        val firstEmpty = slots.indexOfFirst { it == null }
                        if (firstEmpty >= 0) {
                            slots[firstEmpty] = poder
                        } else {
                            // Se não houver mais slot “nulo”, adicionamos ao final
                            slots.add(poder)
                        }
                    }

                    // Atualiza o mapa de slots (embora getOrPut já tenha feito internamente, mas reforça)
                    state.poderSlotsPorArcano[versionKey] = slots

                    // ----------------------------------------------------------
                    // 6) Atualiza a lista geral de poderes selecionados no estado
                    // ----------------------------------------------------------
                    state.poderesSelecionados.clear()
                    state.poderesSelecionados.addAll(slots.filterNotNull())

                    // ----------------------------------------------------------
                    // 7) Finalmente, registra a própria vantagem “novos_poderes” e decrementa PV
                    // ----------------------------------------------------------
                    state.vantagensSelecionadas += vant
                    state.pontosVantagem--
                    state.rebuildAllPericiaStacks()

                    // Como “novos_poderes” pode ser comprado infinitas vezes, não
                    // estamos a bloquear; basta deixá‐lo em pendingNovosPoderes para
                    // poder repetir. Mas podemos limpar aqui se quisermos reabrir o diálogo
                    // sempre ao clicar novamente.
                    showNovosPoderesDialog = false
                    pendingNovosPoderes = null
                },
                onDismiss = {
                    showNovosPoderesDialog = false
                    pendingNovosPoderes = null
                }
            )
        }
    }
}
