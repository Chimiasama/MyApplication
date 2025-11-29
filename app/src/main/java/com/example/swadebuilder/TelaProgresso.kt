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
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.model.CriadorViewModel
import com.example.swadebuilder.model.EquipamentoCategoria
import com.example.swadebuilder.Pericia
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.ui.components.SectionCard
import com.example.swadebuilder.ui.sections.AncestralidadesSection
import com.example.swadebuilder.ui.sections.AtributosContent
import com.example.swadebuilder.ui.sections.ComplicacoesSection
import com.example.swadebuilder.ui.sections.EquipamentoSection
import com.example.swadebuilder.ui.sections.PericiasContent
import com.example.swadebuilder.ui.sections.PoderesSection
import com.example.swadebuilder.ui.sections.SummaryContent
import com.example.swadebuilder.ui.sections.SuperPoderesContent
import com.example.swadebuilder.ui.dialogs.ProgressosDialog
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.semAcentos
import com.example.swadebuilder.listaDeEstagios
import com.example.swadebuilder.dynamicStageCaps
import com.example.swadebuilder.listaAtributos
import com.example.swadebuilder.listaPericias
import com.example.swadebuilder.listaVantagens
import kotlinx.serialization.json.JsonPrimitive

private data class EstagioSlotMeta(
    val estagio: Estagio,
    val rotulo: String,
    val quantidade: Int
)

private class ProgressoDeEstagioSlot(
    val id: String,
    val estagio: Estagio,
    val abreviacao: String,
    val indice: Int
) {
    var escolha by mutableStateOf<EscolhaProgresso?>(null)
}

private fun escolhaLabel(escolha: EscolhaProgresso): String = when (escolha) {
    is EscolhaProgresso.Atributo -> "Atributo +1 (${escolha.atributo})"
    is EscolhaProgresso.PericiaEscolhida -> "Perícia: ${escolha.pericia.nome}"
    is EscolhaProgresso.VantagemEscolhida -> "Vantagem: ${escolha.vantagem.nome}"
    is EscolhaProgresso.RemoverComplicacaoMenor -> "Remover Complicação: ${escolha.nome}"
    EscolhaProgresso.ReservaComplicacaoMaior -> "Reserva para Complicação Maior"
}

private fun resumoRequisitos(v: Vantagem): List<String> {
    val linhas = mutableListOf<String>()
    val req = v.requisitos
    if (req.estagio.isNotBlank()) linhas += "Estágio mínimo: ${req.estagio}"
    if (req.atributoMin.isNotEmpty()) {
        val attr = req.atributoMin.entries.joinToString { (nome, valor) -> "$nome d$valor" }
        linhas += "Atributos: $attr"
    }
    if (req.periciaMin.isNotEmpty()) {
        val per = req.periciaMin.entries.joinToString { (nome, valor) -> "$nome d$valor" }
        linhas += "Perícias: $per"
    }
    if (req.vantagensPrevias.isNotEmpty()) {
        linhas += "Pré-requisitos: ${req.vantagensPrevias.joinToString()}"
    }
    if (req.observacoes.isNotBlank()) linhas += req.observacoes
    if (linhas.isEmpty()) linhas += "Sem requisitos adicionais"
    return linhas
}

