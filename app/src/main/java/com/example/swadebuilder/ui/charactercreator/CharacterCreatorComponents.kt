package com.example.swadebuilder.ui.charactercreator

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout

@Composable
fun SavageNavigationMenu(
    selectedTab: ScreenTab,
    onTabSelected: (ScreenTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ScreenTab.allTabs.forEach { tab ->
            val isSelected = tab == selectedTab
            val containerColor = if (isSelected) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
            val contentColor = if (isSelected) {
                MaterialTheme.colorScheme.onErrorContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(onClick = { onTabSelected(tab) }),
                shape = SwadeDesignSystem.tokens.buttonShape,
                color = containerColor,
                contentColor = contentColor,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Text(
                    text = tab.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    textAlign = TextAlign.Center,
                    style = SwadeDesignSystem.tokens.headerTextStyle.copy(fontSize = 14.sp)
                )
            }
        }
    }
}

data class AttributeItem(
    val name: String,
    val die: String
)

@Composable
fun AttributeGrid(
    attributes: List<AttributeItem>,
    isLandscape: Boolean,
    modifier: Modifier = Modifier
) {
    val rows = if (isLandscape) {
        attributes.chunked((attributes.size + 1) / 2)
    } else {
        listOf(attributes)
    }
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        rows.forEach { columnItems ->
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                columnItems.forEach { attribute ->
                    AttributeRow(attribute = attribute)
                }
            }
        }
    }
}

@Composable
private fun AttributeRow(attribute: AttributeItem, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = SwadeDesignSystem.tokens.panelShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = SwadeDesignSystem.panelBorder()
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            val (icon, name, die) = createRefs()
            Icon(
                imageVector = Icons.Filled.Casino,
                contentDescription = null,
                modifier = Modifier
                    .size(20.dp)
                    .constrainAs(icon) {
                        start.linkTo(parent.start)
                        centerVerticallyTo(parent)
                    }
            )
            Text(
                text = attribute.name,
                modifier = Modifier.constrainAs(name) {
                    start.linkTo(icon.end, margin = 8.dp)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    width = androidx.constraintlayout.compose.Dimension.fillToConstraints
                },
                style = SwadeDesignSystem.tokens.bodyTextStyle.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = attribute.die,
                modifier = Modifier.constrainAs(die) {
                    end.linkTo(parent.end)
                    centerVerticallyTo(parent)
                },
                style = SwadeDesignSystem.tokens.bodyTextStyle.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun InfoPanel(
    title: String,
    rows: List<Pair<String, String>>,
    modifier: Modifier = Modifier,
    shape: Shape = SwadeDesignSystem.tokens.panelShape
) {
    Card(
        modifier = modifier,
        shape = shape,
        border = SwadeDesignSystem.panelBorder(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = SwadeDesignSystem.tokens.headerTextStyle.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            rows.forEach { (label, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = label,
                        style = SwadeDesignSystem.tokens.bodyTextStyle
                    )
                    Text(
                        text = value,
                        style = SwadeDesignSystem.tokens.bodyTextStyle.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
fun BennieRow(
    count: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(count) { index ->
            Box(
                modifier = Modifier
                    .offset(x = (-6 * index).dp)
                    .size(20.dp)
                    .background(
                        color = MaterialTheme.colorScheme.error,
                        shape = CircleShape
                    )
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
    }
}

@Composable
fun SkillsList(
    skills: List<String>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(skills) { skill ->
            Card(
                shape = SwadeDesignSystem.tokens.panelShape,
                border = SwadeDesignSystem.panelBorder(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(
                    text = skill,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    style = SwadeDesignSystem.tokens.bodyTextStyle
                )
            }
        }
    }
}
