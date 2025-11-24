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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.swadebuilder.model.DataRepository
import com.example.swadebuilder.model.Complicacao
import com.example.swadebuilder.model.CriadorViewModel
import com.example.swadebuilder.model.EquipamentoCategoria
import com.example.swadebuilder.model.EquipamentoItem
import com.example.swadebuilder.model.MeuPersonagem
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
import com.example.swadebuilder.ui.screens.TelaInicial
import com.example.swadebuilder.ui.screens.AncestralidadesDetailScreen
import com.example.swadebuilder.ui.screens.AtributosDetailScreen
import com.example.swadebuilder.ui.screens.ComplicacoesDetailScreen
import com.example.swadebuilder.ui.screens.EquipamentosDetailScreen
import com.example.swadebuilder.ui.screens.PericiasDetailScreen
import com.example.swadebuilder.ui.screens.UnifiedScreen
import com.example.swadebuilder.ui.screens.VantagensDetailScreen
import com.example.swadebuilder.ui.sections.VantagensContent
import com.example.swadebuilder.ui.theme.SWADEbuilderTheme
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.loadJsonAsset
import com.example.swadebuilder.util.loadRawText
import com.example.swadebuilder.util.salvarEExibirFichaPdf
import com.example.swadebuilder.util.semAcentos
import com.example.swadebuilder.util.toDiceString
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

        setContent {
            val context = LocalContext.current
            val dataRepository = remember { DataRepository(context) }
            var dataLoaded by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                dataRepository.loadAllData()
                arcanoInfo = dataRepository.arcanoInfo
                listaAtributos = dataRepository.listaAtributos
                mapaAtributosDisplay = dataRepository.mapaAtributosDisplay
                listaPericias = dataRepository.listaPericias
                AppData.basicasVantagens = dataRepository.todasVantagens.filter { it.origem.equals("BASICO", true) }
                AppData.superVantagens = dataRepository.todasVantagens.filter { it.origem.equals("SUPER", ignoreCase = true) }
                listaVantagens = dataRepository.todasVantagens
                AppData.superVantagensParaDetalhe = AppData.superVantagens
                listaComplicacoes = dataRepository.listaComplicacoes
                listaAncestralidadesJson = dataRepository.listaAncestralidadesJson
                racialAttrMinMap = dataRepository.racialAttrMinMap
                racialSkillStartMap = dataRepository.racialSkillStartMap
                dataLoaded = true
            }

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
            val activity = (context as? ComponentActivity)
            var mostrouTelaInicial by rememberSaveable { mutableStateOf(true) }
            var showExitDialog     by rememberSaveable { mutableStateOf(false) }

            var showHelpAppDialog by rememberSaveable { mutableStateOf(false) }

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
                            Text(stringResource(R.string.help_dialog_ok))
                        }
                    },
                    title = { Text(stringResource(R.string.help_dialog_title)) },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 400.dp)
                                .verticalScroll(scrollState)
                        ) {
                            Text(stringResource(R.string.help_dialog_content))
                        }
                    }
                )
            }

            if (showExitDialog) {
                AlertDialog(
                    onDismissRequest = { },
                    title            = { Text(stringResource(R.string.exit_dialog_title)) },
                    confirmButton    = {
                        TextButton(onClick = {
                            activity?.finishAffinity()
                        }) {
                            Text(stringResource(R.string.exit_dialog_yes))
                        }
                    },
                    dismissButton    = {
                        TextButton(onClick = { }) {
                            Text(stringResource(R.string.exit_dialog_no))
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
                                onCriarNovo = { cartaSelvagem, maisPontosPericias, modoSupers, nasceUmHeroi, heroisSemArmadura, usarEspecializacaoPer, semPontosDePoder, grandesResponsabilidades, multiplosAAs ->

                                    criadorViewModel.resetStateParaNovoPersonagem(
                                        cartaSelvagem      = cartaSelvagem,
                                        maisPontosPericias = maisPontosPericias,
                                        modoSupers         = modoSupers,
                                        usarEspecializacoesDePericia = usarEspecializacaoPer,
                                        permiteMultiAntecedenteArcano = multiplosAAs
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
                                        categoriasBasico = dataRepository.equipamentoCategorias,
                                        categoriasSuper  = dataRepository.superequipCategorias
                                    )
                                    mostrouTelaInicial = false
                                },
                                context = context,
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
                                                            text = stringResource(R.string.help_dialog_title),
                                                            fontSize = 16.sp,
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                    }
                                                }
                                            },
                                            navigationIcon = {
                                                TextButton(onClick = { mostrouTelaInicial = true }) {
                                                    Text(
                                                        text       = stringResource(R.string.top_bar_back),
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
                                                    Icon(Icons.Default.Print, contentDescription = stringResource(R.string.top_bar_print))
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
                                                            Toast.makeText(context, R.string.character_saved_success, Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                }) {
                                                    Icon(Icons.Default.Save, contentDescription = stringResource(R.string.top_bar_save))
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
                                                categorias = dataRepository.equipamentoCategorias +
                                                        if (state.modoSuperequip) dataRepository.superequipCategorias else emptyList(),
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
                                                onBack          = {
                                                    showSuperDetail = false
                                                    expPoderes = true        // mantém a seção de superpoderes aberta ao voltar
                                                }
                                            )

                                            else -> UnifiedScreen(
                                                state = state,
                                                onOpenVantagensDetail = { nomeVantagem ->
                                                    highlightedVantagem = nomeVantagem          // já é String
                                                    state.vantagemEmFoco = nomeVantagem         // String? compatível
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
                                                    // se vier com string vazia (lista completa genérica), limpa foco
                                                    state.superPoderEmFoco = nomePoder.ifBlank { null }
                                                    expPoderes = true          // garante que a seção estará aberta ao voltar
                                                    showSuperDetail = true
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

                                                equipamentoCategorias = dataRepository.equipamentoCategorias,
                                                superequipCategorias  = dataRepository.superequipCategorias,
                                                listaSuperPoderes     = dataRepository.listaSuperPoderes
                                            )
                                        }
                                    }
                                }
                            })
                        }
                    }
                }
            }
        }
    }
}

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

// Conteúdo permanece o mesmo

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

// Conteúdo permanece o mesmo

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

// Conteúdo permanece o mesmo



// Conteúdo permanece o mesmo

// Conteúdo permanece o mesmo

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

// Conteúdo permanece o mesmo


// Conteúdo permanece o mesmo

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

// Conteúdo permanece o mesmo