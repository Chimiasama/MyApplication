package com.example.swadebuilder.ui.dialogs

// imports
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.listaVantagens
import com.example.swadebuilder.model.Vantagem

/* ===========================================================
   1) Dialog GENÉRICO para seleção (single/multi)
   =========================================================== */
@Composable
private fun <T> SelectDialog(
    title: String,
    items: List<T>,
    singleSelection: Boolean,
    initialSelection: List<T> = emptyList(),
    maxSelections: Int = Int.MAX_VALUE,
    label: (T) -> String,
    enabled: (T) -> Boolean = { true },
    onConfirm: (List<T>) -> Unit,
    onDismiss: () -> Unit
) {
    val selected = remember { mutableStateListOf<T>().apply { addAll(initialSelection) } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 320.dp)
            ) {
                items(items) { item ->
                    val isEnabled = enabled(item)
                    val isSelected = selected.contains(item)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = isEnabled) {
                                if (singleSelection) {
                                    selected.clear()
                                    selected.add(item)
                                } else {
                                    if (isSelected) selected.remove(item)
                                    else if (selected.size < maxSelections) selected.add(item)
                                }
                            }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (singleSelection) {
                            RadioButton(selected = isSelected, onClick = null, enabled = isEnabled)
                        } else {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = if (isEnabled) { checked ->
                                    if (checked && !isSelected && selected.size < maxSelections) selected.add(item)
                                    else if (!checked && isSelected) selected.remove(item)
                                } else null
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = label(item),
                            modifier = if (!isEnabled) Modifier.alpha(0.5f) else Modifier
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = selected.isNotEmpty(),
                onClick = { onConfirm(selected.toList()) }
            ) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

/* ===========================================================
   2) MANTÉM o nome/assinatura ORIGINAL: AdvantageSelectionDialog
      (usa o genérico por baixo)
   =========================================================== */
@Composable
fun AdvantageSelectionDialog(
    state: CriadorState,
    onSelect: (Vantagem) -> Unit,
    onDismiss: () -> Unit
) {
    val candidatas = listaVantagens.filter { state.podeSelecionar(it) }

    SelectDialog(
        title = "Escolha sua Vantagem",
        items = candidatas,
        singleSelection = true,
        label = { v -> "${v.nome} (${v.requisitos.estagio})" },
        enabled = { _ -> true },
        onConfirm = { sel -> onSelect(sel.first()) },
        onDismiss = onDismiss
    )
}

/* ===========================================================
   3) MANTÉM o nome/assinatura ORIGINAL: MultipleSelectionDialog
      (checkbox com limite)
   =========================================================== */
@Composable
fun MultipleSelectionDialog(
    title: String,
    options: List<String>,
    maxSelections: Int,
    onConfirm: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    SelectDialog(
        title = title,
        items = options,
        singleSelection = false,
        maxSelections = maxSelections,
        label = { it },
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

/* ===========================================================
   4) MANTÉM o nome/assinatura ORIGINAL: ChoiceDialog
      (seleção única simples; sem depender de DropdownField)
   =========================================================== */
@Composable
fun ChoiceDialog(
    options: List<String>,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    SelectDialog(
        title = "Escolha uma opção",
        items = options,
        singleSelection = true,
        label = { it },
        onConfirm = { sel -> onConfirm(sel.first()) },
        onDismiss = onDismiss
    )
}
