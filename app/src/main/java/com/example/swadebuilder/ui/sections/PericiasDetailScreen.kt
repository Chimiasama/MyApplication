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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.R
import com.example.swadebuilder.listaPericias
import com.example.swadebuilder.model.loadPericiasDescriptions
import com.example.swadebuilder.util.semAcentos

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PericiasDetailScreen(
    state: CriadorState,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val descricoes by remember {
        mutableStateOf(loadPericiasDescriptions(context, R.raw.pericias))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lista Completa de Perícias") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text(
                            "Voltar",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        content = { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                items(listaPericias) { per ->
                    val rawName = per.nome.removePrefix("*").trim()
                    val key = "$rawName (${per.atributo})".uppercase().semAcentos()
                    val desc = descricoes[key] ?: "Descrição indisponível."

                    val currentRaw = state.rawTotal(per)
                    val attrKey = state.atributoBaseParaPericia(per)
                    val atrRaw = state.valoresAtributos[attrKey]!!.intValue
                    val capRaw = state.periciaCapRaw(per)

                    val nextRaw = if (currentRaw == 0 && per.basica) 4 else currentRaw + 2
                    val costNormal = if (nextRaw <= atrRaw) 1 else 2

                    val minimoBasico = state.minPericiaPorVantagem[per] ?: 0
                    val minimoOpcional = state.vantagensSelecionadas
                        .flatMap { vant ->
                            vant.requisitos.periciaMinOpcional
                                .filterKeys { it.equals(per.nome, ignoreCase = true) }
                                .values
                        }
                        .maxOrNull() ?: 0
                    val minimoTotal = maxOf(minimoBasico, minimoOpcional)
                    val needsMin = (minimoTotal > 0 && currentRaw in 1 until minimoTotal)

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
                        needsMin -> MaterialTheme.colorScheme.errorContainer
                        currentRaw > 0 || per.basica -> MaterialTheme.colorScheme.tertiaryContainer
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
                                    needsMin -> MaterialTheme.colorScheme.error
                                    podeAumentar -> MaterialTheme.colorScheme.primary
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
    )
}
