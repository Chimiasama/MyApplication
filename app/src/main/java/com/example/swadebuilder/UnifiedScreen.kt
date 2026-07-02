package com.example.swadebuilder

import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.model.CriadorViewModel
import com.example.swadebuilder.model.EquipamentoCategoria
import com.example.swadebuilder.model.SuperPoder
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.model.listaDeEstagios
import com.example.swadebuilder.ui.MainSection
import com.example.swadebuilder.ui.components.SectionCard
import com.example.swadebuilder.ui.dialogs.ProgressosDialog
import com.example.swadebuilder.ui.sections.AncestralidadesSection
import com.example.swadebuilder.ui.sections.AtributosContent
import com.example.swadebuilder.ui.sections.ComplicacoesSection
import com.example.swadebuilder.ui.sections.CrystalHeartSection
import com.example.swadebuilder.ui.sections.EquipamentoSection
import com.example.swadebuilder.ui.sections.PericiasContent
import com.example.swadebuilder.ui.sections.PoderesSection
import com.example.swadebuilder.ui.sections.SummaryContent
import com.example.swadebuilder.ui.sections.SuperPoderesContent
import com.example.swadebuilder.ui.sections.TipoMonstroSection
import com.example.swadebuilder.ui.sections.TroposSection
import com.example.swadebuilder.ui.sections.VantagensContent
import com.example.swadebuilder.ui.sections.XpSection
import com.example.swadebuilder.util.MoneyUtils
import com.example.swadebuilder.util.SecurityUtils
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.semAcentos
import com.example.swadebuilder.util.toEditionDisplayName
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.File
import java.security.MessageDigest

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
// @Preview(showBackground = true) // Commented out to avoid build errors with ViewModel
@Composable
fun PreviewApp() {
    val state = remember { CriadorState() }
    val vm = remember { CriadorViewModel() }

    UnifiedScreen(
        state = state,
        viewModel = vm,
        equipamentoCategorias = emptyList(),
        superequipCategorias = emptyList(),
        listaSuperPoderes = emptyList(),
        onShowMessage = {},
        onUserFeedback = {},
        onRequestProgression = {}
    )
}

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun UnifiedScreen(
    state: CriadorState,
    viewModel: CriadorViewModel,
    equipamentoCategorias: List<EquipamentoCategoria>,
    superequipCategorias: List<EquipamentoCategoria>,
    listaSuperPoderes: List<SuperPoder>,
    modoOficialAtivo: Boolean = false,
    onShowMessage: (String) -> Unit,
    onUserFeedback: () -> Unit,
    onRequestProgression: () -> Unit
) {
    var showAllocDialog by rememberSaveable { mutableStateOf(false) }
    var currentSlotIndex by rememberSaveable { mutableIntStateOf(-1) }
    val context = LocalContext.current

    // --- estados para o MEIO-ELFO / MEIO-ORC ---
    var showMeioElfoDialog by rememberSaveable { mutableStateOf(false) }
    var showMeioOrcDialog by rememberSaveable { mutableStateOf(false) }
    var pendingAncestryKey by rememberSaveable { mutableStateOf<String?>(null) }
    // --------------------------------

    val availableSections = availableSectionsFor(state)
    var activeSection by rememberSaveable { mutableStateOf(MainSection.RESUMO) }

    val forcedSection = if (state.modoProgressaoAtivo) {
        null
    } else {
        when {
            state.mostrandoVantagensProgresso -> MainSection.VANTAGENS
            state.mostrandoPericiasProgresso -> MainSection.PERICIAS
            state.mostrandoAtributosProgresso -> MainSection.ATRIBUTOS
            else -> null
        }
    }
    LaunchedEffect(availableSections) {
        activeSection = resolveActiveSection(activeSection, availableSections)
    }

    LaunchedEffect(forcedSection) {
        forcedSection?.let { activeSection = it }
    }

    val pagerState = rememberPagerState(initialPage = activeSectionIndex(availableSections, activeSection)) {
        availableSections.size
    }
    val coroutineScope = rememberCoroutineScope()
    var lastAutoSaveSection by remember { mutableStateOf<MainSection?>(null) }

    LaunchedEffect(pagerState, availableSections) {
        snapshotFlow {
            val page = if (pagerState.isScrollInProgress) {
                pagerState.targetPage
            } else {
                pagerState.currentPage
            }
            page
        }.collect { page ->
            availableSections.getOrNull(page)?.let { activeSection = it }
        }
    }

    LaunchedEffect(activeSection, availableSections, pagerState.isScrollInProgress) {
        val targetIndex = activeSectionIndex(availableSections, activeSection)
        if (!pagerState.isScrollInProgress && targetIndex != pagerState.currentPage) {
            coroutineScope.launch {
                pagerState.animateScrollToPage(targetIndex)
            }
        }
    }

    val autoSaveJson = remember {
        Json {
            encodeDefaults = true
            prettyPrint = false
            ignoreUnknownKeys = true
            classDiscriminator = "type"
        }
    }
    var lastAutoSavedDigest by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(activeSection) {
        val previousSection = lastAutoSaveSection
        lastAutoSaveSection = activeSection

        if (previousSection == null || previousSection == activeSection || state.idAtual == null) {
            return@LaunchedEffect
        }

        delay(1800)

        val snapshotDigest = runCatching {
            with(MessageDigest.getInstance("SHA-256")) {
                val payload = autoSaveJson.encodeToString(state.toSnapshot().copy(checksum = null))
                digest(payload.toByteArray()).joinToString("") { "%02x".format(it) }
            }
        }.getOrNull() ?: return@LaunchedEffect

        if (snapshotDigest == lastAutoSavedDigest) return@LaunchedEffect

        try {
            viewModel.salvarPersonagem(context, state.nomePersonagem, silent = true)
            lastAutoSavedDigest = snapshotDigest
        } catch (e: Exception) {
            // Auto-save falhou silenciosamente para evitar interrupção do fluxo.
        }
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        Row(Modifier.fillMaxSize()) {
            CreatorNavigationRail(
                sections = availableSections,
                selectedSection = activeSection,
                enabledSections = { state.isSectionEnabled(it) },
                onSelectSection = {
                    onUserFeedback()
                    activeSection = it
                },
                tabStyle = state.estiloAbas,
                state = state
            )
            Column(Modifier.weight(1f)) {
                if (state.isNpcExibicao) {
                    Text(
                        text = "NPC FINALIZADO",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    HorizontalDivider()
                } else if (state.modoProgressaoAtivo) {
                    Text(
                        text = "MODO DE PROGRESSÃO",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    HorizontalDivider()
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val section = availableSections.getOrNull(page) ?: return@HorizontalPager
                    SectionDetailPane(
                        modifier = Modifier.fillMaxSize(),
                        state = state,
                        viewModel = viewModel,
                        selectedSection = section,
                        listaSuperPoderes = listaSuperPoderes,
                        equipamentoCategorias = equipamentoCategorias,
                        superequipCategorias = superequipCategorias,
                        onClearRequested = {
                            onUserFeedback()
                        },
                        onShowMessage = onShowMessage,
                        onRequestProgression = onRequestProgression,
                        onSelectAncestralidade = { nome ->
                            val key = nome.uppercase().semAcentos()
                            if (key != state.ancestralidade) {
                                if (key == "MEIO-ELFOS") {
                                    pendingAncestryKey = key
                                    showMeioElfoDialog = true
                                } else if (key == "MEIO-ORCS") {
                                    pendingAncestryKey = key
                                    showMeioOrcDialog = true
                                } else {
                                    pendingAncestryKey = null
                                    state.aplicarAncestralidade(
                                        key,
                                        viewModel.feedbackMessages as MutableList<String>
                                    )
                                }
                            }
                        },
                        onUseProgress = { index ->
                            if (viewModel.reserveProgressSlot(index)) {
                                currentSlotIndex = index
                                showAllocDialog = true
                            }
                        },
                        onUserFeedback = onUserFeedback
                    )
                }
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (state.isNpcExibicao) {
                Text(
                    text = "NPC FINALIZADO",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                HorizontalDivider()
            } else if (state.modoProgressaoAtivo) {
                Text(
                    text = "MODO DE PROGRESSÃO",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                HorizontalDivider()
            }

            CreatorTabRow(
                sections = availableSections,
                selectedSection = activeSection,
                enabledSections = { state.isSectionEnabled(it) },
                onSelectSection = {
                    onUserFeedback()
                    activeSection = it
                },
                tabStyle = state.estiloAbas,
                state = state
            )

            HorizontalPager(
                state = pagerState,
                userScrollEnabled = true, // Navigation controlled by enabled/disabled sections instead
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val section = availableSections.getOrNull(page) ?: return@HorizontalPager
                SectionDetailPane(
                    modifier = Modifier.fillMaxSize(),
                    state = state,
                    viewModel = viewModel,
                    selectedSection = section,
                    listaSuperPoderes = listaSuperPoderes,
                    equipamentoCategorias = equipamentoCategorias,
                    superequipCategorias = superequipCategorias,
                    onClearRequested = {
                        onUserFeedback()
                    },
                    onShowMessage = onShowMessage,
                    onRequestProgression = onRequestProgression,
                    onSelectAncestralidade = { nome ->
                        val key = nome.uppercase().semAcentos()
                        if (key != state.ancestralidade) {
                            if (key == "MEIO-ELFOS") {
                                pendingAncestryKey = key
                                showMeioElfoDialog = true
                            } else if (key == "MEIO-ORCS") {
                                pendingAncestryKey = key
                                showMeioOrcDialog = true
                            } else {
                                pendingAncestryKey = null
                                state.aplicarAncestralidade(
                                    key,
                                    viewModel.feedbackMessages as MutableList<String>
                                )
                            }
                        }
                    },
                    onUseProgress = { index ->
                        if (viewModel.reserveProgressSlot(index)) {
                            currentSlotIndex = index
                            showAllocDialog = true
                        }
                    },
                    onUserFeedback = onUserFeedback
                )
            }
        }
    }


    if (showMeioElfoDialog && pendingAncestryKey != null) {
        AlertDialog(
            onDismissRequest = {
                pendingAncestryKey = null
                showMeioElfoDialog = false
            },
            title = { Text("Meio-Elfo: escolha a herança") },
            text = {
                Text(
                    "Defina como a herança meio-élfica se manifesta:\n\n" +
                            "• Herança Élfica: começa com Agilidade em d6.\n" +
                            "• Herança Humana: ganha a habilidade Adaptável (uma Vantagem Novato extra)."
                )
            },
            confirmButton = {
                // Herança Élfica (Agilidade d6)
                TextButton(
                    onClick = {
                        val key = pendingAncestryKey ?: return@TextButton

                        // Aplica a ancestralidade Meio-Elfo
                        state.aplicarAncestralidade(
                            key,
                            viewModel.feedbackMessages as MutableList<String>
                        )

                        // Garante Agilidade em d6 (raw = 6) se ainda estiver abaixo
                        val agiState = state.valoresAtributos["AGILIDADE"]
                        if (agiState != null && agiState.intValue < 6) {
                            agiState.intValue = 6
                        }

                        state.meioElfoAgil = true
                        state.recalcularPontosAtributo(viewModel.feedbackMessages as MutableList<String>)
                        pendingAncestryKey = null
                        showMeioElfoDialog = false
                    }
                ) {
                    Text("Herança Élfica (Agilidade d6)")
                }
            },
            dismissButton = {
                // Herança Humana (Adaptável)
                TextButton(
                    onClick = {
                        val key = pendingAncestryKey ?: return@TextButton
                        val hadMeioElfoAgil = state.meioElfoAgil
                        state.meioElfoAgil = false

                        // Aplica a ancestralidade Meio-Elfo
                        state.aplicarAncestralidade(
                            key,
                            viewModel.feedbackMessages as MutableList<String>
                        )

                        // Grants Adaptable trait (free Novice Edge slot). Does NOT add raw PV.
                        // The 'Adaptável' trait is injected via CriadorState.applyAncestryVariantAdjustments
                        // and detected by temAdaptavel().

                        if (hadMeioElfoAgil) {
                            val agilityKey = "AGILIDADE"
                            val agiState = state.valoresAtributos[agilityKey]
                            val agiStack = state.paCostStackPorAtributo[agilityKey]
                            if (agiState != null && agiStack?.isEmpty() == true && agiState.intValue == 6) {
                                agiState.intValue = 4
                            }
                        }

                        pendingAncestryKey = null
                        showMeioElfoDialog = false
                    }
                ) {
                    Text("Herança Humana (Adaptável)")
                }
            }
        )
    }

    if (showMeioOrcDialog && pendingAncestryKey != null) {
        AlertDialog(
            onDismissRequest = {
                pendingAncestryKey = null
                showMeioOrcDialog = false
            },
            title = { Text("Meio-Orc: escolha o atributo inicial") },
            text = {
                Text(
                    "Meio-Orcs herdam a força ou resistência de seus ancestrais. Escolha um atributo para começar em d6:"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val key = pendingAncestryKey ?: return@TextButton
                        // Vigor (Default state behavior)
                        state.meioOrcForca = false
                        state.aplicarAncestralidade(
                            key,
                            viewModel.feedbackMessages as MutableList<String>
                        )
                        pendingAncestryKey = null
                        showMeioOrcDialog = false
                    }
                ) {
                    Text("Vigor d6 (Resistência)")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        val key = pendingAncestryKey ?: return@TextButton
                        // Força
                        state.meioOrcForca = true
                        state.aplicarAncestralidade(
                            key,
                            viewModel.feedbackMessages as MutableList<String>
                        )
                        // O JSON define Vigor 2 (d6) por padrão. O código `atributoBaseRacial` vai sobrescrever
                        // para Força 6 / Vigor 4 se meioOrcForca=true.
                        // Mas aplicarAncestralidade chama recalcularPontosAtributo, que deve pegar o novo base.
                        pendingAncestryKey = null
                        showMeioOrcDialog = false
                    }
                ) {
                    Text("Força d6")
                }
            }
        )
    }

    if (showAllocDialog) {
        ProgressosDialog(
            state = state,
            viewModel = viewModel,
            onShowMessage = onShowMessage,
            slotIndex = currentSlotIndex,
            allAdvantages = viewModel.gameDataStore.getVantagens(),
            listaAtributos = viewModel.gameDataStore.getAtributos(),
            listaPericias = viewModel.gameDataStore.getPericias(),
            mapaAtributosDisplay = viewModel.gameDataStore.getMapaAtributosDisplay(),
            mapaPericias = viewModel.gameDataStore.getMapaPericias(),
            allEstagios = listaDeEstagios,
            onDismiss = {
                viewModel.cancelPendingProgressReservation(currentSlotIndex)
                showAllocDialog = false
                activeSection = MainSection.XP
            }
        )
    }
}


private data class SectionTab(
    val section: MainSection,
    val label: String
)

@Composable
private fun CreatorTabRow(
    sections: List<MainSection>,
    selectedSection: MainSection,
    enabledSections: (MainSection) -> Boolean,
    onSelectSection: (MainSection) -> Unit,
    tabStyle: TabStyle,
    state: CriadorState
) {
    val tabs = remember(
        sections,
        state.compendioArteDaGuerraAtivo,
        state.tropoSelecionado?.tecnicasIniciais
    ) { sections.map { SectionTab(it, it.tabLabel(state)) } }
    if (tabs.isEmpty()) {
        return
    }
    val selectedIndex = tabs.indexOfFirst { it.section == selectedSection }.coerceAtLeast(0)

    PrimaryScrollableTabRow(
        selectedTabIndex = selectedIndex,
        edgePadding = 0.dp,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary,
        indicator = {
            TabRowDefaults.PrimaryIndicator(
                modifier = Modifier.tabIndicatorOffset(selectedIndex),
                height = 3.dp,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)
            )
        }
    ) {
        tabs.forEach { tab ->
            val enabled = enabledSections(tab.section)
            val isSelected = tab.section == selectedSection
            Tab(
                selected = isSelected,
                enabled = enabled,
                onClick = { if (enabled) onSelectSection(tab.section) },
                text = if (tabStyle == TabStyle.TEXTO) {
                    {
                        Text(
                            tab.label,
                            fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                        )
                    }
                } else null,
                icon = if (tabStyle == TabStyle.ICONES) { { Icon(tab.section.icon(), null) } } else null,
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.alpha(if (enabled) 1f else 0.5f)
            )
        }
    }
}

@Composable
private fun CreatorNavigationRail(
    sections: List<MainSection>,
    selectedSection: MainSection,
    enabledSections: (MainSection) -> Boolean,
    onSelectSection: (MainSection) -> Unit,
    tabStyle: TabStyle,
    state: CriadorState
) {
    NavigationRail {
        Column(
            modifier = Modifier
                .width(80.dp) // Ensure explicit width if needed, or rely on intrinsic
                .verticalScroll(rememberScrollState())
        ) {
            sections.forEach { section ->
                val enabled = enabledSections(section)
                NavigationRailItem(
                    selected = section == selectedSection,
                    enabled = enabled,
                    onClick = { if (enabled) onSelectSection(section) },
                    icon = {
                        if (tabStyle == TabStyle.ICONES) {
                            Icon(section.icon(), contentDescription = null)
                        }
                    },
                    label = {
                        if (tabStyle == TabStyle.TEXTO) {
                            Text(section.tabLabel(state), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    },
                    alwaysShowLabel = (tabStyle == TabStyle.TEXTO)
                )
            }
        }
    }
}

private fun MainSection.icon(): ImageVector = when (this) {
    MainSection.RESUMO -> Icons.Default.Description
    MainSection.ANCESTRALIDADES -> Icons.Default.Face
    MainSection.TROPOS -> Icons.Default.AccountBox
    MainSection.MONSTRO -> Icons.Default.BugReport
    MainSection.COMPLICACOES -> Icons.Default.Warning
    MainSection.ATRIBUTOS -> Icons.Default.FitnessCenter
    MainSection.PERICIAS -> Icons.Default.School
    MainSection.VANTAGENS -> Icons.Default.Star
    MainSection.EQUIPAMENTOS -> Icons.Default.ShoppingCart
    MainSection.PODERES -> Icons.Default.FlashOn
    MainSection.XP -> Icons.Default.ArrowUpward
    MainSection.CRYSTAL_HEART -> Icons.Default.Favorite
}

private fun MainSection.tabLabel(state: CriadorState): String = when (this) {
    MainSection.ANCESTRALIDADES -> "Ancestr."
    MainSection.TROPOS -> "Tropos"
    MainSection.COMPLICACOES -> "Complic."
    MainSection.ATRIBUTOS -> "Atributos"
    MainSection.PERICIAS -> "Perícias"
    MainSection.VANTAGENS -> "Vantagens"
    MainSection.EQUIPAMENTOS -> "Equip."
    MainSection.RESUMO -> "Resumo"
    MainSection.PODERES -> if (
        state.compendioArteDaGuerraAtivo &&
        (state.tropoSelecionado?.tecnicasIniciais ?: 0) > 0
    ) {
        "Técnicas"
    } else {
        "Poderes"
    }
    MainSection.XP -> "XP"
    MainSection.MONSTRO -> "Monstro"
    MainSection.CRYSTAL_HEART -> "Crystal Heart".toEditionDisplayName()
}

private fun activeSectionIndex(
    sections: List<MainSection>,
    activeSection: MainSection
): Int {
    val index = sections.indexOf(activeSection)
    return if (index >= 0) index else 0
}

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
private fun SectionDetailPane(
    modifier: Modifier,
    state: CriadorState,
    viewModel: CriadorViewModel,
    selectedSection: MainSection,
    listaSuperPoderes: List<SuperPoder>,
    equipamentoCategorias: List<EquipamentoCategoria>,
    superequipCategorias: List<EquipamentoCategoria>,
    onClearRequested: () -> Unit,
    onShowMessage: (String) -> Unit,
    onRequestProgression: () -> Unit,
    onSelectAncestralidade: (String) -> Unit,
    onUseProgress: (Int) -> Unit,
    onUserFeedback: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        if (state.isNpcExibicao) {
            Box(Modifier.fillMaxSize()) {
                SummaryTabContent(
                    state = state,
                    viewModel = viewModel,
                    onClearRequested = onClearRequested,
                    onShowMessage = onShowMessage,
                    onRequestProgression = onRequestProgression
                )

                TextButton(
                    onClick = { state.isNpcExibicao = false },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text("Voltar para Criação")
                }
            }
        } else if (state.modoProgressaoAtivo) {
            ProgressionDetailContent(
                state = state,
                viewModel = viewModel,
                selectedSection = selectedSection,
                equipamentoCategorias = equipamentoCategorias,
                superequipCategorias = superequipCategorias,
                onClearRequested = onClearRequested,
                onShowMessage = onShowMessage,
                onRequestProgression = onRequestProgression,
                onUseProgress = onUseProgress,
                onUserFeedback = onUserFeedback
            )
        } else {
            CreationDetailContent(
                state = state,
                viewModel = viewModel,
                selectedSection = selectedSection,
                listaSuperPoderes = listaSuperPoderes,
                equipamentoCategorias = equipamentoCategorias,
                superequipCategorias = superequipCategorias,
                onClearRequested = onClearRequested,
                onShowMessage = onShowMessage,
                onRequestProgression = onRequestProgression,
                onSelectAncestralidade = onSelectAncestralidade,
                onUserFeedback = onUserFeedback
            )
        }
    }
}

private fun availableSectionsFor(state: CriadorState): List<MainSection> {
    if (state.isNpcExibicao) return listOf(MainSection.RESUMO)
    val sections = mutableListOf(MainSection.RESUMO)
    if (state.modoProgressaoAtivo && !state.modoLivre) {
        if (state.compendioCrystalHeartAtivo) {
            sections += MainSection.CRYSTAL_HEART
        }
        sections += MainSection.EQUIPAMENTOS
        sections += MainSection.XP
        return sections
    }

    if (!state.compendioWiseguysAtivo && !state.compendioDeadlandsAtivo) {
        sections += MainSection.ANCESTRALIDADES
    }
    if (state.compendioArteDaGuerraAtivo) {
        sections += MainSection.TROPOS
        if (state.isAdgLockedMode) {
            return sections
        }
    }
    if (state.modoMonstroAtivo) {
        sections += MainSection.MONSTRO
    }
    sections += MainSection.COMPLICACOES
    sections += MainSection.ATRIBUTOS
    sections += MainSection.PERICIAS
    sections += MainSection.VANTAGENS
    if (state.compendioCrystalHeartAtivo) {
        sections += MainSection.CRYSTAL_HEART
    }

    val hasArcano = state.temAntecedenteArcano() && !state.celestialAAMilagresDesabilitado
    val mostraPoderesArcanos = hasArcano && !state.compendioCrystalHeartAtivo
    val mostraTecnicasTropo = state.compendioArteDaGuerraAtivo &&
        (state.tropoSelecionado?.tecnicasIniciais ?: 0) > 0 &&
        !state.compendioCrystalHeartAtivo
    if (!state.compendioWiseguysAtivo && (mostraPoderesArcanos || mostraTecnicasTropo || state.modoSupers)) {
        sections += MainSection.PODERES
    }

    sections += MainSection.EQUIPAMENTOS
    return sections
}

internal fun resolveActiveSection(
    requestedSection: MainSection,
    availableSections: List<MainSection>
): MainSection {
    if (availableSections.isEmpty()) {
        return MainSection.RESUMO
    }
    if (requestedSection in availableSections) {
        return requestedSection
    }
    if (MainSection.RESUMO in availableSections) {
        return MainSection.RESUMO
    }
    return availableSections.first()
}

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
private fun ProgressionDetailContent(
    state: CriadorState,
    viewModel: CriadorViewModel,
    selectedSection: MainSection,
    equipamentoCategorias: List<EquipamentoCategoria>,
    superequipCategorias: List<EquipamentoCategoria>,
    onClearRequested: () -> Unit,
    onShowMessage: (String) -> Unit,
    onRequestProgression: () -> Unit,
    onUseProgress: (Int) -> Unit,
    onUserFeedback: () -> Unit
) {
    when (selectedSection) {
        MainSection.VANTAGENS -> {
            SectionCard(
                title    = "Vantagens",
                icon     = Icons.Default.Star,
                showHeader = false
            ) {
                VantagensContent(
                    state = state,
                    multiplosAAHabilitados = state.permiteMultiAntecedenteArcano,
                    viewModel = viewModel,
                    allAdvantages = viewModel.gameDataStore.getVantagens(),
                allSkills = viewModel.gameDataStore.getPericias(),
                allEstagios = listaDeEstagios,
                    onUserFeedback = onUserFeedback
                )
            }

            if (state.mostrandoPoderesProgresso || state.arcanoCompraPendente()) {
                Spacer(Modifier.height(8.dp))
                PoderesSection(
                    state = state,
                    arcanoInfoMap = viewModel.gameDataStore.getArcanoInfoMap(),
                    onShowMessage = onShowMessage
                )
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(thickness = 3.dp)

            Button(
                onClick = {
                    viewModel.finishAdvantageAdvancement()
                    state.mostrandoVantagensProgresso = false
                },
                enabled = state.pontosVantagem == 0 && !state.arcanoCompraPendente(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Confirmar Vantagem e Voltar")
            }
            TextButton(
                onClick = {
                    viewModel.cancelAdvancementInProgress()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar")
            }
        }
        MainSection.PERICIAS -> {
            PericiasContent(
                state = state,
                feedbackMessages = viewModel.feedbackMessages as MutableList<String>,
                onUserFeedback = onUserFeedback
            )

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(thickness = 3.dp)

            Button(
                onClick = {
                    viewModel.finishSkillAdvancement()
                    state.mostrandoPericiasProgresso = false
                },
                enabled = state.pontosPericia == 0,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Confirmar Perícias e Voltar")
            }
            TextButton(
                onClick = {
                    viewModel.cancelAdvancementInProgress()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar")
            }
        }
        MainSection.ATRIBUTOS -> {
            SectionCard(
                title    = "Atributos",
                icon     = Icons.Default.FitnessCenter,
                showHeader = false
            ) {
                AtributosContent(
                    state = state,
                    listaAtributos = viewModel.gameDataStore.getAtributos(),
                    mapaAtributosDisplay = viewModel.gameDataStore.getMapaAtributosDisplay(),
                    mapaAtributosDescricao = viewModel.gameDataStore.currentSnapshot()?.mapaAtributosDescricao ?: emptyMap(),
                    onUserFeedback = onUserFeedback
                )
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(thickness = 3.dp)

            Button(
                onClick = {
                    viewModel.finishAttributeAdvancement()
                    state.mostrandoAtributosProgresso = false
                },
                enabled = state.pontosAtributo == 0,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Confirmar Atributo e Voltar")
            }
            TextButton(
                onClick = {
                    viewModel.cancelAdvancementInProgress()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar")
            }
        }
        MainSection.CRYSTAL_HEART -> CrystalHeartSection(
            state = state,
            viewModel = viewModel
        )
        MainSection.EQUIPAMENTOS -> EquipamentoSection(
            state = state,
            equipamentoCategorias = equipamentoCategorias,
            superequipCategorias = superequipCategorias,
            onUserFeedback = onUserFeedback,
            onLogFeedback = viewModel::logFeedback
        )
        MainSection.XP -> XpSection(
            state = state,
            allAdvantages = viewModel.gameDataStore.getVantagens(),
            onUseProgress = onUseProgress,
            onUndo = {
                viewModel.undoLastProgressAction()
            }
        )
        else -> SummaryTabContent(
            state = state,
            viewModel = viewModel,
            onClearRequested = onClearRequested,
            onShowMessage = onShowMessage,
            onRequestProgression = onRequestProgression
        )
    }
}

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
private fun CreationDetailContent(
    state: CriadorState,
    viewModel: CriadorViewModel,
    selectedSection: MainSection,
    listaSuperPoderes: List<SuperPoder>,
    equipamentoCategorias: List<EquipamentoCategoria>,
    superequipCategorias: List<EquipamentoCategoria>,
    onClearRequested: () -> Unit,
    onShowMessage: (String) -> Unit,
    onRequestProgression: () -> Unit,
    onSelectAncestralidade: (String) -> Unit,
    onUserFeedback: () -> Unit
) {
    val creationLocked = state.criacaoBasicaCongelada

    when (selectedSection) {
        MainSection.RESUMO -> SummaryTabContent(
            state = state,
            viewModel = viewModel,
            onClearRequested = onClearRequested,
            onShowMessage = onShowMessage,
            onRequestProgression = onRequestProgression
        )
        MainSection.ANCESTRALIDADES -> AncestralidadesSection(
            state = state,
            currentAncestralidade = state.ancestralidade,
            supersLocked = creationLocked || !state.isSectionEnabled(MainSection.ANCESTRALIDADES),
            ancestralidadeEmFoco = state.ancestralidadeEmFoco,
            feedbackMessages = viewModel.feedbackMessages as MutableList<String>,
            onSelectAncestralidade = onSelectAncestralidade,
            onUserFeedback = onUserFeedback
        )
        MainSection.TROPOS -> TroposSection(
            state = state,
            listaTropos = viewModel.gameDataStore.getTropos(),
            listaVantagens = viewModel.gameDataStore.getVantagens(),
            feedbackMessages = viewModel.feedbackMessages as MutableList<String>,
            onUserFeedback = onUserFeedback
        )
        MainSection.MONSTRO -> TipoMonstroSection(
            state = state,
            onUserFeedback = onUserFeedback,
            onLogFeedback = viewModel::logFeedback
        )
        MainSection.COMPLICACOES -> ComplicacoesSection(
            state = state,
            feedbackMessages = viewModel.feedbackMessages as MutableList<String>,
            onUserFeedback = onUserFeedback,
            onLogFeedback = viewModel::logFeedback
        )
        MainSection.ATRIBUTOS -> SectionCard(
            title    = "Atributos",
            icon     = Icons.Default.FitnessCenter,
            showHeader = false
        ) {
            AtributosContent(
                state = state,
                listaAtributos = viewModel.gameDataStore.getAtributos(),
                mapaAtributosDisplay = viewModel.gameDataStore.getMapaAtributosDisplay(),
                mapaAtributosDescricao = viewModel.gameDataStore.currentSnapshot()?.mapaAtributosDescricao ?: emptyMap(),
                onUserFeedback = onUserFeedback
            )
        }
        MainSection.PERICIAS -> PericiasContent(
            state = state,
            feedbackMessages = viewModel.feedbackMessages as MutableList<String>,
            onUserFeedback = onUserFeedback
        )
        MainSection.VANTAGENS -> SectionCard(
            title    = "Vantagens",
            icon     = Icons.Default.Star,
            showHeader = false
        ) {
            VantagensContent(
                state = state,
                multiplosAAHabilitados = state.permiteMultiAntecedenteArcano,
                viewModel = viewModel,
                allAdvantages = viewModel.gameDataStore.getVantagens(),
                allSkills = viewModel.gameDataStore.getPericias(),
                allEstagios = listaDeEstagios,
                onUserFeedback = onUserFeedback
            )
        }
        MainSection.CRYSTAL_HEART -> CrystalHeartSection(
            state = state,
            viewModel = viewModel
        )
        MainSection.PODERES -> {
            if (!state.compendioCrystalHeartAtivo) {
                PoderesSection(
                    state = state,
                    arcanoInfoMap = viewModel.gameDataStore.getArcanoInfoMap(),
                    onShowMessage = onShowMessage
                )
                Spacer(Modifier.height(8.dp))
            }
            SuperPoderesSection(
                state = state,
                listaSuperPoderes = listaSuperPoderes,
                allAdvantages = viewModel.gameDataStore.getVantagens(),
                onShowMessage = onShowMessage
            )
        }
        MainSection.EQUIPAMENTOS -> EquipamentoSection(
            state = state,
            equipamentoCategorias = equipamentoCategorias,
            superequipCategorias = superequipCategorias,
            onUserFeedback = onUserFeedback,
            onLogFeedback = viewModel::logFeedback
        )
        else -> SummaryTabContent(
            state = state,
            viewModel = viewModel,
            onClearRequested = onClearRequested,
            onShowMessage = onShowMessage,
            onRequestProgression = onRequestProgression
        )
    }
}

@Composable
private fun SummaryTabContent(
    state: CriadorState,
    viewModel: CriadorViewModel,
    onClearRequested: () -> Unit,
    onShowMessage: (String) -> Unit,
    onRequestProgression: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Restore image selection logic
    val portraitLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        coroutineScope.launch {
            viewModel.atualizarRetrato(context, uri)
        }
    }
    val portraitFile = remember(state.portraitFileName, context) {
        state.portraitFileName?.let {
            try {
                val portraitsDir = File(context.filesDir, "portraits")
                SecurityUtils.getSafeChildFile(portraitsDir, it)
            } catch (e: Exception) {
                null
            }
        }
    }
    val portraitUri = portraitFile?.takeIf { it.exists() }?.let(Uri::fromFile)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        // Pass image selection data to SummaryContent which now houses the placeholder
        SummaryContent(
            state = state,
            imageUri = portraitUri,
            onSelectImage = { portraitLauncher.launch("image/*") }
        )
        Spacer(Modifier.height(12.dp))

        // Removed CharacterPortraitCard and Spacer
    }
}

@Composable
private fun PoderesSection(
    state: CriadorState,
    arcanoInfoMap: Map<String, Triple<Int, Int, String>>,
    onShowMessage: (String) -> Unit = {}
) {
    val temArcano = state.temAntecedenteArcano()
    if (temArcano && !state.celestialAAMilagresDesabilitado) {
        HorizontalDivider(thickness = 1.dp)
        SectionCard(
            title = "Poderes",
            icon = Icons.Default.FlashOn,
            showHeader = false
        ) {
            com.example.swadebuilder.ui.sections.PoderesSection(
                state = state,
                arcanoInfoMap = arcanoInfoMap,
                onShowMessage = onShowMessage
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
private fun SuperPoderesSection(
    state: CriadorState,
    listaSuperPoderes: List<SuperPoder>,
    allAdvantages: List<Vantagem>,
    onShowMessage: (String) -> Unit
) {
    if (state.modoSupers) {
        SuperPoderesContent(
            state = state,
            listaSuperPoderes = listaSuperPoderes,
            allAdvantages = allAdvantages,
            onShowMessage = onShowMessage
        )
    }
}

@Composable
private fun EquipamentoSection(
    state: CriadorState,
    equipamentoCategorias: List<EquipamentoCategoria>,
    superequipCategorias: List<EquipamentoCategoria>,
    onUserFeedback: () -> Unit,
    onLogFeedback: (String) -> Unit = {}
) {
    val hasMusculoso = state.vantagensSelecionadas.any { it.nome.keyify() == "MUSCULOSO" }
    val hasSoldado = state.vantagensSelecionadas.any { it.nome.keyify() == "SOLDADO" }
    val isPersonagemRobotico = state.isPersonagemRobotico()
    val tensaoLimite = if (isPersonagemRobotico) {
        state.limiteModsRoboticos()
    } else {
        state.valorLimiteTensao().second
    }

    EquipamentoSection(
        state = state,
        dinheiro = state.dinheiro,
        requisicao = state.requisicao,
        usaRiqueza = state.usaRiqueza,
        usaRequisicao = state.usaRequisicao,
        dadoRiqueza = state.dadoRiqueza,
        pcTotal = state.pontosComplicacao,
        pcLivres = (state.pontosComplicacao - state.pontosComplicacaoGastos).coerceAtLeast(0),
        recursosPcUsados = state.cpRecursosStack.size,
        emProgresso = state.emProgresso,
        modoProgressaoAtivo = state.modoProgressaoAtivo,
        onUsarPontosBonusEmRecursos = {
            if (state.usaRiqueza || state.usaRequisicao) return@EquipamentoSection
            val pcLivresLocal =
                (state.pontosComplicacao - state.pontosComplicacaoGastos).coerceAtLeast(0)
            if (pcLivresLocal > 0 && state.cpRecursosStack.isEmpty()) {
                state.cpRecursosStack.add(Unit)
                state.pontosComplicacaoGastos += 1
                if (state.compendioPathfinderAtivo) {
                    state.addPathfinderMoney(60000)
                } else {
                    state.dinheiro += 500
                }
            }
        },
        onDesfazerPontosBonusEmRecursos = {
            if (state.usaRiqueza || state.usaRequisicao) return@EquipamentoSection
            val checkAmount = if (state.compendioPathfinderAtivo) 60000 else 500
            if (state.cpRecursosStack.isNotEmpty() && state.dinheiro >= checkAmount) {
                state.cpRecursosStack.removeAt(state.cpRecursosStack.lastIndex)
                state.pontosComplicacaoGastos =
                    (state.pontosComplicacaoGastos - 1).coerceAtLeast(0)
                if (state.compendioPathfinderAtivo) {
                    state.spendPathfinderMoney(60000)
                } else {
                    state.dinheiro -= 500
                }
            }
        },
        onEquipamentoDoubleClick = { equipamento ->
            val custo = MoneyUtils.parseCostInBaseUnit(equipamento.custo, state.compendioPathfinderAtivo)

            // NEW LOGIC: Organic BP Spend
            val moneyNeeded = if (custo > state.dinheiro) custo - state.dinheiro else 0
            val rate = if (state.compendioPathfinderAtivo) 60000 else 500
            val bpNeeded = if (moneyNeeded > 0) kotlin.math.ceil(moneyNeeded.toDouble() / rate).toInt() else 0
            val pcLivres = (state.pontosComplicacao - state.pontosComplicacaoGastos).coerceAtLeast(0)

            val canAfford = state.usaRiqueza || state.usaRequisicao || (custo <= state.dinheiro) || (bpNeeded <= pcLivres)

            if (canAfford) {
                // Auto-convert BP to money if needed
                if (!state.usaRiqueza && !state.usaRequisicao && bpNeeded > 0) {
                     repeat(bpNeeded) {
                         state.gastarPcParaRecursos()
                     }
                }

                state.equipamentosComprados.add(equipamento)
                if (!state.usaRiqueza && !state.usaRequisicao) {
                    if (state.compendioPathfinderAtivo) {
                        state.spendPathfinderMoney(custo)
                    } else {
                        state.dinheiro -= custo
                    }
                }
                onLogFeedback("Equipamento ${equipamento.nome} adicionado.")
                onUserFeedback()
            } else {
                val missing = moneyNeeded
                onLogFeedback("Faltam recursos para obter o equipamento ${equipamento.nome}.")
                onUserFeedback()
            }
        },
        equipamentosComprados = state.equipamentosComprados,
        onRemoveEquipamentoClick = { equipamento ->
            if (!state.modoProgressaoAtivo && equipamento.origemGrant != null) {
                onLogFeedback("Item de vantagem (fixo na criação).")
                onUserFeedback()
            } else {
                val custo = MoneyUtils.parseCostInBaseUnit(equipamento.custo, state.compendioPathfinderAtivo)
                state.equipamentosComprados.remove(equipamento)
                if (!state.usaRiqueza && !state.usaRequisicao) {
                    if (state.compendioPathfinderAtivo) {
                        state.addPathfinderMoney(custo)
                    } else {
                        state.dinheiro += custo
                    }

                    // NEW LOGIC: Organic BP Refund
                    val rate = if (state.compendioPathfinderAtivo) 60000 else 500
                    while (state.cpRecursosStack.isNotEmpty() && state.dinheiro >= rate) {
                        state.devolverPcDeRecursos()
                    }
                }
                onLogFeedback("Equipamento ${equipamento.nome} removido.")
                onUserFeedback()
            }
        },
        categorias = equipamentoCategorias,
        superequipCategorias =
            if (state.modoSupers) superequipCategorias else emptyList(),
        tensaoTotal = state.totalTensaoCibernetica(),
        tensaoLimite = tensaoLimite,
        mechaSlotsTotal = state.totalSlotsMecha(),
        isPersonagemRobotico = isPersonagemRobotico,
        forcaRaw = state.valoresAtributos["FORCA"]?.intValue ?: 4,
        hasMusculoso = hasMusculoso,
        hasSoldado = hasSoldado,
        soldadoCargaAtivo = state.soldadoCargaAtivo,
        onEditarDinheiro = { novoValor -> state.dinheiro = novoValor },
        onEditarRequisicao = { novoValor -> state.requisicao = novoValor },
        onToggleSoldadoCarga = {
            if (hasSoldado) {
                state.soldadoCargaAtivo = !state.soldadoCargaAtivo
            }
        },
        compendioFantasiaAtivo = state.compendioFantasiaAtivo,
        compendioHorrorAtivo = state.compendioHorrorAtivo,
        compendioSciFiAtivo = state.compendioSciFiAtivo,
        compendioPathfinderAtivo = state.compendioPathfinderAtivo,
        compendioDeadlandsAtivo = state.compendioDeadlandsAtivo,
        compendioArteDaGuerraAtivo = state.compendioArteDaGuerraAtivo,
        compendioCidadeSolVaporAtivo = state.compendioCidadeSolVaporAtivo,
        compendioWiseguysAtivo = state.compendioWiseguysAtivo,
        compendioCrystalHeartAtivo = state.compendioCrystalHeartAtivo,
        modoOficialAtivo = state.modoOficialAtivo,
        onUserFeedback = onUserFeedback
    )
}
