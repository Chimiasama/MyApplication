package com.example.swadebuilder.ui.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.model.CiberneticoCatalogWrapper
import com.example.swadebuilder.model.CiberneticoItem
import com.example.swadebuilder.ui.components.SectionHeader
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream

@OptIn(ExperimentalSerializationApi::class)
@Composable
fun CiberneticosSection(
    state: CriadorState,
    onUserFeedback: () -> Unit = {}
) {
    val context = LocalContext.current

    val ciberneticoCatalog = remember(context) {
        runCatching {
            context.assets.open("scifi_ciberneticos.json").use { input ->
                Json { ignoreUnknownKeys = true }.decodeFromStream<CiberneticoCatalogWrapper>(input).ciberneticos
            }
        }.getOrElse { emptyList() }
    }

    val tensaoTotal = state.totalTensaoCibernetica()
    val tensaoLimite = state.valorLimiteTensao().second
    val isPersonagemRobotico = state.isPersonagemRobotico()
    val tensaoLabel = if (isPersonagemRobotico) "Mods" else "Tensão"
    val tensaoExcedida = tensaoTotal > tensaoLimite

    var customName by remember { mutableStateOf("") }
    var customStrainText by remember { mutableStateOf("1") }
    var customEffect by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionHeader(centerText = "Implantes Cibernéticos")
            Text(
                text = "Gerencie e instale peças cibernéticas das categorias Corpo, Defensivo, Ofensivo e Locomoção. O limite de Tensão depende de Espírito e Vigor.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Card do Limite de Tensão
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = if (tensaoExcedida) MaterialTheme.colorScheme.errorContainer
                    else MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$tensaoLabel Cibernética: $tensaoTotal / $tensaoLimite",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (tensaoExcedida) MaterialTheme.colorScheme.onErrorContainer
                            else MaterialTheme.colorScheme.primary
                        )
                        if (tensaoExcedida) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Limite de Tensão Excedido",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    LinearProgressIndicator(
                        progress = { if (tensaoLimite > 0) (tensaoTotal.toFloat() / tensaoLimite.toFloat()).coerceIn(0f, 1f) else 1f },
                        modifier = Modifier.fillMaxWidth(),
                        color = if (tensaoExcedida) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    )

                    if (tensaoExcedida) {
                        Text(
                            text = "Atenção: Tensão limite excedida (+${tensaoTotal - tensaoLimite}). Efeitos colaterais e penalidades podem ser aplicados.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }

        // Implantes por Categoria
        val groupedCatalog = ciberneticoCatalog.groupBy { item ->
            when (item.id.split("_").getOrNull(1)?.uppercase()) {
                "CORPO" -> "CORPO"
                "DEF" -> "DEFENSIVO"
                "OF" -> "OFENSIVO"
                "LOC" -> "LOCOMOÇÃO"
                else -> "OUTROS"
            }
        }

        groupedCatalog.forEach { (catName, itemsInCat) ->
            item {
                SectionHeader(centerText = catName)
            }

            items(itemsInCat, key = { it.id }) { item ->
                val currentInstalledCount = state.ciberneticosInstalados.count { it.id == item.id }

                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.nome,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Tensão: ${item.strain_custo}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (item.efeito.isNotBlank()) {
                                Text(
                                    text = item.efeito,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (currentInstalledCount > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Button(
                                    onClick = {
                                        state.ciberneticosInstalados.add(item.copy(id = "${item.id}_${System.currentTimeMillis()}"))
                                        onUserFeedback()
                                    }
                                ) {
                                    Text("+1 ($currentInstalledCount)")
                                }
                                Spacer(Modifier.width(4.dp))
                                Button(
                                    onClick = {
                                        val idx = state.ciberneticosInstalados.indexOfFirst { it.id.startsWith(item.id) || it.id == item.id }
                                        if (idx != -1) {
                                            state.ciberneticosInstalados.removeAt(idx)
                                        }
                                        onUserFeedback()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text("-")
                                }
                            }
                        } else {
                            Button(
                                onClick = {
                                    state.ciberneticosInstalados.add(item)
                                    onUserFeedback()
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                                Text("Instalar")
                            }
                        }
                    }
                }
            }
        }

        // Adicionar Implante Customizado
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
                        text = "Criar Implante Customizado",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text("Nome do Implante") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = customStrainText,
                            onValueChange = { customStrainText = it.filter { char -> char.isDigit() } },
                            label = { Text("Custo de Tensão") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Spacer(Modifier.width(12.dp))
                        OutlinedTextField(
                            value = customEffect,
                            onValueChange = { customEffect = it },
                            label = { Text("Efeito / Bônus") },
                            modifier = Modifier.weight(2f),
                            singleLine = true
                        )
                    }

                    Button(
                        onClick = {
                            if (customName.isNotBlank()) {
                                val strainVal = customStrainText.toIntOrNull() ?: 1
                                val customItem = CiberneticoItem(
                                    id = "custom_${System.currentTimeMillis()}",
                                    nome = customName.trim(),
                                    strain_custo = strainVal,
                                    efeito = customEffect.trim()
                                )
                                state.ciberneticosInstalados.add(customItem)
                                customName = ""
                                customEffect = ""
                                customStrainText = "1"
                                onUserFeedback()
                            }
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Adicionar Customizado")
                    }
                }
            }
        }

        // Implantes Instalados
        item {
            SectionHeader(centerText = "Implantes Instalados (${state.ciberneticosInstalados.size})")
        }

        if (state.ciberneticosInstalados.isEmpty()) {
            item {
                Text(
                    text = "Nenhum implante cibernético instalado.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(8.dp)
                )
            }
        } else {
            items(state.ciberneticosInstalados, key = { it.id }) { installed ->
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = installed.nome,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Tensão: ${installed.strain_custo} | ${installed.efeito}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = {
                                state.ciberneticosInstalados.removeIf { it.id == installed.id }
                                onUserFeedback()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Remover",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}
