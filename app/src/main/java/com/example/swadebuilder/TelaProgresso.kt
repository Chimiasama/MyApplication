package com.example.swadebuilder

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.model.CriadorViewModel
import com.example.swadebuilder.model.EquipamentoCategoria
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.listaDeEstagios
import com.example.swadebuilder.listaPericias
import com.example.swadebuilder.nivelParaEstagio
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.semAcentos
import com.example.swadebuilder.ui.components.SectionCard
import com.example.swadebuilder.ui.dialogs.ProgressosDialog
import com.example.swadebuilder.ui.sections.AncestralidadesSection
import com.example.swadebuilder.ui.sections.AtributosContent
import com.example.swadebuilder.ui.sections.ComplicacoesSection
import com.example.swadebuilder.ui.sections.EquipamentoSection
import com.example.swadebuilder.ui.sections.InformacoesSection
import com.example.swadebuilder.ui.sections.PericiasContent
import com.example.swadebuilder.ui.sections.PoderesSection
import com.example.swadebuilder.ui.sections.SummaryContent
import com.example.swadebuilder.ui.sections.SuperPoderesContent
import com.example.swadebuilder.ui.sections.VantagensContent
import kotlinx.serialization.json.JsonPrimitive
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun TelaProgresso(
    state: CriadorState,
    viewModel: CriadorViewModel,
    onBack: () -> Unit,
    onOpenVantagensDetail: (String) -> Unit,
    onOpenPericiasDetail: () -> Unit,
    onOpenComplicacoesDetail: () -> Unit,
    onOpenAtributosDetail: () -> Unit,
    onOpenListaAncestralidadesDetail: (String) -> Unit,
    onOpenListaCompletaEquipamento: () -> Unit,
    onOpenPoderesDetail: () -> Unit,
    onOpenSuperPoderesDetail: (String) -> Unit,
    equipamentoCategorias: List<EquipamentoCategoria>,
    superequipCategorias: List<EquipamentoCategoria>,
    listaSuperPoderes: List<SuperPoder>,
    expInfos: Boolean,
    onToggleInfos: () -> Unit,
    expAncs: Boolean,
    onToggleAncs: () -> Unit,
    expComps: Boolean,
    onToggleComps: () -> Unit,
    expEquip: Boolean,
    onToggleEquip: () -> Unit,
    expAttrs: Boolean,
    onToggleAttrs: () -> Unit,
    expPer: Boolean,
    onTogglePer: () -> Unit,
    expVants: Boolean,
    onToggleVants: () -> Unit,
    expResumo: Boolean,
    onToggleResumo: () -> Unit,
    expPoderes: Boolean,
    onTogglePoderes: () -> Unit,
) {
    val scrollState = rememberScrollState()
    val showAllocDialog = rememberSaveable { mutableStateOf(false) }
    val progressSlotState = remember { criarProgressosPorEstagioState() }
    val slotAlvo = remember { mutableStateOf<SlotContext?>(null) }
    val seletorAtual = remember { mutableStateOf<SelectorMode?>(null) }
    val reservasCompMaior = rememberSaveable { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                Spacer(Modifier.width(8.dp))
                Text("Voltar")
            }

            Text(
                text = "Gestão de Progressos",
                modifier = Modifier.weight(1f),
                maxLines = 1
            )
        }

        InformacoesSection(
            state = state,
            expanded = expInfos,
            onToggle = onToggleInfos,
            onUseProgress = { showAllocDialog.value = true }
        )

        HorizontalDivider(thickness = 1.dp)

        AncestralidadesSection(
            currentAncestralidade = state.ancestralidade,
            expanded = expAncs,
            onToggle = onToggleAncs,
            supersLocked = state.criacaoBasicaCongelada,
            ancestralidadeEmFoco = state.ancestralidadeEmFoco,
            onOpenListaAncestralidadesDetail = onOpenListaAncestralidadesDetail,
            onSelectAncestralidade = { nome ->
                val key = nome.uppercase().semAcentos()
                state.aplicarAncestralidade(
                    key,
                    viewModel.feedbackMessages as MutableList<String>
                )
            }
        )

        HorizontalDivider(thickness = 1.dp)

        ComplicacoesSection(
            state = state,
            expanded = expComps,
            onToggle = onToggleComps,
            onOpenComplicacoesDetail = onOpenComplicacoesDetail,
            feedbackMessages = viewModel.feedbackMessages as MutableList<String>
        )

        HorizontalDivider(thickness = 1.dp)

        SectionCard(
            title = "Atributos",
            expanded = expAttrs,
            onToggle = onToggleAttrs,
            icon = Icons.Default.FitnessCenter
        ) {
            AtributosContent(
                state = state,
                onOpenAtributosDetail = onOpenAtributosDetail
            )
        }

        HorizontalDivider(thickness = 1.dp)

        SectionCard(
            title = "Perícias",
            expanded = expPer,
            onToggle = onTogglePer,
            icon = Icons.Default.School
        ) {
            PericiasContent(
                state = state,
                onOpenPericiasDetail = onOpenPericiasDetail,
                feedbackMessages = viewModel.feedbackMessages as MutableList<String>
            )
        }

        HorizontalDivider(thickness = 1.dp)

        SectionCard(
            title = "Vantagens",
            expanded = expVants,
            onToggle = onToggleVants,
            icon = Icons.Default.Star
        ) {
            VantagensContent(
                state = state,
                multiplosAAHabilitados = state.permiteMultiAntecedenteArcano,
                onOpenVantagensDetail = onOpenVantagensDetail,
                viewModel = viewModel
            )
        }

        HorizontalDivider(thickness = 1.dp)

        val temArcano = state.vantagensSelecionadas.any {
            it.nome.keyify().startsWith("ANTECEDENTE ARCANO")
        }

        if (temArcano && !state.celestialAAMilagresDesabilitado) {
            SectionCard(
                title = "Poderes",
                expanded = expPoderes,
                onToggle = onTogglePoderes,
                icon = Icons.Default.FlashOn
            ) {
                PoderesSection(
                    state = state,
                    onOpenListaCompletaPoderes = onOpenPoderesDetail
                )
            }

            HorizontalDivider(thickness = 1.dp)
        }

        if (state.modoSupers) {
            SuperPoderesContent(
                state = state,
                listaSuperPoderes = listaSuperPoderes,
                expanded = expPoderes,
                onToggle = onTogglePoderes,
                onOpenSuperPoderesDetail = onOpenSuperPoderesDetail
            )

            HorizontalDivider(thickness = 1.dp)
        }

        EquipamentoSection(
            dinheiro = state.dinheiro,
            pcTotal = state.pontosComplicacao,
            pcLivres = (state.pontosComplicacao - state.pontosComplicacaoGastos).coerceAtLeast(0),
            recursosPcUsados = state.cpRecursosStack.size,
            expanded = expEquip,
            onToggle = onToggleEquip,
            onUsarPontosBonusEmRecursos = {
                val pcLivresLocal =
                    (state.pontosComplicacao - state.pontosComplicacaoGastos).coerceAtLeast(0)

                if (pcLivresLocal > 0 && state.cpRecursosStack.isEmpty()) {
                    state.cpRecursosStack.add(Unit)
                    state.pontosComplicacaoGastos += 1
                    state.dinheiro += 500
                }
            },
            onDesfazerPontosBonusEmRecursos = {
                if (state.cpRecursosStack.isNotEmpty() && state.dinheiro >= 500) {
                    state.cpRecursosStack.removeAt(state.cpRecursosStack.lastIndex)
                    state.pontosComplicacaoGastos =
                        (state.pontosComplicacaoGastos - 1).coerceAtLeast(0)
                    state.dinheiro -= 500
                }
            },
            onListaCompletaClick = onOpenListaCompletaEquipamento,
            onEquipamentoDoubleClick = { equipamento ->
                val custo = (equipamento.custo as? JsonPrimitive)
                    ?.content?.toIntOrNull() ?: 0
                if (custo <= state.dinheiro) {
                    state.equipamentosComprados.add(equipamento)
                    state.dinheiro -= custo
                }
            },
            equipamentosComprados = state.equipamentosComprados,
            onRemoveEquipamentoClick = { equipamento ->
                val custo = (equipamento.custo as? JsonPrimitive)
                    ?.content?.toIntOrNull() ?: 0
                state.equipamentosComprados.remove(equipamento)
                state.dinheiro += custo
            },
            categorias = equipamentoCategorias,
            superequipCategorias = superequipCategorias
        )

        Spacer(Modifier.height(16.dp))
        HorizontalDivider(thickness = 3.dp)

        StageProgressPrototype(
            state = state,
            slotState = progressSlotState,
            slotAlvo = slotAlvo,
            seletorAtual = seletorAtual,
            reservasCompMaior = reservasCompMaior
        )

        Spacer(Modifier.height(16.dp))
        HorizontalDivider(thickness = 3.dp)

        SectionCard(
            title = "Resumo do Personagem",
            expanded = expResumo,
            onToggle = onToggleResumo,
            icon = Icons.Default.Description
        ) {
            SummaryContent(state = state)
        }
    }

    if (showAllocDialog.value) {
        ProgressosDialog(state = state) {
            showAllocDialog.value = false
        }
    }

    SlotTypeDialog(
        slotState = progressSlotState,
        slotAlvo = slotAlvo,
        seletorAtual = seletorAtual,
        reservasCompMaior = reservasCompMaior
    )

    AdvantageSelectorDialog(
        state = state,
        slotState = progressSlotState,
        slotAlvo = slotAlvo,
        seletorAtual = seletorAtual
    )

    SkillSelectorDialog(
        state = state,
        slotState = progressSlotState,
        slotAlvo = slotAlvo,
        seletorAtual = seletorAtual
    )
}

