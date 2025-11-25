@file:OptIn(
    ExperimentalMaterial3Api::class
)
@file:Suppress("LanguageDetectionInspection")

package com.example.swadebuilder

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.annotation.RawRes
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.swadebuilder.model.AtributoList
import com.example.swadebuilder.model.Complicacao
import com.example.swadebuilder.model.CriadorViewModel
import com.example.swadebuilder.model.EquipamentoCategoria
import com.example.swadebuilder.model.EquipamentoItem
import com.example.swadebuilder.model.PericiaList
import com.example.swadebuilder.model.PersonagemSalvo
import com.example.swadebuilder.model.RacialModifier
import com.example.swadebuilder.model.StorageUtils
import com.example.swadebuilder.model.DataRepository
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.model.loadPericiasDescriptions
import com.example.swadebuilder.ui.components.SectionCard
import com.example.swadebuilder.ui.sections.AncestralidadesDetailScreen
import com.example.swadebuilder.ui.sections.AtributosDetailScreen
import com.example.swadebuilder.ui.sections.ComplicacoesDetailScreen
import com.example.swadebuilder.ui.sections.EquipamentosDetailScreen
import com.example.swadebuilder.ui.sections.PericiasDetailScreen
import com.example.swadebuilder.ui.sections.PoderesDetailScreen
import com.example.swadebuilder.ui.sections.SuperPoderesDetailScreen
import com.example.swadebuilder.ui.sections.VantagensDetailScreen
import com.example.swadebuilder.ui.theme.SWADEbuilderTheme
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.semAcentos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.UUID


@Serializable
data class ArcanoInfo(
    val key: String,
    val slots: Int,
    val pp: Int,
    val foco: String
)

lateinit var arcanoInfo: Map<String, Triple<Int, Int, String>>

data class PurchasedPower(
    val nome: String,
    val custo: Int,
    val baseCost: Int,
    val poderId: String
)

// ===== NOVO: Superpoderes Restritivos (pré-requisito de perícia mínima) =====
const val MIN_RAW_RESTRITIVO = 10  // d10

// chave do superpoder (keyify) -> chave da perícia (keyify)
val SUPER_PODERES_RESTRITIVOS: Map<String, String> = mapOf(
    "Superfeitiçaria".keyify() to "Ocultismo".keyify(),
    "Superciência".keyify()    to "Ciência".keyify()
)

private val json = Json {
    ignoreUnknownKeys = true
}

private const val MULTIPLOS_AA_HABILITADOS: Boolean = false

class MainActivity : ComponentActivity() {
    private lateinit var dataRepository: DataRepository

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    @OptIn(ExperimentalMaterial3Api::class)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        dataRepository = DataRepository(applicationContext)

        val allEquipCategorias: List<EquipamentoCategoria> = dataRepository.loadEquipamentoCategorias()

        val equipamentoCategorias = allEquipCategorias.filter { cat ->
            cat.origem?.equals("super", ignoreCase = true)?.not() ?: true
        }
        val superequipCategorias = allEquipCategorias.filter { cat ->
            cat.origem?.equals("super", ignoreCase = true) ?: false
        }

        val listaSuperPoderes: List<SuperPoder> = dataRepository.loadSuperPoderes()

        val arcanoList: List<ArcanoInfo> = dataRepository.loadArcanoInfo()
        arcanoInfo = arcanoList.associate {
            it.key
                .uppercase()
                .semAcentos()
                .trim() to Triple(it.slots, it.pp, it.foco)
        }

        val atributosData = dataRepository.loadAtributos()
        listaAtributos = atributosData.atributos
            .map { it.nome.keyify() }
        mapaAtributosDisplay = atributosData.atributos
            .associate { it.nome.keyify() to it.nome }

        listaPericias = dataRepository.loadPericias()

        val todasVantagens: List<Vantagem> = dataRepository.loadVantagens()

        AppData.basicasVantagens          = todasVantagens.filter { it.origem.equals("BASICO", true) }
        AppData.superVantagens = todasVantagens.filter {
            it.origem.equals("SUPER", ignoreCase = true)
        }

        listaVantagens = todasVantagens

        AppData.superVantagensParaDetalhe = AppData.superVantagens

