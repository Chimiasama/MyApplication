package com.example.swadebuilder.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SectionHeader(
    onHelpClick: (() -> Unit)? = null,
    centerText: String,
    onCenterClick: (() -> Unit)? = null,
    onListaCompletaClick: (() -> Unit)? = null,
    listaCompletaText: String = "Lista Completa"
) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (onHelpClick != null) {
                    IconButton(
                        onClick = onHelpClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Help,
                            contentDescription = "Ajuda",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .then(
                            if (onCenterClick != null) {
                                Modifier.clickable(onClick = onCenterClick)
                            } else {
                                Modifier
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    val textStyle = if (maxWidth < 360.dp) {
                        MaterialTheme.typography.titleSmall
                    } else {
                        MaterialTheme.typography.titleMedium
                    }

                    Text(
                        text = centerText,
                        textAlign = TextAlign.Start,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = textStyle.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            if (onListaCompletaClick != null && listaCompletaText.isNotEmpty()) {
                androidx.compose.material3.FilledTonalButton(
                    onClick = onListaCompletaClick,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier.heightIn(min = 32.dp)
                ) {
                    Text(listaCompletaText, fontSize = 11.sp)
                }
            }
        }
        androidx.compose.material3.HorizontalDivider(
            modifier = Modifier.padding(horizontal = 8.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    }
}
