package com.example.myapplication.ui.sections

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.AncestralidadesContent
import com.example.myapplication.CriadorState
import com.example.myapplication.SectionCard
import com.example.myapplication.SectionHeader

@Composable
fun AncestralidadesSection(
    state: CriadorState,
    onOpenListaAncestralidadesDetail: () -> Unit
) {
    var expAnc by rememberSaveable { mutableStateOf(false) }
    var showHelp by rememberSaveable { mutableStateOf(false) }
    var showBlockAlert by rememberSaveable { mutableStateOf(false) }
    val blockMessages by remember { mutableStateOf(listOf<String>()) }

    SectionCard(
        title    = "Ancestralidades",
        expanded = expAnc,
        onToggle = { expAnc = !expAnc },
        icon     = Icons.AutoMirrored.Filled.MenuBook
    ) {
        SectionHeader(
            onHelpClick          = { showHelp = true },
            centerText           = "Ancestralidade: ${state.ancestralidade}",
            onCenterClick        = null,
            onListaCompletaClick = onOpenListaAncestralidadesDetail,
            listaCompletaText    = "Lista Completa"
        )

        Spacer(Modifier.height(8.dp))

        if (showHelp) {
            AlertDialog(
                onDismissRequest = { showHelp = false },
                title            = { Text("Sobre Ancestralidades") },
                text             = {
                    Text(
                        "Ao selecionar ancestralidades os ajustes são feitos no personagem de forma automática. " +
                                "Veja Lista Completa para saber sobre cada uma."
                    )
                },
                confirmButton    = {
                    TextButton(onClick = { showHelp = false }) {
                        Text("OK")
                    }
                }
            )
        }

        AncestralidadesContent(state)
    }

    if (showBlockAlert) {
        AlertDialog(
            onDismissRequest = { showBlockAlert = false },
            title            = { Text("Não é possível mudar agora") },
            text             = {
                Column {
                    Text("Para trocar de ancestralidade, primeiro:")
                    Spacer(Modifier.height(8.dp))
                    blockMessages.forEach { msg ->
                        Text("• $msg", fontSize = 14.sp)
                    }
                }
            },
            confirmButton    = {
                TextButton(onClick = { showBlockAlert = false }) {
                    Text("OK")
                }
            }
        )
    }
}