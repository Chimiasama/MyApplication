package com.example.swadebuilder.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.SuperPoder
import com.example.swadebuilder.model.CharacterViewModel
import com.example.swadebuilder.model.CriadorViewModel
import com.example.swadebuilder.model.EquipamentoCategoria
import com.example.swadebuilder.model.ScreenTab
import com.example.swadebuilder.ui.MainSection
import com.example.swadebuilder.ui.components.AttributeGrid
import com.example.swadebuilder.ui.components.BennieComponent
import com.example.swadebuilder.ui.components.InfoPanel
import com.example.swadebuilder.ui.components.SavageNavigationMenu
import com.example.swadebuilder.ui.sections.AncestralidadesSection
import com.example.swadebuilder.ui.sections.AtributosContent
import com.example.swadebuilder.ui.sections.ComplicacoesSection
import com.example.swadebuilder.ui.sections.CrystalHeartSection
import com.example.swadebuilder.ui.sections.EquipamentoSection
import com.example.swadebuilder.ui.sections.PericiasContent
import com.example.swadebuilder.ui.sections.PoderesSection
import com.example.swadebuilder.ui.sections.SuperPoderesContent
import com.example.swadebuilder.ui.sections.TipoMonstroSection
import com.example.swadebuilder.ui.sections.TroposSection
import com.example.swadebuilder.ui.sections.VantagensContent
import com.example.swadebuilder.ui.sections.XpSection
import com.example.swadebuilder.ui.theme.SwadeDesignSystem
import com.example.swadebuilder.ui.theme.SwadeTheme
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.semAcentos
import kotlinx.serialization.json.JsonPrimitive

@Composable
fun MainCharacterScreen(
    state: CriadorState,
    viewModel: CriadorViewModel,
    equipamentoCategorias: List<EquipamentoCategoria>,
    superequipCategorias: List<EquipamentoCategoria>,
    listaSuperPoderes: List<SuperPoder>,
    onUserFeedback: () -> Unit,
    navViewModel: CharacterViewModel = viewModel()
) {
    val selectedTab by navViewModel.selectedTab.collectAsState()

    // Map CriadorState to temporary CharacterStats for the header/sidebar
    val stats = com.example.swadebuilder.model.CharacterStats(
        agility = state.valoresAtributos["AGILIDADE"]?.intValue ?: 4,
        smarts = state.valoresAtributos["ASTUCIA"]?.intValue ?: 4,
        spirit = state.valoresAtributos["ESPIRITO"]?.intValue ?: 4,
        strength = state.valoresAtributos["FORCA"]?.intValue ?: 4,
        vigor = state.valoresAtributos["VIGOR"]?.intValue ?: 4,
        pace = state.valorMovimentacao(),
        parry = state.valorAparar(),
        toughness = state.valorResistenciaFinal(),
        bennies = 3 // Standard default
    )

    SwadeTheme {
        MainCharacterScreenLayout(
            sidebarContent = {
                Text(
                    text = "SAVAGE WORLDS",
                    style = SwadeDesignSystem.typography.header,
                    color = SwadeDesignSystem.colors.onBackground
                )
                Spacer(modifier = Modifier.height(16.dp))
                SavageNavigationMenu(
                    tabs = listOf(ScreenTab.Character, ScreenTab.Skills, ScreenTab.Edges, ScreenTab.Gear, ScreenTab.Notes),
                    selectedTab = selectedTab,
                    onTabSelected = { navViewModel.selectTab(it) }
                )
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = SwadeDesignSystem.colors.border)
                Spacer(modifier = Modifier.height(16.dp))

                // Read-only grid in sidebar (Landscape)
                AttributeGrid(stats = stats, isLandscape = true)
            },
            centerContent = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(Color.DarkGray)
                ) {
                    Text("Character Portrait", modifier = Modifier.align(Alignment.Center), color = Color.White)
                }
                Spacer(modifier = Modifier.height(16.dp))
                BennieComponent(count = stats.bennies)
            },
            rightContent = {
                InfoPanel(stats = stats)
                Spacer(modifier = Modifier.height(16.dp))

                // The main interactive content area (scroll handled inside)
                TabContentSlot(
                    selectedTab = selectedTab,
                    state = state,
                    viewModel = viewModel,
                    equipamentoCategorias = equipamentoCategorias,
                    superequipCategorias = superequipCategorias,
                    listaSuperPoderes = listaSuperPoderes,
                    onUserFeedback = onUserFeedback
                )
            },
            portraitContent = {
                // Header + Bennies
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SAVAGE WORLDS",
                        style = SwadeDesignSystem.typography.header,
                        color = SwadeDesignSystem.colors.onBackground
                    )
                    BennieComponent(count = stats.bennies)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // NOTE: Removed verticalScroll() from here to avoid crash with nested LazyColumns
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Header elements are now part of the top of the column
                    // Since the parent doesn't scroll, these will remain fixed at the top
                    // If we want them to scroll away, we'd need to use a single LazyColumn for the whole screen
                    // For now, fixed header is a safe/good UX for "Attribute Grid"
                    AttributeGrid(stats = stats, isLandscape = false)
                    Spacer(modifier = Modifier.height(16.dp))
                    InfoPanel(stats = stats)
                    Spacer(modifier = Modifier.height(16.dp))

                    // The main interactive content area will take remaining space and handle its own scroll
                    TabContentSlot(
                        selectedTab = selectedTab,
                        state = state,
                        viewModel = viewModel,
                        equipamentoCategorias = equipamentoCategorias,
                        superequipCategorias = superequipCategorias,
                        listaSuperPoderes = listaSuperPoderes,
                        onUserFeedback = onUserFeedback
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                SavageNavigationMenu(
                    tabs = listOf(ScreenTab.Character, ScreenTab.Skills, ScreenTab.Edges, ScreenTab.Gear, ScreenTab.Notes),
                    selectedTab = selectedTab,
                    onTabSelected = { navViewModel.selectTab(it) }
                )
            }
        )
    }
}

