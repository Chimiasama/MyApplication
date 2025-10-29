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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.SuperPoder
import com.example.swadebuilder.model.Poder
import com.example.swadebuilder.model.loadJsonAsset
import com.google.gson.JsonArray

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
    onBack: () -> Unit
) {
    val context = LocalContext.current
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
                            color = Color(0xFF050402)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF2E3C6)
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
                // ---------- MODO SUPERS: SUPERPODERES ----------
                val superPoderes: List<SuperPoder> = remember {
                    context.loadJsonAsset("superpoderes.json")
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
                                        is JsonArray -> m.map { it.asString }
                                        else -> poder.manifestacoes?.toString()?.let { listOf(it) } ?: emptyList()
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
