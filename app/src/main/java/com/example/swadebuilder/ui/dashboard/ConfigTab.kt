package com.example.swadebuilder.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.EditionConfig
import com.example.swadebuilder.criacaoBasicaCongelada
import com.example.swadebuilder.model.CriadorViewModel
import com.example.swadebuilder.ui.MainSection
import com.example.swadebuilder.ui.components.ModuleGroupCard
import com.example.swadebuilder.ui.components.ModuleToggle
import com.example.swadebuilder.ui.components.RuleGroupCard
import com.example.swadebuilder.ui.components.SimpleCheckRow
import com.example.swadebuilder.ui.sections.AncestralidadesSection
import com.example.swadebuilder.ui.sections.TroposSection
import com.example.swadebuilder.util.semAcentos

@Composable
fun ConfigTab(
    state: CriadorState,
    viewModel: CriadorViewModel,
    onUserFeedback: () -> Unit
) {
    val scrollState = rememberScrollState()
    var showMeioElfoDialog by rememberSaveable { mutableStateOf(false) }
    var pendingMeioElfoKey by rememberSaveable { mutableStateOf<String?>(null) }

    // Dialog Meio-Elfo logic duplicated from UnifiedScreen
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
                TextButton(
                    onClick = {
                        val key = pendingMeioElfoKey ?: return@TextButton
                        state.aplicarAncestralidade(key, viewModel.feedbackMessages as MutableList<String>)
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
                TextButton(
                    onClick = {
                        val key = pendingMeioElfoKey ?: return@TextButton
                        state.aplicarAncestralidade(key, viewModel.feedbackMessages as MutableList<String>)
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Identidade & Configuração", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)

        // 1. Identity
        OutlinedTextField(
            value = state.nomePersonagem,
            onValueChange = { state.nomePersonagem = it },
            label = { Text("Nome do Personagem") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.anotacoes,
            onValueChange = { state.anotacoes = it },
            label = { Text("Descrição / Anotações") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        // 2. Ancestry
        AncestralidadesSection(
            state = state,
            currentAncestralidade = state.ancestralidade,
            expanded = state.sectionsExpanded[MainSection.ANCESTRALIDADES] ?: false,
            onToggle = { state.toggleSection(MainSection.ANCESTRALIDADES) },
            supersLocked = state.criacaoBasicaCongelada,
            ancestralidadeEmFoco = state.ancestralidadeEmFoco,
            onSelectAncestralidade = { nome ->
                val key = nome.uppercase().semAcentos()
                if (key != state.ancestralidade) {
                    if (key == "MEIO-ELFOS") {
                        pendingMeioElfoKey = key
                        showMeioElfoDialog = true
                    } else {
                        pendingMeioElfoKey = null
                        state.aplicarAncestralidade(key, viewModel.feedbackMessages as MutableList<String>)
                    }
                }
            },
            onUserFeedback = onUserFeedback
        )

        // 3. Tropos (if active)
        if (state.compendioArteDaGuerraAtivo) {
            TroposSection(
                state = state,
                expanded = state.sectionsExpanded[MainSection.TROPOS] ?: false,
                onToggle = { state.toggleSection(MainSection.TROPOS) },
                onUserFeedback = onUserFeedback
            )
        }

        // 4. Settings (Modules & Rules)
        SettingsSection(state)
    }
}

@Composable
fun SettingsSection(state: CriadorState) {
    val isFullEdition = EditionConfig.isFullEdition

    // UI states for expanding cards (local to this view)
    var expandedBasicModules by rememberSaveable { mutableStateOf(false) }
    var expandedOtherModules by rememberSaveable { mutableStateOf(false) }
    var expandedBasicRules by rememberSaveable { mutableStateOf(false) }
    var expandedHorrorRules by rememberSaveable { mutableStateOf(false) }
    var expandedSupersRules by rememberSaveable { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Módulos & Regras", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        // Basic Modules
        ModuleGroupCard(
            title = "Compêndios Básicos",
            description = "Fantasia, Ficção, Horror e Supers.",
            expanded = expandedBasicModules,
            onToggle = { expandedBasicModules = !expandedBasicModules }
        ) {
            ModuleToggle(
                title = "Compêndio de Fantasia",
                description = "Raças, itens mágicos e regras de fantasia.",
                icon = Icons.Default.AutoAwesome,
                checked = state.compendioFantasiaAtivo,
                onCheckedChange = { state.compendioFantasiaAtivo = it }
            )
            ModuleToggle(
                title = "Compêndio de Ficção",
                description = "Tecnologia avançada, naves e cibernéticos.",
                icon = Icons.Default.RocketLaunch,
                checked = state.compendioSciFiAtivo,
                onCheckedChange = { state.compendioSciFiAtivo = it }
            )
            ModuleToggle(
                title = "Compêndio de Horror",
                description = "Climas sombrios e criaturas aterrorizantes.",
                icon = Icons.Default.Warning,
                checked = state.compendioHorrorAtivo,
                onCheckedChange = { state.compendioHorrorAtivo = it }
            )
            ModuleToggle(
                title = "Superpoderes",
                description = "Ativa Compêndio de Superpoderes (SPC).",
                icon = Icons.Default.Bolt,
                checked = state.modoSupers, // Mapped to modoSupers
                onCheckedChange = { state.modoSupers = it }
            )
        }

        // Other Modules
        ModuleGroupCard(
            title = "Cenários Adicionais",
            description = "Deadlands, Buscatrilha, Crystal Heart, etc.",
            expanded = expandedOtherModules,
            onToggle = { expandedOtherModules = !expandedOtherModules }
        ) {
            ModuleToggle(
                title = if (isFullEdition) "Savage Pathfinder" else "Buscatrilha",
                description = "Cenário e material temático.",
                icon = Icons.Default.Map,
                checked = state.compendioBuscatrilhaAtivo,
                onCheckedChange = { state.compendioBuscatrilhaAtivo = it }
            )
            ModuleToggle(
                title = "Deadlands: O Oeste Estranho",
                description = "Pistoleiros, Harroweds e o horror do Oeste.",
                icon = Icons.Default.Shield,
                checked = state.compendioDeadlandsAtivo,
                onCheckedChange = { state.compendioDeadlandsAtivo = it }
            )
            ModuleToggle(
                title = "Crystal Heart",
                description = "Troque seu coração por um cristal mágico.",
                icon = Icons.Default.Favorite,
                checked = state.compendioCrystalHeartAtivo,
                onCheckedChange = { state.compendioCrystalHeartAtivo = it }
            )
            ModuleToggle(
                title = "Arte da Guerra: Nova Era",
                description = "Ativa Chi, Tropos e equipamentos orientais.",
                icon = Icons.Default.Info,
                checked = state.compendioArteDaGuerraAtivo,
                onCheckedChange = { state.compendioArteDaGuerraAtivo = it }
            )
            ModuleToggle(
                title = "A Cidade do Sol a Vapor",
                description = "Estímulos vitorianos, vapor e tecnomagia.",
                icon = Icons.Default.Build,
                checked = state.compendioCidadeSolVaporAtivo,
                onCheckedChange = { state.compendioCidadeSolVaporAtivo = it }
            )
            ModuleToggle(
                title = "Wiseguys",
                description = "Crime organizado moderno.",
                icon = Icons.Default.Groups,
                checked = state.compendioWiseguysAtivo,
                onCheckedChange = { state.compendioWiseguysAtivo = it }
            )
        }

        // Rules
        RuleGroupCard(
            title = "Regras da Mesa",
            expanded = expandedBasicRules,
            onToggle = { expandedBasicRules = !expandedBasicRules }
        ) {
            SimpleCheckRow("Carta Selvagem", "Personagem principal.", state.cartaSelvagem) { state.cartaSelvagem = it }
            SimpleCheckRow("Mais Pontos de Perícia", "Customização avançada.", state.maisPontosPericias) { state.maisPontosPericias = it }
            SimpleCheckRow("Múltiplos Ant. Arcanos", "Combina classes conjuradoras.", state.permiteMultiAntecedenteArcano) { state.permiteMultiAntecedenteArcano = it }
            SimpleCheckRow("Especialização de Perícias", "Regra opcional.", state.usarEspecializacoesDePericia) { state.usarEspecializacoesDePericia = it }
            SimpleCheckRow("Heróis sem Armadura", "Para cenários Pulp.", state.heroisSemArmadura) { state.heroisSemArmadura = it }
            SimpleCheckRow("Múltiplos Idiomas", "Personagem inicia poliglota.", state.regraMultiplosIdiomas) { state.regraMultiplosIdiomas = it }
            SimpleCheckRow("Nasce um Herói", "Ignora requisitos de Estágio.", state.nasceUmHeroi) { state.nasceUmHeroi = it }
            SimpleCheckRow("Sem Pontos de Poder", "Conjuradores não usam PP.", state.usarSemPontosDePoder) { state.usarSemPontosDePoder = it }
        }

        if (state.compendioHorrorAtivo) {
            RuleGroupCard(
                title = "Regras: Horror",
                expanded = expandedHorrorRules,
                onToggle = { expandedHorrorRules = !expandedHorrorRules }
            ) {
                SimpleCheckRow("Monstros Heróis", "Jogar como vampiro, etc.", state.modoMonstroAtivo) { state.modoMonstroAtivo = it }
            }
        }

        if (state.modoSupers) {
            RuleGroupCard(
                title = "Regras: Superpoderes",
                expanded = expandedSupersRules,
                onToggle = { expandedSupersRules = !expandedSupersRules }
            ) {
                SimpleCheckRow("Grandes Responsabilidades", "Débitos de Poder adicionais.", state.grandesResponsabilidades) { state.grandesResponsabilidades = it }
            }
        }
    }
}
