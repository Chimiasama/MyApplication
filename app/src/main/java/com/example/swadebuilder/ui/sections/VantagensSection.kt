package com.example.swadebuilder.ui.sections

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.CriadorViewModel
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.ui.components.CollapsibleSection
import com.example.swadebuilder.ui.components.SectionCard
import com.example.swadebuilder.ui.components.SectionHeader
import com.example.swadebuilder.ui.components.VantagemListItem

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun VantagensSection(
    state: CriadorState,
    viewModel: CriadorViewModel,
    expanded: Boolean,
    onToggle: () -> Unit,
    compendioFantasiaAtivo: Boolean,
    compendioHorrorAtivo: Boolean,
    compendioSciFiAtivo: Boolean,
    compendioBuscatrilhaAtivo: Boolean,
    compendioDeadlandsAtivo: Boolean,
    compendioCrystalHeartAtivo: Boolean,
    compendioArteDaGuerraAtivo: Boolean,
    compendioCidadeSolVaporAtivo: Boolean,
    compendioWiseguysAtivo: Boolean
) {
    val context = LocalContext.current
    // FIX: Use top-level property or GlobalData if accessible
    val listaVantagens = com.example.swadebuilder.model.GlobalData.listaVantagens

    // Filter Logic
    val listaVantagensAtivas: List<Vantagem> = remember(listaVantagens, state.modoSupers, compendioFantasiaAtivo, compendioHorrorAtivo, compendioBuscatrilhaAtivo, compendioDeadlandsAtivo, compendioCrystalHeartAtivo, compendioArteDaGuerraAtivo, compendioCidadeSolVaporAtivo, compendioWiseguysAtivo) {
        listaVantagens.filter { v ->
            val origemNorm = v.origem.uppercase()
            val isBasico = origemNorm == "BASICO"
            val isSuper = origemNorm == "SUPER"
            val isFantasia = origemNorm == "FANTASIA"
            val isHorror = origemNorm == "HORROR"
            val isBuscatrilha = origemNorm == "FANTASIA_BUSCATRILHA"
            val isDeadlands = origemNorm == "DEADLANDS"
            val isAdg = origemNorm == "ARTE_DA_GUERRA"
            val isCidadeSolVapor = origemNorm == "CIDADE_SOL_VAPOR"
            val isWiseguys = origemNorm == "WISEGUYS"

            if (v.id == "superpoderes") return@filter false
            if (v.id == "aa_agente_syn") return@filter false

            isBasico ||
            (isAdg && state.compendioArteDaGuerraAtivo) ||
            (isSuper && state.modoSupers) ||
            (isFantasia && state.compendioFantasiaAtivo) ||
            (isHorror && state.compendioHorrorAtivo) ||
            (isBuscatrilha && state.compendioBuscatrilhaAtivo) ||
            (isDeadlands && state.compendioDeadlandsAtivo) ||
            (isCidadeSolVapor && state.compendioCidadeSolVaporAtivo) ||
            (isWiseguys && state.compendioWiseguysAtivo)
        }
    }

    SectionCard(
        title = "Vantagens",
        expanded = expanded,
        onToggle = onToggle,
        icon = Icons.Default.AutoAwesome
    ) {
        val pts = state.pontosVantagem
        val label = if (pts == 0) "Nenhum ponto disponível" else "$pts ponto(s) disponível(is)"

        SectionHeader(
            centerText = label,
            onHelpClick = null
        )

        Spacer(Modifier.height(8.dp))

        // Group by category
        val grouped = listaVantagensAtivas.groupBy { it.categoria }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Categoria.entries.forEach { cat ->
                val items = grouped[cat] ?: emptyList()
                if (items.isNotEmpty()) {
                    val isCatExpanded = state.categoriasVantagensExpandidas[cat] ?: false
                    CollapsibleSection(
                        title = cat.name.lowercase().replaceFirstChar { it.uppercase() },
                        expanded = isCatExpanded,
                        onToggle = { state.categoriasVantagensExpandidas[cat] = !isCatExpanded }
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            items.forEach { vant ->
                                VantagemListItem(
                                    vantagem = vant,
                                    state = state,
                                    canSelect = state.podeSelecionar(vant),
                                    onSelect = { v ->
                                        if (state.podeSelecionar(v)) {
                                            if (state.modoProgressaoAtivo && state.mostrandoVantagensProgresso) {
                                                viewModel.selectAdvantageForAdvancement(v)
                                            } else if (!state.baseCreationComplete()) {
                                                if (v.nome.contains("Pontos de Poder", true)) {
                                                    state.comprarPontoDePoder(v)
                                                } else {
                                                    if (state.pontosVantagem > 0) {
                                                        state.pontosVantagem--
                                                        state.applyVantagemDinheiro(v)
                                                        state.vantagensSelecionadas.add(v)
                                                        state.rebuildAllPericiaStacks()
                                                    } else {
                                                        if (state.gastarPcParaVantagem()) {
                                                            state.pontosVantagem--
                                                            state.applyVantagemDinheiro(v)
                                                            state.vantagensSelecionadas.add(v)
                                                            state.rebuildAllPericiaStacks()
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    onRemove = { v ->
                                        if (!state.modoProgressaoAtivo) {
                                            if (v.nome.contains("Pontos de Poder", true)) {
                                                state.removerPontosDePoder(v)
                                            } else {
                                                state.removeVantagemDinheiro(v)
                                                state.vantagensSelecionadas.remove(v)
                                            }
                                            state.pontosVantagem++
                                            state.rebuildAllPericiaStacks()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
