package com.example.swadebuilder.ui.sections

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.model.GlobalData
import com.example.swadebuilder.ui.components.ComplicacaoListItem
import com.example.swadebuilder.ui.components.SectionCard
import com.example.swadebuilder.ui.components.SectionHeader

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun ComplicacoesSection(
    state: CriadorState,
    expanded: Boolean,
    onToggle: () -> Unit,
    readOnly: Boolean
) {
    // FIX: Accessing directly from MainActivity property or passed parameter is better if GlobalData is gone.
    // Assuming GlobalData singleton was removed or not visible.
    // But `VantagensSection` also uses `GlobalData`.
    // I need to check if `GlobalData` exists.
    // If not, I should use `com.example.swadebuilder.listaComplicacoes`.
    val listaComplicacoes = com.example.swadebuilder.listaComplicacoes

    // Filter Logic
    val listaFiltrada = remember(listaComplicacoes, state.modoSupers, state.compendioFantasiaAtivo, state.compendioHorrorAtivo, state.compendioBuscatrilhaAtivo, state.compendioDeadlandsAtivo, state.compendioCrystalHeartAtivo, state.compendioArteDaGuerraAtivo, state.compendioCidadeSolVaporAtivo, state.compendioWiseguysAtivo) {
        val origins = mutableSetOf("BASICO")
        if (state.compendioFantasiaAtivo) origins.add("FANTASIA")
        if (state.compendioHorrorAtivo) origins.add("HORROR")
        if (state.compendioBuscatrilhaAtivo) origins.add("FANTASIA_BUSCATRILHA") // Updated origin
        if (state.compendioDeadlandsAtivo) origins.add("DEADLANDS")
        if (state.compendioArteDaGuerraAtivo) origins.add("ARTE_DA_GUERRA")
        if (state.compendioCidadeSolVaporAtivo) origins.add("CIDADE_SOL_VAPOR")
        if (state.compendioWiseguysAtivo) origins.add("WISEGUYS")
        if (state.modoSupers) origins.add("SUPER")

        listaComplicacoes.filter {
            (it.origem?.uppercase() ?: "BASICO") in origins
        }
    }

    SectionCard(
        title = "Complicações",
        expanded = expanded,
        onToggle = onToggle,
        icon = Icons.Default.Warning
    ) {
        val pts = state.pontosComplicacao
        val gastos = state.pontosComplicacaoGastos

        SectionHeader(
            centerText = "Pontos obtidos: $pts (Gastos: $gastos)",
            onHelpClick = null
        )

        Spacer(Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {

            LazyColumn(
                modifier = Modifier.heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(listaFiltrada) { comp ->
                    val nivelSelecionado = state.complicacoesSelecionadas[comp]
                    ComplicacaoListItem(
                        complicacao = comp,
                        nivelSelecionado = nivelSelecionado,
                        readOnly = readOnly,
                        onSelect = { nivel ->
                            if (!readOnly) {
                                state.complicacoesSelecionadas[comp] = nivel
                                state.recalcularPontosAtributo()
                                state.rebuildAllPericiaStacks()
                            }
                        },
                        onRemove = {
                            if (!readOnly) {
                                state.complicacoesSelecionadas.remove(comp)
                                state.recalcularPontosAtributo()
                                state.rebuildAllPericiaStacks()
                            }
                        }
                    )
                }
            }
        }
    }
}
