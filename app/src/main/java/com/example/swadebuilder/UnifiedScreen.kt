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
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.swadebuilder.ui.sections.PericiasContent
import com.example.swadebuilder.ui.sections.PoderesSection
import com.example.swadebuilder.ui.sections.SummaryContent
import com.example.swadebuilder.ui.sections.SuperPoderesContent
import com.example.swadebuilder.ui.sections.TipoMonstroSection
import com.example.swadebuilder.ui.sections.VantagensContent
import com.example.swadebuilder.ui.sections.XpSection
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.semAcentos
import kotlinx.serialization.json.JsonPrimitive

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
// @Preview(showBackground = true) // Commented out to avoid build errors with ViewModel
@Composable
fun PreviewApp() {
    val state = remember { CriadorState() }
    val vm = remember { CriadorViewModel() }

    UnifiedScreen(
        state = state,
        viewModel = vm,
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

        expMonstro = true,
        onToggleMonstro = {},

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

    expMonstro: Boolean,
    onToggleMonstro: () -> Unit,

    equipamentoCategorias: List<EquipamentoCategoria>,
    superequipCategorias: List<EquipamentoCategoria>,
    listaSuperPoderes: List<SuperPoder>
) {
    if (state.modoSupers) {
        Log.d("DEBUG", "modoSupers é ${state.modoSupers}")
    }

    var showAllocDialog by rememberSaveable { mutableStateOf(false) }
    var currentSlotIndex by rememberSaveable { mutableIntStateOf(-1) }
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
                        viewModel = viewModel
                    )
                }

                if (state.mostrandoPoderesProgresso || state.arcanoCompraPendente()) {
                    Spacer(Modifier.height(8.dp))
                    PoderesSection(state = state, expanded = expPoderes, onToggle = onTogglePoderes)
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(thickness = 3.dp)

                Button(
                    onClick = {
                        viewModel.finishAdvantageAdvancement()
                        state.mostrandoVantagensProgresso = false
                    },
                    enabled = state.pontosVantagem == 0 && !state.arcanoCompraPendente(),
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
                    AtributosContent(state = state)
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
                EquipamentoSection(state = state, expanded = expEquip, onToggle = onToggleEquip, equipamentoCategorias = equipamentoCategorias, superequipCategorias = superequipCategorias)

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
            ResumoSection(state = state, expanded = expResumo, onToggle = onToggleResumo)

            HorizontalDivider(thickness = 1.dp)

            AncestralidadesSection(
                state = state,
                currentAncestralidade = state.ancestralidade,
                expanded = expAncs,
                onToggle = onToggleAncs,
                supersLocked = creationLocked,
                ancestralidadeEmFoco = state.ancestralidadeEmFoco,
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

            if (state.modoMonstroAtivo) {
                HorizontalDivider(thickness = 1.dp)
                TipoMonstroSection(
                    state = state,
                    expanded = expMonstro,
                    onToggle = onToggleMonstro
                )
            }

            HorizontalDivider(thickness = 1.dp)

            ComplicacoesSection(
                state = state,
                expanded = expComps,
                onToggle = onToggleComps,
                feedbackMessages = viewModel.feedbackMessages as MutableList<String>
            )

            HorizontalDivider(thickness = 1.dp)

            SectionCard(
                title    = "Atributos",
                expanded = expAttrs,
                onToggle = onToggleAttrs,
                icon     = Icons.Default.FitnessCenter
            ) {
                AtributosContent(state)
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
                    feedbackMessages = viewModel.feedbackMessages
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
                    viewModel = viewModel
                )
            }

            PoderesSection(state = state, expanded = expPoderes, onToggle = onTogglePoderes)

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(thickness = 1.dp)

            SuperPoderesSection(state = state, listaSuperPoderes = listaSuperPoderes, expanded = expPoderes, onToggle = onTogglePoderes)
            EquipamentoSection(state = state, expanded = expEquip, onToggle = onToggleEquip, equipamentoCategorias = equipamentoCategorias, superequipCategorias = superequipCategorias)

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(thickness = 3.dp)

            if (state.creationComplete()) {
                Button(
                        onClick = {
                            state.modoProgressaoAtivo = true
                            state.progresso = 4
                            state.frozenAdvantageCount = state.vantagensSelecionadas.size
                            state.snapshotFrozenSkillIncrements()
                            state.recomputeAvailableProgress()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Iniciar Progressão")
                    }
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
    onToggle: () -> Unit
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
                state = state
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
private fun SuperPoderesSection(
    state: CriadorState,
    listaSuperPoderes: List<SuperPoder>,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    if (state.modoSupers) {
        SuperPoderesContent(
            state = state,
            listaSuperPoderes = listaSuperPoderes,
            expanded = expanded,
            onToggle = onToggle
        )
    }
}

@Composable
private fun EquipamentoSection(
    state: CriadorState,
    expanded: Boolean,
    onToggle: () -> Unit,
    equipamentoCategorias: List<EquipamentoCategoria>,
    superequipCategorias: List<EquipamentoCategoria>
) {
    val hasMusculoso = state.vantagensSelecionadas.any { it.nome.keyify() == "MUSCULOSO" }
    val hasSoldado = state.vantagensSelecionadas.any { it.nome.keyify() == "SOLDADO" }

    EquipamentoSection(
        dinheiro = state.dinheiro,
        pcTotal = state.pontosComplicacao,
        pcLivres = (state.pontosComplicacao - state.pontosComplicacaoGastos).coerceAtLeast(0),
        recursosPcUsados = state.cpRecursosStack.size,
        emProgresso = state.emProgresso,
        modoProgressaoAtivo = state.modoProgressaoAtivo,
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
        superequipCategorias =
            if (state.modoSuperequip) superequipCategorias else emptyList(),
        forcaRaw = state.valoresAtributos["FORCA"]?.intValue ?: 4,
        hasMusculoso = hasMusculoso,
        hasSoldado = hasSoldado,
        soldadoCargaAtivo = state.soldadoCargaAtivo,
        onEditarDinheiro = { novoValor -> state.dinheiro = novoValor },
        onToggleSoldadoCarga = {
            if (hasSoldado) {
                state.soldadoCargaAtivo = !state.soldadoCargaAtivo
            }
        },
        compendioFantasiaAtivo = state.compendioFantasiaAtivo
    )
}