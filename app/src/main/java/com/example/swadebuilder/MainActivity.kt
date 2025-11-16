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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
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
    val custo: Int,      // custo total em PP (base + modificadores)
    val baseCost: Int,   // quanto foi gasto na barrinha (sem modificadores)
    val poderId: String  // id canônico no ledger, tipo "sp_res", "sp_aparar" etc
)

private val json = Json {
    ignoreUnknownKeys = true
}

// === NOVO: toggle global para múltiplos Antecedentes Arcanos ===
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

        // Preenche basicasVantagens e superVantagens a partir desse JSON
        AppData.basicasVantagens          = todasVantagens.filter { it.origem.equals("BASICO", true) }
        AppData.superVantagens = todasVantagens.filter {
            it.origem.equals("SUPER", ignoreCase = true)
        }

        // Guarda a lista completa localmente
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
            val context = LocalContext.current
            val activity = (context as? ComponentActivity)
            var mostrouTelaInicial by rememberSaveable { mutableStateOf(true) }
            var showExitDialog     by rememberSaveable { mutableStateOf(false) }


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
                        .background(MaterialTheme.colorScheme.surface)  // ✅ fundo agora vem do tema
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
                                    // Aqui passamos as duas listas: básico e super
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
                                            title = {},
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
                                                // Adiciona o CoroutineScope aqui
                                                val scope = rememberCoroutineScope()

                                                IconButton(onClick = {
                                                    // Lança a geração do PDF em uma thread de background (IO)
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

                                                            // ===== NOVOS CAMPOS (SUPERS) =====
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
                                                            vantagensDePoder        = state.vantagensDePoder.toSet(),
                                                            gastosPorPoder          = state.gastosPorPoder.toMap(),
                                                            limiteDePoderDaCampanha = state.limiteDePoderDaCampanha,
                                                            anotacoes              = state.anotacoes
                                                        )
                                                        salvarEExibirFichaPdf(this@MainActivity, personagem)
                                                    }
                                                }) {
                                                    Icon(Icons.Default.Print, contentDescription = "Imprimir ficha")
                                                }

                                                IconButton(onClick = {
                                                    // Lança o salvamento em uma thread de background (IO)
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

                                                            // ===== MODO SUPER =====
                                                            modoSupers              = state.modoSupers,
                                                            modoSuperequip          = state.modoSuperequip,
                                                            modoSuperComplicacoes   = state.modoSuperComplicacoes,

                                                            // Snapshot por conveniência
                                                            superpoderesComprados   = state.superPoderesComprados.map { it.nome },

                                                            // ===== PERSISTÊNCIA DOS CAMPOS NOVOS =====
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

                                                            // ===== ANOTAÇÕES LIVRES =====
                                                            anotacoes               = state.anotacoes
                                                        )

                                                        state.idAtual = personagemId
                                                        StorageUtils.salvarPersonagem(context, salvo)

                                                        // Volta para a Main thread para mostrar o Toast
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
                                }
                            ) { innerPadding ->
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
                                                state  = state,
                                                onBack = { showSuperDetail = false }
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
                                                onOpenSuperPoderesDetail         = { showSuperDetail           = true },
                                                onHelpSuperClick                 = { },

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
                        }
                    }
                }
            }
        }
    }
}

fun Int.toDiceString(): String =
    if (this <= 12) "d$this" else "d12+${(this - 12)}"

// === Helpers de exibição/compat (veículos) ===

// ALIAS LEGADOS para veículos (evitam "Unresolved reference" em código antigo)
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
    // 1) Cria o arquivo na pasta externa interna do app
    val pdfFile = File(context.getExternalFilesDir(null), "ficha_preenchida.pdf")

    // 2) Gera o PDF com nossas linhas
    gerarFichaEmPdf(pdfFile, dadosDoPersonagem)

    // 3) Obtém um URI protegido pelo FileProvider
    val uri: Uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        pdfFile
    )

    // 4) Envia Intent para visualizar
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

    // ---------- Helpers compartilhados ----------
    // Nome bonitinho da ancestralidade (igual ao resumo)
    val ancestralidadeNome: String = listaAncestralidadesJson
        .firstOrNull { it.nome.keyify() == personagem.ancestralidade }
        ?.nome ?: personagem.ancestralidade

    // Vantagens por nome.keyify (para regras de Aparar / Resistência / Movimento)
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

        // No save a gente não guarda o grau de Lento, então tratamos como 1 passo se existir.
        val lentoPenalty = if (temComp("LENTO")) 1 else 0
        val idosoPenalty = if (temComp("IDOSO")) 1 else 0
        val obesoPenalty = if (temComp("OBESO")) 1 else 0
        val ligeiroBonus =
            if (vantagensNomeKey.any { it == "LIGEIRO" }) 2 else 0

        return (base - racialPenalty - lentoPenalty - idosoPenalty - obesoPenalty + ligeiroBonus)
            .coerceAtLeast(0)
    }

    // Mesmo algoritmo de steps de supers do CriadorState
    fun applySuperStepsFrom(rawStart: Int, steps: Int): Int {
        var raw = rawStart
        var remaining = steps.coerceAtLeast(0)

        // CORREÇÃO: se a perícia/atributo está abaixo de d4 (0),
        // o primeiro passo de Superperícia/Superatributo
        // já leva direto para d4 e consome 1 passo.
        if (raw < 4 && remaining > 0) {
            raw = 4
            remaining -= 1
        }

        // Depois disso, seguimos a regra padrão:
        // até d12: +2 por passo; acima de d12: +1 por passo.
        repeat(remaining) {
            raw += if (raw < 12) 2 else 1
        }

        // Garante que nada fique abaixo de d4.
        return raw.coerceAtLeast(4)
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

    // ---------- IDENTIDADE ----------
    lines += "Identidade"
    lines += "Nome: ${personagem.nome.ifBlank { "(sem nome)" }}"
    lines += "Ancestralidade: $ancestralidadeNome"
    lines += ""

    // ---------- DERIVADOS ----------
    lines += "Atributos derivados"
    lines += "Aparar: $aparar"
    lines += "Resistência: $resistenciaTexto"
    lines += "Tamanho: $tamanho"
    lines += "Movimento: $mov"
    if (armadura > 0) {
        lines += "Armadura: $armadura"
    }
    lines += ""

    // ---------- ATRIBUTOS ----------
    lines += "Atributos"
    lines += listaAtributos.joinToString(", ") { attrKey ->
        val label = mapaAtributosDisplay[attrKey] ?: attrKey
        val valor = personagem.atributos[attrKey] ?: 4
        "$label d$valor"
    }
    lines += ""

    // ---------- PERÍCIAS (mesma lógica do resumo / ficha) ----------
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

    // ---------- RECURSOS & EQUIPAMENTOS (igual resumo) ----------
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

    // ---------- VANTAGENS ----------
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

    // ---------- COMPLICAÇÕES ----------
    lines += "Complicações"
    lines += if (personagem.complicacoes.isEmpty()) {
        "– Nenhuma"
    } else {
        // Aqui temos só IDs; imprimimos como vieram salvos.
        personagem.complicacoes.joinToString(", ")
    }
    lines += ""

    // ---------- PODERES ARCANOS (só se houver) ----------
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

    // ---------- SUPERPODERES (só se modo supers ativo) ----------
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

    // ---------- ANOTAÇÕES (se houver) ----------
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
    // A4 aproximado: 595 × 842 pontos
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()

    // Margens em pontos
    val marginLeft   = 40f
    val marginRight  = 40f
    val marginTop    = 50f
    val marginBottom = 40f

    // Configura Paint base e altura de linha
    val paint = Paint().apply { textSize = 12f }
    val fm = paint.fontMetrics
    val lineHeight = fm.descent - fm.ascent + fm.leading

    // Inicia primeira página
    var page = doc.startPage(pageInfo)
    var canvas = page.canvas
    var y = marginTop

    // Helper: cria nova página e reseta o Y
    fun newPage() {
        doc.finishPage(page)
        page = doc.startPage(pageInfo)
        canvas = page.canvas
        y = marginTop
    }

    // Helper: desenha um texto, quebrando em várias linhas se necessário
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

    // 1) Título (sem linhas separadoras)
    val titlePaint = Paint(paint).apply {
        textSize = 16f
        isFakeBoldText = true
    }
    val title = "Ficha de ${personagem.nome}"

    // Se o título não couber na página atual, quebra antes
    val titleFm = titlePaint.fontMetrics
    val titleHeight = titleFm.descent - titleFm.ascent + titleFm.leading
    if (y + titleHeight > pageInfo.pageHeight - marginBottom) {
        newPage()
    }
    canvas.drawText(title, marginLeft, y, titlePaint)
    y += titleHeight + 12f  // respiro generoso após o título

    // 2) Corpo do texto (cada linha d buildSummaryLines é “wrapped”)
    val lines = buildSummaryLines(personagem)
    for (linha in lines) {
        drawWrapped(linha)
    }

    // 3) Finaliza última página e grava
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

    // penalidade de Idoso
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
            + ligeiroBonus)
        .coerceAtLeast(0)
}

