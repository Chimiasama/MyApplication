package com.example.swadebuilder.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Linha compacta (~60dp) para escolha de compêndio/cenário na tela inicial: ícone
 * pequeno + título, sem descrição. Substitui o antigo grid de cards por uma lista
 * de 1 coluna estilo tela de configurações; mesma lógica de seleção (borda/tint
 * quando ativo), só que aplicada à linha em vez do card inteiro.
 */
@Composable
fun CompendiumListRow(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val scheme = MaterialTheme.colorScheme
    val rowBackground = if (isSelected) scheme.primaryContainer.copy(alpha = 0.25f) else Color.Transparent
    val iconBorderColor = when {
        !enabled -> scheme.outlineVariant.copy(alpha = 0.5f)
        isSelected -> scheme.primary
        else -> scheme.outline
    }
    val iconBorderWidth = if (isSelected) 2.dp else 1.dp
    val iconTint = when {
        !enabled -> scheme.onSurfaceVariant.copy(alpha = 0.4f)
        isSelected -> scheme.primary
        else -> scheme.onSurfaceVariant
    }
    val titleColor = if (enabled) scheme.onSurface else scheme.onSurface.copy(alpha = 0.4f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .background(rowBackground)
            .clickable(enabled = enabled, onClick = onToggle)
            .semantics {
                contentDescription = title
                stateDescription = if (isSelected) "Selecionado" else if (enabled) "Disponível para seleção" else "Indisponível"
            }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .border(BorderStroke(iconBorderWidth, iconBorderColor), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = titleColor,
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )
    }
}
