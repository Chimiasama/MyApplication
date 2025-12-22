package com.example.swadebuilder

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.launch
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.model.CriadorViewModel
import com.example.swadebuilder.model.EquipamentoCategoria
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
import com.example.swadebuilder.ui.components.CharacterPortraitCard
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.semAcentos
import com.example.swadebuilder.util.toEditionDisplayName
import kotlinx.serialization.json.JsonPrimitive
import android.net.Uri
import java.io.File

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
    if (state.modoSupers) {
        Log.d("DEBUG", "modoSupers é ${state.modoSupers}")
    }

    var showAllocDialog by rememberSaveable { mutableStateOf(false) }
    var currentSlotIndex by rememberSaveable { mutableIntStateOf(-1) }
    var showClearDialog by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    // --- estados para o MEIO-ELFO ---
    var showMeioElfoDialog by rememberSaveable { mutableStateOf(false) }
    var pendingMeioElfoKey by rememberSaveable { mutableStateOf<String?>(null) }
    // --------------------------------

    val availableSections = availableSectionsFor(state)
    var activeSection by rememberSaveable { mutableStateOf(MainSection.RESUMO) }

    val forcedSection = when {
        state.mostrandoVantagensProgresso -> MainSection.VANTAGENS
        state.mostrandoPericiasProgresso -> MainSection.PERICIAS
        state.mostrandoAtributosProgresso -> MainSection.ATRIBUTOS
        else -> null
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

    LaunchedEffect(pagerState, availableSections) {
        snapshotFlow {
            val page = if (pagerState.isScrollInProgress) {
                pagerState.targetPage
            } else {
                pagerState.currentPage
            }
            page
        }.collect { page ->
            availableSections.getOrNull(page)?.let {
                activeSection = it
                // Auto-save ao mudar de aba
                viewModel.autoSave(context)
            }
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

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        if (state.modoProgressaoAtivo) {
            Text(
                text = "MODO DE PROGRESSÃO",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            HorizontalDivider()
        }

        CreatorTabRow(
            sections = availableSections,
            selectedSection = activeSection,
            onSelectSection = {
                onUserFeedback()
                activeSection = it
            }
        )

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
                    showClearDialog = true
                },
                onShowMessage = onShowMessage,
                onRequestProgression = onRequestProgression,
                onSelectAncestralidade = { nome ->
                    val key = nome.uppercase().semAcentos()
                    if (key != state.ancestralidade) {
                        if (key == "MEIO-ELFOS") {
                            pendingMeioElfoKey = key
                            showMeioElfoDialog = true
                        } else {
                            pendingMeioElfoKey = null
                            state.aplicarAncestralidade(
                                key,
                                viewModel.feedbackMessages as MutableList<String>
                            )
                        }
                    }
                },
                onUseProgress = { index ->
                    currentSlotIndex = index
                    showAllocDialog = true
                },
                onUserFeedback = onUserFeedback
            )
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Limpar personagem") },
            text = { Text("Deseja limpar a ficha atual e iniciar um novo personagem?") },
            confirmButton = {
                TextButton(onClick = {
                    val cartaSelvagem = state.cartaSelvagem
                    val maisPontosPericias = state.maisPontosPericias
                    val modoSupers = state.modoSupers
                    val compendioFantasiaAtivo = state.compendioFantasiaAtivo
                    val compendioHorrorAtivo = state.compendioHorrorAtivo
                    val compendioSciFiAtivo = state.compendioSciFiAtivo
                    val compendioBuscatrilhaAtivo = state.compendioBuscatrilhaAtivo
                    val compendioDeadlandsAtivo = state.compendioDeadlandsAtivo
                    val compendioCrystalHeartAtivo = state.compendioCrystalHeartAtivo
                    val compendioArteDaGuerraAtivo = state.compendioArteDaGuerraAtivo
                    val compendioCidadeSolVaporAtivo = state.compendioCidadeSolVaporAtivo
                    val compendioWiseguysAtivo = state.compendioWiseguysAtivo
                    val modoMonstroAtivo = state.modoMonstroAtivo
                    val usarEspecializacoesDePericia = state.usarEspecializacoesDePericia
                    val grandesResponsabilidades = state.grandesResponsabilidades
                    val regraMultiplosIdiomas = state.regraMultiplosIdiomas
                    val heroisSemArmadura = state.heroisSemArmadura
                    val nasceUmHeroi = state.nasceUmHeroi
                    val usarSemPontosDePoder = state.usarSemPontosDePoder

                    viewModel.resetStateParaNovoPersonagem(
                        cartaSelvagem = cartaSelvagem,
                        maisPontosPericias = maisPontosPericias,
                        modoSupers = modoSupers,
                        compendioFantasiaAtivo = compendioFantasiaAtivo,
                        compendioHorrorAtivo = compendioHorrorAtivo,
                        compendioSciFiAtivo = compendioSciFiAtivo,
                        compendioBuscatrilhaAtivo = compendioBuscatrilhaAtivo,
                        compendioDeadlandsAtivo = compendioDeadlandsAtivo,
                        compendioCrystalHeartAtivo = compendioCrystalHeartAtivo,
                        compendioArteDaGuerraAtivo = compendioArteDaGuerraAtivo,
                        compendioCidadeSolVaporAtivo = compendioCidadeSolVaporAtivo,
                        compendioWiseguysAtivo = compendioWiseguysAtivo,
                        modoMonstroAtivo = modoMonstroAtivo,
                        usarEspecializacoesDePericia = usarEspecializacoesDePericia,
                        grandesResponsabilidades = grandesResponsabilidades,
                        regraMultiplosIdiomas = regraMultiplosIdiomas
                    )
                    viewModel.prepararNomeInicial(context)
                    state.heroisSemArmadura = heroisSemArmadura
                    state.nasceUmHeroi = nasceUmHeroi
                    state.usarSemPontosDePoder = usarSemPontosDePoder
                    state.grandesResponsabilidades = grandesResponsabilidades
                    showClearDialog = false
                    onShowMessage("Ficha limpa.")
                }) {
                    Text("Limpar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showMeioElfoDialog && pendingMeioElfoKey != null) {
        AlertDialog(
            onDismissRequest = {
                pendingMeioElfoKey = null
                showMeioElfoDialog = false
            },
            title = { Text("Meio-Elfo: escolha a herança") },
            text = {
                Text(
                    "Defina como a herança meio-élfica se manifesta:\n\n" +
                            "• Herança Élfica: começa com Agilidade em d6.\n" +
                            "• Herança Humana: ganha +1 Ponto de Vantagem na criação."
                )
            },
            confirmButton = {
                // Herança Élfica (Agilidade d6)
                TextButton(
                    onClick = {
                        val key = pendingMeioElfoKey ?: return@TextButton

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
                        pendingMeioElfoKey = null
                        showMeioElfoDialog = false
                    }
                ) {
                    Text("Herança Élfica (Agilidade d6)")
                }
            },
            dismissButton = {
                // Herança Humana (+1 PV)
                TextButton(
                    onClick = {
                        val key = pendingMeioElfoKey ?: return@TextButton
                        val hadMeioElfoAgil = state.meioElfoAgil
                        state.meioElfoAgil = false

                        // Aplica a ancestralidade Meio-Elfo
                        state.aplicarAncestralidade(
                            key,
                            viewModel.feedbackMessages as MutableList<String>
                        )

                        // Dá 1 ponto de vantagem extra
                        state.pontosVantagem += 1
                        if (hadMeioElfoAgil) {
                            val agilityKey = "AGILIDADE"
                            val agiState = state.valoresAtributos[agilityKey]
                            val agiStack = state.paCostStackPorAtributo[agilityKey]
                            if (agiState != null && agiStack?.isEmpty() == true && agiState.intValue == 6) {
                                agiState.intValue = 4
                            }
                        }

                        pendingMeioElfoKey = null
                        showMeioElfoDialog = false
                    }
                ) {
                    Text("Herança Humana (+1 PV)")
                }
            }
        )
    }

    if (showAllocDialog) {
        ProgressosDialog(
            state = state,
            slotIndex = currentSlotIndex,
            onDismiss = { showAllocDialog = false },
            onStartSkillAdvancement = { slotIndex, stage ->
                viewModel.startSkillAdvancement(slotIndex, stage)
            },
            onStartAdvantageAdvancement = { slotIndex, est ->
                viewModel.startAdvantageAdvancement(slotIndex, est)
            },
            onStartAttributeAdvancement = { slotIndex, stage, consumeReservation ->
                viewModel.startAttributeAdvancement(slotIndex, stage, consumeReservation)
            },
            onReserveLegendaryAttribute = { slotIndex, stage ->
                viewModel.reserveLegendaryAttribute(slotIndex, stage)
            }
        )
    }
}

@Composable
private fun GlobalActionButtons(
    state: CriadorState,
    onClearRequested: () -> Unit,
    onShowMessage: (String) -> Unit,
    onRequestProgression: () -> Unit
) {
    val canFinalize = !state.modoProgressaoAtivo && state.creationComplete()

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Button(
            onClick = onClearRequested,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Delete, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Reiniciar personagem")
        }

        Button(
            onClick = {
                if (!canFinalize) {
                    onShowMessage("Finalize a criação antes de iniciar a progressão.")
                    return@Button
                }

                onRequestProgression()
            },
            enabled = canFinalize,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.ArrowForward, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Finalizar criação")
        }
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
    onSelectSection: (MainSection) -> Unit
) {
    val tabs = remember(sections) { sections.map { SectionTab(it, it.tabLabel()) } }
    if (tabs.isEmpty()) {
        return
    }
    val selectedIndex = tabs.indexOfFirst { it.section == selectedSection }.coerceAtLeast(0)

    ScrollableTabRow(
        selectedTabIndex = selectedIndex,
        edgePadding = 0.dp,
        indicator = { tabPositions ->
            TabRowDefaults.PrimaryIndicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIndex])
            )
        }
    ) {
        tabs.forEach { tab ->
            Tab(
                selected = tab.section == selectedSection,
                onClick = { onSelectSection(tab.section) },
                text = { Text(tab.label) }
            )
        }
    }
}

