package com.example.swadebuilder.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.model.EquipamentoItem
import kotlinx.serialization.json.JsonPrimitive

/**
 * Estado do formulário de "Equipamento" de Conteúdo Customizado (ver SettingsDialog.kt,
 * seletor de categoria). Extraído do diálogo mega-arquivo pra cá porque essa categoria já
 * tinha crescido demais (Tipo/Subtipo, dano estruturado, Força Mínima, estatísticas de
 * arma/armadura/escudo/veículo) pra continuar inline junto com as outras 8 categorias.
 */
class EquipamentoFormState {
    var superType by mutableStateOf("Arma")
    var subtype by mutableStateOf("Corpo a Corpo")
    var cost by mutableStateOf("0")
    var weight by mutableStateOf("0")
    // "Efeito" livre, só usado por Geral/Munição — armas usam o construtor de dano abaixo.
    var effectText by mutableStateOf("")

    // Construtor de dano: em vez de texto livre (onde "3d7" passava batido — esse dado não
    // existe em SWADE), o dado só vem dos chips de dado válido. "Baseado em Força" monta
    // "For+dX" (padrão de corpo a corpo/arremesso); "Dado fixo" monta "NdX" (fogo/energia).
    var danoBaseadoEmForca by mutableStateOf(true)
    var danoDado by mutableStateOf("d6")
    var danoQtd by mutableStateOf("2")
    var danoBonus by mutableStateOf("")

    var pa by mutableStateOf("")
    var alcance by mutableStateOf("")
    var tiros by mutableStateOf("")
    var cdt by mutableStateOf("")
    // "-" = sem Força Mínima cadastrada; qualquer outro valor vem sempre de um dos 5 dados
    // válidos de SWADE (ver ForcaMinimaChipPicker).
    var forcaMin by mutableStateOf("-")
    var aparar by mutableStateOf("")
    var armadura by mutableStateOf("")
    var tamanho by mutableStateOf("")
    var manobrabilidade by mutableStateOf("")
    var velMaxima by mutableStateOf("")
    var resistencia by mutableStateOf("")
    var tripulacao by mutableStateOf("")
    var explosao by mutableStateOf("")
    var cobertura by mutableStateOf("")
    // Só relevante pro subtipo "Distância": machado/adaga/lança de arremesso servem corpo a
    // corpo E à distância a partir de uma única compra; arco/funda/estilingue não. O
    // jogador decide explicitamente em vez do app adivinhar pelo nome (ver
    // EquipamentoItem.usavelCorpoACorpo).
    var usavelCorpoACorpo by mutableStateOf(false)
    // Era/estilo (Medieval/Moderna/Futurista) — tag opcional e multi-selecionável guardada
    // em `subsubtipo`. O próprio catálogo oficial já combina eras num item só quando faz
    // sentido (ex.: subtipo "Modernas e Futuristas"), por isso não é single-choice.
    var eras by mutableStateOf(setOf<String>())

    // Categoria Customizada (ver model/CategoriaCustomizada.kt) escolhida pelo Mestre —
    // quando presente, tem prioridade sobre `categoriaTipo()` calculado a partir de
    // superType/subtype pra decidir em qual seção da tela de Equipamento o item cai
    // (ver model/DataLoader updateActiveModules). Os campos mecânicos (dano, armadura
    // etc.) continuam vindo de superType/subtype normalmente — a categoria
    // customizada só substitui o agrupamento visual, não a mecânica do item.
    var categoriaCustomizadaId by mutableStateOf<String?>(null)

    fun montarDano(): String {
        val bonus = danoBonus.toIntOrNull()?.takeIf { it != 0 }
        val sufixoBonus = when {
            bonus == null -> ""
            bonus > 0 -> "+$bonus"
            else -> "$bonus"
        }
        return if (danoBaseadoEmForca) {
            "For+$danoDado$sufixoBonus"
        } else {
            val qtd = danoQtd.toIntOrNull()?.coerceAtLeast(1) ?: 1
            val base = if (qtd <= 1) danoDado else "$qtd$danoDado"
            "$base$sufixoBonus"
        }
    }

    /**
     * "tipo" de EquipamentoCategoria a gravar no item — decide em qual seção da tela de
     * Equipamento ele aparece (ver updateActiveModules em DataLoader e
     * EquipamentoSection.mapCategory) em vez de cair sempre em "Equipamento Geral".
     */
    fun categoriaTipo(): String = when (superType) {
        "Arma" -> when (subtype) {
            "Distância" -> "Armas à Distância"
            "Fogo" -> "Armas de Fogo"
            "Energia" -> "Armas de Energia"
            else -> "Armas Corpo a Corpo"
        }
        "Armadura" -> "Armaduras"
        "Escudo" -> "Escudos"
        "Munição" -> "Munição"
        "Veículo" -> "Veículos"
        else -> "Equipamento Geral"
    }

