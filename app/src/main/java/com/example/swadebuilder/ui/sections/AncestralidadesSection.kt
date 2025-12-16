package com.example.swadebuilder.ui.sections

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
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
import com.example.swadebuilder.loadRawText
import com.example.swadebuilder.model.loadJsonAsset
import com.example.swadebuilder.ui.components.SectionCard
import com.example.swadebuilder.ui.components.SectionHeader
import com.example.swadebuilder.util.semAcentos
import kotlinx.serialization.Serializable
import com.example.swadebuilder.model.RacialModifier
import com.example.swadebuilder.CriadorState

@Serializable
data class RacialModifierLite(
    val nome: String,
    val originalName: String? = null
)

private const val ASSET_ANCESTRALIDADES = "listaancestralidade.json"

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun AncestralidadesSection(
    state: CriadorState,
    currentAncestralidade: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    supersLocked: Boolean,
    ancestralidadeEmFoco: String?,
    onSelectAncestralidade: (String) -> Unit
) {
    val context = LocalContext.current
    val allowLongTexts = booleanResource(R.bool.enable_long_texts)
    val detalhesExpandidos = remember { mutableStateMapOf<String, Boolean>() }

    val descricaoPorAncestralidade = remember(allowLongTexts) {
        if (!allowLongTexts) {
            emptyMap()
        } else {
            parseAncestralidadeDescriptions(loadRawText(context, R.raw.ancestralidades))
        }
    }

    val compendioFantasiaAtivo = state.compendioFantasiaAtivo
    val compendioTrilhadorAtivo = state.compendioTrilhadorAtivo
    val compendioDeadlandsAtivo = state.compendioDeadlandsAtivo
    val compendioArteDaGuerraAtivo = state.compendioArteDaGuerraAtivo

    val ancestralidadesState = remember(compendioFantasiaAtivo, compendioTrilhadorAtivo, compendioDeadlandsAtivo, compendioArteDaGuerraAtivo) {
        // Load legacy list
        val allLegacy = context.loadJsonAsset<List<RacialModifier>>(ASSET_ANCESTRALIDADES)

        // Load Trilhador list
        val allTrilhador = try {
            context.loadJsonAsset<List<RacialModifier>>("ancestralidades_trilhador.json")
        } catch (e: Exception) {
            emptyList()
        }

        val allDeadlands = try {
            context.loadJsonAsset<List<RacialModifier>>("ancestralidades_deadlands.json")
        } catch (e: Exception) {
            emptyList()
        }

        val allAdg = try {
            context.loadJsonAsset<List<RacialModifier>>("ancestralidades_adg.json")
        } catch (e: Exception) {
            emptyList()
        }

        val all = allLegacy + allTrilhador + allDeadlands + allAdg

        val filtered = all.filter {
            val origin = it.origem?.uppercase() ?: "BASICO"
            origin == "BASICO" || (origin == "FANTASIA" && compendioFantasiaAtivo) || (origin == "FANTASIA_TRILHADOR" && compendioTrilhadorAtivo) || (origin == "DEADLANDS" && compendioDeadlandsAtivo) || (origin == "ARTE_DA_GUERRA" && compendioArteDaGuerraAtivo)
        }.map {
            RacialModifierLite(it.nome, it.originalName)
        }
        mutableStateOf(filtered)
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
            listaCompletaText = ""
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
                    val itemKey = item.nome.uppercase().semAcentos()
                    val isSelected = itemKey == selectedKey.value
                    val descricao = descricaoPorAncestralidade[itemKey].orEmpty()

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        tonalElevation = if (isSelected) 4.dp else 0.dp,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    ) {
                        Column(
                            modifier = Modifier
                                .clickable(enabled = !supersLocked) {
                                    if (supersLocked) return@clickable
                                    selectedKey.value = itemKey
                                    onSelectAncestralidade(item.nome)
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = null,
                                    enabled = !supersLocked
                                )

                                Spacer(Modifier.padding(start = 8.dp))

                                Column(Modifier.weight(1f)) {
                                    val displayName = if (state.modoOficialAtivo && !item.originalName.isNullOrBlank()) {
                                        item.originalName
                                    } else {
                                        item.nome
                                    }

                                    Text(
                                        text = displayName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            if (allowLongTexts && descricao.isNotBlank()) {
                                Spacer(Modifier.height(6.dp))
                                TextButton(
                                    onClick = {
                                        val current = detalhesExpandidos[itemKey] ?: false
                                        detalhesExpandidos[itemKey] = !current
                                    },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = MaterialTheme.colorScheme.primary
                                    ),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        if (detalhesExpandidos[itemKey] == true) "Ocultar detalhes" else "Ver detalhes",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }

                                AnimatedVisibility(visible = detalhesExpandidos[itemKey] == true) {
                                    Text(
                                        text = descricao,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun parseAncestralidadeDescriptions(texto: String): Map<String, String> {
    val linhas = texto.lines()
    val mapa = mutableMapOf<String, StringBuilder>()

    var tituloAtual = ""

    for (linha in linhas) {
        val limpa = linha.trim()
        if (limpa.isBlank()) continue

        val isTitulo = limpa.all { it.isUpperCase() || it == '-' || it == ':' || it.isWhitespace() }
        if (isTitulo) {
            tituloAtual = limpa.removeSuffix(":").uppercase().semAcentos()
            mapa.putIfAbsent(tituloAtual, StringBuilder())
        } else if (tituloAtual.isNotBlank()) {
            mapa.getValue(tituloAtual).appendLine(limpa)
        }
    }

    return mapa.mapValues { it.value.toString().trim() }
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
            .clickable(enabled) { onClick() },
        trailingIcon = trailingIcon,
        singleLine = true
    )
}
