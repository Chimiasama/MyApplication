package com.example.swadebuilder.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.example.swadebuilder.model.CharacterStats
import com.example.swadebuilder.model.ScreenTab
import com.example.swadebuilder.ui.theme.SwadeDesignSystem

// 1. Sidebar Navigation Menu
@Composable
fun SavageNavigationMenu(
    tabs: List<ScreenTab>,
    selectedTab: ScreenTab,
    onTabSelected: (ScreenTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(SwadeDesignSystem.colors.surface)
            .padding(SwadeDesignSystem.dimensions.paddingSmall),
        verticalArrangement = Arrangement.spacedBy(SwadeDesignSystem.dimensions.paddingSmall)
    ) {
        tabs.forEach { tab ->
            val isSelected = tab == selectedTab
            val backgroundColor = if (isSelected) SwadeDesignSystem.colors.highlight else Color.Transparent
            val contentColor = if (isSelected) SwadeDesignSystem.colors.onPrimary else SwadeDesignSystem.colors.onSurface

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(SwadeDesignSystem.shapes.button)
                    .background(backgroundColor)
                    .clickable { onTabSelected(tab) }
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tab.title,
                    style = SwadeDesignSystem.typography.label,
                    color = contentColor
                )
            }
        }
    }
}

// 2. Attribute Grid (ConstraintLayout)
@Composable
fun AttributeGrid(
    stats: CharacterStats,
    isLandscape: Boolean,
    modifier: Modifier = Modifier
) {
    val attributes = listOf(
        "Agility" to stats.agility,
        "Smarts" to stats.smarts,
        "Spirit" to stats.spirit,
        "Strength" to stats.strength,
        "Vigor" to stats.vigor
    )

    ConstraintLayout(modifier = modifier.fillMaxWidth()) {
        // Create references for each attribute row
        val refs = attributes.map { createRef() }

        attributes.forEachIndexed { index, (name, value) ->
            val ref = refs[index]

            val rowModifier = if (isLandscape) {
                // In Landscape: 2 Columns
                // Even index: Left Column
                // Odd index: Right Column
                Modifier.constrainAs(ref) {
                    width = Dimension.fillToConstraints
                    top.linkTo(if (index < 2) parent.top else refs[index - 2].bottom, margin = 8.dp)

                    if (index % 2 == 0) {
                        // Left Column
                        start.linkTo(parent.start)
                        end.linkTo(parent.centerX, margin = 8.dp)
                    } else {
                        // Right Column
                        start.linkTo(parent.centerX, margin = 8.dp)
                        end.linkTo(parent.end)
                    }
                }
            } else {
                // In Portrait: Vertical Stack
                Modifier.constrainAs(ref) {
                    width = Dimension.fillToConstraints
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(if (index == 0) parent.top else refs[index - 1].bottom, margin = 8.dp)
                }
            }

            AttributeRow(
                name = name,
                value = value,
                modifier = rowModifier
            )
        }
    }
}

@Composable
fun AttributeRow(
    name: String,
    value: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(SwadeDesignSystem.colors.surface.copy(alpha = 0.5f), SwadeDesignSystem.shapes.card)
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = SwadeDesignSystem.typography.body,
            color = SwadeDesignSystem.colors.onSurface
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Dice Icon (Simulated with a box for now, as requested "representative icons")
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .drawBehind {
                        drawRect(Color.White, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f))
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "d$value",
                    style = SwadeDesignSystem.typography.statValue,
                    color = SwadeDesignSystem.colors.onSurface,
                    fontSize = 12.sp
                )
            }
        }
    }
}

// 3. Info Panel (Traits)
@Composable
fun InfoPanel(
    stats: CharacterStats,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(SwadeDesignSystem.colors.surface, SwadeDesignSystem.shapes.card)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TraitItem(label = "Pace", value = stats.pace.toString())
        TraitItem(label = "Parry", value = stats.parry.toString())
        TraitItem(label = "Toughness", value = stats.toughness.toString())
    }
}

@Composable
fun TraitItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = SwadeDesignSystem.typography.label,
            color = SwadeDesignSystem.colors.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = SwadeDesignSystem.typography.statValue,
            color = SwadeDesignSystem.colors.onSurface
        )
    }
}

// 4. Bennie Component
@Composable
fun BennieComponent(
    count: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.wrapContentHeight(),
        horizontalArrangement = Arrangement.spacedBy((-12).dp) // Overlap effect
    ) {
        repeat(count) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .drawBehind {
                        drawCircle(color = Color(0xFFD32F2F)) // Red Bennie
                        drawCircle(color = Color.Black, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f))
                    }
            )
        }
    }
}