        listaComplicacoes = dataRepository.loadComplicacoes()

        listaAncestralidadesJson = dataRepository.loadRacialModifiers()

        racialAttrMinMap = listaAncestralidadesJson.associate { rm ->
            val m = rm.atributos
                .mapKeys   { it.key.keyify() }
                .mapValues { 4 + it.value }
            rm.nome.keyify() to m
        }

        racialSkillStartMap = listaAncestralidadesJson.associate { rm ->
            val m = rm.pericias
                .mapKeys   { it.key.keyify() }
                .mapValues { 4 + it.value }
            rm.nome.keyify() to m
        }

        setContent {
            val criadorViewModel: CriadorViewModel = viewModel()
            criadorViewModel.setMultiplosAAHabilitados(MULTIPLOS_AA_HABILITADOS)
            val state = criadorViewModel.state

            // ✅ cada vez que você entra em um personagem novo/carregado,
            //    incrementamos creationSession para zerar as seções/telas.
            var creationSession by rememberSaveable { mutableIntStateOf(0) }

            var expInfos   by rememberSaveable(creationSession) { mutableStateOf(false) }
            var expAncs    by rememberSaveable(creationSession) { mutableStateOf(false) }
            var expComps   by rememberSaveable(creationSession) { mutableStateOf(false) }
            var expEquip   by rememberSaveable(creationSession) { mutableStateOf(false) }

            var expAttrs   by rememberSaveable(creationSession) { mutableStateOf(false) }
            var expPer     by rememberSaveable(creationSession) { mutableStateOf(false) }
            var expVants   by rememberSaveable(creationSession) { mutableStateOf(false) }
            var expPoderes by rememberSaveable(creationSession) { mutableStateOf(false) }
            var expResumo  by rememberSaveable(creationSession) { mutableStateOf(false) }

            var showVantagensDetail       by rememberSaveable(creationSession) { mutableStateOf(false) }
            var showPericiasDetail        by rememberSaveable(creationSession) { mutableStateOf(false) }
            var showComplicacoesDetail    by rememberSaveable(creationSession) { mutableStateOf(false) }
            var showAtributosDetail       by rememberSaveable(creationSession) { mutableStateOf(false) }
            var showAncestralidadesDetail by rememberSaveable(creationSession) { mutableStateOf(false) }
            var showPoderesDetail         by rememberSaveable(creationSession) { mutableStateOf(false) }
            var showEquipLista            by rememberSaveable(creationSession) { mutableStateOf(false) }
            var showSuperDetail           by rememberSaveable(creationSession) { mutableStateOf(false) }

            var highlightedVantagem   by rememberSaveable(creationSession) { mutableStateOf("") }
            var highlightedSuperPoder by rememberSaveable(creationSession) { mutableStateOf("") }

            val context = LocalContext.current
            val activity = (context as? ComponentActivity)
            var mostrouTelaInicial by rememberSaveable { mutableStateOf(true) }
            var showExitDialog     by rememberSaveable { mutableStateOf(false) }

            var showHelpAppDialog by rememberSaveable { mutableStateOf(false) }

            val helpAppText = """
Como usar o app

Este app guia você na criação de personagem para Savage Worlds Edição Aventura (SWADE), seguindo o passo a passo padrão do livro básico. A ideia é você distribuir pontos, escolher opções e, no final, salvar ou imprimir sua ficha.
Os conteúdos textuais foram omitidos para proteger os direitos do conteúdo intelectual. Para acessar as informações você deve ver os livros.

1) Começando (Tela Inicial)

Na tela inicial defina se você usa alguma regra de ambientação, outros livros de referência etc. Carta Selvagem e Mais Pontos de Perícia estão habilitados por padrão. Depois toque em Criar Personagem para ir para o preenchimento. 

Dica: se você já tem um personagem salvo, use Carregar para continuar a partir de onde parou.

2) Ordem sugerida de preenchimento

A ordem abaixo evita retrabalho, porque algumas escolhas afetam limites e pontos:

Ancestralidade

Complicações

Atributos

Perícias

Vantagens / Poderes (ou Superpoderes) / Equipamentos

Resumo final

Essa também é a ordem de disposição das seções do app. A seção de Informações é para detalhes adicionais.

3) Ancestralidade

Escolha primeiro a ancestralidade. Ela pode:

ajustar valores mínimos ou máximos de atributos e perícias;

conceder bônus iniciais gratuitos;

impor penalidades ou limites especiais.

Automático: quando você troca a ancestralidade, o app recalcula limites e:

ajusta atributos para respeitar os novos mínimos/máximos;

reduz perícias que tenham passado do novo limite, devolvendo pontos ao pool quando necessário;

mantém as perícias básicas com mínimo de d4.

Ou seja, você não precisa “consertar na mão” depois da troca.

4) Complicações (ganhar Pontos Bônus)

Complicações servem pra dar cor ao personagem e gerar Pontos Bônus de Criação:

Menor = +1 ponto bônus

Maior = +2 pontos bônus

Limite padrão: até 4 pontos bônus no total.

O contador da seção mostra quantos pontos bônus você tem disponíveis. 

Esses pontos podem ser convertidos nas seções apropriadas (atributos, perícias, vantagens ou recursos). Sempre que você “gasta” pontos bônus, o app registra de onde eles vieram.

Importante:
Se você tentar remover uma complicação que ainda está “financiando” algo, o app impede e avisa. Primeiro desfaça as compras feitas com aqueles pontos.

5) Atributos (5 pontos)

Você tem 5 pontos para distribuir entre os atributos.

Regras que o app segue:

Todos começam em d4.

Cada ponto gasto aumenta um passo: d4 → d6 → d8 → d10 → d12.

O app não deixa gastar mais do que o total disponível.

Se algum bônus de ancestralidade modificar um atributo, o valor final é ajustado.

Sugestão prática: suba primeiro o(s) atributo(s) que sustentam suas perícias principais.

6) Perícias - 15 pontos (12 se não usar regra de ambientação)

Você tem 15 pontos para comprar perícias.

Regras:

Perícias básicas já começam em d4 (mínimo fixo).

Perícias não-básicas começam em - (não treinadas).

Aumento de perícia custa:

1 ponto por passo até igualar o atributo ligado;

2 pontos por passo se passar do atributo.

Você pode reduzir uma perícia não-básica de volta para - e recuperar pontos. As básicas param em d4.

Automático: se você mudar um atributo depois, o app recalcula os custos/limites das perícias que dependem dele. Se alguma perícia ficar acima do permitido pela nova combinação de atributo/ancestralidade, ela é reduzida até o máximo válido e os pontos excedentes voltam para você.

7) Vantagens

Na seção de vantagens você gasta os pontos de vantagens conforme o SWADE.
Toque no + para expandir. Cada vantagem exibe:

nome,

requisitos.

Filtros: na lista você pode filtrar por categoria, requisitos, disponibilidade etc. Isso ajuda a achar rápido o que combina com sua ideia de personagem.

8) Poderes (quando aplicável)

Se o personagem possuir um Antecedente Arcano, a seção de poderes fica disponível:

escolha sua tradição/arcano;

selecione poderes nos espaços disponíveis;

o app controla quantos você pode pegar e evita ultrapassar o limite.

Se você remover o Antecedente Arcano, poderes que dependem dele são limpos automaticamente.

8.1) Superpoderes (quando aplicável)

Em caso de campanha de Supers a seção de Superpoderes vem disponível mas só fica acessível após:
Todos pontos da criação inicial forem distribuídos,

O nível da Campanha de Super for definido.

Com estas definições é possível comprar os superpoderes e o app faz os ajustes na ficha quando for aplicável.

Caso se deseje é possível voltar para a fase inicial de criação ao remover os superpoderes adquiridos e definir o nível da campanha como 0.

9) Equipamentos e Recursos

Aqui você compra itens com o dinheiro inicial.
Você pode:

navegar por categorias,

usar filtros e busca para achar equipamento pelo tipo/nome,

adicionar/remover itens e ver o dinheiro restante atualizar ao vivo.

Esta seção nunca é bloqueada independetemente da fase de criação que o personagem está.

10) Resumo

O resumo mostra tudo consolidado: atributos, perícias, vantagens, complicações, poderes, equipamentos e derivados (Aparar, Resistência, Movimento etc.). É sua checagem final antes de salvar ou imprimir.
Você apagar e escrever nas anotações.

11) Salvar e Imprimir PDF

No topo da tela principal você tem dois ícones:

Salvar (disquete): guarda o personagem no aparelho para abrir depois.

Imprimir (impressora): gera um PDF com a ficha preenchida e abre em um leitor de PDF instalado no celular. 

Dica: renomeie o personagem antes de imprimir para o PDF sair com o nome certo.
""".trimIndent()


            val emTelaDePreenchimento = !(
                    showVantagensDetail ||
                            showPericiasDetail ||
                            showComplicacoesDetail ||
                            showAtributosDetail ||
                            showAncestralidadesDetail ||
                            showPoderesDetail ||
                            showEquipLista ||
                            showSuperDetail
                    )

            BackHandler(enabled = mostrouTelaInicial) {
                showExitDialog = true
            }

            if (showHelpAppDialog) {
                val scrollState = rememberScrollState()

                AlertDialog(
                    onDismissRequest = { showHelpAppDialog = false },
                    confirmButton = {
                        TextButton(onClick = { showHelpAppDialog = false }) {
                            Text("OK")
                        }
                    },
                    title = { Text("Como usar o app") },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 400.dp)
                                .verticalScroll(scrollState)
                        ) {
                            Text(helpAppText)
                        }
                    }
                )
            }

            if (showExitDialog) {
                AlertDialog(
                    onDismissRequest = { },
                    title            = { Text("Deseja encerrar o app?") },
                    confirmButton    = {
                        TextButton(onClick = {
                            activity?.finishAffinity()
                        }) {
                            Text("Sim")
                        }
                    },
                    dismissButton    = {
                        TextButton(onClick = { }) {
                            Text("Não")
                        }
                    }
                )
            }

            SWADEbuilderTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .systemBarsPadding()
                    ) {
                        if (mostrouTelaInicial) {
                            TelaInicial(
                                onCriarNovo = { cartaSelvagem, maisPontosPericias, modoSupers, _, _,
                                                nasceUmHeroi, heroisSemArmadura, usarEspecializacaoPer,
                                                semPontosDePoder, grandesResponsabilidades ->

                                    creationSession++  // ✅ zera expansões/telas para personagem novo

                                    criadorViewModel.resetStateParaNovoPersonagem(
                                        cartaSelvagem      = cartaSelvagem,
                                        maisPontosPericias = maisPontosPericias,
                                        modoSupers         = modoSupers,
                                        usarEspecializacoesDePericia = usarEspecializacaoPer
                                    )
                                    criadorViewModel.state.heroisSemArmadura     = heroisSemArmadura
                                    criadorViewModel.state.nasceUmHeroi          = nasceUmHeroi

                                    criadorViewModel.state.modoSuperequip        = modoSupers
                                    criadorViewModel.state.modoSuperComplicacoes = modoSupers

                                    criadorViewModel.state.usarSemPontosDePoder  = semPontosDePoder
                                    criadorViewModel.normalizeArcanoIdsNoCarregamento()
                                    criadorViewModel.state.grandesResponsabilidades = grandesResponsabilidades

                                    mostrouTelaInicial = false
                                },
                                onLoad = { salvo ->
                                    creationSession++  // ✅ zera expansões/telas ao trocar de personagem

                                    criadorViewModel.loadFromSalvo(
                                        salvo,
                                        categoriasBasico = equipamentoCategorias,
                                        categoriasSuper  = superequipCategorias
                                    )
                                    mostrouTelaInicial = false
                                },
                                context   = context,
                                viewModel = criadorViewModel
                            )
                        } else {
                            BackHandler(
                                enabled = showVantagensDetail
                                        || showPericiasDetail
                                        || showComplicacoesDetail
                                        || showAtributosDetail
                                        || showAncestralidadesDetail
                                        || showPoderesDetail
                                        || showEquipLista
                                        || showSuperDetail
                            ) {
                                showVantagensDetail       = false
                                showPericiasDetail        = false
                                showComplicacoesDetail    = false
                                showAtributosDetail       = false
                                showAncestralidadesDetail = false
                                showPoderesDetail         = false
                                showEquipLista            = false
                                showSuperDetail           = false
                            }
                            BackHandler(
                                enabled = !(
                                        showVantagensDetail
                                                || showPericiasDetail
                                                || showComplicacoesDetail
                                                || showAtributosDetail
                                                || showAncestralidadesDetail
                                                || showPoderesDetail
                                                || showEquipLista
                                                || showSuperDetail
                                        )
                            ) {
                                mostrouTelaInicial = true
                            }

                            Scaffold(
                                containerColor = Color.Transparent,
                                topBar         = {
                                    if (emTelaDePreenchimento) {
                                        TopAppBar(
                                            colors = TopAppBarDefaults.topAppBarColors(
                                                containerColor = Color.Transparent
                                            ),
                                            title = {
                                                Box(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    TextButton(onClick = { showHelpAppDialog = true }) {
                                                        Text(
                                                            text = "Como usar o app",
                                                            fontSize = 16.sp,
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                    }
                                                }
                                            },
                                            navigationIcon = {
                                                TextButton(onClick = { mostrouTelaInicial = true }) {
                                                    Text(
                                                        text       = "Voltar",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize   = 18.sp
                                                    )
                                                }
                                            },
                                            actions = {
                                                val scope = rememberCoroutineScope()

                                                IconButton(onClick = {
                                                    // snapshot no main thread
                                                    val personagem = state.toMeuPersonagem()

                                                    scope.launch(Dispatchers.IO) {
                                                        salvarEExibirFichaPdf(this@MainActivity, personagem)
                                                    }
                                                }) {
                                                    Icon(Icons.Default.Print, contentDescription = "Imprimir ficha")
                                                }

                                                IconButton(onClick = {
                                                    scope.launch(Dispatchers.IO) {
                                                        val personagemId  = state.idAtual ?: UUID.randomUUID().toString()
                                                        val atributosMap  = state.valoresAtributos.mapValues { it.value.intValue }
                                                        val periciasMap   = listaPericias.associate { per -> per.nome to state.rawTotal(per) }
                                                        val complicacoesList = state.complicacoesSelecionadas
                                                            .filterValues { it != null }
                                                            .keys
                                                            .map { it.id }

                                                        val salvo = PersonagemSalvo(
                                                            id                 = personagemId,
                                                            nome               = state.nomePersonagem,
                                                            atributos          = atributosMap,
                                                            pericias           = periciasMap,
                                                            ancestralidade     = state.ancestralidade,
                                                            vantagens          = state.vantagensSelecionadas.map { it.id },
                                                            complicacoes       = complicacoesList,
                                                            cpPaCount          = state.cpPaStack.size,
                                                            cpPvCount          = state.cpPvStack.size,
                                                            cpSpCount          = state.cpSpStack.size,
                                                            cpRecursosCount    = state.cpRecursosStack.size,
                                                            equipamentos       = state.equipamentosComprados.map { it.nome },
                                                            poderes            = state.poderSlotsPorArcano.mapValues { (_, slots) -> slots.filterNotNull() },
                                                            dinheiro           = state.dinheiro,
                                                            pontosRestantes    = state.pontosVantagem,
                                                            maisPontosPericias = state.maisPontosPericias,
                                                            cartaSelvagem      = state.cartaSelvagem,
                                                            heroisSemArmadura  = state.heroisSemArmadura,
                                                            semPontosDePoder   = state.usarSemPontosDePoder,
                                                            usarEspecializacoesDePericia = state.usarEspecializacoesDePericia,
                                                            especializacoesPorPericia    = state.especializacoesPorPericia.toMap(),
                                                            modoSupers              = state.modoSupers,
                                                            modoSuperequip          = state.modoSuperequip,
                                                            modoSuperComplicacoes   = state.modoSuperComplicacoes,
                                                            superpoderesComprados   = state.superPoderesComprados.map { it.nome },
                                                            superPontosTotais       = state.superPontosTotais,
                                                            superPontosDisponiveis  = state.superPontosDisponiveis,
                                                            limitePorPoderPadrao    = state.limitePorPoderPadrao,
                                                            limiteFavorecido        = state.limiteFavorecido,
                                                            idPoderFavorecido       = state.idPoderFavorecido,
                                                            superAtributoIncs       = state.superAtributoIncs.toMap(),
                                                            superPericiaIncs        = state.superPericiaIncs.toMap(),
                                                            bonusPararFromPower     = state.bonusPararFromPower,
                                                            bonusResFromPower       = state.bonusResFromPower,
                                                            armorFromPower          = state.armorFromPower,
                                                            vantagensDePoder        = state.vantagensDePoder.toSet(),
                                                            gastosPorPoder          = state.gastosPorPoder.toMap(),
                                                            limiteDePoderDaCampanha = state.limiteDePoderDaCampanha,
                                                            anotacoes               = state.anotacoes
                                                        )

                                                        state.idAtual = personagemId
                                                        StorageUtils.salvarPersonagem(context, salvo)

                                                        withContext(Dispatchers.Main) {
                                                            Toast.makeText(context, "Personagem salvo com sucesso!", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                }) {
                                                    Icon(Icons.Default.Save, contentDescription = "Salvar")
                                                }
                                            }
                                        )
                                    }
                                },
                                content = { innerPadding ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(innerPadding)
                                ) {
                                    val screenIndex = when {
                                        showEquipLista            -> 1
                                        showAtributosDetail       -> 2
                                        showVantagensDetail       -> 3
                                        showPericiasDetail        -> 4
                                        showComplicacoesDetail    -> 5
                                        showAncestralidadesDetail -> 6
                                        showPoderesDetail         -> 7
                                        showSuperDetail           -> 8
                                        else                      -> 0
                                    }

                                    Crossfade(
                                        targetState   = screenIndex,
                                        animationSpec = tween(durationMillis = 150)
                                    ) { screen ->
                                        when (screen) {
                                            1 -> EquipamentosDetailScreen(
                                                categorias = equipamentoCategorias +
                                                        if (state.modoSuperequip) superequipCategorias else emptyList(),
                                                onBack     = { showEquipLista = false }
                                            )
                                            2 -> {
                                                val atributosText = remember { dataRepository.loadAtributosText() }
                                                AtributosDetailScreen(
                                                    onBack = { showAtributosDetail = false },
                                                    atributosText = atributosText
                                                )
                                            }
                                            3 -> {
                                                val vantagensJson = remember { dataRepository.loadVantagensText() }
                                                VantagensDetailScreen(
                                                    state = state,
                                                    modoSupers = state.modoSupers,
                                                    highlightedName = highlightedVantagem,
                                                    onBack = { showVantagensDetail = false },
                                                    vantagensJson = vantagensJson
                                                )
                                            }
                                            4 -> {
                                                val descriptions = remember { dataRepository.loadPericiasDescriptions() }
                                                PericiasDetailScreen(
                                                    state = state,
                                                    onBack = { showPericiasDetail = false },
                                                    descriptions = descriptions
                                                )
                                            }
                                            5 -> {
                                                val complicacoesJson = remember { dataRepository.loadComplicacoesText() }
                                                ComplicacoesDetailScreen(
                                                    state = state,
                                                    onBack = { showComplicacoesDetail = false },
                                                    mostrarSuper = state.modoSuperComplicacoes,
                                                    complicacoesJson = complicacoesJson
                                                )
                                            }
                                            6 -> {
                                                val ancestralidadesText = remember { dataRepository.loadAncestralidadesText() }
                                                AncestralidadesDetailScreen(
                                                    state = state,
                                                    onBack = { showAncestralidadesDetail = false },
                                                    ancestralidadesText = ancestralidadesText
                                                )
                                            }
                                            7 -> PoderesDetailScreen(
                                                state  = state,
                                                onBack = { showPoderesDetail = false }
                                            )
                                            8 -> SuperPoderesDetailScreen(
                                                state           = state,
                                                highlightedName = highlightedSuperPoder,
                                                onBack          = {
                                                    showSuperDetail = false
                                                    expPoderes = true        // mantém a seção de superpoderes aberta ao voltar
                                                }
                                            )

                                            else -> UnifiedScreen(
                                                state = state,
                                                onOpenVantagensDetail = { nomeVantagem ->
                                                    highlightedVantagem = nomeVantagem
                                                    state.vantagemEmFoco = nomeVantagem
                                                    showVantagensDetail = true
                                                },
                                                onOpenPericiasDetail             = { showPericiasDetail        = true },
                                                onOpenComplicacoesDetail         = { showComplicacoesDetail    = true },
                                                onOpenAtributosDetail            = { showAtributosDetail       = true },
                                                onOpenListaAncestralidadesDetail = { showAncestralidadesDetail = true },
                                                onOpenListaCompletaEquipamento   = { showEquipLista            = true },
                                                onOpenPoderesDetail              = { showPoderesDetail         = true },

                                                onOpenSuperPoderesDetail         = { nomePoder ->
                                                    highlightedSuperPoder = nomePoder
                                                    state.superPoderEmFoco = nomePoder.ifBlank { null }
                                                    expPoderes = true
                                                    showSuperDetail = true
                                                },

                                                // ✅ Informações
                                                expInfos       = expInfos,
                                                onToggleInfos  = { expInfos = !expInfos },

                                                // ✅ NOVO — Ancestralidades
                                                expAncs        = expAncs,
                                                onToggleAncs   = { expAncs = !expAncs },

                                                // ✅ NOVO — Complicações
                                                expComps       = expComps,
                                                onToggleComps  = { expComps = !expComps },

                                                // ✅ NOVO — Equipamentos
                                                expEquip       = expEquip,
                                                onToggleEquip  = { expEquip = !expEquip },

                                                expAttrs       = expAttrs,
                                                onToggleAttrs  = { expAttrs   = !expAttrs },

                                                expPer         = expPer,
                                                onTogglePer    = { expPer     = !expPer },

                                                expVants       = expVants,
                                                onToggleVants  = { expVants   = !expVants },

                                                expResumo      = expResumo,
                                                onToggleResumo = { expResumo  = !expResumo },

                                                expPoderes      = expPoderes,
                                                onTogglePoderes = { expPoderes = !expPoderes },

                                                equipamentoCategorias = equipamentoCategorias,
                                                superequipCategorias  = superequipCategorias,
                                                listaSuperPoderes     = listaSuperPoderes
                                            )
                                        }
                                    }
                                }
                            }
                            )
                        }
                    }
                }
            }
        }
    }
}

