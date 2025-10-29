@file:OptIn(
    ExperimentalMaterial3Api::class
)
@file:Suppress("DEPRECATION", "LanguageDetectionInspection")

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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.booleanResource
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
import com.example.swadebuilder.model.Complicacao
import com.example.swadebuilder.model.CriadorViewModel
import com.example.swadebuilder.model.EquipamentoCategoria
import com.example.swadebuilder.model.EquipamentoItem
import com.example.swadebuilder.model.MeuPersonagem
import com.example.swadebuilder.model.PericiaList
import com.example.swadebuilder.model.PersonagemSalvo
import com.example.swadebuilder.model.Poder
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
import com.example.swadebuilder.ui.sections.PoderesSection
import com.example.swadebuilder.ui.sections.VantagensContent
import com.example.swadebuilder.ui.theme.SWADEbuilderTheme
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.loadJsonAsset
import com.example.swadebuilder.util.semAcentos
import com.example.swadebuilder.util.toStringList
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
import kotlin.math.roundToInt
import com.example.swadebuilder.model.loadJsonAsset as loadPoderesAsset


@Serializable
data class ArcanoInfo(
    val key: String,
    val slots: Int,
    val pp: Int,
    val foco: String
)

lateinit var arcanoInfo: Map<String, Triple<Int, Int, String>>

data class PurchasedPower(val nome: String, val custo: Int)

