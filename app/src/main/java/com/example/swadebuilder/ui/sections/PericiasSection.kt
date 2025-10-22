package com.example.swadebuilder.ui.sections

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.SectionHeader
import com.example.swadebuilder.listaPericias
import com.example.swadebuilder.mapaAtributosDisplay
import com.example.swadebuilder.toDiceString
import kotlin.math.max


@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun PericiasContent(
    state: CriadorState,
    onOpenPericiasDetail: () -> Unit
) {
    val locked = state.progresso > 0
    var showHelp by rememberSaveable { mutableStateOf(false) }

    // Detecta se “Idoso” está ativo pelo bônus de SP
    val idosoActive = state.idosoBonusSp > 0

    // Soma de SP gastos em perícias cujo atributo-base seja “ASTUCIA”
    val astuciaSpent = state.spCostStackPorPericia
        .filterKeys { per -> per.atributo == "ASTUCIA" }
        .values
        .sumOf { costs -> costs.sum() }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 400.dp)
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        stickyHeader {
            val pergaminho = Color(0xFFF2E3C6)
            val showLista = booleanResource(com.example.swadebuilder.R.bool.show_lista_completa)

            Surface(tonalElevation = 0.dp, color = pergaminho) {
                SectionHeader(
                    onHelpClick          = { showHelp = true },
                    centerText           = "Pontos restantes: ${state.pontosPericia}",
                    onListaCompletaClick = if (showLista) ({ onOpenPericiasDetail() }) else null,
                    listaCompletaText    = "Lista Completa"
                )

            }
            if (showHelp) {
                AlertDialog(
                    onDismissRequest = { showHelp = false },
                    title            = { Text("Como funciona") },
                    text             = { Text("Cada avanço de perícia custa 1 SP se abaixo do atributo relacionado ou 2 SP se acima.") },
                    confirmButton    = {
                        TextButton(onClick = { showHelp = false }) { Text("OK") }
                    }
                )
            }
        }

        items(listaPericias) { per ->
            val currentRaw = state.rawTotal(per)
            val attrKey    = state.atributoBaseParaPericia(per)
            val atrRaw     = state.valoresAtributos[attrKey]!!.intValue
            val capRaw     = state.periciaCapRaw(per)
            val nextRaw    = if (currentRaw == 0 && per.basica) 4 else currentRaw + 2
            val costNormal = if (nextRaw <= atrRaw) 1 else 2

            val compStack = state.compCostStackPorPericia.getValue(per)
            val spStack   = state.spCostStackPorPericia.getValue(per)

            // Mínimo imposto por vantagens
            val minimoBasico: Int = state.minPericiaPorVantagem[per] ?: 0

            // Opcional de requisitos
            val opcionalList: List<Int> = state.vantagensSelecionadas.flatMap { vant ->
                val mapaOpc = vant.requisitos.periciaMinOpcional ?: emptyMap()
                mapaOpc.entries
                    .filter { it.key.equals(per.nome, ignoreCase = true) }
                    .map { it.value }
            }
            val minimoOpcional: Int = opcionalList.maxOrNull() ?: 0

            val minimoTotal = max(minimoBasico, minimoOpcional)

            val canDecrease = !locked &&
                    (compStack.isNotEmpty() || spStack.any { it > 0 }) &&
                    (currentRaw - 2 >= minimoTotal)

            val canIncrease = !locked &&
                    state.pontosPericia >= costNormal &&
                    nextRaw <= capRaw &&
                    (if (idosoActive && astuciaSpent < 5)
                        per.atributo == "ASTUCIA"
                    else
                        true)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val defaultSize = MaterialTheme.typography.bodyLarge.fontSize

                Text(
                    text = buildAnnotatedString {
                        if (per.basica) {
                            withStyle(SpanStyle(color = Color.Red, fontWeight = FontWeight.Bold)) {
                                append("✯ ${per.nome}")
                            }
                        } else {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(per.nome)
                            }
                        }
                        withStyle(SpanStyle(fontSize = defaultSize / 2)) {
                            val displayAtr = mapaAtributosDisplay[attrKey] ?: attrKey
                            append(" ($displayAtr)")
                        }
                    },
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                IconButton(
                    onClick = { state.decreasePericia(per) },
                    enabled = canDecrease,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(4.dp)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.fillMaxSize())
                }

                Text(
                    text = when {
                        currentRaw == 0 && per.basica -> "d4"
                        currentRaw == 0               -> "-"
                        else                          -> currentRaw.toDiceString()
                    },
                    modifier = Modifier.width(40.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )

                IconButton(
                    onClick = {
                        state.baseIncsPorPericia[per] = state.baseIncsPorPericia.getValue(per) + 1
                        state.spCostStackPorPericia.getValue(per).add(costNormal)
                    },
                    enabled = canIncrease,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(4.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.fillMaxSize())
                }

                Spacer(Modifier.width(4.dp))
            }
        }
    }
}
