package com.example.myapplication.ui.sections

import android.content.Context
import android.os.Build
import androidx.annotation.RawRes
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MenuAnchorType
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
import com.example.myapplication.R
import com.example.myapplication.SectionCard
import com.example.myapplication.SectionHeader
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class RacialModifierLite(
    val nome: String
    // demais campos do JSON são ignorados propositalmente
)

private val jsonRelaxed = Json { ignoreUnknownKeys = true }

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun AncestralidadesSection(
    onOpenListaAncestralidadesDetail: () -> Unit,
    onSelectAncestralidade: (String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    val ctx = androidx.compose.ui.platform.LocalContext.current
    val showLista = ctx.resources.getBoolean(R.bool.show_lista_completa)

    val ancestralidades by remember {
        mutableStateOf(loadAncestralidadesNomes(context, R.raw.listaancestralidade))
    }

    var expSection by rememberSaveable { mutableStateOf(false) }
    var expMenu    by rememberSaveable { mutableStateOf(false) }
    var showHelp   by rememberSaveable { mutableStateOf(false) }
    var selected by rememberSaveable { mutableStateOf<String?>("HUMANOS") }

    SectionCard(
        title    = "Ancestralidades",
        expanded = expSection,
        onToggle = { expSection = !expSection },
        icon     = Icons.AutoMirrored.Filled.MenuBook
    ) {
        // Cabeçalho no mesmo padrão das outras seções
        SectionHeader(
            onHelpClick   = { showHelp = true },
            centerText    = "Ancestralidade: ${selected ?: "-"}",
            onCenterClick = null,
            onListaCompletaClick = if (showLista) onOpenListaAncestralidadesDetail else null,
            listaCompletaText = "Lista Completa"
        )



        Spacer(Modifier.height(8.dp))

        // Campo read-only com texto PRETO — usa o mesmo composable já existente no projeto
        ExposedDropdownMenuBox(
            expanded         = expMenu,
            onExpandedChange = { expMenu = !expMenu }
        ) {
            // ⚠️ Usa o TransparentOutlinedReadOnlyField QUE JÁ EXISTE na Complicações.
            // Não declare outro com o mesmo nome — isso que causava "Conflicting overloads".
            TransparentOutlinedReadOnlyField(
                text     = selected ?: "Selecione uma ancestralidade",
                enabled  = true,
                onClick  = { expMenu = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
            )

            // Menu suspenso — limita altura; nada de verticalScroll interno (evita crash).
            ExposedDropdownMenu(
                expanded         = expMenu,
                onDismissRequest = { expMenu = false },
                modifier         = Modifier.heightIn(max = 300.dp)
            ) {
                ancestralidades.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item.nome) },
                        onClick = {
                            selected = item.nome
                            expMenu  = false
                            onSelectAncestralidade(item.nome)
                        }
                    )
                }
            }
        }

        if (showHelp) {
            AlertDialog(
                onDismissRequest = { showHelp = false },
                title            = { Text("Sobre Ancestralidades") },
                text             = {
                    Text(
                        "Escolha uma ancestralidade. Os efeitos e cálculos são aplicados no personagem. " +
                                "Para textos descritivos, use “Lista completa”."
                    )
                },
                confirmButton    = {
                    TextButton(onClick = { showHelp = false }) { Text("OK") }
                }
            )
        }
    }
}

// -------- Helpers --------

private fun loadAncestralidadesNomes(
    context: Context,
    @RawRes rawId: Int
): List<RacialModifierLite> {
    val jsonStr = context.resources.openRawResource(rawId).bufferedReader().use { it.readText() }
    return jsonRelaxed.decodeFromString(jsonStr)
}
