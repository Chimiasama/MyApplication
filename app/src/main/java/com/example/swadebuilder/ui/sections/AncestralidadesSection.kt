package com.example.swadebuilder.ui.sections

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.EditionConfig
import com.example.swadebuilder.R
import com.example.swadebuilder.model.AnaoCiberTraitCatalog
import com.example.swadebuilder.model.AnaoCiberTraitSelection
import com.example.swadebuilder.model.Constants
import com.example.swadebuilder.model.RacialModifier
import com.example.swadebuilder.model.canonicalOriginKey
import com.example.swadebuilder.model.getActiveOrigins
import com.example.swadebuilder.model.groupAncestralidadesForDisplay
import com.example.swadebuilder.model.stripAncestralidadeScenarioSuffix
import com.example.swadebuilder.registry.AncestryVariantRegistry
import com.example.swadebuilder.toDiceString
import com.example.swadebuilder.ui.components.SectionCard
import com.example.swadebuilder.ui.components.SectionHeader
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.semAcentos
import com.example.swadebuilder.util.toEditionDisplayName
import com.example.swadebuilder.util.toFancyTitleCase
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
    val desvantagens: List<String> = emptyList(),
    val opcoes: List<String> = emptyList()
)

private fun RacialModifierLite.displayName(showOfficialNames: Boolean): String {
    return if (showOfficialNames && !originalName.isNullOrBlank()) {
        originalName
    } else {
        displayName
    }
}

// RacialSignature/RacialModifier.signature() e o agrupamento por (nome-base, assinatura)
// vivem em com.example.swadebuilder.model.RacialModifier.kt (groupAncestralidadesForDisplay),
// para serem testáveis por unit test puro sem depender do Compose.
private fun stripScenarioSuffix(nome: String): String = stripAncestralidadeScenarioSuffix(nome)

