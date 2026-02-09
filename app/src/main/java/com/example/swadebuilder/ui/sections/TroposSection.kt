package com.example.swadebuilder.ui.sections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.EditionConfig
import com.example.swadebuilder.criacaoBasicaCongelada
import com.example.swadebuilder.keyify
import com.example.swadebuilder.listaTropos
import com.example.swadebuilder.listaVantagens
import com.example.swadebuilder.ui.components.DropdownField
import com.example.swadebuilder.ui.components.RadioButtonRow
import com.example.swadebuilder.ui.components.SectionCard
import kotlin.random.Random

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TroposSection(
    state: CriadorState,
    onUserFeedback: () -> Unit
) {
    if (!state.compendioArteDaGuerraAtivo) return

    val tropos = remember { listaTropos }
    val showOfficialNames = EditionConfig.isFullEdition && state.modoOficialAtivo
    val idParaNome = remember(showOfficialNames) {
        listaVantagens.associate { vant ->
            val nome = if (showOfficialNames && !vant.originalName.isNullOrBlank()) vant.originalName else vant.nome
            vant.id to nome
        }
    }

    SectionCard(
        title = "Tropos",
        icon = Icons.Default.AutoAwesome,
        showHeader = false
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Escolha um único tropo para definir o estilo marcial do personagem.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Option "None" to unlock race change
            val noneSelected = state.tropoSelecionado == null
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        state.selecionarTropo(null)
                        onUserFeedback()
                    },
                colors = CardDefaults.cardColors(
                    containerColor = if (noneSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    RadioButtonRow(
                        selected = noneSelected,
                        label = "Nenhum (Permite alterar Ancestralidade)",
                        onSelect = {
                            state.selecionarTropo(null)
                            onUserFeedback()
                        }
                    )
                    Text(
                        text = "Selecione esta opção se deseja alterar sua Ancestralidade. Enquanto um Tropo estiver ativo, a Ancestralidade fica bloqueada.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 40.dp, top = 4.dp)
                    )
                }
            }

            tropos.forEach { tropo ->
                val selecionado = state.tropoSelecionado?.id == tropo.id
                val vantagensNomeadas = tropo.ganhaAoComprar.map { idParaNome[it] ?: it }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable(enabled = !state.criacaoBasicaCongelada) {
                            state.selecionarTropo(tropo)
                            onUserFeedback()
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selecionado) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        RadioButtonRow(
                            selected = selecionado,
                            label = if (showOfficialNames && tropo.nome.isNotBlank()) tropo.nome else tropo.nome,
                            onSelect = {
                                if (state.criacaoBasicaCongelada) return@RadioButtonRow
                                state.selecionarTropo(tropo)
                                onUserFeedback()
                            }
                        )

                        if (tropo.tecnicasIniciais > 0) {
                            Text(
                                text = "Técnicas iniciais: ${tropo.tecnicasIniciais}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 40.dp, top = 4.dp)
                            )
                        }

                        Text(
                            text = tropo.descricao,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 40.dp, top = 4.dp, end = 8.dp)
                        )

                        if (selecionado && tropo.id == "tropo_buxista") {
                            Spacer(Modifier.size(8.dp))
                            Text(
                                text = "Caminho Sagrado",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 40.dp, top = 4.dp)
                            )
                            Text(
                                text = "Escolha um caminho associado à seita ou ordem do Bu Xista.",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 40.dp, top = 4.dp, end = 8.dp)
                            )
                            val caminhos = listOf(
                                "Equilibrado" to "Alinhando treinamento físico e exercícios mentais, um Bu Xista equilibra ações com pensamento. Enquanto estiver em transe, o Bu Xista recebe +2 em rolagens resistidas de Intimidar, rolagens de Medo e para superar o estado Abalado.",
                                "Círculo" to "Trilhando o caminho do círculo focado na restauração do corpo. Enquanto estiver em transe, como uma ação, um Bu Xista pode gastar um ponto de Chi para curar imediatamente um Ferimento. Isso pode acontecer após o transe, mas deve ser feito dentro da hora em que o Ferimento foi recebido.",
                                "Exterior" to "Bu Xistas exteriores se concentram em canalizar o Chi ao seu redor. Em transe, um Bu Xista recebe +2 em rolagens de Força para empurrar ou para resistir ser empurrado. Além disso, rolagens de Atletismo ou Acrobacia envolvendo saltar, escalar, arremessar ou atividades similares recebem +1.",
                                "Interno" to "Condições interiores do corpo podem ser conquistadas com a prática adequada. Enquanto em transe, um Bu Xista recebe +2 em rolagens de Vigor para superar o estado Atordoado e para resistir a venenos.",
                                "Nascente" to "Nascentes são o centro da comunidade. Enquanto estiver em transe e realizando uma rolagem de Suporte, um Bu Xista recebe +1 nesta rolagem. Além disso, um Bu Xista pode gastar um ponto de Chi para fazer uma rerrolagem de Intimidar, Persuadir, Performance ou Provocar.",
                                "Torre" to "Forte e estável, o corpo e a mente podem resistir aos elementos. Enquanto estiver em transe, um Bu Xista pode gastar um ponto de Chi para manifestar uma aura protetora. Essa aura luminosa dura enquanto o transe estiver ativo, fornecendo um bônus de +4 em Armadura (isso não se acumula com as armaduras usadas)."
                            )
                            Column(modifier = Modifier.padding(start = 40.dp, top = 4.dp, end = 8.dp)) {
                                caminhos.forEach { (nome, descricao) ->
                                    RadioButtonRow(
                                        selected = state.buXistaCaminhoSelecionado == nome,
                                        label = nome,
                                        onSelect = { state.buXistaCaminhoSelecionado = nome }
                                    )
                                    Text(
                                        text = descricao,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(start = 40.dp, bottom = 4.dp, end = 8.dp)
                                    )
                                }
                            }
                        }

                        if (selecionado && tropo.id == "tropo_elementalista") {
                            Spacer(Modifier.size(8.dp))
                            Text(
                                text = "Elemento Primário",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 40.dp, top = 4.dp)
                            )
                            Text(
                                text = "Escolha o elemento associado ao treinamento elementalista.",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 40.dp, top = 4.dp, end = 8.dp)
                            )
                            val elementos = listOf(
                                "Fogo" to "Fogo: associado à paixão e transformação. Conjura chamas e manipula calor.",
                                "Metal" to "Metal: firmeza e determinação. Manobras com metais fabricados e minérios.",
                                "Rocha" to "Rocha: estrutura e estabilidade. Manipula pedras, minerais e a terra.",
                                "Água" to "Água: flexibilidade e sabedoria. Controla fluxo e temperatura da água.",
                                "Madeira" to "Madeira: Chi vivo e crescimento. Manipula plantas e vida."
                            )
                            Column(modifier = Modifier.padding(start = 40.dp, top = 4.dp, end = 8.dp)) {
                                elementos.forEach { (nome, descricao) ->
                                    RadioButtonRow(
                                        selected = state.elementalistaElementoSelecionado == nome,
                                        label = nome,
                                        onSelect = { state.elementalistaElementoSelecionado = nome }
                                    )
                                    Text(
                                        text = descricao,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(start = 40.dp, bottom = 4.dp, end = 8.dp)
                                    )
                                }
                            }
                        }

                        if (selecionado && tropo.id == "tropo_kui") {
                            Spacer(Modifier.size(8.dp))
                            Text(
                                text = "Ferramentas do Ofício",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 40.dp, top = 4.dp)
                            )
                            Text(
                                text = "Escolha a ferramenta associada ao posto do Kui.",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 40.dp, top = 4.dp, end = 8.dp)
                            )
                            val ferramentas = listOf(
                                "Armas Abençoadas" to "Focando no invisível, um Kui usa a bênção do Chi para atacar seres incorpóreos. Encanta armas por 10 minutos e pode abençoar mais objetos com Chi.",
                                "Vínculo Espiritual" to "Ritual de ligação reduz um espírito a um companheiro leal (Extra). Espíritos podem ser reconvocados após 24 horas com ritual e gasto de Chi.",
                                "Talismãs" to "Cria talismãs empoderados pelo Chi, inclusive imitando Técnicas Chi vistas nas últimas 48 horas, com custo em Chi e uso único."
                            )
                            Column(modifier = Modifier.padding(start = 40.dp, top = 4.dp, end = 8.dp)) {
                                ferramentas.forEach { (nome, descricao) ->
                                    RadioButtonRow(
                                        selected = state.kuiFerramentaSelecionada == nome,
                                        label = nome,
                                        onSelect = { state.kuiFerramentaSelecionada = nome }
                                    )
                                    Text(
                                        text = descricao,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(start = 40.dp, bottom = 4.dp, end = 8.dp)
                                    )
                                }
                            }
                        }

                        if (selecionado && tropo.id == "tropo_samurai") {
                            Spacer(Modifier.size(8.dp))
                            Text(
                                text = "Treinamento de Elite",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 40.dp, top = 4.dp)
                            )
                            Text(
                                text = "Escolha a perícia adicional em d6 e a vantagem inicial.",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 40.dp, top = 4.dp, end = 8.dp)
                            )
                            Column(modifier = Modifier.padding(start = 40.dp, top = 4.dp, end = 8.dp)) {
                                Text(
                                    text = "Perícia em d6",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                RadioButtonRow(
                                    selected = state.samuraiPericiaEscolhida == "Jutsu",
                                    label = "Jutsu (Lutar) d6",
                                    onSelect = { state.atualizarSamuraiPericiaEscolhida("Jutsu") }
                                )
                                RadioButtonRow(
                                    selected = state.samuraiPericiaEscolhida == "Atirar",
                                    label = "Atirar d6",
                                    onSelect = { state.atualizarSamuraiPericiaEscolhida("Atirar") }
                                )
                            }
                            Column(modifier = Modifier.padding(start = 40.dp, top = 4.dp, end = 8.dp)) {
                                Text(
                                    text = "Vantagem inicial",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                RadioButtonRow(
                                    selected = state.samuraiVantagemEscolhida == "Comando",
                                    label = "Comando",
                                    onSelect = { state.atualizarSamuraiVantagemEscolhida("Comando") }
                                )
                                RadioButtonRow(
                                    selected = state.samuraiVantagemEscolhida == "Combate",
                                    label = "Vantagem de Combate (1 slot grátis)",
                                    onSelect = { state.atualizarSamuraiVantagemEscolhida("Combate") }
                                )
                                Text(
                                    text = "Com Conhecimento Batalha d8+, o Samurai ignora requisitos de Estágio para vantagens de Liderança.",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            Spacer(Modifier.size(8.dp))
                            Text(
                                text = "Posturas de Combate (escolha 2)",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 40.dp, top = 4.dp)
                            )
                            Text(
                                text = "Selecionadas: ${state.samuraiPosturasSelecionadas.size}/2",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 40.dp, top = 4.dp, end = 8.dp)
                            )
                            val posturas = listOf(
                                "Asa da Garça" to "Adiciona +1 no Aparar.",
                                "Bico do Galo" to "Concede +1 em Ataques Localizados para desarmar oponentes.",
                                "Carapaça da Tartaruga" to "Adiciona +2 na Armadura enquanto estiver utilizando uma armadura.",
                                "Cauda do Macaco" to "Concede Alcance +1.",
                                "Casco do Cavalo" to "Concede +2 nas rolagens resistidas de Força para Empurrar ou ser Empurrado.",
                                "Enxame de Ratos" to "Concede +3 (em vez de +2) em Ataques Selvagens.",
                                "Presas de Águia" to "Concede PA 2 à arma utilizada.",
                                "Presas do Javali" to "Adiciona +1 nas rolagens de ataque."
                            )
                            Column(modifier = Modifier.padding(start = 40.dp, top = 4.dp, end = 8.dp)) {
                                posturas.forEach { (nome, descricao) ->
                                    val selecionada = state.samuraiPosturasSelecionadas.contains(nome)
                                    val habilitada = selecionada || state.samuraiPosturasSelecionadas.size < 2
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Checkbox(
                                            checked = selecionada,
                                            onCheckedChange = { if (habilitada) state.toggleSamuraiPostura(nome) },
                                            enabled = habilitada
                                        )
                                        Column(modifier = Modifier.padding(start = 8.dp)) {
                                            Text(text = nome, style = MaterialTheme.typography.bodyMedium)
                                            Text(text = descricao, style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                            }
                        }

                        if (selecionado && tropo.id == "tropo_shinobi") {
                            Spacer(Modifier.size(8.dp))
                            Text(
                                text = "Talento Shinobi",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 40.dp, top = 4.dp)
                            )
                            Text(
                                text = "Escolha um talento. Místico concede uma Técnica Chi adicional.",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 40.dp, top = 4.dp, end = 8.dp)
                            )
                            val talentos = listOf(
                                "Alteração" to "Ao gastar um ponto de Chi, assume a aparência de outra pessoa como o poder Disfarce (não é Técnica Chi).",
                                "Pés Leves" to "Caminha por lama ou neve sem deixar pegadas pesadas. Rastreadores sofrem -4 em Sobrevivência.",
                                "Místico" to "Conhecimentos avançados do Chi. Começa com uma Técnica Chi adicional.",
                                "Passo das Sombras" to "Gasta um ponto de Chi para ganhar uma ação de Movimentação extra silenciosa; inimigos sofrem -2 em Perceber e ataques furtivos tratam sucesso como Ampliação."
                            )
                            Column(modifier = Modifier.padding(start = 40.dp, top = 4.dp, end = 8.dp)) {
                                talentos.forEach { (nome, descricao) ->
                                    RadioButtonRow(
                                        selected = state.shinobiTalentoSelecionado == nome,
                                        label = nome,
                                        onSelect = { state.shinobiTalentoSelecionado = nome }
                                    )
                                    Text(
                                        text = descricao,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(start = 40.dp, bottom = 4.dp, end = 8.dp)
                                    )
                                }
                            }

                            Spacer(Modifier.size(8.dp))
                            Text(
                                text = "Tipo de Treinamento",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 40.dp, top = 4.dp)
                            )
                            Text(
                                text = "Escolha o treinamento para definir as perícias com rerrolagem de Chi.",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 40.dp, top = 4.dp, end = 8.dp)
                            )
                            val treinamentos = listOf(
                                "Infiltrador" to "Gasta um Chi para rerrolar falhas em Acrobacia, Pesquisar, Furtividade ou Ladinagem.",
                                "Batedor" to "Gasta um Chi para rerrolar falhas em Atletismo, Curar ou Sobrevivência.",
                                "Espião" to "Gasta um Chi para rerrolar falhas em Convenção, Intimidar, Performance, Persuadir ou Idioma."
                            )
                            Column(modifier = Modifier.padding(start = 40.dp, top = 4.dp, end = 8.dp)) {
                                treinamentos.forEach { (nome, descricao) ->
                                    RadioButtonRow(
                                        selected = state.shinobiTreinamentoSelecionado == nome,
                                        label = nome,
                                        onSelect = { state.shinobiTreinamentoSelecionado = nome }
                                    )
                                    Text(
                                        text = descricao,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(start = 40.dp, bottom = 4.dp, end = 8.dp)
                                    )
                                }
                            }
                        }

                        if (selecionado && tropo.id == "tropo_youxia") {
                            Spacer(Modifier.size(8.dp))
                            Text(
                                text = "Kensai (Arma Predileta)",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 40.dp, top = 4.dp)
                            )
                            Text(
                                text = "Escolha a prática marcial associada à arma predileta.",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 40.dp, top = 4.dp, end = 8.dp)
                            )
                            val jutsuTipos = listOf(
                                "Desarmado",
                                "Espada",
                                "Haste",
                                "Concussão",
                                "Corrente",
                                "Leve",
                                "Massivo",
                                "Passivo",
                                "Samurai"
                            )
                            Column(modifier = Modifier.padding(start = 40.dp, top = 4.dp, end = 8.dp)) {
                                jutsuTipos.forEach { nome ->
                                    RadioButtonRow(
                                        selected = state.youxiaJutsuSelecionado == nome,
                                        label = nome,
                                        onSelect = { state.atualizarYouxiaJutsuSelecionado(nome) }
                                    )
                                }
                            }

                            Spacer(Modifier.size(8.dp))
                            Text(
                                text = "Histórico da Arma",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 40.dp, top = 4.dp)
                            )
                            Text(
                                text = "Escolha a característica histórica da arma Kensai.",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 40.dp, top = 4.dp, end = 8.dp)
                            )
                            val historicos = listOf(
                                "Ancestral" to "Passada a cada geração, a arma kensai é um legado e possui a qualidade Ancestral.",
                                "Carregada" to "Gaste um ponto de Chi para carregar a arma com Alta Explosão (Modelo Médio de Explosão) sem dano ao Youxia.",
                                "Penetrante" to "A arma kensai tem PA 4. Gastando um ponto de Chi, o PA é aumentado em 10 até o início da próxima ação.",
                                "Afiada" to "A arma é considerada Arma Pesada. Gastando um ponto de Chi, aumenta o dado de dano em um tipo de dado até o início do próximo turno."
                            )
                            Column(modifier = Modifier.padding(start = 40.dp, top = 4.dp, end = 8.dp)) {
                                historicos.forEach { (nome, descricao) ->
                                    RadioButtonRow(
                                        selected = state.youxiaHistoricoSelecionado == nome,
                                        label = nome,
                                        onSelect = { state.atualizarYouxiaHistoricoSelecionado(nome) }
                                    )
                                    Text(
                                        text = descricao,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(start = 40.dp, bottom = 4.dp, end = 8.dp)
                                    )
                                }
                            }
                        }

                        if (selecionado && tropo.id == "tropo_artista_marcial") {
                            Spacer(Modifier.size(8.dp))
                            Text(
                                text = "Jutsu inicial",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 40.dp, top = 4.dp)
                            )
                            Column(modifier = Modifier.padding(start = 40.dp, top = 4.dp, end = 8.dp)) {
                                RadioButtonRow(
                                    selected = state.artistaMarcialJutsuOpcao == CriadorState.ARTISTA_MARCIAL_JUTSU_D6,
                                    label = "Jutsu (Desarmado) d6",
                                    onSelect = { state.atualizarArtistaMarcialJutsuOpcao(CriadorState.ARTISTA_MARCIAL_JUTSU_D6) }
                                )
                                RadioButtonRow(
                                    selected = state.artistaMarcialJutsuOpcao == CriadorState.ARTISTA_MARCIAL_JUTSU_D4_D4,
                                    label = "Jutsu (Desarmado) d4 + outro Jutsu d4",
                                    onSelect = { state.atualizarArtistaMarcialJutsuOpcao(CriadorState.ARTISTA_MARCIAL_JUTSU_D4_D4) }
                                )
                            }

                            Spacer(Modifier.size(8.dp))
                            Text(
                                text = "Potencial Físico",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 40.dp, top = 4.dp)
                            )
                            Text(
                                text = "Escolha um atributo que ainda não esteja no máximo para receber o bônus e a vantagem.",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 40.dp, top = 4.dp, end = 8.dp)
                            )
                            val agiValue = state.valoresAtributos["AGILIDADE"]?.intValue ?: 0
                            val forValue = state.valoresAtributos["FORCA"]?.intValue ?: 0
                            val vigValue = state.valoresAtributos["VIGOR"]?.intValue ?: 0
                            val agiMax = state.atributoMaxRaw("AGILIDADE")
                            val forMax = state.atributoMaxRaw("FORCA")
                            val vigMax = state.atributoMaxRaw("VIGOR")
                            val agiSelected = state.artistaMarcialPotencialFisico?.keyify() == "AGILIDADE"
                            val forSelected = state.artistaMarcialPotencialFisico?.keyify() == "FORCA"
                            val vigSelected = state.artistaMarcialPotencialFisico?.keyify() == "VIGOR"
                            val agiDisabled = agiValue >= agiMax && !agiSelected
                            val forDisabled = forValue >= forMax && !forSelected
                            val vigDisabled = vigValue >= vigMax && !vigSelected
                            Column(modifier = Modifier.padding(start = 40.dp, top = 4.dp, end = 8.dp)) {
                                RadioButtonRow(
                                    selected = agiSelected,
                                    label = if (agiDisabled) "Agilidade (Esquiva) - no máximo" else "Agilidade (Esquiva)",
                                    onSelect = {
                                        if (!agiDisabled) {
                                            state.atualizarArtistaMarcialPotencialFisico("Agilidade")
                                        }
                                    }
                                )
                                RadioButtonRow(
                                    selected = forSelected,
                                    label = if (forDisabled) "Força (Bloquear) - no máximo" else "Força (Bloquear)",
                                    onSelect = {
                                        if (!forDisabled) {
                                            state.atualizarArtistaMarcialPotencialFisico("Força")
                                        }
                                    }
                                )
                                RadioButtonRow(
                                    selected = vigSelected,
                                    label = if (vigDisabled) "Vigor (Reflexos de Combate) - no máximo" else "Vigor (Reflexos de Combate)",
                                    onSelect = {
                                        if (!vigDisabled) {
                                            state.atualizarArtistaMarcialPotencialFisico("Vigor")
                                        }
                                    }
                                )
                            }

                            Spacer(Modifier.size(8.dp))
                            Text(
                                text = "Técnicas do Artista Marcial (escolha 3)",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 40.dp, top = 4.dp)
                            )
                            Text(
                                text = "Selecionadas: ${state.artistaMarcialTecnicasSelecionadas.size}/3",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 40.dp, top = 4.dp, end = 8.dp)
                            )
                            val tecnicasArtista = listOf(
                                "Sentido às Cegas" to "O aprimoramento de todos os sentidos ocorre se o herói estiver momentaneamente cego ou tiver alguma deficiência visual (como sujeira nos olhos, flashbangs ou até mesmo olhos vendados). O herói adiciona +1d4 nas rolagens de Perceber envolvendo sentidos auditivos e olfativos para localizar seres vivos (ou não vivos) dentro de um Modelo Médio de Explosão.",
                                "Aparar Projéteis" to "Para desviar projéteis, deve-se saber a direção de onde estão vindo. Se o herói é o alvo de um projétil e está ciente de que está vindo, ele o desvia como se tivesse sido bem-sucedido. A exceção a esta regra são efeitos que dependem de impacto ou toque, como uma flecha explosiva ou um dardo envenenado que carrega um veneno de contato.",
                                "Salto Duplo" to "Um herói pode realizar um salto conforme a Vantagem Duplo Salto.",
                                "Evadir" to "O artista marcial é rápido para reagir a situações perigosas. Um herói recebe +2 em rolagens de Evasão.",
                                "Velocista" to "A energia cinética se expande, um herói dobra sua Movimentação base e aumenta sua Corrida em um tipo de dado.",
                                "Durão" to "Horas de treinamento contra árvores ou paredes de pedra permitem que o herói quebre objetos sólidos. Ataques desarmados podem ignorar 4 de dureza ao tentar quebrar ou partir um objeto. Este tipo de dano é sempre considerado o tipo de dano apropriado.",
                                "Improvisador" to "Pode usar armas improvisadas sem penalidades. Itens podem ser usados defensivamente, adicionando +1 no Aparar e +1 na Armadura se forem de peso médio (ou +2 na Armadura se forem pesados).",
                                "Empurrão" to "Trazendo a força da natureza à tona, um ataque desarmado bem-sucedido causa um Empurrão de Chi (veja Regras de Ambientação) equivalente a tamanhos Grandes ou menores. A distância percorrida é aumentada em +1d6.",
                                "Manobras" to "Artistas Marciais usam manobras para desequilibrar e derrubar aqueles que são maiores do que eles, realizando um Desafio para derrubar ou arremessar oponentes de tamanho Grande ou menor sem sofrer penalidades por diferenças de tamanho.",
                                "Regurgitar Veneno" to "Ao ingerir ou ser injetado por veneno, você pode puxá-lo de sua corrente sanguínea até a boca. Você pode cuspir a substância para encerrar os efeitos. Alternativamente, pode ser cuspido como uma arma, usando Atletismo (arremesso) para atingi-lo em um alvo (Alcance: 3/6/12).",
                                "Lutar Sozinho" to "Enquanto cercada por múltiplos inimigos, uma pessoa se destaca no auge do desafio. Os oponentes não recebem bônus de Agrupamento contra o Herói.",
                                "Forte" to "Um Artista Marcial usa Chi para realizar feitos de Força. Ela recebe uma rerrolagem em rolagens Força.",
                                "Resistente" to "O Chi é usado para absorver golpes e manter o herói em pé. O herói recebe +2 em rolagens de Absorção.",
                                "Golpe Vital" to "Atacar o ponto certo pode enfraquecer qualquer brutamontes. Ataques Localizados ao usar golpes desarmados recebem +2 na rolagem."
                            )
                            Column(modifier = Modifier.padding(start = 40.dp, top = 4.dp, end = 8.dp)) {
                                tecnicasArtista.forEach { (nome, descricao) ->
                                    val selecionada = state.artistaMarcialTecnicasSelecionadas.contains(nome)
                                    val habilitada = selecionada || state.artistaMarcialTecnicasSelecionadas.size < 3
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Checkbox(
                                            checked = selecionada,
                                            onCheckedChange = { if (habilitada) state.toggleArtistaMarcialTecnica(nome) },
                                            enabled = habilitada
                                        )
                                        Column(modifier = Modifier.padding(start = 8.dp)) {
                                            Text(text = nome, style = MaterialTheme.typography.bodyMedium)
                                            Text(text = descricao, style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                            }
                        }

                        if (selecionado && tropo.id == "tropo_protagonista") {
                            val random = remember { Random(System.currentTimeMillis()) }
                            val periciaOpcoes = listOf(
                                "Atletismo",
                                "Conhecimento Geral",
                                "Perceber",
                                "Persuadir",
                                "Furtividade"
                            )
                            val vantagemOpcoes = listOf(
                                "Vantagem de Chi",
                                "Vantagem Estranha",
                                "Vantagem de Antecedente",
                                "Vantagem de Combate",
                                "Vantagem Social",
                                "Qualquer Vantagem"
                            )

                            val tecnicasRoll = state.protagonistaRollTecnicas
                            val periciaRoll = state.protagonistaRollPericia
                            val periciasEscolhidas = state.protagonistaPericiasEscolhidas
                            val vantagemRoll = state.protagonistaRollVantagem
                            val qualidadeRoll = state.protagonistaRollQualidade
                            val habilidadeRoll = state.protagonistaRollHabilidade
                            val periciasPaixao = state.protagonistaPericiasPaixao
                            val d4Options = listOf("1", "2", "3", "4")
                            val d6Options = listOf("1", "2", "3", "4", "5", "6")
                            val d8Options = listOf("1", "2", "3", "4", "5", "6", "7", "8")
                            val d10Options = (1..10).map { it.toString() }
                            val d12Options = (1..12).map { it.toString() }

                            fun rollDie(sides: Int): Int = random.nextInt(1, sides + 1)
                            fun applyVantagemRoll(value: Int?) {
                                if (value == null) {
                                    state.updateProtagonistaRollVantagem(null)
                                    return
                                }
                                var roll = value
                                while (roll == 7) {
                                    roll = rollDie(8)
                                }
                                state.updateProtagonistaRollVantagem(roll)
                            }

                            Spacer(Modifier.size(8.dp))
                            Text(
                                text = "Rolagens do Protagonista (opcional)",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 40.dp, top = 4.dp)
                            )
                            Text(
                                text = "Use as rolagens como guia e aplique as escolhas manualmente.",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 40.dp, top = 4.dp, end = 8.dp)
                            )

                            FlowRow(
                                modifier = Modifier.padding(start = 40.dp, top = 8.dp, end = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                DropdownField(
                                    label = "d4",
                                    options = d4Options,
                                    selected = tecnicasRoll?.toString(),
                                    onSelect = { state.updateProtagonistaRollTecnicas(it.toInt()) }
                                )
                                DropdownField(
                                    label = "d6",
                                    options = d6Options,
                                    selected = periciaRoll?.toString(),
                                    onSelect = { state.updateProtagonistaRollPericia(it.toInt()) }
                                )
                                DropdownField(
                                    label = "d8",
                                    options = d8Options,
                                    selected = vantagemRoll?.toString(),
                                    onSelect = { applyVantagemRoll(it.toInt()) }
                                )
                                DropdownField(
                                    label = "d10",
                                    options = d10Options,
                                    selected = qualidadeRoll?.toString(),
                                    onSelect = { state.updateProtagonistaRollQualidade(it.toInt()) }
                                )
                                DropdownField(
                                    label = "d12",
                                    options = d12Options,
                                    selected = habilidadeRoll?.toString(),
                                    onSelect = { state.updateProtagonistaRollHabilidade(it.toInt()) }
                                )
                            }

                            Row(
                                modifier = Modifier.padding(start = 40.dp, top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        state.updateProtagonistaRollTecnicas(rollDie(4))
                                        state.updateProtagonistaRollPericia(rollDie(6))
                                        applyVantagemRoll(rollDie(8))
                                        state.updateProtagonistaRollQualidade(rollDie(10))
                                        state.updateProtagonistaRollHabilidade(rollDie(12))
                                        state.updateProtagonistaPericiasEscolhidas(emptyList())
                                        state.updateProtagonistaPericiasPaixao(emptyList())
                                    }
                                ) {
                                    Text("Rolar tudo")
                                }
                                OutlinedButton(
                                    onClick = {
                                        state.updateProtagonistaRollTecnicas(null)
                                        state.updateProtagonistaRollPericia(null)
                                        state.updateProtagonistaRollVantagem(null)
                                        state.updateProtagonistaRollQualidade(null)
                                        state.updateProtagonistaRollHabilidade(null)
                                        state.updateProtagonistaPericiasEscolhidas(emptyList())
                                        state.updateProtagonistaPericiasPaixao(emptyList())
                                    }
                                ) {
                                    Text("Limpar")
                                }
                            }

                            Spacer(Modifier.size(6.dp))
                            val tecnicasTexto = when (tecnicasRoll) {
                                1 -> "1"
                                2 -> "2"
                                3 -> "2"
                                4 -> "3"
                                else -> "-"
                            }
                            val periciaRollTexto = periciaRoll?.toString() ?: "-"
                            val vantagemRollTexto = vantagemRoll?.toString() ?: "-"
                            val qualidadeRollTexto = qualidadeRoll?.toString() ?: "-"
                            val habilidadeRollTexto = habilidadeRoll?.toString() ?: "-"
                            Text(
                                text = "Técnicas de Chi (d4): $tecnicasTexto",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 40.dp, top = 4.dp)
                            )
                            Text(
                                text = "Perícia Básica (d6): $periciaRollTexto",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 40.dp, top = 4.dp)
                            )

                            if (periciaRoll == 1) {
                                FlowRow(
                                    modifier = Modifier.padding(start = 40.dp, top = 4.dp, end = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    periciaOpcoes.forEach { opcao ->
                                        val selected = periciasEscolhidas.contains(opcao)
                                        FilterChip(
                                            selected = selected,
                                            onClick = {
                                                val updated = if (selected) {
                                                    periciasEscolhidas - opcao
                                                } else if (periciasEscolhidas.size < 2) {
                                                    periciasEscolhidas + opcao
                                                } else {
                                                    periciasEscolhidas
                                                }
                                                state.updateProtagonistaPericiasEscolhidas(updated)
                                            },
                                            label = { Text(opcao) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
                                            )
                                        )
                                    }
                                }
                            } else if (periciaRoll != null) {
                                val periciaTexto = when (periciaRoll) {
                                    2 -> "Atletismo"
                                    3 -> "Conhecimento Geral"
                                    4 -> "Perceber"
                                    5 -> "Persuadir"
                                    6 -> "Furtividade"
                                    else -> "-"
                                }
                                Text(
                                    text = "Resultado: $periciaTexto",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(start = 40.dp, top = 4.dp)
                                )
                            }

                            Text(
                                text = "Vantagem (d8): $vantagemRollTexto",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 40.dp, top = 4.dp)
                            )
                            when (vantagemRoll) {
                                1, 2, 3, 4, 5, 6 -> {
                                    Text(
                                        text = "Resultado: ${vantagemOpcoes[(vantagemRoll ?: 1) - 1]}",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(start = 40.dp, top = 4.dp)
                                    )
                                }
                                7 -> {
                                    Text(
                                        text = "Resultado: Rerrole",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(start = 40.dp, top = 4.dp)
                                    )
                                }
                                8 -> {
                                    Text(
                                        text = "Resultado: escolha qualquer opção acima",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(start = 40.dp, top = 4.dp)
                                    )
                                }
                                else -> Unit
                            }

                            val qualidadeTexto = mapOf(
                                1 to "Bravura: Corajoso + Elevar a Moral.",
                                2 to "Criatividade: +1 Astúcia e rerrolagem por sessão.",
                                3 to "Solidário: Confiável + Comando.",
                                4 to "Força: +1 Força e +2 dano baseado em Força.",
                                5 to "Resiliência: +1 Resistência e +2 Absorção.",
                                6 to "Moralidade: +1 Espírito e Bene em atos altruístas.",
                                7 to "Paixão: duas perícias d6 e Bene extra.",
                                8 to "Flexibilidade: +1 Agilidade e rerrolagem por sessão.",
                                9 to "Confiança: +2 em testes ousados de Característica.",
                                10 to "Resistente: +1 Vigor e +2 contra perigos ambientais."
                            )
                            Text(
                                text = "Qualidades de Herói (d10): $qualidadeRollTexto",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 40.dp, top = 4.dp)
                            )
                            qualidadeRoll?.let { roll ->
                                Text(
                                    text = qualidadeTexto[roll].orEmpty(),
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(start = 40.dp, top = 2.dp, end = 8.dp)
                                )
                            }

                            if (qualidadeRoll == 7) {
                                FlowRow(
                                    modifier = Modifier.padding(start = 40.dp, top = 4.dp, end = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    periciaOpcoes.forEach { opcao ->
                                        val selected = periciasPaixao.contains(opcao)
                                        FilterChip(
                                            selected = selected,
                                            onClick = {
                                                val updated = if (selected) {
                                                    periciasPaixao - opcao
                                                } else if (periciasPaixao.size < 2) {
                                                    periciasPaixao + opcao
                                                } else {
                                                    periciasPaixao
                                                }
                                                state.updateProtagonistaPericiasPaixao(updated)
                                            },
                                            label = { Text(opcao) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
                                            )
                                        )
                                    }
                                }
                            }

                            val habilidadeTexto = mapOf(
                                1 to "Continue!: ignora penalidades até o próximo turno.",
                                2 to "Esforço Extra: modificador grátis em Técnica Chi conhecida.",
                                3 to "Defensivo: desvia ataque gastando Chi.",
                                4 to "Vantagem Adicional: escolha 1 Vantagem extra.",
                                5 to "Talismãs: escreve talismãs como um Kui.",
                                6 to "Arma Ancestral: arma com Arma Predileta Aprimorada e reserva Chi.",
                                7 to "Companheiro: ganha Senhor das Feras.",
                                8 to "Resistência ao Chi: -2 em Técnicas Chi contra você e -2 dano.",
                                9 to "Velocidade Incomum: dobra Movimentação.",
                                10 to "Arma Massiva: arma pesada e bônus de dano por Chi.",
                                11 to "Mais Uma Chance: remove um Ferimento 1x por sessão.",
                                12 to "Trilhando seu Próprio Caminho: escolha outro resultado."
                            )
                            Text(
                                text = "Habilidades (d12): $habilidadeRollTexto",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 40.dp, top = 4.dp)
                            )
                            habilidadeRoll?.let { roll ->
                                Text(
                                    text = habilidadeTexto[roll].orEmpty(),
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(start = 40.dp, top = 2.dp, end = 8.dp)
                                )
                            }
                        }

                        if (tropo.ganhaAoComprar.isNotEmpty()) {
                            Spacer(Modifier.size(6.dp))
                            FlowRow(
                                modifier = Modifier.padding(start = 36.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                vantagensNomeadas.forEach { nome ->
                                    AssistChip(
                                        onClick = {},
                                        enabled = false,
                                        label = { Text(nome) },
                                        colors = AssistChipDefaults.assistChipColors(
                                            disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer
                                        )
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
