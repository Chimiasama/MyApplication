package com.example.swadebuilder.ui.screens

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swadebuilder.R
import com.example.swadebuilder.loadRawText

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

@Composable
fun AtributosDetailScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val atributosTexto = remember {
        loadRawText(context, R.raw.atributos)
    }
    val listaAtributosCompleta = remember {
        parseAtributos(atributosTexto)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        stickyHeader {
            Surface(color = MaterialTheme.colorScheme.surface) {
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

                if (atributoCompleto.descricao.isNotBlank()) {
                    Text(
                        text = atributoCompleto.descricao,
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
            }
        }
    }
}
