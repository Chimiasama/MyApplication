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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swadebuilder.BlocoTexto
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.R
import com.example.swadebuilder.loadRawText
import com.example.swadebuilder.parseAncestralidades

@Composable
fun AncestralidadesDetailScreen(
    state: CriadorState,
    onBack: () -> Unit,
    ancestralidadesText: String
) {
    val listaBlocos = remember(ancestralidadesText) { parseAncestralidades(ancestralidadesText) }

    val atual = remember(state.ancestralidade) { state.ancestralidade.trim().uppercase() }

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
                        text = "Voltar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                HorizontalDivider()
            }
        }

        items(listaBlocos) { bloco ->
            val isTitulo = bloco.tipo == "titulo"
            val titulo = if (isTitulo) bloco.conteudo.removeSuffix(":") else ""
            val destacado = isTitulo && titulo.contains(atual, ignoreCase = true)

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .background(if (destacado) Color(0x11007AFF) else Color.Transparent)
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
