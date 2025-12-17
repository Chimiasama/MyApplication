@file:OptIn(
    ExperimentalMaterial3Api::class
)
@file:Suppress("LanguageDetectionInspection")

package com.example.swadebuilder

import android.content.Context
import android.content.res.AssetManager
import android.os.Build
import android.os.Bundle
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Palette
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import com.example.swadebuilder.model.CrystalHeart
import com.example.swadebuilder.model.EquipamentoCategoria
import com.example.swadebuilder.model.MonstroTemplate
import com.example.swadebuilder.model.PericiaList
import com.example.swadebuilder.model.RacialModifier
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.ui.theme.SWADEbuilderTheme
import com.example.swadebuilder.util.CharacterStorage
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.loadJsonAsset
import com.example.swadebuilder.util.semAcentos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.DateFormat
import kotlin.math.roundToInt


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

private inline fun <reified T> AssetManager.readJsonList(fileName: String): List<T> =
    open(fileName).bufferedReader().use { reader -> json.decodeFromString(reader.readText()) }

private inline fun <reified T> AssetManager.readJsonListOrEmpty(fileName: String): List<T> =
    runCatching { readJsonList<T>(fileName) }.getOrElse { emptyList() }

private inline fun <reified T> AssetManager.readMultipleJsonLists(files: List<String>): List<T> =
    files.flatMap { readJsonListOrEmpty<T>(it) }

private inline fun <reified T> AssetManager.mergeJsonLists(
    primaryFile: String,
    extraFiles: List<String>
): List<T> = readJsonList<T>(primaryFile) + readMultipleJsonLists<T>(extraFiles)

