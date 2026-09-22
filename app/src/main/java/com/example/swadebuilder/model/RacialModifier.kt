package com.example.swadebuilder.model

import com.example.swadebuilder.toDiceString
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.semAcentos
import com.example.swadebuilder.util.toFancyTitleCase
import kotlinx.serialization.Serializable

@Serializable
data class RacialAbility(
    val nome: String,
    val descricao: String,
    // Resumo genérico para a edição Lite (não reproduz o texto do livro original).
    val descricaoLite: String? = null,
    val id: String? = null,
    val category: String? = null,
    val severity: String? = null,

    // CAMADA MECÂNICA / ENGINE PARAMETRIZADA
    val traitId: String? = null,           // Ex: "ATTRIBUTE_BOOST", "SKILL_BOOST", "GRANTED_EDGE", "RACIAL_HINDRANCE", "TOUGHNESS_FLAT", "PACE_CHANGE", "SIZE_CHANGE", "NATURAL_ARMOR"
    val targetRef: String? = null,         // Ex: "AGILIDADE", "VIGOR", "PERCEBER", "SORTUDO", "FORASTEIRO"
    val value: Int = 1,                    // Passos ou modificadores numéricos (ex: 1 para d6, +2 para movimentação)
    val pontos: Int = 0,                   // Valor no orçamento racial (ex: +2, -2)
    val invisivel: Boolean = false,        // Traços ocultos da UI/card (ex: bônus interno de carga/armadura)

    // Quantas vezes este traço foi "comprado"
    val vezes: Int = 1,
    // Ataque(s) natural(is) concedido(s) por esta habilidade racial
    val armasNaturais: List<ArmaNatural> = emptyList()
) {
    /** Retorna o ID mecânico principal — priorizando o novo `traitId` parametrizado, ou o `id` legado. */
    fun resolvedTraitId(): String = traitId ?: id ?: ""

    /**
     * Retorna o valor real de pontos no orçamento racial para este traço — já multiplicado
     * por `vezes` (traços empilháveis, ex.: Movimentação (2) comprado 2x = +4 pontos, não
     * +2). Antes não multiplicava: a aba Ancestralidades mostrava o custo de UMA compra só
     * mesmo em traços comprados várias vezes (bug relatado pelo usuário com Centauro:
     * Movimentação +4 aparecia como "+2 pts"). custoDe() já trata `pontos != 0` como
     * override antes de cair no id — não precisa checar aqui de novo. Mesma fórmula de
     * ResolveVariantPointBudgetUseCase.habilidadeComoItem (custo do editor de Variante),
     * que já multiplicava — as duas ficavam divergentes.
     */
    fun resolvedPontos(allVantagens: List<Vantagem> = emptyList()): Int =
        RacialTraitPointCatalog.custoDe(
            resolvedTraitId(), value, severity, pontos,
            targetRef = targetRef, nome = nome, allVantagens = allVantagens
        ) * vezes
}

