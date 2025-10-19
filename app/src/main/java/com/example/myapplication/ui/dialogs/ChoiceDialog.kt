package com.example.myapplication.ui.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.myapplication.util.DropdownField

@ExperimentalMaterial3Api
@Composable
fun ChoiceDialog(
    options: List<String>,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selected by rememberSaveable { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Escolha uma opção") },
        text = {
            DropdownField(
                label    = "Selecione…",
                options  = options,
                selected = selected,
                enabled  = true
            ) { choice ->
                selected = choice
            }
        },
        confirmButton = {
            TextButton(
                enabled = selected != null,
                onClick = { onConfirm(selected!!) }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}