package com.example.swadebuilder.model

/**
 * Modelo unificado de "Variante" e "Seleção" de ancestralidade.
 *
 * Terminologia (definida junto com o usuário, não inventada por conveniência
 * de código):
 * - Ancestralidade: a raça em si (ex.: Anões, Umvee, Feral). Sempre existe.
 * - Variante: o MESTRE reconfigura a raça pra um cenário/mesa (ex.: Anões
 *   "Ciber", Seres Sintéticos "Máquina (Procurado)"). É opcional — a maioria
 *   das raças não tem nenhuma. Só fica visível quando a regra de livro
 *   "Variantes de Raça" está ligada (desligada por padrão).
 * - Seleção: o JOGADOR escolhe, na criação do próprio personagem, dentro de
 *   opções que a raça (ou a variante escolhida) já oferece (ex.: Umvee
 *   escolhe 1 de 6 Dons da Natureza; Meio-Orc escolhe Força ou Vigor; Anão
 *   Ciber escolhe até 2 pontos de traços negativos). Sempre visível,
 *   independente do toggle de Variantes de Raça.
 *
 * Um traço "raça com Básico/Padrão entre as opções" indica Variante (a opção
 * Básico/Padrão É a raça normal, as outras reconfiguram pra um cenário). Sem
 * Básico/Padrão entre as opções, é Seleção (o jogador tem que escolher algo,
 * não existe "raça normal" à parte).
 */

// ATTRIBUTE/SKILL ainda não são referenciados: nenhuma raça cadastrada usa
// SelectionType.TARGET_ATTRIBUTE_OR_SKILL hoje (ver o comentário em
// ResolveAncestryVariantPackageUseCase.resolveSelection) — o desenho de id
// pra esse tipo de alvo ainda está pendente. Suprimido em vez de removido.
@Suppress("unused")
enum class TraitTargetKind { ATTRIBUTE, SKILL }

/** Como uma Seleção resolve a escolha do jogador em efeito mecânico. */
enum class SelectionType {
    /** Escolhe 1 atributo OU 1 perícia de uma lista (ou livre) para receber
     * o efeito do traço (ex.: Meio-Orc Força-ou-Vigor, Gnomo qual perícia). */
    TARGET_ATTRIBUTE_OR_SKILL,

    /** Escolhe N itens de um catálogo, respeitando um orçamento de pontos
     * e/ou contagem máxima (ex.: Anão Ciber, até 2 pontos de traços
     * negativos, nenhum maior que -2). */
    BUDGETED_CATALOG,

    /** Escolhe 1 pacote inteiro, já com efeitos fixos, entre N opções
     * nomeadas (ex.: Umvee escolhe 1 de 6 Dons da Natureza; Descendente
     * Elemental escolhe 1 elemento; Terracota escolhe Voto ou Obrigação). */
    FIXED_PACKAGE
}

/** Um traço/vantagem/desvantagem a injetar: `nome` é o texto de exibição
 * (o mesmo que já entrava solto em habilidades[]/desvantagensRaciais/
 * vantagensGratis), `id` é o id mecânico ESCRITO À MÃO aqui — nunca
 * derivado do texto em tempo de execução. Antes desta classe, o texto de
 * exibição sozinho chegava ao ModifierEngine, que precisava rodar
 * `String.autoTraitId()` sobre ele pra descobrir se batia com algum id de
 * `RacialTraitPointCatalog.EFEITOS` (ex.: "RESISTÊNCIA +2" -> "RESISTENCIA_2",
 * um id inventado por valor final que nem existe mais — ver `vezes` abaixo)
 * — um "tradutor" de texto em tempo real. Com `id` explícito por entrada,
 * esse tradutor deixou de existir: cada Variante/Seleção já diz, no próprio
 * código-fonte, qual é o id mecânico do traço que está concedendo, do mesmo
 * jeito que qualquer entrada de `habilidades[]` em ancestralidades.json já
 * fazia. Quando o traço não tem efeito numérico modelado (puramente
 * narrativo, ex.: "Garras", "Visão no Escuro"), o id ainda existe — só não
 * bate com nenhuma chave de EFEITOS, exatamente como já acontecia com
 * habilidades sem efeito mecânico. */
data class TraitAddition(val nome: String, val id: String, val vezes: Int = 1)

/** Id + contagem de compras de um traço empilhável (ver RacialTraitPointCatalog.
 * VEZES_MAX) já resolvido por Variante/Seleção — a versão "sem nome de
 * exibição" de [TraitAddition], usada só pra threading do id até o
 * ModifierEngine (ver ResolveAncestryRacialPackageUseCase.Result.racialTraitIds/
 * CriadorState.racialTraitIdsFromVariants). */
