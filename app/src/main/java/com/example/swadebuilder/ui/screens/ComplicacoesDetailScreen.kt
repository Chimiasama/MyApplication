package com.example.swadebuilder.ui.screens

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swadebuilder.R
import com.example.swadebuilder.model.Complicacao
import com.example.swadebuilder.model.CriadorState
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

@Composable
fun ComplicacoesDetailScreen(
    state: CriadorState,
    onBack: () -> Unit,
    mostrarSuper: Boolean
) {
    val context = LocalContext.current

    // Lê todas as complicações do JSON em assets/complicacoes.json
    val listaTodas = remember {
        val jsonString = context.assets.open("complicacoes.json")
            .bufferedReader()
            .use { it.readText() }
        val parser = Json { ignoreUnknownKeys = true; explicitNulls = false }
        parser.decodeFromString(ListSerializer(Complicacao.serializer()), jsonString)
    }

    // Aplica o filtro "Super" conforme o modo selecionado na tela inicial
    val listaFiltrada = remember(mostrarSuper, listaTodas) {
        if (mostrarSuper) listaTodas else listaTodas.filter { !it.origem.equals("SUPER", true) }
    }

    // ► usa o estado global para saber o que já foi escolhido
    val jaEscolhidas = state.complicacoesSelecionadas

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
                        .padding(vertical = 12.dp)
                ) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.top_bar_back),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                HorizontalDivider()
            }
        }

        items(listaFiltrada) { comp ->
            val tipoSel = jaEscolhidas[comp] // "Menor", "Maior" ou null
            val marcado = tipoSel != null
            val sevRaw = comp.severity.trim()
            val gravidade = when (sevRaw.lowercase()) {
                "both"  -> "Menor/Maior"
                "menor" -> "Menor"
                "maior" -> "Maior"
                else    -> sevRaw.ifBlank { "-" }
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .background(if (marcado) Color(0x11007AFF) else Color.Transparent)
            ) {
                // Linha do título
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

                // Descrição (se existir)
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
