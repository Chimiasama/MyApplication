package com.example.swadebuilder.ui.sections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.R
import com.example.swadebuilder.SectionHeader
import com.example.swadebuilder.arcanoInfo              // mapa global montado no MainActivity
import com.example.swadebuilder.model.Poder
import com.example.swadebuilder.model.Vantagem         // tem subtipoArcano/choice
import com.example.swadebuilder.model.loadJsonAsset    // loader com JSON leniente
import com.example.swadebuilder.util.semAcentos

// ---------- Normalizações ----------
private fun String.normAAKey(): String =
    this.uppercase().semAcentos().trim()   // compatível com chaves de arcanoInfo

private fun Vantagem.toArcanoKeyFromModel(): String? {
    // prioridade 1: subtipoArcano vindo direto do JSON (Dom/Magia/Milagres/Psiônicos/Ciência Estranha)
    if (!subtipoArcano.isNullOrBlank()) return subtipoArcano.normAAKey()
    // prioridade 2: escolha feita na vantagem base (se estiver usando o agrupador com choice)
    if (!choice.isNullOrBlank()) return choice!!.normAAKey()
    // fallback: tenta deduzir do nome
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

// Conversor de custo (em PP) -> penalidade de teste (-⌈PP/2⌉)
private fun custoParaPenalidadeTexto(custo: String): String {
    val clean = custo.trim()
    clean.toIntOrNull()?.let { base -> return "-${(base + 1) / 2}" }
    if (clean.contains("/")) {
        return clean.split("/").joinToString("/") { p ->
            p.replace("+", "").trim().toIntOrNull()?.let { "-${(it + 1) / 2}" } ?: "—"
        }
    }
    if (clean.endsWith("+")) clean.removeSuffix("+").toIntOrNull()?.let { return "-${(it + 1) / 2}+" }
    if (clean.startsWith("+")) clean.removePrefix("+").toIntOrNull()?.let { return "-${(it + 1) / 2}" }
    return "—"
}

/** Seção **exclusiva** para PODERES de Antecedente Arcano. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PoderesSection(
    state: CriadorState,
    onOpenListaCompletaPoderes: () -> Unit
) {
    val context = LocalContext.current
    val locked = state.progresso > 0 && !state.emProgresso

    // 1) Detecta arcanos ativos
    val arcanosAtivos = remember(state.vantagensSelecionadas) {
        val ativos = state.vantagensSelecionadas.mapNotNull { it.toArcanoKeyFromModel() }.distinct()
        ativos
    }

    if (arcanosAtivos.isEmpty()) {
        return
    }

    var selectedArcanoKey by rememberSaveable(arcanosAtivos) {
        mutableStateOf(arcanosAtivos.first())
    }
    LaunchedEffect(arcanosAtivos) {
        if (selectedArcanoKey !in arcanosAtivos) selectedArcanoKey = arcanosAtivos.first()
    }

    // 2) Carrega poderes
    val allPoderes: List<Poder> = remember {
        val res: Result<List<Poder>> = runCatching {
            context.loadJsonAsset<List<Poder>>("poderes.json")
        }
        res.getOrElse { emptyList() }
    }

    // 3) Cabeçalho com Foco e (condicional) PP/penalidade
    val arcKey = selectedArcanoKey.normAAKey()
    val (slotsCount, ppTotal, foco) = arcanoInfo[arcKey] ?: Triple(0, 0, "—")

    val showListaCompleta = booleanResource(R.bool.show_lista_completa)
    val center = if (state.usarSemPontosDePoder) {
        "Teste $foco = -(custo/2)"
    } else {
        "PP: $ppTotal  •  $foco"
    }
    SectionHeader(
        onHelpClick = { /* ajuda */ },
        centerText  = center,
        onCenterClick = null,
        onListaCompletaClick = if (showListaCompleta) onOpenListaCompletaPoderes else null,
        listaCompletaText = "Lista Completa"
    )

    HorizontalDivider(thickness = 1.dp)

    // 4) Slots do arcano
    val slots = remember(arcKey, slotsCount) {
        val existente = state.poderSlotsPorArcano[arcKey]
        if (existente != null && existente.size == slotsCount) {
            existente
        } else {
            val nova = mutableStateListOf<String?>().apply {
                repeat(slotsCount) { add(null) }
            }
            state.poderSlotsPorArcano[arcKey] = nova
            nova
        }
    }

    // 5) Lista elegível (pode filtrar por foco/estágio depois)
    val poderesElegiveis = remember(allPoderes) { allPoderes }

    // 6) UI — lista simples, um toque alterna; item já escolhido fica esmaecido
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Bloco de slots
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

        // Lista de poderes — só o nome; toque simples alterna entre adicionar/remover
        poderesElegiveis.forEach { poder ->
            val selecionado = slots.any { it == poder.id }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .alpha(if (selecionado) 0.45f else 1f)
                    .clickable(enabled = !locked) {
                        if (selecionado) {
                            val idx = slots.indexOfFirst { it == poder.id }
                            if (idx >= 0) {
                                slots[idx] = null
                            }
                        } else {
                            val firstEmpty = slots.indexOfFirst { it == null }
                            if (firstEmpty >= 0) {
                                slots[firstEmpty] = poder.id
                            }
                        }
                    }
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text(poder.nome, fontWeight = FontWeight.Bold)
                    if (state.usarSemPontosDePoder) {
                        // Mostra a fórmula de teste no modo Sem PP
                        Text("Penalidade base: ${custoParaPenalidadeTexto(poder.pontosDePoder)}")
                    }
                }
            }
        }
    }
}