fun Int.toDiceString(): String =
    if (this <= 12) "d$this" else "d12+${(this - 12)}"

private val EquipamentoItem.passageiros
    get() = this.tripulacao
private val EquipamentoItem.blindagem
    get() = this.resistencia

data class Pericia(val nome: String, val atributo: String, val basica: Boolean)

var listaComplicacoes: List<Complicacao> = emptyList()

@Serializable
data class SuperPoder(
    val nome: String,
    val estagio: String = "iniciante",
    val custoBase: String? = null,
    val modificadores: List<String>? = null,
    val descricao: String? = null,
    val manifestacoes: JsonElement? = null
)

lateinit var listaAncestralidadesJson: List<RacialModifier>

lateinit var racialAttrMinMap: Map<String, Map<String,Int>>
lateinit var racialSkillStartMap: Map<String, Map<String,Int>>

lateinit var listaAtributos: List<String>
lateinit var mapaAtributosDisplay: Map<String, String>

lateinit var listaPericias: List<Pericia>

fun periciaStartRaw(anc: String, per: Pericia): Int {
    val ancKey = anc.keyify()
    val perKey = per.nome.keyify()
    return racialSkillStartMap[ancKey]?.get(perKey)
        ?: if (per.basica) 4 else 0
}

var listaVantagens:    List<Vantagem>   = emptyList()

