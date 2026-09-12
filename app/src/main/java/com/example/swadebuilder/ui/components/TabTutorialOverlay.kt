package com.example.swadebuilder.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.ui.MainSection

private data class TabTutorialContent(
    val title: String,
    val body: String
)

private val tabTutorials: Map<MainSection, TabTutorialContent> = mapOf(
    MainSection.RESUMO to TabTutorialContent(
        title = "Aba Resumo",
        body = "Aqui você vê o resumo completo da ficha: atributos, perícias, vantagens, complicações, poderes e equipamentos escolhidos. Toque no retrato para colocar uma foto e no campo de nome para identificar o personagem."
    ),
    MainSection.ANCESTRALIDADES to TabTutorialContent(
        title = "Aba Ancestralidades",
        body = "Escolha aqui a raça/ancestralidade do personagem. Cada uma concede traços, bônus e penalidades próprios, e pode liberar variantes com opções adicionais."
    ),
    MainSection.TROPOS to TabTutorialContent(
        title = "Aba Tropos",
        body = "Selecione o Topo (estilo de combate) do personagem, usado pelas regras do compêndio Arte da Guerra."
    ),
    MainSection.MONSTRO to TabTutorialContent(
        title = "Aba Monstro",
        body = "Defina o tipo de monstro e os traços especiais dele, ao criar uma criatura em vez de um personagem comum."
    ),
    MainSection.COMPLICACOES to TabTutorialContent(
        title = "Aba Complicações",
        body = "Escolha as Complicações do personagem. Cada Complicação Menor gera 1 Ponto de Complicação e cada Maior gera 2. Use esses pontos para comprar Perícias, Atributos, Vantagens e outros extras nas demais abas."
    ),
    MainSection.ATRIBUTOS to TabTutorialContent(
        title = "Aba Atributos / Perícias",
        body = "Gaste os Pontos de Atributo para aumentar os dados de Agilidade, Astúcia, Espírito, Força e Vigor, e os Pontos de Perícia para aumentar as perícias. Cada aumento de um grau no dado custa 1 ponto (perícias acima do atributo relacionado custam mais). Pontos de Complicação sobrando também podem ser usados aqui."
    ),
    MainSection.PERICIAS to TabTutorialContent(
        title = "Aba Perícias",
        body = "Gaste Pontos de Perícia para aumentar os dados das perícias do personagem. O custo aumenta quando a perícia fica com um dado maior que o do atributo relacionado."
    ),
    MainSection.VANTAGENS to TabTutorialContent(
        title = "Aba Vantagens",
        body = "Escolha as Vantagens do personagem, respeitando os pré-requisitos de atributo, perícia, estágio (Rank) e outras vantagens. Quando disponíveis, Pontos de Complicação também podem ser usados para comprá-las."
    ),
    MainSection.EQUIPAMENTOS to TabTutorialContent(
        title = "Aba Equipamentos",
        body = "Compre armas, armaduras e outros itens usando o Dinheiro ou a Requisição do personagem. Pontos de Complicação não usados também podem ser convertidos em recursos extras aqui."
    ),
    MainSection.PODERES to TabTutorialContent(
        title = "Aba Poderes",
        body = "Selecione os Poderes disponíveis para o Antecedente Arcano do personagem, ou as Técnicas do Topo, quando aplicável."
    ),
    MainSection.XP to TabTutorialContent(
        title = "Aba XP",
        body = "Use os Pontos de Experiência ganhos em jogo para evoluir o personagem: aumentar atributos e perícias, ou comprar novas vantagens e poderes."
    ),
    MainSection.CRYSTAL_HEART to TabTutorialContent(
        title = "Aba Crystal Heart",
        body = "Gerencie o Crystal Heart do personagem, um recurso especial deste compêndio."
    ),
    MainSection.MECHAS to TabTutorialContent(
        title = "Aba Mechas",
        body = "Configure o mecha do personagem, incluindo os slots de armadura energizada e os equipamentos específicos dele."
    ),
    MainSection.CIBERNETICOS to TabTutorialContent(
        title = "Aba Cibernéticos",
        body = "Instale implantes cibernéticos no personagem, respeitando o limite de Tensão disponível."
    )
)

/**
 * Instrução de primeira visita para cada aba do criador. Aparece uma única vez por aba
 * (controlado por [CriadorState.abasComInstrucaoVista], persistido globalmente em
 * AppPreferences) e pode ser desligada de vez em Configurações ou direto no botão
 * "Não mostrar instruções" do próprio diálogo — pra não obrigar o jogador a fechar
 * uma tela dessas em cada aba se ele não quiser ver.
 */
@Composable
fun TabTutorialOverlay(
    state: CriadorState,
    section: MainSection,
    onDismissed: () -> Unit = {}
) {
    if (!state.instrucoesAbasAtivas) return
    if (section in state.abasComInstrucaoVista) return
    val content = tabTutorials[section] ?: return

    var visible by remember(section) { mutableStateOf(true) }
    if (!visible) return

    fun markSeen() {
        state.abasComInstrucaoVista.add(section)
        visible = false
        onDismissed()
    }

    AlertDialog(
        onDismissRequest = { markSeen() },
        title = { Text(content.title) },
        text = { Text(content.body) },
        confirmButton = {
            TextButton(onClick = { markSeen() }) {
                Text("Entendi")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                state.instrucoesAbasAtivas = false
                markSeen()
            }) {
                Text("Não mostrar instruções")
            }
        }
    )
}
