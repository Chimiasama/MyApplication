package com.example.swadebuilder.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onHelpClick != null) {
            IconButton(onClick = onHelpClick) {
                Icon(
                    Icons.AutoMirrored.Filled.Help,
                    contentDescription = "Ajuda"
                )
            }
        } else {
            Spacer(
                modifier = Modifier.size(48.dp)
            )
        }

        Text(
            text = centerText,
            modifier = Modifier
                .weight(1f)
                .then(
                    if (onCenterClick != null) {
                        Modifier.clickable(onClick = onCenterClick)
                    } else {
                        Modifier
                    }
                )
                .padding(horizontal = 8.dp),
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        if (onListaCompletaClick != null && listaCompletaText.isNotEmpty()) {
            TextButton(onClick = onListaCompletaClick) {
                Text(listaCompletaText, fontSize = 13.sp)
            }
        } else {
            Spacer(
                modifier = Modifier.size(48.dp)
            )
        }
    }
}