data class RacialTraitStack(val id: String, val vezes: Int = 1)

/** Um pacote de efeitos já resolvido — pronto pra entrar em
 * habilidades[]/desvantagensRaciais/vantagensGratis, reaproveitando os
 * mesmos pontos de injeção já usados no resto do app (ModifierEngine,
 * ResolveGrantedAncestryAdvantagesUseCase). Ver AnaoCiberTraits.kt para o
 * precedente desse padrão. */
data class ResolvedTraitPackage(
    val tracosParaAdicionar: List<TraitAddition> = emptyList(),
    val tracosParaRemoverPorId: List<String> = emptyList(),
    val vantagensGratisParaAdicionar: List<TraitAddition> = emptyList(),
    // Ids de Vantagem (vantagens.json) que devem ser garantidas, em vez de
    // casadas por nome — ex.: "poderes_misticos" (Oráculos Aterrorizado).
    val vantagensGratisIds: List<String> = emptyList(),
    val desvantagensParaAdicionar: List<TraitAddition> = emptyList(),
    // Nomes de traços/vantagens automáticas da raça base que esta opção
    // substitui/revoga (ex.: Aquarianos Semi-aquáticos remove "Aquático").
    val tracosParaRemoverPorNome: List<String> = emptyList(),
    // Complicações raciais da raça base que esta opção substitui/revoga
    // (ex.: Centaux Gazela remove "Grande").
    val desvantagensParaRemover: List<String> = emptyList(),
    // Sobrescreve a Armadura Natural da raça quando esta opção muda o valor
    // padrão (ex.: Insetoides Vespa perde a Armadura +2 do Padrão).
    val naturalArmor: Int = 0,
    // Ataque(s) natural(is) que esta Variante concede — dado estruturado
    // (dano/PA prontos), mesmo tipo que RacialAbility.armasNaturais usa pra
    // raça base. Existe porque nem toda arma natural de Variante já está no
    // JSON da raça (ex.: Sáurios "Mordida" só existe pra Básico, nunca foi
    // um campo fixo em ancestralidades.json) — sem isso, CriadorState só
    // conseguia achar essas armas por casamento de palavra-chave em texto.
    val armasNaturaisParaAdicionar: List<ArmaNatural> = emptyList(),
    val anotacoes: List<String> = emptyList()
)

data class FixedPackageOption(
    val id: String,
    val nome: String,
    val pacote: ResolvedTraitPackage
)

data class SelectionDef(
    val id: String,
    val rotulo: String,
    val tipo: SelectionType,
    // TARGET_ATTRIBUTE_OR_SKILL — injectionTemplate usa "{alvo}" como
    // placeholder de exibição (ex.: "{alvo} d6"), mas nenhuma raça cadastrada
    // no registro usa este tipo hoje: um traço de alvo ESCOLHIDO pelo
    // jogador (ex.: Meio-Orc Força-ou-Vigor) precisaria de um efeito
    // mecânico dinâmico (o atributo/perícia certo, não um id fixo do
    // RacialTraitEffect), que ainda não foi desenhado. Até existir esse
    // desenho, ResolveAncestryVariantPackageUseCase.resolveSelection não
    // resolve este tipo (retorna null) — não reintroduzir aqui um id
    // derivado do texto do template como solução provisória.
    val targetKind: TraitTargetKind? = null,
    val targetOptions: List<String>? = null, // null = qualquer atributo/perícia
    val injectionTemplate: String? = null,
    // BUDGETED_CATALOG — delega pro catálogo existente (ex.: AnaoCiberTraitCatalog)
    val catalogId: String? = null,
    // FIXED_PACKAGE
    val pacotesFixos: List<FixedPackageOption>? = null
)

data class VariantOption(
    val id: String,
    val nome: String,
    val oficial: Boolean = true, // false = criada via conteúdo customizado
    val pacoteFixo: ResolvedTraitPackage = ResolvedTraitPackage(),
    val selecoes: List<SelectionDef> = emptyList()
)

data class VariantGroup(
    val opcoes: List<VariantOption>
)

/** Config de variante/seleção de uma ancestralidade — indexada por id
 * estável (keyify do nome), não pelo texto de exibição. */
data class AncestryVariantConfig(
    val ancestralidadeId: String,
    val grupoVariante: VariantGroup? = null,
    val selecoes: List<SelectionDef> = emptyList()
)

/** Resposta do jogador a uma Seleção específica. */
data class SelectionAnswer(
    val selectionId: String,
    val targetChoice: String? = null,          // TARGET_ATTRIBUTE_OR_SKILL
    val catalogChoices: List<String> = emptyList(), // BUDGETED_CATALOG (ids do catálogo)
    val fixedPackageChoiceId: String? = null   // FIXED_PACKAGE
)
