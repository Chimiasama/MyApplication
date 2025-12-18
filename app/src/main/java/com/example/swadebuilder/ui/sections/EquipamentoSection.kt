package com.example.swadebuilder.ui.sections

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CollapsibleSection
import com.example.swadebuilder.R
import com.example.swadebuilder.EditionConfig
import com.example.swadebuilder.model.EquipamentoCategoria
import com.example.swadebuilder.model.EquipamentoItem
import com.example.swadebuilder.ui.components.PbWalletBanner
import com.example.swadebuilder.ui.components.SectionCard
import com.example.swadebuilder.ui.components.SectionHeader
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import com.example.swadebuilder.ui.sections.toResumo
import com.example.swadebuilder.util.semAcentos
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

data class EquipFilter(
    val somenteAcessiveis: Boolean = false,
    val busca: String = "",
    val origens: Set<String> = emptySet(),
    val tipos: Set<String> = emptySet(),
    val subtipos: Set<String> = emptySet()
) {
    fun totalSelections() =
        (if (somenteAcessiveis) 1 else 0) +
                origens.size + tipos.size + subtipos.size +
                if (busca.isNotBlank()) 1 else 0

    fun isEmpty() = totalSelections() == 0
}

@Composable
fun EquipFilterDialog(
    allOrigens: List<String>,
    allTipos: List<String>,
    allSubtipos: List<String>,
    current: EquipFilter,
    onChange: (EquipFilter) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filtros de Equipamentos") },
        text = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(end = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = current.somenteAcessiveis,
                        onCheckedChange = {
                            onChange(current.copy(somenteAcessiveis = it))
                        }
                    )
                    Spacer(Modifier.size(4.dp))
                    Text("Somente acessíveis")
                }
                Spacer(Modifier.size(8.dp))

                Text("Origem", fontWeight = FontWeight.Bold)
                allOrigens.forEach { o ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = o in current.origens,
                            onCheckedChange = {
                                val s = current.origens.toMutableSet()
                                if (it) s += o else s -= o
                                onChange(current.copy(origens = s))
                            }
                        )
                        Spacer(Modifier.size(4.dp))
                        Text(o)
                    }
                }
                Spacer(Modifier.size(8.dp))

                Text("Tipo", fontWeight = FontWeight.Bold)
                allTipos.forEach { t ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = t in current.tipos,
                            onCheckedChange = {
                                val s = current.tipos.toMutableSet()
                                if (it) s += t else s -= t
                                onChange(current.copy(tipos = s))
                            }
                        )
                        Spacer(Modifier.size(4.dp))
                        Text(t)
                    }
                }
                Spacer(Modifier.size(8.dp))

                Text("Subtipo", fontWeight = FontWeight.Bold)
                allSubtipos.forEach { st ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = st in current.subtipos,
                            onCheckedChange = {
                                val s = current.subtipos.toMutableSet()
                                if (it) s += st else s -= st
                                onChange(current.copy(subtipos = s))
                            }
                        )
                        Spacer(Modifier.size(4.dp))
                        Text(st)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EquipamentoSection(
    dinheiro: Int,
    usaRiqueza: Boolean,
    dadoRiqueza: Int,
    pcTotal: Int,
    pcLivres: Int,
    recursosPcUsados: Int,
    emProgresso: Boolean,
    modoProgressaoAtivo: Boolean,
    expanded: Boolean,
    onToggle: () -> Unit,
    onUsarPontosBonusEmRecursos: () -> Unit,
    onDesfazerPontosBonusEmRecursos: () -> Unit,
    onEquipamentoDoubleClick: (EquipamentoItem) -> Unit,
    equipamentosComprados: List<EquipamentoItem>,
    onRemoveEquipamentoClick: (EquipamentoItem) -> Unit,
    categorias: List<EquipamentoCategoria>,
    superequipCategorias: List<EquipamentoCategoria>,
    forcaRaw: Int,
    hasMusculoso: Boolean,
    hasSoldado: Boolean,
    soldadoCargaAtivo: Boolean,
    onEditarDinheiro: (Int) -> Unit,
    onToggleSoldadoCarga: () -> Unit,
    compendioFantasiaAtivo: Boolean = false,
    compendioHorrorAtivo: Boolean = false,
    compendioSciFiAtivo: Boolean = false,
    compendioTrilhadorAtivo: Boolean = false,
    compendioDeadlandsAtivo: Boolean = false,
    compendioArteDaGuerraAtivo: Boolean = false,
    compendioCidadeSolVaporAtivo: Boolean = false,
    compendioWiseguysAtivo: Boolean = false,
    compendioCrystalHeartAtivo: Boolean = false,
    modoOficialAtivo: Boolean = false,
    onUserFeedback: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    var showMoneyDialog by rememberSaveable { mutableStateOf(false) }
    var dinheiroInput by rememberSaveable { mutableStateOf(dinheiro.toString()) }

    var expSuperequip by rememberSaveable { mutableStateOf(false) }
    var expFantasiaEquip by rememberSaveable { mutableStateOf(false) }
    var expHorrorEquip by rememberSaveable { mutableStateOf(false) }
    var expSciFiEquip by rememberSaveable { mutableStateOf(false) }
    var expTrilhadorEquip by rememberSaveable { mutableStateOf(false) }
    var expDeadlandsEquip by rememberSaveable { mutableStateOf(false) }
    var expCidadeSolVaporEquip by rememberSaveable { mutableStateOf(false) }

    var filter by remember { mutableStateOf(EquipFilter()) }
    var showFilterDialog by rememberSaveable { mutableStateOf(false) }
    val allowLongTexts = booleanResource(R.bool.enable_long_texts)
    val detalhesExpandidos = remember { mutableStateMapOf<String, Boolean>() }
    val showOfficialNames = EditionConfig.isFullEdition && modoOficialAtivo

    SectionCard(
        title    = "Equipamento",
        expanded = expanded,
        onToggle = onToggle,
        icon     = Icons.Default.ShoppingCart,
        onToggleFeedback = onUserFeedback
    ) {
        if (!expanded) return@SectionCard

        val allCategorias = (categorias + superequipCategorias)
            .filterNot {
                it.tipo.equals("Equipamento Supers", true) ||
                        it.tipo.equals("Equipamentos Supers", true)
            }
            .filter { categoria ->
                val origem = categoria.origem?.ifBlank { "BASICO" }?.uppercase() ?: "BASICO"
                (origem != "ARTE_DA_GUERRA" || compendioArteDaGuerraAtivo) &&
                        (origem != "CIDADE_SOL_VAPOR" || compendioCidadeSolVaporAtivo) &&
                        (origem != "WISEGUYS" || compendioWiseguysAtivo) &&
                        (origem != "CRYSTAL_HEART" || compendioCrystalHeartAtivo)
            }

        // Filtra as categorias normais (não fantasia nem horror nem sci-fi)
        val normalCategorias = allCategorias.filter {
            val origem = it.origem?.ifBlank { "BASICO" }?.uppercase() ?: "BASICO"
            val isFantasia = origem == "FANTASIA"
            val isHorror = origem == "HORROR"
            val isSciFi = origem == "SCI_FI"
            val isTrilhador = origem == "FANTASIA_TRILHADOR"
            val isDeadlands = origem == "DEADLANDS"
            val isArteDaGuerra = origem == "ARTE_DA_GUERRA"
            val isCidadeSolVapor = origem == "CIDADE_SOL_VAPOR"
            val isWiseguys = origem == "WISEGUYS"
            val isCrystalHeart = origem == "CRYSTAL_HEART"
            !isFantasia && !isHorror && !isSciFi && !isTrilhador && !isDeadlands && !isCidadeSolVapor && (!isArteDaGuerra || compendioArteDaGuerraAtivo) && (!isWiseguys || compendioWiseguysAtivo) && (!isCrystalHeart || compendioCrystalHeartAtivo)
        }

        // Filtra as categorias de fantasia (se ativo)
        val fantasiaCategorias = if (compendioFantasiaAtivo) {
            allCategorias.filter {
                (it.origem?.uppercase() ?: "") == "FANTASIA"
            }
        } else {
            emptyList()
        }

        // Filtra as categorias de horror (se ativo)
        val horrorCategorias = if (compendioHorrorAtivo) {
            allCategorias.filter {
                (it.origem?.uppercase() ?: "") == "HORROR"
            }
        } else {
            emptyList()
        }

        val sciFiCategorias = if (compendioSciFiAtivo) {
            allCategorias.filter {
                (it.origem?.uppercase() ?: "") == "SCI_FI"
            }
        } else {
            emptyList()
        }

        val cidadeSolVaporCategorias = if (compendioCidadeSolVaporAtivo) {
            allCategorias.filter {
                (it.origem?.uppercase() ?: "") == "CIDADE_SOL_VAPOR"
            }
        } else {
            emptyList()
        }

        val trilhadorCategorias = if (compendioTrilhadorAtivo) {
            allCategorias.filter {
                (it.origem?.uppercase() ?: "") == "FANTASIA_TRILHADOR"
            }
        } else {
            emptyList()
        }

        val deadlandsCategorias = if (compendioDeadlandsAtivo) {
            allCategorias.filter {
                (it.origem?.uppercase() ?: "") == "DEADLANDS"
            }
        } else {
            emptyList()
        }

        val superequipCategoriasAtivos = if (state.modoSuperequip) superequipCategorias else emptyList()

        val todasCategoriasFiltraveis = buildList {
            addAll(normalCategorias)
            addAll(fantasiaCategorias)
            addAll(horrorCategorias)
            addAll(sciFiCategorias)
            addAll(cidadeSolVaporCategorias)
            addAll(trilhadorCategorias)
            addAll(deadlandsCategorias)
            addAll(superequipCategoriasAtivos)
        }

        val buscaNormalizada = filter.busca.lowercase().trim().semAcentos()

        fun EquipamentoCategoria.combinaFiltros(): Boolean {
            val safeOrigem = origem?.ifBlank { "BASICO" }?.uppercase() ?: "BASICO"
            if (filter.origens.isNotEmpty() && safeOrigem !in filter.origens) return false
            if (filter.tipos.isNotEmpty() && tipo !in filter.tipos) return false
            if (filter.subtipos.isNotEmpty() && subtipo !in filter.subtipos) return false
            return true
        }

        fun EquipamentoItem.combinaBusca(): Boolean {
            if (buscaNormalizada.isBlank()) return true
            val texto = buildList {
                add(nome)
                originalName?.let { add(it) }
                originalDescription?.let { add(it) }
                observacoes.contentString()?.let { add(it) }
                subtipo?.let { add(it) }
                subsubtipo?.let { add(it) }

                val resumo = toResumo()
                resumo.linhaArma?.let { add(it) }
                resumo.linhaGeral?.let { add(it) }
                resumo.linhaVeiculo?.let { add(it) }
                resumo.observacao?.let { add(it) }
            }.joinToString("\n").semAcentos().lowercase()
            return texto.contains(buscaNormalizada)
        }

        fun EquipamentoItem.ehAcessivel(): Boolean {
            if (!filter.somenteAcessiveis || usaRiqueza) return true
            val custoInt = (custo as? JsonPrimitive)?.content?.toIntOrNull() ?: Int.MAX_VALUE
            return custoInt <= dinheiro
        }

        val equipamentosFiltrados = todasCategoriasFiltraveis
            .filter { it.combinaFiltros() }
            .flatMap { cat -> cat.itens.map { it to cat } }
            .filter { (equip, _) -> equip.ehAcessivel() && equip.combinaBusca() }

        SectionHeader(
            onHelpClick = null,
            centerText = if (usaRiqueza) "Riqueza: d$dadoRiqueza" else "Dinheiro: $dinheiro",
            onCenterClick = null,
            onListaCompletaClick = null,
            listaCompletaText = ""
        )

        if (!usaRiqueza && (emProgresso || modoProgressaoAtivo)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Button(onClick = {
                    dinheiroInput = dinheiro.toString()
                    showMoneyDialog = true
                }) {
                    Text("Editar dinheiro")
                }
            }
        }

        Spacer(modifier = Modifier.size(4.dp))

        if (!emProgresso) {
            PbWalletBanner(
                pcTotal = pcTotal,
                pcLivres = pcLivres,
                spendLabel = "Usar PB em Recursos",
                refundLabel = "Desfazer uso de PB",
                spendEnabled = pcLivres > 0 && recursosPcUsados == 0,
                refundEnabled = recursosPcUsados > 0,
                onSpend = onUsarPontosBonusEmRecursos,
                onRefund = onDesfazerPontosBonusEmRecursos
            )

            Spacer(Modifier.size(8.dp))
        }

        OutlinedTextField(
            value = filter.busca,
            onValueChange = { novo -> filter = filter.copy(busca = novo) },
            label = { Text("Buscar equipamentos") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.FilterAlt,
                    contentDescription = "Filtros",
                    modifier = Modifier.clickable { showFilterDialog = true }
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
        )

        Text(
            text = if (filter.isEmpty()) "Filtrar equipamentos"
            else "Filtros (${filter.totalSelections()})",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clickable { showFilterDialog = true }
        )

        if (!filter.isEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                if (filter.busca.isNotBlank()) {
                    AssistChip(
                        onClick = { filter = filter.copy(busca = "") },
                        label = { Text("Busca: ${filter.busca}") },
                        leadingIcon = { Icon(Icons.Default.Close, contentDescription = "Limpar busca") }
                    )
                }

                if (filter.somenteAcessiveis) {
                    AssistChip(
                        onClick = { filter = filter.copy(somenteAcessiveis = false) },
                        label = { Text("Somente acessíveis") },
                        leadingIcon = { Icon(Icons.Default.Close, contentDescription = "Remover filtro") }
                    )
                }

                filter.origens.forEach { origem ->
                    AssistChip(
                        onClick = {
                            val nova = filter.origens.toMutableSet().apply { remove(origem) }
                            filter = filter.copy(origens = nova)
                        },
                        label = { Text("Origem: $origem") },
                        leadingIcon = { Icon(Icons.Default.Close, contentDescription = "Remover origem") }
                    )
                }

                filter.tipos.forEach { tipo ->
                    AssistChip(
                        onClick = {
                            val nova = filter.tipos.toMutableSet().apply { remove(tipo) }
                            filter = filter.copy(tipos = nova)
                        },
                        label = { Text("Tipo: $tipo") },
                        leadingIcon = { Icon(Icons.Default.Close, contentDescription = "Remover tipo") }
                    )
                }

                filter.subtipos.forEach { subtipo ->
                    AssistChip(
                        onClick = {
                            val nova = filter.subtipos.toMutableSet().apply { remove(subtipo) }
                            filter = filter.copy(subtipos = nova)
                        },
                        label = { Text("Subtipo: $subtipo") },
                        leadingIcon = { Icon(Icons.Default.Close, contentDescription = "Remover subtipo") }
                    )
                }

                AssistChip(
                    onClick = { filter = EquipFilter(busca = filter.busca) },
                    label = { Text("Limpar filtros") },
                    leadingIcon = { Icon(Icons.Default.Close, contentDescription = "Limpar filtros") }
                )
            }
        }

        if (showFilterDialog) {

            val allCategoriasVisiveis = (categorias + superequipCategorias)
                .filterNot {
                    it.tipo.equals("Equipamento Supers", true) ||
                            it.tipo.equals("Equipamentos Supers", true)
                }

            val allTipos = allCategoriasVisiveis.map { it.tipo }.distinct()
            val allSubtipos = allCategoriasVisiveis.map { it.subtipo }.distinct()

            val allOrigens = (categorias + superequipCategorias)
                .map { it.origem?.ifBlank { "BASICO" } ?: "BASICO" }
                .map { it.uppercase() }
                .distinct()

            EquipFilterDialog(
                allOrigens = allOrigens,
                allTipos = allTipos,
                allSubtipos = allSubtipos,
                current = filter,
                onChange = { filter = it },
                onDismiss = { showFilterDialog = false }
            )
        }

        if (equipamentosFiltrados.isNotEmpty()) {
            Text(
                text = "Resultados (${equipamentosFiltrados.size})",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 320.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp)
            ) {
                equipamentosFiltrados.forEach { (equipamento, categoria) ->
                    EquipamentoListItem(
                        equipamento = equipamento,
                        categoria = categoria,
                        onClick = { onEquipamentoDoubleClick(equipamento) },
                        allowLongTexts = allowLongTexts,
                        expanded = detalhesExpandidos[equipamento.nome] == true,
                        onToggleDetails = {
                            val current = detalhesExpandidos[equipamento.nome] ?: false
                            detalhesExpandidos[equipamento.nome] = !current
                        },
                        showOriginalName = showOfficialNames
                    )
                }
            }
        } else if (!filter.isEmpty()) {
            Text(
                text = "Nenhum equipamento encontrado com os filtros atuais.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        Spacer(Modifier.padding(vertical = 4.dp))

        if (equipamentosComprados.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                equipamentosComprados.forEach { eq ->
                    AssistChip(
                        onClick = { onRemoveEquipamentoClick(eq) },
                        label = { Text(eq.nome) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remover"
                            )
                        }
                    )
                }
            }
            Spacer(Modifier.padding(vertical = 4.dp))
        }

        val totalWeight = equipamentosComprados
            .mapNotNull {
                (it.peso as? JsonPrimitive)?.content?.replace(",", ".")?.toFloatOrNull()
            }
            .sum()
        val effectiveStrength = if (hasSoldado && soldadoCargaAtivo) {
            if (forcaRaw < 12) forcaRaw + 2 else forcaRaw + 1
        } else {
            forcaRaw
        }
        val baseLimit = ((effectiveStrength - 2) / 2) * 10f
        val limit = baseLimit + if (hasMusculoso) 10f else 0f

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Peso total: ${"%.1f".format(totalWeight)} / ${"%.1f".format(limit)}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            if (hasSoldado) {
                AssistChip(
                    onClick = onToggleSoldadoCarga,
                    label = {
                        Text(
                            if (soldadoCargaAtivo) "Bônus Soldado ativo" else "Bônus Soldado inativo"
                        )
                    }
                )
            }
        }

        if (compendioSciFiAtivo && sciFiCategorias.isNotEmpty()) {
            Spacer(Modifier.padding(vertical = 4.dp))
            CollapsibleSection(
                title = "Equipamento Sci-Fi",
                expanded = expSciFiEquip,
                onToggle = { expSciFiEquip = !expSciFiEquip },
                onToggleFeedback = onUserFeedback
            ) {
                RenderCategoryList(
                    categories = sciFiCategorias,
                    filter = filter,
                    dinheiro = dinheiro,
                    usaRiqueza = usaRiqueza,
                    allowLongTexts = allowLongTexts,
                    detalhesExpandidos = detalhesExpandidos,
                    onEquipamentoDoubleClick = onEquipamentoDoubleClick,
                    showOriginalName = showOfficialNames,
                    onUserFeedback = onUserFeedback
                )
            }
        }

        if (compendioTrilhadorAtivo && trilhadorCategorias.isNotEmpty()) {
            Spacer(Modifier.padding(vertical = 4.dp))
            CollapsibleSection(
                title = "Equipamento Buscatrilha",
                expanded = expTrilhadorEquip,
                onToggle = { expTrilhadorEquip = !expTrilhadorEquip },
                onToggleFeedback = onUserFeedback
            ) {
                RenderCategoryList(
                    categories = trilhadorCategorias,
                    filter = filter,
                    dinheiro = dinheiro,
                    usaRiqueza = usaRiqueza,
                    allowLongTexts = allowLongTexts,
                    detalhesExpandidos = detalhesExpandidos,
                    onEquipamentoDoubleClick = onEquipamentoDoubleClick,
                    showOriginalName = showOfficialNames,
                    onUserFeedback = onUserFeedback
                )
            }
        }

        if (compendioDeadlandsAtivo && deadlandsCategorias.isNotEmpty()) {
            Spacer(Modifier.padding(vertical = 4.dp))
            CollapsibleSection(
                title = "Equipamento de Deadlands",
                expanded = expDeadlandsEquip,
                onToggle = { expDeadlandsEquip = !expDeadlandsEquip },
                onToggleFeedback = onUserFeedback
            ) {
                RenderCategoryList(
                    categories = deadlandsCategorias,
                    filter = filter,
                    dinheiro = dinheiro,
                    usaRiqueza = usaRiqueza,
                    allowLongTexts = allowLongTexts,
                    detalhesExpandidos = detalhesExpandidos,
                    onEquipamentoDoubleClick = onEquipamentoDoubleClick,
                    showOriginalName = showOfficialNames,
                    onUserFeedback = onUserFeedback
                )
            }
        }

        if (showMoneyDialog) {
            AlertDialog(
                onDismissRequest = { showMoneyDialog = false },
                title = { Text("Editar dinheiro") },
                text = {
                    OutlinedTextField(
                        value = dinheiroInput,
                        onValueChange = { novo ->
                            dinheiroInput = novo.filter { it.isDigit() || it == '-' }
                        },
                        label = { Text("Valor em dinheiro") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        val novoValor = dinheiroInput.toIntOrNull()
                        if (novoValor != null) {
                            onEditarDinheiro(novoValor)
                        }
                        showMoneyDialog = false
                    }) {
                        Text("Salvar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showMoneyDialog = false }) { Text("Cancelar") }
                }
            )
        }

        // Renderiza categorias normais
        val tiposNormais = normalCategorias.map { it.tipo }.distinct()
        val expandedTipoMap = remember { mutableStateMapOf<String, Boolean>() }

        tiposNormais.forEach { tipo ->
            if (filter.tipos.isNotEmpty() && tipo !in filter.tipos) return@forEach

            val isTipoExpanded = expandedTipoMap[tipo] ?: false
            CollapsibleSection(
                title = tipo,
                expanded = isTipoExpanded,
                onToggle = {
                    val newState = !isTipoExpanded
                    // Fecha outros se quiser comportamento de acordeão, ou apenas toggle
                    // expandedTipoMap.keys.forEach { expandedTipoMap[it] = false }
                    expandedTipoMap[tipo] = newState
                },
                onToggleFeedback = onUserFeedback
            ) {
                val scroll = rememberScrollState()
                Column(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                        .verticalScroll(scroll)
                        .padding(start = 8.dp, bottom = 8.dp)
                ) {
                    val catsPorTipo = normalCategorias
                        .filter { it.tipo == tipo }
                        .let { list ->
                            if (filter.origens.isNotEmpty())
                                list.filter {
                                    val safeOrigem = it.origem?.ifBlank { "BASICO" }?.uppercase() ?: "BASICO"
                                    safeOrigem in filter.origens
                                }
                            else list
                        }
                    if (catsPorTipo.isEmpty()) return@Column

                    val subtipos = catsPorTipo.map { it.subtipo }.distinct()
                    val expandedSubtipoMap = remember(tipo) {
                        subtipos.associateWith { mutableStateOf(false) }
                    }

                    subtipos.forEach subtiposLoop@{ subtipo ->
                        if (filter.subtipos.isNotEmpty() && subtipo !in filter.subtipos)
                            return@subtiposLoop

                        val isSubExpanded = expandedSubtipoMap.getValue(subtipo).value
                        CollapsibleSection(
                            title = subtipo,
                            expanded = isSubExpanded,
                            onToggle = {
                                expandedSubtipoMap.forEach { (st, stState) ->
                                    stState.value =
                                        if (st == subtipo) !isSubExpanded else false
                                }
                            },
                            onToggleFeedback = onUserFeedback
                        ) {
                            val scroll2 = rememberScrollState()
                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 200.dp)
                                    .verticalScroll(scroll2)
                                    .padding(start = 8.dp, bottom = 8.dp)
                            ) {
                                val catsPorSub = catsPorTipo.filter { it.subtipo == subtipo }
                                val subsub = catsPorSub.mapNotNull { it.subsubtipo }.distinct()
                                val expandedSubsub = remember(tipo, subtipo) {
                                    subsub.associateWith { mutableStateOf(false) }
                                }

                                if (subsub.isEmpty()) {
                                    catsPorSub.forEach { categoriaAtual ->
                                        categoriaAtual.itens
                                            .filter { eq -> eq.ehAcessivel() && eq.combinaBusca() }
                                            .forEach { equipamento ->
                                                EquipamentoListItem(
                                                    equipamento = equipamento,
                                                    categoria = categoriaAtual,
                                                    onClick = {
                                                        onEquipamentoDoubleClick(equipamento)
                                                    },
                                                    allowLongTexts = allowLongTexts,
                                                    expanded = detalhesExpandidos[equipamento.nome] == true,
                                                    onToggleDetails = {
                                                        val current = detalhesExpandidos[equipamento.nome] ?: false
                                                        detalhesExpandidos[equipamento.nome] = !current
                                                    },
                                                    showOriginalName = showOfficialNames
                                                )
                                            }
                                    }
                                } else {
                                    subsub.forEach { ss ->
                                        val isSsExpanded =
                                            expandedSubsub.getValue(ss).value
                                        CollapsibleSection(
                                            title = ss,
                                            expanded = isSsExpanded,
                                            onToggle = {
                                                expandedSubsub.forEach { (s, sState) ->
                                                    sState.value =
                                                        if (s == ss) !isSsExpanded else false
                                                }
                                            },
                                            onToggleFeedback = onUserFeedback
                                        ) {
                                            val scroll3 = rememberScrollState()
                                            Column(
                                                Modifier
                                                    .fillMaxWidth()
                                                    .heightIn(max = 200.dp)
                                                    .verticalScroll(scroll3)
                                                    .padding(
                                                        start = 8.dp,
                                                        bottom = 8.dp
                                                    )
                                            ) {
                                                catsPorSub
                                                    .filter { it.subsubtipo == ss }
                                                    .forEach { categoriaAtual ->
                                                        categoriaAtual.itens
                                                            .filter { eq -> eq.ehAcessivel() && eq.combinaBusca() }
                                                            .forEach { equipamento ->
                                                                EquipamentoListItem(
                                                                    equipamento = equipamento,
                                                                    categoria = categoriaAtual,
                                                                    onClick = {
                                                                        onEquipamentoDoubleClick(
                                                                            equipamento
                                                                        )
                                                                    },
                                                                    allowLongTexts = allowLongTexts,
                                                                    expanded = detalhesExpandidos[equipamento.nome] == true,
                                                                    onToggleDetails = {
                                                                        val current = detalhesExpandidos[equipamento.nome] ?: false
                                                                        detalhesExpandidos[equipamento.nome] = !current
                                                                    },
                                                                    showOriginalName = showOfficialNames
                                                                )
                                                            }
                                                    }
                                              }
                                          }
                                }
                            }
                        }
                        }
                        Spacer(Modifier.padding(vertical = 2.dp))
                    }
                }
            }
            Spacer(Modifier.padding(vertical = 4.dp))
        }

        // Seção Especial: Equipamento de Fantasia
        if (compendioFantasiaAtivo && fantasiaCategorias.isNotEmpty()) {
            Spacer(Modifier.padding(vertical = 4.dp))
            CollapsibleSection(
                title = "Equipamento de Fantasia",
                expanded = expFantasiaEquip,
                onToggle = { expFantasiaEquip = !expFantasiaEquip },
                onToggleFeedback = onUserFeedback
            ) {
                RenderCategoryList(
                    categories = fantasiaCategorias,
                    filter = filter,
                    dinheiro = dinheiro,
                    usaRiqueza = usaRiqueza,
                    allowLongTexts = allowLongTexts,
                    detalhesExpandidos = detalhesExpandidos,
                    onEquipamentoDoubleClick = onEquipamentoDoubleClick,
                    showOriginalName = showOfficialNames,
                    onUserFeedback = onUserFeedback
                )
            }
        }

        // Seção Especial: Equipamento de Horror
        if (compendioHorrorAtivo && horrorCategorias.isNotEmpty()) {
            Spacer(Modifier.padding(vertical = 4.dp))
            CollapsibleSection(
                title = "Equipamento de Horror",
                expanded = expHorrorEquip,
                onToggle = { expHorrorEquip = !expHorrorEquip },
                onToggleFeedback = onUserFeedback
            ) {
                RenderCategoryList(
                    categories = horrorCategorias,
                    filter = filter,
                    dinheiro = dinheiro,
                    usaRiqueza = usaRiqueza,
                    allowLongTexts = allowLongTexts,
                    detalhesExpandidos = detalhesExpandidos,
                    onEquipamentoDoubleClick = onEquipamentoDoubleClick,
                    showOriginalName = showOfficialNames,
                    onUserFeedback = onUserFeedback
                )
            }
        }

        if (compendioCidadeSolVaporAtivo && cidadeSolVaporCategorias.isNotEmpty()) {
            Spacer(Modifier.padding(vertical = 4.dp))
            CollapsibleSection(
                title = "Equipamento – Cidade do Sol a Vapor",
                expanded = expCidadeSolVaporEquip,
                onToggle = { expCidadeSolVaporEquip = !expCidadeSolVaporEquip },
                onToggleFeedback = onUserFeedback
            ) {
                RenderCategoryList(
                    categories = cidadeSolVaporCategorias,
                    filter = filter,
                    dinheiro = dinheiro,
                    usaRiqueza = usaRiqueza,
                    allowLongTexts = allowLongTexts,
                    detalhesExpandidos = detalhesExpandidos,
                    onEquipamentoDoubleClick = onEquipamentoDoubleClick,
                    showOriginalName = showOfficialNames,
                    onUserFeedback = onUserFeedback
                )
            }
        }

        val supCatsFiltradas = superequipCategorias.let { list ->
            if (filter.origens.isNotEmpty()) {
                list.filter { (it.origem?.uppercase() ?: "") in filter.origens }
            } else {
                list
            }
        }

        if (supCatsFiltradas.isNotEmpty()) {
            CollapsibleSection(
                title = "Superequipamentos",
                expanded = expSuperequip,
                onToggle = { expSuperequip = !expSuperequip },
                onToggleFeedback = onUserFeedback
            ) {
                val scrollSup = rememberScrollState()
                Column(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                        .verticalScroll(scrollSup)
                        .padding(start = 8.dp, bottom = 8.dp)
                ) {
                    supCatsFiltradas.forEach { categoriaAtual ->
                        categoriaAtual.itens
                            .filter { eq -> eq.ehAcessivel() && eq.combinaBusca() }
                            .forEach { equipamento ->
                                EquipamentoListItem(
                                    equipamento = equipamento,
                                    categoria = categoriaAtual,
                                    onClick = { onEquipamentoDoubleClick(equipamento) },
                                    allowLongTexts = allowLongTexts,
                                    expanded = detalhesExpandidos[equipamento.nome] == true,
                                    onToggleDetails = {
                                        val current = detalhesExpandidos[equipamento.nome] ?: false
                                        detalhesExpandidos[equipamento.nome] = !current
                                    },
                                    showOriginalName = showOfficialNames
                                )
                            }
                    }
                }
            }
        }
    }
}

