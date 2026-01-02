@file:OptIn(
    ExperimentalMaterial3Api::class
)
@file:Suppress("LanguageDetectionInspection")

package com.example.swadebuilder

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.swadebuilder.model.Complicacao
import com.example.swadebuilder.model.CriadorViewModel
import com.example.swadebuilder.model.CrystalHeart
import com.example.swadebuilder.model.DataLoader
import com.example.swadebuilder.model.EquipamentoItem
import com.example.swadebuilder.model.MainActivityData
import com.example.swadebuilder.model.MonstroTemplate
import com.example.swadebuilder.model.RacialModifier
import com.example.swadebuilder.model.Tropo
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.ui.theme.SWADEbuilderTheme
import com.example.swadebuilder.util.AppPreferences
import com.example.swadebuilder.util.CharacterPortraitStorage
import com.example.swadebuilder.util.CharacterStorage
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.toEditionDisplayName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
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

private const val MULTIPLOS_AA_HABILITADOS: Boolean = false

enum class PendingNavigationAction {
    ReturnToHome,
    ResetAndReturnHome,
    StartProgression
}

private fun buildUsageInstructions(state: CriadorState, pathfinderLabel: String): String {
    val activeBooks = buildList {
        add("Básico (sempre ativo)")
        if (state.compendioFantasiaAtivo) add("Compêndio Fantasia")
        if (state.compendioHorrorAtivo) add("Compêndio Horror")
        if (state.compendioSciFiAtivo) add("Compêndio Sci-Fi")
        if (state.compendioBuscatrilhaAtivo) add("Compêndio $pathfinderLabel")
        if (state.compendioDeadlandsAtivo) add("Compêndio Deadlands".toEditionDisplayName())
        if (state.compendioArteDaGuerraAtivo) add("Arte da Guerra".toEditionDisplayName())
        if (state.compendioCidadeSolVaporAtivo) add("Cidade do Sol a Vapor".toEditionDisplayName())
        if (state.compendioWiseguysAtivo) add("Wiseguys".toEditionDisplayName())
        if (state.compendioCrystalHeartAtivo) add("Crystal Heart".toEditionDisplayName())
        if (state.modoSupers) add("Supers")
        if (state.modoMonstroAtivo) add("Monstros")
    }

    val booksText = activeBooks.joinToString(separator = "\n") { "• $it" }

    return buildString {
        append("Use as abas no topo para navegar entre as seções do personagem.")
        append("\n\n")
        append("Resumo: visão geral, anotações e retrato do personagem.")
        append("\nAtributos/Perícias/Vantagens/Complicações: ajuste os valores conforme as regras.")
        append("\nEquipamentos: adicione e remova itens do inventário.")
        append("\nXP: use os progressos quando estiver na fase de avanço.")
        append("\n\n")
        append("Livros ativos neste personagem:\n")
        append(booksText)
    }
}

@ExperimentalSerializationApi
class MainActivity : ComponentActivity() {

    private val isDataLoaded = MutableStateFlow(false)
    private lateinit var mainActivityData: MainActivityData

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    @OptIn(ExperimentalMaterial3Api::class)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        lifecycleScope.launch(Dispatchers.IO) {
            mainActivityData = DataLoader.load(this@MainActivity)
            isDataLoaded.value = true
        }