fun CriadorState.valorAparar(): Int {
    val perLutar = listaPericias.firstOrNull { it.nome.equals("Lutar", ignoreCase = true) }
    val lutarRaw = perLutar?.let { rawTotalComSupers(it) } ?: 0   // << usa supers
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
    // base (Vigor/Tamanho/etc.) + bônus vindo de superpoder
    return valorResistenciaBase() + bonusResFromPower
}

fun CriadorState.valorArmaduraEfetiva(): Int {
    val armorFromEquipment = armadura
    val melhorExterna = kotlin.math.max(armorFromPower, armorFromEquipment)
    return (melhorExterna + naturalArmorFromRace).coerceAtLeast(0)
}

fun CriadorState.adicionarVantagemPorSuper(v: Vantagem): Boolean {
    // Recusa LENDÁRIAS quando a origem é SUPER
    if (v.categoria == Categoria.LENDARIAS) return false

    // Ignora SOMENTE Estágio reaproveitando o mecanismo do “Nasce Um Herói”
    val progressoAnterior = overrideStageForVantagem
    overrideStageForVantagem = "Lendário"

    val permitido = podeSelecionar(v) // respeita atributos, perícias, outras vantagens, observações
    overrideStageForVantagem = progressoAnterior

    if (!permitido) return false

    // Marca origem="SUPER" na persistência
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

private const val TOTAL_PROGRESS_LIMIT = 50
val dynamicStageCaps = listaDeEstagios.mapIndexed { idx, st ->
    val prevMax = listaDeEstagios.getOrNull(idx - 1)?.maxProgress ?: 0
    if (idx < listaDeEstagios.lastIndex)
        st.maxProgress - prevMax
    else
        (TOTAL_PROGRESS_LIMIT - prevMax).coerceAtLeast(0)
}

class CriadorState {
    var modoSupers by mutableStateOf(false)
    var modoSuperComplicacoes by mutableStateOf(false)
    var modoSuperequip by mutableStateOf(false)
    var grandesResponsabilidades by mutableStateOf(false)
    companion object { const val BASE_SP_POOL = 15 }
    var maisPontosPericias by mutableStateOf(true)
    var cartaSelvagem       by mutableStateOf(true)
    var dinheiro by mutableIntStateOf(500)
    val poderesSelecionados = mutableStateListOf<String>()
    val equipamentosComprados = mutableStateListOf<EquipamentoItem>()
    var heroisSemArmadura by mutableStateOf(false)
    val cpRecursosStack = mutableStateListOf<Unit>()
    private val _maxedTraits = mutableStateListOf<String>()
    val maxedTraits: List<String> get() = _maxedTraits
    var idAtual by mutableStateOf<String?>(null)

    // Campo de anotações livres do jogador
    var anotacoes by mutableStateOf("")

    val comprasPpPorEstagio = mutableStateMapOf<String, Int>().apply {
        listaDeEstagios.forEach { this[it.nome] = 0 }
    }

    val superPoderesComprados = mutableStateListOf<PurchasedPower>()
    var superNivelCampanha by mutableStateOf<Int?>(null)
    var usarSemPontosDePoder by mutableStateOf(false)

    var superPontosTotais by mutableIntStateOf(0)
    var superPontosDisponiveis by mutableIntStateOf(0)
    var superLimite by mutableIntStateOf(0)
    var superLimitePorPoder by mutableIntStateOf(0)
    var idPoderFavorecido by mutableStateOf<String?>(null)
    val limitePorPoderPadrao: Int
        get() = kotlin.math.floor(superPontosTotais / 3.0).toInt()
    val limiteFavorecido: Int
        get() = kotlin.math.ceil(superPontosTotais / 2.0).toInt()
    var limiteDePoderDaCampanha by mutableIntStateOf(Int.MAX_VALUE)

    // --- Fases do fluxo: supers depois progresso---
    var faseSupersAtiva by mutableStateOf(false)  // true depois que o jogador CONFIRMA o nível I–V

    val superAtributoIncs = mutableStateMapOf<String, Int>()
    val superPericiaIncs = mutableStateMapOf<String, Int>()
    var bonusPararFromPower by mutableIntStateOf(0)
    var bonusResFromPower  by mutableIntStateOf(0)
    var armorFromPower     by mutableIntStateOf(0)
    val vantagensDePoder   = mutableStateSetOf<String>()
    val gastosPorPoder     = mutableStateMapOf<String, Int>()
    var naturalArmorFromRace by mutableIntStateOf(0)

    fun applySuperStepsFrom(rawStart: Int, steps: Int): Int {
        var raw = rawStart
        var remaining = steps.coerceAtLeast(0)

        // CORREÇÃO: se a perícia/atributo está abaixo de d4 (0),
        // o primeiro passo de Superperícia/Superatributo
        // já leva direto para d4 e consome 1 passo.
        if (raw < 4 && remaining > 0) {
            raw = 4
            remaining -= 1
        }

        // Depois disso, seguimos a regra padrão:
        // até d12: +2 por passo; acima de d12: +1 por passo.
        repeat(remaining) {
            raw += if (raw < 12) 2 else 1
        }

        // Garante que nada fique abaixo de d4.
        return raw.coerceAtLeast(4)
    }


    fun atributoRawComSupers(attrKey: String): Int {
        return valoresAtributos[attrKey]?.intValue ?: 4
    }


    /** Respeita o teto de mitigação por supers (clampa apenas a soma dos componentes de supers) */
    private fun clampMitigacaoSupers() {
        val soma = armorFromPower + bonusResFromPower
        if (soma > limiteDePoderDaCampanha) {
            val excesso = soma - limiteDePoderDaCampanha
            // prioridade: reduzir primeiro armorFromPower, depois bonusResFromPower
            val reduzirArmor = excesso.coerceAtMost(armorFromPower)
            armorFromPower -= reduzirArmor
            val rest = excesso - reduzirArmor
            if (rest > 0) bonusResFromPower = (bonusResFromPower - rest).coerceAtLeast(0)
        }
    }

    /** Facilita adicionar/remover efeitos de um PoderId no ledger */
    fun registrarGastoDePoder(poderId: String, custo: Int) {
        val atual = gastosPorPoder[poderId] ?: 0
        gastosPorPoder[poderId] = atual + custo
        superPontosDisponiveis = (superPontosTotais - gastosPorPoder.values.sum()).coerceAtLeast(0)
    }

    fun desfazerGastoDePoder(poderId: String, custo: Int) {
        val atual = (gastosPorPoder[poderId] ?: 0) - custo
        if (atual <= 0) gastosPorPoder.remove(poderId) else gastosPorPoder[poderId] = atual
        superPontosDisponiveis = (superPontosTotais - gastosPorPoder.values.sum()).coerceAtLeast(0)
    }

    fun updateBonusPararFromPower(value: Int) { bonusPararFromPower = value.coerceAtLeast(0) }
    fun updateBonusResFromPower (value: Int) { bonusResFromPower  = value.coerceAtLeast(0); clampMitigacaoSupers() }
    fun updateArmorFromPower    (value: Int) { armorFromPower     = value.coerceAtLeast(0); clampMitigacaoSupers() }

    fun rawTotalComSupers(per: Pericia): Int {
        val base = rawTotal(per)
        val incs = superPericiaIncs[per.nome.keyify()] ?: 0
        return applySuperStepsFrom(base, incs)
    }

    var regraMultiplosIdiomas by mutableStateOf(false)

    // --- NOVO BLOCO: Controle de compra de Vantagens por XP ---
    var pvFromXpOutstanding by mutableIntStateOf(0)          // PV pendente vindo de XP
    var overrideStageForVantagem by mutableStateOf<String?>(null) // estágio de origem do PV
    var openVantagensAfterGrant by mutableStateOf(false)     // sinal pra abrir tela de vantagens


    fun comprarSuperPoder(
        nome: String,
        custo: Int,
        baseCost: Int = custo,
        poderId: String = "sp_${nome.keyify()}",
        registrarNoLedger: Boolean = true
    ) {
        // só compra se houver espaço e pontos disponíveis
        if (superPoderesComprados.size < superLimite && superPontosDisponiveis >= custo) {
            superPoderesComprados.add(
                PurchasedPower(
                    nome = nome,
                    custo = custo,
                    baseCost = baseCost,
                    poderId = poderId
                )
            )

            if (registrarNoLedger) {
                registrarGastoDePoder(poderId, custo)
            }
        }
    }

    fun removerSuperPoder(
        poder: PurchasedPower,
        desfazerNoLedger: Boolean = true
    ) {
        if (superPoderesComprados.remove(poder)) {
            if (desfazerNoLedger) {
                desfazerGastoDePoder(poder.poderId, poder.custo)
            }
        }
    }

    fun grantVantagemPointFromXp(stageName: String) {
        check(progressosDisponiveis > 0) { "Sem XP disponível." }

        // Marca o XP como gasto neste estágio
        stageXpSpent[stageName] = stageXpSpent.getValue(stageName) + 1
        progressosDisponiveis -= 1

        // Concede um ponto de vantagem
        pontosVantagem += 1
        pvFromXpOutstanding += 1

        // Marca que este PV veio deste estágio
        overrideStageForVantagem = stageName

        // Sinaliza para abrir a tela de Vantagens
        openVantagensAfterGrant = true
    }


    fun maxComprasPpAteAgora(): Int {
        return listaDeEstagios.indexOf(estagioAtual()) + 1
    }

    private fun selecionarPontosDePoder(v: Vantagem) {
        val estagio = estagioAtual().nome
        val totalFeitas = comprasPpPorEstagio.values.sum()

        // quantidade permitida = uma por estágio até o atual
        if (totalFeitas >= maxComprasPpAteAgora()) return

        val feitasNoEstagio = comprasPpPorEstagio[estagio] ?: 0
        comprasPpPorEstagio[estagio] = feitasNoEstagio + 1

        // aplica os PP ganhos: 5 nas 4 primeiras, depois 2
        val ganho = if (totalFeitas < 4) 5 else 2
        bonusPoderExtra += ganho

        vantagensSelecionadas += v
    }

    fun removerPontosDePoder(v: Vantagem) {
        if (!vantagensSelecionadas.remove(v)) return

        val totalAntes = comprasPpPorEstagio.values.sum()
        if (totalAntes == 0) return

        val estagio = estagioAtual().nome
        val feitas = comprasPpPorEstagio[estagio] ?: 0
        if (feitas > 0) {
            comprasPpPorEstagio[estagio] = feitas - 1
        } else {
            // fallback: remove de qualquer estágio (último com compra > 0)
            val fallback = comprasPpPorEstagio.entries.lastOrNull { it.value > 0 }
            fallback?.let {
                comprasPpPorEstagio[it.key] = it.value - 1
            }
        }

        val ganhoRemovido = if (totalAntes <= 4) 5 else 2
        bonusPoderExtra = (bonusPoderExtra - ganhoRemovido).coerceAtLeast(0)
    }

    fun comprarPontoDePoder(v: Vantagem) {
        if (!podeSelecionar(v)) return
        selecionarPontosDePoder(v)
        vantagensSelecionadas += v
    }

    val comprasAttrPorEstagio = mutableStateMapOf<String, Int>().apply {
        listaDeEstagios.forEach { this[it.nome] = 0 }
    }

    fun identifyMaxedTraits() {
        _maxedTraits.clear()

        listaAtributos.forEach { attrKey ->
            val current    = valoresAtributos[attrKey]?.intValue ?: return@forEach
            val maxAllowed = atributoMaxRaw(attrKey)
            if (current == maxAllowed) {
                _maxedTraits.add(attrKey)
            }
        }

        listaPericias.forEach { per ->
            val current    = rawTotal(per)
            val maxAllowed = periciaCapRaw(per)
            if (current == maxAllowed) {
                _maxedTraits.add(per.nome.keyify())
            }
        }
    }

    fun applyVantagemDinheiro(v: Vantagem) {
        when (v.nome.trim().uppercase()) {
            "RICO"          -> dinheiro += 1000
            "PODRE DE RICO" -> dinheiro += 1500
        }
    }

    fun removeVantagemDinheiro(vant: Vantagem) {
        val key = vant.nome.trim().uppercase()
        val amount = when (key) {
            "RICO"          -> 1000
            "PODRE DE RICO" -> 1500
            else            -> 0
        }
        if (amount <= 0) return

        while (dinheiro < amount && equipamentosComprados.isNotEmpty()) {
            val eq = equipamentosComprados.removeAt(equipamentosComprados.lastIndex)
            val custo = (eq.custo as? JsonPrimitive)
                ?.content
                ?.toIntOrNull()
                ?: 0
            dinheiro += custo
        }

        dinheiro = (dinheiro - amount).coerceAtLeast(0)
    }

    val minAttrPorVantagem by derivedStateOf {
        val resultado = mutableMapOf<String, Int>()
        vantagensSelecionadas.forEach { vant ->
            vant.requisitos.atributoMin.forEach { (atributo, valorMin) ->
                val atual = resultado[atributo]
                if (atual == null || valorMin > atual) {
                    resultado[atributo] = valorMin
                }
            }
        }
        resultado.toMap()
    }

    val minPericiaPorVantagem: Map<Pericia, Int> by derivedStateOf {
        vantagensSelecionadas.flatMap { vant ->
            // 1) “Obrigatórias”
            val obrigatorias = vant.requisitos.periciaMin   // se for null, vira um Map vazio
                .mapNotNull { (nomeRaw, min) ->
                    val chaveNorm = nomeRaw.uppercase().semAcentos().trim()
                    listaPericias
                        .firstOrNull { it.nome.uppercase().semAcentos() == chaveNorm }
                        ?.let { per -> per to min }
                }

            // 2) “Opcionais”
            val opcionais = vant.requisitos.periciaMinOpcional   // se null, vira Map vazio
                .mapNotNull { (nomeRaw, min) ->
                    val chaveNorm = nomeRaw.uppercase().semAcentos().trim()
                    listaPericias
                        .firstOrNull { it.nome.uppercase().semAcentos() == chaveNorm }
                        ?.let { per -> per to min }
                }

            // 3) “Arma Predileta”
            val fav = run {
                val choiceSnapshot = vant.choice
                if (
                    vant.nome.trim().equals("Arma Predileta", ignoreCase = true)
                    && choiceSnapshot != null
                ) {
                    val key = choiceSnapshot.uppercase().semAcentos().trim()
                    listaPericias
                        .firstOrNull { it.nome.uppercase().semAcentos() == key }
                        ?.let { per -> listOf(per to 8) }
                        .orEmpty()
                } else {
                    emptyList()
                }
            }

            obrigatorias + opcionais + fav
        }
            .groupBy({ it.first }, { it.second })
            .mapValues { (_, listaMinimos) ->
                listaMinimos.maxOrNull() ?: 0
            }
    }

    fun atributoBaseParaPericia(per: Pericia): String {
        return if (per.nome.equals("Atletismo", ignoreCase = true)
            && vantagensSelecionadas.any { it.nome.keyify() == "BRUTAMONTES" }
        ) {
            "FORCA"
        } else {
            per.atributo
        }
    }

    private val incompatibilidades: Map<String, Set<String>> = mapOf(
        "LENTO"   to setOf("LIGEIRO"),
        "LIGEIRO" to setOf("LENTO"),
        "OBESO"      to setOf("MUSCULOSO"),
        "MUSCULOSO"  to setOf("OBESO"),
        "POBREZA"        to setOf("RICO", "PODRE DE RICO"),
        "RICO"           to setOf("POBREZA"),
        "PODRE DE RICO"  to setOf("POBREZA")
    )

    val poderSlotsPorArcano = mutableStateMapOf<String, SnapshotStateList<String?>>()

    // Mapa por Arcano (versionKey: "dom", "magia"...), pilha de compras; cada compra guarda a lista de IDs escolhidos
    val novosPoderesStacksPorArcano = mutableStateMapOf<String, MutableList<List<String>>>()

    // Registra uma compra de Novos Poderes (na criação)
    fun registrarNovosPoderes(versionKey: String, escolhas: List<String>) {
        val pilha = novosPoderesStacksPorArcano.getOrPut(versionKey) { mutableListOf() }
        pilha.add(escolhas)
    }

    // Desfaz a última compra de Novos Poderes daquele arcano (na criação)
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun desfazerUltimosNovosPoderes(versionKey: String, initialSlots: Int) {
        val pilha = novosPoderesStacksPorArcano[versionKey] ?: return
        if (pilha.isEmpty()) return

        // 1) pegue a última compra e remova esses poderes dos slots
        val ultima = pilha.removeLast()
        val slots = poderSlotsPorArcano[versionKey] ?: return

        // Remover as ocorrências desses IDs; preferimos do fim para o começo
        ultima.forEach { poderId ->
            val idx = slots.indexOfLast { it == poderId }
            if (idx >= 0) slots[idx] = null
        }

        // 2) Compactar: remover nulls finais além do mínimo exigido pelos slots iniciais + compras restantes
        val extrasAinda = pilha.sumOf { it.size }
        val tamanhoMinimo = (initialSlots + extrasAinda).coerceAtLeast(initialSlots)

        while (slots.size > tamanhoMinimo && slots.lastOrNull() == null) {
            slots.removeLast()
        }

        // Atualiza a lista plana
        poderesSelecionados.apply {
            clear()
            addAll(slots.filterNotNull())
        }
    }

    var permiteMultiAntecedenteArcano by mutableStateOf(false)
    // HABILITA A REGRA OPCIONAL DE ESPECIALIZAÇÃO DE PERÍCIAS
    var usarEspecializacoesDePericia by mutableStateOf(false)

    // Mapa com as especializações definidas por perícia (chave = nome da perícia)
    val especializacoesPorPericia: SnapshotStateMap<String, com.example.swadebuilder.model.EspecializacoesDto> = mutableStateMapOf()

    var bonusPoderExtra by mutableIntStateOf(0)

    var obesoBonusSize by mutableIntStateOf(0)
    var obesoMalusMov by mutableIntStateOf(0)

    var idosoBonusSp by mutableIntStateOf(0)

    var jovemAutoPequeno by mutableStateOf(false)

    private var jovemMalusPa by mutableIntStateOf(0)
    private var jovemMalusSp by mutableIntStateOf(0)

    fun syncFromCPRefund(pa: Boolean = false, sp: Boolean = false) {
        if (pa) recalcularPontosAtributo()
        if (sp) rebuildAllPericiaStacks()
    }

    val cpPaStack = mutableStateListOf<String>()
    val cpPvStack = mutableStateListOf<Unit>()
    val cpSpStack = mutableStateListOf<Unit>()

    private val totalSpPool: Int
        get() {
            val base = if (maisPontosPericias) BASE_SP_POOL else BASE_SP_POOL - 3
            return (base + cpSpStack.size + idosoBonusSp - jovemMalusSp)
                .coerceAtLeast(0)
        }

    val pontosPericia by derivedStateOf {
        val used = spCostStackPorPericia.values.sumOf { it.sum() } +
                compCostStackPorPericia.values.sumOf { it.sum() }
        totalSpPool - used
    }


    var nomePersonagem by mutableStateOf("")

    var progresso by mutableIntStateOf(0)
    fun estagioAtual(): Estagio {
        return listaDeEstagios.first { progresso in it.minProgress .. it.maxProgress }
    }

    private fun effectiveProgressoParaVantagens(): Int {
        val stName = overrideStageForVantagem ?: return progresso
        val st = listaDeEstagios.firstOrNull { it.nome.equals(stName, ignoreCase = true) }
        // usamos o minProgress do estágio travado para satisfazer checagens de “estágio mínimo”
        return st?.minProgress ?: progresso
    }

    private fun currentProgressStageIndex(): Int {
        val caps = listaDeEstagios.mapIndexed { idx, st ->
            val prevMax = listaDeEstagios.getOrNull(idx - 1)?.maxProgress ?: 0
            if (idx < listaDeEstagios.lastIndex)
                st.maxProgress - prevMax
            else
                (TOTAL_PROGRESS_LIMIT - prevMax).coerceAtLeast(0)
        }
        val firstOpen = caps.indexOfFirst { cap ->
            val nome = listaDeEstagios[caps.indexOf(cap)].nome
            (stageXpSpent[nome] ?: 0) < cap
        }
        return if (firstOpen >= 0) firstOpen else listaDeEstagios.lastIndex
    }

    var ancestralidade by mutableStateOf("HUMANOS")

    val vantagensAutomaticas = mutableStateListOf<String>()

    var pontosVantagem by mutableIntStateOf(0)

    val desvantagensAutomaticas = mutableStateListOf<String>()

    var frozenAdvCount by mutableIntStateOf(0)

    var pontosAtributo by mutableIntStateOf(5)

    var armadura by mutableIntStateOf(0)

    var nasceUmHeroi by mutableStateOf(false)

    val valoresAtributos = listaAtributos.associateWith { mutableIntStateOf(4) }

    val complicacoesSelecionadas: SnapshotStateMap<Complicacao, String?> = mutableStateMapOf()

    val pontosComplicacao: Int
        get() {
            // Complicações automáticas não contam
            val autoKeys = desvantagensAutomaticas
                .map { it.substringBefore("(").trim().keyify() }
                .toSet()

            var total = 0
            var temMaior = false

            // soma bruta (sem teto) e detecta se há ao menos 1 Maior
            for ((comp, tipo) in complicacoesSelecionadas) {
                if (comp.id.keyify() in autoKeys) continue
                when (tipo) {
                    "Maior" -> { total += 2; temMaior = true }
                    "Menor" -> { total += 1 }
                }
            }

            // regra: com Grandes Responsabilidades + pelo menos 1 Maior → teto 6; senão teto 4
            val teto = if (grandesResponsabilidades && temMaior) 6 else 4
            return minOf(total, teto)
        }

    val vantagensSelecionadas      = mutableStateListOf<Vantagem>()

    fun podeSelecionar(v: Vantagem): Boolean {
        val key = v.nome.keyify()

        // 1) Verifica limite de “Pontos de Poder” por estágio
        if (v.nome.contains("Pontos de Poder", ignoreCase = true)) {
            val totalFeitas = comprasPpPorEstagio.values.sum()
            val maxPermitidas = maxComprasPpAteAgora()
            if (totalFeitas >= maxPermitidas) return false
        }

        // 2) Restrição “Antecedente Arcano”
        if (key.startsWith("antecedente arcano")) {
            if (!permiteMultiAntecedenteArcano) {
                // MODO LEGADO: segue permitindo apenas 1 AA no total (como era antes)
                val anyArcano = vantagensSelecionadas.any { it.nome.keyify().startsWith("antecedente arcano") }
                if (anyArcano && vantagensSelecionadas.none { it.nome.keyify() == key }) {
                    return false
                }
            } else {
                // MODO NOVO: permite múltiplos AAs, mas sem duplicar o MESMO subtipo
                // Se você estiver exibindo as variantes com IDs específicas (antecedente_arcano_dom, etc.),
                // basta impedir duplicata de ID:
                val jaTemMesmoId = vantagensSelecionadas.any { it.id == v.id }
                if (jaTemMesmoId) return false
                // Se em algum fluxo ainda for o seletor base com choice, impede MESMA choice repetida:
                if (v.id == "antecedente_arcano" && v.choice != null) {
                    val jaTemMesmaChoice = vantagensSelecionadas.any {
                        it.id == "antecedente_arcano" && it.choice?.keyify() == v.choice?.keyify()
                    }
                    if (jaTemMesmaChoice) return false
                }
            }
        }

        // 3) Regras de “PROFISSIONAL” e “ESPECIALISTA”
        if (key == "profissional" || key == "especialista") {
            val choiceSeguro = v.choice

            // 3.1 Se requer escolha, evite repetir a combinação nome+escolha
            if (v.requiresChoice && choiceSeguro != null) {
                val already = vantagensSelecionadas.any {
                    it.nome.keyify() == key &&
                            it.choice?.keyify() == choiceSeguro.keyify()
                }
                if (already) return false
            }

            // 3.2 Regra extra para “ESPECIALISTA”: requer “profissional” com MESMA escolha
            if (key == "especialista" && choiceSeguro != null) {
                val profExist = vantagensSelecionadas.any {
                    it.id == "profissional" && it.choice?.keyify() == choiceSeguro.keyify()
                }
                if (!profExist) return false
            }

            // 3.3 Se não tem escolha (choiceSeguro == null), basta atender atributo/perícia máximo
            if (choiceSeguro == null) {
                val anyMaxAttr = listaAtributos.any { a ->
                    valoresAtributos[a]!!.intValue == atributoMaxRaw(a)
                }
                val anyMaxPer = listaPericias.any { p ->
                    rawTotal(p) == periciaCapRaw(p)
                }
                return anyMaxAttr || anyMaxPer
            }

            // 3.4 Caso escolha != null: só pode se atributo ou perícia estiver no teto
            val choiceKey = choiceSeguro.keyify()
            return if (listaAtributos.contains(choiceKey)) {
                valoresAtributos[choiceKey]!!.intValue == atributoMaxRaw(choiceKey)
            } else {
                val per = listaPericias.first { it.nome.keyify() == choiceKey }
                rawTotal(per) == periciaCapRaw(per)
            }
        }

        val ignorarEstagioPorNasce =
            (nasceUmHeroi && !emProgresso && pvFromXpOutstanding == 0)
        if (!ignorarEstagioPorNasce) {
            // 4) Checa requisito de estágio mínimo:
            listaDeEstagios
                .firstOrNull { it.nome.equals(v.requisitos.estagio, ignoreCase = true) }
                ?.let { estReqObj ->
                    // Se o progresso atual for menor que o mínimo exigido para este estágio, bloqueia
                    if (effectiveProgressoParaVantagens() < estReqObj.minProgress) return false
                }
        }

        // 5) Checa “vantagens_previas” comparando por ID (com alias para "antecedente_arcano")
        if (v.requisitos.vantagensPrevias.isNotEmpty()) {
            val faltam = v.requisitos.vantagensPrevias.any { prevId ->
                when (prevId) {
                    // Requisito "genérico": aceita QUALQUER subtipo de AA
                    // ou o seletor base com choice definido
                    "antecedente_arcano", "antecedente_arcano:*" -> {
                        vantagensSelecionadas.none { poss ->
                            poss.id.startsWith("antecedente_arcano_") ||
                                    (poss.id == "antecedente_arcano" && !poss.choice.isNullOrBlank())
                        }
                    }
                    else -> {
                        // Requisito "específico": exige ID exata (ex.: antecedente_arcano_milagres,
                        // comando, etc.)
                        vantagensSelecionadas.none { poss ->
                            poss.id == prevId
                        }
                    }
                }
            }
            if (faltam) return false
        }

        // 6) Verifica novamente limite de “Pontos de Poder” (caso não tenha sido tratado acima)
        if (v.nome.contains("Pontos de Poder", ignoreCase = true)) {
            val totalCompras = comprasPpPorEstagio.values.sum()
            val limite = maxComprasPpAteAgora()
            if (totalCompras >= limite) return false
        }
        // 7) Para as demais vantagens, respeita o maxSelections padrão
        else if (v.maxSelections > 0) {
            val ja = vantagensSelecionadas.count { it.id == v.id }
            if (ja >= v.maxSelections) return false
        }

        // 8) Se requer escolha e já escolheu, não pode repetir a mesma combinação
        val choiceSeguro2 = v.choice
        if (v.requiresChoice && choiceSeguro2 != null) {
            val repetida = vantagensSelecionadas.any {
                it.id == v.id && it.choice == choiceSeguro2
            }
            if (repetida) return false
        }

        // 8.1) Verifica requisito de “nível de campanha” (minProgress)
        nivelParaEstagio[v.requisitos.estagio]?.let { estReqObj2 ->
            if (estReqObj2.minProgress > effectiveProgressoParaVantagens()) return false
        }

        // 9) Verifica requisitos de atributo mínimo
        if (v.requisitos.atributoMin.any { (nome, min) ->
                val chaveNorm = nome.uppercase().semAcentos().trim()
                valoresAtributos[chaveNorm]?.intValue?.let { it < min } != false
            }) return false

        // 10) Verifica requisitos de perícia mínima ou vinculada
        val periciaMinMap = v.requisitos.periciaMin

        if (v.vinculadoPericia && periciaMinMap.isNotEmpty()) {
            // Caso seja “vinculadoPericia”, basta que UMA das perícias seja satisfeita (OR)
            val atendeUma = periciaMinMap.any { (perNome, minRaw) ->
                val per = listaPericias.firstOrNull {
                    it.nome.equals(perNome, ignoreCase = true)
                }
                per != null && rawTotal(per) >= minRaw
            }
            if (!atendeUma) return false
        } else {
            // Caso normal, exige que TODAS as perícias mínimas sejam satisfeitas (AND)
            if (periciaMinMap.any { (perNome, minRaw) ->
                    val per = listaPericias.firstOrNull {
                        it.nome.equals(perNome, ignoreCase = true)
                    } ?: return@any false
                    rawTotal(per) < minRaw
                }) {
                return false
            }
        }

        // 11) Verifica requisito de perícia mínima opcional (“periciaMinOpcional”) – basta atender 1
        val periciaMinOpcMap = v.requisitos.periciaMinOpcional
        if (periciaMinOpcMap.isNotEmpty()) {
            val atendeUmaOpc = periciaMinOpcMap.any { (perNome, minRaw) ->
                val per = listaPericias.firstOrNull {
                    it.nome.equals(perNome, ignoreCase = true)
                }
                per != null && rawTotal(per) >= minRaw
            }
            if (!atendeUmaOpc) return false
        }

        // 12) Verifica requisito “exige Carta Selvagem”
        if (v.requisitos.exigeCS && !cartaSelvagem) return false

        // 13) Verifica incompatibilidades (ex.: “RICO” x “POBREZA”)
        val compsConfl = incompatibilidades[key] ?: emptySet()
        val vantKey = v.nome.trim().uppercase()
        if (vantKey == "RICO" || vantKey == "PODRE DE RICO") {
            val tenhoPobreza = complicacoesSelecionadas.keys.any {
                it.id.trim().uppercase() == "POBREZA"
            }
            if (tenhoPobreza) return false
        }
        if (complicacoesSelecionadas.keys
                .map { it.id.keyify() }
                .any { it in compsConfl }
        ) return false

        // Se passou por todas as checagens, pode selecionar
        return true
    }

    var pontosComplicacaoGastos by mutableIntStateOf(0)
    val baseIncsPorPericia = mutableStateMapOf<Pericia, Int>().also { m -> listaPericias.forEach { m[it] = 0 } }
    private val compIncsPorPericia = mutableStateMapOf<Pericia, Int>().also { m -> listaPericias.forEach { m[it] = 0 } }
    val compCostStackPorPericia = mutableStateMapOf<Pericia, MutableList<Int>>().also { m ->
        listaPericias.forEach { m[it] = mutableListOf() }
    }
    val paCostStackPorAtributo = mutableStateMapOf<String, MutableList<Int>>().also { m ->
        listaAtributos.forEach { m[it] = mutableListOf() }
    }
    val spCostStackPorPericia = mutableStateMapOf<Pericia, SnapshotStateList<Int>>().also { m ->
        listaPericias.forEach { m[it] = mutableStateListOf() }
    }

    fun rebuildPericias(desiredRaw: Map<Pericia, Int>) {
        val poolSize = BASE_SP_POOL + cpSpStack.size
        var cumulativeCost = 0

        listaPericias.forEach { per ->

            val cap = periciaCapRaw(per)
            val target = desiredRaw.getValue(per).coerceAtMost(cap)

            val stack = spCostStackPorPericia.getValue(per)
            stack.clear()
            baseIncsPorPericia[per] = 0

            var curr = periciaStartRaw(ancestralidade, per)

            while (curr < target && cumulativeCost < poolSize) {
                val next = when {
                    curr == 0 -> 4           // primeira compra em perícia não básica
                    curr < 12 -> curr + 2    // d4..d10 → próximo tipo de dado
                    else      -> curr + 1    // acima de d12 → d12+1, d12+2...
                }

                val attrKey = atributoBaseParaPericia(per)
                val cost    = if (next <= valoresAtributos[attrKey]!!.intValue) 1 else 2
                if (cumulativeCost + cost > poolSize) break

                stack.add(cost)
                baseIncsPorPericia[per] = baseIncsPorPericia.getValue(per) + 1
                cumulativeCost += cost
                curr = next
            }
        }
    }

    fun decreasePericia(per: Pericia) {
        val spStack = spCostStackPorPericia.getValue(per)
        val idx = spStack.indexOfLast { it > 0 }
        if (idx >= 0) {
            spStack.removeAt(idx)
            baseIncsPorPericia[per] = baseIncsPorPericia.getValue(per) - 1

        }
    }

    fun atributoMinRaw(a: String): Int =
        racialAttrMinMap[ancestralidade]?.get(a) ?: 4

    fun atributoMaxRaw(a: String): Int {
        val minRaw = atributoMinRaw(a)
        // cada 2 pontos acima de 4 = 1 step de dado acima de d4
        // d4  -> extras = 0 -> teto 12  (d12)
        // d6  -> extras = 1 -> teto 13  (d12+1)
        // d8  -> extras = 2 -> teto 14  (d12+2)
        // d10 -> extras = 3 -> teto 15  (d12+3), etc.
        val extras = ((minRaw - 4).coerceAtLeast(0) / 2)
        val baseCap = 12 + extras

        val chave = a.keyify()
        val profCount = vantagensSelecionadas.count {
            it.nome.keyify() == "PROFISSIONAL" && it.choice?.keyify() == chave
        }
        val espCount = vantagensSelecionadas.count {
            it.nome.keyify() == "ESPECIALISTA" && it.choice?.keyify() == chave
        }

        return baseCap + (profCount + espCount) * 2
    }

    fun periciaCapRaw(per: Pericia): Int {
        val startRaw = periciaStartRaw(ancestralidade, per)

        // Regra da criação:
        // - Começa em 0 ou d4  -> teto d12 (12)
        // - Começa em d6+      -> teto d12+1 (13)
        val baseCap = if (startRaw >= 6) 13 else 12

        val chave = per.nome.keyify()
        val profCount = vantagensSelecionadas.count {
            it.nome.keyify() == "PROFISSIONAL" && it.choice?.keyify() == chave
        }
        val espCount = vantagensSelecionadas.count {
            it.nome.keyify() == "ESPECIALISTA" && it.choice?.keyify() == chave
        }

        // Profissional / Especialista ainda podem aumentar o teto em campanhas avançadas
        return baseCap + (profCount + espCount) * 2
    }

    fun rawTotal(per: Pericia): Int {
        val startRaw     = periciaStartRaw(ancestralidade, per)
        val normalIncs   = baseIncsPorPericia.getValue(per)
        val complicsIncs = compIncsPorPericia.getValue(per)
        val totalIncs    = normalIncs + complicsIncs

        // Perícia não básica e sem nenhum investimento: continua 0 ("-")
        if (startRaw == 0 && totalIncs == 0) return 0

        // Se a perícia começa em 0 e tem pelo menos 1 incremento:
        // - primeiro incremento leva de 0 -> d4 (4)
        // - os demais seguem a regra normal (applySuperStepsFrom)
        val (startForSteps, steps) = if (startRaw == 0) {
            4 to (totalIncs - 1).coerceAtLeast(0)
        } else {
            startRaw to totalIncs.coerceAtLeast(0)
        }

        return applySuperStepsFrom(startForSteps, steps)
    }

    fun aplicarAncestralidade(anc: String) {
        val prevAnc = ancestralidade

        // Ajuste do ponto extra de vantagem dos humanos
        if (prevAnc == "HUMANOS" && anc != "HUMANOS") {
            if (vantagensSelecionadas.isNotEmpty()) {
                vantagensSelecionadas.removeAt(vantagensSelecionadas.lastIndex)
            } else {
                pontosVantagem = (pontosVantagem - 1).coerceAtLeast(0)
            }
        } else if (prevAnc != "HUMANOS" && anc == "HUMANOS") {
            pontosVantagem += 1
        }

        // Guarda o valor "dado bruto" das perícias antes da troca
        val desiredRaw = listaPericias.associateWith { rawTotal(it) }

        // ===== ATRIBUTOS: recalcula a partir do mínimo da nova raça + passos já comprados =====
        val newAttrMods = racialAttrMinMap[anc] ?: emptyMap()

        listaAtributos.forEach { nome ->
            val st     = valoresAtributos[nome]!!
            val newMin = newAttrMods[nome] ?: 4

            // teto novo baseado no mínimo racial (d12, d12+1, d12+2, ...)
            val extras = ((newMin - 4).coerceAtLeast(0) / 2)
            val newMax = 12 + extras

            val stack = paCostStackPorAtributo.getValue(nome)
            var raw   = newMin
            var appliedSteps = 0

            // reaplica cada ponto gasto nesse atributo,
            // respeitando o novo teto e a lógica de steps (até 12: +2, acima de 12: +1)
            repeat(stack.size) {
                val candidate = if (raw < 12) raw + 2 else raw + 1
                if (candidate > newMax) {
                    return@repeat
                }
                raw = candidate
                appliedSteps++
            }

            // corta passos que não cabem mais no teto da nova raça
            if (appliedSteps < stack.size) {
                repeat(stack.size - appliedSteps) {
                    stack.removeAt(stack.lastIndex)
                }
            }

            // valor final do atributo para a nova ancestralidade
            st.intValue = raw
        }
        // =====================================================================

        // troca efetiva da ancestralidade
        ancestralidade = anc

        // Vantagens automáticas da ancestralidade anterior que devem ser removidas
        val prevFree = vantagensAutomaticas.toSet() +
                when (prevAnc) {
                    "SAURIOS"    -> setOf("Sentidos Aguçados", "Prontidão")
                    "PEQUENINOS" -> setOf("Sorte")
                    else         -> emptySet()
                }

        vantagensSelecionadas.removeAll { it.nome in prevFree }
        desvantagensAutomaticas.clear()
        vantagensAutomaticas.clear()

        // Carrega as vantagens / desvantagens automáticas da nova ancestralidade
        listaAncestralidadesJson
            .firstOrNull { it.nome.keyify() == anc }
            ?.let { rm ->
                desvantagensAutomaticas.addAll(rm.desvantagens)
                vantagensAutomaticas.addAll(rm.vantagensGratis)
            }

        val keepFreeKeys = vantagensAutomaticas.map { it.keyify() }.toSet()
        vantagensSelecionadas.removeAll { sel ->
            sel.nome.keyify() !in keepFreeKeys
        }

        // Aplica efeitos específicos de cada ancestralidade
        when (anc) {
            "SAURIOS" -> {
                listaVantagens.firstOrNull { it.nome.equals("Sentidos Aguçados", ignoreCase = true) }
                    ?.let { vantagensSelecionadas.add(it) }
                listaVantagens.firstOrNull { it.nome.equals("Prontidão", ignoreCase = true) }
                    ?.let { vantagensSelecionadas.add(it) }
                vantagensAutomaticas.add("Prontidão")
                armadura = 2
            }
            "PEQUENINOS" -> {
                listaVantagens.firstOrNull { it.nome.equals("Sorte", ignoreCase = true) }
                    ?.let { vantagensSelecionadas.add(it) }
                vantagensAutomaticas.add("Sorte")
                armadura = 0
            }
            "CELESTIAIS" -> {
                listaVantagens
                    .firstOrNull { it.nome.equals("ANTECEDENTE ARCANO MILAGRES", ignoreCase = true) }
                    ?.let {
                        vantagensSelecionadas.add(it)
                    }
                vantagensAutomaticas.add("ANTECEDENTE ARCANO MILAGRES")
                armadura = 0
            }
            else -> {
                armadura = 0
            }
        }

        // Adaptável (humano) dá 1 ponto extra de vantagem
        pontosVantagem = if (vantagensAutomaticas.any { it.keyify() == "ADAPTAVEL" }) 1 else 0

        // Remove complicações automáticas da ancestralidade anterior
        val oldAutoKeys = listaAncestralidadesJson
            .firstOrNull { it.nome.keyify() == prevAnc }
            ?.desvantagens
            ?.map { it.substringBefore("(").trim().keyify() }
            ?.toSet()
            ?: emptySet()

        complicacoesSelecionadas.keys
            .filter { it.id.keyify() in oldAutoKeys }
            .forEach { complicacoesSelecionadas.remove(it) }

        // Aplica complicações automáticas da nova ancestralidade
        val autoBaseKeys = desvantagensAutomaticas
            .map { it.substringBefore("(").trim().keyify() }
            .toSet()

        listaComplicacoes
            .filter { it.id.keyify() in autoBaseKeys }
            .forEach { comp ->
                val hasMenor = desvantagensAutomaticas.any {
                    it.substringBefore("(").trim().keyify() == comp.id.keyify()
                            && it.contains("Menor", ignoreCase = true)
                }

                val grau = when (comp.severity.lowercase()) {
                    "both"  -> if (hasMenor) "Menor" else "Maior"
                    "menor" -> "Menor"
                    "maior" -> "Maior"
                    else    -> "Menor"
                }

                complicacoesSelecionadas[comp] = grau
            }

        // Reconstroi perícias com base nos valores "brutos" desejados
        rebuildPericias(desiredRaw)
        // Recalcula os pontos de atributo a partir dos valores atuais e do novo mínimo racial
        recalcularPontosAtributo()
    }

    fun spendProgressAcrossStages(n: Int) {
        var remaining = n
        reachedStages().mapIndexed { idx, est -> idx to est }.forEach { (idx, est) ->
            if (remaining == 0) return@forEach
            val cap   = dynamicStageCaps[idx]
            val spent = stageXpSpent.getValue(est.nome)
            val avail = (cap - spent).coerceAtLeast(0)
            val use   = avail.coerceAtMost(remaining)
            if (use > 0) {
                stageXpSpent[est.nome] = spent + use
                remaining -= use
            }
        }
        val totalSpent = stageXpSpent.values.sum()
        progressosDisponiveis = (progresso - totalSpent).coerceAtLeast(0)
    }

    fun checkFreeze() {
        val idx = currentProgressStageIndex()
        val est = listaDeEstagios[idx]
        val cap = dynamicStageCaps[idx]
        val spent = stageXpSpent.getValue(est.nome)
        if (spent == cap) {
            frozenAdvCount = vantagensSelecionadas.size
        }
    }

    // Calcula quantos Pontos de Atributo ainda restam,
// considerando os valores atuais dos atributos e o mínimo racial.
    private fun calcularPontosAtributoRestantes(): Int {
        val mods = racialAttrMinMap[ancestralidade] ?: emptyMap()
        var usados = 0

        for (nome in listaAtributos) {
            val atual = valoresAtributos[nome]!!.intValue
            val base  = mods[nome] ?: 4

            var cur = base
            while (cur < atual) {
                cur += if (cur < 12) 2 else 1   // até d12: +2; acima de d12: +1
                usados += 1                      // cada passo = 1 PA gasto
            }
        }

        // 5 PA base + extras de CP - penalidades de Jovem
        return (5 + cpPaStack.size - jovemMalusPa) - usados
    }

    fun recalcularPontosAtributo() {
        // Recalcula a partir dos valores atuais
        pontosAtributo = calcularPontosAtributoRestantes()

        // Se ficou negativo, precisamos "desfazer" passos em atributos
        trimAttributeStacks()

        // Mudar atributos pode mudar limite de perícias
        rebuildAllPericiaStacks()
    }

    private fun trimAttributeStacks() {
        // Enquanto tiver PA negativo, desfazemos o último aumento de algum atributo
        while (pontosAtributo < 0) {
            val entry = paCostStackPorAtributo
                .entries
                .firstOrNull { it.value.isNotEmpty() }
                ?: break

            val nomeAttr = entry.key
            val stack    = entry.value

            // Remove o último "passo" registrado nesse atributo
            stack.removeAt(stack.size - 1)

            val mods = racialAttrMinMap[ancestralidade] ?: emptyMap()
            val base = mods[nomeAttr] ?: 4

            val atual = valoresAtributos[nomeAttr]!!.intValue

            // Reverte um passo usando a mesma lógica de steps:
            // se estava acima de d12, o último passo foi +1; caso contrário, foi +2.
            val novo = if (atual > 12) atual - 1 else atual - 2
            valoresAtributos[nomeAttr]!!.intValue = novo.coerceAtLeast(base)

            // Recalcula os PA restantes depois desse rollback
            pontosAtributo = calcularPontosAtributoRestantes()
        }
    }

    fun applyYoungMinor() {
        jovemAutoPequeno = false
        jovemMalusPa = 1
        jovemMalusSp = 2
        recalcularPontosAtributo()
    }

    fun applyYoungMajor(pequComp: Complicacao) {
        jovemAutoPequeno = true
        jovemMalusPa = 2
        jovemMalusSp = 2
        desvantagensAutomaticas.add(pequComp.id.substringBefore("(").trim())
        complicacoesSelecionadas[pequComp] = "Menor"
        recalcularPontosAtributo()
    }

    fun removeYoung(pequComp: Complicacao) {
        jovemAutoPequeno = false
        jovemMalusPa = 0
        jovemMalusSp = 0
        desvantagensAutomaticas.remove(pequComp.id.substringBefore("(").trim())
        complicacoesSelecionadas.remove(pequComp)
        recalcularPontosAtributo()
    }

    var emProgresso by mutableStateOf(false)

    fun creationComplete(): Boolean =
        !emProgresso &&
                pontosAtributo == 0 &&
                pontosPericia == 0 &&
                pontosVantagem == 0 &&
                (pontosComplicacao - pontosComplicacaoGastos).coerceAtLeast(0) == 0


    val stageXpSpent: SnapshotStateMap<String, Int> = mutableStateMapOf<String, Int>().apply {
        listaDeEstagios.forEach { this[it.nome] = 0 }
    }

    var progressosDisponiveis by mutableIntStateOf(0)

    private fun reachedStages(): List<Estagio> =
        listaDeEstagios.filter { progresso >= it.minProgress }

    fun rebuildAllPericiaStacks() {
        var cumulativeCost = 0
        val pool = totalSpPool

        listaPericias.forEach { per ->

            val desiredRaw = rawTotal(per)
            val cap       = periciaCapRaw(per)
            val minRaw    = if (per.basica) 4 else 0

            var target = desiredRaw.coerceIn(minRaw, cap)
            fun costFor(tgt: Int): Int {
                var curr = periciaStartRaw(ancestralidade, per)
                var sum  = 0
                while (curr < tgt) {
                    val next     = if (curr == 0) 4 else curr + 2
                    val attrKey  = atributoBaseParaPericia(per)
                    val stepCost = if (next <= valoresAtributos[attrKey]!!.intValue) 1 else 2
                    sum += stepCost
                    curr = next
                }
                return sum
            }

            var cost = costFor(target)

            while (cumulativeCost + cost > pool) {
                target = (target - 2).coerceAtLeast(minRaw)
                cost   = costFor(target)
            }

            val stack = spCostStackPorPericia.getValue(per)
            stack.clear()
            baseIncsPorPericia[per] = 0

            var currRaw = periciaStartRaw(ancestralidade, per)
            while (currRaw < target) {
                val next     = if (currRaw == 0) 4 else currRaw + 2
                val attrKey  = atributoBaseParaPericia(per)
                val stepCost = if (next <= valoresAtributos[attrKey]!!.intValue) 1 else 2
                stack.add(stepCost)
                baseIncsPorPericia[per] = baseIncsPorPericia.getValue(per) + 1
                currRaw = next
            }

            cumulativeCost += cost
        }
    }
    init {
        aplicarAncestralidade(ancestralidade)
    }
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

    // ► usa o estado global para saber a ancestralidade atual
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
    onHelpClick: () -> Unit,
    centerText: String,
    onCenterClick: (() -> Unit)? = null,
    onListaCompletaClick: (() -> Unit)? = null, // ← agora é opcional
    listaCompletaText: String = "Lista Completa"
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onHelpClick) {
            Icon(Icons.AutoMirrored.Filled.Help, contentDescription = "Ajuda")
        }

        Text(
            centerText,
            Modifier
                .weight(1f)
                .then(if (onCenterClick != null) Modifier.clickable(onClick = onCenterClick) else Modifier)
                .padding(horizontal = 8.dp),
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        if (onListaCompletaClick != null) {        // ← só mostra no full
            TextButton(onClick = onListaCompletaClick) {
                Text(listaCompletaText, fontSize = 13.sp)
            }
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
        onOpenSuperPoderesDetail = {},

        onHelpSuperClick = {},

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
    onOpenSuperPoderesDetail: () -> Unit,
    onHelpSuperClick: () -> Unit,

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
                onOpenSuperPoderesDetail = onOpenSuperPoderesDetail,
                onHelpClick           = onHelpSuperClick
            )
        }

        // ─── Equipamentos ─────────────────────────────────────────────────────────
        EquipamentoSection(
            dinheiro                 = state.dinheiro,
            onHelpClick              = { /* help opcional aqui */ },
            onListaCompletaClick     = onOpenListaCompletaEquipamento,
            onEquipamentoDoubleClick = { equipamento ->
                val custo = (equipamento.custo as? JsonPrimitive)
                    ?.content?.toIntOrNull() ?: 0
                if (state.dinheiro >= custo) {
                    state.dinheiro -= custo
                    state.equipamentosComprados.add(equipamento)
                }
            },
            equipamentosComprados    = state.equipamentosComprados,
            onRemoveEquipamentoClick = { equipamento ->
                val custo = (equipamento.custo as? JsonPrimitive)
                    ?.content?.toIntOrNull() ?: 0
                state.equipamentosComprados.remove(equipamento)
                state.dinheiro += custo
            },
            categorias           = equipamentoCategorias,
            superequipCategorias = if (state.modoSuperequip) superequipCategorias else emptyList()
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
    // Helper para ler JsonElement como texto
    fun JsonElement?.asText(): String? = when (this) {
        is JsonPrimitive -> this.content
        else -> this?.toString()
    }?.takeIf { it.isNotBlank() }

    // --- AGRUPAMENTO (mantido com tua correção) ---

    // Mapa: Tipo -> (Subtipo -> (Subsubtipo -> Itens))
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
            onDismissRequest = { },
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

// Super
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

                    // Horror
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

                    // Fantasia
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

                    // Ficção Científica
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
                TextButton(onClick = { }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // ── Diálogo de carregamento existente ────────────────────────────────────────
    if (showLoadDialog) {
        AlertDialog(
            onDismissRequest = { },
            title            = { Text("Selecione um personagem") },
            text             = {
                LazyColumn {
                    items(nomesSalvos) { (displayName, fileKey) ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    // CORREÇÃO: Carrega em background
                                    scope.launch(Dispatchers.IO) {
                                        val salvo = StorageUtils.carregarPersonagem(context, fileKey)
                                        withContext(Dispatchers.Main) {
                                            salvo?.let {
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
                TextButton(onClick = { }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // ── Diálogo de confirmação de exclusão ──────────────────────────────────────
    if (pendingDelete != null) {
        val (displayName, fileKey) = pendingDelete!!
        AlertDialog(
            onDismissRequest = { },
            title            = { Text("Confirmar exclusão") },
            text             = { Text("Deseja realmente excluir \"$displayName\"?") },
            confirmButton    = {
                TextButton(onClick = {
                    // CORREÇÃO: Deleta em background e atualiza a lista
                    scope.launch(Dispatchers.IO) {
                        StorageUtils.deletarPersonagem(context, fileKey)
                        val listaAtualizada = StorageUtils.listarPersonagens(context)
                        withContext(Dispatchers.Main) {
                            nomesSalvos = listaAtualizada
                        }
                    }
                }) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { }) {
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF2E3C6)), // cor pergaminho
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ícone da seção
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(16.dp))

            // Título e subtítulo
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

            // Seta à direita
            Icon(
                imageVector = Icons.AutoMirrored.Filled.MenuBook, // troque por KeyboardArrowRight se preferir
                contentDescription = null,
                tint = Color.Black
            )
        }
    }
}