@Serializable
data class RacialModifier(
    val id: String? = null,
    val nome: String,
    val originalName: String? = null,
    val originalDescription: String? = null,
    val descricao: String? = null,
    val habilidades: List<RacialAbility> = emptyList(),
    val origem: String = "BASICO",
    val movimentacao: Int = 0,
    val tags: List<String> = emptyList(),
    val opcoes: List<String> = emptyList(),
    val especieId: String? = null,
    // Quase toda raça oficial fecha em ResolveVariantPointBudgetUseCase.
    // DEFAULT_ORCAMENTO (2) — a mesma calibração que o livro usa pra
    // qualquer ancestralidade padrão. Um punhado de cenários avisa
    // explicitamente que usa um orçamento MAIOR pra tudo (ex.: o próprio
    // exemplo "Celestiais e Guardiões" do Básico, pág. 23: "terão +4 pontos
    // em habilidades raciais em vez do +2 habitual"; Crystal Heart segue o
    // mesmo padrão — cada origem já vem com Adaptável + um atributo
    // aumentado, 4 pontos por design). Guardar isso aqui (por raça, não por
    // livro inteiro) deixa o editor de Variante já abrir com o orçamento
    // certo pra essa raça base, em vez do Mestre precisar descobrir sozinho
    // que precisa marcar "Sem limite de pontos".
    val pontosRaciaisEsperados: Int = 2
) {
    /**
     * Retorna vantagens grátis concedidas por traços `racial_edge`/`GRANTED_EDGE` em
     * `habilidades` — delega pra [vantagensGratisEfetivas] (função de topo de arquivo, mesma
     * regra de prioridade de `targetRef` e mesmo dedup por [racialGrantDedupeKey]) em vez de
     * reimplementar a leitura aqui; ver [resolvedDesvantagens] pro motivo desta função existir
     * como método além da função de topo (histórico de call sites já escritos contra o método).
     */
    fun resolvedVantagensGratis(): List<String> = vantagensGratisEfetivas(habilidades)

    /**
     * Retorna desvantagens raciais concedidas por traços `racial_hindrance`/`RACIAL_HINDRANCE`
     * em `habilidades` — delega pra [desvantagensEfetivas]. Antes reimplementava a leitura e,
     * ao contrário do irmão [resolvedVantagensGratis], nunca priorizava `targetRef` no ramo
     * `category == "racial_hindrance"` (só olhava `hab.nome`) — então uma Complicação
     * reskinada por `targetRef` (ex.: Draconianos "Mal-Humorado" concedendo "Arrogante") virava
     * a desvantagem automática com o nome de EXIBIÇÃO da raça em vez do nome real do catálogo
     * de Complicações, quebrando o vínculo com `complicacoes.json`. Delegar corrige os dois call
     * sites reais (`CriadorState.aplicarAncestralidade`/`ComplicacoesSection.kt`) de uma vez.
     */
    fun resolvedDesvantagens(): List<String> = desvantagensEfetivas(habilidades)
}

@Serializable
data class HabilidadeCriacao(
    val nome: String,
    val custo: Int,
    val descricao: String,
    // Resumo genérico para a edição Lite (não reproduz o texto do livro original).
    val descricaoLite: String? = null,
    // Id estável pra Traços Raciais customizados (ver SettingsDialog.kt) — o catálogo oficial
    // (basico_habilidades_raciais.json) continua identificado por `nome` em todo o app, então
    // esse campo é aditivo e não muda a regra de identidade.
    val id: String? = null,
    // Só preenchido nas ENTRADAS DO CATÁLOGO (basico_habilidades_raciais.json)
    // — quantas vezes o livro permite comprar este traço (ver
    // RacialAbility.vezes pro mesmo conceito do lado "traço já escolhido").
    // null/1 = uma vez só; -1 = "S", sem limite no livro (a UI ainda precisa
    // de um teto prático — ver SettingsDialog.kt). Vazio numa instância já
    // ESCOLHIDA (dentro de tracosAdicionados/selectedRacialTraits): ali quem
    // importa é `vezes`, não `vezesMax`.
    val vezesMax: Int? = null,
    // Só preenchido nas instâncias JÁ ESCOLHIDAS: quantas vezes este traço
    // específico foi comprado (custo/efeito já vêm multiplicados por este
    // valor em `custo` e, na hora de virar RacialAbility, aqui também).
    val vezes: Int = 1,
    // Só preenchido nas ENTRADAS DO CATÁLOGO: id compartilhado por várias
    // entradas que são, no livro, o MESMO traço "(1)" (pega uma vez só) com
    // custo/efeito variando por versão — ex.: "Ações Adicionais" custa 4, 5
    // ou 10 pontos conforme a versão (Sci-Fi condicional, Básico/Sci-Fi
    // padrão, Fantasia "Maior" — ver docs/swade_basico|fantasia|scifi).
    // Diferente de vezesMax (que soma o MESMO efeito várias vezes): aqui são
    // versões ALTERNATIVAS e mutuamente exclusivas do mesmo traço — o
    // seletor de Traços Raciais (SettingsDialog.kt) mostra as entradas com o
    // mesmo grupoEscolha como uma linha só, abrindo "Qual versão?" em vez de
    // "Quantas vezes?".
    val grupoEscolha: String? = null,
    // Efeito mecânico parametrizado opcional (mesma "CAMADA MECÂNICA /
    // ENGINE PARAMETRIZADA" de RacialAbility — ver esses três campos lá).
    // Hoje só exposto no editor de Traço Racial (SettingsDialog.kt) pros
    // dois tipos de bônus/penalidade de Pontos de Perícia/Atributo
    // (PERICIA_POINTS_BONUS/ATRIBUTO_POINTS_BONUS, ver RacialTraitEffect);
    // null = traço sem efeito numérico modelado (só flavor + custo), como
    // todo traço customizado antes destes três campos existirem.
    val traitId: String? = null,
    val targetRef: String? = null,
    val value: Int = 0
) {
    fun exibida(): HabilidadeCriacao =
        if (!com.example.swadebuilder.EditionConfig.isFullEdition && !descricaoLite.isNullOrBlank()) copy(descricao = descricaoLite) else this
}

