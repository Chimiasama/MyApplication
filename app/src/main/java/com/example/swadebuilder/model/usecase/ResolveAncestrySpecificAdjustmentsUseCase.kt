package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.AnaoCiberTraitCatalog
import com.example.swadebuilder.model.AnaoCiberTraitSelection
import com.example.swadebuilder.model.AncestryVariantConfig
import com.example.swadebuilder.model.RacialTraitEffect
import com.example.swadebuilder.model.RacialTraitPointCatalog
import com.example.swadebuilder.model.ResolvedTraitPackage
import com.example.swadebuilder.model.SelectionAnswer
import com.example.swadebuilder.model.SelectionDef
import com.example.swadebuilder.model.TraitAddition
import com.example.swadebuilder.model.canonicalOriginKey
import com.example.swadebuilder.registry.AncestryVariantRegistry
import com.example.swadebuilder.util.keyify

class ResolveAncestrySpecificAdjustmentsUseCase(
    private val resolveAncestryVariantUseCase: ResolveAncestryVariantUseCase = ResolveAncestryVariantUseCase(),
    private val resolveAncestryVariantPackageUseCase: ResolveAncestryVariantPackageUseCase = ResolveAncestryVariantPackageUseCase()
) {

    /**
     * Ponte temporária entre o texto de variante hoje armazenado em
     * `scifiVariant` (ex.: "Voto (Maior)") e o id estável do pacote fixo no
     * AncestryVariantRegistry (ex.: "voto") — casa pelo `nome` cadastrado no
     * registro, sem precisar de uma tabela de tradução separada. Isso é só
     * enquanto a Seleção ainda usa o mesmo armazenamento/UI da Variante
     * (unificar isso de vez é um passo à parte, ainda não feito).
     */
    private fun fixedPackageAnswerFrom(def: SelectionDef, displayValue: String?): SelectionAnswer {
        val matchId = def.pacotesFixos?.firstOrNull { it.nome.equals(displayValue, ignoreCase = true) }?.id
        return SelectionAnswer(selectionId = def.id, fixedPackageChoiceId = matchId)
    }

    /**
     * Casa a Variante efetiva (texto já normalizado por
     * ResolveAncestryVariantUseCase) com o id estável da VariantOption
     * correspondente no registro. Sem opção correspondente (ex.:
     * `effectiveVariant` nulo por falta de `ancestryOptions`), cai na opção
     * "Básico"/"Padrão" cadastrada — mesmo comportamento de fallback que o
     * "when" fixo tinha via `else`.
     */
    private fun variantOptionIdFrom(config: AncestryVariantConfig, effectiveVariant: String?): String? {
        val opcoes = config.grupoVariante?.opcoes ?: return null
        return opcoes.firstOrNull { it.nome.equals(effectiveVariant, ignoreCase = true) }?.id
            ?: opcoes.firstOrNull { it.nome.keyify() == "BASICO" || it.nome.keyify() == "PADRAO" }?.id
    }

    /**
     * Constrói o Result a partir do pacote resolvido no registro — só
     * traduz os campos genéricos (traços/vantagens/desvantagens a
     * adicionar/remover). Armadura Natural quando a Variante muda o valor
     * (ex.: Insetoides) já vem embutida em `naturalArmor`; `forceArmorZero`
     * é sempre true nesse grupo de raças (nenhuma delas mantém Armadura sem
     * forçar o reset primeiro), igual ao "when" fixo que este substitui.
     */
    private fun buildResultFromVariantRegistry(ancestralidadeId: String, effectiveVariant: String?): Result? {
        // Só chamado dentro de `isSciFiActive` (ver `ancKey in
        // AncestryVariantRegistry.scifiVariantDrivenKeys` abaixo) — todas as
        // raças desse conjunto são do Sci-Fi.
        val livro = "SCI_FI"
        val config = AncestryVariantRegistry.get(ancestralidadeId, livro) ?: return null
        val variantOptionId = variantOptionIdFrom(config, effectiveVariant) ?: return null
        val resolved = resolveAncestryVariantPackageUseCase.resolve(
            ancestralidadeId = ancestralidadeId,
            livro = livro,
            variantOptionId = variantOptionId,
            selectionAnswers = emptyList()
        )
        return Result(
            naturalArmorFromRace = resolved.naturalArmor,
            forceArmorZero = true,
            ensureAdvantageNames = resolved.vantagensGratisParaAdicionar.map { it.nome },
            ensureAdvantageIds = resolved.vantagensGratisIds,
            ensureAutomaticAdvantages = resolved.vantagensGratisParaAdicionar + resolved.tracosParaAdicionar,
            automaticAdvantagesToRemove = resolved.tracosParaRemoverPorNome,
            ensureRacialDisadvantages = resolved.desvantagensParaAdicionar,
            racialDisadvantagesToRemove = resolved.desvantagensParaRemover,
            elementalAction = ElementalAction.NONE,
            anotacoesToAdd = resolved.anotacoes
        )
    }

    enum class ElementalAction {
        NONE,
        SELECT_DEFAULT,
        REAPPLY_CURRENT
    }

    data class Result(
        val naturalArmorFromRace: Int,
        val forceArmorZero: Boolean,
        // Nomes de Vantagem (vantagens.json) casados por nome pra conceder a
        // Vantagem de verdade — ver ResolveAncestryRacialPackageUseCase.
        val ensureAdvantageNames: List<String>,
        val ensureAdvantageIds: List<String>,
        // Traços/vantagens automáticas que só entram como bookkeeping
        // (vantagensRaciais/ModifierEngine) — cada um já carrega seu id
        // mecânico explícito (ver TraitAddition), nunca derivado do texto
        // em tempo de execução.
        val ensureAutomaticAdvantages: List<TraitAddition>,
        val automaticAdvantagesToRemove: List<String> = emptyList(),
        val ensureRacialDisadvantages: List<TraitAddition>,
        val elementalAction: ElementalAction,
        val anotacoesToAdd: List<String> = emptyList(),
        val racialDisadvantagesToRemove: List<String> = emptyList()
    )

    fun execute(
        anc: String,
        descendenteElementalSelecionado: String?,
        anoesScifiSelecionado: String? = null,
        scifiVariant: String? = null,
        humanoMineradorAtributo: String? = null,
        humanoFantasiaSelecaoAninhada: String? = null,
        anaoCiberTracosSelecionados: List<AnaoCiberTraitSelection> = emptyList(),
        quadroidesTracoNegativoSelecionado: String? = null,
        ancestryOptions: List<String> = emptyList(),
        isSciFiActive: Boolean = false,
        isSciFiMechasActive: Boolean = false,
        ancestryOrigin: String = "BASICO",
        // Ids de habilidade[] da raça já resolvida (ver
        // ApplyAncestryChangeCoordinatorUseCase) — usado pra decidir Armadura
        // Natural por id de traço (ARMADURA), não pelo nome da raça no
        // "when" abaixo. Vazio quando o chamador não tem essa lista (ex.:
        // testes isolados deste use case). Sem contagem de "vezes" aqui de
        // propósito: nenhuma raça oficial hoje tem Armadura Natural além de
        // +2 (1 compra) — se um dia alguma precisar de +4/+6, este cálculo
        // (e o `racialAbilityIds` que o alimenta) precisa virar Map<String,
        // Int> igual a `racialTraitIdsFromVariants` do ModifierEngine.
        racialAbilityIds: Set<String> = emptySet()
    ): Result {
        val naturalArmorFromAbilityId = RacialTraitPointCatalog.EFEITOS
            .filterValues { it is RacialTraitEffect.ArmaduraBonus }
            .entries
            .firstOrNull { (id, _) -> id in racialAbilityIds }
            ?.let { (_, efeito) -> (efeito as RacialTraitEffect.ArmaduraBonus).valor }
            ?: 0
        val ancKey = anc.keyify()
        val effectiveVariant = if (ancestryOptions.isNotEmpty()) {
            resolveAncestryVariantUseCase.execute(
                ResolveAncestryVariantUseCase.Input(
                    selectedVariant = scifiVariant,
                    legacySelectedVariant = anoesScifiSelecionado,
                    availableOptions = ancestryOptions
                )
            ).normalizedSelection
        } else {
            null
        }

        if (isSciFiActive) {
            if (ancKey == "ANOES") {
                // Anões "Ciber" é Variante de verdade (o mestre reconfigura a
                // raça pro cenário) com Seleção aninhada (até 2 pontos de
                // traços negativos, catálogo em AnaoCiberTraitCatalog).
                // Resolvido via AncestryVariantRegistry em vez do "when" fixo
                // que existia aqui antes.
                return if (effectiveVariant == "Ciber") {
                    val pontosUsados = AnaoCiberTraitCatalog.pontosUsados(anaoCiberTracosSelecionados)
                    val tracosValidos = if (pontosUsados <= AnaoCiberTraitCatalog.MAX_PONTOS) {
                        anaoCiberTracosSelecionados
                    } else {
                        // Segurança: nunca aplicar uma seleção que estoure o orçamento de
                        // pontos, mesmo que algo upstream falhe em validar antes de chegar aqui.
                        emptyList()
                    }
                    val racialDisadvantages = AnaoCiberTraitCatalog.buildDesvantagens(tracosValidos).ifEmpty {
                        listOf(
                            TraitAddition(
                                "Anões Ciber: escolha até 2 pontos de traços raciais negativos (nenhum maior que -2) na ficha.",
                                "ANAO_CIBER_TRACOS_PENDENTES"
                            )
                        )
                    }
                    val catalogSelection = AncestryVariantRegistry.get("ANOES", "SCI_FI")
                        ?.grupoVariante?.opcoes?.firstOrNull { it.id == "ciber" }
                        ?.selecoes?.firstOrNull { it.id == "anao_ciber_tracos_negativos" }
                    val resolved = if (catalogSelection != null) {
                        resolveAncestryVariantPackageUseCase.resolve(
                            ancestralidadeId = "ANOES",
                            livro = "SCI_FI",
                            variantOptionId = "ciber",
                            selectionAnswers = emptyList(),
                            catalogPackages = mapOf(
                                catalogSelection.id to ResolvedTraitPackage(desvantagensParaAdicionar = racialDisadvantages)
                            )
                        )
                    } else {
                        ResolvedTraitPackage()
                    }
                    Result(
                        naturalArmorFromRace = 0,
                        forceArmorZero = true,
                        ensureAdvantageNames = resolved.vantagensGratisParaAdicionar.map { it.nome },
                        ensureAdvantageIds = emptyList(),
                        ensureAutomaticAdvantages = resolved.vantagensGratisParaAdicionar,
                        ensureRacialDisadvantages = resolved.desvantagensParaAdicionar,
                        elementalAction = ElementalAction.NONE,
                        anotacoesToAdd = emptyList()
                    )
                } else {
                    // Default / Básico
                    Result(
                        naturalArmorFromRace = 0,
                        forceArmorZero = true,
                        ensureAdvantageNames = emptyList(),
                        ensureAdvantageIds = emptyList(),
                        ensureAutomaticAdvantages = emptyList(),
                        automaticAdvantagesToRemove = listOf("CIBERTOLERÂNCIA", "CIBERTOLERANCIA"),
                        ensureRacialDisadvantages = emptyList(),
                        elementalAction = ElementalAction.NONE
                    )
                }
            }

            if (ancKey == "QUADROIDES" && effectiveVariant == "Habilidoso") {
                // Ação Adicional (Ignora Penalidade) é 1 ponto mais forte que
                // a Física que ela substitui — o livro pede pro mestre
                // equilibrar com 1 ponto de traço negativo. Isso é resolvido
                // como escolha de verdade do jogador (catálogo reaproveitado
                // de AnaoCiberTraitCatalog, mesmo padrão do Anão Ciber acima)
                // em vez de um lembrete solto: sem escolha ainda, usa o
                // primeiro traço da lista.
                val trait = AnaoCiberTraitCatalog.TRACOS_MENOS_UM_QUADROIDES.firstOrNull { it.id == quadroidesTracoNegativoSelecionado }
                    ?: AnaoCiberTraitCatalog.TRACOS_MENOS_UM_QUADROIDES.first()
                val racialDisadvantages = AnaoCiberTraitCatalog.buildDesvantagens(
                    listOf(AnaoCiberTraitSelection(traitId = trait.id))
                )
                val resolved = resolveAncestryVariantPackageUseCase.resolve(
                    ancestralidadeId = "QUADROIDES",
                    livro = "SCI_FI",
                    variantOptionId = "habilidoso",
                    selectionAnswers = emptyList(),
                    catalogPackages = mapOf(
                        "quadroides_traco_negativo" to ResolvedTraitPackage(desvantagensParaAdicionar = racialDisadvantages)
                    )
                )
                return Result(
                    naturalArmorFromRace = 0,
                    forceArmorZero = true,
                    ensureAdvantageNames = emptyList(),
                    ensureAdvantageIds = emptyList(),
                    ensureAutomaticAdvantages = resolved.tracosParaAdicionar,
                    automaticAdvantagesToRemove = resolved.tracosParaRemoverPorNome,
                    ensureRacialDisadvantages = resolved.desvantagensParaAdicionar,
                    elementalAction = ElementalAction.NONE
                )
            }

            // Guarda extra só pra "HUMANOS": é o único id de
            // scifiVariantDrivenKeys que é um nome de exibição compartilhado
            // por raças DIFERENTES em livros diferentes (Sci-Fi "Humanos" —
            // Baixa Gravidade/Minerador — vs. Fantasia "Humanos" — Pacotes
            // Culturais). As outras 19 raças do set não têm esse tipo de
            // colisão, então não precisam checar o livro (e os testes
            // existentes desse use case não passam `ancestryOrigin`, só
            // `isSciFiActive` — exigir o livro pra todas quebraria esses
            // testes sem ganho real). Sem essa checagem pra HUMANOS
            // especificamente, um jogador com Sci-Fi E Fantasia ativos ao
            // mesmo tempo, jogando um Humano de Fantasia, caía neste bloco
            // genérico (que só conhece a config Sci-Fi), sempre resolvendo
            // pra "Padrão" — Pacotes Culturais nunca era aplicado (bug
            // relatado pelo usuário: Senhores dos Cavalos não concedia nada
            // e Adaptável continuava presente).
            val isHumanosDeOutroLivro = ancKey == "HUMANOS" && canonicalOriginKey(ancestryOrigin) != "SCI_FI"
            if (ancKey in AncestryVariantRegistry.scifiVariantDrivenKeys && !isHumanosDeOutroLivro) {
                buildResultFromVariantRegistry(ancKey, effectiveVariant)?.let { return it }
            }

            if (ancKey == "ELEMENTAIS") {
                // Elementais não tem Variante — é Seleção de elemento (todo
                // elemental é de algum elemento). Resolvido via
                // AncestryVariantRegistry em vez do "when" fixo que existia
                // aqui antes.
                val def = AncestryVariantRegistry.get("ELEMENTAIS", "SCI_FI")
                    ?.selecoes?.firstOrNull { it.id == "elementais_scifi_elemento" }
                val resolved = if (def != null) {
                    resolveAncestryVariantPackageUseCase.resolve(
                        ancestralidadeId = "ELEMENTAIS",
                        livro = "SCI_FI",
                        variantOptionId = null,
                        selectionAnswers = listOf(fixedPackageAnswerFrom(def, effectiveVariant))
                    )
                } else {
                    ResolvedTraitPackage()
                }
                return Result(
                    naturalArmorFromRace = 0,
                    forceArmorZero = true,
                    ensureAdvantageNames = emptyList(),
                    ensureAdvantageIds = emptyList(),
                    ensureAutomaticAdvantages = resolved.tracosParaAdicionar,
                    ensureRacialDisadvantages = emptyList(),
                    elementalAction = ElementalAction.NONE
                )
            }

        }

        if (canonicalOriginKey(ancestryOrigin) == "ARTE_DA_GUERRA" && ancKey.contains("UMVEE")) {
            // Umvee não tem Variante nenhuma — "Dom da Natureza" é Seleção de
            // pacote fixo (o jogador escolhe 1 de 6, não o mestre reconfigura
            // a raça). Resolvido via AncestryVariantRegistry em vez do "when"
            // fixo que existia aqui antes.
            val def = AncestryVariantRegistry.get("UMVEE (FILHOS DA LUA)", "ARTE_DA_GUERRA")
                ?.selecoes?.firstOrNull { it.id == "umvee_dom_da_natureza" }
            val resolved = if (def != null) {
                resolveAncestryVariantPackageUseCase.resolve(
                    ancestralidadeId = "UMVEE (FILHOS DA LUA)",
                    livro = "ARTE_DA_GUERRA",
                    variantOptionId = null,
                    selectionAnswers = listOf(fixedPackageAnswerFrom(def, effectiveVariant))
                )
            } else {
                ResolvedTraitPackage()
            }
            return Result(
                naturalArmorFromRace = if (effectiveVariant == "Pedregoso") 2 else 0,
                forceArmorZero = true,
                ensureAdvantageNames = resolved.vantagensGratisParaAdicionar.map { it.nome },
                ensureAdvantageIds = emptyList(),
                ensureAutomaticAdvantages = resolved.vantagensGratisParaAdicionar + resolved.tracosParaAdicionar,
                ensureRacialDisadvantages = resolved.desvantagensParaAdicionar,
                elementalAction = ElementalAction.NONE
            )
        }

        if (canonicalOriginKey(ancestryOrigin) == "FANTASIA" && ancKey.contains("HUMANO")) {
            // Pacotes Culturais: Variante de verdade (mestre/grupo escolhe pra
            // mesa), resolvida via AncestryVariantRegistry.humanoFantasia() em
            // vez do antigo sistema dedicado de Pacote Cultural. As perícias/
            // atributo iniciais (Sobrevivência d6 etc.) e os traços narrativos
            // negativos (Fraqueza Ambiental, Penalidade em Cavalgar) já entram
            // em habilidades[] direto por CriadorState.applyAncestryVariantAdjustments
            // (efeito mecânico via RacialTraitEffect + texto informativo na
            // ficha); aqui só cuida do canal de bookkeeping (vantagensRaciais/
            // desvantagensRaciais) e das Vantagens/Complicações reais
            // (Resistência Ambiental é só texto, Procurado/Código de Honra/Sem
            // Escrúpulos/Analfabeto são Complicações do catálogo, Nascido na
            // Sela é Vantagem real via vantagensGratisIds).
            val config = AncestryVariantRegistry.get("HUMANOS", "FANTASIA")
            val variantOptionId = config?.let { variantOptionIdFrom(it, effectiveVariant) }
            val nestedDef = config?.grupoVariante?.opcoes
                ?.firstOrNull { it.id == variantOptionId }
                ?.selecoes?.firstOrNull()
            val resolved = if (variantOptionId != null) {
                resolveAncestryVariantPackageUseCase.resolve(
                    ancestralidadeId = "HUMANOS",
                    livro = "FANTASIA",
                    variantOptionId = variantOptionId,
                    selectionAnswers = listOfNotNull(
                        nestedDef?.let { fixedPackageAnswerFrom(it, humanoFantasiaSelecaoAninhada) }
                    )
                )
            } else {
                ResolvedTraitPackage()
            }
            return Result(
                naturalArmorFromRace = 0,
                forceArmorZero = true,
                ensureAdvantageNames = emptyList(),
                ensureAdvantageIds = resolved.vantagensGratisIds,
                ensureAutomaticAdvantages = resolved.vantagensGratisParaAdicionar + resolved.tracosParaAdicionar,
                ensureRacialDisadvantages = resolved.desvantagensParaAdicionar + resolved.tracosNegativosParaAdicionar,
                elementalAction = ElementalAction.NONE
            )
        }

        // Bloco hardcoded de FERAL removido: Furioso e Sanguinário (traços
        // FURIOSO/SANGUINARIO em habilidades[], com traitId=GRANTED_EDGE pro
        // primeiro) são resolvidos genericamente pelo mesmo caminho de
        // qualquer outra raça, igual à Mente de Colmeia dos Insetoides
        // (Fantasia) — não precisam mais de "if (ancKey == 'FERAL')" aqui.
        // As Garras (For+d4 sem PA) já vêm de
        // GARRAS_SEM_PA em habilidades[]; o grant duplicado "GARRAS" (com
        // PA, custo errado — 3 em vez de 2) que existia aqui foi removido
        // junto. A nota "não pode canalizar Técnicas de Chi" já está na
        // descrição do próprio traço Limitações Técnicas.

        if (ancKey.contains("TERRACOTA")) {
            // Terracota não tem Variante — é Seleção de pacote fixo: todo
            // Terracota nasce com Voto OU Obrigação (Maior), o jogador só
            // escolhe qual das duas. Resolvido via AncestryVariantRegistry.
            val def = AncestryVariantRegistry.get("TERRACOTA", "ARTE_DA_GUERRA")
                ?.selecoes?.firstOrNull { it.id == "terracota_complicacao" }
            val resolved = if (def != null) {
                resolveAncestryVariantPackageUseCase.resolve(
                    ancestralidadeId = "TERRACOTA",
                    livro = "ARTE_DA_GUERRA",
                    variantOptionId = null,
                    selectionAnswers = listOf(fixedPackageAnswerFrom(def, effectiveVariant))
                )
            } else {
                ResolvedTraitPackage()
            }

            return Result(
                naturalArmorFromRace = 0,
                forceArmorZero = false,
                ensureAdvantageNames = emptyList(),
                ensureAdvantageIds = emptyList(),
                ensureAutomaticAdvantages = emptyList(),
                ensureRacialDisadvantages = resolved.desvantagensParaAdicionar,
                racialDisadvantagesToRemove = listOf("Voto ou Obrigação", "VOTO_OU_OBRIGACAO", "VOTO OU OBRIGACAO"),
                elementalAction = ElementalAction.NONE
            )
        }

        if (ancKey.contains("AKAIMIMI")) {
            return Result(
                naturalArmorFromRace = 0,
                forceArmorZero = true,
                ensureAdvantageNames = emptyList(),
                ensureAdvantageIds = emptyList(),
                ensureAutomaticAdvantages = emptyList(),
                ensureRacialDisadvantages = listOf(TraitAddition("PECULIARIDADE", "PECULIARIDADE")),
                elementalAction = ElementalAction.NONE
            )
        }

        return when (ancKey) {
            // Armadura Natural lida pelo id do traço (ARMADURA, ver
            // ancestralidades.json), não mais fixa por nome de raça — o
            // valor só é diferente de 0 quando a raça resolvida realmente
            // carrega esse traço em habilidades[].
            "SAURIOS" -> Result(
                naturalArmorFromRace = naturalArmorFromAbilityId,
                forceArmorZero = true,
                ensureAdvantageNames = emptyList(),
                ensureAdvantageIds = emptyList(),
                ensureAutomaticAdvantages = listOf(TraitAddition("PRONTIDÃO", "PRONTIDAO")),
                ensureRacialDisadvantages = emptyList(),
                elementalAction = ElementalAction.NONE
            )

            "GOLENS" -> Result(
                naturalArmorFromRace = naturalArmorFromAbilityId,
                forceArmorZero = true,
                ensureAdvantageNames = emptyList(),
                ensureAdvantageIds = emptyList(),
                ensureAutomaticAdvantages = emptyList(),
                ensureRacialDisadvantages = emptyList(),
                elementalAction = ElementalAction.NONE
            )

            "DRACONIANOS" -> Result(
                naturalArmorFromRace = naturalArmorFromAbilityId,
                forceArmorZero = true,
                ensureAdvantageNames = emptyList(),
                ensureAdvantageIds = emptyList(),
                ensureAutomaticAdvantages = emptyList(),
                ensureRacialDisadvantages = emptyList(),
                elementalAction = ElementalAction.NONE
            )

            "INSETOIDES" -> Result(
                naturalArmorFromRace = naturalArmorFromAbilityId,
                forceArmorZero = true,
                ensureAdvantageNames = emptyList(),
                ensureAdvantageIds = emptyList(),
                ensureAutomaticAdvantages = listOf(TraitAddition("GARRAS", "GARRAS")),
                ensureRacialDisadvantages = emptyList(),
                elementalAction = ElementalAction.NONE
            )

            "PEQUENINOS" -> Result(
                naturalArmorFromRace = 0,
                forceArmorZero = true,
                ensureAdvantageNames = listOf("Sorte", "Espirituoso"),
                ensureAdvantageIds = emptyList(),
                ensureAutomaticAdvantages = emptyList(),
                ensureRacialDisadvantages = listOf(
                    TraitAddition("Tamanho -1", "TAMANHO_MENOS_1"),
                    TraitAddition("Movimentação Reduzida", "MOVIMENTACAO_REDUZIDA")
                ),
                elementalAction = ElementalAction.NONE
            )

            "CELESTIAIS" -> {
                if (canonicalOriginKey(ancestryOrigin) == "BASICO") {
                    Result(
                        naturalArmorFromRace = 0,
                        forceArmorZero = true,
                        ensureAdvantageNames = emptyList(),
                        ensureAdvantageIds = listOf("antecedente_arcano_milagres"),
                        ensureAutomaticAdvantages = listOf(TraitAddition("ANTECEDENTE ARCANO (MILAGRES)", "ANTECEDENTE_ARCANO_MILAGRES")),
                        ensureRacialDisadvantages = emptyList(),
                        elementalAction = ElementalAction.NONE
                    )
                } else {
                    Result(
                        naturalArmorFromRace = 0,
                        forceArmorZero = true,
                        ensureAdvantageNames = emptyList(),
                        ensureAdvantageIds = emptyList(),
                        ensureAutomaticAdvantages = emptyList(),
                        ensureRacialDisadvantages = emptyList(),
                        elementalAction = ElementalAction.NONE
                    )
                }
            }

            "HUMANO (WISEGUYS)".keyify() -> Result(
                naturalArmorFromRace = 0,
                forceArmorZero = false,
                ensureAdvantageNames = listOf("Conexões (Máfia)"),
                ensureAdvantageIds = emptyList(),
                ensureAutomaticAdvantages = emptyList(),
                ensureRacialDisadvantages = emptyList(),
                elementalAction = ElementalAction.NONE
            )

            "DESCENDENTE ELEMENTAL".keyify() -> Result(
                naturalArmorFromRace = 0,
                forceArmorZero = true,
                ensureAdvantageNames = emptyList(),
                ensureAdvantageIds = emptyList(),
                ensureAutomaticAdvantages = emptyList(),
                automaticAdvantagesToRemove = if (descendenteElementalSelecionado?.equals("Fogo", ignoreCase = true) == true) {
                    emptyList()
                } else {
                    listOf("RAPIDO", "RÁPIDO", "rapido")
                },
                ensureRacialDisadvantages = emptyList(),
                elementalAction = if (descendenteElementalSelecionado == null) {
                    ElementalAction.SELECT_DEFAULT
                } else {
                    ElementalAction.REAPPLY_CURRENT
                }
            )

            "TRANSMORFOS".keyify() -> Result(
                naturalArmorFromRace = 0,
                forceArmorZero = true,
                ensureAdvantageNames = emptyList(),
                ensureAdvantageIds = listOf("antecedente_arcano_dom"),
                ensureAutomaticAdvantages = listOf(TraitAddition("ANTECEDENTE ARCANO (DOM)", "ANTECEDENTE_ARCANO_DOM")),
                ensureRacialDisadvantages = emptyList(),
                elementalAction = ElementalAction.NONE
            )

            "DEMÔNIO (ABISMO)".keyify() -> Result(
                naturalArmorFromRace = 0,
                forceArmorZero = true,
                ensureAdvantageNames = emptyList(),
                ensureAdvantageIds = listOf("aa_demonio"),
                ensureAutomaticAdvantages = listOf(TraitAddition("ANTECEDENTE ARCANO (DEMÔNIO)", "ANTECEDENTE_ARCANO_DEMONIO")),
                ensureRacialDisadvantages = emptyList(),
                elementalAction = ElementalAction.NONE
            )

            else -> Result(
                naturalArmorFromRace = 0,
                forceArmorZero = true,
                ensureAdvantageNames = emptyList(),
                ensureAdvantageIds = emptyList(),
                ensureAutomaticAdvantages = emptyList(),
                ensureRacialDisadvantages = emptyList(),
                elementalAction = ElementalAction.NONE
            )
        }
    }
}