@Composable
fun MainCharacterScreenLayout(
    sidebarContent: @Composable ColumnScope.() -> Unit,
    centerContent: @Composable ColumnScope.() -> Unit,
    rightContent: @Composable ColumnScope.() -> Unit,
    portraitContent: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        containerColor = SwadeDesignSystem.colors.background
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isLandscape = maxWidth > 600.dp

            if (isLandscape) {
                // LANDSCAPE LAYOUT (3 Columns)
                Row(modifier = Modifier.fillMaxSize()) {
                    // Col 1: Sidebar
                    Column(
                        modifier = Modifier
                            .width(280.dp)
                            .fillMaxHeight()
                            .padding(SwadeDesignSystem.dimensions.paddingMedium)
                            .verticalScroll(rememberScrollState())
                    ) {
                        sidebarContent()
                    }

                    // Col 2: Center
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(SwadeDesignSystem.dimensions.paddingMedium),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        centerContent()
                    }

                    // Col 3: Right
                    Column(
                        modifier = Modifier
                            .weight(1.5f)
                            .fillMaxHeight()
                            .padding(SwadeDesignSystem.dimensions.paddingMedium)
                    ) {
                        rightContent()
                    }
                }
            } else {
                // PORTRAIT LAYOUT
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(SwadeDesignSystem.dimensions.paddingMedium)
                ) {
                    portraitContent()
                }
            }
        }
    }
}

