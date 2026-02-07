package com.example.swadebuilder.ui.sections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.EditionConfig
import com.example.swadebuilder.criacaoBasicaCongelada
import com.example.swadebuilder.listaTropos
import com.example.swadebuilder.listaVantagens
import com.example.swadebuilder.ui.components.RadioButtonRow
import com.example.swadebuilder.ui.components.SectionCard
import kotlin.random.Random

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TroposSection(
    state: CriadorState,
    onUserFeedback: () -> Unit
) {
    if (!state.compendioArteDaGuerraAtivo) return

    val tropos = remember { listaTropos }
    val showOfficialNames = EditionConfig.isFullEdition && state.modoOficialAtivo
    val idParaNome = remember(showOfficialNames) {
        listaVantagens.associate { vant ->
            val nome = if (showOfficialNames && !vant.originalName.isNullOrBlank()) vant.originalName else vant.nome
            vant.id to nome
        }
    }

    SectionCard(
        title = "Tropos",
        icon = Icons.Default.AutoAwesome,
        showHeader = false
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Escolha um único tropo para definir o estilo marcial do personagem.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Option "None" to unlock race change
            val noneSelected = state.tropoSelecionado == null
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        state.selecionarTropo(null)
                        onUserFeedback()
                    },
                colors = CardDefaults.cardColors(
                    containerColor = if (noneSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    RadioButtonRow(
                        selected = noneSelected,
                        label = "Nenhum (Permite alterar Ancestralidade)",
                        onSelect = {
                            state.selecionarTropo(null)
                            onUserFeedback()
                        }
                    )
                    Text(
                        text = "Selecione esta opção se deseja alterar sua Ancestralidade. Enquanto um Tropo estiver ativo, a Ancestralidade fica bloqueada.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 40.dp, top = 4.dp)
                    )
                }
            }

            tropos.forEach { tropo ->
                val selecionado = state.tropoSelecionado?.id == tropo.id
                val vantagensNomeadas = tropo.ganhaAoComprar.map { idParaNome[it] ?: it }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable(enabled = !state.criacaoBasicaCongelada) {
                            state.selecionarTropo(tropo)
                            onUserFeedback()
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selecionado) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        RadioButtonRow(
                            selected = selecionado,
                            label = if (showOfficialNames && tropo.nome.isNotBlank()) tropo.nome else tropo.nome,
                            onSelect = {
                                if (state.criacaoBasicaCongelada) return@RadioButtonRow
                                state.selecionarTropo(tropo)
                                onUserFeedback()
                            }
                        )

                        if (tropo.tecnicasIniciais > 0) {
                            Text(
                                text = "Técnicas iniciais: ${tropo.tecnicasIniciais}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 40.dp, top = 4.dp)
                            )
                        }

                        Text(
                            text = tropo.descricao,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 40.dp, top = 4.dp, end = 8.dp)
                        )

                        if (selecionado && tropo.id == "tropo_protagonista") {
                            val random = remember { Random(System.currentTimeMillis()) }
                            val periciaOpcoes = listOf(
                                "Atletismo",
                                "Conhecimento Geral",
                                "Perceber",
                                "Persuadir",
                                "Furtividade"
                            )
                            val vantagemOpcoes = listOf(
                                "Vantagem de Chi",
                                "Vantagem Estranha",
                                "Vantagem de Antecedente",
                                "Vantagem de Combate",
                                "Vantagem Social",
                                "Qualquer Vantagem"
                            )

                            var tecnicasRoll by rememberSaveable(tropo.id) { mutableStateOf<Int?>(null) }
                            var periciaRoll by rememberSaveable(tropo.id) { mutableStateOf<Int?>(null) }
                            var periciasEscolhidas by rememberSaveable(tropo.id) { mutableStateOf(listOf<String>()) }
                            var vantagemRoll by rememberSaveable(tropo.id) { mutableStateOf<Int?>(null) }
                            var vantagemEscolhida by rememberSaveable(tropo.id) { mutableStateOf<String?>(null) }
                            var qualidadeRoll by rememberSaveable(tropo.id) { mutableStateOf<Int?>(null) }
                            var habilidadeRoll by rememberSaveable(tropo.id) { mutableStateOf<Int?>(null) }

                            fun rollDie(sides: Int): Int = random.nextInt(1, sides + 1)

                            Spacer(Modifier.size(8.dp))
                            Text(
                                text = "Rolagens do Protagonista (opcional)",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 40.dp, top = 4.dp)
                            )
                            Text(
                                text = "Use as rolagens como guia e aplique as escolhas manualmente.",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 40.dp, top = 4.dp, end = 8.dp)
                            )

                            Row(
                                modifier = Modifier.padding(start = 40.dp, top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        tecnicasRoll = rollDie(4)
                                        periciaRoll = rollDie(6)
                                        vantagemRoll = rollDie(8)
                                        qualidadeRoll = rollDie(10)
                                        habilidadeRoll = rollDie(12)
                                        vantagemEscolhida = null
                                        periciasEscolhidas = emptyList()
                                    }
                                ) {
                                    Text("Rolar tudo")
                                }
                                OutlinedButton(
                                    onClick = {
                                        tecnicasRoll = null
                                        periciaRoll = null
                                        vantagemRoll = null
                                        qualidadeRoll = null
                                        habilidadeRoll = null
                                        vantagemEscolhida = null
                                        periciasEscolhidas = emptyList()
                                    }
                                ) {
                                    Text("Limpar")
                                }
                            }

                            Spacer(Modifier.size(6.dp))
                            val tecnicasTexto = when (tecnicasRoll) {
                                1 -> "1"
                                2 -> "2"
                                3 -> "2"
                                4 -> "3"
                                else -> "-"
                            }
                            val periciaRollTexto = periciaRoll?.toString() ?: "-"
                            val vantagemRollTexto = vantagemRoll?.toString() ?: "-"
                            val qualidadeRollTexto = qualidadeRoll?.toString() ?: "-"
                            val habilidadeRollTexto = habilidadeRoll?.toString() ?: "-"
                            Text(
                                text = "Técnicas de Chi (d4): $tecnicasTexto",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 40.dp, top = 4.dp)
                            )
                            Text(
                                text = "Perícia Básica (d6): $periciaRollTexto",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 40.dp, top = 4.dp)
                            )

                            if (periciaRoll == 1) {
                                FlowRow(
                                    modifier = Modifier.padding(start = 40.dp, top = 4.dp, end = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    periciaOpcoes.forEach { opcao ->
                                        val selected = periciasEscolhidas.contains(opcao)
                                        FilterChip(
                                            selected = selected,
                                            onClick = {
                                                periciasEscolhidas = if (selected) {
                                                    periciasEscolhidas - opcao
                                                } else if (periciasEscolhidas.size < 2) {
                                                    periciasEscolhidas + opcao
                                                } else {
                                                    periciasEscolhidas
                                                }
                                            },
                                            label = { Text(opcao) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
                                            )
                                        )
                                    }
                                }
                            } else if (periciaRoll != null) {
                                val periciaTexto = when (periciaRoll) {
                                    2 -> "Atletismo"
                                    3 -> "Conhecimento Geral"
                                    4 -> "Perceber"
                                    5 -> "Persuadir"
                                    6 -> "Furtividade"
                                    else -> "-"
                                }
                                Text(
                                    text = "Resultado: $periciaTexto",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(start = 40.dp, top = 4.dp)
                                )
                            }

                            Text(
                                text = "Vantagem (d8): $vantagemRollTexto",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 40.dp, top = 4.dp)
                            )
                            when (vantagemRoll) {
                                1, 2, 3, 4, 5, 6 -> {
                                    Text(
                                        text = "Resultado: ${vantagemOpcoes[(vantagemRoll ?: 1) - 1]}",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(start = 40.dp, top = 4.dp)
                                    )
                                }
                                7 -> {
                                    Text(
                                        text = "Resultado: Rerrole",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(start = 40.dp, top = 4.dp)
                                    )
                                }
                                8 -> {
                                    Text(
                                        text = "Resultado: escolha qualquer opção acima",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(start = 40.dp, top = 4.dp)
                                    )
                                    FlowRow(
                                        modifier = Modifier.padding(start = 40.dp, top = 4.dp, end = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        vantagemOpcoes.forEach { opcao ->
                                            val selected = vantagemEscolhida == opcao
                                            FilterChip(
                                                selected = selected,
                                                onClick = { vantagemEscolhida = opcao },
                                                label = { Text(opcao) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
                                                )
                                            )
                                        }
                                    }
                                }
                                else -> Unit
                            }

                            val qualidadeTexto = mapOf(
                                1 to "Bravura: Corajoso + Elevar a Moral.",
                                2 to "Criatividade: +1 Astúcia e rerrolagem por sessão.",
                                3 to "Solidário: Confiável + Comando.",
                                4 to "Força: +1 Força e +2 dano baseado em Força.",
                                5 to "Resiliência: +1 Resistência e +2 Absorção.",
                                6 to "Moralidade: +1 Espírito e Bene em atos altruístas.",
                                7 to "Paixão: duas perícias d6 e Bene extra.",
                                8 to "Flexibilidade: +1 Agilidade e rerrolagem por sessão.",
                                9 to "Confiança: +2 em testes ousados de Característica.",
                                10 to "Resistente: +1 Vigor e +2 contra perigos ambientais."
                            )
                            Text(
                                text = "Qualidades de Herói (d10): $qualidadeRollTexto",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 40.dp, top = 4.dp)
                            )
                            qualidadeRoll?.let { roll ->
                                Text(
                                    text = qualidadeTexto[roll].orEmpty(),
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(start = 40.dp, top = 2.dp, end = 8.dp)
                                )
                            }

                            val habilidadeTexto = mapOf(
                                1 to "Continue!: ignora penalidades até o próximo turno.",
                                2 to "Esforço Extra: modificador grátis em Técnica Chi conhecida.",
                                3 to "Defensivo: desvia ataque gastando Chi.",
                                4 to "Vantagem Adicional: escolha 1 Vantagem extra.",
                                5 to "Talismãs: escreve talismãs como um Kui.",
                                6 to "Arma Ancestral: arma com Arma Predileta Aprimorada e reserva Chi.",
                                7 to "Companheiro: ganha Senhor das Feras.",
                                8 to "Resistência ao Chi: -2 em Técnicas Chi contra você e -2 dano.",
                                9 to "Velocidade Incomum: dobra Movimentação.",
                                10 to "Arma Massiva: arma pesada e bônus de dano por Chi.",
                                11 to "Mais Uma Chance: remove um Ferimento 1x por sessão.",
                                12 to "Trilhando seu Próprio Caminho: escolha outro resultado."
                            )
                            Text(
                                text = "Habilidades (d12): $habilidadeRollTexto",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 40.dp, top = 4.dp)
                            )
                            habilidadeRoll?.let { roll ->
                                Text(
                                    text = habilidadeTexto[roll].orEmpty(),
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(start = 40.dp, top = 2.dp, end = 8.dp)
                                )
                            }
                        }

                        if (tropo.ganhaAoComprar.isNotEmpty()) {
                            Spacer(Modifier.size(6.dp))
                            FlowRow(
                                modifier = Modifier.padding(start = 36.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                vantagensNomeadas.forEach { nome ->
                                    AssistChip(
                                        onClick = {},
                                        enabled = false,
                                        label = { Text(nome) },
                                        colors = AssistChipDefaults.assistChipColors(
                                            disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