fun loadRawText(context: Context, @RawRes resId: Int): String {
    val inputStream = context.resources.openRawResource(resId)
    val reader = BufferedReader(InputStreamReader(inputStream))
    return reader.readText()
}

data class Estagio(
    val nome: String,
    val minProgress: Int,
    val maxProgress: Int
)

val listaDeEstagios = listOf(
    Estagio("Novato",     0,  3),
    Estagio("Experiente", 4,  7),
    Estagio("Veterano",   8, 11),
    Estagio("Heroico",   12, 15),
    Estagio("Lendário",  16, Int.MAX_VALUE)
)

val nivelParaEstagio = mapOf(
    "N" to listaDeEstagios.first { it.nome == "Novato" },
    "E" to listaDeEstagios.first { it.nome == "Experiente" },
    "V" to listaDeEstagios.first { it.nome == "Veterano" },
    "H" to listaDeEstagios.first { it.nome == "Heroico" },
    "L" to listaDeEstagios.first { it.nome == "Lendário" }
)

const val TOTAL_PROGRESS_LIMIT = 50
val dynamicStageCaps = listaDeEstagios.mapIndexed { idx, st ->
    val prevMax = listaDeEstagios.getOrNull(idx - 1)?.maxProgress ?: 0
    if (idx < listaDeEstagios.lastIndex)
        st.maxProgress - prevMax
    else
        (TOTAL_PROGRESS_LIMIT - prevMax).coerceAtLeast(0)
}