data class RacialAbilitySignature(val nome: String, val descricao: String)

// Só `habilidades` — atributo/perícia aumentados não são mais um mapa numérico
// à parte de RacialModifier, então já estão implícitos no texto (nome +
// descrição) do traço correspondente em habilidades[] (ex.: "Resistente"
// descreve "começam com um d6 em Vigor"); duas raças com bônus numéricos
// diferentes já produzem traços com texto diferente, então a fusão continua
// tolerante só a diferenças puramente cosméticas de formatação, igual antes.
data class RacialSignature(
    val habilidades: List<RacialAbilitySignature>
)

fun RacialModifier.signature(): RacialSignature =
    RacialSignature(
        habilidades = habilidades
            .map { RacialAbilitySignature(it.nome, it.descricao) }
            .sortedWith(compareBy({ it.nome.uppercase().semAcentos() }, { it.descricao.uppercase().semAcentos() }))
    )

fun stripAncestralidadeScenarioSuffix(nome: String): String =
    nome.replace(Regex("\\s*\\([^)]*\\)\\s*$"), "").trim()

/**
 * Colapsa candidatos de ancestralidade — possivelmente vindos de múltiplos livros ativos ao
 * mesmo tempo (Modo Livre, ou um livro companheiro somado ao Básico) — em grupos prontos para
 * exibição em uma lista de seleção de raça.
 *
 * Duas etapas:
 * 1. Por nome exato: quando o mesmo nome existe em mais de um livro ativo, mantém a versão do
 *    livro de maior [originPriority] (em vez da primeira do arquivo), já que livros de
 *    cenário/companheiros costumam trazer uma versão mais específica que o Básico.
 * 2. Por (nome-base sem sufixo de cenário, assinatura mecânica): funde apenas quando AMBOS
 *    coincidem, nunca só a assinatura — duas raças diferentes podem ter atributos/perícias/
 *    habilidades idênticos por coincidência (um livro reaproveitando o mesmo bloco de
 *    habilidades pra duas entradas com nomes distintos) sem serem a mesma raça. Exigir o nome-base também
 *    preserva a fusão legítima de variantes de nome da mesma raça entre livros (ex.: "Humano"
 *    e "Humano (Buscatrilha)").
 */
fun groupAncestralidadesForDisplay(items: List<RacialModifier>): List<List<RacialModifier>> {
    val prioritized = items.groupBy { it.nome.keyify() }
        .map { (_, duplicates) ->
            if (duplicates.size == 1) duplicates.first() else duplicates.maxBy { originPriority(it.origem) }
        }

    return prioritized
        .groupBy { stripAncestralidadeScenarioSuffix(it.nome).keyify() to it.signature() }
        .values
        .toList()
}

/**
 * Uma Vantagem/Complicação grátis vem sempre embutida numa habilidade
 * (`category == "racial_edge"`/`"racial_hindrance"`, opcionalmente com
 * `traitId="GRANTED_EDGE"/"RACIAL_HINDRANCE"` + `targetRef` quando o `nome`
 * é só skin do livro) — tanto pra Ancestralidade (`RacialAbility`) quanto
 * pro Template de Monstro Heroico (`MonstroHabilidade`, ver
 * `MonstroTemplate.paraCaracteristicas()`). Nenhum dos dois usa mais lista
 * solta de nomes/ids à parte.
 */
private val racialGrantSeveritySuffixRegex = Regex("""\s*\((MAIOR|MENOR)\)\s*$""")

