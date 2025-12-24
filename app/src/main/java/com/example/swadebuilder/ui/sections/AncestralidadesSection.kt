package com.example.swadebuilder.ui.sections

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.LaunchedEffect
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
import com.example.swadebuilder.ui.components.ExpandableSearchFilter
import com.example.swadebuilder.ui.components.SectionCard
import com.example.swadebuilder.EditionConfig
import com.example.swadebuilder.ui.components.SectionHeader
import com.example.swadebuilder.util.toEditionDisplayName
import com.example.swadebuilder.util.semAcentos
import kotlinx.serialization.Serializable
import com.example.swadebuilder.model.RacialModifier
import com.example.swadebuilder.CriadorState

@Serializable
data class RacialModifierLite(
    val nome: String,
    val displayName: String,
    val originalName: String? = null,
    val descricao: String? = null,
    val aliases: Set<String> = emptySet()
)

private const val ASSET_ANCESTRALIDADES = "listaancestralidade.json"

private fun RacialModifierLite.displayName(showOfficialNames: Boolean): String {
    return if (showOfficialNames && !originalName.isNullOrBlank()) {
        originalName
    } else {
        displayName
    }
}

private data class RacialAbilitySignature(
    val nome: String,
    val descricao: String
)

private data class RacialSignature(
    val atributos: Map<String, Int>,
    val pericias: Map<String, Int>,
    val vantagensGratis: List<String>,
    val desvantagens: List<String>,
    val habilidades: List<RacialAbilitySignature>
)

private fun RacialModifier.signature(): RacialSignature {
    fun normalizeList(values: List<String>): List<String> {
        return values.sortedBy { it.uppercase().semAcentos() }
    }

    return RacialSignature(
        atributos = atributos,
        pericias = pericias,
        vantagensGratis = normalizeList(vantagensGratis),
        desvantagens = normalizeList(desvantagens),
        habilidades = habilidades
            .map { RacialAbilitySignature(it.nome, it.descricao) }
            .sortedWith(compareBy({ it.nome.uppercase().semAcentos() }, { it.descricao.uppercase().semAcentos() }))
    )
}

private fun stripScenarioSuffix(nome: String): String {
    return nome.replace(Regex("\\s*\\([^)]*\\)\\s*$"), "").trim()
}

