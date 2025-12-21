package com.example.swadebuilder.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backpack
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.model.CriadorViewModel
import com.example.swadebuilder.model.EquipamentoCategoria
import com.example.swadebuilder.model.SuperPoder
import com.example.swadebuilder.ui.components.LiveSummaryPanel
import com.example.swadebuilder.ui.dashboard.ConfigTab
import com.example.swadebuilder.ui.dashboard.EdgesTab
import com.example.swadebuilder.ui.dashboard.GearTab
import com.example.swadebuilder.ui.dashboard.PowersTab
import com.example.swadebuilder.ui.dashboard.ProgressTab
import com.example.swadebuilder.ui.dashboard.StatsTab
import androidx.compose.runtime.LaunchedEffect

enum class DashboardTab(val label: String, val icon: ImageVector) {
    CONFIG("Identidade", Icons.Default.Settings),
    STATS("Atributos", Icons.Default.FitnessCenter),
    EDGES("Vantagens", Icons.Default.Star),
    POWERS("Poderes", Icons.Default.FlashOn),
    GEAR("Equipamento", Icons.Default.Backpack),
    PROGRESS("Progressão", Icons.Default.TrendingUp)
}

@Composable
fun DashboardScreen(
    state: CriadorState,
    viewModel: CriadorViewModel,
    equipamentoCategorias: List<EquipamentoCategoria>,
    superequipCategorias: List<EquipamentoCategoria>,
    listaSuperPoderes: List<SuperPoder>,
    modoOficialAtivo: Boolean = false,
    onUserFeedback: () -> Unit
) {
    var currentTab by rememberSaveable { mutableStateOf(DashboardTab.CONFIG) }
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Determina quais tabs estão disponíveis
    val availableTabs = DashboardTab.entries.filter { tab ->
        when (tab) {
            DashboardTab.POWERS -> {
                // Só mostra se tiver supers ou algum antecedente arcano (ou se estiver comprando um)
                val temArcano = state.vantagensSelecionadas.any {
                    it.nome.uppercase().startsWith("ANTECEDENTE ARCANO")
                }
                val arcanoPendente = state.arcanoCompraPendente()
                state.modoSupers || temArcano || arcanoPendente || state.mostrandoPoderesProgresso
            }
            DashboardTab.PROGRESS -> state.creationComplete() || state.modoProgressaoAtivo
            else -> true
        }
    }

    // Se a tab atual sumir (ex: removeu AA), volta pra Config
    if (currentTab !in availableTabs) {
        currentTab = DashboardTab.CONFIG
    }

    // Auto-switch tabs based on progression state
    LaunchedEffect(state.mostrandoVantagensProgresso) {
        if (state.mostrandoVantagensProgresso) currentTab = DashboardTab.EDGES
    }
    LaunchedEffect(state.mostrandoPericiasProgresso) {
        if (state.mostrandoPericiasProgresso) currentTab = DashboardTab.STATS
    }
    LaunchedEffect(state.mostrandoAtributosProgresso) {
        if (state.mostrandoAtributosProgresso) currentTab = DashboardTab.STATS
    }
    LaunchedEffect(state.mostrandoPoderesProgresso) {
        if (state.mostrandoPoderesProgresso) currentTab = DashboardTab.POWERS
    }

    if (isLandscape) {
        DashboardLandscapeLayout(
            currentTab = currentTab,
            availableTabs = availableTabs,
            onTabSelected = { currentTab = it },
            content = {
                DashboardContent(
                    tab = currentTab,
                    state = state,
                    viewModel = viewModel,
                    equipamentoCategorias = equipamentoCategorias,
                    superequipCategorias = superequipCategorias,
                    listaSuperPoderes = listaSuperPoderes,
                    isLandscape = true,
                    onUserFeedback = onUserFeedback
                )
            },
            summaryPanel = {
                LiveSummaryPanel(state = state)
            }
        )
    } else {
        DashboardPortraitLayout(
            currentTab = currentTab,
            availableTabs = availableTabs,
            onTabSelected = { currentTab = it },
            content = {
                DashboardContent(
                    tab = currentTab,
                    state = state,
                    viewModel = viewModel,
                    equipamentoCategorias = equipamentoCategorias,
                    superequipCategorias = superequipCategorias,
                    listaSuperPoderes = listaSuperPoderes,
                    isLandscape = false,
                    onUserFeedback = onUserFeedback
                )
            }
        )
    }
}

@Composable
fun DashboardLandscapeLayout(
    currentTab: DashboardTab,
    availableTabs: List<DashboardTab>,
    onTabSelected: (DashboardTab) -> Unit,
    content: @Composable () -> Unit,
    summaryPanel: @Composable () -> Unit
) {
    Row(Modifier.fillMaxSize()) {
        NavigationRail(
            header = {
                // Optional header
            }
        ) {
            availableTabs.forEach { tab ->
                NavigationRailItem(
                    selected = currentTab == tab,
                    onClick = { onTabSelected(tab) },
                    icon = { Icon(tab.icon, contentDescription = tab.label) },
                    label = { Text(tab.label) }
                )
            }
        }

        Box(
            Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            content()
        }

        // Vertical divider could go here

        Box(
            Modifier
                .width(300.dp)
                .fillMaxHeight()
                .padding(8.dp)
        ) {
            summaryPanel()
        }
    }
}

@Composable
fun DashboardPortraitLayout(
    currentTab: DashboardTab,
    availableTabs: List<DashboardTab>,
    onTabSelected: (DashboardTab) -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold(
        bottomBar = {
            NavigationBar {
                availableTabs.forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { onTabSelected(tab) },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            content()
        }
    }
}

@Composable
fun DashboardContent(
    tab: DashboardTab,
    state: CriadorState,
    viewModel: CriadorViewModel,
    equipamentoCategorias: List<EquipamentoCategoria>,
    superequipCategorias: List<EquipamentoCategoria>,
    listaSuperPoderes: List<SuperPoder>,
    isLandscape: Boolean,
    onUserFeedback: () -> Unit
) {
    when (tab) {
        DashboardTab.CONFIG -> ConfigTab(state, viewModel, onUserFeedback)
        DashboardTab.STATS -> StatsTab(state, viewModel, isLandscape, onUserFeedback)
        DashboardTab.EDGES -> EdgesTab(state, viewModel, onUserFeedback)
        DashboardTab.POWERS -> PowersTab(state, viewModel, listaSuperPoderes, onUserFeedback)
        DashboardTab.GEAR -> GearTab(state, equipamentoCategorias, superequipCategorias, onUserFeedback)
        DashboardTab.PROGRESS -> ProgressTab(state, viewModel)
    }
}