@Composable
fun TabContentSlot(
    selectedTab: ScreenTab,
    state: CriadorState,
    viewModel: CriadorViewModel,
    equipamentoCategorias: List<EquipamentoCategoria>,
    superequipCategorias: List<EquipamentoCategoria>,
    listaSuperPoderes: List<SuperPoder>,
    onUserFeedback: () -> Unit
) {
    // We recreate the state-dependent content here
    AnimatedContent(targetState = selectedTab, label = "TabSwitch") { tab ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SwadeDesignSystem.colors.surface.copy(alpha = 0.2f), SwadeDesignSystem.shapes.card)
                .padding(16.dp)
        ) {
            when (tab) {
                ScreenTab.Character -> {
                    // Content that uses normal Columns -> Needs verticalScroll
                    Column(Modifier.verticalScroll(rememberScrollState())) {
                        AncestralidadesSection(
                            state = state,
                            currentAncestralidade = state.ancestralidade,
                            expanded = true,
                            onToggle = {}, // Always expanded in this view
                            supersLocked = state.baseCreationComplete() && state.modoProgressaoAtivo, // Fixed: logic for freeze
                            ancestralidadeEmFoco = state.ancestralidadeEmFoco,
                            onSelectAncestralidade = { nome ->
                                // Logic from UnifiedScreen
                                val key = nome.uppercase().semAcentos()
                                if (key != state.ancestralidade) {
                                    state.aplicarAncestralidade(
                                        key,
                                        viewModel.feedbackMessages as MutableList<String>
                                    )
                                }
                            },
                            onUserFeedback = onUserFeedback
                        )
                        HorizontalDivider(Modifier.padding(vertical = 8.dp))
                        if (state.compendioArteDaGuerraAtivo) {
                            TroposSection(state = state, expanded = true, onToggle = {}, onUserFeedback = onUserFeedback)
                            HorizontalDivider(Modifier.padding(vertical = 8.dp))
                        }
                        if (state.modoMonstroAtivo) {
                            TipoMonstroSection(state = state, expanded = true, onToggle = {}, onUserFeedback = onUserFeedback)
                            HorizontalDivider(Modifier.padding(vertical = 8.dp))
                        }

                        ComplicacoesSection(
                            state = state,
                            expanded = true,
                            onToggle = {},
                            feedbackMessages = viewModel.feedbackMessages as MutableList<String>,
                            onUserFeedback = onUserFeedback
                        )
                        HorizontalDivider(Modifier.padding(vertical = 8.dp))

                        // Interactive Attributes for Editing
                        Text("Edit Attributes", style = SwadeDesignSystem.typography.subHeader)
                        AtributosContent(state, onUserFeedback)

                        // Progression Trigger (Bottom of Character Tab)
                        if (state.creationComplete() && !state.modoProgressaoAtivo) {
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    viewModel.ensureDefaultSpecializations()
                                    state.modoProgressaoAtivo = true
                                    state.progresso = 4
                                    state.frozenAdvantageCount = state.vantagensSelecionadas.size
                                    state.snapshotFrozenSkillIncrements()
                                    state.recomputeAvailableProgress()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Iniciar Progressão")
                            }
                        }
                    }
                }
                ScreenTab.Skills -> {
                    // PericiasSection uses LazyColumn -> NO verticalScroll on container
                    // Note: PericiasContent already contains a LazyColumn.
                    // We must ensure it's not wrapped in a scrollable Column here.
                    // Just wrapping in a non-scrollable Column or Box is fine.
                    Column {
                        PericiasContent(
                            state = state,
                            feedbackMessages = viewModel.feedbackMessages as MutableList<String>,
                            onUserFeedback = onUserFeedback
                        )
                    }
                }
                ScreenTab.Edges -> {
                    // VantagensSection typically uses LazyColumn or Collapsibles.
                    // Let's assume mixed content. If it has LazyColumn, we need to be careful.
                    // UnifiedScreen used Column(verticalScroll) for the whole screen.
                    // VantagensSection inside UnifiedScreen used LazyColumn?
                    // Let's check VantagensSection structure if possible. Assuming it's safe to scroll for now
                    // OR if it crashes we fix.
                    // Actually, VantagensContent usually has a list.
                    // Safest approach: Use verticalScroll. If VantagensContent has a nested LazyColumn, it will crash.
                    // Given the user history, let's assume standard composables.
                    Column(Modifier.verticalScroll(rememberScrollState())) {
                        VantagensContent(
                            state = state,
                            multiplosAAHabilitados = state.permiteMultiAntecedenteArcano,
                            viewModel = viewModel,
                            onUserFeedback = onUserFeedback
                        )
                        HorizontalDivider(Modifier.padding(vertical = 8.dp))

                        if (state.compendioCrystalHeartAtivo) {
                            CrystalHeartSection(
                                state = state,
                                viewModel = viewModel,
                                expanded = true,
                                onToggle = {}
                            )
                        } else {
                            PoderesSection(state = state, onUserFeedback = onUserFeedback)
                        }

                        if (state.modoSupers) {
                             HorizontalDivider(Modifier.padding(vertical = 8.dp))
                             SuperPoderesContent(
                                state = state,
                                listaSuperPoderes = listaSuperPoderes,
                                expanded = true,
                                onToggle = {}
                             )
                        }
                    }
                }
                ScreenTab.Gear -> {
                    // EquipamentoSection usually has lists.
                    Column(Modifier.verticalScroll(rememberScrollState())) {
                         EquipamentoSectionWrapper(
                            state = state,
                            expanded = true,
                            onToggle = {},
                            equipamentoCategorias = equipamentoCategorias,
                            superequipCategorias = superequipCategorias,
                            onUserFeedback = onUserFeedback
                        )
                    }
                }
                ScreenTab.Notes -> {
                    // XP Section fits here or in Character? Putting here for now as "Meta/History"
                    Column(Modifier.verticalScroll(rememberScrollState())) {
                        XpSection(
                            state = state,
                            expanded = true,
                            onToggle = {},
                            onUseProgress = { /* Logic needed for progression dialog */ },
                            onUndo = { viewModel.revertLastAdvancement() },
                            onUserFeedback = onUserFeedback
                        )
                    }
                }
            }
        }
    }
}

// Wrapper to handle calculated properties for Equipment Section
@Composable
private fun EquipamentoSectionWrapper(
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

@Composable
private fun PoderesSection(
    state: CriadorState,
    onUserFeedback: () -> Unit
) {
    val temArcano = state.vantagensSelecionadas.any {
        it.nome.keyify().startsWith("ANTECEDENTE ARCANO")
    }
    if (temArcano && !state.celestialAAMilagresDesabilitado) {
        PoderesSection(
            state = state
        )
    }
}

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
