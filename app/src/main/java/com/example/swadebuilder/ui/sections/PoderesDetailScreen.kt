// PoderesDetailScreen.kt
package com.example.swadebuilder.ui.sections

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import com.example.swadebuilder.CriadorState
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swadebuilder.model.Poder
import com.example.swadebuilder.model.PoderesList
import kotlinx.serialization.json.Json

@Composable
fun PoderesDetailScreen(
    state: CriadorState,
    onBack: () -> Unit
)
 {
    val context = LocalContext.current

    // 1) Criamos UMA ÚNICA instância de Json com ignoreUnknownKeys = true
    val jsonParser = remember {
        Json { ignoreUnknownKeys = true }
    }

    // 2) Carrega o JSON bruto de “poderes.json” apenas UMA vez
    val jsonText = remember {
        context.assets.open("poderes.json")
            .bufferedReader(Charsets.UTF_8)
            .use { it.readText() }
    }

    // 3) Decodifica o texto para PoderesList usando a instância de jsonParser
    val poderes: List<Poder> = remember(jsonText) {
        jsonParser.decodeFromString<PoderesList>(jsonText).poderes
    }

    // 4) Exibe em uma LazyColumn. Cabeçalho “Voltar” fixo no topo:
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        stickyHeader {
            Surface(color = androidx.compose.ui.graphics.Color.Transparent) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onBack() }
                        .padding(vertical = 12.dp, horizontal = 16.dp)
                ) {
                    Text(
                        text = "Voltar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                HorizontalDivider()
            }
        }

        // 5) Para cada Poder, exibimos um item expansível
        items(
            items = poderes,
            key = { it.id } // utiliza o “id” como chave única
        ) { poder ->
            // Estado de expansão para cada item
            var expanded by rememberSaveable(poder.id) { mutableStateOf(false) }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 16.dp)
            ) {
                // 5.1) Cabeçalho clicável que alterna “expanded”
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = poder.nome,                         //  ► Nome
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Recolher" else "Expandir"
                    )
                }

                // 5.2) Quando `expanded == true`, mostramos os detalhes na ordem solicitada:
                AnimatedVisibility(visible = expanded) {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        // • Custo Base (pontosDePoder)
                        Text(
                            text = "Custo Base: ${poder.pontosDePoder}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // • Manifestações
                        Text(
                            text = "Manifestações:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        // Concatena cada item da lista em linhas separadas
                        Text(
                            text = poder.manifestacoes.joinToString(separator = "\n") { "• $it" },
                            fontSize = 14.sp,
                            modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // • Descrição
                        Text(
                            text = "Descrição:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = poder.descricao,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // • Modificadores
                        if (poder.modificadores.isNotEmpty()) {
                            Text(
                                text = "Modificadores:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            poder.modificadores.forEach { mod ->
                                // Exibe cada objeto Modificador no formato: “• Nome (Custo): Descrição”
                                Text(
                                    text = "• ${mod.nome} (${mod.custo}): ${mod.descricao}",
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
            }
        }
    }
}