// Chave de dedup tolerante a diferenças de formatação puramente cosméticas entre a
// mesma vantagem/desvantagem grátis representada de duas formas — id vs nome (ex.:
// "ANTECEDENTE_ARCANO_MILAGRES" vs "Antecedente Arcano (Milagres)") ou nome cru vs
// nome com sufixo de gravidade re-anexado (ex.: "Desastrado" vs "Desastrado (Menor)").
// Reduz a string a só letras/números maiúsculos sem acento — duas grafias que só
// diferem em espaço/underscore/parênteses/pontuação caem na mesma chave.
fun String.racialGrantDedupeKey(): String =
    keyify().replace(racialGrantSeveritySuffixRegex, "").filter { it.isLetterOrDigit() }

fun vantagensGratisEfetivas(habilidades: List<RacialAbility>): List<String> =
    habilidades.filter { it.category == "racial_edge" }.map { hab ->
        // targetRef (traitId=GRANTED_EDGE) tem prioridade — é o id/nome real
        // da Vantagem quando `nome` é só skin (ex.: Kitsunemimi "Socialmente
        // Sofisticados" concedendo "Cativar o Ambiente"). Sem targetRef, cai
        // pro id ?: nome de sempre (raças antigas onde os dois já coincidem).
        hab.targetRef?.takeIf { it.isNotBlank() } ?: hab.id ?: hab.nome
    }.distinctBy { it.racialGrantDedupeKey() }

fun desvantagensEfetivas(habilidades: List<RacialAbility>): List<String> =
    habilidades.filter { it.category == "racial_hindrance" }.map { hab ->
        // targetRef (traitId=RACIAL_HINDRANCE) tem prioridade, mesmo motivo
        // do caso GRANTED_EDGE acima (skin de nome, ex.: Kitsunemimi
        // "Excessivamente Detalhistas" concedendo "Cauteloso").
        val base = hab.targetRef?.takeIf { it.isNotBlank() } ?: hab.nome
        val sev = hab.severity
        if (sev != null && !base.contains("($sev)", ignoreCase = true)) {
            "$base ($sev)"
        } else {
            base
        }
    }.distinctBy { it.racialGrantDedupeKey() }

/**
 * Monta a lista "Características" da aba Ancestralidades inteiramente a
 * partir de dado estruturado — nunca de `RacialAbility.descricao`. A raça não
 * "diz" o que tem em texto livre; ela só carrega habilidades (atributo/
 * perícia aumentados via `AtributoStep`/`PericiaStep`, vantagens/complicações
 * grátis via ids/nomes já existentes, e um id por habilidade solta), e é só
 * esse conjunto que essa função lê. Rótulos de exibição vêm de
 * `RacialTraitPointCatalog.LABEL` por id — se um id não tem entrada lá, cai no
 * `nome` cru da habilidade (nunca na descrição longa), como ponte até o
 * catálogo de rótulos cobrir mais ids.
 *
 * Ex. Elfo (Básico): habilidades=[Ágil(id=AGIL, resolve pra
 * AtributoStep(Agilidade)), Desastrado(id=DESASTRADO,
 * category=racial_hindrance), Visão no Escuro(id=VISAO_NO_ESCURO)] produz:
 * ["Atributo aumentado: Agilidade (d6)", "Complicação racial menor:
 * Desastrado", "Visão no Escuro"] — Ágil não vira linha própria SEGUNDA vez
 * na varredura final porque seu id já virou a primeira linha acima;
 * Desastrado não vira linha própria porque sua categoria já virou a segunda
 * linha via `desvantagens`.
 */
object RacialCaracteristicasResolver {

