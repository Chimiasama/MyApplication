package com.example.swadebuilder.ui.sections

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.R
import com.example.swadebuilder.SectionCard
import com.example.swadebuilder.SectionHeader
import com.example.swadebuilder.model.loadJsonAsset
import kotlinx.serialization.Serializable

@Serializable
data class RacialModifierLite(
    val nome: String
)

private const val ASSET_ANCESTRALIDADES = "listaancestralidade.json"

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun AncestralidadesSection(
    currentAncestralidade: String,
    supersLocked: Boolean, // <- trava da fase de supers
    onOpenListaAncestralidadesDetail: () -> Unit,
    onSelectAncestralidade: (String) -> Unit
) {
    val context = LocalContext.current
    val showLista = booleanResource(R.bool.show_lista_completa)

    // lista do assets (é lida no dropdown)
    val ancestralidadesState = remember {
        mutableStateOf(
            context.loadJsonAsset<List<RacialModifierLite>>(ASSET_ANCESTRALIDADES)
        )
    }

    // estados explícitos (MutableState)
    val expSection = rememberSaveable { mutableStateOf(false) }
    val expMenu    = rememberSaveable { mutableStateOf(false) }
    val selected   = rememberSaveable(currentAncestralidade) {
        mutableStateOf(currentAncestralidade.ifBlank { "HUMANOS" })
    }

    SectionCard(
        title = "Ancestralidades",
        expanded = expSection.value,
        onToggle = { expSection.value = !expSection.value },
        icon = Icons.AutoMirrored.Filled.MenuBook
    ) {
        val centerLabel = if (supersLocked) {
            "Ancestralidade: ${selected.value} (travado na fase Supers)"
        } else {
            "Ancestralidade: ${selected.value}"
        }

        SectionHeader(
            onHelpClick = null,
            centerText = centerLabel,
            onCenterClick = null,
            onListaCompletaClick = if (showLista) onOpenListaAncestralidadesDetail else null,
            listaCompletaText = "Lista Completa"
        )

        Spacer(Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expMenu.value,
            onExpandedChange = {
                if (!supersLocked) {
                    expMenu.value = !expMenu.value
                }
            }
        ) {
            TransparentOutlinedReadOnlyField(
                text = selected.value,
                enabled = !supersLocked, // desabilita o campo quando travado
                onClick = {
                    if (!supersLocked) {
                        expMenu.value = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expMenu.value)
                }
            )

            ExposedDropdownMenu(
                expanded = expMenu.value,
                onDismissRequest = { expMenu.value = false },
                modifier = Modifier.heightIn(max = 300.dp)
            ) {
                ancestralidadesState.value.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item.nome) },
                        onClick = {
                            if (!supersLocked) {
                                selected.value = item.nome
                                expMenu.value = false
                                onSelectAncestralidade(item.nome)
                            }
                        }
                    )
                }
            }
        }

    }
}

@Composable
fun TransparentOutlinedReadOnlyField(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingIcon: (@Composable (() -> Unit))? = null
) {
    androidx.compose.material3.OutlinedTextField(
        value = text,
        onValueChange = {},
        readOnly = true,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .then(Modifier)
            .clickable(enabled) { onClick() },
        trailingIcon = trailingIcon,
        singleLine = true
        // mantenha aqui suas cores/shape/estilo atuais, se tiver
    )
}