    fun build(nome: String, descricao: String, origem: String, id: String): EquipamentoItem {
        fun textoOuNulo(v: String) = v.takeIf { it.isNotBlank() }?.let { JsonPrimitive(it) }
        val isArma = superType == "Arma"
        // "Efeito" (Geral/Munição) não é dano de arma — vai pra observação em vez do campo
        // `dano`, senão EquipamentoFormatters.toResumo() mostraria "Dano: <texto>" num item
        // que não é arma.
        val efeito = if (!isArma) effectText else ""
        val observacoesTexto = if (efeito.isNotBlank()) {
            if (descricao == "-") efeito else "$descricao\n$efeito"
        } else descricao
        val forcaMinValor = forcaMin.takeIf { it != "-" }?.let { JsonPrimitive(it) }
        // Só o subtipo "Distância" pergunta explicitamente (checkbox); fogo/energia nunca
        // servem corpo a corpo; corpo a corpo já cai como melee só por não ter `distancia`,
        // sem precisar do flag (ver ResumoSection.kt).
        val usavelCorpoACorpoValor: Boolean? = when {
            !isArma -> null
            subtype == "Distância" -> usavelCorpoACorpo
            subtype == "Fogo" || subtype == "Energia" -> false
            else -> null
        }
        return EquipamentoItem(
            nome = nome,
            custo = JsonPrimitive(cost.toIntOrNull() ?: 0),
            peso = JsonPrimitive(weight.toFloatOrNull() ?: 0f),
            dano = if (isArma) JsonPrimitive(montarDano()) else null,
            pa = textoOuNulo(pa),
            distancia = textoOuNulo(alcance),
            tiros = textoOuNulo(tiros),
            cdt = textoOuNulo(cdt),
            forcaMin = forcaMinValor,
            aparar = textoOuNulo(aparar),
            armadura = textoOuNulo(armadura),
            tamanho = textoOuNulo(tamanho),
            manobrabilidade = textoOuNulo(manobrabilidade),
            velMaxima = textoOuNulo(velMaxima),
            resistencia = textoOuNulo(resistencia),
            tripulacao = textoOuNulo(tripulacao),
            explosao = textoOuNulo(explosao),
            cobertura = textoOuNulo(cobertura),
            observacoes = JsonPrimitive(observacoesTexto),
            origem = origem,
            subtipo = subtype,
            subsubtipo = eras.takeIf { it.isNotEmpty() }?.sorted()?.joinToString(", "),
            categoriaTipo = categoriaTipo(),
            categoriaCustomizadaId = categoriaCustomizadaId,
            usavelCorpoACorpo = usavelCorpoACorpoValor,
            id = id
        )
    }

    fun reset() {
        superType = "Arma"; subtype = "Corpo a Corpo"
        cost = "0"; weight = "0"; effectText = ""
        danoBaseadoEmForca = true; danoDado = "d6"; danoQtd = "2"; danoBonus = ""
        pa = ""; alcance = ""; tiros = ""; cdt = ""
        forcaMin = "-"; aparar = ""; armadura = ""
        tamanho = ""; manobrabilidade = ""; velMaxima = ""; resistencia = ""; tripulacao = ""
        explosao = ""; cobertura = ""
        usavelCorpoACorpo = false
        eras = emptySet()
        categoriaCustomizadaId = null
    }
}

@Composable
fun rememberEquipamentoFormState(): EquipamentoFormState = remember { EquipamentoFormState() }

// Força Mínima (d4 a d12 mais "-") como Chip Row: 6 opções é demais pra um Segmented
// Control de linha única continuar legível.
@Composable
private fun ForcaMinimaChipPicker(value: String, onValueChange: (String) -> Unit) {
    ChipRow("Força Mínima:") {
        listOf("-", "d4", "d6", "d8", "d10", "d12").forEach { dado ->
            FilterChip(
                selected = value == dado,
                onClick = { onValueChange(dado) },
                label = { Text(dado, style = MaterialTheme.typography.labelSmall) }
            )
        }
    }
}

