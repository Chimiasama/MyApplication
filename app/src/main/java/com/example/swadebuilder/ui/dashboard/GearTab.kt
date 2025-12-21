package com.example.swadebuilder.ui.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.model.EquipamentoCategoria
import com.example.swadebuilder.ui.sections.EquipamentoContent
import com.example.swadebuilder.util.keyify
import kotlinx.serialization.json.JsonPrimitive

@Composable
fun GearTab(
    state: CriadorState,
    equipamentoCategorias: List<EquipamentoCategoria>,
    superequipCategorias: List<EquipamentoCategoria>,
    onUserFeedback: () -> Unit
) {
    val hasMusculoso = state.vantagensSelecionadas.any { it.nome.keyify() == "MUSCULOSO" }
    val hasSoldado = state.vantagensSelecionadas.any { it.nome.keyify() == "SOLDADO" }
    val isPersonagemRobotico = state.isPersonagemRobotico()
    val tensaoLimite = if (isPersonagemRobotico) {
        state.limiteModsRoboticos()
    } else {
        state.valoresAtributos["VIGOR"]?.intValue ?: 4
    }

    Box(Modifier.fillMaxSize().padding(8.dp)) {
        EquipamentoContent(
            dinheiro = state.dinheiro,
            usaRiqueza = state.usaRiqueza,
            dadoRiqueza = state.dadoRiqueza,
            pcTotal = state.pontosComplicacao,
            pcLivres = (state.pontosComplicacao - state.pontosComplicacaoGastos).coerceAtLeast(0),
            recursosPcUsados = state.cpRecursosStack.size,
            emProgresso = state.emProgresso,
            modoProgressaoAtivo = state.modoProgressaoAtivo,
            onUsarPontosBonusEmRecursos = {
                if (state.usaRiqueza) return@EquipamentoContent
                val pcLivresLocal =
                    (state.pontosComplicacao - state.pontosComplicacaoGastos).coerceAtLeast(0)
                if (pcLivresLocal > 0 && state.cpRecursosStack.isEmpty()) {
                    state.cpRecursosStack.add(Unit)
                    state.pontosComplicacaoGastos += 1
                    state.dinheiro += 500
                }
            },
            onDesfazerPontosBonusEmRecursos = {
                if (state.usaRiqueza) return@EquipamentoContent
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
                if (state.usaRiqueza || custo <= state.dinheiro) {
                    state.equipamentosComprados.add(equipamento)
                    if (!state.usaRiqueza) {
                        state.dinheiro -= custo
                    }
                }
            },
            equipamentosComprados = state.equipamentosComprados,
            onRemoveEquipamentoClick = { equipamento ->
                val custo = (equipamento.custo as? JsonPrimitive)
                    ?.content?.toIntOrNull() ?: 0
                state.equipamentosComprados.remove(equipamento)
                if (!state.usaRiqueza) {
                    state.dinheiro += custo
                }
            },
            categorias = equipamentoCategorias,
            superequipCategorias = if (state.modoSupers) superequipCategorias else emptyList(),
            tensaoTotal = state.totalTensaoEquipamentos(),
            tensaoLimite = tensaoLimite,
            isPersonagemRobotico = isPersonagemRobotico,
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
            compendioFantasiaAtivo = state.compendioFantasiaAtivo,
            compendioHorrorAtivo = state.compendioHorrorAtivo,
            compendioSciFiAtivo = state.compendioSciFiAtivo,
            compendioBuscatrilhaAtivo = state.compendioBuscatrilhaAtivo,
            compendioDeadlandsAtivo = state.compendioDeadlandsAtivo,
            compendioArteDaGuerraAtivo = state.compendioArteDaGuerraAtivo,
            compendioCidadeSolVaporAtivo = state.compendioCidadeSolVaporAtivo,
            compendioWiseguysAtivo = state.compendioWiseguysAtivo,
            compendioCrystalHeartAtivo = state.compendioCrystalHeartAtivo,
            modoOficialAtivo = state.modoOficialAtivo,
            onUserFeedback = onUserFeedback
        )
    }
}
