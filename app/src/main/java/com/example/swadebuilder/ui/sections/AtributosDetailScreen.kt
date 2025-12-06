package com.example.swadebuilder.ui.sections

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swadebuilder.R
import com.example.swadebuilder.loadRawText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AtributosDetailScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val allowLongTexts = booleanResource(R.bool.enable_long_texts)
    val detalhesExpandidos = remember { mutableStateMapOf<String, Boolean>() }
    val atributosTexto = remember {
        loadRawText(context, R.raw.atributos)
    }
    val listaAtributosCompleta = remember {
        parseAtributos(atributosTexto)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lista Completa de Atributos") },
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
                items(listaAtributosCompleta) { atributoCompleto ->
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = atributoCompleto.nome,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        val canShowDetails = allowLongTexts && atributoCompleto.descricao.isNotBlank()
                        if (canShowDetails) {
                            TextButton(
                                onClick = {
                                    val current = detalhesExpandidos[atributoCompleto.nome] ?: false
                                    detalhesExpandidos[atributoCompleto.nome] = !current
                                },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    if (detalhesExpandidos[atributoCompleto.nome] == true) "Ocultar detalhes" else "Ver detalhes",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }

                            AnimatedVisibility(visible = detalhesExpandidos[atributoCompleto.nome] == true) {
                                Text(
                                    text = atributoCompleto.descricao,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                    }
                }
            }
        }
    )
}

data class AtributoCompleto(
    val nome: String,
    val descricao: String
)

fun parseAtributos(texto: String): List<AtributoCompleto> {
    val linhas = texto.lines()
    val lista = mutableListOf<AtributoCompleto>()

    var nomeAtual = ""
    val descricaoAtual = StringBuilder()

    for (linha in linhas) {
        val linhaTrimada = linha.trim()

        if (linhaTrimada.isBlank()) continue

        if (linhaTrimada.endsWith(":")) {
            if (nomeAtual.isNotEmpty()) {
                lista.add(
                    AtributoCompleto(
                        nome = nomeAtual,
                        descricao = descricaoAtual.toString().trim()
                    )
                )
            }
            nomeAtual = linhaTrimada.removeSuffix(":").trim()
            descricaoAtual.clear()
        } else {
            descricaoAtual.appendLine(linhaTrimada)
        }
    }

    if (nomeAtual.isNotEmpty()) {
        lista.add(
            AtributoCompleto(
                nome = nomeAtual,
                descricao = descricaoAtual.toString().trim()
            )
        )
    }

    return lista
}
