package com.example.swadebuilder.ui.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.SectionHeader
import com.example.swadebuilder.listaAtributos
import com.example.swadebuilder.mapaAtributosDisplay
import com.example.swadebuilder.toDiceString

@OptIn(ExperimentalTextApi::class)
@Composable
fun AtributosContent(
    state: CriadorState,
    onOpenAtributosDetail: () -> Unit
) {
    val locked = (state.progresso > 0) || state.faseSupersAtiva || state.emProgresso
    var showHelp by rememberSaveable { mutableStateOf(false) }
    // sua cor de pergaminho
    val pergaminho = Color(0xFFF2E3C6)
    val showLista = booleanResource(com.example.swadebuilder.R.bool.show_lista_completa)

    // ===== LARGURA DINÂMICA PARA O VALOR (evitar quebrar "d12+4" e esmagar botões) =====
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val measureStyle = MaterialTheme.typography.bodyLarge  // capturado fora do remember

    // mede a maior string entre todos os atributos (com supers aplicados)
    val valorColWidthDp = remember(state.superAtributoIncs, measureStyle) {
        val samples = listaAtributos.map { nome ->
            state.atributoRawComSupers(nome).toDiceString()
        }
        val maxPx = samples.maxOf { s ->
            textMeasurer.measure(text = s, style = measureStyle).size.width
        }
        with(density) { (maxPx + 12).toDp() } // folga de 12dp para respirar
    }
    // ===============================================================================

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(pergaminho, shape = RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        // cabeçalho
        SectionHeader(
            onHelpClick           = { showHelp = true },
            centerText            = "Pontos restantes: ${state.pontosAtributo}",
            onListaCompletaClick  = if (showLista) ({ onOpenAtributosDetail() }) else null,
            listaCompletaText     = "Lista Completa"
        )

        if (showHelp) {
            AlertDialog(
                onDismissRequest = { showHelp = false },
                title            = { Text("Como funciona") },
                text             = { Text("Cada avanço (d4→d6 etc.) custa 1 Ponto de Atributos (PA). Você pode aumentar até o limite de sua ancestralidade.") },
                confirmButton    = {
                    TextButton(onClick = { showHelp = false }) {
                        Text("OK")
                    }
                }
            )
        }

        Spacer(Modifier.height(8.dp))

        // linhas dos atributos
        listaAtributos.forEach { nome ->
            val baseRaw = state.valoresAtributos[nome]!!.intValue
            val minReq  = state.minAttrPorVantagem[nome] ?: 4
            val maxRaw  = state.atributoMaxRaw(nome)
            val stack   = state.paCostStackPorAtributo.getValue(nome)

            // valor efetivo com supers (para exibição)
            val efetivoRaw = state.atributoRawComSupers(nome)

            val canReduce   = !locked && stack.isNotEmpty() && (baseRaw - 2 >= minReq)
            val canIncrease = !locked && state.pontosAtributo > 0 && (baseRaw + 2 <= maxRaw)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = mapaAtributosDisplay[nome] ?: nome,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                IconButton(
                    onClick = {
                        // reduzir
                        val novo = baseRaw - 2
                        if (novo < minReq) return@IconButton
                        stack.removeAt(stack.lastIndex)
                        state.valoresAtributos[nome]!!.intValue = novo
                        state.pontosAtributo++
                        state.recalcularPontosAtributo()
                    },
                    enabled = canReduce,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(4.dp)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.fillMaxSize())
                }

                Text(
                    // mostra o valor com supers (antes era baseRaw.toDiceString())
                    text = efetivoRaw.toDiceString(),
                    modifier = Modifier.width(valorColWidthDp),
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    textAlign = TextAlign.Center
                )

                IconButton(
                    onClick = {
                        // aumentar
                        stack.add(1)
                        state.valoresAtributos[nome]!!.intValue = baseRaw + 2
                        state.pontosAtributo--
                        state.recalcularPontosAtributo()
                    },
                    enabled = canIncrease,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(4.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.fillMaxSize())
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}
