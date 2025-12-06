package com.example.swadebuilder.ui.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.criacaoBasicaCongelada
import com.example.swadebuilder.model.AtributoJson
import com.example.swadebuilder.model.AtributoList
import com.example.swadebuilder.model.loadJsonAsset
import com.example.swadebuilder.toDiceString
import com.example.swadebuilder.ui.components.AtributoListItem
import com.example.swadebuilder.ui.components.PbWalletBanner
import com.example.swadebuilder.ui.components.SectionHeader

@Composable
fun AtributosContent(
    state: CriadorState
) {
    val locked = state.criacaoBasicaCongelada && !state.attributeAdvancementInProgress
    val context = LocalContext.current

    val atributosData = remember {
        context.loadJsonAsset<AtributoList>("atributos.json")
    }
    val listaAtributos = remember { atributosData.atributos }

    val pcTotal  = state.pontosComplicacao
    val pcGastos = state.pontosComplicacaoGastos
    val pcLivres = (pcTotal - pcGastos).coerceAtLeast(0)
    val paUsados = state.cpPaStack.size

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        SectionHeader(
            onHelpClick = null,
            centerText = "Pontos de Atributo: ${state.pontosAtributo}",
            onListaCompletaClick = null,
            listaCompletaText = "Lista Completa"
        )

        Spacer(Modifier.height(4.dp))

        PbWalletBanner(
            pcTotal = pcTotal,
            pcLivres = pcLivres,
            spendLabel = "Usar PB em Atributos",
            refundLabel = "Desfazer uso de PB",
            spendEnabled = !locked && pcLivres >= 2,
            refundEnabled = !locked && paUsados > 0,
            onSpend = { state.gastarPcParaAtributo() },
            onRefund = {
                state.cpPaStack.removeAt(state.cpPaStack.lastIndex)
                state.pontosComplicacaoGastos =
                    (state.pontosComplicacaoGastos - 2).coerceAtLeast(0)
                state.recalcularPontosAtributo()
            }
        )

        Spacer(Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(listaAtributos) { atributo ->
                val nome = atributo.nome.uppercase()
                val baseRaw = state.valoresAtributos[nome]!!.intValue

                val minReq = maxOf(
                    state.atributoMinRaw(nome),
                    state.minAttrPorVantagem[nome] ?: 4
                )

                val maxRaw  = state.atributoMaxRaw(nome)
                val stack   = state.paCostStackPorAtributo.getValue(nome)

                val efetivoRaw = state.atributoRawComSupers(nome)

                val nextRaw = if (baseRaw < 12) baseRaw + 2 else baseRaw + 1
                val prevRaw = if (baseRaw <= 12) baseRaw - 2 else baseRaw - 1

                val canIncrease = !locked && state.pontosAtributo > 0 && (nextRaw <= maxRaw)

                val canReduce = run {
                    val baseCanReduce = !locked && stack.isNotEmpty() && (prevRaw >= minReq)
                    if (!baseCanReduce) {
                        false
                    } else {
                        if (state.attributeAdvancementInProgress) {
                            val beforeSize = state.attributeStacksBeforeAdvancement?.get(nome) ?: 0
                            stack.size > beforeSize
                        } else {
                            true
                        }
                    }
                }

                AtributoListItem(
                    atributo = atributo,
                    diceValue = efetivoRaw.toDiceString(),
                    canIncrease = canIncrease,
                    canDecrease = canReduce,
                    onIncrease = {
                        if (nextRaw > maxRaw || state.pontosAtributo <= 0) return@AtributoListItem
                        stack.add(1)
                        state.valoresAtributos[nome]!!.intValue = nextRaw
                        state.pontosAtributo--
                        state.recalcularPontosAtributo()
                    },
                    onDecrease = {
                        if (prevRaw < minReq) return@AtributoListItem
                        stack.removeAt(stack.lastIndex)
                        state.valoresAtributos[nome]!!.intValue = prevRaw
                        state.pontosAtributo++
                        state.recalcularPontosAtributo()
                    }
                )
            }
        }
    }
}
