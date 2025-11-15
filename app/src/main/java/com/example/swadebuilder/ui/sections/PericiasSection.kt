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
import com.example.swadebuilder.SectionHeader
import com.example.swadebuilder.listaPericias
import com.example.swadebuilder.mapaAtributosDisplay
import com.example.swadebuilder.model.EspecializacoesDto
import com.example.swadebuilder.toDiceString
import kotlin.math.max

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun PericiasContent(
    state: CriadorState,
    onOpenPericiasDetail: () -> Unit
) {
    val locked = state.progresso > 0
    var showHelp by rememberSaveable { mutableStateOf(false) }

    // Diálogo para PRIMEIRA especialização (quando compra a perícia)
    var showSpecDialog by rememberSaveable { mutableStateOf(false) }
    var specText by rememberSaveable { mutableStateOf("") }
    var specTarget by rememberSaveable { mutableStateOf<com.example.swadebuilder.Pericia?>(null) }
    var buyingExtraSpec by rememberSaveable { mutableStateOf(false) }

    // Diálogo de EDIÇÃO (renomear) de especialização (principal OU extra)
    var showEditDialog by rememberSaveable { mutableStateOf(false) }
    var editIsPrincipal by rememberSaveable { mutableStateOf(false) }
    var editPerTarget by rememberSaveable { mutableStateOf<com.example.swadebuilder.Pericia?>(null) }
    var editOldName by rememberSaveable { mutableStateOf("") }
    var editNewName by rememberSaveable { mutableStateOf("") }

    // Idoso (bônus de SP até 5 na ÁSTUCIA)
    val idosoActive = state.idosoBonusSp > 0
    val astuciaSpent = state.spCostStackPorPericia
        .filterKeys { per -> per.atributo == "ASTUCIA" }
        .values
        .sumOf { costs -> costs.sum() }

    // Largura da coluna de valor (para caber "d12+1" tranquilo, como nos atributos)
    val valorColWidthDp = 80.dp

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 400.dp)
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        stickyHeader {
            // Usa a cor do tema em vez de amarelo fixo
            val pergaminho = MaterialTheme.colorScheme.surfaceVariant
            val showLista = booleanResource(com.example.swadebuilder.R.bool.show_lista_completa)

            Surface(
                tonalElevation = 0.dp,
                color = pergaminho,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                SectionHeader(
                    onHelpClick          = { showHelp = true },
                    centerText           = "Pontos restantes: ${state.pontosPericia}",
                    onListaCompletaClick = if (showLista) ({ onOpenPericiasDetail() }) else null,
                    listaCompletaText    = "Lista Completa"
                )
            }
            if (showHelp) {
                AlertDialog(
                    onDismissRequest = { showHelp = false },
                    title            = { Text("Como funciona") },
                    text             = { Text("Cada avanço de perícia custa 1 SP se abaixo do atributo relacionado ou 2 SP se acima. A primeira especialização é obrigatória quando você compra a perícia; especializações extras custam 1 SP cada.") },
                    confirmButton    = {
                        TextButton(onClick = { showHelp = false }) { Text("OK") }
                    }
                )
            }
        }

        items(listaPericias) { per ->
            val currentRaw = state.rawTotal(per)
            val attrKey    = state.atributoBaseParaPericia(per)
            val atrRaw     = state.valoresAtributos[attrKey]!!.intValue
            val capRaw     = state.periciaCapRaw(per)
            val nextRaw = when {
                currentRaw == 0 && per.basica -> 4        // básicas ficam em d4 quando "zeradas"
                currentRaw < 12               -> currentRaw + 2
                else                          -> currentRaw + 1
            }
            val costNormal = if (nextRaw <= atrRaw) 1 else 2

            val compStack = state.compCostStackPorPericia.getValue(per)
            val spStack   = state.spCostStackPorPericia.getValue(per)

            // Mínimo imposto por vantagens
            val minimoBasico: Int = state.minPericiaPorVantagem[per] ?: 0

            // Opcional de requisitos
            val opcionalList: List<Int> = state.vantagensSelecionadas.flatMap { vant ->
                val mapaOpc = vant.requisitos.periciaMinOpcional ?: emptyMap()
                mapaOpc.entries
                    .filter { it.key.equals(per.nome, ignoreCase = true) }
                    .map { it.value }
            }
            val minimoOpcional: Int = opcionalList.maxOrNull() ?: 0
            val minimoTotal = max(minimoBasico, minimoOpcional)

            val canDecrease = !locked &&
                    (compStack.isNotEmpty() || spStack.any { it > 0 }) &&
                    (currentRaw - 2 >= minimoTotal)

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
                                withStyle(SpanStyle(color = Color.Red, fontWeight = FontWeight.Bold)) {
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

                    // −
                    IconButton(
                        onClick = {
                            // reduzir a perícia
                            state.decreasePericia(per)
                            // se zerou, remover TODAS as especializações da perícia
                            if (state.rawTotal(per) == 0) {
                                state.especializacoesPorPericia.remove(per.nome)
                            }
                        },
                        enabled = canDecrease,
                        modifier = Modifier
                            .size(32.dp)
                            .padding(4.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.fillMaxSize())
                    }

                    // valor (agora com largura maior pra caber "d12+1")
                    Text(
                        text = when (currentRaw) {
                            0 if per.basica -> "d4"
                            0 -> "-"
                            else -> currentRaw.toDiceString()
                        },
                        modifier = Modifier.width(valorColWidthDp),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )

                    // +
                    IconButton(
                        onClick = {
                            // avanço normal de perícia
                            state.baseIncsPorPericia[per] = state.baseIncsPorPericia.getValue(per) + 1
                            state.spCostStackPorPericia.getValue(per).add(costNormal)

                            // se a regra estiver ativa e ainda não existe especialização principal, solicitar agora
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
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.fillMaxSize())
                    }

                    // Esp+
                    val jaTemPrincipal = state.especializacoesPorPericia[per.nome]?.principal != null
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

                // ====== RESUMO / CHIPS DE ESPECIALIZAÇÕES ======
                val espDto: EspecializacoesDto? = state.especializacoesPorPericia[per.nome]
                val principal = espDto?.principal
                // lista de extras = lista sem o principal (e sem duplicatas)
                val extras: List<String> = when {
                    espDto == null -> emptyList()
                    else -> espDto.lista
                        .filter { it.isNotBlank() }
                        .distinct()
                        .filter { it != principal }
                }

                // Pode remover especializações extras? Somente no modo de construção inicial.
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
                        // Chip da principal (SEM remover, mas com editar)
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
                                onRemove = null // travado sempre, mesmo em modo inicial
                            )
                        }

                        // Chips das extras:
                        // - SEM “X” quando locked == true (após construção)
                        // - COM “X” e devolução de 1 SP quando locked == false (construção)
                        extras.forEach { nome ->
                            SpecChip(
                                label = nome,
                                isPrincipal = false,
                                onEdit = {
                                    editIsPrincipal = false
                                    editPerTarget = per
                                    editOldName = nome
                                    editNewName = nome
                                    showEditDialog = true
                                },
                                onRemove = if (canRemoveSpecs) {
                                    {
                                        // remover especialização extra
                                        val atuais = state.especializacoesPorPericia[per.nome]
                                            ?: EspecializacoesDto()
                                        val novaLista = atuais.lista.filter { it != nome }
                                        state.especializacoesPorPericia[per.nome] =
                                            atuais.copy(lista = novaLista)

                                        // devolver 1 SP → remove uma entrada "1" do stack de SP
                                        val stack = state.spCostStackPorPericia.getValue(per)
                                        val idx = stack.indexOfLast { it == 1 }
                                        if (idx != -1) stack.removeAt(idx)
                                    }
                                } else null
                            )
                        }
                    }
                }
            }
        }
    }

    // Diálogo para inserir a ESPECIALIZAÇÃO PRINCIPAL (ou extra, quando Esp+)
    if (showSpecDialog && specTarget != null) {
        AlertDialog(
            onDismissRequest = { showSpecDialog = false },
            title = { Text(if (buyingExtraSpec) "Nova especialização" else "Especialização principal") },
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
                            val atual = state.especializacoesPorPericia[per.nome] ?: EspecializacoesDto()
                            val novo =
                                if (!buyingExtraSpec) {
                                    // definindo a PRINCIPAL
                                    val baseLista = (atual.lista + nomeEsp).distinct()
                                    EspecializacoesDto(
                                        principal = nomeEsp,
                                        lista = baseLista
                                    )
                                } else {
                                    // adicionando EXTRA (1 SP)
                                    val novas = (atual.lista + nomeEsp).distinct()
                                    atual.copy(lista = novas).also {
                                        state.spCostStackPorPericia.getValue(per).add(1)
                                    }
                                }
                            state.especializacoesPorPericia[per.nome] = novo
                        }
                        showSpecDialog = false
                    }
                ) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { showSpecDialog = false }) { Text("Cancelar") }
            }
        )
    }

    // Diálogo para RENOMEAR especialização (principal OU extra)
    if (showEditDialog && editPerTarget != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text(if (editIsPrincipal) "Renomear especialização principal" else "Renomear especialização") },
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
                            val atual = state.especializacoesPorPericia[per.nome] ?: EspecializacoesDto()
                            if (editIsPrincipal) {
                                // renomeia principal e mantém/coerentiza na lista
                                val antiga = atual.principal
                                val listaSemAntiga = atual.lista.filter { it != antiga }
                                val novaLista = (listaSemAntiga + novo).distinct()
                                state.especializacoesPorPericia[per.nome] =
                                    atual.copy(principal = novo, lista = novaLista)
                            } else {
                                // renomeia um item extra na lista
                                val novaLista = atual.lista.map { if (it == editOldName) novo else it }.distinct()
                                state.especializacoesPorPericia[per.nome] =
                                    atual.copy(lista = novaLista)
                            }
                        }
                        showEditDialog = false
                    }
                ) { Text("Salvar") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

/** Chip compacto para exibir/editar/remover especializações. */
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
