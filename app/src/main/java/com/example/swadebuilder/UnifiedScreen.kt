package com.example.swadebuilder

import android.content.res.Configuration
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.model.CriadorViewModel
import com.example.swadebuilder.model.EquipamentoCategoria
import com.example.swadebuilder.ui.MainSection
import com.example.swadebuilder.ui.components.SectionCard
import com.example.swadebuilder.ui.dialogs.ProgressosDialog
import com.example.swadebuilder.ui.sections.AncestralidadesSection
import com.example.swadebuilder.ui.sections.AtributosContent
import com.example.swadebuilder.ui.sections.BasicCharacterInfo
import com.example.swadebuilder.ui.sections.SummaryCompact
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
import kotlinx.serialization.json.JsonPrimitive

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
        onImportRequested = {},
        onShowMessage = {},
        onUserFeedback = {}
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
    onImportRequested: () -> Unit,
    onShowMessage: (String) -> Unit,
    onUserFeedback: () -> Unit
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

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val availableSections = availableSectionsFor(state)
    var selectedSection by rememberSaveable { mutableStateOf(MainSection.RESUMO) }

    val forcedSection = when {
        state.mostrandoVantagensProgresso -> MainSection.VANTAGENS
        state.mostrandoPericiasProgresso -> MainSection.PERICIAS
        state.mostrandoAtributosProgresso -> MainSection.ATRIBUTOS
        else -> null
    }
    val activeSection = forcedSection ?: selectedSection

    LaunchedEffect(availableSections, forcedSection) {
        if (forcedSection != null) {
            selectedSection = forcedSection
        } else if (availableSections.isNotEmpty() && selectedSection !in availableSections) {
            selectedSection = availableSections.first()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (state.modoProgressaoAtivo) {
            Text(
                text = "MODO DE PROGRESSÃO",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
        }

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isLandscape) {
                Column(
                    modifier = Modifier
                        .weight(0.28f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                ) {
                    SectionMenu(
                        sections = availableSections,
                        selectedSection = activeSection,
                        enabled = forcedSection == null,
                        onSelectSection = {
                            onUserFeedback()
                            selectedSection = it
                        }
                    )
                    Spacer(Modifier.height(12.dp))
                    BasicCharacterInfo(state = state)
                }

                Column(
                    modifier = Modifier
                        .weight(0.44f)
                        .fillMaxHeight()
                ) {
                    CharacterPortraitCard()
                    Spacer(Modifier.height(12.dp))
                    GlobalActionButtons(
                        state = state,
                        viewModel = viewModel,
                        onImportRequested = {
                            onUserFeedback()
                            onImportRequested()
                        },
                        onClearRequested = {
                            onUserFeedback()
                            showClearDialog = true
                        },
                        onShowMessage = onShowMessage
                    )
                    Spacer(Modifier.height(12.dp))
                    SectionDetailPane(
                        modifier = Modifier.weight(1f),
                        state = state,
                        viewModel = viewModel,
                        selectedSection = activeSection,
                        listaSuperPoderes = listaSuperPoderes,
                        equipamentoCategorias = equipamentoCategorias,
                        superequipCategorias = superequipCategorias,
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

                Column(
                    modifier = Modifier
                        .weight(0.28f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                ) {
                    SummaryCompact(state = state)
                }
            } else {
                Column(
                    modifier = Modifier
                        .weight(0.4f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                ) {
                    SectionMenu(
                        sections = availableSections,
                        selectedSection = activeSection,
                        enabled = forcedSection == null,
                        onSelectSection = {
                            onUserFeedback()
                            selectedSection = it
                        }
                    )
                    Spacer(Modifier.height(12.dp))
                    BasicCharacterInfo(state = state)
                    Spacer(Modifier.height(12.dp))
                    CharacterPortraitCard()
                    Spacer(Modifier.height(12.dp))
                    GlobalActionButtons(
                        state = state,
                        viewModel = viewModel,
                        onImportRequested = {
                            onUserFeedback()
                            onImportRequested()
                        },
                        onClearRequested = {
                            onUserFeedback()
                            showClearDialog = true
                        },
                        onShowMessage = onShowMessage
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(0.6f)
                        .fillMaxHeight()
                ) {
                    SummaryCompact(state = state)
                    Spacer(Modifier.height(12.dp))
                    SectionDetailPane(
                        modifier = Modifier.weight(1f),
                        state = state,
                        viewModel = viewModel,
                        selectedSection = activeSection,
                        listaSuperPoderes = listaSuperPoderes,
                        equipamentoCategorias = equipamentoCategorias,
                        superequipCategorias = superequipCategorias,
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

                        // Aplica a ancestralidade Meio-Elfo
                        state.aplicarAncestralidade(
                            key,
                            viewModel.feedbackMessages as MutableList<String>
                        )

                        // Dá 1 ponto de vantagem extra
                        state.pontosVantagem += 1

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
private fun SectionMenu(
    sections: List<MainSection>,
    selectedSection: MainSection,
    enabled: Boolean,
    onSelectSection: (MainSection) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                text = "Seções",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            sections.forEach { section ->
                val isSelected = section == selectedSection
                TextButton(
                    onClick = { onSelectSection(section) },
                    enabled = enabled,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isSelected) {
                            "● ${section.label()}"
                        } else {
                            section.label()
                        },
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun GlobalActionButtons(
    state: CriadorState,
    viewModel: CriadorViewModel,
    onImportRequested: () -> Unit,
    onClearRequested: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val canFinalize = !state.modoProgressaoAtivo && state.creationComplete()

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onImportRequested,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.FolderOpen, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Importar")
            }
            Button(
                onClick = onClearRequested,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Limpar")
            }
        }

        Button(
            onClick = {
                if (!canFinalize) {
                    onShowMessage("Finalize a criação antes de iniciar a progressão.")
                    return@Button
                }

                viewModel.ensureDefaultSpecializations()
                state.modoProgressaoAtivo = true
                state.progresso = 4
                state.frozenAdvantageCount = state.vantagensSelecionadas.size
                state.snapshotFrozenSkillIncrements()
                state.recomputeAvailableProgress()
            },
            enabled = canFinalize,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.ArrowForward, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Finalizar")
        }
    }
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
    onSelectAncestralidade: (String) -> Unit,
    onUseProgress: (Int) -> Unit,
    onUserFeedback: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        if (state.modoProgressaoAtivo) {
            ProgressionDetailContent(
                state = state,
                viewModel = viewModel,
                selectedSection = selectedSection,
                equipamentoCategorias = equipamentoCategorias,
                superequipCategorias = superequipCategorias,
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

private fun MainSection.label(): String = when (this) {
    MainSection.ANCESTRALIDADES -> "Ancestralidades"
    MainSection.TROPOS -> "Tropos"
    MainSection.COMPLICACOES -> "Complicações"
    MainSection.ATRIBUTOS -> "Atributos"
    MainSection.PERICIAS -> "Perícias"
    MainSection.VANTAGENS -> "Vantagens"
    MainSection.EQUIPAMENTOS -> "Equipamentos"
    MainSection.RESUMO -> "Resumo"
    MainSection.PODERES -> "Poderes"
    MainSection.XP -> "XP"
    MainSection.MONSTRO -> "Monstro"
    MainSection.CRYSTAL_HEART -> "Crystal Heart"
}

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
private fun ProgressionDetailContent(
    state: CriadorState,
    viewModel: CriadorViewModel,
    selectedSection: MainSection,
    equipamentoCategorias: List<EquipamentoCategoria>,
    superequipCategorias: List<EquipamentoCategoria>,
    onUseProgress: (Int) -> Unit,
    onUserFeedback: () -> Unit
) {
    when (selectedSection) {
        MainSection.VANTAGENS -> {
            SectionCard(
                title    = "Vantagens",
                expanded = state.sectionsExpanded[MainSection.VANTAGENS] ?: false,
                onToggle = { state.toggleSection(MainSection.VANTAGENS) },
                icon     = Icons.Default.Star,
                onToggleFeedback = onUserFeedback
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
                PoderesSection(state = state, onUserFeedback = onUserFeedback)
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
                expanded = state.sectionsExpanded[MainSection.PERICIAS] ?: false,
                onToggle = { state.toggleSection(MainSection.PERICIAS) },
                icon     = Icons.Default.School,
                onToggleFeedback = onUserFeedback
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
                expanded = state.sectionsExpanded[MainSection.ATRIBUTOS] ?: false,
                onToggle = { state.toggleSection(MainSection.ATRIBUTOS) },
                icon     = Icons.Default.FitnessCenter,
                onToggleFeedback = onUserFeedback
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
            expanded = state.sectionsExpanded[MainSection.EQUIPAMENTOS] ?: false,
            onToggle = { state.toggleSection(MainSection.EQUIPAMENTOS) },
            equipamentoCategorias = equipamentoCategorias,
            superequipCategorias = superequipCategorias,
            onUserFeedback = onUserFeedback
        )
        MainSection.XP -> XpSection(
            state = state,
            expanded = state.sectionsExpanded[MainSection.XP] ?: false,
            onToggle = { state.toggleSection(MainSection.XP) },
            onUseProgress = onUseProgress,
            onUndo = {
                viewModel.revertLastAdvancement()
            },
            onUserFeedback = onUserFeedback
        )
        else -> ResumoSection(state = state, onUserFeedback = onUserFeedback)
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
    onSelectAncestralidade: (String) -> Unit,
    onUserFeedback: () -> Unit
) {
    val creationLocked = state.criacaoBasicaCongelada

    when (selectedSection) {
        MainSection.RESUMO -> ResumoSection(state = state, onUserFeedback = onUserFeedback)
        MainSection.ANCESTRALIDADES -> AncestralidadesSection(
            state = state,
            currentAncestralidade = state.ancestralidade,
            expanded = state.sectionsExpanded[MainSection.ANCESTRALIDADES] ?: false,
            onToggle = { state.toggleSection(MainSection.ANCESTRALIDADES) },
            supersLocked = creationLocked,
            ancestralidadeEmFoco = state.ancestralidadeEmFoco,
            onSelectAncestralidade = onSelectAncestralidade,
            onUserFeedback = onUserFeedback
        )
        MainSection.TROPOS -> TroposSection(
            state = state,
            expanded = state.sectionsExpanded[MainSection.TROPOS] ?: false,
            onToggle = { state.toggleSection(MainSection.TROPOS) },
            onUserFeedback = onUserFeedback
        )
        MainSection.MONSTRO -> TipoMonstroSection(
            state = state,
            expanded = state.sectionsExpanded[MainSection.MONSTRO] ?: false,
            onToggle = { state.toggleSection(MainSection.MONSTRO) },
            onUserFeedback = onUserFeedback
        )
        MainSection.COMPLICACOES -> ComplicacoesSection(
            state = state,
            expanded = state.sectionsExpanded[MainSection.COMPLICACOES] ?: false,
            onToggle = { state.toggleSection(MainSection.COMPLICACOES) },
            feedbackMessages = viewModel.feedbackMessages as MutableList<String>,
            onUserFeedback = onUserFeedback
        )
        MainSection.ATRIBUTOS -> SectionCard(
            title    = "Atributos",
            expanded = state.sectionsExpanded[MainSection.ATRIBUTOS] ?: false,
            onToggle = { state.toggleSection(MainSection.ATRIBUTOS) },
            icon     = Icons.Default.FitnessCenter,
            onToggleFeedback = onUserFeedback
        ) {
            AtributosContent(state, onUserFeedback)
        }
        MainSection.PERICIAS -> SectionCard(
            title    = "Perícias",
            expanded = state.sectionsExpanded[MainSection.PERICIAS] ?: false,
            onToggle = { state.toggleSection(MainSection.PERICIAS) },
            icon     = Icons.Default.School,
            onToggleFeedback = onUserFeedback
        ) {
            PericiasContent(
                state = state,
                feedbackMessages = viewModel.feedbackMessages as MutableList<String>,
                onUserFeedback = onUserFeedback
            )
        }
        MainSection.VANTAGENS -> SectionCard(
            title    = "Vantagens",
            expanded = state.sectionsExpanded[MainSection.VANTAGENS] ?: false,
            onToggle = { state.toggleSection(MainSection.VANTAGENS) },
            icon     = Icons.Default.Star,
            onToggleFeedback = onUserFeedback
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
            viewModel = viewModel,
            expanded = state.sectionsExpanded[MainSection.CRYSTAL_HEART] ?: false,
            onToggle = { state.toggleSection(MainSection.CRYSTAL_HEART) }
        )
        MainSection.PODERES -> {
            if (!state.compendioCrystalHeartAtivo) {
                PoderesSection(state = state, onUserFeedback = onUserFeedback)
                Spacer(Modifier.height(8.dp))
            }
            SuperPoderesSection(
                state = state,
                listaSuperPoderes = listaSuperPoderes,
                expanded = state.sectionsExpanded[MainSection.PODERES] ?: false,
                onToggle = { state.toggleSection(MainSection.PODERES) }
            )
        }
        MainSection.EQUIPAMENTOS -> EquipamentoSection(
            state = state,
            expanded = state.sectionsExpanded[MainSection.EQUIPAMENTOS] ?: false,
            onToggle = { state.toggleSection(MainSection.EQUIPAMENTOS) },
            equipamentoCategorias = equipamentoCategorias,
            superequipCategorias = superequipCategorias,
            onUserFeedback = onUserFeedback
        )
        else -> ResumoSection(state = state, onUserFeedback = onUserFeedback)
    }
}

@Composable
private fun ResumoSection(
    state: CriadorState,
    onUserFeedback: () -> Unit = {}
) {
    SectionCard(
        title = "Resumo do Personagem",
        expanded = state.sectionsExpanded[MainSection.RESUMO] ?: false,
        onToggle = { state.toggleSection(MainSection.RESUMO) },
        icon = Icons.Default.Description,
        onToggleFeedback = onUserFeedback
    ) {
        SummaryContent(state)
    }
}

@Composable
private fun PoderesSection(
    state: CriadorState,
    onUserFeedback: () -> Unit
) {
    val temArcano = state.vantagensSelecionadas.any {
        it.nome.keyify().startsWith("ANTECEDENTE ARCANO")
    }
    if (temArcano && !state.celestialAAMilagresDesabilitado) {
        HorizontalDivider(thickness = 1.dp)
        SectionCard(
            title = "Poderes",
            expanded = state.sectionsExpanded[MainSection.PODERES] ?: false,
            onToggle = { state.toggleSection(MainSection.PODERES) },
            icon = Icons.Default.FlashOn,
            onToggleFeedback = onUserFeedback
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
    listaSuperPoderes: List<SuperPoder>,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    if (state.modoSupers) {
        SuperPoderesContent(
            state = state,
            listaSuperPoderes = listaSuperPoderes,
            expanded = expanded,
            onToggle = onToggle
        )
    }
}

@Composable
private fun EquipamentoSection(
    state: CriadorState,
    expanded: Boolean,
    onToggle: () -> Unit,
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
        expanded = expanded,
        onToggle = onToggle,
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
