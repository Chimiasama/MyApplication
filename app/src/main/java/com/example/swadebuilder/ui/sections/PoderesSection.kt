package com.example.swadebuilder.ui.sections

import android.content.res.Resources
import android.util.Log
import androidx.annotation.BoolRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.R
import com.example.swadebuilder.SectionHeader
import com.example.swadebuilder.arcanoInfo              // mapa global montado no MainActivity
import com.example.swadebuilder.model.Poder
import com.example.swadebuilder.model.Vantagem         // tem subtipoArcano/choice
import com.example.swadebuilder.model.loadJsonAsset    // loader com JSON leniente
import com.example.swadebuilder.util.semAcentos
import kotlinx.serialization.Serializable

private const val TAG = "PoderesSection"

@Composable
private fun boolResOrDefault(@BoolRes id: Int, default: Boolean): Boolean {
    val context = LocalContext.current
    return remember(id, default) {
        try { context.resources.getBoolean(id) }
        catch (_: Resources.NotFoundException) {
            Log.w(TAG, "Bool resource ausente (id=$id) — usando default=$default")
            default
        }
    }
}

@Serializable
data class Modificador(val nome: String, val custo: String, val descricao: String)

// ---------- Normalizações ----------
private fun String.normAAKey(): String =
    this.uppercase().semAcentos().trim()   // compatível com chaves de arcanoInfo

private fun Vantagem.toArcanoKeyFromModel(): String? {
    // prioridade 1: subtipoArcano vindo direto do JSON (Dom/Magia/Milagres/Psiônicos/Ciência Estranha)
    if (!subtipoArcano.isNullOrBlank()) return subtipoArcano.normAAKey()
    // prioridade 2: escolha feita na vantagem base (se estiver usando o agrupador com choice)
    if (!choice.isNullOrBlank()) return choice!!.normAAKey()
    // fallback: tenta deduzir do nome
    val n = nome.normAAKey()
    return when {
        "(DOM" in n -> "DOM"
        "(MAGIA" in n -> "MAGIA"
        "(MILAGRES" in n -> "MILAGRES"
        "(PSIONICOS" in n || "(PSIÔNICOS" in nome -> "PSIONICOS"
        "(CIENCIA ESTRANHA" in n || "(CIÊNCIA ESTRANHA" in nome -> "CIENCIA ESTRANHA"
        else -> null
    }
}

// Custo -> penalidade (quando sem PP)
private fun custoParaPenalidadeTexto(custo: String): String {
    val clean = custo.trim()
    clean.toIntOrNull()?.let { base -> return "-${(base + 1) / 2}" }
    if (clean.contains("/")) {
        return clean.split("/").joinToString("/") { p ->
            p.replace("+", "").trim().toIntOrNull()?.let { "-${(it + 1) / 2}" } ?: "—"
        }
    }
    if (clean.endsWith("+")) clean.removeSuffix("+").toIntOrNull()?.let { return "-${(it + 1) / 2}+" }
    if (clean.startsWith("+")) clean.removePrefix("+").toIntOrNull()?.let { return "-${(it + 1) / 2}" }
    return "—"
}

