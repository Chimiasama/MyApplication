package com.example.swadebuilder.ui.sections

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.listaAncestralidadesJson
import com.example.swadebuilder.listaAtributos
import com.example.swadebuilder.listaPericias
import com.example.swadebuilder.mapaAtributosDisplay
import com.example.swadebuilder.periciaStartRaw
import com.example.swadebuilder.toDiceString
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.valorAparar
import com.example.swadebuilder.valorArmaduraEfetiva
import com.example.swadebuilder.valorMovimentacao
import com.example.swadebuilder.valorResistenciaFinal
import com.example.swadebuilder.valorTamanho

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SummaryContent(state: CriadorState) {
    // Nome bonitinho da ancestralidade, usando o JSON já carregado
    val ancestralidadeNome = listaAncestralidadesJson
        .firstOrNull { it.nome.keyify() == state.ancestralidade }
        ?.nome ?: "—"

    // Derivados usando as MESMAS funções que o resto do app
    val aparar = state.valorAparar()
    val resistenciaFinal = state.valorResistenciaFinal()
    val tamanho = state.valorTamanho()
    val movimentacao = state.valorMovimentacao()
    val armadura = state.valorArmaduraEfetiva()

    // Texto de resistência: total (armadura) – ex: 12(7)
    val resistenciaTexto =
        if (armadura > 0) "${resistenciaFinal}(${armadura})" else resistenciaFinal.toString()

    // Template só com FLAGS ATIVAS
    val flagsTemplate: String = run {
        val linhas = mutableListOf<String>()

        if (state.cartaSelvagem) {
            linhas += "- Carta Selvagem"
        }
        if (state.maisPontosPericias) {
            linhas += "- Mais pontos de perícia"
        }
        if (state.heroisSemArmadura) {
            linhas += "- Heróis sem armadura"
        }
        if (state.modoSupers) {
            linhas += "- Modo Supers"
        }
        if (state.modoSuperComplicacoes) {
            linhas += "- Complicações de Super"
        }
        if (state.modoSuperequip) {
            linhas += "- Equipamentos de Super"
        }
        if (state.grandesResponsabilidades) {
            linhas += "- Grandes responsabilidades"
        }

        if (linhas.isEmpty()) {
            ""
        } else {
            buildString {
                appendLine("Opções / flags de campanha ativas:")
                linhas.forEach { appendLine(it) }
                appendLine()
                appendLine("Use o espaço abaixo para anotar ganchos, personalidade, aliados, inimigos, etc.")
            }.trimEnd()
        }
    }

    // Preenche UMA VEZ as anotações se estiverem vazias e houver flags ativas
    LaunchedEffect(flagsTemplate) {
        if (state.anotacoes.isBlank() && flagsTemplate.isNotBlank()) {
            state.anotacoes = flagsTemplate
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // Título
        Text(
            text = "Resumo do Personagem",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Use este resumo para conferir rapidamente se tudo está fechado antes da sessão.",
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(Modifier.height(12.dp))

        // ------------------ IDENTIDADE ------------------
        Text(
            text = "Identidade",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Nome: ${state.nomePersonagem.ifBlank { "(sem nome)" }}",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "Ancestralidade: $ancestralidadeNome",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(12.dp))

        // ------------------ DERIVADOS ------------------
        Text(
            text = "Atributos derivados",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "Aparar: $aparar",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Resistência: $resistenciaTexto",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Tamanho: $tamanho",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Movimento: $movimentacao",
                style = MaterialTheme.typography.bodySmall
            )
            // Armadura ainda é útil ver isolada
            if (armadura > 0) {
                Text(
                    text = "Armadura: $armadura",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // ------------------ ATRIBUTOS ------------------
        Text(
            text = "Atributos",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listaAtributos.forEach { attrKey ->
                val display = mapaAtributosDisplay[attrKey] ?: attrKey
                val rawFinal = state.atributoRawComSupers(attrKey)
                val dado = rawFinal.toDiceString()

                Text(
                    text = "$display: $dado",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // ------------------ PERÍCIAS ------------------
        Text(
            text = "Perícias",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))

        val periciasParaMostrar = listaPericias.filter { per ->
            // Mesma lógica do PDF:
            // - mostra todas as básicas
            // - mostra não básicas só se estiverem acima do valor inicial racial
            val raw = state.rawTotal(per)
            per.basica || raw > periciaStartRaw(state.ancestralidade, per)
        }

        if (periciasParaMostrar.isEmpty()) {
            Text("– Nenhuma", style = MaterialTheme.typography.bodySmall)
        } else {
            periciasParaMostrar.forEach { per ->
                val raw = state.rawTotal(per)
                val dado = raw.toDiceString()
                Text(
                    text = "• ${per.nome}: $dado",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // ------------------ RECURSOS & EQUIPAMENTOS ------------------
        Text(
            text = "Recursos & Equipamentos",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))

        Text(
            text = "Dinheiro restante: ${state.dinheiro}",
            style = MaterialTheme.typography.bodySmall
        )

        if (state.equipamentosComprados.isEmpty()) {
            Text(
                text = "Equipamentos: – Nenhum",
                style = MaterialTheme.typography.bodySmall
            )
        } else {
            Text(
                text = "Equipamentos:",
                style = MaterialTheme.typography.bodySmall
            )
            state.equipamentosComprados.forEach { eq ->
                Text(
                    text = "• ${eq.nome}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // ------------------ VANTAGENS ------------------
        Text(
            text = "Vantagens",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))

        val vantsSelecionadas = state.vantagensSelecionadas
        if (vantsSelecionadas.isEmpty() && state.vantagensAutomaticas.isEmpty()) {
            Text("– Nenhuma", style = MaterialTheme.typography.bodySmall)
        } else {
            vantsSelecionadas.forEach { vant ->
                val choiceSuffix = vant.choice?.let { " ($it)" } ?: ""
                Text(
                    text = "• ${vant.nome}$choiceSuffix",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            state.vantagensAutomaticas.forEach { nomeAuto ->
                Text(
                    text = "• $nomeAuto [Racial]",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // ------------------ COMPLICAÇÕES ------------------
        Text(
            text = "Complicações",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))

        val compList = state.complicacoesSelecionadas.entries.toList()
        if (compList.isEmpty() && state.desvantagensAutomaticas.isEmpty()) {
            Text("– Nenhuma", style = MaterialTheme.typography.bodySmall)
        } else {
            compList.forEach { (comp, grau) ->
                val grauLabel = grau ?: "Menor"
                Text(
                    text = "• ${comp.id} ($grauLabel)",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            state.desvantagensAutomaticas.forEach { nomeAuto ->
                Text(
                    text = "• $nomeAuto [Racial]",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // ------------------ PODERES ARCANOS (só se existirem) ------------------
        if (state.poderSlotsPorArcano.isNotEmpty()) {
            Text(
                text = "Poderes arcanos",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))

            state.poderSlotsPorArcano.forEach { (arcanoKey, slots) ->
                val poderes = slots.filterNotNull()

                val label = arcanoKey
                    .lowercase()
                    .replace('_', ' ')
                    .replaceFirstChar { it.titlecase() }

                val textoPoderes = if (poderes.isEmpty()) {
                    "– nenhum poder escolhido"
                } else {
                    poderes.joinToString(", ")
                }

                Text(
                    text = "• $label: $textoPoderes",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(12.dp))
        }

        // ------------------ SUPERPODERES (só se for supers e tiver poder) ------
        if (state.modoSupers && state.superPoderesComprados.isNotEmpty()) {
            Text(
                text = "Superpoderes",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))

            state.superPoderesComprados.forEach { comprado ->
                Text(
                    text = "• ${comprado.nome} (${comprado.custo} SP)",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(4.dp))
            Text(
                text = "Superpontos: ${state.superPontosTotais} (disponíveis: ${state.superPontosDisponiveis})",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Limite por poder: ${state.superLimitePorPoder}",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(Modifier.height(16.dp))
        }

        HorizontalDivider()
        Spacer(Modifier.height(8.dp))

        // ------------------ ANOTAÇÕES LIVRES ------------------
        Text(
            text = "Anotações livres",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))

        OutlinedTextField(
            value = state.anotacoes,
            onValueChange = { state.anotacoes = it },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp),
            textStyle = MaterialTheme.typography.bodySmall,
            label = { Text("Anote lembretes importantes sobre o personagem") }
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
