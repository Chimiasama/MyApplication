package com.example.swadebuilder.ui.sections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.SectionHeader
import com.example.swadebuilder.listaDeEstagios
import com.example.swadebuilder.model.Poder
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.model.loadJsonAsset
import com.example.swadebuilder.util.keyify


@kotlinx.serialization.Serializable
data class ArcanoInfoItem(
    val key: String,
    val slots: Int,
    val pp: Int,
    val foco: String
)

@Composable
fun PoderesSection(
    state: CriadorState,
    onOpenListaCompletaPoderes: () -> Unit
) {
    // 0) Estado de "bloqueio": quando já estiver em progresso e não for edição.
    val locked = state.progresso > 0 && !state.emProgresso
    val context = LocalContext.current

    // 1) Carrega o JSON de poderes diretamente como List<Poder>
    val allPoderes: List<Poder> = remember {
        context.loadJsonAsset<List<Poder>>("poderes.json")
    }

    // 2) Cria um Map para buscar dados de um poder pelo seu ‘ID’
    val poderesMap: Map<String, Poder> = remember(allPoderes) {
        allPoderes.associateBy { it.id }
    }

    // 3) Carrega arcano_info.json e monta um Map<versãoKey, ArcanoInfoItem>
    val arcanoInfoMap: Map<String, ArcanoInfoItem> = remember {
        val listaArcano: List<ArcanoInfoItem> = context.loadJsonAsset("arcano_info.json")
        // Associe usando keyify para normalizar (e.g. "DOM" vira "dom")
        listaArcano.associateBy { it.key.keyify() }
    }

    // === SUPORTE A MÚLTIPLOS ANTECEDENTES ARCANOS ===
    // Helper para mapear uma vantagem (base ou específica) para a "key" usada no arcano_info.json
    fun arcanoKeyFromVant(v: Vantagem): String? {
        return when {
            v.id == "antecedente_arcano" -> v.choice?.keyify()
            v.id.endsWith("_dom") -> "dom".keyify()
            v.id.endsWith("_magia") -> "magia".keyify()
            v.id.endsWith("_milagres") -> "milagres".keyify()
            v.id.endsWith("_psionicos") -> "psionicos".keyify()
            v.id.endsWith("_ciancia_estranha") || v.id.endsWith("_ciencia_estranha") -> "ciencia estranha".keyify()
            else -> null
        }
    }

    // 4) Descobre TODOS os arcanos ativos no personagem (base+específicos)
    val arcanosAtivos: List<String> = remember(state.vantagensSelecionadas) {
        state.vantagensSelecionadas.mapNotNull { arcanoKeyFromVant(it) }.distinct()
    }

    // 5) Seleciona qual arcano está "ativo" na UI (se houver mais de um, o usuário escolhe)
    var selectedArcanoKey by rememberSaveable(arcanosAtivos) {
        mutableStateOf(arcanosAtivos.firstOrNull())
    }
    // Se a seleção ficou inválida (ex.: removeu um AA), realinha
    if (selectedArcanoKey != null && selectedArcanoKey !in arcanosAtivos) {
        selectedArcanoKey = arcanosAtivos.firstOrNull()
    }

    // 6) Dados do arcano selecionado (slots/pp) — se não houver arcano, fica tudo 0
    val infoDoArcano: ArcanoInfoItem? = selectedArcanoKey?.let { key -> arcanoInfoMap[key] }
    val initialSlots: Int = infoDoArcano?.slots ?: 0
    val basePP: Int       = infoDoArcano?.pp    ?: 0

    // 7) Slots do arcano selecionado: persistimos por 'selectedArcanoKey'
    val slots = if (selectedArcanoKey != null) {
        state.poderSlotsPorArcano.getOrPut(selectedArcanoKey!!) {
            mutableStateListOf<String?>().apply { repeat(initialSlots) { add(null) } }
        }
    } else {
        // sem AA selecionado — lista vazia somente para não quebrar a UI
        remember { mutableStateListOf() }
    }

    // 8) Quantos slots há, quantos estão preenchidos e quantos sobraram
    val totalSlots = slots.size
    val usedCount  = slots.count { it != null }
    val remainingSlots = totalSlots - usedCount

    // 9) Pontos de Poder = base (do arcano selecionado) + bônus do estado
    val bonusPP: Int = state.bonusPoderExtra
    val pp = basePP + bonusPP

    // 10) Diálogo de ajuda
    val showPoderesHelp = rememberSaveable { mutableStateOf(false) }
    val showLista = booleanResource(com.example.swadebuilder.R.bool.show_lista_completa)

    val center = if (state.usarSemPontosDePoder) {
        "Sem Pontos de Poder (teste penalidade -1/2 do custo pra cima)"
    } else {
        "Pontos de Poder: $pp"
    }
    // Se houver mais de um arcano ativo, mostre chips para alternar a visão
    if (arcanosAtivos.size > 1) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 8.dp)
        ) {
            arcanosAtivos.forEach { key ->
                val label = when (key) {
                    "dom" -> "Dom"
                    "magia" -> "Magia"
                    "milagres" -> "Milagres"
                    "psionicos" -> "Psiônicos"
                    "cienciaestranha", "ciencia_estranha", "ciencia estranha" -> "Ciência Estranha"
                    else -> key
                }
                AssistChip(
                    onClick = { selectedArcanoKey = key },
                    label = { Text(label) },
                    enabled = selectedArcanoKey != key
                )
            }
        }
    }

    SectionHeader(
        onHelpClick          = { showPoderesHelp.value = true },
        centerText           = center,
        onListaCompletaClick = if (showLista) onOpenListaCompletaPoderes else null,
        listaCompletaText    = "Lista Completa"
    )

    if (showPoderesHelp.value) {
        AlertDialog(
            onDismissRequest = { showPoderesHelp.value = false },
            title            = { Text("Como funciona") },
            text             = { Text("Toque em um poder para alocá-lo em um dos espaços obtidos pelo Antecedente Arcano correspondente.") },
            confirmButton    = {
                TextButton(onClick = { showPoderesHelp.value = false }) {
                    Text("OK")
                }
            }
        )
    }

    // 11) Exibe os “slots” atuais como chips (com possibilidade de remover cada slot, se não locked)
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        slots.forEachIndexed { idx, poderId ->
            val canRemove = (poderId != null && !locked && !state.emProgresso)
            AssistChip(
                onClick = {
                    if (canRemove) {
                        // ao remover, colocamos o slot como null e atualizamos state.poderesSelecionados
                        slots[idx] = null
                        state.poderesSelecionados.apply {
                            clear()
                            addAll(slots.filterNotNull())
                        }
                    }
                },
                enabled = canRemove,
                modifier = Modifier.alpha(if (canRemove) 1f else 0.3f),
                label = {
                    val nomeDoPoder = poderId
                        ?.let { poderesMap[it]?.nome ?: it }
                        ?: "-"
                    Text(nomeDoPoder)
                },
                leadingIcon = {
                    if (poderId != null) {
                        Icon(Icons.Default.Close, contentDescription = "Remover poder")
                    }
                }
            )
        }
    }

    // 12) Listagem (LazyColumn) de poderes “BÁSICO” disponíveis para alocar:
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 200.dp)
            .padding(bottom = 16.dp)
    ) {
        // a) Índice do estágio atual do personagem
        val curEstagioIdx = listaDeEstagios.indexOfFirst { it.nome == state.estagioAtual().nome }

        // b) Filtra apenas poderes cuja origem seja “BASICO”, que caibam no estágio atual
        //    e cujo ‘ID’ ainda não esteja em nenhum slot
        val disponiveis: List<Poder> = allPoderes
            .filter { poder -> poder.origem.equals("BASICO", ignoreCase = true) }
            .filter { poder ->
                val idxReq = listaDeEstagios.indexOfFirst { it.nome.equals(poder.estagio, ignoreCase = true) }
                idxReq in 0..curEstagioIdx
            }
            .filter { poder ->
                slots.none { it == poder.id }
            }

        if (disponiveis.isEmpty()) {
            item {
                Text("Nenhum poder disponível…", Modifier.padding(16.dp))
            }
        } else {
            items(disponiveis) { poder ->
                Column {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .alpha(if (remainingSlots > 0) 1f else 0.3f)
                            .clickable(enabled = (remainingSlots > 0)) {
                                // Só permite alocar se ainda houver ao menos um slot vazio
                                if (remainingSlots > 0) {
                                    val firstEmpty = slots.indexOfFirst { it == null }
                                    if (firstEmpty >= 0) {
                                        slots[firstEmpty] = poder.id
                                        state.poderesSelecionados.apply {
                                            clear()
                                            addAll(slots.filterNotNull())
                                        }
                                    }
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(poder.nome)
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}
