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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.swadebuilder.model.AtributoList
import com.example.swadebuilder.model.Complicacao
import com.example.swadebuilder.model.CriadorViewModel
import com.example.swadebuilder.model.EquipamentoCategoria
import com.example.swadebuilder.model.MonstroTemplate
import com.example.swadebuilder.model.PericiaList
import com.example.swadebuilder.model.PersonagemSalvo
import com.example.swadebuilder.model.RacialModifier
import com.example.swadebuilder.model.StorageUtils
import com.example.swadebuilder.model.Vantagem
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
            json.decodeFromString(arcanoJson)
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

        AppData.horrorVantagens = todasVantagens.filter {
            it.origem.equals("HORROR", ignoreCase = true)
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

        listaAncestralidadesJson = json.decodeFromString<List<RacialModifier>>(ancestralRaw)

        val monstrosJson = assets
            .open("monstros.json")
            .bufferedReader()
            .use { it.readText() }
        listaMonstroTemplates = json.decodeFromString(monstrosJson)

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
            val snackHost = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()

            var creationSession by rememberSaveable { mutableIntStateOf(0) }

            var expAncs    by rememberSaveable(creationSession) { mutableStateOf(false) }
            var expComps   by rememberSaveable(creationSession) { mutableStateOf(false) }
            var expEquip   by rememberSaveable(creationSession) { mutableStateOf(false) }

            var expAttrs   by rememberSaveable(creationSession) { mutableStateOf(false) }
            var expPer     by rememberSaveable(creationSession) { mutableStateOf(false) }
            var expVants   by rememberSaveable(creationSession) { mutableStateOf(false) }
            var expPoderes by rememberSaveable(creationSession) { mutableStateOf(false) }
            var expResumo  by rememberSaveable(creationSession) { mutableStateOf(false) }
            var expXp by rememberSaveable(creationSession) { mutableStateOf(false) }
            var expMonstro by rememberSaveable(creationSession) { mutableStateOf(false) }

            val context = LocalContext.current
            val activity = (context as? ComponentActivity)
            var mostrouTelaInicial by rememberSaveable { mutableStateOf(true) }
            var showExitDialog     by rememberSaveable { mutableStateOf(false) }

            var showHelpAppDialog by rememberSaveable { mutableStateOf(false) }
            var showThemeDialog by rememberSaveable { mutableStateOf(false) }

            var showFeedbackDialog by rememberSaveable { mutableStateOf(false) }

            LaunchedEffect(state.mostrandoPericiasProgresso) {
                if (state.mostrandoPericiasProgresso) {
                    expPer = true
                }
            }

            LaunchedEffect(state.mostrandoVantagensProgresso) {
                if (state.mostrandoVantagensProgresso) {
                    expVants = true
                }
            }

            LaunchedEffect(state.mostrandoAtributosProgresso) {
                if (state.mostrandoAtributosProgresso) {
                    expAttrs = true
                }
            }

            LaunchedEffect(state.mostrandoPoderesProgresso, state.arcanoCompraPendente()) {
                if (state.mostrandoPoderesProgresso || state.arcanoCompraPendente()) {
                    expPoderes = true
                    expVants = true
                }
            }

            if (showThemeDialog) {
                val themeNames = remember {
                    mapOf(
                        com.example.swadebuilder.ui.theme.AppTheme.DEFAULT   to "Padrão",
                        com.example.swadebuilder.ui.theme.AppTheme.MEDIEVAL  to "Medieval",
                        com.example.swadebuilder.ui.theme.AppTheme.CYBERPUNK to "Cyberpunk",
                        com.example.swadebuilder.ui.theme.AppTheme.WW2       to "Segunda Guerra",
                        com.example.swadebuilder.ui.theme.AppTheme.HORROR    to "Horror",
                        com.example.swadebuilder.ui.theme.AppTheme.SCIFI     to "Sci-Fi",
                        com.example.swadebuilder.ui.theme.AppTheme.PRIDE     to "Pride",
                        com.example.swadebuilder.ui.theme.AppTheme.HALLOWEEN to "Halloween"
                    )
                }

                AlertDialog(
                    onDismissRequest = { showThemeDialog = false },
                    title = { Text(stringResource(R.string.select_theme)) },
                    text = {
                        LazyColumn {
                            items(com.example.swadebuilder.ui.theme.AppTheme.entries) { theme ->
                                TextButton(
                                    onClick = {
                                        criadorViewModel.setAppTheme(theme)
                                        showThemeDialog = false
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(themeNames[theme] ?: theme.name)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showThemeDialog = false }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }

            LaunchedEffect(criadorViewModel.feedbackMessages.size) {
                if (criadorViewModel.feedbackMessages.isNotEmpty()) {
                    showFeedbackDialog = true
                }
            }

            LaunchedEffect(criadorViewModel.feedbackMessages.size) {
                if (state.showHelpMessages && criadorViewModel.feedbackMessages.isNotEmpty()) {
                    criadorViewModel.feedbackMessages.forEach { msg ->
                        scope.launch {
                            snackHost.showSnackbar(msg)
                        }
                    }
                    criadorViewModel.clearFeedbackMessages()
                }
            }

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
                            Text(getHelpAppText(state = state))
                        }
                    }
                )
            }

            if (showExitDialog) {
                AlertDialog(
                    onDismissRequest = { showExitDialog = false },
                    title            = { Text("Deseja encerrar o app?") },
                    confirmButton    = {
                        TextButton(onClick = {
                            activity?.finishAffinity()
                        }) {
                            Text("Sim")
                        }
                    },
                    dismissButton    = {
                        TextButton(onClick = { showExitDialog = false }) {
                            Text("Não")
                        }
                    }
                )
            }

            SWADEbuilderTheme(appTheme = state.appTheme) {
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
                                onCriarNovo = { cartaSelvagem, maisPontosPericias, modoSupers, compendioFantasiaAtivo, compendioHorrorAtivo, modoMonstroAtivo, _, _,
                                                nasceUmHeroi, heroisSemArmadura, usarEspecializacaoPer,
                                                semPontosDePoder, grandesResponsabilidades, showHelpMessages ->

                                    creationSession++

                                    criadorViewModel.resetStateParaNovoPersonagem(
                                        cartaSelvagem      = cartaSelvagem,
                                        maisPontosPericias = maisPontosPericias,
                                        modoSupers         = modoSupers,
                                        compendioFantasiaAtivo = compendioFantasiaAtivo,
                                        compendioHorrorAtivo = compendioHorrorAtivo,
                                        modoMonstroAtivo = modoMonstroAtivo,
                                        usarEspecializacoesDePericia = usarEspecializacaoPer,
                                        showHelpMessages = showHelpMessages
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
                                    creationSession++


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
                            BackHandler {
                                mostrouTelaInicial = true
                            }

                            Scaffold(
                                snackbarHost = { SnackbarHost(hostState = snackHost) },
                                containerColor = Color.Transparent,
                                topBar         = {
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
                                                val personagem = state.toMeuPersonagem()

                                                scope.launch(Dispatchers.IO) {
                                                    salvarEExibirFichaPdf(this@MainActivity, personagem)
                                                }
                                            }) {
                                                Icon(Icons.Default.Print, contentDescription = "Imprimir ficha")
                                            }

                                            IconButton(onClick = { showThemeDialog = true }) {
                                                Icon(Icons.Default.Settings, contentDescription = "Change Theme")
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
                                                    val vantagemChoices = state.vantagensSelecionadas
                                                        .groupBy { it.id }
                                                        .mapValues { (_, list) ->
                                                            list.mapNotNull { it.choice }
                                                                .filter { it.isNotBlank() }
                                                        }

                                                    val salvo = PersonagemSalvo(
                                                        id                 = personagemId,
                                                        nome               = state.nomePersonagem,
                                                        atributos          = atributosMap,
                                                        pericias           = periciasMap,
                                                        ancestralidade     = state.ancestralidade,
                                                        vantagens          = state.vantagensSelecionadas.map { it.id },
                                                        vantagemChoices    = vantagemChoices,
                                                        vantagensRaciais   = state.vantagensRaciais.toList(),
                                                        complicacoes       = complicacoesList,
                                                        cpPaCount          = state.cpPaStack.size,
                                                        cpPvCount          = state.cpPvStack.size,
                                                        cpSpCount          = state.cpSpStack.size,
                                                        cpRecursosCount    = state.cpRecursosStack.size,
                                                        equipamentos       = state.equipamentosComprados.map { it.nome },
                                                        poderes            = state.poderSlotsPorArcano.mapValues { (_, slots) -> slots.filterNotNull() },
                                                        dinheiro           = state.dinheiro,
                                                        pontosRestantes    = state.pontosVantagem,
                                                        progresso          = state.progresso,
                                                        progressosDisponiveis = state.progressosDisponiveis,
                                                        stageXpSpent          = state.stageXpSpent.toMap(),
                                                        xpSlots               = state.xpSlots.toList(),
                                                        paFromProgress        = state.paFromProgress,
                                                        pvFromXpOutstanding   = state.pvFromXpOutstanding,
                                                        legendaryAttrReservations = state.legendaryAttrReservations,
                                                        frozenAdvantageCount  = state.frozenAdvantageCount,
                                                        modoProgressaoAtivo   = state.modoProgressaoAtivo,
                                                        emProgresso           = state.emProgresso,
                                                        advancementHistory    = state.advancementHistory.toList(),
                                                        naturalArmorFromRace = state.naturalArmorFromRace,
                                                        armorBase            = state.armadura,
                                                        maisPontosPericias = state.maisPontosPericias,
                                                        cartaSelvagem      = state.cartaSelvagem,
                                                        heroisSemArmadura  = state.heroisSemArmadura,
                                                        soldadoCargaAtivo  = state.soldadoCargaAtivo,
                                                        semPontosDePoder   = state.usarSemPontosDePoder,
                                                        usarEspecializacoesDePericia = state.usarEspecializacoesDePericia,
                                                        especializacoesPorPericia    = state.especializacoesPorPericia.toMap(),
                                                        modoSupers              = state.modoSupers,
                                                        compendioFantasiaAtivo  = state.compendioFantasiaAtivo,
                                                        compendioHorrorAtivo    = state.compendioHorrorAtivo,
                                                        modoMonstroAtivo        = state.modoMonstroAtivo,
                                                        tipoMonstroSelecionado  = state.tipoMonstroSelecionado,
                                                        modoSuperequip          = state.modoSuperequip,
                                                        modoSuperComplicacoes   = state.modoSuperComplicacoes,
                                                        superInvestments        = state.superInvestments.toList(),
                                                        superPontosTotais       = state.superPontosTotais,
                                                        superPontosDisponiveis  = state.superPontosDisponiveis,
                                                        limitePorPoderPadrao    = state.limitePorPoderPadrao,
                                                        limiteFavorecido        = state.limiteFavorecido,
                                                        poderFavoritoId         = state.poderFavoritoId,
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

                                                        Toast.makeText(
                                                            context,
                                                            "Personagem salvo com sucesso!",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                }
                                            }) {
                                                Icon(Icons.Default.Save, contentDescription = "Salvar")
                                            }
                                        }
                                    )
                                },
                                content = { innerPadding ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(innerPadding)
                                    ) {
                                        UnifiedScreen(
                                            state = state,
                                            viewModel = criadorViewModel,

                                            expAncs        = expAncs,
                                            onToggleAncs   = { expAncs = !expAncs },

                                            expComps       = expComps,
                                            onToggleComps  = { expComps = !expComps },

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

                                            expXp = expXp,
                                            onToggleXp = { expXp = !expXp },

                                            expMonstro = expMonstro,
                                            onToggleMonstro = { expMonstro = !expMonstro },

                                            equipamentoCategorias = equipamentoCategorias,
                                            superequipCategorias  = superequipCategorias,
                                            listaSuperPoderes     = listaSuperPoderes
                                        )
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

private fun getHelpAppText(state: CriadorState): String {
    val isFullVersion = true
    val isSupersMode = state.modoSupers

    val fullVersionInstruction = if (isFullVersion) {
        "Caso deseje acessar os dados das seções para consultar pode usar o botão lista completa ou tocar direto no olho ao lado do nome.\n\n"
    } else {
        ""
    }

    val powersInstruction = if (isSupersMode) {
        """
        8) Superpoderes

        Em caso de campanha de Supers a seção de Superpoderes vem disponível mas só fica acessível após:
        Todos pontos da criação inicial forem distribuídos,

        O nível da Campanha de Super for definido.

        Com estas definições é possível comprar os superpoderes e o app faz os ajustes na ficha quando for aplicável.

        Caso se deseje é possível voltar para a fase inicial de criação ao remover os superpoderes adquiridos e definir o nível da campanha como 0.
        """.trimIndent()
    } else {
        """
        8) Poderes

        Se o personagem possuir um Antecedente Arcano, a seção de poderes fica disponível:

        escolha sua tradição/arcano;

        selecione poderes nos espaços disponíveis;

        o app controla quantos você pode pegar e evita ultrapassar o limite.

        Se você remover o Antecedente Arcano, poderes que dependem dele são limpos automaticamente.
        """.trimIndent()
    }

    return """
    Como usar o app

    $fullVersionInstruction
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

    ajusta atributos para respeitar os novos mínimos/máx-imos;

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

    $powersInstruction

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
}
fun Int.toDiceString(): String =
    if (this <= 12) "d$this" else "d12+${(this - 12)}"

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
lateinit var listaMonstroTemplates: List<MonstroTemplate>

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

fun stageIndexForSlot(slotIndex: Int): Int {
    var remaining = slotIndex
    dynamicStageCaps.forEachIndexed { idx, cap ->
        if (remaining < cap) return idx
        remaining -= cap
    }
    return dynamicStageCaps.lastIndex
}

fun stageForSlot(slotIndex: Int): Estagio = listaDeEstagios[stageIndexForSlot(slotIndex)]

val nivelParaEstagio = mapOf(
    "N" to listaDeEstagios.first { it.nome == "Novato" },
    "E" to listaDeEstagios.first { it.nome == "Experiente" },
    "V" to listaDeEstagios.first { it.nome == "Veterano" },
    "H" to listaDeEstagios.first { it.nome == "Heroico" },
    "L" to listaDeEstagios.first { it.nome == "Lendário" }
)

const val TOTAL_PROGRESS_LIMIT = 20
val dynamicStageCaps = listaDeEstagios.mapIndexed { idx, st ->
    val prevMax = listaDeEstagios.getOrNull(idx - 1)?.maxProgress ?: 0
    if (idx < listaDeEstagios.lastIndex)
        st.maxProgress - prevMax
    else
        (TOTAL_PROGRESS_LIMIT - prevMax).coerceAtLeast(0)
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
                contentDescription = stringResource(id = if (expanded) R.string.cd_collapse else R.string.cd_expand)
            )
        }
        if (expanded) content()
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
                contentDescription = title,
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
                contentDescription = "",
                tint = Color.Black
            )
        }
    }
}