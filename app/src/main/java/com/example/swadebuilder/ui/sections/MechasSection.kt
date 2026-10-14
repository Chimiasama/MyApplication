package com.example.swadebuilder.ui.sections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.sp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.EditionConfig
import com.example.swadebuilder.model.MechaCatalogWrapper
import com.example.swadebuilder.model.MechaItem
import com.example.swadebuilder.model.MechaModCatalogWrapper
import com.example.swadebuilder.model.MechaModItem
import com.example.swadebuilder.model.MechaWeaponCatalogWrapper
import com.example.swadebuilder.model.MechaWeaponItem
import com.example.swadebuilder.ui.components.MarqueeText
import com.example.swadebuilder.ui.components.SectionHeader
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream

private val mechasSectionJson = Json { ignoreUnknownKeys = true }

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
                mechasSectionJson.decodeFromStream<MechaCatalogWrapper>(input).mechas
            }
        }.getOrElse { emptyList() }
    }

    val modCatalog = remember(context) {
        runCatching {
            context.assets.open("scifi_mecha_mods.json").use { input ->
                mechasSectionJson.decodeFromStream<MechaModCatalogWrapper>(input).modificadores
            }
        }.getOrElse { emptyList() }.map { it.exibido() }
    }

    val weaponCatalog = remember(context) {
        runCatching {
            context.assets.open("scifi_mecha_weapons.json").use { input ->
                mechasSectionJson.decodeFromStream<MechaWeaponCatalogWrapper>(input).armas
            }
        }.getOrElse { emptyList() }.map { it.exibido() }
    }

    var showCreateCustomDialog by remember { mutableStateOf(false) }
    var highlightedMechaId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(highlightedMechaId) {
        if (highlightedMechaId != null) {
            delay(450)
            highlightedMechaId = null
        }
    }

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
            Text(
                text = "Os preços em \$ mostrados abaixo são só de referência/registro (tabela do livro) — o app não desconta esse valor do seu dinheiro. Montar um Mecha sem descontar o custo depende da aprovação do mestre.",
                style = MaterialTheme.typography.labelSmall,
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
                            val isHighlighted = highlightedMechaId == mecha.id
                            OutlinedButton(
                                onClick = {
                                    highlightedMechaId = mecha.id
                                    state.mechasSelecionados.add(
                                        // UUID em vez de currentTimeMillis(): dois cliques rápidos no mesmo
                                        // mecha podiam cair no mesmo milissegundo e gerar ids duplicados.
                                        mecha.copy(id = "${mecha.id}_${java.util.UUID.randomUUID()}")
                                    )
                                    onUserFeedback()
                                },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isHighlighted) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceContainerLow,
                                    contentColor = if (isHighlighted) MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.primary
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SmartToy,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 6.dp)
                                )
                                Text(mecha.nome)
                            }
                        }

                        OutlinedButton(
                            onClick = { showCreateCustomDialog = true },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 6.dp)
                            )
                            Text("Criar Mecha Personalizado")
                        }
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
                com.example.swadebuilder.ui.components.EmptyState(
                    message = "Nenhum Mecha selecionado. Escolha um modelo acima ou crie um do zero."
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
                        id = "custom_mech_${java.util.UUID.randomUUID()}",
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

                TextButton(
                    onClick = {
                        val tamanho = tamanhoText.toIntOrNull() ?: 0
                        val multiplicador = if (categoriaChassiText.trim().equals("Colossal", ignoreCase = true)) 4 else 3
                        modMaxText = (tamanho * multiplicador).coerceAtLeast(0).toString()
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Calcular MODs pelo Tamanho (Tam. × 3, ou × 4 se Colossal)")
                }
            }
        }
    )
}