private val json = Json {
    ignoreUnknownKeys = true
}

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

        val ancestralRaw = loadRawText(this, R.raw.listaancestralidade)
        listaAncestralidadesJson = Json
            .decodeFromString<List<RacialModifier>>(ancestralRaw)

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

            SWADEbuilderTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter            = painterResource(R.drawable.bg_pergaminho),
                        contentDescription = null,
                        modifier           = Modifier.matchParentSize(),
                        contentScale       = ContentScale.Crop
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .systemBarsPadding()
                    ) {
                        if (mostrouTelaInicial) {
                            TelaInicial(
                                onCriarNovo = { cartaSelvagem, maisPontosPericias, modoSupers, modoSuperequip, modoSuperComplicacoes ->
                                    criadorViewModel.resetStateParaNovoPersonagem(
                                        cartaSelvagem      = cartaSelvagem,
                                        maisPontosPericias = maisPontosPericias,
                                        modoSupers         = modoSupers
                                    )
                                    criadorViewModel.state.modoSuperequip = modoSuperequip
                                    criadorViewModel.state.modoSuperComplicacoes = modoSuperComplicacoes

                                    mostrouTelaInicial = false
                                },
                                onLoad = { salvo ->
                                    criadorViewModel.loadFromSalvo(salvo, equipamentoCategorias)
                                    mostrouTelaInicial = false
                                },
                                context              = context,
                                viewModel            = criadorViewModel
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
                                                IconButton(onClick = {
                                                    val personagem = MeuPersonagem(
                                                        nome            = state.nomePersonagem,
                                                        atributos       = state.valoresAtributos.mapValues { it.value.intValue },
                                                        pericias        = listaPericias.associate { per -> per.nome to state.rawTotal(per) },
                                                        ancestralidade  = state.ancestralidade,
                                                        vantagens       = state.vantagensSelecionadas.map { it.nome },
                                                        complicacoes    = state.complicacoesSelecionadas
                                                            .filterValues { it != null }
                                                            .keys
                                                            .map { it.id },
                                                        equipamentos    = state.equipamentosComprados.toList(),
                                                        poderes         = state.poderSlotsPorArcano.mapValues { (_, slots) -> slots.filterNotNull() },
                                                        dinheiro        = state.dinheiro,
                                                        pontosRestantes = state.pontosVantagem
                                                    )
                                                    salvarEExibirFichaPdf(this@MainActivity, personagem)
                                                }) {
                                                    Icon(Icons.Default.Print, contentDescription = "Imprimir ficha")
                                                }
                                                IconButton(onClick = {
                                                    val personagemId     = state.idAtual ?: UUID.randomUUID().toString()
                                                    val atributosMap     = state.valoresAtributos.mapValues { it.value.intValue }
                                                    val periciasMap      = listaPericias.associate { per -> per.nome to state.rawTotal(per) }
                                                    val vantagensList    = state.vantagensSelecionadas.map { it.nome }
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
                                                        vantagens          = vantagensList,
                                                        complicacoes       = complicacoesList,
                                                        equipamentos       = state.equipamentosComprados.map { it.nome },
                                                        poderes            = state.poderSlotsPorArcano.mapValues { (_, slots) -> slots.filterNotNull() },
                                                        dinheiro           = state.dinheiro,
                                                        pontosRestantes    = state.pontosVantagem,
                                                        maisPontosPericias = state.maisPontosPericias,
                                                        cartaSelvagem      = state.cartaSelvagem
                                                    )
                                                    state.idAtual = personagemId
                                                    StorageUtils.salvarPersonagem(context, salvo)
                                                    Toast.makeText(
                                                        context,
                                                        "Personagem salvo com sucesso!",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
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
                                                state = state,
                                                modoSupers = state.modoSupers,
                                                highlightedName = highlightedVantagem,
                                                onBack = { showVantagensDetail = false }
                                            )
                                            4 -> PericiasDetailScreen(
                                                state = state,
                                                onBack = { showPericiasDetail = false }
                                            )
                                            5 -> ComplicacoesDetailScreen(
                                                state = state,
                                                onBack = { showComplicacoesDetail = false },
                                                mostrarSuper = state.modoSuperComplicacoes
                                            )
                                            6 -> AncestralidadesDetailScreen(
                                                state = state,
                                                onBack = { showAncestralidadesDetail = false }
                                            )
                                            7 -> PoderesDetailScreen(
                                                state = state,
                                                onBack = { showPoderesDetail = false }
                                            )
                                            8 -> PoderesDetailScreen(
                                                state = state,
                                                onBack = { showSuperDetail = false }
                                            )

                                            else -> UnifiedScreen(
                                                state = state,
                                                onOpenVantagensDetail = { nomeVantagem ->
                                                    highlightedVantagem = nomeVantagem.toString()
                                                    showVantagensDetail = true
                                                },
                                                onOpenPericiasDetail = { showPericiasDetail = true },
                                                onOpenComplicacoesDetail = { showComplicacoesDetail = true },
                                                onOpenAtributosDetail = { showAtributosDetail = true },
                                                onOpenListaAncestralidadesDetail = { showAncestralidadesDetail = true },
                                                onOpenListaCompletaEquipamento = { showEquipLista = true },
                                                onOpenPoderesDetail = { showPoderesDetail = true },
                                                onHelpSuperClick = { },

                                                expAttrs = expAttrs,
                                                onToggleAttrs = { expAttrs   = !expAttrs },
                                                expPer = expPer,
                                                onTogglePer = { expPer     = !expPer },
                                                expVants = expVants,
                                                onToggleVants = { expVants   = !expVants },
                                                expResumo = expResumo,
                                                onToggleResumo = { expResumo  = !expResumo },
                                                expPoderes = expPoderes,
                                                onTogglePoderes = { expPoderes = !expPoderes },

                                                equipamentoCategorias = equipamentoCategorias,
                                                superequipCategorias = superequipCategorias,
                                                listaSuperPoderes = listaSuperPoderes
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
    if (this <= 12) "d$this" else "d12+${(this - 12) / 2}"

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

    // ── Atributos ────────────────────────────────────────────────
    lines += "Atributos"
    lines += personagem.atributos.entries
        .joinToString(", ") { (nome, valor) -> "$nome d$valor" }
    lines += ""

    // ── Perícias ──────────────────────────────────────────────────
    val periciasParaMostrar = listaPericias.filter { per ->
        per.basica || (personagem.pericias[per.nome] ?: 0) > periciaStartRaw(personagem.ancestralidade, per)
    }
    lines += "Perícias"
    lines += if (periciasParaMostrar.isEmpty()) {
        "– Nenhuma"
    } else {
        periciasParaMostrar
            .joinToString(", ") { per ->
                "${per.nome} d${personagem.pericias[per.nome] ?: 0}"
            }
    }
    lines += ""

    // ── Vantagens ─────────────────────────────────────────────────
    lines += "Vantagens"
    lines += if (personagem.vantagens.isEmpty()) {
        "– Nenhuma"
    } else {
        personagem.vantagens.joinToString(", ")
    }
    lines += ""

    // ── Complicações ──────────────────────────────────────────────
    lines += "Complicações"
    lines += if (personagem.complicacoes.isEmpty()) {
        "– Nenhuma"
    } else {
        personagem.complicacoes.joinToString(", ")
    }
    lines += ""

    // ── Equipamentos Comprados ───────────────────────────────────
    lines += "Equipamentos Comprados"
    if (personagem.equipamentos.isEmpty()) {
        lines += "– Nenhum"
    } else {
        lines += personagem.equipamentos.joinToString(", ") { eq ->
            val detalhes = mutableListOf(
                "Custo: ${eq.custo}",
                "Peso: ${eq.peso}"
            )
            eq.dano?.let     { detalhes += "Dano: $it" }
            eq.armadura?.let { detalhes += "Armadura: $it" }
            "${eq.nome} (${detalhes.joinToString(", ")})"
        }
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

    // Configura Paint e altura de linha
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
            // quantos caracteres cabem em uma linha
            val count = paint.breakText(text, start, text.length, true, maxWidth, null)
            val line = text.substring(start, start + count)
            // se ultrapassar margem inferior, troca de página
            if (y + lineHeight > pageInfo.pageHeight - marginBottom) {
                newPage()
            }
            canvas.drawText(line, marginLeft, y, paint)
            y += lineHeight
            start += count
        }
    }

    // 1) Título
    canvas.drawText("Ficha de ${personagem.nome}", marginLeft, y, paint)

    // 2) Corpo do texto (cada linha de buildSummaryLines é “wrapped”)
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
    val perLutar = listaPericias
        .firstOrNull { it.nome.equals("Lutar", ignoreCase = true) }
    val lutarRaw = perLutar?.let { rawTotal(it) } ?: 0
    val base     = 2 + (lutarRaw / 2)

    val bloquearBonus =
        if (vantagensSelecionadas.any { it.nome.keyify() == "BLOQUEAR" })
            1
        else
            0

    val bloquearAprimoradoBonus =
        if (vantagensSelecionadas.any { it.nome.keyify() == "BLOQUEAR APRIMORADO" })
            1
        else
            0

    return base + bloquearBonus + bloquearAprimoradoBonus
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
    companion object { const val BASE_SP_POOL = 15 }
    var maisPontosPericias by mutableStateOf(true)
    var cartaSelvagem       by mutableStateOf(true)
    var dinheiro by mutableIntStateOf(500)
    val poderesSelecionados = mutableStateListOf<String>()
    val equipamentosComprados = mutableStateListOf<EquipamentoItem>()
    val cpRecursosStack = mutableStateListOf<Unit>()
    private val _maxedTraits = mutableStateListOf<String>()
    val maxedTraits: List<String> get() = _maxedTraits
    var idAtual by mutableStateOf<String?>(null)
    val comprasPpPorEstagio = mutableStateMapOf<String, Int>().apply {
        listaDeEstagios.forEach { this[it.nome] = 0 }
    }

    val superPoderesComprados = mutableStateListOf<PurchasedPower>()
    var superNivelCampanha by mutableStateOf<Int?>(null)
    private var superUsarProgresso by mutableStateOf(false)
    var superPontosTotais by mutableIntStateOf(0)
    var superPontosDisponiveis by mutableIntStateOf(0)
    var superLimite by mutableIntStateOf(0)

    // --- NOVO BLOCO: Controle de compra de Vantagens por XP ---
    var pvFromXpOutstanding by mutableIntStateOf(0)          // PV pendente vindo de XP
    var overrideStageForVantagem by mutableStateOf<String?>(null) // estágio de origem do PV
    var openVantagensAfterGrant by mutableStateOf(false)     // sinal pra abrir tela de vantagens


    fun comprarSuperPoder(nome: String, custo: Int) {
        // só compra se houver espaço e pontos disponíveis
        if (superPoderesComprados.size < superLimite && superPontosDisponiveis >= custo) {
            superPoderesComprados.add(PurchasedPower(nome, custo))
            superPontosDisponiveis -= custo
        }
    }

    fun removerSuperPoder(poder: PurchasedPower) {
        if (superPoderesComprados.remove(poder)) {
            superPontosDisponiveis += poder.custo
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

    fun aplicarSuperpoderes(v: Vantagem, nivel: Int, usarProgresso: Boolean) {
        if (v.nome != "Superpoderes") return

        // 1) atualiza o estado com o número de campanha
        superNivelCampanha   = nivel
        superUsarProgresso   = usarProgresso

        // 2) calcula total de pontos e limite de super
        val pontos = listOf(15, 30, 45, 60, 75)[nivel - 1]
        val limite = listOf(5, 10, 15, 20, 25)[nivel - 1]
        superPontosTotais     = pontos
        superLimite           = limite
        superPontosDisponiveis = if (usarProgresso) (pontos * 2) / 3 else pontos

        // 3) adiciona a vantagem “Superpoderes” à lista
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
    var permiteMultiAntecedenteArcano by mutableStateOf(false)
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
        // totalSpPool vem de BASE_SP_POOL + cpSpStack.size
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

    val valoresAtributos = listaAtributos.associateWith { mutableIntStateOf(4) }

    val complicacoesSelecionadas: SnapshotStateMap<Complicacao, String?> = mutableStateMapOf()
    val pontosComplicacao: Int
        get() {
            // autoKeys continua o mesmo: conjunto de IDs (chaves) de complicações automáticas
            val autoKeys = desvantagensAutomaticas
                .map { it.substringBefore("(").trim().keyify() }
                .toSet()

            var total = 0
            for ((comp, tipo) in complicacoesSelecionadas) {
                // “comp.id” é a chave normalizada da complicação
                if (comp.id.keyify() in autoKeys) continue

                total += when (tipo) {
                    "Maior" -> 2
                    "Menor" -> 1
                    else    -> 0
                }
                if (total >= 4) {
                    total = 4
                    break
                }
            }
            return total
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

        // 2) Restrição “Antecedente Arcano”: só pode ter um antecedente arcano ativo de cada vez
        if (key.startsWith("antecedente arcano")) {
            val anyArcano = vantagensSelecionadas
                .any { it.nome.keyify().startsWith("antecedente arcano") }
            if (anyArcano && vantagensSelecionadas.none { it.nome.keyify() == key }) {
                return false
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

            // 3.2 Regra extra para “ESPECIALISTA”: só vale se já houver “PROFISSIONAL” na mesma escolha
            if (key == "especialista" && choiceSeguro != null) {
                val profExist = vantagensSelecionadas.any {
                    it.nome.keyify() == "profissional" &&
                            it.choice?.keyify() == choiceSeguro.keyify()
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

        // 4) Checa requisito de estágio mínimo:
        listaDeEstagios
            .firstOrNull { it.nome.equals(v.requisitos.estagio, ignoreCase = true) }
            ?.let { estReqObj ->
                // Se o progresso atual for menor que o mínimo exigido para este estágio, bloqueia
                if (effectiveProgressoParaVantagens() < estReqObj.minProgress) return false
            }

        // 5) Checa “vantagens_previas” (se houver alguma, exige que o nome correspondente já esteja em vantagensSelecionadas)
        if (v.requisitos.vantagensPrevias.isNotEmpty()) {
            val faltam = v.requisitos.vantagensPrevias.any { prevNomeRaw ->
                val prevKey = prevNomeRaw.uppercase().semAcentos().trim()
                vantagensSelecionadas.none { sel ->
                    sel.nome.uppercase().semAcentos().trim() == prevKey
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
            val ja = vantagensSelecionadas.count { it.nome == v.nome }
            if (ja >= v.maxSelections) return false
        }

        // 8) Se requer escolha e já escolheu, não pode repetir a mesma combinação
        val choiceSeguro2 = v.choice
        if (v.requiresChoice && choiceSeguro2 != null) {
            val repetida = vantagensSelecionadas.any {
                it.nome == v.nome && it.choice == choiceSeguro2
            }
            if (repetida) return false
        }


        // 8) Verifica requisito de “nível de campanha” (minProgress)
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
                val next = if (curr == 0) 4 else curr + 2
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

    private fun atributoMinRaw(a: String): Int = racialAttrMinMap[ancestralidade]?.get(a) ?: 4
    fun atributoMaxRaw(a: String): Int {
        val baseCap = 12 + (atributoMinRaw(a) - 4)
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
        val freebies = ((startRaw - 4).coerceAtLeast(0) / 2)
        val baseCap  = 12 + freebies * 2

        val chave = per.nome.keyify()
        val profCount = vantagensSelecionadas.count {
            it.nome.keyify() == "PROFISSIONAL" && it.choice?.keyify() == chave
        }
        val espCount = vantagensSelecionadas.count {
            it.nome.keyify() == "ESPECIALISTA" && it.choice?.keyify() == chave
        }

        return baseCap + (profCount + espCount) * 2
    }

    fun rawTotal(per: Pericia): Int {
        val startRaw    = periciaStartRaw(ancestralidade, per)
        val normalIncs  = baseIncsPorPericia.getValue(per)
        val complicsIncs= compIncsPorPericia.getValue(per)
        val totalIncs   = normalIncs + complicsIncs

        val extraStep   = if (startRaw == 0 && totalIncs > 0) 2 else 0
        return startRaw + 2 * totalIncs + extraStep
    }

    fun aplicarAncestralidade(anc: String) {
        val prevAnc = ancestralidade
        if (prevAnc == "HUMANOS" && anc != "HUMANOS") {
            if (vantagensSelecionadas.isNotEmpty()) {
                vantagensSelecionadas.removeAt(vantagensSelecionadas.lastIndex)
            } else {
                pontosVantagem = (pontosVantagem - 1).coerceAtLeast(0)
            }
        } else if (prevAnc != "HUMANOS" && anc == "HUMANOS") {
            pontosVantagem += 1
        }
        val desiredRaw = listaPericias.associateWith { rawTotal(it) }
        val oldAttrMods = racialAttrMinMap[prevAnc] ?: emptyMap()
        val newAttrMods = racialAttrMinMap[anc]     ?: emptyMap()
        listaAtributos.forEach { nome ->
            val st = valoresAtributos[nome]!!
            val oldMin = oldAttrMods[nome] ?: 4
            val newMin = newAttrMods[nome] ?: 4
            val reverted = (st.intValue - oldMin).coerceAtLeast(0)
            st.intValue = (reverted + newMin).coerceIn(newMin, 12 + (newMin - 4))
        }
        ancestralidade = anc
        val prevFree = vantagensAutomaticas.toSet() +
                when (prevAnc) {
                    "SAURIOS"    -> setOf("Sentidos Aguçados", "Prontidão")
                    "PEQUENINOS" -> setOf("Sorte")
                    else         -> emptySet()
                }
        vantagensSelecionadas.removeAll { it.nome in prevFree }
        desvantagensAutomaticas.clear()
        vantagensAutomaticas.clear()
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
        pontosVantagem = if (vantagensAutomaticas.any { it.keyify() == "ADAPTAVEL" }) 1 else 0
        val oldAutoKeys = listaAncestralidadesJson
            .firstOrNull { it.nome.keyify() == prevAnc }
            ?.desvantagens
            ?.map { it.substringBefore("(").trim().keyify() }
            ?.toSet()
            ?: emptySet()
        complicacoesSelecionadas.keys
            .filter { it.id.keyify() in oldAutoKeys }
            .forEach { complicacoesSelecionadas.remove(it) }

        val autoBaseKeys = desvantagensAutomaticas
            .map { it.substringBefore("(").trim().keyify() }
            .toSet()

        listaComplicacoes
            .filter { it.id.keyify() in autoBaseKeys }
            .forEach { comp ->
                // “hasMenor” detecta se, no texto das desvantagens automáticas, há a palavra “Menor”
                val hasMenor = desvantagensAutomaticas.any {
                    it.substringBefore("(").trim().keyify() == comp.id.keyify()
                            && it.contains("Menor", ignoreCase = true)
                }

                val grau = when (comp.severity.lowercase()) {
                    // se severity == “both”, então permitimos “Menor” ou “Maior”
                    "both" -> if (hasMenor) "Menor" else "Maior"
                    // se severity == “menor”, só faz sentido atribuir “Menor”
                    "menor" -> "Menor"
                    // se severity == “maior”, só faz sentido atribuir “Maior”
                    "maior" -> "Maior"
                    else    -> "Menor"
                }

                complicacoesSelecionadas[comp] = grau
            }

        rebuildPericias(desiredRaw)
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

    fun recalcularPontosAtributo() {
        val mods = racialAttrMinMap[ancestralidade] ?: emptyMap()
        var usados = 0
        for (nome in listaAtributos) {
            val atual = valoresAtributos[nome]!!.intValue
            val base = mods[nome] ?: 4
            usados += ((atual - base).coerceAtLeast(0)) / 2
        }
        pontosAtributo = (5 + cpPaStack.size - jovemMalusPa) - usados

        trimAttributeStacks()
        rebuildAllPericiaStacks()
    }

    private fun trimAttributeStacks() {
        while (pontosAtributo < 0) {
            // invés de .removeLast() use:
            val entry = paCostStackPorAtributo
                .entries
                .firstOrNull { it.value.isNotEmpty() }
                ?: break
            val stack = entry.value
            stack.removeAt(stack.size - 1)
            valoresAtributos[entry.key]!!.intValue -= 2
            val mods = racialAttrMinMap[ancestralidade] ?: emptyMap()
            var usados = 0
            for (nome in listaAtributos) {
                val atual = valoresAtributos[nome]!!.intValue
                val base = mods[nome] ?: 4
                usados += ((atual - base).coerceAtLeast(0)) / 2
            }
            pontosAtributo = (5 + cpPaStack.size - jovemMalusPa) - usados
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

    fun canChangeAncestralidade(): Boolean = true

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
                tint = Color.Black
            )
            Text(
                title,
                fontSize = 27.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(Modifier.weight(1f))
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = Color.Black
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
                    containerColor = Color(0xFFF2E3C6) // pergaminho
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
                showDialog          = false
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
fun AncestralidadesContent(state: CriadorState) {
    val locked = state.progresso > 0
    var expAnc by rememberSaveable { mutableStateOf(false) }
    var showMeioElfoDialog by rememberSaveable { mutableStateOf(false) }
    var pendingMeioElfoKey by rememberSaveable { mutableStateOf<String?>(null) }

    ExposedDropdownMenuBox(
        expanded = expAnc,
        onExpandedChange = { if (!locked) expAnc = !expAnc }
    ) {
        OutlinedTextField(
            value = state.ancestralidade,
            onValueChange = {},
            readOnly = true,
            label = { Text("Ancestralidade") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expAnc) },
            enabled = !locked,

            // --- CORREÇÃO APLICADA AQUI ---
            colors = TextFieldDefaults.colors(
                // Cores quando Ativado
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedIndicatorColor = Color.Black,      // Corrigido
                unfocusedIndicatorColor = Color.Black,    // Corrigido
                focusedTrailingIconColor = Color.Black,
                unfocusedTrailingIconColor = Color.Black,
                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.Black,

                // Cores quando Desativado (para ficar cinza)
                disabledTextColor = Color.Gray,
                disabledIndicatorColor = Color.Gray,    // Corrigido
                disabledTrailingIconColor = Color.Gray,
                disabledLabelColor = Color.Gray
            ),
            // --- FIM DA CORREÇÃO ---

            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (locked) 0.3f else 1f)
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .combinedClickable(
                    enabled = !locked,
                    onClick    = { expAnc = true },
                    onLongClick= {
                        if (state.canChangeAncestralidade())
                            state.aplicarAncestralidade("HUMANOS")
                    }
                )
        )
        ExposedDropdownMenu(
            expanded = expAnc,
            onDismissRequest = { expAnc = false },
            modifier = Modifier
                .heightIn(max = 300.dp)
                .verticalScroll(rememberScrollState())
        ) {
            listaAncestralidadesJson.forEach { anc ->
                DropdownMenuItem(
                    text = { Text(anc.nome) },
                    onClick = {
                        expAnc = false
                        val key = anc.nome.keyify()
                        if (key == state.ancestralidade) return@DropdownMenuItem

                        if (key == "MEIO-ELFOS") {
                            pendingMeioElfoKey = key
                            showMeioElfoDialog = true
                        } else {
                            pendingMeioElfoKey = null
                            state.aplicarAncestralidade(key)
                        }
                    },
                    enabled = !locked
                )
            }
        }
    }

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
        state,
        onOpenVantagensDetail = {},
        onOpenPericiasDetail = {},
        onOpenComplicacoesDetail = {},
        onOpenAtributosDetail = {},
        onOpenListaAncestralidadesDetail = {},
        onOpenListaCompletaEquipamento = {},
        onOpenPoderesDetail = {},   // único callback para “Lista Completa” de Poderes/Supers
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
    onOpenPoderesDetail: () -> Unit,   // unificado (Poderes/Supers)
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
                state.aplicarAncestralidade(nome.uppercase().semAcentos())
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
                onOpenVantagensDetail = onOpenVantagensDetail,
                onTogglePoderes = onTogglePoderes
            )
        }

        // ─── Poderes (só mostra se já escolheu Arcano) ───────────────────────────
        if (state.vantagensSelecionadas.any { it.nome.keyify().startsWith("ANTECEDENTE ARCANO") }) {
            HorizontalDivider(thickness = 1.dp)

            SectionCard(
                title    = "Poderes",
                expanded = expPoderes,
                onToggle = onTogglePoderes,
                icon     = Icons.Default.FlashOn
            ) {
                PoderesSection(state, onOpenPoderesDetail)
            }
        }

        Spacer(Modifier.height(8.dp))
        HorizontalDivider(thickness = 1.dp)

        // ─── SuperPoderes (modo Supers) ─────────────────────────────────────────
        if (state.modoSupers) {
            SuperPoderesContent(
                state             = state,
                listaSuperPoderes = listaSuperPoderes,
                expanded          = expSupers,
                onToggle          = { expSupers = !expSupers },
                onOpenPoderesDetail  = onOpenPoderesDetail,
                onHelpClick       = onHelpSuperClick
                        )
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(thickness = 1.dp)
        }

        EquipamentoSection(
            dinheiro                 = state.dinheiro,
            onHelpClick              = { /* ... */ },
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
            categorias               = equipamentoCategorias,
            superequipCategorias     = if (state.modoSuperequip) superequipCategorias else emptyList()
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
                    when {
                        currentRaw == 0 && per.basica -> "Atual: d4 (básica)"
                        currentRaw == 0              -> "Atual: —"
                        else                         -> "Atual: d${currentRaw}"
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
fun SummaryContent(state: CriadorState) {
    // Para recalcular traits ao chegar em Lendário
    LaunchedEffect(state.estagioAtual().nome, state.stageXpSpent.values.sum()) {
        if (state.estagioAtual().nome == "Lendário") {
            state.identifyMaxedTraits()
        }
    }

    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 600.dp)
            .verticalScroll(scroll)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // — Ancestralidade sempre aparece —
        Text("Ancestralidade: ${state.ancestralidade}")

        // — Vantagens Raciais —
        val raciais = state.vantagensAutomaticas.distinct()
        Text("Vantagens raciais:")
        if (raciais.isEmpty()) {
            Text("  – Nenhuma", Modifier.padding(start = 8.dp))
        } else {
            raciais.forEach { v ->
                Text("  • ${v.uppercase()}", Modifier.padding(start = 8.dp))
            }
        }

        // — Vantagens —
        val raciaisKeys = raciais.map { it.keyify() }
        val outrasVants = state.vantagensSelecionadas.filter { it.nome.keyify() !in raciaisKeys }
        Text("Vantagens:")
        if (outrasVants.isEmpty()) {
            Text("  – Nenhuma", Modifier.padding(start = 8.dp))
        } else {
            outrasVants.forEach { v ->
                val label = v.choice?.let { "${v.nome} ($it)" } ?: v.nome
                Text("  • ${label.uppercase()}", Modifier.padding(start = 8.dp))
            }
        }

        // — Desvantagens Automáticas —
        Text("Desvantagens automáticas:")
        if (state.desvantagensAutomaticas.isEmpty()) {
            Text("  – Nenhuma", Modifier.padding(start = 8.dp))
        } else {
            state.desvantagensAutomaticas.forEach { d ->
                Text("  • $d", Modifier.padding(start = 8.dp))
            }
        }

        // — Complicações —
        Text("Complicações:")
        val comps = state.complicacoesSelecionadas.filterValues { it != null }
        if (comps.isEmpty()) {
            Text("  – Nenhuma", Modifier.padding(start = 8.dp))
        } else {
            comps.forEach { (c, grau) ->
                Text("  • ${c.name} ($grau)", Modifier.padding(start = 8.dp))
            }
        }

        // — Atributos —
        Text("Atributos:")
        listaAtributos.forEach { nome ->
            val raw = state.valoresAtributos[nome]!!.intValue
            val display = mapaAtributosDisplay[nome] ?: nome
            Text("  • $display: ${raw.toDiceString()}", Modifier.padding(start = 8.dp))
        }

        // — Perícias —
        Text("Perícias:")
        val toShow = listaPericias.filter { per ->
            val humanStart = periciaStartRaw("HUMANOS", per)
            per.basica || state.rawTotal(per) != humanStart
        }
        if (toShow.isEmpty()) {
            Text("  – Nenhuma", Modifier.padding(start = 8.dp))
        } else {
            toShow.forEach { per ->
                val raw = state.rawTotal(per)
                Text("  • ${per.nome}: ${raw.toDiceString()}", Modifier.padding(start = 8.dp))
            }
        }

        // — Aqui vem OU os Antecedentes Arcanos OU os Superpoderes —
        if (state.modoSupers) {
            Text("Superpoderes Comprados:", fontWeight = FontWeight.Bold)
            val comprados = state.superPoderesComprados
            if (comprados.isEmpty()) {
                Text("  – Nenhum", Modifier.padding(start = 8.dp))
            } else {
                comprados.forEach { pp: PurchasedPower ->
                    // pp.nome é a string "Balançar", pp.custo é o inteiro 3
                    Text("  • ${pp.nome} ${pp.custo}", Modifier.padding(start = 8.dp))
                }
            }
            Text(
                "Custo total gasto: ${state.superPontosTotais - state.superPontosDisponiveis}",
                Modifier.padding(start = 8.dp)
            )
        } else {
            Spacer(Modifier.height(4.dp))
            if (state.poderSlotsPorArcano.isNotEmpty()) {
                Text("Antecedentes Arcanos:", fontWeight = FontWeight.Bold)
                state.poderSlotsPorArcano.forEach { (arcKey, slots) ->
                    val pp = arcanoInfo[arcKey]?.second ?: 0
                    Text("  • ${arcKey.uppercase()} – PP: $pp", Modifier.padding(start = 8.dp))
                    slots.filterNotNull().forEach { poder ->
                        Text("    • $poder", Modifier.padding(start = 16.dp))
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // — Equipamentos Comprados —
        Text("Equipamentos Comprados:", fontWeight = FontWeight.Bold)
        if (state.equipamentosComprados.isEmpty()) {
            Text("  – Nenhum", Modifier.padding(start = 8.dp))
        } else {
            state.equipamentosComprados.forEach { eq ->
                Text("  • ${eq.nome}", Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Composable
fun CircleStat(value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(4.dp)
            .size(70.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .border(2.dp, Color.Red, CircleShape)
        ) {
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium)
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
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
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
    val itensPorTipo = categorias
        .groupBy { it.tipo }
        .mapValues { (_, cats) -> cats.flatMap { it.itens } }

    val expandedState = remember {
        itensPorTipo.keys.associateWith { mutableStateOf(false) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        stickyHeader {
            Surface(color = Color.Transparent) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onBack)
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
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

        itensPorTipo.forEach { (tipo, itens) ->
            item {
                val exp = expandedState.getValue(tipo)
                Column {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { exp.value = !exp.value }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(tipo, style = MaterialTheme.typography.titleMedium)
                        Icon(
                            imageVector = if (exp.value) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (exp.value) "Fechar" else "Abrir"
                        )
                    }

                    if (exp.value) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 100.dp, max = 300.dp)
                        ) {
                            LazyColumn {
                                items(itens) { equipamento ->
                                    Column(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Text(
                                            equipamento.nome,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 16.sp
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            buildString {
                                                append("Custo: ${equipamento.custo}")
                                                append(", Peso: ${equipamento.peso}")
                                                equipamento.dano?.let { append(", Dano: $it") }
                                                equipamento.forcaMin?.let { append(", Força mínima: $it") }
                                                equipamento.armadura?.let { append(", Armadura: $it") }
                                                equipamento.aparar?.let { append(", Aparar: $it") }
                                                val obs = (equipamento.observacoes as? JsonPrimitive)?.content
                                                if (!obs.isNullOrBlank()) append(", Observações: $obs")
                                            }
                                        )
                                    }
                                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
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
        modoSuperComplicacoes: Boolean
    ) -> Unit,
    onLoad: (PersonagemSalvo) -> Unit,
    context: Context,
    viewModel: CriadorViewModel
) {
    var showLoadDialog by rememberSaveable { mutableStateOf(false) }
    // carrega já de cara todos os pares (displayName, fileKey)
    var nomesSalvos by remember { mutableStateOf(StorageUtils.listarPersonagens(context)) }
    var pendingDelete by rememberSaveable { mutableStateOf<Pair<String, String>?>(null) }

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
    var optSuperVantagens by rememberSaveable { mutableStateOf(false) }

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
                nomesSalvos = StorageUtils.listarPersonagens(context)
                if (nomesSalvos.isEmpty()) {
                    Toast.makeText(context, "Nenhum personagem salvo.", Toast.LENGTH_SHORT).show()
                } else {
                    showLoadDialog = true
                }
            },
            enabled = nomesSalvos.isNotEmpty(),
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
                            Text("Carta Selvagem")
                        }
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
                            Text("Mais pontos de perícias")
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { optMultiAntecedenteArcano = !optMultiAntecedenteArcano }
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = optMultiAntecedenteArcano,
                                onCheckedChange = { optMultiAntecedenteArcano = it }
                            )
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
                            Checkbox(
                                checked = optEspecializacaoPer,
                                onCheckedChange = { optEspecializacaoPer = it }
                            )
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
                            Checkbox(
                                checked = optHeroiSemArmadura,
                                onCheckedChange = { optHeroiSemArmadura = it }
                            )
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
                            Checkbox(
                                checked = optMultiplosIdiomas,
                                onCheckedChange = { optMultiplosIdiomas = it }
                            )
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
                            Checkbox(
                                checked = optNasceUmHeroi,
                                onCheckedChange = { optNasceUmHeroi = it }
                            )
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
                            Checkbox(
                                checked = optSemPontosPoder,
                                onCheckedChange = { optSemPontosPoder = it }
                            )
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
                            Text("Superpoderes")
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { optSuperequipamentos = !optSuperequipamentos }
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = optSuperequipamentos,
                                onCheckedChange = { optSuperequipamentos = it }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Superequipamentos")
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { optSuperComplicacoes = !optSuperComplicacoes }
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = optSuperComplicacoes,
                                onCheckedChange = { optSuperComplicacoes = it }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Supercomplicações")
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { optSuperVantagens = !optSuperVantagens }
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = optSuperVantagens,
                                onCheckedChange = { optSuperVantagens = it }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Supervantagens")
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
                        optCartaSelvagem,         // cartaSelvagem
                        optMaisPontosPericias,    // maisPontosPericias
                        optSuperPoderes,          // modoSupers
                        optSuperequipamentos,     // modoSuperequipamentos  ← é este que aciona a aba!
                        optSuperComplicacoes      // modoSuperComplicacoes
                    )
                    viewModel.state.permiteMultiAntecedenteArcano = optMultiAntecedenteArcano
                    if (optSuperPoderes) {
                        viewModel.state.aplicarSuperpoderes(
                            v = viewModel.state.vantagensSelecionadas
                                .first { it.nome.equals("SUPERPODERES", true) },
                            nivel = 1,
                            usarProgresso = false
                        )
                    }
                    showNewOptionsDialog = false
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

    // ── Diálogo de carregamento existente ────────────────────────────────────────
    if (showLoadDialog) {
        AlertDialog(
            onDismissRequest = { showLoadDialog = false },
            title            = { Text("Selecione um personagem") },
            text             = {
                LazyColumn {
                    items(nomesSalvos) { (displayName, fileKey) ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    StorageUtils
                                        .carregarPersonagem(context, fileKey)
                                        ?.let { salvo ->
                                            onLoad(salvo)
                                            showLoadDialog = false
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
                TextButton(onClick = { showLoadDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // ── Diálogo de confirmação de exclusão ──────────────────────────────────────
    if (pendingDelete != null) {
        val (displayName, fileKey) = pendingDelete!!
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title            = { Text("Confirmar exclusão") },
            text             = { Text("Deseja realmente excluir \"$displayName\"?") },
            confirmButton    = {
                TextButton(onClick = {
                    StorageUtils.deletarPersonagem(context, fileKey)
                    nomesSalvos = StorageUtils.listarPersonagens(context)
                    pendingDelete = null
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
@Composable
fun BuySuperPowerDialog(
    poder: SuperPoder,
    pontosDisponiveis: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    // limites para o custo base
    val baseMin = poder.custoBase?.toIntOrNull() ?: 1
    val rawBaseMax = poder.custoBase?.toIntOrNull() ?: pontosDisponiveis
    val baseMax = rawBaseMax
        .coerceAtMost(pontosDisponiveis)
        .coerceAtLeast(baseMin)
    var baseCost by rememberSaveable { mutableIntStateOf(baseMin) }

    val modStates = remember(poder.modificadores) {
        poder.modificadores.orEmpty().map { modObj ->

            val name = modObj.substringBefore(":").trim()

            val paren = Regex("\\(([^)]*)\\)").find(name)?.groupValues?.get(1).orEmpty()
            val opts = paren
                .split("/")
                .mapNotNull { it.trim().removePrefix("+").toIntOrNull() }
                .takeIf { it.isNotEmpty() }
                ?: listOf(0)

            ModState(
                name     = name,
                options  = opts,
                included = mutableStateOf(false),
                selected = mutableIntStateOf(opts.first())
            )
        }
    }

    val totalCost = baseCost +
            modStates.filter { it.included.value }
                .sumOf { it.selected.value }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Comprar “${poder.nome}”") },
        text = {
            // Scroll interno
            val scroll = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 280.dp)
                    .verticalScroll(scroll)
                    .padding(8.dp)
            ) {
                // Slider de custo base
                Text("Custo base: $baseCost")
                Slider(
                    value = baseCost.toFloat(),
                    onValueChange = { novo ->
                        baseCost = novo.roundToInt().coerceIn(baseMin, baseMax)
                    },
                    valueRange = baseMin.toFloat()..baseMax.toFloat(),
                    steps = (baseMax - baseMin).coerceAtLeast(1) - 1,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                if (modStates.isNotEmpty()) {
                    Text("Modificadores:", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(4.dp))

                    modStates.forEach { mod ->
                        if (mod.options.size == 1) {
                            // único valor: checkbox + nome + custo fixo
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { mod.included.value = !mod.included.value }
                                    .padding(vertical = 4.dp)
                            ) {
                                Checkbox(
                                    checked = mod.included.value,
                                    onCheckedChange = { mod.included.value = it }
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(mod.name)
                                Spacer(Modifier.weight(1f))
                                if (mod.included.value) {
                                    Text("+${mod.selected.value}")
                                }
                            }
                        } else {
                            // múltiplas opções: checkbox + nome
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { mod.included.value = !mod.included.value }
                                    .padding(vertical = 4.dp)
                            ) {
                                Checkbox(
                                    checked = mod.included.value,
                                    onCheckedChange = { mod.included.value = it }
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(mod.name)
                            }
                            // se estiver incluído, exibe as radio options
                            if (mod.included.value) {
                                Spacer(Modifier.height(4.dp))
                                mod.options.forEach { opt ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 28.dp, top = 2.dp, bottom = 2.dp)
                                            .clickable { mod.selected.value = opt }
                                    ) {
                                        RadioButton(
                                            selected = (mod.selected.value == opt),
                                            onClick  = { mod.selected.value = opt }
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text("+$opt")
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    "Custo total: $totalCost",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = pontosDisponiveis >= totalCost,
                onClick = { onConfirm(totalCost) }
            ) { Text("Confirmar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

private data class ModState(
    val name: String,
    val options: List<Int>,
    val included: MutableState<Boolean>,
    val selected: MutableState<Int>
)

@Composable
fun SuperPoderesSection(
    state: CriadorState,
    listaSuperPoderes: List<SuperPoder>,
    expanded: Boolean
) {
    if (!expanded) return

    var poderParaComprar by remember { mutableStateOf<SuperPoder?>(null) }
    val nivelAtual    = state.superNivelCampanha ?: 1
    val sliderEnabled = state.creationComplete()

    Column(Modifier.fillMaxWidth().padding(8.dp)) {
        // 1) chips dos poderes já comprados
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement   = Arrangement.spacedBy(4.dp)
        ) {
            state.superPoderesComprados.forEach { p ->
                AssistChip(
                    onClick = { state.removerSuperPoder(p) },
                    label = { Text("${p.nome} (+${p.custo})") },
                    leadingIcon = { Icon(Icons.Default.Close, contentDescription = "Remover") }
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // 2) slider de nível e info de pontos/limite
        Text("Nível de Superpoderes: $nivelAtual")
        Slider(
            value = nivelAtual.toFloat(),
            onValueChange = { novo ->
                state.aplicarSuperpoderes(
                    v = state.vantagensSelecionadas
                        .first { it.nome.equals("Superpoderes", ignoreCase = true) },
                    nivel = novo.roundToInt().coerceIn(1,5),
                    usarProgresso = false
                )
            },
            valueRange = 1f..5f,
            steps = 3,
            enabled = sliderEnabled,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(4.dp))
        Text("Pontos disponíveis: ${state.superPontosDisponiveis}")
        Text("Limite de superpoderes: ${state.superLimite}")

        Spacer(Modifier.height(8.dp))

        // 3) lista de poderes que ainda posso comprar
        listaSuperPoderes.forEach { poder ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { poderParaComprar = poder }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(poder.nome, Modifier.weight(1f))
                Icon(Icons.Default.FlashOn, contentDescription = "Comprar")
            }
        }
    }

    // diálogo de compra
    poderParaComprar?.let { poder ->
        BuySuperPowerDialog(
            poder = poder,
            pontosDisponiveis = state.superPontosDisponiveis,
            onConfirm = { custo ->
                state.comprarSuperPoder(poder.nome, custo)
                poderParaComprar = null
            },
            onDismiss = { poderParaComprar = null }
        )
    }
}

@Composable
fun SuperPoderesContent(
    state: CriadorState,
    listaSuperPoderes: List<SuperPoder>,
    expanded: Boolean,
    onToggle: () -> Unit,
    onOpenPoderesDetail: () -> Unit,   // <- usa o mesmo callback da lista completa de poderes
    onHelpClick: () -> Unit
) {
    SectionCard(
        title    = "Superpoderes",
        expanded = expanded,
        onToggle = onToggle,
        icon     = Icons.Default.FlashOn
    ) {
        val showLista = booleanResource(R.bool.show_lista_completa)

        SectionHeader(
            onHelpClick          = onHelpClick,
            centerText           = "Pontos de Super: ${state.superPontosDisponiveis}",
            onCenterClick        = onToggle,
            onListaCompletaClick = if (showLista) onOpenPoderesDetail else null,
            listaCompletaText    = "Lista Completa"
        )

        SuperPoderesSection(
            state             = state,
            listaSuperPoderes = listaSuperPoderes,
            expanded          = expanded
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun PoderesDetailScreen(
    state: CriadorState,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // Se tiver Antecedente Arcano selecionado, SEMPRE mostra magias (poderes.json)
    // mesmo que o modo supers esteja ativo.
    val hasAntecedenteArcano = remember(state.vantagensSelecionadas) {
        state.vantagensSelecionadas.any { it.id == "antecedente_arcano" }
    }
    val showSupers = remember(state.modoSupers, hasAntecedenteArcano) {
        state.modoSupers && !hasAntecedenteArcano
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Transparent
    ) {
        Column {
            TopAppBar(
                title = {
                    Text(
                        if (showSupers) "Lista Completa de Superpoderes"
                        else "Lista Completa de Poderes"
                    )
                },
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF2E3C6)
                )
            )

            Spacer(Modifier.height(8.dp))

            if (!showSupers) {
                // ---------- MAGIAS (Antecedente Arcano) ----------
                val allPoderes: List<Poder> = remember {
                    context.loadPoderesAsset<List<Poder>>("poderes.json")
                }

                LazyColumn(Modifier.fillMaxSize()) {
                    items(allPoderes, key = { it.id }) { poder ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "• ${poder.nome}",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 18.sp,
                                color = Color(0xFF050402)
                            )

                            Spacer(Modifier.height(4.dp))

                            Text(
                                text = "Estágio: ${poder.estagio}",
                                fontSize = 14.sp,
                                color = Color(0xFF050402)
                            )

                            Spacer(Modifier.height(2.dp))

                            Text(
                                text = "Pontos de Poder: ${poder.pontosDePoder}",
                                fontSize = 14.sp,
                                color = Color(0xFF050402)
                            )

                            Spacer(Modifier.height(4.dp))

                            if (poder.manifestacoes.isNotEmpty()) {
                                Text(
                                    text = "Manifestações:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF050402)
                                )
                                poder.manifestacoes.forEach { man ->
                                    Text(
                                        text = "- $man",
                                        fontSize = 14.sp,
                                        color = Color(0xFF050402)
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                            }

                            if (poder.descricao.isNotBlank()) {
                                Text(
                                    text = "Descrição:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF050402)
                                )
                                Text(
                                    text = poder.descricao,
                                    fontSize = 14.sp,
                                    color = Color(0xFF050402)
                                )
                                Spacer(Modifier.height(4.dp))
                            }

                            if (poder.modificadores.isNotEmpty()) {
                                Text(
                                    text = "Modificadores:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF050402)
                                )
                                poder.modificadores.forEach { mod ->
                                    Text(
                                        text = "- ${mod.nome} (Custo: ${mod.custo})",
                                        fontSize = 14.sp,
                                        color = Color(0xFF050402)
                                    )
                                    if (mod.descricao.isNotBlank()) {
                                        Text(
                                            text = "  ${mod.descricao}",
                                            fontSize = 14.sp,
                                            color = Color(0xFF050402)
                                        )
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                            }

                            HorizontalDivider(
                                thickness = 1.dp,
                                color = Color(0xFF050402).copy(alpha = 0.2f)
                            )
                        }
                    }
                }
            } else {
                // ---------- SUPERPODERES ----------
                val superPoderes: List<SuperPoder> = remember {
                    context.loadJsonAsset("superpoderes.json")
                }

                LazyColumn(Modifier.fillMaxSize()) {
                    items(superPoderes, key = { it.nome }) { poder ->
                        var expanded by rememberSaveable(poder.nome) { mutableStateOf(false) }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expanded = !expanded },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = poder.nome,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Spacer(Modifier.weight(1f))
                                Icon(
                                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                    contentDescription = null
                                )
                            }

                            AnimatedVisibility(visible = expanded) {
                                Column(modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)) {
                                    poder.custoBase?.let { custo ->
                                        Text(
                                            text = "Custo Base: $custo",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp
                                        )
                                        Spacer(Modifier.height(4.dp))
                                    }

                                    val listaManifestacoes: List<String> =
                                        poder.manifestacoes.toStringList()
                                    if (listaManifestacoes.isNotEmpty()) {
                                        Text(
                                            text = "Manifestações:",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp
                                        )
                                        listaManifestacoes.forEach { man ->
                                            Text(
                                                text = "- $man",
                                                fontSize = 14.sp,
                                                modifier = Modifier.padding(start = 8.dp)
                                            )
                                        }
                                        Spacer(Modifier.height(4.dp))
                                    }

                                    poder.descricao?.let { desc ->
                                        Text(
                                            text = "Descrição:",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = desc,
                                            fontSize = 14.sp,
                                            modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                                        )
                                        Spacer(Modifier.height(4.dp))
                                    }

                                    if (!poder.modificadores.isNullOrEmpty()) {
                                        Text(
                                            text = "Modificadores:",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp
                                        )
                                        poder.modificadores.forEach { mod ->
                                            Text(
                                                text = "- $mod",
                                                fontSize = 14.sp,
                                                modifier = Modifier.padding(start = 8.dp)
                                            )
                                        }
                                        Spacer(Modifier.height(4.dp))
                                    }
                                }
                            }

                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

