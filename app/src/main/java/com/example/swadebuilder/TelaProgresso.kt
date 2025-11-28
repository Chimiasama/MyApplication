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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.model.CriadorViewModel
import com.example.swadebuilder.model.EquipamentoCategoria
import com.example.swadebuilder.model.Vantagem
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
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.semAcentos
import kotlinx.serialization.json.JsonPrimitive

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
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
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
    val descricao: String = "LIVRE",
    val atende: Boolean = true,
    val detalhes: String = "",
    val stageName: String? = null,
    val custo: Int = 0,
    // ATRIBUTO
    val atributoKey: String? = null,
    val valorAtributoAntes: Int? = null,
    // PERÍCIA – legado (1 perícia só)
    val periciaNome: String? = null,
    val periciaRawAntes: Int? = null,
    // PERÍCIAS – NOVO (1 perícia maior OU 2 menores no mesmo slot)
    val periciasNomes: List<String>? = null,
    val periciasRawsAntes: List<Int>? = null,
    // VANTAGEM
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
    val capRaw: Int,
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

private fun estagioMinimoParaVantagem(v: Vantagem): Estagio {
    val reqEst = v.requisitos.estagio.trim()
    if (reqEst.isNotBlank()) {
        listaDeEstagios.firstOrNull { it.nome.equals(reqEst, ignoreCase = true) }?.let { return it }
        nivelParaEstagio[reqEst.uppercase()]?.let { return it }
    }

    val nivelBruto = v.nivel
    if (nivelBruto.isNotBlank()) {
        val normalizado = nivelBruto.trim()
        nivelParaEstagio[normalizado.uppercase()]?.let { return it }
        listaDeEstagios.firstOrNull { it.nome.equals(normalizado, ignoreCase = true) }?.let { return it }
    }

    return nivelParaEstagio.getValue("N")
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
                val podeUsarEsteSlot = run {
                    if (slot.tipo != SlotTipo.LIVRE) return@run true
                    slots.take(index).all { it.tipo != SlotTipo.LIVRE }
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable(enabled = podeUsarEsteSlot) {
                            slotAlvo.value = SlotContext(desc.codigo, index)
                            seletorAtual.value = SelectorMode.Root
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (!podeUsarEsteSlot) {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        } else if (slot.tipo == SlotTipo.LIVRE) {
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
                        } else if (!podeUsarEsteSlot) {
                            Text(
                                text = "Bloqueado",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
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
    val atributoCapBateu =
        stageInfo.descriptor.codigo != "L" && attrsJaComprados >= stageInfo.maxAttrsAllowed

    // --- NOVO: verifica se há complicações removíveis ---

    val autoKeys = state.desvantagensAutomaticas
        .map { it.substringBefore("(").trim().keyify() }
        .toSet()

    val hasCompMenor = state.complicacoesSelecionadas.any { (comp, tipo) ->
        comp.id.keyify() !in autoKeys &&
                tipo.equals("Menor", ignoreCase = true)
    }

    val hasCompMaior = state.complicacoesSelecionadas.any { (comp, tipo) ->
        comp.id.keyify() !in autoKeys &&
                tipo.equals("Maior", ignoreCase = true)
    }

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

                // --- Remoção / reserva de complicações só aparecem se houver algo removível ---

                if (hasCompMenor) {
                    TextButton(onClick = {
                        val anterior = slots[alvo.slotIndex]
                        reverterSlotChoice(
                            state,
                            stageInfo.descriptor.estagio.nome,
                            anterior,
                            reservasCompMaior
                        )
                        if (consumeProgressForStage(
                                state,
                                stageInfo.descriptor.estagio.nome,
                                1
                            )
                        ) {
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
                }

                if (hasCompMaior) {
                    TextButton(onClick = {
                        val anterior = slots[alvo.slotIndex]
                        reverterSlotChoice(
                            state,
                            stageInfo.descriptor.estagio.nome,
                            anterior,
                            reservasCompMaior
                        )
                        if (consumeProgressForStage(
                                state,
                                stageInfo.descriptor.estagio.nome,
                                1
                            )
                        ) {
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
                }

                TextButton(onClick = {
                    val anterior = slots[alvo.slotIndex]
                    reverterSlotChoice(
                        state,
                        stageInfo.descriptor.estagio.nome,
                        anterior,
                        reservasCompMaior
                    )
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
    slotAlvo: MutableState<SlotContext?>,
    seletorAtual: MutableState<SelectorMode?>,
    reservasCompMaior: MutableState<Int>
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

    // Vantagens permitidas até o estágio atual
    val candidatas = listaVantagens.filter { vant ->
        val rawNivel = vant.nivel?.trim()
        val chaveNivel = when {
            rawNivel.isNullOrBlank() -> "N"
            rawNivel.equals("null", ignoreCase = true) -> "N"
            else -> rawNivel.uppercase()
        }

        val estReq = nivelParaEstagio[chaveNivel] ?: nivelParaEstagio.getValue("N")
        val reqIdx = listaDeEstagios.indexOfFirst {
            it.nome.equals(estReq.nome, ignoreCase = true)
        }
        reqIdx <= stageIndex
    }

    AlertDialog(
        onDismissRequest = {
            seletorAtual.value = null
            slotAlvo.value = null
        },
        title = { Text("Vantagens até ${stageInfo.descriptor.codigo}") },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
            ) {
                items(candidatas) { vant ->
                    val atendeRequisitos = strictRequirementsOk(state, vant, stageIndex)
                    val podeSelecionar = state.podeSelecionar(vant) && atendeRequisitos
                    val requisitos = formatarRequisitos(vant)

                    val rawNivel = vant.nivel?.trim()
                    val chaveNivel = when {
                        rawNivel.isNullOrBlank() -> "N"
                        rawNivel.equals("null", ignoreCase = true) -> "N"
                        else -> rawNivel.uppercase()
                    }
                    val estReq = nivelParaEstagio[chaveNivel] ?: nivelParaEstagio.getValue("N")

                    val temRecursos = progressDisponivel > 0 && creditosStage > 0
                    val habilitado = podeSelecionar && temRecursos

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = habilitado) {
                                // se não estiver habilitado, simplesmente ignora o clique
                                if (!habilitado) return@clickable

                                val anterior = slotAtual
                                reverterSlotChoice(
                                    state,
                                    stageInfo.descriptor.estagio.nome,
                                    anterior,
                                    reservasCompMaior
                                )

                                if (!consumeProgressForStage(
                                        state,
                                        stageInfo.descriptor.estagio.nome,
                                        1
                                    )
                                ) {
                                    return@clickable
                                }

                                // Aqui seguimos o mesmo padrão do diálogo original:
                                // convertemos 1 progresso em 1 PV reservado para comprar vantagem.
                                state.grantVantagemPointFromXp(stageInfo.descriptor.estagio.nome)

                                slotState[alvo.stageCode]?.set(
                                    alvo.slotIndex,
                                    SlotChoice(
                                        tipo = SlotTipo.VANTAGEM,
                                        descricao = "PV reservado: ${vant.nome}",
                                        atende = podeSelecionar,
                                        detalhes = buildString {
                                            append(requisitos.ifBlank { "Sem requisitos declarados" })
                                            if (vant.requiresChoice) {
                                                append(" — requer escolha específica fora deste seletor")
                                            }
                                            if (!podeSelecionar) {
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
                        Text(
                            text = vant.nome,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Estágio mínimo: ${estReq.nome}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (requisitos.isNotBlank()) {
                            Text(
                                requisitos,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        if (!atendeRequisitos) {
                            Text(
                                text = "Requisitos não atendidos no personagem atual",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        if (!state.podeSelecionar(vant)) {
                            Text(
                                text = "Não pode selecionar novamente (limite de cópias / regras especiais).",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        if (vant.requiresChoice) {
                            Text(
                                text = "Precisa de choice/variante — resolverá fora deste protótipo.",
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
            TextButton(onClick = { seletorAtual.value = SelectorMode.Root }) {
                Text("Voltar")
            }
        }
    )
}


@Composable
private fun SkillSelectorDialog(
    state: CriadorState,
    slotState: MutableMap<String, SnapshotStateList<SlotChoice>>,
    slotAlvo: MutableState<SlotContext?>,
    seletorAtual: MutableState<SelectorMode?>,
    reservasCompMaior: MutableState<Int>
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
        if (next > cap) return@mapNotNull null

        val baseAttrKey = state.atributoBaseParaPericia(per)
        val atrRaw = state.valoresAtributos[baseAttrKey]?.intValue ?: 4
        val cost = if (next <= atrRaw) 1 else 2

        PericiaAvanco(
            pericia = per,
            atual = atual,
            capRaw = cap,
            next = next,
            cost = cost
        )
    }

    val periciasAltas = periciasValidas.filter { it.cost == 2 }
    val periciasBaixas = periciasValidas.filter { it.cost == 1 }

    var modo by remember { mutableStateOf("PericiaAlta") }

    var perAltaSelecionada by remember { mutableStateOf<PericiaAvanco?>(null) }
    val baixasSelecionadas = remember { mutableStateListOf<PericiaAvanco>() }

    val podeGastarUmProgresso = progressDisponivel >= 1 && creditosStage >= 1

    val podeConfirmarAlta =
        modo == "PericiaAlta" && podeGastarUmProgresso && perAltaSelecionada != null

    val podeConfirmarBaixas =
        modo == "PericiasBaixas" && podeGastarUmProgresso && baixasSelecionadas.size == 2

    AlertDialog(
        onDismissRequest = {
            seletorAtual.value = null
            slotAlvo.value = null
        },
        title = { Text("Subir perícias por XP (1 alta OU 2 baixas)") },
        text = {
            if (periciasValidas.isEmpty()) {
                Text("Nenhuma perícia pode ser aumentada via avanço agora.")
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 520.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Este slot consome 1 progresso do estágio ${stageInfo.descriptor.estagio.nome}. " +
                                "Você pode usar esse avanço como 1 aumento caro (perícia acima do atributo) " +
                                "ou 2 aumentos baratos (perícias até o atributo).",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(Modifier.height(4.dp))

                    // ===== OPÇÃO 1: PERÍCIA ≥ ATRIBUTO (1 alta) =====
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                modo = "PericiaAlta"
                            }
                    ) {
                        RadioButton(
                            selected = (modo == "PericiaAlta"),
                            onClick = { modo = "PericiaAlta" }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Perícia ≥ Atributo (1 aumento caro)")
                    }

                    if (modo == "PericiaAlta") {
                        if (periciasAltas.isEmpty()) {
                            Text(
                                text = "Nenhuma perícia está acima do atributo para ser aumentada.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 220.dp)
                            ) {
                                items(periciasAltas) { avanco ->
                                    val selected = perAltaSelecionada?.pericia == avanco.pericia
                                    val atualLabel = if (avanco.atual <= 0) "—" else avanco.atual.toDiceString()

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable(enabled = podeGastarUmProgresso) {
                                                perAltaSelecionada = avanco
                                            }
                                            .padding(vertical = 6.dp, horizontal = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = selected,
                                            onClick = null,
                                            enabled = podeGastarUmProgresso
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                avanco.pericia.nome,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = "$atualLabel → ${avanco.next.toDiceString()}",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // ===== OPÇÃO 2: 2x PERÍCIAS ≤ ATRIBUTO (2 baixas) =====
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                modo = "PericiasBaixas"
                            }
                    ) {
                        RadioButton(
                            selected = (modo == "PericiasBaixas"),
                            onClick = { modo = "PericiasBaixas" }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("2× Perícias ≤ Atributo (2 aumentos baratos)")
                    }

                    if (modo == "PericiasBaixas") {
                        if (periciasBaixas.isEmpty()) {
                            Text(
                                text = "Nenhuma perícia elegível abaixo (ou até) o atributo para este avanço.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Text(
                                text = "Selecione exatamente duas perícias para subir +1 cada.",
                                style = MaterialTheme.typography.bodySmall
                            )
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 220.dp)
                            ) {
                                items(periciasBaixas) { avanco ->
                                    val checked = baixasSelecionadas.any { it.pericia == avanco.pericia }
                                    val atualLabel = if (avanco.atual <= 0) "—" else avanco.atual.toDiceString()

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable(enabled = podeGastarUmProgresso) {
                                                if (checked) {
                                                    baixasSelecionadas.removeAll { it.pericia == avanco.pericia }
                                                } else if (baixasSelecionadas.size < 2) {
                                                    baixasSelecionadas.add(avanco)
                                                }
                                            }
                                            .padding(vertical = 6.dp, horizontal = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = checked,
                                            onCheckedChange = null,
                                            enabled = podeGastarUmProgresso
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                avanco.pericia.nome,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = "$atualLabel → ${avanco.next.toDiceString()}",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val anterior = slotAtual
                    reverterSlotChoice(
                        state = state,
                        stageName = stageInfo.descriptor.estagio.nome,
                        choice = anterior,
                        reservasCompMaior = reservasCompMaior
                    )

                    if (!consumeProgressForStage(state, stageInfo.descriptor.estagio.nome, 1)) {
                        seletorAtual.value = null
                        slotAlvo.value = null
                        return@TextButton
                    }

                    if (modo == "PericiaAlta") {
                        val avanco = perAltaSelecionada ?: run {
                            seletorAtual.value = null
                            slotAlvo.value = null
                            return@TextButton
                        }

                        val per = avanco.pericia
                        val rawAntes = avanco.atual

                        state.baseIncsPorPericia[per] =
                            state.baseIncsPorPericia.getValue(per) + 1
                        state.spCostStackPorPericia.getValue(per).add(0)
                        state.rebuildAllPericiaStacks()

                        val atualLabel = if (avanco.atual <= 0) "—" else avanco.atual.toDiceString()

                        val desc = "Perícia: ${per.nome} (+1 passo, acima do atributo)"
                        val detalhes =
                            "$atualLabel → ${avanco.next.toDiceString()} (1 progresso)"

                        slotState[alvo.stageCode]?.set(
                            alvo.slotIndex,
                            SlotChoice(
                                tipo = SlotTipo.PERICIA,
                                descricao = desc,
                                detalhes = detalhes,
                                stageName = stageInfo.descriptor.estagio.nome,
                                custo = 1,
                                periciaNome = per.nome,
                                periciaRawAntes = rawAntes,
                                periciasNomes = listOf(per.nome),
                                periciasRawsAntes = listOf(rawAntes)
                            )
                        )
                    } else {
                        val selecionadas = baixasSelecionadas.toList()
                        if (selecionadas.size != 2) {
                            seletorAtual.value = null
                            slotAlvo.value = null
                            return@TextButton
                        }

                        val nomes = mutableListOf<String>()
                        val rawsAntes = mutableListOf<Int>()

                        selecionadas.forEach { avanco ->
                            val per = avanco.pericia
                            val rawAntes = avanco.atual

                            nomes.add(per.nome)
                            rawsAntes.add(rawAntes)

                            state.baseIncsPorPericia[per] =
                                state.baseIncsPorPericia.getValue(per) + 1
                            state.spCostStackPorPericia.getValue(per).add(0)
                        }
                        state.rebuildAllPericiaStacks()

                        val desc = "Perícias: ${nomes.joinToString(" +1, ")} +1"
                        val detalhes = buildString {
                            append("2× abaixo do atributo (1 progresso): ")
                            append(
                                selecionadas.joinToString(" | ") { avanco ->
                                    val atualLabel =
                                        if (avanco.atual <= 0) "—" else avanco.atual.toDiceString()
                                    "${avanco.pericia.nome}: " +
                                            "$atualLabel → ${avanco.next.toDiceString()}"
                                }
                            )
                        }

                        slotState[alvo.stageCode]?.set(
                            alvo.slotIndex,
                            SlotChoice(
                                tipo = SlotTipo.PERICIA,
                                descricao = desc,
                                detalhes = detalhes,
                                stageName = stageInfo.descriptor.estagio.nome,
                                custo = 1,
                                periciaNome = nomes.firstOrNull(),
                                periciaRawAntes = rawsAntes.firstOrNull(),
                                periciasNomes = nomes,
                                periciasRawsAntes = rawsAntes
                            )
                        )
                    }

                    seletorAtual.value = null
                    slotAlvo.value = null
                },
                enabled = podeConfirmarAlta || podeConfirmarBaixas
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                seletorAtual.value = null
                slotAlvo.value = null
            }) {
                Text("Cancelar")
            }
        }
    )
}

private fun formatarRequisitos(v: Vantagem): String {
    val partes = mutableListOf<String>()

    // NÃO colocamos mais "Estágio mínimo" aqui — isso já é exibido separado no diálogo

    if (v.requisitos.atributoMin.isNotEmpty()) {
        partes += "Atributos: " + v.requisitos.atributoMin
            .entries
            .joinToString { (nome, min) -> "$nome ${min.toDiceString()}" }
    }
    if (v.requisitos.periciaMin.isNotEmpty()) {
        partes += "Perícias: " + v.requisitos.periciaMin
            .entries
            .joinToString { (nome, min) -> "$nome ${min.toDiceString()}" }
    }
    if (v.requisitos.periciaMinOpcional.isNotEmpty()) {
        partes += "Perícias (opcional): " + v.requisitos.periciaMinOpcional
            .entries
            .joinToString { (nome, min) -> "$nome ${min.toDiceString()}" }
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
    // estágio mínimo
    val reqEstBruto = v.requisitos.estagio
    val reqEst = reqEstBruto?.trim().orEmpty()
    if (reqEst.isNotEmpty()) {
        val reqIdx = listaDeEstagios.indexOfFirst {
            it.nome.equals(reqEst, ignoreCase = true)
        }
        if (reqIdx != -1 && reqIdx > estIndex) return false
    }

    // atributos mínimos
    if (v.requisitos.atributoMin.any { (nome, min) ->
            val chaveNorm = nome.uppercase().semAcentos().trim()
            val valor = state.valoresAtributos[chaveNorm]?.intValue
            valor == null || valor < min
        }
    ) return false

    // perícias mínimas (obrigatórias)
    val perMin = v.requisitos.periciaMin
    if (perMin.isNotEmpty()) {
        if (v.vinculadoPericia) {
            // basta uma das perícias bater o mínimo
            val atendeUma = perMin.any { (perNome, minRaw) ->
                val per = listaPericias.firstOrNull { it.nome.equals(perNome, ignoreCase = true) }
                per != null && state.rawTotal(per) >= minRaw
            }
            if (!atendeUma) return false
        } else {
            // todas as declaradas devem atender
            val falhaAlguma = perMin.any { (perNome, minRaw) ->
                val per = listaPericias.firstOrNull { it.nome.equals(perNome, ignoreCase = true) }
                    ?: return@any true
                state.rawTotal(per) < minRaw
            }
            if (falhaAlguma) return false
        }
    }

    // perícias mínimas opcionais
    val perMinOpc = v.requisitos.periciaMinOpcional
    if (perMinOpc.isNotEmpty()) {
        val ok = perMinOpc.any { (perNome, minRaw) ->
            val per = listaPericias.firstOrNull { it.nome.equals(perNome, ignoreCase = true) }
            per != null && state.rawTotal(per) >= minRaw
        }
        if (!ok) return false
    }

    // vantagens prévias
    if (v.requisitos.vantagensPrevias.isNotEmpty()) {
        val tenhoTodas = v.requisitos.vantagensPrevias.all { req ->
            val reqNorm = req.uppercase().semAcentos().trim()
            state.vantagensSelecionadas.any {
                it.nome.uppercase().semAcentos().trim() == reqNorm
            }
        }
        if (!tenhoTodas) return false
    }

    // carta selvagem, se exigir
    if (v.requisitos.exigeCS && !state.cartaSelvagem) return false

    return true
}

private fun reverterSlotChoice(
    state: CriadorState,
    stageName: String,
    choice: SlotChoice,
    reservasCompMaior: MutableState<Int>
) {
    if (choice.tipo == SlotTipo.LIVRE || choice.custo <= 0) return

    // Se o slot tinha um stageName próprio, usamos ele; senão usamos o parâmetro
    val effectiveStageName = choice.stageName ?: stageName

    // Sempre devolve primeiro o progresso gasto nesse slot
    refundProgressForStage(state, effectiveStageName, choice.custo)

    when (choice.tipo) {
        SlotTipo.ATRIBUTO -> {
            choice.atributoKey?.let { key ->
                val base = choice.valorAtributoAntes
                if (base != null) {
                    // volta exatamente para o valor anterior salvo
                    state.valoresAtributos[key]?.intValue = base
                } else {
                    // fallback: desce 1 passo respeitando o mínimo do atributo
                    val atual = state.valoresAtributos[key]?.intValue ?: 0
                    val step = if (atual > 12) 1 else 2
                    state.valoresAtributos[key]?.intValue =
                        (atual - step).coerceAtLeast(state.atributoMinRaw(key))
                }

                // decrementa o contador de compras por estágio
                val prev = state.comprasAttrPorEstagio[effectiveStageName] ?: 0
                state.comprasAttrPorEstagio[effectiveStageName] = (prev - 1).coerceAtLeast(0)

                state.recalcularPontosAtributo()
            }
        }

        SlotTipo.VANTAGEM -> {
            // Aqui estamos DESFAZENDO a reserva de PV originada deste slot
            state.pontosVantagem = (state.pontosVantagem - 1).coerceAtLeast(0)
            state.pvFromXpOutstanding = (state.pvFromXpOutstanding - 1).coerceAtLeast(0)
            if (state.pvFromXpOutstanding == 0) {
                state.overrideStageForVantagem = null
                state.openVantagensAfterGrant = false
            }
        }

        SlotTipo.PERICIA -> {
            // Caso legado: 1 perícia só
            choice.periciaNome?.let { nome ->
                val per = listaPericias.firstOrNull { it.nome == nome }
                per?.let {
                    if (state.baseIncsPorPericia.getValue(it) > 0) {
                        state.baseIncsPorPericia[it] =
                            state.baseIncsPorPericia.getValue(it) - 1
                    }
                    val stack = state.spCostStackPorPericia.getValue(it)
                    if (stack.isNotEmpty()) stack.removeAt(stack.size - 1)
                }
            }

            // Caso novo: até 2 perícias no mesmo slot
            if (choice.periciasNomes != null && choice.periciasRawsAntes != null) {
                choice.periciasNomes.zip(choice.periciasRawsAntes).forEach { (nome, rawAntes) ->
                    val per = listaPericias.firstOrNull { it.nome == nome }
                    per?.let {
                        // Reverte stack de custo até voltar para rawAntes
                        val stack = state.spCostStackPorPericia.getValue(it)
                        var currentRaw = state.rawTotal(it)
                        while (currentRaw > rawAntes && stack.isNotEmpty()) {
                            val stepCost = stack.removeAt(stack.size - 1)
                            currentRaw = if (currentRaw > 4) currentRaw - 2 else 0
                            // devolve custo na pilha global de progresso
                            // (no nosso fluxo isso será 0, então é inócuo, mas deixo por consistência)
                            refundProgressForStage(state, effectiveStageName, stepCost)
                        }

                        val baseIncs = state.baseIncsPorPericia.getValue(it)
                        if (baseIncs > 0) {
                            state.baseIncsPorPericia[it] = baseIncs - 1
                        }
                    }
                }
            }

            // Recalcula tudo no final
            state.rebuildAllPericiaStacks()
        }

        SlotTipo.REMOVER_COMP_MENOR -> {
            // nada além da devolução de XP já feita acima
        }

        SlotTipo.RESERVA_COMP_MAIOR -> {
            reservasCompMaior.value = (reservasCompMaior.value - 1).coerceAtLeast(0)
        }

        else -> {
            // LIVRE ou futuros tipos
        }
    }
}