private fun adjustBuscatrilhaName(nome: String): String {
    return nome.replace("Trilhador", "Buscatrilha")
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun AncestralidadesSection(
    state: CriadorState,
    currentAncestralidade: String,
    supersLocked: Boolean,
    ancestralidadeEmFoco: String?,
    onSelectAncestralidade: (String) -> Unit,
    onUserFeedback: () -> Unit
) {
    val context = LocalContext.current
    val allowLongTexts = booleanResource(R.bool.enable_long_texts)
    val detalhesExpandidos = remember { mutableStateMapOf<String, Boolean>() }

    val showOfficialNames = EditionConfig.isFullEdition && state.modoOficialAtivo

    val compendioFantasiaAtivo = state.compendioFantasiaAtivo
    val compendioBuscatrilhaAtivo = state.compendioBuscatrilhaAtivo
    val compendioDeadlandsAtivo = state.compendioDeadlandsAtivo
    val compendioArteDaGuerraAtivo = state.compendioArteDaGuerraAtivo
    val compendioWiseguysAtivo = state.compendioWiseguysAtivo
    val compendioCidadeSolVaporAtivo = state.compendioCidadeSolVaporAtivo
    val compendioHorrorAtivo = state.compendioHorrorAtivo
    val compendioSciFiAtivo = state.compendioSciFiAtivo
    val compendioCrystalHeartAtivo = state.compendioCrystalHeartAtivo

    val ancestralidadesState = androidx.compose.runtime.produceState(
        initialValue = emptyList<RacialModifierLite>(),
        compendioFantasiaAtivo,
        compendioBuscatrilhaAtivo,
        compendioDeadlandsAtivo,
        compendioArteDaGuerraAtivo,
        compendioWiseguysAtivo,
        compendioCidadeSolVaporAtivo,
        compendioHorrorAtivo,
        compendioSciFiAtivo,
        compendioCrystalHeartAtivo
    ) {
        val all = context.loadJsonAsset<List<RacialModifier>>(ASSET_ANCESTRALIDADES)

        val activeOrigins = buildList {
            if (compendioArteDaGuerraAtivo) add("ARTE_DA_GUERRA")
            if (compendioFantasiaAtivo) add("FANTASIA")
            if (compendioBuscatrilhaAtivo) add("FANTASIABUSCATRILHA")
            if (compendioDeadlandsAtivo) add("OESTE_ESTRANHO")
            if (compendioCidadeSolVaporAtivo) add("CIDADE_SOL_VAPOR")
            if (compendioWiseguysAtivo) add("WISEGUYS")
            if (compendioHorrorAtivo) add("HORROR")
            if (compendioSciFiAtivo) add("SCI_FI")
            if (compendioCrystalHeartAtivo) add("CRYSTAL_HEART")
        }

        val allowedOrigins = when (activeOrigins.size) {
            0 -> setOf("BASICO")
            1 -> setOf(activeOrigins.first())
            else -> (activeOrigins + "BASICO").toSet()
        }

        val filtered = all.filter {
            val origin = it.origem?.uppercase() ?: "BASICO"

            origin in allowedOrigins
        }

        val deduped = filtered
            .groupBy { it.signature() }
            .values
            .map { group ->
                val representative = group.first()
                val hasMultipleOrigins = group.map { (it.origem ?: "BASICO").uppercase() }.toSet().size > 1
                val baseDisplayName = if (hasMultipleOrigins) {
                    stripScenarioSuffix(representative.nome)
                } else {
                    representative.nome
                }
                val adjustedName = adjustBuscatrilhaName(representative.nome)
                val displayName = adjustBuscatrilhaName(baseDisplayName)
                val originalName = if (EditionConfig.isFullEdition && !hasMultipleOrigins) {
                    representative.originalName
                } else {
                    null
                }
                val aliasKeys = group
                    .map { adjustBuscatrilhaName(it.nome).uppercase().semAcentos() }
                    .toSet()
                RacialModifierLite(
                    nome = adjustedName,
                    displayName = displayName.toEditionDisplayName(),
                    originalName = originalName,
                    descricao = representative.descricao,
                    aliases = aliasKeys
                )
            }

        value = deduped
    }

    val selectedKey = rememberSaveable { mutableStateOf("") }

    LaunchedEffect(ancestralidadesState.value, currentAncestralidade) {
        val available = ancestralidadesState.value
        val currentKey = currentAncestralidade.uppercase().semAcentos()

        val preferredItem = when {
            currentKey.isNotBlank() -> available.firstOrNull { it.aliases.contains(currentKey) }
            else -> null
        } ?: available.firstOrNull { it.nome.contains("Humano", ignoreCase = true) }
            ?: available.firstOrNull()

        preferredItem?.let { item ->
            val key = item.nome.uppercase().semAcentos()
            if (selectedKey.value != key) {
                selectedKey.value = key
            }

            if (item.nome != currentAncestralidade) {
                onSelectAncestralidade(item.nome)
            }
        }
    }

    val selectedDisplayName =
        ancestralidadesState.value.firstOrNull { item ->
            item.nome.uppercase().semAcentos() == selectedKey.value
        }?.displayName(showOfficialNames) ?: "HUMANOS"

    val focoKey = ancestralidadeEmFoco
        ?.uppercase()
        ?.semAcentos()
        ?.takeIf { it.isNotBlank() }

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var isSearchExpanded by rememberSaveable { mutableStateOf(false) }

    val listaBase = ancestralidadesState.value

    val listaFiltrada = remember(listaBase, searchQuery) {
        if (searchQuery.isBlank()) listaBase
        else listaBase.filter {
            it.nome.semAcentos().contains(searchQuery.semAcentos(), ignoreCase = true) ||
            (it.descricao?.semAcentos()?.contains(searchQuery.semAcentos(), ignoreCase = true) == true)
        }
    }

    val listaOrdenada = remember(listaFiltrada, focoKey) {
        if (focoKey != null) {
            val (foco, resto) = listaFiltrada.partition {
                it.nome.uppercase().semAcentos() == focoKey
            }
            foco + resto
        } else {
            listaFiltrada
        }
    }

    SectionCard(
        title = "Ancestralidades",
        icon = Icons.AutoMirrored.Filled.MenuBook,
        showHeader = false
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

        ExpandableSearchFilter(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            isExpanded = isSearchExpanded,
            onExpandedChange = { isSearchExpanded = it },
            placeholder = "Pesquisar Ancestralidades..."
        )

        Spacer(Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
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
                    .fillMaxHeight(),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp)
            ) {
                items(listaOrdenada) { item ->
                    val itemKey = item.nome.uppercase().semAcentos()
                    val isSelected = itemKey == selectedKey.value
                    val descricao = item.descricao.orEmpty()

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
                                onUserFeedback()
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
                                    val displayName = item.displayName(showOfficialNames)

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
