package com.example.swadebuilder.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.FeedbackController
import com.example.swadebuilder.TabStyle
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    state: CriadorState,
    onDismiss: () -> Unit,
    onThemeSelect: () -> Unit,
    persistPrefs: () -> Unit,
    persistFeedbackPrefs: () -> Unit,
    feedbackController: FeedbackController
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
             TextButton(onClick = onDismiss) { Text("Fechar") }
        },
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
            ) {
                 // Header
                Text(
                    text = "Mudar Tema do App",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onThemeSelect() }
                        .padding(vertical = 16.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Geral Section
                SectionTitle("Geral")
                SettingsSwitchRow(
                    label = "Mostrar descrições na tela inicial",
                    checked = state.mostrarDescricaoHome,
                    onCheckedChange = {
                        state.mostrarDescricaoHome = it
                        persistPrefs()
                    }
                )
                SettingsSwitchRow(
                    label = "Exibir mensagens do sistema",
                    checked = state.showSystemMessages,
                    onCheckedChange = {
                        state.showSystemMessages = it
                        persistPrefs()
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Aparência Section
                SectionTitle("Aparência")
                SettingsSwitchRow(
                    label = "Exibir ícone de livro",
                    checked = state.mostrarIdentificadorLivro,
                    onCheckedChange = {
                        state.mostrarIdentificadorLivro = it
                        persistPrefs()
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))
                TabStyleDropdown(state = state, persistPrefs = persistPrefs)

                Spacer(modifier = Modifier.height(24.dp))

                // Feedback Section
                SectionTitle("Feedback")
                Spacer(modifier = Modifier.height(8.dp))

                FeedbackStepper(
                    title = "Intensidade da Vibração",
                    value = state.hapticStrength,
                    onValueChange = {
                         state.hapticStrength = it
                         persistFeedbackPrefs()
                         feedbackController.play(state.hapticStrength, 0)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                FeedbackStepper(
                    title = "Volume dos Sons",
                    value = state.soundVolume,
                    onValueChange = {
                         state.soundVolume = it
                         persistFeedbackPrefs()
                         feedbackController.play(0, state.soundVolume)
                    }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        textContentColor = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.secondary),
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun SettingsSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun TabStyleDropdown(
    state: CriadorState,
    persistPrefs: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("Apenas Ícones" to TabStyle.ICONES, "Apenas Texto" to TabStyle.TEXTO)
    val selectedText = if (state.estiloAbas == TabStyle.ICONES) "Apenas Ícones" else "Apenas Texto"

    androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedText,
            onValueChange = {},
            readOnly = true,
            label = { Text("Estilo das Abas") },
            // Removed trailing icon to simplify imports, focusing on minimalism
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            enabled = false, // To make it behave more like a button but still look like TF? No, enabled=false greys it out.
            // Clickable on TF is tricky. Usually needs Box overlay or enabled=false + Box click.
            // Let's use Box click.
             colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        // Overlay box for click
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { expanded = true }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            options.forEach { (label, value) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        state.estiloAbas = value
                        persistPrefs()
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun FeedbackStepper(
    title: String,
    value: Int,
    onValueChange: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = title, style = MaterialTheme.typography.bodyMedium)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween // Using SpaceBetween to spread out controls or Start?
            // "Abaixo dele uma Row contendo: IconButton, Text, IconButton"
            // Usually centered or close together.
            // Let's use a centered approach for the controls, but left aligned relative to the screen width?
            // "Use bastante padding entre os elementos"
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                IconButton(onClick = {
                    val newValue = (value - 10).coerceAtLeast(0)
                    onValueChange(newValue)
                }) {
                    Icon(Icons.Default.Remove, contentDescription = "Diminuir")
                }

                Text(
                    text = "$value%",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                IconButton(onClick = {
                    val newValue = (value + 10).coerceAtMost(100)
                    onValueChange(newValue)
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Aumentar")
                }
            }
        }
    }
}
