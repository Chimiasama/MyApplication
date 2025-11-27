package com.example.swadebuilder.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AssistChip
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.listaAtributos
import com.example.swadebuilder.listaPericias
import com.example.swadebuilder.listaVantagens
import com.example.swadebuilder.model.CriadorViewModel
import com.example.swadebuilder.model.PowerEffect
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.util.keyify
import kotlinx.coroutines.launch

/**
 * Diálogo de Supers (separado do ProgressosDialog).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupersDialog(
    state: CriadorState,
    viewModel: CriadorViewModel,
    onConfirmLock: () -> Unit,
    onDismiss: () -> Unit
) {
    // <- AGORA: só permite editar supers se:
    //    1) a fase de supers já foi iniciada
    //    2) ainda não está em Progressos
    //    3) a ficha básica está completa (creationComplete)
    val supersEditaveis =
        state.faseSupersAtiva &&
                !state.emProgresso &&
                state.creationComplete()

    val snackHost = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    fun showSnack(msg: String) = scope.launch { snackHost.showSnackbar(message = msg) }

    val temOMelhorQueHa: Boolean = remember(state.vantagensSelecionadas) {
        state.vantagensSelecionadas.any { it.nome.keyify() == "O MELHOR QUE HÁ".keyify() }
    }

    val candidatosFav: List<Pair<String, String>> = remember {
        buildList {
            add("sp_armor" to "Armadura (super)")
            add("sp_res"   to "Resistência (super)")
            add("sp_aparar" to "Aparar (super)")

            // NOVO: Bônus de Perícia pode ser favorecido
            add("sp_bonus_pericia" to "Bônus de Perícia")

            add("sp_superatributo" to "Superatributo")
            add("sp_superpericia" to "Superperícia")
            add("sp_supervantagem" to "Supervantagem")

        }
    }

    var favExpanded by rememberSaveable { mutableStateOf(false) }
    var localFavId  by rememberSaveable { mutableStateOf(state.poderFavoritoId) }
    val precisaDefinirFav = temOMelhorQueHa && localFavId.isNullOrEmpty()

    fun badgeText(poderId: String): String {
        val gasto = state.gastosPorPoder[poderId] ?: 0
        val limite = viewModel.perPowerLimit(poderId)
        return "$gasto/$limite"
    }

    val custoStepAtributo = 2
    val custoStepPericia  = 1
    val custoFlatPequeno  = 1
    val custoVant         = 2

    val gastosArmor = state.gastosPorPoder["sp_armor"] ?: 0
    val gastosRes   = state.gastosPorPoder["sp_res"]   ?: 0
    val shareLimit  = state.superLimitePorPoder
    val shareUsed   = gastosArmor + gastosRes
    val shareRest   = (shareLimit - shareUsed).coerceAtLeast(0)

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 360.dp, max = 560.dp)
                .padding(8.dp)
        ) {
            Column(Modifier.fillMaxWidth().padding(12.dp)) {
                Text("Superpoderes", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                SnackbarHost(hostState = snackHost)

                // --- topo fixo (acima da “linha vermelha”)
                Spacer(Modifier.height(8.dp))
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Pontos de Super: ${state.superPontosDisponiveis}/${state.superPontosTotais}")
                        Spacer(Modifier.height(6.dp))
                        Text("Limite por poder (padrão): ${state.limitePorPoderPadrao}")
                        Text("Limite favorecido: ${state.limiteFavorecido}")
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Limite compartilhado Armadura+Resistência (SP gastos): $shareUsed / $shareLimit"
                        )
                    }
                }

                // >>> mensagem de bloqueio enquanto a ficha inicial não estiver completa
                if (!state.creationComplete()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Antes de investir em superpoderes, termine de distribuir os pontos iniciais " +
                                "(Atributos, Perícias, Vantagens e Complicações).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (temOMelhorQueHa) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Poder favorecido (O MELHOR QUE HÁ) — obrigatório",
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(6.dp))
                    ExposedDropdownMenuBox(
                        expanded = favExpanded,
                        onExpandedChange = { favExpanded = !favExpanded }
                    ) {
                        val label = candidatosFav.firstOrNull { it.first == localFavId }?.second
                            ?: "Selecione o poder favorecido…"
                        OutlinedTextField(
                            value = label,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = favExpanded)
                            },
                            modifier = Modifier
                                .menuAnchor(
                                    ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                                    enabled = true
                                )
                                .fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = favExpanded,
                            onDismissRequest = { favExpanded = false }
                        ) {
                            candidatosFav.forEach { (id, nome) ->
                                DropdownMenuItem(
                                    text = { Text(nome) },
                                    onClick = {
                                        // atualiza o rascunho local (pro label do campo)
                                        localFavId = id
                                        // usa a API do ViewModel pra registrar o favorito de verdade
                                        viewModel.definirPoderFavorecido(id)
                                        favExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    if (precisaDefinirFav && supersEditaveis) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Defina o poder favorecido para liberar gastos.",
                            color = Color(0xFFB00020)
                        )
                    }
                }

                // --- “linha vermelha”
                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))

                // --- ÁREA ROLÁVEL (lista de poderes) ---
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    // Mitigação
                    item {
                        Text("Mitigação (super)", fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(6.dp))
                        PoderRowPlusMinus(
                            titulo = "Armadura (super)",
                            badge = badgeText("sp_armor"),
                            minusEnabled = supersEditaveis &&
                                    (state.gastosPorPoder["sp_armor"] ?: 0) > 0 &&
                                    !precisaDefinirFav,
                            plusEnabled  = supersEditaveis &&
                                    !precisaDefinirFav &&
                                    shareRest > 0 &&
                                    (state.gastosPorPoder["sp_armor"] ?: 0) < viewModel.perPowerLimit("sp_armor") &&
                                    state.superPontosDisponiveis >= custoFlatPequeno,
                            onMinus = {
                                val investment = state.superInvestments.lastOrNull { it.powerId == "sp_armor" }
                                if (investment != null) {
                                    val r = viewModel.desfazerInvestimentoSuper(investment)
                                    if (!r.ok) showSnack(r.mensagem)
                                }
                            },
                            onPlus  = {
                                val investment = com.example.swadebuilder.model.SuperInvestment(
                                    powerId = "sp_armor",
                                    displayName = "Armadura",
                                    cost = custoFlatPequeno,
                                    effect = PowerEffect.BonusArmadura(1)
                                )
                                val r = viewModel.tentarInvestirSuper(investment)
                                if (!r.ok) showSnack(r.mensagem)
                            }
                        )
                        PoderRowPlusMinus(
                            titulo = "Resistência (super)",
                            badge = badgeText("sp_res"),
                            minusEnabled = supersEditaveis &&
                                    (state.gastosPorPoder["sp_res"] ?: 0) > 0 &&
                                    !precisaDefinirFav,
                            plusEnabled  = supersEditaveis &&
                                    !precisaDefinirFav &&
                                    shareRest > 0 &&
                                    (state.gastosPorPoder["sp_res"] ?: 0) < viewModel.perPowerLimit("sp_res") &&
                                    state.superPontosDisponiveis >= custoFlatPequeno,
                            onMinus = {
                                val investment = state.superInvestments.lastOrNull { it.powerId == "sp_res" }
                                if (investment != null) {
                                    val r = viewModel.desfazerInvestimentoSuper(investment)
                                    if (!r.ok) showSnack(r.mensagem)
                                }
                            },
                            onPlus  = {
                                val investment = com.example.swadebuilder.model.SuperInvestment(
                                    powerId = "sp_res",
                                    displayName = "Resistência",
                                    cost = custoFlatPequeno,
                                    effect = PowerEffect.BonusResistencia(1)
                                )
                                val r = viewModel.tentarInvestirSuper(investment)
                                if (!r.ok) showSnack(r.mensagem)
                            }
                        )
                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(8.dp))
                    }

                    // Aparar
                    item {
                        Text("Aparar", fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(6.dp))
                        PoderRowPlusMinus(
                            titulo = "Aparar (super)",
                            badge = badgeText("sp_aparar"),
                            minusEnabled = supersEditaveis &&
                                    (state.gastosPorPoder["sp_aparar"] ?: 0) > 0 &&
                                    !precisaDefinirFav,
                            plusEnabled  = supersEditaveis &&
                                    !precisaDefinirFav &&
                                    state.superPontosDisponiveis >= custoFlatPequeno,
                            onMinus = {
                                val investment = state.superInvestments.lastOrNull { it.powerId == "sp_aparar" }
                                if (investment != null) {
                                    val r = viewModel.desfazerInvestimentoSuper(investment)
                                    if (!r.ok) showSnack(r.mensagem)
                                }
                            },
                            onPlus  = {
                                val investment = com.example.swadebuilder.model.SuperInvestment(
                                    powerId = "sp_aparar",
                                    displayName = "Aparar",
                                    cost = custoFlatPequeno,
                                    effect = PowerEffect.BonusAparar(1)
                                )
                                val r = viewModel.tentarInvestirSuper(investment)
                                if (!r.ok) showSnack(r.mensagem)
                            }
                        )
                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(8.dp))
                    }

                    // Superatributo (2:1)
                    item {
                        Text("Superatributo", fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(6.dp))
                    }
                    items(listaAtributos) { attr ->
                        val poderId = "sp_superatributo"

                        PoderRowPlusMinus(
                            titulo = attr,
                            badge = badgeText(poderId),
                            minusEnabled = supersEditaveis &&
                                    (state.gastosPorPoder[poderId] ?: 0) > 0 &&
                                    !precisaDefinirFav,
                            plusEnabled  = supersEditaveis &&
                                    !precisaDefinirFav &&
                                    state.superPontosDisponiveis >= custoStepAtributo,
                            onMinus = {
                                val investment = state.superInvestments.lastOrNull {
                                    it.powerId == poderId && (it.effect as? PowerEffect.SuperAtributo)?.attrKey == attr.uppercase()
                                }
                                if (investment != null) {
                                    val r = viewModel.desfazerInvestimentoSuper(investment)
                                    if (!r.ok) showSnack(r.mensagem)
                                }
                            },
                            onPlus  = {
                                val investment = com.example.swadebuilder.model.SuperInvestment(
                                    powerId = poderId,
                                    displayName = "Superatributo: $attr",
                                    cost = custoStepAtributo,
                                    effect = PowerEffect.SuperAtributo(attr.uppercase(), 1)
                                )
                                val r = viewModel.tentarInvestirSuper(investment)
                                if (!r.ok) showSnack(r.mensagem)
                            }
                        )
                    }

                    item {
                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(8.dp))
                    }

                    // Superperícia (1:1)
                    item {
                        Text("Superperícia", fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(6.dp))
                    }
                    items(listaPericias) { per ->
                        val poderId = "sp_superpericia"

                        PoderRowPlusMinus(
                            titulo = per.nome,
                            badge = badgeText(poderId),
                            minusEnabled = supersEditaveis &&
                                    (state.gastosPorPoder[poderId] ?: 0) > 0 &&
                                    !precisaDefinirFav,
                            plusEnabled  = supersEditaveis &&
                                    !precisaDefinirFav &&
                                    state.superPontosDisponiveis >= custoStepPericia,
                            onMinus = {
                                val investment = state.superInvestments.lastOrNull {
                                    it.powerId == poderId && (it.effect as? PowerEffect.SuperPericia)?.periciaKey == per.nome.keyify()
                                }
                                if (investment != null) {
                                    val r = viewModel.desfazerInvestimentoSuper(investment)
                                    if (!r.ok) showSnack(r.mensagem)
                                }
                            },
                            onPlus  = {
                                val investment = com.example.swadebuilder.model.SuperInvestment(
                                    powerId = poderId,
                                    displayName = "Superperícia: ${per.nome}",
                                    cost = custoStepPericia,
                                    effect = PowerEffect.SuperPericia(per.nome.keyify(), 1)
                                )
                                val r = viewModel.tentarInvestirSuper(investment)
                                if (!r.ok) showSnack(r.mensagem)
                            }
                        )
                    }

                    item {
                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(8.dp))
                    }

                    // Supervantagem
                    item {
                        Text("Supervantagem", fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(6.dp))

                        var expVant by rememberSaveable { mutableStateOf(false) }
                        var selectedVant by rememberSaveable { mutableStateOf<Vantagem?>(null) }

                        ExposedDropdownMenuBox(
                            expanded = expVant,
                            onExpandedChange = { expVant = !expVant }
                        ) {
                            OutlinedTextField(
                                value = selectedVant?.nome ?: "Escolha uma vantagem para comprar via SUPER…",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expVant) },
                                modifier = Modifier
                                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                                    .fillMaxWidth()
                            )
                            DropdownMenu(
                                expanded = expVant,
                                onDismissRequest = { expVant = false }
                            ) {
                                listaVantagens.forEach { v ->
                                    DropdownMenuItem(
                                        text = { Text(v.nome) },
                                        onClick = { selectedVant = v; expVant = false }
                                    )
                                }
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                        ) {
                            Button(
                                enabled = supersEditaveis &&
                                        selectedVant != null &&
                                        !precisaDefinirFav &&
                                        state.superPontosDisponiveis >= custoVant,
                                onClick = {
                                    val v = selectedVant ?: return@Button
                                    val investment = com.example.swadebuilder.model.SuperInvestment(
                                        powerId = "sp_supervantagem",
                                        displayName = "Supervantagem: ${v.nome}",
                                        cost = custoVant,
                                        effect = PowerEffect.SuperVantagem(v.id)
                                    )
                                    val r = viewModel.tentarInvestirSuper(investment)
                                    if (!r.ok) showSnack(r.mensagem)
                                }
                            ) { Text("Comprar (2)") }

                            Button(
                                enabled = supersEditaveis &&
                                        selectedVant != null &&
                                        (state.gastosPorPoder["sp_supervantagem"] ?: 0) > 0 &&
                                        !precisaDefinirFav,
                                onClick = {
                                    val v = selectedVant ?: return@Button
                                    val investment = state.superInvestments.lastOrNull {
                                        it.powerId == "sp_supervantagem" && (it.effect as? PowerEffect.SuperVantagem)?.vantagemId == v.id
                                    }
                                    if (investment != null) {
                                        val r = viewModel.desfazerInvestimentoSuper(investment)
                                        if (!r.ok) showSnack(r.mensagem)
                                    }
                                }
                            ) { Text("Remover") }
                        }
                        Text(
                            "Superatributo custa 2 por ‘step’ (+2 até d12; acima de d12 = +1 por step).",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                // rodapé
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = { onConfirmLock(); onDismiss() }) {
                        Text("Confirmar")
                    }
                }
            }
        }
    }
}


@Composable
private fun PoderRowPlusMinus(
    titulo: String,
    badge: String,
    minusEnabled: Boolean,
    plusEnabled: Boolean,
    onMinus: () -> Unit,
    onPlus: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(titulo, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.weight(1f))
        AssistChip(onClick = {}, enabled = false, label = { Text(badge) })
        IconButton(enabled = minusEnabled, onClick = onMinus) {
            Icon(Icons.Default.Remove, contentDescription = "Diminuir")
        }
        IconButton(enabled = plusEnabled, onClick = onPlus) {
            Icon(Icons.Default.Add, contentDescription = "Aumentar")
        }
    }
}