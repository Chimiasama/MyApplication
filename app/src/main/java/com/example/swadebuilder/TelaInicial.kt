package com.example.swadebuilder

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.unit.sp
import com.example.swadebuilder.model.CriadorViewModel

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
        compendioTrilhadorAtivo: Boolean,
        compendioDeadlandsAtivo: Boolean,
        modoMonstroAtivo: Boolean,
        modoSuperequipamentos: Boolean,
        modoSuperComplicacoes: Boolean,
        nasceUmHeroi: Boolean,
        heroisSemArmadura: Boolean,
        expecializacaoPer: Boolean,
        semPontosDePoder: Boolean,
        grandesResponsabilidades: Boolean,
        showHelpMessages: Boolean
    ) -> Unit,
    onCarregarPersonagem: () -> Unit,
    context: Context,
    viewModel: CriadorViewModel
) {
    // --- State Variables (matching original logic) ---

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
    // optSuperequipamentos and optSuperComplicacoes removed (now auto-enabled with optSuperPoderes)
    var optGrandesResponsabilidades by rememberSaveable { mutableStateOf(false) }

    // Horror
    var optCompendioHorror by rememberSaveable { mutableStateOf(false) }
    var optModoMonstro by rememberSaveable { mutableStateOf(false) }

    // Fantasy
    var optCompendioFantasia by rememberSaveable { mutableStateOf(false) }
    var optCompendioTrilhador by rememberSaveable { mutableStateOf(false) }
    var optCompendioDeadlands by rememberSaveable { mutableStateOf(false) }

    // SciFi
    var optCompendioSciFi by rememberSaveable { mutableStateOf(false) }

    // Dialog States
    var showCreditsDialog by remember { mutableStateOf(false) }
    var showHelpAppDialog by rememberSaveable { mutableStateOf(false) }

    // UI Expansion States
    var expandedBasicRules by rememberSaveable { mutableStateOf(false) }
    var expandedHorrorRules by rememberSaveable { mutableStateOf(false) }
    var expandedSupersRules by rememberSaveable { mutableStateOf(false) }


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("SWADE Builder", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    IconButton(onClick = onCarregarPersonagem) {
                        Icon(Icons.Default.FolderOpen, contentDescription = "Carregar Personagem")
                    }
                    IconButton(onClick = { showHelpAppDialog = true }) {
                        Icon(Icons.Default.Help, contentDescription = "Ajuda")
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
                        optCompendioTrilhador,
                        optCompendioDeadlands,
                        optModoMonstro,
                        optSuperPoderes, // superequipamentos enabled if supers enabled
                        optSuperPoderes, // supercomplicacoes enabled if supers enabled
                        optNasceUmHeroi,
                        optHeroiSemArmadura,
                        optEspecializacaoPer,
                        optSemPontosPoder,
                        optGrandesResponsabilidades,
                        true // showHelpMessages defaulted to true
                    )
                    // Set ViewModel states that are handled outside the creation lambda
                    viewModel.state.compendioTrilhadorAtivo = optCompendioTrilhador
                    viewModel.state.compendioDeadlandsAtivo = optCompendioDeadlands
                    viewModel.state.permiteMultiAntecedenteArcano = optMultiAntecedenteArcano
                    viewModel.state.regraMultiplosIdiomas = optMultiplosIdiomas
                },
                icon = { Icon(Icons.Default.ArrowForward, contentDescription = null) },
                text = { Text("CRIAR PERSONAGEM") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = innerPadding,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
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

            // --- Compêndios Section ---
            item { SectionHeader("Compêndios & Cenários") }

            item {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    ModuleToggle(
                        title = "Compêndio de Fantasia",
                        description = "Raças, itens mágicos e regras de fantasia.",
                        icon = Icons.Default.AutoAwesome,
                        checked = optCompendioFantasia,
                        onCheckedChange = { optCompendioFantasia = it }
                    )
                    ModuleToggle(
                        title = "Compêndio de Horror",
                        description = "Climas sombrios e criaturas aterrorizantes.",
                        icon = Icons.Default.Warning,
                        checked = optCompendioHorror,
                        onCheckedChange = { optCompendioHorror = it }
                    )
                    ModuleToggle(
                        title = "Superpoderes",
                        description = "Ativa Compêndio de Superpoderes (SPC).",
                        icon = Icons.Default.Bolt,
                        checked = optSuperPoderes,
                        onCheckedChange = { optSuperPoderes = it }
                    )
                    ModuleToggle(
                        title = "Compêndio de Sci-Fi",
                        description = "Tecnologia avançada, naves e cibernéticos.",
                        icon = Icons.Default.RocketLaunch,
                        checked = optCompendioSciFi,
                        onCheckedChange = { optCompendioSciFi = it }
                    )
                    ModuleToggle(
                        title = "Savage Pathfinder",
                        description = "Conteúdo oficial de Golarion (Classes, Raças).",
                        icon = Icons.Default.Map,
                        checked = optCompendioTrilhador,
                        onCheckedChange = { optCompendioTrilhador = it }
                    )
                    ModuleToggle(
                        title = "Deadlands: O Oeste Estranho",
                        description = "Pistoleiros, Harroweds e o horror do Oeste.",
                        icon = Icons.Default.Shield,
                        checked = optCompendioDeadlands,
                        onCheckedChange = { optCompendioDeadlands = it }
                    )
                }
            }

            // --- Regras da Mesa (Collapsible Sections) ---
            item { SectionHeader("Regras de Criação") }

            item {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Livro Básico
                    RuleGroupCard(
                        title = "Regras: Livro Básico",
                        expanded = expandedBasicRules,
                        onToggle = { expandedBasicRules = !expandedBasicRules }
                    ) {
                        SimpleCheckRow("Carta Selvagem", "Personagem principal (Benes, Dado Selvagem).", optCartaSelvagem) { optCartaSelvagem = it }
                        SimpleCheckRow("Mais Pontos de Perícia", "Customização avançada (Regra da Casa).", optMaisPontosPericias) { optMaisPontosPericias = it }
                        SimpleCheckRow("Múltiplos Ant. Arcanos", "Permite combinar classes conjuradoras.", optMultiAntecedenteArcano) { optMultiAntecedenteArcano = it }
                        SimpleCheckRow("Especialização de Perícias", "Regra opcional de especialização.", optEspecializacaoPer) { optEspecializacaoPer = it }
                        SimpleCheckRow("Heróis sem Armadura", "Para cenários Pulp/Cinematográficos.", optHeroiSemArmadura) { optHeroiSemArmadura = it }
                        SimpleCheckRow("Múltiplos Idiomas", "Personagem inicia poliglota.", optMultiplosIdiomas) { optMultiplosIdiomas = it }
                        SimpleCheckRow("Nasce um Herói", "Ignora requisitos de Estágio na criação.", optNasceUmHeroi) { optNasceUmHeroi = it }
                        SimpleCheckRow("Sem Pontos de Poder", "Conjuradores não usam PP.", optSemPontosPoder) { optSemPontosPoder = it }
                    }

                    // Regras Horror
                    if (optCompendioHorror) {
                        RuleGroupCard(
                            title = "Regras: Horror",
                            expanded = expandedHorrorRules,
                            onToggle = { expandedHorrorRules = !expandedHorrorRules }
                        ) {
                            SimpleCheckRow(
                                title = "Monstros Heróis",
                                description = "Jogar como vampiro, lobisomem, etc.",
                                checked = optModoMonstro,
                                onCheckedChange = { optModoMonstro = it }
                            )
                        }
                    }

                    // Regras Supers
                    if (optSuperPoderes) {
                        RuleGroupCard(
                            title = "Regras: Superpoderes",
                            expanded = expandedSupersRules,
                            onToggle = { expandedSupersRules = !expandedSupersRules }
                        ) {
                            SimpleCheckRow(
                                title = "Grandes Responsabilidades",
                                description = "Débitos de Poder adicionais.",
                                checked = optGrandesResponsabilidades,
                                onCheckedChange = { optGrandesResponsabilidades = it }
                            )
                            // Superequipamentos and Supercomplicacoes are now implied by the module itself
                        }
                    }
                }
            }

            // Spacer for FAB
            item { Spacer(Modifier.height(80.dp)) }
        }
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

    if (showHelpAppDialog) {
        val helpAppText = """
Este app ajuda você a criar personagens de Savage Worlds passo a passo.

1) Tela Inicial
   • Escolha os livros e modos de jogo.
   • Toque em "CRIAR PERSONAGEM".

2) Fluxo
   • Ancestralidade → Atributos → Perícias → Complicações → Vantagens.

3) Pontos
   • Complicações geram pontos bônus para gastar em outras áreas.
   • Use os botões dentro de cada seção para gastar esses pontos.
""".trimIndent()

        AlertDialog(
            onDismissRequest = { showHelpAppDialog = false },
            confirmButton = { TextButton(onClick = { showHelpAppDialog = false }) { Text("OK") } },
            title = { Text("Guia Rápido") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Text(helpAppText)
                }
            }
        )
    }
}

@Composable
fun RuleGroupCard(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Build, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(title, fontWeight = FontWeight.SemiBold)
                }
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    null
                )
            }
            if (expanded) {
                Spacer(Modifier.height(8.dp))
                content()
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
    )
}

@Composable
fun ModuleToggle(
    title: String,
    description: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clickable { onCheckedChange(!checked) },
        colors = CardDefaults.cardColors(
            containerColor = if (checked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
            contentColor = if (checked) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
        ),
        border = if (checked) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (checked) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha=0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = null // Handled by Card click
            )
        }
    }
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
