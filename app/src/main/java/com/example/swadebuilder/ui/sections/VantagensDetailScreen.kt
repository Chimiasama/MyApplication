package com.example.swadebuilder.ui.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swadebuilder.AppData
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.util.semAcentos
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VantagensDetailScreen(
    state: CriadorState,
    modoSupers: Boolean,
    highlightedName: String,
    onBack: () -> Unit,
    vantagensJson: String
) {
    val todasVantagens: List<Vantagem> = remember(vantagensJson) {
        val parser = Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }
        parser.decodeFromString(
            ListSerializer(Vantagem.serializer()),
            vantagensJson
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
