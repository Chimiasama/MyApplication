package com.example.swadebuilder.ui.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun ConfirmActionDialog(
    title: String,
    text: String,
    confirmText: String = "Confirmar",
    dismissText: String = "Cancelar",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onCancel: (() -> Unit)? = null // Optional third button
) {
    AlertDialog(
        onDismissRequest = { onCancel?.invoke() ?: onDismiss() },
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        }
    )
}
