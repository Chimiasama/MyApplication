package com.example.swadebuilder.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.availableSectionsFor
import com.example.swadebuilder.ui.MainSection
import com.example.swadebuilder.util.AppPreferences

const val TUTORIAL_KEY_CONFIGURACOES = "CONFIGURACOES"
const val TUTORIAL_KEY_GERENCIAR_CONTEUDO = "GERENCIAR_CONTEUDO"

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
        body = "Escolha as Complicações do personagem. Cada Complicação Menor gera 1 Ponto de Complicação e cada Maior gera 2. Use esses pontos para comprar Perícias, Atributos, Vantagens e outros extras nas demais abas — mas atenção: cada compra extra feita com eles GASTA Pontos de Complicação na hora, mesmo aparecendo só como um \"+N\" na outra aba. É fácil gastar tudo sem perceber; volte aqui sempre que quiser conferir quanto ainda sobrou."
    ),
    MainSection.ATRIBUTOS to TabTutorialContent(
        title = "Aba Atributos / Perícias",
        body = "Gaste os Pontos de Atributo para aumentar os dados de Agilidade, Astúcia, Espírito, Força e Vigor, e os Pontos de Perícia para aumentar as perícias. Cada aumento de um grau no dado custa 1 ponto (perícias acima do atributo relacionado custam mais). Pontos de Complicação sobrando também podem ser usados aqui como \"PC livres\": cada aumento comprado assim GASTA Pontos de Complicação (2 por atributo, 1 por perícia) — a aba mostra abaixo do cabeçalho quanto você já usou desse jeito."
    ),
    MainSection.PERICIAS to TabTutorialContent(
        title = "Aba Perícias",
        body = "Gaste Pontos de Perícia para aumentar os dados das perícias do personagem. O custo aumenta quando a perícia fica com um dado maior que o do atributo relacionado. Perícias compradas com Pontos de Complicação (\"PC livres\") também gastam esses pontos na hora."
    ),
    MainSection.VANTAGENS to TabTutorialContent(
        title = "Aba Vantagens",
        body = "Escolha as Vantagens do personagem, respeitando os pré-requisitos de atributo, perícia, estágio (Rank) e outras vantagens. Quando disponíveis, Pontos de Complicação também podem ser usados para comprá-las (2 PC por Vantagem) — isso gasta os PC na hora, mesmo aparecendo só como pontos extra de Vantagem."
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

private val configuracoesTutorial = TabTutorialContent(
    title = "Configurações",
    body = "Aqui você ajusta as preferências do app: estilo e visual das abas, tema, vibração, som e o conteúdo customizado. O interruptor \"Instruções das Abas\", logo no topo, liga um replay completo destas instruções — ele se desliga sozinho quando você termina de ver todas de novo."
)

private val gerenciarConteudoTutorial = TabTutorialContent(
    title = "Gerenciar Conteúdo Customizado",
    body = "Aqui você cria e edita vantagens, perícias, complicações, poderes, equipamentos e outros itens personalizados, que passam a ficar disponíveis nas abas de criação junto com o conteúdo oficial."
)

/**
 * Todas as chaves de tutorial que existem no momento para o personagem atual: as abas do
 * pager disponíveis (ver [availableSectionsFor]) mais as telas que não são abas do pager
 * (Configurações e Gerenciar Conteúdo Customizado, sempre acessíveis). Usado para saber se
 * ainda falta alguma instrução a mostrar — ver o interruptor em SettingsDialog.kt.
 */
fun tutorialKeysDisponiveis(state: CriadorState): List<String> =
    availableSectionsFor(state).map { it.name } +
        listOf(TUTORIAL_KEY_CONFIGURACOES, TUTORIAL_KEY_GERENCIAR_CONTEUDO)

@Composable
private fun TutorialOverlay(
    state: CriadorState,
    key: String,
    content: TabTutorialContent
) {
    if (key in state.abasComInstrucaoVista) return

    val context = LocalContext.current
    var visible by remember(key) { mutableStateOf(true) }
    if (!visible) return

    AlertDialog(
        onDismissRequest = {
            state.abasComInstrucaoVista.add(key)
            AppPreferences.saveTutorialSeen(context, state.abasComInstrucaoVista.toSet())
            visible = false
        },
        title = { Text(content.title) },
        text = { Text(content.body) },
        confirmButton = {
            TextButton(onClick = {
                state.abasComInstrucaoVista.add(key)
                AppPreferences.saveTutorialSeen(context, state.abasComInstrucaoVista.toSet())
                visible = false
            }) {
                Text("Entendi")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                // Encerra o replay inteiro de uma vez, não só esta tela — ver o
                // interruptor "Instruções das Abas" em SettingsDialog.kt.
                state.abasComInstrucaoVista.addAll(tutorialKeysDisponiveis(state))
                AppPreferences.saveTutorialSeen(context, state.abasComInstrucaoVista.toSet())
                visible = false
            }) {
                Text("Não mostrar instruções")
            }
        }
    )
}

/**
 * Instrução de primeira visita para cada aba do criador. Aparece uma única vez por aba,
 * controlada por [CriadorState.abasComInstrucaoVista] (persistido globalmente via
 * AppPreferences) e pode ser revista a qualquer momento com o interruptor "Instruções das
 * Abas" em Configurações, ou desligada de vez no botão "Não mostrar instruções" do próprio
 * diálogo — pra não obrigar o jogador a fechar uma tela dessas em cada aba se não quiser ver.
 */
@Composable
fun TabTutorialOverlay(
    state: CriadorState,
    section: MainSection
) {
    val content = tabTutorials[section] ?: return
    TutorialOverlay(state = state, key = section.name, content = content)
}

/** Instrução de primeira visita para a tela de Configurações (ver [SettingsDialog]). */
@Composable
fun ConfiguracoesTutorialOverlay(state: CriadorState) {
    TutorialOverlay(state = state, key = TUTORIAL_KEY_CONFIGURACOES, content = configuracoesTutorial)
}

/** Instrução de primeira visita para o diálogo de Gerenciar Conteúdo Customizado. */
@Composable
fun GerenciarConteudoTutorialOverlay(state: CriadorState) {
    TutorialOverlay(state = state, key = TUTORIAL_KEY_GERENCIAR_CONTEUDO, content = gerenciarConteudoTutorial)
}
