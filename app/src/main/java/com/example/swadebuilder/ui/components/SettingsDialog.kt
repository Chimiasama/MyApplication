package com.example.swadebuilder.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.foundation.clickable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import com.example.swadebuilder.util.AppPreferences
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.IconButton
import androidx.compose.ui.text.style.TextAlign
import com.example.swadebuilder.toDiceString
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.util.loadJsonAsset
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.toIdSlug
import com.example.swadebuilder.util.toEditionDisplayName
import com.example.swadebuilder.model.Requisito
import com.example.swadebuilder.model.getActiveOrigins
import com.example.swadebuilder.model.getDisplayName
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.FeedbackController
import com.example.swadebuilder.TabStyle
import com.example.swadebuilder.ui.theme.AppTheme
import kotlin.math.roundToInt

import androidx.compose.material3.OutlinedButton

// Livro Básico: "Super Poderes (2+X)... o custo é 2 — pelo Antecedente Arcano
// (Super Poderes) — mais o custo do poder selecionado (X)." Muitos poderes do
// Compêndio de Super Poderes têm custo em escada por nível (ex.: "1/2/3/4/5"),
// então X aqui é o primeiro degrau (a compra mínima do poder).
private fun primeiroCustoSuperPoder(custoBase: String?): Int =
    custoBase
        ?.split("/")
        ?.firstOrNull()
        ?.trim()
        ?.replace('–', '-')
        ?.toIntOrNull()
        ?: 1

/**
 * Rótulo + grupo de FilterChips do formulário de Conteúdo Customizado — era repetido em cada
 * categoria com pequenas inconsistências (algumas usavam Row sem quebra de linha, que cortava
 * os chips em telas estreitas quando havia várias opções).
 */
/**
 * Sem `private`: além de usado nas outras categorias deste diálogo, é reaproveitado por
 * EquipamentoCreatorForm.kt (categoria "Equipamento" foi extraída pra lá — ver comentário
 * no topo daquele arquivo).
 */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun LabeledChipGroup(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.FlowRowScope.() -> Unit
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        androidx.compose.foundation.layout.FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            content = content
        )
    }
}

