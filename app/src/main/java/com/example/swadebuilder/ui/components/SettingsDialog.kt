package com.example.swadebuilder.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.foundation.clickable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.IconButton
import androidx.compose.ui.text.style.TextAlign
import com.example.swadebuilder.toDiceString
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.CustomContentType
import com.example.swadebuilder.util.loadJsonAsset
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.model.Requisito
import com.example.swadebuilder.model.getActiveOrigins
import com.example.swadebuilder.model.getDisplayName
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.FeedbackController
import com.example.swadebuilder.TabStyle
import com.example.swadebuilder.ui.theme.AppTheme
import kotlin.math.roundToInt

import androidx.compose.material3.OutlinedButton

// Livro Básico: "Super Poderes (2+X)... o custo é 2 — pelo Antecedente Arcano
// (Super Poderes) — mais o custo do poder selecionado (X)." Muitos poderes do
// Compêndio de Super Poderes têm custo em escada por nível (ex.: "1/2/3/4/5"),
// então X aqui é o primeiro degrau (a compra mínima do poder).
private fun primeiroCustoSuperPoder(custoBase: String?): Int =
    custoBase
        ?.split("/")
        ?.firstOrNull()
        ?.trim()
        ?.replace('–', '-')
        ?.toIntOrNull()
        ?: 1

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    state: CriadorState,
    isHomeScreen: Boolean = false,
    isCreationPhase: Boolean = false,
    onDismiss: () -> Unit,
    persistPrefs: () -> Unit,
    feedbackController: FeedbackController,
    onResetRulesToDefaults: (() -> Unit)? = null,
    onThemeSelected: (AppTheme) -> Unit
) {
    var showNpcWarning by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    val themeNames = remember {
        mapOf(
            AppTheme.DEFAULT   to "Padrão",
            AppTheme.MEDIEVAL  to "Medieval",
            AppTheme.CYBERPUNK to "Cyberpunk",
            AppTheme.WW2       to "Segunda Guerra",
            AppTheme.HORROR    to "Horror",
            AppTheme.SCIFI     to "Sci-Fi",
            AppTheme.MINIMALIST to "Minimalista",
            AppTheme.HALLOWEEN to "Halloween"
        )
    }

    val themeDescriptions = remember {
        mapOf(
            AppTheme.DEFAULT   to "Pergaminho clássico (Old School)",
            AppTheme.MEDIEVAL  to "Manuscrito antigo e detalhes dourados",
            AppTheme.CYBERPUNK to "Estilo Matrix com linhas wireframe verdes",
            AppTheme.WW2       to "Papel Khaki e carimbo militar de campo",
            AppTheme.HORROR    to "Atmosfera gótica e detalhes carmesim",
            AppTheme.SCIFI     to "Interface holofuturista e azul estelar",
            AppTheme.MINIMALIST to "Design limpo e alto contraste",
            AppTheme.HALLOWEEN to "Laranja abóbora e roxo místico"
        )
    }

    val sortedThemes = remember(themeNames) {
        AppTheme.entries.sortedBy { themeNames[it] ?: it.name }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configurações", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Card "Interface do Sistema"
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Interface do Sistema",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Mensagens do Sistema", style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = state.showSystemMessages,
                                onCheckedChange = {
                                    state.showSystemMessages = it
                                    persistPrefs()
                                },
                                modifier = Modifier.scale(0.8f)
                            )
                        }

                        if (isHomeScreen) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Não solicitar escolha de regras", style = MaterialTheme.typography.bodyMedium)
                                    Text("Direto para criação com regras padrão.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(
                                    checked = state.pularSelecaoRegras,
                                    onCheckedChange = {
                                        state.pularSelecaoRegras = it
                                        persistPrefs()
                                        onResetRulesToDefaults?.invoke()
                                    },
                                    modifier = Modifier.scale(0.8f)
                                )
                            }
                        }

                        // NPC Mode Toggle (Only during creation phase and if not already NPC)
                        if (isCreationPhase && !state.modoProgressaoAtivo && !state.isNpcExibicao) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Modo Livre (NPC)", style = MaterialTheme.typography.bodyMedium)
                                    Text("Ignora custos e requisitos.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(
                                    checked = state.modoLivre,
                                    onCheckedChange = { if (it && !state.modoLivre) showNpcWarning = true },
                                    enabled = !state.modoLivre, // Irreversible
                                    modifier = Modifier.scale(0.8f)
                                )
                            }
                        }
                    }
                }

                // Card "Conteúdo Customizado"
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    var showCustomContentDialog by remember { mutableStateOf(false) }
                    var customItemName by remember { mutableStateOf("") }
                    var customItemDesc by remember { mutableStateOf("") }
                    var customPackageJson by remember { mutableStateOf("") }
                    var statusMessage by remember { mutableStateOf<String?>(null) }

                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Conteúdo Customizado",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Crie vantagens e itens caseiros com prefixo 'custom:'.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedButton(
                            onClick = {
                                customItemName = ""
                                customItemDesc = ""
                                customPackageJson = ""
                                statusMessage = null
                                showCustomContentDialog = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Gerenciar Conteúdo Customizado")
                        }
                    }

                    if (showCustomContentDialog) {
                        val context = androidx.compose.ui.platform.LocalContext.current
                        val customStorageManager = remember { com.example.swadebuilder.util.CustomStorageManager() }
                        val manager = remember { com.example.swadebuilder.util.CustomContentManager() }
                        val activeBookKey = remember(state) {
                            state.getActiveOrigins().firstOrNull() ?: "BASICO"
                        }
                        var selectedCategory by remember { mutableStateOf("Vantagem") }
                        var customRequirements by remember { mutableStateOf("") }
                        var customAdvCategory by remember { mutableStateOf(com.example.swadebuilder.model.Categoria.PROFISSIONAL) }
                        var customStage by remember { mutableStateOf("Novato") }
                        var customAttrMin by remember { mutableStateOf(mapOf<String, Int>()) }
                        var customSkillMin by remember { mutableStateOf(mapOf<String, Int>()) }
                        var customPrereqEdges by remember { mutableStateOf(listOf<String>()) }
                        var customPrereqComps by remember { mutableStateOf(listOf<String>()) }
                        var showAttrDialog by remember { mutableStateOf(false) }
                        var showSkillDialog by remember { mutableStateOf(false) }
                        var showEdgeDialog by remember { mutableStateOf(false) }
                        var showCompDialog by remember { mutableStateOf(false) }
                        var customSeverity by remember { mutableStateOf("Maior") }
                        var customEquipSuperType by remember { mutableStateOf("Arma") }
                        var customEquipSubtype by remember { mutableStateOf("Corpo a Corpo") }
                        var customCost by remember { mutableStateOf("0") }
                        var customWeight by remember { mutableStateOf("0") }
                        var customDamage by remember { mutableStateOf("") }
                        var customPp by remember { mutableStateOf("1") }
                        var customSuperPoderCustoBase by remember { mutableStateOf("2") }
                        // Um modificador por linha, no mesmo formato usado pelo catálogo oficial
                        // ("Nome (+custo): descrição"), ex.: "Área (+2): Modelo Médio de Explosão".
                        var customSuperPoderModificadores by remember { mutableStateOf("") }
                        var customRange by remember { mutableStateOf("Toque") }
                        var customDuration by remember { mutableStateOf("3 turnos") }
                        var customRacialTrait by remember { mutableStateOf("") }
                        var showJsonImportSection by remember { mutableStateOf(false) }
                        var refreshTrigger by remember { mutableStateOf(0) }

                        var customTraitCost by remember { mutableStateOf("1") }
                        var selectedRacialTraits by remember { mutableStateOf(listOf<com.example.swadebuilder.model.HabilidadeCriacao>()) }
                        var showTraitSelectDialog by remember { mutableStateOf(false) }

                        // Estado da Variante de Raça custom (ver ResolveVariantPointBudgetUseCase / CustomAncestryVariant).
                        var varianteBaseRacaId by remember { mutableStateOf<String?>(null) }
                        var showVarianteBaseRacaDialog by remember { mutableStateOf(false) }
                        var varianteTracosRemovidos by remember { mutableStateOf(listOf<String>()) }
                        var varianteVantagensGratisRemovidas by remember { mutableStateOf(listOf<String>()) }
                        var varianteDesvantagensRemovidas by remember { mutableStateOf(listOf<String>()) }
                        var varianteTracosAdicionados by remember { mutableStateOf(listOf<com.example.swadebuilder.model.HabilidadeCriacao>()) }
                        var showVarianteTraitAddDialog by remember { mutableStateOf(false) }
                        var varianteVantagensAdicionadas by remember { mutableStateOf(listOf<String>()) }
                        var showVarianteVantagemAddDialog by remember { mutableStateOf(false) }
                        var varianteComplicacoesAdicionadas by remember { mutableStateOf(listOf<com.example.swadebuilder.model.CustomVariantComplicacaoEscolhida>()) }
                        var showVarianteComplicacaoSeveridadeDialog by remember { mutableStateOf(false) }
                        var showVarianteComplicacaoPickDialog by remember { mutableStateOf(false) }
                        var varianteComplicacaoComoMaiorEscolhido by remember { mutableStateOf(false) }
                        var varianteSemLimite by remember { mutableStateOf(false) }

                        val baseRacialCatalog: List<com.example.swadebuilder.model.HabilidadeCriacao> = remember {
                            runCatching {
                                context.loadJsonAsset<List<com.example.swadebuilder.model.HabilidadeCriacao>>("basico_habilidades_raciais.json")
                            }.getOrElse { emptyList() }.map { it.exibida() }
                        }

                        // "Super Poderes (2+X)": o traço racial em si custa 2 pontos, mais o
                        // custo do Super Poder do Compêndio de Super Poderes escolhido pelo
                        // Mestre (X). Como X varia por poder, a entrada de HabilidadeCriacao
                        // final é montada dinamicamente (ver superPoderRacialPickerTarget),
                        // não é um custo fixo de catálogo como os outros traços.
                        val superPoderesCatalog: List<com.example.swadebuilder.model.SuperPoder> = remember {
                            runCatching {
                                context.loadJsonAsset<List<com.example.swadebuilder.model.SuperPoder>>("super_poderes.json")
                            }.getOrElse { emptyList() }
                        }
                        var superPoderRacialPickerTarget by remember {
                            mutableStateOf<((com.example.swadebuilder.model.HabilidadeCriacao) -> Unit)?>(null)
                        }

                        val categories = remember(isHomeScreen) {
                            if (isHomeScreen) {
                                listOf("Vantagem", "Complicação", "Poder", "Super Poder", "Raça", "Traço Racial", "Variante de Raça")
                            } else {
                                listOf("Vantagem", "Complicação", "Equipamento", "Poder", "Super Poder", "Raça", "Traço Racial", "Variante de Raça")
                            }
                        }
                        if (selectedCategory !in categories) {
                            selectedCategory = categories.first()
                        }
                        val activeBookCustomData = remember(activeBookKey, refreshTrigger) {
                            customStorageManager.loadCustomContent(context, activeBookKey)
                        }

                        AlertDialog(
                            onDismissRequest = { showCustomContentDialog = false },
                            title = { Text("Criar Conteúdo Customizado", style = MaterialTheme.typography.titleMedium) },
                            text = {
                                Column(
                                    modifier = Modifier
                                        .verticalScroll(rememberScrollState())
                                        .fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "O que você deseja criar?",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    // Category selector horizontal carousel (styled like superpower carousel with smooth edge gradient)
                                    Box(
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        androidx.compose.foundation.lazy.LazyRow(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(end = 24.dp)
                                        ) {
                                            items(categories.size) { index ->
                                                val cat = categories[index]
                                                val isSel = selectedCategory == cat
                                                androidx.compose.material3.FilterChip(
                                                    selected = isSel,
                                                    onClick = { selectedCategory = cat },
                                                    label = {
                                                        Text(
                                                            text = cat,
                                                            style = MaterialTheme.typography.labelMedium,
                                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                                        )
                                                    },
                                                    colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                )
                                            }
                                        }

                                        // Edge gradient fade
                                        Box(
                                            modifier = Modifier
                                                .width(20.dp)
                                                .align(Alignment.CenterEnd)
                                                .fillMaxHeight()
                                                .background(
                                                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                                                        colors = listOf(
                                                            androidx.compose.ui.graphics.Color.Transparent,
                                                            MaterialTheme.colorScheme.surfaceContainerHigh
                                                        )
                                                    )
                                                )
                                        )
                                    }

                                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

                                    // Container card for form elements to prevent overlapping and maintain clean spacing
                                    androidx.compose.material3.Surface(
                                        shape = MaterialTheme.shapes.medium,
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            // Common Name field
                                            androidx.compose.material3.OutlinedTextField(
                                                value = customItemName,
                                                onValueChange = { customItemName = it },
                                                label = { Text("Nome da $selectedCategory") },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth()
                                            )

                                            // Category-specific fields
                                            when (selectedCategory) {
                                                "Vantagem" -> {
                                                    // Modular Requirements Section
                                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                        Text("Requisitos Modulares da Vantagem:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)

                                                        // Summary of configured requirements
                                                        val reqSummary = buildList {
                                                            if (customStage.isNotBlank()) add("Estágio: $customStage")
                                                            if (customAttrMin.isNotEmpty()) add("Atributos: " + customAttrMin.entries.joinToString { "${it.key} d${it.value}" })
                                                            if (customSkillMin.isNotEmpty()) add("Perícias: " + customSkillMin.entries.joinToString { "${it.key} d${it.value}" })
                                                            if (customPrereqEdges.isNotEmpty()) add("Vantagens Prévias: ${customPrereqEdges.size} selecionada(s)")
                                                            if (customPrereqComps.isNotEmpty()) add("Complicações: ${customPrereqComps.size} selecionada(s)")
                                                        }
                                                        if (reqSummary.isNotEmpty()) {
                                                            Text(
                                                                text = reqSummary.joinToString(" | "),
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                        }

                                                        // Interactive Selector Buttons
                                                        @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
                                                        androidx.compose.foundation.layout.FlowRow(
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                                        ) {
                                                            OutlinedButton(onClick = { showAttrDialog = true }) {
                                                                Text(if (customAttrMin.isEmpty()) "+ Atributos" else "Atributos (${customAttrMin.size})", style = MaterialTheme.typography.labelSmall)
                                                            }
                                                            OutlinedButton(onClick = { showSkillDialog = true }) {
                                                                Text(if (customSkillMin.isEmpty()) "+ Perícias" else "Perícias (${customSkillMin.size})", style = MaterialTheme.typography.labelSmall)
                                                            }
                                                            OutlinedButton(onClick = { showEdgeDialog = true }) {
                                                                Text(if (customPrereqEdges.isEmpty()) "+ Vantagens Prévias" else "Vantagens (${customPrereqEdges.size})", style = MaterialTheme.typography.labelSmall)
                                                            }
                                                            OutlinedButton(onClick = { showCompDialog = true }) {
                                                                Text(if (customPrereqComps.isEmpty()) "+ Complicações" else "Complicações (${customPrereqComps.size})", style = MaterialTheme.typography.labelSmall)
                                                            }
                                                        }
                                                    }

                                                    androidx.compose.material3.OutlinedTextField(
                                                        value = customRequirements,
                                                        onValueChange = { customRequirements = it },
                                                        label = { Text("Outros Requisitos (texto livre)") },
                                                        singleLine = true,
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                    val availableAdvCategories = remember(state.listaVantagens, activeBookKey, state.compendioArteDaGuerraAtivo, state.compendioPathfinderAtivo, state.compendioDeadlandsAtivo, state.compendioHorrorAtivo, state.modoMonstroAtivo, state.modoSupers) {
                                                        val baseCategories = mutableSetOf(
                                                            Categoria.ANTECEDENTE,
                                                            Categoria.COMBATE,
                                                            Categoria.ESTRANHAS,
                                                            Categoria.LENDARIAS,
                                                            Categoria.LIDERANCA,
                                                            Categoria.PODER,
                                                            Categoria.PROFISSIONAL,
                                                            Categoria.SOCIAIS
                                                        )
                                                        if (state.listaVantagens.isNotEmpty()) {
                                                            baseCategories.addAll(state.listaVantagens.map { it.categoria })
                                                        }
                                                        if (activeBookKey == "ARTE_DA_GUERRA" || state.compendioArteDaGuerraAtivo) {
                                                            baseCategories.addAll(listOf(Categoria.CHI, Categoria.TROPO, Categoria.ESTILO_MARCIAL))
                                                        }
                                                        if (activeBookKey == "PATHFINDER" || state.compendioPathfinderAtivo) {
                                                            baseCategories.addAll(listOf(Categoria.CLASSE, Categoria.VANTAGEM_DE_CLASSE, Categoria.PRESTIGIO, Categoria.ANCESTRALIDADE))
                                                        }
                                                        if (activeBookKey == "DEADLANDS" || state.compendioDeadlandsAtivo) {
                                                            baseCategories.addAll(listOf(Categoria.ATORMENTADO, Categoria.ANCESTRALIDADE))
                                                        }
                                                        if (activeBookKey == "HORROR" || state.compendioHorrorAtivo || state.modoMonstroAtivo) {
                                                            baseCategories.add(Categoria.MONSTRUOSAS)
                                                        }
                                                        if (state.modoSupers) {
                                                            baseCategories.add(Categoria.SUPER)
                                                        }
                                                        Categoria.entries.filter { it in baseCategories }
                                                    }

                                                    if (customAdvCategory !in availableAdvCategories) {
                                                        customAdvCategory = availableAdvCategories.firstOrNull() ?: Categoria.PROFISSIONAL
                                                    }

                                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                        Text("Categoria da Vantagem:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                                        @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
                                                        androidx.compose.foundation.layout.FlowRow(
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                                        ) {
                                                            availableAdvCategories.forEach { catEnum ->
                                                                androidx.compose.material3.FilterChip(
                                                                    selected = customAdvCategory == catEnum,
                                                                    onClick = { customAdvCategory = catEnum },
                                                                    label = { Text(catEnum.getDisplayName(), style = MaterialTheme.typography.labelSmall) }
                                                                )
                                                            }
                                                        }
                                                    }
                                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                        Text("Estágio Mínimo:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                                        @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
                                                        androidx.compose.foundation.layout.FlowRow(
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                                        ) {
                                                            listOf("Novato", "Experiente", "Veterano", "Heroico", "Lendário").forEach { stage ->
                                                                androidx.compose.material3.FilterChip(
                                                                    selected = customStage == stage,
                                                                    onClick = { customStage = stage },
                                                                    label = { Text(stage, style = MaterialTheme.typography.labelSmall) }
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                                "Complicação" -> {
                                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                        Text("Severidade Permitida:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                            listOf("Maior", "Menor", "Maior ou Menor").forEach { sev ->
                                                                androidx.compose.material3.FilterChip(
                                                                    selected = customSeverity == sev,
                                                                    onClick = { customSeverity = sev },
                                                                    label = { Text(sev, style = MaterialTheme.typography.labelSmall) }
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                                "Equipamento" -> {
                                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                        Text("Tipo de Equipamento:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                            listOf("Arma", "Armadura", "Escudo", "Geral", "Veículo").forEach { st ->
                                                                androidx.compose.material3.FilterChip(
                                                                    selected = customEquipSuperType == st,
                                                                    onClick = {
                                                                        customEquipSuperType = st
                                                                        customEquipSubtype = when (st) {
                                                                            "Arma" -> "Corpo a Corpo"
                                                                            "Armadura" -> "Armadura Corporal"
                                                                            "Escudo" -> "Escudo"
                                                                            "Veículo" -> "Veículo"
                                                                            else -> "Equipamento Geral"
                                                                        }
                                                                    },
                                                                    label = { Text(st, style = MaterialTheme.typography.labelSmall) }
                                                                )
                                                            }
                                                        }
                                                    }
                                                    if (customEquipSuperType == "Arma") {
                                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                            Text("Subtipo de Arma:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                                listOf("Corpo a Corpo", "Ataque a Distância", "Futurista").forEach { sub ->
                                                                    androidx.compose.material3.FilterChip(
                                                                        selected = customEquipSubtype == sub,
                                                                        onClick = { customEquipSubtype = sub },
                                                                        label = { Text(sub, style = MaterialTheme.typography.labelSmall) }
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        androidx.compose.material3.OutlinedTextField(
                                                            value = customCost,
                                                            onValueChange = { customCost = it },
                                                            label = { Text("Custo ($)") },
                                                            singleLine = true,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                        androidx.compose.material3.OutlinedTextField(
                                                            value = customWeight,
                                                            onValueChange = { customWeight = it },
                                                            label = { Text("Peso (kg)") },
                                                            singleLine = true,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                    }
                                                    androidx.compose.material3.OutlinedTextField(
                                                        value = customDamage,
                                                        onValueChange = { customDamage = it },
                                                        label = { Text("Dano / Armadura / Efeito (ex: For+d12+5)") },
                                                        singleLine = true,
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                }
                                                "Poder" -> {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        androidx.compose.material3.OutlinedTextField(
                                                            value = customPp,
                                                            onValueChange = { customPp = it },
                                                            label = { Text("Pontos de Poder") },
                                                            singleLine = true,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                        androidx.compose.material3.OutlinedTextField(
                                                            value = customRange,
                                                            onValueChange = { customRange = it },
                                                            label = { Text("Alcance") },
                                                            singleLine = true,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                    }
                                                    androidx.compose.material3.OutlinedTextField(
                                                        value = customDuration,
                                                        onValueChange = { customDuration = it },
                                                        label = { Text("Duração (ex: 3 turnos)") },
                                                        singleLine = true,
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                }
                                                "Super Poder" -> {
                                                    androidx.compose.material3.OutlinedTextField(
                                                        value = customSuperPoderCustoBase,
                                                        onValueChange = { customSuperPoderCustoBase = it },
                                                        label = { Text("Custo Base (ex: 2 ou 1/2/3/4/5)") },
                                                        singleLine = true,
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                    androidx.compose.material3.OutlinedTextField(
                                                        value = customSuperPoderModificadores,
                                                        onValueChange = { customSuperPoderModificadores = it },
                                                        label = { Text("Modificadores (1 por linha, ex: Área (+2): Modelo Médio de Explosão)") },
                                                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                                    )
                                                }
                                                "Raça" -> {
                                                    val netRacePoints = selectedRacialTraits.sumOf { it.custo }
                                                    val pointColor = if (netRacePoints == 2) MaterialTheme.colorScheme.primary else if (netRacePoints < 2) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error

                                                    OutlinedCard(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        colors = CardDefaults.outlinedCardColors(
                                                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                                        )
                                                    ) {
                                                        Column(
                                                            modifier = Modifier.padding(12.dp),
                                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                                        ) {
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Column {
                                                                    Text(
                                                                        text = "Traços Raciais",
                                                                        style = MaterialTheme.typography.titleSmall,
                                                                        fontWeight = FontWeight.Bold
                                                                    )
                                                                    Text(
                                                                        text = "Pontos: $netRacePoints / 2",
                                                                        style = MaterialTheme.typography.labelSmall,
                                                                        fontWeight = FontWeight.SemiBold,
                                                                        color = pointColor
                                                                    )
                                                                }
                                                                FilledTonalIconButton(
                                                                    onClick = { showTraitSelectDialog = true },
                                                                    modifier = Modifier.size(32.dp)
                                                                ) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.Add,
                                                                        contentDescription = "Adicionar Traço",
                                                                        modifier = Modifier.size(18.dp)
                                                                    )
                                                                }
                                                            }

                                                            if (selectedRacialTraits.isEmpty()) {
                                                                Text(
                                                                    text = "Nenhum traço racial adicionado. O padrão de criação de raças busca fechar em +2 pontos.",
                                                                    style = MaterialTheme.typography.bodySmall,
                                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                                )
                                                            } else {
                                                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                                    selectedRacialTraits.forEach { trait ->
                                                                        Row(
                                                                            modifier = Modifier.fillMaxWidth(),
                                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                                            verticalAlignment = Alignment.CenterVertically
                                                                        ) {
                                                                            Text(
                                                                                text = "• ${trait.nome} (${if (trait.custo > 0) "+${trait.custo}" else "${trait.custo}"} pts)",
                                                                                style = MaterialTheme.typography.bodySmall,
                                                                                fontWeight = FontWeight.Medium,
                                                                                modifier = Modifier.weight(1f)
                                                                            )
                                                                            TextButton(
                                                                                onClick = { selectedRacialTraits = selectedRacialTraits - trait },
                                                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                                                            ) {
                                                                                Text("Remover", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                                "Traço Racial" -> {
                                                    androidx.compose.material3.OutlinedTextField(
                                                        value = customTraitCost,
                                                        onValueChange = { customTraitCost = it },
                                                        label = { Text("Custo em Pontos (ex: 2 para positivo, -1 para negativo)") },
                                                        singleLine = true,
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                }
                                                "Variante de Raça" -> {
                                                    val baseRacaOptions = remember(state.listaAncestralidadesJson) {
                                                        state.listaAncestralidadesJson.distinctBy { it.nome.keyify() }.sortedBy { it.nome }
                                                    }
                                                    val varianteBaseRaca = remember(varianteBaseRacaId, baseRacaOptions) {
                                                        baseRacaOptions.firstOrNull { it.nome.keyify() == varianteBaseRacaId }
                                                    }

                                                    OutlinedButton(
                                                        onClick = { showVarianteBaseRacaDialog = true },
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Text(varianteBaseRaca?.nome ?: "Selecionar Raça Base")
                                                    }

                                                    if (varianteBaseRaca == null) {
                                                        Text(
                                                            text = "Escolha a raça base pra começar a montar a Variante.",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    } else {
                                                        val itensRemoviveis = remember(varianteBaseRaca) {
                                                            com.example.swadebuilder.model.usecase.ResolveVariantPointBudgetUseCase.itensRemoviveisDe(varianteBaseRaca)
                                                        }
                                                        val nHab = varianteBaseRaca.habilidades.size
                                                        val nVant = varianteBaseRaca.vantagensGratis.size
                                                        val habilidadeItems = itensRemoviveis.take(nHab).filter { it.habilidadeId != null }
                                                        val vantagemGratisItems = itensRemoviveis.drop(nHab).take(nVant)
                                                        val desvantagemItems = itensRemoviveis.drop(nHab + nVant)

                                                        val itensRemovidosSelecionados = habilidadeItems.filter { it.habilidadeId in varianteTracosRemovidos } +
                                                            vantagemGratisItems.filter { it.label in varianteVantagensGratisRemovidas } +
                                                            desvantagemItems.filter { it.label in varianteDesvantagensRemovidas }

                                                        val itensAdicionadosSelecionados = buildList {
                                                            varianteTracosAdicionados.forEach { trait ->
                                                                add(com.example.swadebuilder.model.usecase.VariantBudgetItem(label = trait.nome, custo = trait.custo, habilidadeId = trait.nome.lowercase().replace(" ", "_")))
                                                            }
                                                            varianteVantagensAdicionadas.forEach { vid ->
                                                                state.listaVantagens.firstOrNull { it.id == vid }?.let {
                                                                    add(com.example.swadebuilder.model.usecase.ResolveVariantPointBudgetUseCase.vantagemComoItemAdicionado(it))
                                                                }
                                                            }
                                                            varianteComplicacoesAdicionadas.forEach { esc ->
                                                                state.listaComplicacoes.firstOrNull { it.id == esc.complicacaoId }?.let {
                                                                    add(com.example.swadebuilder.model.usecase.ResolveVariantPointBudgetUseCase.complicacaoComoItemAdicionado(it, esc.comoMaior))
                                                                }
                                                            }
                                                        }

                                                        val budgetResult = remember(itensRemovidosSelecionados, itensAdicionadosSelecionados, varianteSemLimite) {
                                                            com.example.swadebuilder.model.usecase.ResolveVariantPointBudgetUseCase().resolve(
                                                                itensRemovidosSelecionados, itensAdicionadosSelecionados, semLimite = varianteSemLimite
                                                            )
                                                        }

                                                        OutlinedCard(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                                                        ) {
                                                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                                val saldoColor = if (budgetResult.dentroDoOrcamento) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                                                Text(
                                                                    text = if (varianteSemLimite) "Saldo: ${budgetResult.saldo} (sem limite)" else "Saldo: ${budgetResult.saldo} / ±${budgetResult.orcamento}",
                                                                    style = MaterialTheme.typography.labelSmall,
                                                                    fontWeight = FontWeight.SemiBold,
                                                                    color = saldoColor
                                                                )
                                                                Row(
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    verticalAlignment = Alignment.CenterVertically
                                                                ) {
                                                                    Checkbox(checked = varianteSemLimite, onCheckedChange = { varianteSemLimite = it })
                                                                    Spacer(Modifier.width(4.dp))
                                                                    Text("Sem limite de pontos (raças mais fortes)", style = MaterialTheme.typography.labelSmall)
                                                                }

                                                                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                                                                Text("Remover da raça base:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

                                                                if (habilidadeItems.isEmpty() && vantagemGratisItems.isEmpty() && desvantagemItems.isEmpty()) {
                                                                    Text("Esta raça não tem traços removíveis.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                                } else {
                                                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                                        habilidadeItems.forEach { item ->
                                                                            val isSel = item.habilidadeId in varianteTracosRemovidos
                                                                            Row(
                                                                                modifier = Modifier.fillMaxWidth().clickable {
                                                                                    varianteTracosRemovidos = if (isSel) varianteTracosRemovidos - item.habilidadeId!! else varianteTracosRemovidos + item.habilidadeId!!
                                                                                },
                                                                                verticalAlignment = Alignment.CenterVertically
                                                                            ) {
                                                                                Checkbox(checked = isSel, onCheckedChange = {
                                                                                    varianteTracosRemovidos = if (it) varianteTracosRemovidos + item.habilidadeId!! else varianteTracosRemovidos - item.habilidadeId!!
                                                                                })
                                                                                Text("${item.label} (${item.custo})", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                                                            }
                                                                        }
                                                                        vantagemGratisItems.forEach { item ->
                                                                            val isSel = item.label in varianteVantagensGratisRemovidas
                                                                            Row(
                                                                                modifier = Modifier.fillMaxWidth().clickable {
                                                                                    varianteVantagensGratisRemovidas = if (isSel) varianteVantagensGratisRemovidas - item.label else varianteVantagensGratisRemovidas + item.label
                                                                                },
                                                                                verticalAlignment = Alignment.CenterVertically
                                                                            ) {
                                                                                Checkbox(checked = isSel, onCheckedChange = {
                                                                                    varianteVantagensGratisRemovidas = if (it) varianteVantagensGratisRemovidas + item.label else varianteVantagensGratisRemovidas - item.label
                                                                                })
                                                                                Text("${item.label} (${item.custo})", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                                                            }
                                                                        }
                                                                        desvantagemItems.forEach { item ->
                                                                            val isSel = item.label in varianteDesvantagensRemovidas
                                                                            Row(
                                                                                modifier = Modifier.fillMaxWidth().clickable {
                                                                                    varianteDesvantagensRemovidas = if (isSel) varianteDesvantagensRemovidas - item.label else varianteDesvantagensRemovidas + item.label
                                                                                },
                                                                                verticalAlignment = Alignment.CenterVertically
                                                                            ) {
                                                                                Checkbox(checked = isSel, onCheckedChange = {
                                                                                    varianteDesvantagensRemovidas = if (it) varianteDesvantagensRemovidas + item.label else varianteDesvantagensRemovidas - item.label
                                                                                })
                                                                                Text("${item.label} (${item.custo})", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                                                            }
                                                                        }
                                                                    }
                                                                }

                                                                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                                                                Text("Adicionar à Variante:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

                                                                @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
                                                                androidx.compose.foundation.layout.FlowRow(
                                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                                                ) {
                                                                    OutlinedButton(onClick = { showVarianteTraitAddDialog = true }) {
                                                                        Text("+ Traço Racial", style = MaterialTheme.typography.labelSmall)
                                                                    }
                                                                    OutlinedButton(onClick = { showVarianteVantagemAddDialog = true }) {
                                                                        Text("+ Vantagem", style = MaterialTheme.typography.labelSmall)
                                                                    }
                                                                    OutlinedButton(onClick = { showVarianteComplicacaoSeveridadeDialog = true }) {
                                                                        Text("+ Complicação", style = MaterialTheme.typography.labelSmall)
                                                                    }
                                                                }

                                                                if (itensAdicionadosSelecionados.isNotEmpty()) {
                                                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                                        varianteTracosAdicionados.forEach { trait ->
                                                                            Row(
                                                                                modifier = Modifier.fillMaxWidth(),
                                                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                                                verticalAlignment = Alignment.CenterVertically
                                                                            ) {
                                                                                Text("• ${trait.nome} (${if (trait.custo > 0) "+${trait.custo}" else "${trait.custo}"})", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                                                                TextButton(onClick = { varianteTracosAdicionados = varianteTracosAdicionados - trait }, contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)) {
                                                                                    Text("Remover", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                                                                                }
                                                                            }
                                                                        }
                                                                        varianteVantagensAdicionadas.forEach { vid ->
                                                                            val vant = state.listaVantagens.firstOrNull { it.id == vid }
                                                                            Row(
                                                                                modifier = Modifier.fillMaxWidth(),
                                                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                                                verticalAlignment = Alignment.CenterVertically
                                                                            ) {
                                                                                Text("• ${vant?.nome ?: vid}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                                                                TextButton(onClick = { varianteVantagensAdicionadas = varianteVantagensAdicionadas - vid }, contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)) {
                                                                                    Text("Remover", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                                                                                }
                                                                            }
                                                                        }
                                                                        varianteComplicacoesAdicionadas.forEach { esc ->
                                                                            val comp = state.listaComplicacoes.firstOrNull { it.id == esc.complicacaoId }
                                                                            Row(
                                                                                modifier = Modifier.fillMaxWidth(),
                                                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                                                verticalAlignment = Alignment.CenterVertically
                                                                            ) {
                                                                                Text("• ${comp?.name ?: esc.complicacaoId} (${if (esc.comoMaior) "Maior" else "Menor"})", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                                                                TextButton(onClick = { varianteComplicacoesAdicionadas = varianteComplicacoesAdicionadas - esc }, contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)) {
                                                                                    Text("Remover", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            // Common Description field
                                            androidx.compose.material3.OutlinedTextField(
                                                value = customItemDesc,
                                                onValueChange = { customItemDesc = it },
                                                label = { Text("Descrição / Efeitos") },
                                                modifier = Modifier.fillMaxWidth().height(90.dp),
                                                maxLines = 4
                                            )
                                        }
                                    }

                                    // Collapsible Advanced JSON Import section
                                    TextButton(onClick = { showJsonImportSection = !showJsonImportSection }) {
                                        Text(if (showJsonImportSection) "▼ Ocultar Importação JSON" else "▶ Avançado: Importar Pacote JSON")
                                    }

                                    if (showJsonImportSection) {
                                        androidx.compose.material3.OutlinedTextField(
                                            value = customPackageJson,
                                            onValueChange = { customPackageJson = it },
                                            label = { Text("Cole o JSON do pacote") },
                                            modifier = Modifier.fillMaxWidth().height(90.dp)
                                        )
                                    }

                                    // Custom Content Items Manager List for the active book
                                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                                    Text(
                                        text = "Itens Customizados do Livro ($activeBookKey)",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )

                                    val allCustomItems = remember(activeBookCustomData, isHomeScreen) {
                                        buildList {
                                            activeBookCustomData.vantagens.forEach { add("Vantagem" to it.nome) }
                                            activeBookCustomData.complicacoes.forEach { add("Complicação" to it.name) }
                                            if (!isHomeScreen) {
                                                activeBookCustomData.equipamentos.forEach { add("Equipamento" to it.nome) }
                                            }
                                            activeBookCustomData.poderes.forEach { add("Poder" to it.nome) }
                                            activeBookCustomData.superPoderes.forEach { add("Super Poder" to it.nome) }
                                            activeBookCustomData.racas.forEach { add("Raça" to it.nome) }
                                            activeBookCustomData.habilidadesRaciais.forEach { add("Traço Racial" to it.nome) }
                                            activeBookCustomData.variantesRaciais.forEach { add("Variante de Raça" to it.nome) }
                                        }
                                    }

                                    if (allCustomItems.isEmpty()) {
                                        Text(
                                            text = "Nenhum item customizado criado neste livro ainda.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    } else {
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            allCustomItems.forEach { (type, name) ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "[$type] $name ⓒ",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                    TextButton(onClick = {
                                                        when (type) {
                                                            "Vantagem" -> {
                                                                val item = activeBookCustomData.vantagens.firstOrNull { it.nome == name }
                                                                item?.let { customStorageManager.deleteVantagem(context, activeBookKey, it.id) }
                                                            }
                                                            "Complicação" -> {
                                                                val item = activeBookCustomData.complicacoes.firstOrNull { it.name == name }
                                                                item?.let { customStorageManager.deleteComplicacao(context, activeBookKey, it.id) }
                                                            }
                                                            "Equipamento" -> customStorageManager.deleteEquipamento(context, activeBookKey, name)
                                                            "Poder" -> {
                                                                val item = activeBookCustomData.poderes.firstOrNull { it.nome == name }
                                                                item?.let { customStorageManager.deletePoder(context, activeBookKey, it.id) }
                                                            }
                                                            "Super Poder" -> customStorageManager.deleteSuperPoder(context, activeBookKey, name)
                                                            "Raça" -> customStorageManager.deleteRaca(context, activeBookKey, name)
                                                            "Traço Racial" -> customStorageManager.deleteHabilidadeRacial(context, activeBookKey, name)
                                                            "Variante de Raça" -> {
                                                                val item = activeBookCustomData.variantesRaciais.firstOrNull { it.nome == name }
                                                                item?.let {
                                                                    customStorageManager.deleteVarianteRacial(context, activeBookKey, it.id)
                                                                    state.listaVariantesRaciaisCustom = state.listaVariantesRaciaisCustom.filterNot { v -> v.id == it.id }
                                                                }
                                                            }
                                                        }
                                                        refreshTrigger++
                                                    }) {
                                                        Text("Deletar", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    if (statusMessage != null) {
                                        Text(
                                            text = statusMessage!!,
                                            color = MaterialTheme.colorScheme.primary,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            },
                            confirmButton = {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    TextButton(onClick = {
                                                val safeDesc = customItemDesc.ifBlank { "-" }
                                                if (customItemName.isNotBlank()) {
                                            val id = "custom:${customItemName.lowercase().replace(" ", "_")}"
                                            when (selectedCategory) {
                                                "Vantagem" -> {
                                                    val combinedPrevEdges = customPrereqEdges + customPrereqComps
                                                    val reqObj = Requisito(
                                                        estagio = customStage,
                                                        atributoMin = customAttrMin,
                                                        periciaMin = customSkillMin,
                                                        vantagensPrevias = combinedPrevEdges,
                                                        observacoes = customRequirements
                                                    )
                                                    val newAdv = com.example.swadebuilder.model.Vantagem(
                                                        id = id,
                                                        nome = customItemName,
                                                        categoria = customAdvCategory,
                                                        descricao = safeDesc,
                                                        origem = activeBookKey,
                                                        requisitos = reqObj
                                                    )
                                                    customStorageManager.addVantagem(context, activeBookKey, newAdv)
                                                    state.addCustomVantagem(newAdv)
                                                            statusMessage = "Vantagem '$customItemName' salva no livro $activeBookKey!"
                                                }
                                                "Complicação" -> {
                                                    val newComp = com.example.swadebuilder.model.Complicacao(
                                                        id = id,
                                                        name = customItemName,
                                                        severity = customSeverity,
                                                                description = safeDesc,
                                                                origem = activeBookKey
                                                    )
                                                            customStorageManager.addComplicacao(context, activeBookKey, newComp)
                                                    state.addCustomComplicacao(newComp)
                                                            statusMessage = "Complicação '$customItemName' salva no livro $activeBookKey!"
                                                }
                                                "Equipamento" -> {
                                                    val newEquip = com.example.swadebuilder.model.EquipamentoItem(
                                                        nome = customItemName,
                                                        custo = kotlinx.serialization.json.JsonPrimitive(customCost.toIntOrNull() ?: 0),
                                                        peso = kotlinx.serialization.json.JsonPrimitive(customWeight.toFloatOrNull() ?: 0f),
                                                        dano = if (customDamage.isNotBlank()) kotlinx.serialization.json.JsonPrimitive(customDamage) else null,
                                                                observacoes = kotlinx.serialization.json.JsonPrimitive(safeDesc),
                                                                origem = activeBookKey,
                                                                subtipo = customEquipSubtype
                                                    )
                                                            customStorageManager.addEquipamento(context, activeBookKey, newEquip)
                                                    state.addCustomEquipamento(newEquip)
                                                            statusMessage = "Equipamento '$customItemName' salvo no livro $activeBookKey!"
                                                }
                                                "Poder" -> {
                                                    val newPoder = com.example.swadebuilder.model.Poder(
                                                        id = id,
                                                        nome = customItemName,
                                                        pontosDePoder = customPp.ifBlank { "1" },
                                                        distancia = customRange.ifBlank { "Toque" },
                                                        duracao = customDuration.ifBlank { "3 turnos" },
                                                                descricao = safeDesc,
                                                        estagio = "Novato",
                                                                origem = activeBookKey
                                                    )
                                                            customStorageManager.addPoder(context, activeBookKey, newPoder)
                                                    state.addCustomPoder(newPoder)
                                                            statusMessage = "Poder '$customItemName' salvo no livro $activeBookKey!"
                                                }
                                                "Super Poder" -> {
                                                    val modificadoresList = customSuperPoderModificadores
                                                        .lines()
                                                        .map { it.trim() }
                                                        .filter { it.isNotBlank() }
                                                    val newSuperPoder = com.example.swadebuilder.model.SuperPoder(
                                                        nome = customItemName,
                                                        custoBase = customSuperPoderCustoBase.ifBlank { "2" },
                                                        descricao = safeDesc,
                                                        modificadores = modificadoresList.ifEmpty { null }
                                                    )
                                                    customStorageManager.addSuperPoder(context, activeBookKey, newSuperPoder)
                                                    state.addCustomSuperPoder(newSuperPoder)
                                                    statusMessage = "Super Poder '$customItemName' salvo no livro $activeBookKey!"
                                                }
                                                "Raça" -> {
                                                    val raceAbilities = if (selectedRacialTraits.isNotEmpty()) {
                                                        selectedRacialTraits.map { trait ->
                                                            com.example.swadebuilder.model.RacialAbility(
                                                                nome = trait.nome,
                                                                descricao = trait.descricao,
                                                                id = trait.nome.lowercase().replace(" ", "_"),
                                                                category = if (trait.custo >= 0) "racial_trait_positive" else "racial_trait_negative"
                                                            )
                                                        }
                                                    } else {
                                                        listOf(
                                                            com.example.swadebuilder.model.RacialAbility(
                                                                nome = "Traço Customizado",
                                                                descricao = safeDesc,
                                                                id = "${id}_trait",
                                                                category = "racial_trait_positive"
                                                            )
                                                        )
                                                    }
                                                    val newRace = com.example.swadebuilder.model.RacialModifier(
                                                        nome = customItemName,
                                                        descricao = safeDesc,
                                                        atributos = emptyMap(),
                                                        pericias = emptyMap(),
                                                        origem = activeBookKey,
                                                        habilidades = raceAbilities
                                                    )
                                                    customStorageManager.addRaca(context, activeBookKey, newRace)
                                                    state.listaAncestralidadesJson = state.listaAncestralidadesJson + newRace
                                                    statusMessage = "Raça '$customItemName' salva no livro $activeBookKey!"
                                                }
                                                "Traço Racial" -> {
                                                    val costInt = customTraitCost.toIntOrNull() ?: 1
                                                    val newTrait = com.example.swadebuilder.model.HabilidadeCriacao(
                                                        nome = customItemName,
                                                        custo = costInt,
                                                        descricao = safeDesc
                                                    )
                                                    customStorageManager.addHabilidadeRacial(context, activeBookKey, newTrait)
                                                    statusMessage = "Traço racial '$customItemName' salvo no livro $activeBookKey!"
                                                }
                                                "Variante de Raça" -> {
                                                    val baseRaca = varianteBaseRacaId?.let { bid -> state.listaAncestralidadesJson.firstOrNull { it.nome.keyify() == bid } }
                                                    if (baseRaca == null) {
                                                        statusMessage = "Selecione a raça base da Variante."
                                                    } else {
                                                        val itensRemoviveis = com.example.swadebuilder.model.usecase.ResolveVariantPointBudgetUseCase.itensRemoviveisDe(baseRaca)
                                                        val nHab = baseRaca.habilidades.size
                                                        val nVant = baseRaca.vantagensGratis.size
                                                        val habilidadeItems = itensRemoviveis.take(nHab).filter { it.habilidadeId != null }
                                                        val vantagemGratisItems = itensRemoviveis.drop(nHab).take(nVant)
                                                        val desvantagemItems = itensRemoviveis.drop(nHab + nVant)

                                                        val itensRemovidosSelecionados = habilidadeItems.filter { it.habilidadeId in varianteTracosRemovidos } +
                                                            vantagemGratisItems.filter { it.label in varianteVantagensGratisRemovidas } +
                                                            desvantagemItems.filter { it.label in varianteDesvantagensRemovidas }

                                                        val itensAdicionadosSelecionados = buildList {
                                                            varianteTracosAdicionados.forEach { trait ->
                                                                add(com.example.swadebuilder.model.usecase.VariantBudgetItem(label = trait.nome, custo = trait.custo, habilidadeId = trait.nome.lowercase().replace(" ", "_")))
                                                            }
                                                            varianteVantagensAdicionadas.forEach { vid ->
                                                                state.listaVantagens.firstOrNull { it.id == vid }?.let {
                                                                    add(com.example.swadebuilder.model.usecase.ResolveVariantPointBudgetUseCase.vantagemComoItemAdicionado(it))
                                                                }
                                                            }
                                                            varianteComplicacoesAdicionadas.forEach { esc ->
                                                                state.listaComplicacoes.firstOrNull { it.id == esc.complicacaoId }?.let {
                                                                    add(com.example.swadebuilder.model.usecase.ResolveVariantPointBudgetUseCase.complicacaoComoItemAdicionado(it, esc.comoMaior))
                                                                }
                                                            }
                                                        }

                                                        val budgetResult = com.example.swadebuilder.model.usecase.ResolveVariantPointBudgetUseCase().resolve(
                                                            itensRemovidosSelecionados, itensAdicionadosSelecionados, semLimite = varianteSemLimite
                                                        )

                                                        if (!budgetResult.dentroDoOrcamento) {
                                                            statusMessage = "A Variante precisa fechar em exatamente ±${budgetResult.orcamento} pontos (saldo atual: ${budgetResult.saldo}), ou marque 'Sem limite de pontos'."
                                                        } else {
                                                            val newVariant = com.example.swadebuilder.model.CustomAncestryVariant(
                                                                id = id,
                                                                ancestralidadeId = varianteBaseRacaId!!,
                                                                nome = customItemName,
                                                                descricao = safeDesc,
                                                                tracosRemovidosIds = varianteTracosRemovidos,
                                                                vantagensGratisRemovidas = varianteVantagensGratisRemovidas,
                                                                desvantagensRemovidas = varianteDesvantagensRemovidas,
                                                                tracosAdicionados = varianteTracosAdicionados,
                                                                vantagensAdicionadasIds = varianteVantagensAdicionadas,
                                                                complicacoesAdicionadas = varianteComplicacoesAdicionadas,
                                                                semLimiteDePontos = varianteSemLimite
                                                            )
                                                            customStorageManager.addVarianteRacial(context, activeBookKey, newVariant)
                                                            state.listaVariantesRaciaisCustom = state.listaVariantesRaciaisCustom + newVariant
                                                            statusMessage = "Variante '$customItemName' salva no livro $activeBookKey!"
                                                            varianteBaseRacaId = null
                                                            varianteTracosRemovidos = emptyList()
                                                            varianteVantagensGratisRemovidas = emptyList()
                                                            varianteDesvantagensRemovidas = emptyList()
                                                            varianteTracosAdicionados = emptyList()
                                                            varianteVantagensAdicionadas = emptyList()
                                                            varianteComplicacoesAdicionadas = emptyList()
                                                            varianteSemLimite = false
                                                        }
                                                    }
                                                }
                                            }
                                                    refreshTrigger++
                                            customItemName = ""
                                            customItemDesc = ""
                                            customRequirements = ""
                                            customAttrMin = emptyMap()
                                            customSkillMin = emptyMap()
                                            customPrereqEdges = emptyList()
                                            customPrereqComps = emptyList()
                                            customDamage = ""
                                            customRacialTrait = ""
                                        } else if (customPackageJson.isNotBlank()) {
                                            val importRes = manager.importPackageFromJson(customPackageJson)
                                            if (importRes.isSuccess) {
                                                statusMessage = "Pacote '${importRes.getOrNull()?.packageName}' importado com sucesso!"
                                                customPackageJson = ""
                                            } else {
                                                statusMessage = "Erro ao importar: ${importRes.exceptionOrNull()?.message}"
                                            }
                                        } else {
                                                    statusMessage = "Preencha o Nome do item."
                                        }
                                            }) { Text("Salvar Item") }
                                    TextButton(onClick = { showCustomContentDialog = false }) { Text("Fechar") }
                                }
                            }
                        )

                        // Requirement Selector Modals
                        if (showAttrDialog) {
                            val attrs = listOf("AGILIDADE" to "Agilidade", "ASTUCIA" to "Astúcia", "ESPIRITO" to "Espírito", "FORCA" to "Força", "VIGOR" to "Vigor")
                            val steps = listOf(0, 4, 6, 8, 10, 12, 13)
                            AlertDialog(
                                onDismissRequest = { showAttrDialog = false },
                                title = { Text("Atributos Mínimos") },
                                text = {
                                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                        attrs.forEach { (key, name) ->
                                            val currentDie = customAttrMin[key] ?: 0
                                            val currentIndex = steps.indexOf(currentDie).coerceAtLeast(0)
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = name,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    IconButton(
                                                        onClick = {
                                                            if (currentIndex > 0) {
                                                                val newDie = steps[currentIndex - 1]
                                                                val mut = customAttrMin.toMutableMap()
                                                                if (newDie == 0) mut.remove(key) else mut[key] = newDie
                                                                customAttrMin = mut
                                                            }
                                                        },
                                                        enabled = currentIndex > 0
                                                    ) {
                                                        Icon(Icons.Default.Remove, contentDescription = "Diminuir")
                                                    }
                                                    Text(
                                                        text = currentDie.toDiceString(),
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        modifier = Modifier.width(48.dp),
                                                        textAlign = TextAlign.Center
                                                    )
                                                    IconButton(
                                                        onClick = {
                                                            if (currentIndex < steps.lastIndex) {
                                                                val newDie = steps[currentIndex + 1]
                                                                val mut = customAttrMin.toMutableMap()
                                                                mut[key] = newDie
                                                                customAttrMin = mut
                                                            }
                                                        },
                                                        enabled = currentIndex < steps.lastIndex
                                                    ) {
                                                        Icon(Icons.Default.Add, contentDescription = "Aumentar")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                                confirmButton = { TextButton(onClick = { showAttrDialog = false }) { Text("OK") } }
                            )
                        }

                        if (showSkillDialog) {
                            val allSkillsList = state.listaPericias.map { it.nome }.distinct().sorted()
                            val steps = listOf(0, 4, 6, 8, 10, 12, 13)
                            var filterSkillText by remember { mutableStateOf("") }
                            AlertDialog(
                                onDismissRequest = { showSkillDialog = false },
                                title = { Text("Perícias Mínimas") },
                                text = {
                                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                        androidx.compose.material3.OutlinedTextField(
                                            value = filterSkillText,
                                            onValueChange = { filterSkillText = it },
                                            label = { Text("Filtrar Perícia") },
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                        )
                                        allSkillsList.filter { it.contains(filterSkillText, ignoreCase = true) }.forEach { skillName ->
                                            val currentDie = customSkillMin[skillName] ?: 0
                                            val currentIndex = steps.indexOf(currentDie).coerceAtLeast(0)
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = skillName,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    IconButton(
                                                        onClick = {
                                                            if (currentIndex > 0) {
                                                                val newDie = steps[currentIndex - 1]
                                                                val mut = customSkillMin.toMutableMap()
                                                                if (newDie == 0) mut.remove(skillName) else mut[skillName] = newDie
                                                                customSkillMin = mut
                                                            }
                                                        },
                                                        enabled = currentIndex > 0
                                                    ) {
                                                        Icon(Icons.Default.Remove, contentDescription = "Diminuir")
                                                    }
                                                    Text(
                                                        text = currentDie.toDiceString(),
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        modifier = Modifier.width(48.dp),
                                                        textAlign = TextAlign.Center
                                                    )
                                                    IconButton(
                                                        onClick = {
                                                            if (currentIndex < steps.lastIndex) {
                                                                val newDie = steps[currentIndex + 1]
                                                                val mut = customSkillMin.toMutableMap()
                                                                mut[skillName] = newDie
                                                                customSkillMin = mut
                                                            }
                                                        },
                                                        enabled = currentIndex < steps.lastIndex
                                                    ) {
                                                        Icon(Icons.Default.Add, contentDescription = "Aumentar")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                                confirmButton = { TextButton(onClick = { showSkillDialog = false }) { Text("OK") } }
                            )
                        }

                        if (showEdgeDialog) {
                            val availableEdges = state.listaVantagens.distinctBy { it.id }.sortedBy { it.nome }
                            var filterEdgeText by remember { mutableStateOf("") }
                            AlertDialog(
                                onDismissRequest = { showEdgeDialog = false },
                                title = { Text("Vantagens Prévias") },
                                text = {
                                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                        androidx.compose.material3.OutlinedTextField(
                                            value = filterEdgeText,
                                            onValueChange = { filterEdgeText = it },
                                            label = { Text("Filtrar Vantagem") },
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                        )
                                        availableEdges.filter { it.nome.contains(filterEdgeText, ignoreCase = true) }.forEach { edge ->
                                            val isSel = edge.id in customPrereqEdges
                                            Row(
                                                modifier = Modifier.fillMaxWidth().clickable {
                                                    customPrereqEdges = if (isSel) customPrereqEdges - edge.id else customPrereqEdges + edge.id
                                                }.padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Checkbox(checked = isSel, onCheckedChange = {
                                                    customPrereqEdges = if (it) customPrereqEdges + edge.id else customPrereqEdges - edge.id
                                                })
                                                Spacer(Modifier.width(8.dp))
                                                Text(edge.nome, style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                    }
                                },
                                confirmButton = { TextButton(onClick = { showEdgeDialog = false }) { Text("OK") } }
                            )
                        }

                        if (showCompDialog) {
                            val availableComps = state.listaComplicacoes.distinctBy { it.id }.sortedBy { it.name }
                            var filterCompText by remember { mutableStateOf("") }
                            AlertDialog(
                                onDismissRequest = { showCompDialog = false },
                                title = { Text("Complicações Mínimas") },
                                text = {
                                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                        androidx.compose.material3.OutlinedTextField(
                                            value = filterCompText,
                                            onValueChange = { filterCompText = it },
                                            label = { Text("Filtrar Complicação") },
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                        )
                                        availableComps.filter { it.name.contains(filterCompText, ignoreCase = true) }.forEach { comp ->
                                            val isSel = comp.id in customPrereqComps
                                            Row(
                                                modifier = Modifier.fillMaxWidth().clickable {
                                                    customPrereqComps = if (isSel) customPrereqComps - comp.id else customPrereqComps + comp.id
                                                }.padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Checkbox(checked = isSel, onCheckedChange = {
                                                    customPrereqComps = if (it) customPrereqComps + comp.id else customPrereqComps - comp.id
                                                })
                                                Spacer(Modifier.width(8.dp))
                                                Text(comp.name, style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                    }
                                },
                                confirmButton = { TextButton(onClick = { showCompDialog = false }) { Text("OK") } }
                            )
                        }

                        if (showTraitSelectDialog) {
                            val allTraitsCatalog = (baseRacialCatalog + activeBookCustomData.habilidadesRaciais).distinctBy { it.nome }
                            var filterTraitText by remember { mutableStateOf("") }
                            AlertDialog(
                                onDismissRequest = { showTraitSelectDialog = false },
                                title = { Text("Selecionar Traços Raciais") },
                                text = {
                                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                        androidx.compose.material3.OutlinedTextField(
                                            value = filterTraitText,
                                            onValueChange = { filterTraitText = it },
                                            label = { Text("Filtrar Traço") },
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                        )
                                        allTraitsCatalog.filter { it.nome.contains(filterTraitText, ignoreCase = true) }.forEach { trait ->
                                            val isSuperPoderesRow = trait.nome == "Super Poderes"
                                            val isSel = if (isSuperPoderesRow) {
                                                selectedRacialTraits.any { it.nome.startsWith("Super Poderes (") }
                                            } else {
                                                selectedRacialTraits.any { it.nome.equals(trait.nome, ignoreCase = true) }
                                            }
                                            val onToggle: (Boolean) -> Unit = { checked ->
                                                if (isSuperPoderesRow) {
                                                    if (checked) {
                                                        superPoderRacialPickerTarget = { escolhido ->
                                                            selectedRacialTraits = selectedRacialTraits + escolhido
                                                        }
                                                    } else {
                                                        selectedRacialTraits = selectedRacialTraits.filterNot { it.nome.startsWith("Super Poderes (") }
                                                    }
                                                } else {
                                                    selectedRacialTraits = if (checked) selectedRacialTraits + trait else selectedRacialTraits.filterNot { it.nome.equals(trait.nome, ignoreCase = true) }
                                                }
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth().clickable { onToggle(!isSel) }.padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Checkbox(checked = isSel, onCheckedChange = onToggle)
                                                Spacer(Modifier.width(8.dp))
                                                Column {
                                                    Text("${trait.nome} (${if (trait.custo > 0) "+${trait.custo}" else "${trait.custo}"} pts)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                                    if (trait.descricao.isNotBlank()) {
                                                        Text(trait.descricao, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                                confirmButton = { TextButton(onClick = { showTraitSelectDialog = false }) { Text("OK") } }
                            )
                        }

                        if (showVarianteBaseRacaDialog) {
                            val baseRacaOptions = state.listaAncestralidadesJson.distinctBy { it.nome.keyify() }.sortedBy { it.nome }
                            var filterBaseRacaText by remember { mutableStateOf("") }
                            AlertDialog(
                                onDismissRequest = { showVarianteBaseRacaDialog = false },
                                title = { Text("Raça Base da Variante") },
                                text = {
                                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                        androidx.compose.material3.OutlinedTextField(
                                            value = filterBaseRacaText,
                                            onValueChange = { filterBaseRacaText = it },
                                            label = { Text("Filtrar Raça") },
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                        )
                                        baseRacaOptions.filter { it.nome.contains(filterBaseRacaText, ignoreCase = true) }.forEach { raca ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth().clickable {
                                                    varianteBaseRacaId = raca.nome.keyify()
                                                    varianteTracosRemovidos = emptyList()
                                                    varianteVantagensGratisRemovidas = emptyList()
                                                    varianteDesvantagensRemovidas = emptyList()
                                                    showVarianteBaseRacaDialog = false
                                                }.padding(vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(raca.nome, style = MaterialTheme.typography.bodyMedium)
                                            }
                                        }
                                    }
                                },
                                confirmButton = { TextButton(onClick = { showVarianteBaseRacaDialog = false }) { Text("Fechar") } }
                            )
                        }

                        if (showVarianteTraitAddDialog) {
                            val allVarianteTraitsCatalog = (baseRacialCatalog + activeBookCustomData.habilidadesRaciais).distinctBy { it.nome }
                            var filterVarianteTraitText by remember { mutableStateOf("") }
                            AlertDialog(
                                onDismissRequest = { showVarianteTraitAddDialog = false },
                                title = { Text("Adicionar Traço Racial") },
                                text = {
                                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                        androidx.compose.material3.OutlinedTextField(
                                            value = filterVarianteTraitText,
                                            onValueChange = { filterVarianteTraitText = it },
                                            label = { Text("Filtrar Traço") },
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                        )
                                        allVarianteTraitsCatalog.filter { it.nome.contains(filterVarianteTraitText, ignoreCase = true) }.forEach { trait ->
                                            val isSuperPoderesRow = trait.nome == "Super Poderes"
                                            val isSel = if (isSuperPoderesRow) {
                                                varianteTracosAdicionados.any { it.nome.startsWith("Super Poderes (") }
                                            } else {
                                                varianteTracosAdicionados.any { it.nome.equals(trait.nome, ignoreCase = true) }
                                            }
                                            val onToggle: (Boolean) -> Unit = { checked ->
                                                if (isSuperPoderesRow) {
                                                    if (checked) {
                                                        superPoderRacialPickerTarget = { escolhido ->
                                                            varianteTracosAdicionados = varianteTracosAdicionados + escolhido
                                                        }
                                                    } else {
                                                        varianteTracosAdicionados = varianteTracosAdicionados.filterNot { it.nome.startsWith("Super Poderes (") }
                                                    }
                                                } else {
                                                    varianteTracosAdicionados = if (checked) varianteTracosAdicionados + trait else varianteTracosAdicionados.filterNot { it.nome.equals(trait.nome, ignoreCase = true) }
                                                }
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth().clickable { onToggle(!isSel) }.padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Checkbox(checked = isSel, onCheckedChange = onToggle)
                                                Spacer(Modifier.width(8.dp))
                                                Column {
                                                    Text("${trait.nome} (${if (trait.custo > 0) "+${trait.custo}" else "${trait.custo}"} pts)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                                    if (trait.descricao.isNotBlank()) {
                                                        Text(trait.descricao, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                                confirmButton = { TextButton(onClick = { showVarianteTraitAddDialog = false }) { Text("OK") } }
                            )
                        }

                        superPoderRacialPickerTarget?.let { onEscolhido ->
                            var filterSuperPoderText by remember { mutableStateOf("") }
                            AlertDialog(
                                onDismissRequest = { superPoderRacialPickerTarget = null },
                                title = { Text("Escolher Super Poder") },
                                text = {
                                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                        Text(
                                            "O traço Super Poderes custa 2 pontos pelo Antecedente Arcano (Super Poderes) mais o custo do poder escolhido.",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                        androidx.compose.material3.OutlinedTextField(
                                            value = filterSuperPoderText,
                                            onValueChange = { filterSuperPoderText = it },
                                            label = { Text("Filtrar Super Poder") },
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                        )
                                        superPoderesCatalog
                                            .filter { it.nome.contains(filterSuperPoderText, ignoreCase = true) }
                                            .forEach { poder ->
                                                val custoPoder = primeiroCustoSuperPoder(poder.custoBase)
                                                val custoTotal = 2 + custoPoder
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().clickable {
                                                        onEscolhido(
                                                            com.example.swadebuilder.model.HabilidadeCriacao(
                                                                nome = "Super Poderes (${poder.nome})",
                                                                custo = custoTotal,
                                                                descricao = "Antecedente Arcano (Super Poderes) + poder \"${poder.nome}\" (custo base ${poder.custoBase ?: custoPoder}).",
                                                                descricaoLite = "Concede o Antecedente Arcano (Super Poderes) e o poder \"${poder.nome}\" do Compêndio de Super Poderes."
                                                            )
                                                        )
                                                        superPoderRacialPickerTarget = null
                                                    }.padding(vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column {
                                                        Text("${poder.nome} (2+$custoPoder = $custoTotal pts)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                                        if (!poder.descricao.isNullOrBlank()) {
                                                            Text(poder.descricao, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                                                        }
                                                    }
                                                }
                                            }
                                    }
                                },
                                confirmButton = { TextButton(onClick = { superPoderRacialPickerTarget = null }) { Text("Cancelar") } }
                            )
                        }

                        if (showVarianteVantagemAddDialog) {
                            val availableVarianteVantagens = state.listaVantagens.distinctBy { it.id }.sortedBy { it.nome }
                            var filterVarianteVantagemText by remember { mutableStateOf("") }
                            AlertDialog(
                                onDismissRequest = { showVarianteVantagemAddDialog = false },
                                title = { Text("Adicionar Vantagem Grátis") },
                                text = {
                                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                        androidx.compose.material3.OutlinedTextField(
                                            value = filterVarianteVantagemText,
                                            onValueChange = { filterVarianteVantagemText = it },
                                            label = { Text("Filtrar Vantagem") },
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                        )
                                        availableVarianteVantagens.filter { it.nome.contains(filterVarianteVantagemText, ignoreCase = true) }.forEach { vant ->
                                            val isSel = vant.id in varianteVantagensAdicionadas
                                            Row(
                                                modifier = Modifier.fillMaxWidth().clickable {
                                                    varianteVantagensAdicionadas = if (isSel) varianteVantagensAdicionadas - vant.id else varianteVantagensAdicionadas + vant.id
                                                }.padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Checkbox(checked = isSel, onCheckedChange = {
                                                    varianteVantagensAdicionadas = if (it) varianteVantagensAdicionadas + vant.id else varianteVantagensAdicionadas - vant.id
                                                })
                                                Spacer(Modifier.width(8.dp))
                                                Text("${vant.nome} (${vant.requisitos.estagio})", style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                    }
                                },
                                confirmButton = { TextButton(onClick = { showVarianteVantagemAddDialog = false }) { Text("OK") } }
                            )
                        }

                        if (showVarianteComplicacaoSeveridadeDialog) {
                            AlertDialog(
                                onDismissRequest = { showVarianteComplicacaoSeveridadeDialog = false },
                                title = { Text("Severidade da Complicação") },
                                text = {
                                    Text(
                                        "Escolha a severidade da Complicação a adicionar. Isso filtra quais Complicações do(s) livro(s) ativo(s) ficam disponíveis.",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                },
                                confirmButton = {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        TextButton(onClick = {
                                            varianteComplicacaoComoMaiorEscolhido = false
                                            showVarianteComplicacaoSeveridadeDialog = false
                                            showVarianteComplicacaoPickDialog = true
                                        }) { Text("Menor (-1)") }
                                        TextButton(onClick = {
                                            varianteComplicacaoComoMaiorEscolhido = true
                                            showVarianteComplicacaoSeveridadeDialog = false
                                            showVarianteComplicacaoPickDialog = true
                                        }) { Text("Maior (-2)") }
                                    }
                                },
                                dismissButton = { TextButton(onClick = { showVarianteComplicacaoSeveridadeDialog = false }) { Text("Cancelar") } }
                            )
                        }

                        if (showVarianteComplicacaoPickDialog) {
                            val comoMaior = varianteComplicacaoComoMaiorEscolhido
                            val availableVarianteComps = state.listaComplicacoes.distinctBy { it.id }.filter { comp ->
                                val sev = comp.severity.trim().lowercase()
                                if (comoMaior) {
                                    sev == "maior" || (sev.contains("menor") && sev.contains("maior"))
                                } else {
                                    sev == "menor" || (sev.contains("menor") && sev.contains("maior"))
                                }
                            }.sortedBy { it.name }
                            var filterVarianteCompText by remember { mutableStateOf("") }
                            AlertDialog(
                                onDismissRequest = { showVarianteComplicacaoPickDialog = false },
                                title = { Text(if (comoMaior) "Complicações Maiores" else "Complicações Menores") },
                                text = {
                                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                        androidx.compose.material3.OutlinedTextField(
                                            value = filterVarianteCompText,
                                            onValueChange = { filterVarianteCompText = it },
                                            label = { Text("Filtrar Complicação") },
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                        )
                                        availableVarianteComps.filter { it.name.contains(filterVarianteCompText, ignoreCase = true) }.forEach { comp ->
                                            val isSel = varianteComplicacoesAdicionadas.any { it.complicacaoId == comp.id && it.comoMaior == comoMaior }
                                            Row(
                                                modifier = Modifier.fillMaxWidth().clickable {
                                                    varianteComplicacoesAdicionadas = if (isSel) {
                                                        varianteComplicacoesAdicionadas.filterNot { it.complicacaoId == comp.id && it.comoMaior == comoMaior }
                                                    } else {
                                                        varianteComplicacoesAdicionadas + com.example.swadebuilder.model.CustomVariantComplicacaoEscolhida(comp.id, comoMaior)
                                                    }
                                                }.padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Checkbox(checked = isSel, onCheckedChange = {
                                                    varianteComplicacoesAdicionadas = if (it) {
                                                        varianteComplicacoesAdicionadas + com.example.swadebuilder.model.CustomVariantComplicacaoEscolhida(comp.id, comoMaior)
                                                    } else {
                                                        varianteComplicacoesAdicionadas.filterNot { e -> e.complicacaoId == comp.id && e.comoMaior == comoMaior }
                                                    }
                                                })
                                                Spacer(Modifier.width(8.dp))
                                                Text(comp.name, style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                    }
                                },
                                confirmButton = { TextButton(onClick = { showVarianteComplicacaoPickDialog = false }) { Text("OK") } }
                            )
                        }
                    }
                }

                // Card "Backup e Transferência (JSON)"
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    var showExportDialog by remember { mutableStateOf(false) }
                    var showImportDialog by remember { mutableStateOf(false) }
                    var backupJsonText by remember { mutableStateOf("") }
                    var importErrorText by remember { mutableStateOf<String?>(null) }

                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Backup da Ficha (JSON)",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Exporte ou importe fichas em formato JSON seguro.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    backupJsonText = com.example.swadebuilder.util.CharacterBackupManager.exportBackupJson(state.toSnapshot())
                                    showExportDialog = true
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Exportar JSON")
                            }
                            OutlinedButton(
                                onClick = {
                                    backupJsonText = ""
                                    importErrorText = null
                                    showImportDialog = true
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Importar JSON")
                            }
                        }
                    }

                    if (showExportDialog) {
                        AlertDialog(
                            onDismissRequest = { showExportDialog = false },
                            title = { Text("Backup JSON da Ficha") },
                            text = {
                                Column {
                                    Text("Copie o texto JSON abaixo para salvar seu backup em outro lugar:", style = MaterialTheme.typography.bodySmall)
                                    Spacer(Modifier.height(8.dp))
                                    androidx.compose.material3.OutlinedTextField(
                                        value = backupJsonText,
                                        onValueChange = {},
                                        readOnly = true,
                                        modifier = Modifier.fillMaxWidth().height(180.dp)
                                    )
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = { showExportDialog = false }) { Text("Fechar") }
                            }
                        )
                    }

                    if (showImportDialog) {
                        AlertDialog(
                            onDismissRequest = { showImportDialog = false },
                            title = { Text("Importar Backup JSON") },
                            text = {
                                Column {
                                    Text("Cole o texto JSON do backup da ficha:", style = MaterialTheme.typography.bodySmall)
                                    Spacer(Modifier.height(8.dp))
                                    androidx.compose.material3.OutlinedTextField(
                                        value = backupJsonText,
                                        onValueChange = { backupJsonText = it; importErrorText = null },
                                        modifier = Modifier.fillMaxWidth().height(160.dp),
                                        placeholder = { Text("{ \"id\": ... }") }
                                    )
                                    if (importErrorText != null) {
                                        Spacer(Modifier.height(8.dp))
                                        Text(importErrorText!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        when (val result = com.example.swadebuilder.util.CharacterBackupManager.importBackupJson(backupJsonText)) {
                                            is com.example.swadebuilder.util.CharacterBackupManager.ImportResult.Success -> {
                                                state.restoreFromSnapshot(result.snapshot, mutableListOf())
                                                showImportDialog = false
                                            }
                                            is com.example.swadebuilder.util.CharacterBackupManager.ImportResult.Failure -> {
                                                importErrorText = result.reason
                                            }
                                        }
                                    },
                                    enabled = backupJsonText.isNotBlank()
                                ) {
                                    Text("Restaurar Ficha")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showImportDialog = false }) { Text("Cancelar") }
                            }
                        )
                    }
                }

                // Card "Visual e Tema"
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Visual e Tema",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text("Estilo das Abas / Opções", style = MaterialTheme.typography.bodyMedium)

                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val options = listOf(TabStyle.ICONES, TabStyle.TEXTO)
                            val labels = listOf("Ícones", "Texto")

                            options.forEachIndexed { index, option ->
                                SegmentedButton(
                                    selected = state.estiloAbas == option,
                                    onClick = {
                                        state.estiloAbas = option
                                        persistPrefs()
                                    },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size)
                                ) {
                                    Text(labels[index])
                                }
                            }
                        }

                        Spacer(Modifier.height(4.dp))

                        // Theme Selection Trigger Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Tema do App", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = themeNames[state.appTheme] ?: state.appTheme.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            OutlinedButton(
                                onClick = { showThemeDialog = true }
                            ) {
                                Text("Alterar Tema")
                            }
                        }
                    }
                }

                // Card "Sons e Vibração"
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp) // Increased spacing for cleaner look
                    ) {
                        Text(
                            text = "Sons e Vibração",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // Haptic Feedback
                        Column {
                            Text("Intensidade da Vibração", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Vibration,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Slider(
                                    value = state.hapticStrength.toFloat(),
                                    onValueChange = { state.hapticStrength = it.roundToInt() },
                                    onValueChangeFinished = {
                                        persistPrefs()
                                        feedbackController.play(state.hapticStrength, 0)
                                    },
                                    valueRange = 0f..100f,
                                    modifier = Modifier.weight(1f),
                                    thumb = {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                                        )
                                    },
                                    track = { sliderState ->
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(2.dp)
                                                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                                        ) {
                                            val fraction = (sliderState.value - sliderState.valueRange.start) / (sliderState.valueRange.endInclusive - sliderState.valueRange.start)
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(fraction)
                                                    .fillMaxHeight()
                                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                                            )
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("${state.hapticStrength}%", style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        // App Sounds
                        Column {
                            Text("Volume", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Slider(
                                    value = state.soundVolume.toFloat(),
                                    onValueChange = { state.soundVolume = it.roundToInt() },
                                    onValueChangeFinished = {
                                        persistPrefs()
                                        feedbackController.play(0, state.soundVolume)
                                    },
                                    valueRange = 0f..100f,
                                    modifier = Modifier.weight(1f),
                                    thumb = {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .background(MaterialTheme.colorScheme.secondary, CircleShape)
                                        )
                                    },
                                    track = { sliderState ->
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(2.dp)
                                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f), CircleShape)
                                        ) {
                                            val fraction = (sliderState.value - sliderState.valueRange.start) / (sliderState.valueRange.endInclusive - sliderState.valueRange.start)
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(fraction)
                                                    .fillMaxHeight()
                                                    .background(MaterialTheme.colorScheme.secondary, CircleShape)
                                            )
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("${state.soundVolume}%", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar")
            }
        }
    )

    if (showNpcWarning) {
        AlertDialog(
            onDismissRequest = { showNpcWarning = false },
            title = { Text("Transformar em NPC?") },
            text = { Text("Ao ativar o Modo Livre, este personagem será transformado em um NPC. Custos de pontos e requisitos serão ignorados, e a progressão de XP padrão será desabilitada. Esta ação é irreversível para este personagem.") },
            confirmButton = {
                TextButton(onClick = {
                    state.modoLivre = true
                    showNpcWarning = false
                }) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { showNpcWarning = false }) { Text("Cancelar") }
            }
        )
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Selecionar Tema do App", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    sortedThemes.forEach { theme ->
                        val isSelected = state.appTheme == theme
                        val themeLabel = themeNames[theme] ?: theme.name
                        val themeDesc = themeDescriptions[theme] ?: ""
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                        ) {
                            if (isSelected) {
                                TextButton(
                                    onClick = { showThemeDialog = false },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("✓ $themeLabel", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                        if (themeDesc.isNotBlank()) {
                                            Text(themeDesc, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            } else {
                                OutlinedButton(
                                    onClick = {
                                        onThemeSelected(theme)
                                        persistPrefs()
                                        feedbackController.play(state.hapticStrength, state.soundVolume)
                                        showThemeDialog = false
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(themeLabel, style = MaterialTheme.typography.titleMedium)
                                        if (themeDesc.isNotBlank()) {
                                            Text(themeDesc, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
