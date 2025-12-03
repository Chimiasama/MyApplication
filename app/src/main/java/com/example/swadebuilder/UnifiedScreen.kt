package com.example.swadebuilder

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.model.CriadorViewModel
import com.example.swadebuilder.model.EquipamentoCategoria
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
import com.example.swadebuilder.ui.sections.XpSection
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.semAcentos
import kotlinx.serialization.json.JsonPrimitive

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Preview(showBackground = true)
@Composable
fun PreviewApp() {
    val state = remember { CriadorState() }
    val vm = remember { CriadorViewModel() }

    UnifiedScreen(
        state = state,
        viewModel = vm,   // <<<<<<<<<<<<<< ADICIONADO

        onOpenVantagensDetail = { _ -> },
        onOpenPericiasDetail = {},
        onOpenComplicacoesDetail = {},
        onOpenAtributosDetail = {},
        onOpenListaAncestralidadesDetail = { _ -> },
        onOpenListaCompletaEquipamento = {},
        onOpenPoderesDetail = {},
        onOpenSuperPoderesDetail = { _ -> },

        expInfos = true,
        onToggleInfos = {},

        expAncs = true,
        onToggleAncs = {},

        expComps = true,
        onToggleComps = {},

        expEquip = true,
        onToggleEquip = {},

        expAttrs = true,
        onToggleAttrs = {},
        expPer = true,
        onTogglePer = {},
        expVants = true,
        onToggleVants = {},
        expResumo = true,
        onToggleResumo = {},
        expPoderes = true,
        onTogglePoderes = {},
        expXp = true,
        onToggleXp = {},

        equipamentoCategorias = emptyList(),
        superequipCategorias = emptyList(),
        listaSuperPoderes = emptyList()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun UnifiedScreen(
    state: CriadorState,
    viewModel: CriadorViewModel,
    onOpenVantagensDetail: (String) -> Unit,
    onOpenPericiasDetail: () -> Unit,
    onOpenComplicacoesDetail: () -> Unit,
    onOpenAtributosDetail: () -> Unit,
    onOpenListaAncestralidadesDetail: (String) -> Unit,
    onOpenListaCompletaEquipamento: () -> Unit,
    onOpenPoderesDetail: () -> Unit,
    onOpenSuperPoderesDetail: (String) -> Unit,

    // ✅ expansões hoistadas
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

    expXp: Boolean,
    onToggleXp: () -> Unit,

    equipamentoCategorias: List<EquipamentoCategoria>,
    superequipCategorias: List<EquipamentoCategoria>,
    listaSuperPoderes: List<SuperPoder>
) {
    if (state.modoSupers) {
        Log.d("DEBUG", "modoSupers é ${state.modoSupers}")
    }

    var showAllocDialog by rememberSaveable { mutableStateOf(false) }
    var currentSlotIndex by rememberSaveable { mutableStateOf(-1) }
    val scrollState = rememberScrollState()

    // --- estados para o MEIO-ELFO ---
    var showMeioElfoDialog by rememberSaveable { mutableStateOf(false) }
    var pendingMeioElfoKey by rememberSaveable { mutableStateOf<String?>(null) }
    // --------------------------------
    val creationLocked = state.criacaoBasicaCongelada

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        if (state.modoProgressaoAtivo) {
            if (state.mostrandoVantagensProgresso) {
                // Progression: Advantages
                ResumoSection(state = state, expanded = expResumo, onToggle = onToggleResumo)

                SectionCard(
                    title    = "Vantagens",
                    expanded = expVants,
                    onToggle = onToggleVants,
                    icon     = Icons.Default.Star
                ) {
                    VantagensContent(
                        state = state,
                        multiplosAAHabilitados = state.permiteMultiAntecedenteArcano,
                        onOpenVantagensDetail  = onOpenVantagensDetail,
                        viewModel = viewModel
                    )
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(thickness = 3.dp)

                Button(
                    onClick = {
                        viewModel.finishAdvantageAdvancement()
                        state.mostrandoVantagensProgresso = false
                    },
                    enabled = state.pontosVantagem == 0,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Confirmar Vantagem e Voltar")
                }
                TextButton(
                    onClick = {
                        viewModel.cancelAdvancementInProgress()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancelar")
                }

            } else if (state.mostrandoPericiasProgresso) {
                // Progression: Skills
                ResumoSection(state = state, expanded = expResumo, onToggle = onToggleResumo)

                SectionCard(
                    title    = "Perícias",
                    expanded = expPer,
                    onToggle = onTogglePer,
                    icon     = Icons.Default.School
                ) {
                    PericiasContent(
                        state = state,
                        viewModel = viewModel,
                        onOpenPericiasDetail = onOpenPericiasDetail,
                        feedbackMessages = viewModel.feedbackMessages as MutableList<String>
                    )
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(thickness = 3.dp)

                Button(
                    onClick = {
                        viewModel.finishSkillAdvancement()
                        state.mostrandoPericiasProgresso = false
                    },
                    enabled = state.pontosPericia == 0,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Confirmar Perícias e Voltar")
                }
                TextButton(
                    onClick = {
                        viewModel.cancelAdvancementInProgress()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancelar")
                }

            } else if (state.mostrandoAtributosProgresso) {
                // Progression: Attributes
                ResumoSection(state = state, expanded = expResumo, onToggle = onToggleResumo)

                SectionCard(
                    title    = "Atributos",
                    expanded = expAttrs,
                    onToggle = onToggleAttrs,
                    icon     = Icons.Default.FitnessCenter
                ) {
                    AtributosContent(
                        state = state,
                        viewModel = viewModel,
                        onOpenAtributosDetail = onOpenAtributosDetail
                    )
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(thickness = 3.dp)

                Button(
                    onClick = {
                        viewModel.finishAttributeAdvancement()
                        state.mostrandoAtributosProgresso = false
                    },
                    enabled = state.pontosAtributo == 0,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Confirmar Atributo e Voltar")
                }
                TextButton(
                    onClick = {
                        viewModel.cancelAdvancementInProgress()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancelar")
                }

            } else {
                // Default Progression View
                ResumoSection(state = state, expanded = expResumo, onToggle = onToggleResumo)
                PoderesSection(state = state, expanded = expPoderes, onToggle = onTogglePoderes, onOpenPoderesDetail = onOpenPoderesDetail)
                SuperPoderesSection(state = state, listaSuperPoderes = listaSuperPoderes, expanded = expPoderes, onToggle = onTogglePoderes, onOpenSuperPoderesDetail = onOpenSuperPoderesDetail)
                EquipamentoSection(state = state, expanded = expEquip, onToggle = onToggleEquip, onOpenListaCompletaEquipamento = onOpenListaCompletaEquipamento, equipamentoCategorias = equipamentoCategorias, superequipCategorias = superequipCategorias)

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(thickness = 3.dp)

                XpSection(
                    state = state,
                    expanded = expXp,
                    onToggle = onToggleXp,
                    onUseProgress = { index ->
                        currentSlotIndex = index
                        showAllocDialog = true
                    },
                    onUndo = {
                        viewModel.revertLastAdvancement()
                    }
                )
            }
        } else {
            // Creation Phase Layout
            InformacoesSection(
                state = state,
                expanded = expInfos,
                onToggle = onToggleInfos,
                onUseProgress = { showAllocDialog = true }
            )

            HorizontalDivider(thickness = 1.dp)

            AncestralidadesSection(
                currentAncestralidade = state.ancestralidade,
                expanded = expAncs,
                onToggle = onToggleAncs,
                supersLocked = creationLocked,
                ancestralidadeEmFoco = state.ancestralidadeEmFoco,
                onOpenListaAncestralidadesDetail = onOpenListaAncestralidadesDetail,
                onSelectAncestralidade = { nome ->
                    val key = nome.uppercase().semAcentos()
                    if (key == state.ancestralidade) return@AncestralidadesSection
                    if (key == "MEIO-ELFOS") {
                        pendingMeioElfoKey = key
                        showMeioElfoDialog = true
                    } else {
                        pendingMeioElfoKey = null
                        state.aplicarAncestralidade(
                            key,
                            viewModel.feedbackMessages as MutableList<String>
                        )
                    }
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
                title    = "Atributos",
                expanded = expAttrs,
                onToggle = onToggleAttrs,
                icon     = Icons.Default.FitnessCenter
            ) {
                AtributosContent(state, viewModel, onOpenAtributosDetail)
            }

            HorizontalDivider(thickness = 1.dp)

            SectionCard(
                title    = "Perícias",
                expanded = expPer,
                onToggle = onTogglePer,
                icon     = Icons.Default.School
            ) {
                PericiasContent(
                    state = state,
                    viewModel = viewModel,
                    onOpenPericiasDetail = onOpenPericiasDetail,
                    feedbackMessages = viewModel.feedbackMessages as MutableList<String>
                )
            }

            HorizontalDivider(thickness = 1.dp)

            SectionCard(
                title    = "Vantagens",
                expanded = expVants,
                onToggle = onToggleVants,
                icon     = Icons.Default.Star
            ) {
                VantagensContent(
                    state = state,
                    multiplosAAHabilitados = state.permiteMultiAntecedenteArcano,
                    onOpenVantagensDetail  = onOpenVantagensDetail,
                    viewModel = viewModel
                )
            }

            PoderesSection(state = state, expanded = expPoderes, onToggle = onTogglePoderes, onOpenPoderesDetail = onOpenPoderesDetail)

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(thickness = 1.dp)

            SuperPoderesSection(state = state, listaSuperPoderes = listaSuperPoderes, expanded = expPoderes, onToggle = onTogglePoderes, onOpenSuperPoderesDetail = onOpenSuperPoderesDetail)
            EquipamentoSection(state = state, expanded = expEquip, onToggle = onToggleEquip, onOpenListaCompletaEquipamento = onOpenListaCompletaEquipamento, equipamentoCategorias = equipamentoCategorias, superequipCategorias = superequipCategorias)

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(thickness = 3.dp)

            ResumoSection(state = state, expanded = expResumo, onToggle = onToggleResumo)

                Button(
                    onClick = {
                        state.modoProgressaoAtivo = true
                        state.progresso = 4
                        state.frozenAdvantageCount = state.vantagensSelecionadas.size
                        state.frozenSkillIncrements.clear()
                        state.baseIncsPorPericia.forEach { (pericia, incs) ->
                            state.frozenSkillIncrements[pericia.nome] = incs
                        }
                        state.recomputeAvailableProgress()
                    },
                    enabled = state.creationComplete(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Iniciar Progressão")
                }
            }
        }

    if (showMeioElfoDialog && pendingMeioElfoKey != null) {
        AlertDialog(
            onDismissRequest = {
                pendingMeioElfoKey = null
                showMeioElfoDialog = false
            },
            title = { Text("Meio-Elfo: escolha a herança") },
            text = {
                Text(
                    "Defina como a herança meio-élfica se manifesta:\n\n" +
                            "• Herança Élfica: começa com Agilidade em d6.\n" +
                            "• Herança Humana: ganha +1 Ponto de Vantagem na criação."
                )
            },
            confirmButton = {
                // Herança Élfica (Agilidade d6)
                TextButton(
                    onClick = {
                        val key = pendingMeioElfoKey ?: return@TextButton

                        // Aplica a ancestralidade Meio-Elfo
                        state.aplicarAncestralidade(
                            key,
                            viewModel.feedbackMessages as MutableList<String>
                        )

                        // Garante Agilidade em d6 (raw = 6) se ainda estiver abaixo
                        val agiState = state.valoresAtributos["AGILIDADE"]
                        if (agiState != null && agiState.intValue < 6) {
                            agiState.intValue = 6
                        }

                        state.meioElfoAgil = true
                        pendingMeioElfoKey = null
                        showMeioElfoDialog = false
                    }
                ) {
                    Text("Herança Élfica (Agilidade d6)")
                }
            },
            dismissButton = {
                // Herança Humana (+1 PV)
                TextButton(
                    onClick = {
                        val key = pendingMeioElfoKey ?: return@TextButton

                        // Aplica a ancestralidade Meio-Elfo
                        state.aplicarAncestralidade(
                            key,
                            viewModel.feedbackMessages as MutableList<String>
                        )

                        // Dá 1 ponto de vantagem extra
                        state.pontosVantagem += 1

                        pendingMeioElfoKey = null
                        showMeioElfoDialog = false
                    }
                ) {
                    Text("Herança Humana (+1 PV)")
                }
            }
        )
    }

    if (showAllocDialog) {
        ProgressosDialog(
            state = state,
            slotIndex = currentSlotIndex,
            onDismiss = { showAllocDialog = false },
            onStartSkillAdvancement = { slotIndex, stage ->
                viewModel.startSkillAdvancement(slotIndex, stage)
            },
            onStartAdvantageAdvancement = { slotIndex, est ->
                viewModel.startAdvantageAdvancement(slotIndex, est)
            },
            onStartAttributeAdvancement = { slotIndex, stage, consumeReservation ->
                viewModel.startAttributeAdvancement(slotIndex, stage, consumeReservation)
            },
            onReserveLegendaryAttribute = { slotIndex, stage ->
                viewModel.reserveLegendaryAttribute(slotIndex, stage)
            }
        )
    }
}

@Composable
private fun ResumoSection(
    state: CriadorState,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    SectionCard(
        title = "Resumo do Personagem",
        expanded = expanded,
        onToggle = onToggle,
        icon = Icons.Default.Description
    ) {
        SummaryContent(state)
    }
}

@Composable
private fun PoderesSection(
    state: CriadorState,
    expanded: Boolean,
    onToggle: () -> Unit,
    onOpenPoderesDetail: () -> Unit
) {
    val temArcano = state.vantagensSelecionadas.any {
        it.nome.keyify().startsWith("ANTECEDENTE ARCANO")
    }
    if (temArcano && !state.celestialAAMilagresDesabilitado) {
        HorizontalDivider(thickness = 1.dp)
        SectionCard(
            title = "Poderes",
            expanded = expanded,
            onToggle = onToggle,
            icon = Icons.Default.FlashOn
        ) {
            PoderesSection(
                state = state,
                onOpenListaCompletaPoderes = onOpenPoderesDetail
            )
        }
    }
}

@Composable
private fun SuperPoderesSection(
    state: CriadorState,
    listaSuperPoderes: List<SuperPoder>,
    expanded: Boolean,
    onToggle: () -> Unit,
    onOpenSuperPoderesDetail: (String) -> Unit
) {
    if (state.modoSupers) {
        SuperPoderesContent(
            state = state,
            listaSuperPoderes = listaSuperPoderes,
            expanded = expanded,
            onToggle = onToggle,
            onOpenSuperPoderesDetail = onOpenSuperPoderesDetail
        )
    }
}

@Composable
private fun EquipamentoSection(
    state: CriadorState,
    expanded: Boolean,
    onToggle: () -> Unit,
    onOpenListaCompletaEquipamento: () -> Unit,
    equipamentoCategorias: List<EquipamentoCategoria>,
    superequipCategorias: List<EquipamentoCategoria>
) {
    EquipamentoSection(
        dinheiro = state.dinheiro,
        pcTotal = state.pontosComplicacao,
        pcLivres = (state.pontosComplicacao - state.pontosComplicacaoGastos).coerceAtLeast(0),
        recursosPcUsados = state.cpRecursosStack.size,
        expanded = expanded,
        onToggle = onToggle,
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
}