private fun MainSection.tabLabel(): String = when (this) {
    MainSection.ANCESTRALIDADES -> "Ancestr."
    MainSection.TROPOS -> "Tropos"
    MainSection.COMPLICACOES -> "Complic."
    MainSection.ATRIBUTOS -> "Atributos"
    MainSection.PERICIAS -> "Perícias"
    MainSection.VANTAGENS -> "Vantagens"
    MainSection.EQUIPAMENTOS -> "Equip."
    MainSection.RESUMO -> "Resumo"
    MainSection.PODERES -> "Poderes"
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
        if (state.modoProgressaoAtivo) {
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
    val sections = mutableListOf(MainSection.RESUMO)
    if (state.modoProgressaoAtivo) {
        if (state.mostrandoAtributosProgresso) {
            sections += MainSection.ATRIBUTOS
        }
        if (state.mostrandoPericiasProgresso) {
            sections += MainSection.PERICIAS
        }
        if (state.mostrandoVantagensProgresso) {
            sections += MainSection.VANTAGENS
        }
        sections += MainSection.EQUIPAMENTOS
        sections += MainSection.XP
        return sections
    }

    sections += MainSection.ANCESTRALIDADES
    if (state.compendioArteDaGuerraAtivo) {
        sections += MainSection.TROPOS
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

    val hasArcano = state.vantagensSelecionadas.any {
        it.nome.keyify().startsWith("ANTECEDENTE ARCANO")
    } && !state.celestialAAMilagresDesabilitado
    val mostraPoderesArcanos = hasArcano && !state.compendioCrystalHeartAtivo
    if (mostraPoderesArcanos || state.modoSupers) {
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
                    onUserFeedback = onUserFeedback
                )
            }

            if (state.mostrandoPoderesProgresso || state.arcanoCompraPendente()) {
                Spacer(Modifier.height(8.dp))
                PoderesSection(state = state)
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
            SectionCard(
                title    = "Perícias",
                icon     = Icons.Default.School,
                showHeader = false
            ) {
                PericiasContent(
                    state = state,
                    feedbackMessages = viewModel.feedbackMessages as MutableList<String>,
                    onUserFeedback = onUserFeedback
                )
            }

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
                AtributosContent(state = state, onUserFeedback = onUserFeedback)
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
        MainSection.EQUIPAMENTOS -> EquipamentoSection(
            state = state,
            equipamentoCategorias = equipamentoCategorias,
            superequipCategorias = superequipCategorias,
            onUserFeedback = onUserFeedback
        )
        MainSection.XP -> XpSection(
            state = state,
            onUseProgress = onUseProgress,
            onUndo = {
                viewModel.revertLastAdvancement()
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
            supersLocked = creationLocked,
            ancestralidadeEmFoco = state.ancestralidadeEmFoco,
            onSelectAncestralidade = onSelectAncestralidade,
            onUserFeedback = onUserFeedback
        )
        MainSection.TROPOS -> TroposSection(
            state = state,
            onUserFeedback = onUserFeedback
        )
        MainSection.MONSTRO -> TipoMonstroSection(
            state = state,
            onUserFeedback = onUserFeedback
        )
        MainSection.COMPLICACOES -> ComplicacoesSection(
            state = state,
            feedbackMessages = viewModel.feedbackMessages as MutableList<String>,
            onUserFeedback = onUserFeedback
        )
        MainSection.ATRIBUTOS -> SectionCard(
            title    = "Atributos",
            icon     = Icons.Default.FitnessCenter,
            showHeader = false
        ) {
            AtributosContent(state, onUserFeedback)
        }
        MainSection.PERICIAS -> SectionCard(
            title    = "Perícias",
            icon     = Icons.Default.School,
            showHeader = false
        ) {
            PericiasContent(
                state = state,
                feedbackMessages = viewModel.feedbackMessages as MutableList<String>,
                onUserFeedback = onUserFeedback
            )
        }
        MainSection.VANTAGENS -> SectionCard(
            title    = "Vantagens",
            icon     = Icons.Default.Star,
            showHeader = false
        ) {
            VantagensContent(
                state = state,
                multiplosAAHabilitados = state.permiteMultiAntecedenteArcano,
                viewModel = viewModel,
                onUserFeedback = onUserFeedback
            )
        }
        MainSection.CRYSTAL_HEART -> CrystalHeartSection(
            state = state,
            viewModel = viewModel
        )
        MainSection.PODERES -> {
            if (!state.compendioCrystalHeartAtivo) {
                PoderesSection(state = state)
                Spacer(Modifier.height(8.dp))
            }
            SuperPoderesSection(
                state = state,
                listaSuperPoderes = listaSuperPoderes
            )
        }
        MainSection.EQUIPAMENTOS -> EquipamentoSection(
            state = state,
            equipamentoCategorias = equipamentoCategorias,
            superequipCategorias = superequipCategorias,
            onUserFeedback = onUserFeedback
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
    val portraitLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        viewModel.atualizarRetrato(context, uri)
    }
    val portraitFile = remember(state.portraitFileName, context) {
        state.portraitFileName?.let { File(context.filesDir, "portraits/$it") }
    }
    val portraitUri = portraitFile?.takeIf { it.exists() }?.let(Uri::fromFile)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        SummaryContent(state)
        Spacer(Modifier.height(12.dp))
        CharacterPortraitCard(
            imageUri = portraitUri,
            onSelectImage = { portraitLauncher.launch("image/*") }
        )
        Spacer(Modifier.height(12.dp))
        if (!state.modoProgressaoAtivo) {
            GlobalActionButtons(
                state = state,
                onClearRequested = onClearRequested,
                onShowMessage = onShowMessage,
                onRequestProgression = onRequestProgression
            )
        }
    }
}

@Composable
private fun PoderesSection(
    state: CriadorState
) {
    val temArcano = state.vantagensSelecionadas.any {
        it.nome.keyify().startsWith("ANTECEDENTE ARCANO")
    }
    if (temArcano && !state.celestialAAMilagresDesabilitado) {
        HorizontalDivider(thickness = 1.dp)
        SectionCard(
            title = "Poderes",
            icon = Icons.Default.FlashOn,
            showHeader = false
        ) {
            PoderesSection(
                state = state
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
private fun SuperPoderesSection(
    state: CriadorState,
    listaSuperPoderes: List<SuperPoder>
) {
    if (state.modoSupers) {
        SuperPoderesContent(
            state = state,
            listaSuperPoderes = listaSuperPoderes
        )
    }
}

@Composable
private fun EquipamentoSection(
    state: CriadorState,
    equipamentoCategorias: List<EquipamentoCategoria>,
    superequipCategorias: List<EquipamentoCategoria>,
    onUserFeedback: () -> Unit
) {
    val hasMusculoso = state.vantagensSelecionadas.any { it.nome.keyify() == "MUSCULOSO" }
    val hasSoldado = state.vantagensSelecionadas.any { it.nome.keyify() == "SOLDADO" }
    val isPersonagemRobotico = state.isPersonagemRobotico()
    val tensaoLimite = if (isPersonagemRobotico) {
        state.limiteModsRoboticos()
    } else {
        state.valoresAtributos["VIGOR"]?.intValue ?: 4
    }

    EquipamentoSection(
        dinheiro = state.dinheiro,
        usaRiqueza = state.usaRiqueza,
        dadoRiqueza = state.dadoRiqueza,
        pcTotal = state.pontosComplicacao,
        pcLivres = (state.pontosComplicacao - state.pontosComplicacaoGastos).coerceAtLeast(0),
        recursosPcUsados = state.cpRecursosStack.size,
        emProgresso = state.emProgresso,
        modoProgressaoAtivo = state.modoProgressaoAtivo,
        onUsarPontosBonusEmRecursos = {
            if (state.usaRiqueza) return@EquipamentoSection
            val pcLivresLocal =
                (state.pontosComplicacao - state.pontosComplicacaoGastos).coerceAtLeast(0)
            if (pcLivresLocal > 0 && state.cpRecursosStack.isEmpty()) {
                state.cpRecursosStack.add(Unit)
                state.pontosComplicacaoGastos += 1
                state.dinheiro += 500
            }
        },
        onDesfazerPontosBonusEmRecursos = {
            if (state.usaRiqueza) return@EquipamentoSection
            if (state.cpRecursosStack.isNotEmpty() && state.dinheiro >= 500) {
                state.cpRecursosStack.removeAt(state.cpRecursosStack.lastIndex)
                state.pontosComplicacaoGastos =
                    (state.pontosComplicacaoGastos - 1).coerceAtLeast(0)
                state.dinheiro -= 500
            }
        },
        onEquipamentoDoubleClick = { equipamento ->
            val custo = (equipamento.custo as? JsonPrimitive)
                ?.content?.toIntOrNull() ?: 0
            if (state.usaRiqueza || custo <= state.dinheiro) {
                state.equipamentosComprados.add(equipamento)
                if (!state.usaRiqueza) {
                    state.dinheiro -= custo
                }
            }
        },
        equipamentosComprados = state.equipamentosComprados,
        onRemoveEquipamentoClick = { equipamento ->
            val custo = (equipamento.custo as? JsonPrimitive)
                ?.content?.toIntOrNull() ?: 0
            state.equipamentosComprados.remove(equipamento)
            if (!state.usaRiqueza) {
                state.dinheiro += custo
            }
        },
        categorias = equipamentoCategorias,
        superequipCategorias =
            if (state.modoSupers) superequipCategorias else emptyList(),
        tensaoTotal = state.totalTensaoEquipamentos(),
        tensaoLimite = tensaoLimite,
        isPersonagemRobotico = isPersonagemRobotico,
        forcaRaw = state.valoresAtributos["FORCA"]?.intValue ?: 4,
        hasMusculoso = hasMusculoso,
        hasSoldado = hasSoldado,
        soldadoCargaAtivo = state.soldadoCargaAtivo,
        onEditarDinheiro = { novoValor -> state.dinheiro = novoValor },
        onToggleSoldadoCarga = {
            if (hasSoldado) {
                state.soldadoCargaAtivo = !state.soldadoCargaAtivo
            }
        },
        compendioFantasiaAtivo = state.compendioFantasiaAtivo,
        compendioHorrorAtivo = state.compendioHorrorAtivo,
        compendioSciFiAtivo = state.compendioSciFiAtivo,
        compendioBuscatrilhaAtivo = state.compendioBuscatrilhaAtivo,
        compendioDeadlandsAtivo = state.compendioDeadlandsAtivo,
        compendioArteDaGuerraAtivo = state.compendioArteDaGuerraAtivo,
        compendioCidadeSolVaporAtivo = state.compendioCidadeSolVaporAtivo,
        compendioWiseguysAtivo = state.compendioWiseguysAtivo,
        compendioCrystalHeartAtivo = state.compendioCrystalHeartAtivo,
        modoOficialAtivo = state.modoOficialAtivo,
        onUserFeedback = onUserFeedback
    )
}