    fun resolver(habilidades: List<RacialAbility>, allVantagens: List<Vantagem> = emptyList()): List<String> {
        val linhas = mutableListOf<String>()

        fun formatPts(pts: Int): String = when {
            pts > 0 -> " (+$pts pts)"
            pts < 0 -> " ($pts pts)"
            else -> ""
        }

        // Atributo/Perícia racial aumentados: lidos direto de habilidades[] via
        // AtributoStep/PericiaStep — RacialModifier não carrega mais os mapas
        // numéricos estáticos `atributos`/`pericias` que faziam isso em paralelo
        // (removidos: a mesma informação já vinha, redundante, de um traço com id
        // resolvível — ver auditoria da migração; Elementais [Sci-Fi] é o único
        // caso tratado à parte, direto em CriadorState.atributoBaseRacial()).
        habilidades.forEach { hab ->
            val efeito = RacialTraitPointCatalog.efeitoDe(hab.resolvedTraitId(), hab.targetRef, hab.value)
            if (efeito is RacialTraitEffect.AtributoStep) {
                val delta = efeito.passos * 2
                val dado = (4 + delta).toDiceString()
                val verbo = if (delta >= 0) "aumentado" else "reduzido"
                linhas += "Atributo $verbo: ${efeito.atributo.toFancyTitleCase()} ($dado)${formatPts(delta)}"
            }
        }
        habilidades.forEach { hab ->
            val efeito = RacialTraitPointCatalog.efeitoDe(hab.resolvedTraitId(), hab.targetRef, hab.value)
            if (efeito is RacialTraitEffect.PericiaStep) {
                val dado = (4 + efeito.passos * 2).toDiceString()
                // Custo real (não um formato fixo por passo): perícias básicas têm
                // desconto — ver RacialTraitPointCatalog.custoDe/CUSTOS.
                linhas += "Perícia inicial: ${efeito.pericia.toFancyTitleCase()} ($dado)${formatPts(hab.resolvedPontos(allVantagens))}"
            }
        }

        vantagensGratisEfetivas(habilidades)
            .filterNot { it.keyify() == Constants.ID_AA_AGENT_SYN.keyify() }
            .forEach { entrada ->
                val cleanName = entrada.replace(Regex("(?i)^Vantagem\\s+(Racial|Grátis):\\s*"), "").trim()
                // Custo real da Vantagem concedida (por Estágio, ver
                // RacialTraitPointCatalog.custoDeVantagem) em vez do +2 fixo de antes — mesmo
                // achado do custoDe("GRANTED_EDGE") acima: sem a Vantagem resolvível no
                // catálogo (allVantagens vazio, ou nome sem match), mantém o fallback de 2.
                val chaveVantagem = entrada.keyify()
                val vantagemConcedida = allVantagens.firstOrNull { it.id.keyify() == chaveVantagem }
                    ?: allVantagens.firstOrNull { it.nome.keyify() == chaveVantagem }
                val pts = vantagemConcedida?.let { RacialTraitPointCatalog.custoDeVantagem(it) } ?: 2
                linhas += "Vantagem Racial: ${cleanName.toFancyTitleCase()}${formatPts(pts)}"
            }

        desvantagensEfetivas(habilidades).forEach { entrada ->
            val cleanName = entrada.replace(Regex("(?i)^Complicação\\s+(Racial|Maior|Menor):\\s*"), "").trim()
            val match = Regex("""^(.*?)\s*\((Maior|Menor)\)$""").find(cleanName)
            if (match != null) {
                val (nome, severidade) = match.destructured
                val pts = if (severidade.equals("Maior", ignoreCase = true)) -2 else -1
                linhas += "Complicação ${severidade.toFancyTitleCase()}: ${nome.toFancyTitleCase()}${formatPts(pts)}"
            } else {
                val pts = if (entrada.contains("Maior", ignoreCase = true)) -2 else -1
                linhas += "Complicação Racial: ${cleanName.toFancyTitleCase()}${formatPts(pts)}"
            }
        }

        habilidades.filter { !it.invisivel }.forEach { hab ->
            val tid = hab.resolvedTraitId().uppercase()
            if (hab.category == "racial_hindrance" || hab.category == "racial_edge" || tid == "RACIAL_HINDRANCE" || tid == "GRANTED_EDGE") return@forEach
            val id = hab.id?.keyify()
            val efeito = RacialTraitPointCatalog.efeitoDe(id, hab.targetRef, hab.value)
            if (efeito is RacialTraitEffect.AtributoStep || efeito is RacialTraitEffect.PericiaStep) return@forEach
            // labelComVezes escala o rótulo (ex.: "Tamanho +1" -> "Tamanho +3" pra
            // Meio-Gigantes, vezes=3) quando o id tem LABEL cadastrado; sem LABEL,
            // cai no nome cru da habilidade, igual antes (nunca no id em si).
            val rotulo = id?.takeIf { RacialTraitPointCatalog.LABEL.containsKey(it) }
                ?.let { RacialTraitPointCatalog.labelComVezes(it, hab.vezes) }
                ?: hab.nome.toFancyTitleCase()
            val pts = hab.resolvedPontos(allVantagens)
            linhas += "$rotulo${formatPts(pts)}"
        }

        return linhas
    }
}
