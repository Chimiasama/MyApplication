package com.example.swadebuilder.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swadebuilder.R

@Composable
fun CollapsibleSection(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    onToggleFeedback: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val stateDescriptionText = stringResource(
        if (expanded) R.string.state_expanded else R.string.state_collapsed
    )

    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(
                    onClick = {
                        onToggleFeedback?.invoke()
                        onToggle()
                    },
                    role = Role.Button
                )
                .semantics {
                    stateDescription = stateDescriptionText
                }
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Icon(
                imageVector = if (expanded) Icons.Default.Remove else Icons.Default.Add,
                contentDescription = null // Null because the row itself announces state via semantics
            )
        }
        if (expanded) content()
    }
}
