package com.example.swadebuilder

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoodBad
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsMartialArts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.model.CriadorViewModel
import com.example.swadebuilder.ui.components.ModuleCard
import com.example.swadebuilder.util.toEditionDisplayName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaInicial(
    onCriarNovo: (
        cartaSelvagem: Boolean,
        maisPontosPericias: Boolean,
        modoSupers: Boolean,
        compendioFantasiaAtivo: Boolean,
        compendioHorrorAtivo: Boolean,
        compendioSciFiAtivo: Boolean,
        compendioPathfinderAtivo: Boolean,
        compendioDeadlandsAtivo: Boolean,
        compendioCrystalHeartAtivo: Boolean,
        compendioArteDaGuerraAtivo: Boolean,
        compendioCidadeSolVaporAtivo: Boolean,
        compendioWiseguysAtivo: Boolean,
        modoMonstroAtivo: Boolean,
        nasceUmHeroi: Boolean,
        heroisSemArmadura: Boolean,
        especializacaoPer: Boolean,
        semPontosDePoder: Boolean,
        multiplosIdiomas: Boolean,
        grandesResponsabilidades: Boolean,
        optRegraFama: Boolean,
        optRegraRiqueza: Boolean,
        optRegraCosaNostra: Boolean
    ) -> Unit,
    onCarregarPersonagem: () -> Unit,
    onOpenSettings: () -> Unit,
    context: Context,
    viewModel: CriadorViewModel
) {
    val isFullEdition = EditionConfig.isFullEdition

    // --- State Variables ---

    // Core Rules
    var optCartaSelvagem by rememberSaveable { mutableStateOf(true) }
    var optMaisPontosPericias by rememberSaveable { mutableStateOf(true) }
    var optMultiAntecedenteArcano by rememberSaveable { mutableStateOf(false) }
    var optEspecializacaoPer by rememberSaveable { mutableStateOf(false) }
    var optHeroiSemArmadura by rememberSaveable { mutableStateOf(false) }
    var optMultiplosIdiomas by rememberSaveable { mutableStateOf(false) }
    var optNasceUmHeroi by rememberSaveable { mutableStateOf(false) }
    var optSemPontosPoder by rememberSaveable { mutableStateOf(false) }

    // Supers
    var optSuperPoderes by rememberSaveable { mutableStateOf(false) }
    var optGrandesResponsabilidades by rememberSaveable { mutableStateOf(false) }

    // Horror
    var optCompendioHorror by rememberSaveable { mutableStateOf(false) }
    var optModoMonstro by rememberSaveable { mutableStateOf(false) }

    // Fantasy
    var optCompendioFantasia by rememberSaveable { mutableStateOf(false) }
    var optCompendioPathfinder by rememberSaveable { mutableStateOf(false) }
    var optCompendioDeadlands by rememberSaveable { mutableStateOf(false) }
    var optCompendioCrystalHeart by rememberSaveable { mutableStateOf(false) }
    var optCompendioArteDaGuerra by rememberSaveable { mutableStateOf(false) }
    var optRegraFama by rememberSaveable { mutableStateOf(false) }
    var optCompendioCidadeSolVapor by rememberSaveable { mutableStateOf(false) }
    var optCompendioWiseguys by rememberSaveable { mutableStateOf(false) }
    var optRegraRiqueza by rememberSaveable { mutableStateOf(false) }
    var optRegraCosaNostra by rememberSaveable { mutableStateOf(false) }

    // SciFi
    var optCompendioSciFi by rememberSaveable { mutableStateOf(false) }

    // Dialog States
    var showCreditsDialog by remember { mutableStateOf(false) }
    var showRulesDialog by remember { mutableStateOf(false) }

    // Helper for applying rules presets
    fun applyRulesPreset(preset: String) {
        when (preset) {
            "Supers" -> {
                optCartaSelvagem = true
                optMaisPontosPericias = true
                optNasceUmHeroi = true // Comum em supers
            }
            "Arte da Guerra" -> {
                optCartaSelvagem = true
                optNasceUmHeroi = true
                optHeroiSemArmadura = true
            }
            "Wiseguys" -> {
                optCartaSelvagem = true
                optMaisPontosPericias = true
                optRegraRiqueza = true
                optRegraCosaNostra = true
                optMultiAntecedenteArcano = false
                optEspecializacaoPer = false
                optHeroiSemArmadura = false
                optMultiplosIdiomas = false
                optNasceUmHeroi = false
                optSemPontosPoder = false
            }
            "Pathfinder" -> {
                optMaisPontosPericias = false
            }
            "Básico" -> {
                optCartaSelvagem = true
                optMaisPontosPericias = true
                optNasceUmHeroi = false
                optHeroiSemArmadura = false
                optRegraRiqueza = false
                optRegraCosaNostra = false
                optRegraFama = false
            }
        }
    }

    // Data for Grid
    data class ModuleItemData(
        val title: String,
        val description: String,
        val icon: ImageVector,
        val isSelected: Boolean,
        val enabled: Boolean,
        val onToggle: () -> Unit,
        val onRulesClick: () -> Unit,
        val isRulesActive: Boolean = false
    )

    val isAnyBookSelected = optCompendioFantasia || optCompendioSciFi || optCompendioHorror || optSuperPoderes ||
            optCompendioPathfinder || optCompendioDeadlands || optCompendioCrystalHeart ||
            optCompendioArteDaGuerra || optCompendioCidadeSolVapor || optCompendioWiseguys

    val officialModules = listOf(
        ModuleItemData(
            "Regras Básicas",
            "Savage Worlds Edição Aventura (Core).",
            Icons.Default.MenuBook,
            isSelected = !isAnyBookSelected,
            enabled = true,
            onToggle = {
                 // Clicking Basic unchecks everything else
                 optCompendioFantasia = false
                 optCompendioHorror = false
                 optCompendioSciFi = false
                 optSuperPoderes = false
                 optCompendioPathfinder = false
                 optCompendioDeadlands = false
                 optCompendioCrystalHeart = false
                 optCompendioArteDaGuerra = false
                 optCompendioCidadeSolVapor = false
                 optCompendioWiseguys = false
                 applyRulesPreset("Básico")
            },
            onRulesClick = { applyRulesPreset("Básico"); showRulesDialog = true },
            isRulesActive = !optSuperPoderes && !optCompendioPathfinder && !optCompendioArteDaGuerra && !optCompendioWiseguys
        ),
        ModuleItemData(
            "Compêndio de Fantasia",
            "Raças, itens mágicos e regras de fantasia.",
            Icons.Default.AutoAwesome,
            optCompendioFantasia,
            !isAnyBookSelected || optCompendioFantasia,
            { optCompendioFantasia = !optCompendioFantasia },
            { applyRulesPreset("Básico"); showRulesDialog = true } // Fantasy usually builds on Basic
        ),
        ModuleItemData(
            "Compêndio de Ficção",
            "Tecnologia avançada, naves e cibernéticos.",
            Icons.Default.RocketLaunch,
            optCompendioSciFi,
            !isAnyBookSelected || optCompendioSciFi,
            { optCompendioSciFi = !optCompendioSciFi },
            { applyRulesPreset("Básico"); showRulesDialog = true }
        ),
        ModuleItemData(
            "Compêndio de Horror",
            "Climas sombrios e criaturas aterrorizantes.",
            Icons.Default.MoodBad,
            optCompendioHorror,
            !isAnyBookSelected || optCompendioHorror,
            { optCompendioHorror = !optCompendioHorror },
            { applyRulesPreset("Básico"); showRulesDialog = true }
        ),
        ModuleItemData(
            "Superpoderes",
            "Seja um superherói!",
            Icons.Default.Bolt,
            optSuperPoderes,
            !isAnyBookSelected || optSuperPoderes,
            {
                optSuperPoderes = !optSuperPoderes
                if (optSuperPoderes) applyRulesPreset("Supers")
            },
            { applyRulesPreset("Supers"); showRulesDialog = true },
            isRulesActive = optSuperPoderes
        )
    )

    val settingModules = listOf(
        ModuleItemData(
            androidx.compose.ui.res.stringResource(R.string.sw_pathfinder_label),
            if (isFullEdition) "Conteúdo oficial de Mundo Ancestral (Classes, Raças)." else "Cenário ${androidx.compose.ui.res.stringResource(R.string.sw_pathfinder_label)} e material temático.",
            Icons.Default.Map,
            optCompendioPathfinder,
            !isAnyBookSelected || optCompendioPathfinder,
            {
                optCompendioPathfinder = !optCompendioPathfinder
                if (optCompendioPathfinder) applyRulesPreset("Pathfinder")
            },
            { applyRulesPreset("Pathfinder"); showRulesDialog = true },
            isRulesActive = optCompendioPathfinder
        ),
        ModuleItemData(
            "Deadlands".toEditionDisplayName(),
            if (isFullEdition) "Pistoleiros, atormentados e o horror do Oeste." else "Pistoleiros, revividos e o horror do Oeste.",
            Icons.Default.Shield,
            optCompendioDeadlands,
            !isAnyBookSelected || optCompendioDeadlands,
            { optCompendioDeadlands = !optCompendioDeadlands },
            { applyRulesPreset("Básico"); showRulesDialog = true }
        ),
        ModuleItemData(
            "Crystal Heart".toEditionDisplayName(),
            if (isFullEdition) "Troque seu coração por um cristal mágico." else "Troque seu coração por uma pedra mágica.",
            Icons.Default.Favorite,
            optCompendioCrystalHeart,
            !isAnyBookSelected || optCompendioCrystalHeart,
            { optCompendioCrystalHeart = !optCompendioCrystalHeart },
            { applyRulesPreset("Básico"); showRulesDialog = true }
        ),
        ModuleItemData(
            "Arte da Guerra: Nova Era".toEditionDisplayName(),
            "Ativa Chi, Tropos e equipamentos orientais.",
            Icons.Filled.SportsMartialArts,
            optCompendioArteDaGuerra,
            !isAnyBookSelected || optCompendioArteDaGuerra,
            {
                optCompendioArteDaGuerra = !optCompendioArteDaGuerra
                if (optCompendioArteDaGuerra) applyRulesPreset("Arte da Guerra")
            },
            { applyRulesPreset("Arte da Guerra"); showRulesDialog = true },
            isRulesActive = optCompendioArteDaGuerra
        ),
        ModuleItemData(
            "A Cidade do Sol a Vapor".toEditionDisplayName(),
            "Estímulos vitorianos, vapor e tecnomagia.",
            Icons.Default.Build,
            optCompendioCidadeSolVapor,
            !isAnyBookSelected || optCompendioCidadeSolVapor,
            { optCompendioCidadeSolVapor = !optCompendioCidadeSolVapor },
            { applyRulesPreset("Básico"); showRulesDialog = true }
        ),
        ModuleItemData(
            "Wiseguys".toEditionDisplayName(),
            "Crime organizado moderno, conexões e esquemas.",
            Icons.Default.Groups,
            optCompendioWiseguys,
            !isAnyBookSelected || optCompendioWiseguys,
            {
                optCompendioWiseguys = !optCompendioWiseguys
                if (optCompendioWiseguys) applyRulesPreset("Wiseguys")
            },
            { applyRulesPreset("Wiseguys"); showRulesDialog = true },
            isRulesActive = optCompendioWiseguys
        )
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("SWADE Builder", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    IconButton(onClick = onCarregarPersonagem) {
                        Icon(Icons.Default.FolderOpen, contentDescription = "Carregar Personagem")
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Configurações")
                    }
                    IconButton(onClick = { showCreditsDialog = true }) {
                        Icon(Icons.Default.Info, contentDescription = "Créditos")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    onCriarNovo(
                        optCartaSelvagem,
                        optMaisPontosPericias,
                        optSuperPoderes,
                        optCompendioFantasia,
                        optCompendioHorror,
                        optCompendioSciFi,
                        optCompendioPathfinder,
                        optCompendioDeadlands,
                        optCompendioCrystalHeart,
                        optCompendioArteDaGuerra,
                        optCompendioCidadeSolVapor,
                        optCompendioWiseguys,
                        optModoMonstro,
                        optNasceUmHeroi,
                        optHeroiSemArmadura,
                        optEspecializacaoPer,
                        optSemPontosPoder,
                        optMultiplosIdiomas,
                        optGrandesResponsabilidades,
                        optRegraFama,
                        optRegraRiqueza,
                        optRegraCosaNostra
                    )
                    viewModel.state.compendioPathfinderAtivo = optCompendioPathfinder
                    viewModel.state.compendioDeadlandsAtivo = optCompendioDeadlands
                    viewModel.state.compendioCrystalHeartAtivo = optCompendioCrystalHeart
                    viewModel.state.compendioArteDaGuerraAtivo = optCompendioArteDaGuerra
                    viewModel.state.compendioCidadeSolVaporAtivo = optCompendioCidadeSolVapor
                    viewModel.state.compendioWiseguysAtivo = optCompendioWiseguys
                    viewModel.state.optRegraRiqueza = optRegraRiqueza
                    viewModel.state.optRegraCosaNostra = optRegraCosaNostra
                    viewModel.state.permiteMultiAntecedenteArcano = optMultiAntecedenteArcano
                    viewModel.state.regraMultiplosIdiomas = optMultiplosIdiomas
                },
                icon = { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null) },
                text = { Text("CRIAR PERSONAGEM") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
        ) {
            // Header Text
            item(span = { GridItemSpan(2) }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Configuração da Campanha",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Selecione os livros e regras para seu novo herói.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // --- Compêndios Oficiais ---
            item(span = { GridItemSpan(2) }) { SectionHeader("Compêndios Oficiais") }

            items(officialModules) { module ->
                ModuleCard(
                    title = module.title,
                    description = module.description,
                    icon = module.icon,
                    isSelected = module.isSelected,
                    enabled = module.enabled,
                    onToggle = module.onToggle,
                    showDescription = viewModel.state.mostrarDescricaoHome,
                    onRulesClick = if (module.isSelected) module.onRulesClick else null,
                    isRulesActive = module.isRulesActive,
                    tabStyle = viewModel.state.estiloAbas
                )
            }

            // --- Cenários de Campanha ---
            item(span = { GridItemSpan(2) }) { SectionHeader("Cenários de Campanha") }

            items(settingModules) { module ->
                ModuleCard(
                    title = module.title,
                    description = module.description,
                    icon = module.icon,
                    isSelected = module.isSelected,
                    enabled = module.enabled,
                    onToggle = module.onToggle,
                    showDescription = viewModel.state.mostrarDescricaoHome,
                    onRulesClick = if (module.isSelected) module.onRulesClick else null,
                    isRulesActive = module.isRulesActive,
                    tabStyle = viewModel.state.estiloAbas
                )
            }

            // Spacer for FAB
            item(span = { GridItemSpan(2) }) { Spacer(Modifier.height(80.dp)) }
        }
    }

    // --- Dialogs ---

    if (showRulesDialog) {
        AlertDialog(
            onDismissRequest = { showRulesDialog = false },
            confirmButton = { TextButton(onClick = { showRulesDialog = false }) { Text("Fechar") } },
            title = { Text("Regras de Cenário") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    if (optSuperPoderes) {
                        SimpleCheckRow("Nasce um Herói", "Ignora requisitos de Estágio na criação.", optNasceUmHeroi) { optNasceUmHeroi = it }
                        SimpleCheckRow("Heróis sem Armadura", "Para cenários Pulp/Cinematográficos.", optHeroiSemArmadura) { optHeroiSemArmadura = it }
                        if (!optCompendioPathfinder) {
                            SimpleCheckRow("Múltiplos Idiomas", "Personagem inicia poliglota.", optMultiplosIdiomas) { optMultiplosIdiomas = it }
                        }
                        SimpleCheckRow(
                            title = "Grandes Responsabilidades",
                            description = "Débitos de Poder adicionais.",
                            checked = optGrandesResponsabilidades,
                            onCheckedChange = { optGrandesResponsabilidades = it }
                        )
                        SimpleCheckRow("Carta Selvagem", "Personagem principal (Benes, Dado Selvagem).", optCartaSelvagem) { optCartaSelvagem = it }
                        SimpleCheckRow("Mais Pontos de Perícia", "Customização avançada (Regra da Casa).", optMaisPontosPericias) { optMaisPontosPericias = it }
                        SimpleCheckRow("Especialização de Perícias", "Regra opcional de especialização.", optEspecializacaoPer) { optEspecializacaoPer = it }
                    } else if (optCompendioArteDaGuerra) {
                        SimpleCheckRow("Carta Selvagem", "Personagem principal (Benes, Dado Selvagem).", optCartaSelvagem) { optCartaSelvagem = it }
                        SimpleCheckRow("Nasce um Herói", "Ignora requisitos de Estágio na criação.", optNasceUmHeroi) { optNasceUmHeroi = it }
                        SimpleCheckRow("Heróis sem Armadura", "Para cenários Pulp/Cinematográficos.", optHeroiSemArmadura) { optHeroiSemArmadura = it }
                        SimpleCheckRow(
                            title = "Regra de Fama",
                            description = "Adiciona o atributo Fama (Arte da Guerra).",
                            checked = optRegraFama,
                            onCheckedChange = { optRegraFama = it }
                        )
                    } else {
                        SimpleCheckRow("Carta Selvagem", "Personagem principal (Benes, Dado Selvagem).", optCartaSelvagem) { optCartaSelvagem = it }
                        SimpleCheckRow("Mais Pontos de Perícia", "Customização avançada (Regra da Casa).", optMaisPontosPericias) { optMaisPontosPericias = it }

                        if (!optCompendioWiseguys) {
                            if (!optCompendioFantasia && !optCompendioHorror) {
                                SimpleCheckRow(
                                    "Múltiplos Ant. Arcanos",
                                    "Permite combinar classes conjuradoras.",
                                    optMultiAntecedenteArcano
                                ) { optMultiAntecedenteArcano = it }
                            }
                            SimpleCheckRow("Especialização de Perícias", "Regra opcional de especialização.", optEspecializacaoPer) { optEspecializacaoPer = it }
                            SimpleCheckRow("Heróis sem Armadura", "Para cenários Pulp/Cinematográficos.", optHeroiSemArmadura) { optHeroiSemArmadura = it }
                            if (!optCompendioPathfinder) {
                                SimpleCheckRow("Múltiplos Idiomas", "Personagem inicia poliglota.", optMultiplosIdiomas) { optMultiplosIdiomas = it }
                            }
                            SimpleCheckRow("Nasce um Herói", "Ignora requisitos de Estágio na criação.", optNasceUmHeroi) { optNasceUmHeroi = it }
                            SimpleCheckRow("Sem Pontos de Poder", "Conjuradores não usam PP.", optSemPontosPoder) { optSemPontosPoder = it }
                        }

                        SimpleCheckRow(
                            title = "Regra de Riqueza",
                            description = "Substitui dinheiro por rolagens de Riqueza.",
                            checked = optRegraRiqueza,
                            onCheckedChange = { optRegraRiqueza = it }
                        )

                        if (optCompendioWiseguys) {
                            SimpleCheckRow(
                                title = "A Cosa Nostra",
                                description = "Regra de ambientação (Wiseguys).",
                                checked = optRegraCosaNostra,
                                onCheckedChange = { optRegraCosaNostra = it }
                            )
                        }

                        if (optCompendioHorror) {
                            SimpleCheckRow(
                                title = "Monstros Heróis",
                                description = "Jogar como vampiro, lobisomem, etc.",
                                checked = optModoMonstro,
                                onCheckedChange = { optModoMonstro = it }
                            )
                        }
                    }
                }
            }
        )
    }

    // --- Dialogs ---

    if (showCreditsDialog) {
        AlertDialog(
            onDismissRequest = { showCreditsDialog = false },
            confirmButton = { TextButton(onClick = { showCreditsDialog = false }) { Text("Fechar") } },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.sw_fan_logo),
                        contentDescription = "Savage Worlds Fan Logo",
                        modifier = Modifier.fillMaxWidth().height(100.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = """
Este jogo faz referência ao sistema de regras Savage Worlds, disponibilizado mundialmente pela Pinnacle Entertainment Group (www.peginc.com) e no Brasil pela RetroPunk Publicações (www.retropunk.net).

Savage Worlds e todas as suas logos e marcas associadas são de propriedade da Pinnacle Entertainment Group. Utilizadas com permissão. A Pinnacle e a RetroPunk não fazem nenhuma representação ou garantia quanto à qualidade, viabilidade ou adequação em relação a este produto.

Feito por Rafael S.W.
""".trimIndent(),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Justify
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        )
    }
}


@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

@Composable
fun SimpleCheckRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = null)
        Spacer(Modifier.width(8.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(text = description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
