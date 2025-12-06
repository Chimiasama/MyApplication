package com.example.swadebuilder.ui.sections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swadebuilder.model.EquipamentoCategoria
import com.example.swadebuilder.ui.components.EquipamentoListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipamentosDetailScreen(
    categorias: List<EquipamentoCategoria>,
    onBack: () -> Unit
) {
    val mapa = remember(categorias) {
        categorias
            .groupBy { cat ->
                val tipoOriginal = cat.tipo
                val isSuper = cat.origem.equals("SUPER", ignoreCase = true)
                if (isSuper) "Superequipamentos" else tipoOriginal
            }
            .mapValues { (_, porTipo) ->
                porTipo.groupBy { it.subtipo }
                    .mapValues { (_, porSubtipo) ->
                        porSubtipo.groupBy { it.subsubtipo ?: "" }
                            .mapValues { (_, listaFinal) ->
                                listaFinal.flatMap { it.itens }.sortedBy { it.nome }
                            }
                    }
            }
    }

    val expTipo = remember { mutableStateMapOf<String, Boolean>() }
    val expSub = remember { mutableStateMapOf<String, Boolean>() }
    val expSubSub = remember { mutableStateMapOf<String, Boolean>() }


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
                    item(key = "tipo-$tipo") {
                        val isExpanded = expTipo[tipo] ?: false
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { expTipo[tipo] = !isExpanded }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(tipo, style = MaterialTheme.typography.titleMedium)
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (isExpanded) "Fechar" else "Abrir"
                            )
                        }
                    }

                    if (expTipo[tipo] == true) {
                        subMapa.toSortedMap(compareBy { it.lowercase() })
                            .forEach { (subtipo, sub2Mapa) ->
                                val subKey = "$tipo-$subtipo"
                                item(key = "sub-$subKey") {
                                    val isExpanded = expSub[subKey] ?: false
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .clickable { expSub[subKey] = !isExpanded }
                                            .padding(vertical = 6.dp, horizontal = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(subtipo, fontWeight = FontWeight.Bold)
                                        Icon(
                                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = if (isExpanded) "Fechar" else "Abrir"
                                        )
                                    }
                                }

                                if (expSub[subKey] == true) {
                                    sub2Mapa.toSortedMap(compareBy { it.lowercase() })
                                        .forEach { (subsub, itens) ->
                                            if (subsub.isNotBlank()) {
                                                val subSubKey = "$subKey-$subsub"
                                                item(key = "subsub-$subSubKey") {
                                                    val isExpanded = expSubSub[subSubKey] ?: false
                                                    Row(
                                                        Modifier
                                                            .fillMaxWidth()
                                                            .clickable { expSubSub[subSubKey] = !isExpanded }
                                                            .padding(vertical = 4.dp, horizontal = 16.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(subsub)
                                                        Icon(
                                                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                            contentDescription = if (isExpanded) "Fechar" else "Abrir"
                                                        )
                                                    }
                                                }
                                                if (expSubSub[subSubKey] == true) {
                                                    items(itens, key = { "item-${it.nome}" }) { equipamento ->
                                                        Column(Modifier.padding(start = 24.dp)) {
                                                            EquipamentoListItem(equipamento = equipamento)
                                                        }
                                                    }
                                                }
                                            } else {
                                                items(itens, key = { "item-${it.nome}" }) { equipamento ->
                                                    Column(Modifier.padding(start = 16.dp)) {
                                                        EquipamentoListItem(equipamento = equipamento)
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
