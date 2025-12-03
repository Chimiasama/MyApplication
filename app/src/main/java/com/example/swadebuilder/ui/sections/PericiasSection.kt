package com.example.swadebuilder.ui.sections

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.listaPericias
import com.example.swadebuilder.mapaAtributosDisplay
import com.example.swadebuilder.model.EspecializacoesDto
import com.example.swadebuilder.toDiceString
import com.example.swadebuilder.ui.components.PbWalletBanner
import com.example.swadebuilder.ui.components.SectionHeader
import kotlin.math.max

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun PericiasContent(
    state: CriadorState,
    onOpenPericiasDetail: () -> Unit,
    feedbackMessages: MutableList<String>
) {
    val locked = state.criacaoBasicaCongelada && !state.skillAdvancementInProgress

    val pcTotal  = state.pontosComplicacao
    val pcGastos = state.pontosComplicacaoGastos
    val pcLivres = (pcTotal - pcGastos).coerceAtLeast(0)
    val spUsados = state.cpSpStack.size

    var showSpecDialog by rememberSaveable { mutableStateOf(false) }
    var specText by rememberSaveable { mutableStateOf("") }
    var specTarget by rememberSaveable { mutableStateOf<com.example.swadebuilder.Pericia?>(null) }
    var buyingExtraSpec by rememberSaveable { mutableStateOf(false) }

    var showEditDialog by rememberSaveable { mutableStateOf(false) }
    var editIsPrincipal by rememberSaveable { mutableStateOf(false) }
    var editPerTarget by rememberSaveable { mutableStateOf<com.example.swadebuilder.Pericia?>(null) }
    var editOldName by rememberSaveable { mutableStateOf("") }
    var editNewName by rememberSaveable { mutableStateOf("") }

    val idosoActive = state.idosoBonusSp > 0

    val valorColWidthDp = 80.dp

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 400.dp)
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        stickyHeader {
            val pergaminho = MaterialTheme.colorScheme.surfaceVariant
            val showLista = booleanResource(com.example.swadebuilder.R.bool.show_lista_completa)

            Surface(
                tonalElevation = 0.dp,
                color = pergaminho,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    SectionHeader(
                        onHelpClick          = null,
                        centerText           = "Pontos de Perícia: ${state.pontosPericia}",
                        onListaCompletaClick = if (showLista) ({ onOpenPericiasDetail() }) else null,
                        listaCompletaText    = "Lista Completa"
                    )

                    Spacer(Modifier.height(4.dp))

                    PbWalletBanner(
                        pcTotal = pcTotal,
                        pcLivres = pcLivres,
                        spendLabel = "Usar PB em Perícias",
                        refundLabel = "Desfazer uso de PB",
                        spendEnabled = !locked && pcLivres > 0,
                        refundEnabled = !locked && spUsados > 0,
                        onSpend = {
                            state.cpSpStack.add(Unit)
                            state.pontosComplicacaoGastos += 1
                        },
                        onRefund = {
                            state.cpSpStack.removeAt(state.cpSpStack.lastIndex)
                            state.pontosComplicacaoGastos =
                                (state.pontosComplicacaoGastos - 1).coerceAtLeast(0)
                            state.syncFromCPRefund(sp = true, feedbackMessages = feedbackMessages)
                        }
                    )
                }
            }
        }

        items(listaPericias) { per ->
            val currentRaw = state.rawTotal(per)
            val attrKey    = state.atributoBaseParaPericia(per)
            val atrRaw     = state.valoresAtributos[attrKey]!!.intValue
            val capRaw     = state.periciaCapRaw(per)

            val displayRaw = state.rawTotalComSupers(per)

            val nextRaw = when {
                currentRaw == 0 && per.basica -> 4
                currentRaw < 12               -> currentRaw + 2
                else                          -> currentRaw + 1
            }
            val costNormal = if (nextRaw <= atrRaw) 1 else 2

            val compStack = state.compCostStackPorPericia.getValue(per)
            val spStack   = state.spCostStackPorPericia.getValue(per)

            val minimoBasico: Int = state.minPericiaPorVantagem[per] ?: 0

            val opcionalList: List<Int> = state.vantagensSelecionadas.flatMap { vant ->
                val mapaOpc = vant.requisitos.periciaMinOpcional ?: emptyMap()
                mapaOpc.entries
                    .filter { it.key.equals(per.nome, ignoreCase = true) }
                    .map { it.value }
            }
            val minimoOpcional: Int = opcionalList.maxOrNull() ?: 0
            val minimoTotal = max(minimoBasico, minimoOpcional)

            val canDecrease = if (state.modoProgressaoAtivo) {
                val frozenIncs = state.frozenSkillIncrements[per.nome] ?: 0
                state.baseIncsPorPericia.getValue(per) > frozenIncs
            } else {
                !locked &&
                        (compStack.isNotEmpty() || spStack.any { it > 0 }) &&
                        (currentRaw - 2 >= minimoTotal)
            }

            val astuciaSpent = state.spCostStackPorPericia
                .filterKeys { p -> p.atributo == "ASTUCIA" }
                .values
                .sumOf { costs -> costs.sum() }

            val canIncrease = !locked &&
                    state.pontosPericia >= costNormal &&
                    nextRaw <= capRaw &&
                    (if (idosoActive && astuciaSpent < 5)
                        per.atributo == "ASTUCIA"
                    else
                        true)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val defaultSize = MaterialTheme.typography.bodyLarge.fontSize

                    Text(
                        text = buildAnnotatedString {
                            if (per.basica) {
                                withStyle(
                                    SpanStyle(
                                        color = Color.Red,
                                        fontWeight = FontWeight.Bold
                                    )
                                ) {
                                    append("✯ ${per.nome}")
                                }
                            } else {
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append(per.nome)
                                }
                            }
                            withStyle(SpanStyle(fontSize = defaultSize / 2)) {
                                val displayAtr = mapaAtributosDisplay[attrKey] ?: attrKey
                                append(" ($displayAtr)")
                            }
                        },
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    IconButton(
                        onClick = {
                            state.decreasePericia(per)
                            if (state.rawTotal(per) == 0) {
                                state.especializacoesPorPericia.remove(per.nome)
                            }
                        },
                        enabled = canDecrease,
                        modifier = Modifier
                            .size(32.dp)
                            .padding(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Remove,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Text(
                        text = when {
                            displayRaw == 0 && per.basica -> "d4"
                            displayRaw == 0 -> "-"
                            else -> displayRaw.toDiceString()
                        },
                        modifier = Modifier.width(valorColWidthDp),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )

                    IconButton(
                        onClick = {
                            val currRawNow = state.rawTotal(per)
                            val attrKeyNow = state.atributoBaseParaPericia(per)
                            val atrRawNow  = state.valoresAtributos[attrKeyNow]!!.intValue
                            val capRawNow  = state.periciaCapRaw(per)

                            val nextRawNow = when {
                                currRawNow == 0 && per.basica -> 4
                                currRawNow < 12               -> currRawNow + 2
                                else                          -> currRawNow + 1
                            }
                            val costNow = if (nextRawNow <= atrRawNow) 1 else 2

                            val astuciaSpentNow = state.spCostStackPorPericia
                                .filterKeys { p -> p.atributo == "ASTUCIA" }
                                .values
                                .sumOf { costs -> costs.sum() }

                            val canIncreaseNow = !locked &&
                                    state.pontosPericia >= costNow &&
                                    nextRawNow <= capRawNow &&
                                    (if (idosoActive && astuciaSpentNow < 5)
                                        per.atributo == "ASTUCIA"
                                    else
                                        true)

                            if (!canIncreaseNow) {
                                return@IconButton
                            }

                            state.increasePericiaFromAdvancement(per, costNow)

                            if (state.usarEspecializacoesDePericia) {
                                val esp = state.especializacoesPorPericia[per.nome]
                                if (esp?.principal == null) {
                                    specTarget = per
                                    specText = ""
                                    buyingExtraSpec = false
                                    showSpecDialog = true
                                }
                            }
                        },
                        enabled = canIncrease,
                        modifier = Modifier
                            .size(32.dp)
                            .padding(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    val jaTemPrincipal =
                        state.especializacoesPorPericia[per.nome]?.principal != null
                    if (state.usarEspecializacoesDePericia && jaTemPrincipal) {
                        TextButton(
                            onClick = {
                                specTarget = per
                                specText = ""
                                buyingExtraSpec = true
                                showSpecDialog = true
                            },
                            enabled = !locked && state.pontosPericia >= 1
                        ) {
                            Text("Esp+")
                        }
                    }
                }

                val espDto: EspecializacoesDto? = state.especializacoesPorPericia[per.nome]
                val principal = espDto?.principal
                val extras: List<String> = when {
                    espDto == null -> emptyList()
                    else -> espDto.lista
                        .filter { it.isNotBlank() }
                        .distinct()
                        .filter { it != principal }
                }

                val canRemoveSpecs = !locked

                if (principal != null || extras.isNotEmpty()) {
                    Spacer(Modifier.width(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (principal != null) {
                            SpecChip(
                                label = principal,
                                isPrincipal = true,
                                onEdit = {
                                    editIsPrincipal = true
                                    editPerTarget = per
                                    editOldName = principal
                                    editNewName = principal
                                    showEditDialog = true
                                },
                                onRemove = null
                            )
                        }

                        extras.forEach { extra ->
                            SpecChip(
                                label = extra,
                                isPrincipal = false,
                                onEdit = {
                                    editIsPrincipal = false
                                    editPerTarget = per
                                    editOldName = extra
                                    editNewName = extra
                                    showEditDialog = true
                                },
                                onRemove =
                                    if (canRemoveSpecs) {
                                        {
                                            val atual =
                                                state.especializacoesPorPericia[per.nome]
                                                    ?: EspecializacoesDto()
                                            val novaLista =
                                                atual.lista.filter { it != extra }
                                            state.especializacoesPorPericia[per.nome] =
                                                atual.copy(lista = novaLista)
                                        }
                                    } else null
                            )
                        }
                    }
                }
            }
        }
    }

    if (showSpecDialog && specTarget != null) {
        AlertDialog(
            onDismissRequest = { showSpecDialog = false },
            title = {
                Text(
                    if (buyingExtraSpec)
                        "Nova especialização"
                    else
                        "Especialização principal"
                )
            },
            text = {
                Column {
                    Text("Perícia: ${specTarget!!.nome}")
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        value = specText,
                        onValueChange = { specText = it },
                        label = { Text("Nome da especialização") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val per = specTarget!!
                        val nomeEsp = specText.trim()
                        if (nomeEsp.isNotEmpty()) {
                            val atual =
                                state.especializacoesPorPericia[per.nome]
                                    ?: EspecializacoesDto()
                            val novo =
                                if (!buyingExtraSpec) {
                                    val baseLista =
                                        (atual.lista + nomeEsp).distinct()
                                    EspecializacoesDto(
                                        principal = nomeEsp,
                                        lista = baseLista
                                    )
                                } else {
                                    val novas =
                                        (atual.lista + nomeEsp).distinct()
                                    atual.copy(lista = novas).also {
                                        state.spCostStackPorPericia
                                            .getValue(per)
                                            .add(1)
                                    }
                                }
                            state.especializacoesPorPericia[per.nome] = novo
                        }
                        showSpecDialog = false
                    }
                ) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { showSpecDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showEditDialog && editPerTarget != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = {
                Text(
                    if (editIsPrincipal)
                        "Renomear especialização principal"
                    else
                        "Renomear especialização"
                )
            },
            text = {
                Column {
                    Text("Perícia: ${editPerTarget!!.nome}")
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        value = editNewName,
                        onValueChange = { editNewName = it },
                        label = { Text("Novo nome") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val per = editPerTarget!!
                        val novo = editNewName.trim()
                        if (novo.isNotEmpty()) {
                            val atual =
                                state.especializacoesPorPericia[per.nome]
                                    ?: EspecializacoesDto()
                            if (editIsPrincipal) {
                                val antiga = atual.principal
                                val listaSemAntiga =
                                    atual.lista.filter { it != antiga }
                                val novaLista =
                                    (listaSemAntiga + novo).distinct()
                                state.especializacoesPorPericia[per.nome] =
                                    atual.copy(
                                        principal = novo,
                                        lista = novaLista
                                    )
                            } else {
                                val novaLista =
                                    atual.lista.map { if (it == editOldName) novo else it }
                                        .distinct()
                                state.especializacoesPorPericia[per.nome] =
                                    atual.copy(lista = novaLista)
                            }
                        }
                        showEditDialog = false
                    }
                ) { Text("Salvar") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun SpecChip(
    label: String,
    isPrincipal: Boolean,
    onEdit: (() -> Unit)?,
    onRemove: (() -> Unit)?
) {
    val colors = MaterialTheme.colorScheme

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(
                color = if (isPrincipal)
                    colors.secondaryContainer
                else
                    colors.surfaceVariant,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = if (isPrincipal) "$label (principal)" else label,
            style = MaterialTheme.typography.labelMedium,
            color = if (isPrincipal)
                colors.onSecondaryContainer
            else
                colors.onSurfaceVariant
        )
        if (onEdit != null) {
            Spacer(Modifier.width(2.dp))
            IconButton(
                onClick = onEdit,
                modifier = Modifier.size(18.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Renomear",
                    tint = colors.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
        if (onRemove != null) {
            Spacer(Modifier.width(2.dp))
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(18.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remover",
                    tint = colors.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}