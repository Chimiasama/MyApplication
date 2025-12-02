package com.example.swadebuilder.ui.sections

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swadebuilder.model.EquipamentoCategoria
import com.example.swadebuilder.model.EquipamentoItem
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

@OptIn(ExperimentalMaterial3Api::class)
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

                    val labelTipo = if (isSuper) {
                        if (tipoOriginal.contains("Equipamento Supers", ignoreCase = true)) {
                            "Superequip - Veículos"
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

    val expTipo = remember(mapa) { mapa.keys.associateWith { mutableStateOf(false) } }
    val expSub =
        remember(mapa) { mapa.mapValues { (_, sub) -> sub.keys.associateWith { mutableStateOf(false) } } }
    val expSub2 = remember(mapa) {
        mapa.mapValues { (_, sub) ->
            sub.mapValues { (_, sub2) -> sub2.keys.associateWith { mutableStateOf(false) } }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lista Completa de Equipamentos") },
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
                                subMapa.toSortedMap(compareBy { it.lowercase() })
                                    .forEach { (subtipo, sub2Mapa) ->
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
                                                sub2Mapa.toSortedMap(compareBy { it.lowercase() })
                                                    .forEach { (subsub, itens) ->
                                                        val ess = expSub2.getValue(tipo)
                                                            .getValue(subtipo).getValue(subsub)
                                                        Column(
                                                            Modifier.padding(
                                                                start = 8.dp,
                                                                bottom = 4.dp
                                                            )
                                                        ) {
                                                            if (subsub.isNotBlank()) {
                                                                Row(
                                                                    Modifier
                                                                        .fillMaxWidth()
                                                                        .clickable {
                                                                            ess.value = !ess.value
                                                                        }
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
                                                                ess.value = true
                                                            }

                                                            if (ess.value) {
                                                                Column(
                                                                    Modifier
                                                                        .fillMaxWidth()
                                                                        .padding(
                                                                            start = 8.dp,
                                                                            bottom = 8.dp
                                                                        )
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
                                                                                Text(
                                                                                    eq.nome,
                                                                                    style = MaterialTheme.typography.bodyLarge
                                                                                )
                                                                                eq.custo.asText()
                                                                                    ?.let { Text(it) }
                                                                            }

                                                                            val linhaArma =
                                                                                listOfNotNull(
                                                                                    eq.dano.asText()
                                                                                        ?.let { "Dano: $it" },
                                                                                    eq.pa.asText()
                                                                                        ?.let { "PA: $it" },
                                                                                    eq.cdt.asText()
                                                                                        ?.let { "CdT: $it" },
                                                                                    eq.distancia.asText()
                                                                                        ?.let { "Distância: $it" },
                                                                                    eq.tiros.asText()
                                                                                        ?.let { "Tiros: $it" },
                                                                                ).joinToString("  •  ")
                                                                                    .takeIf { it.isNotBlank() }

                                                                            val linhaGeral =
                                                                                listOfNotNull(
                                                                                    eq.peso.asText()
                                                                                        ?.let { "Peso: $it" },
                                                                                    eq.forcaMin.asText()
                                                                                        ?.let { "Força mín.: $it" },
                                                                                    eq.armadura.asText()
                                                                                        ?.let { "Armadura: $it" },
                                                                                    eq.aparar.asText()
                                                                                        ?.let { "Aparar: $it" },
                                                                                ).joinToString("  •  ")
                                                                                    .takeIf { it.isNotBlank() }

                                                                            val linhaVeiculo =
                                                                                listOfNotNull(
                                                                                    eq.velMaxima.asText()
                                                                                        ?.let { "Vel. máx.: $it" },
                                                                                    eq.manobrabilidade.asText()
                                                                                        ?.let { "Manobrabilidade: $it" },
                                                                                    eq.tamanho.asText()
                                                                                        ?.let { "Tamanho: $it" },
                                                                                    eq.resistencia.asText()
                                                                                        ?.let { "Resistência: $it" },
                                                                                    eq.tripulacao.asText()
                                                                                        ?.let { "Tripulação: $it" },
                                                                                    eq.blindagem.asText()
                                                                                        ?.let { "Blindagem: $it" },
                                                                                    eq.passageiros.asText()
                                                                                        ?.let { "Passageiros: $it" },
                                                                                ).joinToString("  •  ")
                                                                                    .takeIf { it.isNotBlank() }

                                                                            linhaArma?.let {
                                                                                Text(
                                                                                    it,
                                                                                    style = MaterialTheme.typography.bodySmall
                                                                                )
                                                                            }
                                                                            linhaGeral?.let {
                                                                                Text(
                                                                                    it,
                                                                                    style = MaterialTheme.typography.bodySmall
                                                                                )
                                                                            }
                                                                            linhaVeiculo?.let {
                                                                                Text(
                                                                                    it,
                                                                                    style = MaterialTheme.typography.bodySmall
                                                                                )
                                                                            }

                                                                            eq.observacoes.asText()
                                                                                ?.takeIf { it.isNotBlank() }
                                                                                ?.let {
                                                                                    Text(
                                                                                        it,
                                                                                        style = MaterialTheme.typography.bodySmall
                                                                                    )
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
    )
}

private val EquipamentoItem.passageiros
    get() = this.tripulacao
private val EquipamentoItem.blindagem
    get() = this.resistencia
