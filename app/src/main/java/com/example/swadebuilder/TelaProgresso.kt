package com.example.swadebuilder

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swadebuilder.model.Vantagem

// ── Data Structures ──────────────────────────────────────────────────────────────────
/**
 * Represents the different character progression stages in SWADE.
 */
enum class Stage(val label: String, val fullName: String) {
    NOVATO("N", "Novato"),
    EXPERIENTE("E", "Experiente"),
    VETERANO("V", "Veterano"),
    HEROICO("H", "Heroico"),
    LENDARIO("L", "Lendário")
}

/**
 * Represents a single progression slot, which has a stage and a selected choice.
 * @param id A unique identifier for the slot to help with state management in Compose.
 * @param stage The progression stage this slot belongs to (e.g., Novato, Experiente).
 * @param choice The progression choice made for this slot, initially null.
 */
data class ProgressionSlot(
    val id: Int,
    val stage: Stage,
    var choice: ProgressionChoice? = null
)

/**
 * Represents a choice made during progression. This is a sealed class to accommodate
 * different types of choices with different associated data.
 */
sealed class ProgressionChoice {
    object IncreaseAttribute : ProgressionChoice()
    data class SelectVantagem(val vantagem: Vantagem) : ProgressionChoice()
    object IncreaseSkill : ProgressionChoice()
    object RemoveMinorHindrance : ProgressionChoice()
    object ReserveMajorHindranceSlot : ProgressionChoice()

    // A display name to show in the UI.
    val displayName: String
        get() = when (this) {
            is IncreaseAttribute -> "Aumentar Atributo"
            is SelectVantagem -> "Vantagem: ${vantagem.nome}"
            is IncreaseSkill -> "Aumentar Perícia"
            is RemoveMinorHindrance -> "Remover Complicação Menor"
            is ReserveMajorHindranceSlot -> "Reservar p/ Complicação Maior"
        }
}

/**
 * Generates the initial list of progression slots based on SWADE rules.
 */
fun generateInitialSlots(): List<ProgressionSlot> {
    val slots = mutableListOf<ProgressionSlot>()
    var idCounter = 0
    repeat(3) { slots.add(ProgressionSlot(idCounter++, Stage.NOVATO)) }
    repeat(4) { slots.add(ProgressionSlot(idCounter++, Stage.EXPERIENTE)) }
    repeat(4) { slots.add(ProgressionSlot(idCounter++, Stage.VETERANO)) }
    repeat(4) { slots.add(ProgressionSlot(idCounter++, Stage.HEROICO)) }
    repeat(4) { slots.add(ProgressionSlot(idCounter++, Stage.LENDARIO)) } // Placeholder
    return slots
}

// ── Composable ───────────────────────────────────────────────────────────────────────
@Composable
fun TelaProgresso() {
    val progressionSlots = remember { mutableStateListOf(*generateInitialSlots().toTypedArray()) }
    var showDialog by remember { mutableStateOf(false) }
    var selectedSlot by remember { mutableStateOf<ProgressionSlot?>(null) }

    val groupedSlots = progressionSlots.groupBy { it.stage }

    LazyColumn(modifier = Modifier.padding(16.dp)) {
        groupedSlots.forEach { (stage, slots) ->
            item {
                Text(
                    text = stage.fullName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                HorizontalDivider()
            }
            items(slots) { slot ->
                ProgressionSlotRow(
                    slot = slot,
                    onClick = {
                        selectedSlot = slot
                        showDialog = true
                    }
                )
            }
        }
    }

    if (showDialog && selectedSlot != null) {
        ProgressionChoiceDialog(
            slot = selectedSlot!!,
            onDismiss = { showDialog = false },
            onChoiceSelected = { choice ->
                val index = progressionSlots.indexOfFirst { it.id == selectedSlot!!.id }
                if (index != -1) {
                    progressionSlots[index] = progressionSlots[index].copy(choice = choice)
                }
                showDialog = false
            },
            progressionSlots = progressionSlots
        )
    }
}

/**
 * A composable that displays a single progression slot.
 * @param slot The progression slot to display.
 * @param onClick A callback to be invoked when the slot is clicked.
 */
@Composable
fun ProgressionSlotRow(slot: ProgressionSlot, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
    ) {
        Text(
            text = "${slot.stage.label}",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = slot.choice?.displayName ?: "________",
            fontSize = 16.sp
        )
    }
}

/**
 * A dialog that allows the user to select a progression choice for a given slot.
 * @param slot The slot for which to select a choice.
 * @param onDismiss A callback to be invoked when the dialog is dismissed.
 * @param onChoiceSelected A callback to be invoked when a choice is selected.
 */
@Composable
fun ProgressionChoiceDialog(
    slot: ProgressionSlot,
    onDismiss: () -> Unit,
    onChoiceSelected: (ProgressionChoice) -> Unit,
    progressionSlots: List<ProgressionSlot>
) {
    val currentStage = slot.stage
    val hasIncreasedAttribute = progressionSlots
        .filter { it.stage == currentStage }
        .any { it.choice is ProgressionChoice.IncreaseAttribute }

    // Por enquanto a lista é estática; depois você pode plugar aqui
    // a lógica real de filtrar vantagens / etc.
    val availableChoices = mutableListOf<ProgressionChoice>()
    if (!hasIncreasedAttribute || currentStage == Stage.LENDARIO) {
        availableChoices.add(ProgressionChoice.IncreaseAttribute)
    }
    availableChoices.add(ProgressionChoice.IncreaseSkill)
    // availableChoices.add(ProgressionChoice.SelectVantagem(algumaVantagemReal))
    availableChoices.add(ProgressionChoice.RemoveMinorHindrance)
    availableChoices.add(ProgressionChoice.ReserveMajorHindranceSlot)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Selecione o Progresso") },
        text = {
            Column {
                availableChoices.forEach { choice ->
                    Text(
                        text = choice.displayName,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onChoiceSelected(choice) }
                            .padding(vertical = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
