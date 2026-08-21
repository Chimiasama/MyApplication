package com.example.swadebuilder.ui.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.sp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.model.MechaCatalogWrapper
import com.example.swadebuilder.model.MechaItem
import com.example.swadebuilder.model.MechaModCatalogWrapper
import com.example.swadebuilder.model.MechaModItem
import com.example.swadebuilder.model.MechaWeaponCatalogWrapper
import com.example.swadebuilder.model.MechaWeaponItem
import com.example.swadebuilder.ui.components.SectionHeader
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream

@OptIn(ExperimentalLayoutApi::class, ExperimentalSerializationApi::class)
@Composable
fun MechasSection(
    state: CriadorState,
    onUserFeedback: () -> Unit = {}
) {
    val context = LocalContext.current

    val mechaCatalog = remember(context) {
        runCatching {
            context.assets.open("scifi_mechas.json").use { input ->
                Json { ignoreUnknownKeys = true }.decodeFromStream<MechaCatalogWrapper>(input).mechas
            }
        }.getOrElse { emptyList() }
    }

    val modCatalog = remember(context) {
        runCatching {
            context.assets.open("scifi_mecha_mods.json").use { input ->
                Json { ignoreUnknownKeys = true }.decodeFromStream<MechaModCatalogWrapper>(input).modificadores
            }
        }.getOrElse { emptyList() }
    }

    val weaponCatalog = remember(context) {
        runCatching {
            context.assets.open("scifi_mecha_weapons.json").use { input ->
                Json { ignoreUnknownKeys = true }.decodeFromStream<MechaWeaponCatalogWrapper>(input).armas
            }
        }.getOrElse { emptyList() }
    }

    var showCreateCustomDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionHeader(centerText = "Gestão de Mechas")
            Text(
                text = "Selecione um chassi base oficial ou crie um Mecha do zero. Adicione Modificadores e Armamentos (que consomem espaços de MODs). Qualidades Negativas devolvem espaços de MODs para customização extra.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Catálogo de Chassis Disponíveis e Botão para Criar do Zero
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Modelos / Chassis Disponíveis",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        mechaCatalog.forEach { mecha ->
                            Text(
                                text = "+ ${mecha.nome}",
                                modifier = Modifier
                                    .clip(MaterialTheme.shapes.small)
                                    .clickable {
                                        state.mechasSelecionados.add(
                                            mecha.copy(id = "${mecha.id}_${System.currentTimeMillis()}")
                                        )
                                        onUserFeedback()
                                    }
                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Text(
                            text = "+ Criar Mecha Personalizado",
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.small)
                                .clickable { showCreateCustomDialog = true }
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Mechas do Personagem
        item {
            SectionHeader(centerText = "Mechas Equipados (${state.mechasSelecionados.size})")
        }

        if (state.mechasSelecionados.isEmpty()) {
            item {
                Text(
                    text = "Nenhum Mecha selecionado. Escolha um modelo acima ou crie um do zero.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(8.dp)
                )
            }
        } else {
            items(state.mechasSelecionados, key = { it.id }) { mecha ->
                MechaCardItem(
                    mecha = mecha,
                    modCatalog = modCatalog,
                    weaponCatalog = weaponCatalog,
                    onUpdateMecha = { updated ->
                        val index = state.mechasSelecionados.indexOfFirst { it.id == mecha.id }
                        if (index != -1) {
                            state.mechasSelecionados[index] = updated
                        }
                    },
                    onRemove = {
                        state.mechasSelecionados.removeIf { it.id == mecha.id }
                        onUserFeedback()
                    }
                )
            }
        }
    }

    // Modal de Criação de Mecha Customizado do Zero
    if (showCreateCustomDialog) {
        CreateCustomMechaDialog(
            onDismiss = { showCreateCustomDialog = false },
            onCreate = { newMecha ->
                state.mechasSelecionados.add(newMecha)
                showCreateCustomDialog = false
                onUserFeedback()
            }
        )
    }
}

@Composable
private fun CreateCustomMechaDialog(
    onDismiss: () -> Unit,
    onCreate: (MechaItem) -> Unit
) {
    var nomeText by remember { mutableStateOf("Mecha Customizado") }
    var categoriaChassiText by remember { mutableStateOf("Grande") }
    var tamanhoText by remember { mutableStateOf("7") }
    var manobText by remember { mutableStateOf("0") }
    var velMaxText by remember { mutableStateOf("8") }
    var resBaseText by remember { mutableStateOf("15") }
    var armBaseText by remember { mutableStateOf("20") }
    var ferimentosText by remember { mutableStateOf("4") }
    var forcaText by remember { mutableStateOf("d12+7") }
    var energiaText by remember { mutableStateOf("5") }
    var modMaxText by remember { mutableStateOf("21") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            FilledTonalButton(
                onClick = {
                    val customMecha = MechaItem(
                        id = "custom_mech_${System.currentTimeMillis()}",
                        nome = nomeText.ifBlank { "Mecha Customizado" },
                        categoria_chassi = categoriaChassiText,
                        tamanho = tamanhoText.toIntOrNull() ?: 7,
                        manobrabilidade = manobText.toIntOrNull() ?: 0,
                        vel_maxima = velMaxText.toIntOrNull() ?: 8,
                        resistencia_base = resBaseText.toIntOrNull() ?: 15,
                        armadura_base = armBaseText.toIntOrNull() ?: 20,
                        ferimentos = ferimentosText.toIntOrNull() ?: 4,
                        forca = forcaText.ifBlank { "d12+7" },
                        energia_dias = energiaText.toIntOrNull() ?: 5,
                        mod_pontos_max = modMaxText.toIntOrNull() ?: 21,
                        sistemas_instalados = listOf("Selado", "Sensores HUD")
                    )
                    onCreate(customMecha)
                }
            ) {
                Text("Criar Mecha")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        title = { Text("Criar Mecha do Zero") },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = nomeText,
                    onValueChange = { nomeText = it },
                    label = { Text("Nome / Apelido do Mecha") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = tamanhoText,
                        onValueChange = { tamanhoText = it.filter { char -> char.isDigit() || char == '-' } },
                        label = { Text("Tamanho") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = manobText,
                        onValueChange = { manobText = it.filter { char -> char.isDigit() || char == '-' } },
                        label = { Text("Manobrabilidade") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = velMaxText,
                        onValueChange = { velMaxText = it.filter { char -> char.isDigit() } },
                        label = { Text("Vel. Máxima") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = ferimentosText,
                        onValueChange = { ferimentosText = it.filter { char -> char.isDigit() } },
                        label = { Text("Ferimentos") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = resBaseText,
                        onValueChange = { resBaseText = it.filter { char -> char.isDigit() } },
                        label = { Text("Resistência Base") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = armBaseText,
                        onValueChange = { armBaseText = it.filter { char -> char.isDigit() } },
                        label = { Text("Armadura Base") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = forcaText,
                        onValueChange = { forcaText = it },
                        label = { Text("Força") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = modMaxText,
                        onValueChange = { modMaxText = it.filter { char -> char.isDigit() } },
                        label = { Text("Espaços MODs Max") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        }
    )
}

@Composable
private fun CircleToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(if (checked) MaterialTheme.colorScheme.primary else Color.Transparent)
            .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onPrimary)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MechaCardItem(
    mecha: MechaItem,
    modCatalog: List<MechaModItem>,
    weaponCatalog: List<MechaWeaponItem>,
    onUpdateMecha: (MechaItem) -> Unit,
    onRemove: () -> Unit
) {
    var weaponInput by remember { mutableStateOf("") }
    var systemInput by remember { mutableStateOf("") }
    var showModDialog by remember { mutableStateOf(false) }
    var showWeaponCatalogDialog by remember { mutableStateOf(false) }

    // Dynamic stat calculations with modifiers and equipped weapons MOD costs
    val modsDoModifiers = mecha.mods_instalados.sumOf { it.mods_cost }
    val modsDasArmas = mecha.armas_equipadas.sumOf { armaStr ->
        val found = weaponCatalog.firstOrNull { w -> armaStr.contains(w.nome, ignoreCase = true) || w.nome.contains(armaStr, ignoreCase = true) }
        found?.mods_cost ?: 0
    }
    val modsGasto = modsDoModifiers + modsDasArmas
    val modsRestantes = mecha.mod_pontos_max - modsGasto

    val extraRes = mecha.mods_instalados.count { it.id == "mod_def_resistencia" } - (2 * mecha.mods_instalados.count { it.id == "mod_neg_danificado" })
    val resistenciaCalc = mecha.resistencia_base + extraRes

    val extraArmadura = (2 * mecha.mods_instalados.count { it.id == "mod_def_armadura_extra" }) + (2 * mecha.customizacoes.blindagem_extra)
    val armaduraCalc = mecha.armadura_base + extraArmadura

    val manobCalc = mecha.manobrabilidade + mecha.mods_instalados.count { it.id == "mod_loc_manobrabilidade" } - mecha.mods_instalados.count { it.id == "mod_neg_manob_reduzida" }
    val velCalc = mecha.vel_maxima + mecha.mods_instalados.count { it.id == "mod_loc_velocidade_ampliada" } - mecha.mods_instalados.count { it.id == "mod_neg_vel_reduzida" }
    val ferimentosCalc = (mecha.ferimentos - mecha.mods_instalados.count { it.id == "mod_neg_fragil" }).coerceAtLeast(1)

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = mecha.nome,
                    onValueChange = { newName -> onUpdateMecha(mecha.copy(nome = newName)) },
                    label = { Text("Nome / Apelido do Mecha") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remover Mecha",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Text(
                text = "Tam: ${mecha.tamanho} | Manob: ${if (manobCalc >= 0) "+$manobCalc" else "$manobCalc"} | Vel. Máx: $velCalc | Resist: $resistenciaCalc ($armaduraCalc) | Ferimentos: $ferimentosCalc | Força: ${mecha.forca}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Capacidade MODs: Espaços Usados: $modsGasto / ${mecha.mod_pontos_max} (Restantes: $modsRestantes)",
                style = MaterialTheme.typography.labelSmall,
                color = if (modsRestantes < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider(thickness = 0.5.dp)

            // Modificadores Instalados
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Modificadores & Qualidades", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text(
                        text = "+",
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { showModDialog = true }
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    mecha.mods_instalados.forEach { mod ->
                        val isNeg = mod.mods_cost < 0
                        InputChip(
                            selected = true,
                            onClick = {
                                onUpdateMecha(
                                    mecha.copy(
                                        mods_instalados = mecha.mods_instalados - mod
                                    )
                                )
                            },
                            label = { Text("${mod.nome} [${if (isNeg) "${mod.mods_cost} MODs" else "+${mod.mods_cost} MODs"}]") },
                            trailingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
                        )
                    }
                }
            }

            // Customizações Rápidas
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Blindagem Extra: ", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        FilledTonalIconButton(
                            onClick = {
                                val newBlindagem = (mecha.customizacoes.blindagem_extra - 1).coerceAtLeast(0)
                                onUpdateMecha(
                                    mecha.copy(
                                        customizacoes = mecha.customizacoes.copy(blindagem_extra = newBlindagem)
                                    )
                                )
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Diminuir", modifier = Modifier.size(14.dp))
                        }
                        Text(
                            text = " +${mecha.customizacoes.blindagem_extra} ",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        FilledTonalIconButton(
                            onClick = {
                                val newBlindagem = mecha.customizacoes.blindagem_extra + 1
                                onUpdateMecha(
                                    mecha.copy(
                                        customizacoes = mecha.customizacoes.copy(blindagem_extra = newBlindagem)
                                    )
                                )
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Aumentar", modifier = Modifier.size(14.dp))
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Propulsores", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        CircleToggle(
                            checked = mecha.customizacoes.propulsores,
                            onCheckedChange = { prop ->
                                onUpdateMecha(
                                    mecha.copy(
                                        customizacoes = mecha.customizacoes.copy(propulsores = prop)
                                    )
                                )
                            }
                        )
                    }
                }
            }

            // Armas Equipadas
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Armas Equipadas", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    OutlinedButton(
                        onClick = { showWeaponCatalogDialog = true },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Catálogo de Armas", fontSize = 12.sp)
                    }
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    mecha.armas_equipadas.forEach { arma ->
                        InputChip(
                            selected = true,
                            onClick = {
                                onUpdateMecha(
                                    mecha.copy(
                                        armas_equipadas = mecha.armas_equipadas - arma
                                    )
                                )
                            },
                            label = { Text(arma) },
                            trailingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = weaponInput,
                        onValueChange = { weaponInput = it },
                        label = { Text("Arma Personalizada") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(Modifier.width(8.dp))
                    FilledTonalIconButton(
                        onClick = {
                            if (weaponInput.isNotBlank()) {
                                onUpdateMecha(
                                    mecha.copy(
                                        armas_equipadas = mecha.armas_equipadas + weaponInput.trim()
                                    )
                                )
                                weaponInput = ""
                            }
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Adicionar")
                    }
                }
            }

            // Anotações
            OutlinedTextField(
                value = mecha.customizacoes.anotacoes,
                onValueChange = { note ->
                    onUpdateMecha(
                        mecha.copy(
                            customizacoes = mecha.customizacoes.copy(anotacoes = note)
                        )
                    )
                },
                label = { Text("Anotações do Mecha") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // Modal de Modificadores com Categorias Expansíveis
    if (showModDialog) {
        val expandedCategories = remember { mutableStateMapOf<String, Boolean>() }

        AlertDialog(
            onDismissRequest = { showModDialog = false },
            confirmButton = {
                TextButton(onClick = { showModDialog = false }) { Text("Fechar") }
            },
            title = {
                Text(
                    text = "Catálogo de Modificadores de Mecha",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val grouped = modCatalog.groupBy { it.categoria }
                    grouped.forEach { (catName, mods) ->
                        val isExpanded = expandedCategories[catName] ?: false

                        item {
                            OutlinedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expandedCategories[catName] = !isExpanded },
                                colors = CardDefaults.outlinedCardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$catName (${mods.size})",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = if (isExpanded) "Recolher" else "Expandir",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        if (isExpanded) {
                            items(mods) { mod ->
                                val currentUses = mecha.mods_instalados.count { it.id == mod.id }
                                val isMaxed = currentUses >= mod.max_uses
                                val isNeg = mod.mods_cost < 0

                                OutlinedCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 6.dp),
                                    colors = CardDefaults.outlinedCardColors(
                                        containerColor = if (isNeg) MaterialTheme.colorScheme.error.copy(alpha = 0.06f)
                                        else MaterialTheme.colorScheme.surfaceContainerLow
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = mod.nome,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = if (isNeg) "${mod.mods_cost} MODs" else "+${mod.mods_cost} MODs",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = if (isNeg) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                            Text(
                                                text = mod.descricao,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Spacer(Modifier.width(8.dp))
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = if (isMaxed) "Máx" else "+",
                                                modifier = Modifier
                                                    .clip(CircleShape)
                                                    .clickable(enabled = !isMaxed) {
                                                        onUpdateMecha(
                                                            mecha.copy(
                                                                mods_instalados = mecha.mods_instalados + mod
                                                            )
                                                        )
                                                    }
                                                    .padding(horizontal = 10.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.titleMedium,
                                                color = if (isMaxed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (currentUses > 0) {
                                                Text(
                                                    text = "-",
                                                    modifier = Modifier
                                                        .clip(CircleShape)
                                                        .clickable {
                                                            onUpdateMecha(
                                                                mecha.copy(
                                                                    mods_instalados = mecha.mods_instalados - mod
                                                                )
                                                            )
                                                        }
                                                        .padding(horizontal = 10.dp, vertical = 0.dp),
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = MaterialTheme.colorScheme.error,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        )
    }

    // Modal de Catálogo de Armas Oficiais para Mechas
    if (showWeaponCatalogDialog) {
        AlertDialog(
            onDismissRequest = { showWeaponCatalogDialog = false },
            confirmButton = {
                TextButton(onClick = { showWeaponCatalogDialog = false }) { Text("Fechar") }
            },
            title = {
                Text(
                    text = "Armas de Mecha e Veículos",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(weaponCatalog) { w ->
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = w.nome,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${w.mods_cost} MODs",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Text(
                                        text = w.descricao,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                OutlinedButton(
                                    onClick = {
                                        onUpdateMecha(
                                            mecha.copy(
                                                armas_equipadas = mecha.armas_equipadas + w.nome
                                            )
                                        )
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Equipar", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}
