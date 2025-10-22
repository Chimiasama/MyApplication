package com.example.swadebuilder.ui.dialogs

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@ExperimentalMaterial3Api
@Composable
fun MultipleSelectionDialog(
    title: String,
    options: List<String>,
    maxSelections: Int,
    onConfirm: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    val selectedItems = remember { mutableStateListOf<String>() }

    AlertDialog(
        onDismissRequest = onDismiss,

        containerColor    = Color.Transparent,
        titleContentColor = Color.Black,
        textContentColor  = Color.Black,

        title = {
            Text(title, color = Color.Black)
        },
        text = {
            LazyColumn(
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
            ) {
                items(options) { option ->
                    val isSelected = option in selectedItems
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isSelected) selectedItems.remove(option)
                                else if (selectedItems.size < maxSelections)
                                    selectedItems.add(option)
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { checked ->
                                if (checked && selectedItems.size < maxSelections)
                                    selectedItems.add(option)
                                else if (!checked)
                                    selectedItems.remove(option)
                            }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(option, color = Color.Black)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(selectedItems.toList()) },
                enabled = selectedItems.isNotEmpty()
            ) {
                Text("OK", color = Color.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color.Black)
            }
        }
    )
}
