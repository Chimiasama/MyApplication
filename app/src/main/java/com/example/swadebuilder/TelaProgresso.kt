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
import com.example.swadebuilder.Pericia
import com.example.swadebuilder.dynamicStageCaps
import com.example.swadebuilder.listaAtributos
import com.example.swadebuilder.listaDeEstagios
import com.example.swadebuilder.listaPericias
import com.example.swadebuilder.listaVantagens
import com.example.swadebuilder.mapaAtributosDisplay
import com.example.swadebuilder.model.CriadorViewModel
import com.example.swadebuilder.model.EquipamentoCategoria
import com.example.swadebuilder.model.SuperPoder
import com.example.swadebuilder.model.Vantagem
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
        state = state,
        slotState = progressSlotState,
        slotAlvo = slotAlvo,
        seletorAtual = seletorAtual,
        reservasCompMaior = reservasCompMaior
    )

    AdvantageSelectorDialog(
        state = state,
        slotState = progressSlotState,
        slotAlvo = slotAlvo,
        seletorAtual = seletorAtual,
        reservasCompMaior = reservasCompMaior
    )

    SkillSelectorDialog(
        state = state,
        slotState = progressSlotState,
        slotAlvo = slotAlvo,
        seletorAtual = seletorAtual,
        reservasCompMaior = reservasCompMaior
    )

    AttributeSelectorDialog(
        state = state,
        slotState = progressSlotState,
        slotAlvo = slotAlvo,
        seletorAtual = seletorAtual,
        reservasCompMaior = reservasCompMaior
    )
}

