package com.example.swadebuilder.ui.sections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.model.CriadorViewModel
import com.example.swadebuilder.model.CrystalHeart
import com.example.swadebuilder.ui.components.CollapsibleSection
import com.example.swadebuilder.ui.components.SectionCard
import java.util.UUID

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CrystalHeartSection(
    state: CriadorState,
    viewModel: CriadorViewModel,
) {
    SectionCard(
        title = "Coração de Cristal",
        icon = Icons.Default.Favorite,
        showHeader = false
    ) {
        val context = LocalContext.current
        val temAgenteSyn = state.vantagensSelecionadas.any { it.id == "aa_agente_syn" }
        val stageOrder = listOf("Novato", "Experiente", "Veterano", "Heroico", "Lendário")
        val maxNameLength = 60
        val maxTextLength = 500
        val maxPowersLength = 1000

        Column(
            modifier = Modifier
                .padding(8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (!temAgenteSyn) {
                Text(
                    text = "Você precisa da vantagem 'Antecedente Arcano: Canalizar Cristal' para equipar um Coração de Cristal.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                val selectedHeart = state.coracaoCrystalSelecionado
                val placeholderHeart = state.listaCoracoesCrystal.firstOrNull { it.placeholder }
                val customHearts = state.listaCoracoesCrystal.filter { it.custom }
                val standardHearts = state.listaCoracoesCrystal.filter { !it.custom && !it.placeholder }
                var previewHeart by remember { mutableStateOf<CrystalHeart?>(null) }
                var editHeart by remember { mutableStateOf<CrystalHeart?>(null) }

                if (selectedHeart != null) {
                    Text(
                        text = "Coração Equipado:",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(selectedHeart.nome, fontWeight = FontWeight.Bold)
                                if (selectedHeart.custom) {
                                    Spacer(Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Coração personalizado",
                                        tint = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                            }
                            Text("Estágio: ${selectedHeart.estagio}")
                            if (selectedHeart.habilidadePassiva != null) {
                                Text("Passiva: ${selectedHeart.habilidadePassiva}")
                            }
                            if (selectedHeart.complicacaoInerente != null) {
                                Text("Complicação: ${selectedHeart.complicacaoInerente}", color = MaterialTheme.colorScheme.error)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text("Poderes: ${selectedHeart.poderes.joinToString(", ")}")
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                placeholderHeart?.let { starter ->
                    Text(
                        text = "Coração Inicial (Personalizável):",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    CrystalHeartItem(starter) {
                        editHeart = starter
                    }
                    Text(
                        text = "Toque para preencher manualmente e salvar como coração personalizado.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                }

                if (customHearts.isNotEmpty()) {
                    Text(
                        text = "Corações Personalizados:",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    customHearts.forEach { heart ->
                        CrystalHeartItem(heart) {
                            previewHeart = heart
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                Text(
                    text = "Corações Disponíveis:",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                val expandedStages = remember { mutableStateMapOf<String, Boolean>() }
                val heartsByStage = standardHearts
                    .groupBy { it.estagio.ifBlank { "Outros" } }
                    .toSortedMap(compareBy<String> { stageOrder.indexOf(it).takeIf { idx -> idx >= 0 } ?: Int.MAX_VALUE }
                        .thenBy { it })

                Column {
                    heartsByStage.forEach { (stage, hearts) ->
                        val expanded = expandedStages[stage] ?: false
                        CollapsibleSection(
                            title = stage,
                            expanded = expanded,
                            onToggle = { expandedStages[stage] = !expanded }
                        ) {
                            hearts.forEach { heart ->
                                CrystalHeartItem(heart) {
                                    previewHeart = heart
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }

                if (previewHeart != null) {
                    Dialog(onDismissRequest = { previewHeart = null }) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = previewHeart!!.nome,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (previewHeart!!.custom) {
                                        Spacer(Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "Coração personalizado",
                                            tint = MaterialTheme.colorScheme.tertiary
                                        )
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                Text("Estágio: ${previewHeart!!.estagio}")
                                Spacer(Modifier.height(8.dp))
                                if (previewHeart!!.habilidadePassiva != null) {
                                    Text("Passiva: ${previewHeart!!.habilidadePassiva}")
                                }
                                if (previewHeart!!.complicacaoInerente != null) {
                                    Text("Complicação: ${previewHeart!!.complicacaoInerente}", color = MaterialTheme.colorScheme.error)
                                }
                                Spacer(Modifier.height(8.dp))
                                Text("Poderes: ${previewHeart!!.poderes.joinToString(", ")}")

                                if (previewHeart!!.descricao != null) {
                                    Spacer(Modifier.height(8.dp))
                                    Text("Descrição: ${previewHeart!!.descricao}", style = MaterialTheme.typography.bodySmall)
                                }

                                Spacer(Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    if (previewHeart!!.custom) {
                                        TextButton(onClick = {
                                            val heartName = previewHeart!!.nome
                                            if (viewModel.removerCrystalHeartPersonalizado(context, previewHeart!!.id)) {
                                                viewModel.logFeedback("Coração $heartName removido.")
                                            }
                                            previewHeart = null
                                        }) {
                                            Text("Excluir", color = MaterialTheme.colorScheme.error)
                                        }
                                        Spacer(Modifier.width(8.dp))
                                    }
                                    TextButton(onClick = { previewHeart = null }) {
                                        Text("Cancelar", color = MaterialTheme.colorScheme.error)
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    TextButton(onClick = {
                                        viewModel.selecionarCrystalHeart(previewHeart!!)
                                        previewHeart = null
                                    }) {
                                        Text("Selecionar")
                                    }
                                }
                            }
                        }
                    }
                }

                if (editHeart != null) {
                    val starter = editHeart!!
                    var nome by remember(starter) { mutableStateOf(starter.nome) }
                    var estagio by remember(starter) { mutableStateOf(starter.estagio.ifBlank { "Novato" }) }
                    var passiva by remember(starter) { mutableStateOf(starter.habilidadePassiva.orEmpty()) }
                    var complicacao by remember(starter) { mutableStateOf(starter.complicacaoInerente.orEmpty()) }
                    var poderes by remember(starter) { mutableStateOf(starter.poderes.joinToString("\n")) }
                    var descricao by remember(starter) { mutableStateOf(starter.descricao.orEmpty()) }
                    var stageExpanded by remember { mutableStateOf(false) }
                    val keyboardController = LocalSoftwareKeyboardController.current

                    Dialog(onDismissRequest = { editHeart = null }) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = "Personalizar Coração",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = nome,
                                    onValueChange = { if (it.length <= maxNameLength) nome = it },
                                    label = { Text("Nome do Coração") },
                                    supportingText = { Text("${nome.length}/$maxNameLength • Preencha manualmente") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "Estágio",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(4.dp))
                                Box {
                                    OutlinedButton(
                                        onClick = {
                                            keyboardController?.hide()
                                            stageExpanded = true
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(estagio)
                                    }
                                    DropdownMenu(
                                        expanded = stageExpanded,
                                        onDismissRequest = { stageExpanded = false }
                                    ) {
                                        stageOrder.forEach { option ->
                                            DropdownMenuItem(
                                                text = { Text(option) },
                                                onClick = {
                                                    estagio = option
                                                    stageExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = passiva,
                                    onValueChange = { if (it.length <= maxTextLength) passiva = it },
                                    label = { Text("Passiva") },
                                    supportingText = { Text("${passiva.length}/$maxTextLength • Preencha manualmente") },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 2
                                )
                                Spacer(Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = complicacao,
                                    onValueChange = { if (it.length <= maxTextLength) complicacao = it },
                                    label = { Text("Complicação") },
                                    supportingText = { Text("${complicacao.length}/$maxTextLength • Preencha manualmente") },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 2
                                )
                                Spacer(Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = poderes,
                                    onValueChange = { if (it.length <= maxPowersLength) poderes = it },
                                    label = { Text("Poderes (um por linha)") },
                                    supportingText = { Text("${poderes.length}/$maxPowersLength • Preencha manualmente") },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 4
                                )
                                Spacer(Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = descricao,
                                    onValueChange = { if (it.length <= maxTextLength) descricao = it },
                                    label = { Text("Descrição") },
                                    supportingText = { Text("${descricao.length}/$maxTextLength • Preencha manualmente") },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 3
                                )
                                Spacer(Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(onClick = { editHeart = null }) {
                                        Text("Cancelar", color = MaterialTheme.colorScheme.error)
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    TextButton(onClick = {
                                        val poderesList = poderes.lines().map { it.trim() }.filter { it.isNotBlank() }
                                        val customHeart = CrystalHeart(
                                            id = "custom_${UUID.randomUUID()}",
                                            nome = nome,
                                            estagio = estagio,
                                            habilidadePassiva = passiva.ifBlank { null },
                                            poderes = poderesList,
                                            complicacaoInerente = complicacao.ifBlank { null },
                                            origem = "CRYSTAL_HEART",
                                            descricao = descricao.ifBlank { null },
                                            custom = true
                                        )
                                        val saved = viewModel.salvarCrystalHeartPersonalizado(context, customHeart)
                                        if (saved != null) {
                                            viewModel.selecionarCrystalHeart(saved)
                                            viewModel.logFeedback("Coração ${saved.nome} criado e salvo em ${saved.estagio}.")
                                        }
                                        editHeart = null
                                    }) {
                                        Text("Selecionar")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CrystalHeartItem(heart: CrystalHeart, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = heart.nome, fontWeight = FontWeight.Bold)
                    if (heart.custom) {
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Coração personalizado",
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
                Text(text = "Estágio: ${heart.estagio}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
