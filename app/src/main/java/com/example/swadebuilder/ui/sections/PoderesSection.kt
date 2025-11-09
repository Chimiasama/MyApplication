package com.example.swadebuilder.ui.sections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.SectionHeader
import com.example.swadebuilder.listaDeEstagios
import com.example.swadebuilder.model.CriadorViewModel
import com.example.swadebuilder.model.Poder
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.model.loadJsonAsset
import com.example.swadebuilder.ui.dialogs.SupersDialog
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
    onOpenListaCompletaPoderes: () -> Unit,
    viewModel: CriadorViewModel? = null   // <-- tipo corrigido
) {
    // Se for MODO SUPERS, mostramos um cabeçalho simples e um botão para abrir o SupersDialog,
    // mantendo o ProgressosDialog totalmente intacto (fase anterior).
    if (state.modoSupers) {
        var showDialog by rememberSaveable { mutableStateOf(false) }
        var showNivelDialog by rememberSaveable { mutableStateOf(false) }
        var nivelEscolhido by rememberSaveable { mutableStateOf("III") }

        val avisoFav = remember(state.vantagensSelecionadas, state.idPoderFavorecido) {
            val temOMelhor = state.vantagensSelecionadas.any { it.nome.keyify() == "O MELHOR QUE HÁ".keyify() }
            temOMelhor && state.idPoderFavorecido.isNullOrEmpty()
        }

        SectionHeader(
            onHelpClick = { /* help opcional */ },
            centerText  = "Supers • Pontos: ${state.superPontosDisponiveis}/${state.superPontosTotais} • Limite padrão: ${state.limitePorPoderPadrao} • Favorecido: ${state.limiteFavorecido}",
            onListaCompletaClick = onOpenListaCompletaPoderes,
            listaCompletaText = "Lista Completa"
        )

        if (avisoFav) {
            Text(
                "Defina o poder favorecido (O MELHOR QUE HÁ) para liberar gastos.",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { showNivelDialog = true },
                // habilita só quando o personagem concluiu a criação inicial
                enabled = state.creationComplete() && !state.faseSupersAtiva && !state.emProgresso
            ) {
                Text("Nível da Campanha Supers")
            }
            Spacer(Modifier.width(8.dp))
            // (opcional) você pode colocar aqui um Text com preview do nível escolhido
        }
        if (showNivelDialog) {
            AlertDialog(
                onDismissRequest = { showNivelDialog = false },
                title = { Text("Nível de Poder da Campanha") },
                text  = {
                    Column {
                        Text("Selecione o nível (I a V) e confirme.")
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            listOf("I","II","III","IV","V").forEach { opt ->
                                TextButton(onClick = { nivelEscolhido = opt }) {
                                    Text(if (nivelEscolhido == opt) "[$opt]" else opt)
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        val previewTotal = when (nivelEscolhido) {
                            "I" -> 15; "II" -> 30; "III" -> 45; "IV" -> 60; "V" -> 75
                            else -> 15
                        }
                        Text("Prévia: Nível $nivelEscolhido → $previewTotal pontos de super.")
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel?.iniciarFaseSupers(nivelEscolhido)
                        showNivelDialog = false
                    }) { Text("Confirmar") }
                },
                dismissButton = {
                    TextButton(onClick = { showNivelDialog = false }) { Text("Cancelar") }
                }
            )
        }


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = { showDialog = true }) { Text("Ir para Superpoderes") }
            TextButton(onClick = onOpenListaCompletaPoderes) { Text("Lista Completa") }
        }

        if (showDialog && viewModel != null) {
            SupersDialog(
                state = state,
                viewModel = viewModel,
                onConfirmLock = {
                    // Aqui você pode “travar” a fase de supers conforme sua regra.
                    // Ex.: state.emProgresso = true (se esta for a sua flag de travamento geral),
                    // ou salvar um campo próprio se você já tiver.
                },
                onDismiss = { showDialog = false }
            )
        }

        // nada mais a renderizar nesta seção quando for supers
        return
    }

    // ======= ABAIXO: CONTEÚDO ORIGINAL DE PODERES (magias), INALTERADO =======

    // 0) Estado de "bloqueio": quando já estiver em progresso e não for edição.
    val locked = state.progresso > 0 && !state.emProgresso
    val context = LocalContext.current

    // 1) Carrega o JSON de poderes diretamente como List<Poder>
    val allPoderes: List<Poder> = remember {
        context.loadJsonAsset<List<Poder>>("poderes.json")
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

    // 8) Quantos slots há e quantos sobraram
    val remainingSlots = slots.size - slots.count { it != null }

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
                    val nomeDoPoder = poderId ?: "-"
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

        // b) Filtra apenas poderes de origem “BASICO”, que caibam no estágio atual
        //    e cujo ‘ID’ ainda não esteja em nenhum slot
        val disponiveis: List<Poder> = allPoderes
            .filter { poder -> poder.origem.equals("BASICO", ignoreCase = true) }
            .filter { poder ->
                val idxReq = listaDeEstagios.indexOfFirst { it.nome.equals(poder.estagio, ignoreCase = true) }
                idxReq in 0..curEstagioIdx
            }
            .filter { poder -> slots.none { it == poder.id } }

        if (disponiveis.isEmpty()) {
            item { Text("Nenhum poder disponível…", Modifier.padding(16.dp)) }
        } else {
            items(disponiveis, key = { it.id }) { poder ->
                Column {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .alpha(if (remainingSlots > 0) 1f else 0.3f)
                            .clickable(enabled = (remainingSlots > 0)) {
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
                    ) { Text(poder.nome) }
                    HorizontalDivider()
                }
            }
        }
    }
}