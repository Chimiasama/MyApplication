package com.example.swadebuilder.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

@Composable
fun StandardListItem(
    modifier: Modifier = Modifier,
    title: @Composable RowScope.() -> Unit,
    subtitle: (@Composable () -> Unit)? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable RowScope.() -> Unit)? = null,
    detailsContent: (@Composable ColumnScope.() -> Unit)? = null,
    bottomContent: (@Composable ColumnScope.() -> Unit)? = null,
    statusText: String? = null,
    statusColor: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    val showDetails = detailsContent != null

    val containerModifier = modifier
        .fillMaxWidth()
        .then(
            if (onClick != null) {
                Modifier // Clickable is usually handled by Card if we use Card, but here we use a custom Column or Card
            } else Modifier
        )

    Card(
        modifier = containerModifier.padding(horizontal = 4.dp, vertical = 2.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(10.dp),
        onClick = { onClick?.invoke() },
        enabled = enabled && onClick != null
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (leadingContent != null) {
                    leadingContent()
                    Spacer(Modifier.width(8.dp))
                }

                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    title()
                }

                if (statusText != null) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelMedium,
                        color = statusColor
                    )
                }

                if (trailingContent != null) {
                    trailingContent()
                }
            }

            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                subtitle()
            }

            if (bottomContent != null) {
                Spacer(Modifier.height(4.dp))
                bottomContent()
            }

            if (showDetails) {
                Spacer(Modifier.height(2.dp))
                TextButton(
                    onClick = { expanded = !expanded },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 0.dp, vertical = 2.dp),
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Text(
                        if (expanded) "Ocultar detalhes" else "Ver detalhes",
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                AnimatedVisibility(visible = expanded) {
                    Column(modifier = Modifier.padding(top = 4.dp)) {
                        detailsContent?.invoke(this)
                    }
                }
            }
        }
    }
}