private const val MULTIPLOS_AA_HABILITADOS: Boolean = false

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    @OptIn(ExperimentalMaterial3Api::class)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val equipamentoCategorias = assets.mergeJsonLists<EquipamentoCategoria>(
            primaryFile = "equipamentos.json",
            extraFiles = listOf(
                "equipamentos_crystal.json",
                "equipamentos_adg.json",
                "equipamentos_sol_vapor.json",
                "equipamentos_wiseguys.json"
            )
        ).filter { cat ->
            cat.origem?.equals("super", ignoreCase = true)?.not() ?: true
        }
        val superequipCategorias = assets.readJsonList<EquipamentoCategoria>("equipamentos.json").filter { cat ->
            cat.origem?.equals("super", ignoreCase = true) ?: false
        }

        val crystalHeartsJson = try {
            assets.open("coracoes_crystal.json")
                .bufferedReader()
                .use { it.readText() }
        } catch (_: Exception) { "[]" }
        listaCoracoesCrystal = json.decodeFromString(crystalHeartsJson)

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


        val vantagensExtras = assets.readMultipleJsonLists<Vantagem>(
            listOf(
                "vantagens_adg.json",
                "vantagens_sol_vapor.json",
                "vantagens_wiseguys.json"
            )
        )

        AppData.basicasVantagens = todasVantagens.filter { it.origem.equals("BASICO", true) }
        AppData.superVantagens = todasVantagens.filter {
            it.origem.equals("SUPER", ignoreCase = true)
        }

        AppData.horrorVantagens = todasVantagens.filter {
            it.origem.equals("HORROR", ignoreCase = true)
        }
        AppData.trilhadorVantagens = todasVantagens.filter {
            it.origem.equals("TRILHADOR", ignoreCase = true)
        }

        val vantCrystal = assets.readJsonListOrEmpty<Vantagem>("vantagens_crystal.json")

        listaVantagens = todasVantagens + vantCrystal + vantagensExtras

        AppData.superVantagensParaDetalhe = AppData.superVantagens


        val todasComplicacoes = this.loadJsonAsset<List<Complicacao>>("complicacoes.json")

        val complicacaoExtras = assets.readMultipleJsonLists<Complicacao>(
            listOf(
                "complicacoes_crystal.json",
                "complicacoes_adg.json",
                "complicacoes_sol_vapor.json",
                "complicacoes_wiseguys.json"
            )
        )

        listaComplicacoes = todasComplicacoes + complicacaoExtras

        val ancestralFiles = listOf(
            "ancestralidades_trilhador.json",
            "ancestralidades_sci_fi.json",
            "ancestralidades_deadlands.json",
            "ancestralidades_adg.json",
            "ancestralidades_crystal.json",
            "ancestralidades_sol_vapor.json",
            "ancestralidades_wiseguys.json"
        )

        listaAncestralidadesJson = assets.mergeJsonLists<RacialModifier>(
            primaryFile = "listaancestralidade.json",
            extraFiles = ancestralFiles
        )

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

            val context = LocalContext.current
            val activity = (context as? ComponentActivity)
            var mostrouTelaInicial by rememberSaveable { mutableStateOf(true) }
            var showExitDialog     by rememberSaveable { mutableStateOf(false) }

            val feedbackController = remember { FeedbackController(context) }
            DisposableEffect(Unit) {
                onDispose { feedbackController.dispose() }
            }
            val triggerFeedback = remember(state.hapticStrength, state.soundVolume) {
                { feedbackController.play(state.hapticStrength, state.soundVolume) }
            }

            var showSettingsDialog by rememberSaveable { mutableStateOf(false) }
            var showThemeSelectionDialog by rememberSaveable { mutableStateOf(false) }

            var showSaveDialog by rememberSaveable { mutableStateOf(false) }
            var showLoadDialog by rememberSaveable { mutableStateOf(false) }
            var saveName by rememberSaveable { mutableStateOf("") }

            val savedEntries = remember { mutableStateListOf<CharacterStorage.SaveEntry>() }
            var entryToDelete by remember { mutableStateOf<CharacterStorage.SaveEntry?>(null) }

            LaunchedEffect(showLoadDialog) {
                if (showLoadDialog) {
                    savedEntries.clear()
                    savedEntries.addAll(criadorViewModel.listarPersonagensSalvos(context))
                }
            }

            LaunchedEffect(state.mostrandoPericiasProgresso) {
                if (state.mostrandoPericiasProgresso) {
                    state.sectionsExpanded[com.example.swadebuilder.ui.MainSection.PERICIAS] = true
                }
            }

            LaunchedEffect(state.mostrandoVantagensProgresso) {
                if (state.mostrandoVantagensProgresso) {
                    state.sectionsExpanded[com.example.swadebuilder.ui.MainSection.VANTAGENS] = true
                }
            }

            LaunchedEffect(state.mostrandoAtributosProgresso) {
                if (state.mostrandoAtributosProgresso) {
                    state.sectionsExpanded[com.example.swadebuilder.ui.MainSection.ATRIBUTOS] = true
                }
            }

            LaunchedEffect(state.mostrandoPoderesProgresso, state.arcanoCompraPendente()) {
                if (state.mostrandoPoderesProgresso || state.arcanoCompraPendente()) {
                    state.sectionsExpanded[com.example.swadebuilder.ui.MainSection.PODERES] = true
                    state.sectionsExpanded[com.example.swadebuilder.ui.MainSection.VANTAGENS] = true
                }
            }

            // -- Settings Dialog --
            if (showSettingsDialog) {
                AlertDialog(
                    onDismissRequest = { showSettingsDialog = false },
                    title = { Text("Configurações") },
                    text = {
                        Column {
                            Text("Resposta háptica")
                            Slider(
                                value = state.hapticStrength.toFloat(),
                                onValueChange = { state.hapticStrength = it.roundToInt() },
                                valueRange = 0f..100f,
                                steps = 4,
                                modifier = Modifier.fillMaxWidth(),
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                )
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Intensidade: ${state.hapticStrength}%")
                                TextButton(onClick = { feedbackController.play(state.hapticStrength, 0) }) {
                                    Text("Testar vibração")
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            Text("Sons do app")
                            Slider(
                                value = state.soundVolume.toFloat(),
                                onValueChange = { state.soundVolume = it.roundToInt() },
                                valueRange = 0f..100f,
                                steps = 4,
                                modifier = Modifier.fillMaxWidth(),
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.secondary,
                                    activeTrackColor = MaterialTheme.colorScheme.secondary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                                )
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Volume: ${state.soundVolume}%")
                                TextButton(onClick = { feedbackController.play(0, state.soundVolume) }) {
                                    Text("Testar som")
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            // Theme Selection Button
                            TextButton(
                                onClick = {
                                    triggerFeedback()
                                    showThemeSelectionDialog = true
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Palette, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Mudar Tema do App")
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showSettingsDialog = false }) {
                            Text("Fechar")
                        }
                    }
                )
            }

            // -- Nested Theme Selection Dialog --
            if (showThemeSelectionDialog) {
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
                    onDismissRequest = { showThemeSelectionDialog = false },
                    title = { Text(stringResource(R.string.select_theme)) },
                    text = {
                        LazyColumn {
                            items(com.example.swadebuilder.ui.theme.AppTheme.entries) { theme ->
                                TextButton(
                                    onClick = {
                                        criadorViewModel.setAppTheme(theme)
                                        showThemeSelectionDialog = false
                                        // Also close main settings if desired? keeping open for now.
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(themeNames[theme] ?: theme.name)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showThemeSelectionDialog = false }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }

            if (entryToDelete != null) {
                AlertDialog(
                    onDismissRequest = { entryToDelete = null },
                    title = { Text("Apagar personagem") },
                    text = { Text("Deseja apagar \"${entryToDelete?.nome}\"?") },
                    confirmButton = {
                        TextButton(onClick = {
                            entryToDelete?.let { entry ->
                                CharacterStorage.delete(context, entry.id)
                                savedEntries.removeAll { it.id == entry.id }
                                if (state.idAtual == entry.id) {
                                    state.idAtual = null
                                }
                                scope.launch {
                                    snackHost.showSnackbar("Personagem removido")
                                }
                            }
                            entryToDelete = null
                        }) {
                            Text("Apagar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { entryToDelete = null }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }

            if (showSaveDialog) {
                AlertDialog(
                    onDismissRequest = { showSaveDialog = false },
                    title = { Text("Salvar personagem") },
                    text = {
                        Column {
                            Text("Defina um nome para o salvamento.")
                            OutlinedTextField(
                                value = saveName.ifBlank { state.nomePersonagem },
                                onValueChange = { saveName = it },
                                label = { Text("Nome do arquivo") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            val entry = criadorViewModel.salvarPersonagem(
                                context,
                                saveName.ifBlank { state.nomePersonagem }
                            )
                            showSaveDialog = false
                            scope.launch {
                                snackHost.showSnackbar("Personagem salvo: ${entry.nome}")
                            }
                        }) {
                            Text("Salvar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showSaveDialog = false }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }

            if (showLoadDialog) {
                AlertDialog(
                    onDismissRequest = { showLoadDialog = false },
                    title = { Text("Carregar personagem") },
                    text = {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            if (savedEntries.isEmpty()) {
                                Text("Nenhum personagem salvo.")
                            } else {
                                savedEntries.forEach { entry ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(entry.nome)
                                            Text(
                                                DateFormat.getDateTimeInstance().format(entry.timestamp),
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                        Row {
                                            TextButton(onClick = { entryToDelete = entry }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Apagar personagem")
                                                Spacer(Modifier.width(4.dp))
                                                Text("Apagar")
                                            }
                                            TextButton(onClick = {
                                                val loaded = criadorViewModel.carregarPersonagem(context, entry.id)
                                                if (loaded) {
                                                    creationSession++
                                                    mostrouTelaInicial = false
                                                    showLoadDialog = false
                                                    scope.launch {
                                                        snackHost.showSnackbar("Carregado: ${entry.nome}")
                                                    }
                                                } else {
                                                    scope.launch {
                                                        snackHost.showSnackbar("Falha ao carregar o personagem")
                                                    }
                                                }
                                            }) {
                                                Text("Carregar")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showLoadDialog = false }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }

            BackHandler(enabled = mostrouTelaInicial) {
                showExitDialog = true
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
                                onCriarNovo = { cartaSelvagem, maisPontosPericias, modoSupers, compendioFantasiaAtivo, compendioHorrorAtivo, compendioSciFiAtivo, compendioTrilhadorAtivo, compendioDeadlandsAtivo, compendioCrystalHeartAtivo, compendioArteDaGuerraAtivo, compendioCidadeSolVaporAtivo, compendioWiseguysAtivo, modoMonstroAtivo, _, _,
                                                nasceUmHeroi, heroisSemArmadura, usarEspecializacaoPer,
                                                semPontosDePoder, grandesResponsabilidades ->

                                    creationSession++

                                    criadorViewModel.resetStateParaNovoPersonagem(
                                        cartaSelvagem      = cartaSelvagem,
                                        maisPontosPericias = maisPontosPericias,
                                        modoSupers         = modoSupers,
                                        compendioFantasiaAtivo = compendioFantasiaAtivo,
                                        compendioHorrorAtivo = compendioHorrorAtivo,
                                        compendioSciFiAtivo = compendioSciFiAtivo,
                                        compendioTrilhadorAtivo = compendioTrilhadorAtivo,
                                        compendioDeadlandsAtivo = compendioDeadlandsAtivo,
                                        compendioCrystalHeartAtivo = compendioCrystalHeartAtivo,
                                        compendioArteDaGuerraAtivo = compendioArteDaGuerraAtivo,
                                        compendioCidadeSolVaporAtivo = compendioCidadeSolVaporAtivo,
                                        compendioWiseguysAtivo = compendioWiseguysAtivo,
                                        modoMonstroAtivo = modoMonstroAtivo,
                                        usarEspecializacoesDePericia = usarEspecializacaoPer
                                        // showHelpMessages removido
                                    )
                                    criadorViewModel.prepararNomeInicial(context)
                                    criadorViewModel.state.heroisSemArmadura     = heroisSemArmadura
                                    criadorViewModel.state.nasceUmHeroi          = nasceUmHeroi

                                    criadorViewModel.state.modoSuperequip        = modoSupers
                                    criadorViewModel.state.modoSuperComplicacoes = modoSupers

                                    criadorViewModel.state.usarSemPontosDePoder  = semPontosDePoder
                                    criadorViewModel.normalizeArcanoIdsNoCarregamento()
                                    criadorViewModel.state.grandesResponsabilidades = grandesResponsabilidades

                                    mostrouTelaInicial = false
                                },
                                onCarregarPersonagem = { showLoadDialog = true },
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
                                                // Titulo de ajuda removido
                                            }
                                        },
                                        navigationIcon = {
                                            TextButton(onClick = {
                                                triggerFeedback()
                                                criadorViewModel.resetToEmptyState()
                                                mostrouTelaInicial = true
                                            }) {
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
                                                triggerFeedback()
                                                saveName = state.nomePersonagem
                                                showSaveDialog = true
                                            }) {
                                                Icon(Icons.Default.Save, contentDescription = "Salvar personagem")
                                            }

                                            IconButton(onClick = {
                                                triggerFeedback()
                                                showLoadDialog = true
                                            }) {
                                                Icon(Icons.Default.FolderOpen, contentDescription = "Carregar personagem")
                                            }

                                            IconButton(onClick = {
                                                triggerFeedback()
                                                val personagem = state.toMeuPersonagem()

                                                scope.launch(Dispatchers.IO) {
                                                    produzirEExibirFichaPdf(this@MainActivity, personagem)
                                                }
                                            }) {
                                                Icon(Icons.Default.Print, contentDescription = "Imprimir ficha")
                                            }

                                            IconButton(onClick = {
                                                triggerFeedback()
                                                showSettingsDialog = true
                                            }) {
                                                Icon(Icons.Default.Settings, contentDescription = "Change Theme")
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
                                            equipamentoCategorias = equipamentoCategorias,
                                            superequipCategorias  = superequipCategorias,
                                            listaSuperPoderes     = listaSuperPoderes,
                                            modoOficialAtivo      = state.modoOficialAtivo
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

fun Int.toDiceString(): String =
    if (this <= 12) "d$this" else "d12+${(this - 12)}"

data class Pericia(val nome: String, val atributo: String, val basica: Boolean)

var listaComplicacoes: List<Complicacao> = emptyList()

var listaCoracoesCrystal: List<CrystalHeart> = emptyList()

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