package com.example.swadebuilder.ui.charactercreator

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CharacterCreatorScreen(
    modifier: Modifier = Modifier,
    viewModel: CharacterViewModel = viewModel()
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    SwadeThemeWrapper(modifier = modifier.fillMaxSize()) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isLandscape = maxWidth > maxHeight
            MainCharacterScreen(
                isLandscape = isLandscape,
                modifier = Modifier.fillMaxSize(),
                navigationSlot = { navModifier ->
                    SavageNavigationMenu(
                        selectedTab = selectedTab,
                        onTabSelected = viewModel::onTabSelected,
                        modifier = navModifier
                    )
                },
                portraitSlot = { portraitModifier ->
                    PortraitPanel(modifier = portraitModifier)
                },
                attributesSlot = { attributesModifier ->
                    AttributeSection(
                        isLandscape = isLandscape,
                        modifier = attributesModifier
                    )
                },
                traitsSlot = { traitsModifier ->
                    TraitsSection(modifier = traitsModifier)
                },
                contentSlot = { contentModifier ->
                    AnimatedContent(
                        targetState = selectedTab,
                        modifier = contentModifier,
                        label = "main-tab-content"
                    ) { tab ->
                        when (tab) {
                            ScreenTab.Character -> CharacterTabContent()
                            ScreenTab.Skills -> SkillsTabContent()
                            ScreenTab.Edges -> PlaceholderTabContent("Edges & Hindrances")
                            ScreenTab.Gear -> PlaceholderTabContent("Gear")
                            ScreenTab.Notes -> PlaceholderTabContent("Notes")
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun MainCharacterScreen(
    isLandscape: Boolean,
    navigationSlot: @Composable (Modifier) -> Unit,
    portraitSlot: @Composable (Modifier) -> Unit,
    attributesSlot: @Composable (Modifier) -> Unit,
    traitsSlot: @Composable (Modifier) -> Unit,
    contentSlot: @Composable (Modifier) -> Unit,
    modifier: Modifier = Modifier
) {
    if (isLandscape) {
        Row(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier.weight(0.9f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                navigationSlot(Modifier.fillMaxWidth())
                attributesSlot(Modifier.fillMaxWidth())
            }
            Column(
                modifier = Modifier.weight(1.2f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                portraitSlot(Modifier.fillMaxWidth())
            }
            Column(
                modifier = Modifier.weight(0.9f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                traitsSlot(Modifier.fillMaxWidth())
                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    contentSlot(Modifier.fillMaxSize())
                }
            }
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            navigationSlot(Modifier.fillMaxWidth())
            traitsSlot(Modifier.fillMaxWidth())
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    attributesSlot(Modifier.fillMaxWidth())
                    portraitSlot(Modifier.fillMaxWidth())
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        contentSlot(Modifier.fillMaxSize())
                    }
                }
            }
        }
    }
}

@Composable
private fun PortraitPanel(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = SwadeDesignSystem.tokens.panelShape,
        border = SwadeDesignSystem.panelBorder(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Character Portrait",
                style = SwadeDesignSystem.tokens.headerTextStyle,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun AttributeSection(
    isLandscape: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Attributes",
            style = SwadeDesignSystem.tokens.headerTextStyle.copy(fontWeight = FontWeight.Bold)
        )
        AttributeGrid(
            attributes = listOf(
                AttributeItem("Agility", "d6"),
                AttributeItem("Smarts", "d8"),
                AttributeItem("Spirit", "d6"),
                AttributeItem("Strength", "d6"),
                AttributeItem("Vigor", "d6")
            ),
            isLandscape = isLandscape,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun TraitsSection(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        InfoPanel(
            title = "Traits",
            rows = listOf(
                "Pace" to "6",
                "Parry" to "5",
                "Toughness" to "7"
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = SwadeDesignSystem.tokens.panelShape,
            border = SwadeDesignSystem.panelBorder(),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Bennies",
                    style = SwadeDesignSystem.tokens.headerTextStyle
                )
                Spacer(modifier = Modifier.height(8.dp))
                BennieRow(count = 3)
            }
        }
    }
}

@Composable
private fun CharacterTabContent(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = SwadeDesignSystem.tokens.panelShape,
        border = SwadeDesignSystem.panelBorder(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Name: Adventurer",
                style = SwadeDesignSystem.tokens.bodyTextStyle
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Race: Human",
                style = SwadeDesignSystem.tokens.bodyTextStyle
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Archetype: Explorer",
                style = SwadeDesignSystem.tokens.bodyTextStyle
            )
        }
    }
}

@Composable
private fun SkillsTabContent(modifier: Modifier = Modifier) {
    SkillsList(
        skills = listOf(
            "Shooting d6",
            "Notice d8",
            "Stealth d6",
            "Survival d4"
        ),
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
    )
}

@Composable
private fun PlaceholderTabContent(title: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = SwadeDesignSystem.tokens.panelShape,
        border = SwadeDesignSystem.panelBorder(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                style = SwadeDesignSystem.tokens.headerTextStyle
            )
        }
    }
}
