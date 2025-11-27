package com.example.swadebuilder

import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swadebuilder.model.CriadorViewModel
import com.example.swadebuilder.model.PersonagemSalvo
import com.example.swadebuilder.model.StorageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun TelaInicial(
    onCriarNovo: (
        cartaSelvagem: Boolean,
        maisPontosPericias: Boolean,
        modoSupers: Boolean,
        modoSuperequipamentos: Boolean,
        modoSuperComplicacoes: Boolean,
        nasceUmHeroi: Boolean,
        heroisSemArmadura: Boolean,
        expecializacaoPer: Boolean,
        semPontosDePoder: Boolean,
        grandesResponsabilidades: Boolean,
        showHelpMessages: Boolean
    ) -> Unit,
    onLoad: (PersonagemSalvo) -> Unit,
    context: Context,
    viewModel: CriadorViewModel
) {
    var showLoadDialog by rememberSaveable { mutableStateOf(false) }

    // CORREÇÃO: Inicializa como lista vazia e carrega em background
    var nomesSalvos by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var pendingDelete by rememberSaveable { mutableStateOf<Pair<String, String>?>(null) }

    // CORREÇÃO: Adiciona um CoroutineScope
    val scope = rememberCoroutineScope()

    // CORREÇÃO: Carrega a lista inicial de forma assíncrona
    LaunchedEffect(Unit) {
        nomesSalvos = withContext(Dispatchers.IO) {
            StorageUtils.listarPersonagens(context)
        }
    }

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

    // Fantasia
    var expFantasia by rememberSaveable { mutableStateOf(false) }

    // Ficção Científica
    var expFiccao by rememberSaveable { mutableStateOf(false) }

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
   • Quando terminar, use o botão de salvar (ícone de disquete) ou de imprimir (ícone de impressora).
""".trimIndent()


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = { showNewOptionsDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Criar Novo Personagem")
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                // CORREÇÃO: Atualiza a lista em background antes de mostrar
                scope.launch(Dispatchers.IO) {
                    val listaAtualizada = StorageUtils.listarPersonagens(context)
                    withContext(Dispatchers.Main) {
                        nomesSalvos = listaAtualizada // Atualiza o state na Main thread
                        if (listaAtualizada.isEmpty()) {
                            Toast.makeText(context, "Nenhum personagem salvo.", Toast.LENGTH_SHORT).show()
                        } else {
                            showLoadDialog = true
                        }
                    }
                }
            },
            enabled = nomesSalvos.isNotEmpty(), // <-- CORRIGIDO
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Carregar Personagem Salvo")
        }
        Spacer(modifier = Modifier.height(240.dp))

        Button(
            onClick = { showCreditsDialog = true },
            modifier = Modifier.fillMaxWidth()
                .padding(bottom = 240.dp)
                .alpha(0.6f)
        ) {
            Text("Créditos e Licença")
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
                Column {
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
                            contentDescription = null
                        )
                    }
                    if (expLivroBasico) {
                        Spacer(Modifier.height(4.dp))

                        // Carta Selvagem (NEGRITO)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { optCartaSelvagem = !optCartaSelvagem }
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = optCartaSelvagem,
                                onCheckedChange = { optCartaSelvagem = it }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Carta Selvagem", fontWeight = FontWeight.Bold)
                        }

                        // Mais pontos de perícias (NEGRITO)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { optMaisPontosPericias = !optMaisPontosPericias }
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = optMaisPontosPericias,
                                onCheckedChange = { optMaisPontosPericias = it }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Mais pontos de perícias", fontWeight = FontWeight.Bold)
                        }

                        // (mantém as demais opções do Livro Básico como já existiam)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { optMultiAntecedenteArcano = !optMultiAntecedenteArcano }
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(checked = optMultiAntecedenteArcano, onCheckedChange = { optMultiAntecedenteArcano = it })
                            Spacer(Modifier.width(8.dp))
                            Text("Múltiplos Antecedentes Arcanos")
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { optEspecializacaoPer = !optEspecializacaoPer }
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(checked = optEspecializacaoPer, onCheckedChange = { optEspecializacaoPer = it })
                            Spacer(Modifier.width(8.dp))
                            Text("Especialização de Perícias")
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { optHeroiSemArmadura = !optHeroiSemArmadura }
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(checked = optHeroiSemArmadura, onCheckedChange = { optHeroiSemArmadura = it })
                            Spacer(Modifier.width(8.dp))
                            Text("Heróis sem Armadura")
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { optMultiplosIdiomas = !optMultiplosIdiomas }
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(checked = optMultiplosIdiomas, onCheckedChange = { optMultiplosIdiomas = it })
                            Spacer(Modifier.width(8.dp))
                            Text("Múltiplos Idiomas")
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { optNasceUmHeroi = !optNasceUmHeroi }
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(checked = optNasceUmHeroi, onCheckedChange = { optNasceUmHeroi = it })
                            Spacer(Modifier.width(8.dp))
                            Text("Nasce um Herói")
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { optSemPontosPoder = !optSemPontosPoder }
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(checked = optSemPontosPoder, onCheckedChange = { optSemPontosPoder = it })
                            Spacer(Modifier.width(8.dp))
                            Text("Sem pontos de Poder")
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { optShowHelpMessages = !optShowHelpMessages }
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(checked = optShowHelpMessages, onCheckedChange = { optShowHelpMessages = it })
                            Spacer(Modifier.width(8.dp))
                            Text("Mostrar mensagens de auxílio")
                        }
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
                            contentDescription = null
                        )
                    }
                    if (expSuper) {
                        Spacer(Modifier.height(4.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { optSuperPoderes = !optSuperPoderes }
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = optSuperPoderes,
                                onCheckedChange = { optSuperPoderes = it }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Superpoderes", fontWeight = FontWeight.Bold)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { optGrandesResponsabilidades = !optGrandesResponsabilidades }
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = optGrandesResponsabilidades,
                                onCheckedChange = { optGrandesResponsabilidades = it }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Grandes Responsabilidades")
                        }
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
                            contentDescription = null
                        )
                    }
                    if (expHorror) {
                        Text("— sem opções por enquanto —")
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
                            contentDescription = null
                        )
                    }
                    if (expFantasia) {
                        Text("— sem opções por enquanto —")
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
                            contentDescription = null
                        )
                    }
                    if (expFiccao) {
                        Text("— sem opções por enquanto —")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {

                    onCriarNovo(
                        optCartaSelvagem,
                        optMaisPontosPericias,
                        optSuperPoderes,
                        optSuperequipamentos,
                        optSuperComplicacoes,
                        optNasceUmHeroi,
                        optHeroiSemArmadura,
                        optEspecializacaoPer,
                        optSemPontosPoder,
                        optGrandesResponsabilidades,
                        optShowHelpMessages
                    )

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

    if (showLoadDialog) {
        AlertDialog(
            onDismissRequest = {
                showLoadDialog = false
                pendingDelete = null
            },
            title = { Text("Selecione um personagem") },
            text = {
                LazyColumn {
                    items(nomesSalvos) { (displayName, fileKey) ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    scope.launch(Dispatchers.IO) {
                                        val salvo = StorageUtils.carregarPersonagem(context, fileKey)
                                        withContext(Dispatchers.Main) {
                                            salvo?.let {
                                                showLoadDialog = false
                                                pendingDelete = null
                                                onLoad(it)
                                            }
                                        }
                                    }
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(displayName, Modifier.weight(1f))
                            IconButton(onClick = { pendingDelete = displayName to fileKey }) {
                                Icon(Icons.Default.Delete, contentDescription = "Deletar")
                            }
                        }
                        HorizontalDivider()
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = {
                        showLoadDialog = false
                        pendingDelete = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (pendingDelete != null) {
        val (displayName, fileKey) = pendingDelete!!
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Confirmar exclusão") },
            text  = { Text("Deseja realmente excluir \"$displayName\"?") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch(Dispatchers.IO) {
                        StorageUtils.deletarPersonagem(context, fileKey)
                        val listaAtualizada = StorageUtils.listarPersonagens(context)
                        withContext(Dispatchers.Main) {
                            nomesSalvos = listaAtualizada
                            pendingDelete = null
                        }
                    }
                }) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}