package com.example.swadebuilder.ui.sections

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.buildSummaryLines
import com.example.swadebuilder.toMeuPersonagem

@Composable
fun SummaryContent(state: CriadorState) {

    val flagsTemplate = remember(state) {
        listOfNotNull(
            "Carta Selvagem".takeIf { state.cartaSelvagem },
            "Heróis Sem Armadura".takeIf { state.heroisSemArmadura },
            "Sem Pontos de Poder".takeIf { state.usarSemPontosDePoder },
            "Mais Pontos de Perícias".takeIf { state.maisPontosPericias },
            "Especializações".takeIf { state.usarEspecializacoesDePericia }
        )
    }

    LaunchedEffect(flagsTemplate) {
        if (state.anotacoes.isBlank() && flagsTemplate.isNotEmpty()) {
            state.anotacoes = flagsTemplate.joinToString("\n") { "• $it" }
        }
    }

    val personagem = state.toMeuPersonagem()
    val allLines = buildSummaryLines(personagem)

    val anotIndex = allLines.indexOf("Anotações")
    val lines = if (anotIndex >= 0) allLines.take(anotIndex) else allLines

    val headers = remember {
        setOf(
            "Identidade",
            "Atributos derivados",
            "Atributos",
            "Perícias",
            "Recursos & Equipamentos",
            "Vantagens",
            "Complicações",
            "Poderes arcanos",
            "Superpoderes"
        )
    }

    Column(Modifier.fillMaxWidth()) {

        lines.forEach { linha ->
            when {
                linha.isBlank() -> Spacer(Modifier.height(8.dp))
                linha in headers -> {
                    Text(
                        text = linha,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                }
                else -> {
                    Text(
                        text = linha,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        HorizontalDivider(thickness = 1.dp)
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = state.anotacoes,
            onValueChange = { state.anotacoes = it },
            label = { Text("Anotações") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
    }
}

@Composable
fun CircleStat(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                textAlign = TextAlign.Center
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}