@Composable
private fun AttributeSelectorDialog(
    state: CriadorState,
    slotState: MutableMap<String, SnapshotStateList<SlotChoice>>,
    slotAlvo: androidx.compose.runtime.MutableState<SlotContext?>,
    seletorAtual: androidx.compose.runtime.MutableState<SelectorMode?>,
    reservasCompMaior: androidx.compose.runtime.MutableState<Int>
) {
    if (seletorAtual.value != SelectorMode.Attribute) return
    val alvo = slotAlvo.value ?: return
    val stageInfo = stageMath(state, alvo.stageCode) ?: return
    val slots = slotState[alvo.stageCode] ?: return
    val slotAtual = slots.getOrNull(alvo.slotIndex) ?: return

    val progressDisponivel = state.progressosDisponiveis + slotAtual.custo
    val creditosStage = stageInfo.creditsLeft +
            if (slotAtual.stageName == stageInfo.descriptor.estagio.nome) slotAtual.custo else 0
    val attrsJaComprados = stageInfo.boughtAttrsSoFar -
            if (slotAtual.tipo == SlotTipo.ATRIBUTO && slotAtual.stageName == stageInfo.descriptor.estagio.nome) 1 else 0
    val limiteDeAtributo = attrsJaComprados < stageInfo.maxAttrsAllowed || stageInfo.descriptor.estagio.nome == "Lendário"

    AlertDialog(
        onDismissRequest = {
            seletorAtual.value = null
            slotAlvo.value = null
        },
        title = { Text("Escolher atributo (custo ${stageInfo.costAttr})") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "1 aumento por estágio (Lendário aceita múltiplos). Usa progresso real do estágio ${stageInfo.descriptor.codigo}.",
                    style = MaterialTheme.typography.bodySmall
                )
                listaAtributos.forEach { attrKey ->
                    val display = mapaAtributosDisplay[attrKey] ?: attrKey
                    val atual = state.valoresAtributos[attrKey]?.intValue ?: 0
                    val maxRaw = state.atributoMaxRaw(attrKey)
                    val incremento = if (atual < 12) 2 else 1
                    val proximo = (atual + incremento).coerceAtMost(maxRaw)
                    val pode = limiteDeAtributo && progressDisponivel >= stageInfo.costAttr && creditosStage >= stageInfo.costAttr && atual < maxRaw

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = pode) {
                                val anterior = slotAtual
                                reverterSlotChoice(state, stageInfo.descriptor.estagio.nome, anterior, reservasCompMaior)
                                if (consumeProgressForStage(state, stageInfo.descriptor.estagio.nome, stageInfo.costAttr)) {
                                    state.comprasAttrPorEstagio[stageInfo.descriptor.estagio.nome] =
                                        (state.comprasAttrPorEstagio[stageInfo.descriptor.estagio.nome] ?: 0) + 1
                                    state.valoresAtributos[attrKey]?.intValue = proximo
                                    state.recalcularPontosAtributo()

                                    slotState[alvo.stageCode]?.set(
                                        alvo.slotIndex,
                                        SlotChoice(
                                            tipo = SlotTipo.ATRIBUTO,
                                            descricao = "Atributo +1 ($display)",
                                            detalhes = "${atual.toDiceString()} → ${proximo.toDiceString()} (custo ${stageInfo.costAttr})",
                                            stageName = stageInfo.descriptor.estagio.nome,
                                            custo = stageInfo.costAttr,
                                            atributoKey = attrKey,
                                            valorAtributoAntes = atual
                                        )
                                    )
                                }
                                seletorAtual.value = null
                                slotAlvo.value = null
                            }
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(display, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "${atual.toDiceString()} → ${proximo.toDiceString()} (teto ${maxRaw.toDiceString()})",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (pode) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                            )
                        }
                        if (!pode) {
                            Text(
                                text = "Bloqueado",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    HorizontalDivider()
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = { seletorAtual.value = SelectorMode.Root }) { Text("Voltar") }
        }
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

private enum class SelectorMode { Root, Advantage, Skill, Attribute }

private data class SlotChoice(
    val tipo: SlotTipo = SlotTipo.LIVRE,
    val descricao: String = "Slot livre",
    val atende: Boolean = true,
    val detalhes: String? = null,
    val stageName: String? = null,
    val custo: Int = 0,
    val atributoKey: String? = null,
    val valorAtributoAntes: Int? = null,
    val periciaNome: String? = null,
    val periciaRawAntes: Int? = null,
    val vantagemId: String? = null
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

private data class StageMath(
    val descriptor: StageDescriptor,
    val index: Int,
    val stageCap: Int,
    val spentHere: Int,
    val creditsLeft: Int,
    val costAttr: Int,
    val boughtAttrsSoFar: Int,
    val maxAttrsAllowed: Int
)

private data class PericiaAvanco(
    val pericia: Pericia,
    val atual: Int,
    val cap: Int,
    val next: Int,
    val cost: Int
)

private fun stageMath(state: CriadorState, stageCode: String): StageMath? {
    val desc = stagePlan().firstOrNull { it.codigo == stageCode } ?: return null
    val idx = listaDeEstagios.indexOfFirst { it.nome == desc.estagio.nome }
    if (idx < 0) return null

    val stageCap = dynamicStageCaps[idx]
    val spentHere = state.stageXpSpent.getValue(desc.estagio.nome)
    val creditsLeft = stageCap - spentHere

    val boughtSoFar = listaDeEstagios.take(idx + 1)
        .sumOf { state.comprasAttrPorEstagio.getValue(it.nome) }
    val maxAllowed = if (desc.estagio.nome == "Lendário") Int.MAX_VALUE else (idx + 1)
    val costAttr = if (desc.estagio.nome == "Lendário") 2 else 1

    return StageMath(
        descriptor = desc,
        index = idx,
        stageCap = stageCap,
        spentHere = spentHere,
        creditsLeft = creditsLeft,
        costAttr = costAttr,
        boughtAttrsSoFar = boughtSoFar,
        maxAttrsAllowed = maxAllowed
    )
}

private fun consumeProgressForStage(
    state: CriadorState,
    stageName: String,
    cost: Int
): Boolean {
    if (cost <= 0) return false
    val idx = listaDeEstagios.indexOfFirst { it.nome == stageName }
    if (idx < 0) return false
    val cap = dynamicStageCaps[idx]
    val spent = state.stageXpSpent.getValue(stageName)
    if (spent + cost > cap) return false
    if (state.progressosDisponiveis < cost) return false

    state.stageXpSpent[stageName] = spent + cost
    state.progressosDisponiveis -= cost
    return true
}

private fun refundProgressForStage(
    state: CriadorState,
    stageName: String,
    cost: Int
) {
    if (cost <= 0) return
    val spent = state.stageXpSpent.getValue(stageName)
    state.stageXpSpent[stageName] = (spent - cost).coerceAtLeast(0)
    state.progressosDisponiveis += cost
}

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
            text = "Progressos por estágio (interface alternativa)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Mantemos o diálogo original de progressos intacto. Aqui cada slot N/E/V/H/L consome XP real ao confirmar a escolha.",
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
            val stageInfoAtual = stageMath(state, desc.codigo)
            Text(
                text = "${desc.estagio.nome} (${desc.codigo})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            stageInfoAtual?.let {
                Text(
                    text = "XP neste estágio: ${it.spentHere} / ${it.stageCap}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

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
    state: CriadorState,
    slotState: MutableMap<String, SnapshotStateList<SlotChoice>>,
    slotAlvo: androidx.compose.runtime.MutableState<SlotContext?>,
    seletorAtual: androidx.compose.runtime.MutableState<SelectorMode?>,
    reservasCompMaior: androidx.compose.runtime.MutableState<Int>
) {
    if (seletorAtual.value != SelectorMode.Root) return
    val alvo = slotAlvo.value ?: return
    val stageInfo = stageMath(state, alvo.stageCode) ?: return
    val slots = slotState[alvo.stageCode] ?: return
    val slotAtual = slots.getOrNull(alvo.slotIndex) ?: return

    val progressDisponivel = state.progressosDisponiveis + slotAtual.custo
    val creditosStage = stageInfo.creditsLeft +
            if (slotAtual.stageName == stageInfo.descriptor.estagio.nome) slotAtual.custo else 0
    val attrsJaComprados = stageInfo.boughtAttrsSoFar -
            if (slotAtual.tipo == SlotTipo.ATRIBUTO && slotAtual.stageName == stageInfo.descriptor.estagio.nome) 1 else 0
    val atributoCapBateu = stageInfo.descriptor.codigo != "L" && attrsJaComprados >= stageInfo.maxAttrsAllowed

    AlertDialog(
        onDismissRequest = {
            seletorAtual.value = null
            slotAlvo.value = null
        },
        title = { Text("Definir gasto para ${stageInfo.descriptor.codigo} slot ${alvo.slotIndex + 1}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Cada escolha consome progresso real do estágio ${stageInfo.descriptor.estagio.nome}.",
                    style = MaterialTheme.typography.bodySmall
                )

                TextButton(
                    onClick = {
                        if (atributoCapBateu) return@TextButton
                        seletorAtual.value = SelectorMode.Attribute
                    },
                    enabled = !atributoCapBateu &&
                            progressDisponivel >= stageInfo.costAttr &&
                            creditosStage >= stageInfo.costAttr
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
                    val anterior = slots[alvo.slotIndex]
                    reverterSlotChoice(state, stageInfo.descriptor.estagio.nome, anterior, reservasCompMaior)
                    if (consumeProgressForStage(state, stageInfo.descriptor.estagio.nome, 1)) {
                        slots[alvo.slotIndex] = SlotChoice(
                            tipo = SlotTipo.REMOVER_COMP_MENOR,
                            descricao = "Remover Complicação Menor",
                            detalhes = "Registrar remoção manualmente nas complicações.",
                            stageName = stageInfo.descriptor.estagio.nome,
                            custo = 1
                        )
                    }
                    seletorAtual.value = null
                    slotAlvo.value = null
                }) {
                    Text("Remover Complicação Menor")
                }

                TextButton(onClick = {
                    val anterior = slots[alvo.slotIndex]
                    reverterSlotChoice(state, stageInfo.descriptor.estagio.nome, anterior, reservasCompMaior)
                    if (consumeProgressForStage(state, stageInfo.descriptor.estagio.nome, 1)) {
                        reservasCompMaior.value += 1
                        slots[alvo.slotIndex] = SlotChoice(
                            tipo = SlotTipo.RESERVA_COMP_MAIOR,
                            descricao = "Reserva para Complicação Maior",
                            detalhes = "Reserva ${reservasCompMaior.value} — aplicar remoção manualmente ao juntar reservas.",
                            stageName = stageInfo.descriptor.estagio.nome,
                            custo = 1
                        )
                    }
                    seletorAtual.value = null
                    slotAlvo.value = null
                }) {
                    Text("Reservar para remover Complicação Maior")
                }

                TextButton(onClick = {
                    val anterior = slots[alvo.slotIndex]
                    reverterSlotChoice(state, stageInfo.descriptor.estagio.nome, anterior, reservasCompMaior)
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
    seletorAtual: androidx.compose.runtime.MutableState<SelectorMode?>,
    reservasCompMaior: androidx.compose.runtime.MutableState<Int>
) {
    if (seletorAtual.value != SelectorMode.Advantage) return
    val alvo = slotAlvo.value ?: return
    val stageInfo = stageMath(state, alvo.stageCode) ?: return
    val stageIndex = listaDeEstagios.indexOfFirst { it.nome == stageInfo.descriptor.estagio.nome }
    if (stageIndex < 0) return
    val slots = slotState[alvo.stageCode] ?: return
    val slotAtual = slots.getOrNull(alvo.slotIndex) ?: return

    val progressDisponivel = state.progressosDisponiveis + slotAtual.custo
    val creditosStage = stageInfo.creditsLeft +
            if (slotAtual.stageName == stageInfo.descriptor.estagio.nome) slotAtual.custo else 0

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
        title = { Text("Vantagens até ${stageInfo.descriptor.codigo}") },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(candidatas) { vant ->
                    val atende = strictRequirementsOk(state, vant, stageIndex)
                    val requisitos = formatarRequisitos(vant)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (progressDisponivel <= 0 || creditosStage <= 0) return@clickable
                                val anterior = slotAtual
                                reverterSlotChoice(state, stageInfo.descriptor.estagio.nome, anterior, reservasCompMaior)
                                if (state.progressosDisponiveis <= 0 || stageInfo.creditsLeft <= 0) return@clickable
                                state.grantVantagemPointFromXp(stageInfo.descriptor.estagio.nome)
                                slotState[alvo.stageCode]?.set(
                                    alvo.slotIndex,
                                    SlotChoice(
                                        tipo = SlotTipo.VANTAGEM,
                                        descricao = "PV reservado: ${vant.nome}",
                                        atende = atende,
                                        detalhes = buildString {
                                            append(requisitos)
                                            if (vant.requiresChoice) {
                                                append(" — requer escolha específica fora deste seletor")
                                            }
                                            if (!atende) {
                                                append(" — requisitos não atendidos pelo estado atual")
                                            }
                                            append(". PV destacado do estágio ${stageInfo.descriptor.codigo}.")
                                        },
                                        stageName = stageInfo.descriptor.estagio.nome,
                                        custo = 1,
                                        vantagemId = vant.id
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
    seletorAtual: androidx.compose.runtime.MutableState<SelectorMode?>,
    reservasCompMaior: androidx.compose.runtime.MutableState<Int>
) {
    if (seletorAtual.value != SelectorMode.Skill) return
    val alvo = slotAlvo.value ?: return
    val stageInfo = stageMath(state, alvo.stageCode) ?: return
    val slots = slotState[alvo.stageCode] ?: return
    val slotAtual = slots.getOrNull(alvo.slotIndex) ?: return
    val progressDisponivel = state.progressosDisponiveis + slotAtual.custo
    val creditosStage = stageInfo.creditsLeft +
            if (slotAtual.stageName == stageInfo.descriptor.estagio.nome) slotAtual.custo else 0

    val periciasValidas = listaPericias.mapNotNull { per ->
        val atual = state.rawTotal(per)
        val cap = state.periciaCapRaw(per)
        if (atual >= cap) return@mapNotNull null
        val next = when {
            atual == 0 -> 4
            atual < 12 -> atual + 2
            else -> atual + 1
        }
        val cost = if (next <= state.valoresAtributos[state.atributoBaseParaPericia(per)]!!.intValue) 1 else 2
        PericiaAvanco(per, atual, cap, next, cost)
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
                    items(periciasValidas) { avanco ->
                        val pode = progressDisponivel >= avanco.cost && creditosStage >= avanco.cost
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = pode) {
                                    val anterior = slotAtual
                                    reverterSlotChoice(state, stageInfo.descriptor.estagio.nome, anterior, reservasCompMaior)
                                    if (consumeProgressForStage(state, stageInfo.descriptor.estagio.nome, avanco.cost)) {
                                        state.baseIncsPorPericia[avanco.pericia] =
                                            state.baseIncsPorPericia.getValue(avanco.pericia) + 1
                                        state.spCostStackPorPericia.getValue(avanco.pericia).add(0)
                                        state.rebuildAllPericiaStacks()

                                        slotState[alvo.stageCode]?.set(
                                            alvo.slotIndex,
                                            SlotChoice(
                                                tipo = SlotTipo.PERICIA,
                                                descricao = "Perícia: ${avanco.pericia.nome} (+1 passo)",
                                                detalhes = "${avanco.atual.toDiceString()} → ${avanco.next.toDiceString()} (custo ${avanco.cost} avanço)",
                                                stageName = stageInfo.descriptor.estagio.nome,
                                                custo = avanco.cost,
                                                periciaNome = avanco.pericia.nome,
                                                periciaRawAntes = avanco.atual
                                            )
                                        )
                                    }
                                    seletorAtual.value = null
                                    slotAlvo.value = null
                                }
                                .padding(vertical = 8.dp, horizontal = 4.dp)
                        ) {
                            Text(avanco.pericia.nome, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "${avanco.pericia.atributo} | ${avanco.atual.toDiceString()} → ${avanco.next.toDiceString()} (custo ${avanco.cost})",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (pode) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
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

private fun strictRequirementsOk(state: CriadorState, v: Vantagem, estIndex: Int): Boolean {
    val reqEst = v.requisitos.estagio
    if (reqEst.isNotBlank()) {
        val reqIdx = listaDeEstagios.indexOfFirst { it.nome.equals(reqEst, ignoreCase = true) }
        if (reqIdx != -1 && reqIdx > estIndex) return false
    }
    if (v.requisitos.atributoMin.any { (nome, min) ->
            val chaveNorm = nome.uppercase().semAcentos().trim()
            val valor = state.valoresAtributos[chaveNorm]?.intValue
            valor == null || valor < min
        }
    ) return false
    val perMin = v.requisitos.periciaMin
    if (perMin.isNotEmpty()) {
        if (v.vinculadoPericia) {
            val atendeUma = perMin.any { (perNome, minRaw) ->
                val per = listaPericias.firstOrNull { it.nome.equals(perNome, ignoreCase = true) }
                per != null && state.rawTotal(per) >= minRaw
            }
            if (!atendeUma) return false
        } else {
            val falhaAlguma = perMin.any { (perNome, minRaw) ->
                val per = listaPericias.firstOrNull { it.nome.equals(perNome, ignoreCase = true) }
                    ?: return@any true
                state.rawTotal(per) < minRaw
            }
            if (falhaAlguma) return false
        }
    }
    val perMinOpc = v.requisitos.periciaMinOpcional
    if (perMinOpc.isNotEmpty()) {
        val ok = perMinOpc.any { (perNome, minRaw) ->
            val per = listaPericias.firstOrNull { it.nome.equals(perNome, ignoreCase = true) }
            per != null && state.rawTotal(per) >= minRaw
        }
        if (!ok) return false
    }
    if (v.requisitos.vantagensPrevias.isNotEmpty()) {
        val tenhoTodas = v.requisitos.vantagensPrevias.all { req ->
            val reqNorm = req.uppercase().semAcentos().trim()
            state.vantagensSelecionadas.any { it.nome.uppercase().semAcentos().trim() == reqNorm }
        }
        if (!tenhoTodas) return false
    }
    if (v.requisitos.exigeCS && !state.cartaSelvagem) return false
    return true
}

private fun reverterSlotChoice(
    state: CriadorState,
    stageFallbackName: String,
    choice: SlotChoice,
    reservasCompMaior: androidx.compose.runtime.MutableState<Int>
) {
    val stageName = choice.stageName ?: stageFallbackName
    when (choice.tipo) {
        SlotTipo.ATRIBUTO -> {
            refundProgressForStage(state, stageName, choice.custo)
            choice.atributoKey?.let { key ->
                val base = choice.valorAtributoAntes
                if (base != null) {
                    state.valoresAtributos[key]?.intValue = base
                } else {
                    val atual = state.valoresAtributos[key]?.intValue ?: 0
                    val step = if (atual > 12) 1 else 2
                    state.valoresAtributos[key]?.intValue = (atual - step).coerceAtLeast(state.atributoMinRaw(key))
                }
                val prev = state.comprasAttrPorEstagio[stageName] ?: 0
                state.comprasAttrPorEstagio[stageName] = (prev - 1).coerceAtLeast(0)
                state.recalcularPontosAtributo()
            }
        }

        SlotTipo.VANTAGEM -> {
            refundProgressForStage(state, stageName, choice.custo)
            state.pontosVantagem = (state.pontosVantagem - 1).coerceAtLeast(0)
            state.pvFromXpOutstanding = (state.pvFromXpOutstanding - 1).coerceAtLeast(0)
            if (state.pvFromXpOutstanding == 0) {
                state.overrideStageForVantagem = null
                state.openVantagensAfterGrant = false
            }
        }

        SlotTipo.PERICIA -> {
            refundProgressForStage(state, stageName, choice.custo)
            choice.periciaNome?.let { nome ->
                val per = listaPericias.firstOrNull { it.nome == nome }
                per?.let {
                    if (state.baseIncsPorPericia.getValue(it) > 0) {
                        state.baseIncsPorPericia[it] = state.baseIncsPorPericia.getValue(it) - 1
                    }
                    val stack = state.spCostStackPorPericia.getValue(it)
                    if (stack.isNotEmpty()) stack.removeAt(stack.size - 1)
                    state.rebuildAllPericiaStacks()
                }
            }
        }

        SlotTipo.REMOVER_COMP_MENOR -> {
            refundProgressForStage(state, stageName, choice.custo)
        }

        SlotTipo.RESERVA_COMP_MAIOR -> {
            refundProgressForStage(state, stageName, choice.custo)
            reservasCompMaior.value = (reservasCompMaior.value - 1).coerceAtLeast(0)
        }

        else -> {}
    }
}
