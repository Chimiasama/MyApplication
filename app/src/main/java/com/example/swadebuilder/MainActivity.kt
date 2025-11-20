@file:OptIn(
    ExperimentalMaterial3Api::class
)
@file:Suppress("LanguageDetectionInspection")

package com.example.swadebuilder

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.swadebuilder.model.AtributoList
import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.Complicacao
import com.example.swadebuilder.model.CriadorViewModel
import com.example.swadebuilder.model.EquipamentoCategoria
import com.example.swadebuilder.model.EquipamentoItem
import com.example.swadebuilder.model.MeuPersonagem
import com.example.swadebuilder.model.PericiaList
import com.example.swadebuilder.model.PersonagemSalvo
import com.example.swadebuilder.model.RacialModifier
import com.example.swadebuilder.model.StorageUtils
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.model.loadPericiasDescriptions
import com.example.swadebuilder.ui.dialogs.ProgressosDialog
import com.example.swadebuilder.ui.sections.AncestralidadesSection
import com.example.swadebuilder.ui.sections.AtributosContent
import com.example.swadebuilder.ui.sections.ComplicacoesSection
import com.example.swadebuilder.ui.sections.EquipamentoSection
import com.example.swadebuilder.ui.sections.InformacoesSection
import com.example.swadebuilder.ui.sections.PericiasContent
import com.example.swadebuilder.ui.sections.PoderesDetailScreen
import com.example.swadebuilder.ui.sections.PoderesSection
import com.example.swadebuilder.ui.sections.SummaryContent
import com.example.swadebuilder.ui.sections.SuperPoderesContent
import com.example.swadebuilder.ui.sections.SuperPoderesDetailScreen
import com.example.swadebuilder.ui.sections.VantagensContent
import com.example.swadebuilder.ui.theme.SWADEbuilderTheme
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.loadJsonAsset
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
import java.io.File
import java.io.FileOutputStream
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
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    @OptIn(ExperimentalMaterial3Api::class)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val allEquipJson = assets
            .open("equipamentos.json")
            .bufferedReader()
            .use { it.readText() }
        val allEquipCategorias: List<EquipamentoCategoria> =
            json.decodeFromString(allEquipJson)

        val equipamentoCategorias = allEquipCategorias.filter { cat ->
            cat.origem?.equals("super", ignoreCase = true)?.not() ?: true
        }
        val superequipCategorias = allEquipCategorias.filter { cat ->
            cat.origem?.equals("super", ignoreCase = true) ?: false
        }

        val superPoderesJson = assets
            .open("superpoderes.json")
            .bufferedReader()
            .use { it.readText() }
        val listaSuperPoderes: List<SuperPoder> =
            json.decodeFromString(superPoderesJson)

        val arcanoJson = assets.open("arcano_info.json")
            .bufferedReader().use { it.readText() }
        val arcanoList: List<ArcanoInfo> =
            Json.decodeFromString(arcanoJson)
        arcanoInfo = arcanoList.associate {
            it.key
                .uppercase()
                .semAcentos()
                .trim() to Triple(it.slots, it.pp, it.foco)
        }

        val atributosData = this.loadJsonAsset<AtributoList>("atributos.json")
        listaAtributos = atributosData.atributos
            .map { it.nome.keyify() }
        mapaAtributosDisplay = atributosData.atributos
            .associate { it.nome.keyify() to it.nome }

        val periciasData = this.loadJsonAsset<PericiaList>("pericias.json")
        listaPericias = periciasData.pericias.map { pj ->
            Pericia(
                nome     = pj.nome,
                atributo = pj.atributo.uppercase().semAcentos(),
                basica   = pj.basica
            )
        }

        val todasVantagens: List<Vantagem> = this.loadJsonAsset("Vantagens.json")

        AppData.basicasVantagens          = todasVantagens.filter { it.origem.equals("BASICO", true) }
        AppData.superVantagens = todasVantagens.filter {
            it.origem.equals("SUPER", ignoreCase = true)
        }

        listaVantagens = todasVantagens

        AppData.superVantagensParaDetalhe = AppData.superVantagens


        val complicacoesJson = assets
            .open("complicacoes.json")
            .bufferedReader()
            .use { it.readText() }

        listaComplicacoes = json.decodeFromString(
            ListSerializer(Complicacao.serializer()),
            complicacoesJson
        )

        val ancestralRaw = assets.open("listaancestralidade.json")
            .bufferedReader(Charsets.UTF_8)
            .use { it.readText() }

        listaAncestralidadesJson = Json.decodeFromString<List<RacialModifier>>(ancestralRaw)

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

            var expAttrs    by rememberSaveable { mutableStateOf(false) }
            var expPer      by rememberSaveable { mutableStateOf(false) }
            var expVants    by rememberSaveable { mutableStateOf(false) }
            var expPoderes  by rememberSaveable { mutableStateOf(false) }
            var expResumo   by rememberSaveable { mutableStateOf(false) }

            var showVantagensDetail       by rememberSaveable { mutableStateOf(false) }
            var showPericiasDetail        by rememberSaveable { mutableStateOf(false) }
            var showComplicacoesDetail    by rememberSaveable { mutableStateOf(false) }
            var showAtributosDetail       by rememberSaveable { mutableStateOf(false) }
            var showAncestralidadesDetail by rememberSaveable { mutableStateOf(false) }
            var showPoderesDetail         by rememberSaveable { mutableStateOf(false) }
            var showEquipLista            by rememberSaveable { mutableStateOf(false) }
            var showSuperDetail           by rememberSaveable { mutableStateOf(false) }
            var highlightedVantagem by rememberSaveable { mutableStateOf("") }
            var highlightedSuperPoder by rememberSaveable { mutableStateOf("") }
            val context = LocalContext.current
            val activity = (context as? ComponentActivity)
            var mostrouTelaInicial by rememberSaveable { mutableStateOf(true) }
            var showExitDialog     by rememberSaveable { mutableStateOf(false) }

            var showHelpAppDialog by rememberSaveable { mutableStateOf(false) }

            val helpAppText = """
Este app ajuda você a criar personagens de Savage Worlds passo a passo.

1) Tela Inicial
   • Escolha se o personagem é Carta Selvagem, se terá mais pontos de Perícia, se usa modo Supers etc.
   • Depois toque em "Criar Personagem".

2) Ordem sugerida de preenchimento
   • Ancestralidade → define bônus e limites de atributos/perícias.
   • Atributos → distribua os pontos de atributo iniciais.
   • Perícias → gaste os pontos de perícia disponíveis.
   • Complicações → escolha Complicações para ganhar Pontos Bônus de Criação.
   • Vantagens, Poderes e Equipamentos → usam esses recursos para finalizar a ficha.

3) Pontos Bônus de Criação
   • Cada Complicação Menor gera 1 Ponto Bônus.
   • Cada Complicação Maior gera 2 Pontos Bônus.
   • O contador em "Complicações" mostra quantos Pontos Bônus você tem livres.
   • Esses Pontos Bônus podem ser usados em Atributos, Perícias, Vantagens ou Recursos
     pelos botões específicos de cada seção.

4) Ajustes e devoluções
   • Se você usou Pontos Bônus em Atributos/Perícias/Vantagens/Recursos e quiser desfazer,
     use as opções de "desfazer Pontos Bônus" nas seções correspondentes.
   • Se ainda houver Pontos Bônus em uso, algumas Complicações não poderão ser removidas:
     primeiro desfaça os pontos comprados com elas.

5) Dicas gerais
   • Toque no título de cada seção para expandir/fechar.
   • Use "Lista Completa" para ler o texto completo das Vantagens, Perícias e Complicações.
   • Use os ícones de salvar/imprimir no topo para guardar ou gerar a ficha em PDF.
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
                                onCriarNovo = { cartaSelvagem, maisPontosPericias, modoSupers, _, _ , nasceUmHeroi, heroisSemArmadura, usarEspecializacaoPer, semPontosDePoder, grandesResponsabilidades ->

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
                                                    scope.launch(Dispatchers.IO) {
                                                        val personagem = MeuPersonagem(
                                                            nome            = state.nomePersonagem,
                                                            atributos       = state.valoresAtributos.mapValues { it.value.intValue },
                                                            pericias        = listaPericias.associate { per -> per.nome to state.rawTotal(per) },
                                                            ancestralidade  = state.ancestralidade,
                                                            vantagens       = state.vantagensSelecionadas.map { it.id },
                                                            complicacoes    = state.complicacoesSelecionadas
                                                                .filterValues { it != null }
                                                                .keys
                                                                .map { it.id },
                                                            equipamentos    = state.equipamentosComprados.toList(),
                                                            poderes         = state.poderSlotsPorArcano.mapValues { (_, slots) -> slots.filterNotNull() },
                                                            dinheiro        = state.dinheiro,
                                                            pontosRestantes = state.pontosVantagem,
                                                            modoSupers              = state.modoSupers,
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
                                                            bonusMovimentacaoFromPower = state.bonusMovimentacaoFromPower,
                                                            vantagensDePoder        = state.vantagensDePoder.toSet(),
                                                            gastosPorPoder          = state.gastosPorPoder.toMap(),
                                                            limiteDePoderDaCampanha = state.limiteDePoderDaCampanha,
                                                            anotacoes               = state.anotacoes
                                                        )
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
                                            2 -> AtributosDetailScreen(onBack = { showAtributosDetail = false })
                                            3 -> VantagensDetailScreen(
                                                state           = state,
                                                modoSupers      = state.modoSupers,
                                                highlightedName = highlightedVantagem,
                                                onBack          = { showVantagensDetail = false }
                                            )
                                            4 -> PericiasDetailScreen(
                                                state  = state,
                                                onBack = { showPericiasDetail = false }
                                            )
                                            5 -> ComplicacoesDetailScreen(
                                                state        = state,
                                                onBack       = { showComplicacoesDetail = false },
                                                mostrarSuper = state.modoSuperComplicacoes
                                            )
                                            6 -> AncestralidadesDetailScreen(
                                                state  = state,
                                                onBack = { showAncestralidadesDetail = false }
                                            )
                                            7 -> PoderesDetailScreen(
                                                state  = state,
                                                onBack = { showPoderesDetail = false }
                                            )
                                            8 -> SuperPoderesDetailScreen(
                                                state           = state,
                                                highlightedName = highlightedSuperPoder,
                                                onBack          = { showSuperDetail = false }
                                            )

                                            else -> UnifiedScreen(
                                                state = state,
                                                onOpenVantagensDetail = { nomeVantagem ->
                                                    highlightedVantagem = nomeVantagem.toString()
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
                                                    showSuperDetail       = true
                                                },

                                                expAttrs       = expAttrs,
                                                onToggleAttrs  = { expAttrs   = !expAttrs },
                                                expPer         = expPer,
                                                onTogglePer    = { expPer     = !expPer },
                                                expVants       = expVants,
                                                onToggleVants  = { expVants   = !expVants },
                                                expResumo      = expResumo,
                                                onToggleResumo = { expResumo  = !expResumo },
                                                expPoderes     = expPoderes,
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

fun salvarEExibirFichaPdf(context: Context, dadosDoPersonagem: MeuPersonagem) {
    val pdfFile = File(context.getExternalFilesDir(null), "ficha_preenchida.pdf")

    gerarFichaEmPdf(pdfFile, dadosDoPersonagem)

    val uri: Uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        pdfFile
    )

    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    }

    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    } else {
        Toast.makeText(context, "Nenhum app de PDF encontrado.", Toast.LENGTH_SHORT).show()
    }
}

fun buildSummaryLines(personagem: MeuPersonagem): List<String> {
    val lines = mutableListOf<String>()

    val ancestralidadeNome: String = listaAncestralidadesJson
        .firstOrNull { it.nome.keyify() == personagem.ancestralidade }
        ?.nome ?: personagem.ancestralidade

    val vantagensNomeKey: List<String> = listaVantagens
        .filter { it.id in personagem.vantagens }
        .map { it.nome.keyify() }

    fun temComp(key: String): Boolean =
        personagem.complicacoes.any { it.keyify() == key }

    fun racialSize(): Int =
        listaAncestralidadesJson
            .firstOrNull { it.nome.keyify() == personagem.ancestralidade }
            ?.desvantagens
            ?.firstOrNull { it.startsWith("TAMANHO", ignoreCase = true) }
            ?.substringAfter("TAMANHO")
            ?.trim()
            ?.toIntOrNull()
            ?: 0

    fun tamanhoTotal(): Int {
        val base = racialSize()
        val obesoBonus = if (temComp("OBESO")) 1 else 0
        val pequenoPenalty = if (temComp("PEQUENO")) -1 else 0
        return base + obesoBonus + pequenoPenalty
    }

    fun resistenciaBase(): Int {
        val vigorRaw = personagem.atributos["VIGOR"] ?: 4
        val base = 2 + (vigorRaw / 2)

        val bonusPos =
            if (vantagensNomeKey.any { it == "RESISTENCIA" }) 1 else 0
        val bonusNeg =
            if (personagem.complicacoes.any { it.keyify() == "FRAGIL" }) -1 else 0

        val brigaoBonus = vantagensNomeKey.count { it in listOf("BRIGAO", "PUGILISTA") }

        return (base + bonusPos + bonusNeg + brigaoBonus + tamanhoTotal())
            .coerceAtLeast(0)
    }

    fun resistenciaFinal(): Int =
        resistenciaBase() + personagem.bonusResFromPower

    fun calcMovimento(): Int {
        val base = 6

        val racialPenalty =
            listaAncestralidadesJson
                .firstOrNull { it.nome.keyify() == personagem.ancestralidade }
                ?.desvantagens
                ?.any { it.contains("MOVIMENTAÇÃO REDUZIDA", ignoreCase = true) }
                .takeIf { it == true }
                ?.let { 1 }
                ?: 0

        val lentoPenalty = if (temComp("LENTO")) 1 else 0
        val idosoPenalty = if (temComp("IDOSO")) 1 else 0
        val obesoPenalty = if (temComp("OBESO")) 1 else 0
        val ligeiroBonus =
            if (vantagensNomeKey.any { it == "LIGEIRO" }) 2 else 0

        return (
                base
                        - racialPenalty
                        - lentoPenalty
                        - idosoPenalty
                        - obesoPenalty
                        + ligeiroBonus
                        + personagem.bonusMovimentacaoFromPower
                ).coerceAtLeast(0)
    }

    fun applySuperStepsFrom(rawStart: Int, steps: Int): Int {
        var raw = rawStart
        var remaining = steps.coerceAtLeast(0)

        if (raw <= 0 && remaining > 0) {
            raw = 4
            remaining -= 1
        }

        repeat(remaining) {
            raw += if (raw < 12) 2 else 1
        }

        return raw
    }

    fun calcAparar(): Int {
        val lutarRawBase = personagem.pericias["Lutar"] ?: 0
        val lutarStepsFromSupers = personagem.superPericiaIncs["LUTAR"] ?: 0
        val lutarComSupers = applySuperStepsFrom(lutarRawBase, lutarStepsFromSupers)

        val base = 2 + (lutarComSupers / 2)

        val bloquearBonus =
            if (vantagensNomeKey.any { it == "BLOQUEAR" }) 1 else 0
        val bloquearAprimoradoBonus =
            if (vantagensNomeKey.any { it == "BLOQUEAR APRIMORADO" }) 1 else 0

        return base + bloquearBonus + bloquearAprimoradoBonus + personagem.bonusPararFromPower
    }

    fun calcArmaduraEfetiva(): Int {
        return personagem.armorFromPower.coerceAtLeast(0)
    }

    val aparar = calcAparar()
    val resFinal = resistenciaFinal()
    val tamanho = tamanhoTotal()
    val mov = calcMovimento()
    val armadura = calcArmaduraEfetiva()
    val resistenciaTexto =
        if (armadura > 0) "${resFinal}(${armadura})" else resFinal.toString()

    lines += "Identidade"
    lines += "Nome: ${personagem.nome.ifBlank { "(sem nome)" }}"
    lines += "Ancestralidade: $ancestralidadeNome"
    lines += ""

    lines += "Atributos derivados"
    lines += "Aparar: $aparar"
    lines += "Resistência: $resistenciaTexto"
    lines += "Tamanho: $tamanho"
    lines += "Movimento: $mov"
    if (armadura > 0) {
        lines += "Armadura: $armadura"
    }
    lines += ""

    lines += "Atributos"
    lines += listaAtributos.joinToString(", ") { attrKey ->
        val label = mapaAtributosDisplay[attrKey] ?: attrKey
        val valor = personagem.atributos[attrKey] ?: 4
        "$label d$valor"
    }
    lines += ""

    val periciasParaMostrar = listaPericias.filter { per ->
        per.basica || (personagem.pericias[per.nome] ?: 0) >
                periciaStartRaw(personagem.ancestralidade, per)
    }

    lines += "Perícias"
    if (periciasParaMostrar.isEmpty()) {
        lines += "– Nenhuma"
    } else {
        periciasParaMostrar.forEach { per ->
            val raw = personagem.pericias[per.nome] ?: 0
            lines += "• ${per.nome} d$raw"
        }
    }
    lines += ""

    lines += "Recursos & Equipamentos"
    lines += "Dinheiro restante: ${personagem.dinheiro}"
    if (personagem.equipamentos.isEmpty()) {
        lines += "Equipamentos: – Nenhum"
    } else {
        lines += "Equipamentos:"
        personagem.equipamentos.forEach { eq ->
            lines += "• ${eq.nome}"
        }
    }
    lines += ""

    lines += "Vantagens"
    if (personagem.vantagens.isEmpty()) {
        lines += "– Nenhuma"
    } else {
        val nomesVantagens = listaVantagens
            .filter { it.id in personagem.vantagens }
            .map { it.nome }
        lines += nomesVantagens.joinToString(", ")
    }
    lines += ""

    lines += "Complicações"
    lines += if (personagem.complicacoes.isEmpty()) {
        "– Nenhuma"
    } else {
        personagem.complicacoes.joinToString(", ")
    }
    lines += ""

    if (personagem.poderes.isNotEmpty()) {
        lines += "Poderes arcanos"
        personagem.poderes.forEach { (arcanoKey, lista) ->
            val label = arcanoKey
                .lowercase()
                .replace('_', ' ')
                .replaceFirstChar { it.titlecase() }

            lines += if (lista.isEmpty()) {
                "• $label: – nenhum poder escolhido"
            } else {
                "• $label: ${lista.joinToString(", ")}"
            }
        }
        lines += ""
    }

    if (personagem.modoSupers &&
        (personagem.superPontosTotais > 0 || personagem.gastosPorPoder.isNotEmpty())
    ) {
        lines += "Superpoderes"

        if (personagem.gastosPorPoder.isEmpty()) {
            lines += "– Nenhum superpoder registrado"
        } else {
            personagem.gastosPorPoder.forEach { (poderId, custo) ->
                lines += "• $poderId: $custo SP"
            }
        }

        lines += "Superpontos: ${personagem.superPontosTotais} (disponíveis: ${personagem.superPontosDisponiveis})"
        lines += "Limite por poder: ${personagem.limitePorPoderPadrao}"
        lines += ""
    }

    if (personagem.anotacoes.isNotBlank()) {
        lines += "Anotações"
        personagem.anotacoes
            .lines()
            .forEach { linha -> lines += linha }
    }

    return lines
}

fun gerarFichaEmPdf(destino: File, personagem: MeuPersonagem) {
    val doc = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()

    val marginLeft   = 40f
    val marginRight  = 40f
    val marginTop    = 50f
    val marginBottom = 40f

    val paint = Paint().apply { textSize = 12f }
    val fm = paint.fontMetrics
    val lineHeight = fm.descent - fm.ascent + fm.leading

    var page = doc.startPage(pageInfo)
    var canvas = page.canvas
    var y = marginTop

    fun newPage() {
        doc.finishPage(page)
        page = doc.startPage(pageInfo)
        canvas = page.canvas
        y = marginTop
    }

    fun drawWrapped(text: String) {
        var start = 0
        val maxWidth = pageInfo.pageWidth - marginLeft - marginRight
        while (start < text.length) {
            val count = paint.breakText(text, start, text.length, true, maxWidth, null)
            val line = text.substring(start, start + count)
            if (y + lineHeight > pageInfo.pageHeight - marginBottom) {
                newPage()
            }
            canvas.drawText(line, marginLeft, y, paint)
            y += lineHeight
            start += count
        }
    }

    val titlePaint = Paint(paint).apply {
        textSize = 16f
        isFakeBoldText = true
    }
    val title = "Ficha de ${personagem.nome}"

    val titleFm = titlePaint.fontMetrics
    val titleHeight = titleFm.descent - titleFm.ascent + titleFm.leading
    if (y + titleHeight > pageInfo.pageHeight - marginBottom) {
        newPage()
    }
    canvas.drawText(title, marginLeft, y, titlePaint)
    y += titleHeight + 12f

    val lines = buildSummaryLines(personagem)
    for (linha in lines) {
        drawWrapped(linha)
    }

    doc.finishPage(page)
    FileOutputStream(destino).use { out ->
        doc.writeTo(out)
    }
    doc.close()
}

lateinit var listaAncestralidadesJson: List<RacialModifier>

lateinit var racialAttrMinMap: Map<String, Map<String,Int>>
lateinit var racialSkillStartMap: Map<String, Map<String,Int>>

lateinit var listaAtributos: List<String>
lateinit var mapaAtributosDisplay: Map<String, String>

lateinit var listaPericias: List<Pericia>

fun CriadorState.valorMovimentacao(): Int {
    val base = 6

    val racialPenalty =
        listaAncestralidadesJson
            .firstOrNull { it.nome.keyify() == ancestralidade }
            ?.desvantagens
            ?.any { it.contains("MOVIMENTAÇÃO REDUZIDA", ignoreCase = true) }
            .takeIf { it == true }
            ?.let { 1 }
            ?: 0

    val idosoPenalty =
        complicacoesSelecionadas
            .filterKeys { it.id.keyify() == "IDOSO" }
            .isNotEmpty()
            .takeIf { it }
            ?.let { 1 }
            ?: 0

    val lentoPenalty = complicacoesSelecionadas
        .entries
        .firstOrNull { it.key.id.keyify() == "LENTO" }
        ?.let { (_, grau) ->
            when (grau) {
                "Menor" -> 1
                "Maior" -> 2
                else    -> 0
            }
        }
        ?: 0

    val obesoPenalty =
        complicacoesSelecionadas
            .filterKeys { it.id.keyify() == "OBESO" }
            .isNotEmpty()
            .takeIf { it }
            ?.let { 1 }
            ?: 0

    val ligeiroBonus =
        if (vantagensSelecionadas.any { it.nome.keyify() == "LIGEIRO" })
            2
        else
            0

    return (base
            - racialPenalty
            - idosoPenalty
            - lentoPenalty
            - obesoPenalty
            + ligeiroBonus
            + bonusMovimentacaoFromPower)
        .coerceAtLeast(0)
}

fun CriadorState.valorAparar(): Int {
    val perLutar = listaPericias.firstOrNull { it.nome.equals("Lutar", ignoreCase = true) }
    val lutarRaw = perLutar?.let { rawTotalComSupers(it) } ?: 0
    val base     = 2 + (lutarRaw / 2)

    val bloquearBonus =
        if (vantagensSelecionadas.any { it.nome.keyify() == "BLOQUEAR" }) 1 else 0
    val bloquearAprimoradoBonus =
        if (vantagensSelecionadas.any { it.nome.keyify() == "BLOQUEAR APRIMORADO" }) 1 else 0

    return base + bloquearBonus + bloquearAprimoradoBonus + bonusPararFromPower
}

fun CriadorState.valorResistenciaBase(): Int {
    val vigorRaw = valoresAtributos["VIGOR"]?.intValue ?: 4
    val base     = 2 + (vigorRaw / 2)
    val bonusPos = if (vantagensAutomaticas.any { it.keyify() == "RESISTENCIA" }) 1 else 0
    val bonusNeg = if (desvantagensAutomaticas.any { it.keyify() == "FRAGIL" }) -1 else 0
    val racialSize = listaAncestralidadesJson
        .firstOrNull { it.nome.keyify() == ancestralidade }
        ?.desvantagens
        ?.firstOrNull { it.startsWith("TAMANHO", ignoreCase = true) }
        ?.substringAfter("TAMANHO")
        ?.trim()
        ?.toIntOrNull()
        ?: 0
    val obesoBonus =
        if (complicacoesSelecionadas.keys.any { it.id.keyify() == "OBESO" }) +1 else 0
    val pequenoPenalty =
        if (complicacoesSelecionadas.keys.any { it.id.keyify() == "PEQUENO" }) -1 else 0
    val brigaoBonus = vantagensSelecionadas
        .count { it.nome.keyify() in listOf("BRIGAO", "PUGILISTA") }
    val sizeRaw = racialSize + obesoBonus + pequenoPenalty
    return (base + bonusPos + bonusNeg + brigaoBonus + sizeRaw)
        .coerceAtLeast(0)
}

fun CriadorState.valorResistenciaFinal(): Int {
    return valorResistenciaBase() + bonusResFromPower
}

fun CriadorState.valorArmaduraEfetiva(): Int {
    val armorFromEquipment = armadura
    val melhorExterna = kotlin.math.max(armorFromPower, armorFromEquipment)
    return (melhorExterna + naturalArmorFromRace).coerceAtLeast(0)
}

fun CriadorState.adicionarVantagemPorSuper(v: Vantagem): Boolean {
    if (v.categoria == Categoria.LENDARIAS) return false

    val progressoAnterior = overrideStageForVantagem
    overrideStageForVantagem = "Lendário"

    val permitido = podeSelecionar(v)
    overrideStageForVantagem = progressoAnterior

    if (!permitido) return false

    if (!vantagensSelecionadas.contains(v)) {
        vantagensSelecionadas += v
        vantagensDePoder += v.id
        return true
    }
    return false
}

fun CriadorState.removerVantagemPorSuper(v: Vantagem) {
    vantagensSelecionadas.remove(v)
    vantagensDePoder.remove(v.id)
}


fun CriadorState.valorTamanho(): Int {
    val desc = listaAncestralidadesJson
        .firstOrNull { it.nome.keyify() == ancestralidade }
        ?.desvantagens
        ?.firstOrNull { it.startsWith("TAMANHO", ignoreCase = true) }

    val racialSize = desc
        ?.substringAfter("TAMANHO")
        ?.trim()
        ?.toIntOrNull()
        ?: 0
    val obesoBonus =
        if (complicacoesSelecionadas.keys.any { it.id.keyify() == "OBESO" })
            1
        else
            0
    val pequenoPenalty =
        if (complicacoesSelecionadas.keys.any { it.id.keyify() == "PEQUENO" })
            -1
        else
            0
    val musculosoBonus =
        if (vantagensSelecionadas.any { it.nome.keyify() == "MUSCULOSO" })
            1
        else
            0
    val raw = racialSize + obesoBonus + pequenoPenalty + musculosoBonus
    return raw.coerceIn(-1, 3)
}

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



@Composable
fun SectionCard(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    val headerColor = MaterialTheme.colorScheme.onBackground
    val cardColor   = MaterialTheme.colorScheme.surfaceVariant

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(vertical = 8.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(36.dp)
                    .padding(end = 12.dp),
                tint = headerColor
            )
            Text(
                text       = title,
                fontSize   = 27.sp,
                fontWeight = FontWeight.Bold,
                color      = headerColor
            )
            Spacer(Modifier.weight(1f))
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = headerColor
            )
        }

        AnimatedVisibility(visible = expanded) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = cardColor
                )
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
fun AncestralidadesDetailScreen(
    state: CriadorState,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val ancestralidadesTexto = remember { loadRawText(context, R.raw.ancestralidades) }
    val listaBlocos = remember { parseAncestralidades(ancestralidadesTexto) }

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
private fun ProgressRow(
    label: String,
    count: Int,
    remaining: Int,
    onPlus: () -> Unit,
    onMinus: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, Modifier.weight(1f))
        IconButton(onClick = onMinus, enabled = count > 0) {
            Icon(Icons.Default.Remove, contentDescription = "menos")
        }
        Text("$count", Modifier.width(32.dp), textAlign = TextAlign.Center)
        IconButton(onClick = onPlus, enabled = remaining > 0) {
            Icon(Icons.Default.Add, contentDescription = "mais")
        }
    }
}

@Composable
fun ProgressosSection(state: CriadorState) {
    var showDialog by rememberSaveable { mutableStateOf(false) }

    // ⬇️ detecta se comprou antecedente arcano
    val hasArcaneBg = state.vantagensSelecionadas
        .any { it.nome.startsWith("Antecedente Arcano", ignoreCase = true) }

    // ⬇️ detecta se já alocou pelo menos um poder
    val powersAssigned = state.poderSlotsPorArcano.values
        .any { slots -> slots.any { it != null } }

    // ⬇️ só permite gastar se NÃO estiver pendente de escolher poderes
    val canUseProgress = !hasArcaneBg || powersAssigned

    SectionCard(
        title    = "Gastar Progressos (${state.progressosDisponiveis})",
        expanded = true,
        onToggle = {},
        icon     = Icons.Default.FlashOn
    ) {
        Button(
            onClick = { showDialog = true },
            enabled = canUseProgress
        ) {
            Text("Usar Progresso")
        }

        if (showDialog) {
            ProgressosDialog(state) {
                state.frozenAdvCount = state.vantagensSelecionadas.size
                state.emProgresso    = true
            }
        }
    }
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
fun AtributosDetailScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val atributosTexto = remember {
        loadRawText(context, R.raw.atributos)
    }
    val listaAtributosCompleta = remember {
        parseAtributos(atributosTexto)
    }

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

        items(listaAtributosCompleta) { atributoCompleto ->
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = atributoCompleto.nome,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))

                if (atributoCompleto.descricao.isNotBlank()) {
                    Text(
                        text = atributoCompleto.descricao,
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VantagensDetailScreen(
    state: CriadorState,
    modoSupers: Boolean,
    highlightedName: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // 1) Carrega TODAS as vantagens do asset
    val todasVantagens: List<Vantagem> = remember {
        val jsonString = context.assets.open("Vantagens.json")
            .bufferedReader()
            .use { it.readText() }
        val parser = Json {
            ignoreUnknownKeys  = true
            explicitNulls      = false
        }
        parser.decodeFromString(
            ListSerializer(Vantagem.serializer()),
            jsonString
        )
    }

    // 2) Filtro de grupo (mantém sua regra original)
    val listaFiltradaParaGrupo = remember(modoSupers, todasVantagens) {
        if (!modoSupers) {
            // no “básico” mostramos todas; o filtro por origem BASICO acontece abaixo
            todasVantagens
        } else {
            // no modo supers, remove entradas específicas do grupo (como AA e Poder)
            todasVantagens.filter { vant ->
                vant.id != "antecedente_arcano" &&
                        !vant.requisitos.vantagensPrevias.contains("antecedente_arcano") &&
                        vant.categoria.name.uppercase() != "PODER"
            }
        }
    }

    // 3) Constrói blocos de texto (categorias Normais = origem BASICO)
    val categoriasNormais: Map<String, List<String>> = remember(listaFiltradaParaGrupo) {
        listaFiltradaParaGrupo
            .filter { it.origem.equals("BASICO", ignoreCase = true) }
            .groupBy { it.categoria.name }
            .mapValues { entry ->
                entry.value.map { vant ->
                    buildString {
                        append(vant.nome)
                        append("\nEstágio: ${vant.requisitos.estagio}")
                        vant.requisitos.atributoMin.forEach { (atributo, minimo) ->
                            append("\n$atributo ≥ $minimo")
                        }
                        vant.requisitos.periciaMin.forEach { (pericia, minimo) ->
                            append("\n$pericia ≥ $minimo")
                        }
                        vant.requisitos.periciaMinOpcional.forEach { (pericia, valorMinimo) ->
                            append("\n$pericia d$valorMinimo+")
                        }
                        vant.requisitos.vantagensPrevias.forEach { req ->
                            append("\nPré‐requisito: $req")
                        }
                        if (vant.requisitos.observacoes.isNotBlank()) {
                            append("\nObservações: ${vant.requisitos.observacoes}")
                        }
                        append("\n\n${vant.descricao}")
                    }
                }
            }
    }

    // 4) Constrói blocos de texto (categorias Super — somente quando modoSupers)
    val categoriasSuper: Map<String, List<String>> = remember(modoSupers) {
        if (!modoSupers) {
            emptyMap()
        } else {
            val supList = AppData.superVantagensParaDetalhe
            supList.groupBy { it.categoria.name }
                .mapValues { entry ->
                    entry.value.map { vant ->
                        buildString {
                            append(vant.nome)
                            append("\nEstágio: ${vant.requisitos.estagio}")
                            vant.requisitos.periciaMin.forEach { (pericia, minimo) ->
                                append("\n$pericia ≥ $minimo")
                            }
                            vant.requisitos.periciaMinOpcional.forEach { (pericia, valorMinimo) ->
                                append("\n$pericia ≥ $valorMinimo")
                            }
                            vant.requisitos.vantagensPrevias.forEach { req ->
                                append("\nPré‐requisito: $req")
                            }
                            if (vant.requisitos.observacoes.isNotBlank()) {
                                append("\nObservações: ${vant.requisitos.observacoes}")
                            }
                            append("\n\n${vant.descricao}")
                        }
                    }
                }
        }
    }

    // 5) Merge de categorias, preservando seu formato (chave normalizada -> Pair(nomeExibicao, blocos))
    val todasCategorias: Map<String, Pair<String, List<String>>> = remember(
        categoriasNormais,
        categoriasSuper
    ) {
        val tempMap = mutableMapOf<String, Pair<String, MutableList<String>>>()

        categoriasNormais.forEach { (categoriaEnumName, blocosTexto) ->
            val chaveNorm = categoriaEnumName
                .uppercase()
                .semAcentos()
                .removePrefix("DE ")
                .trim()
            tempMap[chaveNorm] = Pair(categoriaEnumName, blocosTexto.toMutableList())
        }

        categoriasSuper.forEach { (categoriaEnumName, blocosTextoSuper) ->
            val chaveNorm = categoriaEnumName
                .uppercase()
                .semAcentos()
                .removePrefix("DE ")
                .trim()
            if (tempMap.containsKey(chaveNorm)) {
                val (_, blocosMutaveis) = tempMap.getValue(chaveNorm)
                blocosMutaveis.addAll(blocosTextoSuper)
            } else {
                tempMap[chaveNorm] = Pair(
                    categoriaEnumName.lowercase().replaceFirstChar { it.uppercase() },
                    blocosTextoSuper.toMutableList()
                )
            }
        }

        tempMap.mapValues { (_, pair) ->
            pair.first to pair.second.toList()
        }
    }

    // 6) ► MAPA título -> Vantagem para conseguirmos cruzar com o state
    //     (o título é sempre a PRIMEIRA LINHA do bloco)
    val tituloParaVant: Map<String, Vantagem> = remember(listaFiltradaParaGrupo, modoSupers) {
        val base = mutableMapOf<String, Vantagem>()
        // do grupo filtrado normal/super
        listaFiltradaParaGrupo.forEach { base[it.nome] = it }
        // adiciona também as super-detalhadas (se existirem) para bater o título
        if (modoSupers) {
            AppData.superVantagensParaDetalhe.forEach { base[it.nome] = it }
        }
        base
    }

    // 7) Estado visual de expansão por categoria + rolagem até destaque
    val expandedState = remember {
        mutableStateMapOf<String, Boolean>().apply {
            todasCategorias.keys.forEach { put(it, false) }
        }
    }
    val listState = rememberLazyListState()

    LaunchedEffect(highlightedName, todasCategorias) {
        if (highlightedName.isNotEmpty()) {
            val targetCat: String? = todasCategorias.entries.firstOrNull { (_, pair) ->
                pair.second.any { bloco -> bloco.lines().first() == highlightedName }
            }?.key
            targetCat?.let { expandedState[it] = true }

            val keysList = mutableListOf<String>()
            todasCategorias.forEach { (cat, pair) ->
                keysList.add("header-$cat")
                if (expandedState[cat] == true) {
                    pair.second.forEach { bloco ->
                        keysList.add(bloco.lines().first())
                    }
                }
            }
            val idx = keysList.indexOf(highlightedName)
            if (idx >= 0) {
                listState.animateScrollToItem(idx)
            }
        }
    }

    // 8) ► Conjuntos para cruzar com o state (já possui / pode selecionar)
    val nomesJaSelecionadas = remember(state.vantagensSelecionadas) {
        state.vantagensSelecionadas.map { it.nome }.toSet()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text(
                            "Voltar",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF050402)
                        )
                    }
                },
                title = { },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF2E3C6)
                )
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            contentPadding = innerPadding,
            modifier = Modifier.fillMaxSize()
        ) {
            todasCategorias.forEach { (cat, pair) ->
                val displayName = pair.first
                val listaBlocos = pair.second

                // Cabeçalho da categoria
                item(key = "header-$cat") {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                val atual = expandedState[cat] ?: false
                                expandedState[cat] = !atual
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = if (expandedState[cat] == true)
                                Icons.Filled.ExpandLess
                            else
                                Icons.Filled.ExpandMore,
                            contentDescription = null
                        )
                    }
                }

                // Itens da categoria
                if (expandedState[cat] == true) {
                    listaBlocos.forEachIndexed { index, bloco ->
                        val linhas = bloco.lines()
                        val titulo = linhas.first()

                        // ► “binding” com o state: já escolhida? requisitos ok?
                        val vant = tituloParaVant[titulo]
                        val jaTem = (titulo in nomesJaSelecionadas)
                        val requisitosOk = vant?.let { state.podeSelecionar(it) } ?: true

                        item(key = "$cat-$titulo-$index") {
                            Column(
                                Modifier
                                    .padding(start = 24.dp, bottom = 16.dp)
                                    .background(
                                        when {
                                            jaTem -> Color(0x11007AFF)     // já possui → leve destaque
                                            requisitosOk -> Color.Transparent // pode pegar
                                            else -> Color(0x11FF0000)       // pendente → leve vermelho
                                        }
                                    )
                                    .padding(start = 4.dp, top = 8.dp, end = 4.dp, bottom = 8.dp)
                            ) {
                                // Linha de título + status
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = titulo,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    // Selo de status à direita
                                    val status = when {
                                        jaTem -> "já selecionada"
                                        requisitosOk -> "requisitos OK"
                                        else -> "requisitos pendentes"
                                    }
                                    Text(
                                        status,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = when {
                                            jaTem -> Color(0xFF0047BA)
                                            requisitosOk -> Color(0xFF2E7D32)
                                            else -> Color(0xFFB00020)
                                        }
                                    )
                                }

                                Spacer(Modifier.height(4.dp))

                                if (linhas.size > 1) {
                                    Text(
                                        text = linhas.drop(1).joinToString("\n"),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontSize = 14.sp
                                    )
                                }

                                Spacer(Modifier.height(12.dp))
                                HorizontalDivider(color = Color.Gray, thickness = 1.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Preview(showBackground = true)
@Composable
fun PreviewApp() {
    val state = remember { CriadorState() }
    UnifiedScreen(
        state = state,
        onOpenVantagensDetail = {},
        onOpenPericiasDetail = {},
        onOpenComplicacoesDetail = {},
        onOpenAtributosDetail = {},
        onOpenListaAncestralidadesDetail = {},
        onOpenListaCompletaEquipamento = {},
        onOpenPoderesDetail = {},
        onOpenSuperPoderesDetail = { _ -> },
        expAttrs = true,
        onToggleAttrs = {},
        expPer = true,
        onTogglePer = {},
        expVants = true,
        onToggleVants = {},
        expResumo = true,
        onToggleResumo = {},
        expPoderes = true,
        onTogglePoderes = {},
        equipamentoCategorias = emptyList(),
        superequipCategorias = emptyList(),
        listaSuperPoderes = emptyList()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun UnifiedScreen(
    state: CriadorState,
    onOpenVantagensDetail: (Any?) -> Unit,
    onOpenPericiasDetail: () -> Unit,
    onOpenComplicacoesDetail: () -> Unit,
    onOpenAtributosDetail: () -> Unit,
    onOpenListaAncestralidadesDetail: () -> Unit,
    onOpenListaCompletaEquipamento: () -> Unit,
    onOpenPoderesDetail: () -> Unit,
    onOpenSuperPoderesDetail: (String) -> Unit,
    expAttrs: Boolean,
    onToggleAttrs: () -> Unit,

    expPer: Boolean,
    onTogglePer: () -> Unit,

    expVants: Boolean,
    onToggleVants: () -> Unit,

    expResumo: Boolean,
    onToggleResumo: () -> Unit,

    expPoderes: Boolean,
    onTogglePoderes: () -> Unit,

    equipamentoCategorias: List<EquipamentoCategoria>,
    superequipCategorias: List<EquipamentoCategoria>,
    listaSuperPoderes: List<SuperPoder>
) {
    var expSupers by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.modoSupers) {
        Log.d("DEBUG", "modoSupers é ${state.modoSupers}")
    }
    var showAllocDialog by rememberSaveable { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    // --- estados para o MEIO-ELFO ---
    var showMeioElfoDialog by rememberSaveable { mutableStateOf(false) }
    var pendingMeioElfoKey by rememberSaveable { mutableStateOf<String?>(null) }
    // ---------------------------------
    val supersLocked = state.criacaoBasicaCongelada

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // ─── Informações iniciais ────────────────────────────────────────────────
        InformacoesSection(
            state = state,
            onUseProgress = { showAllocDialog = true }
        )

        HorizontalDivider(thickness = 1.dp)

        // ─── Ancestralidades ──────────────────────────────────────────────────────
        AncestralidadesSection(
            currentAncestralidade = state.ancestralidade,
            supersLocked = supersLocked,
            onOpenListaAncestralidadesDetail = onOpenListaAncestralidadesDetail,
            onSelectAncestralidade = { nome ->
                val key = nome.uppercase().semAcentos()

                if (key == state.ancestralidade) return@AncestralidadesSection

                if (key == "MEIO-ELFOS") {
                    pendingMeioElfoKey = key
                    showMeioElfoDialog = true
                } else {
                    pendingMeioElfoKey = null
                    state.aplicarAncestralidade(key)
                }
            }
        )

        HorizontalDivider(thickness = 1.dp)

        // ─── Complicações ─────────────────────────────────────────────────────────
        ComplicacoesSection(
            state = state,
            onOpenComplicacoesDetail = onOpenComplicacoesDetail
        )

        HorizontalDivider(thickness = 1.dp)

        // ─── Atributos ────────────────────────────────────────────────────────────
        SectionCard(
            title    = "Atributos",
            expanded = expAttrs,
            onToggle = onToggleAttrs,
            icon     = Icons.Default.FitnessCenter
        ) {
            AtributosContent(state, onOpenAtributosDetail)
        }

        HorizontalDivider(thickness = 1.dp)

        // ─── Perícias ─────────────────────────────────────────────────────────────
        SectionCard(
            title    = "Perícias",
            expanded = expPer,
            onToggle = onTogglePer,
            icon     = Icons.Default.School
        ) {
            PericiasContent(state, onOpenPericiasDetail)
        }

        HorizontalDivider(thickness = 1.dp)

        // ─── Vantagens ────────────────────────────────────────────────────────────
        SectionCard(
            title    = "Vantagens",
            expanded = expVants,
            onToggle = onToggleVants,
            icon     = Icons.Default.Star
        ) {
            VantagensContent(
                state = state,
                multiplosAAHabilitados = state.permiteMultiAntecedenteArcano,
                onOpenVantagensDetail  = onOpenVantagensDetail
            )
        }

        // ─── Poderes (só mostra se já escolheu Arcano) ───────────────────────────
        if (state.vantagensSelecionadas.any { it.nome.keyify().startsWith("ANTECEDENTE ARCANO") }) {
            HorizontalDivider(thickness = 1.dp)

            // ─── Poderes (magias) ─────────────────────────────────────────────────────
            SectionCard(
                title    = "Poderes",
                expanded = expPoderes,
                onToggle = onTogglePoderes,
                icon     = Icons.Default.FlashOn
            ) {
                PoderesSection(
                    state = state,
                    onOpenListaCompletaPoderes = onOpenPoderesDetail
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        HorizontalDivider(thickness = 1.dp)

        // ─── SuperPoderes (modo Supers) ─────────────────────────────────────────
        if (state.modoSupers) {
            SuperPoderesContent(
                state                 = state,
                listaSuperPoderes     = listaSuperPoderes,
                expanded              = expSupers,
                onToggle              = { expSupers = !expSupers },
                onOpenSuperPoderesDetail = onOpenSuperPoderesDetail
            )
        }

        EquipamentoSection(
            dinheiro                 = state.dinheiro,
            pcTotal                  = state.pontosComplicacao,
            pcLivres                 = (state.pontosComplicacao - state.pontosComplicacaoGastos).coerceAtLeast(0),
            recursosPcUsados         = state.cpRecursosStack.size,
            onUsarPontosBonusEmRecursos = {
                val pcLivresLocal =
                    (state.pontosComplicacao - state.pontosComplicacaoGastos).coerceAtLeast(0)

                // Só permite 1 PB em Recursos
                if (pcLivresLocal > 0 && state.cpRecursosStack.isEmpty()) {
                    state.cpRecursosStack.add(Unit)
                    state.pontosComplicacaoGastos += 1
                    state.dinheiro += 500
                }
            },
            onDesfazerPontosBonusEmRecursos = {
                // Só devolve se ainda tiver pelo menos 500 em dinheiro
                if (state.cpRecursosStack.isNotEmpty() && state.dinheiro >= 500) {
                    state.cpRecursosStack.removeAt(state.cpRecursosStack.lastIndex)
                    state.pontosComplicacaoGastos =
                        (state.pontosComplicacaoGastos - 1).coerceAtLeast(0)
                    state.dinheiro -= 500
                }
            },
            onListaCompletaClick     = onOpenListaCompletaEquipamento,
            onEquipamentoDoubleClick = { equipamento ->
                val custo = (equipamento.custo as? JsonPrimitive)
                    ?.content?.toIntOrNull() ?: 0
                if (custo <= state.dinheiro) {
                    state.equipamentosComprados.add(equipamento)
                    state.dinheiro -= custo
                }
            },
            equipamentosComprados    = state.equipamentosComprados,
            onRemoveEquipamentoClick = { equipamento ->
                val custo = (equipamento.custo as? JsonPrimitive)
                    ?.content?.toIntOrNull() ?: 0
                state.equipamentosComprados.remove(equipamento)
                state.dinheiro += custo
            },
            categorias               = equipamentoCategorias,
            superequipCategorias     = superequipCategorias
        )

        Spacer(Modifier.height(16.dp))
        HorizontalDivider(thickness = 3.dp)

        // ─── Resumo ───────────────────────────────────────────────────────────────
        SectionCard(
            title    = "Resumo do Personagem",
            expanded = expResumo,
            onToggle = onToggleResumo,
            icon     = Icons.Default.Description
        ) {
            SummaryContent(state)
        }

        // ─── Diálogo de alocação de progressos ──────────────────────────────────
        if (showAllocDialog) {
            ProgressosDialog(state) {
                state.frozenAdvCount = state.vantagensSelecionadas.size
                state.emProgresso    = true
                showAllocDialog      = false
            }
        }
    }

    // ─── Diálogo especial do MEIO-ELFO ───────────────────────────────────────────
    if (showMeioElfoDialog && pendingMeioElfoKey != null) {
        val key = pendingMeioElfoKey!!
        AlertDialog(
            onDismissRequest = { showMeioElfoDialog = false },
            title   = { Text("Meio-Elfo: escolha sua herança") },
            text    = { Text("Selecione qual benefício você gostaria de herdar:") },
            confirmButton = {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            // herda ADAPTÁVEL (humano) → +1 ponto de vantagem
                            state.aplicarAncestralidade(key)
                            state.pontosVantagem += 1
                            showMeioElfoDialog = false
                        },
                        modifier = Modifier.defaultMinSize(minWidth = 100.dp)
                    ) {
                        Text("ADAPTÁVEL")
                    }
                    Button(
                        onClick = {
                            // herda AGILIDADE d6 (como elfos)
                            state.aplicarAncestralidade(key)
                            state.valoresAtributos["AGILIDADE"]!!.intValue = 6
                            (racialAttrMinMap as MutableMap)[key] =
                                (racialAttrMinMap[key] ?: emptyMap()) + ("AGILIDADE" to 6)
                            state.recalcularPontosAtributo()
                            showMeioElfoDialog = false
                        },
                        modifier = Modifier.defaultMinSize(minWidth = 100.dp)
                    ) {
                        Text("AGILIDADE")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showMeioElfoDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
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
fun ComplicacoesDetailScreen(
    state: CriadorState,
    onBack: () -> Unit,
    mostrarSuper: Boolean
) {
    val context = LocalContext.current

    // Lê todas as complicações do JSON em assets/complicacoes.json
    val listaTodas = remember {
        val jsonString = context.assets.open("complicacoes.json")
            .bufferedReader()
            .use { it.readText() }
        val parser = Json { ignoreUnknownKeys = true; explicitNulls = false }
        parser.decodeFromString(ListSerializer(Complicacao.serializer()), jsonString)
    }

    // Aplica o filtro "Super" conforme o modo selecionado na tela inicial
    val listaFiltrada = remember(mostrarSuper, listaTodas) {
        if (mostrarSuper) listaTodas else listaTodas.filter { !it.origem.equals("SUPER", true) }
    }

    // ► usa o estado global para saber o que já foi escolhido
    val jaEscolhidas = state.complicacoesSelecionadas

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

        items(listaFiltrada) { comp ->
            val tipoSel = jaEscolhidas[comp] // "Menor", "Maior" ou null
            val marcado = tipoSel != null
            val sevRaw = comp.severity.trim()
            val gravidade = when (sevRaw.lowercase()) {
                "both"  -> "Menor/Maior"
                "menor" -> "Menor"
                "maior" -> "Maior"
                else    -> sevRaw.ifBlank { "-" }
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .background(if (marcado) Color(0x11007AFF) else Color.Transparent)
            ) {
                // Linha do título
                Text(
                    buildString {
                        append(comp.name)
                        append(" (")
                        append(gravidade)
                        append(")")
                        if (marcado) append(" — já escolhida: $tipoSel")
                    },
                    fontWeight = FontWeight.SemiBold
                )

                // Descrição (se existir)
                if (comp.description.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = comp.description,
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 14.sp
                    )
                }

                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
            }
        }
    }
}


@Composable
fun PericiasDetailScreen(
    state: CriadorState,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // Mapa de descrições já existente (R.raw.pericias)
    val descricoes by remember {
        mutableStateOf(loadPericiasDescriptions(context, R.raw.pericias))
    }

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
                        .padding(vertical = 12.dp, horizontal = 16.dp)
                ) {
                    Text(
                        text = "Voltar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                HorizontalDivider()
            }
        }

        // ► Usa a lista oficial (Pericia), que conversa com o CriadorState
        items(listaPericias) { per ->
            // Chave p/ descrição
            val rawName = per.nome.removePrefix("*").trim()
            val key = "$rawName (${per.atributo})".uppercase().semAcentos()
            val desc = descricoes[key] ?: "Descrição indisponível."

            // ► Estes dados vêm do state (uso REAL do state)
            val currentRaw = state.rawTotal(per)                        // d0/d4/d6...
            val attrKey    = state.atributoBaseParaPericia(per)
            val atrRaw     = state.valoresAtributos[attrKey]!!.intValue
            val capRaw     = state.periciaCapRaw(per)

            val nextRaw    = if (currentRaw == 0 && per.basica) 4 else currentRaw + 2
            val costNormal = if (nextRaw <= atrRaw) 1 else 2           // 1 até atributo, 2 acima

            // Mínimos por vantagens (inclui opcionais)
            val minimoBasico  = state.minPericiaPorVantagem[per] ?: 0
            val minimoOpcional = state.vantagensSelecionadas
                .flatMap { vant ->
                    vant.requisitos.periciaMinOpcional
                        .filterKeys { it.equals(per.nome, ignoreCase = true) }
                        .values
                }
                .maxOrNull() ?: 0
            val minimoTotal = maxOf(minimoBasico, minimoOpcional)
            val needsMin    = (minimoTotal > 0 && currentRaw in 1 until minimoTotal)

            // Pode aumentar agora (respeitando SP disponíveis, cap, etc.)
            val podeAumentar = state.pontosPericia >= costNormal && nextRaw <= capRaw

            // Status amigável
            val status = buildString {
                append(
                    when (currentRaw) {
                        0 if per.basica -> "Atual: d4 (básica)"
                        0 -> "Atual: —"
                        else -> "Atual: d${currentRaw}"
                    }
                )
                append(" • Próximo: ")
                append(if (nextRaw > capRaw) "— (teto)" else if (currentRaw == 0 && !per.basica) "d4" else "d$nextRaw")
                append(" • Custo: ")
                append(if (nextRaw > capRaw) "—" else "$costNormal SP")
                append(" • Cap: d$capRaw")
                if (minimoTotal > 0) {
                    append(" • Mín.: d$minimoTotal")
                    if (needsMin) append(" (abaixo)")
                }
            }

            // Destaque visual
            val bg = when {
                needsMin -> Color(0x11FF0000)         // vermelho claro
                currentRaw > 0 || per.basica -> Color(0x11007AFF) // azul claro
                else -> Color.Transparent
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .background(bg)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (per.basica) {
                        Text("✯", color = MaterialTheme.colorScheme.error, fontSize = 20.sp)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        text = "${per.nome} (${per.atributo})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        when {
                            needsMin -> "abaixo do mínimo"
                            podeAumentar -> "pode aumentar"
                            else -> ""
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = when {
                            needsMin -> Color(0xFFB00020)
                            podeAumentar -> Color(0xFF2E7D32)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }

                Spacer(Modifier.height(6.dp))
                Text(status, style = MaterialTheme.typography.labelMedium)

                Spacer(Modifier.height(8.dp))
                Text(desc, style = MaterialTheme.typography.bodyMedium, fontSize = 14.sp)

                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
            }
        }
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
fun EquipamentosDetailScreen(
    categorias: List<EquipamentoCategoria>,
    onBack: () -> Unit
) {
        fun JsonElement?.asText(): String? = when (this) {
        is JsonPrimitive -> this.content
        else -> this?.toString()
    }?.takeIf { it.isNotBlank() }

    val mapa =
        remember(categorias) {
            categorias
                .sortedWith(
                    compareBy<EquipamentoCategoria> { it.tipo.lowercase() }
                        .thenBy { it.subtipo.lowercase() }
                        .thenBy { (it.subsubtipo ?: "").lowercase() }
                )
                .groupBy { cat ->
                    val tipoOriginal = cat.tipo
                    val isSuper = cat.origem.equals("SUPER", ignoreCase = true)

                    // Normaliza o rótulo exibido para o grupo
                    val labelTipo = if (isSuper) {
                        if (tipoOriginal.contains("Equipamento Supers", ignoreCase = true)) {
                            "Superequip - Veículos"     // ← fica igual ao padrão, mas com nome claro
                        } else {
                            "Superequip - $tipoOriginal"
                        }
                    } else {
                        tipoOriginal
                    }

                    labelTipo
                }
                .mapValues { (_, porTipo) ->
                    porTipo.groupBy { it.subtipo }.mapValues { (_, porSubtipo) ->
                        porSubtipo.groupBy { it.subsubtipo ?: "" }.mapValues { (_, listaFinal) ->
                            listaFinal.flatMap { it.itens }.sortedBy { it.nome }
                        }
                    }
                }
        }

    // --- ESTADOS DE EXPANSÃO (mantidos) ---
    val expTipo  = remember(mapa) { mapa.keys.associateWith { mutableStateOf(false) } }
    val expSub   = remember(mapa) { mapa.mapValues { (_, sub) -> sub.keys.associateWith { mutableStateOf(false) } } }
    val expSub2  = remember(mapa) {
        mapa.mapValues { (_, sub) ->
            sub.mapValues { (_, sub2) -> sub2.keys.associateWith { mutableStateOf(false) } }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header "Voltar"
        stickyHeader {
            Surface(color = Color.Transparent) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onBack)
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Voltar", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                HorizontalDivider()
            }
        }

        // TIPOS
        mapa.toSortedMap(compareBy { it.lowercase() }).forEach { (tipo, subMapa) ->
            item {
                val et = expTipo.getValue(tipo)
                Column {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { et.value = !et.value }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(tipo, style = MaterialTheme.typography.titleMedium)
                        Icon(
                            imageVector = if (et.value) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (et.value) "Fechar" else "Abrir"
                        )
                    }

                    if (et.value) {
                        // SUBTIPOS
                        subMapa.toSortedMap(compareBy { it.lowercase() }).forEach { (subtipo, sub2Mapa) ->
                            val es = expSub.getValue(tipo).getValue(subtipo)
                            Column(Modifier.padding(start = 8.dp, bottom = 8.dp)) {
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .clickable { es.value = !es.value }
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(subtipo, fontWeight = FontWeight.Bold)
                                    Icon(
                                        imageVector = if (es.value) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = if (es.value) "Fechar" else "Abrir"
                                    )
                                }

                                if (es.value) {
                                    // SUBSUBTIPOS
                                    sub2Mapa.toSortedMap(compareBy { it.lowercase() }).forEach { (subsub, itens) ->
                                        val ess = expSub2.getValue(tipo).getValue(subtipo).getValue(subsub)
                                        Column(Modifier.padding(start = 8.dp, bottom = 4.dp)) {
                                            if (subsub.isNotBlank()) {
                                                Row(
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .clickable { ess.value = !ess.value }
                                                        .padding(vertical = 4.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(subsub)
                                                    Icon(
                                                        imageVector = if (ess.value) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                        contentDescription = if (ess.value) "Fechar" else "Abrir"
                                                    )
                                                }
                                            } else {
                                                ess.value = true // sem subsubtipo: já aberto
                                            }

                                            if (ess.value) {
                                                // LISTA DE ITENS (com DETALHES)
                                                Column(
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .padding(start = 8.dp, bottom = 8.dp)
                                                ) {
                                                    itens.forEach { eq ->
                                                        Column(
                                                            Modifier
                                                                .fillMaxWidth()
                                                                .padding(vertical = 6.dp)
                                                        ) {
                                                            Row(
                                                                Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.SpaceBetween
                                                            ) {
                                                                Text(eq.nome, style = MaterialTheme.typography.bodyLarge)
                                                                eq.custo.asText()?.let { Text(it) }
                                                            }

                                                            // ===== PADRONIZAÇÃO DAS LINHAS =====
                                                            // 1) Linha de "arma" (se aplicável)
                                                            val linhaArma = listOfNotNull(
                                                                eq.dano.asText()?.let { "Dano: $it" },
                                                                eq.pa.asText()?.let { "PA: $it" },
                                                                eq.cdt.asText()?.let { "CdT: $it" },
                                                                eq.distancia.asText()?.let { "Distância: $it" },
                                                                eq.tiros.asText()?.let { "Tiros: $it" },
                                                            ).joinToString("  •  ").takeIf { it.isNotBlank() }

                                                            // 2) Linha geral (peso/força/armadura/aparar)
                                                            val linhaGeral = listOfNotNull(
                                                                eq.peso.asText()?.let { "Peso: $it" },
                                                                eq.forcaMin.asText()?.let { "Força mín.: $it" },
                                                                eq.armadura.asText()?.let { "Armadura: $it" },
                                                                eq.aparar.asText()?.let { "Aparar: $it" },
                                                            ).joinToString("  •  ").takeIf { it.isNotBlank() }

                                                            // 3) Linha veículo — mesma lógica/ordem usada no superquip de veículos
                                                            val linhaVeiculo = listOfNotNull(
                                                                eq.velMaxima.asText()?.let { "Vel. máx.: $it" },
                                                                eq.manobrabilidade.asText()?.let { "Manobrabilidade: $it" },
                                                                eq.tamanho.asText()?.let { "Tamanho: $it" },
                                                                eq.resistencia.asText()?.let { "Resistência: $it" },
                                                                eq.tripulacao.asText()?.let { "Tripulação: $it" },
                                                                eq.blindagem.asText()?.let { "Blindagem: $it" },
                                                                eq.passageiros.asText()?.let { "Passageiros: $it" },
                                                            ).joinToString("  •  ").takeIf { it.isNotBlank() }
                                                            // ===== FIM PADRONIZAÇÃO =====

                                                            linhaArma?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                                                            linhaGeral?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                                                            linhaVeiculo?.let { Text(it, style = MaterialTheme.typography.bodySmall) }

                                                            eq.observacoes.asText()?.takeIf { it.isNotBlank() }?.let {
                                                                Text(it, style = MaterialTheme.typography.bodySmall)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}



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
        grandesResponsabilidades: Boolean
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
                        optGrandesResponsabilidades
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