package com.example.swadebuilder

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.swadebuilder.model.CriadorViewModel
import com.example.swadebuilder.ui.MainSection
import com.example.swadebuilder.ui.dialogs.SaveLoadDialog
import com.example.swadebuilder.ui.dialogs.SelectionDialog
import com.example.swadebuilder.ui.dialogs.SettingsDialog
import com.example.swadebuilder.ui.sections.AncestralidadesSection
import com.example.swadebuilder.ui.sections.AtributosSection
import com.example.swadebuilder.ui.sections.ComplicacoesSection
import com.example.swadebuilder.ui.sections.CrystalHeartSection
import com.example.swadebuilder.ui.sections.EquipamentoSection
import com.example.swadebuilder.ui.sections.FaseSupersSection
import com.example.swadebuilder.ui.sections.MonstroTemplateSection
import com.example.swadebuilder.ui.sections.PericiasSection
import com.example.swadebuilder.ui.sections.PoderesSection
import com.example.swadebuilder.ui.sections.RecursosSection
import com.example.swadebuilder.ui.sections.ResumoSection
import com.example.swadebuilder.ui.sections.TecnicasChiSection
import com.example.swadebuilder.ui.sections.TroposSection
import com.example.swadebuilder.ui.sections.VantagensSection
import com.example.swadebuilder.ui.sections.XpSection
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun UnifiedScreen(
    onBack: () -> Unit,
    criadorViewModel: CriadorViewModel = viewModel()
) {
    val state = criadorViewModel.state
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showSaveDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    // Feedback messages
    val messages = criadorViewModel.feedbackMessages
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            val lastMsg = messages.last()
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = lastMsg,
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    // Determine lock state based on current phase and supers mode
    val emProgresso = state.emProgresso
    val modoSupers = state.modoSupers
    val superFaseAtiva = state.faseSupersAtiva

    // UI Locking Logic
    val supersLocked = modoSupers && superFaseAtiva
    val progressLocked = state.criacaoBasicaCongeladaComXp

    // Combined locks for specific sections
    val ancestralidadeLocked = supersLocked || progressLocked
    val atributosLocked = supersLocked || progressLocked
    val periciasLocked = supersLocked || progressLocked
    val complicacoesLocked = supersLocked || progressLocked

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.nomePersonagem.ifBlank { "Novo Personagem" },
                            fontWeight = FontWeight.Bold
                        )
                        if (state.modoProgressaoAtivo) {
                            Text(
                                "Modo de Progressão (${state.estagioAtual().nome})",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = { showSaveDialog = true }) {
                        Icon(Icons.Default.Save, contentDescription = "Salvar")
                    }
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Configurações")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->

        // If progression is active, decide what to show
        // If showing dedicated selection screens (Advantages/Skills/Powers/Attributes), hide others
        val showingProgressionDetail = state.mostrandoVantagensProgresso ||
                state.mostrandoPericiasProgresso ||
                state.mostrandoPoderesProgresso ||
                state.mostrandoAtributosProgresso

        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)) {

            if (state.modoProgressaoAtivo) {
                // Progression Mode Layout
                ProgressionModeContent(
                    state = state,
                    viewModel = criadorViewModel,
                    showingDetail = showingProgressionDetail
                )
            } else {
                // Creation Mode Layout
                CreationModeContent(
                    state = state,
                    viewModel = criadorViewModel,
                    ancestralidadeLocked = ancestralidadeLocked,
                    atributosLocked = atributosLocked,
                    periciasLocked = periciasLocked,
                    complicacoesLocked = complicacoesLocked,
                    emProgresso = emProgresso,
                    supersLocked = supersLocked
                )
            }
        }
    }

    if (showSaveDialog) {
        SaveLoadDialog(
            isSaveMode = true,
            currentName = state.nomePersonagem,
            existingFiles = CharacterStorage.listSaves(context),
            onDismiss = { showSaveDialog = false },
            onConfirm = { name ->
                criadorViewModel.salvarPersonagem(context, name)
                showSaveDialog = false
            },
            onDelete = {} // Not used in save mode
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(
            onDismiss = { showSettingsDialog = false },
            currentTheme = state.appTheme,
            onThemeSelected = { criadorViewModel.setAppTheme(it) }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
private fun CreationModeContent(
    state: com.example.swadebuilder.CriadorState,
    viewModel: CriadorViewModel,
    ancestralidadeLocked: Boolean,
    atributosLocked: Boolean,
    periciasLocked: Boolean,
    complicacoesLocked: Boolean,
    emProgresso: Boolean,
    supersLocked: Boolean
) {
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 80.dp)
    ) {
        // SUMMARY SECTION
        item {
            ResumoSection(
                state = state,
                expanded = state.sectionsExpanded[MainSection.RESUMO] ?: true,
                onToggle = { state.toggleSection(MainSection.RESUMO) }
            )
        }

        // XP SECTION (Can enable progression mode)
        item {
            XpSection(
                viewModel = viewModel,
                expanded = state.sectionsExpanded[MainSection.XP] ?: false,
                onToggle = { state.toggleSection(MainSection.XP) }
            )
        }

        // SUPERS PHASE SECTION
        if (state.modoSupers) {
            item {
                FaseSupersSection(
                    viewModel = viewModel,
                    expanded = state.sectionsExpanded[MainSection.SUPERS] ?: true,
                    onToggle = { state.toggleSection(MainSection.SUPERS) }
                )
            }
        }

        // ANCESTRY SECTION
        item {
            AncestralidadesSection(
                state = state,
                currentAncestralidade = state.ancestralidade,
                expanded = state.sectionsExpanded[MainSection.ANCESTRALIDADE] ?: false,
                onToggle = { state.toggleSection(MainSection.ANCESTRALIDADE) },
                supersLocked = ancestralidadeLocked,
                ancestralidadeEmFoco = state.ancestralidadeEmFoco,
                onSelectAncestralidade = { newAnc ->
                    // Apply ancestry change logic
                    state.aplicarAncestralidade(newAnc, mutableListOf()) // Using empty list for now, messages handled inside
                }
            )
        }

        // MONSTER TEMPLATE (Horror)
        if (state.modoMonstroAtivo) {
            item {
                MonstroTemplateSection(
                    state = state,
                    expanded = state.sectionsExpanded[MainSection.MONSTRO] ?: false,
                    onToggle = { state.toggleSection(MainSection.MONSTRO) }
                )
            }
        }

        // CRYSTAL HEART SECTION
        if (state.compendioCrystalHeartAtivo) {
            item {
                CrystalHeartSection(
                    state = state,
                    expanded = state.sectionsExpanded[MainSection.CRYSTAL_HEART] ?: false,
                    onToggle = { state.toggleSection(MainSection.CRYSTAL_HEART) },
                    onEquip = { viewModel.selecionarCrystalHeart(it) },
                    onUnequip = { viewModel.desequiparCrystalHeart() }
                )
            }
        }

        // ARTE DA GUERRA SECTIONS
        if (state.compendioArteDaGuerraAtivo) {
            item {
                TroposSection(
                    state = state,
                    expanded = state.sectionsExpanded[MainSection.TROPOS] ?: false,
                    onToggle = { state.toggleSection(MainSection.TROPOS) }
                )
            }
            item {
                TecnicasChiSection(
                    state = state,
                    expanded = state.sectionsExpanded[MainSection.TECNICAS_CHI] ?: false,
                    onToggle = { state.toggleSection(MainSection.TECNICAS_CHI) }
                )
            }
        }

        // ATTRIBUTES SECTION
        item {
            AtributosSection(
                state = state,
                viewModel = viewModel,
                expanded = state.sectionsExpanded[MainSection.ATRIBUTOS] ?: false,
                onToggle = { state.toggleSection(MainSection.ATRIBUTOS) },
                readOnly = atributosLocked
            )
        }

        // SKILLS SECTION
        item {
            PericiasSection(
                state = state,
                viewModel = viewModel,
                expanded = state.sectionsExpanded[MainSection.PERICIAS] ?: false,
                onToggle = { state.toggleSection(MainSection.PERICIAS) },
                readOnly = periciasLocked
            )
        }

        // ADVANTAGES SECTION
        item {
            VantagensSection(
                state = state,
                viewModel = viewModel,
                expanded = state.sectionsExpanded[MainSection.VANTAGENS] ?: false,
                onToggle = { state.toggleSection(MainSection.VANTAGENS) },
                compendioFantasiaAtivo = state.compendioFantasiaAtivo,
                compendioHorrorAtivo = state.compendioHorrorAtivo,
                compendioSciFiAtivo = state.compendioSciFiAtivo,
                compendioTrilhadorAtivo = state.compendioBuscatrilhaAtivo, // Updated to Buscatrilha
                compendioDeadlandsAtivo = state.compendioDeadlandsAtivo,
                compendioCrystalHeartAtivo = state.compendioCrystalHeartAtivo,
                compendioArteDaGuerraAtivo = state.compendioArteDaGuerraAtivo,
                compendioCidadeSolVaporAtivo = state.compendioCidadeSolVaporAtivo,
                compendioWiseguysAtivo = state.compendioWiseguysAtivo
            )
        }

        // HINDRANCES SECTION
        item {
            ComplicacoesSection(
                state = state,
                expanded = state.sectionsExpanded[MainSection.COMPLICACOES] ?: false,
                onToggle = { state.toggleSection(MainSection.COMPLICACOES) },
                readOnly = complicacoesLocked
            )
        }

        // POWERS SECTION (Hidden for Crystal Heart)
        if (!state.compendioCrystalHeartAtivo) {
            item {
                PoderesSection(
                    state = state,
                    viewModel = viewModel,
                    expanded = state.sectionsExpanded[MainSection.PODERES] ?: false,
                    onToggle = { state.toggleSection(MainSection.PODERES) }
                )
            }
        }

        // RESOURCES SECTION
        item {
            RecursosSection(
                state = state,
                expanded = state.sectionsExpanded[MainSection.RECURSOS] ?: false,
                onToggle = { state.toggleSection(MainSection.RECURSOS) }
            )
        }

        // EQUIPMENT SECTION (Always editable)
        item {
            EquipamentoSection(
                state = state,
                expanded = state.sectionsExpanded[MainSection.EQUIPAMENTO] ?: false,
                onToggle = { state.toggleSection(MainSection.EQUIPAMENTO) },
                compendioFantasiaAtivo = state.compendioFantasiaAtivo,
                compendioHorrorAtivo = state.compendioHorrorAtivo,
                compendioSciFiAtivo = state.compendioSciFiAtivo,
                compendioTrilhadorAtivo = state.compendioBuscatrilhaAtivo, // Updated to Buscatrilha
                compendioDeadlandsAtivo = state.compendioDeadlandsAtivo,
                compendioCrystalHeartAtivo = state.compendioCrystalHeartAtivo,
                compendioArteDaGuerraAtivo = state.compendioArteDaGuerraAtivo,
                compendioCidadeSolVaporAtivo = state.compendioCidadeSolVaporAtivo,
                compendioWiseguysAtivo = state.compendioWiseguysAtivo
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
private fun ProgressionModeContent(
    state: com.example.swadebuilder.CriadorState,
    viewModel: CriadorViewModel,
    showingDetail: Boolean
) {
    if (showingDetail) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Summary is always useful
            ResumoSection(
                state = state,
                expanded = true, // Always expanded in this mode for context
                onToggle = { }
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (state.mostrandoVantagensProgresso) {
                VantagensSection(
                    state = state,
                    viewModel = viewModel,
                    expanded = true,
                    onToggle = { },
                    compendioFantasiaAtivo = state.compendioFantasiaAtivo,
                    compendioHorrorAtivo = state.compendioHorrorAtivo,
                    compendioSciFiAtivo = state.compendioSciFiAtivo,
                    compendioTrilhadorAtivo = state.compendioBuscatrilhaAtivo, // Updated
                    compendioDeadlandsAtivo = state.compendioDeadlandsAtivo,
                    compendioCrystalHeartAtivo = state.compendioCrystalHeartAtivo,
                    compendioArteDaGuerraAtivo = state.compendioArteDaGuerraAtivo,
                    compendioCidadeSolVaporAtivo = state.compendioCidadeSolVaporAtivo,
                    compendioWiseguysAtivo = state.compendioWiseguysAtivo
                )
            } else if (state.mostrandoPericiasProgresso) {
                PericiasSection(
                    state = state,
                    viewModel = viewModel,
                    expanded = true,
                    onToggle = { },
                    readOnly = false
                )
            } else if (state.mostrandoPoderesProgresso) {
                PoderesSection(
                    state = state,
                    viewModel = viewModel,
                    expanded = true,
                    onToggle = { }
                )
            } else if (state.mostrandoAtributosProgresso) {
                AtributosSection(
                    state = state,
                    viewModel = viewModel,
                    expanded = true,
                    onToggle = { },
                    readOnly = false
                )
            }
        }
    } else {
        // Standard Progression View
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 80.dp)
        ) {
            item {
                ResumoSection(
                    state = state,
                    expanded = state.sectionsExpanded[MainSection.RESUMO] ?: true,
                    onToggle = { state.toggleSection(MainSection.RESUMO) }
                )
            }
            item {
                XpSection(
                    viewModel = viewModel,
                    expanded = state.sectionsExpanded[MainSection.XP] ?: true,
                    onToggle = { state.toggleSection(MainSection.XP) }
                )
            }
            // Powers are relevant in progression
             if (!state.compendioCrystalHeartAtivo) {
                item {
                    PoderesSection(
                        state = state,
                        viewModel = viewModel,
                        expanded = state.sectionsExpanded[MainSection.PODERES] ?: false,
                        onToggle = { state.toggleSection(MainSection.PODERES) }
                    )
                }
            }
            // Equipment is always relevant
            item {
                EquipamentoSection(
                    state = state,
                    expanded = state.sectionsExpanded[MainSection.EQUIPAMENTO] ?: false,
                    onToggle = { state.toggleSection(MainSection.EQUIPAMENTO) },
                    compendioFantasiaAtivo = state.compendioFantasiaAtivo,
                    compendioHorrorAtivo = state.compendioHorrorAtivo,
                    compendioSciFiAtivo = state.compendioSciFiAtivo,
                    compendioTrilhadorAtivo = state.compendioBuscatrilhaAtivo,
                    compendioDeadlandsAtivo = state.compendioDeadlandsAtivo,
                    compendioCrystalHeartAtivo = state.compendioCrystalHeartAtivo,
                    compendioArteDaGuerraAtivo = state.compendioArteDaGuerraAtivo,
                    compendioCidadeSolVaporAtivo = state.compendioCidadeSolVaporAtivo,
                    compendioWiseguysAtivo = state.compendioWiseguysAtivo
                )
            }
        }
    }
}
