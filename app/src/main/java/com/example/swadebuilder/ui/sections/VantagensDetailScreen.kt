package com.example.swadebuilder.ui.sections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.ui.components.VantagemListItem
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VantagensDetailScreen(
    highlightedName: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val todasVantagens: List<Vantagem> = remember {
        val jsonString = context.assets.open("Vantagens.json")
            .bufferedReader()
            .use { it.readText() }
        val parser = Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }
        parser.decodeFromString(
            ListSerializer(Vantagem.serializer()),
            jsonString
        )
    }

    val vantagensPorCategoria = remember(todasVantagens) {
        todasVantagens.groupBy { it.categoria.name }
    }

    val expandedState = remember {
        mutableStateMapOf<String, Boolean>().apply {
            vantagensPorCategoria.keys.forEach { put(it, false) }
        }
    }
    val listState = rememberLazyListState()

    LaunchedEffect(highlightedName, vantagensPorCategoria) {
        if (highlightedName.isNotEmpty()) {
            val targetCategory = vantagensPorCategoria.entries.firstOrNull { (_, vantagens) ->
                vantagens.any { it.nome == highlightedName }
            }?.key
            targetCategory?.let { expandedState[it] = true }

            val items = mutableListOf<String>()
            vantagensPorCategoria.forEach { (category, vantagens) ->
                items.add("header-$category")
                if (expandedState[category] == true) {
                    items.addAll(vantagens.map { it.nome })
                }
            }
            val index = items.indexOf(highlightedName)
            if (index >= 0) {
                listState.animateScrollToItem(index)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lista de Vantagens") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text(
                            "Voltar",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            contentPadding = innerPadding,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            vantagensPorCategoria.forEach { (category, vantagens) ->
                item {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                val current = expandedState[category] ?: false
                                expandedState[category] = !current
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = if (expandedState[category] == true) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = null
                        )
                    }
                }
                if (expandedState[category] == true) {
                    items(vantagens) { vantagem ->
                        VantagemListItem(vantagem = vantagem)
                    }
                }
            }
        }
    }
}