private enum class SlotTipo {
    LIVRE,
    ATRIBUTO,
    VANTAGEM,
    PERICIA,
    REMOVER_COMP_MENOR,
    RESERVA_COMP_MAIOR
}

private enum class SelectorMode { Root, Advantage, Skill }

private data class SlotChoice(
    val tipo: SlotTipo = SlotTipo.LIVRE,
    val descricao: String = "Slot livre",
    val atende: Boolean = true,
    val detalhes: String? = null
)

private data class StageDescriptor(
    val estagio: Estagio,
    val codigo: String,
    val slotsIniciais: Int
)

private data class SlotContext(val stageCode: String, val slotIndex: Int)

private fun stagePlan(): List<StageDescriptor> = listOf(
    StageDescriptor(nivelParaEstagio.getValue("N"), "N", 3),
    StageDescriptor(nivelParaEstagio.getValue("E"), "E", 4),
    StageDescriptor(nivelParaEstagio.getValue("V"), "V", 4),
    StageDescriptor(nivelParaEstagio.getValue("H"), "H", 4),
    StageDescriptor(nivelParaEstagio.getValue("L"), "L", 4) // Lendário: pode adicionar mais slots manualmente
)

private fun criarProgressosPorEstagioState(): MutableMap<String, SnapshotStateList<SlotChoice>> {
    val mapa = mutableStateMapOf<String, SnapshotStateList<SlotChoice>>()
    stagePlan().forEach { desc ->
        mapa[desc.codigo] = mutableStateListOf<SlotChoice>().apply {
            repeat(desc.slotsIniciais) { add(SlotChoice()) }
        }
    }
    return mapa
}

