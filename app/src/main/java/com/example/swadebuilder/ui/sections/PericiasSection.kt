package com.example.swadebuilder.ui.sections

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.PericiaRuleSnapshot
import com.example.swadebuilder.R
import com.example.swadebuilder.calcularPericiaRules
import com.example.swadebuilder.criacaoBasicaCongelada
import com.example.swadebuilder.listaPericias
import com.example.swadebuilder.mapaAtributosDisplay
import com.example.swadebuilder.model.EspecializacoesDto
import com.example.swadebuilder.model.loadPericiasDescriptions
import com.example.swadebuilder.toDiceString
import com.example.swadebuilder.ui.components.PbWalletBanner
import com.example.swadebuilder.ui.components.SectionHeader
import com.example.swadebuilder.util.semAcentos

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun PericiasContent(
    state: CriadorState,
    feedbackMessages: MutableList<String>,
    onUserFeedback: () -> Unit
) {
    val context = LocalContext.current
    val allowLongTexts = booleanResource(R.bool.enable_long_texts)
    val descricoes = remember(allowLongTexts) {
        if (!allowLongTexts) emptyMap() else loadPericiasDescriptions(context, R.raw.pericias)
    }
    val detalhesExpandidos = remember { mutableStateMapOf<String, Boolean>() }

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

    // PROMPT 5: State for Note Dialog
    var showNoteDialog by rememberSaveable { mutableStateOf(false) }
    var noteText by rememberSaveable { mutableStateOf("") }
    var noteTarget by rememberSaveable { mutableStateOf<com.example.swadebuilder.Pericia?>(null) }

    val idosoActive = state.idosoBonusSp > 0

    val valorColWidthDp = 80.dp

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 400.dp)
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        stickyHeader {
            val pergaminho = MaterialTheme.colorScheme.surfaceVariant

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
                        onListaCompletaClick = null,
                        listaCompletaText    = ""
                    )

                    Spacer(Modifier.height(4.dp))

                    if (!state.emProgresso) {
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
        }

        items(
            listaPericias.filter { per ->
                if (per.nome.equals("Jutsu", ignoreCase = true)) {
                    state.compendioArteDaGuerraAtivo
                } else if (per.nome.equals("Alquimia", ignoreCase = true)) {
                    state.compendioFantasiaAtivo || state.compendioHorrorAtivo
                } else {
                    true
                }
            }
        ) { per ->
            val regra: PericiaRuleSnapshot = state.calcularPericiaRules(
                pericia = per,
                idosoActive = idosoActive,
                locked = locked
            )

            val rawName = per.nome.removePrefix("*").trim()
            val descKey = "$rawName (${per.atributo})".uppercase().semAcentos()

            val descricao = if (per.nome.equals("Alquimia", ignoreCase = true)) {
                val fantasiaAtivo = state.compendioFantasiaAtivo
                val horrorAtivo = state.compendioHorrorAtivo
                val txtFantasia = "Esta é a perícia arcana para alquimistas (veja a página 102), mas também pode ser usada para criar itens alquímicos (página 68). Pode ser usada no lugar de Ciências ao examinar reações químicas, estudar reagentes e outros tópicos relacionados."
                val txtHorror = "Esta é a perícia arcana para alquimistas (veja p. 70) e também pode ser usada para criar itens alquímicos (p. 117) ou ser usada no lugar de Ciências ao examinar reações químicas, estudar reagentes ou assuntos relacionados."

                when {
                    fantasiaAtivo && horrorAtivo ->
                        "[FANTASIA] $txtFantasia\n\n[HORROR] $txtHorror"
                    fantasiaAtivo -> txtFantasia
                    horrorAtivo -> txtHorror
                    else -> ""
                }
            } else {
                descricoes[descKey].orEmpty()
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val defaultSize = MaterialTheme.typography.bodyLarge.fontSize

                    Column(modifier = Modifier.weight(1f)) {
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
                                    val displayAtr = mapaAtributosDisplay[regra.attrKey] ?: regra.attrKey
                                    append(" ($displayAtr)")
                                }
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        // PROMPT 5: Display Skill Note
                        val note = state.notasPericia[per.nome]
                        if (!note.isNullOrBlank()) {
                            Text(
                                text = "($note)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }

                    // PROMPT 5: Edit Note Button
                    if (regra.displayRaw > 0) { // Only allow notes if skill is purchased/has value
                        IconButton(
                            onClick = {
                                noteTarget = per
                                noteText = state.notasPericia[per.nome] ?: ""
                                showNoteDialog = true
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .padding(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Editar nota",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            state.decreasePericia(per)
                            if (state.rawTotal(per) == 0) {
                                state.especializacoesPorPericia.remove(per.nome)
                                state.notasPericia.remove(per.nome) // Clear note if skill is removed
                            }
                            onUserFeedback()
                        },
                        enabled = regra.canDecrease,
                        modifier = Modifier
                            .size(32.dp)
                            .padding(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Remove,
                            contentDescription = "Diminuir ${per.nome}",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Text(
                        text = when {
                            regra.displayRaw == 0 && per.basica -> "d4"
                            regra.displayRaw == 0 -> "-"
                            else -> regra.displayRaw.toDiceString()
                        },
                        modifier = Modifier.width(valorColWidthDp),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )

                    IconButton(
                        onClick = {
                            val regrasAtuais = state.calcularPericiaRules(
                                pericia = per,
                                idosoActive = idosoActive,
                                locked = locked
                            )

                            if (!regrasAtuais.canIncrease) {
                                return@IconButton
                            }

                            state.increasePericiaFromAdvancement(per, regrasAtuais.cost)
                            onUserFeedback()

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
                        enabled = regra.canIncrease,
                        modifier = Modifier
                            .size(32.dp)
                            .padding(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Aumentar ${per.nome}",
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
                                onUserFeedback()
                            },
                            enabled = !locked && state.pontosPericia >= 1
                        ) {
                            Text("Esp+")
                        }
                    }
                }

                if (allowLongTexts && descricao.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    TextButton(
                        onClick = {
                            val current = detalhesExpandidos[descKey] ?: false
                            detalhesExpandidos[descKey] = !current
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            if (detalhesExpandidos[descKey] == true) "Ocultar detalhes" else "Ver detalhes",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    AnimatedVisibility(visible = detalhesExpandidos[descKey] == true) {
                        Text(
                            text = descricao,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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

    // PROMPT 5: Note Dialog
    if (showNoteDialog && noteTarget != null) {
        AlertDialog(
            onDismissRequest = { showNoteDialog = false },
            title = { Text("Nota de Perícia (Especialização)") },
            text = {
                Column {
                    Text("Perícia: ${noteTarget!!.nome}")
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text("Ex: Pistolas, Espadas, etc.") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val per = noteTarget!!
                        if (noteText.isBlank()) {
                            state.notasPericia.remove(per.nome)
                        } else {
                            state.notasPericia[per.nome] = noteText.trim()
                        }
                        showNoteDialog = false
                    }
                ) { Text("Salvar") }
            },
            dismissButton = {
                TextButton(onClick = { showNoteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
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
                    contentDescription = "Renomear $label",
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
                    contentDescription = "Remover $label",
                    tint = colors.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}