/** Seção **exclusiva** para PODERES de Antecedente Arcano. */
@Composable
fun PoderesSection(
    state: CriadorState,
    onOpenListaCompletaPoderes: () -> Unit
) {
    val context = LocalContext.current
    val locked = state.progresso > 0 && !state.emProgresso

    // 1) Detecta arcanos ativos de forma robusta (modelo -> chave compatível com arcanoInfo)
    val arcanosAtivos = remember(state.vantagensSelecionadas) {
        val ativos = state.vantagensSelecionadas.mapNotNull { it.toArcanoKeyFromModel() }.distinct()
        Log.d(TAG, "Vantagens selecionadas: ${state.vantagensSelecionadas.joinToString { it.nome }}")
        Log.d(TAG, "Arcanos ativos: $ativos")
        ativos
    }

    // Se não houver AA, esta seção idealmente nem apareceria; por segurança, só retorna.
    if (arcanosAtivos.isEmpty()) {
        Log.w(TAG, "Nenhum Antecedente Arcano ativo — seção não renderizada.")
        return
    }

    var selectedArcanoKey by rememberSaveable(arcanosAtivos) {
        mutableStateOf(arcanosAtivos.first())
    }
    LaunchedEffect(arcanosAtivos) {
        if (selectedArcanoKey !in arcanosAtivos) selectedArcanoKey = arcanosAtivos.first()
    }

    // 2) Carrega poderes (modelo com StringOrIntSerializer para pontosDePoder)
    val allPoderes: List<Poder> = remember {
        val res: Result<List<Poder>> = runCatching {
            context.loadJsonAsset<List<Poder>>("poderes.json")
        }
        res.onFailure { Log.e(TAG, "Falha lendo poderes.json", it) }
            .getOrElse { emptyList() }
    }

    // 3) Header padronizado mostrando PP e Foco reais vindos do arcanoInfo global
    val arcKey = selectedArcanoKey.normAAKey() // mesma normalização das chaves do arcanoInfo
    val (slotsCount, ppTotal, foco) = arcanoInfo[arcKey] ?: Triple(0, 0, "—")

    val showListaCompleta = boolResOrDefault(R.bool.show_lista_completa, true)
    SectionHeader(
        onHelpClick = { /* abre ajuda se quiser */ },
        centerText  = "PP: $ppTotal  •  Foco: $foco",
        onCenterClick = null,
        onListaCompletaClick = if (showListaCompleta) onOpenListaCompletaPoderes else null,
        listaCompletaText = "Lista Completa"
    )

    HorizontalDivider(thickness = 1.dp)

    // 4) Slots do arcano selecionado
    val slots = remember(arcKey, slotsCount) {
        val existente = state.poderSlotsPorArcano[arcKey]
        if (existente != null && existente.size == slotsCount) {
            existente
        } else {
            val nova = mutableStateListOf<String?>().apply {
                repeat(slotsCount) { add(null) }
            }
            state.poderSlotsPorArcano[arcKey] = nova
            nova
        }
    }

    // 5) (opcional) aqui você poderia filtrar por estágio/foco se desejar
    val poderesElegiveis = remember(allPoderes) { allPoderes }

    // 6) UI — sem LazyColumn para evitar altura infinita (a tela pai já rola)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Bloco de slots
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Column(Modifier.padding(12.dp)) {
                Text("Slots: $slotsCount", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))

                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    slots.forEachIndexed { idx, poderId ->
                        val label = poderId ?: "— vazio —"
                        AssistChip(
                            onClick = {
                                if (!locked && poderId != null) {
                                    slots[idx] = null
                                    Log.d(TAG, "[$arcKey] limpou slot ${idx + 1}")
                                }
                            },
                            label = { Text("${idx + 1}: $label") },
                            enabled = !locked && poderId != null
                        )
                    }
                }
            }
        }

        // Lista de poderes
        poderesElegiveis.forEach { poder ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text(poder.nome, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(2.dp))

                    if (state.usarSemPontosDePoder) {
                        Text("Penalidade: ${custoParaPenalidadeTexto(poder.pontosDePoder)}")
                    } else {
                        Text("Custo (PP): ${poder.pontosDePoder}")
                    }

                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            enabled = !locked && slots.any { it == null },
                            onClick = {
                                val firstEmpty = slots.indexOfFirst { it == null }
                                if (firstEmpty >= 0) {
                                    slots[firstEmpty] = poder.id
                                    Log.d(TAG, "[$arcKey] slot ${firstEmpty + 1} <- ${poder.id}")
                                }
                            }
                        ) { Text("Comprar") }

                        Button(
                            enabled = !locked && slots.any { it == poder.id },
                            onClick = {
                                val idx = slots.indexOfFirst { it == poder.id }
                                if (idx >= 0) {
                                    slots[idx] = null
                                    Log.d(TAG, "[$arcKey] removeu ${poder.id} do slot ${idx + 1}")
                                }
                            }
                        ) { Text("Remover") }
                    }
                }
            }
        }
    }

    // 7) Log final rápido pra depuração
    LaunchedEffect(poderesElegiveis.size, selectedArcanoKey, slots.size) {
        val usados = slots.count { it != null }
        Log.d(TAG, "ready: arcano=$arcKey slots=${slots.size} usados=$usados poderes=${poderesElegiveis.size}")
    }
}