        setContent {
            val dataLoaded by isDataLoaded.collectAsState()

            if (!dataLoaded) {
                 Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                      CircularProgressIndicator()
                 }
            } else {
                val equipamentoCategorias = mainActivityData.equipamentoCategorias
                val superequipCategorias = mainActivityData.superequipCategorias
                val listaSuperPoderes = mainActivityData.listaSuperPoderes

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
            LaunchedEffect(Unit) {
                val prefs = AppPreferences.loadFeedbackPrefs(
                    context,
                    CriadorState.DEFAULT_HAPTIC_STRENGTH,
                    CriadorState.DEFAULT_SOUND_VOLUME
                )
                state.hapticStrength = prefs.hapticStrength
                state.soundVolume = prefs.soundVolume
            }
            val persistFeedbackPrefs: () -> Unit = remember {
                {
                    AppPreferences.saveFeedbackPrefs(
                        context,
                        state.hapticStrength,
                        state.soundVolume
                    )
                }
            }
            val triggerFeedback = remember(state.hapticStrength, state.soundVolume) {
                { feedbackController.play(state.hapticStrength, state.soundVolume) }
            }

            var showSettingsDialog by rememberSaveable { mutableStateOf(false) }
            var showHelpDialog by rememberSaveable { mutableStateOf(false) }
            var showThemeSelectionDialog by rememberSaveable { mutableStateOf(false) }

            var showSaveDialog by rememberSaveable { mutableStateOf(false) }
            var showLoadDialog by rememberSaveable { mutableStateOf(false) }
            var saveName by rememberSaveable { mutableStateOf("") }
            var pendingNavigationAction by rememberSaveable {
                mutableStateOf<PendingNavigationAction?>(null)
            }
            var showSaveBeforeNavigateDialog by rememberSaveable { mutableStateOf(false) }

            val savedEntries = remember { mutableStateListOf<CharacterStorage.SaveEntry>() }
            var entryToDelete by remember { mutableStateOf<CharacterStorage.SaveEntry?>(null) }

            val startProgression = {
                criadorViewModel.ensureDefaultSpecializations()
                state.modoProgressaoAtivo = true
                state.progresso = 4
                state.frozenAdvantageCount = state.vantagensSelecionadas.size
                state.snapshotFrozenSkillIncrements()
                state.recomputeAvailableProgress()
            }

            val executePendingNavigation: (PendingNavigationAction) -> Unit = { action ->
                when (action) {
                    PendingNavigationAction.ReturnToHome -> {
                        mostrouTelaInicial = true
                    }
                    PendingNavigationAction.ResetAndReturnHome -> {
                        criadorViewModel.resetToEmptyState()
                        mostrouTelaInicial = true
                    }
                    PendingNavigationAction.StartProgression -> {
                        startProgression()
                    }
                }
            }

            val requestNavigation: (PendingNavigationAction) -> Unit = { action ->
                pendingNavigationAction = action
                showSaveBeforeNavigateDialog = true
            }

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
                                onValueChangeFinished = {
                                    persistFeedbackPrefs()
                                    feedbackController.play(state.hapticStrength, 0)
                                },
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
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            Text("Sons do app")
                            Slider(
                                value = state.soundVolume.toFloat(),
                                onValueChange = { state.soundVolume = it.roundToInt() },
                                onValueChangeFinished = {
                                    persistFeedbackPrefs()
                                    feedbackController.play(0, state.soundVolume)
                                },
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
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            // Retrato: Expandir
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Expandir retrato no resumo")
                                androidx.compose.material3.Switch(
                                    checked = state.expandirRetrato,
                                    onCheckedChange = { state.expandirRetrato = it }
                                )
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

            if (showHelpDialog) {
                val pathfinderLabel = stringResource(R.string.sw_pathfinder_label)
                AlertDialog(
                    onDismissRequest = { showHelpDialog = false },
                    title = { Text("Como usar o app") },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 360.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(buildUsageInstructions(state, pathfinderLabel))
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showHelpDialog = false }) {
                            Text("Entendi")
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
                        com.example.swadebuilder.ui.theme.AppTheme.MINIMALIST to "Minimalista",
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
                                scope.launch {
                                    val snapshotToDelete = when (
                                        val result = CharacterStorage.load(context, entry.id)
                                    ) {
                                        is CharacterStorage.LoadResult.Success -> result.snapshot
                                        else -> null
                                    }
                                    CharacterStorage.delete(context, entry.id)
                                    savedEntries.removeAll { it.id == entry.id }
                                    if (state.idAtual == entry.id) {
                                        state.idAtual = null
                                    }
                                    snapshotToDelete?.selecoes?.retratoFileName?.let { fileName ->
                                        CharacterPortraitStorage.deleteIfUnused(context, fileName)
                                    }
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
                            triggerFeedback()
                            scope.launch {
                                val entry = criadorViewModel.salvarPersonagem(
                                    context,
                                    saveName.ifBlank { state.nomePersonagem }
                                )
                                showSaveDialog = false
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

            if (showSaveBeforeNavigateDialog && pendingNavigationAction != null) {
                val action = pendingNavigationAction!!
                val dialogMessage = when (action) {
                    PendingNavigationAction.StartProgression ->
                        "Deseja salvar o personagem antes de ir para os progressos?"
                    PendingNavigationAction.ReturnToHome,
                    PendingNavigationAction.ResetAndReturnHome ->
                        "Deseja salvar o personagem antes de voltar para a tela inicial?"
                }

                AlertDialog(
                    onDismissRequest = {
                        showSaveBeforeNavigateDialog = false
                        pendingNavigationAction = null
                    },
                    title = { Text("Salvar personagem?") },
                    text = { Text(dialogMessage) },
                    confirmButton = {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = {
                                triggerFeedback()
                                scope.launch {
                                    val entry = criadorViewModel.salvarPersonagem(
                                        context,
                                        state.nomePersonagem
                                    )
                                    showSaveBeforeNavigateDialog = false
                                    pendingNavigationAction = null
                                    executePendingNavigation(action)

                                    launch {
                                        snackHost.showSnackbar("Personagem salvo: ${entry.nome}")
                                    }
                                }
                            }) {
                                Text("Salvar")
                            }
                            TextButton(onClick = {
                                triggerFeedback()
                                showSaveBeforeNavigateDialog = false
                                pendingNavigationAction = null
                                executePendingNavigation(action)
                            }) {
                                Text("Não salvar")
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showSaveBeforeNavigateDialog = false
                            pendingNavigationAction = null
                        }) {
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
                                                triggerFeedback()
                                                scope.launch {
                                                    val result = criadorViewModel.carregarPersonagem(
                                                        context,
                                                        entry.id
                                                    )
                                                    if (result.success) {
                                                        creationSession++
                                                        mostrouTelaInicial = false
                                                        showLoadDialog = false
                                                        snackHost.showSnackbar("Carregado: ${entry.nome}")
                                                    } else {
                                                        snackHost.showSnackbar(
                                                            result.message
                                                                ?: "Falha ao carregar o personagem"
                                                        )
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
                                onCriarNovo = { cartaSelvagem, maisPontosPericias, modoSupers, compendioFantasiaAtivo, compendioHorrorAtivo, compendioSciFiAtivo, compendioBuscatrilhaAtivo, compendioDeadlandsAtivo, compendioCrystalHeartAtivo, compendioArteDaGuerraAtivo, compendioCidadeSolVaporAtivo, compendioWiseguysAtivo, modoMonstroAtivo,
                                                nasceUmHeroi, heroisSemArmadura, usarEspecializacaoPer,
                                                semPontosDePoder, multiplosIdiomas, grandesResponsabilidades,
                                                optRegraFama, optRegraRiqueza, optRegraCosaNostra ->

                                    creationSession++

                                    criadorViewModel.resetStateParaNovoPersonagem(
                                        cartaSelvagem      = cartaSelvagem,
                                        maisPontosPericias = maisPontosPericias,
                                        modoSupers         = modoSupers,
                                        compendioFantasiaAtivo = compendioFantasiaAtivo,
                                        compendioHorrorAtivo = compendioHorrorAtivo,
                                        compendioSciFiAtivo = compendioSciFiAtivo,
                                        compendioBuscatrilhaAtivo = compendioBuscatrilhaAtivo,
                                        compendioDeadlandsAtivo = compendioDeadlandsAtivo,
                                        compendioCrystalHeartAtivo = compendioCrystalHeartAtivo,
                                        compendioArteDaGuerraAtivo = compendioArteDaGuerraAtivo,
                                        compendioCidadeSolVaporAtivo = compendioCidadeSolVaporAtivo,
                                        compendioWiseguysAtivo = compendioWiseguysAtivo,
                                        modoMonstroAtivo = modoMonstroAtivo,
                                        usarEspecializacoesDePericia = usarEspecializacaoPer,
                                        regraMultiplosIdiomas = multiplosIdiomas,
                                        optRegraFama = optRegraFama,
                                        optRegraRiqueza = optRegraRiqueza,
                                        optRegraCosaNostra = optRegraCosaNostra
                                        // showHelpMessages removido
                                    )
                                    scope.launch {
                                        criadorViewModel.prepararNomeInicial(context)
                                    }
                                    criadorViewModel.state.heroisSemArmadura     = heroisSemArmadura
                                    criadorViewModel.state.nasceUmHeroi          = nasceUmHeroi

                                    criadorViewModel.state.usarSemPontosDePoder  = semPontosDePoder
                                    criadorViewModel.normalizeArcanoIdsNoCarregamento()
                                    criadorViewModel.state.grandesResponsabilidades = grandesResponsabilidades

                                    mostrouTelaInicial = false
                                },
                                onCarregarPersonagem = { showLoadDialog = true },
                                onOpenSettings = { showSettingsDialog = true },
                                context   = context,
                                viewModel = criadorViewModel
                            )
                        } else {
                            BackHandler {
                                requestNavigation(PendingNavigationAction.ReturnToHome)
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
                                                TextButton(onClick = {
                                                    triggerFeedback()
                                                    showHelpDialog = true
                                                }) {
                                                    Text(
                                                        text = "Como usar",
                                                        style = MaterialTheme.typography.titleMedium,
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                            }
                                        },
                                        navigationIcon = {
                                            TextButton(onClick = {
                                                triggerFeedback()
                                                requestNavigation(PendingNavigationAction.ResetAndReturnHome)
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
                                            modoOficialAtivo      = state.modoOficialAtivo,
                                            onShowMessage         = { message ->
                                                scope.launch {
                                                    snackHost.showSnackbar(message)
                                                }
                                            },
                                            onUserFeedback        = triggerFeedback,
                                            onRequestProgression  = {
                                                requestNavigation(PendingNavigationAction.StartProgression)
                                            }
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
            } // Close else block
        }
    }
}

fun Int.toDiceString(): String =
    if (this == 0) "-" else if (this <= 12) "d$this" else "d12+${(this - 12)}"

data class Pericia(
    val nome: String,
    val atributo: String,
    val basica: Boolean,
    val origem: String? = null
)

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
lateinit var mapaPericias: Map<String, Pericia>
lateinit var mapaPericiasDescricao: Map<String, String>
lateinit var mapaPericiasDescricaoAdg: Map<String, String>
lateinit var mapaAtributosDescricao: Map<String, String>

fun periciaStartRaw(anc: String, per: Pericia): Int {
    val ancKey = anc.keyify()
    val perKey = per.nome.keyify()
    return racialSkillStartMap[ancKey]?.get(perKey)
        ?: if (per.basica) 4 else 0
}

var listaVantagens:    List<Vantagem>   = emptyList()
lateinit var listaTropos: List<Tropo>
var listaEquipamentos: List<EquipamentoItem> = emptyList()

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
