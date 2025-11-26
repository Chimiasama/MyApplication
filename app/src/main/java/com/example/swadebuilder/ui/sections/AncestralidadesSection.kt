package com.example.swadebuilder.ui.sections

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.R
import com.example.swadebuilder.model.loadJsonAsset
import com.example.swadebuilder.ui.components.SectionCard
import com.example.swadebuilder.ui.components.SectionHeader
import com.example.swadebuilder.util.semAcentos
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
    expanded: Boolean,
    onToggle: () -> Unit,
    supersLocked: Boolean, // trava da fase de supers
    onOpenListaAncestralidadesDetail: (String) -> Unit,
    onSelectAncestralidade: (String) -> Unit
) {
    val context = LocalContext.current
    val showLista = booleanResource(R.bool.show_lista_completa)

    // lista do assets
    val ancestralidadesState = remember {
        mutableStateOf(
            context.loadJsonAsset<List<RacialModifierLite>>(ASSET_ANCESTRALIDADES)
        )
    }

    // chave normalizada da ancestralidade atual (ex: "ANÕES" -> "ANOES")
    val selectedKey = rememberSaveable(currentAncestralidade) {
        mutableStateOf(
            currentAncestralidade.uppercase().semAcentos().ifBlank { "HUMANOS" }
        )
    }

    // Nome bonitinho pra exibir no cabeçalho
    val selectedDisplayName =
        ancestralidadesState.value.firstOrNull { item ->
            item.nome.uppercase().semAcentos() == selectedKey.value
        }?.nome ?: "HUMANOS"

    SectionCard(
        title = "Ancestralidades",
        expanded = expanded,
        onToggle = onToggle,
        icon = Icons.AutoMirrored.Filled.MenuBook
    ) {
        val centerLabel = if (supersLocked) {
            "Ancestralidade: $selectedDisplayName (travado na fase Supers)"
        } else {
            "Ancestralidade: $selectedDisplayName"
        }

        SectionHeader(
            onHelpClick = null,
            centerText = centerLabel,
            onCenterClick = null,
            onListaCompletaClick = if (showLista) {
                { onOpenListaAncestralidadesDetail("") }
            } else null,
            listaCompletaText = "Lista Completa"
        )

        Spacer(Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 260.dp)
                .alpha(if (supersLocked) 0.3f else 1f)
        ) {
            Text(
                "Lista de ancestralidades disponíveis:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(4.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(ancestralidadesState.value) { item ->
                    val itemKey = item.nome.uppercase().semAcentos()
                    val isSelected = itemKey == selectedKey.value

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                                else
                                    MaterialTheme.colorScheme.surface,
                                shape = MaterialTheme.shapes.small
                            )
                            .clickable(enabled = !supersLocked) {
                                if (supersLocked) return@clickable
                                // grava a chave normalizada
                                selectedKey.value = itemKey
                                // avisa o caller com o nome "bonito"
                                onSelectAncestralidade(item.nome)
                            }
                            .padding(vertical = 8.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = null, // clique tratado na Row
                            enabled = !supersLocked
                        )

                        Spacer(Modifier.padding(start = 8.dp))

                        Text(
                            text = item.nome,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )

                        if (showLista) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = "Detalhes",
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable { onOpenListaAncestralidadesDetail(item.nome) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Overload mantido por compatibilidade com outros pontos do app.
 * (Mesmo que esta seção não use mais dropdown.)
 */
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
            .clickable(enabled) { onClick() },
        trailingIcon = trailingIcon,
        singleLine = true
    )
}
