package com.example.swadebuilder.ui.sections

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.swadebuilder.model.EspecializacoesDto
import com.example.swadebuilder.model.Pericia
import com.example.swadebuilder.model.SAVAGE_PATHFINDER_BLOCKED_SKILLS
import com.example.swadebuilder.toDiceString
import com.example.swadebuilder.ui.components.PbWalletBanner
import com.example.swadebuilder.ui.components.SectionCard
import com.example.swadebuilder.ui.components.SectionHeader
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.semAcentos

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun PericiasContent(
    state: CriadorState,
    feedbackMessages: MutableList<String>,
    onUserFeedback: () -> Unit
) {
    LocalContext.current
    val allowLongTexts = booleanResource(R.bool.enable_long_texts)
    val usePbWalletRedesign = booleanResource(R.bool.enable_pb_wallet_redesign)
    val detalhesExpandidos = remember { mutableStateMapOf<String, Boolean>() }

    val locked = state.criacaoBasicaCongelada && !state.skillAdvancementInProgress

    val pcTotal  = state.pontosComplicacao
    val pcGastos = state.pontosComplicacaoGastos
    val pcLivres = (pcTotal - pcGastos).coerceAtLeast(0)

    var showSpecDialog by rememberSaveable { mutableStateOf(false) }
    var specText by rememberSaveable { mutableStateOf("") }
    var specTarget by remember { mutableStateOf<Pericia?>(null) }
    var buyingExtraSpec by rememberSaveable { mutableStateOf(false) }

    var showEditDialog by rememberSaveable { mutableStateOf(false) }
    var editIsPrincipal by rememberSaveable { mutableStateOf(false) }
    var editPerTarget by remember { mutableStateOf<Pericia?>(null) }
    var editOldName by rememberSaveable { mutableStateOf("") }
    var editNewName by rememberSaveable { mutableStateOf("") }

    // PROMPT 5: State for Note Dialog
    var showNoteDialog by rememberSaveable { mutableStateOf(false) }
    var noteText by rememberSaveable { mutableStateOf("") }
    var noteTarget by remember { mutableStateOf<Pericia?>(null) }

    var showIdiomaDialog by rememberSaveable { mutableStateOf(false) }
    var idiomaText by rememberSaveable { mutableStateOf("") }
    var idiomaTarget by remember { mutableStateOf<Pericia?>(null) }
    var idiomaPendingCost by rememberSaveable { mutableIntStateOf(0) }
    var idiomaEditMode by rememberSaveable { mutableStateOf(false) }

    val idosoActive = state.idosoBonusSp > 0

    val valorColWidthDp = 80.dp

    // Jutsu description provided by user
    val jutsuDesc = """
        Jutsu representa o treinamento em uma categoria de instrumentos de combate corpo a corpo. Jutsu segue todas as regras da perícia Lutar, mas utiliza a regra Especialização de Perícia exclusivamente para esta perícia. Quando um personagem usa uma arma que não está coberta por uma perícia Jutsu conhecida, ele sofre uma penalidade de -2. Ao contrário da Especialização de Perícia, cada vez que um herói deseja aprender uma nova categoria através de um Progresso, isso é contado como aprender uma nova perícia. Isso significa que cada grupo de Jutsu é uma perícia separada. As seguintes categorias são exemplos, mas não abrangem a ampla gama de opções de combate corpo a corpo disponíveis. Jogadores e Narradores devem estar abertos a discutir a adição, remoção, agrupamento ou até mesmo a criação de novas categorias conforme necessário para se adequar à campanha. Jutsu (Concussão): Esta categoria de perícia foca no uso de objetos sólidos sem gumes cortantes. Desde o uso do bastão defensivo de 3 partes até as tonfas de madeira, a proficiência neste grupo também inclui nunchaku e chuis. Proficiência: bastões de 3 partes, chui (maça), pá do monge, nunchaku, tetsubo, tonfa, martelo de guerra. Jutsu (Corrente): Está incluído neste grupo armas únicas que exigem uma habilidade especial e oferecem alcance letal. Elas são consideradas não-convencionais (desonrosas). São usadas principalmente por diversos grupos de youxia e shinobi. Proficiência: dardo com corda, kusarigama, kyoketsu-shogi, manriki kusari, martelo meteoro, cabelo. Jutsu (Leve): A categoria de armas leves abrange uma mistura de habilidades variadas. Envolve desde as facas mais comuns até o leque de guerra do Daimiô; esses objetos atuam como complementos para espadas e armas primárias. Proficiência: faca, kama, tessen, jette, sai, espada borboleta, nunchaku, escova de ferro, tekko kagi. Jutsu (Massivo): Armas Massivas são usadas com destreza e grande facilidade. Aqueles familiarizados com itens Massivos não sofrem penalidades ao empunhá-los. Jutsu (Passivo): Instrumentos usados por aqueles que evitam o caminho da agressão. Proficiência: bastão-bo, escova de ferro, jitte, nunchaku, sai, tessen. Jutsu (Haste): Armas cortantes anexadas a longos bastões de madeira ou metal, armas desta categoria são vistas entre os camponeses e soldados voluntários. O treinamento abrange a prática no uso do yari no campo de batalha à frente, até lanças usadas pela cavalaria. Proficiência: bastão-bo, alabarda, lança, machado longo, naginata, yari. Jutsu (Samurai): Esta categoria é ensinada especificamente àqueles que frequentaram uma Academia de Guerra ou que foram aprendizes de um Samurai. Proficiência: katana, naginata, nodachi, tanto, tessen, wakizashi. Jutsu (Espada): O caminho da espada é o tipo de arma mais comum encontrado nas mãos de heróis em todo o reino. Em duelos, a esgrima é considerada a habilidade mais honrosa a ser utilizada pelos campeões. Proficiência: dao, jian, katana, nodachi, shang gou, wakizashi. Jutsu (Desarmado): O Caminho do Punho Vazio vem em formas variadas e é ensinado em muitos estilos diferentes. Esta é a perícia para o artista marcial desarmado que gosta de se envolver em combate desarmado. Proficiência: punho, pé, cabeçada, ombros, pernas, cotovelos, joelhos, dedos.
    """.trimIndent()

    val leiDesc = "Esta perícia é usada para descobrir o que pode ser feito sem consequências jurídicas, proteger seus interesses legais e defender a si ou a outra pessoa num tribunal."

    val periciasVisiveis = remember(
        state.periciasComIdiomas(),
        state.compendioArteDaGuerraAtivo,
        state.compendioFantasiaAtivo,
        state.compendioHorrorAtivo
    ) {
        state.periciasComIdiomas().filter { per ->
            if (per.nome.equals("Jutsu", ignoreCase = true)) {
                // Remove original "Jutsu" if present, we handle it via Lutar logic now,
                // but if it's in the list it might be from the old json if not removed.
                // The user removed it from json.
                // But just in case:
                false
            } else if (per.nome.equals("Alquimia", ignoreCase = true)) {
                state.compendioFantasiaAtivo || state.compendioHorrorAtivo
            } else if (state.compendioPathfinderAtivo) {
                // Pathfinder exclusions
                val n = per.nome.keyify()
                n != "FOCO" && n !in SAVAGE_PATHFINDER_BLOCKED_SKILLS
            } else {
                true
            }
        }.filter {
            it.origem?.uppercase() != "SUPLEMENTO" || state.rawTotal(it) > 0
        }.distinctBy { it.nome.keyify() }
    }

    SectionCard(
        title = "Perícias",
        icon = Icons.Default.School,
        showHeader = false
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // --- HEADER SECTION (Fixed) ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                SectionHeader(
                    onHelpClick          = null,
                    centerText           = "Pontos de Perícia: ${state.pontosPericia} (${pcLivres} PB)",
                    onListaCompletaClick = null,
                    listaCompletaText    = ""
                )

                Spacer(Modifier.height(4.dp))

                if (!state.emProgresso && usePbWalletRedesign) {
                    PbWalletBanner(
                        pcTotal = pcTotal,
                        pcLivres = pcLivres,
                        spendLabel = "PB usado automaticamente ao subir Perícias",
                        refundLabel = "PB devolvido automaticamente ao reduzir",
                        spendEnabled = false,
                        refundEnabled = false,
                        onSpend = {},
                        onRefund = {}
                    )
                }
            }

            // --- LIST SECTION (Scrollable) ---
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(
                    items = periciasVisiveis,
                    key = { it.nome },
                    contentType = { "pericia_item" }
                ) { per ->
                    val regra: PericiaRuleSnapshot = state.calcularPericiaRules(
                        pericia = per,
                        idosoActive = idosoActive,
                        locked = locked
                    )

                    val isIdioma = state.isIdiomaPericia(per)
                    val isJutsu = state.isJutsuPericia(per)
                    val rawName = if (isIdioma) "Idiomas" else if (isJutsu) "Jutsu" else per.nome.removePrefix("*").trim()
                    val descKey = "$rawName (${per.atributo})".uppercase().semAcentos()

                    val descricao = if (isJutsu) {
                        jutsuDesc
                    } else if (per.nome.equals("Lei", ignoreCase = true)) {
                        leiDesc
                    } else if (per.nome.equals("Alquimia", ignoreCase = true)) {
                        val fantasiaAtivo = state.compendioFantasiaAtivo
                        val horrorAtivo = state.compendioHorrorAtivo
                        val txtFantasia = "Esta é a perícia arcana para alquimistas (veja a página 102), mas também pode ser usada para criar itens alquímicos (página 68). Pode ser usada no lugar de Ciências ao examinar reações químicas, estudar reagentes e outros tópicos relacionados."
                        val txtHorror = "Esta é a perícia arcana para alquimistas (veja a página 70) e também pode ser usada para criar itens alquímicos (página 117) ou ser usada no lugar de Ciências ao examinar reações químicas, estudar reagentes ou assuntos relacionados."

                        when {
                            fantasiaAtivo && horrorAtivo ->
                                "[FANTASIA] $txtFantasia\n\n[HORROR] $txtHorror"
                            fantasiaAtivo -> txtFantasia
                            horrorAtivo -> txtHorror
                            else -> ""
                        }
                    } else {
                        if (allowLongTexts) per.descricao.orEmpty() else ""
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
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
                                        val displayName = if (isIdioma) "Idiomas" else if (isJutsu) "Jutsu" else per.nome
                                        if (state.isPericiaBasicaEfetiva(per)) {
                                            withStyle(
                                                SpanStyle(
                                                    color = Color.Red,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            ) {
                                                append("✯ $displayName")
                                            }
                                        } else {
                                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                                append(displayName)
                                            }
                                        }
                                        withStyle(SpanStyle(fontSize = defaultSize / 2)) {
                                            val displayAtr = state.mapaAtributosDisplay[regra.attrKey] ?: regra.attrKey
                                            append(" ($displayAtr)")
                                        }
                                    },
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
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
                            // Correction: Show edit button ONLY if optional rule is active OR it's Idioma/Jutsu
                            if ((isIdioma || isJutsu) && regra.displayRaw > 0) {
                                IconButton(
                                    onClick = {
                                        idiomaTarget = per
                                        idiomaText = state.notasPericia[per.nome] ?: ""
                                        idiomaEditMode = true
                                        showIdiomaDialog = true
                                    },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .padding(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Editar",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else if (regra.displayRaw > 0 && state.usarEspecializacoesDePericia) {
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
                                    if (isIdioma) {
                                        state.syncIdiomaSlots()
                                    }
                                    if (isJutsu) {
                                        state.syncJutsuSlots()
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
                                text = when (regra.displayRaw) {
                                    0 if state.isPericiaBasicaEfetiva(per) -> "d4"
                                    0 -> "-"
                                    else -> regra.displayRaw.toDiceString()
                                },
                                modifier = Modifier.width(valorColWidthDp),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )

                            IconButton(
                                onClick = {
                                    val regrasAtuais = state.calcularPericiaRules(
                                        pericia = per,
                                        idosoActive = idosoActive,
                                        locked = locked
                                    )

                                    if ((isIdioma || isJutsu) && state.rawTotal(per) == 0) {
                                        val podeCobrirComPb = !state.emProgresso && state.pbLivres() >= regrasAtuais.cost
                                        if (!regrasAtuais.canIncrease && !podeCobrirComPb) {
                                            return@IconButton
                                        }

                                        idiomaTarget = per
                                        idiomaText = ""
                                        idiomaPendingCost = regrasAtuais.cost
                                        idiomaEditMode = false
                                        showIdiomaDialog = true
                                        return@IconButton
                                    }

                                    val cpSpBefore = state.cpSpStack.size
                                    val tentouCobrirComPb = !state.emProgresso && !regrasAtuais.canIncrease && state.ensurePericiaBudgetWithPb(regrasAtuais.cost)
                                    if (!regrasAtuais.canIncrease && !tentouCobrirComPb) {
                                        return@IconButton
                                    }

                                    val regrasRecalculadas = state.calcularPericiaRules(
                                        pericia = per,
                                        idosoActive = idosoActive,
                                        locked = locked
                                    )

                                    if (!regrasRecalculadas.canIncrease) {
                                        while (state.cpSpStack.size > cpSpBefore) {
                                            state.cpSpStack.removeAt(state.cpSpStack.lastIndex)
                                            state.pontosComplicacaoGastos =
                                                (state.pontosComplicacaoGastos - 1).coerceAtLeast(0)
                                        }
                                        state.syncFromCPRefund(sp = true, feedbackMessages = feedbackMessages)
                                        return@IconButton
                                    }

                                    state.increasePericiaFromAdvancement(per, regrasRecalculadas.cost)
                                    if (isIdioma) {
                                        state.syncIdiomaSlots()
                                    }
                                    if (isJutsu) {
                                        state.syncJutsuSlots()
                                    }
                                    onUserFeedback()

                                    if (!isIdioma && !isJutsu && state.usarEspecializacoesDePericia) {
                                        val esp = state.especializacoesPorPericia[per.nome]
                                        if (esp?.principal == null) {
                                            specTarget = per
                                            specText = ""
                                            buyingExtraSpec = false
                                            showSpecDialog = true
                                        }
                                    }
                                },
                                enabled = regra.canIncrease || (!state.emProgresso && state.pbLivres() >= regra.cost),
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
                            if (!isIdioma && !isJutsu && state.usarEspecializacoesDePericia && jaTemPrincipal) {
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
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface
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

                        if (!isIdioma && !isJutsu) {
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
            }
        }
    }

    if (showIdiomaDialog && idiomaTarget != null) {
        val isJutsuTarget = state.isJutsuPericia(idiomaTarget!!)
        AlertDialog(
            onDismissRequest = {
                showIdiomaDialog = false
                idiomaEditMode = false
                idiomaTarget = null
            },
            title = {
                val action = if (idiomaEditMode) "Editar" else "Novo"
                val subj = if (isJutsuTarget) "Jutsu" else "Idioma"
                Text("$action $subj")
            },
            text = {
                Column {
                    Text("Perícia: ${if (isJutsuTarget) "Jutsu" else "Idiomas"}")
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        value = idiomaText,
                        onValueChange = { idiomaText = it },
                        label = { Text(if (isJutsuTarget) "Ex: Espada, Leve, Desarmado..." else "Ex: Espanhol, Língua de Sinais...") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val per = idiomaTarget!!
                        val isJutsu = state.isJutsuPericia(per)
                        val label = idiomaText.trim().ifBlank {
                            if (isJutsu) "Jutsu Desconhecido" else state.idiomaDefaultLabel(per)
                        }
                        if (!idiomaEditMode) {
                            if (!state.calcularPericiaRules(
                                    pericia = per,
                                    idosoActive = idosoActive,
                                    locked = locked
                                ).canIncrease
                            ) {
                                val cobriu = !state.emProgresso && state.ensurePericiaBudgetWithPb(idiomaPendingCost)
                                if (!cobriu) {
                                    showIdiomaDialog = false
                                    idiomaEditMode = false
                                    idiomaTarget = null
                                    return@TextButton
                                }
                            }
                            state.notasPericia[per.nome] = label
                            state.increasePericiaFromAdvancement(per, idiomaPendingCost)
                            if (isJutsu) state.syncJutsuSlots() else state.syncIdiomaSlots()
                            onUserFeedback()
                        } else {
                            state.notasPericia[per.nome] = label
                        }
                        showIdiomaDialog = false
                        idiomaEditMode = false
                        idiomaTarget = null
                    }
                ) { Text("Salvar") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showIdiomaDialog = false
                    idiomaEditMode = false
                    idiomaTarget = null
                }) {
                    Text("Cancelar")
                }
            }
        )
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
                        var nomeEsp = specText.trim()

                        // AUTO-FILL PLACEHOLDER IF EMPTY
                        if (nomeEsp.isEmpty()) {
                            val atual = state.especializacoesPorPericia[per.nome]
                            val count = (if (atual?.principal != null) 1 else 0) + (atual?.lista?.size ?: 0)
                            nomeEsp = "Especialização ${count + 1}"
                        }

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SpecChip(
    label: String,
    isPrincipal: Boolean,
    onEdit: (() -> Unit)?,
    onRemove: (() -> Unit)?
) {
    val colors = MaterialTheme.colorScheme
    val containerColor = if (isPrincipal) colors.secondaryContainer else colors.surfaceVariant
    val contentColor = if (isPrincipal) colors.onSecondaryContainer else colors.onSurfaceVariant

    InputChip(
        selected = isPrincipal,
        onClick = { onEdit?.invoke() },
        label = {
            Text(
                text = if (isPrincipal) "$label (principal)" else label,
                style = MaterialTheme.typography.labelMedium
            )
        },
        trailingIcon = {
            if (onRemove != null) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remover $label",
                        modifier = Modifier.size(16.dp),
                        tint = contentColor.copy(alpha = 0.7f)
                    )
                }
            } else if (onEdit != null) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Renomear $label",
                    modifier = Modifier.size(16.dp),
                    tint = contentColor.copy(alpha = 0.7f)
                )
            }
        },
        colors = InputChipDefaults.inputChipColors(
            containerColor = containerColor,
            labelColor = contentColor,
            selectedContainerColor = containerColor,
            selectedLabelColor = contentColor,
            trailingIconColor = contentColor,
            selectedTrailingIconColor = contentColor
        ),
        border = null,
        shape = RoundedCornerShape(16.dp)
    )
}