data class BlocoTexto(val tipo: String, val conteudo: String)

fun parseAncestralidades(texto: String): List<BlocoTexto> {
    val linhas = texto.lines()
    val blocos = mutableListOf<BlocoTexto>()

    var esperandoTitulo = true

    for (linha in linhas) {
        val linhaLimpa = linha.trim()

        if (linhaLimpa.isBlank()) {
            esperandoTitulo = true
            continue
        }

        if (esperandoTitulo && linhaLimpa.all { it.isUpperCase() || it == '-' || it == ':' || it.isWhitespace() }) {
            blocos.add(BlocoTexto("titulo", linhaLimpa))
            esperandoTitulo = false
        } else {
            blocos.add(BlocoTexto("texto", linhaLimpa))
            esperandoTitulo = false
        }
    }

    return blocos
}

@Composable
fun SectionHeader(
    onHelpClick: (() -> Unit)? = null,
    centerText: String,
    onCenterClick: (() -> Unit)? = null,
    onListaCompletaClick: (() -> Unit)? = null,
    listaCompletaText: String = "Lista Completa"
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Coluna da esquerda: botão de ajuda (ou espaço em branco se não tiver ajuda)
        if (onHelpClick != null) {
            IconButton(onClick = onHelpClick) {
                Icon(
                    Icons.AutoMirrored.Filled.Help,
                    contentDescription = "Ajuda"
                )
            }
        } else {
            // Espaçador para não bagunçar o alinhamento
            Spacer(
                modifier = Modifier.size(48.dp)
            )
        }

        // Texto central (clicável se onCenterClick != null)
        Text(
            text = centerText,
            modifier = Modifier
                .weight(1f)
                .then(
                    if (onCenterClick != null) {
                        Modifier.clickable(onClick = onCenterClick)
                    } else {
                        Modifier
                    }
                )
                .padding(horizontal = 8.dp),
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        // Botão "Lista completa" à direita (ou espaçador)
        if (onListaCompletaClick != null && listaCompletaText.isNotEmpty()) {
            TextButton(onClick = onListaCompletaClick) {
                Text(listaCompletaText, fontSize = 13.sp)
            }
        } else {
            Spacer(
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

@Composable
fun CollapsibleSection(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Icon(
                imageVector = if (expanded) Icons.Default.Remove else Icons.Default.Add,
                contentDescription = null
            )
        }
        if (expanded) content()
    }
}

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
fun RadioButtonRow(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(vertical = 4.dp)
    ) {
        RadioButton(
            selected = selected,
            onClick  = onSelect
        )
        Spacer(Modifier.width(8.dp))
        Text(text = label)
    }
}




@Composable
fun PowerDropdownMenu(
    label: String,
    options: List<String>,
    selected: String?,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange
    ) {
        OutlinedTextField(
            value = selected ?: label,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                .clickable { onExpandedChange(true) }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier
                .exposedDropdownSize()
                .heightIn(max = 200.dp)

        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        onExpandedChange(false)
                    }
                )
            }
        }
    }
}




@Composable
fun SelecaoCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 12.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.Black),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF2E3C6)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    color = Color.Black,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                contentDescription = null,
                tint = Color.Black
            )
        }
    }
}