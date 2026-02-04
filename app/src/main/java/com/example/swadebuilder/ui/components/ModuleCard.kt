package com.example.swadebuilder.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.TabStyle

@Composable
fun ModuleCard(
    title: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    enabled: Boolean = true,
    onToggle: () -> Unit,
    showDescription: Boolean = true,
    onRulesClick: (() -> Unit)? = null,
    isRulesActive: Boolean = false,
    tabStyle: TabStyle = TabStyle.TEXTO,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(targetValue = if (isSelected) 1.04f else if (enabled) 1.0f else 0.97f, label = "scale")
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else if (enabled) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    val borderWidth = if (isSelected) 2.dp else 1.dp
    val containerColor = if (isSelected)
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
    else if (enabled)
        MaterialTheme.colorScheme.surface
    else
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)

    OutlinedCard(
        onClick = onToggle,
        enabled = enabled,
        modifier = modifier
            .scale(scale)
            .fillMaxWidth()
            .padding(vertical = if (showDescription) 0.dp else 4.dp), // Minimal padding if collapsed
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(borderWidth, borderColor),
        elevation = CardDefaults.outlinedCardElevation(defaultElevation = if (isSelected) 8.dp else 2.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = containerColor)
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            // Inner Frame (Surface)
            androidx.compose.material3.Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (showDescription) Modifier.defaultMinSize(minHeight = 168.dp)
                        else Modifier
                    ),
                shape = MaterialTheme.shapes.small,
                color = if (enabled) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                shadowElevation = if (isSelected) 2.dp else 0.dp
            ) {
                // Content
                Box(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(if (showDescription) 40.dp else 32.dp),
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                        if (showDescription) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Rules Button (TopEnd)
                    if (onRulesClick != null) {
                        val rulesColor = if (isRulesActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)

                        Box(
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            if (tabStyle == TabStyle.ICONES) {
                                IconButton(
                                    onClick = onRulesClick
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Gavel,
                                        contentDescription = "Regras",
                                        tint = rulesColor
                                    )
                                }
                            } else {
                                TextButton(
                                    onClick = onRulesClick
                                ) {
                                    Text(
                                        text = "REGRAS",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = rulesColor
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Check Icon (TopStart)
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selecionado",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(2.dp)
                        .size(28.dp)
                )
            }
        }
    }
}
