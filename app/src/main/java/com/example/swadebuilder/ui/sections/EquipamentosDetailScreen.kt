package com.example.swadebuilder.ui.sections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
    val equipamentosPorCategoria = remember(categorias) {
        categorias.groupBy(
            { it.tipo },
            { it.itens }
        ).mapValues { it.value.flatten() }
    }

    val expandedState = remember {
        mutableStateMapOf<String, Boolean>().apply {
            equipamentosPorCategoria.keys.forEach { put(it, false) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lista de Equipamentos") },
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
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            equipamentosPorCategoria.forEach { (category, equipamentos) ->
                item {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                val current = expandedState[category] ?: false
                                expandedState[category] = !current
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(category, style = MaterialTheme.typography.titleMedium)
                        Icon(
                            imageVector = if (expandedState[category] == true) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = if (expandedState[category] == true) "Fechar" else "Abrir"
                        )
                    }
                }
                if (expandedState[category] == true) {
                    items(equipamentos) { equipamento ->
                        EquipamentoListItem(equipamento = equipamento)
                    }
                }
            }
        }
    }
}