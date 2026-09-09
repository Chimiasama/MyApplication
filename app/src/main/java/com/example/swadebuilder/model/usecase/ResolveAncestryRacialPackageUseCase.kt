package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.AnaoCiberTraitSelection
import com.example.swadebuilder.model.RacialTraitStack
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.model.racialGrantDedupeKey
import com.example.swadebuilder.util.keyify

class ResolveAncestryRacialPackageUseCase(
    private val resolveGrantedAncestryAdvantagesUseCase: ResolveGrantedAncestryAdvantagesUseCase = ResolveGrantedAncestryAdvantagesUseCase(),
    private val resolveAncestrySpecificAdjustmentsUseCase: ResolveAncestrySpecificAdjustmentsUseCase = ResolveAncestrySpecificAdjustmentsUseCase()
) {

    data class Params(
        val anc: String,
        val descendenteElementalSelecionado: String?,
        val anoesScifiSelecionado: String? = null,
        val scifiVariant: String? = null,
        val humanoMineradorAtributo: String? = null,
        val humanoFantasiaSelecaoAninhada: String? = null,
        val anaoCiberTracosSelecionados: List<AnaoCiberTraitSelection> = emptyList(),
        val quadroidesTracoNegativoSelecionado: String? = null,
        val ancestryOptions: List<String> = emptyList(),
        val isSciFiActive: Boolean = false,
        val isSciFiMechasActive: Boolean = false,
        val allAdvantages: List<Vantagem>,
        val selectedAdvantages: List<Vantagem>,
        val previousFreeAdvantageKeys: Set<String>,
        val ancestryGrantedAdvantages: List<String>,
        val ancestryAutomaticDisadvantages: List<String>,
        val ancestryOrigin: String = "BASICO",
        // Ids de habilidade[] da raça já resolvida — ver
        // ResolveAncestrySpecificAdjustmentsUseCase.execute(racialAbilityIds).
        val racialAbilityIds: Set<String> = emptySet()
    )

    data class Result(
        val selectedAdvantages: List<Vantagem>,
        val vantagensAutomaticas: List<String>,
        val vantagensRaciais: List<String>,
        val desvantagensRaciais: List<String>,
        val naturalArmorFromRace: Int,
        val forceArmorZero: Boolean,
        val elementalAction: ResolveAncestrySpecificAdjustmentsUseCase.ElementalAction,
        val anotacoesToAdd: List<String> = emptyList(),
        // Ids mecânicos reais (+ contagem de compras, ver
        // RacialTraitPointCatalog.VEZES_MAX) dos traços injetados por
        // Variante/Seleção (ensureAutomaticAdvantages/ensureRacialDisadvantages)
        // — ver TraitAddition. ModifierEngine lê esta lista direto, sem
        // precisar derivar id nenhum do texto de vantagensRaciais/desvantagensRaciais.
        val racialTraitIds: List<RacialTraitStack> = emptyList()
    )

    fun execute(params: Params): Result {
        val selected = params.selectedAdvantages
            .filterNot {
                val isLeakedDom = (it.id == "antecedente_arcano" && it.choice?.keyify() == "DOM" && "ANTECEDENTE ARCANO (DOM)" in params.previousFreeAdvantageKeys) ||
                                  (it.id == "antecedente_arcano_dom" && "ANTECEDENTE ARCANO (DOM)" in params.previousFreeAdvantageKeys)
                val isLeakedTelepata = (it.id == "poderes_misticos" && it.choice?.keyify() == "TELEPATA" && "PODERES MISTICOS (TELEPATA)" in params.previousFreeAdvantageKeys)
                it.nome.keyify() in params.previousFreeAdvantageKeys || it.id.keyify() in params.previousFreeAdvantageKeys || isLeakedDom || isLeakedTelepata
            }
            .toMutableList()

        // distinctBy(racialGrantDedupeKey): defesa extra pro caso de algum chamador futuro
        // passar uma lista não deduplicada aqui — ver RacialModifier.kt
        // vantagensGratisEfetivas/desvantagensEfetivas pro motivo (raças do catálogo que
        // registram a mesma vantagem/desvantagem solta e embutida numa habilidade ao mesmo
        // tempo). O chamador atual (ApplyAncestryChangeCoordinatorUseCase) já dedupliaca
        // antes de passar pra cá, então isso é redundante hoje — de propósito.
        val vantagensAutomaticas = params.ancestryGrantedAdvantages.distinctBy { it.racialGrantDedupeKey() }.toMutableList()
        val vantagensRaciais = params.ancestryGrantedAdvantages.distinctBy { it.racialGrantDedupeKey() }.toMutableList()
        val desvantagensRaciais = params.ancestryAutomaticDisadvantages.distinctBy { it.racialGrantDedupeKey() }.toMutableList()

        val grantedAdvantagesResult = resolveGrantedAncestryAdvantagesUseCase.execute(
            ResolveGrantedAncestryAdvantagesUseCase.Params(
                grantedAdvantageNamesOrIds = params.ancestryGrantedAdvantages,
                allAdvantages = params.allAdvantages,
                selectedAdvantages = selected
            )
        )
        selected.addAll(grantedAdvantagesResult.advantagesToAdd)

        // Ensure granted racial advantages are marked as automatic/racial to prevent removal during validation
        grantedAdvantagesResult.advantagesToAdd.forEach { adv ->
            val key = adv.nome.keyify()
            if (vantagensRaciais.none { it.keyify() == key }) {
                vantagensRaciais.add(adv.nome) // Add name or ID to list for validation exclusion
            }
        }

        val ancestrySpecificAdjustments = resolveAncestrySpecificAdjustmentsUseCase.execute(
            anc = params.anc,
            descendenteElementalSelecionado = params.descendenteElementalSelecionado,
            anoesScifiSelecionado = params.anoesScifiSelecionado,
            scifiVariant = params.scifiVariant,
            humanoMineradorAtributo = params.humanoMineradorAtributo,
            humanoFantasiaSelecaoAninhada = params.humanoFantasiaSelecaoAninhada,
            anaoCiberTracosSelecionados = params.anaoCiberTracosSelecionados,
            quadroidesTracoNegativoSelecionado = params.quadroidesTracoNegativoSelecionado,
            ancestryOptions = params.ancestryOptions,
            isSciFiActive = params.isSciFiActive,
            isSciFiMechasActive = params.isSciFiMechasActive,
            ancestryOrigin = params.ancestryOrigin,
            racialAbilityIds = params.racialAbilityIds
        )

        // Ambos os blocos abaixo (ensureAdvantageNames/ensureAdvantageIds) concedem a
        // Vantagem de verdade — igual a uma comprada manualmente, pra herdar toda a
        // mecânica dela (rerrolagem, bônus, etc.) — mas são grátis (parte do pacote
        // racial, sem gastar PV). Sem marcar em `vantagensRaciais`, essa Vantagem cai
        // sem proteção em RemoveInvalidAdvantagesAfterAncestryChangeUseCase (que só
        // pula a checagem de requisitos pra quem está em `automaticAdvantages`/
        // `automaticRacialAdvantages`) — se ela tiver um requisito de atributo/perícia
        // que o personagem não atenda ainda (ex.: Nascido na Sela exige Agilidade d8,
        // que um Humano recém-criado não tem), a validação a remove na mesma hora que
        // a concede, e como não está em `previousFreeAdvantageKeys` (que só lê
        // vantagensAutomaticas/vantagensRaciais), CriadorState.aplicarAncestralidade
        // trata a remoção como se fosse uma Vantagem comprada perdendo requisito e
        // devolve 1 PV — um PV fantasma, já que o jogador nunca gastou nada nela (bug
        // relatado pelo usuário com Senhores dos Cavalos/Nascido na Sela).
        ancestrySpecificAdjustments.ensureAdvantageNames.forEach { advantageName ->
            params.allAdvantages.firstOrNull { it.nome.equals(advantageName, ignoreCase = true) }
                ?.let { edge ->
                    if (selected.none { it.id == edge.id }) {
                        selected.add(edge)
                    }
                    if (vantagensRaciais.none { it.equals(edge.nome, ignoreCase = true) }) {
                        vantagensRaciais.add(edge.nome)
                    }
                }
        }

        ancestrySpecificAdjustments.ensureAdvantageIds.forEach { advantageId ->
            // "conexoes_mafia" é pseudo-id (não existe no catálogo): Humano
            // (Wiseguys) concede a Vantagem real "Conexões" já com a escolha
            // "Máfia" pré-marcada, igual ao fallback de Antecedente Arcano (Dom)
            // logo abaixo — antes disso tentava casar por NOME exato contra
            // "Conexões (Máfia)", que não existe no catálogo (lá é só
            // "Conexões", com a escolha num campo separado), então nunca batia
            // e o Humano (Wiseguys) nunca recebia a Vantagem de raça (bug real,
            // silencioso — só o toggle separado "Cosa Nostra" concedia).
            if (advantageId == "conexoes_mafia") {
                val conexoes = params.allAdvantages.firstOrNull { it.id.keyify() == "CONEXOES" }
                if (conexoes != null) {
                    if (selected.none { it.id == conexoes.id && (it.choice ?: "").keyify() == "MAFIA" }) {
                        selected.add(conexoes.copy(choice = "Máfia"))
                    }
                    if (vantagensRaciais.none { it.equals(conexoes.nome, ignoreCase = true) }) {
                        vantagensRaciais.add(conexoes.nome)
                    }
                }
                return@forEach
            }

            // Comparação por id normalizada (keyify): os ids sintéticos usados
            // pelos pacotes raciais (TraitAddition.id, ex.: "SENHOR_DAS_FERAS")
            // seguem a convenção interna (maiúsculo com underscore), enquanto o
            // catálogo real (vantagens.json) usa minúsculo (ex.:
            // "senhor_das_feras") — == exato nunca batia por causa disso, e
            // silenciosamente caía pro fallback (raça sem a Vantagem).
            val edge = params.allAdvantages.firstOrNull { it.id.keyify() == advantageId.keyify() }
            if (edge != null) {
                if (selected.none { it.id == edge.id }) {
                    selected.add(edge)
                }
                if (vantagensRaciais.none { it.equals(edge.nome, ignoreCase = true) }) {
                    vantagensRaciais.add(edge.nome)
                }
                return@forEach
            }

            // Fallback for scenarios that hide/replace specific Arcane Background entries.
            // Example: Transmorfos need AA (Dom) even when "antecedente_arcano_dom" is not present in loaded advantages.
            if (advantageId == "antecedente_arcano_dom") {
                val genericArcane = params.allAdvantages.firstOrNull { it.id == "antecedente_arcano" }
                if (genericArcane != null) {
                    if (selected.none { it.id == genericArcane.id && (it.choice ?: "").keyify() == "DOM" }) {
                        selected.add(genericArcane.copy(choice = "DOM"))
                    }
                    if (vantagensRaciais.none { it.equals(genericArcane.nome, ignoreCase = true) }) {
                        vantagensRaciais.add(genericArcane.nome)
                    }
                }
            }
        }

        val racialTraitIds = mutableListOf<RacialTraitStack>()

        ancestrySpecificAdjustments.ensureAutomaticAdvantages.forEach { automaticAdvantage ->
            if (vantagensAutomaticas.none { it.equals(automaticAdvantage.nome, ignoreCase = true) }) {
                vantagensAutomaticas.add(automaticAdvantage.nome)
            }
            if (vantagensRaciais.none { it.equals(automaticAdvantage.nome, ignoreCase = true) }) {
                vantagensRaciais.add(automaticAdvantage.nome)
            }
            racialTraitIds.add(RacialTraitStack(automaticAdvantage.id, automaticAdvantage.vezes))
        }

        ancestrySpecificAdjustments.automaticAdvantagesToRemove.forEach { toRemove ->
            val key = toRemove.keyify()
            vantagensAutomaticas.removeAll { it.keyify() == key }
            vantagensRaciais.removeAll { it.keyify() == key }
            selected.removeAll { it.id.keyify() == key || it.nome.keyify() == key }
        }

        ancestrySpecificAdjustments.ensureRacialDisadvantages.forEach { racialDisadvantage ->
            if (desvantagensRaciais.none { it.equals(racialDisadvantage.nome, ignoreCase = true) }) {
                desvantagensRaciais.add(racialDisadvantage.nome)
            }
            racialTraitIds.add(RacialTraitStack(racialDisadvantage.id, racialDisadvantage.vezes))
        }

        ancestrySpecificAdjustments.racialDisadvantagesToRemove.forEach { toRemove ->
            desvantagensRaciais.removeAll { it.keyify() == toRemove.keyify() }
        }

        return Result(
            selectedAdvantages = selected,
            vantagensAutomaticas = vantagensAutomaticas,
            vantagensRaciais = vantagensRaciais,
            desvantagensRaciais = desvantagensRaciais,
            naturalArmorFromRace = ancestrySpecificAdjustments.naturalArmorFromRace,
            forceArmorZero = ancestrySpecificAdjustments.forceArmorZero,
            elementalAction = ancestrySpecificAdjustments.elementalAction,
            anotacoesToAdd = ancestrySpecificAdjustments.anotacoesToAdd,
            racialTraitIds = racialTraitIds
        )
    }
}
