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

// Usado por SelectionDef.targetKind pra decidir o traço mecânico injetado
// por uma Seleção TARGET_ATTRIBUTE_OR_SKILL (ATTRIBUTE_BOOST ou SKILL_BOOST
// — ver ResolveAncestryVariantPackageUseCase.resolveSelection). SKILL ainda
// não é usado por nenhuma raça cadastrada (só ATTRIBUTE, ver
// AncestryVariantRegistry.meioOrc()/feral()), mas o enum já cobre os dois
// porque o mecanismo de resolução em si não distingue.
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
data class TraitAddition(
    val nome: String,
    val id: String,
    val vezes: Int = 1,
    // Override pra traços puramente de bookkeeping orçamentário — sem efeito
    // mecânico próprio, só um ajuste de pontos pra fechar o total de uma opção
    // de Variante contra outra (ex.: Elementais "Ar, Fogo ou Água" trocando
    // Forte+Resistência, 6 pts, por Forma de Energia, 4 pts, precisa de +2 pra
    // fechar igual à opção "Padrão"). 0/false preserva o comportamento de
    // sempre: pontos vêm de RacialTraitPointCatalog via `id`, traço visível.
    val pontos: Int = 0,
    val invisivel: Boolean = false,
    // Override do mecanismo parametrizado (RacialAbility.traitId/targetRef) —
    // usado por Seleções cujo efeito depende de um alvo ESCOLHIDO pelo
    // jogador (ex.: TARGET_ATTRIBUTE_OR_SKILL: "ATTRIBUTE_BOOST" + targetRef
    // = o atributo escolhido), onde `id` sozinho não é suficiente pra
    // RacialTraitPointCatalog.efeitoDe() resolver o efeito certo. `id`
    // continua sendo a identidade estável do traço (dedup/remoção); quando
    // `traitId` está presente, RacialAbility.resolvedTraitId() o prioriza
    // pra leitura do efeito mecânico, mas note que o custo em pontos
    // (ResolveVariantPointBudgetUseCase/ValidateAncestryOptionBudgetsUseCase)
    // ainda lê `id` cru — por isso quem usa `traitId` aqui também deve
    // preencher `pontos` explicitamente, não confiar em CUSTOS[id].
    val traitId: String? = null,
    val targetRef: String? = null,
    // Idem — override de RacialAbility.value (passos acima de d4; ver
    // RacialTraitEffect.AtributoStep/PericiaStep.passos) pros mesmos casos
    // de `traitId`/`targetRef` acima, quando o passo não é o padrão 1 (d6
    // de atributo, d4 de perícia) — ex.: Usagimimi "Definido pelo Ofício"
    // concede perícia d6 (passos=2), não d4.
    val value: Int = 1
)

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
    // Traços narrativos NEGATIVOS que não são Complicação de verdade (ex.:
    // Fraqueza Ambiental, Penalidade em Cavalgar) — categoria
    // "racial_trait_negative" em habilidades[], igual a Chifres/Cabeça Dura
    // do lado positivo. Diferente de `desvantagensParaAdicionar`, que é só
    // pra Complicação real do catálogo (complicacoes.json).
    val tracosNegativosParaAdicionar: List<TraitAddition> = emptyList(),
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
    // placeholder de exibição (ex.: "{alvo} d6"). resolveSelection() injeta
    // um TraitAddition com traitId="ATTRIBUTE_BOOST"/"SKILL_BOOST" (conforme
    // targetKind) + targetRef=o alvo escolhido pelo jogador (answer.targetChoice,
    // validado contra targetOptions; sem resposta, cai no primeiro de
    // targetOptions) — RacialTraitPointCatalog.efeitoDe() já sabe resolver
    // esse par (traitId, targetRef) em RacialTraitEffect.AtributoStep/
    // PericiaStep dinâmico, o mesmo mecanismo que MonstroTemplate.kt já usa
    // pra atributo de monstro. Usado por Meio-Orc (Fantasia, Força-ou-Vigor)
    // e Feral (Arte da Guerra, Força/Vigor/Agilidade) — ver
    // AncestryVariantRegistry.meioOrc()/feral().
    val targetKind: TraitTargetKind? = null,
    val targetOptions: List<String>? = null, // null = qualquer atributo/perícia
    // Alvo default quando o jogador ainda não escolheu nada — o livro não
    // define um padrão universal (ex.: Meio-Orc "Endurecido" não diz Força
    // OU Vigor por padrão), então cada Seleção declara o seu aqui em vez de
    // um `?: "Vigor"` espalhado em CriadorState/na UI. Nulo cai no primeiro
    // de `targetOptions`.
    val defaultTargetChoice: String? = null,
    // Quantos passos acima de d4 o alvo escolhido recebe — mesma unidade
    // que o laço genérico de resolução ao vivo usa (CriadorState.
    // atributoBaseRacial/periciaStartRawInternal: `4 + passos*2`), então
    // passos=0 é d4 e passos=1 é d6. ATTRIBUTE_BOOST sempre usa passos>=1
    // (atributo já nasce em d4, não faz sentido "subir pra d4"; o padrão 1
    // cobre Meio-Orc/Feral/Minerador, todos "d6 à escolha"). SKILL_BOOST
    // usa passos=0 quando o livro diz só "começam com d4" (perícia nasce
    // DESTREINADA, então d4 já É o primeiro patamar — ex.: Kitsunemimi
    // Preparado, Gnomo Obsessivos) e passos=1 quando o livro diz "d6"
    // (ex.: Usagimimi Definido pelo Ofício). O custo em pontos vem de
    // RacialTraitPointCatalog.custoDe() com este valor (ATTRIBUTE_BOOST=
    // passos*2, SKILL_BOOST=passos+1 — a mesma fonte única que já calibra
    // o catálogo oficial: pericia_racial_d4=1pt/passos=0,
    // pericia_racial_d6=2pt/passos=1), nunca hardcoded aqui.
    val passos: Int = 1,
    val injectionTemplate: String? = null,
    // BUDGETED_CATALOG — delega pro catálogo existente (ex.: AnaoCiberTraitCatalog)
    val catalogId: String? = null,
    // FIXED_PACKAGE
    val pacotesFixos: List<FixedPackageOption>? = null,
    // Id do traço (habilidades[] da raça BASE) que sinaliza "esta Seleção
    // está ativa nesta raça" e é substituído/complementado pelo traço já
    // resolvido — mesmo papel que HERANCA/SIGNOS_DE_NASCENCA/ENDURECIDO/
    // PRIMITIVO já cumpriam como "if" por id espalhado em
    // CriadorState.applyAncestryVariantAdjustments, agora declarado aqui
    // pra virar dado, não código, por Seleção. Nulo pra Seleções resolvidas
    // por outro caminho (ex.: aninhadas dentro de uma VariantOption, como
    // Humanos Sci-Fi "Minerador" — já gated pelo variantOptionId escolhido).
    val marcadorTraitId: String? = null,
    // true mantém o traço-marcador visível em habilidades[] depois de
    // resolvido (ex.: SIGNOS_DE_NASCENCA — o card de referência dos 13
    // Signos é útil mesmo com uma opção ativa). false (padrão) remove o
    // marcador, como HERANCA já fazia.
    val manterMarcadorVisivel: Boolean = false
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
 * estável (keyify do nome) + `livro` (mesmo vocabulário de
 * `canonicalOriginKey()`: "SCI_FI", "FANTASIA", "ARTE_DA_GUERRA" etc.), não
 * pelo texto de exibição. O par (livro, ancestralidadeId) existe porque
 * várias raças de livros diferentes compartilham o mesmo nome de exibição
 * (ex.: "HUMANOS" existe tanto no Sci-Fi quanto no Fantasia, cada um com
 * suas próprias Variantes) — sem o livro, a segunda registrada sobrescreveria
 * a primeira no mapa. Cada livro tem seu próprio conjunto de Variantes
 * registradas; nada aqui é compartilhado entre livros. */
data class AncestryVariantConfig(
    val ancestralidadeId: String,
    val livro: String,
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
