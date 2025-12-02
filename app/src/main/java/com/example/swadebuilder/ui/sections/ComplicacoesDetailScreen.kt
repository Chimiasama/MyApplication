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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.model.Complicacao
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplicacoesDetailScreen(
    state: CriadorState,
    onBack: () -> Unit,
    mostrarSuper: Boolean
) {
    val context = LocalContext.current

    val listaTodas = remember {
        val jsonString = context.assets.open("complicacoes.json")
            .bufferedReader()
            .use { it.readText() }
        val parser = Json { ignoreUnknownKeys = true; explicitNulls = false }
        parser.decodeFromString(ListSerializer(Complicacao.serializer()), jsonString)
    }

    val listaFiltrada = remember(mostrarSuper, listaTodas) {
        if (mostrarSuper) listaTodas else listaTodas.filter { !it.origem.equals("SUPER", true) }
    }

    val jaEscolhidas = state.complicacoesSelecionadas

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (mostrarSuper) "Lista de Complicações e Supercomplicações" else "Lista de Complicações") },
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
                items(listaFiltrada) { comp ->
                    val tipoSel = jaEscolhidas[comp]
                    val marcado = tipoSel != null
                    val sevRaw = comp.severity.trim()
                    val gravidade = when (sevRaw.lowercase()) {
                        "both" -> "Menor/Maior"
                        "menor" -> "Menor"
                        "maior" -> "Maior"
                        else -> sevRaw.ifBlank { "-" }
                    }

                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .background(if (marcado) MaterialTheme.colorScheme.tertiaryContainer else Color.Transparent)
                    ) {
                        Text(
                            buildString {
                                append(comp.name)
                                append(" (")
                                append(gravidade)
                                append(")")
                                if (marcado) append(" — já escolhida: $tipoSel")
                            },
                            fontWeight = FontWeight.SemiBold
                        )

                        if (comp.description.isNotBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = comp.description,
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider()
                    }
                }
            }
        }
    )
}
