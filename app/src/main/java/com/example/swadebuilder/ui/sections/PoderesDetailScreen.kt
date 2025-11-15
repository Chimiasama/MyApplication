package com.example.swadebuilder.ui.sections

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.SuperPoder
import com.example.swadebuilder.model.CriadorViewModel
import com.example.swadebuilder.model.Poder
import com.example.swadebuilder.model.loadJsonAsset
import com.example.swadebuilder.ui.dialogs.SupersDialog

private fun custoParaPenalidadeTexto(custo: String): String {
    val clean = custo.trim()

    clean.toIntOrNull()?.let { base ->
        val pen = (base + 1) / 2 // ceil(base/2)
        return "-$pen"
    }

    if (clean.contains("/")) {
        val parts = clean.split("/")
        val mapped = parts.map { p ->
            val n = p.replace("+", "").trim().toIntOrNull()
            n?.let { "-${(it + 1) / 2}" } ?: "—"
        }
        return mapped.joinToString("/")
    }

    if (clean.endsWith("+")) {
        val n = clean.removeSuffix("+").toIntOrNull()
        return n?.let { "-${(it + 1) / 2}+" } ?: "—"
    }

    if (clean.startsWith("+")) {
        val n = clean.removePrefix("+").toIntOrNull()
        return n?.let { "-${(it + 1) / 2}" } ?: "—"
    }

    return "—"
}

/**
 * UM ÚNICO DETAIL:
 * - Se state.modoSupers == false  -> mostra lista de MAGIAS (poderes.json, classe Poder)
 * - Se state.modoSupers == true   -> mostra lista de SUPERPODERES (superpoderes.json, classe SuperPoder)
 */
@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun PoderesDetailScreen(
    state: CriadorState,
    onBack: () -> Unit,
    viewModel: CriadorViewModel? = null // <- opcional: usado para abrir o SupersDialog
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val isSupers = state.modoSupers

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Transparent
    ) {
        Column {
            TopAppBar(
                title = {
                    Text(
                        if (isSupers) "Lista Completa de Superpoderes"
                        else "Lista Completa de Poderes"
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text(
                            "Voltar",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )

            Spacer(Modifier.height(8.dp))

            if (!isSupers) {
                // ---------- MODO NORMAL: PODERES (MAGIAS) ----------
                val allPoderes: List<Poder> = remember {
                    context.loadJsonAsset("poderes.json")
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
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(Modifier.height(4.dp))

                            Text(
                                text = "Estágio: ${poder.estagio}",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(Modifier.height(2.dp))

                            if (state.usarSemPontosDePoder) {
                                // pontosDePoder já é String no seu modelo — remove toString() redundante
                                val custoStr: String = poder.pontosDePoder
                                Text(
                                    text = "Penalidade base: ${custoParaPenalidadeTexto(custoStr)}",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            } else {
                                Text(
                                    text = "Pontos de Poder: ${poder.pontosDePoder}",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Spacer(Modifier.height(4.dp))

                            if (poder.manifestacoes.isNotEmpty()) {
                                Text(
                                    text = "Manifestações:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                poder.manifestacoes.forEach { man ->
                                    Text(
                                        text = "- $man",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                            }

                            if (poder.descricao.isNotBlank()) {
                                Text(
                                    text = "Descrição:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = poder.descricao,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.height(4.dp))
                            }

                            if (poder.modificadores.isNotEmpty()) {
                                Text(
                                    text = "Modificadores:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                poder.modificadores.forEach { mod ->
                                    Text(
                                        text = "- ${mod.nome} (Custo: ${mod.custo})",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (state.usarSemPontosDePoder) {
                                        Text(
                                            text = "  Penalidade: ${custoParaPenalidadeTexto(mod.custo)}",
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    if (mod.descricao.isNotBlank()) {
                                        Text(
                                            text = "  ${mod.descricao}",
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                            }

                            HorizontalDivider(
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                            )
                        }
                    }
                }
            } else {
                // ---------- MODO SUPERS: SUPERPODERES ----------
                val superPoderes: List<SuperPoder> = remember {
                    context.loadJsonAsset("superpoderes.json")
                }

                // Cabeçalho de controle/atalho para diálogo
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        "Pontos: ${state.superPontosDisponiveis}/${state.superPontosTotais} • Limite padrão: ${state.limitePorPoderPadrao} • Favorecido: ${state.limiteFavorecido}",
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.weight(1f))
                    var showSupers by rememberSaveable { mutableStateOf(false) }

                    if (showSupers && viewModel != null) {
                        SupersDialog(
                            state = state,
                            viewModel = viewModel,
                            onConfirmLock = { /* travar fase de supers: mover para seu fluxo */ },
                            onDismiss = { showSupers = false }
                        )
                    }
                }

                LazyColumn(Modifier.fillMaxSize()) {
                    items(superPoderes, key = { it.nome }) { poder ->
                        var expanded by rememberSaveable(poder.nome) { mutableStateOf(false) }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
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
                                Column(Modifier.padding(top = 4.dp, bottom = 8.dp)) {
                                    poder.custoBase?.let { custo ->
                                        Text(
                                            text = "Custo Base: $custo",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp
                                        )
                                        Spacer(Modifier.height(4.dp))
                                    }

                                    val mans = when (val m = poder.manifestacoes) {
                                        is List<*> -> m.filterIsInstance<String>()
                                        is String -> listOf(m)
                                        null -> emptyList()
                                        else -> emptyList()
                                    }
                                    if (mans.isNotEmpty()) {
                                        Text(
                                            text = "Manifestações:",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp
                                        )
                                        mans.forEach { man ->
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