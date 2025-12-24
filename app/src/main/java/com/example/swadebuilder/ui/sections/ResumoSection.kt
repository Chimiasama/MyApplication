package com.example.swadebuilder.ui.sections

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

@Composable
fun SummaryContent(state: CriadorState) {

    LocalContext.current

    val flagsTemplate = remember(state) {
        listOfNotNull(
            "Carta Selvagem".takeIf { state.cartaSelvagem },
            "Heróis Sem Armadura".takeIf { state.heroisSemArmadura },
            "Sem Pontos de Poder".takeIf { state.usarSemPontosDePoder },
            "Mais Pontos de Perícias".takeIf { state.maisPontosPericias },
            "Especializações".takeIf { state.usarEspecializacoesDePericia }
        )
    }

    LaunchedEffect(flagsTemplate) {
        if (state.anotacoes.isBlank() && flagsTemplate.isNotEmpty()) {
            state.anotacoes = flagsTemplate.joinToString("\n") { "• $it" }
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
            ancestralidade = "Ancestralidade: $ancestralidadeValue$monstroInfo"
        )

        Spacer(Modifier.height(12.dp))

        derivedSection?.let {
            DerivedStatsRow(stats = it.toStats())
            Spacer(Modifier.height(12.dp))
        }

        if (attributesSection != null || skillsSection != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                attributesSection?.let {
                    SummarySectionCard(
                        section = it,
                        modifier = Modifier.weight(0.42f),
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp)
                    )
                }
                skillsSection?.let {
                    SummarySectionCard(
                        section = it,
                        modifier = Modifier.weight(0.58f)
                    )
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
            onValueChange = { state.anotacoes = it },
            label = { Text("Anotações") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
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
            ancestralidade = "Ancestralidade: $ancestralidadeValue$monstroInfo"
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
    ancestralidade: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = nome,
                onValueChange = onNomeChange,
                label = { Text("Nome do Personagem") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = ancestralidade,
                style = MaterialTheme.typography.bodyMedium,
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
            val (unarmedDmg, unarmedNotes) = state.calculaAtaqueDesarmado()
            CombatRow(name = "Desarmado", stats = unarmedDmg, notes = unarmedNotes)

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
                Text("– Nenhuma", style = MaterialTheme.typography.bodySmall)
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
                Text("– Nenhum", style = MaterialTheme.typography.bodySmall)
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
private fun DerivedStatsRow(stats: List<Pair<String, String>>) {
    if (stats.isEmpty()) return

    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        stats.forEach { (label, value) ->
            CircleStat(label = label, value = value)
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
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "•",
            style = textStyle,
            modifier = Modifier.padding(end = 6.dp)
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
                .clip(CircleShape)
                .border(
                    width = 1.dp,
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
                        onValueChange = { editNewName = it },
                        label = { Text("Nome") },
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
