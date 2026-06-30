@file:OptIn(
    ExperimentalMaterial3Api::class
)
@file:Suppress("LanguageDetectionInspection", "unused")

package com.example.swadebuilder

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MoodBad
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsMartialArts
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.swadebuilder.model.CriadorViewModel
import com.example.swadebuilder.model.SnapshotFlags
import com.example.swadebuilder.security.SecurityHardening
import com.example.swadebuilder.ui.components.SettingsDialog
import com.example.swadebuilder.ui.theme.SWADEbuilderTheme
import com.example.swadebuilder.util.AppPreferences
import com.example.swadebuilder.util.CharacterPortraitStorage
import com.example.swadebuilder.util.CharacterStorage
import com.example.swadebuilder.util.SecurityUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi

private const val MULTIPLOS_AA_HABILITADOS: Boolean = false

enum class PendingNavigationAction {
    ReturnToHome,
    ResetAndReturnHome,
    StartProgression
}

@ExperimentalSerializationApi
class MainActivity : ComponentActivity() {

    private val isDataLoaded = MutableStateFlow<LoadingState>(LoadingState.Loading)

    private fun getModuleIcon(flags: SnapshotFlags?): ImageVector {
        if (flags == null) return Icons.AutoMirrored.Filled.MenuBook
        return when {
            flags.modoSupers -> Icons.Default.Bolt
            flags.compendioPathfinderAtivo -> Icons.Default.Map
            flags.compendioDeadlandsAtivo -> Icons.Default.Shield
            flags.compendioCrystalHeartAtivo -> Icons.Default.Favorite
            flags.compendioArteDaGuerraAtivo -> Icons.Filled.SportsMartialArts
            flags.compendioCidadeSolVaporAtivo -> Icons.Default.Build
            flags.compendioWiseguysAtivo -> Icons.Default.Groups
            flags.compendioFantasiaAtivo -> Icons.Default.AutoAwesome
            flags.compendioHorrorAtivo -> Icons.Default.MoodBad
            flags.compendioSciFiAtivo -> Icons.Default.RocketLaunch
            else -> Icons.AutoMirrored.Filled.MenuBook
        }
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    @OptIn(ExperimentalMaterial3Api::class)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Security Integrity Check
        try {
            SecurityHardening.integrityCheck(this)
        } catch (_: SecurityException) {
            finishAffinity()
            android.os.Process.killProcess(android.os.Process.myPid())
            return
        }

        // Protect against Tapjacking (Overlay attacks)
        window.decorView.filterTouchesWhenObscured = true

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val viewModel = ViewModelProvider(this)[CriadorViewModel::class.java]
        val activeKeys = viewModel.state.getActiveModuleKeys()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                viewModel.carregarDadosDeJogo(this@MainActivity, activeKeys)
                isDataLoaded.value = LoadingState.Success

                launch {
                    viewModel.prewarmBaselineData(this@MainActivity)
                    viewModel.prewarmLikelyModuleTransitions(this@MainActivity, activeKeys)
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Erro ao carregar dados: ${e.message}")
                isDataLoaded.value = LoadingState.Error(e.message ?: "Erro desconhecido")
            }
        }

