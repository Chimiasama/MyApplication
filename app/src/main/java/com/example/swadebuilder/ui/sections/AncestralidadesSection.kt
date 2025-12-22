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
    val originalName: String? = null
)

private const val ASSET_ANCESTRALIDADES = "listaancestralidade.json"

private fun RacialModifierLite.displayName(showOfficialNames: Boolean): String {
    return if (showOfficialNames && !originalName.isNullOrBlank()) {
        originalName
    } else {
        displayName
    }
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

    val descricaoPorAncestralidade = remember(allowLongTexts) {
        if (!allowLongTexts) {
            emptyMap()
        } else {
            parseAncestralidadeDescriptions(loadRawText(context, R.raw.ancestralidades))
        }
    }

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

    val apenasArteDaGuerra = compendioArteDaGuerraAtivo &&
            !compendioFantasiaAtivo &&
            !compendioBuscatrilhaAtivo &&
            !compendioDeadlandsAtivo &&
            !compendioCidadeSolVaporAtivo &&
            !compendioWiseguysAtivo &&
            !compendioHorrorAtivo &&
            !compendioSciFiAtivo &&
            !compendioCrystalHeartAtivo

    val ancestralidadesState = remember(
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

        val filtered = all.filter {
            val origin = it.origem?.uppercase() ?: "BASICO"

            when (origin) {
                "BASICO" -> !apenasArteDaGuerra
                "ARTE_DA_GUERRA" -> compendioArteDaGuerraAtivo
                "FANTASIA" -> compendioFantasiaAtivo
                "FANTASIABUSCATRILHA" -> compendioBuscatrilhaAtivo
                "DEADLANDS" -> compendioDeadlandsAtivo
                "CIDADE_SOL_VAPOR" -> compendioCidadeSolVaporAtivo
                "WISEGUYS" -> compendioWiseguysAtivo
                "SCI_FI" -> compendioSciFiAtivo
                "CRYSTAL_HEART" -> compendioCrystalHeartAtivo
                "HORROR" -> compendioHorrorAtivo
                else -> false
            }
        }.map {
            val buscatrilhaName = it.nome.replace("Trilhador", "Buscatrilha")
            val originalName = if (EditionConfig.isFullEdition) it.originalName else null
            RacialModifierLite(
                nome = buscatrilhaName,
                displayName = buscatrilhaName.toEditionDisplayName(),
                originalName = originalName
            )
        }
        mutableStateOf(filtered)
    }

    val selectedKey = rememberSaveable { mutableStateOf("") }

    LaunchedEffect(ancestralidadesState.value, currentAncestralidade) {
        val available = ancestralidadesState.value
        val availableKeys = available.map { it.nome.uppercase().semAcentos() }
        val currentKey = currentAncestralidade.uppercase().semAcentos()

        val preferredKey = when {
            currentKey.isNotBlank() && availableKeys.contains(currentKey) -> currentKey
            else -> {
                available.firstOrNull { it.nome.contains("Humano", ignoreCase = true) }
                    ?.nome
                    ?.uppercase()
                    ?.semAcentos()
                    ?: availableKeys.firstOrNull()
            }
        }

        preferredKey?.let { key ->
            if (selectedKey.value != key) {
                selectedKey.value = key
            }

            available.firstOrNull { it.nome.uppercase().semAcentos() == key }?.nome?.let { nome ->
                if (nome != currentAncestralidade) {
                    onSelectAncestralidade(nome)
                }
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
