package com.example.swadebuilder.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.foundation.clickable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import com.example.swadebuilder.util.AppPreferences
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.IconButton
import androidx.compose.ui.text.style.TextAlign
import com.example.swadebuilder.toDiceString
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.util.loadJsonAsset
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.toIdSlug
import com.example.swadebuilder.util.toEditionDisplayName
import com.example.swadebuilder.model.Requisito
import com.example.swadebuilder.model.getActiveOrigins
import com.example.swadebuilder.model.getDisplayName
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.FeedbackController
import com.example.swadebuilder.TabStyle
import com.example.swadebuilder.ui.theme.AppTheme
import kotlin.math.roundToInt

import androidx.compose.material3.OutlinedButton

// Livro Básico: "Super Poderes (2+X)... o custo é 2 — pelo Antecedente Arcano
// (Super Poderes) — mais o custo do poder selecionado (X)." Muitos poderes do
// Compêndio de Super Poderes têm custo em escada por nível (ex.: "1/2/3/4/5"),
// então X aqui é o primeiro degrau (a compra mínima do poder).
private fun primeiroCustoSuperPoder(custoBase: String?): Int =
    custoBase
        ?.split("/")
        ?.firstOrNull()
        ?.trim()
        ?.replace('–', '-')
        ?.toIntOrNull()
        ?: 1

// Junta o conteúdo customizado de TODOS os livros de armazenamento (Geral incluso) num
// único BookCustomContent — mesma normalização de identidade usada pela lista "Gerenciar
// Conteúdo Customizado" (activeBookCustomData, mais abaixo neste arquivo): um mesmo
// id/nome salvo sob mais de uma tag de livro vira uma cópia representativa só. Usado pro
// backup de ficha (ver "Backup e Transferência (JSON)"), pra nunca deixar a ficha
// referenciar conteúdo custom que não existe em outro aparelho/instalação.
private fun aggregateAllCustomContent(
    context: android.content.Context,
    manager: com.example.swadebuilder.util.CustomStorageManager
): com.example.swadebuilder.util.BookCustomContent {
    val livros = com.example.swadebuilder.util.TODOS_OS_LIVROS + com.example.swadebuilder.util.TAG_GERAL
    val all = livros.map { manager.loadCustomContent(context, it) }
    return com.example.swadebuilder.util.BookCustomContent(
        bookKey = "TODOS",
        vantagens = all.flatMap { it.vantagens }.distinctBy { it.id },
        complicacoes = all.flatMap { it.complicacoes }.distinctBy { it.id },
        equipamentos = all.flatMap { it.equipamentos }.distinctBy { it.nome.lowercase() },
        poderes = all.flatMap { it.poderes }.distinctBy { it.id },
        superPoderes = all.flatMap { it.superPoderes }.distinctBy { it.nome.lowercase() },
        racas = all.flatMap { it.racas }.distinctBy { it.nome.lowercase() },
        habilidadesRaciais = all.flatMap { it.habilidadesRaciais }.distinctBy { it.nome.lowercase() },
        variantesRaciais = all.flatMap { it.variantesRaciais }.distinctBy { it.id }
    )
}

// Contrapartida de aggregateAllCustomContent() na importação: grava cada item no disco
// (CustomStorageManager, sob o livro do próprio item.origem — GERAL quando o tipo não tem
// origem, como Super Poder/Traço Racial) e sincroniza em memória (state.lista*) usando os
// mesmos helpers "pula se já existir" do fluxo normal de criação (ver TextButton "Salvar
// Item" acima), pra que os ids/nomes que a ficha restaurada referencia já resolvam
// imediatamente, sem esperar um reload completo dos dados do jogo.
private fun mergeImportedCustomContent(
    context: android.content.Context,
    state: CriadorState,
    manager: com.example.swadebuilder.util.CustomStorageManager,
    bundle: com.example.swadebuilder.util.BookCustomContent
) {
    val geral = com.example.swadebuilder.util.TAG_GERAL
    bundle.vantagens.forEach { item ->
        manager.addVantagem(context, item.origem.ifBlank { geral }, item)
        state.addCustomVantagem(item)
    }
    bundle.complicacoes.forEach { item ->
        manager.addComplicacao(context, item.origem.ifBlank { geral }, item)
        state.addCustomComplicacao(item)
    }
    bundle.equipamentos.forEach { item ->
        manager.addEquipamento(context, item.origem?.ifBlank { geral } ?: geral, item)
        state.addCustomEquipamento(item)
    }
    bundle.poderes.forEach { item ->
        manager.addPoder(context, item.origem.ifBlank { geral }, item)
        state.addCustomPoder(item)
    }
    bundle.superPoderes.forEach { item ->
        manager.addSuperPoder(context, geral, item)
        state.addCustomSuperPoder(item)
    }
    bundle.racas.forEach { item ->
        manager.addRaca(context, item.origem.ifBlank { geral }, item)
        if (state.listaAncestralidadesJson.none { it.nome.equals(item.nome, ignoreCase = true) }) {
            state.listaAncestralidadesJson = state.listaAncestralidadesJson + item
        }
    }
    bundle.habilidadesRaciais.forEach { item ->
        manager.addHabilidadeRacial(context, geral, item)
    }
    bundle.variantesRaciais.forEach { item ->
        manager.addVarianteRacial(context, geral, item)
        if (state.listaVariantesRaciaisCustom.none { it.id == item.id }) {
            state.listaVariantesRaciaisCustom = state.listaVariantesRaciaisCustom + item
        }
    }
}

