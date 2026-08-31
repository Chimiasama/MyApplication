package com.example.swadebuilder.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swadebuilder.ui.theme.Spacing

@Composable
fun SectionCard(
    title: String,
    icon: ImageVector,
    showHeader: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardColor   = MaterialTheme.colorScheme.surfaceVariant

    Column(modifier = Modifier.fillMaxWidth()) {
        if (showHeader) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.md, horizontal = Spacing.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(end = Spacing.md),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.xs, vertical = 0.dp),
            shape = MaterialTheme.shapes.medium,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = cardColor
            )
        ) {
            Column(modifier = Modifier.padding(Spacing.lg)) {
                content()
            }
        }
    }
}
