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

/** Um pacote de efeitos já resolvido — string pronta pra entrar em
 * habilidades[]/desvantagensRaciais/vantagensGratis, reaproveitando os
 * mesmos pontos de injeção já usados no resto do app (ModifierEngine,
 * ResolveGrantedAncestryAdvantagesUseCase). Ver AnaoCiberTraits.kt para o
 * precedente desse padrão. */
data class ResolvedTraitPackage(
    val tracosParaAdicionar: List<String> = emptyList(),
    val tracosParaRemoverPorId: List<String> = emptyList(),
    val vantagensGratisParaAdicionar: List<String> = emptyList(),
    val desvantagensParaAdicionar: List<String> = emptyList(),
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
    // TARGET_ATTRIBUTE_OR_SKILL — injectionTemplate usa "{alvo}" como placeholder
    // (ex.: "{alvo} d6"). Escrito à mão por raça, não gerado automaticamente:
    // strings de injeção precisam ser verificadas contra os regex do
    // ModifierEngine caso a caso (ver nota em AnaoCiberTraits.kt sobre a
    // colisão "Aparar Baixo" vs "Aparar -1") — um template genérico "errado"
    // pode colidir com hardcode de outra raça ou simplesmente não casar com
    // nenhum regex.
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