@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun AncestralidadesSection(
    state: CriadorState,
    currentAncestralidade: String,
    supersLocked: Boolean,
    ancestralidadeEmFoco: String?,
    feedbackMessages: MutableList<String>,
    onSelectAncestralidade: (String) -> Unit,
    onUserFeedback: () -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val allowLongTexts = booleanResource(R.bool.enable_long_texts)
    val detalhesExpandidos = remember { mutableStateMapOf<String, Boolean>() }

    val showOfficialNames = EditionConfig.isFullEdition && state.modoOficialAtivo

    val compendioFantasiaAtivo = state.compendioFantasiaAtivo
    val compendioPathfinderAtivo = state.compendioPathfinderAtivo
    val compendioDeadlandsAtivo = state.compendioDeadlandsAtivo
    val compendioArteDaGuerraAtivo = state.compendioArteDaGuerraAtivo
    val compendioWiseguysAtivo = state.compendioWiseguysAtivo
    val compendioCidadeSolVaporAtivo = state.compendioCidadeSolVaporAtivo
    val compendioHorrorAtivo = state.compendioHorrorAtivo
    val compendioSciFiAtivo = state.compendioSciFiAtivo
    val compendioCrystalHeartAtivo = state.compendioCrystalHeartAtivo
    val pathfinderLabel = stringResource(R.string.sw_pathfinder_label)

    val ancestralidadesState = androidx.compose.runtime.produceState(
        initialValue = emptyList<RacialModifierLite>(),
        context,
        configuration,
        pathfinderLabel,
        compendioFantasiaAtivo,
        compendioPathfinderAtivo,
        compendioDeadlandsAtivo,
        compendioArteDaGuerraAtivo,
        compendioWiseguysAtivo,
        compendioCidadeSolVaporAtivo,
        compendioHorrorAtivo,
        compendioSciFiAtivo,
        compendioCrystalHeartAtivo
    ) {
        val all = state.listaAncestralidadesJson

        fun adjustName(nome: String): String {
            if (compendioPathfinderAtivo) {
                 return nome.replace(" (Trilhador)", "")
                    .replace(" (Buscatrilha)", "")
                    .replace(" (Pathfinder)", "")
            }
            return nome.replace("Trilhador", pathfinderLabel)
                .replace("Buscatrilha", pathfinderLabel)
                .replace("Pathfinder", pathfinderLabel)
        }

        val activeOrigins = state.getActiveOrigins()

        val allowedOrigins = if (activeOrigins.isNotEmpty()) {
            activeOrigins
        } else {
            setOf("BASICO")
        }

        val filtered = all.filter {
            val origin = canonicalOriginKey(it.origem)
            val key = it.nome.keyify()

            // Cidade do Sol a Vapor (jogadores): Humanos, Demônios e Meio-Demônios.
            if (compendioCidadeSolVaporAtivo && origin == "CIDADE_SOL_VAPOR") {
                val nomeSemSufixo = stripScenarioSuffix(it.nome).keyify()
                val isHumano = key.startsWith("HUMANO") || nomeSemSufixo == "HUMANO"
                val isDemonio = key.startsWith("DEMONIO") || nomeSemSufixo == "DEMONIO"
                val isMeioDemonio = key.startsWith("MEIO-DEMONIO") || nomeSemSufixo == "MEIO-DEMONIO"
                if (!isHumano && !isDemonio && !isMeioDemonio) return@filter false
            }

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

        // Deduplicação (por nome, priorizando o livro de cenário/companheiro sobre o Básico
        // quando os dois estão ativos; e por nome-base + assinatura mecânica, para tratar
        // variantes de nome da mesma raça entre livros sem fundir raças diferentes que
        // coincidem em mecânica) vive em com.example.swadebuilder.model.RacialModifier.kt
        // (groupAncestralidadesForDisplay), testada por unit test puro.
        val deduped = groupAncestralidadesForDisplay(filtered)
            .map { group ->
                val representative = group.first()
                val originsInGroup = group.map { canonicalOriginKey(it.origem) }.toSet()
                val hasMultipleOrigins = originsInGroup.size > 1

                val baseDisplayName = if (hasMultipleOrigins) {
                    stripScenarioSuffix(representative.nome)
                } else {
                    representative.nome
                }
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
                    RacialAbilityLite(it.nome.toFancyTitleCase(), it.descricao)
                }

                RacialModifierLite(
                    nome = representative.nome,
                    displayName = displayName.toEditionDisplayName().toFancyTitleCase(),
                    originalName = originalName,
                    descricao = representative.descricao,
                    aliases = aliasKeys,
                    origens = originsInGroup,
                    habilidades = habilidadesLite,
                    atributos = representative.atributos,
                    pericias = representative.pericias,
                    vantagensGratis = representative.vantagensGratis,
                    desvantagens = representative.desvantagens,
                    opcoes = representative.opcoes
                )
            }.sortedBy { it.nome }

        value = deduped
    }

    val selectedKey = rememberSaveable { mutableStateOf("") }

    LaunchedEffect(ancestralidadesState.value, currentAncestralidade) {
        val available = ancestralidadesState.value
        val currentKey = currentAncestralidade.uppercase().semAcentos()

        val humanSearch = if (compendioPathfinderAtivo) "Humano (Buscatrilha)" else "Humanos"
        val fallbackHuman = if (compendioPathfinderAtivo) "Humanos" else null

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
        }?.displayName(showOfficialNames)?.toFancyTitleCase() ?: "Humanos"

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
                                    val displayName = item.displayName(showOfficialNames).toFancyTitleCase()

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
                                    val signDesc = if (EditionConfig.isFullEdition) {
                                        CriadorState.SIGNOS_ADG_DESC[selectedSign]
                                    } else {
                                        CriadorState.SIGNOS_ADG_DESC_LITE[selectedSign] ?: CriadorState.SIGNOS_ADG_DESC[selectedSign]
                                    }
                                    if (signDesc != null) {
                                        Text(
                                            text = signDesc,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    if (selectedSign.equals("Serpente", ignoreCase = true)) {
                                        Spacer(Modifier.height(8.dp))
                                        Text("Perícia Bônus:", style = MaterialTheme.typography.labelMedium)
                                        Column {
                                            com.example.swadebuilder.ui.components.RadioButtonRow(
                                                label = "Jogar",
                                                selected = state.signoSerpentePericiaEscolhida == "Jogar",
                                                onSelect = {
                                                    state.signoSerpentePericiaEscolhida = "Jogar"
                                                    state.rebuildAllPericiaStacks()
                                                }
                                            )
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

                            if (isSelected) {
                                val opcoesValidas = item.opcoes

                                val isUmvee = item.nome.keyify().contains("UMVEE")
                                // Seleção (o jogador escolhe entre opções que a própria raça já
                                // oferece, ex.: Terracota Voto/Obrigação) fica sempre visível.
                                // Variante (o mestre reconfigura a raça pro cenário, ex.: Anões
                                // Ciber) só aparece com a regra de livro "Variantes de Raça"
                                // ligada. Raças ainda não migradas pro AncestryVariantRegistry
                                // são tratadas como Variante (todas têm Básico/Padrão entre as
                                // opções, o sinal que o próprio usuário definiu para o caso).
                                val variantConfig = AncestryVariantRegistry.get(item.nome.keyify())
                                val isSelecaoPura = variantConfig != null && variantConfig.grupoVariante == null
                                val showOpcoesPicker = opcoesValidas.size > 1 && (isSelecaoPura || state.optVariantesDeRacaAtivo)
                                if (showOpcoesPicker) {
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        when {
                                            isUmvee -> "Dons da Natureza:"
                                            isSelecaoPura -> "Seleção:"
                                            else -> "Variante:"
                                        },
                                        style = MaterialTheme.typography.labelMedium
                                    )

                                    var expanded by remember { mutableStateOf(false) }

                                    val currentSelection = state.resolveSciFiVariantSelectionFor(
                                        ancestryName = item.nome,
                                        availableOptions = opcoesValidas
                                    ) ?: opcoesValidas.firstOrNull().orEmpty()

                                    Box {
                                        OutlinedButton(onClick = { expanded = true }) {
                                            Text(currentSelection.toFancyTitleCase())
                                        }
                                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                            opcoesValidas.forEach { opt ->
                                                DropdownMenuItem(
                                                    text = { Text(opt.toFancyTitleCase()) },
                                                    onClick = {
                                                        state.selecionarScifiVariant(opt)
                                                        expanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    // Human Miner Attribute Choice
                                    if (item.nome.keyify() == "HUMANOS" && currentSelection == "Minerador") {
                                        Spacer(Modifier.height(8.dp))
                                        val attributeOptions = listOf("Força", "Vigor")
                                        var attributeExpanded by remember { mutableStateOf(false) }
                                        val currentAttributeSelection = state.humanoMineradorAtributo
                                            ?.takeIf { it in attributeOptions }
                                            ?: "Força"
                                        Text("Bônus de Atributo (d6 inicial):", style = MaterialTheme.typography.labelMedium)
                                        Box {
                                            OutlinedButton(onClick = { attributeExpanded = true }) {
                                                Text(currentAttributeSelection.toFancyTitleCase())
                                            }
                                            DropdownMenu(expanded = attributeExpanded, onDismissRequest = { attributeExpanded = false }) {
                                                attributeOptions.forEach { option ->
                                                    DropdownMenuItem(
                                                        text = { Text(option.toFancyTitleCase()) },
                                                        onClick = {
                                                            state.selecionarHumanoMineradorAtributo(option)
                                                            attributeExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Anões Ciber: até 2 pontos de traços raciais negativos (nenhum maior que -2)
                                    if (item.nome.keyify() == "ANOES" && currentSelection == "Ciber") {
                                        Spacer(Modifier.height(12.dp))
                                        val pontosUsados = AnaoCiberTraitCatalog.pontosUsados(state.anaoCiberTracosSelecionados)
                                        Text(
                                            "Traços Raciais Negativos (até 2 pontos, nenhum maior que -2):",
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                        Text(
                                            "Pontos usados: $pontosUsados / ${AnaoCiberTraitCatalog.MAX_PONTOS}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Column {
                                            AnaoCiberTraitCatalog.TRACOS.forEach { trait ->
                                                val selecaoAtual = state.anaoCiberTracosSelecionados.firstOrNull { it.traitId == trait.id }
                                                val marcado = selecaoAtual != null
                                                val custoAbs = -trait.custo
                                                val cabeNoOrcamento = pontosUsados + custoAbs <= AnaoCiberTraitCatalog.MAX_PONTOS
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.alpha(if (marcado || cabeNoOrcamento) 1f else 0.4f)
                                                ) {
                                                    Checkbox(
                                                        checked = marcado,
                                                        enabled = marcado || cabeNoOrcamento,
                                                        onCheckedChange = { checked ->
                                                            val atualizados = if (checked) {
                                                                // Traços paramétricos já entram com um alvo padrão
                                                                // (o primeiro disponível), senão a desvantagem fica
                                                                // "muda" na ficha até o jogador abrir o dropdown.
                                                                val novaSelecao = AnaoCiberTraitSelection(
                                                                    traitId = trait.id,
                                                                    escolhaAtributo = if (trait.exigeEscolhaAtributo) {
                                                                        state.listaAtributos.firstOrNull()
                                                                    } else null,
                                                                    escolhaPericia = if (trait.exigeEscolhaPericia) {
                                                                        state.periciasFiltradasPorCompendio.minByOrNull { it.nome }?.nome
                                                                    } else null
                                                                )
                                                                state.anaoCiberTracosSelecionados + novaSelecao
                                                            } else {
                                                                state.anaoCiberTracosSelecionados.filterNot { it.traitId == trait.id }
                                                            }
                                                            state.selecionarAnaoCiberTracos(atualizados)
                                                        }
                                                    )
                                                    Text("${trait.nome} (${trait.custo})", style = MaterialTheme.typography.bodyMedium)
                                                }

                                                if (marcado && trait.exigeEscolhaAtributo) {
                                                    var atributoExpanded by remember { mutableStateOf(false) }
                                                    val atributoEscolhido = selecaoAtual?.escolhaAtributo ?: state.listaAtributos.firstOrNull().orEmpty()
                                                    Row(
                                                        modifier = Modifier.padding(start = 32.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text("Atributo: ", style = MaterialTheme.typography.bodySmall)
                                                        Box {
                                                            OutlinedButton(onClick = { atributoExpanded = true }) {
                                                                Text(atributoEscolhido)
                                                            }
                                                            DropdownMenu(expanded = atributoExpanded, onDismissRequest = { atributoExpanded = false }) {
                                                                state.listaAtributos.forEach { atributoOpcao ->
                                                                    DropdownMenuItem(
                                                                        text = { Text(atributoOpcao) },
                                                                        onClick = {
                                                                            val atualizados = state.anaoCiberTracosSelecionados.map {
                                                                                if (it.traitId == trait.id) it.copy(escolhaAtributo = atributoOpcao) else it
                                                                            }
                                                                            state.selecionarAnaoCiberTracos(atualizados)
                                                                            atributoExpanded = false
                                                                        }
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }

                                                if (marcado && trait.exigeEscolhaPericia) {
                                                    var periciaExpanded by remember { mutableStateOf(false) }
                                                    val periciasDisponiveis = state.periciasFiltradasPorCompendio.sortedBy { it.nome }
                                                    val periciaEscolhida = selecaoAtual?.escolhaPericia
                                                        ?: periciasDisponiveis.firstOrNull()?.nome.orEmpty()
                                                    Row(
                                                        modifier = Modifier.padding(start = 32.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text("Perícia: ", style = MaterialTheme.typography.bodySmall)
                                                        Box {
                                                            OutlinedButton(onClick = { periciaExpanded = true }) {
                                                                Text(periciaEscolhida)
                                                            }
                                                            DropdownMenu(expanded = periciaExpanded, onDismissRequest = { periciaExpanded = false }) {
                                                                periciasDisponiveis.forEach { periciaOpcao ->
                                                                    DropdownMenuItem(
                                                                        text = { Text(periciaOpcao.nome) },
                                                                        onClick = {
                                                                            val atualizados = state.anaoCiberTracosSelecionados.map {
                                                                                if (it.traitId == trait.id) it.copy(escolhaPericia = periciaOpcao.nome) else it
                                                                            }
                                                                            state.selecionarAnaoCiberTracos(atualizados)
                                                                            periciaExpanded = false
                                                                        }
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

                                if (isFeral) {
                                    Spacer(Modifier.height(8.dp))
                                    Text("Dons da Natureza: Ápice", style = MaterialTheme.typography.labelMedium)
                                    Spacer(Modifier.height(8.dp))
                                    val attributeOptions = listOf("Força", "Vigor", "Agilidade")
                                    var attributeExpanded by remember { mutableStateOf(false) }
                                    val currentAttributeSelection = state.humanoMineradorAtributo
                                        ?.takeIf { it in attributeOptions }
                                        ?: "Força"
                                    Text("Atributo Primitivo (d6 inicial):", style = MaterialTheme.typography.labelMedium)
                                    Box {
                                        OutlinedButton(onClick = { attributeExpanded = true }) {
                                            Text(currentAttributeSelection.toFancyTitleCase())
                                        }
                                        DropdownMenu(expanded = attributeExpanded, onDismissRequest = { attributeExpanded = false }) {
                                            attributeOptions.forEach { option ->
                                                DropdownMenuItem(
                                                    text = { Text(option.toFancyTitleCase()) },
                                                    onClick = {
                                                        state.selecionarHumanoMineradorAtributo(option)
                                                        attributeExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }


                            if (isSelected && item.origens.contains("FANTASIA") && item.nome.contains("Humano", ignoreCase = true)) {
                                Spacer(Modifier.height(8.dp))
                                Text("Pacote Cultural:", style = MaterialTheme.typography.labelMedium)

                                var expanded by remember { mutableStateOf(false) }

                                Box {
                                    OutlinedButton(onClick = { expanded = true }) {
                                        Text(state.pacoteCulturalFantasiaSelecionado)
                                    }
                                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                        CriadorState.PACOTES_CULTURAIS_FANTASIA.forEach { pacote ->
                                            DropdownMenuItem(
                                                text = { Text(pacote) },
                                                onClick = {
                                                    state.selecionarPacoteCulturalFantasia(pacote)
                                                    expanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(Modifier.height(4.dp))
                                val packageDesc = if (EditionConfig.isFullEdition) {
                                    CriadorState.PACOTES_CULTURAIS_FANTASIA_DESC[state.pacoteCulturalFantasiaSelecionado]
                                } else {
                                    CriadorState.PACOTES_CULTURAIS_FANTASIA_DESC_LITE[state.pacoteCulturalFantasiaSelecionado]
                                        ?: CriadorState.PACOTES_CULTURAIS_FANTASIA_DESC[state.pacoteCulturalFantasiaSelecionado]
                                }
                                if (packageDesc != null) {
                                    Text(
                                        text = packageDesc,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (state.pacoteCulturalFantasiaSelecionado == "Povo do Mar") {
                                    Spacer(Modifier.height(8.dp))
                                    Text("Compensação:", style = MaterialTheme.typography.labelMedium)
                                    Column {
                                        com.example.swadebuilder.ui.components.RadioButtonRow(
                                            label = "Penalidade em Cavalgar (-1)",
                                            selected = state.povoDoMarOpcao == "Penalidade em Cavalgar",
                                            onSelect = {
                                                val error = state.selecionarPovoDoMarOpcao("Penalidade em Cavalgar")
                                                if (error != null) android.widget.Toast.makeText(context, error, android.widget.Toast.LENGTH_LONG).show()
                                            }
                                        )
                                        com.example.swadebuilder.ui.components.RadioButtonRow(
                                            label = "Procurado (Maior)",
                                            selected = state.povoDoMarOpcao == "Procurado (Maior)",
                                            onSelect = {
                                                val error = state.selecionarPovoDoMarOpcao("Procurado (Maior)")
                                                if (error != null) android.widget.Toast.makeText(context, error, android.widget.Toast.LENGTH_LONG).show()
                                            }
                                        )
                                    }
                                }

                                if (state.pacoteCulturalFantasiaSelecionado == "Senhores dos Cavalos") {
                                    Spacer(Modifier.height(8.dp))
                                    com.example.swadebuilder.ui.components.CheckboxRow(
                                        label = "Receber Vantagem 'Nascido na Sela'?",
                                        checked = state.senhoresCavalosExtra,
                                        onCheckedChange = {
                                            val error = state.toggleSenhoresCavalosExtra(it)
                                            if (error != null) android.widget.Toast.makeText(context, error, android.widget.Toast.LENGTH_LONG).show()
                                        }
                                    )

                                    if (state.senhoresCavalosExtra) {
                                        Spacer(Modifier.height(4.dp))
                                        Text("Compensação:", style = MaterialTheme.typography.labelMedium)
                                        Column {
                                            com.example.swadebuilder.ui.components.RadioButtonRow(
                                                label = "Código de Honra (Maior)",
                                                selected = state.senhoresCavalosCompensacao == "Código de Honra",
                                                onSelect = {
                                                    val error = state.selecionarSenhoresCavalosCompensacao("Código de Honra")
                                                    if (error != null) android.widget.Toast.makeText(context, error, android.widget.Toast.LENGTH_LONG).show()
                                                }
                                            )
                                            com.example.swadebuilder.ui.components.RadioButtonRow(
                                                label = "Sem Escrúpulos (Menor) e Analfabeto (Menor)",
                                                selected = state.senhoresCavalosCompensacao == "Sem Escrúpulos e Analfabeto",
                                                onSelect = {
                                                    val error = state.selecionarSenhoresCavalosCompensacao("Sem Escrúpulos e Analfabeto")
                                                    if (error != null) android.widget.Toast.makeText(context, error, android.widget.Toast.LENGTH_LONG).show()
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            if (isSelected && state.compendioPathfinderAtivo && item.nome.keyify().contains("GNOMO")) {
                                Spacer(Modifier.height(8.dp))
                                Text("Perícia Obsessiva (Astúcia):", style = MaterialTheme.typography.labelMedium)

                                var expanded by remember { mutableStateOf(false) }
                                val smartsSkills = state.periciasFiltradasPorCompendio
                                    .filter {
                                        val key = it.nome.keyify()
                                        it.atributo == "ASTUCIA" &&
                                        !key.contains("IDIOMAS") &&
                                        (!compendioPathfinderAtivo || (key != "ALQUIMIA" && key != "CIENCIA ESTRANHA"))
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



                            if (isSelected && item.nome.keyify().contains("KITSUNEMIMI")) {
                                Spacer(Modifier.height(8.dp))
                                Text("Perícia Preparada:", style = MaterialTheme.typography.labelMedium)

                                var expanded by remember { mutableStateOf(false) }
                                val allowedSkills = listOf(
                                    "Conhecimento Acadêmico",
                                    "Convenção",
                                    "Intimidar",
                                    "Pesquisar",
                                    "Provocar"
                                )

                                Box {
                                    OutlinedButton(onClick = { expanded = true }) {
                                        Text(state.kitsunemimiPericiaEscolhida ?: "Selecionar Perícia")
                                    }
                                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                        allowedSkills.forEach { skillName ->
                                            DropdownMenuItem(
                                                text = { Text(skillName) },
                                                onClick = {
                                                    state.selecionarPericiaKitsunemimi(skillName)
                                                    expanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            if (isSelected && item.nome.keyify().contains("USAGIMIMI")) {
                                Spacer(Modifier.height(8.dp))
                                Text("Perícia Definida pelo Ofício (d6):", style = MaterialTheme.typography.labelMedium)

                                var expanded by remember { mutableStateOf(false) }
                                val adgSkills = state.listaPericias
                                    .filter {
                                        val key = it.nome.keyify()
                                        it.origem == "ARTE_DA_GUERRA" &&
                                            !key.startsWith("IDIOMAS") &&
                                            !key.startsWith("JUTSU")
                                    }
                                    .map { it.nome }
                                    .distinctBy { it.keyify() }
                                    .sortedBy { it }

                                Box {
                                    OutlinedButton(onClick = { expanded = true }) {
                                        Text(state.usagimimiPericiaEscolhida ?: "Selecionar Perícia")
                                    }
                                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                        adgSkills.forEach { skillName ->
                                            DropdownMenuItem(
                                                text = { Text(skillName) },
                                                onClick = {
                                                    state.selecionarPericiaUsagimimi(skillName, feedbackMessages)
                                                    expanded = false
                                                    onUserFeedback()
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
                                                "${k.toFancyTitleCase()} ${dieVal.toDiceString()}"
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
                                                val die = if (v == 0) "d4-2" else (4 + (v - 1) * 2).toDiceString()
                                                "${k.toFancyTitleCase()} $die"
                                            }
                                            Text(
                                                text = "Perícias: $skillsText",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(Modifier.height(4.dp))
                                        }

                                        // Free Advantages
                                        val vantagensGratisVisiveis = item.vantagensGratis.filterNot {
                                            it.keyify() == Constants.ID_AA_AGENT_SYN.keyify()
                                        }
                                        if (vantagensGratisVisiveis.isNotEmpty()) {
                                            val advsText = vantagensGratisVisiveis.joinToString(", ") { it.toFancyTitleCase() }
                                            Text(
                                                text = "Vantagens: $advsText",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(Modifier.height(4.dp))
                                        }

                                        // Hindrances
                                        if (item.desvantagens.isNotEmpty()) {
                                            val hindsText = item.desvantagens.joinToString(", ") { it.toFancyTitleCase() }
                                            Text(
                                                text = "Complicações: $hindsText",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(Modifier.height(4.dp))
                                        }

                                        // Abilities
                                        val habilidadesExibidas = buildList {
                                            addAll(item.habilidades)
                                            if (
                                                isSelected &&
                                                compendioSciFiAtivo &&
                                                item.nome.keyify() == "AVIANOS" &&
                                                state.resolveSciFiVariantSelectionFor(item.nome, item.opcoes).equals("Ave de rapina", ignoreCase = true)
                                            ) {
                                                if (none { it.nome.keyify() == "FORMA ALIENIGENA" }) {
                                                    add(
                                                        RacialAbilityLite(
                                                            nome = "Forma Alienígena",
                                                            descricao = "O tamanho e a forma destes seres são incompatíveis com a maioria dos equipamentos e veículos usados no cenário. Só podem usar armaduras personalizadas e subtraem 1 das rolagens de Característica ao usar equipamentos e veículos não personalizados. Os itens podem ser personalizados para funcionar para a personagem por 100% do custo base (a critério do Mestre). Se a criatura também for Grande (veja Savage Worlds Edição Aventura), use apenas essa habilidade."
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                        if (habilidadesExibidas.isNotEmpty()) {
                                            Spacer(Modifier.height(4.dp))
                                            habilidadesExibidas.forEach { ability ->
                                                Column(modifier = Modifier.padding(bottom = 4.dp)) {
                                                    val abilityDisplayName =
                                                        if (ability.nome.keyify() == "FORTUNA DA") "Sorte" else ability.nome
                                                    Text(
                                                        text = abilityDisplayName,
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
