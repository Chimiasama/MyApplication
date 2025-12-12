package com.example.swadebuilder.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.ui.theme.AppTheme

@Composable
fun AjudaDialog(
    state: CriadorState,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Instruções de Uso") },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
            ) {
                // Seção Básico (Sempre visível)
                InstructionSection(
                    title = "Básico",
                    content = """
                        1. Distribua seus pontos iniciais: 5 pontos para Atributos e 15 pontos para Perícias.
                        2. Escolha uma Ancestralidade (Básica ou outra se disponível).
                        3. Adicione Complicações (Menores valem 1 ponto, Maiores valem 2). Você pode ganhar até 4 pontos de Benefício (PB) para gastar em Vantagens, Perícias ou Atributos.
                        4. Clique em "Vantagens" para comprar vantagens usando PB.
                        5. Use a aba "Equipamentos" para comprar itens iniciais com seu dinheiro ($500 base).
                        6. Quando terminar a criação básica, clique em "Iniciar Progressão" para gastar XP e subir de posto.
                    """.trimIndent()
                )

                // Seção Fantasia
                if (state.compendioFantasiaAtivo) {
                    InstructionSection(
                        title = "Fantasia",
                        content = """
                            • Habilita Ancestralidades, Vantagens e Equipamentos do Compêndio de Fantasia.
                            • Procure por itens marcados com origem "FANTASIA" nas listas.
                            • Equipamentos de Fantasia aparecem em uma seção dedicada na aba de Equipamentos.
                        """.trimIndent()
                    )
                }

                // Seção Horror
                if (state.compendioHorrorAtivo) {
                    InstructionSection(
                        title = "Horror",
                        content = """
                            • Habilita Vantagens Monstruosas e itens do Compêndio de Horror.
                            • Permite selecionar modelos de monstros se o modo "Monstro Herói" estiver ativo.
                            • Equipamentos de caça a monstros disponíveis na aba de Equipamentos.
                        """.trimIndent()
                    )
                }

                // Seção Deadlands
                if (state.compendioDeadlandsAtivo) {
                    InstructionSection(
                        title = "Deadlands: O Oeste Estranho",
                        content = """
                            • Habilita Ancestralidades, Vantagens e Equipamentos de Deadlands.
                            • Mecânica de Ressuscitado (Harrowed) e Antecedentes Arcanos específicos (Abençoado, Cientista Louco, etc).
                            • Equipamentos como Armas de Pedra Fantasma e Dispositivos Infernais.
                        """.trimIndent()
                    )
                }

                // Seção Supers e Poderes
                if (state.modoSupers) {
                    InstructionSection(
                        title = "Superpoderes",
                        content = """
                            • Habilita a aba de Superpoderes.
                            • Distribua seus Pontos de Poder (PP) comprando poderes e aprimoramentos.
                            • O "Limite de Poder" restringe o gasto máximo em um único poder.
                            • Vantagens de origem "SUPER" estão disponíveis.
                        """.trimIndent()
                    )
                } else {
                    InstructionSection(
                        title = "Poderes",
                        content = """
                            Se o personagem possuir um Antecedente Arcano, a seção de poderes fica disponível:
                            • Escolha sua tradição/arcano.
                            • Selecione poderes nos espaços disponíveis.
                            • O app controla quantos você pode pegar e evita ultrapassar o limite.
                            • Se você remover o Antecedente Arcano, poderes que dependem dele são limpos automaticamente.
                        """.trimIndent()
                    )
                }

                // Placeholder Ficção Científica (Baseado no tema por enquanto, já que não há modo ativo)
                if (state.appTheme == AppTheme.SCIFI || state.appTheme == AppTheme.CYBERPUNK) {
                    InstructionSection(
                        title = "Ficção Científica",
                        content = "Módulo de Ficção Científica (Em desenvolvimento)."
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Entendi")
            }
        }
    )
}

@Composable
private fun InstructionSection(
    title: String,
    content: String
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
