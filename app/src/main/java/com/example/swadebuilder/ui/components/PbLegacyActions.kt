package com.example.swadebuilder.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PbLegacyActions(
    spendLabel: String,
    refundLabel: String,
    spendEnabled: Boolean,
    refundEnabled: Boolean,
    onSpend: () -> Unit,
    onRefund: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TextButton(
            onClick = onSpend,
            enabled = spendEnabled,
            modifier = Modifier.weight(1f)
        ) {
            Text(spendLabel)
        }

        TextButton(
            onClick = onRefund,
            enabled = refundEnabled,
            modifier = Modifier.weight(1f)
        ) {
            Text(refundLabel)
        }
    }
}
