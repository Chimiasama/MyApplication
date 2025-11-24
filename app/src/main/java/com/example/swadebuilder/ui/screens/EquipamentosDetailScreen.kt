package com.example.swadebuilder.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swadebuilder.R
import com.example.swadebuilder.model.EquipamentoCategoria
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

@Composable
fun EquipamentosDetailScreen(
    categorias: List<EquipamentoCategoria>,
    onBack: () -> Unit
) {
    fun JsonElement?.asText(): String? = when (this) {
        is JsonPrimitive -> this.content
        else -> this?.toString()
    }?.takeIf { it.isNotBlank() }

    val mapa =
        remember(categorias) {
            categorias
                .sortedWith(
                    compareBy<EquipamentoCategoria> { it.tipo.lowercase() }
                        .thenBy { it.subtipo.lowercase() }
                        .thenBy { (it.subsubtipo ?: "").lowercase() }
                )
                .groupBy { cat ->
                    val tipoOriginal = cat.tipo
                    val isSuper = cat.origem.equals("SUPER", ignoreCase = true)

                    // Normaliza o rótulo exibido para o grupo
                    val labelTipo = if (isSuper) {
                        if (tipoOriginal.contains("Equipamento Supers", ignoreCase = true)) {
                            "Superequip - Veículos"     // ← fica igual ao padrão, mas com nome claro
                        } else {
                            "Superequip - $tipoOriginal"
                        }
                    } else {
                        tipoOriginal
                    }

                    labelTipo
                }
                .mapValues { (_, porTipo) ->
                    porTipo.groupBy { it.subtipo }.mapValues { (_, porSubtipo) ->
                        porSubtipo.groupBy { it.subsubtipo ?: "" }.mapValues { (_, listaFinal) ->
                            listaFinal.flatMap { it.itens }.sortedBy { it.nome }
                        }
                    }
                }
        }

    // --- ESTADOS DE EXPANSÃO (mantidos) ---
    val expTipo  = remember(mapa) { mapa.keys.associateWith { mutableStateOf(false) } }
    val expSub   = remember(mapa) { mapa.mapValues { (_, sub) -> sub.keys.associateWith { mutableStateOf(false) } } }
    val expSub2  = remember(mapa) {
        mapa.mapValues { (_, sub) ->
            sub.mapValues { (_, sub2) -> sub2.keys.associateWith { mutableStateOf(false) } }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header "Voltar"
        stickyHeader {
            Surface(color = MaterialTheme.colorScheme.surface) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onBack)
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.top_bar_back), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                HorizontalDivider()
            }
        }

        // TIPOS
        mapa.toSortedMap(compareBy { it.lowercase() }).forEach { (tipo, subMapa) ->
            item {
                val et = expTipo.getValue(tipo)
                Column {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { et.value = !et.value }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(tipo, style = MaterialTheme.typography.titleMedium)
                        Icon(
                            imageVector = if (et.value) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (et.value) "Fechar" else "Abrir"
                        )
                    }

                    if (et.value) {
                        // SUBTIPOS
                        subMapa.toSortedMap(compareBy { it.lowercase() }).forEach { (subtipo, sub2Mapa) ->
                            val es = expSub.getValue(tipo).getValue(subtipo)
                            Column(Modifier.padding(start = 8.dp, bottom = 8.dp)) {
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .clickable { es.value = !es.value }
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(subtipo, fontWeight = FontWeight.Bold)
                                    Icon(
                                        imageVector = if (es.value) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = if (es.value) "Fechar" else "Abrir"
                                    )
                                }

                                if (es.value) {
                                    // SUBSUBTIPOS
                                    sub2Mapa.toSortedMap(compareBy { it.lowercase() }).forEach { (subsub, itens) ->
                                        val ess = expSub2.getValue(tipo).getValue(subtipo).getValue(subsub)
                                        Column(Modifier.padding(start = 8.dp, bottom = 4.dp)) {
                                            if (subsub.isNotBlank()) {
                                                Row(
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .clickable { ess.value = !ess.value }
                                                        .padding(vertical = 4.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(subsub)
                                                    Icon(
                                                        imageVector = if (ess.value) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                        contentDescription = if (ess.value) "Fechar" else "Abrir"
                                                    )
                                                }
                                            } else {
                                                ess.value = true // sem subsubtipo: já aberto
                                            }

                                            if (ess.value) {
                                                // LISTA DE ITENS (com DETALHES)
                                                Column(
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .padding(start = 8.dp, bottom = 8.dp)
                                                ) {
                                                    itens.forEach { eq ->
                                                        Column(
                                                            Modifier
                                                                .fillMaxWidth()
                                                                .padding(vertical = 6.dp)
                                                        ) {
                                                            Row(
                                                                Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.SpaceBetween
                                                            ) {
                                                                Text(eq.nome, style = MaterialTheme.typography.bodyLarge)
                                                                eq.custo.asText()?.let { Text(it) }
                                                            }

                                                            // ===== PADRONIZAÇÃO DAS LINHAS =====
                                                            // 1) Linha de "arma" (se aplicável)
                                                            val linhaArma = listOfNotNull(
                                                                eq.dano.asText()?.let { "Dano: $it" },
                                                                eq.pa.asText()?.let { "PA: $it" },
                                                                eq.cdt.asText()?.let { "CdT: $it" },
                                                                eq.distancia.asText()?.let { "Distância: $it" },
                                                                eq.tiros.asText()?.let { "Tiros: $it" },
                                                            ).joinToString("  •  ").takeIf { it.isNotBlank() }

                                                            // 2) Linha geral (peso/força/armadura/aparar)
                                                            val linhaGeral = listOfNotNull(
                                                                eq.peso.asText()?.let { "Peso: $it" },
                                                                eq.forcaMin.asText()?.let { "Força mín.: $it" },
                                                                eq.armadura.asText()?.let { "Armadura: $it" },
                                                                eq.aparar.asText()?.let { "Aparar: $it" },
                                                            ).joinToString("  •  ").takeIf { it.isNotBlank() }

                                                            // 3) Linha veículo — mesma lógica/ordem usada no superquip de veículos
                                                            val linhaVeiculo = listOfNotNull(
                                                                eq.velMaxima.asText()?.let { "Vel. máx.: $it" },
                                                                eq.manobrabilidade.asText()?.let { "Manobrabilidade: $it" },
                                                                eq.tamanho.asText()?.let { "Tamanho: $it" },
                                                                eq.resistencia.asText()?.let { "Resistência: $it" },
                                                                eq.tripulacao.asText()?.let { "Tripulação: $it" },
                                                            ).joinToString("  •  ").takeIf { it.isNotBlank() }
                                                            // ===== FIM PADRONIZAÇÃO =====

                                                            linhaArma?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                                                            linhaGeral?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                                                            linhaVeiculo?.let { Text(it, style = MaterialTheme.typography.bodySmall) }

                                                            eq.observacoes.asText()?.takeIf { it.isNotBlank() }?.let {
                                                                Text(it, style = MaterialTheme.typography.bodySmall)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