@Composable
fun EquipamentoListItem(
    equipamento: EquipamentoItem,
    categoria: EquipamentoCategoria? = null,
    onClick: () -> Unit,
    allowLongTexts: Boolean,
    expanded: Boolean,
    onToggleDetails: () -> Unit,
    showOriginalName: Boolean = false
) {
    val resumo = equipamento.toResumo()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    if (showOriginalName && !equipamento.originalName.isNullOrBlank()) equipamento.originalName else equipamento.nome,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                resumo.custo?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            listOfNotNull(
                resumo.linhaArma,
                resumo.linhaGeral,
                resumo.linhaVeiculo,
            ).forEach { linha ->
                Text(
                    linha,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            resumo.observacao?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            val origemTag = categoria?.origem?.ifBlank { "BASICO" }
                ?: equipamento.origem?.ifBlank { "BASICO" }
            val infoTags = listOfNotNull(
                categoria?.tipo,
                categoria?.subtipo ?: equipamento.subtipo,
                categoria?.subsubtipo ?: equipamento.subsubtipo,
                origemTag?.let { "Origem: ${it.replace('_', ' ')}" }
            )

            if (infoTags.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    infoTags.forEach { tag ->
                        AssistChip(
                            onClick = {},
                            enabled = false,
                            label = { Text(tag) }
                        )
                    }
                }
            }

            val detalhes = buildList {
                equipamento.observacoes.contentString()?.let { add("Observações: $it") }
                equipamento.pmf.contentString()?.let { add("PMF: $it") }
                equipamento.malfuncionamento.contentString()?.let { add("Malfuncionamento: $it") }
                equipamento.forcaMin.contentString()?.let { add("Força mínima: $it") }
                equipamento.distancia.contentString()?.let { add("Distância: $it") }
                equipamento.dano.contentString()?.let { add("Dano: $it") }
                equipamento.tiros.contentString()?.let { add("Tiros: $it") }
                equipamento.tamanho.contentString()?.let { add("Tamanho: $it") }
                equipamento.manobrabilidade.contentString()?.let { add("Manobrabilidade: $it") }
                equipamento.velMaxima.contentString()?.let { add("Velocidade Máx.: $it") }
                equipamento.resistencia.contentString()?.let { add("Resistência: $it") }
                equipamento.tripulacao.contentString()?.let { add("Tripulação: $it") }
                equipamento.tensao?.let { add("Tensão: $it") }
                equipamento.mods_slots?.let { add("Slots de Mods: $it") }
            }

            if (allowLongTexts && detalhes.isNotEmpty()) {
                Spacer(Modifier.height(2.dp))
                TextButton(
                    onClick = onToggleDetails,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        if (expanded) "Ocultar detalhes" else "Ver detalhes",
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                AnimatedVisibility(visible = expanded) {
                    Column(Modifier.padding(top = 4.dp)) {
                        detalhes.forEach { linha ->
                            Text(linha, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

private fun JsonElement?.contentString(): String? = this
    ?.jsonPrimitive
    ?.contentOrNull
    ?.takeIf { it.isNotBlank() }