// Os mechas prontos do catálogo (scifi_mechas.json) descrevem armas equipadas em texto livre
// (ex.: "2x Lança-Mísseis (6d6, PA 16, Guiado)"), com prefixos de quantidade e sufixos que não
// batem por igualdade exata com o nome canônico do catálogo de armas (ex.: "Lança-Mísseis Leve
// (6d6, PA 16)") — isso fazia o custo em MODs dessas armas nunca ser contabilizado. As estatísticas
// entre parênteses (dano/PA) são estáveis entre os dois textos, então usamos elas como assinatura:
// a entrada do catálogo casa se seu conteúdo entre parênteses for um prefixo do texto do mecha.
private val parenStatsRegex = Regex("\\(([^)]*)\\)")

private fun extractParenStats(text: String): String? =
    parenStatsRegex.find(text)?.groupValues?.getOrNull(1)?.trim()?.lowercase()

private fun matchWeaponFromCatalog(armaStr: String, catalog: List<MechaWeaponItem>): MechaWeaponItem? {
    val trimmed = armaStr.trim()
    catalog.firstOrNull { it.nome.equals(trimmed, ignoreCase = true) }?.let { return it }

    val armaStats = extractParenStats(trimmed) ?: return null
    return catalog.firstOrNull { w ->
        val catalogStats = extractParenStats(w.nome) ?: return@firstOrNull false
        catalogStats.isNotBlank() && armaStats.startsWith(catalogStats)
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
    val context = LocalContext.current
    var systemInput by remember { mutableStateOf("") }
    var showModDialog by remember { mutableStateOf(false) }
    var showWeaponCatalogDialog by remember { mutableStateOf(false) }
    var showCreateCustomWeaponDialog by remember { mutableStateOf(false) }
    var infoDialogTitle by remember { mutableStateOf<String?>(null) }
    var infoDialogContent by remember { mutableStateOf<String?>(null) }

    // Dynamic stat calculations with modifiers and equipped weapons MOD costs. Mods cujo custo
    // escala com o Tamanho ("Metade do Tam." no livro) são resolvidos contra o tamanho ATUAL do
    // chassi a cada recomposição, em vez de ficar congelado no valor de quando foram instalados.
    // Armas customizadas do próprio Mecha entram na mesma busca que o catálogo oficial, senão
    // seu Mods cost (definido na criação, ver CreateCustomMechaWeaponDialog) nunca seria contado.
    val weaponCatalogComCustomizadas = weaponCatalog + mecha.armasCustomizadas
    val modsDoModifiers = mecha.mods_instalados.sumOf { it.custoResolvido(mecha.tamanho) }
    val modsDasArmas = mecha.armas_equipadas.sumOf { armaStr ->
        matchWeaponFromCatalog(armaStr, weaponCatalogComCustomizadas)?.mods_cost ?: 0
    }
    val modsGasto = modsDoModifiers + modsDasArmas
    val modsRestantes = mecha.mod_pontos_max - modsGasto

    val extraRes = mecha.mods_instalados.count { it.id == "mod_def_resistencia" } - (2 * mecha.mods_instalados.count { it.id == "mod_neg_danificado" })
    val resistenciaCalc = mecha.resistencia_base + extraRes

    val armaduraExtraBruta = (2 * mecha.mods_instalados.count { it.id == "mod_def_armadura_extra" }) + (2 * mecha.customizacoes.blindagem_extra)
    val armaduraExtraMax = mecha.armaduraExtraMaximaResolvida()
    // armaduraExtraMax = 0 significa "categoria de chassi desconhecida" (não trava).
    val armaduraExtraLimitada = if (armaduraExtraMax > 0) armaduraExtraBruta.coerceAtMost(armaduraExtraMax) else armaduraExtraBruta
    val armaduraCalc = mecha.armadura_base + armaduraExtraLimitada
    val armaduraNoLimite = armaduraExtraMax > 0 && armaduraExtraBruta > armaduraExtraMax

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
            if (mecha.custo.isNotBlank()) {
                Text(
                    text = "Preço de referência do chassi: ${mecha.custo} (não descontado do seu dinheiro)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "Capacidade MODs: Espaços Usados: $modsGasto / ${mecha.mod_pontos_max} (Restantes: $modsRestantes)",
                style = MaterialTheme.typography.labelSmall,
                color = if (modsRestantes < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (armaduraNoLimite) {
                Text(
                    text = "Armadura Extra no limite máximo do chassi (+$armaduraExtraMax); o excedente não é aplicado.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

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

                val modsAgrupados = remember(mecha.mods_instalados, mecha.tamanho) {
                    mecha.mods_instalados.groupBy { it.id }.values.map { lista ->
                        val embaixador = lista.first()
                        val qtd = lista.size
                        val custoUnitario = embaixador.custoResolvido(mecha.tamanho)
                        val custoTotal = custoUnitario * qtd
                        Triple(embaixador, qtd, custoTotal)
                    }
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    modsAgrupados.forEach { (mod, qtd, custoTotal) ->
                        val isNeg = custoTotal < 0
                        val sign = if (isNeg) "" else "+"
                        val labelTexto = if (qtd > 1) {
                            "${mod.nome} x$qtd [$sign$custoTotal MODs]"
                        } else {
                            "${mod.nome} [$sign$custoTotal MODs]"
                        }

                        InputChip(
                            selected = true,
                            onClick = {
                                val indexParaRemover = mecha.mods_instalados.indexOfLast { it.id == mod.id }
                                if (indexParaRemover >= 0) {
                                    val novaLista = mecha.mods_instalados.toMutableList().apply { removeAt(indexParaRemover) }
                                    onUpdateMecha(mecha.copy(mods_instalados = novaLista))
                                }
                            },
                            label = { Text(labelTexto) },
                            trailingIcon = { Icon(Icons.Default.Delete, contentDescription = "Remover $labelTexto") }
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
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        AssistChip(
                            onClick = { showWeaponCatalogDialog = true },
                            label = { Text("Catálogo") },
                            leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        )
                        AssistChip(
                            onClick = { showCreateCustomWeaponDialog = true },
                            label = { Text("Criar") },
                            leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        )
                    }
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    mecha.armas_equipadas.forEach { arma ->
                        val armaMods = matchWeaponFromCatalog(arma, weaponCatalogComCustomizadas)?.mods_cost
                        InputChip(
                            selected = true,
                            onClick = {
                                val restantes = mecha.armas_equipadas - arma
                                onUpdateMecha(
                                    mecha.copy(
                                        armas_equipadas = restantes,
                                        // Só descarta a definição customizada quando a última cópia
                                        // equipada dela for removida (senão perderíamos o Mods cost
                                        // de outras armas iguais que ainda estão equipadas).
                                        armasCustomizadas = if (restantes.none { it == arma }) {
                                            mecha.armasCustomizadas.filterNot { it.nome == arma }
                                        } else mecha.armasCustomizadas
                                    )
                                )
                            },
                            label = { Text(if (armaMods != null) "$arma [$armaMods MODs]" else arma) },
                            trailingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
                        )
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
                                // Armadura Extra tem um limite próprio (Armadura Máxima da categoria do
                                // chassi, livro), não um max_uses fixo — cada compra soma +2 de Armadura.
                                val isMaxed = if (mod.id == "mod_def_armadura_extra") {
                                    armaduraExtraMax > 0 && armaduraExtraBruta >= armaduraExtraMax
                                } else {
                                    currentUses >= mod.max_uses
                                }
                                val custoMod = mod.custoResolvido(mecha.tamanho)
                                val isNeg = custoMod < 0
                                // Qualidades negativas devolvem espaços de MODs, então nunca são
                                // bloqueadas pelo orçamento — só por max_uses. Mods de custo
                                // positivo são bloqueados se ultrapassarem o total do chassi.
                                val estouraOrcamento = custoMod > 0 && modsGasto + custoMod > mecha.mod_pontos_max

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
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable(enabled = EditionConfig.isFullEdition) {
                                                    val custoStr = if (mod.custo.isNotBlank()) "\n\nCusto: ${mod.custo}" else ""
                                                    infoDialogTitle = mod.nome
                                                    infoDialogContent = (mod.descricao + custoStr).ifBlank { "Nenhuma descrição disponível." }
                                                },
                                            verticalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            MarqueeText(
                                                text = mod.nome,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                        }
                                        Spacer(Modifier.width(8.dp))
                                        val conflitoMsg = com.example.swadebuilder.model.MechaModIncompatibilityValidator.validarInstalacao(mod, mecha.mods_instalados)
                                        val temConflito = conflitoMsg != null
                                        val custoTotalAcumulado = custoMod * currentUses

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            if (currentUses > 0) {
                                                FilledTonalIconButton(
                                                    onClick = {
                                                        val idx = mecha.mods_instalados.indexOfLast { it.id == mod.id }
                                                        if (idx >= 0) {
                                                            val novaLista = mecha.mods_instalados.toMutableList().apply { removeAt(idx) }
                                                            onUpdateMecha(mecha.copy(mods_instalados = novaLista))
                                                        }
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(Icons.Default.Remove, contentDescription = "Diminuir ${mod.nome}", modifier = Modifier.size(14.dp))
                                                }

                                                val signTotal = if (custoTotalAcumulado < 0) "" else "+"
                                                Text(
                                                    text = "x$currentUses [$signTotal$custoTotalAcumulado MODs]",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isNeg) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                                )
                                            } else {
                                                val signUnit = if (isNeg) "" else "+"
                                                Text(
                                                    text = if (isMaxed) "Máx" else if (estouraOrcamento) "Sem MODs" else if (temConflito) "Bloq" else "$signUnit$custoMod MODs",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = if (temConflito || isMaxed || estouraOrcamento) MaterialTheme.colorScheme.onSurfaceVariant else if (isNeg) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            FilledTonalIconButton(
                                                onClick = {
                                                    if (conflitoMsg != null) {
                                                        android.widget.Toast.makeText(context, conflitoMsg, android.widget.Toast.LENGTH_LONG).show()
                                                        return@FilledTonalIconButton
                                                    }
                                                    if (isMaxed) {
                                                        android.widget.Toast.makeText(context, "Limite máximo atingido para '${mod.nome}'.", android.widget.Toast.LENGTH_SHORT).show()
                                                        return@FilledTonalIconButton
                                                    }
                                                    if (estouraOrcamento) {
                                                        android.widget.Toast.makeText(context, "Espaço de MODs insuficiente para '${mod.nome}'.", android.widget.Toast.LENGTH_SHORT).show()
                                                        return@FilledTonalIconButton
                                                    }
                                                    onUpdateMecha(mecha.copy(mods_instalados = mecha.mods_instalados + mod))
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Default.Add, contentDescription = "Instalar ${mod.nome}", modifier = Modifier.size(14.dp))
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
                        val estouraOrcamentoArma = modsGasto + w.mods_cost > mecha.mod_pontos_max
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
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable(enabled = EditionConfig.isFullEdition) {
                                            val custoStr = if (w.custo.isNotBlank()) "\n\nCusto: ${w.custo}" else ""
                                            infoDialogTitle = w.nome
                                            infoDialogContent = (w.descricao + custoStr).ifBlank { "Nenhuma descrição disponível." }
                                        },
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = w.nome,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    val custoText = if (w.custo.isNotBlank()) " | ${w.custo}" else ""
                                    Text(
                                        text = "${w.mods_cost} MODs$custoText",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                OutlinedButton(
                                    enabled = !estouraOrcamentoArma,
                                    onClick = {
                                        onUpdateMecha(
                                            mecha.copy(
                                                armas_equipadas = mecha.armas_equipadas + w.nome
                                            )
                                        )
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(if (estouraOrcamentoArma) "Sem MODs" else "Equipar", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        )
    }

    if (infoDialogTitle != null && EditionConfig.isFullEdition) {
        AlertDialog(
            onDismissRequest = {
                infoDialogTitle = null
                infoDialogContent = null
            },
            title = {
                Text(infoDialogTitle!!, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = infoDialogContent.orEmpty().ifBlank { "Nenhuma descrição disponível." },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        infoDialogTitle = null
                        infoDialogContent = null
                    }
                ) {
                    Text("Fechar")
                }
            }
        )
    }

    // Modal de Criação de Arma Customizada para este Mecha. Substitui o antigo campo de texto
    // livre "Arma Personalizada" (que aceitava qualquer string e nunca gastava MODs) — aqui o
    // Mods cost é um campo estruturado obrigatório, então a arma sempre entra na conta de
    // `modsDasArmas` acima, igual às armas do catálogo oficial.
    if (showCreateCustomWeaponDialog) {
        CreateCustomMechaWeaponDialog(
            onDismiss = { showCreateCustomWeaponDialog = false },
            onCreate = { newWeapon ->
                onUpdateMecha(
                    mecha.copy(
                        armasCustomizadas = mecha.armasCustomizadas + newWeapon,
                        armas_equipadas = mecha.armas_equipadas + newWeapon.nome
                    )
                )
                showCreateCustomWeaponDialog = false
            }
        )
    }
}

@Composable
private fun CreateCustomMechaWeaponDialog(
    onDismiss: () -> Unit,
    onCreate: (MechaWeaponItem) -> Unit
) {
    var nomeText by remember { mutableStateOf("") }

    // Dano Estruturado
    var danoBaseadoEmForca by remember { mutableStateOf(false) }
    var danoQtd by remember { mutableStateOf("3") }
    var danoDado by remember { mutableStateOf("d8") }
    var danoBonusText by remember { mutableStateOf("0") }

    // Estatísticas da Arma
    var paText by remember { mutableStateOf("4") }
    var cdt by remember { mutableStateOf("1") }
    var distancia by remember { mutableStateOf("50/100/200") }
    var modsCostText by remember { mutableStateOf("1") }
    var tamanhoMinimoText by remember { mutableStateOf("4") }
    var custoRefText by remember { mutableStateOf("") }

    // Propriedades / Observações
    val propriedadesDisponiveis = listOf("Arma Pesada", "Cauterizante", "Sobrecarga", "Defesa de Ponto", "Tiro de Reação", "MPE", "MME", "MGE", "Guiada")
    val propriedadesSelecionadas = remember { mutableStateListOf<String>() }
    var obsLivreText by remember { mutableStateOf("") }

    fun montarDano(): String {
        val b = danoBonusText.toIntOrNull() ?: 0
        val sufixoBonus = if (b > 0) "+$b" else if (b < 0) "$b" else ""
        return if (danoBaseadoEmForca) {
            "For+$danoDado$sufixoBonus"
        } else {
            val qtd = danoQtd.toIntOrNull()?.coerceAtLeast(1) ?: 1
            val base = if (qtd <= 1) danoDado else "$qtd$danoDado"
            "$base$sufixoBonus"
        }
    }

    val danoFinal = montarDano()
    val paInt = paText.toIntOrNull() ?: 0
    val modsCostInt = modsCostText.toIntOrNull()?.coerceAtLeast(0) ?: 1

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            FilledTonalButton(
                enabled = nomeText.isNotBlank(),
                onClick = {
                    val propsStr = propriedadesSelecionadas.joinToString(", ")
                    val descPartes = listOfNotNull(
                        distancia.takeIf { it.isNotBlank() }?.let { "Distância: $it" },
                        tamanhoMinimoText.takeIf { it.isNotBlank() && it != "-" }?.let { "Tamanho Mín.: $it" },
                        propsStr.takeIf { it.isNotBlank() },
                        obsLivreText.takeIf { it.isNotBlank() }
                    )
                    val descFinal = descPartes.joinToString(". ")

                    val nomeComDanoEPa = buildString {
                        append(nomeText.trim())
                        append(" ($danoFinal")
                        if (paInt > 0) append(", PA $paInt")
                        if (cdt.isNotBlank() && cdt != "1") append(", CdT $cdt")
                        append(")")
                    }

                    onCreate(
                        MechaWeaponItem(
                            id = "custom_mecha_weapon_${java.util.UUID.randomUUID()}",
                            nome = nomeComDanoEPa,
                            mods_cost = modsCostInt,
                            custo = custoRefText.trim(),
                            descricao = descFinal
                        )
                    )
                }
            ) {
                Text("Criar Arma")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        title = { Text("Criar Arma de Mecha") },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Monte uma arma estruturada com dados de SWADE, custo em MODs e propriedades de Mecha.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Nome da Arma
                OutlinedTextField(
                    value = nomeText,
                    onValueChange = { nomeText = it },
                    label = { Text("Nome da Arma") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // --- CONSTRUTOR DE DANO ---
                Text("Dano da Arma:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = !danoBaseadoEmForca,
                        onClick = { danoBaseadoEmForca = false },
                        label = { Text("Dado Fixo (3d8)") }
                    )
                    FilterChip(
                        selected = danoBaseadoEmForca,
                        onClick = { danoBaseadoEmForca = true },
                        label = { Text("Baseado em Força (For+d12)") }
                    )
                }

                // Dado e Quantidade (Carrossel Horizontal de 1 a 20 e Tipos de Dados)
                if (!danoBaseadoEmForca) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Quantidade de Dados (1 a 20): $danoQtd", style = MaterialTheme.typography.labelSmall)
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            (1..20).forEach { q ->
                                val qStr = q.toString()
                                FilterChip(
                                    selected = danoQtd == qStr,
                                    onClick = { danoQtd = qStr },
                                    label = { Text(qStr) }
                                )
                            }
                        }
                    }
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Tipo do Dado:", style = MaterialTheme.typography.labelSmall)
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("d4", "d6", "d8", "d10", "d12", "d20", "d100").forEach { d ->
                            FilterChip(
                                selected = danoDado == d,
                                onClick = { danoDado = d },
                                label = { Text(d) }
                            )
                        }
                    }
                }

                // Bônus Fixo de Dano (Campo numérico livre)
                OutlinedTextField(
                    value = danoBonusText,
                    onValueChange = { danoBonusText = it.filter { c -> c.isDigit() || c == '-' } },
                    label = { Text("Bônus Fixo de Dano (ex: 0, 2, 20, 100)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                Text(
                    text = "Dano Resultante: $danoFinal",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                HorizontalDivider()

                // --- PA e CdT e Alcance ---
                OutlinedTextField(
                    value = paText,
                    onValueChange = { paText = it.filter { c -> c.isDigit() } },
                    label = { Text("PA - Penetração de Armadura (ex: 0, 4, 20, 100)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = cdt,
                        onValueChange = { cdt = it },
                        label = { Text("CdT (Cadência)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = distancia,
                        onValueChange = { distancia = it },
                        label = { Text("Alcance / Distância") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // --- CUSTO EM MODS & TAMANHO MÍNIMO ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = modsCostText,
                        onValueChange = { modsCostText = it.filter { c -> c.isDigit() } },
                        label = { Text("Custo em MODs (ex: 0, 1, 2, 20)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = tamanhoMinimoText,
                        onValueChange = { tamanhoMinimoText = it },
                        label = { Text("Tam. Mín. (ex: 4, 7, 12, -)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = custoRefText,
                    onValueChange = { custoRefText = it },
                    label = { Text("Preço de Referência ($ opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // --- PROPRIEDADES ESPECIAIS ---
                Text("Propriedades / Módulos Especiais:", style = MaterialTheme.typography.labelSmall)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    propriedadesDisponiveis.forEach { prop ->
                        val sel = prop in propriedadesSelecionadas
                        FilterChip(
                            selected = sel,
                            onClick = {
                                if (sel) propriedadesSelecionadas.remove(prop)
                                else propriedadesSelecionadas.add(prop)
                            },
                            label = { Text(prop) }
                        )
                    }
                }

                OutlinedTextField(
                    value = obsLivreText,
                    onValueChange = { obsLivreText = it },
                    label = { Text("Observações Adicionais (opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}
