package com.example.swadebuilder.ui.sections

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.SuperPoder
import com.example.swadebuilder.model.loadJsonAsset
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun SuperPoderesDetailScreen(
    state: CriadorState,
    highlightedName: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val superPoderes: List<SuperPoder> = remember {
        context.loadJsonAsset("superpoderes.json")
    }

    val listState = rememberLazyListState()

    LaunchedEffect(highlightedName, superPoderes) {
        if (highlightedName.isNotEmpty()) {
            val index = superPoderes.indexOfFirst {
                it.nome.equals(highlightedName, ignoreCase = true)
            }
            if (index >= 0) {
                listState.scrollToItem(index)
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Transparent
    ) {
        Column {
            TopAppBar(
                title = { Text("Lista Completa de Superpoderes") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text(
                            "Voltar",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    "Pontos: ${state.superPontosDisponiveis}/${state.superPontosTotais} • " +
                            "Limite padrão: ${state.limitePorPoderPadrao} • " +
                            "Favorecido: ${state.limiteFavorecido}",
                    fontWeight = FontWeight.SemiBold
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState
            ) {
                items(superPoderes, key = { it.nome }) { poder ->
                    val startExpanded =
                        highlightedName.isNotEmpty() &&
                                poder.nome.equals(highlightedName, ignoreCase = true)

                    var expanded by rememberSaveable(poder.nome, highlightedName) {
                        mutableStateOf(startExpanded)
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = !expanded },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = poder.nome,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Spacer(Modifier.weight(1f))
                            Icon(
                                imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = null
                            )
                        }

                        AnimatedVisibility(visible = expanded) {
                            Column(Modifier.padding(top = 4.dp, bottom = 8.dp)) {
                                poder.custoBase?.let { custo ->
                                    Text(
                                        text = "Custo Base: $custo",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                    Spacer(Modifier.height(4.dp))
                                }

                                val mods = poder.modificadores ?: emptyList()
                                if (mods.isNotEmpty()) {
                                    Text(
                                        text = "Modificadores:",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                    mods.forEach { mod ->
                                        Text(
                                            text = "- $mod",
                                            fontSize = 14.sp,
                                            modifier = Modifier.padding(start = 8.dp)
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                }

                                val mans: List<String> = when (val m = poder.manifestacoes) {
                                    is JsonArray -> {
                                        m.mapNotNull { elem ->
                                            (elem as? JsonPrimitive)?.contentOrNull
                                                ?: (elem as? JsonPrimitive)?.content
                                        }
                                    }
                                    is JsonPrimitive -> {
                                        listOf(m.content)
                                    }
                                    else -> emptyList()
                                }

                                if (mans.isNotEmpty()) {
                                    Text(
                                        text = "Manifestações:",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                    mans.forEach { man ->
                                        Text(
                                            text = "- $man",
                                            fontSize = 14.sp,
                                            modifier = Modifier.padding(start = 8.dp)
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                }

                                poder.descricao?.let { desc ->
                                    Text(
                                        text = "Descrição:",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = desc,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }

                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
