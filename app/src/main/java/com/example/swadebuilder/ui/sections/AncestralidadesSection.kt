package com.example.swadebuilder.ui.sections

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.EditionConfig
import com.example.swadebuilder.R
import com.example.swadebuilder.model.RacialModifier
import com.example.swadebuilder.model.loadJsonAsset
import com.example.swadebuilder.ui.components.ExpandableSearchFilter
import com.example.swadebuilder.ui.components.SectionCard
import com.example.swadebuilder.ui.components.SectionHeader
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.semAcentos
import com.example.swadebuilder.util.titleCase
import com.example.swadebuilder.util.toEditionDisplayName
import kotlinx.serialization.Serializable

@Serializable
data class RacialAbilityLite(
    val nome: String,
    val descricao: String
)

@Serializable
data class RacialModifierLite(
    val nome: String,
    val displayName: String,
    val originalName: String? = null,
    val descricao: String? = null,
    val aliases: Set<String> = emptySet(),
    val origens: Set<String> = emptySet(),
    val habilidades: List<RacialAbilityLite> = emptyList(),
    val atributos: Map<String, Int> = emptyMap(),
    val pericias: Map<String, Int> = emptyMap(),
    val vantagensGratis: List<String> = emptyList(),
    val desvantagens: List<String> = emptyList()
)

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
        val all = com.example.swadebuilder.listaAncestralidadesJson
        val pathfinderLabel = context.getString(R.string.sw_pathfinder_label)

        fun adjustName(nome: String): String {
            if (compendioBuscatrilhaAtivo) {
                 return nome.replace(" (Trilhador)", "")
                    .replace(" (Buscatrilha)", "")
                    .replace(" (Pathfinder)", "")
            }
            return nome.replace("Trilhador", pathfinderLabel)
                .replace("Buscatrilha", pathfinderLabel)
                .replace("Pathfinder", pathfinderLabel)
        }

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
            1 -> {
                val origin = activeOrigins.first()
                if (origin == "FANTASIA") setOf(origin, "BASICO") else setOf(origin)
            }
            else -> (activeOrigins + "BASICO").toSet()
        }

        val filtered = all.filter {
            val origin = it.origem?.uppercase() ?: "BASICO"
            val key = it.nome.keyify()

            // Logic for Fantasy Compendium exclusions
            if (compendioFantasiaAtivo) {
                if (key == "ANDROIDES") return@filter false
                if (origin == "BASICO" && (key == "CELESTIAIS" || key == "GUARDIOES")) return@filter false
            }

            // Logic for Lite Edition exclusions
            if (!EditionConfig.isFullEdition && origin == "BASICO" && (key == "CELESTIAIS" || key == "GUARDIOES")) {
                return@filter false
            }

            origin in allowedOrigins
        }

        val deduped = filtered
            .groupBy { it.signature() }
            .values
            .map { group ->
                val representative = group.first()
                val originsInGroup = group.map { (it.origem ?: "BASICO").uppercase() }.toSet()
                val hasMultipleOrigins = originsInGroup.size > 1

                val baseDisplayName = if (hasMultipleOrigins) {
                    stripScenarioSuffix(representative.nome)
                } else {
                    representative.nome
                }
                val adjustedName = adjustName(representative.nome)
                val displayName = adjustName(baseDisplayName)
                val originalName = if (EditionConfig.isFullEdition && !hasMultipleOrigins) {
                    representative.originalName
                } else {
                    null
                }
                val aliasKeys = group
                    .map { adjustName(it.nome).uppercase().semAcentos() }
                    .toSet()

                val habilidadesLite = representative.habilidades.map {
                    RacialAbilityLite(it.nome, it.descricao)
                }

                RacialModifierLite(
                    nome = representative.nome,
                    displayName = displayName.toEditionDisplayName(),
                    originalName = originalName,
                    descricao = representative.descricao,
                    aliases = aliasKeys,
                    origens = originsInGroup,
                    habilidades = habilidadesLite,
                    atributos = representative.atributos,
                    pericias = representative.pericias,
                    vantagensGratis = representative.vantagensGratis,
                    desvantagens = representative.desvantagens
                )
            }.sortedBy { it.nome }

        value = deduped
    }

    val selectedKey = rememberSaveable { mutableStateOf("") }

    LaunchedEffect(ancestralidadesState.value, currentAncestralidade) {
        val available = ancestralidadesState.value
        val currentKey = currentAncestralidade.uppercase().semAcentos()

        val humanSearch = if (compendioBuscatrilhaAtivo) "Humano (Buscatrilha)" else "Humanos"
        val fallbackHuman = if (compendioBuscatrilhaAtivo) "Humanos" else null

        val preferredItem = when {
            currentKey.isNotBlank() -> available.firstOrNull {
                it.nome.keyify() == currentKey || it.aliases.contains(currentKey)
            }
            else -> null
        } ?: available.firstOrNull { it.nome.equals(humanSearch, ignoreCase = true) }
          ?: fallbackHuman?.let { fallback -> available.firstOrNull { it.nome.equals(fallback, ignoreCase = true) } }
          ?: available.firstOrNull { it.nome.contains("Humano", ignoreCase = true) }
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
        }?.displayName(showOfficialNames)?.titleCase() ?: "Humanos"

    val focoKey = ancestralidadeEmFoco
        ?.uppercase()
        ?.semAcentos()
        ?.takeIf { it.isNotBlank() }

    val listaBase = ancestralidadesState.value

    val listaOrdenada = remember(listaBase, focoKey) {
        if (focoKey != null) {
            val (foco, resto) = listaBase.partition {
                it.nome.uppercase().semAcentos() == focoKey
            }
            foco + resto
        } else {
            listaBase
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

        Spacer(Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (supersLocked) 0.3f else 1f)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(listaOrdenada) { item ->
                    val itemKey = item.nome.uppercase().semAcentos()
                    val isSelected = itemKey == selectedKey.value
                    val descricao = item.descricao.orEmpty()
                    val hasDescription = descricao.isNotBlank() || item.habilidades.isNotEmpty()

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !supersLocked) {
                                if (supersLocked) return@clickable
                                onUserFeedback()
                                selectedKey.value = itemKey
                                onSelectAncestralidade(item.nome)
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    val displayName = item.displayName(showOfficialNames).titleCase()

                                    Text(
                                        text = displayName,
                                        style = MaterialTheme.typography.bodyMedium, // Smaller font
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selecionado",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            if (isSelected && item.origens.contains("ARTE_DA_GUERRA") && item.nome.contains("Humano", ignoreCase = true)) {
                                Spacer(Modifier.height(8.dp))
                                Text("Signo de Nascença:", style = MaterialTheme.typography.labelMedium)

                                var expanded by remember { mutableStateOf(false) }

                                Box {
                                    OutlinedButton(onClick = { expanded = true }) {
                                        Text(state.signoAdgSelecionado ?: "Selecionar Signo")
                                    }
                                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                        CriadorState.SIGNOS_ADG.forEach { signo ->
                                            DropdownMenuItem(
                                                text = { Text(signo) },
                                                onClick = {
                                                    state.selecionarSigno(signo)
                                                    expanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                val selectedSign = state.signoAdgSelecionado
                                if (selectedSign != null) {
                                    Spacer(Modifier.height(4.dp))
                                    val signDesc = CriadorState.SIGNOS_ADG_DESC[selectedSign]
                                    if (signDesc != null && EditionConfig.isFullEdition) {
                                        Text(
                                            text = signDesc,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    if (selectedSign.equals("Serpente", ignoreCase = true)) {
                                        Spacer(Modifier.height(8.dp))
                                        Text("Perícia Bônus:", style = MaterialTheme.typography.labelMedium)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            com.example.swadebuilder.ui.components.RadioButtonRow(
                                                label = "Jogar",
                                                selected = state.signoSerpentePericiaEscolhida == "Jogar",
                                                onSelect = {
                                                    state.signoSerpentePericiaEscolhida = "Jogar"
                                                    state.rebuildAllPericiaStacks()
                                                }
                                            )
                                            Spacer(Modifier.width(16.dp))
                                            com.example.swadebuilder.ui.components.RadioButtonRow(
                                                label = "Performance",
                                                selected = state.signoSerpentePericiaEscolhida == "Performance",
                                                onSelect = {
                                                    state.signoSerpentePericiaEscolhida = "Performance"
                                                    state.rebuildAllPericiaStacks()
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            if (isSelected && item.nome.keyify().contains("GNOMO") && item.nome.keyify().contains("BUSCATRILHA")) {
                                Spacer(Modifier.height(8.dp))
                                Text("Perícia Obsessiva (Astúcia):", style = MaterialTheme.typography.labelMedium)

                                var expanded by remember { mutableStateOf(false) }
                                val smartsSkills = state.periciasFiltradasPorCompendio
                                    .filter {
                                        val key = it.nome.keyify()
                                        it.atributo == "ASTUCIA" &&
                                        !key.contains("IDIOMAS") &&
                                        (!compendioBuscatrilhaAtivo || (key != "ALQUIMIA" && key != "CIENCIA ESTRANHA"))
                                    }
                                    .sortedBy { it.nome }

                                Box {
                                    OutlinedButton(onClick = { expanded = true }) {
                                        Text(state.gnomoPericiaEscolhida ?: "Selecionar Perícia")
                                    }
                                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                        smartsSkills.forEach { skill ->
                                            DropdownMenuItem(
                                                text = { Text(skill.nome) },
                                                onClick = {
                                                    state.selecionarPericiaGnomo(skill.nome)
                                                    expanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            if (isSelected && item.nome.keyify() == "DESCENDENTE ELEMENTAL") {
                                Spacer(Modifier.height(8.dp))
                                Text("Herança Elemental:", style = MaterialTheme.typography.labelMedium)

                                var expanded by remember { mutableStateOf(false) }
                                val options = listOf("Ar", "Água", "Fogo", "Terra")

                                Box {
                                    OutlinedButton(onClick = { expanded = true }) {
                                        Text(state.descendenteElementalSelecionado ?: "Selecionar Elemento")
                                    }
                                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                        options.forEach { elem ->
                                            DropdownMenuItem(
                                                text = { Text(elem) },
                                                onClick = {
                                                    state.selecionarDescendenteElemental(elem)
                                                    expanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            if (allowLongTexts && hasDescription) {
                                Spacer(Modifier.height(4.dp))
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
                                        style = MaterialTheme.typography.labelSmall, // Smaller font for button
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                AnimatedVisibility(visible = detalhesExpandidos[itemKey] == true) {
                                    Column(modifier = Modifier.padding(top = 4.dp)) {
                                        // Description
                                        if (descricao.isNotBlank()) {
                                            Text(
                                                text = descricao,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(Modifier.height(8.dp))
                                        }

                                        // Attributes
                                        if (item.atributos.isNotEmpty()) {
                                            val attrsText = item.atributos.entries.joinToString(", ") { (k, v) ->
                                                val dieVal = 4 + v
                                                val dieStr = if (dieVal > 12) {
                                                    "d12+${dieVal - 12}"
                                                } else {
                                                    "d$dieVal"
                                                }
                                                "$k $dieStr"
                                            }
                                            Text(
                                                text = "Atributos: $attrsText",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(Modifier.height(4.dp))
                                        }

                                        // Skills
                                        if (item.pericias.isNotEmpty()) {
                                            val skillsText = item.pericias.entries.joinToString(", ") { (k, v) ->
                                                val die = if (v == 0) "d4-2" else "d${(4 + (v - 1) * 2).coerceAtMost(12)}"
                                                "$k $die"
                                            }
                                            Text(
                                                text = "Perícias: $skillsText",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(Modifier.height(4.dp))
                                        }

                                        // Free Advantages
                                        if (item.vantagensGratis.isNotEmpty()) {
                                            val advsText = item.vantagensGratis.joinToString(", ") { it.titleCase() }
                                            Text(
                                                text = "Vantagens: $advsText",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(Modifier.height(4.dp))
                                        }

                                        // Hindrances
                                        if (item.desvantagens.isNotEmpty()) {
                                            val hindsText = item.desvantagens.joinToString(", ") { it.titleCase() }
                                            Text(
                                                text = "Complicações: $hindsText",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(Modifier.height(4.dp))
                                        }

                                        // Abilities
                                        if (item.habilidades.isNotEmpty()) {
                                            Spacer(Modifier.height(4.dp))
                                            item.habilidades.forEach { ability ->
                                                Column(modifier = Modifier.padding(bottom = 4.dp)) {
                                                    Text(
                                                        text = ability.nome,
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                    Text(
                                                        text = ability.descricao,
                                                        style = MaterialTheme.typography.bodySmall,
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
