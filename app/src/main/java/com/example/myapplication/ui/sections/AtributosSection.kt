package com.example.myapplication.ui.sections

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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.myapplication.CriadorState
import com.example.myapplication.SectionHeader
import com.example.myapplication.listaAtributos
import com.example.myapplication.mapaAtributosDisplay
import com.example.myapplication.toDiceString

@Composable
fun AtributosContent(
    state: CriadorState,
    onOpenAtributosDetail: () -> Unit
) {
    val locked = state.progresso > 0
    var showHelp by rememberSaveable { mutableStateOf(false) }
    // sua cor de pergaminho
    val pergaminho = Color(0xFFF2E3C6)

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
            onListaCompletaClick  = { onOpenAtributosDetail() }
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
                    text = baseRaw.toDiceString(),
                    modifier = Modifier.width(40.dp),
                    style = MaterialTheme.typography.bodyLarge,
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
