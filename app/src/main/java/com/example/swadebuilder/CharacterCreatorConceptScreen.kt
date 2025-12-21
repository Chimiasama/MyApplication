package com.example.swadebuilder

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val BackgroundColor = Color(0xFF1B1B1B)
private val CardColor = Color(0xFF2A2A2A)
private val BorderColor = Color(0xFF5A4A3A)
private val AccentColor = Color(0xFFB08D57)
private val TextPrimary = Color(0xFFF2E9DC)
private val TextSecondary = Color(0xFFCBBBA0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterCreatorConceptScreen() {
    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Savage Creator",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Voltar",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Opções",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF202020)
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF202020)) {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = TextPrimary
                        )
                    },
                    label = {
                        Text("Info", color = TextSecondary)
                    }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Casino,
                            contentDescription = "Dice Roller",
                            tint = TextPrimary
                        )
                    },
                    label = {
                        Text("Dice Roller", color = TextSecondary)
                    }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = TextPrimary
                        )
                    },
                    label = {
                        Text("Settings", color = TextSecondary)
                    }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { },
                containerColor = AccentColor,
                contentColor = Color(0xFF1B1B1B)
            ) {
                Text(
                    text = "FINALIZAR",
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF1A1A1A), Color(0xFF262626))
                    )
                )
                .padding(paddingValues)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(0.4f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        MenuButton(text = "Personagem")
                        MenuButton(text = "Perícias")
                        MenuButton(text = "Vantagens & Complicações")
                        MenuButton(text = "Equipamento")
                        MenuButton(text = "Notas")
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    SectionCard(title = "ATRIBUTOS") {
                        StatRow(label = "Agility", value = "d6")
                        StatRow(label = "Smarts", value = "d8")
                        StatRow(label = "Spirit", value = "d6")
                        StatRow(label = "Strength", value = "d6")
                        StatRow(label = "Vigor", value = "d8")
                    }

                    SectionCard(
                        title = "Character Portrait",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                    ) {
                        PortraitPlaceholder()
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(0.6f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SectionCard(title = "TRAITS") {
                        DetailRow(label = "Pace", value = "6")
                        DetailRow(label = "Parry", value = "5")
                        DetailRow(label = "Toughness", value = "7")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Bennies",
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "3",
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            BenniesRow(count = 3)
                        }
                    }

                    SectionCard(title = "SKILLS") {
                        DetailRow(label = "Shooting", value = "d6")
                        DetailRow(label = "Notice", value = "d8")
                        DetailRow(label = "Stealth", value = "d6")
                        DetailRow(label = "Survival", value = "d4")
                    }

                    SectionCard(title = "GEAR") {
                        DetailRow(label = "Equipment", value = "Explorer's Pack")
                        DetailRow(label = "Weapons", value = "Revolver, Knife")
                        DetailRow(label = "Armor", value = "Leather Jacket")
                        DetailRow(label = "Inventory", value = "Supplies")
                        DetailRow(label = "Money", value = "$100")
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuButton(text: String) {
    val shape = RoundedCornerShape(12.dp)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        colors = CardDefaults.cardColors(containerColor = CardColor),
        shape = shape,
        border = BorderStroke(1.dp, BorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CardColor),
        shape = shape,
        border = BorderStroke(1.dp, BorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                color = AccentColor,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
            content()
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = TextSecondary,
            modifier = Modifier.weight(1f)
        )
        DiceChip(text = value)
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = TextSecondary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun DiceChip(text: String) {
    Surface(
        modifier = Modifier
            .wrapContentHeight()
            .shadow(4.dp, RoundedCornerShape(10.dp)),
        color = Color(0xFF3A3A3A),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun BenniesRow(count: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(count) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFB1352A))
                    .border(BorderStroke(1.dp, Color(0xFF741E18)), CircleShape)
            )
        }
    }
}

@Composable
private fun PortraitPlaceholder() {
    val borderShape = RoundedCornerShape(14.dp)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF3A342A), Color(0xFF2A241B))
                ),
                borderShape
            )
            .border(BorderStroke(2.dp, BorderColor), borderShape)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Portrait",
            tint = Color(0xFF8C7A66),
            modifier = Modifier.size(100.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1B1B1B)
@Composable
private fun CharacterCreatorConceptScreenPreview() {
    MaterialTheme {
        CharacterCreatorConceptScreen()
    }
}
