package com.example.swadebuilder.ui.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.R
import com.example.swadebuilder.loadRawText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AncestralidadesDetailScreen(
    state: CriadorState,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val ancestralidadesTexto = remember { loadRawText(context, R.raw.ancestralidades) }
    val listaBlocos = remember { parseAncestralidades(ancestralidadesTexto) }

    val atual = remember(state.ancestralidade, state.ancestralidadeEmFoco) {
        (state.ancestralidadeEmFoco ?: state.ancestralidade)
            .trim()
            .uppercase()
    }

    val listState = rememberLazyListState()

    LaunchedEffect(atual, listaBlocos) {
        if (atual.isNotBlank()) {
            val idxTitulo = listaBlocos.indexOfFirst { bloco ->
                bloco.tipo == "titulo" &&
                        bloco.conteudo
                            .removeSuffix(":")
                            .contains(atual, ignoreCase = true)
            }

            if (idxTitulo >= 0) {
                listState.scrollToItem(idxTitulo + 1)
            }
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lista Completa de Ancestralidades") },
                navigationIcon = {
                    TextButton(onClick = {
                        state.ancestralidadeEmFoco = null
                        onBack()
                    }) {
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
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                items(listaBlocos) { bloco ->
                    val isTitulo = bloco.tipo == "titulo"
                    val titulo = if (isTitulo) bloco.conteudo.removeSuffix(":") else ""
                    val destacado = isTitulo && titulo.contains(atual, ignoreCase = true)

                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .background(if (destacado) MaterialTheme.colorScheme.tertiaryContainer else Color.Transparent)
                    ) {
                        if (isTitulo) {
                            val label = if (destacado) "$titulo (atual)" else titulo
                            Text(label, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        } else {
                            Text(
                                text = bloco.conteudo,
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider()
                    }
                }
            }
        }
    )
}

data class BlocoTexto(val tipo: String, val conteudo: String)

fun parseAncestralidades(texto: String): List<BlocoTexto> {
    val linhas = texto.lines()
    val blocos = mutableListOf<BlocoTexto>()

    var esperandoTitulo = true

    for (linha in linhas) {
        val linhaLimpa = linha.trim()

        if (linhaLimpa.isBlank()) {
            esperandoTitulo = true
            continue
        }

        if (esperandoTitulo && linhaLimpa.all { it.isUpperCase() || it == '-' || it == ':' || it.isWhitespace() }) {
            blocos.add(BlocoTexto("titulo", linhaLimpa))
            esperandoTitulo = false
        } else {
            blocos.add(BlocoTexto("texto", linhaLimpa))
            esperandoTitulo = false
        }
    }

    return blocos
}
