package com.example.swadebuilder.ui.sections

import androidx.compose.foundation.clickable
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
import com.example.swadebuilder.AppData
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.ui.components.VantagemListItem
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VantagensDetailScreen(
    state: CriadorState,
    modoSupers: Boolean,
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

    val idToNameMap = remember(todasVantagens) {
        todasVantagens.associate { it.id to it.nome }
    }

    val listaFiltrada = remember(modoSupers, todasVantagens, AppData.superVantagensParaDetalhe) {
        val baseList = if (!modoSupers) {
            todasVantagens
        } else {
            todasVantagens.filter { vant ->
                vant.id != "antecedente_arcano" &&
                        !vant.requisitos.vantagensPrevias.contains("antecedente_arcano") &&
                        vant.categoria.name.uppercase() != "PODER"
            }
        }
        baseList + if (modoSupers) AppData.superVantagensParaDetalhe else emptyList()
    }

    val vantagensAgrupadas = remember(listaFiltrada) {
        listaFiltrada.groupBy { it.categoria.name.lowercase().replaceFirstChar { char -> if (char.isLowerCase()) char.titlecase() else char.toString() } }
            .toSortedMap(compareBy { it })
    }

    val expandedState = remember {
        mutableStateMapOf<String, Boolean>().apply {
            vantagensAgrupadas.keys.forEach { put(it, false) }
        }
    }
    val listState = rememberLazyListState()

    LaunchedEffect(highlightedName, vantagensAgrupadas) {
        if (highlightedName.isNotEmpty()) {
            val targetCat = vantagensAgrupadas.entries.firstOrNull { (_, vants) ->
                vants.any { it.nome == highlightedName }
            }?.key
            if (targetCat != null) {
                expandedState[targetCat] = true
                val groupIndex = vantagensAgrupadas.keys.indexOf(targetCat)
                var itemIndexInList = groupIndex + 1

                val vantsInGroup = vantagensAgrupadas[targetCat] ?: emptyList()
                val indexInGroup = vantsInGroup.indexOfFirst { it.nome == highlightedName }
                if (indexInGroup != -1) {
                    itemIndexInList += indexInGroup
                    listState.animateScrollToItem(itemIndexInList)
                }
            }
        }
    }

    val nomesJaSelecionadas = remember(state.vantagensSelecionadas) {
        state.vantagensSelecionadas.map { it.nome }.toSet()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text(
                            "Voltar",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                        )
                    }
                },
                title = { Text(if (modoSupers) "Lista de Vantagens e Supervantagens" else "Lista de Vantagens") },
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
            vantagensAgrupadas.forEach { (categoria, vants) ->
                item(key = "header-$categoria") {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                val atual = expandedState[categoria] ?: false
                                expandedState[categoria] = !atual
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = categoria,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = if (expandedState[categoria] == true)
                                Icons.Filled.ExpandLess
                            else
                                Icons.Filled.ExpandMore,
                            contentDescription = null
                        )
                    }
                }

                if (expandedState[categoria] == true) {
                    items(vants, key = { "vant-${it.id}" }) { vant ->
                        VantagemListItem(
                            vantagem = vant,
                            isRequirementMet = state.podeSelecionar(vant),
                            isAlreadyAcquired = vant.nome in nomesJaSelecionadas,
                            idToNameMap = idToNameMap
                        )
                    }
                }
            }
        }
    }
}