private sealed class EscolhaProgresso(val tipo: String) {
    data class Atributo(val atributo: String) : EscolhaProgresso("Atributo")
    data class VantagemEscolhida(val vantagem: Vantagem) : EscolhaProgresso("Vantagem")
    data class PericiaEscolhida(val pericia: Pericia) : EscolhaProgresso("Perícia")
    data class RemoverComplicacaoMenor(val nome: String) : EscolhaProgresso("Complicação")
    object ReservaComplicacaoMaior : EscolhaProgresso("ReservaComplicaçãoMaior")
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun TelaProgresso(
    state: CriadorState,
    viewModel: CriadorViewModel,
    onBack: () -> Unit,
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
    var showProgressDialog by rememberSaveable { mutableStateOf(false) }
    var expProgressos by rememberSaveable { mutableStateOf(true) }
    val prototipoMetas = remember {
        listOf(
            EstagioSlotMeta(listaDeEstagios.first { it.nome == "Novato" }, "N", 3),
            EstagioSlotMeta(listaDeEstagios.first { it.nome == "Experiente" }, "E", 4),
            EstagioSlotMeta(listaDeEstagios.first { it.nome == "Veterano" }, "V", 4),
            EstagioSlotMeta(listaDeEstagios.first { it.nome == "Heroico" }, "H", 4),
            EstagioSlotMeta(listaDeEstagios.first { it.nome == "Lendário" }, "L", 4),
        )
    }
    val prototipoSlots = remember {
        mutableStateListOf<ProgressoDeEstagioSlot>().apply {
            prototipoMetas.forEach { meta ->
                repeat(meta.quantidade) { idx ->
                    add(
                        ProgressoDeEstagioSlot(
                            id = "${meta.rotulo}_$idx",
                            estagio = meta.estagio,
                            abreviacao = meta.rotulo,
                            indice = idx
                        )
                    )
                }
            }
        }
    }
    var slotEmFoco by remember { mutableStateOf<ProgressoDeEstagioSlot?>(null) }
    var tipoEscolhido by remember { mutableStateOf<String?>(null) }
    var atributoSelecionado by remember { mutableStateOf<String?>(null) }
    var periciaSelecionada by remember { mutableStateOf<Pericia?>(null) }
    var vantagemSelecionada by remember { mutableStateOf<Vantagem?>(null) }
    var compMenorSelecionada by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()

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

        Spacer(Modifier.height(16.dp))

        SectionCard(
            title = "Progressos (XP)",
            expanded = expProgressos,
            onToggle = { expProgressos = !expProgressos },
            icon = Icons.Default.FlashOn
        ) {
            val reachedStages = listaDeEstagios.filter { state.progresso >= it.minProgress }

            Text(
                text = "Progressos disponíveis: ${state.progressosDisponiveis}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val slots = state.progressosDisponiveis.coerceAtLeast(1)
                repeat(slots) { idx ->
                    AssistChip(
                        onClick = { showProgressDialog = true },
                        enabled = state.progressosDisponiveis > 0,
                        label = {
                            if (state.progressosDisponiveis > 0) {
                                Text("XP ${idx + 1}")
                            } else {
                                Text("Sem XP")
                            }
                        }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 180.dp)
            ) {
                items(reachedStages) { est ->
                    val idx = listaDeEstagios.indexOf(est)
                    val cap = dynamicStageCaps.getOrElse(idx) { 0 }
                    val spent = state.stageXpSpent[est.nome] ?: 0
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(est.nome, fontWeight = FontWeight.SemiBold)
                        Text("$spent / $cap", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { showProgressDialog = true },
                enabled = state.progressosDisponiveis > 0,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Usar progresso para comprar melhorias")
            }
        }

        Spacer(Modifier.height(16.dp))
        HorizontalDivider(thickness = 3.dp)

        SectionCard(
            title = "Progressos por estágio (protótipo)",
            expanded = true,
            onToggle = { },
            icon = Icons.Default.FlashOn
        ) {
            Text(
                text = "Protótipo inspirado na coluna lateral da ficha física. Cada slot armazena uma compra seguindo as regras por estágio.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            prototipoMetas.forEach { meta ->
                Text(
                    text = "${meta.rotulo} — ${meta.estagio.nome}",
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    prototipoSlots.filter { it.estagio == meta.estagio }.forEach { slot ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    slotEmFoco = slot
                                    tipoEscolhido = slot.escolha?.tipo
                                    atributoSelecionado = (slot.escolha as? EscolhaProgresso.Atributo)?.atributo
                                    periciaSelecionada = (slot.escolha as? EscolhaProgresso.PericiaEscolhida)?.pericia
                                    vantagemSelecionada = (slot.escolha as? EscolhaProgresso.VantagemEscolhida)?.vantagem
                                    compMenorSelecionada = (slot.escolha as? EscolhaProgresso.RemoverComplicacaoMenor)?.nome
                                }
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${slot.abreviacao} ${slot.indice + 1}",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(52.dp)
                            )
                            val escolhaLabel = slot.escolha?.let { escolhaLabel(it) } ?: "(vazio)"
                            Text(
                                text = escolhaLabel,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            val reservasMaior = prototipoSlots.count { it.escolha is EscolhaProgresso.ReservaComplicacaoMaior }
            if (reservasMaior > 0) {
                Text(
                    text = "Reservas para Complicação Maior: $reservasMaior",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Text(
                text = "Limite técnico: 1 aumento de atributo por estágio (exceto L). Se precisar mudar a regra, ajuste no salvamento futuro.",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

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

    if (showProgressDialog) {
        ProgressosDialog(state) {
            showProgressDialog = false
        }
    }

    slotEmFoco?.let { slot ->
        val stageIndex = listaDeEstagios.indexOf(slot.estagio)
        val attrUsos = prototipoSlots.count {
            it.estagio.nome == slot.estagio.nome && it.escolha is EscolhaProgresso.Atributo
        } - if (slot.escolha is EscolhaProgresso.Atributo) 1 else 0
        val atributoBloqueado = attrUsos >= 1 && slot.estagio.nome != "Lendário"
        val complicacoesMenores = state.complicacoesSelecionadas
            .filterValues { it.equals("Menor", ignoreCase = true) }
            .keys
        val periciasValidas = listaPericias.filter { per ->
            state.rawTotal(per) < state.periciaCapRaw(per)
        }
        val vantagensCompat = listaVantagens.filter { v ->
            val reqEst = v.requisitos.estagio
            val reqIdx = listaDeEstagios.indexOfFirst { it.nome.equals(reqEst, ignoreCase = true) }
            reqIdx == -1 || reqIdx <= stageIndex
        }

        AlertDialog(
            onDismissRequest = {
                slotEmFoco = null
                tipoEscolhido = null
                atributoSelecionado = null
                periciaSelecionada = null
                vantagemSelecionada = null
                compMenorSelecionada = null
            },
            title = {
                Text("${slot.abreviacao} ${slot.indice + 1} — ${slot.estagio.nome}")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(
                            label = { Text("Atributo") },
                            onClick = {
                                tipoEscolhido = "Atributo"
                                periciaSelecionada = null
                                vantagemSelecionada = null
                                compMenorSelecionada = null
                            },
                            enabled = !atributoBloqueado || slot.escolha is EscolhaProgresso.Atributo
                        )
                        AssistChip(
                            label = { Text("Vantagem") },
                            onClick = {
                                tipoEscolhido = "Vantagem"
                                atributoSelecionado = null
                                periciaSelecionada = null
                                compMenorSelecionada = null
                            }
                        )
                        AssistChip(
                            label = { Text("Perícia") },
                            onClick = {
                                tipoEscolhido = "Perícia"
                                atributoSelecionado = null
                                vantagemSelecionada = null
                                compMenorSelecionada = null
                            },
                            enabled = periciasValidas.isNotEmpty()
                        )
                        AssistChip(
                            label = { Text("Complicação Menor") },
                            onClick = {
                                tipoEscolhido = "Complicação"
                                atributoSelecionado = null
                                vantagemSelecionada = null
                                periciaSelecionada = null
                            },
                            enabled = complicacoesMenores.isNotEmpty()
                        )
                        AssistChip(
                            label = { Text("Reserva p/ Maior") },
                            onClick = {
                                tipoEscolhido = "ReservaComplicaçãoMaior"
                                atributoSelecionado = null
                                vantagemSelecionada = null
                                periciaSelecionada = null
                                compMenorSelecionada = null
                            }
                        )
                    }

                    when (tipoEscolhido) {
                        "Atributo" -> {
                            if (atributoBloqueado) {
                                Text(
                                    text = "Este estágio já tem um aumento de atributo. Regra protótipo: apenas 1 por estágio.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listaAtributos.forEach { attr ->
                                    AssistChip(
                                        onClick = { atributoSelecionado = attr },
                                        label = { Text(attr) }
                                    )
                                }
                            }
                        }
                        "Perícia" -> {
                            if (periciasValidas.isEmpty()) {
                                Text("Nenhuma perícia pode ser aumentada via avanço agora.")
                            } else {
                                LazyColumn(modifier = Modifier.heightIn(max = 220.dp)) {
                                    items(periciasValidas) { per ->
                                        val atual = state.rawTotal(per)
                                        val cap = state.periciaCapRaw(per)
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { periciaSelecionada = per }
                                                .padding(vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(per.nome)
                                            Text("d$atual → d${atual + 1} (cap d$cap)")
                                        }
                                    }
                                }
                            }
                        }
                        "Vantagem" -> {
                            Text(
                                text = "Mostrando vantagens com estágio ≤ ${slot.estagio.nome}.",
                                style = MaterialTheme.typography.bodySmall
                            )
                            LazyColumn(modifier = Modifier.heightIn(max = 240.dp)) {
                                items(vantagensCompat) { vant ->
                                    val atende = state.podeSelecionar(vant)
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { vantagemSelecionada = vant }
                                            .padding(vertical = 6.dp)
                                    ) {
                                        Text(vant.nome, fontWeight = FontWeight.SemiBold)
                                        resumoRequisitos(vant).forEach { linha ->
                                            Text(
                                                text = linha,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (atende) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        "Complicação" -> {
                            if (complicacoesMenores.isEmpty()) {
                                Text("Nenhuma complicação menor disponível para remoção.")
                            } else {
                                LazyColumn(modifier = Modifier.heightIn(max = 180.dp)) {
                                    items(complicacoesMenores.toList()) { comp ->
                                        Text(
                                            text = comp.name,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { compMenorSelecionada = comp.name }
                                                .padding(vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                        "ReservaComplicaçãoMaior" -> {
                            Text(
                                text = "Reserva técnica para remover Complicação Maior futuramente. Use múltiplas reservas se necessário.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            },
            confirmButton = {
                val confirmEnabled = when (tipoEscolhido) {
                    "Atributo" -> atributoSelecionado != null && (!atributoBloqueado || slot.escolha is EscolhaProgresso.Atributo)
                    "Perícia" -> periciaSelecionada != null
                    "Vantagem" -> vantagemSelecionada?.let { state.podeSelecionar(it) } == true
                    "Complicação" -> compMenorSelecionada != null
                    "ReservaComplicaçãoMaior" -> true
                    else -> false
                }

                TextButton(
                    onClick = {
                        slot.escolha = when (tipoEscolhido) {
                            "Atributo" -> atributoSelecionado?.let { EscolhaProgresso.Atributo(it) }
                            "Perícia" -> periciaSelecionada?.let { EscolhaProgresso.PericiaEscolhida(it) }
                            "Vantagem" -> vantagemSelecionada?.let { EscolhaProgresso.VantagemEscolhida(it) }
                            "Complicação" -> compMenorSelecionada?.let { EscolhaProgresso.RemoverComplicacaoMenor(it) }
                            "ReservaComplicaçãoMaior" -> EscolhaProgresso.ReservaComplicacaoMaior
                            else -> null
                        }
                        slotEmFoco = null
                        tipoEscolhido = null
                        atributoSelecionado = null
                        periciaSelecionada = null
                        vantagemSelecionada = null
                        compMenorSelecionada = null
                    },
                    enabled = confirmEnabled
                ) { Text("Aplicar") }
            },
            dismissButton = {
                TextButton(onClick = {
                    slotEmFoco = null
                    tipoEscolhido = null
                    atributoSelecionado = null
                    periciaSelecionada = null
                    vantagemSelecionada = null
                    compMenorSelecionada = null
                }) { Text("Cancelar") }
            }
        )
    }

}