// Dropdown com checkbox (em vez de mais uma fileira de chips sempre visível) pra tags
// opcionais e combináveis — o catálogo oficial já mistura eras num item só quando faz
// sentido (ex.: subtipo "Modernas e Futuristas"), por isso é multi-seleção.
@Composable
private fun EraDropdownPicker(selecionadas: Set<String>, onChange: (Set<String>) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val opcoes = listOf("Medieval", "Moderna", "Futurista")
    Column {
        Text("Era/Estilo (opcional):", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(4.dp))
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(
                if (selecionadas.isEmpty()) "Selecionar..." else selecionadas.sorted().joinToString(", "),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium
            )
            Text("▾", style = MaterialTheme.typography.bodyMedium)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            opcoes.forEach { opcao ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = opcao in selecionadas, onCheckedChange = null)
                            Spacer(Modifier.width(8.dp))
                            Text(opcao)
                        }
                    },
                    onClick = {
                        onChange(if (opcao in selecionadas) selecionadas - opcao else selecionadas + opcao)
                    }
                )
            }
        }
    }
}

@Composable
fun EquipamentoCreatorFields(
    state: EquipamentoFormState,
    categoriasCustomizadas: List<com.example.swadebuilder.model.CategoriaCustomizada> = emptyList(),
    onCreateCategoria: (String) -> com.example.swadebuilder.model.CategoriaCustomizada = { com.example.swadebuilder.model.CategoriaCustomizada(id = "", nome = it, tipoEntidade = com.example.swadebuilder.model.TipoEntidadeCategoria.EQUIPAMENTO) },
    onRenameCategoria: (String, String) -> Unit = { _, _ -> },
    onDeleteCategoria: (String) -> Unit = {}
) {
    // Tipo escolhido aqui decide (a) quais campos mecânicos aparecem abaixo e (b) o
    // `categoriaTipo` gravado no item — é o que faz o item cair na seção certa
    // (Armas/Armaduras/Veículos/etc.) da tela de Equipamento em vez de sempre em
    // "Equipamento Geral" (ver updateActiveModules em DataLoader e EquipamentoSection.mapCategory).
    ChipRow("Tipo de Item:") {
        listOf("Arma", "Armadura", "Escudo", "Munição", "Veículo", "Geral").forEach { st ->
            FilterChip(
                selected = state.superType == st,
                onClick = {
                    state.superType = st
                    state.subtype = when (st) {
                        "Arma" -> "Corpo a Corpo"
                        "Armadura" -> "Armadura Corporal"
                        "Escudo" -> "Escudo"
                        "Munição" -> "Munição"
                        "Veículo" -> "Veículo"
                        else -> "Equipamento Geral"
                    }
                    // Cada tipo cuida só das próprias tags mecânicas — troca de tipo limpa
                    // os campos do tipo anterior pra não salvar, por exemplo,
                    // Manobrabilidade de veículo numa arma.
                    state.pa = ""; state.alcance = ""; state.tiros = ""; state.cdt = ""
                    state.forcaMin = "-"; state.aparar = ""; state.armadura = ""
                    state.tamanho = ""; state.manobrabilidade = ""
                    state.velMaxima = ""; state.resistencia = ""; state.tripulacao = ""
                    state.explosao = ""; state.cobertura = ""
                    state.effectText = ""
                    state.danoBaseadoEmForca = true
                    state.danoDado = "d6"; state.danoQtd = "2"; state.danoBonus = ""
                    state.usavelCorpoACorpo = false
                    state.eras = emptySet()
                },
                label = { Text(st, style = MaterialTheme.typography.labelSmall) }
            )
        }
    }
    if (state.superType == "Arma") {
        ChipRow("Subtipo de Arma:") {
            listOf("Corpo a Corpo", "Distância", "Fogo", "Energia").forEach { sub ->
                FilterChip(
                    selected = state.subtype == sub,
                    onClick = {
                        state.subtype = sub
                        // Corpo a corpo/arremesso é sempre For+dado no SWADE; fogo/energia
                        // é dado fixo por padrão (dá pra trocar).
                        state.danoBaseadoEmForca = sub == "Corpo a Corpo" || sub == "Distância"
                        // O checkbox de "também corpo a corpo" só existe pro subtipo
                        // Distância — sair dele descarta a marcação.
                        if (sub != "Distância") state.usavelCorpoACorpo = false
                    },
                    label = { Text(sub, style = MaterialTheme.typography.labelSmall) }
                )
            }
        }
    }

    CategorySelector(
        label = "Categoria customizada (opcional):",
        officialOptions = emptyList(),
        selectedOfficial = null,
        customCategorias = categoriasCustomizadas,
        selectedCustomId = state.categoriaCustomizadaId,
        onSelectOfficial = {},
        onSelectCustomId = { state.categoriaCustomizadaId = it },
        onCreateCustom = onCreateCategoria,
        onRenameCustom = onRenameCategoria,
        onDeleteCustom = onDeleteCategoria
    )

    // Custo e Peso valem pra qualquer tipo de item (livro básico, Cap. 2).
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = state.cost,
            onValueChange = { state.cost = it },
            label = { Text("Custo ($)") },
            singleLine = true,
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = state.weight,
            onValueChange = { state.weight = it },
            label = { Text("Peso (kg)") },
            singleLine = true,
            modifier = Modifier.weight(1f)
        )
    }

    if (state.superType != "Geral" && state.superType != "Munição") {
        EraDropdownPicker(selecionadas = state.eras, onChange = { state.eras = it })
    }

    val isArma = state.superType == "Arma"
    val isArmaADistancia = isArma && state.subtype in listOf("Distância", "Fogo", "Energia")
    val isArmaCorpoACorpo = isArma && state.subtype == "Corpo a Corpo"

    if (isArma) {
        SegmentedControl(
            label = "Dano baseado em:",
            options = listOf("Força + dado", "Dado fixo"),
            selectedIndex = if (state.danoBaseadoEmForca) 0 else 1,
            onSelect = { state.danoBaseadoEmForca = it == 0 }
        )
        if (!state.danoBaseadoEmForca) {
            OutlinedTextField(
                value = state.danoQtd,
                onValueChange = { state.danoQtd = it },
                label = { Text("Quantidade de dados (ex: 2)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }
        run {
            val dice = listOf("d4", "d6", "d8", "d10", "d12")
            SegmentedControl(
                label = if (state.danoBaseadoEmForca) "Dado (For+):" else "Dado:",
                options = dice,
                selectedIndex = dice.indexOf(state.danoDado).coerceAtLeast(0),
                onSelect = { state.danoDado = dice[it] }
            )
        }
        OutlinedTextField(
            value = state.danoBonus,
            onValueChange = { state.danoBonus = it },
            label = { Text("Bônus fixo (opcional, ex: 2 ou -1)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            "Dano final: ${state.montarDano()}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = state.pa,
                onValueChange = { state.pa = it },
                label = { Text("PA (Perf. Armadura)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }
        ForcaMinimaChipPicker(value = state.forcaMin, onValueChange = { state.forcaMin = it })
    }
    if (isArmaCorpoACorpo) {
        OutlinedTextField(
            value = state.aparar,
            onValueChange = { state.aparar = it },
            label = { Text("Bônus de Aparar (ex: +1)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
    if (isArmaADistancia) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = state.alcance,
                onValueChange = { state.alcance = it },
                label = { Text("Alcance (curto/médio/longo)") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = state.tiros,
                onValueChange = { state.tiros = it },
                label = { Text("Tiros") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }
        OutlinedTextField(
            value = state.cdt,
            onValueChange = { state.cdt = it },
            label = { Text("CdT (Cadência de Tiro)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = state.explosao,
            onValueChange = { state.explosao = it },
            label = { Text("Área de Efeito (opcional, ex: MPE/MME/MGE)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        // Só faz sentido pro subtipo "Distância" (arco, funda, faca/machado/lança de
        // arremesso) — arma de fogo/energia nunca dobra como arma corpo a corpo. Machado/
        // faca/lança de arremesso marcam isso; arco/funda/estilingue não (mesmo usando
        // "For" no dano, a Força ali é a força de puxada).
        if (state.subtype == "Distância") {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Checkbox(
                    checked = state.usavelCorpoACorpo,
                    onCheckedChange = { state.usavelCorpoACorpo = it }
                )
                Text(
                    "Também serve corpo a corpo (arma de arremesso, ex.: machado/adaga)",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }

    if (state.superType == "Armadura") {
        OutlinedTextField(
            value = state.armadura,
            onValueChange = { state.armadura = it },
            label = { Text("Bônus de Armadura (ex: +2)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        ForcaMinimaChipPicker(value = state.forcaMin, onValueChange = { state.forcaMin = it })
    }

    if (state.superType == "Escudo") {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = state.aparar,
                onValueChange = { state.aparar = it },
                label = { Text("Bônus de Aparar (ex: +1)") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = state.cobertura,
                onValueChange = { state.cobertura = it },
                label = { Text("Cobertura (ex: -2)") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }
    }

    if (state.superType == "Veículo") {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = state.tamanho,
                onValueChange = { state.tamanho = it },
                label = { Text("Tamanho") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = state.manobrabilidade,
                onValueChange = { state.manobrabilidade = it },
                label = { Text("Manobrabilidade") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = state.velMaxima,
                onValueChange = { state.velMaxima = it },
                label = { Text("Vel. Máxima") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = state.resistencia,
                onValueChange = { state.resistencia = it },
                label = { Text("Resistência") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }
        OutlinedTextField(
            value = state.tripulacao,
            onValueChange = { state.tripulacao = it },
            label = { Text("Tripulação") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (state.superType == "Geral" || state.superType == "Munição") {
        OutlinedTextField(
            value = state.effectText,
            onValueChange = { state.effectText = it },
            label = { Text("Efeito (opcional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
