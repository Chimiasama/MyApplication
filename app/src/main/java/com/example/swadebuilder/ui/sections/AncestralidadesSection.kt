package com.example.swadebuilder.ui.sections

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.R
import com.example.swadebuilder.model.Ancestralidade
import com.example.swadebuilder.model.loadJsonAsset
import com.example.swadebuilder.ui.components.AncestralidadeListItem
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
    supersLocked: Boolean,
    ancestralidadeEmFoco: String?,
    onSelectAncestralidade: (String) -> Unit
) {
    val context = LocalContext.current
    val showLista = booleanResource(R.bool.show_full_descriptions)

    val ancestralidadesState = remember {
        mutableStateOf(
            context.loadJsonAsset<List<Ancestralidade>>(ASSET_ANCESTRALIDADES)
        )
    }

    val selectedKey = rememberSaveable(currentAncestralidade) {
        mutableStateOf(
            currentAncestralidade.uppercase().semAcentos().ifBlank { "HUMANOS" }
        )
    }

    val selectedDisplayName =
        ancestralidadesState.value.firstOrNull { item ->
            item.nome.uppercase().semAcentos() == selectedKey.value
        }?.nome ?: "HUMANOS"

    val focoKey = ancestralidadeEmFoco
        ?.uppercase()
        ?.semAcentos()
        ?.takeIf { it.isNotBlank() }

    val listaOrdenada =
        if (focoKey != null) {
            val (foco, resto) = ancestralidadesState.value.partition {
                it.nome.uppercase().semAcentos() == focoKey
            }
            foco + resto
        } else {
            ancestralidadesState.value
        }

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
            onListaCompletaClick = null,
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
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp)
            ) {
                items(listaOrdenada) { item ->
                    AncestralidadeListItem(
                        ancestralidade = item,
                        isClickable = !supersLocked,
                        onClick = {
                            if (supersLocked) return@AncestralidadeListItem
                            selectedKey.value = item.nome.uppercase().semAcentos()
                            onSelectAncestralidade(item.nome)
                        }
                    )
                }
            }
        }
    }
}
