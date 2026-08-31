package com.example.swadebuilder.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.ui.theme.Spacing
import com.example.swadebuilder.ui.theme.emphasis

/**
 * Como marcar "isto está selecionado" numa lista. Antes disso o app tinha 5 respostas
 * diferentes pra essa pergunta espalhadas pelas telas: Checkbox de verdade, FilterChip fazendo
 * o papel de seleção, um "RadioButton" que na real era OutlinedButton fingindo (nunca existiu
 * um RadioButton nativo no app), cartão trocando de cor sozinho, e o ModuleCard (borda + ícone
 * + escala). [SelectableItemRow] cobre os casos de lista vertical (que eram a maioria desses
 * cinco) com um afeto só: borda + preenchimento leve quando selecionado + um indicador que
 * muda de forma conforme o tipo de escolha. Filtros em chip horizontal continuam com
 * FilterChip normalmente — layout diferente, não faz parte dessa fragmentação.
 */
enum class SelectionMode { UNICA, MULTIPLA }

@Composable
fun SelectableItemRow(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    mode: SelectionMode = SelectionMode.MULTIPLA,
    enabled: Boolean = true,
    trailingContent: (@Composable () -> Unit)? = null
) {
    val scheme = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.small
    val borderColor = when {
        !enabled -> scheme.outlineVariant.copy(alpha = 0.4f)
        selected -> scheme.primary
        else -> scheme.outlineVariant
    }
    val containerColor = when {
        !enabled -> scheme.surfaceVariant.copy(alpha = 0.4f)
        selected -> scheme.primaryContainer
        else -> scheme.surfaceVariant
    }
    val contentColor = when {
        !enabled -> scheme.onSurfaceVariant.copy(alpha = 0.5f)
        selected -> scheme.onPrimaryContainer
        else -> scheme.onSurfaceVariant
    }
    val a11yRole = if (mode == SelectionMode.UNICA) Role.RadioButton else Role.Checkbox

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(containerColor)
            .border(BorderStroke(if (selected) 1.5.dp else 1.dp, borderColor), shape)
            .clickable(enabled = enabled, onClick = onClick)
            .semantics {
                role = a11yRole
                this.selected = selected
            }
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = if (selected) MaterialTheme.typography.emphasis else MaterialTheme.typography.bodyMedium,
                color = contentColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.8f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        trailingContent?.invoke()
        SelectionIndicator(selected = selected, mode = mode, enabled = enabled)
    }
}

@Composable
private fun SelectionIndicator(
    selected: Boolean,
    mode: SelectionMode,
    enabled: Boolean
) {
    val scheme = MaterialTheme.colorScheme
    val shape: Shape = if (mode == SelectionMode.UNICA) CircleShape else RoundedCornerShape(6.dp)
    val borderColor = when {
        !enabled -> scheme.outline.copy(alpha = 0.4f)
        selected -> scheme.primary
        else -> scheme.outline
    }
    val fillColor = if (selected && enabled) scheme.primary else Color.Transparent

    Box(
        modifier = Modifier
            .size(20.dp)
            .clip(shape)
            .background(fillColor)
            .border(1.5.dp, borderColor, shape),
        contentAlignment = Alignment.Center
    ) {
        if (selected && enabled) {
            if (mode == SelectionMode.MULTIPLA) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = scheme.onPrimary,
                    modifier = Modifier.size(13.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(scheme.onPrimary)
                )
            }
        }
    }
}

/**
 * Mesma linguagem visual de [SelectableItemRow], sem o indicador de seleção — pra rótulos
 * "informativos" dentro de um item (tags, categoria) que não são, eles mesmos, a escolha.
 * Evita usar AssistChip pra esse papel só porque ele tem a mesma altura de um FilterChip.
 */
@Composable
fun ItemTag(
    text: String,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false
) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (emphasized) scheme.secondaryContainer else scheme.surfaceVariant)
            .padding(horizontal = Spacing.sm, vertical = Spacing.xs)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = if (emphasized) scheme.onSecondaryContainer else LocalContentColor.current.copy(alpha = 0.75f)
        )
    }
}
