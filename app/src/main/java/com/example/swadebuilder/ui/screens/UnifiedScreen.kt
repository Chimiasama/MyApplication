package com.example.swadebuilder.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.model.CriadorState
import com.example.swadebuilder.model.EquipamentoCategoria
import com.example.swadebuilder.model.SuperPoder
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
import com.example.swadebuilder.util.semAcentos
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import com.example.swadebuilder.racialAttrMinMap


@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun UnifiedScreen(
    state: CriadorState,
    onOpenVantagensDetail: (String) -> Unit,
    onOpenPericiasDetail: () -> Unit,
    onOpenComplicacoesDetail: () -> Unit,
    onOpenAtributosDetail: () -> Unit,
    onOpenListaAncestralidadesDetail: () -> Unit,
    onOpenListaCompletaEquipamento: () -> Unit,
    onOpenPoderesDetail: () -> Unit,
    onOpenSuperPoderesDetail: (String) -> Unit,
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

    equipamentoCategorias: List<EquipamentoCategoria>,
    superequipCategorias: List<EquipamentoCategoria>,
    listaSuperPoderes: List<SuperPoder>
) {
    LaunchedEffect(state.modoSupers) {
        Log.d("DEBUG", "modoSupers é ${state.modoSupers}")
    }
    var showAllocDialog by rememberSaveable { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    // --- estados para o MEIO-ELFO ---
    var showMeioElfoDialog by rememberSaveable { mutableStateOf(false) }
    var pendingMeioElfoKey by rememberSaveable { mutableStateOf<String?>(null) }
    // ---------------------------------
    val supersLocked = state.criacaoBasicaCongelada

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // ─── Informações iniciais ────────────────────────────────────────────────
        InformacoesSection(
            state = state,
            onUseProgress = { showAllocDialog = true }
        )

        HorizontalDivider(thickness = 1.dp)

        // ─── Ancestralidades ──────────────────────────────────────────────────────
        AncestralidadesSection(
            currentAncestralidade = state.ancestralidade,
            supersLocked = supersLocked,
            onOpenListaAncestralidadesDetail = onOpenListaAncestralidadesDetail,
            onSelectAncestralidade = { nome ->
                val key = nome.uppercase().semAcentos()

                if (key == state.ancestralidade) return@AncestralidadesSection

                if (key == "MEIO-ELFOS") {
                    pendingMeioElfoKey = key
                    showMeioElfoDialog = true
                } else {
                    pendingMeioElfoKey = null
                    state.aplicarAncestralidade(key)
                }
            }
        )

        HorizontalDivider(thickness = 1.dp)

        // ─── Complicações ─────────────────────────────────────────────────────────
        ComplicacoesSection(
            state = state,
            onOpenComplicacoesDetail = onOpenComplicacoesDetail
        )

        HorizontalDivider(thickness = 1.dp)

        // ─── Atributos ────────────────────────────────────────────────────────────
        SectionCard(
            title    = "Atributos",
            expanded = expAttrs,
            onToggle = onToggleAttrs,
            icon     = Icons.Default.FitnessCenter
        ) {
            AtributosContent(state, onOpenAtributosDetail)
        }

        HorizontalDivider(thickness = 1.dp)

        // ─── Perícias ─────────────────────────────────────────────────────────────
        SectionCard(
            title    = "Perícias",
            expanded = expPer,
            onToggle = onTogglePer,
            icon     = Icons.Default.School
        ) {
            PericiasContent(state, onOpenPericiasDetail)
        }

        HorizontalDivider(thickness = 1.dp)

        // ─── Vantagens ────────────────────────────────────────────────────────────
        SectionCard(
            title    = "Vantagens",
            expanded = expVants,
            onToggle = onToggleVants,
            icon     = Icons.Default.Star
        ) {
            VantagensContent(
                state = state,
                multiplosAAHabilitados = state.permiteMultiAntecedenteArcano,
                onOpenVantagensDetail  = onOpenVantagensDetail
            )
        }

        // ─── Poderes (só mostra se já escolheu Arcano) ───────────────────────────
        if (state.vantagensSelecionadas.any { it.nome.semAcentos().startsWith("ANTECEDENTE ARCANO") }) {
            HorizontalDivider(thickness = 1.dp)

            // ─── Poderes (magias) ─────────────────────────────────────────────────────
            SectionCard(
                title    = "Poderes",
                expanded = expPoderes,
                onToggle = onTogglePoderes,
                icon     = Icons.Default.FlashOn
            ) {
                PoderesSection(
                    state = state,
                    onOpenListaCompletaPoderes = onOpenPoderesDetail
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        HorizontalDivider(thickness = 1.dp)

        // ─── SuperPoderes (modo Supers) ─────────────────────────────────────────
        if (state.modoSupers) {
            SuperPoderesContent(
                state                 = state,
                listaSuperPoderes     = listaSuperPoderes,
                expanded              = expPoderes,
                onToggle              = onTogglePoderes,
                onOpenSuperPoderesDetail = onOpenSuperPoderesDetail
            )
        }

        EquipamentoSection(
            dinheiro                 = state.dinheiro,
            pcTotal                  = state.pontosComplicacao,
            pcLivres                 = (state.pontosComplicacao - state.pontosComplicacaoGastos).coerceAtLeast(0),
            recursosPcUsados         = state.cpRecursosStack.size,
            onUsarPontosBonusEmRecursos = {
                val pcLivresLocal =
                    (state.pontosComplicacao - state.pontosComplicacaoGastos).coerceAtLeast(0)

                // Só permite 1 PB em Recursos
                if (pcLivresLocal > 0 && state.cpRecursosStack.isEmpty()) {
                    state.cpRecursosStack.add(Unit)
                    state.pontosComplicacaoGastos += 1
                    state.dinheiro += 500
                }
            },
            onDesfazerPontosBonusEmRecursos = {
                // Só devolve se ainda tiver pelo menos 500 em dinheiro
                if (state.cpRecursosStack.isNotEmpty() && state.dinheiro >= 500) {
                    state.cpRecursosStack.removeAt(state.cpRecursosStack.lastIndex)
                    state.pontosComplicacaoGastos =
                        (state.pontosComplicacaoGastos - 1).coerceAtLeast(0)
                    state.dinheiro -= 500
                }
            },
            onListaCompletaClick     = onOpenListaCompletaEquipamento,
            onEquipamentoDoubleClick = { equipamento ->
                val custo = (equipamento.custo as? kotlinx.serialization.json.JsonPrimitive)
                    ?.content?.toIntOrNull() ?: 0
                if (custo <= state.dinheiro) {
                    state.equipamentosComprados.add(equipamento)
                    state.dinheiro -= custo
                }
            },
            equipamentosComprados    = state.equipamentosComprados,
            onRemoveEquipamentoClick = { equipamento ->
                val custo = (equipamento.custo as? kotlinx.serialization.json.JsonPrimitive)
                    ?.content?.toIntOrNull() ?: 0
                state.equipamentosComprados.remove(equipamento)
                state.dinheiro += custo
            },
            categorias               = equipamentoCategorias,
            superequipCategorias     = superequipCategorias,
            state = state
        )

        Spacer(Modifier.height(16.dp))
        HorizontalDivider(thickness = 3.dp)

        // ─── Resumo ───────────────────────────────────────────────────────────────
        SectionCard(
            title    = "Resumo do Personagem",
            expanded = expResumo,
            onToggle = onToggleResumo,
            icon     = Icons.Default.Description
        ) {
            SummaryContent(state)
        }

        // ─── Diálogo de alocação de progressos ──────────────────────────────────
        if (showAllocDialog) {
            ProgressosDialog(state) {
                state.frozenAdvCount = state.vantagensSelecionadas.size
                state.emProgresso    = true
                showAllocDialog      = false
            }
        }
    }

    // ─── Diálogo especial do MEIO-ELFO ───────────────────────────────────────────
    if (showMeioElfoDialog && pendingMeioElfoKey != null) {
        val key = pendingMeioElfoKey!!
        AlertDialog(
            onDismissRequest = { showMeioElfoDialog = false },
            title   = { Text("Meio-Elfo: escolha sua herança") },
            text    = { Text("Selecione qual benefício você gostaria de herdar:") },
            confirmButton = {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            // herda ADAPTÁVEL (humano) → +1 ponto de vantagem
                            state.aplicarAncestralidade(key)
                            state.pontosVantagem += 1
                            showMeioElfoDialog = false
                        },
                        modifier = Modifier.defaultMinSize(minWidth = 100.dp)
                    ) {
                        Text("ADAPTÁVEL")
                    }
                    Button(
                        onClick = {
                            // herda AGILIDADE d6 (como elfos)
                            state.aplicarAncestralidade(key)
                            state.valoresAtributos["AGILIDADE"]!!.intValue = 6
                            (racialAttrMinMap as MutableMap)[key] =
                                (racialAttrMinMap[key] ?: emptyMap()) + ("AGILIDADE" to 6)
                            state.recalcularPontosAtributo()
                            showMeioElfoDialog = false
                        },
                        modifier = Modifier.defaultMinSize(minWidth = 100.dp)
                    ) {
                        Text("AGILIDADE")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showMeioElfoDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Preview(showBackground = true)
@Composable
fun PreviewApp() {
    val state = remember { CriadorState() }
    UnifiedScreen(
        state = state,
        onOpenVantagensDetail = { _ -> },
        onOpenPericiasDetail = {},
        onOpenComplicacoesDetail = {},
        onOpenAtributosDetail = {},
        onOpenListaAncestralidadesDetail = {},
        onOpenListaCompletaEquipamento = {},
        onOpenPoderesDetail = {},
        onOpenSuperPoderesDetail = { _ -> },
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
        equipamentoCategorias = emptyList(),
        superequipCategorias = emptyList(),
        listaSuperPoderes = emptyList()
    )
}
