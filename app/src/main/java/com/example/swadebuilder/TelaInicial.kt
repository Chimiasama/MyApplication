package com.example.swadebuilder

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swadebuilder.model.CriadorViewModel

@Composable
fun TelaInicial(
    onCriarNovo: (
        cartaSelvagem: Boolean,
        maisPontosPericias: Boolean,
        modoSupers: Boolean,
        compendioFantasiaAtivo: Boolean,
        compendioHorrorAtivo: Boolean,
        compendioSciFiAtivo: Boolean,
        compendioTrilhadorAtivo: Boolean,
        compendioDeadlandsAtivo: Boolean,
        modoMonstroAtivo: Boolean,
        modoSuperequipamentos: Boolean,
        modoSuperComplicacoes: Boolean,
        nasceUmHeroi: Boolean,
        heroisSemArmadura: Boolean,
        expecializacaoPer: Boolean,
        semPontosDePoder: Boolean,
        grandesResponsabilidades: Boolean,
        showHelpMessages: Boolean
    ) -> Unit,
    onCarregarPersonagem: () -> Unit,
    context: Context,
    viewModel: CriadorViewModel
) {
    // Estados do diálogo de opções iniciais
    var showNewOptionsDialog by rememberSaveable { mutableStateOf(false) }

    // Livro Básico
    var expLivroBasico by rememberSaveable { mutableStateOf(false) }
    var optCartaSelvagem by rememberSaveable { mutableStateOf(true) }
    var optMaisPontosPericias by rememberSaveable { mutableStateOf(true) }
    var optMultiAntecedenteArcano by rememberSaveable { mutableStateOf(false) }
    var optEspecializacaoPer by rememberSaveable { mutableStateOf(false) }
    var optHeroiSemArmadura by rememberSaveable { mutableStateOf(false) }
    var optMultiplosIdiomas by rememberSaveable { mutableStateOf(false) }
    var optNasceUmHeroi by rememberSaveable { mutableStateOf(false) }
    var optSemPontosPoder by rememberSaveable { mutableStateOf(false) }
    var optShowHelpMessages by rememberSaveable { mutableStateOf(false) }

    // Super
    var expSuper by rememberSaveable { mutableStateOf(false) }
    var optSuperPoderes by rememberSaveable { mutableStateOf(false) }
    var optSuperequipamentos by rememberSaveable { mutableStateOf(false) }
    var optSuperComplicacoes by rememberSaveable { mutableStateOf(false) }
    var optGrandesResponsabilidades by rememberSaveable { mutableStateOf(false) }


    // Horror
    var expHorror by rememberSaveable { mutableStateOf(false) }
    var optCompendioHorror by rememberSaveable { mutableStateOf(false) }
    var optModoMonstro by rememberSaveable { mutableStateOf(false) }

    // Fantasia
    var expFantasia by rememberSaveable { mutableStateOf(false) }
    var optCompendioFantasia by rememberSaveable { mutableStateOf(false) }
    var optCompendioTrilhador by rememberSaveable { mutableStateOf(false) }
    var expDeadlands by rememberSaveable { mutableStateOf(false) }
    var optCompendioDeadlands by rememberSaveable { mutableStateOf(false) }

    // Ficção Científica
    var expFiccao by rememberSaveable { mutableStateOf(false) }
    var optCompendioSciFi by rememberSaveable { mutableStateOf(false) }

    var showCreditsDialog by remember { mutableStateOf(false) }

    var showHelpAppDialog by rememberSaveable { mutableStateOf(false) }

    val helpAppText = """
Este app ajuda você a criar personagens de Savage Worlds passo a passo.

1) Tela Inicial
   • Escolha se o personagem é Carta Selvagem, se terá mais pontos de Perícia, modo Supers, etc.
   • Depois toque em "Criar Personagem".

2) Ordem sugerida de preenchimento
   • Ancestralidade → define bônus e limites de atributos/perícias.
   • Atributos → distribua os pontos de atributo iniciais.
   • Perícias → gaste os pontos de perícia disponíveis.
   • Complicações → escolha Complicações para ganhar Pontos Bônus.
   • Vantagens, Poderes e Equipamentos → gastam os recursos gerados nas etapas anteriores.

3) Pontos Bônus (vindos de Complicações)
   • Cada Complicação Menor gera 1 Ponto Bônus.
   • Cada Complicação Maior gera 2 Pontos Bônus.
   • O contador em "Complicações" mostra quantos Pontos Bônus você ainda tem livres.
   • Esses Pontos Bônus podem ser usados em Atributos, Perícias, Vantagens ou Recursos,
     através dos botões específicos em cada seção.

4) Ajustes e devoluções
   • Se você usou Pontos Bônus em Atributos/Perícias/Vantagens/Recursos e quiser desfazer,
     use as opções de "desfazer Pontos Bônus" nas seções correspondentes.
   • Se ainda houver Pontos Bônus em uso, algumas Complicações não poderão ser removidas:
     primeiro desfaça os pontos comprados com elas.

5) Dicas gerais
   • Toque no título de cada seção para expandir/fechar.
   • Use a opção "Lista Completa" para ler textos mais longos direto no app.
   • Quando terminar, use o botão de imprimir (ícone de impressora).
""".trimIndent()


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            tonalElevation = 4.dp,
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Construa seu personagem",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Escolha o tipo de aventura e os livros que vão guiar a criação.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Button(
                    onClick = { showNewOptionsDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Criar novo personagem")
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Quer retomar um herói?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Carregue um personagem salvo para continuar de onde parou.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        TextButton(
                            onClick = onCarregarPersonagem,
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Carregar personagem")
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Divider(modifier = Modifier.padding(horizontal = 48.dp))
            TextButton(onClick = { showHelpAppDialog = true }) {
                Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.alpha(0.7f))
                Spacer(Modifier.width(6.dp))
                Text("Guia rápido do app", modifier = Modifier.alpha(0.7f))
            }
            TextButton(onClick = { showCreditsDialog = true }) {
                Text("Créditos e licença", modifier = Modifier.alpha(0.6f))
            }
        }

        // --- Diálogo com a imagem e o texto ---
        if (showCreditsDialog) {
            AlertDialog(
                onDismissRequest = { showCreditsDialog = false },
                confirmButton = {
                    TextButton(onClick = { showCreditsDialog = false }) {
                        Text("Fechar")
                    }
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.sw_fan_logo),
                            contentDescription = "Savage Worlds Fan Logo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = """
Este jogo faz referência ao sistema de regras Savage Worlds, disponibilizado mundialmente pela Pinnacle Entertainment Group (www.peginc.com) e no Brasil pela RetroPunk Publicações (www.retropunk.net). 

Savage Worlds e todas as suas logos e marcas associadas são de propriedade da Pinnacle Entertainment Group. Utilizadas com permissão. A Pinnacle e a RetroPunk não fazem nenhuma representação ou garantia quanto à qualidade, viabilidade ou adequação em relação a este produto.

Feito por Rafael S.W.
                        """.trimIndent(),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Justify,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            )
        }

    }

    // ── Diálogo de configurações iniciais ────────────────────────────────────────
    if (showNewOptionsDialog) {
        AlertDialog(
            onDismissRequest = { showNewOptionsDialog = false },
            title            = { Text("Configurações Iniciais") },
            text             = {
                val scrollState = rememberScrollState()

                Column(
                    modifier = Modifier
                        .heightIn(max = 520.dp)
                        .verticalScroll(scrollState)
                ) {
                    Text(
                        text = "Selecione quais livros e variações estarão disponíveis na criação.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Livro Básico
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { expLivroBasico = !expLivroBasico }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text("Livro Básico", fontWeight = FontWeight.Bold)
                        Icon(
                            imageVector = if (expLivroBasico) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = stringResource(id = if (expLivroBasico) R.string.cd_collapse else R.string.cd_expand)
                        )
                    }
                    if (expLivroBasico) {
                        Spacer(Modifier.height(4.dp))

                        OptionRow(
                            checked = optCartaSelvagem,
                            title = "Carta Selvagem",
                            description = "Personagens com Benes extras e resistência heroica.",
                            emphasis = true,
                            onToggle = { optCartaSelvagem = !optCartaSelvagem }
                        )
                        OptionRow(
                            checked = optMaisPontosPericias,
                            title = "Mais pontos de perícias",
                            description = "Distribua mais pontos iniciais para customizar o herói.",
                            emphasis = true,
                            onToggle = { optMaisPontosPericias = !optMaisPontosPericias }
                        )
                        OptionRow(
                            checked = optMultiAntecedenteArcano,
                            title = "Múltiplos Antecedentes Arcanos",
                            description = "Permite combinar diferentes fontes de poder.",
                            onToggle = { optMultiAntecedenteArcano = !optMultiAntecedenteArcano }
                        )
                        OptionRow(
                            checked = optEspecializacaoPer,
                            title = "Especialização de Perícias",
                            description = "Ativa a regra opcional de especializações.",
                            onToggle = { optEspecializacaoPer = !optEspecializacaoPer }
                        )
                        OptionRow(
                            checked = optHeroiSemArmadura,
                            title = "Heróis sem Armadura",
                            description = "Crie personagens destemidos que não usam proteção pesada.",
                            onToggle = { optHeroiSemArmadura = !optHeroiSemArmadura }
                        )
                        OptionRow(
                            checked = optMultiplosIdiomas,
                            title = "Múltiplos Idiomas",
                            description = "Comece falando mais de um idioma.",
                            onToggle = { optMultiplosIdiomas = !optMultiplosIdiomas }
                        )
                        OptionRow(
                            checked = optNasceUmHeroi,
                            title = "Nasce um Herói",
                            description = "Seu personagem não pode morrer na criação.",
                            onToggle = { optNasceUmHeroi = !optNasceUmHeroi }
                        )
                        OptionRow(
                            checked = optSemPontosPoder,
                            title = "Sem pontos de Poder",
                            description = "Remove o custo de Pontos de Poder para poderes.",
                            onToggle = { optSemPontosPoder = !optSemPontosPoder }
                        )
                        OptionRow(
                            checked = optShowHelpMessages,
                            title = "Mostrar mensagens de auxílio",
                            description = "Sugestões rápidas aparecem enquanto você cria o personagem.",
                            onToggle = { optShowHelpMessages = !optShowHelpMessages }
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    if (showHelpAppDialog) {
                        AlertDialog(
                            onDismissRequest = { showHelpAppDialog = false },
                            confirmButton = {
                                TextButton(onClick = { showHelpAppDialog = false }) {
                                    Text("OK")
                                }
                            },
                            title = { Text("Como usar o app") },
                            text = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 400.dp)
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    Text(helpAppText)
                                }
                            }
                        )
                    }

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { expSuper = !expSuper }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text("Super", fontWeight = FontWeight.Bold)
                        Icon(
                            imageVector = if (expSuper) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = stringResource(id = if (expSuper) R.string.cd_collapse else R.string.cd_expand)
                        )
                    }
                    if (expSuper) {
                        Spacer(Modifier.height(4.dp))

                        OptionRow(
                            checked = optSuperPoderes,
                            title = "Superpoderes",
                            description = "Ativa vantagens e poderes de heróis super-humanos.",
                            emphasis = true,
                            onToggle = { optSuperPoderes = !optSuperPoderes }
                        )
                        OptionRow(
                            checked = optGrandesResponsabilidades,
                            title = "Grandes Responsabilidades",
                            description = "Personagens com superpoderes começam com Débitos adicionais.",
                            onToggle = { optGrandesResponsabilidades = !optGrandesResponsabilidades }
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { expHorror = !expHorror }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text("Horror", fontWeight = FontWeight.Bold)
                        Icon(
                            imageVector = if (expHorror) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = stringResource(id = if (expHorror) R.string.cd_collapse else R.string.cd_expand)
                        )
                    }
                    if (expHorror) {
                        Spacer(Modifier.height(4.dp))

                        OptionRow(
                            checked = optCompendioHorror,
                            title = "Compêndio de Horror",
                            description = "Climas sombrios, medos e criaturas aterrorizantes.",
                            emphasis = true,
                            onToggle = { optCompendioHorror = !optCompendioHorror }
                        )
                        OptionRow(
                            checked = optModoMonstro,
                            title = "Monstros Heróis",
                            description = "Crie personagens usando modelos de criatura.",
                            emphasis = true,
                            onToggle = { optModoMonstro = !optModoMonstro }
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { expFantasia = !expFantasia }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text("Fantasia", fontWeight = FontWeight.Bold)
                        Icon(
                            imageVector = if (expFantasia) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = stringResource(id = if (expFantasia) R.string.cd_collapse else R.string.cd_expand)
                        )
                    }
                    if (expFantasia) {
                        Spacer(Modifier.height(4.dp))

                        OptionRow(
                            checked = optCompendioFantasia,
                            title = "Compêndio de Fantasia",
                            description = "Elfos, magia e itens maravilhosos.",
                            emphasis = true,
                            onToggle = { optCompendioFantasia = !optCompendioFantasia }
                        )

                        OptionRow(
                            checked = optCompendioTrilhador,
                            title = "Savage Pathfinder (Trilhador)",
                            description = "Regras oficiais para aventuras em Golarion.",
                            emphasis = true,
                            onToggle = { optCompendioTrilhador = !optCompendioTrilhador }
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { expFiccao = !expFiccao }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text("Ficção Científica", fontWeight = FontWeight.Bold)
                        Icon(
                            imageVector = if (expFiccao) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = stringResource(id = if (expFiccao) R.string.cd_collapse else R.string.cd_expand)
                        )
                    }
                    if (expFiccao) {
                        Spacer(Modifier.height(4.dp))

                        OptionRow(
                            checked = optCompendioSciFi,
                            title = "Compêndio de Sci-Fi",
                            description = "Naves, tecnologias exóticas e mutações futuristas.",
                            emphasis = true,
                            onToggle = { optCompendioSciFi = !optCompendioSciFi }
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { expDeadlands = !expDeadlands }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Deadlands: O Oeste Estranho", fontWeight = FontWeight.Bold)
                        Icon(
                            imageVector = if (expDeadlands) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = stringResource(id = if (expDeadlands) R.string.cd_collapse else R.string.cd_expand)
                        )
                    }
                    if (expDeadlands) {
                        Spacer(Modifier.height(4.dp))

                        OptionRow(
                            checked = optCompendioDeadlands,
                            title = "Ativar conteúdo de Deadlands",
                            description = "Harroweds, dispositivos infernais e o Velho Oeste sombrio.",
                            emphasis = true,
                            onToggle = { optCompendioDeadlands = !optCompendioDeadlands }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {

                    onCriarNovo(
                        optCartaSelvagem,
                        optMaisPontosPericias,
                        optSuperPoderes,
                        optCompendioFantasia,
                        optCompendioHorror,
                        optCompendioSciFi,
                        optCompendioTrilhador,
                        optCompendioDeadlands,
                        optModoMonstro,
                        optSuperequipamentos,
                        optSuperComplicacoes,
                        optNasceUmHeroi,
                        optHeroiSemArmadura,
                        optEspecializacaoPer,
                        optSemPontosPoder,
                        optGrandesResponsabilidades,
                        optShowHelpMessages
                    )
                    viewModel.state.compendioTrilhadorAtivo = optCompendioTrilhador
                    viewModel.state.compendioDeadlandsAtivo = optCompendioDeadlands

                    viewModel.state.permiteMultiAntecedenteArcano = optMultiAntecedenteArcano
                    viewModel.state.regraMultiplosIdiomas = optMultiplosIdiomas

                }) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewOptionsDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun OptionRow(
    checked: Boolean,
    title: String,
    description: String? = null,
    emphasis: Boolean = false,
    onToggle: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = 6.dp)
    ) {
        Checkbox(checked = checked, onCheckedChange = { onToggle() })
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = if (emphasis) FontWeight.Bold else FontWeight.SemiBold
            )
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
