package com.example.swadebuilder.ui.sections

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.R
import com.example.swadebuilder.model.ArcaneConfig
import com.example.swadebuilder.model.Poder
import com.example.swadebuilder.model.getActiveOrigins
import com.example.swadebuilder.model.loadJsonAsset
import com.example.swadebuilder.model.powerAssetOriginKey
import com.example.swadebuilder.normAAKey
import com.example.swadebuilder.toArcanoKey
import com.example.swadebuilder.ui.components.ExpandableSearchFilter
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.ptBrCollator
import com.example.swadebuilder.util.semAcentos
import com.example.swadebuilder.util.toFancyTitleCase
import kotlinx.serialization.Serializable

@Serializable
private data class DominioJson(val nome: String, val poderes: List<String>)

private fun custoParaPenalidadeTexto(custo: String): String {
    val clean = custo.trim()
    clean.toIntOrNull()?.let { base -> return "-${(base + 1) / 2}" }
    if (clean.contains("/")) {
        return clean.split("/").joinToString("/") { p ->
            p.replace("+", "").trim().toIntOrNull()?.let { "-${(it + 1) / 2}" } ?: "—"
        }
    }
    if (clean.endsWith("+")) clean.removeSuffix("+").toIntOrNull()?.let { return "-${(it + 1) / 2}+" }
    if (clean.startsWith("+")) clean.removePrefix("+").toIntOrNull()?.let { return "-${(it + 1) / 2}" }
    return "—"
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PoderesSection(
    state: CriadorState,
    arcanoInfoMap: Map<String, Triple<Int, Int, String>>,
    onShowMessage: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val allowLongTexts = booleanResource(R.bool.enable_long_texts)

    val locked = state.criacaoBasicaCongeladaComXp

    // Identify active Arcane Backgrounds
    val arcanosAtivos = remember(state.vantagensSelecionadas, state.tropoSelecionado, state.compendioArteDaGuerraAtivo, state.ancestralidade) {
        val ativos = state.vantagensSelecionadas.mapNotNull { it.toArcanoKey() }.toMutableList()
        if (state.ancestralidade.keyify() == "TRANSMORFOS") {
            ativos.add("DOM")
        }
        if (state.compendioArteDaGuerraAtivo && state.tropoSelecionado?.id == "tropo_elementalista") {
            ativos.add("ELEMENTALISTA")
        }
        if (state.compendioArteDaGuerraAtivo && !state.isFeralAdgSelecionado() && (state.tropoSelecionado?.tecnicasIniciais ?: 0) > 0) {
            ativos.add("MESTRE DO CHI")
        }
        ativos.distinct()
    }

    if (arcanosAtivos.isEmpty()) return

    // Ensure slot lists are initialized and resized correctly for all active backgrounds
    LaunchedEffect(arcanosAtivos, state.vantagensSelecionadas.size, state.tropoSelecionado) {
        arcanosAtivos.forEach { arcKeyRaw ->
            val arcKey = arcKeyRaw.normAAKey()
            if (state.usaPoderesDisponiveisPorEstagio(arcKey)) {
                state.poderSlotsPorArcano.remove(arcKey)
                return@forEach
            }
            val slotsCount = state.getEffectiveSlotsCountForArcano(arcKey)
            val existente = state.poderSlotsPorArcano[arcKey]

            if (existente == null) {
                val nova = mutableStateListOf<String?>().apply { repeat(slotsCount) { add(null) } }
                state.poderSlotsPorArcano[arcKey] = nova
            } else {
                if (existente.size < slotsCount) {
                    while (existente.size < slotsCount) { existente.add(null) }
                } else if (existente.size > slotsCount) {
                    while (existente.size > slotsCount) { existente.removeAt(existente.lastIndex) }
                }
            }
        }
    }

    val powerCache: Map<String, List<Poder>> by androidx.compose.runtime.produceState(initialValue = emptyMap()) {
        val origins = listOf("basico", "fantasia", "scifi", "horror", "deadlands", "pathfinder", "crystal", "sol_vapor", "wiseguys", "adg")
        val map = mutableMapOf<String, List<Poder>>()

        origins.forEach { org ->
            val list = runCatching { context.loadJsonAsset<List<Poder>>("${org}_poderes.json") }.getOrElse { emptyList() }
            map[org.uppercase()] = list
        }

        // SUPER special case: avoid conflict with specialized Super Powers file
        val superBaseList = runCatching { context.loadJsonAsset<List<Poder>>("super_poderes_base.json") }.getOrElse { emptyList() }
        map["SUPER"] = superBaseList

        // ADG special case (if adg_tecnicas_chi is preferred but adg_poderes also exists now, we might want to merge or prefer one)
        // Keeping existing logic but ensuring "ARTE DA GUERRA" key is set.
        // If adg_poderes.json was loaded above into map["ADG"], this might overwrite it with tecnicas_chi or vice versa depending on intent.
        // The prompt implies strict referencing. ADG usually means Tecnicas Chi.
        // However, if we created adg_poderes.json, maybe we should load it too?
        // Let's load tecnicas_chi separate and merge if needed, or just prioritize tecnicas_chi as it is the setting specific power set.
        val adgChiList = runCatching { context.loadJsonAsset<List<Poder>>("adg_tecnicas_chi.json") }.getOrElse { emptyList() }
        val adgStandardList = map["ADG"] ?: emptyList()
        val combinedAdg = (adgStandardList + adgChiList).distinctBy { it.id }

        map["ADG"] = combinedAdg
        map["ARTE DA GUERRA"] = combinedAdg

        value = map
    }

    val dominiosCache: List<DominioJson> by androidx.compose.runtime.produceState(initialValue = emptyList()) {
        val list = runCatching { context.loadJsonAsset<List<DominioJson>>("fantasia_dominios.json") }.getOrElse { emptyList() }
        value = list
    }

    val dominiosPathfinderCache: List<DominioJson> by androidx.compose.runtime.produceState(initialValue = emptyList()) {
        val list = runCatching { context.loadJsonAsset<List<DominioJson>>("pathfinder_dominios.json") }.getOrElse { emptyList() }
        value = list
    }

    val allPoderes = remember(powerCache) {
        powerCache.values.flatten().distinctBy { it.id }
    }

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var isSearchExpanded by rememberSaveable { mutableStateOf(false) }
    var selectedRank by rememberSaveable { mutableStateOf("Todos") }

    // Track expanded state for each AB section. Default to true (expanded).
    // Using remember instead of rememberSaveable to avoid crash with Map serialization.
    val sectionStates = remember { mutableStateMapOf<String, Boolean>() }

    val idToName = remember(allPoderes) {
        allPoderes.associate { it.id to it.nome.toFancyTitleCase() }
    }

    // Determine which ABs to display
    val displayKeys = if (!state.permiteMultiAntecedenteArcano && !state.compendioFantasiaAtivo && !state.compendioHorrorAtivo && !state.compendioPathfinderAtivo) {
        listOf(arcanosAtivos.first())
    } else {
        arcanosAtivos
    }

    val hasStandardAB = arcanosAtivos.any { it.normAAKey() != "MISTICO" }

    val sharedTotalPP = remember(state.compendioFantasiaAtivo, state.compendioHorrorAtivo, state.compendioPathfinderAtivo, state.ancestralidade, arcanosAtivos, state.bonusPoderExtra, arcanoInfoMap, hasStandardAB) {
        if (!state.compendioFantasiaAtivo && !state.compendioHorrorAtivo && !state.compendioPathfinderAtivo) 0 else {
            val maxBase = arcanosAtivos.filter { it.normAAKey() != "MISTICO" }.maxOfOrNull { k -> arcanoInfoMap[k.normAAKey()]?.second ?: 0 } ?: 0
            val gnomeBonus = if (state.compendioPathfinderAtivo && state.ancestralidade.uppercase().contains("GNOMO") && hasStandardAB) 1 else 0
            maxBase + state.bonusPoderExtra + gnomeBonus
        }
    }

    val includeBasicPowers = remember(
        state.compendioFantasiaAtivo,
        state.compendioHorrorAtivo,
        state.compendioSciFiAtivo,
        state.compendioPathfinderAtivo,
        state.compendioDeadlandsAtivo,
        state.compendioCrystalHeartAtivo,
        state.compendioArteDaGuerraAtivo,
        state.compendioCidadeSolVaporAtivo,
        state.compendioWiseguysAtivo,
        state.modoSupers
    ) {
        "BASICO" in state.getActiveOrigins()
    }

    // Pre-calculate powers for each displayed key to avoid doing it inside LazyColumn (and avoid @Composable error)
    val powersByArcKey = remember(
        powerCache,
        searchQuery,
        selectedRank,
        displayKeys,
        includeBasicPowers,
        state.vantagensSelecionadas,
        state.tropoSelecionado,
        state.dominioClerigoSelecionado,
        state.dominioClerigoPathfinderSelecionado,
        dominiosCache,
        dominiosPathfinderCache
    ) {
        displayKeys.associateWith { arcKeyRaw ->
            val arcKey = arcKeyRaw.normAAKey()
            // Determine origin
            val advantage = state.vantagensSelecionadas.find { it.toArcanoKey() == arcKeyRaw }
            val usaTecnicasTropo = state.compendioArteDaGuerraAtivo &&
                arcKey == "MESTRE DO CHI" &&
                advantage == null &&
                (state.tropoSelecionado?.tecnicasIniciais ?: 0) > 0
            val usaListaChi = state.compendioArteDaGuerraAtivo && arcKey == "MESTRE DO CHI"

            val permittedSet = advantage?.poderesPermitidos?.takeIf { it.isNotEmpty() }?.toSet()
                ?: ArcaneConfig.getPermittedPowers(arcKey)
            val blockedSet = ArcaneConfig.getBlockedPowers(arcKey)
            val stageBasedPowers = state.poderesDisponiveisPorEstagioParaArcano(arcKey)
            val usaPoderesPorEstagio = stageBasedPowers.isNotEmpty()
            val originRaw = when {
                usaListaChi -> "ARTE DA GUERRA"
                else -> advantage?.origem
                    ?: if (state.compendioArteDaGuerraAtivo && arcKey == "ELEMENTALISTA") "ARTE DA GUERRA" else "BASICO"
            }
            val normalizedOrigin = powerAssetOriginKey(originRaw)

            val specificList = powerCache[normalizedOrigin] ?: emptyList()
            val basicList = powerCache["BASICO"] ?: emptyList()
            var sourceList = when {
                usaListaChi -> specificList
                normalizedOrigin == "BASICO" -> basicList
                includeBasicPowers -> (specificList + basicList).distinctBy { it.id }
                else -> specificList
            }

            // Fantasy Cleric Domain Filtering
            if (state.compendioFantasiaAtivo && arcKey == "CLERIGO") {
                val domName = state.dominioClerigoSelecionado
                if (domName != null) {
                    val dom = dominiosCache.find { it.nome == domName }
                    if (dom != null) {
                        sourceList = sourceList.filter { it.id in dom.poderes }
                    } else {
                        sourceList = emptyList()
                    }
                } else {
                    sourceList = emptyList()
                }
            }

            // Pathfinder Cleric/Miracles Domain Filtering
            if (state.compendioPathfinderAtivo && (arcKey == "CLERIGO_PF" || arcKey == "MILAGRES_PF")) {
                val domName = state.dominioClerigoPathfinderSelecionado
                if (domName != null) {
                    val dom = dominiosPathfinderCache.find { it.nome == domName }
                    if (dom != null) {
                        // For CLERIGO_PF (Class): Allow Domain Powers OR General List (handled by permittedSet)
                        // For MILAGRES_PF (AB): Allow ONLY Domain Powers
                        if (arcKey == "MILAGRES_PF") {
                            sourceList = sourceList.filter { it.id in dom.poderes }
                        } else {
                            // CLERIGO_PF: Keep full list for now, filter in step 1 using permitedSet + Domain
                        }
                    } else if (arcKey == "MILAGRES_PF") {
                        sourceList = emptyList()
                    }
                } else if (arcKey == "MILAGRES_PF") {
                    sourceList = emptyList()
                }
            }

            sourceList.filter { power ->
                val isDemonExclusivePower = power.id.endsWith("_demonio")
                val hasDemonAb = state.vantagensSelecionadas.any { it.id == "aa_demonio" }
                if (isDemonExclusivePower) {
                    if (arcKey != "DEMONIO") return@filter false
                    if (!hasDemonAb) return@filter false
                }

                // Meio-Demônio (Cidade do Sol a Vapor):
                // Disfarce Demoníaco não é inicial e só fica disponível em Experiente.
                if (
                    state.compendioCidadeSolVaporAtivo &&
                    arcKey == "DEMONIO" &&
                    state.ancestralidade.keyify().contains("MEIO-DEMONIO") &&
                    power.id == "disfarce_demoniaco"
                ) {
                    val estagioAtual = state.estagioAtual().nome.semAcentos().uppercase()
                    val podeUsarDisfarce = estagioAtual in setOf("EXPERIENTE", "VETERANO", "HEROICO", "HEROICO", "LENDARIO")
                    if (!podeUsarDisfarce) return@filter false
                }

                // 1. Check permissions/blocks
                val isAllowed = if (state.compendioFantasiaAtivo && arcKey == "CLERIGO") {
                    true // Already filtered by domain logic above (Fantasy Cleric)
                } else if (state.compendioPathfinderAtivo && arcKey == "CLERIGO_PF") {
                    // Class Cleric: Allowed if in Permitted Set OR in Domain
                    val inPermitted = permittedSet?.contains(power.id) == true
                    val inDomain = if (state.dominioClerigoPathfinderSelecionado != null) {
                        val dom = dominiosPathfinderCache.find { it.nome == state.dominioClerigoPathfinderSelecionado }
                        dom?.poderes?.contains(power.id) == true
                    } else false
                    inPermitted || inDomain
                } else if (state.compendioPathfinderAtivo && arcKey == "MILAGRES_PF") {
                    true // Already filtered by domain logic above (Miracles AB)
                } else if (permittedSet != null) {
                    power.id in permittedSet
                } else if (blockedSet.isNotEmpty()) {
                    power.id !in blockedSet
                } else {
                    true // No restriction
                }

                if (!isAllowed) return@filter false

                if (usaPoderesPorEstagio) {
                    val requiredStage = stageBasedPowers[power.id] ?: return@filter false
                    if (!state.estagioAtinge(requiredStage)) return@filter false
                    if (!state.atendeRequisitoEspecialDePoderPorArcano(arcKey, power.id)) return@filter false
                }

                // 2. Check Search
                val matchSearch = if (searchQuery.isBlank()) true else {
                    power.nome.semAcentos().contains(searchQuery.semAcentos(), ignoreCase = true) ||
                    power.descricao.semAcentos().contains(searchQuery.semAcentos(), ignoreCase = true)
                }

                // 3. Check Rank
                val matchRank = if (selectedRank == "Todos") true else {
                    val rankSource = if (usaPoderesPorEstagio) {
                        stageBasedPowers[power.id] ?: power.estagio
                    } else {
                        power.estagio
                    }
                    rankSource.semAcentos().equals(selectedRank.semAcentos(), ignoreCase = true)
                }

                matchSearch && matchRank
            }.sortedWith(compareBy(ptBrCollator) { it.nome.toFancyTitleCase() })
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // --- FILTERS ---
        item {
            ExpandableSearchFilter(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                isExpanded = isSearchExpanded,
                onExpandedChange = { isSearchExpanded = it },
                onClear = {
                    searchQuery = ""
                    selectedRank = "Todos"
                },
                placeholder = "Pesquisar Poderes..."
            )
        }

        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedRank == "Todos",
                        onClick = { selectedRank = "Todos" },
                        label = { Text("Todos") }
                    )
                }
                items(listOf("Novato", "Experiente", "Veterano", "Heroico", "Lendario")) { rank ->
                    FilterChip(
                        selected = selectedRank == rank,
                        onClick = { selectedRank = rank },
                        label = { Text(rank) }
                    )
                }
            }
        }

        // --- ARCANE BACKGROUND SECTIONS ---
        displayKeys.forEach { arcKeyRaw ->
            val arcKey = arcKeyRaw.normAAKey()
            val baseInfo = arcanoInfoMap[arcKey] ?: Triple(0, 0, "—")
            val ppTotal = baseInfo.second
            val foco = baseInfo.third
            val slotsCount = state.getEffectiveSlotsCountForArcano(arcKey)
            val usaPoderesPorEstagio = state.usaPoderesDisponiveisPorEstagio(arcKey)

            val centerText = if (state.usarSemPontosDePoder) {
                "Teste $foco = -(custo/2)"
            } else if (usaPoderesPorEstagio) {
                "Poderes por estágio  •  PP especiais  •  $foco"
            } else {
                val ppDisplay = if (arcKey == "MISTICO") {
                    val gnomeBonus = if (state.compendioPathfinderAtivo && state.ancestralidade.uppercase().contains("GNOMO") && !hasStandardAB) 1 else 0
                    ppTotal + gnomeBonus
                } else if (state.compendioFantasiaAtivo || state.compendioHorrorAtivo || state.compendioPathfinderAtivo) {
                    sharedTotalPP
                } else {
                    ppTotal
                }
                "PP: $ppDisplay  •  $foco"
            }

            // Treat null as true (default expanded)
            val isExpanded = sectionStates[arcKey] ?: true

            val poderesParaEsteArcano = powersByArcKey[arcKeyRaw] ?: emptyList()

            // Domain Selection UI for Cleric (Fantasy)
            if (state.compendioFantasiaAtivo && arcKey == "CLERIGO") {
                item(key = "domain_selector_$arcKey") {
                    var expandedDomain by remember { mutableStateOf(false) }

                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)) {
                        OutlinedTextField(
                            value = state.dominioClerigoSelecionado ?: "Selecione um Domínio",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Domínio Divino") },
                            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, "Expandir") },
                            modifier = Modifier
                                .fillMaxWidth(),
                            enabled = !locked
                        )
                        Box(
                            Modifier
                                .matchParentSize()
                                .clickable(enabled = !locked) { expandedDomain = true }
                        )

                        DropdownMenu(
                            expanded = expandedDomain,
                            onDismissRequest = { expandedDomain = false }
                        ) {
                            dominiosCache.forEach { dom ->
                                DropdownMenuItem(
                                    text = { Text(dom.nome) },
                                    onClick = {
                                        state.dominioClerigoSelecionado = dom.nome
                                        expandedDomain = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Domain Selection UI for Cleric/Miracles (Pathfinder)
            if (state.compendioPathfinderAtivo && (arcKey == "CLERIGO_PF" || arcKey == "MILAGRES_PF")) {
                item(key = "domain_selector_pf_$arcKey") {
                    var expandedDomain by remember { mutableStateOf(false) }

                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)) {
                        OutlinedTextField(
                            value = state.dominioClerigoPathfinderSelecionado ?: "Selecione um Domínio",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Domínio Divino (Pathfinder)") },
                            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, "Expandir") },
                            modifier = Modifier
                                .fillMaxWidth(),
                            enabled = !locked
                        )
                        Box(
                            Modifier
                                .matchParentSize()
                                .clickable(enabled = !locked) { expandedDomain = true }
                        )

                        DropdownMenu(
                            expanded = expandedDomain,
                            onDismissRequest = { expandedDomain = false }
                        ) {
                            dominiosPathfinderCache.forEach { dom ->
                                DropdownMenuItem(
                                    text = { Text(dom.nome) },
                                    onClick = {
                                        state.dominioClerigoPathfinderSelecionado = dom.nome
                                        expandedDomain = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // HEADER (Custom Collapsible)
            item(key = "header_$arcKey") {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // Toggle state. If missing (true), become false. If present, negate.
                                sectionStates[arcKey] = !isExpanded
                            }
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = centerText,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = if (isExpanded) "Recolher" else "Expandir",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    HorizontalDivider(thickness = 1.dp)
                }
            }

            if (isExpanded) {
                // SLOTS PANEL
                if (usaPoderesPorEstagio) {
                    item(key = "stage_based_info_$arcKey") {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Column(Modifier.padding(8.dp)) {
                                Text("Disponibilidade especial", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Este antecedente não usa slots nem Novos Poderes. Todos os poderes abaixo ficam disponíveis automaticamente quando o estágio é alcançado.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                } else {
                    item(key = "slots_$arcKey") {
                        val slots = state.poderSlotsPorArcano[arcKey] ?: remember(arcKey) {
                            mutableStateListOf<String?>().apply {
                                repeat(state.getEffectiveSlotsCountForArcano(arcKey)) { add(null) }
                            }
                        }
                        // Lock logic
                        val lockedCount = if (state.mostrandoPoderesProgresso && state.arcanoEmCompraViaXpKey == arcKey)
                            state.arcanoSnapshotAntesDaCompra?.size ?: 0
                        else 0

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Column(Modifier.padding(8.dp)) {
                                Text("Slots: $slotsCount", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                Spacer(Modifier.height(4.dp))
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    slots.forEachIndexed { idx, poderId ->
                                        val label = if (poderId == null) "— vazio —" else (idToName[poderId] ?: poderId.toFancyTitleCase())
                                        val isFixed = state.isFixedPower(arcKey, poderId)
                                        val isSlotLocked = locked || idx < lockedCount || isFixed
                                        AssistChip(
                                            colors = AssistChipDefaults.assistChipColors(
                                                containerColor = if (poderId == null) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                                            ),
                                            onClick = {
                                                if (!isSlotLocked && poderId != null) {
                                                    val (pode, msg) = state.podeRemoverPoderDoSlot(poderId)
                                                    if (!pode) {
                                                        onShowMessage(msg ?: "Não é possível remover este poder.")
                                                    } else {
                                                        slots[idx] = null
                                                        state.syncPoderesSelecionadosFromSlots()
                                                        state.manifestacoesPoderes.remove(poderId)
                                                    }
                                                }
                                            },
                                            label = {
                                                Text(
                                                    text = "${idx + 1}: $label",
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            },
                                            enabled = !isSlotLocked && poderId != null
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // POWERS LIST
                if (poderesParaEsteArcano.isEmpty()) {
                    item {
                        Text(
                            "Nenhum poder disponível para os filtros selecionados.",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    items(
                        items = poderesParaEsteArcano,
                        key = { "${arcKey}_${it.id}" } // Unique key per AB + Power
                    ) { poder ->
                        val usaPoderesPorEstagioCard = state.usaPoderesDisponiveisPorEstagio(arcKey)
                        val slots = if (usaPoderesPorEstagioCard) {
                            remember(arcKey) { mutableStateListOf<String?>() }
                        } else {
                            state.poderSlotsPorArcano[arcKey] ?: remember(arcKey) {
                                mutableStateListOf<String?>().apply {
                                    repeat(state.getEffectiveSlotsCountForArcano(arcKey)) { add(null) }
                                }
                            }
                        }
                        val selecionado = if (usaPoderesPorEstagioCard) false else slots.any { it?.equals(poder.id, ignoreCase = true) == true }
                        val lockedCount = if (state.mostrandoPoderesProgresso && state.arcanoEmCompraViaXpKey == arcKey)
                            state.arcanoSnapshotAntesDaCompra?.size ?: 0
                        else 0

                        var expanded by remember { mutableStateOf(false) }

                        val isFixed = state.isFixedPower(arcKey, poder.id)
                        val isCardLocked = locked || isFixed

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 2.dp) // Reduced padding
                                .alpha(if (selecionado) 0.6f else 1f)
                                .clickable(enabled = !isCardLocked) {
                                    if (usaPoderesPorEstagioCard) {
                                        expanded = !expanded
                                        return@clickable
                                    }
                                    if (selecionado) {
                                        val idx = slots.indexOfFirst { it?.equals(poder.id, ignoreCase = true) == true }
                                        if (idx >= 0 && idx >= lockedCount) {
                                            val (pode, msg) = state.podeRemoverPoderDoSlot(poder.id)
                                            if (!pode) {
                                                onShowMessage(msg ?: "Não é possível remover este poder.")
                                            } else {
                                                slots[idx] = null
                                                state.syncPoderesSelecionadosFromSlots()
                                                state.manifestacoesPoderes.remove(poder.id)
                                            }
                                        }
                                    } else {
                                        val requiredSlots = state.getEffectiveSlotsCountForArcano(arcKey)
                                        while (slots.size < requiredSlots) {
                                            slots.add(null)
                                        }
                                        val firstEmpty = slots.indexOfFirst { it == null }
                                        if (firstEmpty >= 0 && firstEmpty >= lockedCount) {
                                            slots[firstEmpty] = poder.id
                                            state.syncPoderesSelecionadosFromSlots()
                                        }
                                    }
                                }
                        ) {
                            Column(Modifier.padding(8.dp)) { // Compact internal padding
                                val ppExibicao = if (
                                    state.compendioCidadeSolVaporAtivo &&
                                    arcKey == "DEMONIO" &&
                                    state.ancestralidade.keyify().contains("MEIO-DEMONIO") &&
                                    poder.id == "disfarce_demoniaco"
                                ) {
                                    "2"
                                } else {
                                    poder.pontosDePoder
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    var displayNome = poder.nome.toFancyTitleCase()
                                    if (state.compendioPathfinderAtivo && arcKey == "MISTICO") {
                                        displayNome = displayNome
                                            .replace("Aumentar/Reduzir Característica", "Aumentar Característica")
                                            .replace("Morosidade/Velocidade", "Velocidade")
                                    }
                                    Text(displayNome, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    val specialStage = state.poderesDisponiveisPorEstagioParaArcano(arcKey)[poder.id]
                                    Text(
                                        if (usaPoderesPorEstagioCard && specialStage != null) "$specialStage • PP: $ppExibicao" else "PP: $ppExibicao",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                if (state.usarSemPontosDePoder) {
                                    Text("Penalidade base: ${custoParaPenalidadeTexto(ppExibicao)}", style = MaterialTheme.typography.bodySmall)
                                }

                                val manifestacoesDisponiveis = poder.manifestacoes.filter { it.isNotBlank() }
                                val modificadoresDisponiveis = poder.modificadores.filter { mod ->
                                    mod.nome.isNotBlank() || mod.descricao.isNotBlank()
                                }

                                val detalhesDisponiveis = allowLongTexts && (
                                    poder.descricao.isNotBlank() ||
                                        manifestacoesDisponiveis.isNotEmpty() ||
                                        modificadoresDisponiveis.isNotEmpty()
                                )

                                if (detalhesDisponiveis) {
                                    Spacer(Modifier.height(4.dp))
                                    TextButton(
                                        onClick = { expanded = !expanded },
                                        modifier = Modifier.height(24.dp), // Reduce button height
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                    ) {
                                        Text(
                                            if (expanded) "Ocultar detalhes" else "Ver detalhes",
                                            fontWeight = FontWeight.Medium,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    AnimatedVisibility(visible = expanded) {
                                        Column(Modifier.padding(top = 4.dp)) {
                                            if (poder.descricao.isNotBlank()) {
                                                Text(poder.descricao, style = MaterialTheme.typography.bodySmall)
                                                Spacer(Modifier.height(4.dp))
                                            }

                                            Text("Distância: ${poder.distancia}", style = MaterialTheme.typography.labelSmall)
                                            Text("Duração: ${poder.duracao}", style = MaterialTheme.typography.labelSmall)

                                            if (manifestacoesDisponiveis.isNotEmpty()) {
                                                Spacer(Modifier.height(4.dp))
                                                Text("Manifestações:", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelSmall)
                                                manifestacoesDisponiveis.forEach { man ->
                                                    Text("• $man", style = MaterialTheme.typography.bodySmall)
                                                }
                                            }

                                            if (modificadoresDisponiveis.isNotEmpty()) {
                                                Spacer(Modifier.height(4.dp))
                                                Text("Modificadores:", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelSmall)
                                                modificadoresDisponiveis.forEach { mod ->
                                                    Text(
                                                        "${mod.nome} (${mod.custo}): ${mod.descricao}",
                                                        style = MaterialTheme.typography.bodySmall
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
            }

            // Divider between AB sections (except after last)
            if (arcKeyRaw != displayKeys.last()) {
                item {
                    Spacer(Modifier.height(16.dp)) // Slightly reduced spacer
                }
            }
        }
    }
}
