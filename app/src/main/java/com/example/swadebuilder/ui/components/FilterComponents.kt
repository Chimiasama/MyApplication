package com.example.swadebuilder.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Bloco de filtro colapsável de um só nível (ex.: "Estágio", "Atributos", "Perícias" em
 * VantFilterDialog) — cabeçalho clicável (título + contagem de selecionados + seta) que
 * some/mostra um FlowRow de FilterChip por baixo, em vez da lista vertical de Checkbox+Text
 * que ficava sempre toda aberta (o problema relatado: muitas opções empilhadas, sem como
 * recolher, além do Checkbox quadrado destoar do resto do app, que já usa FilterChip
 * arredondado em toda outra lista de opções). Reaproveita CollapsibleSection pelo mesmo
 * motivo de qualquer outra lista expansível do app (grupos de Equipamentos/Vantagens na tela
 * principal já usam o mesmo componente).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterChipGroup(
    title: String,
    options: List<String>,
    selected: Set<String>,
    onToggle: (String) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    counts: Map<String, Int> = emptyMap()
) {
    if (options.isEmpty()) return

    val displayTitle = if (selected.isEmpty()) title else "$title (${selected.size})"
    CollapsibleSection(
        title = displayTitle,
        expanded = expanded,
        onToggle = { onExpandedChange(!expanded) }
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            options.forEach { opt ->
                val count = counts[opt]
                FilterChip(
                    selected = opt in selected,
                    onClick = { onToggle(opt) },
                    label = { Text(if (count != null) "$opt ($count)" else opt) }
                )
            }
        }
    }
}

/**
 * Bloco de filtro colapsável de dois níveis (categoria + subseções — ex.: EquipFilterDialog,
 * "Armas" com as subseções "Corpo a Corpo"/"Ataque a Distância"/etc.): a própria categoria
 * ainda é um Checkbox (selecionar ela sozinha já filtra por tudo dentro dela, comportamento
 * preservado), mas as subseções agora ficam num FlowRow de FilterChip que só aparece expandido
 * — antes ficavam sempre todas abertas ao mesmo tempo, pra toda categoria, o que é o que
 * tornava a lista gigante e difícil de navegar com muitas categorias ativas ao mesmo tempo
 * (vários livros/módulos ligados).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterCategoryGroup(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    subOptions: List<String>,
    selectedSubOptions: Set<String>,
    onToggleSub: (String) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    enabled = subOptions.isNotEmpty(),
                    role = Role.Button,
                    onClick = { onExpandedChange(!expanded) }
                )
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Checkbox(checked = checked, onCheckedChange = onCheckedChange)
                Spacer(Modifier.size(4.dp))
                Text(title, fontWeight = FontWeight.SemiBold)
                if (selectedSubOptions.isNotEmpty()) {
                    Text(
                        " (${selectedSubOptions.size})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            if (subOptions.isNotEmpty()) {
                Icon(
                    imageVector = if (expanded) Icons.Default.Remove else Icons.Default.Add,
                    contentDescription = null
                )
            }
        }
        if (expanded && subOptions.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 40.dp, bottom = 8.dp)
            ) {
                subOptions.forEach { sub ->
                    FilterChip(
                        selected = sub in selectedSubOptions,
                        onClick = { onToggleSub(sub) },
                        label = { Text(sub, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
        }
    }
}