        setContent {
            val loadingState by isDataLoaded.collectAsState()

            when (val currentState = loadingState) {
                is LoadingState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
                is LoadingState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Erro ao carregar dados:\n${currentState.message}",
                            color = Color.Red,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                is LoadingState.Success -> {
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
                val prefs = AppPreferences.loadPrefs(
                    context,
                    CriadorState.DEFAULT_HAPTIC_STRENGTH,
                    CriadorState.DEFAULT_SOUND_VOLUME
                )
                state.hapticStrength = prefs.hapticStrength
                state.soundVolume = prefs.soundVolume
                state.estiloAbas = prefs.tabStyle
                state.mostrarIdentificadorLivro = prefs.showBookIcon
                state.mostrarDescricaoHome = prefs.showDescHome
                state.showSystemMessages = prefs.showSystemMessages
                state.appTheme = prefs.appTheme
            }
            val persistPrefs: () -> Unit = remember {
                {
                    AppPreferences.savePrefs(
                        context,
                        state.hapticStrength,
                        state.soundVolume,
                        state.estiloAbas,
                        state.mostrarIdentificadorLivro,
                        state.mostrarDescricaoHome,
                        state.showSystemMessages,
                        state.appTheme
                    )
                }
            }

            val triggerFeedback = remember(state.hapticStrength, state.soundVolume) {
                { feedbackController.play(state.hapticStrength, state.soundVolume) }
            }

            var showSettingsDialog by rememberSaveable { mutableStateOf(false) }

            var showSaveDialog by rememberSaveable { mutableStateOf(false) }
            var showLoadDialog by rememberSaveable { mutableStateOf(false) }
            var showResetDialog by rememberSaveable { mutableStateOf(false) } // New Reset Dialog
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

            // Observe feedback messages and show snackbar
            LaunchedEffect(criadorViewModel.feedbackMessages.size) {
                if (criadorViewModel.feedbackMessages.isNotEmpty()) {
                    val message = criadorViewModel.feedbackMessages.last()
                    if (state.showSystemMessages) {
                        snackHost.showSnackbar(message)
                    }
                    criadorViewModel.clearFeedbackMessages()
                }
            }

            // -- Settings Dialog --
            if (showSettingsDialog) {
                SWADEbuilderTheme(appTheme = state.appTheme) {
                    SettingsDialog(
                        state = state,
                        onDismiss = { showSettingsDialog = false },
                        persistPrefs = { persistPrefs() },
                        feedbackController = feedbackController,
                        onThemeSelected = { theme -> criadorViewModel.setAppTheme(theme) }
                    )
                }
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
                val isValid = SecurityUtils.isValidFilename(saveName)
                var saveAsNew by rememberSaveable { mutableStateOf(false) }

                AlertDialog(
                    onDismissRequest = { showSaveDialog = false },
                    title = { Text("Salvar personagem") },
                    text = {
                        Column {
                            Text("Defina um nome para o salvamento.")
                            OutlinedTextField(
                                value = saveName,
                                onValueChange = { saveName = it },
                                label = { Text("Nome do arquivo") },
                                isError = !isValid,
                                supportingText = if (!isValid) {
                                    { Text("Inválido: use apenas letras, números, '.', '_' ou '-' (máx 50).") }
                                } else null,
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (state.idAtual != null) {
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Checkbox(
                                        checked = saveAsNew,
                                        onCheckedChange = { saveAsNew = it }
                                    )
                                    Text(
                                        text = "Salvar como novo arquivo (Cópia)",
                                        modifier = Modifier.clickable { saveAsNew = !saveAsNew }
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                triggerFeedback()
                                scope.launch {
                                    val entry = criadorViewModel.salvarPersonagem(
                                        context,
                                        saveName,
                                        criarCopia = saveAsNew
                                    )
                                    showSaveDialog = false
                                    snackHost.showSnackbar("Personagem salvo: ${entry.nome}")
                                }
                            },
                            enabled = isValid
                        ) {
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
                var selectedEntry by remember { mutableStateOf<CharacterStorage.SaveEntry?>(null) }

                AlertDialog(
                    onDismissRequest = { showLoadDialog = false },
                    title = { Text("Carregar personagem") },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 350.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            if (savedEntries.isEmpty()) {
                                Text("Nenhum personagem salvo.")
                            } else {
                                savedEntries.forEach { entry ->
                                    val isSelected = selectedEntry?.id == entry.id
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                            .clickable { selectedEntry = if (isSelected) null else entry }
                                            .padding(vertical = 8.dp, horizontal = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = getModuleIcon(entry.flags),
                                            contentDescription = null,
                                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(end = 16.dp)
                                        )

                                        Text(
                                            text = entry.nome,
                                            modifier = Modifier.weight(1f),
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    HorizontalDivider()
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { showLoadDialog = false }) {
                                if (state.estiloAbas == TabStyle.ICONES) {
                                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Fechar")
                                } else {
                                    Text("Fechar", style = MaterialTheme.typography.bodySmall)
                                }
                            }

                            Row {
                                TextButton(
                                    onClick = { entryToDelete = selectedEntry },
                                    enabled = selectedEntry != null
                                ) {
                                    if (state.estiloAbas == TabStyle.ICONES) {
                                        Icon(Icons.Default.Delete, contentDescription = "Apagar")
                                    } else {
                                        Text("Apagar", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                                Spacer(Modifier.width(8.dp))
                                TextButton(
                                    onClick = {
                                        selectedEntry?.let { entry ->
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
                                        }
                                    },
                                    enabled = selectedEntry != null
                                ) {
                                    if (state.estiloAbas == TabStyle.ICONES) {
                                        Icon(Icons.Default.Upload, contentDescription = "Carregar")
                                    } else {
                                        Text("Carregar", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    },
                    dismissButton = null
                )
            }

            if (showResetDialog) {
                AlertDialog(
                    onDismissRequest = { showResetDialog = false },
                    title = { Text("Limpar personagem") },
                    text = { Text("Deseja limpar a ficha atual e iniciar um novo personagem?") },
                    confirmButton = {
                        TextButton(onClick = {
                            val cartaSelvagem = state.cartaSelvagem
                            val maisPontosPericias = state.maisPontosPericias
                            val modoSupers = state.modoSupers
                            val compendioFantasiaAtivo = state.compendioFantasiaAtivo
                            val compendioHorrorAtivo = state.compendioHorrorAtivo
                            val compendioSciFiAtivo = state.compendioSciFiAtivo
                            val compendioPathfinderAtivo = state.compendioPathfinderAtivo
                            val compendioDeadlandsAtivo = state.compendioDeadlandsAtivo
                            val compendioCrystalHeartAtivo = state.compendioCrystalHeartAtivo
                            val compendioArteDaGuerraAtivo = state.compendioArteDaGuerraAtivo
                            val compendioCidadeSolVaporAtivo = state.compendioCidadeSolVaporAtivo
                            val compendioWiseguysAtivo = state.compendioWiseguysAtivo
                            val modoMonstroAtivo = state.modoMonstroAtivo
                            val usarEspecializacoesDePericia = state.usarEspecializacoesDePericia
                            val grandesResponsabilidades = state.grandesResponsabilidades
                            val regraMultiplosIdiomas = state.regraMultiplosIdiomas
                            val nasceUmHeroi = state.nasceUmHeroi
                            val usarSemPontosDePoder = state.usarSemPontosDePoder
                            val compendioScifiMechasCiberneticosAtivo = state.compendioScifiMechasCiberneticosAtivo

                            criadorViewModel.resetStateParaNovoPersonagem(
                                cartaSelvagem = cartaSelvagem,
                                maisPontosPericias = maisPontosPericias,
                                modoSupers = modoSupers,
                                compendioFantasiaAtivo = compendioFantasiaAtivo,
                                compendioHorrorAtivo = compendioHorrorAtivo,
                                compendioSciFiAtivo = compendioSciFiAtivo,
                                compendioScifiMechasCiberneticosAtivo = compendioScifiMechasCiberneticosAtivo,
                                compendioPathfinderAtivo = compendioPathfinderAtivo,
                                compendioDeadlandsAtivo = compendioDeadlandsAtivo,
                                compendioCrystalHeartAtivo = compendioCrystalHeartAtivo,
                                compendioArteDaGuerraAtivo = compendioArteDaGuerraAtivo,
                                compendioCidadeSolVaporAtivo = compendioCidadeSolVaporAtivo,
                                compendioWiseguysAtivo = compendioWiseguysAtivo,
                                modoMonstroAtivo = modoMonstroAtivo,
                                usarEspecializacoesDePericia = usarEspecializacoesDePericia,
                                grandesResponsabilidades = grandesResponsabilidades,
                                regraMultiplosIdiomas = regraMultiplosIdiomas
                            )
                            scope.launch {
                                criadorViewModel.prepararNomeInicial(context)
                            }
                            state.nasceUmHeroi = nasceUmHeroi
                            state.usarSemPontosDePoder = usarSemPontosDePoder
                            state.grandesResponsabilidades = grandesResponsabilidades
                            showResetDialog = false
                            scope.launch {
                                snackHost.showSnackbar("Ficha limpa.")
                            }
                        }) {
                            Text("Limpar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showResetDialog = false }) {
                            Text("Cancelar")
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
                                onCriarNovo = { cartaSelvagem, maisPontosPericias, modoSupers, compendioFantasiaAtivo, compendioHorrorAtivo, compendioSciFiAtivo, compendioPathfinderAtivo, compendioDeadlandsAtivo, compendioCrystalHeartAtivo, compendioArteDaGuerraAtivo, compendioCidadeSolVaporAtivo, compendioWiseguysAtivo, modoMonstroAtivo,
                                                nasceUmHeroi, usarEspecializacaoPer,
                                                semPontosDePoder, multiplosIdiomas, grandesResponsabilidades,
                                                optRegraFama, optRegraRiqueza, optRegraCosaNostra,
                                                optRegraMechasCiberneticos ->

                                    creationSession++

                                    criadorViewModel.resetStateParaNovoPersonagem(
                                        cartaSelvagem      = cartaSelvagem,
                                        maisPontosPericias = maisPontosPericias,
                                        modoSupers         = modoSupers,
                                        compendioFantasiaAtivo = compendioFantasiaAtivo,
                                        compendioHorrorAtivo = compendioHorrorAtivo,
                                        compendioSciFiAtivo = compendioSciFiAtivo,
                                        compendioScifiMechasCiberneticosAtivo = optRegraMechasCiberneticos,
                                        compendioPathfinderAtivo = compendioPathfinderAtivo,
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
                                    )
                                    scope.launch {
                                        criadorViewModel.prepararNomeInicial(context)
                                    }
                                    criadorViewModel.state.nasceUmHeroi          = nasceUmHeroi

                                    criadorViewModel.state.usarSemPontosDePoder  = semPontosDePoder
                                    criadorViewModel.normalizeArcanoIdsNoCarregamento()
                                    criadorViewModel.state.grandesResponsabilidades = grandesResponsabilidades

                                    mostrouTelaInicial = false
                                },
                                onCarregarPersonagem = { showLoadDialog = true },
                                onOpenSettings = { showSettingsDialog = true },
                                viewModel = criadorViewModel
                            )
                        } else {
                            BackHandler {
                                requestNavigation(PendingNavigationAction.ReturnToHome)
                            }

                            Scaffold(
                                snackbarHost = { SnackbarHost(hostState = snackHost) },
                                floatingActionButton = {
                                    if (!state.modoProgressaoAtivo && !state.isNpcExibicao && (state.modoLivre || state.creationComplete())) {
                                        ExtendedFloatingActionButton(
                                            onClick = {
                                                triggerFeedback()
                                                if (state.modoLivre) {
                                                    state.isNpcExibicao = true
                                                } else {
                                                    requestNavigation(PendingNavigationAction.StartProgression)
                                                }
                                            },
                                            icon = { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null) },
                                            text = { Text("Finalizar") },
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                },
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
                                            ) {}
                                        },
                                        navigationIcon = {
                                            if (state.estiloAbas == TabStyle.ICONES) {
                                                IconButton(onClick = {
                                                    triggerFeedback()
                                                    requestNavigation(PendingNavigationAction.ResetAndReturnHome)
                                                }) {
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                        contentDescription = "Voltar"
                                                    )
                                                }
                                            } else {
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
                                            }
                                        },
                                        actions = {
                                            // Reset Character Button
                                            if (!state.modoProgressaoAtivo) {
                                                IconButton(onClick = {
                                                    triggerFeedback()
                                                    showResetDialog = true
                                                }) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Reiniciar personagem")
                                                }
                                            }

                                            IconButton(onClick = {
                                                triggerFeedback()
                                                saveName = SecurityUtils.sanitizeFilename(state.nomePersonagem)
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
                                                val attributes = criadorViewModel.gameDataStore.getAtributos()
                                                val mapAttrDisplay = criadorViewModel.gameDataStore.getMapaAtributosDisplay()
                                                val complications = criadorViewModel.gameDataStore.getComplicacoes()
                                                val advantages = criadorViewModel.gameDataStore.getVantagens()
                                                val powers = criadorViewModel.gameDataStore.getPoderes()

                                                scope.launch(Dispatchers.IO) {
                                                    produzirEExibirFichaPdf(
                                                        this@MainActivity,
                                                        personagem,
                                                        attributes,
                                                        mapAttrDisplay,
                                                        complications,
                                                        advantages,
                                                        powers
                                                    ) { msg ->
                                                        scope.launch {
                                                            snackHost.showSnackbar(msg)
                                                        }
                                                    }
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
                                            equipamentoCategorias = criadorViewModel.gameDataStore.getEquipamentoCategorias(),
                                            superequipCategorias  = criadorViewModel.gameDataStore.getSuperequipCategorias(),
                                            listaSuperPoderes     = criadorViewModel.gameDataStore.getSuperPoderes(),
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
            } // Close else block (actually close Success state block)
            } // Close when
        }
    }
}

sealed class LoadingState {
    object Loading : LoadingState()
    object Success : LoadingState()
    data class Error(val message: String) : LoadingState()
}