/**
 * Rótulo + grupo de FilterChips do formulário de Conteúdo Customizado — era repetido em cada
 * categoria com pequenas inconsistências (algumas usavam Row sem quebra de linha, que cortava
 * os chips em telas estreitas quando havia várias opções).
 */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun LabeledChipGroup(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.FlowRowScope.() -> Unit
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        androidx.compose.foundation.layout.FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            content = content
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    state: CriadorState,
    isHomeScreen: Boolean = false,
    isCreationPhase: Boolean = false,
    onDismiss: () -> Unit,
    persistPrefs: () -> Unit,
    feedbackController: FeedbackController,
    onResetRulesToDefaults: (() -> Unit)? = null,
    // Chamado depois de qualquer criação/edição/exclusão de conteúdo customizado
    // (Vantagem, Complicação, Equipamento, Poder, Raça, Variante de Raça etc.):
    // invalida o cache de GameDataSnapshot por combinação de livros
    // (GameDataRepository/ModuleSnapshotCache), que senão continuaria devolvendo
    // um snapshot desatualizado — sem o conteúdo recém-criado — na próxima vez
    // que um personagem NOVO for criado com a mesma combinação de livros.
    onCustomContentChanged: () -> Unit = {},
    onThemeSelected: (AppTheme) -> Unit
) {
    var showNpcWarning by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    val themeNames = remember {
        mapOf(
            AppTheme.DEFAULT   to "Padrão",
            AppTheme.MEDIEVAL  to "Medieval",
            AppTheme.CYBERPUNK to "Cyberpunk",
            AppTheme.WW2       to "Segunda Guerra",
            AppTheme.HORROR    to "Horror",
            AppTheme.SCIFI     to "Sci-Fi",
            AppTheme.MINIMALIST to "Minimalista",
            AppTheme.HALLOWEEN to "Halloween"
        )
    }

    val themeDescriptions = remember {
        mapOf(
            AppTheme.DEFAULT   to "Pergaminho clássico (Old School)",
            AppTheme.MEDIEVAL  to "Manuscrito antigo e detalhes dourados",
            AppTheme.CYBERPUNK to "Estilo Matrix com linhas wireframe verdes",
            AppTheme.WW2       to "Papel Khaki e carimbo militar de campo",
            AppTheme.HORROR    to "Atmosfera gótica e detalhes carmesim",
            AppTheme.SCIFI     to "Interface holofuturista e azul estelar",
            AppTheme.MINIMALIST to "Design limpo e alto contraste",
            AppTheme.HALLOWEEN to "Laranja abóbora e roxo místico"
        )
    }

    val sortedThemes = remember(themeNames) {
        AppTheme.entries.sortedBy { themeNames[it] ?: it.name }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        // Um toque sem querer fora da área do diálogo (comum numa tela cheia de
        // opções) não deve fechar tudo e voltar pra ficha — só o botão
        // "Fechar"/voltar do sistema fecha.
        properties = androidx.compose.ui.window.DialogProperties(dismissOnClickOutside = false),
        title = { Text("Configurações", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Card "Interface do Sistema"
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
                            text = "Interface do Sistema",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Mensagens do Sistema", style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = state.showSystemMessages,
                                onCheckedChange = {
                                    state.showSystemMessages = it
                                    persistPrefs()
                                },
                                modifier = Modifier.scale(0.8f)
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                "Modo de Distribuição (Atributos e Perícias)",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = state.modoSelecaoPericia == AppPreferences.ModoSelecaoPericia.CARROSSEL_POPOVER,
                                    onClick = {
                                        state.modoSelecaoPericia = AppPreferences.ModoSelecaoPericia.CARROSSEL_POPOVER
                                        persistPrefs()
                                    },
                                    label = { Text("Tocar e Escolher", style = MaterialTheme.typography.labelSmall) },
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = state.modoSelecaoPericia == AppPreferences.ModoSelecaoPericia.STEPPER_CORES,
                                    onClick = {
                                        state.modoSelecaoPericia = AppPreferences.ModoSelecaoPericia.STEPPER_CORES
                                        persistPrefs()
                                    },
                                    label = { Text("Botões + e -", style = MaterialTheme.typography.labelSmall) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        if (isHomeScreen) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Não solicitar escolha de regras", style = MaterialTheme.typography.bodyMedium)
                                    Text("Direto para criação com regras padrão.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(
                                    checked = state.pularSelecaoRegras,
                                    onCheckedChange = {
                                        state.pularSelecaoRegras = it
                                        persistPrefs()
                                        onResetRulesToDefaults?.invoke()
                                    },
                                    modifier = Modifier.scale(0.8f)
                                )
                            }
                        }

                        // NPC Mode Toggle (Only during creation phase and if not already NPC)
                        if (isCreationPhase && !state.modoProgressaoAtivo && !state.isNpcExibicao) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Modo Livre (NPC)", style = MaterialTheme.typography.bodyMedium)
                                    Text("Ignora custos e requisitos.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(
                                    checked = state.modoLivre,
                                    onCheckedChange = { if (it && !state.modoLivre) showNpcWarning = true },
                                    enabled = !state.modoLivre, // Irreversible
                                    modifier = Modifier.scale(0.8f)
                                )
                            }
                        }
                    }
                }

                // Card "Conteúdo Customizado"
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    var showCustomContentDialog by remember { mutableStateOf(false) }
                    var customItemName by remember { mutableStateOf("") }
                    var customItemDesc by remember { mutableStateOf("") }
                    var statusMessage by remember { mutableStateOf<String?>(null) }

                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Conteúdo Customizado",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Crie vantagens e itens caseiros com prefixo 'custom:'.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedButton(
                            onClick = {
                                customItemName = ""
                                customItemDesc = ""
                                statusMessage = null
                                showCustomContentDialog = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Gerenciar Conteúdo Customizado")
                        }
                    }

                    if (showCustomContentDialog) {
                        val context = androidx.compose.ui.platform.LocalContext.current
                        val customStorageManager = remember { com.example.swadebuilder.util.CustomStorageManager() }
                        // Livro(s) a que o item sendo criado vai ficar vinculado — o jogador
                        // escolhe isso na hora de salvar (ver "Seletor de Livros" abaixo), não
                        // fica mais preso ao livro que estava ativo quando abriu essa tela.
                        // Por padrão já vem com o livro atualmente ativo marcado.
                        var selectedBookTags by remember(state) {
                            mutableStateOf(setOf(state.getActiveOrigins().firstOrNull() ?: "BASICO"))
                        }
                        var selectedCategory by remember { mutableStateOf("Vantagem") }
                        var customRequirements by remember { mutableStateOf("") }
                        var customAdvCategory by remember { mutableStateOf(com.example.swadebuilder.model.Categoria.PROFISSIONAL) }
                        var customStage by remember { mutableStateOf("Novato") }
                        var customAttrMin by remember { mutableStateOf(mapOf<String, Int>()) }
                        var customSkillMin by remember { mutableStateOf(mapOf<String, Int>()) }
                        var customPrereqEdges by remember { mutableStateOf(listOf<String>()) }
                        var customPrereqComps by remember { mutableStateOf(listOf<String>()) }
                        var showAttrDialog by remember { mutableStateOf(false) }
                        var showSkillDialog by remember { mutableStateOf(false) }
                        var showEdgeDialog by remember { mutableStateOf(false) }
                        var showCompDialog by remember { mutableStateOf(false) }
                        var customSeverity by remember { mutableStateOf("Maior") }
                        var customEquipSuperType by remember { mutableStateOf("Arma") }
                        var customEquipSubtype by remember { mutableStateOf("Corpo a Corpo") }
                        var customCost by remember { mutableStateOf("0") }
                        var customWeight by remember { mutableStateOf("0") }
                        var customDamage by remember { mutableStateOf("") }
                        var customPp by remember { mutableStateOf("1") }
                        var customSuperPoderCustoBase by remember { mutableStateOf("2") }
                        // Um modificador por linha, no mesmo formato usado pelo catálogo oficial
                        // ("Nome (+custo): descrição"), ex.: "Área (+2): Modelo Médio de Explosão".
                        var customSuperPoderModificadores by remember { mutableStateOf("") }
                        // Antecedente Arcano customizado: "lista aberta" usa todos os poderes
                        // do(s) livro(s) marcado(s) no Seletor de Livros (ou de todos, se for
                        // "Geral") — ver Vantagem.poderesPermitidos/origem e o hook em
                        // PoderesSection.kt. Desmarcando, o Mestre escolhe poderes específicos.
                        var customAaListaAberta by remember { mutableStateOf(true) }
                        var customAaPoderesEspecificos by remember { mutableStateOf(setOf<String>()) }
                        var showAaPoderesPickerDialog by remember { mutableStateOf(false) }
                        var customRange by remember { mutableStateOf("Toque") }
                        var customDuration by remember { mutableStateOf("3 turnos") }
                        var customRacialTrait by remember { mutableStateOf("") }
                        var refreshTrigger by remember { mutableStateOf(0) }

                        var customTraitCost by remember { mutableStateOf("1") }
                        var selectedRacialTraits by remember { mutableStateOf(listOf<com.example.swadebuilder.model.HabilidadeCriacao>()) }
                        var showTraitSelectDialog by remember { mutableStateOf(false) }

                        // Estado da Variante de Raça custom (ver ResolveVariantPointBudgetUseCase / CustomAncestryVariant).
                        var varianteBaseRacaId by remember { mutableStateOf<String?>(null) }
                        var showVarianteBaseRacaDialog by remember { mutableStateOf(false) }
                        var varianteTracosRemovidos by remember { mutableStateOf(listOf<String>()) }
                        var varianteVantagensGratisRemovidas by remember { mutableStateOf(listOf<String>()) }
                        var varianteDesvantagensRemovidas by remember { mutableStateOf(listOf<String>()) }
                        var varianteTracosAdicionados by remember { mutableStateOf(listOf<com.example.swadebuilder.model.HabilidadeCriacao>()) }
                        var showVarianteTraitAddDialog by remember { mutableStateOf(false) }
                        var varianteVantagensAdicionadas by remember { mutableStateOf(listOf<String>()) }
                        var showVarianteVantagemAddDialog by remember { mutableStateOf(false) }
                        var varianteComplicacoesAdicionadas by remember { mutableStateOf(listOf<com.example.swadebuilder.model.CustomVariantComplicacaoEscolhida>()) }
                        var showVarianteComplicacaoSeveridadeDialog by remember { mutableStateOf(false) }
                        var showVarianteComplicacaoPickDialog by remember { mutableStateOf(false) }
                        var varianteComplicacaoComoMaiorEscolhido by remember { mutableStateOf(false) }
                        var varianteSemLimite by remember { mutableStateOf(false) }

                        val baseRacialCatalog: List<com.example.swadebuilder.model.HabilidadeCriacao> = remember {
                            runCatching {
                                context.loadJsonAsset<List<com.example.swadebuilder.model.HabilidadeCriacao>>("basico_habilidades_raciais.json")
                            }.getOrElse { emptyList() }.map { it.exibida() }
                        }

                        // "Super Poderes (2+X)": o traço racial em si custa 2 pontos, mais o
                        // custo do Super Poder do Compêndio de Super Poderes escolhido pelo
                        // Mestre (X). Como X varia por poder, a entrada de HabilidadeCriacao
                        // final é montada dinamicamente (ver superPoderRacialPickerTarget),
                        // não é um custo fixo de catálogo como os outros traços.
                        val superPoderesCatalog: List<com.example.swadebuilder.model.SuperPoder> = remember {
                            runCatching {
                                context.loadJsonAsset<List<com.example.swadebuilder.model.SuperPoder>>("super_poderes.json")
                            }.getOrElse { emptyList() }
                        }
                        var superPoderRacialPickerTarget by remember {
                            mutableStateOf<((com.example.swadebuilder.model.HabilidadeCriacao) -> Unit)?>(null)
                        }

                        // "Bônus/Penalidade de Perícia (±1/±2)": traços genéricos do catálogo
                        // oficial (basico_habilidades_raciais.json) que dizem "uma Perícia
                        // específica"/"uma perícia escolhida" — sem picker, o Mestre selecionava
                        // o traço genérico e não havia registro de qual perícia era. O app não
                        // aplica esse bônus em teste nenhum (perícia aqui é só custo de criação
                        // de raça, o teste em si é decidido à mesa), mas o traço final precisa
                        // deixar claro qual perícia foi escolhida — mesmo padrão de
                        // superPoderRacialPickerTarget, embutindo a escolha no nome
                        // ("Bônus de Perícia (+1): Intimidar") sem tocar no id
                        // (bonus_pericia_1/2, penalidade_pericia_1/2 continuam os mesmos).
                        val periciaChoiceTraitIds = remember {
                            setOf("bonus_pericia_1", "bonus_pericia_2", "penalidade_pericia_1", "penalidade_pericia_2")
                        }
                        var periciaTraitPickerTarget by remember {
                            mutableStateOf<Pair<com.example.swadebuilder.model.HabilidadeCriacao, (com.example.swadebuilder.model.HabilidadeCriacao) -> Unit>?>(null)
                        }

                        // "Empilháveis" (Armadura/Resistência/Aparar/Aparar Baixo/
                        // Tamanho +1/Frágil/Movimentação): os 3 livros marcam cada um
                        // com "(N)"/"(S)" — quantas vezes pode ser comprado, custo e
                        // efeito multiplicando por compra (ver
                        // RacialTraitPointCatalog.VEZES_MAX/basico_habilidades_raciais.json
                        // "vezesMax"). Mesmo padrão de picker que
                        // periciaTraitPickerTarget: ao marcar, abre "Quantas vezes?" em
                        // vez de já adicionar o traço com 1 compra.
                        var stackPickerTarget by remember {
                            mutableStateOf<Pair<com.example.swadebuilder.model.HabilidadeCriacao, (com.example.swadebuilder.model.HabilidadeCriacao) -> Unit>?>(null)
                        }

                        // Traços com "grupoEscolha" (ex.: Ações Adicionais 4/5/10 pontos) são
                        // versões ALTERNATIVAS do mesmo traço "(1)" — mesmo padrão de picker que
                        // stackPickerTarget, mas escolhendo entre entradas de catálogo distintas
                        // (nome/custo/id próprios) em vez de multiplicar 1..vezesMax.
                        var groupPickerTarget by remember {
                            mutableStateOf<Pair<com.example.swadebuilder.model.HabilidadeCriacao, (com.example.swadebuilder.model.HabilidadeCriacao) -> Unit>?>(null)
                        }

                        // Todas as categorias sempre disponíveis, em qualquer tela — a engrenagem
                        // já é global (tela inicial, criação, fase de XP), então não há mais
                        // motivo pra esconder categorias por causa de "isHomeScreen".
                        val categories = remember {
                            listOf("Vantagem", "Complicação", "Equipamento", "Poder", "Super Poder", "Antecedente Arcano", "Raça", "Traço Racial", "Variante de Raça")
                        }
                        if (selectedCategory !in categories) {
                            selectedCategory = categories.first()
                        }
                        // Todos os "livros" de armazenamento que existem (livros reais + Geral).
                        // Conteúdo customizado não fica mais preso a um livro só: é gravado sob
                        // cada tag escolhida (ver selectedBookTags), então pra ler/listar/apagar
                        // é preciso varrer todos eles, não só o que estava ativo quando abriu.
                        val todosOsLivrosDeArmazenamento = remember {
                            com.example.swadebuilder.util.TODOS_OS_LIVROS + com.example.swadebuilder.util.TAG_GERAL
                        }
                        val allCustomDataByBook = remember(refreshTrigger) {
                            todosOsLivrosDeArmazenamento.associateWith { customStorageManager.loadCustomContent(context, it) }
                        }
                        val activeBookCustomData = remember(allCustomDataByBook) {
                            val all = allCustomDataByBook.values
                            com.example.swadebuilder.util.BookCustomContent(
                                bookKey = "TODOS",
                                vantagens = all.flatMap { it.vantagens }.distinctBy { it.id },
                                complicacoes = all.flatMap { it.complicacoes }.distinctBy { it.id },
                                equipamentos = all.flatMap { it.equipamentos }.distinctBy { it.nome.lowercase() },
                                poderes = all.flatMap { it.poderes }.distinctBy { it.id },
                                superPoderes = all.flatMap { it.superPoderes }.distinctBy { it.nome.lowercase() },
                                racas = all.flatMap { it.racas }.distinctBy { it.nome.lowercase() },
                                habilidadesRaciais = all.flatMap { it.habilidadesRaciais }.distinctBy { it.nome.lowercase() },
                                variantesRaciais = all.flatMap { it.variantesRaciais }.distinctBy { it.id }
                            )
                        }

                        // Catálogo completo (oficial + customizado), sem colapsar por
                        // grupoEscolha — usado tanto pelas listas de seleção de Traços
                        // Raciais (que colapsam pra 1 linha por grupo) quanto pelo diálogo
                        // "Qual versão?" do groupPickerTarget (que precisa ver TODAS as
                        // alternativas do grupo escolhido).
                        val fullTraitsCatalog = remember(activeBookCustomData) {
                            (baseRacialCatalog + activeBookCustomData.habilidadesRaciais).distinctBy { it.nome }
                        }

                        AlertDialog(
                            onDismissRequest = { showCustomContentDialog = false },
                            // Mesmo motivo do diálogo de Configurações: essa tela tem
                            // formulário longo, um toque de leve fora da área não pode
                            // derrubar o que já foi digitado.
                            properties = androidx.compose.ui.window.DialogProperties(dismissOnClickOutside = false),
                            title = { Text("Criar Conteúdo Customizado", style = MaterialTheme.typography.titleMedium) },
                            text = {
                                Column(
                                    modifier = Modifier
                                        .verticalScroll(rememberScrollState())
                                        .fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "O que você deseja criar?",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    // Category selector horizontal carousel (styled like superpower carousel with smooth edge gradient)
                                    Box(
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        androidx.compose.foundation.lazy.LazyRow(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(end = 24.dp)
                                        ) {
                                            items(categories.size) { index ->
                                                val cat = categories[index]
                                                val isSel = selectedCategory == cat
                                                androidx.compose.material3.FilterChip(
                                                    selected = isSel,
                                                    onClick = { selectedCategory = cat },
                                                    label = {
                                                        Text(
                                                            text = cat,
                                                            style = MaterialTheme.typography.labelMedium,
                                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                                        )
                                                    },
                                                    colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                )
                                            }
                                        }

                                        // Edge gradient fade
                                        Box(
                                            modifier = Modifier
                                                .width(20.dp)
                                                .align(Alignment.CenterEnd)
                                                .fillMaxHeight()
                                                .background(
                                                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                                                        colors = listOf(
                                                            androidx.compose.ui.graphics.Color.Transparent,
                                                            MaterialTheme.colorScheme.surfaceContainerHigh
                                                        )
                                                    )
                                                )
                                        )
                                    }

                                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

                                    // Container card for form elements to prevent overlapping and maintain clean spacing
                                    androidx.compose.material3.Surface(
                                        shape = MaterialTheme.shapes.medium,
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            // Common Name field
                                            androidx.compose.material3.OutlinedTextField(
                                                value = customItemName,
                                                onValueChange = { customItemName = it },
                                                label = { Text("Nome da $selectedCategory") },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth()
                                            )

                                            // Seletor de Livros: em quais livros esse item vai aparecer. "Geral"
                                            // funciona em qualquer combinação de livros ativos; os demais são
                                            // específicos. Vale pra todas as categorias, é escolhido uma vez só
                                            // aqui e usado na hora de salvar.
                                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text("Vincular a quais livros:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                                @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
                                                androidx.compose.foundation.layout.FlowRow(
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    androidx.compose.material3.FilterChip(
                                                        selected = com.example.swadebuilder.util.TAG_GERAL in selectedBookTags,
                                                        onClick = {
                                                            selectedBookTags = if (com.example.swadebuilder.util.TAG_GERAL in selectedBookTags) {
                                                                selectedBookTags - com.example.swadebuilder.util.TAG_GERAL
                                                            } else {
                                                                selectedBookTags + com.example.swadebuilder.util.TAG_GERAL
                                                            }
                                                        },
                                                        label = { Text("Geral", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) }
                                                    )
                                                    com.example.swadebuilder.util.TODOS_OS_LIVROS.forEach { bookKey ->
                                                        androidx.compose.material3.FilterChip(
                                                            selected = bookKey in selectedBookTags,
                                                            onClick = {
                                                                selectedBookTags = if (bookKey in selectedBookTags) {
                                                                    selectedBookTags - bookKey
                                                                } else {
                                                                    selectedBookTags + bookKey
                                                                }
                                                            },
                                                            label = { Text(bookKey.toEditionDisplayName(), style = MaterialTheme.typography.labelSmall) }
                                                        )
                                                    }
                                                }
                                                if (selectedBookTags.isEmpty()) {
                                                    Text(
                                                        "Nenhum livro marcado — vai salvar no livro ativo no momento.",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.error
                                                    )
                                                }
                                            }

                                            // Category-specific fields
                                            when (selectedCategory) {
                                                "Vantagem" -> {
                                                    // Modular Requirements Section
                                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                        Text("Requisitos Modulares da Vantagem:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)

                                                        // Summary of configured requirements
                                                        val reqSummary = buildList {
                                                            if (customStage.isNotBlank()) add("Estágio: $customStage")
                                                            if (customAttrMin.isNotEmpty()) add("Atributos: " + customAttrMin.entries.joinToString { "${it.key} d${it.value}" })
                                                            if (customSkillMin.isNotEmpty()) add("Perícias: " + customSkillMin.entries.joinToString { "${it.key} d${it.value}" })
                                                            if (customPrereqEdges.isNotEmpty()) add("Vantagens Prévias: ${customPrereqEdges.size} selecionada(s)")
                                                            if (customPrereqComps.isNotEmpty()) add("Complicações: ${customPrereqComps.size} selecionada(s)")
                                                        }
                                                        if (reqSummary.isNotEmpty()) {
                                                            Text(
                                                                text = reqSummary.joinToString(" | "),
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                        }

                                                        // Interactive Selector Buttons
                                                        @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
                                                        androidx.compose.foundation.layout.FlowRow(
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                                        ) {
                                                            OutlinedButton(onClick = { showAttrDialog = true }) {
                                                                Text(if (customAttrMin.isEmpty()) "+ Atributos" else "Atributos (${customAttrMin.size})", style = MaterialTheme.typography.labelSmall)
                                                            }
                                                            OutlinedButton(onClick = { showSkillDialog = true }) {
                                                                Text(if (customSkillMin.isEmpty()) "+ Perícias" else "Perícias (${customSkillMin.size})", style = MaterialTheme.typography.labelSmall)
                                                            }
                                                            OutlinedButton(onClick = { showEdgeDialog = true }) {
                                                                Text(if (customPrereqEdges.isEmpty()) "+ Vantagens Prévias" else "Vantagens (${customPrereqEdges.size})", style = MaterialTheme.typography.labelSmall)
                                                            }
                                                            OutlinedButton(onClick = { showCompDialog = true }) {
                                                                Text(if (customPrereqComps.isEmpty()) "+ Complicações" else "Complicações (${customPrereqComps.size})", style = MaterialTheme.typography.labelSmall)
                                                            }
                                                        }
                                                    }

                                                    androidx.compose.material3.OutlinedTextField(
                                                        value = customRequirements,
                                                        onValueChange = { customRequirements = it },
                                                        label = { Text("Outros Requisitos (texto livre)") },
                                                        singleLine = true,
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                    val availableAdvCategories = remember(state.listaVantagens, selectedBookTags, state.compendioArteDaGuerraAtivo, state.compendioPathfinderAtivo, state.compendioDeadlandsAtivo, state.compendioHorrorAtivo, state.modoMonstroAtivo, state.modoSupers) {
                                                        val baseCategories = mutableSetOf(
                                                            Categoria.ANTECEDENTE,
                                                            Categoria.COMBATE,
                                                            Categoria.ESTRANHAS,
                                                            Categoria.LENDARIAS,
                                                            Categoria.LIDERANCA,
                                                            Categoria.PODER,
                                                            Categoria.PROFISSIONAL,
                                                            Categoria.SOCIAIS
                                                        )
                                                        if (state.listaVantagens.isNotEmpty()) {
                                                            baseCategories.addAll(state.listaVantagens.map { it.categoria })
                                                        }
                                                        if ("ARTE_DA_GUERRA" in selectedBookTags || state.compendioArteDaGuerraAtivo) {
                                                            baseCategories.addAll(listOf(Categoria.CHI, Categoria.TROPO, Categoria.ESTILO_MARCIAL))
                                                        }
                                                        if ("PATHFINDER" in selectedBookTags || state.compendioPathfinderAtivo) {
                                                            baseCategories.addAll(listOf(Categoria.CLASSE, Categoria.VANTAGEM_DE_CLASSE, Categoria.PRESTIGIO, Categoria.ANCESTRALIDADE))
                                                        }
                                                        if ("DEADLANDS" in selectedBookTags || state.compendioDeadlandsAtivo) {
                                                            baseCategories.addAll(listOf(Categoria.ATORMENTADO, Categoria.ANCESTRALIDADE))
                                                        }
                                                        if ("HORROR" in selectedBookTags || state.compendioHorrorAtivo || state.modoMonstroAtivo) {
                                                            baseCategories.add(Categoria.MONSTRUOSAS)
                                                        }
                                                        if (state.modoSupers) {
                                                            baseCategories.add(Categoria.SUPER)
                                                        }
                                                        Categoria.entries.filter { it in baseCategories }
                                                    }

                                                    if (customAdvCategory !in availableAdvCategories) {
                                                        customAdvCategory = availableAdvCategories.firstOrNull() ?: Categoria.PROFISSIONAL
                                                    }

                                                    LabeledChipGroup("Categoria da Vantagem:") {
                                                        availableAdvCategories.forEach { catEnum ->
                                                            androidx.compose.material3.FilterChip(
                                                                selected = customAdvCategory == catEnum,
                                                                onClick = { customAdvCategory = catEnum },
                                                                label = { Text(catEnum.getDisplayName(), style = MaterialTheme.typography.labelSmall) }
                                                            )
                                                        }
                                                    }
                                                    LabeledChipGroup("Estágio Mínimo:") {
                                                        listOf("Novato", "Experiente", "Veterano", "Heroico", "Lendário").forEach { stage ->
                                                            androidx.compose.material3.FilterChip(
                                                                selected = customStage == stage,
                                                                onClick = { customStage = stage },
                                                                label = { Text(stage, style = MaterialTheme.typography.labelSmall) }
                                                            )
                                                        }
                                                    }
                                                }
                                                "Complicação" -> {
                                                    LabeledChipGroup("Severidade Permitida:") {
                                                        listOf("Maior", "Menor", "Maior ou Menor").forEach { sev ->
                                                            androidx.compose.material3.FilterChip(
                                                                selected = customSeverity == sev,
                                                                onClick = { customSeverity = sev },
                                                                label = { Text(sev, style = MaterialTheme.typography.labelSmall) }
                                                            )
                                                        }
                                                    }
                                                }
                                                "Equipamento" -> {
                                                    LabeledChipGroup("Tipo de Equipamento:") {
                                                        listOf("Arma", "Armadura", "Escudo", "Geral", "Veículo").forEach { st ->
                                                            androidx.compose.material3.FilterChip(
                                                                selected = customEquipSuperType == st,
                                                                onClick = {
                                                                    customEquipSuperType = st
                                                                    customEquipSubtype = when (st) {
                                                                        "Arma" -> "Corpo a Corpo"
                                                                        "Armadura" -> "Armadura Corporal"
                                                                        "Escudo" -> "Escudo"
                                                                        "Veículo" -> "Veículo"
                                                                        else -> "Equipamento Geral"
                                                                    }
                                                                },
                                                                label = { Text(st, style = MaterialTheme.typography.labelSmall) }
                                                            )
                                                        }
                                                    }
                                                    if (customEquipSuperType == "Arma") {
                                                        LabeledChipGroup("Subtipo de Arma:") {
                                                            listOf("Corpo a Corpo", "Ataque a Distância", "Futurista").forEach { sub ->
                                                                androidx.compose.material3.FilterChip(
                                                                    selected = customEquipSubtype == sub,
                                                                    onClick = { customEquipSubtype = sub },
                                                                    label = { Text(sub, style = MaterialTheme.typography.labelSmall) }
                                                                )
                                                            }
                                                        }
                                                    }
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        androidx.compose.material3.OutlinedTextField(
                                                            value = customCost,
                                                            onValueChange = { customCost = it },
                                                            label = { Text("Custo ($)") },
                                                            singleLine = true,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                        androidx.compose.material3.OutlinedTextField(
                                                            value = customWeight,
                                                            onValueChange = { customWeight = it },
                                                            label = { Text("Peso (kg)") },
                                                            singleLine = true,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                    }
                                                    androidx.compose.material3.OutlinedTextField(
                                                        value = customDamage,
                                                        onValueChange = { customDamage = it },
                                                        label = { Text("Dano / Armadura / Efeito (ex: For+d12+5)") },
                                                        singleLine = true,
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                }
                                                "Poder" -> {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        androidx.compose.material3.OutlinedTextField(
                                                            value = customPp,
                                                            onValueChange = { customPp = it },
                                                            label = { Text("Pontos de Poder") },
                                                            singleLine = true,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                        androidx.compose.material3.OutlinedTextField(
                                                            value = customRange,
                                                            onValueChange = { customRange = it },
                                                            label = { Text("Alcance") },
                                                            singleLine = true,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                    }
                                                    androidx.compose.material3.OutlinedTextField(
                                                        value = customDuration,
                                                        onValueChange = { customDuration = it },
                                                        label = { Text("Duração (ex: 3 turnos)") },
                                                        singleLine = true,
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                }
                                                "Super Poder" -> {
                                                    androidx.compose.material3.OutlinedTextField(
                                                        value = customSuperPoderCustoBase,
                                                        onValueChange = { customSuperPoderCustoBase = it },
                                                        label = { Text("Custo Base (ex: 2 ou 1/2/3/4/5)") },
                                                        singleLine = true,
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                    androidx.compose.material3.OutlinedTextField(
                                                        value = customSuperPoderModificadores,
                                                        onValueChange = { customSuperPoderModificadores = it },
                                                        label = { Text("Modificadores (1 por linha, ex: Área (+2): Modelo Médio de Explosão)") },
                                                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                                    )
                                                }
                                                "Antecedente Arcano" -> {
                                                    Text(
                                                        "Cria a vantagem \"ANTECEDENTE ARCANO ($customItemName)\". Os livros marcados acima no Seletor de Livros também decidem de qual livro vêm os poderes na lista aberta.",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        androidx.compose.material3.FilterChip(
                                                            selected = customAaListaAberta,
                                                            onClick = { customAaListaAberta = true },
                                                            label = { Text("Lista aberta", style = MaterialTheme.typography.labelSmall) }
                                                        )
                                                        androidx.compose.material3.FilterChip(
                                                            selected = !customAaListaAberta,
                                                            onClick = { customAaListaAberta = false },
                                                            label = { Text("Poderes específicos", style = MaterialTheme.typography.labelSmall) }
                                                        )
                                                    }
                                                    if (customAaListaAberta) {
                                                        Text(
                                                            if (com.example.swadebuilder.util.TAG_GERAL in selectedBookTags) {
                                                                "Vai liberar todos os poderes de todos os livros."
                                                            } else {
                                                                "Vai liberar todos os poderes de: ${selectedBookTags.joinToString(", ") { it.toEditionDisplayName() }.ifBlank { "(marque um livro acima)" }}"
                                                            },
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    } else {
                                                        OutlinedButton(onClick = { showAaPoderesPickerDialog = true }) {
                                                            Text(
                                                                if (customAaPoderesEspecificos.isEmpty()) "+ Escolher Poderes" else "Poderes (${customAaPoderesEspecificos.size})",
                                                                style = MaterialTheme.typography.labelSmall
                                                            )
                                                        }
                                                    }
                                                }
                                                "Raça" -> {
                                                    val netRacePoints = selectedRacialTraits.sumOf { it.custo }
                                                    val pointColor = if (netRacePoints == 2) MaterialTheme.colorScheme.primary else if (netRacePoints < 2) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error

                                                    OutlinedCard(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        colors = CardDefaults.outlinedCardColors(
                                                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                                        )
                                                    ) {
                                                        Column(
                                                            modifier = Modifier.padding(12.dp),
                                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                                        ) {
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Column {
                                                                    Text(
                                                                        text = "Traços Raciais",
                                                                        style = MaterialTheme.typography.titleSmall,
                                                                        fontWeight = FontWeight.Bold
                                                                    )
                                                                    Text(
                                                                        text = "Pontos: $netRacePoints / 2",
                                                                        style = MaterialTheme.typography.labelSmall,
                                                                        fontWeight = FontWeight.SemiBold,
                                                                        color = pointColor
                                                                    )
                                                                }
                                                                FilledTonalIconButton(
                                                                    onClick = { showTraitSelectDialog = true },
                                                                    modifier = Modifier.size(32.dp)
                                                                ) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.Add,
                                                                        contentDescription = "Adicionar Traço",
                                                                        modifier = Modifier.size(18.dp)
                                                                    )
                                                                }
                                                            }

                                                            if (selectedRacialTraits.isEmpty()) {
                                                                Text(
                                                                    text = "Nenhum traço racial adicionado. O padrão de criação de raças busca fechar em +2 pontos.",
                                                                    style = MaterialTheme.typography.bodySmall,
                                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                                )
                                                            } else {
                                                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                                    selectedRacialTraits.forEach { trait ->
                                                                        Row(
                                                                            modifier = Modifier.fillMaxWidth(),
                                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                                            verticalAlignment = Alignment.CenterVertically
                                                                        ) {
                                                                            Text(
                                                                                text = "• ${trait.nome} (${if (trait.custo > 0) "+${trait.custo}" else "${trait.custo}"} pts)",
                                                                                style = MaterialTheme.typography.bodySmall,
                                                                                fontWeight = FontWeight.Medium,
                                                                                modifier = Modifier.weight(1f)
                                                                            )
                                                                            TextButton(
                                                                                onClick = { selectedRacialTraits = selectedRacialTraits - trait },
                                                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                                                            ) {
                                                                                Text("Remover", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                                "Traço Racial" -> {
                                                    androidx.compose.material3.OutlinedTextField(
                                                        value = customTraitCost,
                                                        onValueChange = { customTraitCost = it },
                                                        label = { Text("Custo em Pontos (ex: 2 para positivo, -1 para negativo)") },
                                                        singleLine = true,
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                }
                                                "Variante de Raça" -> {
                                                    val baseRacaOptions = remember(state.listaAncestralidadesJson) {
                                                        state.listaAncestralidadesJson.distinctBy { it.nome.keyify() }.sortedBy { it.nome }
                                                    }
                                                    val varianteBaseRaca = remember(varianteBaseRacaId, baseRacaOptions) {
                                                        baseRacaOptions.firstOrNull { it.nome.keyify() == varianteBaseRacaId }
                                                    }

                                                    OutlinedButton(
                                                        onClick = { showVarianteBaseRacaDialog = true },
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Text(varianteBaseRaca?.nome ?: "Selecionar Raça Base")
                                                    }

                                                    if (varianteBaseRaca == null) {
                                                        Text(
                                                            text = "Escolha a raça base pra começar a montar a Variante.",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    } else {
                                                        val itensRemoviveis = remember(varianteBaseRaca) {
                                                            com.example.swadebuilder.model.usecase.ResolveVariantPointBudgetUseCase.itensRemoviveisDe(varianteBaseRaca)
                                                        }
                                                        val nHab = varianteBaseRaca.habilidades.size
                                                        val nVant = varianteBaseRaca.vantagensGratis.size
                                                        val habilidadeItems = itensRemoviveis.take(nHab).filter { it.habilidadeId != null }
                                                        val vantagemGratisItems = itensRemoviveis.drop(nHab).take(nVant)
                                                        val desvantagemItems = itensRemoviveis.drop(nHab + nVant)

                                                        val itensRemovidosSelecionados = habilidadeItems.filter { it.habilidadeId in varianteTracosRemovidos } +
                                                            vantagemGratisItems.filter { it.label in varianteVantagensGratisRemovidas } +
                                                            desvantagemItems.filter { it.label in varianteDesvantagensRemovidas }

                                                        val itensAdicionadosSelecionados = buildList {
                                                            varianteTracosAdicionados.forEach { trait ->
                                                                add(com.example.swadebuilder.model.usecase.VariantBudgetItem(label = trait.nome, custo = trait.custo, habilidadeId = trait.nome.lowercase().replace(" ", "_")))
                                                            }
                                                            varianteVantagensAdicionadas.forEach { vid ->
                                                                state.listaVantagens.firstOrNull { it.id == vid }?.let {
                                                                    add(com.example.swadebuilder.model.usecase.ResolveVariantPointBudgetUseCase.vantagemComoItemAdicionado(it))
                                                                }
                                                            }
                                                            varianteComplicacoesAdicionadas.forEach { esc ->
                                                                state.listaComplicacoes.firstOrNull { it.id == esc.complicacaoId }?.let {
                                                                    add(com.example.swadebuilder.model.usecase.ResolveVariantPointBudgetUseCase.complicacaoComoItemAdicionado(it, esc.comoMaior))
                                                                }
                                                            }
                                                        }

                                                        val valorBaseRaca = remember(varianteBaseRaca) {
                                                            com.example.swadebuilder.model.usecase.ResolveVariantPointBudgetUseCase.valorTotalDe(varianteBaseRaca)
                                                        }
                                                        val budgetResult = remember(valorBaseRaca, itensRemovidosSelecionados, itensAdicionadosSelecionados, varianteSemLimite) {
                                                            com.example.swadebuilder.model.usecase.ResolveVariantPointBudgetUseCase().resolve(
                                                                valorBaseRaca, itensRemovidosSelecionados, itensAdicionadosSelecionados, semLimite = varianteSemLimite
                                                            )
                                                        }

                                                        OutlinedCard(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                                                        ) {
                                                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                                val saldoColor = if (budgetResult.dentroDoOrcamento) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                                                Text(
                                                                    text = if (varianteSemLimite) "Pontos da raça: ${budgetResult.saldo} (sem limite)" else "Pontos da raça: ${budgetResult.saldo} / ${budgetResult.orcamento}",
                                                                    style = MaterialTheme.typography.labelSmall,
                                                                    fontWeight = FontWeight.SemiBold,
                                                                    color = saldoColor
                                                                )
                                                                CheckboxRow(
                                                                    label = "Sem limite de pontos (raças mais fortes)",
                                                                    checked = varianteSemLimite,
                                                                    onCheckedChange = { varianteSemLimite = it }
                                                                )

                                                                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                                                                Text("Remover da raça base:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

                                                                if (habilidadeItems.isEmpty() && vantagemGratisItems.isEmpty() && desvantagemItems.isEmpty()) {
                                                                    Text("Esta raça não tem traços removíveis.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                                } else {
                                                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                                        habilidadeItems.forEach { item ->
                                                                            val isSel = item.habilidadeId in varianteTracosRemovidos
                                                                            Row(
                                                                                modifier = Modifier.fillMaxWidth().clickable {
                                                                                    varianteTracosRemovidos = if (isSel) varianteTracosRemovidos - item.habilidadeId!! else varianteTracosRemovidos + item.habilidadeId!!
                                                                                },
                                                                                verticalAlignment = Alignment.CenterVertically
                                                                            ) {
                                                                                Checkbox(checked = isSel, onCheckedChange = {
                                                                                    varianteTracosRemovidos = if (it) varianteTracosRemovidos + item.habilidadeId!! else varianteTracosRemovidos - item.habilidadeId!!
                                                                                })
                                                                                Text("${item.label} (${item.custo})", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                                                            }
                                                                        }
                                                                        vantagemGratisItems.forEach { item ->
                                                                            val isSel = item.label in varianteVantagensGratisRemovidas
                                                                            Row(
                                                                                modifier = Modifier.fillMaxWidth().clickable {
                                                                                    varianteVantagensGratisRemovidas = if (isSel) varianteVantagensGratisRemovidas - item.label else varianteVantagensGratisRemovidas + item.label
                                                                                },
                                                                                verticalAlignment = Alignment.CenterVertically
                                                                            ) {
                                                                                Checkbox(checked = isSel, onCheckedChange = {
                                                                                    varianteVantagensGratisRemovidas = if (it) varianteVantagensGratisRemovidas + item.label else varianteVantagensGratisRemovidas - item.label
                                                                                })
                                                                                Text("${item.label} (${item.custo})", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                                                            }
                                                                        }
                                                                        desvantagemItems.forEach { item ->
                                                                            val isSel = item.label in varianteDesvantagensRemovidas
                                                                            Row(
                                                                                modifier = Modifier.fillMaxWidth().clickable {
                                                                                    varianteDesvantagensRemovidas = if (isSel) varianteDesvantagensRemovidas - item.label else varianteDesvantagensRemovidas + item.label
                                                                                },
                                                                                verticalAlignment = Alignment.CenterVertically
                                                                            ) {
                                                                                Checkbox(checked = isSel, onCheckedChange = {
                                                                                    varianteDesvantagensRemovidas = if (it) varianteDesvantagensRemovidas + item.label else varianteDesvantagensRemovidas - item.label
                                                                                })
                                                                                Text("${item.label} (${item.custo})", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                                                            }
                                                                        }
                                                                    }
                                                                }

                                                                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                                                                Text("Adicionar à Variante:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

                                                                @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
                                                                androidx.compose.foundation.layout.FlowRow(
                                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                                                ) {
                                                                    OutlinedButton(onClick = { showVarianteTraitAddDialog = true }) {
                                                                        Text("+ Traço Racial", style = MaterialTheme.typography.labelSmall)
                                                                    }
                                                                    OutlinedButton(onClick = { showVarianteVantagemAddDialog = true }) {
                                                                        Text("+ Vantagem", style = MaterialTheme.typography.labelSmall)
                                                                    }
                                                                    OutlinedButton(onClick = { showVarianteComplicacaoSeveridadeDialog = true }) {
                                                                        Text("+ Complicação", style = MaterialTheme.typography.labelSmall)
                                                                    }
                                                                }

                                                                if (itensAdicionadosSelecionados.isNotEmpty()) {
                                                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                                        varianteTracosAdicionados.forEach { trait ->
                                                                            Row(
                                                                                modifier = Modifier.fillMaxWidth(),
                                                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                                                verticalAlignment = Alignment.CenterVertically
                                                                            ) {
                                                                                Text("• ${trait.nome} (${if (trait.custo > 0) "+${trait.custo}" else "${trait.custo}"})", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                                                                TextButton(onClick = { varianteTracosAdicionados = varianteTracosAdicionados - trait }, contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)) {
                                                                                    Text("Remover", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                                                                                }
                                                                            }
                                                                        }
                                                                        varianteVantagensAdicionadas.forEach { vid ->
                                                                            val vant = state.listaVantagens.firstOrNull { it.id == vid }
                                                                            Row(
                                                                                modifier = Modifier.fillMaxWidth(),
                                                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                                                verticalAlignment = Alignment.CenterVertically
                                                                            ) {
                                                                                Text("• ${vant?.nome ?: vid}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                                                                TextButton(onClick = { varianteVantagensAdicionadas = varianteVantagensAdicionadas - vid }, contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)) {
                                                                                    Text("Remover", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                                                                                }
                                                                            }
                                                                        }
                                                                        varianteComplicacoesAdicionadas.forEach { esc ->
                                                                            val comp = state.listaComplicacoes.firstOrNull { it.id == esc.complicacaoId }
                                                                            Row(
                                                                                modifier = Modifier.fillMaxWidth(),
                                                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                                                verticalAlignment = Alignment.CenterVertically
                                                                            ) {
                                                                                Text("• ${comp?.name ?: esc.complicacaoId} (${if (esc.comoMaior) "Maior" else "Menor"})", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                                                                TextButton(onClick = { varianteComplicacoesAdicionadas = varianteComplicacoesAdicionadas - esc }, contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)) {
                                                                                    Text("Remover", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
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

                                            // Common Description field
                                            androidx.compose.material3.OutlinedTextField(
                                                value = customItemDesc,
                                                onValueChange = { customItemDesc = it },
                                                label = { Text("Descrição / Efeitos") },
                                                modifier = Modifier.fillMaxWidth().height(90.dp),
                                                maxLines = 4
                                            )
                                        }
                                    }

                                    // Custom Content Items Manager List (todos os livros)
                                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                                    Text(
                                        text = "Itens Customizados (todos os livros)",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )

                                    val allCustomItems = remember(activeBookCustomData) {
                                        buildList {
                                            activeBookCustomData.vantagens.forEach { add("Vantagem" to it.nome) }
                                            activeBookCustomData.complicacoes.forEach { add("Complicação" to it.name) }
                                            activeBookCustomData.equipamentos.forEach { add("Equipamento" to it.nome) }
                                            activeBookCustomData.poderes.forEach { add("Poder" to it.nome) }
                                            activeBookCustomData.superPoderes.forEach { add("Super Poder" to it.nome) }
                                            activeBookCustomData.racas.forEach { add("Raça" to it.nome) }
                                            activeBookCustomData.habilidadesRaciais.forEach { add("Traço Racial" to it.nome) }
                                            activeBookCustomData.variantesRaciais.forEach { add("Variante de Raça" to it.nome) }
                                        }
                                    }

                                    if (allCustomItems.isEmpty()) {
                                        Text(
                                            text = "Nenhum item customizado criado neste livro ainda.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    } else {
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            allCustomItems.forEach { (type, name) ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "[$type] $name ⓒ",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                    TextButton(onClick = {
                                                        // Apaga em TODOS os livros de armazenamento: o mesmo item pode ter
                                                        // sido salvo sob várias tags (ver selectedBookTags na criação), e
                                                        // cada chamada de delete é um no-op inofensivo nos livros onde o
                                                        // item não existe.
                                                        // Sincroniza também state.lista* (não só o disco): sem isso, o item
                                                        // apagado continuava selecionável nas telas de ficha até o app
                                                        // recarregar os dados do zero, e um novo item criado com o mesmo
                                                        // nome logo em seguida seria barrado por falso positivo de colisão.
                                                        when (type) {
                                                            "Vantagem" -> {
                                                                val item = activeBookCustomData.vantagens.firstOrNull { it.nome == name }
                                                                item?.let { i ->
                                                                    todosOsLivrosDeArmazenamento.forEach { customStorageManager.deleteVantagem(context, it, i.id) }
                                                                    state.listaVantagens = state.listaVantagens.filterNot { it.id == i.id }
                                                                }
                                                            }
                                                            "Complicação" -> {
                                                                val item = activeBookCustomData.complicacoes.firstOrNull { it.name == name }
                                                                item?.let { i ->
                                                                    todosOsLivrosDeArmazenamento.forEach { customStorageManager.deleteComplicacao(context, it, i.id) }
                                                                    state.listaComplicacoes = state.listaComplicacoes.filterNot { it.id == i.id }
                                                                }
                                                            }
                                                            "Equipamento" -> {
                                                                todosOsLivrosDeArmazenamento.forEach { customStorageManager.deleteEquipamento(context, it, name) }
                                                                state.listaEquipamentos = state.listaEquipamentos.filterNot { it.nome.equals(name, ignoreCase = true) }
                                                            }
                                                            "Poder" -> {
                                                                val item = activeBookCustomData.poderes.firstOrNull { it.nome == name }
                                                                item?.let { i ->
                                                                    todosOsLivrosDeArmazenamento.forEach { customStorageManager.deletePoder(context, it, i.id) }
                                                                    state.listaPoderes = state.listaPoderes.filterNot { it.id == i.id }
                                                                }
                                                            }
                                                            "Super Poder" -> {
                                                                todosOsLivrosDeArmazenamento.forEach { customStorageManager.deleteSuperPoder(context, it, name) }
                                                                state.listaSuperPoderes = state.listaSuperPoderes.filterNot { it.nome.equals(name, ignoreCase = true) }
                                                            }
                                                            "Raça" -> {
                                                                todosOsLivrosDeArmazenamento.forEach { customStorageManager.deleteRaca(context, it, name) }
                                                                state.listaAncestralidadesJson = state.listaAncestralidadesJson.filterNot { it.nome.equals(name, ignoreCase = true) }
                                                            }
                                                            "Traço Racial" -> todosOsLivrosDeArmazenamento.forEach { customStorageManager.deleteHabilidadeRacial(context, it, name) }
                                                            "Variante de Raça" -> {
                                                                val item = activeBookCustomData.variantesRaciais.firstOrNull { it.nome == name }
                                                                item?.let { i ->
                                                                    todosOsLivrosDeArmazenamento.forEach { customStorageManager.deleteVarianteRacial(context, it, i.id) }
                                                                    state.listaVariantesRaciaisCustom = state.listaVariantesRaciaisCustom.filterNot { v -> v.id == i.id }
                                                                }
                                                            }
                                                        }
                                                        refreshTrigger++
                                                        onCustomContentChanged()
                                                    }) {
                                                        Text("Deletar", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                Column(horizontalAlignment = Alignment.End) {
                                    // Fora da área rolável (diferente de onde estava antes, dentro do
                                    // Column de `text`): fica sempre visível colado nos botões, sem
                                    // precisar rolar a tela pra descobrir se salvou ou por que não salvou.
                                    if (statusMessage != null) {
                                        val isErro = statusMessage!!.let {
                                            it.startsWith("Erro") || it.startsWith("Preencha") ||
                                                it.startsWith("Selecione") || it.contains("precisa fechar")
                                        }
                                        Text(
                                            text = statusMessage!!,
                                            color = if (isErro) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(bottom = 4.dp, end = 4.dp)
                                        )
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    TextButton(onClick = {
                                                val safeDesc = customItemDesc.ifBlank { "-" }
                                                if (customItemName.isNotBlank()) {
                                            // toIdSlug() normaliza acento/espaço/pontuação (ver StringExtensions.kt) — evita
                                            // que "Fogo" e "Fôgo" gerem ids diferentes, ou que "Fogo do Inferno" e
                                            // "fogo   do inferno" colidam sem o app perceber.
                                            val id = "custom:${customItemName.toIdSlug()}"
                                            val normalizedName = customItemName.keyify()
                                            // Livro(s) escolhidos no Seletor de Livros; se nada foi marcado, cai no
                                            // livro atualmente ativo pra não perder a criação.
                                            val tags = selectedBookTags.ifEmpty {
                                                setOf(state.getActiveOrigins().firstOrNull() ?: "BASICO")
                                            }
                                            val tagsLabel = tags.joinToString(", ") {
                                                if (it == com.example.swadebuilder.util.TAG_GERAL) "Geral" else it.toEditionDisplayName()
                                            }
                                            // Bloqueia colisão de nome/id antes de salvar: CustomStorageManager.addXxx()
                                            // sobrescreve silenciosamente por id/nome no disco, enquanto
                                            // CriadorState.addCustomXxx() ignora silenciosamente a nova entrada se o
                                            // id/nome já existir em memória — os dois lados divergiam sem esse guard.
                                            // Compara contra o catálogo oficial + custom ativo (state.lista*) e contra
                                            // TODO o conteúdo customizado já salvo em qualquer livro (activeBookCustomData),
                                            // pra nunca deixar dois itens com o mesmo nome/id coexistirem.
                                            val colisao: String? = when (selectedCategory) {
                                                "Vantagem", "Antecedente Arcano" ->
                                                    if (state.listaVantagens.any { it.id == id } || activeBookCustomData.vantagens.any { it.id == id }) "Vantagem" else null
                                                "Complicação" ->
                                                    if (state.listaComplicacoes.any { it.id == id } || activeBookCustomData.complicacoes.any { it.id == id }) "Complicação" else null
                                                "Equipamento" ->
                                                    if (state.listaEquipamentos.any { it.nome.keyify() == normalizedName } || activeBookCustomData.equipamentos.any { it.nome.keyify() == normalizedName }) "Equipamento" else null
                                                "Poder" ->
                                                    if (state.listaPoderes.any { it.id == id } || activeBookCustomData.poderes.any { it.id == id }) "Poder" else null
                                                "Super Poder" ->
                                                    if (state.listaSuperPoderes.any { it.nome.keyify() == normalizedName } || activeBookCustomData.superPoderes.any { it.nome.keyify() == normalizedName }) "Super Poder" else null
                                                "Raça" ->
                                                    if (state.listaAncestralidadesJson.any { it.nome.keyify() == normalizedName } || activeBookCustomData.racas.any { it.nome.keyify() == normalizedName }) "Raça" else null
                                                "Traço Racial" ->
                                                    if (baseRacialCatalog.any { it.nome.keyify() == normalizedName } || activeBookCustomData.habilidadesRaciais.any { it.nome.keyify() == normalizedName }) "Traço Racial" else null
                                                "Variante de Raça" ->
                                                    if (state.listaVariantesRaciaisCustom.any { it.id == id } || activeBookCustomData.variantesRaciais.any { it.id == id }) "Variante de Raça" else null
                                                else -> null
                                            }
                                            if (colisao != null) {
                                                statusMessage = "Erro: já existe um(a) $colisao chamado(a) '$customItemName'. Escolha outro nome."
                                            } else {
                                            when (selectedCategory) {
                                                "Vantagem" -> {
                                                    val combinedPrevEdges = customPrereqEdges + customPrereqComps
                                                    val reqObj = Requisito(
                                                        estagio = customStage,
                                                        atributoMin = customAttrMin,
                                                        periciaMin = customSkillMin,
                                                        vantagensPrevias = combinedPrevEdges,
                                                        observacoes = customRequirements
                                                    )
                                                    val newAdv = com.example.swadebuilder.model.Vantagem(
                                                        id = id,
                                                        nome = customItemName,
                                                        categoria = customAdvCategory,
                                                        descricao = safeDesc,
                                                        origem = tags.first(),
                                                        requisitos = reqObj
                                                    )
                                                    tags.forEach { tag -> customStorageManager.addVantagem(context, tag, newAdv.copy(origem = tag)) }
                                                    state.addCustomVantagem(newAdv)
                                                            statusMessage = "Vantagem '$customItemName' salva em: $tagsLabel"
                                                }
                                                "Complicação" -> {
                                                    val newComp = com.example.swadebuilder.model.Complicacao(
                                                        id = id,
                                                        name = customItemName,
                                                        severity = customSeverity,
                                                                description = safeDesc,
                                                                origem = tags.first()
                                                    )
                                                            tags.forEach { tag -> customStorageManager.addComplicacao(context, tag, newComp.copy(origem = tag)) }
                                                    state.addCustomComplicacao(newComp)
                                                            statusMessage = "Complicação '$customItemName' salva em: $tagsLabel"
                                                }
                                                "Equipamento" -> {
                                                    val newEquip = com.example.swadebuilder.model.EquipamentoItem(
                                                        nome = customItemName,
                                                        custo = kotlinx.serialization.json.JsonPrimitive(customCost.toIntOrNull() ?: 0),
                                                        peso = kotlinx.serialization.json.JsonPrimitive(customWeight.toFloatOrNull() ?: 0f),
                                                        dano = if (customDamage.isNotBlank()) kotlinx.serialization.json.JsonPrimitive(customDamage) else null,
                                                                observacoes = kotlinx.serialization.json.JsonPrimitive(safeDesc),
                                                                origem = tags.first(),
                                                                subtipo = customEquipSubtype,
                                                                id = id
                                                    )
                                                            tags.forEach { tag -> customStorageManager.addEquipamento(context, tag, newEquip.copy(origem = tag)) }
                                                    state.addCustomEquipamento(newEquip)
                                                            statusMessage = "Equipamento '$customItemName' salvo em: $tagsLabel"
                                                }
                                                "Poder" -> {
                                                    val newPoder = com.example.swadebuilder.model.Poder(
                                                        id = id,
                                                        nome = customItemName,
                                                        pontosDePoder = customPp.ifBlank { "1" },
                                                        distancia = customRange.ifBlank { "Toque" },
                                                        duracao = customDuration.ifBlank { "3 turnos" },
                                                                descricao = safeDesc,
                                                        estagio = "Novato",
                                                                origem = tags.first()
                                                    )
                                                            tags.forEach { tag -> customStorageManager.addPoder(context, tag, newPoder.copy(origem = tag)) }
                                                    state.addCustomPoder(newPoder)
                                                            statusMessage = "Poder '$customItemName' salvo em: $tagsLabel"
                                                }
                                                "Super Poder" -> {
                                                    val modificadoresList = customSuperPoderModificadores
                                                        .lines()
                                                        .map { it.trim() }
                                                        .filter { it.isNotBlank() }
                                                    val newSuperPoder = com.example.swadebuilder.model.SuperPoder(
                                                        nome = customItemName,
                                                        custoBase = customSuperPoderCustoBase.ifBlank { "2" },
                                                        descricao = safeDesc,
                                                        modificadores = modificadoresList.ifEmpty { null },
                                                        id = id
                                                    )
                                                    tags.forEach { tag -> customStorageManager.addSuperPoder(context, tag, newSuperPoder) }
                                                    state.addCustomSuperPoder(newSuperPoder)
                                                    statusMessage = "Super Poder '$customItemName' salvo em: $tagsLabel"
                                                }
                                                "Antecedente Arcano" -> {
                                                    // Vira uma Vantagem categoria ANTECEDENTE de verdade — reaproveita
                                                    // 100% do pipeline de Vantagem customizada (armazenamento, merge,
                                                    // seleção na ficha). O nome "ANTECEDENTE ARCANO (X)" é reconhecido
                                                    // por Vantagem.toArcanoKey() (ver ArcanoExtensions.kt, fallback
                                                    // adicionado pra AAs customizados), e poderesPermitidos vazio =
                                                    // lista aberta (todos os poderes do `origem`), ou preenchido =
                                                    // só os poderes escolhidos — os dois lidos nativamente por
                                                    // PoderesSection.kt sem precisar de nenhum código novo lá.
                                                    val newAA = com.example.swadebuilder.model.Vantagem(
                                                        id = id,
                                                        nome = "ANTECEDENTE ARCANO ($customItemName)",
                                                        categoria = Categoria.ANTECEDENTE,
                                                        descricao = safeDesc,
                                                        origem = tags.first(),
                                                        requisitos = Requisito(estagio = "Novato"),
                                                        poderesPermitidos = if (customAaListaAberta) emptyList() else customAaPoderesEspecificos.toList()
                                                    )
                                                    tags.forEach { tag -> customStorageManager.addVantagem(context, tag, newAA.copy(origem = tag)) }
                                                    state.addCustomVantagem(newAA)
                                                    statusMessage = "Antecedente Arcano '$customItemName' salvo em: $tagsLabel"
                                                }
                                                "Raça" -> {
                                                    val raceAbilities = if (selectedRacialTraits.isNotEmpty()) {
                                                        selectedRacialTraits.map { trait ->
                                                            com.example.swadebuilder.model.RacialAbility(
                                                                nome = trait.nome,
                                                                descricao = trait.descricao,
                                                                // Id real do catálogo, nunca derivado do nome de
                                                                // exibição — ver o mesmo ajuste em CriadorState.kt
                                                                // (applyCustomAncestryVariantIfSelected).
                                                                id = trait.id ?: trait.nome.toIdSlug(),
                                                                category = if (trait.custo >= 0) "racial_trait_positive" else "racial_trait_negative",
                                                                vezes = trait.vezes
                                                            )
                                                        }
                                                    } else {
                                                        listOf(
                                                            com.example.swadebuilder.model.RacialAbility(
                                                                nome = "Traço Customizado",
                                                                descricao = safeDesc,
                                                                id = "${id}_trait",
                                                                category = "racial_trait_positive"
                                                            )
                                                        )
                                                    }
                                                    val newRace = com.example.swadebuilder.model.RacialModifier(
                                                        id = id,
                                                        nome = customItemName,
                                                        descricao = safeDesc,
                                                        atributos = emptyMap(),
                                                        pericias = emptyMap(),
                                                        origem = tags.first(),
                                                        habilidades = raceAbilities
                                                    )
                                                    tags.forEach { tag -> customStorageManager.addRaca(context, tag, newRace.copy(origem = tag)) }
                                                    state.listaAncestralidadesJson = state.listaAncestralidadesJson + newRace
                                                    statusMessage = "Raça '$customItemName' salva em: $tagsLabel"
                                                }
                                                "Traço Racial" -> {
                                                    val costInt = customTraitCost.toIntOrNull() ?: 1
                                                    val newTrait = com.example.swadebuilder.model.HabilidadeCriacao(
                                                        nome = customItemName,
                                                        custo = costInt,
                                                        descricao = safeDesc,
                                                        id = id
                                                    )
                                                    tags.forEach { tag -> customStorageManager.addHabilidadeRacial(context, tag, newTrait) }
                                                    statusMessage = "Traço racial '$customItemName' salvo em: $tagsLabel"
                                                }
                                                "Variante de Raça" -> {
                                                    val baseRaca = varianteBaseRacaId?.let { bid -> state.listaAncestralidadesJson.firstOrNull { it.nome.keyify() == bid } }
                                                    if (baseRaca == null) {
                                                        statusMessage = "Selecione a raça base da Variante."
                                                    } else {
                                                        val itensRemoviveis = com.example.swadebuilder.model.usecase.ResolveVariantPointBudgetUseCase.itensRemoviveisDe(baseRaca)
                                                        val nHab = baseRaca.habilidades.size
                                                        val nVant = baseRaca.vantagensGratis.size
                                                        val habilidadeItems = itensRemoviveis.take(nHab).filter { it.habilidadeId != null }
                                                        val vantagemGratisItems = itensRemoviveis.drop(nHab).take(nVant)
                                                        val desvantagemItems = itensRemoviveis.drop(nHab + nVant)

                                                        val itensRemovidosSelecionados = habilidadeItems.filter { it.habilidadeId in varianteTracosRemovidos } +
                                                            vantagemGratisItems.filter { it.label in varianteVantagensGratisRemovidas } +
                                                            desvantagemItems.filter { it.label in varianteDesvantagensRemovidas }

                                                        val itensAdicionadosSelecionados = buildList {
                                                            varianteTracosAdicionados.forEach { trait ->
                                                                add(com.example.swadebuilder.model.usecase.VariantBudgetItem(label = trait.nome, custo = trait.custo, habilidadeId = trait.nome.lowercase().replace(" ", "_")))
                                                            }
                                                            varianteVantagensAdicionadas.forEach { vid ->
                                                                state.listaVantagens.firstOrNull { it.id == vid }?.let {
                                                                    add(com.example.swadebuilder.model.usecase.ResolveVariantPointBudgetUseCase.vantagemComoItemAdicionado(it))
                                                                }
                                                            }
                                                            varianteComplicacoesAdicionadas.forEach { esc ->
                                                                state.listaComplicacoes.firstOrNull { it.id == esc.complicacaoId }?.let {
                                                                    add(com.example.swadebuilder.model.usecase.ResolveVariantPointBudgetUseCase.complicacaoComoItemAdicionado(it, esc.comoMaior))
                                                                }
                                                            }
                                                        }

                                                        val valorBaseRaca = com.example.swadebuilder.model.usecase.ResolveVariantPointBudgetUseCase.valorTotalDe(baseRaca)
                                                        val budgetResult = com.example.swadebuilder.model.usecase.ResolveVariantPointBudgetUseCase().resolve(
                                                            valorBaseRaca, itensRemovidosSelecionados, itensAdicionadosSelecionados, semLimite = varianteSemLimite
                                                        )

                                                        if (!budgetResult.dentroDoOrcamento) {
                                                            statusMessage = "A Variante precisa fechar em exatamente ${budgetResult.orcamento} pontos (pontos atuais: ${budgetResult.saldo}), ou marque 'Sem limite de pontos'."
                                                        } else {
                                                            val newVariant = com.example.swadebuilder.model.CustomAncestryVariant(
                                                                id = id,
                                                                ancestralidadeId = varianteBaseRacaId!!,
                                                                nome = customItemName,
                                                                descricao = safeDesc,
                                                                tracosRemovidosIds = varianteTracosRemovidos,
                                                                vantagensGratisRemovidas = varianteVantagensGratisRemovidas,
                                                                desvantagensRemovidas = varianteDesvantagensRemovidas,
                                                                tracosAdicionados = varianteTracosAdicionados,
                                                                vantagensAdicionadasIds = varianteVantagensAdicionadas,
                                                                complicacoesAdicionadas = varianteComplicacoesAdicionadas,
                                                                semLimiteDePontos = varianteSemLimite
                                                            )
                                                            tags.forEach { tag -> customStorageManager.addVarianteRacial(context, tag, newVariant) }
                                                            state.listaVariantesRaciaisCustom = state.listaVariantesRaciaisCustom + newVariant
                                                            statusMessage = "Variante '$customItemName' salva em: $tagsLabel"
                                                            varianteBaseRacaId = null
                                                            varianteTracosRemovidos = emptyList()
                                                            varianteVantagensGratisRemovidas = emptyList()
                                                            varianteDesvantagensRemovidas = emptyList()
                                                            varianteTracosAdicionados = emptyList()
                                                            varianteVantagensAdicionadas = emptyList()
                                                            varianteComplicacoesAdicionadas = emptyList()
                                                            varianteSemLimite = false
                                                        }
                                                    }
                                                }
                                            }
                                                    refreshTrigger++
                                            onCustomContentChanged()
                                            customItemName = ""
                                            customItemDesc = ""
                                            customRequirements = ""
                                            customAttrMin = emptyMap()
                                            customSkillMin = emptyMap()
                                            customPrereqEdges = emptyList()
                                            customPrereqComps = emptyList()
                                            customDamage = ""
                                            customRacialTrait = ""
                                            }
                                        } else {
                                                    statusMessage = "Preencha o Nome do item."
                                        }
                                            }) { Text("Salvar Item") }
                                    TextButton(onClick = { showCustomContentDialog = false }) { Text("Fechar") }
                                }
                                }
                            }
                        )

                        // Requirement Selector Modals
                        if (showAttrDialog) {
                            val attrs = listOf("AGILIDADE" to "Agilidade", "ASTUCIA" to "Astúcia", "ESPIRITO" to "Espírito", "FORCA" to "Força", "VIGOR" to "Vigor")
                            val steps = listOf(0, 4, 6, 8, 10, 12, 13)
                            AlertDialog(
                                onDismissRequest = { showAttrDialog = false },
                                title = { Text("Atributos Mínimos") },
                                text = {
                                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                        attrs.forEach { (key, name) ->
                                            val currentDie = customAttrMin[key] ?: 0
                                            val currentIndex = steps.indexOf(currentDie).coerceAtLeast(0)
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = name,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    IconButton(
                                                        onClick = {
                                                            if (currentIndex > 0) {
                                                                val newDie = steps[currentIndex - 1]
                                                                val mut = customAttrMin.toMutableMap()
                                                                if (newDie == 0) mut.remove(key) else mut[key] = newDie
                                                                customAttrMin = mut
                                                            }
                                                        },
                                                        enabled = currentIndex > 0
                                                    ) {
                                                        Icon(Icons.Default.Remove, contentDescription = "Diminuir")
                                                    }
                                                    Text(
                                                        text = currentDie.toDiceString(),
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        modifier = Modifier.width(48.dp),
                                                        textAlign = TextAlign.Center
                                                    )
                                                    IconButton(
                                                        onClick = {
                                                            if (currentIndex < steps.lastIndex) {
                                                                val newDie = steps[currentIndex + 1]
                                                                val mut = customAttrMin.toMutableMap()
                                                                mut[key] = newDie
                                                                customAttrMin = mut
                                                            }
                                                        },
                                                        enabled = currentIndex < steps.lastIndex
                                                    ) {
                                                        Icon(Icons.Default.Add, contentDescription = "Aumentar")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                                confirmButton = { TextButton(onClick = { showAttrDialog = false }) { Text("OK") } }
                            )
                        }

                        if (showSkillDialog) {
                            val allSkillsList = state.listaPericias.map { it.nome }.distinct().sorted()
                            val steps = listOf(0, 4, 6, 8, 10, 12, 13)
                            var filterSkillText by remember { mutableStateOf("") }
                            AlertDialog(
                                onDismissRequest = { showSkillDialog = false },
                                title = { Text("Perícias Mínimas") },
                                text = {
                                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                        androidx.compose.material3.OutlinedTextField(
                                            value = filterSkillText,
                                            onValueChange = { filterSkillText = it },
                                            label = { Text("Filtrar Perícia") },
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                        )
                                        allSkillsList.filter { it.contains(filterSkillText, ignoreCase = true) }.forEach { skillName ->
                                            val currentDie = customSkillMin[skillName] ?: 0
                                            val currentIndex = steps.indexOf(currentDie).coerceAtLeast(0)
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = skillName,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    IconButton(
                                                        onClick = {
                                                            if (currentIndex > 0) {
                                                                val newDie = steps[currentIndex - 1]
                                                                val mut = customSkillMin.toMutableMap()
                                                                if (newDie == 0) mut.remove(skillName) else mut[skillName] = newDie
                                                                customSkillMin = mut
                                                            }
                                                        },
                                                        enabled = currentIndex > 0
                                                    ) {
                                                        Icon(Icons.Default.Remove, contentDescription = "Diminuir")
                                                    }
                                                    Text(
                                                        text = currentDie.toDiceString(),
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        modifier = Modifier.width(48.dp),
                                                        textAlign = TextAlign.Center
                                                    )
                                                    IconButton(
                                                        onClick = {
                                                            if (currentIndex < steps.lastIndex) {
                                                                val newDie = steps[currentIndex + 1]
                                                                val mut = customSkillMin.toMutableMap()
                                                                mut[skillName] = newDie
                                                                customSkillMin = mut
                                                            }
                                                        },
                                                        enabled = currentIndex < steps.lastIndex
                                                    ) {
                                                        Icon(Icons.Default.Add, contentDescription = "Aumentar")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                                confirmButton = { TextButton(onClick = { showSkillDialog = false }) { Text("OK") } }
                            )
                        }

                        if (showEdgeDialog) {
                            val availableEdges = state.listaVantagens.distinctBy { it.id }.sortedBy { it.nome }
                            var filterEdgeText by remember { mutableStateOf("") }
                            AlertDialog(
                                onDismissRequest = { showEdgeDialog = false },
                                title = { Text("Vantagens Prévias") },
                                text = {
                                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                        androidx.compose.material3.OutlinedTextField(
                                            value = filterEdgeText,
                                            onValueChange = { filterEdgeText = it },
                                            label = { Text("Filtrar Vantagem") },
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                        )
                                        availableEdges.filter { it.nome.contains(filterEdgeText, ignoreCase = true) }.forEach { edge ->
                                            val isSel = edge.id in customPrereqEdges
                                            Row(
                                                modifier = Modifier.fillMaxWidth().clickable {
                                                    customPrereqEdges = if (isSel) customPrereqEdges - edge.id else customPrereqEdges + edge.id
                                                }.padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Checkbox(checked = isSel, onCheckedChange = {
                                                    customPrereqEdges = if (it) customPrereqEdges + edge.id else customPrereqEdges - edge.id
                                                })
                                                Spacer(Modifier.width(8.dp))
                                                Text(edge.nome, style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                    }
                                },
                                confirmButton = { TextButton(onClick = { showEdgeDialog = false }) { Text("OK") } }
                            )
                        }

                        if (showCompDialog) {
                            val availableComps = state.listaComplicacoes.distinctBy { it.id }.sortedBy { it.name }
                            var filterCompText by remember { mutableStateOf("") }
                            AlertDialog(
                                onDismissRequest = { showCompDialog = false },
                                title = { Text("Complicações Mínimas") },
                                text = {
                                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                        androidx.compose.material3.OutlinedTextField(
                                            value = filterCompText,
                                            onValueChange = { filterCompText = it },
                                            label = { Text("Filtrar Complicação") },
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                        )
                                        availableComps.filter { it.name.contains(filterCompText, ignoreCase = true) }.forEach { comp ->
                                            val isSel = comp.id in customPrereqComps
                                            Row(
                                                modifier = Modifier.fillMaxWidth().clickable {
                                                    customPrereqComps = if (isSel) customPrereqComps - comp.id else customPrereqComps + comp.id
                                                }.padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Checkbox(checked = isSel, onCheckedChange = {
                                                    customPrereqComps = if (it) customPrereqComps + comp.id else customPrereqComps - comp.id
                                                })
                                                Spacer(Modifier.width(8.dp))
                                                Text(comp.name, style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                    }
                                },
                                confirmButton = { TextButton(onClick = { showCompDialog = false }) { Text("OK") } }
                            )
                        }

                        if (showTraitSelectDialog) {
                            val allTraitsCatalog = fullTraitsCatalog.distinctBy { it.grupoEscolha ?: it.nome }
                            var filterTraitText by remember { mutableStateOf("") }
                            AlertDialog(
                                onDismissRequest = { showTraitSelectDialog = false },
                                title = { Text("Selecionar Traços Raciais") },
                                text = {
                                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                        androidx.compose.material3.OutlinedTextField(
                                            value = filterTraitText,
                                            onValueChange = { filterTraitText = it },
                                            label = { Text("Filtrar Traço") },
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                        )
                                        allTraitsCatalog.filter { it.nome.contains(filterTraitText, ignoreCase = true) }.forEach { trait ->
                                            val isSuperPoderesRow = trait.nome == "Super Poderes"
                                            val isPericiaChoiceRow = trait.id in periciaChoiceTraitIds
                                            val isStackableRow = (trait.vezesMax ?: 1) > 1
                                            val isGrupoEscolhaRow = trait.grupoEscolha != null
                                            val isSel = if (isSuperPoderesRow) {
                                                selectedRacialTraits.any { it.nome.startsWith("Super Poderes (") }
                                            } else if (isGrupoEscolhaRow) {
                                                selectedRacialTraits.any { it.grupoEscolha == trait.grupoEscolha }
                                            } else if (isPericiaChoiceRow || isStackableRow) {
                                                selectedRacialTraits.any { it.id == trait.id }
                                            } else {
                                                selectedRacialTraits.any { it.nome.equals(trait.nome, ignoreCase = true) }
                                            }
                                            val onToggle: (Boolean) -> Unit = { checked ->
                                                if (isSuperPoderesRow) {
                                                    if (checked) {
                                                        superPoderRacialPickerTarget = { escolhido ->
                                                            selectedRacialTraits = selectedRacialTraits + escolhido
                                                        }
                                                    } else {
                                                        selectedRacialTraits = selectedRacialTraits.filterNot { it.nome.startsWith("Super Poderes (") }
                                                    }
                                                } else if (isGrupoEscolhaRow) {
                                                    if (checked) {
                                                        groupPickerTarget = trait to { escolhido ->
                                                            selectedRacialTraits = selectedRacialTraits + escolhido
                                                        }
                                                    } else {
                                                        selectedRacialTraits = selectedRacialTraits.filterNot { it.grupoEscolha == trait.grupoEscolha }
                                                    }
                                                } else if (isStackableRow) {
                                                    if (checked) {
                                                        stackPickerTarget = trait to { escolhido ->
                                                            selectedRacialTraits = selectedRacialTraits + escolhido
                                                        }
                                                    } else {
                                                        selectedRacialTraits = selectedRacialTraits.filterNot { it.id == trait.id }
                                                    }
                                                } else if (isPericiaChoiceRow) {
                                                    if (checked) {
                                                        periciaTraitPickerTarget = trait to { escolhido ->
                                                            selectedRacialTraits = selectedRacialTraits + escolhido
                                                        }
                                                    } else {
                                                        selectedRacialTraits = selectedRacialTraits.filterNot { it.id == trait.id }
                                                    }
                                                } else {
                                                    selectedRacialTraits = if (checked) selectedRacialTraits + trait else selectedRacialTraits.filterNot { it.nome.equals(trait.nome, ignoreCase = true) }
                                                }
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth().clickable { onToggle(!isSel) }.padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Checkbox(checked = isSel, onCheckedChange = onToggle)
                                                Spacer(Modifier.width(8.dp))
                                                Column {
                                                    val jaEscolhido = if (isGrupoEscolhaRow) {
                                                        selectedRacialTraits.firstOrNull { it.grupoEscolha == trait.grupoEscolha }
                                                    } else {
                                                        selectedRacialTraits.firstOrNull { it.id == trait.id }
                                                    }
                                                    val rotuloCusto = if ((isStackableRow || isGrupoEscolhaRow) && jaEscolhido != null) {
                                                        "${jaEscolhido.nome} (${if (jaEscolhido.custo > 0) "+${jaEscolhido.custo}" else "${jaEscolhido.custo}"} pts)"
                                                    } else {
                                                        "${trait.nome} (${if (trait.custo > 0) "+${trait.custo}" else "${trait.custo}"} pts)"
                                                    }
                                                    Text(rotuloCusto, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                                    if (trait.descricao.isNotBlank()) {
                                                        Text(trait.descricao, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                                confirmButton = { TextButton(onClick = { showTraitSelectDialog = false }) { Text("OK") } }
                            )
                        }

                        if (showVarianteBaseRacaDialog) {
                            val baseRacaOptions = state.listaAncestralidadesJson.distinctBy { it.nome.keyify() }.sortedBy { it.nome }
                            var filterBaseRacaText by remember { mutableStateOf("") }
                            AlertDialog(
                                onDismissRequest = { showVarianteBaseRacaDialog = false },
                                title = { Text("Raça Base da Variante") },
                                text = {
                                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                        androidx.compose.material3.OutlinedTextField(
                                            value = filterBaseRacaText,
                                            onValueChange = { filterBaseRacaText = it },
                                            label = { Text("Filtrar Raça") },
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                        )
                                        baseRacaOptions.filter { it.nome.contains(filterBaseRacaText, ignoreCase = true) }.forEach { raca ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth().clickable {
                                                    varianteBaseRacaId = raca.nome.keyify()
                                                    varianteTracosRemovidos = emptyList()
                                                    varianteVantagensGratisRemovidas = emptyList()
                                                    varianteDesvantagensRemovidas = emptyList()
                                                    showVarianteBaseRacaDialog = false
                                                }.padding(vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(raca.nome, style = MaterialTheme.typography.bodyMedium)
                                            }
                                        }
                                    }
                                },
                                confirmButton = { TextButton(onClick = { showVarianteBaseRacaDialog = false }) { Text("Fechar") } }
                            )
                        }

                        if (showVarianteTraitAddDialog) {
                            val allVarianteTraitsCatalog = fullTraitsCatalog.distinctBy { it.grupoEscolha ?: it.nome }
                            var filterVarianteTraitText by remember { mutableStateOf("") }
                            AlertDialog(
                                onDismissRequest = { showVarianteTraitAddDialog = false },
                                title = { Text("Adicionar Traço Racial") },
                                text = {
                                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                        androidx.compose.material3.OutlinedTextField(
                                            value = filterVarianteTraitText,
                                            onValueChange = { filterVarianteTraitText = it },
                                            label = { Text("Filtrar Traço") },
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                        )
                                        allVarianteTraitsCatalog.filter { it.nome.contains(filterVarianteTraitText, ignoreCase = true) }.forEach { trait ->
                                            val isSuperPoderesRow = trait.nome == "Super Poderes"
                                            val isPericiaChoiceRow = trait.id in periciaChoiceTraitIds
                                            val isStackableRow = (trait.vezesMax ?: 1) > 1
                                            val isGrupoEscolhaRow = trait.grupoEscolha != null
                                            val isSel = if (isSuperPoderesRow) {
                                                varianteTracosAdicionados.any { it.nome.startsWith("Super Poderes (") }
                                            } else if (isGrupoEscolhaRow) {
                                                varianteTracosAdicionados.any { it.grupoEscolha == trait.grupoEscolha }
                                            } else if (isPericiaChoiceRow || isStackableRow) {
                                                varianteTracosAdicionados.any { it.id == trait.id }
                                            } else {
                                                varianteTracosAdicionados.any { it.nome.equals(trait.nome, ignoreCase = true) }
                                            }
                                            val onToggle: (Boolean) -> Unit = { checked ->
                                                if (isSuperPoderesRow) {
                                                    if (checked) {
                                                        superPoderRacialPickerTarget = { escolhido ->
                                                            varianteTracosAdicionados = varianteTracosAdicionados + escolhido
                                                        }
                                                    } else {
                                                        varianteTracosAdicionados = varianteTracosAdicionados.filterNot { it.nome.startsWith("Super Poderes (") }
                                                    }
                                                } else if (isGrupoEscolhaRow) {
                                                    if (checked) {
                                                        groupPickerTarget = trait to { escolhido ->
                                                            varianteTracosAdicionados = varianteTracosAdicionados + escolhido
                                                        }
                                                    } else {
                                                        varianteTracosAdicionados = varianteTracosAdicionados.filterNot { it.grupoEscolha == trait.grupoEscolha }
                                                    }
                                                } else if (isStackableRow) {
                                                    if (checked) {
                                                        stackPickerTarget = trait to { escolhido ->
                                                            varianteTracosAdicionados = varianteTracosAdicionados + escolhido
                                                        }
                                                    } else {
                                                        varianteTracosAdicionados = varianteTracosAdicionados.filterNot { it.id == trait.id }
                                                    }
                                                } else if (isPericiaChoiceRow) {
                                                    if (checked) {
                                                        periciaTraitPickerTarget = trait to { escolhido ->
                                                            varianteTracosAdicionados = varianteTracosAdicionados + escolhido
                                                        }
                                                    } else {
                                                        varianteTracosAdicionados = varianteTracosAdicionados.filterNot { it.id == trait.id }
                                                    }
                                                } else {
                                                    varianteTracosAdicionados = if (checked) varianteTracosAdicionados + trait else varianteTracosAdicionados.filterNot { it.nome.equals(trait.nome, ignoreCase = true) }
                                                }
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth().clickable { onToggle(!isSel) }.padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Checkbox(checked = isSel, onCheckedChange = onToggle)
                                                Spacer(Modifier.width(8.dp))
                                                Column {
                                                    val jaEscolhido = if (isGrupoEscolhaRow) {
                                                        varianteTracosAdicionados.firstOrNull { it.grupoEscolha == trait.grupoEscolha }
                                                    } else {
                                                        varianteTracosAdicionados.firstOrNull { it.id == trait.id }
                                                    }
                                                    val rotuloCusto = if ((isStackableRow || isGrupoEscolhaRow) && jaEscolhido != null) {
                                                        "${jaEscolhido.nome} (${if (jaEscolhido.custo > 0) "+${jaEscolhido.custo}" else "${jaEscolhido.custo}"} pts)"
                                                    } else {
                                                        "${trait.nome} (${if (trait.custo > 0) "+${trait.custo}" else "${trait.custo}"} pts)"
                                                    }
                                                    Text(rotuloCusto, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                                    if (trait.descricao.isNotBlank()) {
                                                        Text(trait.descricao, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                                confirmButton = { TextButton(onClick = { showVarianteTraitAddDialog = false }) { Text("OK") } }
                            )
                        }

                        superPoderRacialPickerTarget?.let { onEscolhido ->
                            var filterSuperPoderText by remember { mutableStateOf("") }
                            AlertDialog(
                                onDismissRequest = { superPoderRacialPickerTarget = null },
                                title = { Text("Escolher Super Poder") },
                                text = {
                                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                        Text(
                                            "O traço Super Poderes custa 2 pontos pelo Antecedente Arcano (Super Poderes) mais o custo do poder escolhido.",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                        androidx.compose.material3.OutlinedTextField(
                                            value = filterSuperPoderText,
                                            onValueChange = { filterSuperPoderText = it },
                                            label = { Text("Filtrar Super Poder") },
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                        )
                                        superPoderesCatalog
                                            .filter { it.nome.contains(filterSuperPoderText, ignoreCase = true) }
                                            .forEach { poder ->
                                                val custoPoder = primeiroCustoSuperPoder(poder.custoBase)
                                                val custoTotal = 2 + custoPoder
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().clickable {
                                                        onEscolhido(
                                                            com.example.swadebuilder.model.HabilidadeCriacao(
                                                                nome = "Super Poderes (${poder.nome})",
                                                                custo = custoTotal,
                                                                descricao = "Antecedente Arcano (Super Poderes) + poder \"${poder.nome}\" (custo base ${poder.custoBase ?: custoPoder}).",
                                                                descricaoLite = "Concede o Antecedente Arcano (Super Poderes) e o poder \"${poder.nome}\" do Compêndio de Super Poderes."
                                                            )
                                                        )
                                                        superPoderRacialPickerTarget = null
                                                    }.padding(vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column {
                                                        Text("${poder.nome} (2+$custoPoder = $custoTotal pts)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                                        if (!poder.descricao.isNullOrBlank()) {
                                                            Text(poder.descricao, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                                                        }
                                                    }
                                                }
                                            }
                                    }
                                },
                                confirmButton = { TextButton(onClick = { superPoderRacialPickerTarget = null }) { Text("Cancelar") } }
                            )
                        }

                        periciaTraitPickerTarget?.let { (trait, onEscolhido) ->
                            var filterPericiaTraitText by remember { mutableStateOf("") }
                            AlertDialog(
                                onDismissRequest = { periciaTraitPickerTarget = null },
                                title = { Text("Escolher Perícia") },
                                text = {
                                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                        Text(
                                            "${trait.nome} — a que se aplica ao teste da perícia é decidida à mesa; aqui só registramos qual perícia foi escolhida pra este traço.",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                        androidx.compose.material3.OutlinedTextField(
                                            value = filterPericiaTraitText,
                                            onValueChange = { filterPericiaTraitText = it },
                                            label = { Text("Filtrar Perícia") },
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                        )
                                        state.listaPericias
                                            .distinctBy { it.nome }
                                            .sortedBy { it.nome }
                                            .filter { it.nome.contains(filterPericiaTraitText, ignoreCase = true) }
                                            .forEach { pericia ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().clickable {
                                                        onEscolhido(
                                                            com.example.swadebuilder.model.HabilidadeCriacao(
                                                                nome = "${trait.nome}: ${pericia.nome}",
                                                                custo = trait.custo,
                                                                descricao = "${trait.descricao} Perícia escolhida: ${pericia.nome}.",
                                                                descricaoLite = trait.descricaoLite,
                                                                id = trait.id
                                                            )
                                                        )
                                                        periciaTraitPickerTarget = null
                                                    }.padding(vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(pericia.nome, style = MaterialTheme.typography.bodyMedium)
                                                }
                                            }
                                    }
                                },
                                confirmButton = { TextButton(onClick = { periciaTraitPickerTarget = null }) { Text("Cancelar") } }
                            )
                        }

                        stackPickerTarget?.let { (trait, onEscolhido) ->
                            val vezesMax = trait.vezesMax?.takeIf { it > 0 } ?: 1
                            AlertDialog(
                                onDismissRequest = { stackPickerTarget = null },
                                title = { Text("Quantas vezes?") },
                                text = {
                                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                        Text(
                                            trait.nome,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (trait.descricao.isNotBlank()) {
                                            Text(
                                                trait.descricao,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(bottom = 8.dp)
                                            )
                                        }
                                        Text(
                                            "O livro permite comprar este traço até $vezesMax ${if (vezesMax == 1) "vez" else "vezes"} — cada compra soma o efeito de novo.",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                        (1..vezesMax).forEach { n ->
                                            val custoTotal = trait.custo * n
                                            val rotulo = com.example.swadebuilder.model.RacialTraitPointCatalog.labelComVezes(trait.id, n)
                                            Row(
                                                modifier = Modifier.fillMaxWidth().clickable {
                                                    onEscolhido(
                                                        trait.copy(
                                                            nome = rotulo,
                                                            custo = custoTotal,
                                                            vezes = n
                                                        )
                                                    )
                                                    stackPickerTarget = null
                                                }.padding(vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    "${n}x — $rotulo (${if (custoTotal > 0) "+$custoTotal" else "$custoTotal"} pts)",
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            }
                                        }
                                    }
                                },
                                confirmButton = { TextButton(onClick = { stackPickerTarget = null }) { Text("Cancelar") } }
                            )
                        }

                        groupPickerTarget?.let { (trait, onEscolhido) ->
                            val opcoes = fullTraitsCatalog
                                .filter { it.grupoEscolha == trait.grupoEscolha }
                                .sortedBy { it.custo }
                            AlertDialog(
                                onDismissRequest = { groupPickerTarget = null },
                                title = { Text("Qual versão?") },
                                text = {
                                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                        Text(
                                            "O livro traz mais de uma versão deste traço, cada uma com seu próprio custo — escolha uma (pode ser trocada removendo e adicionando de novo).",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                        opcoes.forEach { opcao ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth().clickable {
                                                    onEscolhido(opcao)
                                                    groupPickerTarget = null
                                                }.padding(vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(
                                                        "${opcao.nome} (${if (opcao.custo > 0) "+${opcao.custo}" else "${opcao.custo}"} pts)",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    if (opcao.descricao.isNotBlank()) {
                                                        Text(
                                                            opcao.descricao,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                                confirmButton = { TextButton(onClick = { groupPickerTarget = null }) { Text("Cancelar") } }
                            )
                        }

                        if (showAaPoderesPickerDialog) {
                            val availablePoderes = state.listaPoderes.distinctBy { it.id }.sortedBy { it.nome }
                            var filterAaPoderText by remember { mutableStateOf("") }
                            AlertDialog(
                                onDismissRequest = { showAaPoderesPickerDialog = false },
                                title = { Text("Poderes do Antecedente Arcano") },
                                text = {
                                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                        androidx.compose.material3.OutlinedTextField(
                                            value = filterAaPoderText,
                                            onValueChange = { filterAaPoderText = it },
                                            label = { Text("Filtrar Poder") },
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                        )
                                        availablePoderes.filter { it.nome.contains(filterAaPoderText, ignoreCase = true) }.forEach { poder ->
                                            val isSel = poder.id in customAaPoderesEspecificos
                                            Row(
                                                modifier = Modifier.fillMaxWidth().clickable {
                                                    customAaPoderesEspecificos = if (isSel) customAaPoderesEspecificos - poder.id else customAaPoderesEspecificos + poder.id
                                                }.padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Checkbox(checked = isSel, onCheckedChange = {
                                                    customAaPoderesEspecificos = if (it) customAaPoderesEspecificos + poder.id else customAaPoderesEspecificos - poder.id
                                                })
                                                Spacer(Modifier.width(8.dp))
                                                Text("${poder.nome} (${poder.origem})", style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                    }
                                },
                                confirmButton = { TextButton(onClick = { showAaPoderesPickerDialog = false }) { Text("OK") } }
                            )
                        }

                        if (showVarianteVantagemAddDialog) {
                            val availableVarianteVantagens = state.listaVantagens.distinctBy { it.id }.sortedBy { it.nome }
                            var filterVarianteVantagemText by remember { mutableStateOf("") }
                            AlertDialog(
                                onDismissRequest = { showVarianteVantagemAddDialog = false },
                                title = { Text("Adicionar Vantagem Grátis") },
                                text = {
                                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                        androidx.compose.material3.OutlinedTextField(
                                            value = filterVarianteVantagemText,
                                            onValueChange = { filterVarianteVantagemText = it },
                                            label = { Text("Filtrar Vantagem") },
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                        )
                                        availableVarianteVantagens.filter { it.nome.contains(filterVarianteVantagemText, ignoreCase = true) }.forEach { vant ->
                                            val isSel = vant.id in varianteVantagensAdicionadas
                                            Row(
                                                modifier = Modifier.fillMaxWidth().clickable {
                                                    varianteVantagensAdicionadas = if (isSel) varianteVantagensAdicionadas - vant.id else varianteVantagensAdicionadas + vant.id
                                                }.padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Checkbox(checked = isSel, onCheckedChange = {
                                                    varianteVantagensAdicionadas = if (it) varianteVantagensAdicionadas + vant.id else varianteVantagensAdicionadas - vant.id
                                                })
                                                Spacer(Modifier.width(8.dp))
                                                Text("${vant.nome} (${vant.requisitos.estagio})", style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                    }
                                },
                                confirmButton = { TextButton(onClick = { showVarianteVantagemAddDialog = false }) { Text("OK") } }
                            )
                        }

                        if (showVarianteComplicacaoSeveridadeDialog) {
                            AlertDialog(
                                onDismissRequest = { showVarianteComplicacaoSeveridadeDialog = false },
                                title = { Text("Severidade da Complicação") },
                                text = {
                                    Text(
                                        "Escolha a severidade da Complicação a adicionar. Isso filtra quais Complicações do(s) livro(s) ativo(s) ficam disponíveis.",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                },
                                confirmButton = {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        TextButton(onClick = {
                                            varianteComplicacaoComoMaiorEscolhido = false
                                            showVarianteComplicacaoSeveridadeDialog = false
                                            showVarianteComplicacaoPickDialog = true
                                        }) { Text("Menor (-1)") }
                                        TextButton(onClick = {
                                            varianteComplicacaoComoMaiorEscolhido = true
                                            showVarianteComplicacaoSeveridadeDialog = false
                                            showVarianteComplicacaoPickDialog = true
                                        }) { Text("Maior (-2)") }
                                    }
                                },
                                dismissButton = { TextButton(onClick = { showVarianteComplicacaoSeveridadeDialog = false }) { Text("Cancelar") } }
                            )
                        }

                        if (showVarianteComplicacaoPickDialog) {
                            val comoMaior = varianteComplicacaoComoMaiorEscolhido
                            val availableVarianteComps = state.listaComplicacoes.distinctBy { it.id }.filter { comp ->
                                val sev = comp.severity.trim().lowercase()
                                if (comoMaior) {
                                    sev == "maior" || (sev.contains("menor") && sev.contains("maior"))
                                } else {
                                    sev == "menor" || (sev.contains("menor") && sev.contains("maior"))
                                }
                            }.sortedBy { it.name }
                            var filterVarianteCompText by remember { mutableStateOf("") }
                            AlertDialog(
                                onDismissRequest = { showVarianteComplicacaoPickDialog = false },
                                title = { Text(if (comoMaior) "Complicações Maiores" else "Complicações Menores") },
                                text = {
                                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                        androidx.compose.material3.OutlinedTextField(
                                            value = filterVarianteCompText,
                                            onValueChange = { filterVarianteCompText = it },
                                            label = { Text("Filtrar Complicação") },
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                        )
                                        availableVarianteComps.filter { it.name.contains(filterVarianteCompText, ignoreCase = true) }.forEach { comp ->
                                            val isSel = varianteComplicacoesAdicionadas.any { it.complicacaoId == comp.id && it.comoMaior == comoMaior }
                                            Row(
                                                modifier = Modifier.fillMaxWidth().clickable {
                                                    varianteComplicacoesAdicionadas = if (isSel) {
                                                        varianteComplicacoesAdicionadas.filterNot { it.complicacaoId == comp.id && it.comoMaior == comoMaior }
                                                    } else {
                                                        varianteComplicacoesAdicionadas + com.example.swadebuilder.model.CustomVariantComplicacaoEscolhida(comp.id, comoMaior)
                                                    }
                                                }.padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Checkbox(checked = isSel, onCheckedChange = {
                                                    varianteComplicacoesAdicionadas = if (it) {
                                                        varianteComplicacoesAdicionadas + com.example.swadebuilder.model.CustomVariantComplicacaoEscolhida(comp.id, comoMaior)
                                                    } else {
                                                        varianteComplicacoesAdicionadas.filterNot { e -> e.complicacaoId == comp.id && e.comoMaior == comoMaior }
                                                    }
                                                })
                                                Spacer(Modifier.width(8.dp))
                                                Text(comp.name, style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                    }
                                },
                                confirmButton = { TextButton(onClick = { showVarianteComplicacaoPickDialog = false }) { Text("OK") } }
                            )
                        }
                    }
                }

                // Card "Backup e Transferência (JSON)"
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    var showExportDialog by remember { mutableStateOf(false) }
                    var showImportDialog by remember { mutableStateOf(false) }
                    var backupJsonText by remember { mutableStateOf("") }
                    var importErrorText by remember { mutableStateOf<String?>(null) }
                    val backupContext = androidx.compose.ui.platform.LocalContext.current
                    val backupStorageManager = remember { com.example.swadebuilder.util.CustomStorageManager() }

                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Backup da Ficha (JSON)",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Exporte ou importe fichas em formato JSON seguro.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val bundle = aggregateAllCustomContent(backupContext, backupStorageManager)
                                    val snapshot = state.toSnapshot().copy(customContent = bundle)
                                    backupJsonText = com.example.swadebuilder.util.CharacterBackupManager.exportBackupJson(snapshot)
                                    showExportDialog = true
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Exportar JSON")
                            }
                            OutlinedButton(
                                onClick = {
                                    backupJsonText = ""
                                    importErrorText = null
                                    showImportDialog = true
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Importar JSON")
                            }
                        }
                    }

                    if (showExportDialog) {
                        AlertDialog(
                            onDismissRequest = { showExportDialog = false },
                            title = { Text("Backup JSON da Ficha") },
                            text = {
                                Column {
                                    Text("Copie o texto JSON abaixo para salvar seu backup em outro lugar:", style = MaterialTheme.typography.bodySmall)
                                    Spacer(Modifier.height(8.dp))
                                    androidx.compose.material3.OutlinedTextField(
                                        value = backupJsonText,
                                        onValueChange = {},
                                        readOnly = true,
                                        modifier = Modifier.fillMaxWidth().height(180.dp)
                                    )
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = { showExportDialog = false }) { Text("Fechar") }
                            }
                        )
                    }

                    if (showImportDialog) {
                        AlertDialog(
                            onDismissRequest = { showImportDialog = false },
                            title = { Text("Importar Backup JSON") },
                            text = {
                                Column {
                                    Text("Cole o texto JSON do backup da ficha:", style = MaterialTheme.typography.bodySmall)
                                    Spacer(Modifier.height(8.dp))
                                    androidx.compose.material3.OutlinedTextField(
                                        value = backupJsonText,
                                        onValueChange = { backupJsonText = it; importErrorText = null },
                                        modifier = Modifier.fillMaxWidth().height(160.dp),
                                        placeholder = { Text("{ \"id\": ... }") }
                                    )
                                    if (importErrorText != null) {
                                        Spacer(Modifier.height(8.dp))
                                        Text(importErrorText!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        when (val result = com.example.swadebuilder.util.CharacterBackupManager.importBackupJson(backupJsonText)) {
                                            is com.example.swadebuilder.util.CharacterBackupManager.ImportResult.Success -> {
                                                // Mescla o conteúdo customizado embutido no backup ANTES de restaurar a
                                                // ficha, pra que vantagens/raça/poderes/etc. custom que ela referencia já
                                                // existam em state.lista* no momento em que restoreFromSnapshot resolve
                                                // cada seleção — sem isso a ficha restaurada mostraria seleções "vazias"
                                                // até um reload completo dos dados do jogo.
                                                result.snapshot.customContent?.let { bundle ->
                                                    mergeImportedCustomContent(backupContext, state, backupStorageManager, bundle)
                                                    onCustomContentChanged()
                                                }
                                                state.restoreFromSnapshot(result.snapshot, mutableListOf())
                                                showImportDialog = false
                                            }
                                            is com.example.swadebuilder.util.CharacterBackupManager.ImportResult.Failure -> {
                                                importErrorText = result.reason
                                            }
                                        }
                                    },
                                    enabled = backupJsonText.isNotBlank()
                                ) {
                                    Text("Restaurar Ficha")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showImportDialog = false }) { Text("Cancelar") }
                            }
                        )
                    }
                }

                // Card "Visual e Tema"
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
                            text = "Visual e Tema",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text("Estilo das Abas / Opções", style = MaterialTheme.typography.bodyMedium)

                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val options = listOf(TabStyle.ICONES, TabStyle.TEXTO)
                            val labels = listOf("Ícones", "Texto")

                            options.forEachIndexed { index, option ->
                                SegmentedButton(
                                    selected = state.estiloAbas == option,
                                    onClick = {
                                        state.estiloAbas = option
                                        persistPrefs()
                                    },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size)
                                ) {
                                    Text(labels[index])
                                }
                            }
                        }

                        Spacer(Modifier.height(4.dp))

                        // Theme Selection Trigger Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Tema do App", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = themeNames[state.appTheme] ?: state.appTheme.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            OutlinedButton(
                                onClick = { showThemeDialog = true }
                            ) {
                                Text("Alterar Tema")
                            }
                        }
                    }
                }

                // Card "Sons e Vibração"
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp) // Increased spacing for cleaner look
                    ) {
                        Text(
                            text = "Sons e Vibração",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // Haptic Feedback
                        Column {
                            Text("Intensidade da Vibração", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Vibration,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Slider(
                                    value = state.hapticStrength.toFloat(),
                                    onValueChange = { state.hapticStrength = it.roundToInt() },
                                    onValueChangeFinished = {
                                        persistPrefs()
                                        feedbackController.play(state.hapticStrength, 0)
                                    },
                                    valueRange = 0f..100f,
                                    modifier = Modifier.weight(1f),
                                    thumb = {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                                        )
                                    },
                                    track = { sliderState ->
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(2.dp)
                                                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                                        ) {
                                            val fraction = (sliderState.value - sliderState.valueRange.start) / (sliderState.valueRange.endInclusive - sliderState.valueRange.start)
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(fraction)
                                                    .fillMaxHeight()
                                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                                            )
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("${state.hapticStrength}%", style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        // App Sounds
                        Column {
                            Text("Volume", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Slider(
                                    value = state.soundVolume.toFloat(),
                                    onValueChange = { state.soundVolume = it.roundToInt() },
                                    onValueChangeFinished = {
                                        persistPrefs()
                                        feedbackController.play(0, state.soundVolume)
                                    },
                                    valueRange = 0f..100f,
                                    modifier = Modifier.weight(1f),
                                    thumb = {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .background(MaterialTheme.colorScheme.secondary, CircleShape)
                                        )
                                    },
                                    track = { sliderState ->
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(2.dp)
                                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f), CircleShape)
                                        ) {
                                            val fraction = (sliderState.value - sliderState.valueRange.start) / (sliderState.valueRange.endInclusive - sliderState.valueRange.start)
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(fraction)
                                                    .fillMaxHeight()
                                                    .background(MaterialTheme.colorScheme.secondary, CircleShape)
                                            )
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("${state.soundVolume}%", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar")
            }
        }
    )

    if (showNpcWarning) {
        AlertDialog(
            onDismissRequest = { showNpcWarning = false },
            title = { Text("Transformar em NPC?") },
            text = { Text("Ao ativar o Modo Livre, este personagem será transformado em um NPC. Custos de pontos e requisitos serão ignorados, e a progressão de XP padrão será desabilitada. Esta ação é irreversível para este personagem.") },
            confirmButton = {
                TextButton(onClick = {
                    state.modoLivre = true
                    showNpcWarning = false
                }) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { showNpcWarning = false }) { Text("Cancelar") }
            }
        )
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Selecionar Tema do App", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    sortedThemes.forEach { theme ->
                        val isSelected = state.appTheme == theme
                        val themeLabel = themeNames[theme] ?: theme.name
                        val themeDesc = themeDescriptions[theme] ?: ""
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                        ) {
                            if (isSelected) {
                                TextButton(
                                    onClick = { showThemeDialog = false },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("✓ $themeLabel", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                        if (themeDesc.isNotBlank()) {
                                            Text(themeDesc, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            } else {
                                OutlinedButton(
                                    onClick = {
                                        onThemeSelected(theme)
                                        persistPrefs()
                                        feedbackController.play(state.hapticStrength, state.soundVolume)
                                        showThemeDialog = false
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(themeLabel, style = MaterialTheme.typography.titleMedium)
                                        if (themeDesc.isNotBlank()) {
                                            Text(themeDesc, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