@Composable
private fun StageProgressPrototype(
    state: CriadorState,
    slotState: MutableMap<String, SnapshotStateList<SlotChoice>>,
    slotAlvo: androidx.compose.runtime.MutableState<SlotContext?>,
    seletorAtual: androidx.compose.runtime.MutableState<SelectorMode?>,
    reservasCompMaior: androidx.compose.runtime.MutableState<Int>
) {
    val plano = remember { stagePlan() }
    val lendarioKey = remember { plano.last().codigo }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Progressos por estágio (protótipo)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Mantemos o diálogo original de progressos intacto. Aqui você pode testar slots N/E/V/H/L sem consumir XP real.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        Text(
            text = "Regra crítica: só 1 aumento de atributo por estágio (Lendário aceita múltiplos, mas revise antes de validar).",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.tertiary
        )

        Spacer(Modifier.height(8.dp))

        plano.forEach { desc ->
            Text(
                text = "${desc.estagio.nome} (${desc.codigo})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            val slots = slotState.getOrPut(desc.codigo) { mutableStateListOf() }
            slots.forEachIndexed { index, slot ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                            slotAlvo.value = SlotContext(desc.codigo, index)
                            seletorAtual.value = SelectorMode.Root
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (slot.tipo == SlotTipo.LIVRE) {
                            MaterialTheme.colorScheme.surfaceVariant
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${desc.codigo} ${slot.descricao}",
                                fontWeight = if (slot.tipo == SlotTipo.LIVRE) FontWeight.Normal else FontWeight.SemiBold
                            )
                            slot.detalhes?.let { detalhe ->
                                Text(
                                    text = detalhe,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            if (slot.tipo == SlotTipo.VANTAGEM && !slot.atende) {
                                Text(
                                    text = "Requisitos pendentes (não confirmado)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        if (slot.tipo != SlotTipo.LIVRE) {
                            Text(
                                text = "Editar",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            if (desc.codigo == lendarioKey) {
                TextButton(onClick = {
                    slotState[desc.codigo]?.add(SlotChoice())
                }) {
                    Icon(Icons.Default.Info, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Adicionar slot Lendário extra")
                }
            }
        }

        Text(
            text = "Reservas para Complicação Maior: ${reservasCompMaior.value} (placeholder – exige fluxo próprio)",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = "Apenas a TelaProgresso foi tocada; demais telas permanecem intactas.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
private fun SlotTypeDialog(
    slotState: MutableMap<String, SnapshotStateList<SlotChoice>>,
    slotAlvo: androidx.compose.runtime.MutableState<SlotContext?>,
    seletorAtual: androidx.compose.runtime.MutableState<SelectorMode?>,
    reservasCompMaior: androidx.compose.runtime.MutableState<Int>
) {
    if (seletorAtual.value != SelectorMode.Root) return
    val alvo = slotAlvo.value ?: return
    val stageDesc = stagePlan().firstOrNull { it.codigo == alvo.stageCode } ?: return
    val slots = slotState[alvo.stageCode] ?: return

    val atributoCapBateu = stageDesc.codigo != "L" && slots.count { it.tipo == SlotTipo.ATRIBUTO } >
            (if (slots.getOrNull(alvo.slotIndex)?.tipo == SlotTipo.ATRIBUTO) 1 else 0)

    AlertDialog(
        onDismissRequest = {
            seletorAtual.value = null
            slotAlvo.value = null
        },
        title = { Text("Definir gasto para ${stageDesc.codigo} slot ${alvo.slotIndex + 1}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Escolha um uso para o progresso. Não consome XP real; serve para testar fluxo.",
                    style = MaterialTheme.typography.bodySmall
                )

                TextButton(
                    onClick = {
                        if (atributoCapBateu) return@TextButton
                        slots[alvo.slotIndex] = SlotChoice(
                            tipo = SlotTipo.ATRIBUTO,
                            descricao = "Atributo +1",
                            detalhes = if (stageDesc.codigo == "L") {
                                "Lendário aceita múltiplos, mas valide custo e gating."
                            } else {
                                "Regra: máximo 1 aumento de atributo neste estágio."
                            }
                        )
                        seletorAtual.value = null
                        slotAlvo.value = null
                    },
                    enabled = !atributoCapBateu
                ) { Text("Aumentar atributo") }
                if (atributoCapBateu) {
                    Text(
                        text = "Limite de atributo deste estágio já usado (exceto regras especiais Lendário).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                TextButton(onClick = { seletorAtual.value = SelectorMode.Advantage }) {
                    Text("Comprar vantagem (filtrada por estágio)")
                }

                TextButton(onClick = { seletorAtual.value = SelectorMode.Skill }) {
                    Text("Aumentar perícia (opções válidas apenas)")
                }

                TextButton(onClick = {
                    slots[alvo.slotIndex] = SlotChoice(
                        tipo = SlotTipo.REMOVER_COMP_MENOR,
                        descricao = "Remover Complicação Menor",
                        detalhes = "Placeholder — requer validação manual para impacto em CP/Histórico"
                    )
                    seletorAtual.value = null
                    slotAlvo.value = null
                }) {
                    Text("Remover Complicação Menor")
                }

                TextButton(onClick = {
                    reservasCompMaior.value += 1
                    slots[alvo.slotIndex] = SlotChoice(
                        tipo = SlotTipo.RESERVA_COMP_MAIOR,
                        descricao = "Reserva para Complicação Maior",
                        detalhes = "Reserva ${reservasCompMaior.value} — acumule antes de aplicar de fato"
                    )
                    seletorAtual.value = null
                    slotAlvo.value = null
                }) {
                    Text("Reservar para remover Complicação Maior")
                }

                TextButton(onClick = {
                    slots[alvo.slotIndex] = SlotChoice()
                    seletorAtual.value = null
                    slotAlvo.value = null
                }) {
                    Text("Limpar slot")
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = {
                seletorAtual.value = null
                slotAlvo.value = null
            }) { Text("Fechar") }
        }
    )
}

@Composable
private fun AdvantageSelectorDialog(
    state: CriadorState,
    slotState: MutableMap<String, SnapshotStateList<SlotChoice>>,
    slotAlvo: androidx.compose.runtime.MutableState<SlotContext?>,
    seletorAtual: androidx.compose.runtime.MutableState<SelectorMode?>
) {
    if (seletorAtual.value != SelectorMode.Advantage) return
    val alvo = slotAlvo.value ?: return
    val stageDesc = stagePlan().firstOrNull { it.codigo == alvo.stageCode } ?: return
    val stageIndex = listaDeEstagios.indexOfFirst { it.nome == stageDesc.estagio.nome }
    if (stageIndex < 0) return

    val candidatas = listaVantagens.filter { vant ->
        val estReq = nivelParaEstagio[vant.nivel.uppercase()] ?: nivelParaEstagio.getValue("N")
        val reqIdx = listaDeEstagios.indexOfFirst { it.nome.equals(estReq.nome, ignoreCase = true) }
        reqIdx <= stageIndex
    }

    AlertDialog(
        onDismissRequest = {
            seletorAtual.value = null
            slotAlvo.value = null
        },
        title = { Text("Vantagens até ${stageDesc.codigo}") },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(candidatas) { vant ->
                    val atende = state.podeSelecionar(vant)
                    val requisitos = formatarRequisitos(vant)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                slotState[alvo.stageCode]?.set(
                                    alvo.slotIndex,
                                    SlotChoice(
                                        tipo = SlotTipo.VANTAGEM,
                                        descricao = "Vantagem: ${vant.nome}",
                                        atende = atende,
                                        detalhes = buildString {
                                            append(requisitos)
                                            if (vant.requiresChoice) {
                                                append(" — requer escolha específica não resolvida aqui")
                                            }
                                            if (!atende) {
                                                append(" — requisitos não atendidos pelo estado atual")
                                            }
                                        }
                                    )
                                )
                                seletorAtual.value = null
                                slotAlvo.value = null
                            }
                            .padding(vertical = 8.dp, horizontal = 4.dp)
                    ) {
                        Text("${vant.nome} (${vant.nivel})", fontWeight = FontWeight.SemiBold)
                        Text(requisitos.ifBlank { "Sem requisitos declarados" }, style = MaterialTheme.typography.bodySmall)
                        if (!atende) {
                            Text(
                                text = "Requisitos não atendidos no personagem atual",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        if (vant.requiresChoice) {
                            Text(
                                text = "Precisa de choice/variante — resolverá fora deste protótipo",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = {
                seletorAtual.value = SelectorMode.Root
            }) { Text("Voltar") }
        }
    )
}

@Composable
private fun SkillSelectorDialog(
    state: CriadorState,
    slotState: MutableMap<String, SnapshotStateList<SlotChoice>>,
    slotAlvo: androidx.compose.runtime.MutableState<SlotContext?>,
    seletorAtual: androidx.compose.runtime.MutableState<SelectorMode?>
) {
    if (seletorAtual.value != SelectorMode.Skill) return
    val alvo = slotAlvo.value ?: return
    val periciasValidas = listaPericias.filter { per ->
        state.rawTotal(per) < state.periciaCapRaw(per)
    }

    AlertDialog(
        onDismissRequest = {
            seletorAtual.value = null
            slotAlvo.value = null
        },
        title = { Text("Perícias elegíveis para avanço") },
        text = {
            if (periciasValidas.isEmpty()) {
                Text("Nenhuma perícia pode ser aumentada via avanço agora.")
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(periciasValidas) { per ->
                        val atual = state.rawTotal(per)
                        val cap = state.periciaCapRaw(per)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    slotState[alvo.stageCode]?.set(
                                        alvo.slotIndex,
                                        SlotChoice(
                                            tipo = SlotTipo.PERICIA,
                                            descricao = "Perícia: ${per.nome} (+1 passo)",
                                            detalhes = "Atual: ${atual.toDiceString()} / Teto: ${cap.toDiceString()}"
                                        )
                                    )
                                    seletorAtual.value = null
                                    slotAlvo.value = null
                                }
                                .padding(vertical = 8.dp, horizontal = 4.dp)
                        ) {
                            Text(per.nome, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "${per.atributo} | ${atual.toDiceString()} → ${((atual + 2).coerceAtMost(cap)).toDiceString()} (custo padrão de avanço)",
                                style = MaterialTheme.typography.bodySmall
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = { seletorAtual.value = SelectorMode.Root }) { Text("Voltar") }
        }
    )
}

private fun formatarRequisitos(v: Vantagem): String {
    val partes = mutableListOf<String>()
    if (v.requisitos.estagio.isNotBlank()) partes += "Estágio mínimo: ${v.requisitos.estagio}"
    if (v.requisitos.atributoMin.isNotEmpty()) {
        partes += "Atributos: " + v.requisitos.atributoMin.entries.joinToString { (nome, min) -> "$nome ${min.toDiceString()}" }
    }
    if (v.requisitos.periciaMin.isNotEmpty()) {
        partes += "Perícias: " + v.requisitos.periciaMin.entries.joinToString { (nome, min) -> "$nome ${min.toDiceString()}" }
    }
    if (v.requisitos.periciaMinOpcional.isNotEmpty()) {
        partes += "Perícias (opcional): " + v.requisitos.periciaMinOpcional.entries.joinToString { (nome, min) -> "$nome ${min.toDiceString()}" }
    }
    if (v.requisitos.vantagensPrevias.isNotEmpty()) {
        partes += "Vantagens prévias: ${v.requisitos.vantagensPrevias.joinToString()}"
    }
    if (v.requisitos.observacoes.isNotBlank()) {
        partes += v.requisitos.observacoes
    }
    return partes.joinToString(separator = " • ")
}
