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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.buildSummaryLines
import com.example.swadebuilder.model.StorageUtils
import com.example.swadebuilder.toMeuPersonagem
import com.example.swadebuilder.util.keyify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun SummaryContent(state: CriadorState) {

    val context = LocalContext.current
    val nomesSalvos = remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }

    LaunchedEffect(Unit) {
        nomesSalvos.value = withContext(Dispatchers.IO) {
            StorageUtils.listarPersonagens(context)
        }
    }

    LaunchedEffect(nomesSalvos.value) {
        if (state.nomePersonagem.isBlank()) {
            var idx = 1
            while (nomesSalvos.value.any { it.first == "Nome $idx" }) {
                idx++
            }
            state.nomePersonagem = "Nome $idx"
        }
    }

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

    val personagem = state.toMeuPersonagem()
    val allLines = buildSummaryLines(personagem)

    val anotIndex = allLines.indexOf("Anotações")
    val lines = if (anotIndex >= 0) allLines.take(anotIndex) else allLines

    val headers = remember {
        setOf(
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
    }

    val sections = remember(lines) { lines.toSummarySections(headers) }

    val identitySection = sections.firstOrNull { it.title == "Identidade" }
    val derivedSection = sections.firstOrNull { it.title == "Atributos derivados" }
    val attributesSection = sections.firstOrNull { it.title == "Atributos" }
    val skillsSection = sections.firstOrNull { it.title == "Perícias" }

    val hasMusculoso = state.vantagensSelecionadas.any { it.nome.keyify() == "musculoso" }
    val hasSoldado = state.vantagensSelecionadas.any { it.nome.keyify() == "soldado" }
    val bonusCapacity = (if (hasMusculoso) 10f else 0f) + (if (hasSoldado) 10f else 0f)
    val strengthRaw = state.valoresAtributos["FORCA"]?.intValue ?: 4
    val baseLimit = ((strengthRaw - 2) / 2) * 10f
    val weightLimit = baseLimit + bonusCapacity
    val totalWeight = state.equipamentosComprados
        .mapNotNull { item ->
            (item.peso as? kotlinx.serialization.json.JsonPrimitive)
                ?.content
                ?.replace(",", ".")
                ?.toFloatOrNull()
        }
        .sum()
    val ratio = if (weightLimit > 0f) totalWeight / weightLimit else Float.POSITIVE_INFINITY
    val weightWarning = when {
        ratio >= 4f -> "Impossível carregar tanto peso, remova itens ou aumente a força."
        ratio >= 3f -> "Peso extremo! Penalidades severas de sobrecarga."
        ratio > 1f -> "Peso excedido! Você está sobrecarregado."
        else -> null
    }

    val sectionsWithWeight = sections.map { section ->
        if (section.title == "Recursos & Equipamentos") {
            val weightLine = "Peso: ${"%.1f".format(totalWeight)} / ${"%.1f".format(weightLimit)}"
            val updatedItems = buildList {
                add(weightLine)
                weightWarning?.let { add("• $it") }
                addAll(section.items)
            }
            section.copy(items = updatedItems)
        } else {
            section
        }
    }

    val otherSections = sectionsWithWeight.filterNot {
        it.title in setOf("Identidade", "Atributos derivados", "Atributos", "Perícias")
    }

    val nome = state.nomePersonagem

    val ancestralidadeValue = identitySection?.items
        ?.firstOrNull { it.startsWith("Ancestralidade:") }
        ?.substringAfter(":")
        ?.trim()
        .orEmpty()
        .ifBlank { "–" }

    Column(Modifier.fillMaxWidth()) {
        IdentityCard(
            nome = nome,
            onNomeChange = { state.nomePersonagem = it },
            ancestralidade = "Ancestralidade: $ancestralidadeValue"
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
                        modifier = Modifier.weight(0.45f)
                    )
                }
                skillsSection?.let {
                    SummarySectionCard(
                        section = it,
                        modifier = Modifier.weight(0.55f)
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        val filteredSections = otherSections.filterNot { section ->
            when (section.title) {
                "Vantagens", "Complicações" ->
                    section.items.none { it.trim() != "– Nenhuma" }

                else -> false
            }
        }

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

private data class SummarySection(
    val title: String,
    val items: List<String>
)

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
private fun SummarySectionCard(section: SummarySection, modifier: Modifier = Modifier) {
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
            section.items.forEach { item ->
                when {
                    item.startsWith("•") -> BulletRow(text = item.removePrefix("•").trim())
                    item.contains(":") -> LabelValueRow(item)
                    else -> Text(text = item, style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun LabelValueRow(raw: String) {
    val label = raw.substringBefore(":").trim()
    val value = raw.substringAfter(":").trim()
    if (label.isBlank() || value.isBlank()) {
        Text(text = raw, style = MaterialTheme.typography.bodyMedium)
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
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
private fun BulletRow(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "•",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(end = 6.dp)
        )
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
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
                .size(52.dp)
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
