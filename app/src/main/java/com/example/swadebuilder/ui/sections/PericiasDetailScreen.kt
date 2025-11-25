package com.example.swadebuilder.ui.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.listaPericias
import com.example.swadebuilder.util.semAcentos

@Composable
fun PericiasDetailScreen(
    state: CriadorState,
    onBack: () -> Unit,
    descriptions: Map<String, String>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        stickyHeader {
            Surface(color = Color.Transparent) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onBack() }
                        .padding(vertical = 12.dp, horizontal = 16.dp)
                ) {
                    Text(
                        text = "Voltar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                HorizontalDivider()
            }
        }

        items(listaPericias) { per ->
            val rawName = per.nome.removePrefix("*").trim()
            val key = "$rawName (${per.atributo})".uppercase().semAcentos()
            val desc = descriptions[key] ?: "Descrição indisponível."

            val currentRaw = state.rawTotal(per)
            val attrKey    = state.atributoBaseParaPericia(per)
            val atrRaw     = state.valoresAtributos[attrKey]!!.intValue
            val capRaw     = state.periciaCapRaw(per)

            val nextRaw    = if (currentRaw == 0 && per.basica) 4 else currentRaw + 2
            val costNormal = if (nextRaw <= atrRaw) 1 else 2

            val minimoBasico  = state.minPericiaPorVantagem[per] ?: 0
            val minimoOpcional = state.vantagensSelecionadas
                .flatMap { vant ->
                    vant.requisitos.periciaMinOpcional
                        .filterKeys { it.equals(per.nome, ignoreCase = true) }
                        .values
                }
                .maxOrNull() ?: 0
            val minimoTotal = maxOf(minimoBasico, minimoOpcional)
            val needsMin    = (minimoTotal > 0 && currentRaw < minimoTotal)

            val podeAumentar = state.pontosPericia >= costNormal && nextRaw <= capRaw

            val status = buildString {
                append(
                    when (currentRaw) {
                        0 if per.basica -> "Atual: d4 (básica)"
                        0 -> "Atual: —"
                        else -> "Atual: d${currentRaw}"
                    }
                )
                append(" • Próximo: ")
                append(if (nextRaw > capRaw) "— (teto)" else if (currentRaw == 0 && !per.basica) "d4" else "d$nextRaw")
                append(" • Custo: ")
                append(if (nextRaw > capRaw) "—" else "$costNormal SP")
                append(" • Cap: d$capRaw")
                if (minimoTotal > 0) {
                    append(" • Mín.: d$minimoTotal")
                    if (needsMin) append(" (abaixo)")
                }
            }

            val bg = when {
                needsMin -> Color(0x11FF0000)
                currentRaw > 0 || per.basica -> Color(0x11007AFF)
                else -> Color.Transparent
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .background(bg)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (per.basica) {
                        Text("✯", color = MaterialTheme.colorScheme.error, fontSize = 20.sp)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        text = "${per.nome} (${per.atributo})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        when {
                            needsMin -> "abaixo do mínimo"
                            podeAumentar -> "pode aumentar"
                            else -> ""
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = when {
                            needsMin -> Color(0xFFB00020)
                            podeAumentar -> Color(0xFF2E7D32)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }

                Spacer(Modifier.height(6.dp))
                Text(status, style = MaterialTheme.typography.labelMedium)

                Spacer(Modifier.height(8.dp))
                Text(desc, style = MaterialTheme.typography.bodyMedium, fontSize = 14.sp)

                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
            }
        }
    }
}
