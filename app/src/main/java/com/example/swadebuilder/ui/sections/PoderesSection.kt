package com.example.swadebuilder.ui.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.R
import com.example.swadebuilder.arcanoInfo
import com.example.swadebuilder.criacaoBasicaCongeladaComXp
import com.example.swadebuilder.model.Poder
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.model.loadJsonAsset
import com.example.swadebuilder.ui.components.PoderListItem
import com.example.swadebuilder.ui.components.SectionHeader
import com.example.swadebuilder.util.semAcentos

private fun String.normAAKey(): String =
    this.uppercase().semAcentos().trim()

private fun Vantagem.toArcanoKeyFromModel(): String? {
    if (!subtipoArcano.isNullOrBlank()) return subtipoArcano.normAAKey()
    if (!choice.isNullOrBlank()) return choice!!.normAAKey()
    val n = nome.normAAKey()
    return when {
        "(DOM" in n -> "DOM"
        "(MAGIA" in n -> "MAGIA"
        "(MILAGRES" in n -> "MILAGRES"
        ("(PSIONICOS" in n) || ("(PSIÔNICOS" in nome) -> "PSIONICOS"
        ("(CIENCIA ESTRANHA" in n) || ("(CIÊNCIA ESTRANHA" in nome) -> "CIENCIA ESTRANHA"
        else -> null
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PoderesSection(
    state: CriadorState
) {
    val context = LocalContext.current

    val arcanosAtivos = remember(state.vantagensSelecionadas) {
        state.vantagensSelecionadas.mapNotNull { it.toArcanoKeyFromModel() }.distinct()
    }
    if (arcanosAtivos.isEmpty()) return

    val allPoderes: List<Poder> = remember {
        runCatching { context.loadJsonAsset<List<Poder>>("poderes.json") }.getOrElse { emptyList() }
    }
    val poderesElegiveis = remember(allPoderes) { allPoderes }

    if (!state.permiteMultiAntecedenteArcano) {
        var selectedArcanoKey by rememberSaveable(arcanosAtivos) { mutableStateOf(arcanosAtivos.first()) }
        LaunchedEffect(arcanosAtivos) {
            if (selectedArcanoKey !in arcanosAtivos) selectedArcanoKey = arcanosAtivos.first()
        }

        ArcanoArea(
            arcKeyRaw = selectedArcanoKey,
            state = state,
            poderesElegiveis = poderesElegiveis,
            locked = state.criacaoBasicaCongeladaComXp
        )
        return
    }

    arcanosAtivos.forEach { arcKeyRaw ->
        ArcanoArea(
            arcKeyRaw = arcKeyRaw,
            state = state,
            poderesElegiveis = poderesElegiveis,
            locked = state.criacaoBasicaCongeladaComXp
        )
        Spacer(Modifier.height(4.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ArcanoArea(
    arcKeyRaw: String,
    state: CriadorState,
    poderesElegiveis: List<Poder>,
    locked: Boolean
) {
    val arcKey = arcKeyRaw.normAAKey()
    val (slotsCount, ppTotal, foco) = arcanoInfo[arcKey] ?: Triple(0, 0, "—")
    val showListaCompleta = booleanResource(R.bool.show_lista_completa)
    val center = if (state.usarSemPontosDePoder) {
        "Teste $foco = -(custo/2)"
    } else {
        "PP: $ppTotal  •  $foco"
    }

    SectionHeader(
        onHelpClick = null,
        centerText  = center,
        onCenterClick = null,
        onListaCompletaClick = null,
        listaCompletaText = "Poderes"
    )

    HorizontalDivider(thickness = 1.dp)

    val slots = remember(arcKey, slotsCount) {
        val existente = state.poderSlotsPorArcano[arcKey]
        if (existente != null && existente.size == slotsCount) {
            existente
        } else {
            val nova = mutableStateListOf<String?>().apply { repeat(slotsCount) { add(null) } }
            state.poderSlotsPorArcano[arcKey] = nova
            nova
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text("Slots: $slotsCount", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                slots.forEachIndexed { idx, poderId ->
                    val label = poderId ?: "— vazio —"
                    AssistChip(
                        onClick = {
                            if (!locked && poderId != null) {
                                slots[idx] = null
                            }
                        },
                        label = { Text("${idx + 1}: $label") },
                        enabled = !locked && poderId != null
                    )
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 400.dp)
            .padding(bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = poderesElegiveis,
            key = { it.id }
        ) { poder ->
            val selecionado = slots.any { it == poder.id }
            PoderListItem(
                poder = poder,
                isClickable = !locked,
                onClick = {
                    if (selecionado) {
                        val idx = slots.indexOfFirst { it == poder.id }
                        if (idx >= 0) slots[idx] = null
                    } else {
                        val firstEmpty = slots.indexOfFirst { it == null }
                        if (firstEmpty >= 0) slots[firstEmpty] = poder.id
                    }
                }
            )
        }
    }
}
