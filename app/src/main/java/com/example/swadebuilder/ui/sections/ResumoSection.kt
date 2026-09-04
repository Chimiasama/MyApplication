package com.example.swadebuilder.ui.sections

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MoodBad
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.OutlinedButton
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsMartialArts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.model.getActiveOrigins
import com.example.swadebuilder.buildAncestralidadeDisplay
import com.example.swadebuilder.buildSummaryLines
import com.example.swadebuilder.model.Constants
import com.example.swadebuilder.model.CriadorViewModel
import com.example.swadebuilder.model.Pericia
import com.example.swadebuilder.toDiceString
import com.example.swadebuilder.toMeuPersonagem
import com.example.swadebuilder.ui.components.ChoiceButtonRow
import com.example.swadebuilder.util.CharacterPortraitStorage
import com.example.swadebuilder.util.toFancyTitleCase

@Composable
private fun getCompendiumIcons(state: CriadorState): List<Pair<ImageVector, Color>> {
    val icons = mutableListOf<Pair<ImageVector, Color>>()

    if (state.modoSupers) icons.add(Icons.Default.Bolt to Color(0xFFFFD700)) // Gold
    if (state.compendioFantasiaAtivo) icons.add(Icons.Default.AutoAwesome to Color(0xFF9C27B0)) // Purple
    if (state.compendioHorrorAtivo) icons.add(Icons.Default.MoodBad to Color(0xFFD32F2F)) // Red
    if (state.compendioSciFiAtivo) icons.add(Icons.Default.RocketLaunch to Color(0xFF03A9F4)) // Light Blue
    if (state.compendioPathfinderAtivo) icons.add(Icons.Default.Map to Color(0xFF4CAF50)) // Green
    if (state.compendioDeadlandsAtivo) icons.add(Icons.Default.Shield to Color(0xFFFF9800)) // Orange
    if (state.compendioArteDaGuerraAtivo) icons.add(Icons.Default.SportsMartialArts to Color(0xFF795548)) // Brown
    if (state.compendioWiseguysAtivo) icons.add(Icons.Default.Groups to Color.Gray)
    if (state.compendioCrystalHeartAtivo) icons.add(Icons.Default.Favorite to Color(0xFFE91E63)) // Pink
    if (state.compendioCidadeSolVaporAtivo) icons.add(Icons.Default.Build to Color.LightGray)

    // Basic Rules Icon logic: Show if no other "Setting Book" is active
    val isAnySpecificBook = state.modoSupers ||
            state.compendioFantasiaAtivo ||
            state.compendioHorrorAtivo ||
            state.compendioSciFiAtivo ||
            state.compendioPathfinderAtivo ||
            state.compendioDeadlandsAtivo ||
            state.compendioArteDaGuerraAtivo ||
            state.compendioWiseguysAtivo ||
            state.compendioCrystalHeartAtivo ||
            state.compendioCidadeSolVaporAtivo

    if (!isAnySpecificBook) {
        icons.add(Icons.AutoMirrored.Filled.MenuBook to Color(0xFF6D4C41)) // Brown 600
    }

    return icons
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SummaryContent(
    state: CriadorState,
    viewModel: CriadorViewModel = viewModel(),
    imageUri: Uri? = null,
    onSelectImage: () -> Unit = {}
) {

    val context = LocalContext.current

    val flagsTemplate = remember(state) {
        listOfNotNull(
            "Carta Selvagem".takeIf { state.cartaSelvagem },
            "Sem Pontos de Poder".takeIf { state.usarSemPontosDePoder },
            "Mais Pontos de Perícias".takeIf { state.maisPontosPericias },
            "Especializações".takeIf { state.usarEspecializacoesDePericia },
            if (state.compendioPathfinderAtivo) {
                val hasLinguista = state.vantagensSelecionadas.any { it.id == "linguista" }
                "Idiomas: comum + ${if (hasLinguista) "Astúcia" else "1/2 Astúcia"}"
            } else null
        )
    }

    LaunchedEffect(flagsTemplate) {
        if (state.anotacoes.isBlank() && flagsTemplate.isNotEmpty()) {
            state.anotacoes = flagsTemplate.joinToString("\n") { "• $it" }
        }
    }

    // Dynamic update for Language note (Pathfinder)
    val hasLinguista = state.vantagensSelecionadas.any { it.id == "linguista" }
    var previousPathfinderState by remember { mutableStateOf(state.compendioPathfinderAtivo) }

    LaunchedEffect(hasLinguista, state.compendioPathfinderAtivo) {
        val newText = "• Idiomas: comum + ${if (hasLinguista) "Astúcia" else "1/2 Astúcia"}"

        // Case 1: Just toggled Pathfinder ON? Append if missing.
        // This handles existing characters that didn't have it initialized via flagsTemplate
        if (!previousPathfinderState && state.compendioPathfinderAtivo) {
            if (!state.anotacoes.contains("Idiomas: comum")) {
                state.anotacoes = if (state.anotacoes.isBlank()) newText else "${state.anotacoes}\n$newText"
            }
        }
        previousPathfinderState = state.compendioPathfinderAtivo

        // Case 2: Already active, Linguist changed (or re-evaluating). Update if old version present.
        if (state.compendioPathfinderAtivo) {
            val oldText1 = "• Idiomas: comum + 1/2 Astúcia"
            val oldText2 = "• Idiomas: comum + Astúcia"

            if (state.anotacoes.contains(oldText1)) {
                state.anotacoes = state.anotacoes.replace(oldText1, newText)
            } else if (state.anotacoes.contains(oldText2)) {
                state.anotacoes = state.anotacoes.replace(oldText2, newText)
            }
        }
    }

    val sections = rememberSummarySections(state, viewModel)

    val derivedSection = sections.firstOrNull { it.title == "Atributos derivados" }
    val attributesSection = sections.firstOrNull { it.title == "Atributos" }
    val skillsSection = sections.firstOrNull { it.title == "Perícias" }

    val hasMusculoso = state.vantagensSelecionadas.any { it.id == Constants.ID_MUSCULOSO }
    val hasSoldado = state.vantagensSelecionadas.any { it.id == Constants.ID_SOLDADO }
    val hasDwarfLoadBonus = state.compendioPathfinderAtivo && state.currentAncestryDef?.especieId == "anao"
    val weightLimit = state.valorCargaMaxima()
    val totalWeight = state.equipamentosComprados
        .mapNotNull { item ->
            (item.peso as? kotlinx.serialization.json.JsonPrimitive)
                ?.content
                ?.replace(",", ".")
                ?.toFloatOrNull()
        }
        .sum()
    val isPersonagemRobotico = state.isPersonagemRobotico()
    val tensaoTotal = state.totalTensaoCibernetica()
    val tensaoLimite = if (isPersonagemRobotico) {
        state.limiteModsRoboticos()
    } else {
        state.valorLimiteTensao().second
    }
    val tensaoLabel = if (isPersonagemRobotico) "Mods" else "Tensão"
    val ratio = if (weightLimit > 0f) totalWeight / weightLimit else Float.POSITIVE_INFINITY
    val weightWarning = when {
        ratio >= 4f -> "Impossível carregar tanto peso, remova itens ou aumente a força."
        ratio >= 3f -> "Peso extremo! Penalidades severas de sobrecarga."
        ratio > 1f -> "Peso excedido! Você está sobrecarregado."
        else -> null
    }

    // Calculate weight info separately to pass to the custom card
    val weightInfoLines = remember(state.equipamentosComprados, state.valoresAtributos, state.vantagensSelecionadas) {
        val soldierLabel = when {
            hasSoldado && state.soldadoCargaAtivo && hasDwarfLoadBonus -> " (Soldado e Anão +2 dados)"
            hasSoldado && state.soldadoCargaAtivo -> " (Soldado +1 dado)"
            hasSoldado -> " (Soldado inativo)"
            hasDwarfLoadBonus -> " (Anão +1 dado)"
            else -> ""
        }
        val weightLine = "Peso: ${"%.1f".format(totalWeight)} / ${"%.1f".format(weightLimit)}$soldierLabel"
        buildList {
            add(weightLine)
            if (state.compendioSciFiAtivo) {
                val tensaoLine = "$tensaoLabel: $tensaoTotal/$tensaoLimite"
                val tensaoWarning = if (tensaoTotal > tensaoLimite) {
                    val excess = tensaoTotal - tensaoLimite
                    "• Sobrecarga Cibernética: ${if (excess > 2) "Exausto" else "Fatigado"}"
                } else {
                    null
                }
                add(tensaoLine)
                tensaoWarning?.let { add(it) }
            }
            weightWarning?.let { add("• $it") }
        }
    }

    val otherSections = sections.filterNot {
        it.title in setOf("Identidade", "Atributos derivados", "Atributos", "Perícias", "Recursos & Equipamentos")
    }

    val nome = state.nomePersonagem

    val ancestralidadeValue = buildAncestralidadeDisplay(state.toMeuPersonagem(), especieId = state.currentAncestryDef?.especieId)

    val heartValue = state.coracaoCrystalSelecionado?.nome

    val monstroInfo = if (state.modoMonstroAtivo) {
        val tipoNome = state.listaMonstroTemplates.find { it.id == state.tipoMonstroSelecionado }?.nome ?: "Desconhecido"
        "\nTipo de Monstro: $tipoNome"
    } else ""

    val ancestralidadeDisplay = buildString {
        append("$ancestralidadeValue$monstroInfo")
        if (heartValue != null) {
            append("\nCoração: $heartValue")
        }
    }

    Column(Modifier.fillMaxWidth()) {
        IdentityCard(
            nome = nome,
            onNomeChange = { state.nomePersonagem = it },
            ancestralidade = ancestralidadeDisplay,
        activeCompendiums = if (state.mostrarIdentificadorLivro) getCompendiumIcons(state) else emptyList()
        )

        if (!state.modoProgressaoAtivo) {
            var showArchetypeDialog by remember { mutableStateOf(false) }

            androidx.compose.material3.OutlinedButton(
                onClick = { showArchetypeDialog = true },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text("Aplicar Arquétipo / Template")
            }

            if (showArchetypeDialog) {
                com.example.swadebuilder.ui.dialogs.ArchetypeSelectionDialog(
                    settingKey = state.getActiveOrigins().firstOrNull() ?: "BASICO",
                    onDismiss = { showArchetypeDialog = false },
                    onApplyArchetype = { archetype ->
                        viewModel.applyArchetype(archetype)
                    }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        derivedSection?.let {
            val showWealthControl = state.modoProgressaoAtivo && state.usaRiqueza
            DerivedStatsRow(
                stats = it.toStats(),
                onFamaChange = if (state.modoProgressaoAtivo && state.optRegraFama) { delta ->
                    state.famaManual += delta
                } else null,
                onWealthChange = if (showWealthControl) { delta ->
                    state.riquezaModifier += delta
                } else null,
                wealthDieValue = if (showWealthControl) state.dadoRiqueza.toDiceString() else null
            )
            Spacer(Modifier.height(12.dp))
        }

        // Layout: Attributes + Image Placeholder in a Row, then Skills below
        attributesSection?.let { attrSection ->
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummarySectionCard(
                    section = attrSection,
                    modifier = Modifier.weight(1f),
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp)
                )

                // Image Placeholder
                val imageBitmapState = produceState<ImageBitmap?>(initialValue = null, state.portraitFileName) {
                    value = if (state.portraitFileName == null) {
                        null
                    } else {
                        CharacterPortraitStorage.loadPortrait(context, state.portraitFileName!!, targetWidth = 512)?.asImageBitmap()
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(0.7f)
                        .aspectRatio(0.8f) // Fixed aspect ratio
                        .clickable(onClick = onSelectImage),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    border = androidx.compose.foundation.BorderStroke(4.dp, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        val imageBitmap = imageBitmapState.value
                        val isPortraitLoading = state.portraitFileName != null && imageBitmap == null
                        if (imageBitmap != null) {
                            PortraitImage(
                                imageBitmap = imageBitmap,
                                scaleType = state.portraitScaleType,
                                offsetY = state.portraitOffsetY,
                                zoom = state.portraitZoom,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else if (isPortraitLoading) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                com.example.swadebuilder.ui.components.LoadingState(modifier = Modifier.fillMaxWidth())
                            }
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Retrato",
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        // Skills Section (Horizontal / Flow)
        skillsSection?.let { skillSection ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text(
                        text = skillSection.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        skillSection.items.forEach { item ->
                            SkillChip(item)
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        if (state.usarEspecializacoesDePericia) {
            SpecializationsSummaryCard(state = state)
            Spacer(Modifier.height(12.dp))
        }

        SkillNotesSummaryCard(state = state)
        Spacer(Modifier.height(12.dp))

        val filteredSections = otherSections.filterNot { section ->
            when (section.title) {
                "Vantagens", "Complicações" ->
                    section.items.none { it.trim() != "– Nenhuma" }

                else -> false
            }
        }

        // Render Custom Equipment Card
        CombatAndEquipmentCard(state = state, weightInfo = weightInfoLines)
        Spacer(Modifier.height(12.dp))

        filteredSections.forEachIndexed { idx, section ->
            SummarySectionCard(section = section)
            if (idx != filteredSections.lastIndex) {
                Spacer(Modifier.height(12.dp))
            }
        }

        Spacer(Modifier.height(16.dp))
        HorizontalDivider(thickness = 1.dp)
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = state.anotacoes,
            onValueChange = { if (it.length <= 5000) state.anotacoes = it },
            label = { Text("Anotações") },
            supportingText = { Text("${state.anotacoes.length}/5000") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 5,
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
private fun SkillNotesSummaryCard(
    state: CriadorState,
    modifier: Modifier = Modifier
) {
    var showEditDialog by rememberSaveable { mutableStateOf(false) }
    var editPerTarget by rememberSaveable { mutableStateOf<Pericia?>(null) }
    var editText by rememberSaveable { mutableStateOf("") }

    val editableSkills = state.periciasComIdiomas().filter { per ->
        val hasPoints = state.rawTotal(per) > 0
        if (!hasPoints) return@filter false

        state.isIdiomaPericia(per) || state.isJutsuPericia(per) || state.usarEspecializacoesDePericia
    }

    if (editableSkills.isEmpty()) return

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                text = "Notas de Perícias",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))

            editableSkills.forEach { per ->
                val note = state.notasPericia[per.nome].orEmpty()
                val displayName =
                    if (state.compendioArteDaGuerraAtivo && per.nome.equals("Lutar", ignoreCase = true)) "Jutsu"
                    else per.nome
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (note.isNotBlank()) note else "(sem descrição)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = {
                            editPerTarget = per
                            editText = note
                            showEditDialog = true
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar nota da perícia",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
            }
        }
    }

    if (showEditDialog && editPerTarget != null) {
        val per = editPerTarget!!
        val isIdiomaOuJutsu = state.isIdiomaPericia(per) || state.isJutsuPericia(per)
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text(if (isIdiomaOuJutsu) "Editar idioma" else "Editar nota da perícia") },
            text = {
                Column {
                    Text("Perícia: ${if (state.compendioArteDaGuerraAtivo && per.nome.equals("Lutar", ignoreCase = true)) "Jutsu" else per.nome}")
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editText,
                        onValueChange = { if (it.length <= 80) editText = it },
                        label = { Text("Descrição") },
                        supportingText = { Text("${editText.length}/80") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val newValue = editText.trim()
                        if (newValue.isNotBlank()) {
                            state.notasPericia[per.nome] = newValue
                        } else if (isIdiomaOuJutsu) {
                            state.notasPericia[per.nome] = state.idiomaDefaultLabel(per)
                        } else {
                            state.notasPericia.remove(per.nome)
                        }
                        showEditDialog = false
                    }
                ) { Text("Salvar") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

private data class SummarySection(
    val title: String,
    val items: List<String>
)

private val summaryHeaders = setOf(
    "Identidade",
    "Atributos derivados",
    "Atributos",
    "Perícias",
    "Recursos & Equipamentos",
    "Vantagens",
    "Complicações",
    "Poderes arcanos",
    "Superpoderes"
)

@Composable
private fun rememberSummarySections(state: CriadorState, viewModel: CriadorViewModel): List<SummarySection> {
    val personagem = state.toMeuPersonagem()
    val allAdvantages = viewModel.gameDataStore.getVantagens()
    val allLines = buildSummaryLines(
        personagem = personagem,
        allAdvantages = allAdvantages,
        listaAncestralidades = viewModel.gameDataStore.getAncestralidades(),
        listaMonstros = viewModel.gameDataStore.getMonstroTemplates(),
        listaComplicacoes = viewModel.gameDataStore.getComplicacoes(),
        listaAtributos = viewModel.gameDataStore.getAtributos(),
        mapaAtributosDisplay = viewModel.gameDataStore.getMapaAtributosDisplay(),
        listaPericias = viewModel.gameDataStore.getPericias(),
        listaPoderes = viewModel.gameDataStore.getPoderes(),
        arcanoInfo = viewModel.gameDataStore.getArcanoInfoMap(),
        ancestralidadeAtual = state.currentAncestryDef
    )
    val anotIndex = allLines.indexOf("Anotações")
    val lines = if (anotIndex >= 0) allLines.take(anotIndex) else allLines

    return remember(lines) { lines.toSummarySections(summaryHeaders) }
}

private fun List<String>.toSummarySections(headers: Set<String>): List<SummarySection> {
    val sections = mutableListOf<SummarySection>()
    var currentTitle: String? = null
    var currentItems = mutableListOf<String>()

    fun flushCurrent() {
        if (currentTitle != null) {
            sections += SummarySection(currentTitle!!, currentItems.toList())
        }
        currentItems = mutableListOf()
    }

    for (line in this) {
        when {
            line in headers -> {
                flushCurrent()
                currentTitle = line
            }
            line.isNotBlank() -> currentItems += line
        }
    }

    flushCurrent()
    return sections
}

private fun SummarySection.toStats(): List<Pair<String, String>> =
    items.mapNotNull { line ->
        val label = line.substringBefore(":", missingDelimiterValue = "")
        val value = line.substringAfter(":", missingDelimiterValue = "")
        if (label.isNotBlank() && value.isNotBlank()) {
            label.trim() to value.trim()
        } else {
            null
        }
    }

@Composable
private fun IdentityCard(
    nome: String,
    onNomeChange: (String) -> Unit,
    ancestralidade: String,
    activeCompendiums: List<Pair<ImageVector, Color>> = emptyList()
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            if (activeCompendiums.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    activeCompendiums.forEach { (icon, color) ->
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier
                                .size(24.dp)
                                .padding(start = 4.dp)
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
            }

            OutlinedTextField(
                value = nome,
                onValueChange = { if (it.length <= 60) onNomeChange(it) },
                label = { Text("Nome do Personagem") },
                supportingText = { Text("${nome.length}/60") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = ancestralidade,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CombatAndEquipmentCard(
    state: CriadorState,
    weightInfo: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                text = "Combate & Equipamentos",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))

            // Weight Info
            weightInfo.forEach { item ->
                if (item.contains("Peso:")) {
                    Text(text = item, style = MaterialTheme.typography.bodySmall)
                } else if (item.startsWith("•")) {
                    Text(text = item, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                } else {
                    Text(text = item, style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            // --- ATAQUES ---
            Text(text = "Ataques", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))

            // Natural Attack
            val naturalWeapons = state.extrairArmasNaturais()
            naturalWeapons.forEach { w ->
                val dmg = (w.dano as? kotlinx.serialization.json.JsonPrimitive)?.content ?: "-"
                // Extract notes/modifiers from observacoes
                val notes = (w.observacoes as? kotlinx.serialization.json.JsonPrimitive)?.content ?: ""

                // Map "Ataque Natural" -> "Desarmado" for better display
                val displayName = if (w.nome == "Ataque Natural") "Desarmado" else w.nome
                CombatRow(name = displayName.toFancyTitleCase(), stats = dmg, notes = notes)
            }

            // Weapons
            val weapons = state.equipamentosComprados.filter { it.dano != null }
            weapons.forEach { weapon ->
                val dmg = (weapon.dano as? kotlinx.serialization.json.JsonPrimitive)?.content ?: "-"
                val apVal = (weapon.pa as? kotlinx.serialization.json.JsonPrimitive)?.content
                val ap = if (!apVal.isNullOrBlank() && apVal != "0") "PA $apVal" else ""
                val range = (weapon.distancia as? kotlinx.serialization.json.JsonPrimitive)?.content ?: ""
                val stats = listOf(dmg, ap, range).filter { it.isNotBlank() && it != "-" }.joinToString(", ")
                val notes = (weapon.observacoes as? kotlinx.serialization.json.JsonPrimitive)?.content ?: ""
                CombatRow(name = weapon.nome.toFancyTitleCase(), stats = stats.ifBlank { dmg }, notes = notes)
            }

            Spacer(Modifier.height(12.dp))

            // --- DEFESA ---
            Text(text = "Armaduras", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            val armors = state.equipamentosComprados.filter { it.armadura != null || it.aparar != null }
            if (armors.isEmpty()) {
                Text(
                    "– Nenhuma",
                    style = MaterialTheme.typography.bodySmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            } else {
                armors.forEach { item ->
                    val armorVal = (item.armadura as? kotlinx.serialization.json.JsonPrimitive)?.content?.toIntOrNull()
                    val parryVal = (item.aparar as? kotlinx.serialization.json.JsonPrimitive)?.content?.toIntOrNull()

                    val parts = mutableListOf<String>()
                    if (armorVal != null && armorVal != 0) parts.add("Armadura +$armorVal")
                    if (parryVal != null && parryVal != 0) parts.add("Aparar +$parryVal")

                    val notes = (item.observacoes as? kotlinx.serialization.json.JsonPrimitive)?.content ?: ""
                    CombatRow(name = item.nome.toFancyTitleCase(), stats = parts.joinToString(", "), notes = notes)
                }
            }

            Spacer(Modifier.height(12.dp))

            // --- OUTROS ---
            Text(text = "Outros Itens", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            val others = state.equipamentosComprados.filter { it.dano == null && it.armadura == null && it.aparar == null }
            if (others.isEmpty()) {
                Text(
                    "– Nenhum",
                    style = MaterialTheme.typography.bodySmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            } else {
                others.forEach { item ->
                    Text("• ${item.nome.toFancyTitleCase()}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun CombatRow(name: String, stats: String, notes: String) {
    Column(Modifier.padding(vertical = 2.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(text = stats, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        }
        if (notes.isNotBlank()) {
            Text(text = notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun DerivedStatsRow(
    stats: List<Pair<String, String>>,
    onFamaChange: ((Int) -> Unit)? = null,
    onWealthChange: ((Int) -> Unit)? = null,
    wealthDieValue: String? = null
) {
    if (stats.isEmpty()) return

    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        stats.forEach { (label, value) ->
            if (label == "Fama" && onFamaChange != null) {
                EditableCircleStat(label = label, value = value, onDelta = onFamaChange)
            } else {
                CircleStat(label = label, value = value)
            }
        }

        // Se a riqueza deve ser mostrada como dado editável (apenas em progresso + regra ativa)
        if (wealthDieValue != null && onWealthChange != null) {
             EditableCircleStat(label = "Riqueza", value = wealthDieValue, onDelta = onWealthChange)
        }
    }
}

@Composable
fun EditableCircleStat(
    label: String,
    value: String,
    onDelta: (Int) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .background(MaterialTheme.colorScheme.surface, CircleShape)
                .clip(CircleShape)
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                textAlign = TextAlign.Center
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = { onDelta(-1) },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(Icons.Default.Remove, "Diminuir", modifier = Modifier.size(16.dp))
            }
            IconButton(
                onClick = { onDelta(1) },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(Icons.Default.Add, "Aumentar", modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun SummarySectionCard(
    section: SummarySection,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                text = section.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            SummarySectionItems(items = section.items, textStyle = textStyle)
        }
    }
}

@Composable
private fun SummarySectionItems(
    items: List<String>,
    textStyle: TextStyle
) {
    items.forEachIndexed { idx, item ->
        when {
            item.startsWith("•") -> BulletRow(text = item.removePrefix("•").trim(), textStyle = textStyle)
            item.contains(":") -> LabelValueRow(item, textStyle)
            else -> Text(text = item, style = textStyle)
        }
        if (idx != items.lastIndex) {
            Spacer(Modifier.height(6.dp))
        }
    }
}

@Composable
private fun LabelValueRow(raw: String, textStyle: TextStyle) {
    val label = raw.substringBefore(":").trim()
    val value = raw.substringAfter(":").trim()
    if (label.isBlank() || value.isBlank()) {
        Text(text = raw, style = textStyle)
        return
    }

    Text(
        buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                append(label)
                append(": ")
            }
            append(value)
        },
        style = textStyle
    )
}

@Composable
private fun BulletRow(text: String, textStyle: TextStyle) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .padding(top = 8.dp, end = 8.dp)
                .size(6.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
        )
        Text(text = text, style = textStyle)
    }
}

@Composable
fun CircleStat(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .background(MaterialTheme.colorScheme.surface, CircleShape)
                .clip(CircleShape)
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                textAlign = TextAlign.Center
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SpecializationsSummaryCard(
    state: CriadorState,
    modifier: Modifier = Modifier
) {
    var showEditDialog by rememberSaveable { mutableStateOf(false) }
    var editPerTarget by rememberSaveable { mutableStateOf<Pericia?>(null) }
    var editOldName by rememberSaveable { mutableStateOf("") }
    var editNewName by rememberSaveable { mutableStateOf("") }
    var editIsPrincipal by rememberSaveable { mutableStateOf(false) }

    val skillsWithSpecs = state.listaPericias.filter { per ->
        val hasPoints = state.rawTotal(per) > 0 || per.basica
        val specs = state.especializacoesPorPericia[per.nome]
        hasPoints && specs != null && (specs.principal != null || specs.lista.isNotEmpty())
    }

    if (skillsWithSpecs.isEmpty()) return

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                text = "Especializações",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))

            skillsWithSpecs.forEach { per ->
                val specDto = state.especializacoesPorPericia[per.nome] ?: return@forEach
                val list = mutableListOf<Pair<String, Boolean>>()
                specDto.principal?.let { list.add(it to true) }
                specDto.lista.filter { it != specDto.principal }.forEach { list.add(it to false) }

                Column(modifier = Modifier.fillMaxWidth()) {
                    val displayName =
                        if (state.compendioArteDaGuerraAtivo && per.nome.equals("Lutar", ignoreCase = true)) "Jutsu"
                        else per.nome
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    list.forEach { (name, isPrincipal) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
                        ) {
                            Text(
                                text = "• $name",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            IconButton(
                                onClick = {
                                    editPerTarget = per
                                    editOldName = name
                                    editNewName = name
                                    editIsPrincipal = isPrincipal
                                    showEditDialog = true
                                },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }

    if (showEditDialog && editPerTarget != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Renomear Especialização") },
            text = {
                Column {
                    Text("Perícia: ${editPerTarget!!.nome}")
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editNewName,
                        onValueChange = { if (it.length <= 50) editNewName = it },
                        label = { Text("Nome") },
                        supportingText = { Text("${editNewName.length}/50") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val per = editPerTarget!!
                        val novo = editNewName.trim()
                        if (novo.isNotEmpty()) {
                            val atual = state.especializacoesPorPericia[per.nome]
                            if (atual != null) {
                                val newDto = if (editIsPrincipal) {
                                    val listClean = atual.lista.filter { it != editOldName }
                                    atual.copy(principal = novo, lista = (listClean + novo).distinct())
                                } else {
                                    val newList = atual.lista.map { if (it == editOldName) novo else it }.distinct()
                                    atual.copy(lista = newList)
                                }
                                state.especializacoesPorPericia[per.nome] = newDto
                            }
                        }
                        showEditDialog = false
                    }
                ) { Text("Salvar") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun SkillChip(text: String) {
    val label = text.substringBefore(":").trim()
    val value = text.substringAfter(":").trim()

    androidx.compose.material3.Surface(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            if (value.isNotBlank() && value != label) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}

/**
 * Renderização compartilhada do retrato — usada tanto no card do Resumo quanto na
 * pré-visualização do ImageSettingsDialog, pra garantir que os dois fiquem sempre idênticos.
 */
@Composable
private fun PortraitImage(
    imageBitmap: ImageBitmap,
    scaleType: String,
    offsetY: Float,
    zoom: Float,
    modifier: Modifier = Modifier
) {
    val contentScale = if (scaleType == "FIT") ContentScale.Fit else ContentScale.Crop
    val alignment: Alignment = if (scaleType == "CROP") {
        BiasAlignment(horizontalBias = 0f, verticalBias = (offsetY * 2f - 1f).coerceIn(-1f, 1f))
    } else {
        Alignment.Center
    }
    val appliedZoom = if (scaleType == "CROP") zoom.coerceIn(1f, 2.5f) else 1f

    Image(
        bitmap = imageBitmap,
        contentDescription = "Retrato",
        contentScale = contentScale,
        alignment = alignment,
        modifier = modifier.graphicsLayer(scaleX = appliedZoom, scaleY = appliedZoom)
    )
}

