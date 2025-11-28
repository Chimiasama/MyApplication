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
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.model.CriadorViewModel
import com.example.swadebuilder.model.EquipamentoCategoria
import com.example.swadebuilder.ui.components.SectionCard
import com.example.swadebuilder.ui.sections.AncestralidadesSection
import com.example.swadebuilder.ui.sections.AtributosContent
import com.example.swadebuilder.ui.sections.ComplicacoesSection
import com.example.swadebuilder.ui.sections.EquipamentoSection
import com.example.swadebuilder.ui.sections.PericiasContent
import com.example.swadebuilder.ui.sections.PoderesSection
import com.example.swadebuilder.ui.sections.SummaryContent
import com.example.swadebuilder.ui.sections.SuperPoderesContent
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

}
