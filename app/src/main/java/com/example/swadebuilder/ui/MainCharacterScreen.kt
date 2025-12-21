package com.example.swadebuilder.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.swadebuilder.model.CharacterViewModel
import com.example.swadebuilder.model.ScreenTab
import com.example.swadebuilder.ui.components.AttributeGrid
import com.example.swadebuilder.ui.components.BennieComponent
import com.example.swadebuilder.ui.components.InfoPanel
import com.example.swadebuilder.ui.components.SavageNavigationMenu
import com.example.swadebuilder.ui.theme.SwadeDesignSystem
import com.example.swadebuilder.ui.theme.SwadeTheme

@Composable
fun MainCharacterScreen(
    viewModel: CharacterViewModel = viewModel()
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val skillsList = viewModel.skillsList

    SwadeTheme {
        MainCharacterScreenLayout(
            sidebarContent = {
                Column {
                   Text(
                        text = "SAVAGE WORLDS",
                        style = SwadeDesignSystem.typography.header,
                        color = SwadeDesignSystem.colors.onBackground
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    SavageNavigationMenu(
                        tabs = listOf(ScreenTab.Character, ScreenTab.Skills, ScreenTab.Edges, ScreenTab.Gear, ScreenTab.Notes),
                        selectedTab = selectedTab,
                        onTabSelected = { viewModel.selectTab(it) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = SwadeDesignSystem.colors.border)
                    Spacer(modifier = Modifier.height(16.dp))
                    // In Landscape, attributes live in Sidebar
                    AttributeGrid(stats = stats, isLandscape = true)
                }
            },
            centerContent = {
                // Portrait Placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(Color.DarkGray)
                ) {
                    Text("Character Portrait", modifier = Modifier.align(Alignment.Center), color = Color.White)
                }
                Spacer(modifier = Modifier.height(16.dp))
                BennieComponent(count = stats.bennies)
            },
            rightContent = {
                InfoPanel(stats = stats)
                Spacer(modifier = Modifier.height(16.dp))
                TabContentSlot(selectedTab = selectedTab, skills = skillsList)
            },
            // Portrait-specific combined content
            portraitContent = {
                // Header + Bennies
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SAVAGE WORLDS",
                        style = SwadeDesignSystem.typography.header,
                        color = SwadeDesignSystem.colors.onBackground
                    )
                    BennieComponent(count = stats.bennies)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Main Scrollable Area
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Attributes Stack
                    AttributeGrid(stats = stats, isLandscape = false)

                    Spacer(modifier = Modifier.height(16.dp))

                    InfoPanel(stats = stats)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Tab Content
                    TabContentSlot(selectedTab = selectedTab, skills = skillsList)
                }

                // Navigation at bottom for Portrait flow
                 Spacer(modifier = Modifier.height(8.dp))
                 SavageNavigationMenu(
                        tabs = listOf(ScreenTab.Character, ScreenTab.Skills, ScreenTab.Edges, ScreenTab.Gear, ScreenTab.Notes),
                        selectedTab = selectedTab,
                        onTabSelected = { viewModel.selectTab(it) }
                    )
            }
        )
    }
}

@Composable
fun MainCharacterScreenLayout(
    sidebarContent: @Composable () -> Unit,
    centerContent: @Composable () -> Unit,
    rightContent: @Composable () -> Unit,
    portraitContent: @Composable () -> Unit
) {
    Scaffold(
        containerColor = SwadeDesignSystem.colors.background
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isLandscape = maxWidth > 600.dp

            if (isLandscape) {
                // LANDSCAPE LAYOUT (3 Columns)
                Row(modifier = Modifier.fillMaxSize()) {
                    // Col 1: Sidebar
                    Column(
                        modifier = Modifier
                            .width(280.dp) // Increased slightly for 2-col attributes
                            .fillMaxHeight()
                            .padding(SwadeDesignSystem.dimensions.paddingMedium)
                            .verticalScroll(rememberScrollState())
                    ) {
                        sidebarContent()
                    }

                    // Col 2: Center
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(SwadeDesignSystem.dimensions.paddingMedium),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        centerContent()
                    }

                    // Col 3: Right
                    Column(
                        modifier = Modifier
                            .weight(1.5f)
                            .fillMaxHeight()
                            .padding(SwadeDesignSystem.dimensions.paddingMedium)
                    ) {
                        rightContent()
                    }
                }
            } else {
                // PORTRAIT LAYOUT
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(SwadeDesignSystem.dimensions.paddingMedium)
                ) {
                    portraitContent()
                }
            }
        }
    }
}

@Composable
fun TabContentSlot(
    selectedTab: ScreenTab,
    skills: List<Pair<String, String>>
) {
    AnimatedContent(targetState = selectedTab, label = "TabSwitch") { tab ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SwadeDesignSystem.colors.surface.copy(alpha = 0.2f), SwadeDesignSystem.shapes.card)
                .padding(16.dp)
        ) {
            when (tab) {
                ScreenTab.Skills -> {
                    Column {
                        Text("Skills", style = SwadeDesignSystem.typography.subHeader, color = SwadeDesignSystem.colors.onSurface)
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyColumn {
                            items(skills) { (name, die) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(name, style = SwadeDesignSystem.typography.body, color = SwadeDesignSystem.colors.onSurface)
                                    Text(die, style = SwadeDesignSystem.typography.statValue, color = SwadeDesignSystem.colors.highlight)
                                }
                                HorizontalDivider(color = SwadeDesignSystem.colors.border.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
                else -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Content for ${tab.title}",
                            style = SwadeDesignSystem.typography.body,
                            color = SwadeDesignSystem.colors.onSurface
                        )
                    }
                }
            }
        }
    }
}
