package com.example.swadebuilder.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Define Colors locally to ensure standalone compilation without relying on Theme.kt specifics
private val MetalBackground = Color(0xFF1b1b1b)
private val MetalCardSurface = Color(0xFF2a2a2a)
private val MetalBorder = Color(0xFF5a4a3a)
private val MetalTextPrimary = Color(0xFFE0E0E0)
private val MetalTextSecondary = Color(0xFFAAAAAA)
private val GoldAccent = Color(0xFFC5A059) // Gold-ish for emphasis
private val TraitPaceColor = Color(0xFF4CAF50)
private val TraitParryColor = Color(0xFF2196F3)
private val TraitToughnessColor = Color(0xFFF44336)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterCreatorConceptScreen() {
    Scaffold(
        containerColor = MetalBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Savage Creator",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            color = MetalTextPrimary
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { /* TODO: Navigation */ }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = MetalTextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Settings */ }) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "Opções",
                            tint = MetalTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MetalBackground,
                    titleContentColor = MetalTextPrimary
                ),
                modifier = Modifier.border(
                    width = 1.dp,
                    color = MetalBorder.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
                )
            )
        },
        bottomBar = {
            ConceptBottomBar()
        }
    ) { paddingValues ->
        // Main content area with 2 columns
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(8.dp)
        ) {
            // Left Column (40%)
            Column(
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxHeight()
                    .padding(end = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Navigation Menu
                ConceptMenuSection()

                // Attributes
                ConceptAttributesSection()

                // Portrait (takes remaining space or fixed height)
                ConceptPortraitSection(modifier = Modifier.weight(1f))
            }

            // Right Column (60%)
            Column(
                modifier = Modifier
                    .weight(0.6f)
                    .fillMaxHeight()
                    .padding(start = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Traits (Pace, Parry, Toughness, Bennies)
                ConceptTraitsSection()

                // Skills
                ConceptSkillsSection()

                // Gear
                ConceptGearSection(modifier = Modifier.weight(1f))

                // Finalize Button
                Button(
                    onClick = { /* Action */ },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .border(1.dp, MetalBorder, RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = "FINALIZAR",
                        color = Color.Black,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }
        }
    }
}

// --- Auxiliary Composables ---

@Composable
fun ConceptBottomBar() {
    NavigationBar(
        containerColor = MetalCardSurface,
        contentColor = MetalTextPrimary,
        modifier = Modifier.border(
            width = 1.dp,
            color = MetalBorder.copy(alpha = 0.5f),
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
        )
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Info, contentDescription = "Info") },
            label = { Text("Info") },
            selected = false,
            onClick = {},
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = MetalTextSecondary,
                unselectedTextColor = MetalTextSecondary
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Casino, contentDescription = "Rolador") },
            label = { Text("Dice") },
            selected = true,
            onClick = {},
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = GoldAccent,
                selectedTextColor = GoldAccent,
                indicatorColor = MetalBackground
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Settings, contentDescription = "Config") },
            label = { Text("Settings") },
            selected = false,
            onClick = {},
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = MetalTextSecondary,
                unselectedTextColor = MetalTextSecondary
            )
        )
    }
}

@Composable
fun ConceptMenuSection() {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        MenuButton("Personagem", true)
        MenuButton("Perícias", false)
        MenuButton("Vantagens & Compl.", false)
        MenuButton("Equipamento", false)
        MenuButton("Notas", false)
    }
}

@Composable
fun MenuButton(text: String, selected: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(
                color = if (selected) GoldAccent.copy(alpha = 0.2f) else MetalCardSurface,
                shape = RoundedCornerShape(4.dp)
            )
            .border(
                width = 1.dp,
                color = if (selected) GoldAccent else MetalBorder,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            color = if (selected) GoldAccent else MetalTextPrimary,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            maxLines = 1
        )
    }
}

@Composable
fun ConceptAttributesSection() {
    SectionCard(title = "ATRIBUTOS") {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            AttributeRow("Agilidade", "d8")
            AttributeRow("Astúcia", "d6")
            AttributeRow("Espírito", "d6")
            AttributeRow("Força", "d4")
            AttributeRow("Vigor", "d6")
        }
    }
}

@Composable
fun AttributeRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = MetalTextSecondary,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
        )
        Box(
            modifier = Modifier
                .background(MetalBorder, CircleShape)
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = value,
                color = MetalTextPrimary,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun ConceptPortraitSection(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(2.dp, MetalBorder, RoundedCornerShape(8.dp))
            .background(Color.Black, RoundedCornerShape(8.dp))
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, MetalCardSurface)
                    ),
                    shape = RoundedCornerShape(4.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            // Placeholder silhouette
            Icon(
                imageVector = Icons.Filled.Info, // Placeholder icon
                contentDescription = null,
                tint = MetalBorder,
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = "PORTRAIT",
                color = MetalBorder,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp)
            )
        }
    }
}

@Composable
fun ConceptTraitsSection() {
    SectionCard(title = "TRAITS") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TraitItem("Mov.", "6", TraitPaceColor)
            TraitItem("Aparar", "5", TraitParryColor)
            TraitItem("Resist.", "5", TraitToughnessColor)

            // Bennies
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Benes",
                    style = MaterialTheme.typography.labelSmall,
                    color = MetalTextSecondary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(GoldAccent)
                                .border(1.dp, Color.White, CircleShape)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TraitItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MetalTextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = color
        )
    }
}

@Composable
fun ConceptSkillsSection() {
    SectionCard(title = "SKILLS") {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            SkillRow("Atirar", "d6")
            SkillRow("Perceber", "d8")
            SkillRow("Furtividade", "d6")
            SkillRow("Sobrevivência", "d4")
        }
    }
}

@Composable
fun SkillRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MetalTextPrimary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = GoldAccent
        )
    }
}

@Composable
fun ConceptGearSection(modifier: Modifier = Modifier) {
    SectionCard(title = "GEAR", modifier = modifier) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(end = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            GearItem("Espada Curta", "For+d6")
            GearItem("Corselete de Couro", "+2 Armadura")
            HorizontalDivider(color = MetalBorder, thickness = 1.dp)
            GearItem("Mochila", "2kg")
            GearItem("Cantil", "Cheio")
            GearItem("Rações", "5 dias")
            GearItem("Corda", "10m")

            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Dinheiro", color = GoldAccent, style = MaterialTheme.typography.bodySmall)
                Text("$ 500", color = MetalTextPrimary, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun GearItem(name: String, detail: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            color = MetalTextPrimary
        )
        Text(
            text = detail,
            style = MaterialTheme.typography.labelSmall,
            color = MetalTextSecondary
        )
    }
}


@Composable
fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MetalBorder)
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = Color.Black
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false) // Allow dynamic height but respect parents if constrained
                .background(MetalCardSurface)
                .border(1.dp, MetalBorder)
                .padding(8.dp)
        ) {
            content()
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun PreviewCharacterCreatorConcept() {
    CharacterCreatorConceptScreen()
}