// Seletor de livros do formulário de Conteúdo Customizado, como lista suspensa (em vez de
// uma fileira de FilterChips sempre expandida com "Geral" + todos os livros do app, que
// ocupava bastante espaço vertical). Fechado, mostra só os livros já marcados — por padrão
// o livro ativo no momento (ver selectedBookTags em SettingsDialog); aberto, lista "Geral" e
// todos os livros pra marcar/desmarcar, no mesmo padrão multi-seleção do EraDropdownPicker
// de EquipamentoCreatorForm.kt.
@Composable
private fun BookTagsDropdownPicker(selected: Set<String>, onChange: (Set<String>) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val resumo = remember(selected) {
        selected.joinToString(", ") {
            if (it == com.example.swadebuilder.util.TAG_GERAL) "Geral" else it.toEditionDisplayName()
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Vincular a quais livros:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(
                resumo.ifBlank { "Nenhum livro marcado" },
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Text("▾", style = MaterialTheme.typography.bodyMedium)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = com.example.swadebuilder.util.TAG_GERAL in selected, onCheckedChange = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Geral", fontWeight = FontWeight.Bold)
                    }
                },
                onClick = {
                    onChange(
                        if (com.example.swadebuilder.util.TAG_GERAL in selected) {
                            selected - com.example.swadebuilder.util.TAG_GERAL
                        } else {
                            selected + com.example.swadebuilder.util.TAG_GERAL
                        }
                    )
                }
            )
            com.example.swadebuilder.util.TODOS_OS_LIVROS.forEach { bookKey ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = bookKey in selected, onCheckedChange = null)
                            Spacer(Modifier.width(8.dp))
                            Text(bookKey.toEditionDisplayName())
                        }
                    },
                    onClick = {
                        onChange(if (bookKey in selected) selected - bookKey else selected + bookKey)
                    }
                )
            }
        }
        if (selected.isEmpty()) {
            Text(
                "Nenhum livro marcado — vai salvar no livro ativo no momento.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    state: CriadorState,
    isHomeScreen: Boolean = false,
    isCreationPhase: Boolean = false,
    onDismiss: () -> Unit,
    persistPrefs: () -> Unit,
    feedbackController: FeedbackController,
    onResetRulesToDefaults: (() -> Unit)? = null,
    // Chamado depois de qualquer criação/edição/exclusão de conteúdo customizado
    // (Vantagem, Complicação, Equipamento, Poder, Raça, Variante de Raça etc.):
    // invalida o cache de GameDataSnapshot por combinação de livros
    // (GameDataRepository/ModuleSnapshotCache), que senão continuaria devolvendo
    // um snapshot desatualizado — sem o conteúdo recém-criado — na próxima vez
    // que um personagem NOVO for criado com a mesma combinação de livros.
    onCustomContentChanged: () -> Unit = {},
    onThemeSelected: (AppTheme) -> Unit
) {
    var showNpcWarning by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    val themeNames = remember {
        mapOf(
            AppTheme.DEFAULT   to "Padrão",
            AppTheme.MEDIEVAL  to "Medieval",
            AppTheme.CYBERPUNK to "Cyberpunk",
            AppTheme.WW2       to "Segunda Guerra",
            AppTheme.HORROR    to "Horror",
            AppTheme.SCIFI     to "Sci-Fi",
            AppTheme.MINIMALIST to "Minimalista",
            AppTheme.HALLOWEEN to "Halloween"
        )
    }

    val themeDescriptions = remember {
        mapOf(
            AppTheme.DEFAULT   to "Pergaminho clássico (Old School)",
            AppTheme.MEDIEVAL  to "Manuscrito antigo e detalhes dourados",
            AppTheme.CYBERPUNK to "Estilo Matrix com linhas wireframe verdes",
            AppTheme.WW2       to "Papel Khaki e carimbo militar de campo",
            AppTheme.HORROR    to "Atmosfera gótica e detalhes carmesim",
            AppTheme.SCIFI     to "Interface holofuturista e azul estelar",
            AppTheme.MINIMALIST to "Design limpo e alto contraste",
            AppTheme.HALLOWEEN to "Laranja abóbora e roxo místico"
        )
    }

    val sortedThemes = remember(themeNames) {
        AppTheme.entries.sortedBy { themeNames[it] ?: it.name }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        // Um toque sem querer fora da área do diálogo (comum numa tela cheia de
        // opções) não deve fechar tudo e voltar pra ficha — só o botão
        // "Fechar"/voltar do sistema fecha.
        properties = androidx.compose.ui.window.DialogProperties(dismissOnClickOutside = false),
        title = { Text("Configurações", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Card "Interface do Sistema"
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Interface do Sistema",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Mensagens do Sistema", style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = state.showSystemMessages,
                                onCheckedChange = {
                                    state.showSystemMessages = it
                                    persistPrefs()
                                },
                                modifier = Modifier.scale(0.8f)
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                "Modo de Distribuição (Atributos e Perícias)",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            SingleChoiceSegmentedButtonRow(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val options = listOf(
                                    AppPreferences.ModoSelecaoPericia.CARROSSEL_POPOVER,
                                    AppPreferences.ModoSelecaoPericia.STEPPER_CORES
                                )
                                val labels = listOf("Tocar e Escolher", "Botões + e -")

                                options.forEachIndexed { index, option ->
                                    SegmentedButton(
                                        selected = state.modoSelecaoPericia == option,
                                        onClick = {
                                            state.modoSelecaoPericia = option
                                            persistPrefs()
                                        },
                                        shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size)
                                    ) {
                                        Text(labels[index], style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }

                        if (isHomeScreen) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Não solicitar escolha de regras", style = MaterialTheme.typography.bodyMedium)
                                    Text("Direto para criação com regras padrão.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(
                                    checked = state.pularSelecaoRegras,
                                    onCheckedChange = {
                                        state.pularSelecaoRegras = it
                                        persistPrefs()
                                        onResetRulesToDefaults?.invoke()
                                    },
                                    modifier = Modifier.scale(0.8f)
                                )
                            }
                        }

                        // NPC Mode Toggle (Only during creation phase and if not already NPC)
                        if (isCreationPhase && !state.modoProgressaoAtivo && !state.isNpcExibicao) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Modo Livre (NPC)", style = MaterialTheme.typography.bodyMedium)
                                    Text("Ignora custos e requisitos.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(
                                    checked = state.modoLivre,
                                    onCheckedChange = { if (it && !state.modoLivre) showNpcWarning = true },
                                    enabled = !state.modoLivre, // Irreversible
                                    modifier = Modifier.scale(0.8f)
                                )
                            }
                        }
                    }
                }

                // Card "Conteúdo Customizado"
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    var showCustomContentDialog by remember { mutableStateOf(false) }

                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Conteúdo Customizado",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Criação Direta nas Abas", style = MaterialTheme.typography.bodyMedium)
                                Text("Exibe botão de criação rápida '+ Criar' dentro das abas.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = state.habilitarCriacaoNasAbas,
                                onCheckedChange = {
                                    state.habilitarCriacaoNasAbas = it
                                    persistPrefs()
                                },
                                modifier = Modifier.scale(0.8f)
                            )
                        }
                        Text(
                            text = "Crie vantagens e itens caseiros com prefixo 'custom:'.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedButton(
                            onClick = {
                                showCustomContentDialog = true
                            },
                            shape = MaterialTheme.shapes.small,
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Gerenciar", style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    if (showCustomContentDialog) {
                        CustomContentManageDialog(
                            state = state,
                            initialCategory = "Vantagem",
                            onDismiss = { showCustomContentDialog = false },
                            onCustomContentChanged = onCustomContentChanged
                        )
                    }
                }

                // Card "Visual e Tema"
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Visual e Tema",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text("Estilo das Abas / Opções", style = MaterialTheme.typography.bodyMedium)

                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val options = listOf(TabStyle.ICONES, TabStyle.TEXTO)
                            val labels = listOf("Ícones", "Texto")

                            options.forEachIndexed { index, option ->
                                SegmentedButton(
                                    selected = state.estiloAbas == option,
                                    onClick = {
                                        state.estiloAbas = option
                                        persistPrefs()
                                    },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size)
                                ) {
                                    Text(labels[index])
                                }
                            }
                        }

                        Spacer(Modifier.height(4.dp))

                        // Theme Selection Trigger Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Tema do App", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = themeNames[state.appTheme] ?: state.appTheme.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            OutlinedButton(
                                onClick = { showThemeDialog = true }
                            ) {
                                Text("Alterar Tema")
                            }
                        }
                    }
                }

                // Card "Sons e Vibração"
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp) // Increased spacing for cleaner look
                    ) {
                        Text(
                            text = "Sons e Vibração",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // Haptic Feedback
                        Column {
                            Text("Intensidade da Vibração", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Vibration,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Slider(
                                    value = state.hapticStrength.toFloat(),
                                    onValueChange = { state.hapticStrength = it.roundToInt() },
                                    onValueChangeFinished = {
                                        persistPrefs()
                                        feedbackController.play(state.hapticStrength, 0)
                                    },
                                    valueRange = 0f..100f,
                                    modifier = Modifier.weight(1f),
                                    thumb = {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                                        )
                                    },
                                    track = { sliderState ->
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(2.dp)
                                                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                                        ) {
                                            val fraction = (sliderState.value - sliderState.valueRange.start) / (sliderState.valueRange.endInclusive - sliderState.valueRange.start)
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(fraction)
                                                    .fillMaxHeight()
                                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                                            )
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("${state.hapticStrength}%", style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        // App Sounds
                        Column {
                            Text("Volume", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Slider(
                                    value = state.soundVolume.toFloat(),
                                    onValueChange = { state.soundVolume = it.roundToInt() },
                                    onValueChangeFinished = {
                                        persistPrefs()
                                        feedbackController.play(0, state.soundVolume)
                                    },
                                    valueRange = 0f..100f,
                                    modifier = Modifier.weight(1f),
                                    thumb = {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .background(MaterialTheme.colorScheme.secondary, CircleShape)
                                        )
                                    },
                                    track = { sliderState ->
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(2.dp)
                                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f), CircleShape)
                                        ) {
                                            val fraction = (sliderState.value - sliderState.valueRange.start) / (sliderState.valueRange.endInclusive - sliderState.valueRange.start)
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(fraction)
                                                    .fillMaxHeight()
                                                    .background(MaterialTheme.colorScheme.secondary, CircleShape)
                                            )
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("${state.soundVolume}%", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar")
            }
        }
    )

    if (showNpcWarning) {
        AlertDialog(
            onDismissRequest = { showNpcWarning = false },
            title = { Text("Transformar em NPC?") },
            text = { Text("Ao ativar o Modo Livre, este personagem será transformado em um NPC. Custos de pontos e requisitos serão ignorados, e a progressão de XP padrão será desabilitada. Esta ação é irreversível para este personagem.") },
            confirmButton = {
                TextButton(onClick = {
                    state.modoLivre = true
                    showNpcWarning = false
                }) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { showNpcWarning = false }) { Text("Cancelar") }
            }
        )
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Selecionar Tema do App", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    sortedThemes.forEach { theme ->
                        val isSelected = state.appTheme == theme
                        val themeLabel = themeNames[theme] ?: theme.name
                        val themeDesc = themeDescriptions[theme] ?: ""
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                        ) {
                            if (isSelected) {
                                TextButton(
                                    onClick = { showThemeDialog = false },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("✓ $themeLabel", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                        if (themeDesc.isNotBlank()) {
                                            Text(themeDesc, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            } else {
                                OutlinedButton(
                                    onClick = {
                                        onThemeSelected(theme)
                                        persistPrefs()
                                        feedbackController.play(state.hapticStrength, state.soundVolume)
                                        showThemeDialog = false
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(themeLabel, style = MaterialTheme.typography.titleMedium)
                                        if (themeDesc.isNotBlank()) {
                                            Text(themeDesc, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomContentManageDialog(
    state: CriadorState,
    initialCategory: String = "Vantagem",
    onDismiss: () -> Unit,
    onCustomContentChanged: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var customItemName by remember { mutableStateOf("") }
    var customItemDesc by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf<String?>(null) }

                        val customStorageManager = remember { com.example.swadebuilder.util.CustomStorageManager() }
                        // Livro(s) a que o item sendo criado vai ficar vinculado — o jogador
                        // escolhe isso na hora de salvar (ver "Seletor de Livros" abaixo), não
                        // fica mais preso ao livro que estava ativo quando abriu essa tela.
                        // Por padrão já vem com o livro atualmente ativo marcado.
                        var selectedBookTags by remember(state) {
                            mutableStateOf(setOf(state.getActiveOrigins().firstOrNull() ?: "BASICO"))
                        }
                        var selectedCategory by remember { mutableStateOf(initialCategory) }
                        var customRequirements by remember { mutableStateOf("") }
                        var customAdvCategory by remember { mutableStateOf(com.example.swadebuilder.model.Categoria.PROFISSIONAL) }
                        // Categoria Customizada (ver model/CategoriaCustomizada.kt) escolhida pelo
                        // Mestre pra Vantagem/Poder/Super Poder/Complicação — só relevante quando
                        // não nulo; pra Vantagem, escolher uma categoria customizada também força
                        // customAdvCategory = Categoria.CUSTOMIZADA (ver seletor abaixo).
                        var customAdvCategoriaCustomizadaId by remember { mutableStateOf<String?>(null) }
                        var customPoderCategoriaId by remember { mutableStateOf<String?>(null) }
                        var customSuperPoderCategoriaId by remember { mutableStateOf<String?>(null) }
                        var customComplicacaoCategoriaId by remember { mutableStateOf<String?>(null) }
                        // Item (tipo, nome) selecionado pra reatribuir de categoria na lista
                        // "Itens Customizados" abaixo, sem reabrir o formulário inteiro.
                        var moverCategoriaTarget by remember { mutableStateOf<Pair<String, String>?>(null) }
                        var customStage by remember { mutableStateOf("Novato") }
                        var customAttrMin by remember { mutableStateOf(mapOf<String, Int>()) }
                        var customSkillMin by remember { mutableStateOf(mapOf<String, Int>()) }
                        var customPrereqEdges by remember { mutableStateOf(listOf<String>()) }
                        var customPrereqComps by remember { mutableStateOf(listOf<String>()) }
                        // Pré-requisito por Categoria Customizada (ver
                        // ValidateCustomCategoryPrerequisiteUseCase) — alternativa a
                        // customPrereqEdges quando o Mestre quer exigir "qualquer vantagem
                        // desta categoria" em vez de uma vantagem específica por id.
                        var customPrereqCategoriasCustomizadas by remember { mutableStateOf(setOf<String>()) }
                        var showAttrDialog by remember { mutableStateOf(false) }
                        var showSkillDialog by remember { mutableStateOf(false) }
                        var showEdgeDialog by remember { mutableStateOf(false) }
                        var showCompDialog by remember { mutableStateOf(false) }
                        var customSeverity by remember { mutableStateOf("Maior") }
                        // Perícia customizada: qual Atributo ela usa como base do teste, e se
                        // já começa em d4 grátis (perícia "básica") — mesmos dois campos que o
                        // catálogo oficial (Pericia.atributo/basica, ver model/Pericia.kt).
                        var customPericiaAtributoVinculado by remember { mutableStateOf<String?>(null) }
                        var customPericiaBasica by remember { mutableStateOf(false) }
                        // Categoria "Equipamento" extraída pra EquipamentoCreatorForm.kt — já
                        // tinha crescido demais (Tipo/Subtipo, dano estruturado, Força Mínima,
                        // estatísticas de arma/armadura/escudo/veículo) pra continuar inline
                        // aqui junto com as outras 8 categorias deste diálogo.
                        val equipForm = rememberEquipamentoFormState()
                        var customPp by remember { mutableStateOf("1") }
                        var customSuperPoderCustoBase by remember { mutableStateOf("2") }
                        // Lista de modificadores do SuperPoder sendo criado
                        var customSuperPoderModificadoresList by remember { mutableStateOf(listOf<String>()) }
                        var showSuperPoderModificadoresPickerDialog by remember { mutableStateOf(false) }
                        // Modificador de Poder (ver model/ModificadorCustomizado.kt): não cria um
                        // Super Poder novo, cria um modificador avulso e o anexa a um poder já
                        // existente (oficial ou customizado) — cobre o modificador "Especial" do
                        // livro (negociado à mesa, sem texto fixo) e qualquer outro modificador
                        // de casa que o Mestre queira adicionar a um poder específico.
                        var customModificadorCusto by remember { mutableStateOf("+2") }
                        var customModificadorPoderAlvoNome by remember { mutableStateOf<String?>(null) }
                        var showModificadorPoderPickerDialog by remember { mutableStateOf(false) }
                        // Antecedente Arcano customizado: "lista aberta" usa todos os poderes
                        // do(s) livro(s) marcado(s) no Seletor de Livros (ou de todos, se for
                        // "Geral") — ver Vantagem.poderesPermitidos/origem e o hook em
                        // PoderesSection.kt. Desmarcando, o Mestre escolhe poderes específicos.
                        var customAaListaAberta by remember { mutableStateOf(true) }
                        var customAaPoderesEspecificos by remember { mutableStateOf(setOf<String>()) }
                        var showAaPoderesPickerDialog by remember { mutableStateOf(false) }
                        var customRange by remember { mutableStateOf("Toque") }
                        var customDuration by remember { mutableStateOf("3 turnos") }
                        var customRacialTrait by remember { mutableStateOf("") }
                        var refreshTrigger by remember { mutableIntStateOf(0) }

                        var customTraitCost by remember { mutableStateOf("1") }
                        // Efeito mecânico opcional do Traço Racial (ver
                        // RacialTraitEffect.PericiaPoolBonus/AtributoPoolBonus) — "Nenhum"
                        // = traço só flavor + custo, como sempre foi.
                        var customTraitEfeitoTipo by remember { mutableStateOf("Nenhum") }
                        var customTraitEfeitoValor by remember { mutableStateOf("") }
                        var selectedRacialTraits by remember { mutableStateOf(listOf<com.example.swadebuilder.model.HabilidadeCriacao>()) }
                        var showTraitSelectDialog by remember { mutableStateOf(false) }
                        // Completude da criação de Raça: RacialModifier.atributos/pericias/
                        // movimentacao já existiam no modelo (toda raça oficial os usa — ex.:
                        // Anão com Vigor d6 mínimo), mas o formulário de Raça customizada nunca
                        // os preenchia — só dava pra montar a raça via Traços Raciais soltos.
                        var racaAtributosMin by remember { mutableStateOf(mapOf<String, Int>()) }
                        var racaPericiasIniciais by remember { mutableStateOf(mapOf<String, Int>()) }
                        var racaMovimentacao by remember { mutableStateOf("0") } // bônus/penalidade, não valor absoluto
                        var showRacaAttrDialog by remember { mutableStateOf(false) }
                        var showRacaSkillDialog by remember { mutableStateOf(false) }

                        // Estado da Variante de Raça custom (ver ResolveVariantPointBudgetUseCase / CustomAncestryVariant).
                        var varianteBaseRacaId by remember { mutableStateOf<String?>(null) }
                        var showVarianteBaseRacaDialog by remember { mutableStateOf(false) }
                        var varianteTracosRemovidos by remember { mutableStateOf(listOf<String>()) }
                        var varianteTracosAdicionados by remember { mutableStateOf(listOf<com.example.swadebuilder.model.HabilidadeCriacao>()) }
                        var showVarianteTraitAddDialog by remember { mutableStateOf(false) }
                        var varianteVantagensAdicionadas by remember { mutableStateOf(listOf<String>()) }
                        var showVarianteVantagemAddDialog by remember { mutableStateOf(false) }
                        var varianteComplicacoesAdicionadas by remember { mutableStateOf(listOf<com.example.swadebuilder.model.CustomVariantComplicacaoEscolhida>()) }
                        var showVarianteComplicacaoSeveridadeDialog by remember { mutableStateOf(false) }
                        var showVarianteComplicacaoPickDialog by remember { mutableStateOf(false) }
                        var varianteComplicacaoComoMaiorEscolhido by remember { mutableStateOf(false) }
                        var varianteSemLimite by remember { mutableStateOf(false) }

                        val baseRacialCatalog: List<com.example.swadebuilder.model.HabilidadeCriacao> = remember {
                            runCatching {
                                context.loadJsonAsset<List<com.example.swadebuilder.model.HabilidadeCriacao>>("basico_habilidades_raciais.json")
                            }.getOrElse { emptyList() }.map { it.exibida() }
                        }

                        // "Super Poderes (2+X)": o traço racial em si custa 2 pontos, mais o
                        // custo do Super Poder do Compêndio de Super Poderes escolhido pelo
                        // Mestre (X). Como X varia por poder, a entrada de HabilidadeCriacao
                        // final é montada dinamicamente (ver superPoderRacialPickerTarget),
                        // não é um custo fixo de catálogo como os outros traços.
                        val superPoderesCatalog: List<com.example.swadebuilder.model.SuperPoder> = remember {
                            runCatching {
                                context.loadJsonAsset<List<com.example.swadebuilder.model.SuperPoder>>("super_poderes.json")
                            }.getOrElse { emptyList() }
                        }
                        var superPoderRacialPickerTarget by remember {
                            mutableStateOf<((com.example.swadebuilder.model.HabilidadeCriacao) -> Unit)?>(null)
                        }

                        // "Bônus/Penalidade de Perícia (±1/±2)": traços genéricos do catálogo
                        // oficial (basico_habilidades_raciais.json) que dizem "uma Perícia
                        // específica"/"uma perícia escolhida" — sem picker, o Mestre selecionava
                        // o traço genérico e não havia registro de qual perícia era. O app não
                        // aplica esse bônus em teste nenhum (perícia aqui é só custo de criação
                        // de raça, o teste em si é decidido à mesa), mas o traço final precisa
                        // deixar claro qual perícia foi escolhida — mesmo padrão de
                        // superPoderRacialPickerTarget, embutindo a escolha no nome
                        // ("Bônus de Perícia (+1): Intimidar") sem tocar no id
                        // (bonus_pericia_1/2, penalidade_pericia_1/2 continuam os mesmos).
                        // "pericia_racial_d4"/"pericia_racial_d6" entraram no mesmo picker por
                        // outro motivo: o livro (pág. 20/21 do Básico) diz que uma Perícia
                        // Racial em d6 custa 2 pontos, "ou 1 se já é uma perícia básica" —
                        // Atletismo/Conhecimento Geral/Furtividade/Perceber/Persuadir (e
                        // qualquer outra marcada `Pericia.basica` no catálogo, ex.: Canalizar
                        // Cristal do Crystal Heart) já começam em d4 de graça pra todo mundo,
                        // então subir pra d6 é só meio caminho, não o traço completo. O picker
                        // já existia pra registrar QUAL perícia foi escolhida (sem isso, o
                        // custo certo nem dava pra calcular); o ajuste de custo em si acontece
                        // no onEscolhido do AlertDialog "Escolher Perícia" abaixo, lendo
                        // `pericia.basica` — o mesmo campo estruturado que o resto do app já
                        // usa pra decidir se uma perícia começa em d4 de graça (PericiaRules.kt,
                        // EnsureDefaultSpecializationsUseCase), nunca um nome comparado em texto.
                        val periciaChoiceTraitIds = remember {
                            setOf(
                                "bonus_pericia_1", "bonus_pericia_2", "penalidade_pericia_1", "penalidade_pericia_2",
                                "pericia_racial_d4", "pericia_racial_d6"
                            )
                        }
                        var periciaTraitPickerTarget by remember {
                            mutableStateOf<Pair<com.example.swadebuilder.model.HabilidadeCriacao, (com.example.swadebuilder.model.HabilidadeCriacao) -> Unit>?>(null)
                        }

                        // "Empilháveis" (Armadura/Resistência/Aparar/Aparar Baixo/
                        // Tamanho +1/Frágil/Movimentação): os 3 livros marcam cada um
                        // com "(N)"/"(S)" — quantas vezes pode ser comprado, custo e
                        // efeito multiplicando por compra (ver
                        // RacialTraitPointCatalog.VEZES_MAX/basico_habilidades_raciais.json
                        // "vezesMax"). Mesmo padrão de picker que
                        // periciaTraitPickerTarget: ao marcar, abre "Quantas vezes?" em
                        // vez de já adicionar o traço com 1 compra.
                        var stackPickerTarget by remember {
                            mutableStateOf<Pair<com.example.swadebuilder.model.HabilidadeCriacao, (com.example.swadebuilder.model.HabilidadeCriacao) -> Unit>?>(null)
                        }

                        // Traços com "grupoEscolha" (ex.: Ações Adicionais 4/5/10 pontos) são
                        // versões ALTERNATIVAS do mesmo traço "(1)" — mesmo padrão de picker que
                        // stackPickerTarget, mas escolhendo entre entradas de catálogo distintas
                        // (nome/custo/id próprios) em vez de multiplicar 1..vezesMax.
                        var groupPickerTarget by remember {
                            mutableStateOf<Pair<com.example.swadebuilder.model.HabilidadeCriacao, (com.example.swadebuilder.model.HabilidadeCriacao) -> Unit>?>(null)
                        }

                        // Todas as categorias sempre disponíveis, em qualquer tela — a engrenagem
                        // já é global (tela inicial, criação, fase de XP), então não há mais
                        // motivo pra esconder categorias por causa de "isHomeScreen".
                        val categories = remember {
                            listOf("Vantagem", "Complicação", "Equipamento", "Poder", "Super Poder", "Modificador de Poder", "Antecedente Arcano", "Raça", "Traço Racial", "Variante de Raça", "Atributo", "Perícia")
                        }
                        if (selectedCategory !in categories) {
                            selectedCategory = categories.first()
                        }
                        // Categorias de gênero masculino, pra concordância em "Nome do/da
                        // <categoria>" abaixo — as demais ("Vantagem", "Complicação", "Raça",
                        // "Variante de Raça") são femininas e usam "da" por padrão.
                        val categoriasMasculinas = remember {
                            setOf("Equipamento", "Poder", "Super Poder", "Modificador de Poder", "Antecedente Arcano", "Traço Racial", "Atributo")
                        }
                        val artigoCategoria = if (selectedCategory in categoriasMasculinas) "do" else "da"
                        // Todos os "livros" de armazenamento que existem (livros reais + Geral).
                        // Conteúdo customizado não fica mais preso a um livro só: é gravado sob
                        // cada tag escolhida (ver selectedBookTags), então pra ler/listar/apagar
                        // é preciso varrer todos eles, não só o que estava ativo quando abriu.
                        val todosOsLivrosDeArmazenamento = remember {
                            com.example.swadebuilder.util.TODOS_OS_LIVROS + com.example.swadebuilder.util.TAG_GERAL
                        }
                        val allCustomDataByBook = remember(refreshTrigger) {
                            todosOsLivrosDeArmazenamento.associateWith { customStorageManager.loadCustomContent(context, it) }
                        }
                        val activeBookCustomData = remember(allCustomDataByBook) {
                            val all = allCustomDataByBook.values
                            com.example.swadebuilder.util.BookCustomContent(
                                bookKey = "TODOS",
                                vantagens = all.flatMap { it.vantagens }.distinctBy { it.id },
                                complicacoes = all.flatMap { it.complicacoes }.distinctBy { it.id },
                                equipamentos = all.flatMap { it.equipamentos }.distinctBy { it.nome.lowercase() },
                                poderes = all.flatMap { it.poderes }.distinctBy { it.id },
                                superPoderes = all.flatMap { it.superPoderes }.distinctBy { it.nome.lowercase() },
                                racas = all.flatMap { it.racas }.distinctBy { it.nome.lowercase() },
                                habilidadesRaciais = all.flatMap { it.habilidadesRaciais }.distinctBy { it.nome.lowercase() },
                                variantesRaciais = all.flatMap { it.variantesRaciais }.distinctBy { it.id },
                                categoriasCustomizadas = all.flatMap { it.categoriasCustomizadas }.distinctBy { it.id },
                                atributosCustomizados = all.flatMap { it.atributosCustomizados }.distinctBy { it.nome.lowercase() },
                                periciasCustomizadas = all.flatMap { it.periciasCustomizadas }.distinctBy { it.nome.lowercase() },
                                modificadoresCustomizados = all.flatMap { it.modificadoresCustomizados }.distinctBy { it.id }
                            )
                        }

                        // Todos os Super Poderes que podem receber um Modificador de Poder
                        // (ver "Modificador de Poder" mais abaixo): catálogo oficial + qualquer
                        // Super Poder customizado já criado em qualquer livro de armazenamento.
                        val superPoderesParaModificador = remember(activeBookCustomData) {
                            (superPoderesCatalog + activeBookCustomData.superPoderes).distinctBy { it.nome.keyify() }
                        }

                        // Cria/renomeia/exclui uma Categoria Customizada (ver model/CategoriaCustomizada.kt),
                        // gravando em todos os livros marcados no Seletor de Livros (criação) ou em
                        // todos os livros de armazenamento (renomear/excluir, já que a categoria pode
                        // ter sido salva sob mais de uma tag).
                        fun criarCategoriaCustomizada(nome: String, tipo: com.example.swadebuilder.model.TipoEntidadeCategoria): com.example.swadebuilder.model.CategoriaCustomizada {
                            val newCat = com.example.swadebuilder.model.CategoriaCustomizada(
                                id = "catcustom:${tipo.name.lowercase()}:${nome.toIdSlug()}",
                                nome = nome,
                                tipoEntidade = tipo
                            )
                            val tags = selectedBookTags.ifEmpty {
                                setOf(state.getActiveOrigins().firstOrNull() ?: "BASICO")
                            }
                            tags.forEach { tag -> customStorageManager.addCategoriaCustomizada(context, tag, newCat) }
                            state.addCustomCategoriaCustomizada(newCat)
                            refreshTrigger++
                            return newCat
                        }
                        fun renomearCategoriaCustomizada(id: String, novoNome: String) {
                            todosOsLivrosDeArmazenamento.forEach { customStorageManager.renameCategoriaCustomizada(context, it, id, novoNome) }
                            state.renameCustomCategoriaCustomizada(id, novoNome)
                            refreshTrigger++
                        }
                        fun excluirCategoriaCustomizada(id: String) {
                            todosOsLivrosDeArmazenamento.forEach { customStorageManager.deleteCategoriaCustomizada(context, it, id) }
                            state.removeCustomCategoriaCustomizada(id)
                            refreshTrigger++
                        }

                        // Catálogo completo (oficial + customizado), sem colapsar por
                        // grupoEscolha — usado tanto pelas listas de seleção de Traços
                        // Raciais (que colapsam pra 1 linha por grupo) quanto pelo diálogo
                        // "Qual versão?" do groupPickerTarget (que precisa ver TODAS as
                        // alternativas do grupo escolhido).
                        val fullTraitsCatalog = remember(activeBookCustomData) {
                            (baseRacialCatalog + activeBookCustomData.habilidadesRaciais).distinctBy { it.nome }
                        }

                        AlertDialog(
                            onDismissRequest = onDismiss,
                            // Mesmo motivo do diálogo de Configurações: essa tela tem
                            // formulário longo, um toque de leve fora da área não pode
                            // derrubar o que já foi digitado.
                            properties = androidx.compose.ui.window.DialogProperties(dismissOnClickOutside = false),
                            title = { Text("Criar Conteúdo Customizado", style = MaterialTheme.typography.titleMedium) },
                            text = {
                                Column(
                                    modifier = Modifier
                                        .verticalScroll(rememberScrollState())
                                        .fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "O que você deseja criar?",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    // Category selector horizontal carousel (styled like superpower carousel with smooth edge gradient)
                                    Box(
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        androidx.compose.foundation.lazy.LazyRow(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(end = 24.dp)
                                        ) {
                                            items(categories.size) { index ->
                                                val cat = categories[index]
                                                val isSel = selectedCategory == cat
                                                androidx.compose.material3.FilterChip(
                                                    selected = isSel,
                                                    onClick = { selectedCategory = cat },
                                                    label = {
                                                        Text(
                                                            text = cat,
                                                            style = MaterialTheme.typography.labelMedium,
                                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                                        )
                                                    },
                                                    colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                )
                                            }
                                        }

                                        // Edge gradient fade
                                        Box(
                                            modifier = Modifier
                                                .width(20.dp)
                                                .align(Alignment.CenterEnd)
                                                .fillMaxHeight()
                                                .background(
                                                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                                                        colors = listOf(
                                                            androidx.compose.ui.graphics.Color.Transparent,
                                                            MaterialTheme.colorScheme.surfaceContainerHigh
                                                        )
                                                    )
                                                )
                                        )
                                    }

                                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

                                    // Container card for form elements to prevent overlapping and maintain clean spacing
                                    androidx.compose.material3.Surface(
                                        shape = MaterialTheme.shapes.medium,
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            // Common Name field
                                            androidx.compose.material3.OutlinedTextField(
                                                value = customItemName,
                                                onValueChange = { customItemName = it },
                                                label = { Text("Nome $artigoCategoria $selectedCategory") },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth()
                                            )

                                            // Seletor de Livros: em quais livros esse item vai aparecer. "Geral"
                                            // funciona em qualquer combinação de livros ativos; os demais são
                                            // específicos. Vale pra todas as categorias, é escolhido uma vez só
                                            // aqui e usado na hora de salvar. Lista suspensa em vez de chips
                                            // sempre expandidos: por padrão já vem só com o livro ativo marcado
                                            // (ver selectedBookTags acima), abrindo é que mostra os demais.
                                            BookTagsDropdownPicker(
                                                selected = selectedBookTags,
                                                onChange = { selectedBookTags = it }
                                            )

                                            // Category-specific fields
                                            when (selectedCategory) {
                                                "Vantagem" -> {
                                                    // Modular Requirements Section
                                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                        Text("Requisitos Modulares da Vantagem:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)

                                                        // Summary of configured requirements
                                                        val reqSummary = buildList {
                                                            if (customStage.isNotBlank()) add("Estágio: $customStage")
                                                            if (customAttrMin.isNotEmpty()) add("Atributos: " + customAttrMin.entries.joinToString { "${it.key} d${it.value}" })
                                                            if (customSkillMin.isNotEmpty()) add("Perícias: " + customSkillMin.entries.joinToString { "${it.key} d${it.value}" })
                                                            if (customPrereqEdges.isNotEmpty()) add("Vantagens Prévias: ${customPrereqEdges.size} selecionada(s)")
                                                            if (customPrereqComps.isNotEmpty()) add("Complicações: ${customPrereqComps.size} selecionada(s)")
                                                            if (customPrereqCategoriasCustomizadas.isNotEmpty()) add("Categoria(s) prévia(s): ${customPrereqCategoriasCustomizadas.size} selecionada(s)")
                                                        }
                                                        if (reqSummary.isNotEmpty()) {
                                                            Text(
                                                                text = reqSummary.joinToString(" | "),
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                        }

                                                        // Interactive Selector Buttons
                                                        @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
                                                        androidx.compose.foundation.layout.FlowRow(
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                                        ) {
                                                            OutlinedButton(onClick = { showAttrDialog = true }) {
                                                                Text(if (customAttrMin.isEmpty()) "+ Atributos" else "Atributos (${customAttrMin.size})", style = MaterialTheme.typography.labelSmall)
                                                            }
                                                            OutlinedButton(onClick = { showSkillDialog = true }) {
                                                                Text(if (customSkillMin.isEmpty()) "+ Perícias" else "Perícias (${customSkillMin.size})", style = MaterialTheme.typography.labelSmall)
                                                            }
                                                            OutlinedButton(onClick = { showEdgeDialog = true }) {
                                                                Text(if (customPrereqEdges.isEmpty()) "+ Vantagens Prévias" else "Vantagens (${customPrereqEdges.size})", style = MaterialTheme.typography.labelSmall)
                                                            }
                                                            OutlinedButton(onClick = { showCompDialog = true }) {
                                                                Text(if (customPrereqComps.isEmpty()) "+ Complicações" else "Complicações (${customPrereqComps.size})", style = MaterialTheme.typography.labelSmall)
                                                            }
                                                        }
                                                    }

                                                    androidx.compose.material3.OutlinedTextField(
                                                        value = customRequirements,
                                                        onValueChange = { customRequirements = it },
                                                        label = { Text("Outros Requisitos (texto livre)") },
                                                        singleLine = true,
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                    val availableAdvCategories = remember(state.listaVantagens, selectedBookTags, state.compendioArteDaGuerraAtivo, state.compendioPathfinderAtivo, state.compendioDeadlandsAtivo, state.compendioHorrorAtivo, state.modoMonstroAtivo, state.modoSupers) {
                                                        val baseCategories = mutableSetOf(
                                                            Categoria.ANTECEDENTE,
                                                            Categoria.COMBATE,
                                                            Categoria.ESTRANHAS,
                                                            Categoria.LENDARIAS,
                                                            Categoria.LIDERANCA,
                                                            Categoria.PODER,
                                                            Categoria.PROFISSIONAL,
                                                            Categoria.SOCIAIS
                                                        )
                                                        if (state.listaVantagens.isNotEmpty()) {
                                                            baseCategories.addAll(state.listaVantagens.map { it.categoria })
                                                        }
                                                        if ("ARTE_DA_GUERRA" in selectedBookTags || state.compendioArteDaGuerraAtivo) {
                                                            baseCategories.addAll(listOf(Categoria.CHI, Categoria.TROPO, Categoria.ESTILO_MARCIAL))
                                                        }
                                                        if ("PATHFINDER" in selectedBookTags || state.compendioPathfinderAtivo) {
                                                            baseCategories.addAll(listOf(Categoria.CLASSE, Categoria.VANTAGEM_DE_CLASSE, Categoria.PRESTIGIO, Categoria.ANCESTRALIDADE))
                                                        }
                                                        if ("DEADLANDS" in selectedBookTags || state.compendioDeadlandsAtivo) {
                                                            baseCategories.addAll(listOf(Categoria.ATORMENTADO, Categoria.ANCESTRALIDADE))
                                                        }
                                                        if ("HORROR" in selectedBookTags || state.compendioHorrorAtivo || state.modoMonstroAtivo) {
                                                            baseCategories.add(Categoria.MONSTRUOSAS)
                                                        }
                                                        if (state.modoSupers) {
                                                            baseCategories.add(Categoria.SUPER)
                                                        }
                                                        Categoria.entries.filter { it in baseCategories }
                                                    }

                                                    // CUSTOMIZADA nunca está em availableAdvCategories (não é uma
                                                    // categoria oficial escolhível nos chips acima) — sem essa
                                                    // exceção, este reset rodaria a cada recomposição e desfaria
                                                    // a escolha de categoria customizada assim que o Mestre a
                                                    // selecionasse no CategoriaCustomizadaChipRow abaixo.
                                                    if (customAdvCategory != Categoria.CUSTOMIZADA && customAdvCategory !in availableAdvCategories) {
                                                        customAdvCategory = availableAdvCategories.firstOrNull() ?: Categoria.PROFISSIONAL
                                                    }

                                                    LabeledChipGroup("Categoria da Vantagem:") {
                                                        availableAdvCategories.forEach { catEnum ->
                                                            androidx.compose.material3.FilterChip(
                                                                selected = customAdvCategory == catEnum && customAdvCategoriaCustomizadaId == null,
                                                                onClick = {
                                                                    customAdvCategory = catEnum
                                                                    customAdvCategoriaCustomizadaId = null
                                                                },
                                                                label = { Text(catEnum.getDisplayName(), style = MaterialTheme.typography.labelSmall) }
                                                            )
                                                        }
                                                    }
                                                    // Categoria Customizada do Mestre: alternativa às categorias oficiais
                                                    // fixas acima — escolher uma força customAdvCategory = CUSTOMIZADA
                                                    // (ver Vantagem.categoriaExibicao()).
                                                    com.example.swadebuilder.ui.components.CategoriaCustomizadaChipRow(
                                                        label = "Ou categoria customizada:",
                                                        categorias = activeBookCustomData.categoriasCustomizadas.filter { it.tipoEntidade == com.example.swadebuilder.model.TipoEntidadeCategoria.VANTAGEM },
                                                        selectedId = customAdvCategoriaCustomizadaId,
                                                        onSelect = { id ->
                                                            customAdvCategoriaCustomizadaId = id
                                                            if (id != null) customAdvCategory = com.example.swadebuilder.model.Categoria.CUSTOMIZADA
                                                        },
                                                        onCreate = { nome -> criarCategoriaCustomizada(nome, com.example.swadebuilder.model.TipoEntidadeCategoria.VANTAGEM) },
                                                        onRename = ::renomearCategoriaCustomizada,
                                                        onDelete = ::excluirCategoriaCustomizada,
                                                        allowNone = true,
                                                        noneLabel = "Nenhuma (usar acima)"
                                                    )
                                                    // Pré-requisito por Categoria Customizada: exige que o personagem já
                                                    // tenha alguma Vantagem da(s) categoria(s) marcada(s), sem precisar
                                                    // apontar uma vantagem específica (isso já existe em "+ Vantagens
                                                    // Prévias" acima). Útil pra campanhas com progressão em categorias
                                                    // próprias (ex.: "Pacto Menor" libera "Pacto Maior").
                                                    val categoriasVantagemParaPrereq = activeBookCustomData.categoriasCustomizadas.filter { it.tipoEntidade == com.example.swadebuilder.model.TipoEntidadeCategoria.VANTAGEM }
                                                    if (categoriasVantagemParaPrereq.isNotEmpty()) {
                                                        LabeledChipGroup("Exige Vantagem de categoria (pré-requisito, opcional):") {
                                                            categoriasVantagemParaPrereq.forEach { cat ->
                                                                androidx.compose.material3.FilterChip(
                                                                    selected = cat.id in customPrereqCategoriasCustomizadas,
                                                                    onClick = {
                                                                        customPrereqCategoriasCustomizadas = if (cat.id in customPrereqCategoriasCustomizadas) {
                                                                            customPrereqCategoriasCustomizadas - cat.id
                                                                        } else {
                                                                            customPrereqCategoriasCustomizadas + cat.id
                                                                        }
                                                                    },
                                                                    label = { Text(cat.nome, style = MaterialTheme.typography.labelSmall) }
                                                                )
                                                            }
                                                        }
                                                    }
                                                    LabeledChipGroup("Estágio Mínimo:") {
                                                        // Nomes vêm de model/Estagio.kt (fonte única) em vez de uma cópia
                                                        // solta aqui — evita divergir se os estágios mudarem.
                                                        com.example.swadebuilder.model.listaDeEstagios.map { it.nome }.forEach { stage ->
                                                            androidx.compose.material3.FilterChip(
                                                                selected = customStage == stage,
                                                                onClick = { customStage = stage },
                                                                label = { Text(stage, style = MaterialTheme.typography.labelSmall) }
                                                            )
                                                        }
                                                    }
                                                }
                                                "Complicação" -> {
                                                    LabeledChipGroup("Severidade Permitida:") {
                                                        listOf("Maior", "Menor", "Maior ou Menor").forEach { sev ->
                                                            androidx.compose.material3.FilterChip(
                                                                selected = customSeverity == sev,
                                                                onClick = { customSeverity = sev },
                                                                label = { Text(sev, style = MaterialTheme.typography.labelSmall) }
                                                            )
                                                        }
                                                    }
                                                    com.example.swadebuilder.ui.components.CategoriaCustomizadaChipRow(
                                                        label = "Categoria (opcional):",
                                                        categorias = activeBookCustomData.categoriasCustomizadas.filter { it.tipoEntidade == com.example.swadebuilder.model.TipoEntidadeCategoria.COMPLICACAO },
                                                        selectedId = customComplicacaoCategoriaId,
                                                        onSelect = { customComplicacaoCategoriaId = it },
                                                        onCreate = { nome -> criarCategoriaCustomizada(nome, com.example.swadebuilder.model.TipoEntidadeCategoria.COMPLICACAO) },
                                                        onRename = ::renomearCategoriaCustomizada,
                                                        onDelete = ::excluirCategoriaCustomizada
                                                    )
                                                }
                                                "Equipamento" -> {
                                                    EquipamentoCreatorFields(
                                                        equipForm,
                                                        categoriasCustomizadas = activeBookCustomData.categoriasCustomizadas.filter { it.tipoEntidade == com.example.swadebuilder.model.TipoEntidadeCategoria.EQUIPAMENTO },
                                                        onCreateCategoria = { nome -> criarCategoriaCustomizada(nome, com.example.swadebuilder.model.TipoEntidadeCategoria.EQUIPAMENTO) },
                                                        onRenameCategoria = ::renomearCategoriaCustomizada,
                                                        onDeleteCategoria = ::excluirCategoriaCustomizada
                                                    )
                                                }
                                                "Poder" -> {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        androidx.compose.material3.OutlinedTextField(
                                                            value = customPp,
                                                            onValueChange = { customPp = it },
                                                            label = { Text("Pontos de Poder") },
                                                            singleLine = true,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                        androidx.compose.material3.OutlinedTextField(
                                                            value = customRange,
                                                            onValueChange = { customRange = it },
                                                            label = { Text("Alcance") },
                                                            singleLine = true,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                    }
                                                    androidx.compose.material3.OutlinedTextField(
                                                        value = customDuration,
                                                        onValueChange = { customDuration = it },
                                                        label = { Text("Duração (ex: 3 turnos)") },
                                                        singleLine = true,
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                    com.example.swadebuilder.ui.components.CategoriaCustomizadaChipRow(
                                                        label = "Categoria (opcional):",
                                                        categorias = activeBookCustomData.categoriasCustomizadas.filter { it.tipoEntidade == com.example.swadebuilder.model.TipoEntidadeCategoria.PODER },
                                                        selectedId = customPoderCategoriaId,
                                                        onSelect = { customPoderCategoriaId = it },
                                                        onCreate = { nome -> criarCategoriaCustomizada(nome, com.example.swadebuilder.model.TipoEntidadeCategoria.PODER) },
                                                        onRename = ::renomearCategoriaCustomizada,
                                                        onDelete = ::excluirCategoriaCustomizada
                                                    )
                                                }
                                                "Super Poder" -> {
                                                    androidx.compose.material3.OutlinedTextField(
                                                        value = customSuperPoderCustoBase,
                                                        onValueChange = { customSuperPoderCustoBase = it },
                                                        label = { Text("Custo Base (ex: 2 ou 1/2/3/4/5)") },
                                                        singleLine = true,
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text(
                                                        text = "Modificadores do Super Poder",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    OutlinedButton(
                                                        onClick = { showSuperPoderModificadoresPickerDialog = true },
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(vertical = 4.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Add,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                        Spacer(Modifier.width(4.dp))
                                                        Text("Catálogo de Modificadores", style = MaterialTheme.typography.labelSmall)
                                                    }
                                                    if (customSuperPoderModificadoresList.isNotEmpty()) {
                                                        Spacer(Modifier.height(4.dp))
                                                        Column(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .background(
                                                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                                                    RoundedCornerShape(8.dp)
                                                                )
                                                                .padding(8.dp),
                                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                                        ) {
                                                            customSuperPoderModificadoresList.forEach { mod ->
                                                                Row(
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    verticalAlignment = Alignment.CenterVertically,
                                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                                ) {
                                                                    Text(
                                                                        text = "• $mod",
                                                                        style = MaterialTheme.typography.bodySmall,
                                                                        modifier = Modifier.weight(1f)
                                                                    )
                                                                    IconButton(
                                                                        onClick = {
                                                                            customSuperPoderModificadoresList = customSuperPoderModificadoresList - mod
                                                                        },
                                                                        modifier = Modifier.size(24.dp)
                                                                    ) {
                                                                        Icon(
                                                                            imageVector = Icons.Default.Close,
                                                                            contentDescription = "Remover",
                                                                            modifier = Modifier.size(16.dp),
                                                                            tint = MaterialTheme.colorScheme.error
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    com.example.swadebuilder.ui.components.CategoriaCustomizadaChipRow(
                                                        label = "Categoria (opcional):",
                                                        categorias = activeBookCustomData.categoriasCustomizadas.filter { it.tipoEntidade == com.example.swadebuilder.model.TipoEntidadeCategoria.SUPER_PODER },
                                                        selectedId = customSuperPoderCategoriaId,
                                                        onSelect = { customSuperPoderCategoriaId = it },
                                                        onCreate = { nome -> criarCategoriaCustomizada(nome, com.example.swadebuilder.model.TipoEntidadeCategoria.SUPER_PODER) },
                                                        onRename = ::renomearCategoriaCustomizada,
                                                        onDelete = ::excluirCategoriaCustomizada
                                                    )
                                                }
                                                "Modificador de Poder" -> {
                                                    Text(
                                                        "Cria um modificador avulso (ex.: o \"Especial\" do livro, negociado à mesa) e o anexa a um Super Poder já existente — oficial ou customizado. O nome acima é o nome do modificador; a descrição abaixo é o texto que vai aparecer nele.",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    OutlinedButton(
                                                        onClick = { showModificadorPoderPickerDialog = true },
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Text(
                                                            customModificadorPoderAlvoNome?.let { "Poder alvo: $it" } ?: "Escolher Super Poder alvo"
                                                        )
                                                    }
                                                    androidx.compose.material3.OutlinedTextField(
                                                        value = customModificadorCusto,
                                                        onValueChange = { customModificadorCusto = it },
                                                        label = { Text("Custo (ex: +2 ou -1/-2)") },
                                                        singleLine = true,
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                }
                                                "Antecedente Arcano" -> {
                                                    Text(
                                                        "Cria a vantagem \"ANTECEDENTE ARCANO ($customItemName)\". Os livros marcados acima no Seletor de Livros também decidem de qual livro vêm os poderes na lista aberta.",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        androidx.compose.material3.FilterChip(
                                                            selected = customAaListaAberta,
                                                            onClick = { customAaListaAberta = true },
                                                            label = { Text("Lista aberta", style = MaterialTheme.typography.labelSmall) }
                                                        )
                                                        androidx.compose.material3.FilterChip(
                                                            selected = !customAaListaAberta,
                                                            onClick = { customAaListaAberta = false },
                                                            label = { Text("Poderes específicos", style = MaterialTheme.typography.labelSmall) }
                                                        )
                                                    }
                                                    if (customAaListaAberta) {
                                                        Text(
                                                            if (com.example.swadebuilder.util.TAG_GERAL in selectedBookTags) {
                                                                "Vai liberar todos os poderes de todos os livros."
                                                            } else {
                                                                "Vai liberar todos os poderes de: ${selectedBookTags.joinToString(", ") { it.toEditionDisplayName() }.ifBlank { "(marque um livro acima)" }}"
                                                            },
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    } else {
                                                        OutlinedButton(onClick = { showAaPoderesPickerDialog = true }) {
                                                            Text(
                                                                if (customAaPoderesEspecificos.isEmpty()) "+ Escolher Poderes" else "Poderes (${customAaPoderesEspecificos.size})",
                                                                style = MaterialTheme.typography.labelSmall
                                                            )
                                                        }
                                                    }
                                                }
                                                "Raça" -> {
                                                    val netRacePoints = selectedRacialTraits.sumOf { it.custo }
                                                    val pointColor = if (netRacePoints == 2) MaterialTheme.colorScheme.primary else if (netRacePoints < 2) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error

                                                    OutlinedCard(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        colors = CardDefaults.outlinedCardColors(
                                                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                                        )
                                                    ) {
                                                        Column(
                                                            modifier = Modifier.padding(12.dp),
                                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                                        ) {
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Column {
                                                                    Text(
                                                                        text = "Traços Raciais",
                                                                        style = MaterialTheme.typography.titleSmall,
                                                                        fontWeight = FontWeight.Bold
                                                                    )
                                                                    Text(
                                                                        text = "Pontos: $netRacePoints / 2",
                                                                        style = MaterialTheme.typography.labelSmall,
                                                                        fontWeight = FontWeight.SemiBold,
                                                                        color = pointColor
                                                                    )
                                                                }
                                                                FilledTonalIconButton(
                                                                    onClick = { showTraitSelectDialog = true },
                                                                    modifier = Modifier.size(32.dp)
                                                                ) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.Add,
                                                                        contentDescription = "Adicionar Traço",
                                                                        modifier = Modifier.size(18.dp)
                                                                    )
                                                                }
                                                            }

                                                            if (selectedRacialTraits.isEmpty()) {
                                                                Text(
                                                                    text = "Nenhum traço racial adicionado. O padrão de criação de raças busca fechar em +2 pontos.",
                                                                    style = MaterialTheme.typography.bodySmall,
                                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                                )
                                                            } else {
                                                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                                    selectedRacialTraits.forEach { trait ->
                                                                        Row(
                                                                            modifier = Modifier.fillMaxWidth(),
                                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                                            verticalAlignment = Alignment.CenterVertically
                                                                        ) {
                                                                            Text(
                                                                                text = "• ${trait.nome} (${if (trait.custo > 0) "+${trait.custo}" else "${trait.custo}"} pts)",
                                                                                style = MaterialTheme.typography.bodySmall,
                                                                                fontWeight = FontWeight.Medium,
                                                                                modifier = Modifier.weight(1f)
                                                                            )
                                                                            TextButton(
                                                                                onClick = { selectedRacialTraits = selectedRacialTraits - trait },
                                                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                                                            ) {
                                                                                Text("Remover", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }

                                                    Spacer(Modifier.height(8.dp))

                                                    OutlinedCard(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        colors = CardDefaults.outlinedCardColors(
                                                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                                        )
                                                    ) {
                                                        Column(
                                                            modifier = Modifier.padding(12.dp),
                                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                                        ) {
                                                            Text(
                                                                text = "Atributos, Perícias e Movimentação",
                                                                style = MaterialTheme.typography.titleSmall,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                            val racaReqSummary = buildList {
                                                                if (racaAtributosMin.isNotEmpty()) add("Atributos: " + racaAtributosMin.entries.joinToString { "${it.key} ${it.value.toDiceString()}" })
                                                                if (racaPericiasIniciais.isNotEmpty()) add("Perícias: " + racaPericiasIniciais.entries.joinToString { "${it.key} ${it.value.toDiceString()}" })
                                                            }
                                                            if (racaReqSummary.isNotEmpty()) {
                                                                Text(
                                                                    text = racaReqSummary.joinToString(" | "),
                                                                    style = MaterialTheme.typography.bodySmall,
                                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                                )
                                                            }
                                                            @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
                                                            androidx.compose.foundation.layout.FlowRow(
                                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                                            ) {
                                                                OutlinedButton(onClick = { showRacaAttrDialog = true }) {
                                                                    Text(if (racaAtributosMin.isEmpty()) "+ Atributos Mínimos" else "Atributos (${racaAtributosMin.size})", style = MaterialTheme.typography.labelSmall)
                                                                }
                                                                OutlinedButton(onClick = { showRacaSkillDialog = true }) {
                                                                    Text(if (racaPericiasIniciais.isEmpty()) "+ Perícias Iniciais" else "Perícias (${racaPericiasIniciais.size})", style = MaterialTheme.typography.labelSmall)
                                                                }
                                                            }
                                                            androidx.compose.material3.OutlinedTextField(
                                                                value = racaMovimentacao,
                                                                onValueChange = { racaMovimentacao = it },
                                                                label = { Text("Bônus de Movimentação (opcional, ex: +2 ou -1)") },
                                                                singleLine = true,
                                                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                                                modifier = Modifier.fillMaxWidth()
                                                            )
                                                        }
                                                    }
                                                }
                                                "Traço Racial" -> {
                                                    androidx.compose.material3.OutlinedTextField(
                                                        value = customTraitCost,
                                                        onValueChange = { customTraitCost = it },
                                                        label = { Text("Custo em Pontos (ex: 2 para positivo, -1 para negativo)") },
                                                        singleLine = true,
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                    Spacer(Modifier.height(8.dp))
                                                    // Efeito mecânico opcional: além do custo (orçamento da raça),
                                                    // um traço pode dar/tirar Pontos de Perícia ou de Atributo de
                                                    // verdade (quantos pontos e o custo são decisão de quem cria
                                                    // — ver RacialTraitEffect.PericiaPoolBonus/AtributoPoolBonus).
                                                    // "Nenhum" continua sendo o traço puramente de flavor/custo de
                                                    // sempre, sem efeito numérico modelado.
                                                    Text(
                                                        "Efeito mecânico (opcional):",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                    @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
                                                    androidx.compose.foundation.layout.FlowRow(
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        listOf(
                                                            "Nenhum",
                                                            "Bônus de Pontos de Perícia",
                                                            "Penalidade de Pontos de Perícia",
                                                            "Bônus de Pontos de Atributo",
                                                            "Penalidade de Pontos de Atributo"
                                                        ).forEach { tipo ->
                                                            androidx.compose.material3.FilterChip(
                                                                selected = customTraitEfeitoTipo == tipo,
                                                                onClick = { customTraitEfeitoTipo = tipo },
                                                                label = { Text(tipo, style = MaterialTheme.typography.labelSmall) }
                                                            )
                                                        }
                                                    }
                                                    if (customTraitEfeitoTipo != "Nenhum") {
                                                        Spacer(Modifier.height(4.dp))
                                                        androidx.compose.material3.OutlinedTextField(
                                                            value = customTraitEfeitoValor,
                                                            onValueChange = { customTraitEfeitoValor = it },
                                                            label = {
                                                                Text(
                                                                    if (customTraitEfeitoTipo.startsWith("Bônus"))
                                                                        "Quantos pontos concede (ex: 3)"
                                                                    else
                                                                        "Quantos pontos tira (ex: 2 — vai virar penalidade)"
                                                                )
                                                            },
                                                            singleLine = true,
                                                            modifier = Modifier.fillMaxWidth()
                                                        )
                                                    }
                                                }
                                                "Variante de Raça" -> {
                                                    val baseRacaOptions = remember(state.listaAncestralidadesJson) {
                                                        state.listaAncestralidadesJson.distinctBy { it.nome.keyify() }.sortedBy { it.nome }
                                                    }
                                                    val varianteBaseRaca = remember(varianteBaseRacaId, baseRacaOptions) {
                                                        baseRacaOptions.firstOrNull { it.nome.keyify() == varianteBaseRacaId }
                                                    }

                                                    OutlinedButton(
                                                        onClick = { showVarianteBaseRacaDialog = true },
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Text(varianteBaseRaca?.nome ?: "Selecionar Raça Base")
                                                    }

                                                    if (varianteBaseRaca == null) {
                                                        Text(
                                                            text = "Escolha a raça base pra começar a montar a Variante.",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    } else {
                                                        val habilidadeItems = remember(varianteBaseRaca) {
                                                            com.example.swadebuilder.model.usecase.ResolveVariantPointBudgetUseCase.itensRemoviveisDe(varianteBaseRaca)
                                                                .filter { it.habilidadeId != null }
                                                        }

                                                        val itensRemovidosSelecionados = habilidadeItems.filter { it.habilidadeId in varianteTracosRemovidos }

                                                        val itensAdicionadosSelecionados = buildList {
                                                            varianteTracosAdicionados.forEach { trait ->
                                                                add(com.example.swadebuilder.model.usecase.VariantBudgetItem(label = trait.nome, custo = trait.custo, habilidadeId = trait.nome.lowercase().replace(" ", "_")))
                                                            }
                                                            varianteVantagensAdicionadas.forEach { vid ->
                                                                state.listaVantagens.firstOrNull { it.id == vid }?.let {
                                                                    add(com.example.swadebuilder.model.usecase.ResolveVariantPointBudgetUseCase.vantagemComoItemAdicionado(it))
                                                                }
                                                            }
                                                            varianteComplicacoesAdicionadas.forEach { esc ->
                                                                state.listaComplicacoes.firstOrNull { it.id == esc.complicacaoId }?.let {
                                                                    add(com.example.swadebuilder.model.usecase.ResolveVariantPointBudgetUseCase.complicacaoComoItemAdicionado(it, esc.comoMaior))
                                                                }
                                                            }
                                                        }

                                                        val valorBaseRaca = remember(varianteBaseRaca) {
                                                            com.example.swadebuilder.model.usecase.ResolveVariantPointBudgetUseCase.valorTotalDe(varianteBaseRaca)
                                                        }
                                                        val budgetResult = remember(valorBaseRaca, itensRemovidosSelecionados, itensAdicionadosSelecionados, varianteSemLimite) {
                                                            com.example.swadebuilder.model.usecase.ResolveVariantPointBudgetUseCase().resolve(
                                                                valorBaseRaca, itensRemovidosSelecionados, itensAdicionadosSelecionados,
                                                                orcamento = varianteBaseRaca.pontosRaciaisEsperados,
                                                                semLimite = varianteSemLimite
                                                            )
                                                        }

                                                        OutlinedCard(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                                                        ) {
                                                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                                val saldoColor = if (budgetResult.dentroDoOrcamento) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                                                Text(
                                                                    text = if (varianteSemLimite) "Pontos da raça: ${budgetResult.saldo} (sem limite)" else "Pontos da raça: ${budgetResult.saldo} / ${budgetResult.orcamento}",
                                                                    style = MaterialTheme.typography.labelSmall,
                                                                    fontWeight = FontWeight.SemiBold,
                                                                    color = saldoColor
                                                                )
                                                                CheckboxRow(
                                                                    label = "Sem limite de pontos (raças mais fortes)",
                                                                    checked = varianteSemLimite,
                                                                    onCheckedChange = { varianteSemLimite = it }
                                                                )

                                                                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                                                                Text("Remover da raça base:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

                                                                if (habilidadeItems.isEmpty()) {
                                                                    Text("Esta raça não tem traços removíveis.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                                } else {
                                                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                                        habilidadeItems.forEach { item ->
                                                                            val isSel = item.habilidadeId in varianteTracosRemovidos
                                                                            Row(
                                                                                modifier = Modifier.fillMaxWidth().clickable {
                                                                                    varianteTracosRemovidos = if (isSel) varianteTracosRemovidos - item.habilidadeId!! else varianteTracosRemovidos + item.habilidadeId!!
                                                                                },
                                                                                verticalAlignment = Alignment.CenterVertically
                                                                            ) {
                                                                                Checkbox(checked = isSel, onCheckedChange = {
                                                                                    varianteTracosRemovidos = if (it) varianteTracosRemovidos + item.habilidadeId!! else varianteTracosRemovidos - item.habilidadeId!!
                                                                                })
                                                                                Text("${item.label} (${item.custo})", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                                                            }
                                                                        }
                                                                    }
                                                                }

                                                                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                                                                Text("Adicionar à Variante:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

                                                                @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
                                                                androidx.compose.foundation.layout.FlowRow(
                                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                                                ) {
                                                                    OutlinedButton(onClick = { showVarianteTraitAddDialog = true }) {
                                                                        Text("+ Traço Racial", style = MaterialTheme.typography.labelSmall)
                                                                    }
                                                                    OutlinedButton(onClick = { showVarianteVantagemAddDialog = true }) {
                                                                        Text("+ Vantagem", style = MaterialTheme.typography.labelSmall)
                                                                    }
                                                                    OutlinedButton(onClick = { showVarianteComplicacaoSeveridadeDialog = true }) {
                                                                        Text("+ Complicação", style = MaterialTheme.typography.labelSmall)
                                                                    }
                                                                }

                                                                if (itensAdicionadosSelecionados.isNotEmpty()) {
                                                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                                        varianteTracosAdicionados.forEach { trait ->
                                                                            Row(
                                                                                modifier = Modifier.fillMaxWidth(),
                                                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                                                verticalAlignment = Alignment.CenterVertically
                                                                            ) {
                                                                                Text("• ${trait.nome} (${if (trait.custo > 0) "+${trait.custo}" else "${trait.custo}"})", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                                                                TextButton(onClick = { varianteTracosAdicionados = varianteTracosAdicionados - trait }, contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)) {
                                                                                    Text("Remover", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                                                                                }
                                                                            }
                                                                        }
                                                                        varianteVantagensAdicionadas.forEach { vid ->
                                                                            val vant = state.listaVantagens.firstOrNull { it.id == vid }
                                                                            Row(
                                                                                modifier = Modifier.fillMaxWidth(),
                                                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                                                verticalAlignment = Alignment.CenterVertically
                                                                            ) {
                                                                                Text("• ${vant?.nome ?: vid}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                                                                TextButton(onClick = { varianteVantagensAdicionadas = varianteVantagensAdicionadas - vid }, contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)) {
                                                                                    Text("Remover", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                                                                                }
                                                                            }
                                                                        }
                                                                        varianteComplicacoesAdicionadas.forEach { esc ->
                                                                            val comp = state.listaComplicacoes.firstOrNull { it.id == esc.complicacaoId }
                                                                            Row(
                                                                                modifier = Modifier.fillMaxWidth(),
                                                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                                                verticalAlignment = Alignment.CenterVertically
                                                                            ) {
                                                                                Text("• ${comp?.name ?: esc.complicacaoId} (${if (esc.comoMaior) "Maior" else "Menor"})", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                                                                TextButton(onClick = { varianteComplicacoesAdicionadas = varianteComplicacoesAdicionadas - esc }, contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)) {
                                                                                    Text("Remover", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                                "Atributo" -> {
                                                    Text(
                                                        "Cria um novo Atributo pra esta campanha (ex.: uma característica extra numa variante de regra própria). Começa em d4, igual aos demais, e aparece na ficha de todo personagem.",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                "Perícia" -> {
                                                    val atributosDisponiveis = remember(state.mapaAtributosDisplay) {
                                                        state.mapaAtributosDisplay.values.distinct().sorted()
                                                    }
                                                    if (customPericiaAtributoVinculado == null || customPericiaAtributoVinculado !in atributosDisponiveis) {
                                                        customPericiaAtributoVinculado = atributosDisponiveis.firstOrNull()
                                                    }
                                                    LabeledChipGroup("Atributo vinculado:") {
                                                        atributosDisponiveis.forEach { nomeAtr ->
                                                            androidx.compose.material3.FilterChip(
                                                                selected = customPericiaAtributoVinculado == nomeAtr,
                                                                onClick = { customPericiaAtributoVinculado = nomeAtr },
                                                                label = { Text(nomeAtr, style = MaterialTheme.typography.labelSmall) }
                                                            )
                                                        }
                                                    }
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Checkbox(checked = customPericiaBasica, onCheckedChange = { customPericiaBasica = it })
                                                        Text(
                                                            "Perícia básica (todo personagem já começa com d4 de graça)",
                                                            style = MaterialTheme.typography.bodySmall
                                                        )
                                                    }
                                                }
                                            }

                                            // Common Description field
                                            androidx.compose.material3.OutlinedTextField(
                                                value = customItemDesc,
                                                onValueChange = { customItemDesc = it },
                                                label = { Text("Descrição / Efeitos") },
                                                modifier = Modifier.fillMaxWidth().height(90.dp),
                                                maxLines = 4
                                            )
                                        }
                                    }

                                    // Custom Content Items Manager List (todos os livros)
                                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                                    Text(
                                        text = "Itens Customizados (todos os livros)",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )

                                    val allCustomItems = remember(activeBookCustomData) {
                                        buildList {
                                            activeBookCustomData.vantagens.forEach { add("Vantagem" to it.nome) }
                                            activeBookCustomData.complicacoes.forEach { add("Complicação" to it.name) }
                                            activeBookCustomData.equipamentos.forEach { add("Equipamento" to it.nome) }
                                            activeBookCustomData.poderes.forEach { add("Poder" to it.nome) }
                                            activeBookCustomData.superPoderes.forEach { add("Super Poder" to it.nome) }
                                            activeBookCustomData.racas.forEach { add("Raça" to it.nome) }
                                            activeBookCustomData.habilidadesRaciais.forEach { add("Traço Racial" to it.nome) }
                                            activeBookCustomData.variantesRaciais.forEach { add("Variante de Raça" to it.nome) }
                                            activeBookCustomData.atributosCustomizados.forEach { add("Atributo" to it.nome) }
                                            activeBookCustomData.periciasCustomizadas.forEach { add("Perícia" to it.nome) }
                                            activeBookCustomData.modificadoresCustomizados.forEach { add("Modificador de Poder" to "${it.nome} (→ ${it.poderAlvoNome})") }
                                        }
                                    }

                                    if (allCustomItems.isEmpty()) {
                                        Text(
                                            text = "Nenhum item customizado criado neste livro ainda.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    } else {
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            allCustomItems.forEach { (type, name) ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "[$type] $name ⓒ",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontWeight = FontWeight.Medium,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    if (type in setOf("Vantagem", "Equipamento", "Poder", "Super Poder", "Complicação")) {
                                                        TextButton(onClick = { moverCategoriaTarget = type to name }) {
                                                            Text("Categoria", style = MaterialTheme.typography.labelSmall)
                                                        }
                                                    }
                                                    TextButton(onClick = {
                                                        // Apaga em TODOS os livros de armazenamento: o mesmo item pode ter
                                                        // sido salvo sob várias tags (ver selectedBookTags na criação), e
                                                        // cada chamada de delete é um no-op inofensivo nos livros onde o
                                                        // item não existe.
                                                        // Sincroniza também state.lista* (não só o disco): sem isso, o item
                                                        // apagado continuava selecionável nas telas de ficha até o app
                                                        // recarregar os dados do zero, e um novo item criado com o mesmo
                                                        // nome logo em seguida seria barrado por falso positivo de colisão.
                                                        when (type) {
                                                            "Vantagem" -> {
                                                                val item = activeBookCustomData.vantagens.firstOrNull { it.nome == name }
                                                                item?.let { i ->
                                                                    todosOsLivrosDeArmazenamento.forEach { customStorageManager.deleteVantagem(context, it, i.id) }
                                                                    state.listaVantagens = state.listaVantagens.filterNot { it.id == i.id }
                                                                }
                                                            }
                                                            "Complicação" -> {
                                                                val item = activeBookCustomData.complicacoes.firstOrNull { it.name == name }
                                                                item?.let { i ->
                                                                    todosOsLivrosDeArmazenamento.forEach { customStorageManager.deleteComplicacao(context, it, i.id) }
                                                                    state.listaComplicacoes = state.listaComplicacoes.filterNot { it.id == i.id }
                                                                }
                                                            }
                                                            "Equipamento" -> {
                                                                todosOsLivrosDeArmazenamento.forEach { customStorageManager.deleteEquipamento(context, it, name) }
                                                                state.listaEquipamentos = state.listaEquipamentos.filterNot { it.nome.equals(name, ignoreCase = true) }
                                                            }
                                                            "Poder" -> {
                                                                val item = activeBookCustomData.poderes.firstOrNull { it.nome == name }
                                                                item?.let { i ->
                                                                    todosOsLivrosDeArmazenamento.forEach { customStorageManager.deletePoder(context, it, i.id) }
                                                                    state.listaPoderes = state.listaPoderes.filterNot { it.id == i.id }
                                                                }
                                                            }
                                                            "Super Poder" -> {
                                                                todosOsLivrosDeArmazenamento.forEach { customStorageManager.deleteSuperPoder(context, it, name) }
                                                                state.listaSuperPoderes = state.listaSuperPoderes.filterNot { it.nome.equals(name, ignoreCase = true) }
                                                            }
                                                            "Raça" -> {
                                                                todosOsLivrosDeArmazenamento.forEach { customStorageManager.deleteRaca(context, it, name) }
                                                                state.listaAncestralidadesJson = state.listaAncestralidadesJson.filterNot { it.nome.equals(name, ignoreCase = true) }
                                                            }
                                                            "Traço Racial" -> todosOsLivrosDeArmazenamento.forEach { customStorageManager.deleteHabilidadeRacial(context, it, name) }
                                                            "Variante de Raça" -> {
                                                                val item = activeBookCustomData.variantesRaciais.firstOrNull { it.nome == name }
                                                                item?.let { i ->
                                                                    todosOsLivrosDeArmazenamento.forEach { customStorageManager.deleteVarianteRacial(context, it, i.id) }
                                                                    state.listaVariantesRaciaisCustom = state.listaVariantesRaciaisCustom.filterNot { v -> v.id == i.id }
                                                                }
                                                            }
                                                            "Atributo" -> {
                                                                todosOsLivrosDeArmazenamento.forEach { customStorageManager.deleteAtributoCustomizado(context, it, name) }
                                                                state.removeCustomAtributo(name)
                                                            }
                                                            "Perícia" -> {
                                                                todosOsLivrosDeArmazenamento.forEach { customStorageManager.deletePericiaCustomizada(context, it, name) }
                                                                state.removeCustomPericia(name)
                                                            }
                                                            "Modificador de Poder" -> {
                                                                val item = activeBookCustomData.modificadoresCustomizados.firstOrNull { "${it.nome} (→ ${it.poderAlvoNome})" == name }
                                                                item?.let { i ->
                                                                    todosOsLivrosDeArmazenamento.forEach { customStorageManager.deleteModificadorCustomizado(context, it, i.id) }
                                                                    state.removeCustomModificador(i)
                                                                }
                                                            }
                                                        }
                                                        refreshTrigger++
                                                        onCustomContentChanged()
                                                    }) {
                                                        Text("Deletar", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    moverCategoriaTarget?.let { (moveType, moveName) ->
                                        val tipoEntidade = when (moveType) {
                                            "Vantagem" -> com.example.swadebuilder.model.TipoEntidadeCategoria.VANTAGEM
                                            "Equipamento" -> com.example.swadebuilder.model.TipoEntidadeCategoria.EQUIPAMENTO
                                            "Poder" -> com.example.swadebuilder.model.TipoEntidadeCategoria.PODER
                                            "Super Poder" -> com.example.swadebuilder.model.TipoEntidadeCategoria.SUPER_PODER
                                            else -> com.example.swadebuilder.model.TipoEntidadeCategoria.COMPLICACAO
                                        }
                                        val categoriasDoTipo = activeBookCustomData.categoriasCustomizadas.filter { it.tipoEntidade == tipoEntidade }
                                        val categoriaAtualId = when (moveType) {
                                            "Vantagem" -> activeBookCustomData.vantagens.firstOrNull { it.nome == moveName }?.categoriaCustomizadaId
                                            "Equipamento" -> activeBookCustomData.equipamentos.firstOrNull { it.nome.equals(moveName, ignoreCase = true) }?.categoriaCustomizadaId
                                            "Poder" -> activeBookCustomData.poderes.firstOrNull { it.nome == moveName }?.categoriaCustomizadaId
                                            "Super Poder" -> activeBookCustomData.superPoderes.firstOrNull { it.nome.equals(moveName, ignoreCase = true) }?.categoriaCustomizadaId
                                            else -> activeBookCustomData.complicacoes.firstOrNull { it.name == moveName }?.categoriaCustomizadaId
                                        }
                                        com.example.swadebuilder.ui.components.MoverCategoriaDialog(
                                            itemNome = moveName,
                                            categorias = categoriasDoTipo,
                                            categoriaAtualId = categoriaAtualId,
                                            onConfirm = { novaCategoriaId ->
                                                when (moveType) {
                                                    "Vantagem" -> {
                                                        val item = activeBookCustomData.vantagens.firstOrNull { it.nome == moveName }
                                                        item?.let { i ->
                                                            val novaCategoriaEnum = if (novaCategoriaId != null) com.example.swadebuilder.model.Categoria.CUSTOMIZADA else com.example.swadebuilder.model.Categoria.PROFISSIONAL
                                                            todosOsLivrosDeArmazenamento.forEach { tag ->
                                                                val existente = customStorageManager.loadCustomContent(context, tag).vantagens.firstOrNull { v -> v.id == i.id }
                                                                if (existente != null) {
                                                                    customStorageManager.addVantagem(context, tag, existente.copy(categoria = novaCategoriaEnum, categoriaCustomizadaId = novaCategoriaId))
                                                                }
                                                            }
                                                            state.listaVantagens = state.listaVantagens.map { v -> if (v.id == i.id) v.copy(categoria = novaCategoriaEnum, categoriaCustomizadaId = novaCategoriaId) else v }
                                                        }
                                                    }
                                                    "Equipamento" -> {
                                                        todosOsLivrosDeArmazenamento.forEach { tag ->
                                                            val existente = customStorageManager.loadCustomContent(context, tag).equipamentos.firstOrNull { e -> e.nome.equals(moveName, ignoreCase = true) }
                                                            if (existente != null) {
                                                                customStorageManager.addEquipamento(context, tag, existente.copy(categoriaCustomizadaId = novaCategoriaId))
                                                            }
                                                        }
                                                        state.listaEquipamentos = state.listaEquipamentos.map { e -> if (e.nome.equals(moveName, ignoreCase = true)) e.copy(categoriaCustomizadaId = novaCategoriaId) else e }
                                                    }
                                                    "Poder" -> {
                                                        val item = activeBookCustomData.poderes.firstOrNull { it.nome == moveName }
                                                        item?.let { i ->
                                                            todosOsLivrosDeArmazenamento.forEach { tag ->
                                                                val existente = customStorageManager.loadCustomContent(context, tag).poderes.firstOrNull { p -> p.id == i.id }
                                                                if (existente != null) {
                                                                    customStorageManager.addPoder(context, tag, existente.copy(categoriaCustomizadaId = novaCategoriaId))
                                                                }
                                                            }
                                                            state.listaPoderes = state.listaPoderes.map { p -> if (p.id == i.id) p.copy(categoriaCustomizadaId = novaCategoriaId) else p }
                                                        }
                                                    }
                                                    "Super Poder" -> {
                                                        todosOsLivrosDeArmazenamento.forEach { tag ->
                                                            val existente = customStorageManager.loadCustomContent(context, tag).superPoderes.firstOrNull { sp -> sp.nome.equals(moveName, ignoreCase = true) }
                                                            if (existente != null) {
                                                                customStorageManager.addSuperPoder(context, tag, existente.copy(categoriaCustomizadaId = novaCategoriaId))
                                                            }
                                                        }
                                                        state.listaSuperPoderes = state.listaSuperPoderes.map { sp -> if (sp.nome.equals(moveName, ignoreCase = true)) sp.copy(categoriaCustomizadaId = novaCategoriaId) else sp }
                                                    }
                                                    else -> {
                                                        val item = activeBookCustomData.complicacoes.firstOrNull { it.name == moveName }
                                                        item?.let { i ->
                                                            todosOsLivrosDeArmazenamento.forEach { tag ->
                                                                val existente = customStorageManager.loadCustomContent(context, tag).complicacoes.firstOrNull { c -> c.id == i.id }
                                                                if (existente != null) {
                                                                    customStorageManager.addComplicacao(context, tag, existente.copy(categoriaCustomizadaId = novaCategoriaId))
                                                                }
                                                            }
                                                            state.listaComplicacoes = state.listaComplicacoes.map { c -> if (c.id == i.id) c.copy(categoriaCustomizadaId = novaCategoriaId) else c }
                                                        }
                                                    }
                                                }
                                                refreshTrigger++
                                                onCustomContentChanged()
                                                moverCategoriaTarget = null
                                            },
                                            onDismiss = { moverCategoriaTarget = null }
                                        )
                                    }
                                }
                            },
                            confirmButton = {
                                Column(horizontalAlignment = Alignment.End) {
                                    // Fora da área rolável (diferente de onde estava antes, dentro do
                                    // Column de `text`): fica sempre visível colado nos botões, sem
                                    // precisar rolar a tela pra descobrir se salvou ou por que não salvou.
                                    if (statusMessage != null) {
                                        val isErro = statusMessage!!.let {
                                            it.startsWith("Erro") || it.startsWith("Preencha") ||
                                                it.startsWith("Selecione") || it.contains("precisa fechar")
                                        }
                                        Text(
                                            text = statusMessage!!,
                                            color = if (isErro) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(bottom = 4.dp, end = 4.dp)
                                        )
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    TextButton(onClick = {
                                                val safeDesc = customItemDesc.ifBlank { "-" }
                                                if (customItemName.isNotBlank()) {
                                            // toIdSlug() normaliza acento/espaço/pontuação (ver StringExtensions.kt) — evita
                                            // que "Fogo" e "Fôgo" gerem ids diferentes, ou que "Fogo do Inferno" e
                                            // "fogo   do inferno" colidam sem o app perceber.
                                            val id = "custom:${customItemName.toIdSlug()}"
                                            val normalizedName = customItemName.keyify()
                                            // Livro(s) escolhidos no Seletor de Livros; se nada foi marcado, cai no
                                            // livro atualmente ativo pra não perder a criação.
                                            val tags = selectedBookTags.ifEmpty {
                                                setOf(state.getActiveOrigins().firstOrNull() ?: "BASICO")
                                            }
                                            val tagsLabel = tags.joinToString(", ") {
                                                if (it == com.example.swadebuilder.util.TAG_GERAL) "Geral" else it.toEditionDisplayName()
                                            }
                                            // Bloqueia colisão de nome/id antes de salvar: CustomStorageManager.addXxx()
                                            // sobrescreve silenciosamente por id/nome no disco, enquanto
                                            // CriadorState.addCustomXxx() ignora silenciosamente a nova entrada se o
                                            // id/nome já existir em memória — os dois lados divergiam sem esse guard.
                                            // Compara contra o catálogo oficial + custom ativo (state.lista*) e contra
                                            // TODO o conteúdo customizado já salvo em qualquer livro (activeBookCustomData),
                                            // pra nunca deixar dois itens com o mesmo nome/id coexistirem.
                                            val colisao: String? = when (selectedCategory) {
                                                "Vantagem", "Antecedente Arcano" ->
                                                    if (state.listaVantagens.any { it.id == id } || activeBookCustomData.vantagens.any { it.id == id }) "Vantagem" else null
                                                "Complicação" ->
                                                    if (state.listaComplicacoes.any { it.id == id } || activeBookCustomData.complicacoes.any { it.id == id }) "Complicação" else null
                                                "Equipamento" ->
                                                    if (state.listaEquipamentos.any { it.nome.keyify() == normalizedName } || activeBookCustomData.equipamentos.any { it.nome.keyify() == normalizedName }) "Equipamento" else null
                                                "Poder" ->
                                                    if (state.listaPoderes.any { it.id == id } || activeBookCustomData.poderes.any { it.id == id }) "Poder" else null
                                                "Super Poder" ->
                                                    if (state.listaSuperPoderes.any { it.nome.keyify() == normalizedName } || activeBookCustomData.superPoderes.any { it.nome.keyify() == normalizedName }) "Super Poder" else null
                                                "Raça" ->
                                                    if (state.listaAncestralidadesJson.any { it.nome.keyify() == normalizedName } || activeBookCustomData.racas.any { it.nome.keyify() == normalizedName }) "Raça" else null
                                                "Traço Racial" ->
                                                    if (baseRacialCatalog.any { it.nome.keyify() == normalizedName } || activeBookCustomData.habilidadesRaciais.any { it.nome.keyify() == normalizedName }) "Traço Racial" else null
                                                "Variante de Raça" ->
                                                    if (state.listaVariantesRaciaisCustom.any { it.id == id } || activeBookCustomData.variantesRaciais.any { it.id == id }) "Variante de Raça" else null
                                                "Atributo" ->
                                                    if (state.mapaAtributosDisplay.values.any { it.equals(customItemName, ignoreCase = true) } || activeBookCustomData.atributosCustomizados.any { it.nome.equals(customItemName, ignoreCase = true) }) "Atributo" else null
                                                "Perícia" ->
                                                    if (state.listaPericias.any { it.nome.equals(customItemName, ignoreCase = true) } || activeBookCustomData.periciasCustomizadas.any { it.nome.equals(customItemName, ignoreCase = true) }) "Perícia" else null
                                                else -> null
                                            }
                                            if (colisao != null) {
                                                statusMessage = "Erro: já existe um(a) $colisao chamado(a) '$customItemName'. Escolha outro nome."
                                            } else {
                                            when (selectedCategory) {
                                                "Vantagem" -> {
                                                    val combinedPrevEdges = customPrereqEdges + customPrereqComps
                                                    val reqObj = Requisito(
                                                        estagio = customStage,
                                                        atributoMin = customAttrMin,
                                                        periciaMin = customSkillMin,
                                                        vantagensPrevias = combinedPrevEdges,
                                                        observacoes = customRequirements,
                                                        categoriasCustomizadasRequeridas = customPrereqCategoriasCustomizadas.toList()
                                                    )
                                                    val newAdv = com.example.swadebuilder.model.Vantagem(
                                                        id = id,
                                                        nome = customItemName,
                                                        categoria = customAdvCategory,
                                                        categoriaCustomizadaId = customAdvCategoriaCustomizadaId,
                                                        descricao = safeDesc,
                                                        origem = tags.first(),
                                                        requisitos = reqObj
                                                    )
                                                    tags.forEach { tag -> customStorageManager.addVantagem(context, tag, newAdv.copy(origem = tag)) }
                                                    state.addCustomVantagem(newAdv)
                                                            statusMessage = "Vantagem '$customItemName' salva em: $tagsLabel"
                                                }
                                                "Complicação" -> {
                                                    val newComp = com.example.swadebuilder.model.Complicacao(
                                                        id = id,
                                                        name = customItemName,
                                                        severity = customSeverity,
                                                                description = safeDesc,
                                                                origem = tags.first(),
                                                                categoriaCustomizadaId = customComplicacaoCategoriaId
                                                    )
                                                            tags.forEach { tag -> customStorageManager.addComplicacao(context, tag, newComp.copy(origem = tag)) }
                                                    state.addCustomComplicacao(newComp)
                                                            statusMessage = "Complicação '$customItemName' salva em: $tagsLabel"
                                                }
                                                "Equipamento" -> {
                                                    val newEquip = equipForm.build(customItemName, safeDesc, tags.first(), id)
                                                            tags.forEach { tag -> customStorageManager.addEquipamento(context, tag, newEquip.copy(origem = tag)) }
                                                    state.addCustomEquipamento(newEquip)
                                                            statusMessage = "Equipamento '$customItemName' salvo em: $tagsLabel"
                                                }
                                                "Poder" -> {
                                                    val newPoder = com.example.swadebuilder.model.Poder(
                                                        id = id,
                                                        nome = customItemName,
                                                        pontosDePoder = customPp.ifBlank { "1" },
                                                        distancia = customRange.ifBlank { "Toque" },
                                                        duracao = customDuration.ifBlank { "3 turnos" },
                                                                descricao = safeDesc,
                                                        estagio = "Novato",
                                                                origem = tags.first(),
                                                                categoriaCustomizadaId = customPoderCategoriaId
                                                    )
                                                            tags.forEach { tag -> customStorageManager.addPoder(context, tag, newPoder.copy(origem = tag)) }
                                                    state.addCustomPoder(newPoder)
                                                            statusMessage = "Poder '$customItemName' salvo em: $tagsLabel"
                                                }
                                                "Super Poder" -> {
                                                    val newSuperPoder = com.example.swadebuilder.model.SuperPoder(
                                                        nome = customItemName,
                                                        custoBase = customSuperPoderCustoBase.ifBlank { "2" },
                                                        descricao = safeDesc,
                                                        modificadores = customSuperPoderModificadoresList.ifEmpty { null },
                                                        id = id,
                                                        categoriaCustomizadaId = customSuperPoderCategoriaId
                                                    )
                                                    tags.forEach { tag -> customStorageManager.addSuperPoder(context, tag, newSuperPoder) }
                                                    state.addCustomSuperPoder(newSuperPoder)
                                                    statusMessage = "Super Poder '$customItemName' salvo em: $tagsLabel"
                                                }
                                                "Modificador de Poder" -> {
                                                    val poderAlvo = customModificadorPoderAlvoNome
                                                    if (poderAlvo == null) {
                                                        statusMessage = "Escolha o Super Poder alvo do modificador."
                                                    } else {
                                                        val newModificador = com.example.swadebuilder.model.ModificadorCustomizado(
                                                            id = "custom:mod:${poderAlvo.toIdSlug()}:${customItemName.toIdSlug()}",
                                                            poderAlvoNome = poderAlvo,
                                                            nome = customItemName,
                                                            custo = customModificadorCusto.ifBlank { "+0" },
                                                            descricao = safeDesc
                                                        )
                                                        tags.forEach { tag -> customStorageManager.addModificadorCustomizado(context, tag, newModificador) }
                                                        state.addCustomModificador(newModificador)
                                                        statusMessage = "Modificador '$customItemName' anexado a '$poderAlvo' em: $tagsLabel"
                                                    }
                                                }
                                                "Antecedente Arcano" -> {
                                                    // Vira uma Vantagem categoria ANTECEDENTE de verdade — reaproveita
                                                    // 100% do pipeline de Vantagem customizada (armazenamento, merge,
                                                    // seleção na ficha). O nome "ANTECEDENTE ARCANO (X)" é reconhecido
                                                    // por Vantagem.toArcanoKey() (ver ArcanoExtensions.kt, fallback
                                                    // adicionado pra AAs customizados), e poderesPermitidos vazio =
                                                    // lista aberta (todos os poderes do `origem`), ou preenchido =
                                                    // só os poderes escolhidos — os dois lidos nativamente por
                                                    // PoderesSection.kt sem precisar de nenhum código novo lá.
                                                    val newAA = com.example.swadebuilder.model.Vantagem(
                                                        id = id,
                                                        nome = "ANTECEDENTE ARCANO ($customItemName)",
                                                        categoria = Categoria.ANTECEDENTE,
                                                        descricao = safeDesc,
                                                        origem = tags.first(),
                                                        requisitos = Requisito(estagio = "Novato"),
                                                        poderesPermitidos = if (customAaListaAberta) emptyList() else customAaPoderesEspecificos.toList()
                                                    )
                                                    tags.forEach { tag -> customStorageManager.addVantagem(context, tag, newAA.copy(origem = tag)) }
                                                    state.addCustomVantagem(newAA)
                                                    statusMessage = "Antecedente Arcano '$customItemName' salvo em: $tagsLabel"
                                                }
                                                "Raça" -> {
                                                    val raceAbilities = if (selectedRacialTraits.isNotEmpty()) {
                                                        selectedRacialTraits.map { trait ->
                                                            com.example.swadebuilder.model.RacialAbility(
                                                                nome = trait.nome,
                                                                descricao = trait.descricao,
                                                                // Id real do catálogo, nunca derivado do nome de
                                                                // exibição — ver o mesmo ajuste em CriadorState.kt
                                                                // (applyCustomAncestryVariantIfSelected).
                                                                id = trait.id ?: trait.nome.toIdSlug(),
                                                                category = if (trait.custo >= 0) "racial_trait_positive" else "racial_trait_negative",
                                                                vezes = trait.vezes,
                                                                traitId = trait.traitId,
                                                                targetRef = trait.targetRef,
                                                                value = trait.value
                                                            )
                                                        }
                                                    } else {
                                                        listOf(
                                                            com.example.swadebuilder.model.RacialAbility(
                                                                nome = "Traço Customizado",
                                                                descricao = safeDesc,
                                                                id = "${id}_trait",
                                                                category = "racial_trait_positive"
                                                            )
                                                        )
                                                    }
                                                    // Atributos Mínimos/Perícias Iniciais viram traços ATTRIBUTE_BOOST/
                                                    // SKILL_BOOST em habilidades[] — RacialModifier não tem mais mapas
                                                    // numéricos `atributos`/`pericias` em paralelo (removidos: eram
                                                    // redundantes com um traço de id resolvível pra toda raça oficial
                                                    // já auditada, ver RacialTraitPointCatalog.EFEITOS). O picker
                                                    // guarda o dado absoluto (4/6/8/10/12) só porque é mais intuitivo
                                                    // de mostrar pro jogador ("Vigor d6" em vez de "Vigor, 1 passo");
                                                    // `value` do traço sintético é sempre "passos acima de d4"
                                                    // (dado-4)/2, a mesma unidade que AtributoStep/PericiaStep usam.
                                                    val atributoTracos = racaAtributosMin.filterValues { it > 4 }.map { (attr, dado) ->
                                                        com.example.swadebuilder.model.RacialAbility(
                                                            nome = "Atributo Aumentado: $attr",
                                                            descricao = "",
                                                            traitId = "ATTRIBUTE_BOOST",
                                                            targetRef = attr,
                                                            value = (dado - 4) / 2,
                                                            invisivel = true
                                                        )
                                                    }
                                                    val periciaTracos = racaPericiasIniciais.filterValues { it > 4 }.map { (per, dado) ->
                                                        com.example.swadebuilder.model.RacialAbility(
                                                            nome = "Perícia Inicial: $per",
                                                            descricao = "",
                                                            traitId = "SKILL_BOOST",
                                                            targetRef = per,
                                                            value = (dado - 4) / 2,
                                                            invisivel = true
                                                        )
                                                    }
                                                    val newRace = com.example.swadebuilder.model.RacialModifier(
                                                        id = id,
                                                        nome = customItemName,
                                                        descricao = safeDesc,
                                                        movimentacao = racaMovimentacao.toIntOrNull() ?: 0,
                                                        origem = tags.first(),
                                                        habilidades = raceAbilities + atributoTracos + periciaTracos
                                                    )
                                                    tags.forEach { tag -> customStorageManager.addRaca(context, tag, newRace.copy(origem = tag)) }
                                                    state.listaAncestralidadesJson = state.listaAncestralidadesJson + newRace
                                                    statusMessage = "Raça '$customItemName' salva em: $tagsLabel"
                                                }
                                                "Traço Racial" -> {
                                                    val costInt = customTraitCost.toIntOrNull() ?: 1
                                                    val efeitoMagnitude = customTraitEfeitoValor.toIntOrNull()?.let { kotlin.math.abs(it) } ?: 0
                                                    val (efeitoTraitId, efeitoValue) = when (customTraitEfeitoTipo) {
                                                        "Bônus de Pontos de Perícia" -> "PERICIA_POINTS_BONUS" to efeitoMagnitude
                                                        "Penalidade de Pontos de Perícia" -> "PERICIA_POINTS_BONUS" to -efeitoMagnitude
                                                        "Bônus de Pontos de Atributo" -> "ATRIBUTO_POINTS_BONUS" to efeitoMagnitude
                                                        "Penalidade de Pontos de Atributo" -> "ATRIBUTO_POINTS_BONUS" to -efeitoMagnitude
                                                        else -> null to 0
                                                    }
                                                    val newTrait = com.example.swadebuilder.model.HabilidadeCriacao(
                                                        nome = customItemName,
                                                        custo = costInt,
                                                        descricao = safeDesc,
                                                        id = id,
                                                        traitId = efeitoTraitId,
                                                        value = efeitoValue
                                                    )
                                                    tags.forEach { tag -> customStorageManager.addHabilidadeRacial(context, tag, newTrait) }
                                                    statusMessage = "Traço racial '$customItemName' salvo em: $tagsLabel"
                                                }
                                                "Variante de Raça" -> {
                                                    val baseRaca = varianteBaseRacaId?.let { bid -> state.listaAncestralidadesJson.firstOrNull { it.nome.keyify() == bid } }
                                                    if (baseRaca == null) {
                                                        statusMessage = "Selecione a raça base da Variante."
                                                    } else {
                                                        val habilidadeItems = com.example.swadebuilder.model.usecase.ResolveVariantPointBudgetUseCase.itensRemoviveisDe(baseRaca)
                                                            .filter { it.habilidadeId != null }

                                                        val itensRemovidosSelecionados = habilidadeItems.filter { it.habilidadeId in varianteTracosRemovidos }

                                                        val itensAdicionadosSelecionados = buildList {
                                                            varianteTracosAdicionados.forEach { trait ->
                                                                add(com.example.swadebuilder.model.usecase.VariantBudgetItem(label = trait.nome, custo = trait.custo, habilidadeId = trait.nome.lowercase().replace(" ", "_")))
                                                            }
                                                            varianteVantagensAdicionadas.forEach { vid ->
                                                                state.listaVantagens.firstOrNull { it.id == vid }?.let {
                                                                    add(com.example.swadebuilder.model.usecase.ResolveVariantPointBudgetUseCase.vantagemComoItemAdicionado(it))
                                                                }
                                                            }
                                                            varianteComplicacoesAdicionadas.forEach { esc ->
                                                                state.listaComplicacoes.firstOrNull { it.id == esc.complicacaoId }?.let {
                                                                    add(com.example.swadebuilder.model.usecase.ResolveVariantPointBudgetUseCase.complicacaoComoItemAdicionado(it, esc.comoMaior))
                                                                }
                                                            }
                                                        }

                                                        val valorBaseRaca = com.example.swadebuilder.model.usecase.ResolveVariantPointBudgetUseCase.valorTotalDe(baseRaca)
                                                        val budgetResult = com.example.swadebuilder.model.usecase.ResolveVariantPointBudgetUseCase().resolve(
                                                            valorBaseRaca, itensRemovidosSelecionados, itensAdicionadosSelecionados,
                                                            orcamento = baseRaca.pontosRaciaisEsperados,
                                                            semLimite = varianteSemLimite
                                                        )

                                                        if (!budgetResult.dentroDoOrcamento) {
                                                            statusMessage = "A Variante precisa fechar em exatamente ${budgetResult.orcamento} pontos (pontos atuais: ${budgetResult.saldo}), ou marque 'Sem limite de pontos'."
                                                        } else {
                                                            val newVariant = com.example.swadebuilder.model.CustomAncestryVariant(
                                                                id = id,
                                                                ancestralidadeId = varianteBaseRacaId!!,
                                                                nome = customItemName,
                                                                descricao = safeDesc,
                                                                tracosRemovidosIds = varianteTracosRemovidos,
                                                                tracosAdicionados = varianteTracosAdicionados,
                                                                vantagensAdicionadasIds = varianteVantagensAdicionadas,
                                                                complicacoesAdicionadas = varianteComplicacoesAdicionadas,
                                                                semLimiteDePontos = varianteSemLimite
                                                            )
                                                            tags.forEach { tag -> customStorageManager.addVarianteRacial(context, tag, newVariant) }
                                                            state.listaVariantesRaciaisCustom = state.listaVariantesRaciaisCustom + newVariant
                                                            statusMessage = "Variante '$customItemName' salva em: $tagsLabel"
                                                            varianteBaseRacaId = null
                                                            varianteTracosRemovidos = emptyList()
                                                            varianteTracosAdicionados = emptyList()
                                                            varianteVantagensAdicionadas = emptyList()
                                                            varianteComplicacoesAdicionadas = emptyList()
                                                            varianteSemLimite = false
                                                        }
                                                    }
                                                }
                                                "Atributo" -> {
                                                    val newAtributo = com.example.swadebuilder.model.AtributoJson(
                                                        nome = customItemName,
                                                        min = 4,
                                                        descricao = safeDesc
                                                    )
                                                    tags.forEach { tag -> customStorageManager.addAtributoCustomizado(context, tag, newAtributo) }
                                                    state.addCustomAtributo(newAtributo)
                                                    statusMessage = "Atributo '$customItemName' salvo em: $tagsLabel"
                                                }
                                                "Perícia" -> {
                                                    val newPericia = com.example.swadebuilder.model.PericiaJson(
                                                        nome = customItemName,
                                                        atributo = customPericiaAtributoVinculado ?: "Força",
                                                        basica = customPericiaBasica,
                                                        origem = tags.first(),
                                                        descricao = safeDesc,
                                                        id = id
                                                    )
                                                    tags.forEach { tag -> customStorageManager.addPericiaCustomizada(context, tag, newPericia.copy(origem = tag)) }
                                                    state.addCustomPericia(newPericia)
                                                    statusMessage = "Perícia '$customItemName' salva em: $tagsLabel"
                                                }
                                            }
                                                    refreshTrigger++
                                            onCustomContentChanged()
                                            customItemName = ""
                                            customItemDesc = ""
                                            customRequirements = ""
                                            customAttrMin = emptyMap()
                                            customSkillMin = emptyMap()
                                            customPrereqEdges = emptyList()
                                            customPrereqComps = emptyList()
                                            equipForm.reset()
                                            racaAtributosMin = emptyMap()
                                            racaPericiasIniciais = emptyMap()
                                            racaMovimentacao = "0"
                                            customRacialTrait = ""
                                            selectedRacialTraits = emptyList()
                                            customAaPoderesEspecificos = emptySet()
                                            customPericiaAtributoVinculado = null
                                            customPericiaBasica = false
                                            customModificadorPoderAlvoNome = null
                                            customModificadorCusto = "+2"
                                            }
                                        } else {
                                                    statusMessage = "Preencha o Nome do item."
                                        }
                                            }) { Text("Salvar Item") }
                                    TextButton(onClick = onDismiss) { Text("Fechar") }
                                }
                                }
                            }
                        )

                        // Requirement Selector Modals
                        if (showAttrDialog) {
                            // Vem de state.mapaAtributosDisplay (não mais uma lista fixa de 5) pra
                            // incluir também Atributos Customizados (ver CriadorState.addCustomAtributo)
                            // como opção de pré-requisito — a ordem de inserção já deixa os 5
                            // oficiais primeiro, seguidos dos customizados.
                            val attrs = state.mapaAtributosDisplay.entries.map { it.key to it.value }
                            val steps = listOf(0, 4, 6, 8, 10, 12, 13)
                            AlertDialog(
                                onDismissRequest = { showAttrDialog = false },
                                title = { Text("Atributos Mínimos") },
                                text = {
                                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                        attrs.forEach { (key, name) ->
                                            val currentDie = customAttrMin[key] ?: 0
                                            val currentIndex = steps.indexOf(currentDie).coerceAtLeast(0)
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = name,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    IconButton(
                                                        onClick = {
                                                            if (currentIndex > 0) {
                                                                val newDie = steps[currentIndex - 1]
                                                                val mut = customAttrMin.toMutableMap()
                                                                if (newDie == 0) mut.remove(key) else mut[key] = newDie
                                                                customAttrMin = mut
                                                            }
                                                        },
                                                        enabled = currentIndex > 0
                                                    ) {
                                                        Icon(Icons.Default.Remove, contentDescription = "Diminuir")
                                                    }
                                                    Text(
                                                        text = currentDie.toDiceString(),
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        modifier = Modifier.width(48.dp),
                                                        textAlign = TextAlign.Center
                                                    )
                                                    IconButton(
                                                        onClick = {
                                                            if (currentIndex < steps.lastIndex) {
                                                                val newDie = steps[currentIndex + 1]
                                                                val mut = customAttrMin.toMutableMap()
                                                                mut[key] = newDie
                                                                customAttrMin = mut
                                                            }
                                                        },
                                                        enabled = currentIndex < steps.lastIndex
                                                    ) {
                                                        Icon(Icons.Default.Add, contentDescription = "Aumentar")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                                confirmButton = { TextButton(onClick = { showAttrDialog = false }) { Text("OK") } }
                            )
                        }

                        if (showSkillDialog) {
                            val allSkillsList = state.listaPericias.map { it.nome }.distinct().sorted()
                            val steps = listOf(0, 4, 6, 8, 10, 12, 13)
                            var filterSkillText by remember { mutableStateOf("") }
                            AlertDialog(
                                onDismissRequest = { showSkillDialog = false },
                                title = { Text("Perícias Mínimas") },
                                text = {
                                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                        androidx.compose.material3.OutlinedTextField(
                                            value = filterSkillText,
                                            onValueChange = { filterSkillText = it },
                                            label = { Text("Filtrar Perícia") },
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                        )
                                        allSkillsList.filter { it.contains(filterSkillText, ignoreCase = true) }.forEach { skillName ->
                                            val currentDie = customSkillMin[skillName] ?: 0
                                            val currentIndex = steps.indexOf(currentDie).coerceAtLeast(0)
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = skillName,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    IconButton(
                                                        onClick = {
                                                            if (currentIndex > 0) {
                                                                val newDie = steps[currentIndex - 1]
                                                                val mut = customSkillMin.toMutableMap()
                                                                if (newDie == 0) mut.remove(skillName) else mut[skillName] = newDie
                                                                customSkillMin = mut
                                                            }
                                                        },
                                                        enabled = currentIndex > 0
                                                    ) {
                                                        Icon(Icons.Default.Remove, contentDescription = "Diminuir")
                                                    }
                                                    Text(
                                                        text = currentDie.toDiceString(),
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        modifier = Modifier.width(48.dp),
                                                        textAlign = TextAlign.Center
                                                    )
                                                    IconButton(
                                                        onClick = {
                                                            if (currentIndex < steps.lastIndex) {
                                                                val newDie = steps[currentIndex + 1]
                                                                val mut = customSkillMin.toMutableMap()
                                                                mut[skillName] = newDie
                                                                customSkillMin = mut
                                                            }
                                                        },
                                                        enabled = currentIndex < steps.lastIndex
                                                    ) {
                                                        Icon(Icons.Default.Add, contentDescription = "Aumentar")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                                confirmButton = { TextButton(onClick = { showSkillDialog = false }) { Text("OK") } }
                            )
                        }

                        // Mesmo padrão de stepper de showAttrDialog acima, mas gravando em
                        // racaAtributosMin (mínimo de atributo da Raça, ex.: Anão = Vigor d6)
                        // em vez de num requisito de Vantagem.
                        if (showRacaAttrDialog) {
                            val attrs = listOf("AGILIDADE" to "Agilidade", "ASTUCIA" to "Astúcia", "ESPIRITO" to "Espírito", "FORCA" to "Força", "VIGOR" to "Vigor")
                            // Sem "13" (d12+1): o traço ATTRIBUTE_BOOST sintetizado ao salvar guarda
                            // "passos acima de d4" como inteiro — (dado-4)/2 só é exato pros dados
                            // pares abaixo. Um mínimo racial de d12+1 também não faz sentido de
                            // qualquer forma (nenhuma raça oficial começa tão alta).
                            val steps = listOf(0, 4, 6, 8, 10, 12)
                            AlertDialog(
                                onDismissRequest = { showRacaAttrDialog = false },
                                title = { Text("Atributos Mínimos da Raça") },
                                text = {
                                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                        attrs.forEach { (key, name) ->
                                            val currentDie = racaAtributosMin[key] ?: 0
                                            val currentIndex = steps.indexOf(currentDie).coerceAtLeast(0)
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(text = name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    IconButton(
                                                        onClick = {
                                                            if (currentIndex > 0) {
                                                                val newDie = steps[currentIndex - 1]
                                                                val mut = racaAtributosMin.toMutableMap()
                                                                if (newDie == 0) mut.remove(key) else mut[key] = newDie
                                                                racaAtributosMin = mut
                                                            }
                                                        },
                                                        enabled = currentIndex > 0
                                                    ) { Icon(Icons.Default.Remove, contentDescription = "Diminuir") }
                                                    Text(
                                                        text = currentDie.toDiceString(),
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        modifier = Modifier.width(48.dp),
                                                        textAlign = TextAlign.Center
                                                    )
                                                    IconButton(
                                                        onClick = {
                                                            if (currentIndex < steps.lastIndex) {
                                                                val mut = racaAtributosMin.toMutableMap()
                                                                mut[key] = steps[currentIndex + 1]
                                                                racaAtributosMin = mut
                                                            }
                                                        },
                                                        enabled = currentIndex < steps.lastIndex
                                                    ) { Icon(Icons.Default.Add, contentDescription = "Aumentar") }
                                                }
                                            }
                                        }
                                    }
                                },
                                confirmButton = { TextButton(onClick = { showRacaAttrDialog = false }) { Text("OK") } }
                            )
                        }

                        // Mesmo padrão de showSkillDialog acima, gravando em
                        // racaPericiasIniciais (perícia inicial da Raça) em vez de requisito.
                        if (showRacaSkillDialog) {
                            val allSkillsList = state.listaPericias.map { it.nome }.distinct().sorted()
                            // Sem "13" (d12+1) — mesmo motivo do picker de Atributos Mínimos acima.
                            val steps = listOf(0, 4, 6, 8, 10, 12)
                            var filterSkillText by remember { mutableStateOf("") }
                            AlertDialog(
                                onDismissRequest = { showRacaSkillDialog = false },
                                title = { Text("Perícias Iniciais da Raça") },
                                text = {
                                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                        androidx.compose.material3.OutlinedTextField(
                                            value = filterSkillText,
                                            onValueChange = { filterSkillText = it },
                                            label = { Text("Filtrar Perícia") },
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                        )
                                        allSkillsList.filter { it.contains(filterSkillText, ignoreCase = true) }.forEach { skillName ->
                                            val currentDie = racaPericiasIniciais[skillName] ?: 0
                                            val currentIndex = steps.indexOf(currentDie).coerceAtLeast(0)
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(text = skillName, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    IconButton(
                                                        onClick = {
                                                            if (currentIndex > 0) {
                                                                val newDie = steps[currentIndex - 1]
                                                                val mut = racaPericiasIniciais.toMutableMap()
                                                                if (newDie == 0) mut.remove(skillName) else mut[skillName] = newDie
                                                                racaPericiasIniciais = mut
                                                            }
                                                        },
                                                        enabled = currentIndex > 0
                                                    ) { Icon(Icons.Default.Remove, contentDescription = "Diminuir") }
                                                    Text(
                                                        text = currentDie.toDiceString(),
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        modifier = Modifier.width(48.dp),
                                                        textAlign = TextAlign.Center
                                                    )
                                                    IconButton(
                                                        onClick = {
                                                            if (currentIndex < steps.lastIndex) {
                                                                val mut = racaPericiasIniciais.toMutableMap()
                                                                mut[skillName] = steps[currentIndex + 1]
                                                                racaPericiasIniciais = mut
                                                            }
                                                        },
                                                        enabled = currentIndex < steps.lastIndex
                                                    ) { Icon(Icons.Default.Add, contentDescription = "Aumentar") }
                                                }
                                            }
                                        }
                                    }
                                },
                                confirmButton = { TextButton(onClick = { showRacaSkillDialog = false }) { Text("OK") } }
                            )
                        }

                        if (showEdgeDialog) {
                            val availableEdges = state.listaVantagens.distinctBy { it.id }.sortedBy { it.nome }
                            var filterEdgeText by remember { mutableStateOf("") }
                            AlertDialog(
                                onDismissRequest = { showEdgeDialog = false },
                                title = { Text("Vantagens Prévias") },
                                text = {
                                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                        androidx.compose.material3.OutlinedTextField(
                                            value = filterEdgeText,
                                            onValueChange = { filterEdgeText = it },
                                            label = { Text("Filtrar Vantagem") },
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                        )
                                        availableEdges.filter { it.nome.contains(filterEdgeText, ignoreCase = true) }.forEach { edge ->
                                            val isSel = edge.id in customPrereqEdges
                                            Row(
                                                modifier = Modifier.fillMaxWidth().clickable {
                                                    customPrereqEdges = if (isSel) customPrereqEdges - edge.id else customPrereqEdges + edge.id
                                                }.padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Checkbox(checked = isSel, onCheckedChange = {
                                                    customPrereqEdges = if (it) customPrereqEdges + edge.id else customPrereqEdges - edge.id
                                                })
                                                Spacer(Modifier.width(8.dp))
                                                Text(edge.nome, style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                    }
                                },
                                confirmButton = { TextButton(onClick = { showEdgeDialog = false }) { Text("OK") } }
                            )
                        }

                        if (showCompDialog) {
                            val availableComps = state.listaComplicacoes.distinctBy { it.id }.sortedBy { it.name }
                            var filterCompText by remember { mutableStateOf("") }
                            AlertDialog(
                                onDismissRequest = { showCompDialog = false },
                                title = { Text("Complicações Mínimas") },
                                text = {
                                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                        androidx.compose.material3.OutlinedTextField(
                                            value = filterCompText,
                                            onValueChange = { filterCompText = it },
                                            label = { Text("Filtrar Complicação") },
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                        )
                                        availableComps.filter { it.name.contains(filterCompText, ignoreCase = true) }.forEach { comp ->
                                            val isSel = comp.id in customPrereqComps
                                            Row(
                                                modifier = Modifier.fillMaxWidth().clickable {
                                                    customPrereqComps = if (isSel) customPrereqComps - comp.id else customPrereqComps + comp.id
                                                }.padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Checkbox(checked = isSel, onCheckedChange = {
                                                    customPrereqComps = if (it) customPrereqComps + comp.id else customPrereqComps - comp.id
                                                })
                                                Spacer(Modifier.width(8.dp))
                                                Text(comp.name, style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                    }
                                },
                                confirmButton = { TextButton(onClick = { showCompDialog = false }) { Text("OK") } }
                            )
                        }

                        if (showTraitSelectDialog) {
                            val allTraitsCatalog = fullTraitsCatalog.distinctBy { it.grupoEscolha ?: it.nome }
                            var filterTraitText by remember { mutableStateOf("") }
                            AlertDialog(
                                onDismissRequest = { showTraitSelectDialog = false },
                                title = { Text("Selecionar Traços Raciais") },
                                text = {
                                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                        androidx.compose.material3.OutlinedTextField(
                                            value = filterTraitText,
                                            onValueChange = { filterTraitText = it },
                                            label = { Text("Filtrar Traço") },
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                        )
                                        allTraitsCatalog.filter { it.nome.contains(filterTraitText, ignoreCase = true) }.forEach { trait ->
                                            val isSuperPoderesRow = trait.nome == "Super Poderes"
                                            val isPericiaChoiceRow = trait.id in periciaChoiceTraitIds
                                            val isStackableRow = (trait.vezesMax ?: 1) > 1
                                            val isGrupoEscolhaRow = trait.grupoEscolha != null
                                            val isSel = if (isSuperPoderesRow) {
                                                selectedRacialTraits.any { it.nome.startsWith("Super Poderes (") }
                                            } else if (isGrupoEscolhaRow) {
                                                selectedRacialTraits.any { it.grupoEscolha == trait.grupoEscolha }
                                            } else if (isPericiaChoiceRow || isStackableRow) {
                                                selectedRacialTraits.any { it.id == trait.id }
                                            } else {
                                                selectedRacialTraits.any { it.nome.equals(trait.nome, ignoreCase = true) }
                                            }
                                            val onToggle: (Boolean) -> Unit = { checked ->
                                                if (isSuperPoderesRow) {
                                                    if (checked) {
                                                        superPoderRacialPickerTarget = { escolhido ->
                                                            selectedRacialTraits = selectedRacialTraits + escolhido
                                                        }
                                                    } else {
                                                        selectedRacialTraits = selectedRacialTraits.filterNot { it.nome.startsWith("Super Poderes (") }
                                                    }
                                                } else if (isGrupoEscolhaRow) {
                                                    if (checked) {
                                                        groupPickerTarget = trait to { escolhido ->
                                                            selectedRacialTraits = selectedRacialTraits + escolhido
                                                        }
                                                    } else {
                                                        selectedRacialTraits = selectedRacialTraits.filterNot { it.grupoEscolha == trait.grupoEscolha }
                                                    }
                                                } else if (isStackableRow) {
                                                    if (checked) {
                                                        stackPickerTarget = trait to { escolhido ->
                                                            selectedRacialTraits = selectedRacialTraits + escolhido
                                                        }
                                                    } else {
                                                        selectedRacialTraits = selectedRacialTraits.filterNot { it.id == trait.id }
                                                    }
                                                } else if (isPericiaChoiceRow) {
                                                    if (checked) {
                                                        periciaTraitPickerTarget = trait to { escolhido ->
                                                            selectedRacialTraits = selectedRacialTraits + escolhido
                                                        }
                                                    } else {
                                                        selectedRacialTraits = selectedRacialTraits.filterNot { it.id == trait.id }
                                                    }
                                                } else {
                                                    selectedRacialTraits = if (checked) selectedRacialTraits + trait else selectedRacialTraits.filterNot { it.nome.equals(trait.nome, ignoreCase = true) }
                                                }
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth().clickable { onToggle(!isSel) }.padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Checkbox(checked = isSel, onCheckedChange = onToggle)
                                                Spacer(Modifier.width(8.dp))
                                                Column {
                                                    val jaEscolhido = if (isGrupoEscolhaRow) {
                                                        selectedRacialTraits.firstOrNull { it.grupoEscolha == trait.grupoEscolha }
                                                    } else {
                                                        selectedRacialTraits.firstOrNull { it.id == trait.id }
                                                    }
                                                    val rotuloCusto = if ((isStackableRow || isGrupoEscolhaRow) && jaEscolhido != null) {
                                                        "${jaEscolhido.nome} (${if (jaEscolhido.custo > 0) "+${jaEscolhido.custo}" else "${jaEscolhido.custo}"} pts)"
                                                    } else {
                                                        "${trait.nome} (${if (trait.custo > 0) "+${trait.custo}" else "${trait.custo}"} pts)"
                                                    }
                                                    Text(rotuloCusto, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                                    if (trait.descricao.isNotBlank()) {
                                                        Text(trait.descricao, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                                confirmButton = { TextButton(onClick = { showTraitSelectDialog = false }) { Text("OK") } }
                            )
                        }

                        if (showVarianteBaseRacaDialog) {
                            val baseRacaOptions = state.listaAncestralidadesJson.distinctBy { it.nome.keyify() }.sortedBy { it.nome }
                            var filterBaseRacaText by remember { mutableStateOf("") }
                            AlertDialog(
                                onDismissRequest = { showVarianteBaseRacaDialog = false },
                                title = { Text("Raça Base da Variante") },
                                text = {
                                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                        androidx.compose.material3.OutlinedTextField(
                                            value = filterBaseRacaText,
                                            onValueChange = { filterBaseRacaText = it },
                                            label = { Text("Filtrar Raça") },
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                        )
                                        baseRacaOptions.filter { it.nome.contains(filterBaseRacaText, ignoreCase = true) }.forEach { raca ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth().clickable {
                                                    varianteBaseRacaId = raca.nome.keyify()
                                                    varianteTracosRemovidos = emptyList()
                                                    showVarianteBaseRacaDialog = false
                                                }.padding(vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(raca.nome, style = MaterialTheme.typography.bodyMedium)
                                            }
                                        }
                                    }
                                },
                                confirmButton = { TextButton(onClick = { showVarianteBaseRacaDialog = false }) { Text("Fechar") } }
                            )
                        }

                        if (showVarianteTraitAddDialog) {
                            val allVarianteTraitsCatalog = fullTraitsCatalog.distinctBy { it.grupoEscolha ?: it.nome }
                            var filterVarianteTraitText by remember { mutableStateOf("") }
                            AlertDialog(
                                onDismissRequest = { showVarianteTraitAddDialog = false },
                                title = { Text("Adicionar Traço Racial") },
                                text = {
                                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                        androidx.compose.material3.OutlinedTextField(
                                            value = filterVarianteTraitText,
                                            onValueChange = { filterVarianteTraitText = it },
                                            label = { Text("Filtrar Traço") },
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                        )
                                        allVarianteTraitsCatalog.filter { it.nome.contains(filterVarianteTraitText, ignoreCase = true) }.forEach { trait ->
                                            val isSuperPoderesRow = trait.nome == "Super Poderes"
                                            val isPericiaChoiceRow = trait.id in periciaChoiceTraitIds
                                            val isStackableRow = (trait.vezesMax ?: 1) > 1
                                            val isGrupoEscolhaRow = trait.grupoEscolha != null
                                            val isSel = if (isSuperPoderesRow) {
                                                varianteTracosAdicionados.any { it.nome.startsWith("Super Poderes (") }
                                            } else if (isGrupoEscolhaRow) {
                                                varianteTracosAdicionados.any { it.grupoEscolha == trait.grupoEscolha }
                                            } else if (isPericiaChoiceRow || isStackableRow) {
                                                varianteTracosAdicionados.any { it.id == trait.id }
                                            } else {
                                                varianteTracosAdicionados.any { it.nome.equals(trait.nome, ignoreCase = true) }
                                            }
                                            val onToggle: (Boolean) -> Unit = { checked ->
                                                if (isSuperPoderesRow) {
                                                    if (checked) {
                                                        superPoderRacialPickerTarget = { escolhido ->
                                                            varianteTracosAdicionados = varianteTracosAdicionados + escolhido
                                                        }
                                                    } else {
                                                        varianteTracosAdicionados = varianteTracosAdicionados.filterNot { it.nome.startsWith("Super Poderes (") }
                                                    }
                                                } else if (isGrupoEscolhaRow) {
                                                    if (checked) {
                                                        groupPickerTarget = trait to { escolhido ->
                                                            varianteTracosAdicionados = varianteTracosAdicionados + escolhido
                                                        }
                                                    } else {
                                                        varianteTracosAdicionados = varianteTracosAdicionados.filterNot { it.grupoEscolha == trait.grupoEscolha }
                                                    }
                                                } else if (isStackableRow) {
                                                    if (checked) {
                                                        stackPickerTarget = trait to { escolhido ->
                                                            varianteTracosAdicionados = varianteTracosAdicionados + escolhido
                                                        }
                                                    } else {
                                                        varianteTracosAdicionados = varianteTracosAdicionados.filterNot { it.id == trait.id }
                                                    }
                                                } else if (isPericiaChoiceRow) {
                                                    if (checked) {
                                                        periciaTraitPickerTarget = trait to { escolhido ->
                                                            varianteTracosAdicionados = varianteTracosAdicionados + escolhido
                                                        }
                                                    } else {
                                                        varianteTracosAdicionados = varianteTracosAdicionados.filterNot { it.id == trait.id }
                                                    }
                                                } else {
                                                    varianteTracosAdicionados = if (checked) varianteTracosAdicionados + trait else varianteTracosAdicionados.filterNot { it.nome.equals(trait.nome, ignoreCase = true) }
                                                }
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth().clickable { onToggle(!isSel) }.padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Checkbox(checked = isSel, onCheckedChange = onToggle)
                                                Spacer(Modifier.width(8.dp))
                                                Column {
                                                    val jaEscolhido = if (isGrupoEscolhaRow) {
                                                        varianteTracosAdicionados.firstOrNull { it.grupoEscolha == trait.grupoEscolha }
                                                    } else {
                                                        varianteTracosAdicionados.firstOrNull { it.id == trait.id }
                                                    }
                                                    val rotuloCusto = if ((isStackableRow || isGrupoEscolhaRow) && jaEscolhido != null) {
                                                        "${jaEscolhido.nome} (${if (jaEscolhido.custo > 0) "+${jaEscolhido.custo}" else "${jaEscolhido.custo}"} pts)"
                                                    } else {
                                                        "${trait.nome} (${if (trait.custo > 0) "+${trait.custo}" else "${trait.custo}"} pts)"
                                                    }
                                                    Text(rotuloCusto, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                                    if (trait.descricao.isNotBlank()) {
                                                        Text(trait.descricao, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                                confirmButton = { TextButton(onClick = { showVarianteTraitAddDialog = false }) { Text("OK") } }
                            )
                        }

                        superPoderRacialPickerTarget?.let { onEscolhido ->
                            var filterSuperPoderText by remember { mutableStateOf("") }
                            AlertDialog(
                                onDismissRequest = { superPoderRacialPickerTarget = null },
                                title = { Text("Escolher Super Poder") },
                                text = {
                                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                        Text(
                                            "O traço Super Poderes custa 2 pontos pelo Antecedente Arcano (Super Poderes) mais o custo do poder escolhido.",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                        androidx.compose.material3.OutlinedTextField(
                                            value = filterSuperPoderText,
                                            onValueChange = { filterSuperPoderText = it },
                                            label = { Text("Filtrar Super Poder") },
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                        )
                                        superPoderesCatalog
                                            .filter { it.nome.contains(filterSuperPoderText, ignoreCase = true) }
                                            .forEach { poder ->
                                                val custoPoder = primeiroCustoSuperPoder(poder.custoBase)
                                                val custoTotal = 2 + custoPoder
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().clickable {
                                                        onEscolhido(
                                                            com.example.swadebuilder.model.HabilidadeCriacao(
                                                                nome = "Super Poderes (${poder.nome})",
                                                                custo = custoTotal,
                                                                descricao = "Antecedente Arcano (Super Poderes) + poder \"${poder.nome}\" (custo base ${poder.custoBase ?: custoPoder}).",
                                                                descricaoLite = "Concede o Antecedente Arcano (Super Poderes) e o poder \"${poder.nome}\" do Compêndio de Super Poderes."
                                                            )
                                                        )
                                                        superPoderRacialPickerTarget = null
                                                    }.padding(vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column {
                                                        Text("${poder.nome} (2+$custoPoder = $custoTotal pts)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                                        if (!poder.descricao.isNullOrBlank()) {
                                                            Text(poder.descricao, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                                                        }
                                                    }
                                                }
                                            }
                                    }
                                },
                                confirmButton = { TextButton(onClick = { superPoderRacialPickerTarget = null }) { Text("Cancelar") } }
                            )
                        }

                        if (showModificadorPoderPickerDialog) {
                            var filterModificadorPoderText by remember { mutableStateOf("") }
                            AlertDialog(
                                onDismissRequest = { showModificadorPoderPickerDialog = false },
                                title = { Text("Escolher Super Poder alvo") },
                                text = {
                                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                        androidx.compose.material3.OutlinedTextField(
                                            value = filterModificadorPoderText,
                                            onValueChange = { filterModificadorPoderText = it },
                                            label = { Text("Filtrar Super Poder") },
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                        )
                                        superPoderesParaModificador
                                            .filter { it.nome.contains(filterModificadorPoderText, ignoreCase = true) }
                                            .forEach { poder ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().clickable {
                                                        customModificadorPoderAlvoNome = poder.nome
                                                        showModificadorPoderPickerDialog = false
                                                    }.padding(vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column {
                                                        Text(poder.nome, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                                        if (!poder.descricao.isNullOrBlank()) {
                                                            Text(poder.descricao, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                                                        }
                                                    }
                                                }
                                            }
                                    }
                                },
                                confirmButton = { TextButton(onClick = { showModificadorPoderPickerDialog = false }) { Text("Cancelar") } }
                            )
                        }

                        periciaTraitPickerTarget?.let { (trait, onEscolhido) ->
                            var filterPericiaTraitText by remember { mutableStateOf("") }
                            AlertDialog(
                                onDismissRequest = { periciaTraitPickerTarget = null },
                                title = { Text("Escolher Perícia") },
                                text = {
                                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                        Text(
                                            "${trait.nome} — a que se aplica ao teste da perícia é decidida à mesa; aqui só registramos qual perícia foi escolhida pra este traço.",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                        androidx.compose.material3.OutlinedTextField(
                                            value = filterPericiaTraitText,
                                            onValueChange = { filterPericiaTraitText = it },
                                            label = { Text("Filtrar Perícia") },
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                        )
                                        state.listaPericias
                                            .distinctBy { it.nome }
                                            .sortedBy { it.nome }
                                            .filter { it.nome.contains(filterPericiaTraitText, ignoreCase = true) }
                                            .forEach { pericia ->
                                                // "Perícia Racial (d6)" custa 2, mas o livro dá desconto
                                                // pra 1 quando a perícia escolhida já é uma Perícia
                                                // Básica (começa em d4 de graça pra qualquer
                                                // personagem) — ver comentário de periciaChoiceTraitIds.
                                                val custoFinal = if (trait.id == "pericia_racial_d6" && pericia.basica) 1 else trait.custo
                                                val nomeComDesconto = if (custoFinal != trait.custo) {
                                                    "${trait.nome}: ${pericia.nome} (perícia básica, custo reduzido)"
                                                } else {
                                                    "${trait.nome}: ${pericia.nome}"
                                                }
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().clickable {
                                                        onEscolhido(
                                                            com.example.swadebuilder.model.HabilidadeCriacao(
                                                                nome = nomeComDesconto,
                                                                custo = custoFinal,
                                                                descricao = "${trait.descricao} Perícia escolhida: ${pericia.nome}.",
                                                                descricaoLite = trait.descricaoLite,
                                                                id = trait.id
                                                            )
                                                        )
                                                        periciaTraitPickerTarget = null
                                                    }.padding(vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        if (trait.id == "pericia_racial_d6") "${pericia.nome} (${if (custoFinal != trait.custo) "+1 pt" else "+2 pts"})" else pericia.nome,
                                                        style = MaterialTheme.typography.bodyMedium
                                                    )
                                                }
                                            }
                                    }
                                },
                                confirmButton = { TextButton(onClick = { periciaTraitPickerTarget = null }) { Text("Cancelar") } }
                            )
                        }

                        stackPickerTarget?.let { (trait, onEscolhido) ->
                            val vezesMax = trait.vezesMax?.takeIf { it > 0 } ?: 1
                            AlertDialog(
                                onDismissRequest = { stackPickerTarget = null },
                                title = { Text("Quantas vezes?") },
                                text = {
                                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                        Text(
                                            trait.nome,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (trait.descricao.isNotBlank()) {
                                            Text(
                                                trait.descricao,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(bottom = 8.dp)
                                            )
                                        }
                                        Text(
                                            "O livro permite comprar este traço até $vezesMax ${if (vezesMax == 1) "vez" else "vezes"} — cada compra soma o efeito de novo.",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                        (1..vezesMax).forEach { n ->
                                            val custoTotal = trait.custo * n
                                            val rotulo = com.example.swadebuilder.model.RacialTraitPointCatalog.labelComVezes(trait.id, n)
                                            Row(
                                                modifier = Modifier.fillMaxWidth().clickable {
                                                    onEscolhido(
                                                        trait.copy(
                                                            nome = rotulo,
                                                            custo = custoTotal,
                                                            vezes = n
                                                        )
                                                    )
                                                    stackPickerTarget = null
                                                }.padding(vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    "${n}x — $rotulo (${if (custoTotal > 0) "+$custoTotal" else "$custoTotal"} pts)",
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            }
                                        }
                                    }
                                },
                                confirmButton = { TextButton(onClick = { stackPickerTarget = null }) { Text("Cancelar") } }
                            )
                        }

                        groupPickerTarget?.let { (trait, onEscolhido) ->
                            val opcoes = fullTraitsCatalog
                                .filter { it.grupoEscolha == trait.grupoEscolha }
                                .sortedBy { it.custo }
                            AlertDialog(
                                onDismissRequest = { groupPickerTarget = null },
                                title = { Text("Qual versão?") },
                                text = {
                                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                        Text(
                                            "O livro traz mais de uma versão deste traço, cada uma com seu próprio custo — escolha uma (pode ser trocada removendo e adicionando de novo).",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                        opcoes.forEach { opcao ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth().clickable {
                                                    onEscolhido(opcao)
                                                    groupPickerTarget = null
                                                }.padding(vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(
                                                        "${opcao.nome} (${if (opcao.custo > 0) "+${opcao.custo}" else "${opcao.custo}"} pts)",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    if (opcao.descricao.isNotBlank()) {
                                                        Text(
                                                            opcao.descricao,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                                confirmButton = { TextButton(onClick = { groupPickerTarget = null }) { Text("Cancelar") } }
                            )
                        }

                        if (showSuperPoderModificadoresPickerDialog) {
                            val allMods = remember(state.listaSuperPoderes, activeBookCustomData.modificadoresCustomizados) {
                                (state.listaSuperPoderes.flatMap { it.modificadores ?: emptyList() } +
                                 activeBookCustomData.modificadoresCustomizados.map { it.paraTexto() })
                                    .map { it.trim() }
                                    .filter { it.isNotBlank() }
                                    .distinct()
                                    .sorted()
                            }
                            var filterModText by remember { mutableStateOf("") }
                            AlertDialog(
                                onDismissRequest = { showSuperPoderModificadoresPickerDialog = false },
                                title = { Text("Catálogo de Modificadores") },
                                text = {
                                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                        androidx.compose.material3.OutlinedTextField(
                                            value = filterModText,
                                            onValueChange = { filterModText = it },
                                            label = { Text("Filtrar Modificador") },
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                        )
                                        allMods.filter { it.contains(filterModText, ignoreCase = true) }.forEach { mod ->
                                            val isSel = mod in customSuperPoderModificadoresList
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        customSuperPoderModificadoresList = if (isSel) {
                                                            customSuperPoderModificadoresList - mod
                                                        } else {
                                                            customSuperPoderModificadoresList + mod
                                                        }
                                                    }
                                                    .padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Checkbox(
                                                    checked = isSel,
                                                    onCheckedChange = null
                                                )
                                                Spacer(Modifier.width(8.dp))
                                                Text(mod, style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                    }
                                },
                                confirmButton = { TextButton(onClick = { showSuperPoderModificadoresPickerDialog = false }) { Text("Concluído") } }
                            )
                        }

                        if (showAaPoderesPickerDialog) {
                            val availablePoderes = state.listaPoderes.distinctBy { it.id }.sortedBy { it.nome }
                            var filterAaPoderText by remember { mutableStateOf("") }
                            AlertDialog(
                                onDismissRequest = { showAaPoderesPickerDialog = false },
                                title = { Text("Poderes do Antecedente Arcano") },
                                text = {
                                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                        androidx.compose.material3.OutlinedTextField(
                                            value = filterAaPoderText,
                                            onValueChange = { filterAaPoderText = it },
                                            label = { Text("Filtrar Poder") },
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                        )
                                        availablePoderes.filter { it.nome.contains(filterAaPoderText, ignoreCase = true) }.forEach { poder ->
                                            val isSel = poder.id in customAaPoderesEspecificos
                                            Row(
                                                modifier = Modifier.fillMaxWidth().clickable {
                                                    customAaPoderesEspecificos = if (isSel) customAaPoderesEspecificos - poder.id else customAaPoderesEspecificos + poder.id
                                                }.padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Checkbox(checked = isSel, onCheckedChange = {
                                                    customAaPoderesEspecificos = if (it) customAaPoderesEspecificos + poder.id else customAaPoderesEspecificos - poder.id
                                                })
                                                Spacer(Modifier.width(8.dp))
                                                Text("${poder.nome} (${poder.origem})", style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                    }
                                },
                                confirmButton = { TextButton(onClick = { showAaPoderesPickerDialog = false }) { Text("OK") } }
                            )
                        }

                        if (showVarianteVantagemAddDialog) {
                            val availableVarianteVantagens = state.listaVantagens.distinctBy { it.id }.sortedBy { it.nome }
                            var filterVarianteVantagemText by remember { mutableStateOf("") }
                            AlertDialog(
                                onDismissRequest = { showVarianteVantagemAddDialog = false },
                                title = { Text("Adicionar Vantagem Grátis") },
                                text = {
                                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                        androidx.compose.material3.OutlinedTextField(
                                            value = filterVarianteVantagemText,
                                            onValueChange = { filterVarianteVantagemText = it },
                                            label = { Text("Filtrar Vantagem") },
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                        )
                                        availableVarianteVantagens.filter { it.nome.contains(filterVarianteVantagemText, ignoreCase = true) }.forEach { vant ->
                                            val isSel = vant.id in varianteVantagensAdicionadas
                                            Row(
                                                modifier = Modifier.fillMaxWidth().clickable {
                                                    varianteVantagensAdicionadas = if (isSel) varianteVantagensAdicionadas - vant.id else varianteVantagensAdicionadas + vant.id
                                                }.padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Checkbox(checked = isSel, onCheckedChange = {
                                                    varianteVantagensAdicionadas = if (it) varianteVantagensAdicionadas + vant.id else varianteVantagensAdicionadas - vant.id
                                                })
                                                Spacer(Modifier.width(8.dp))
                                                Text("${vant.nome} (${vant.requisitos.estagio})", style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                    }
                                },
                                confirmButton = { TextButton(onClick = { showVarianteVantagemAddDialog = false }) { Text("OK") } }
                            )
                        }

                        if (showVarianteComplicacaoSeveridadeDialog) {
                            AlertDialog(
                                onDismissRequest = { showVarianteComplicacaoSeveridadeDialog = false },
                                title = { Text("Severidade da Complicação") },
                                text = {
                                    Text(
                                        "Escolha a severidade da Complicação a adicionar. Isso filtra quais Complicações do(s) livro(s) ativo(s) ficam disponíveis.",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                },
                                confirmButton = {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        TextButton(onClick = {
                                            varianteComplicacaoComoMaiorEscolhido = false
                                            showVarianteComplicacaoSeveridadeDialog = false
                                            showVarianteComplicacaoPickDialog = true
                                        }) { Text("Menor (-1)") }
                                        TextButton(onClick = {
                                            varianteComplicacaoComoMaiorEscolhido = true
                                            showVarianteComplicacaoSeveridadeDialog = false
                                            showVarianteComplicacaoPickDialog = true
                                        }) { Text("Maior (-2)") }
                                    }
                                },
                                dismissButton = { TextButton(onClick = { showVarianteComplicacaoSeveridadeDialog = false }) { Text("Cancelar") } }
                            )
                        }

                        if (showVarianteComplicacaoPickDialog) {
                            val comoMaior = varianteComplicacaoComoMaiorEscolhido
                            val availableVarianteComps = state.listaComplicacoes.distinctBy { it.id }.filter { comp ->
                                val sev = comp.severity.trim().lowercase()
                                if (comoMaior) {
                                    sev == "maior" || (sev.contains("menor") && sev.contains("maior"))
                                } else {
                                    sev == "menor" || (sev.contains("menor") && sev.contains("maior"))
                                }
                            }.sortedBy { it.name }
                            var filterVarianteCompText by remember { mutableStateOf("") }
                            AlertDialog(
                                onDismissRequest = { showVarianteComplicacaoPickDialog = false },
                                title = { Text(if (comoMaior) "Complicações Maiores" else "Complicações Menores") },
                                text = {
                                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                        androidx.compose.material3.OutlinedTextField(
                                            value = filterVarianteCompText,
                                            onValueChange = { filterVarianteCompText = it },
                                            label = { Text("Filtrar Complicação") },
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                        )
                                        availableVarianteComps.filter { it.name.contains(filterVarianteCompText, ignoreCase = true) }.forEach { comp ->
                                            val isSel = varianteComplicacoesAdicionadas.any { it.complicacaoId == comp.id && it.comoMaior == comoMaior }
                                            Row(
                                                modifier = Modifier.fillMaxWidth().clickable {
                                                    varianteComplicacoesAdicionadas = if (isSel) {
                                                        varianteComplicacoesAdicionadas.filterNot { it.complicacaoId == comp.id && it.comoMaior == comoMaior }
                                                    } else {
                                                        varianteComplicacoesAdicionadas + com.example.swadebuilder.model.CustomVariantComplicacaoEscolhida(comp.id, comoMaior)
                                                    }
                                                }.padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Checkbox(checked = isSel, onCheckedChange = {
                                                    varianteComplicacoesAdicionadas = if (it) {
                                                        varianteComplicacoesAdicionadas + com.example.swadebuilder.model.CustomVariantComplicacaoEscolhida(comp.id, comoMaior)
                                                    } else {
                                                        varianteComplicacoesAdicionadas.filterNot { e -> e.complicacaoId == comp.id && e.comoMaior == comoMaior }
                                                    }
                                                })
                                                Spacer(Modifier.width(8.dp))
                                                Text(comp.name, style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                    }
                                },
                                confirmButton = { TextButton(onClick = { showVarianteComplicacaoPickDialog = false }) { Text("OK") } }
                            )
                        }

}
