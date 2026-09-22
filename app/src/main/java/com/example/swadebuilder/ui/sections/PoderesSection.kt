package com.example.swadebuilder.ui.sections

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.R
import com.example.swadebuilder.model.ArcaneConfig
import com.example.swadebuilder.model.Poder
import com.example.swadebuilder.model.getActiveOrigins
import com.example.swadebuilder.util.loadJsonAssetAsync
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

/**
 * Poderes com nome "Aspecto1/Aspecto2" (ex.: "Iluminar/Obscurecer") existem
 * como um único id no catálogo — não há um poder "só Iluminar" separado de
 * um "só Obscurecer" (ver ArcaneConfig.SOL_VAPOR_DEMONIO_ALLOWED_POWERS).
 * Quando o livro restringe um Antecedente Arcano a só um dos dois aspectos
 * (ex.: Feiticeiro/Demônio só tem "Obscurecer", nunca "Iluminar"), isso é
 * puramente cosmético — id, custo e efeito mecânico continuam os mesmos do
 * poder combinado. Mesma técnica já usada pro Místico (Pathfinder) logo
 * abaixo, só que centralizada por arcKey em vez de replicada inline.
 */
private fun aspectOnlyPowerDisplayName(rawDisplayName: String, arcKey: String): String {
    return when (arcKey) {
        "DEMONIO", "DEMONIO_MEIO" -> rawDisplayName
            .replace("Iluminar/Obscurecer", "Obscurecer")
            .replace("Aumentar/Reduzir Característica", "Reduzir Característica")
            .replace("Morosidade/Velocidade", "Morosidade")
        "MILAGRES", "ANJO" -> rawDisplayName
            .replace("Iluminar/Obscurecer", "Iluminar")
            .replace("Aumentar/Reduzir Característica", "Aumentar Característica")
            .replace("Morosidade/Velocidade", "Velocidade")
        else -> rawDisplayName
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PoderesSection(
    state: CriadorState,
    arcanoInfoMap: Map<String, Triple<Int, Int, String>>,
    onShowMessage: (String) -> Unit = {},
    onCustomContentChanged: () -> Unit = {}
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
            ativos.add("TECNICAS ELEMENTAIS")
        }
        if (state.compendioArteDaGuerraAtivo && !state.isFeralAdgSelecionado() && (state.tropoSelecionado?.tecnicasIniciais ?: 0) > 0) {
            ativos.add("TECNICAS CHI")
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
                // This runs in LaunchedEffect, not during composition. The state list is
                // intentionally created once in CriadorState's slot map and then reused.
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

    val powerCacheState by androidx.compose.runtime.produceState<Map<String, List<Poder>>?>(initialValue = null) {
        value = com.example.swadebuilder.model.poderesPorOrigem(context)
    }
    val powerCache: Map<String, List<Poder>> = powerCacheState ?: emptyMap()
    val isPowersLoading = powerCacheState == null

    val dominiosCache: List<DominioJson> by androidx.compose.runtime.produceState(initialValue = emptyList()) {
        val list = runCatching { context.loadJsonAssetAsync<List<DominioJson>>("fantasia_dominios.json") }.getOrElse { emptyList() }
        value = list
    }

    val dominiosPathfinderCache: List<DominioJson> by androidx.compose.runtime.produceState(initialValue = emptyList()) {
        val list = runCatching { context.loadJsonAssetAsync<List<DominioJson>>("pathfinder_dominios.json") }.getOrElse { emptyList() }
        value = list
    }

    val allPoderes = remember(powerCache) {
        powerCache.values.flatten().distinctBy { it.id }
    }

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var isSearchExpanded by rememberSaveable { mutableStateOf(false) }
    var selectedRank by rememberSaveable { mutableStateOf("Todos") }
    // Filtro por Categoria Customizada (ver model/CategoriaCustomizada.kt) — só aparece se o
    // Mestre tiver criado alguma categoria pra Poder nesta campanha.
    var selectedCategoriaCustomId by rememberSaveable { mutableStateOf<String?>(null) }
    val categoriasPoder = remember(state.listaCategoriasCustomizadas) {
        state.listaCategoriasCustomizadas.filter { it.tipoEntidade == com.example.swadebuilder.model.TipoEntidadeCategoria.PODER }
    }

    // Track expanded state for each AB section. Default to true (expanded).
    // Using remember instead of rememberSaveable to avoid crash with Map serialization.
    val sectionStates = remember { mutableStateMapOf<String, Boolean>() }

    val idToName = remember(allPoderes) {
        allPoderes.associate { it.id to it.nome.toFancyTitleCase() }
    }

    // Determine which ABs to display
    // Fantasia/Horror/SciFi/Pathfinder tratam múltiplos Antecedentes Arcanos como prática normal
    // do cenário (reserva de PP compartilhada, livro) — não dependem da regra opcional pra mostrar
    // mais de um AB de uma vez.
    val displayKeys = if (!state.permiteMultiplosAntecedentesArcanos) {
        listOf(arcanosAtivos.first())
    } else {
        arcanosAtivos
    }

    // "MÚLTIPLOS ANTECEDENTES ARCANOS" (Fantasia p.69, Horror p.69, Sci-Fi, Pathfinder —
    // texto idêntico nos quatro livros): "Se já tiver um Antecedente Arcano ou Poderes
    // Místicos, usa a MAIOR reserva inicial de Pontos de Poder e aplica quaisquer aumentos
    // de outras fontes a ela. Todos os seus Antecedentes Arcanos e Poderes Místicos
    // compartilham essa reserva." — Poderes Místicos entra nesse máximo igual a qualquer
    // outro Antecedente Arcano (10 PP fixos, ver Fantasia p.177/Horror p.14), não uma
    // reserva separada — antes MISTICO tinha sua própria conta isolada (ppTotal próprio,
    // sem entrar no maxOf nem levar bonusPoderExtra), então um personagem com Místico E
    // outro Antecedente Arcano via dois números diferentes em vez de uma reserva só.
    val sharedTotalPP = remember(state.compendioFantasiaAtivo, state.compendioHorrorAtivo, state.compendioPathfinderAtivo, state.compendioSciFiAtivo, state.ancestralidade, arcanosAtivos, state.bonusPoderExtra, arcanoInfoMap) {
        if (!state.compendioFantasiaAtivo && !state.compendioHorrorAtivo && !state.compendioPathfinderAtivo && !state.compendioSciFiAtivo) 0 else {
            val maxBaseOutros = arcanosAtivos.filter { it.normAAKey() != "MISTICO" }.maxOfOrNull { k -> arcanoInfoMap[k.normAAKey()]?.second ?: 0 } ?: 0
            val temMistico = arcanosAtivos.any { it.normAAKey() == "MISTICO" }
            val maxBase = if (temMistico) maxOf(maxBaseOutros, 10) else maxBaseOutros
            // Magia Gnômica (Pathfinder): "Gnomos com um Antecedente Arcano ou Poder
            // Místico adicionam seu Ponto de Poder de bônus à sua reserva" — soma uma vez
            // só, sem depender de ter ou não um Antecedente Arcano "padrão" (Místico já
            // conta pra essa condição sozinho).
            val gnomeBonus = if (state.compendioPathfinderAtivo && state.ancestralidade.uppercase().contains("GNOMO")) 1 else 0
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

    // Livros relevantes pro personagem: união dos livros de cada Antecedente Arcano
    // mostrado (mesma lógica de origem de powersByArcKey, abaixo) + Básico quando
    // aplicável. Usado só pras contagens dos chips "Todos"/"Novato"/... — que antes
    // somavam allPoderes (todo poder de todo livro que o app conhece, ativo ou não no
    // personagem) — bug real relatado pelo usuário: com só o livro Básico ativo, o chip
    // mostrava "Todos (225)" em vez de 54. A lista de poderes de fato selecionáveis
    // (poderesParaEsteArcano, abaixo) já era corretamente restrita ao livro certo; só a
    // contagem do chip estava errada.
    val relevantPowerOrigins = remember(
        displayKeys,
        state.vantagensSelecionadas,
        state.compendioArteDaGuerraAtivo,
        state.tropoSelecionado,
        includeBasicPowers
    ) {
        buildSet {
            displayKeys.forEach { arcKeyRaw ->
                val arcKey = arcKeyRaw.normAAKey()
                val advantage = state.vantagensSelecionadas.find { it.toArcanoKey() == arcKeyRaw }
                val usaListaChi = state.compendioArteDaGuerraAtivo && arcKey == "TECNICAS CHI"
                val usaTecnicasElementais = state.compendioArteDaGuerraAtivo && arcKey == "TECNICAS ELEMENTAIS"
                val originRaw = when {
                    usaListaChi || usaTecnicasElementais -> "ARTE DA GUERRA"
                    else -> advantage?.origem ?: "BASICO"
                }
                add(powerAssetOriginKey(originRaw))
            }
            if (includeBasicPowers) add("BASICO")
        }
    }
    val allPoderesRelevantes = remember(powerCache, relevantPowerOrigins) {
        relevantPowerOrigins.flatMap { powerCache[it] ?: emptyList() }.distinctBy { it.id }
    }

    // Pre-calculate powers for each displayed key to avoid doing it inside LazyColumn (and avoid @Composable error)
    val powersByArcKey = remember(
        powerCache,
        searchQuery,
        selectedRank,
        selectedCategoriaCustomId,
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
            // "TECNICAS CHI" é a chave própria do sistema de Técnicas de Chi por Tropo da
            // Arte da Guerra (sem Vantagem correspondente) — NUNCA a mesma chave do
            // Antecedente Arcano (Mestre do Chi) de Deadlands, que é uma Vantagem de verdade
            // com sua própria lista de poderes (ver CriadorState.fixedPowersByArcano). Usar
            // chaves distintas evita depender de um guard de origem pra não vazar a lista de
            // poderes de um sistema pro outro.
            val usaTecnicasTropo = state.compendioArteDaGuerraAtivo &&
                arcKey == "TECNICAS CHI" &&
                advantage == null &&
                (state.tropoSelecionado?.tecnicasIniciais ?: 0) > 0
            val usaListaChi = state.compendioArteDaGuerraAtivo && arcKey == "TECNICAS CHI"
            // "TECNICAS ELEMENTAIS" é a chave própria do Tropo Elementalista da Arte da
            // Guerra (sem Vantagem correspondente) — NUNCA "ELEMENTALISTA" (essa é exclusiva
            // do Antecedente Arcano de Fantasia, com sua própria lista de poderes; ver
            // CriadorState.fixedPowersByArcano e ArcaneConfig.FANTASIA_ELEMENTALISTA).
            val usaTecnicasElementais = state.compendioArteDaGuerraAtivo && arcKey == "TECNICAS ELEMENTAIS"
            val originRaw = when {
                usaListaChi || usaTecnicasElementais -> "ARTE DA GUERRA"
                else -> advantage?.origem ?: "BASICO"
            }
            val normalizedOrigin = powerAssetOriginKey(originRaw)

            // `originRaw` desambigua outras colisões de nome LEGÍTIMAS entre Antecedentes
            // Arcanos de verdade de livros diferentes (ex.: ALQUIMIA entre Fantasia/Horror,
            // FEITICEIRO entre Fantasia/Cidade do Sol a Vapor) — casos em que os dois lados
            // são Vantagens reais com o mesmo nome, então uma chave própria não faz sentido
            // (mudaria o nome de uma Vantagem real). Diferente do caso de Mestre do
            // Chi/Elementalista, onde um dos lados era só uma chave interna de Tropo sem
            // Vantagem nenhuma — esses já usam chave própria, sem precisar de `origem`.
            val permittedSet = advantage?.poderesPermitidos?.takeIf { it.isNotEmpty() }?.toSet()
                ?: if (usaTecnicasTropo) null else ArcaneConfig.getPermittedPowers(arcKey, originRaw)
            val stageBasedPowers = state.poderesDisponiveisPorEstagioParaArcano(arcKey)
            val usaPoderesPorEstagio = stageBasedPowers.isNotEmpty()

            val specificList = powerCache[normalizedOrigin] ?: emptyList()
            val basicList = powerCache["BASICO"] ?: emptyList()
            var sourceList = when {
                // Antecedente Arcano Customizado tageado "Geral" (ver criador em
                // SettingsDialog): lista aberta = todos os poderes de todos os
                // livros, não só o balde vazio de um "livro" GERAL que não existe
                // em powerCache.
                normalizedOrigin == com.example.swadebuilder.util.TAG_GERAL -> allPoderes
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
                // "_demonio": poderes exclusivos do Antecedente Arcano (Demônio) —
                // tanto a versão de sangue puro (DEMONIO, aa_demonio) quanto a
                // diluída dos Meio-Demônios (DEMONIO_MEIO, aa_demonio_meio_demonio).
                // Quais ids exatos cada um pode ver (incluindo excluir Disfarce
                // Demoníaco "puro" da versão diluída) já vem de
                // ArcaneConfig.SOL_VAPOR_DEMONIO_ALLOWED_POWERS/
                // SOL_VAPOR_DEMONIO_MEIO_ALLOWED_POWERS via permittedSet logo
                // abaixo — este bloco só bloqueia vazamento pra OUTROS Antecedentes
                // (ex.: um AA Customizado "Geral" que puxa allPoderes inteiro).
                val isDemonExclusivePower = power.id.endsWith("_demonio")
                val hasDemonAb = state.vantagensSelecionadas.any { it.id == "aa_demonio" || it.id == "aa_demonio_meio_demonio" }
                if (isDemonExclusivePower) {
                    if (arcKey != "DEMONIO" && arcKey != "DEMONIO_MEIO") return@filter false
                    if (!hasDemonAb) return@filter false
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
                } else {
                    true // No restriction
                }

                if (!isAllowed) return@filter false

                if (usaPoderesPorEstagio) {
                    val requiredStage = stageBasedPowers[power.id] ?: return@filter false
                    if (!state.estagioAtinge(requiredStage)) return@filter false
                }
                // Fora do sistema "poderes por estágio": não filtra mais por
                // state.poderAtendeEstagio() aqui — um poder de Estágio acima do atual
                // continua na lista, só aparece bloqueado no card (ver isStageLocked
                // abaixo), igual ao que já acontece com Vantagens ("Requisitos
                // pendentes"). Escondê-lo da lista inteira (comportamento anterior)
                // deixava a contagem do filtro "Todos" mentindo e o poder parecia não
                // existir no catálogo — bug real relatado pelo usuário com Banir.

                // Requisito de "precisa ter outra Vantagem antes" (ex.: Disfarce
                // Demoníaco diluído do Meio-Demônio exige Disfarce Demoníaco
                // Experiente) vale independente do arcano estar em modo por
                // estágio ou no sistema normal de slots — ver
                // requisitoEspecialDePoderPorArcano.
                if (!state.atendeRequisitoEspecialDePoderPorArcano(arcKey, power.id)) return@filter false

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

                // 4. Check Categoria Customizada
                val matchCategoria = selectedCategoriaCustomId == null ||
                    power.categoriaCustomizadaId == selectedCategoriaCustomId

                matchSearch && matchRank && matchCategoria
            }.sortedWith(compareBy(ptBrCollator) { it.nome.toFancyTitleCase() })
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (state.habilitarCriacaoNasAbas) {
            item {
                var showCreateOptionsDialog by rememberSaveable { mutableStateOf(false) }
                var targetCreationCategory by rememberSaveable { mutableStateOf<String?>(null) }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { showCreateOptionsDialog = true },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Criar...", style = MaterialTheme.typography.labelSmall)
                    }
                }

                if (showCreateOptionsDialog) {
                    AlertDialog(
                        onDismissRequest = { showCreateOptionsDialog = false },
                        title = { Text("Criar em Poderes") },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("O que você deseja criar?", style = MaterialTheme.typography.bodyMedium)
                                OutlinedButton(
                                    onClick = {
                                        showCreateOptionsDialog = false
                                        targetCreationCategory = "Poder"
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Novo Poder")
                                }
                                OutlinedButton(
                                    onClick = {
                                        showCreateOptionsDialog = false
                                        targetCreationCategory = "Antecedente Arcano"
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Novo Antecedente Arcano")
                                }
                            }
                        },
                        confirmButton = {},
                        dismissButton = {
                            TextButton(onClick = { showCreateOptionsDialog = false }) {
                                Text("Cancelar")
                            }
                        }
                    )
                }

                targetCreationCategory?.let { cat ->
                    com.example.swadebuilder.ui.components.CustomContentManageDialog(
                        state = state,
                        initialCategory = cat,
                        onDismiss = { targetCreationCategory = null },
                        onCustomContentChanged = onCustomContentChanged
                    )
                }
            }
        }

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
                    val count = allPoderesRelevantes.size
                    FilterChip(
                        selected = selectedRank == "Todos",
                        onClick = { selectedRank = "Todos" },
                        label = { Text("Todos ($count)") }
                    )
                }
                items(listOf("Novato", "Experiente", "Veterano", "Heroico", "Lendario")) { rank ->
                    val count = remember(allPoderesRelevantes, rank) {
                        allPoderesRelevantes.count { it.estagio.semAcentos().equals(rank.semAcentos(), ignoreCase = true) }
                    }
                    FilterChip(
                        selected = selectedRank == rank,
                        onClick = { selectedRank = rank },
                        label = { Text("$rank ($count)") }
                    )
                }
            }
        }

        if (categoriasPoder.isNotEmpty()) {
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            "Categoria:",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedCategoriaCustomId == null,
                            onClick = { selectedCategoriaCustomId = null },
                            label = { Text("Todas") }
                        )
                    }
                    items(categoriasPoder, key = { it.id }) { cat ->
                        FilterChip(
                            selected = selectedCategoriaCustomId == cat.id,
                            onClick = { selectedCategoriaCustomId = cat.id },
                            label = { Text(cat.nome) }
                        )
                    }
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
                val ppDisplay = if (state.compendioFantasiaAtivo || state.compendioHorrorAtivo || state.compendioPathfinderAtivo || state.compendioSciFiAtivo) {
                    // Cobre MISTICO e qualquer outro Antecedente Arcano igual — os dois
                    // compartilham a mesma reserva (ver comentário em sharedTotalPP, acima).
                    sharedTotalPP
                } else {
                    // Só o livro Básico ativo: sharedTotalPP não entra nessa conta (ver guard
                    // logo acima, restrito a Fantasia/Horror/Pathfinder/SciFi), então a
                    // Vantagem Pontos de Poder (bonusPoderExtra) precisa ser somada aqui — sem
                    // isso a reserva exibida nunca refletia a compra (bug real relatado pelo
                    // usuário).
                    ppTotal + state.bonusPoderExtra
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

                        var editingSlotIdx by remember(arcKey) { mutableStateOf<Int?>(null) }

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
                                        // Chave por índice RAW do slot (não pelo id do poder) — Novos
                                        // Poderes permite repetir um poder já conhecido num slot extra
                                        // (ver botão "Duplicar" na lista abaixo), então duas cópias do
                                        // mesmo poder precisam de notas de Manifestação independentes.
                                        val nota = if (poderId != null) state.manifestacoesPoderes["$arcKey#$idx"]?.trim()?.takeIf { it.isNotBlank() } else null
                                        val baseLabel = if (poderId == null) "— vazio —" else aspectOnlyPowerDisplayName(idToName[poderId] ?: poderId.toFancyTitleCase(), arcKey)
                                        val label = if (nota != null) "$baseLabel ($nota)" else baseLabel
                                        val isFixed = state.isFixedPower(arcKey, poderId)
                                        val isSlotLocked = locked || idx < lockedCount || isFixed
                                        AssistChip(
                                            colors = AssistChipDefaults.assistChipColors(
                                                containerColor = if (poderId == null) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                                            ),
                                            onClick = {
                                                // Toque no corpo do chip abre a edição da anotação de
                                                // Manifestação — não remove mais o poder (ver ícone "x"
                                                // abaixo), pra reduzir remoção acidental de um slot já
                                                // pago.
                                                if (poderId != null) editingSlotIdx = idx
                                            },
                                            label = {
                                                Text(
                                                    text = "${idx + 1}: $label",
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            },
                                            // Sinaliza visualmente que o corpo do chip é tocável pra
                                            // editar a Manifestação (sem isso, nada no chip indicava
                                            // que dava pra tocar — usuário relatou não ter achado onde
                                            // colocar a Manifestação).
                                            leadingIcon = if (poderId != null) {
                                                {
                                                    Icon(
                                                        imageVector = Icons.Filled.Edit,
                                                        contentDescription = "Editar Manifestação",
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            } else null,
                                            trailingIcon = if (poderId != null && !isSlotLocked) {
                                                {
                                                    Icon(
                                                        imageVector = Icons.Filled.Close,
                                                        contentDescription = "Remover poder do slot",
                                                        modifier = Modifier
                                                            .size(16.dp)
                                                            .clickable {
                                                                val (pode, msg) = state.podeRemoverPoderDoSlot(poderId)
                                                                if (!pode) {
                                                                    onShowMessage(msg ?: "Não é possível remover este poder.")
                                                                } else {
                                                                    slots[idx] = null
                                                                    state.syncPoderesSelecionadosFromSlots()
                                                                    state.manifestacoesPoderes.remove("$arcKey#$idx")
                                                                }
                                                            }
                                                    )
                                                }
                                            } else null,
                                            enabled = poderId != null
                                        )
                                    }
                                }

                                val idxSendoEditado = editingSlotIdx
                                val poderSendoEditado = idxSendoEditado?.let { slots.getOrNull(it) }
                                if (idxSendoEditado != null && poderSendoEditado != null) {
                                    val chaveNota = "$arcKey#$idxSendoEditado"
                                    var textoManifestacao by remember(arcKey, idxSendoEditado) {
                                        mutableStateOf(state.manifestacoesPoderes[chaveNota] ?: "")
                                    }
                                    AlertDialog(
                                        onDismissRequest = { editingSlotIdx = null },
                                        title = { Text("Manifestação") },
                                        text = {
                                            Column {
                                                Text(
                                                    "Anotação livre pra descrever a Manifestação deste poder (ex.: \"Gelo\" pro poder Raio, criando um Raio de Gelo). As Manifestações listadas no livro são só exemplos de inspiração, não uma lista fechada.",
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                                Spacer(Modifier.height(8.dp))
                                                OutlinedTextField(
                                                    value = textoManifestacao,
                                                    onValueChange = { textoManifestacao = it },
                                                    label = { Text("Manifestação") },
                                                    singleLine = true,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            }
                                        },
                                        confirmButton = {
                                            TextButton(onClick = {
                                                val trimmed = textoManifestacao.trim()
                                                if (trimmed.isBlank()) {
                                                    state.manifestacoesPoderes.remove(chaveNota)
                                                } else {
                                                    state.manifestacoesPoderes[chaveNota] = trimmed
                                                }
                                                editingSlotIdx = null
                                            }) { Text("Salvar") }
                                        },
                                        dismissButton = {
                                            TextButton(onClick = { editingSlotIdx = null }) { Text("Cancelar") }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // POWERS LIST
                if (isPowersLoading) {
                    item {
                        com.example.swadebuilder.ui.components.LoadingState(
                            message = "Carregando poderes..."
                        )
                    }
                } else if (poderesParaEsteArcano.isEmpty()) {
                    item {
                        com.example.swadebuilder.ui.components.EmptyState(
                            message = "Nenhum poder disponível para os filtros selecionados."
                        )
                    }
                } else {
                    items(
                        items = poderesParaEsteArcano,
                        key = { "${arcKey}_${it.id}" } // Unique key per AB + Power
                    ) { poder ->
                        var showPowerDetailsDialog by remember { mutableStateOf(false) }
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
                        // Estágio do poder acima do Estágio atual do personagem — mesma regra
                        // de podeSelecionar pra Vantagens (mostra bloqueado com o motivo, não
                        // esconde da lista). Não se aplica ao sistema "poderes por estágio"
                        // (usaPoderesPorEstagioCard), que já tem seu próprio filtro acima.
                        val isStageLocked = !usaPoderesPorEstagioCard && !state.poderAtendeEstagio(poder.estagio)
                        val isCardLocked = locked || isFixed || isStageLocked

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    isStageLocked -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    selecionado -> MaterialTheme.colorScheme.primaryContainer
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            ),
                            border = if (selecionado) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 2.dp) // Reduced padding
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
                                                state.manifestacoesPoderes.remove("$arcKey#$idx")
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
                                // Por id do traço (ver applyAncestryVariantAdjustments em
                                // CriadorState), não por nome de raça: Meio-Demônio troca o
                                // marcador "ADAPTAVEL_OU_ANTECEDENTE_ARCANO_DEMONIO" por
                                // "ANTECEDENTE_ARCANO_DEMONIO_MEIO" ou "ADAPTAVEL" conforme
                                // a escolha do jogador.
                                val ehMeioDemonio = state.currentAncestryDef?.habilidades?.any {
                                    it.id == "ANTECEDENTE_ARCANO_DEMONIO_MEIO" ||
                                        it.id == "ADAPTAVEL_OU_ANTECEDENTE_ARCANO_DEMONIO"
                                } == true
                                val ppExibicao = if (
                                    state.compendioCidadeSolVaporAtivo &&
                                    arcKey == "DEMONIO" &&
                                    ehMeioDemonio &&
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
                                    displayNome = aspectOnlyPowerDisplayName(displayNome, arcKey)
                                    val isCustom = poder.origem.equals("CUSTOM", ignoreCase = true) || poder.id.startsWith("custom:") || poder.id.startsWith("fanmade:")
                                    if (isCustom) {
                                        displayNome = "$displayNome ⓒ"
                                    }

                                    Column(modifier = Modifier.clickable { showPowerDetailsDialog = true }) {
                                        Text(
                                            text = displayNome,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        // Categoria Customizada (ver model/CategoriaCustomizada.kt) — só
                                        // aparece pra Poderes customizados que o Mestre organizou numa categoria.
                                        poder.categoriaCustomizadaId?.let { catId ->
                                            val nomeCategoria = state.listaCategoriasCustomizadas.firstOrNull { it.id == catId }?.nome
                                            if (nomeCategoria != null) {
                                                Text(
                                                    text = nomeCategoria,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.secondary
                                                )
                                            }
                                        }
                                    }
                                val specialStage = state.poderesDisponiveisPorEstagioParaArcano(arcKey)[poder.id]
                                Text(
                                    when {
                                        usaPoderesPorEstagioCard && specialStage != null -> "$specialStage • PP: $ppExibicao"
                                        isStageLocked -> "Requer Estágio ${poder.estagio} • PP: $ppExibicao"
                                        else -> "PP: $ppExibicao"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isStageLocked) MaterialTheme.colorScheme.error else Color.Unspecified
                                )
                            }

                            // "Uma personagem pode adicionar uma nova Manifestação a um poder
                            // que já possui em vez de ganhar um novo poder" (livro básico, Vantagem
                            // Novos Poderes) — só some pra repetir num slot que veio de Novos
                            // Poderes (não nos slots iniciais do Antecedente Arcano); a Manifestação
                            // em si é uma anotação livre editável no chip do slot, não uma escolha
                            // daqui (ver dialog de edição na seção "Slots" acima).
                            if (!usaPoderesPorEstagioCard && selecionado) {
                                val boundary = state.getSlotsCountForArcano(arcKey) - state.getNovosPoderesBonusSlotsForArcano(arcKey)
                                val duplicateIdx = slots.withIndex().firstOrNull { (i, v) -> v == null && i >= boundary && i >= lockedCount }?.index
                                if (duplicateIdx != null) {
                                    Text(
                                        "+ Repetir num slot extra de Novos Poderes",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .padding(top = 2.dp)
                                            .clickable {
                                                slots[duplicateIdx] = poder.id
                                                state.syncPoderesSelecionadosFromSlots()
                                            }
                                    )
                                }
                            }

                            if (state.usarSemPontosDePoder) {
                                Text("Penalidade base: ${custoParaPenalidadeTexto(ppExibicao)}", style = MaterialTheme.typography.bodySmall)
                            }

                            if (showPowerDetailsDialog) {
                                val manifestacoesDisponiveis = poder.manifestacoes.filter { it.isNotBlank() }
                                val modificadoresDisponiveis = poder.modificadores.filter { mod ->
                                    mod.nome.isNotBlank() || mod.descricao.isNotBlank()
                                }
                                var displayNomeDialog = aspectOnlyPowerDisplayName(poder.nome.toFancyTitleCase(), arcKey)
                                val isCustomDialog = poder.origem.equals("CUSTOM", ignoreCase = true) || poder.id.startsWith("custom:") || poder.id.startsWith("fanmade:")
                                if (isCustomDialog) displayNomeDialog = "$displayNomeDialog ⓒ"

                                AlertDialog(
                                    onDismissRequest = { showPowerDetailsDialog = false },
                                    title = {
                                        Text(displayNomeDialog, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    },
                                    text = {
                                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                            if (poder.descricao.isNotBlank()) {
                                                Text(poder.descricao, style = MaterialTheme.typography.bodyMedium)
                                                Spacer(Modifier.height(8.dp))
                                            }
                                            Text("Distância: ${poder.distancia}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                            Text("Duração: ${poder.duracao}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)

                                            if (manifestacoesDisponiveis.isNotEmpty()) {
                                                Spacer(Modifier.height(8.dp))
                                                Text("Manifestações:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                                                manifestacoesDisponiveis.forEach { man ->
                                                    Text("• $man", style = MaterialTheme.typography.bodySmall)
                                                }
                                            }

                                            if (modificadoresDisponiveis.isNotEmpty()) {
                                                Spacer(Modifier.height(8.dp))
                                                Text("Modificadores:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                                                modificadoresDisponiveis.forEach { mod ->
                                                    Text(
                                                        "• ${mod.nome} (${mod.custo}): ${mod.descricao}",
                                                        style = MaterialTheme.typography.bodySmall
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    confirmButton = {
                                        TextButton(onClick = { showPowerDetailsDialog = false }) {
                                            Text("Fechar")
                                        }
                                    }
                                )
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
