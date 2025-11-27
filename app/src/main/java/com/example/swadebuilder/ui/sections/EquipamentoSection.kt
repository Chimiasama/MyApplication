package com.example.swadebuilder.ui.sections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CollapsibleSection
import com.example.swadebuilder.model.EquipamentoCategoria
import com.example.swadebuilder.model.EquipamentoItem
import com.example.swadebuilder.ui.components.SectionCard
import com.example.swadebuilder.ui.components.SectionHeader
import kotlinx.serialization.json.JsonPrimitive

data class EquipFilter(
    val somenteAcessiveis: Boolean = false,
    val origens: Set<String> = emptySet(),
    val tipos: Set<String> = emptySet(),
    val subtipos: Set<String> = emptySet()
) {
    fun totalSelections() =
        (if (somenteAcessiveis) 1 else 0) +
                origens.size + tipos.size + subtipos.size

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

@Composable
fun EquipamentoSection(
    dinheiro: Int,
    pcTotal: Int,
    pcLivres: Int,
    recursosPcUsados: Int,
    expanded: Boolean,
    onToggle: () -> Unit,
    onUsarPontosBonusEmRecursos: () -> Unit,
    onDesfazerPontosBonusEmRecursos: () -> Unit,
    onListaCompletaClick: () -> Unit,
    onEquipamentoDoubleClick: (EquipamentoItem) -> Unit,
    equipamentosComprados: List<EquipamentoItem>,
    onRemoveEquipamentoClick: (EquipamentoItem) -> Unit,
    categorias: List<EquipamentoCategoria>,
    superequipCategorias: List<EquipamentoCategoria>
) {
    var expSuperequip by rememberSaveable { mutableStateOf(false) }

    var filter by remember { mutableStateOf(EquipFilter()) }
    var showFilterDialog by rememberSaveable { mutableStateOf(false) }
    val showLista = booleanResource(com.example.swadebuilder.R.bool.show_lista_completa)

    SectionCard(
        title    = "Equipamento",
        expanded = expanded,
        onToggle = onToggle,
        icon     = Icons.Default.ShoppingCart
    ) {
        if (!expanded) return@SectionCard

        SectionHeader(
            onHelpClick = null,
            centerText = "Dinheiro: $dinheiro • Pontos Bônus: $pcLivres de $pcTotal",
            onCenterClick = null,
            onListaCompletaClick = if (showLista) onListaCompletaClick else null,
            listaCompletaText = "Lista Completa"
        )

        Spacer(modifier = Modifier.size(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val podeUsarPcRecursos = pcLivres > 0 && recursosPcUsados == 0
            val podeDesfazerPcRecursos = recursosPcUsados > 0

            TextButton(
                onClick = onUsarPontosBonusEmRecursos,
                enabled = podeUsarPcRecursos
            ) {
                Text("Usar PB em Recursos")
            }

            TextButton(
                onClick = onDesfazerPontosBonusEmRecursos,
                enabled = podeDesfazerPcRecursos
            ) {
                Text("Desfazer uso de PB")
            }
        }

        if (pcTotal == 0) {
            Spacer(Modifier.size(4.dp))
            Text(
                text = "Para ganhar Pontos Bônus, escolha Complicações na seção apropriada.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        Spacer(Modifier.size(8.dp))
        Text(
            text = if (filter.isEmpty()) "Filtrar equipamentos"
            else "Filtros (${filter.totalSelections()})",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clickable { showFilterDialog = true }
        )

        if (showFilterDialog) {

            val allCategoriasVisiveis = (categorias + superequipCategorias)
                .filterNot {
                    it.tipo.equals("Equipamento Supers", true) ||
                            it.tipo.equals("Equipamentos Supers", true)
                }

            val allTipos = allCategoriasVisiveis.map { it.tipo }.distinct()
            val allSubtipos = allCategoriasVisiveis.map { it.subtipo }.distinct()

            val allOrigens = (categorias.mapNotNull { it.origem } +
                    superequipCategorias.mapNotNull { it.origem })
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
        Text(
            "Peso total: $totalWeight",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        val allCategorias = (categorias + superequipCategorias)
            .filterNot {
                it.tipo.equals("Equipamento Supers", true) ||
                        it.tipo.equals("Equipamentos Supers", true)
            }

        val tipos = allCategorias.map { it.tipo }.distinct()

        val expandedTipoMap = remember { tipos.associateWith { mutableStateOf(false) } }

        tipos.forEach { tipo ->
            if (filter.tipos.isNotEmpty() && tipo !in filter.tipos) return@forEach

            val isTipoExpanded = expandedTipoMap.getValue(tipo).value
            CollapsibleSection(
                title = tipo,
                expanded = isTipoExpanded,
                onToggle = {
                    expandedTipoMap.forEach { (t, st) ->
                        st.value = if (t == tipo) !isTipoExpanded else false
                    }
                }
            ) {
                val scroll = rememberScrollState()
                Column(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                        .verticalScroll(scroll)
                        .padding(start = 8.dp, bottom = 8.dp)
                ) {
                    val catsPorTipo = allCategorias
                        .filter { it.tipo == tipo }
                        .let { list ->
                            if (filter.origens.isNotEmpty())
                                list.filter {
                                    (it.origem?.uppercase() ?: "") in filter.origens
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
                            }
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
                                    catsPorSub
                                        .flatMap { it.itens }
                                        .filter { eq ->
                                            if (filter.somenteAcessiveis) {
                                                val c = (eq.custo as? JsonPrimitive)
                                                    ?.content?.toIntOrNull()
                                                    ?: Int.MAX_VALUE
                                                if (c > dinheiro) return@filter false
                                            }
                                            true
                                        }
                                        .forEach { equipamento ->
                                            Row(
                                                Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        onEquipamentoDoubleClick(equipamento)
                                                    }
                                                    .padding(
                                                        vertical = 4.dp,
                                                        horizontal = 4.dp
                                                    ),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    equipamento.nome,
                                                    Modifier.weight(1f),
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                                Text(
                                                    equipamento.custo.toString(),
                                                    style = MaterialTheme.typography.bodyMedium
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
                                            }
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
                                                    .flatMap { it.itens }
                                                    .filter { eq ->
                                                        if (filter.somenteAcessiveis) {
                                                            val c =
                                                                (eq.custo as? JsonPrimitive)
                                                                    ?.content?.toIntOrNull()
                                                                    ?: Int.MAX_VALUE
                                                            if (c > dinheiro) return@filter false
                                                        }
                                                        true
                                                    }
                                                    .forEach { equipamento ->
                                                        Row(
                                                            Modifier
                                                                .fillMaxWidth()
                                                                .clickable {
                                                                    onEquipamentoDoubleClick(equipamento)
                                                                }
                                                                .padding(
                                                                    vertical = 4.dp,
                                                                    horizontal = 4.dp
                                                                ),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text(
                                                                equipamento.nome,
                                                                Modifier.weight(1f),
                                                                style = MaterialTheme.typography.bodyMedium
                                                            )
                                                            Text(
                                                                equipamento.custo.toString(),
                                                                style = MaterialTheme.typography.bodyMedium
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

        val supCatsFiltradas = (superequipCategorias).let { list ->
            val byOrigem = if (filter.origens.isNotEmpty())
                list.filter { (it.origem?.uppercase() ?: "") in filter.origens }
            else list
            byOrigem
        }

        if (supCatsFiltradas.isNotEmpty()) {
            CollapsibleSection(
                title = "Superequipamentos",
                expanded = expSuperequip,
                onToggle = { expSuperequip = !expSuperequip }
            ) {
                val scrollSup = rememberScrollState()
                Column(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                        .verticalScroll(scrollSup)
                        .padding(start = 8.dp, bottom = 8.dp)
                ) {
                    supCatsFiltradas
                        .flatMap { it.itens }
                        .filter { eq ->
                            if (filter.somenteAcessiveis) {
                                val c = (eq.custo as? JsonPrimitive)
                                    ?.content?.toIntOrNull() ?: Int.MAX_VALUE
                                if (c > dinheiro) return@filter false
                            }
                            true
                        }
                        .forEach { equipamento ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onEquipamentoDoubleClick(equipamento)
                                    }
                                    .padding(vertical = 4.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    equipamento.nome,
                                    Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    equipamento.custo.toString(),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                }
            }
        }
    }
}
