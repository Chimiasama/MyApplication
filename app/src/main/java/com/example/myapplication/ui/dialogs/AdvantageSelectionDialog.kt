package com.example.myapplication.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.CriadorState
import com.example.myapplication.listaVantagens
import com.example.myapplication.model.Vantagem

@Composable
fun AdvantageSelectionDialog(
    state: CriadorState,
    onSelect: (Vantagem) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Escolha sua Vantagem") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                listaVantagens
                    .filter { state.podeSelecionar(it) }
                    .forEach { vant ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(vant) }
                                .padding(vertical = 8.dp, horizontal = 4.dp)
                        ) {
                            // substitui “vant.nivel” por “vant.requisitos.estagio”
                            Text("${vant.nome} (${vant.requisitos.estagio})", Modifier.weight(1f))
                        }
                        HorizontalDivider()
                    }
            }
        },
        confirmButton = { /* não usado – fechamos a lista ao clicar numa vantagem */ },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
