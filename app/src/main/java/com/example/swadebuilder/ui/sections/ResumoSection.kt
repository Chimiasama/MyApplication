package com.example.swadebuilder.ui.sections

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.MoodBad
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsMartialArts
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
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
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.Pericia
import com.example.swadebuilder.buildSummaryLines
import com.example.swadebuilder.listaPericias
import com.example.swadebuilder.toMeuPersonagem
import com.example.swadebuilder.util.keyify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max

private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    val (height: Int, width: Int) = options.run { outHeight to outWidth }
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {
        val halfHeight: Int = height / 2
        val halfWidth: Int = width / 2

        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}

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
        icons.add(Icons.Default.MenuBook to Color(0xFF6D4C41)) // Brown 600
    }

    return icons
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SummaryContent(
    state: CriadorState,
    imageUri: Uri? = null,
    onSelectImage: () -> Unit = {}
) {

    val context = LocalContext.current

    val flagsTemplate = remember(state) {
        listOfNotNull(
            "Carta Selvagem".takeIf { state.cartaSelvagem },
            "Heróis Sem Armadura".takeIf { state.heroisSemArmadura },
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

    val sections = rememberSummarySections(state)

    val identitySection = sections.firstOrNull { it.title == "Identidade" }
    val derivedSection = sections.firstOrNull { it.title == "Atributos derivados" }
    val attributesSection = sections.firstOrNull { it.title == "Atributos" }
    val skillsSection = sections.firstOrNull { it.title == "Perícias" }

    val hasMusculoso = state.vantagensSelecionadas.any { it.nome.keyify() == "MUSCULOSO" }
    val hasSoldado = state.vantagensSelecionadas.any { it.nome.keyify() == "SOLDADO" }
    val bonusCapacity = if (hasMusculoso) 10f else 0f
    val strengthRaw = state.valoresAtributos["FORCA"]?.intValue ?: 4
    val effectiveStrengthForLoad = if (hasSoldado && state.soldadoCargaAtivo) {
        if (strengthRaw < 12) strengthRaw + 2 else strengthRaw + 1
    } else {
        strengthRaw
    }
    val baseLimit = ((effectiveStrengthForLoad - 2) / 2) * 10f
    val weightLimit = baseLimit + bonusCapacity
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
        state.valoresAtributos["VIGOR"]?.intValue ?: 4
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
            hasSoldado && state.soldadoCargaAtivo -> " (Soldado +1 dado)"
            hasSoldado -> " (Soldado inativo)"
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

    val ancestralidadeValue = identitySection?.items
        ?.firstOrNull { it.startsWith("Ancestralidade:") }
        ?.substringAfter(":")
        ?.trim()
        .orEmpty()
        .ifBlank { "–" }

    val monstroInfo = if (state.modoMonstroAtivo) {
        val tipoNome = com.example.swadebuilder.listaMonstroTemplates.find { it.id == state.tipoMonstroSelecionado }?.nome ?: "Desconhecido"
        "\nTipo de Monstro: $tipoNome"
    } else ""

    Column(Modifier.fillMaxWidth()) {
        IdentityCard(
            nome = nome,
            onNomeChange = { state.nomePersonagem = it },
            ancestralidade = "Ancestralidade: $ancestralidadeValue$monstroInfo",
        activeCompendiums = if (state.mostrarIdentificadorLivro) getCompendiumIcons(state) else emptyList()
        )

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
                wealthDieValue = if (showWealthControl) "d${state.dadoRiqueza}" else null
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
                val imageBitmapState = produceState<ImageBitmap?>(initialValue = null, imageUri) {
                    value = if (imageUri == null) {
                        null
                    } else {
                        withContext(Dispatchers.IO) {
                            runCatching {
                                val maxSide = 1024
                                val options = BitmapFactory.Options().apply {
                                    inJustDecodeBounds = true
                                }
                                context.contentResolver.openInputStream(imageUri)?.use { stream ->
                                    BitmapFactory.decodeStream(stream, null, options)
                                }

                                options.inSampleSize = calculateInSampleSize(options, maxSide, maxSide)
                                options.inJustDecodeBounds = false

                                context.contentResolver.openInputStream(imageUri)?.use { stream ->
                                    val sampled = BitmapFactory.decodeStream(stream, null, options)
                                        ?: return@runCatching null

                                    val currentMax = max(sampled.width, sampled.height)
                                    val finalBitmap = if (currentMax > maxSide) {
                                         val ratio = maxSide.toFloat() / currentMax.toFloat()
                                         val targetWidth = (sampled.width * ratio).toInt().coerceAtLeast(1)
                                         val targetHeight = (sampled.height * ratio).toInt().coerceAtLeast(1)
                                         Bitmap.createScaledBitmap(sampled, targetWidth, targetHeight, true)
                                    } else {
                                         sampled
                                    }
                                    finalBitmap.asImageBitmap()
                                }
                            }.getOrNull()
                        }
                    }
                }

                var showImageSettings by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier
                        .weight(1f)
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
                        if (imageBitmap != null) {
                            val scale = if (state.portraitScaleType == "FIT") ContentScale.Fit else ContentScale.Crop
                            val align = when (state.portraitAlignment) {
                                "TOP" -> Alignment.TopCenter
                                "BOTTOM" -> Alignment.BottomCenter
                                else -> Alignment.Center
                            }

                            Image(
                                bitmap = imageBitmap,
                                contentDescription = "Retrato",
                                contentScale = scale,
                                alignment = align,
                                modifier = Modifier.fillMaxSize()
                            )
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

                        if (imageBitmap != null) {
                            IconButton(
                                onClick = { showImageSettings = true },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Ajustes da Foto",
                                    tint = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                    modifier = Modifier
                                        .background(
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                            CircleShape
                                        )
                                        .padding(4.dp)
                                )
                            }
                        }
                    }
                }

                if (showImageSettings) {
                    ImageSettingsDialog(
                        state = state,
                        onDismiss = { showImageSettings = false }
                    )
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
fun BasicCharacterInfo(
    state: CriadorState,
    showDerivedStats: Boolean = false
) {
    val sections = rememberSummarySections(state)
    val identitySection = sections.firstOrNull { it.title == "Identidade" }
    val derivedSection = sections.firstOrNull { it.title == "Atributos derivados" }

    val nome = state.nomePersonagem
    val ancestralidadeValue = identitySection?.items
        ?.firstOrNull { it.startsWith("Ancestralidade:") }
        ?.substringAfter(":")
        ?.trim()
        .orEmpty()
        .ifBlank { "–" }

    val monstroInfo = if (state.modoMonstroAtivo) {
        val tipoNome = com.example.swadebuilder.listaMonstroTemplates
            .find { it.id == state.tipoMonstroSelecionado }
            ?.nome
            ?: "Desconhecido"
        "\nTipo de Monstro: $tipoNome"
    } else {
        ""
    }

    Column(Modifier.fillMaxWidth()) {
        IdentityCard(
            nome = nome,
            onNomeChange = { state.nomePersonagem = it },
            ancestralidade = "Ancestralidade: $ancestralidadeValue$monstroInfo",
        activeCompendiums = if (state.mostrarIdentificadorLivro) getCompendiumIcons(state) else emptyList()
        )

        if (showDerivedStats) {
            Spacer(Modifier.height(12.dp))
            derivedSection?.let {
                DerivedStatsRow(stats = it.toStats())
            }
        }
    }
}

@Composable
fun SummaryCompact(state: CriadorState) {
    val sections = rememberSummarySections(state)
    val traitsSection = sections.firstOrNull { it.title == "Atributos" }
    val skillsSection = sections.firstOrNull { it.title == "Perícias" }
    val gearSection = sections.firstOrNull { it.title == "Recursos & Equipamentos" }
    val inventorySections = sections.filter { it.title in inventoryTitles }
        .filterNot { it.isEmptyPlaceholder() }

    val cards = listOfNotNull(
        traitsSection?.let { "Traits" to listOf(it) },
        skillsSection?.let { "Skills" to listOf(it) },
        gearSection?.let { "Gear" to listOf(it) },
        inventorySections.takeIf { it.isNotEmpty() }?.let { "Inventory" to it }
    )

    Column(Modifier.fillMaxWidth()) {
        cards.forEachIndexed { idx, (title, cardSections) ->
            SummaryCompactCard(
                title = title,
                sections = cardSections,
                textStyle = MaterialTheme.typography.bodySmall
            )
            if (idx != cards.lastIndex) {
                Spacer(Modifier.height(12.dp))
            }
        }
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

private val inventoryTitles = setOf(
    "Vantagens",
    "Complicações",
    "Poderes arcanos",
    "Superpoderes"
)

@Composable
private fun rememberSummarySections(state: CriadorState): List<SummarySection> {
    val personagem = state.toMeuPersonagem()
    val allLines = buildSummaryLines(personagem)
    val anotIndex = allLines.indexOf("Anotações")
    val lines = if (anotIndex >= 0) allLines.take(anotIndex) else allLines

    return remember(lines) { lines.toSummarySections(summaryHeaders) }
}

private fun SummarySection.isEmptyPlaceholder(): Boolean =
    items.all { item ->
        val trimmed = item.trim()
        trimmed == "– Nenhuma" || trimmed == "– Nenhum"
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
                CombatRow(name = displayName, stats = dmg, notes = notes)
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
                CombatRow(name = weapon.nome, stats = stats.ifBlank { dmg }, notes = notes)
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
                    CombatRow(name = item.nome, stats = parts.joinToString(", "), notes = notes)
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
                    Text("• ${item.nome}", style = MaterialTheme.typography.bodySmall)
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
private fun SummaryCompactCard(
    title: String,
    sections: List<SummarySection>,
    textStyle: TextStyle
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
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            sections.forEachIndexed { idx, section ->
                if (sections.size > 1) {
                    Text(
                        text = section.title,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(6.dp))
                }
                SummarySectionItems(items = section.items, textStyle = textStyle)
                if (idx != sections.lastIndex) {
                    Spacer(Modifier.height(10.dp))
                }
            }
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

    val skillsWithSpecs = listaPericias.filter { per ->
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
                    Text(
                        text = per.nome,
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

@Composable
private fun ImageSettingsDialog(
    state: CriadorState,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajustes da Foto") },
        text = {
            Column {
                Text("Modo de Exibição", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.RadioButton(
                        selected = state.portraitScaleType == "CROP",
                        onClick = { state.portraitScaleType = "CROP" }
                    )
                    Text(
                        "Preencher (Corte)",
                        modifier = Modifier.clickable { state.portraitScaleType = "CROP" },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.RadioButton(
                        selected = state.portraitScaleType == "FIT",
                        onClick = { state.portraitScaleType = "FIT" }
                    )
                    Text(
                        "Ajustar (Inteiro)",
                        modifier = Modifier.clickable { state.portraitScaleType = "FIT" },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (state.portraitScaleType == "CROP") {
                    Spacer(Modifier.height(16.dp))
                    Text("Alinhamento", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf("TOP" to "Topo", "CENTER" to "Centro", "BOTTOM" to "Baixo").forEach { (key, label) ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                androidx.compose.material3.RadioButton(
                                    selected = state.portraitAlignment == key,
                                    onClick = { state.portraitAlignment = key }
                                )
                                Text(
                                    label,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.clickable { state.portraitAlignment = key }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Concluir") }
        }
    )
}
