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

    // 1) Carrega todo o JSON de poderes diretamente como List<Poder>
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

    // 4) Identifica se existe alguma Vantagem de AA e qual foi a opção escolhida (ex: "Dom")
    val vantAA = state.vantagensSelecionadas
        .firstOrNull { it.id == "antecedente_arcano" }

    // 5) Se não existir, versionKey será "", senão, normaliza a escolha (e.g. "Dom" → "dom")
    val versionKey = vantAA
        ?.choice
        ?.keyify()
        ?: ""

    // 6) Busca no mapa de arcanoInfo os dados daquele Arcano (slots e PP)
    val infoDoArcano: ArcanoInfoItem? = arcanoInfoMap[versionKey]
    val initialSlots: Int = infoDoArcano?.slots ?: 0
    val basePP: Int       = infoDoArcano?.pp    ?: 0

    // 7) Usa o estado para criar e persistir a lista de slots conforme initialSlots
    val slots = state.poderSlotsPorArcano.getOrPut(versionKey) {
        mutableStateListOf<String?>().apply {
            repeat(initialSlots) {
                add(null)
            }
        }
    }

    // 8) Quantos slots há, quantos estão preenchidos e quantos sobraram
    val totalSlots = slots.size
    val usedCount  = slots.count { it != null }
    val remainingSlots = totalSlots - usedCount

    // 9) Cálculo de Pontos de Poder: base (do arcano) + bônus de estado
    val bonusPP: Int = state.bonusPoderExtra
    val pp = basePP + bonusPP

    // 10) Diálogo de ajuda
    var showHelp by rememberSaveable { mutableStateOf(false) }
    val showLista = booleanResource(com.example.swadebuilder.R.bool.show_lista_completa)


    SectionHeader(
        onHelpClick          = { showHelp = true },
        centerText           = "Pontos de Poder: $pp",
        onListaCompletaClick = if (showLista) onOpenListaCompletaPoderes else null,
        listaCompletaText    = "Lista Completa"
    )

    if (showHelp) {
        AlertDialog(
            onDismissRequest = { showHelp = false },
            title            = { Text("Como funciona") },
            text             = { Text("Toque em um poder para alocá-lo em um dos espaços obtidos pelo Antecedente Arcano correspondente.") },
            confirmButton    = {
                TextButton(onClick = { showHelp = false }) {
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
            val canRemove = (poderId != null && !locked)
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
