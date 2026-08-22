package com.example.swadebuilder.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.model.CustomContentType
import com.example.swadebuilder.model.Requisito
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.FeedbackController
import com.example.swadebuilder.TabStyle
import com.example.swadebuilder.ui.theme.AppTheme
import kotlin.math.roundToInt

import androidx.compose.material3.OutlinedButton

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
                    var customItemName by remember { mutableStateOf("") }
                    var customItemDesc by remember { mutableStateOf("") }
                    var customPackageJson by remember { mutableStateOf("") }
                    var statusMessage by remember { mutableStateOf<String?>(null) }

                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Conteúdo Customizado",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Crie vantagens e itens caseiros com prefixo 'custom:'.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedButton(
                            onClick = {
                                customItemName = ""
                                customItemDesc = ""
                                customPackageJson = ""
                                statusMessage = null
                                showCustomContentDialog = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Gerenciar Conteúdo Customizado")
                        }
                    }

                    if (showCustomContentDialog) {
                        val manager = remember { com.example.swadebuilder.util.CustomContentManager() }

                        AlertDialog(
                            onDismissRequest = { showCustomContentDialog = false },
                            title = { Text("Conteúdo Customizado") },
                            text = {
                                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                    Text("Criar Item Customizado", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(8.dp))
                                    androidx.compose.material3.OutlinedTextField(
                                        value = customItemName,
                                        onValueChange = { customItemName = it },
                                        label = { Text("Nome do Item") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    androidx.compose.material3.OutlinedTextField(
                                        value = customItemDesc,
                                        onValueChange = { customItemDesc = it },
                                        label = { Text("Descrição") },
                                        modifier = Modifier.fillMaxWidth().height(100.dp)
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    Text("Importar Pacote JSON", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(8.dp))
                                    androidx.compose.material3.OutlinedTextField(
                                        value = customPackageJson,
                                        onValueChange = { customPackageJson = it },
                                        label = { Text("Cole o JSON do pacote") },
                                        modifier = Modifier.fillMaxWidth().height(100.dp)
                                    )
                                    if (statusMessage != null) {
                                        Spacer(Modifier.height(8.dp))
                                        Text(statusMessage!!, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            },
                            confirmButton = {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    TextButton(onClick = {
                                        if (customItemName.isNotBlank() && customItemDesc.isNotBlank()) {
                                            val id = "custom:${customItemName.lowercase().replace(" ", "_")}"
                                            val customItem = com.example.swadebuilder.model.CustomContentItem(
                                                id = id,
                                                name = customItemName,
                                                type = CustomContentType.ADVANTAGE,
                                                category = "vantagem",
                                                description = customItemDesc
                                            )
                                            when (val validation = manager.validateItem(customItem)) {
                                                is com.example.swadebuilder.util.CustomContentValidationResult.Valid -> {
                                                    val newAdv = com.example.swadebuilder.model.Vantagem(
                                                        id = id,
                                                        nome = customItemName,
                                                        categoria = com.example.swadebuilder.model.Categoria.PROFISSIONAL,
                                                        descricao = customItemDesc,
                                                        origem = "CUSTOM",
                                                        requisitos = Requisito()
                                                    )
                                                    state.addCustomVantagem(newAdv)
                                                    statusMessage = "Item '$customItemName' adicionado com sucesso!"
                                                    customItemName = ""
                                                    customItemDesc = ""
                                                }
                                                is com.example.swadebuilder.util.CustomContentValidationResult.Invalid -> {
                                                    statusMessage = "Erro: ${validation.reason}"
                                                }
                                            }
                                        } else if (customPackageJson.isNotBlank()) {
                                            val importRes = manager.importPackageFromJson(customPackageJson)
                                            if (importRes.isSuccess) {
                                                statusMessage = "Pacote '${importRes.getOrNull()?.packageName}' importado com sucesso!"
                                                customPackageJson = ""
                                            } else {
                                                statusMessage = "Erro ao importar: ${importRes.exceptionOrNull()?.message}"
                                            }
                                        }
                                    }) { Text("Adicionar / Importar") }
                                    TextButton(onClick = { showCustomContentDialog = false }) { Text("Fechar") }
                                }
                            }
                        )
                    }
                }

                // Card "Backup e Transferência (JSON)"
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    var showExportDialog by remember { mutableStateOf(false) }
                    var showImportDialog by remember { mutableStateOf(false) }
                    var backupJsonText by remember { mutableStateOf("") }
                    var importErrorText by remember { mutableStateOf<String?>(null) }

                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Backup da Ficha (JSON)",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Exporte ou importe fichas em formato JSON seguro.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    backupJsonText = com.example.swadebuilder.util.CharacterBackupManager.exportBackupJson(state.toSnapshot())
                                    showExportDialog = true
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Exportar JSON")
                            }
                            OutlinedButton(
                                onClick = {
                                    backupJsonText = ""
                                    importErrorText = null
                                    showImportDialog = true
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Importar JSON")
                            }
                        }
                    }

                    if (showExportDialog) {
                        AlertDialog(
                            onDismissRequest = { showExportDialog = false },
                            title = { Text("Backup JSON da Ficha") },
                            text = {
                                Column {
                                    Text("Copie o texto JSON abaixo para salvar seu backup em outro lugar:", style = MaterialTheme.typography.bodySmall)
                                    Spacer(Modifier.height(8.dp))
                                    androidx.compose.material3.OutlinedTextField(
                                        value = backupJsonText,
                                        onValueChange = {},
                                        readOnly = true,
                                        modifier = Modifier.fillMaxWidth().height(180.dp)
                                    )
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = { showExportDialog = false }) { Text("Fechar") }
                            }
                        )
                    }

                    if (showImportDialog) {
                        AlertDialog(
                            onDismissRequest = { showImportDialog = false },
                            title = { Text("Importar Backup JSON") },
                            text = {
                                Column {
                                    Text("Cole o texto JSON do backup da ficha:", style = MaterialTheme.typography.bodySmall)
                                    Spacer(Modifier.height(8.dp))
                                    androidx.compose.material3.OutlinedTextField(
                                        value = backupJsonText,
                                        onValueChange = { backupJsonText = it; importErrorText = null },
                                        modifier = Modifier.fillMaxWidth().height(160.dp),
                                        placeholder = { Text("{ \"id\": ... }") }
                                    )
                                    if (importErrorText != null) {
                                        Spacer(Modifier.height(8.dp))
                                        Text(importErrorText!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        when (val result = com.example.swadebuilder.util.CharacterBackupManager.importBackupJson(backupJsonText)) {
                                            is com.example.swadebuilder.util.CharacterBackupManager.ImportResult.Success -> {
                                                state.restoreFromSnapshot(result.snapshot, mutableListOf())
                                                showImportDialog = false
                                            }
                                            is com.example.swadebuilder.util.CharacterBackupManager.ImportResult.Failure -> {
                                                importErrorText = result.reason
                                            }
                                        }
                                    },
                                    enabled = backupJsonText.isNotBlank()
                                ) {
                                    Text("Restaurar Ficha")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showImportDialog = false }) { Text("Cancelar") }
                            }
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
                                    imageVector = Icons.Default.VolumeUp,
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
