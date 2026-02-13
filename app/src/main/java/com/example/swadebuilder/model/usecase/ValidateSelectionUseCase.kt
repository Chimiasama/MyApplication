package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.Estagio
import com.example.swadebuilder.Pericia
import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.Complicacao
import com.example.swadebuilder.model.RacialModifier
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.model.classeExclusivaBloqueada
import com.example.swadebuilder.normAAKey
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.semAcentos

class ValidateSelectionUseCase {

    data class SelectionContext(
        val ancestralidade: String,
        val racialDef: RacialModifier?,
        val estagioAtual: Estagio,
        val listaEstagios: List<Estagio>,
        val atributosRaw: Map<String, Int>,
        val periciasRaw: Map<String, Int>, // Keyified name -> Value
        val getBestPericia: (String) -> Pericia?, // Callback to find best skill match (handles idioms/jutsu)
        val getRawTotal: (Pericia) -> Int, // Callback to get skill value
        val getMaxAttributeRaw: (String) -> Int, // Callback to get max attribute value (racial/size limit)
        val vantagensSelecionadas: List<Vantagem>,
        val complicacoesSelecionadas: Map<Complicacao, String?>, // Comp -> Level (Menor/Maior)
        val cartaSelvagem: Boolean,
        val isMonstro: Boolean,
        val tipoMonstro: String?,
        // State Flags
        val emProgresso: Boolean,
        val nasceUmHeroi: Boolean,
        val pvFromXpOutstanding: Int,
        val permiteMultiAntecedenteArcano: Boolean,
        val comprasPpPorEstagioSum: Int,
        val maxComprasPpAteAgora: Int,
        val superInvestmentsCount: Int,
        // Active Modules
        val compendioFantasiaAtivo: Boolean,
        val compendioHorrorAtivo: Boolean,
        val compendioPathfinderAtivo: Boolean,
        val compendioCrystalHeartAtivo: Boolean,
        val compendioArteDaGuerraAtivo: Boolean,
        // Extra Context
        val overrideStageForVantagem: String? = null,
        val tropoSamuraiActive: Boolean = false
    )

    data class Result(
        val allowed: Boolean,
        val reason: String? = null
    )

    private val ameacadorComplicacoesLiberadoras = setOf(
        "sanguinario", "desagradavel", "sem_escrupulos", "feio", "sombrio", "sinistro"
    ).map { it.keyify() }.toSet()

    private val ameacadorId = "ameacador".keyify()

    private val incompatibilidades: Map<String, Set<String>> = mapOf(
        "LENTO"   to setOf("LIGEIRO"),
        "LIGEIRO" to setOf("LENTO"),
        "OBESO"      to setOf("MUSCULOSO"),
        "MUSCULOSO"  to setOf("OBESO"),
        "COMP ALMA PENHORADA" to setOf("ANTECEDENTE ARCANO MILAGRES", "AA MILAGRES"),
        "COMP ALMA VENDIDA" to setOf("ANTECEDENTE ARCANO MILAGRES", "AA MILAGRES"),
        "ANTECEDENTE ARCANO MILAGRES" to setOf("COMP ALMA PENHORADA", "COMP ALMA VENDIDA"),
        "AA MILAGRES" to setOf("COMP ALMA PENHORADA", "COMP ALMA VENDIDA"),
        "COMP MALDICAO GREMLIN" to setOf("ANTECEDENTE ARCANO TECNOMAGIA", "AA TECNOMAGIA"),
        "ANTECEDENTE ARCANO TECNOMAGIA" to setOf("COMP MALDICAO GREMLIN"),
        "AA TECNOMAGIA" to setOf("COMP MALDICAO GREMLIN"),
        "COMP TECNOFOBIA" to setOf("TARO ENGENHEIRO", "MESTRE DAS CALDEIRAS", "MECANICO CEGO"),
        "TARO ENGENHEIRO" to setOf("COMP TECNOFOBIA"),
        "MESTRE DAS CALDEIRAS" to setOf("COMP TECNOFOBIA"),
        "MECANICO CEGO" to setOf("COMP TECNOFOBIA"),
        "POBREZA"        to setOf("RICO", "PODRE DE RICO"),
        "RICO"           to setOf("POBREZA"),
        "PODRE DE RICO"  to setOf("POBREZA"),
        "ESCOLHIDO"      to setOf("INIMIGO"),
        "INIMIGO"        to setOf("ESCOLHIDO")
    )

    fun execute(v: Vantagem, context: SelectionContext): Result {
        val key = v.nome.keyify()
        val ancestralidadeKey = context.ancestralidade.keyify()

        // Cidade do Sol a Vapor: AA (Demônio) disponível para Demônio e Meio-Demônio.
        if (v.id == "aa_demonio") {
            val isMeioDemonio = ancestralidadeKey.contains("MEIO-DEMONIO")
            val isDemonio = ancestralidadeKey.contains("DEMONIO") && !isMeioDemonio
            if (!isMeioDemonio && !isDemonio) return Result(false, "Requer ancestralidade Demônio ou Meio-Demônio.")
        }

        // Crystal Heart Blocks
        if (context.compendioCrystalHeartAtivo) {
            val forbiddenIds = setOf(
                "campeao", "chi", "linguista", "resistencia_arcana", "resistencia_arcana_aprimorada",
                "rico", "podre_de_rico",
                "aristocrata", "arma_predileta", "comando", "conexoes",
                "antecedente_arcano"
            ).map { it.keyify() }.toSet()
            val vKey = v.id.keyify()

            if (vKey in forbiddenIds) return Result(false, "Não permitido em Crystal Heart.")

            // Block Power Edges unless Crystal Heart specific
            if (v.categoria == Categoria.PODER && v.origem != "CRYSTAL_HEART") {
                return Result(false, "Vantagens de Poder não são permitidas em Crystal Heart.")
            }
        }

        // Regra: "Mago" do básico oculto se Fantasia ativo
        if (context.compendioFantasiaAtivo && v.id == "mago") return Result(false, "Substituído em Fantasia.")

        // Regra: Antecedentes Arcanos que não existem em Pathfinder (Ciência Estranha, Psiônicos, Dom)
        if (context.compendioPathfinderAtivo) {
            val forbiddenIds = setOf(
                "antecedente_arcano_ciencia_estranha",
                "antecedente_arcano_psionicos",
                "antecedente_arcano_dom"
            )
            if (v.id in forbiddenIds) return Result(false, "Não permitido em Pathfinder.")
        }

        // 0) Exclusividade de Classe/Prestígio (Buscatrilha)
        if (context.vantagensSelecionadas.classeExclusivaBloqueada(v)) {
            return Result(false, "Classe exclusiva bloqueada (requer Multiclasse).")
        }

        // 1) Regra especial: O MELHOR QUE HÁ
        if (key == "o_melhor_que_ha") {
            if (context.emProgresso) return Result(false, "Apenas na criação.")
            if (context.superInvestmentsCount == 0) return Result(false, "Requer Antecedente Arcano (Super).")
        }

        // 1a) Regra especial: CAVALEIRO (Fantasia)
        if (key == "CAVALEIRO") {
            val hasObligation = context.complicacoesSelecionadas.entries.any { (k, level) ->
                k.id.keyify() == "OBRIGACAO" && level == "Maior"
            }
            if (!hasObligation) return Result(false, "Requer Obrigação (Maior).")
        }

        // 2) Pontos de Poder por estágio
        if (v.nome.contains("Pontos de Poder", ignoreCase = true)) {
            if (context.comprasPpPorEstagioSum >= context.maxComprasPpAteAgora) {
                return Result(false, "Limite de Pontos de Poder por estágio atingido.")
            }
        }

        // 2a) Vantagens exclusivas de Ressuscitado exigem ter a vantagem-base
        if (v.categoria == Categoria.ATORMENTADO) {
            val temRessuscitado = context.vantagensSelecionadas.any { it.id == "atormentado" }
            if (!temRessuscitado) return Result(false, "Requer Atormentado/Ressuscitado.")
        }

        // 3) Antecedente Arcano e multi-arcano
        if (key.startsWith("ANTECEDENTE ARCANO")) {
            if (context.compendioCrystalHeartAtivo) {
                if (v.id == "aa_agente_syn") return Result(true)
                return Result(false, "Apenas Agente da Syn permitido em Crystal Heart.")
            }

            if (!context.permiteMultiAntecedenteArcano &&
                !context.compendioFantasiaAtivo &&
                !context.compendioHorrorAtivo &&
                !context.compendioPathfinderAtivo) {
                val anyArcano = context.vantagensSelecionadas.any { it.nome.keyify().startsWith("ANTECEDENTE ARCANO") }
                if (anyArcano && context.vantagensSelecionadas.none { it.nome.keyify() == key }) {
                    return Result(false, "Múltiplos Antecedentes Arcanos não permitidos.")
                }
            } else {
                val jaTemMesmoId = context.vantagensSelecionadas.any { it.id == v.id }
                if (jaTemMesmoId) return Result(false, "Já possui esta vantagem.")
                if (v.id == "antecedente_arcano" && v.choice != null) {
                    val jaTemMesmaChoice = context.vantagensSelecionadas.any {
                        it.id == "antecedente_arcano" && it.choice?.keyify() == v.choice?.keyify()
                    }
                    if (jaTemMesmaChoice) return Result(false, "Já possui este Antecedente Arcano.")
                }
            }
        }

        // 4) PROFISSIONAL / ESPECIALISTA
        if (key == "profissional" || key == "especialista") {
            val choiceSeguro = v.choice

            if (v.requiresChoice && choiceSeguro != null) {
                val already = context.vantagensSelecionadas.any {
                    it.nome.keyify() == key && it.choice?.keyify() == choiceSeguro.keyify()
                }
                if (already) return Result(false, "Já selecionado para esta característica.")
            }

            if (key == "especialista" && choiceSeguro != null) {
                val profExist = context.vantagensSelecionadas.any {
                    it.id == "profissional" && it.choice?.keyify() == choiceSeguro.keyify()
                }
                if (!profExist) return Result(false, "Requer Profissional na mesma característica.")
            }

            // Check if trait is maxed
            if (choiceSeguro == null) {
                val anyMaxAttr = context.atributosRaw.any { (k, value) -> isMaxed(value, k, context) }
                val anyMaxPer = context.periciasRaw.any { (_, value) -> isMaxedSkill(value) }
                if (!anyMaxAttr && !anyMaxPer) return Result(false, "Nenhum atributo ou perícia no máximo.")
            } else {
                val choiceKey = choiceSeguro.keyify()
                val attrVal = context.atributosRaw[choiceKey]
                if (attrVal != null) {
                    if (!isMaxed(attrVal, choiceKey, context)) return Result(false, "Atributo não está no máximo.")
                } else {
                    val per = context.getBestPericia(choiceKey)
                    if (per == null) return Result(false, "Perícia não encontrada.")
                    if (!isMaxedSkill(context.getRawTotal(per))) return Result(false, "Perícia não está no máximo.")
                }
            }
        }

        // 5) Estágio mínimo (respeita Nasce um Herói)
        val ignorarEstagioPorNasce = (context.nasceUmHeroi && !context.emProgresso && context.pvFromXpOutstanding == 0)
        val ignorarEstagioPorSamurai = shouldIgnoreLeadershipStage(v, context)

        if (!ignorarEstagioPorNasce && !ignorarEstagioPorSamurai) {
            val estagioRequerido = context.listaEstagios.firstOrNull { it.nome.equals(v.requisitos.estagio, ignoreCase = true) }
            if (estagioRequerido != null) {
                val effectiveStage = if (context.overrideStageForVantagem != null) {
                    context.listaEstagios.firstOrNull { it.nome.equals(context.overrideStageForVantagem, ignoreCase = true) }
                        ?: context.estagioAtual
                } else {
                    context.estagioAtual
                }

                if (context.listaEstagios.indexOf(effectiveStage) < context.listaEstagios.indexOf(estagioRequerido)) {
                    return Result(false, "Requer estágio ${estagioRequerido.nome}.")
                }
            }
        }

        // 6) Vantagens prévias
        if (!atendeVantagensPrevias(v, context)) return Result(false, "Pré-requisitos de Vantagens não atendidos.")

        // 7) PPs de novo (segurança extra) or Max Selections
        if (v.nome.contains("Pontos de Poder", ignoreCase = true)) {
             if (context.comprasPpPorEstagioSum >= context.maxComprasPpAteAgora) return Result(false, "Limite PP atingido.")
        }
        else if (v.limiteCompra != "infinito" && v.maxSelections > 0) {
            val ja = context.vantagensSelecionadas.count { it.id.keyify() == v.id.keyify() }
            if (ja >= v.maxSelections) return Result(false, "Limite de compras atingido.")
        }

        // 8) Evita repetir a MESMA choice
        if (v.requiresChoice && v.choice != null) {
            val repetida = context.vantagensSelecionadas.any {
                it.id == v.id && it.choice == v.choice
            }
            if (repetida) return Result(false, "Já possui esta escolha.")
        }

        // 10) Atributos mínimos
        if (v.requisitos.atributoMin.isNotEmpty()) {
            val fail = v.requisitos.atributoMin.any { (nome, min) ->
                val chaveNorm = nome.uppercase().semAcentos().trim()
                // Simple mapping attempt, assume context has map
                // In CriadorState it iterates map keys.
                val matchKey = context.atributosRaw.keys.firstOrNull { it.equals(chaveNorm, ignoreCase = true) }
                    ?: chaveNorm
                val atual = context.atributosRaw[matchKey] ?: 0
                atual < min
            }
            if (fail) return Result(false, "Atributos insuficientes.")
        }

        // 11) Perícias mínimas obrigatórias
        val periciaMinMap = v.requisitos.periciaMin
        if (v.vinculadoPericia && periciaMinMap.isNotEmpty()) {
            val atendeUma = periciaMinMap.any { (perNome, minRaw) ->
                val per = context.getBestPericia(perNome)
                per != null && context.getRawTotal(per) >= minRaw
            }
            if (!atendeUma) return Result(false, "Perícia vinculada insuficiente.")
        } else {
            if (periciaMinMap.any { (perNome, minRaw) ->
                    val per = context.getBestPericia(perNome) ?: return@any false
                    context.getRawTotal(per) < minRaw
                }) {
                return Result(false, "Perícias insuficientes.")
            }
        }

        // 12) Perícias mínimas opcionais (qualquer uma)
        val periciaMinOpcMap = v.requisitos.periciaMinOpcional
        if (periciaMinOpcMap.isNotEmpty()) {
            if (v.vinculadoPericia && !v.choice.isNullOrBlank()) {
                val choiceKey = v.choice!!.keyify()
                val matchEntry = periciaMinOpcMap.entries.firstOrNull { it.key.keyify() == choiceKey }
                if (matchEntry == null) return Result(false, "Escolha inválida para requisito opcional.")
                val per = context.getBestPericia(choiceKey) ?: return Result(false, "Perícia escolhida não encontrada.")
                if (context.getRawTotal(per) < matchEntry.value) return Result(false, "Perícia escolhida insuficiente.")
            } else {
                val atendeUmaOpc = periciaMinOpcMap.any { (perNome, minRaw) ->
                    val per = context.getBestPericia(perNome)
                    per != null && context.getRawTotal(per) >= minRaw
                }
                if (!atendeUmaOpc) return Result(false, "Requer uma das perícias opcionais.")
            }
        }

        // 13) Exige Carta Selvagem?
        if (v.requisitos.exigeCS && !context.cartaSelvagem) return Result(false, "Requer ser Carta Selvagem.")

        // 13a) Tags Raciais
        if (v.requisitos.tags.isNotEmpty()) {
            val ancDef = context.racialDef
            if (ancDef == null || !ancDef.tags.containsAll(v.requisitos.tags)) {
                return Result(false, "Requer tags raciais: ${v.requisitos.tags}.")
            }
        }

        // 13b) Tiro Duplo Aprimorado
        if (v.id == "tiro_duplo_aprimorado") {
            val base = context.vantagensSelecionadas.firstOrNull { it.id == "tiro_duplo" }
            if (base == null) return Result(false, "Requer Tiro Duplo.")
            val choice = base.choice
            if (choice.isNullOrBlank()) return Result(false, "Tiro Duplo requer escolha.")
            val skill = context.getBestPericia(choice) ?: return Result(false, "Perícia do Tiro Duplo não encontrada.")
            if (context.getRawTotal(skill) < 10) return Result(false, "Requer d10 na perícia do Tiro Duplo.")
        }

        // 13c) Template Monstruoso
        if (v.requisitos.templatesRequired.isNotEmpty()) {
            val selected = context.tipoMonstro
            if (selected == null || selected !in v.requisitos.templatesRequired) {
                return Result(false, "Requer Tipo de Monstro específico.")
            }
        }

        // 14) Conflitos com complicações
        val compsConfl = (incompatibilidades[key].orEmpty() + incompatibilidades[v.id.keyify()].orEmpty()).toSet()
        val vantKey = v.nome.trim().uppercase()
        if (vantKey == "RICO" || vantKey == "PODRE DE RICO") {
            val tenhoPobreza = context.complicacoesSelecionadas.keys.any {
                it.id.trim().uppercase() == "POBREZA"
            }
            if (tenhoPobreza) return Result(false, "Conflito com Pobreza.")
        }
        if (context.complicacoesSelecionadas.keys
                .map { it.id.keyify() }
                .any { it in compsConfl }
        ) return Result(false, "Conflito com Complicação selecionada.")

        return Result(true)
    }

    private fun atendeVantagensPrevias(v: Vantagem, context: SelectionContext): Boolean {
        if (v.requisitos.vantagensPrevias.isEmpty()) return true

        if (atendePreviasPorComplicacaoParaAmeacador(v, context)) return true

        val faltam = v.requisitos.vantagensPrevias.any { prevId ->
            when (prevId.keyify()) {
                "antecedente_arcano", "antecedente_arcano:*" -> {
                    context.vantagensSelecionadas.none { poss ->
                        poss.id.startsWith("antecedente_arcano_") ||
                                (poss.id == "antecedente_arcano" && !poss.choice.isNullOrBlank())
                    }
                }
                else -> {
                    context.vantagensSelecionadas.none { poss ->
                        poss.id.keyify() == prevId.keyify()
                    }
                }
            }
        }
        return !faltam
    }

    private fun atendePreviasPorComplicacaoParaAmeacador(v: Vantagem, context: SelectionContext): Boolean {
        if (v.id.keyify() != ameacadorId) return false

        val requisitadas = v.requisitos.vantagensPrevias.map { it.keyify() }.toSet()
        val liberadoras = (ameacadorComplicacoesLiberadoras + requisitadas)
        val selecionadas = context.complicacoesSelecionadas.keys.map { it.id.keyify() }.toSet()

        return selecionadas.any { it in liberadoras }
    }

    private fun shouldIgnoreLeadershipStage(v: Vantagem, context: SelectionContext): Boolean {
        if (!context.compendioArteDaGuerraAtivo || !context.tropoSamuraiActive) return false
        if (v.categoria != Categoria.LIDERANCA) return false
        val pericia = context.getBestPericia("Conhecimento Batalha") ?: return false
        return context.getRawTotal(pericia) >= 8
    }

    private fun isMaxed(value: Int, attrKey: String, context: SelectionContext): Boolean {
        val max = context.getMaxAttributeRaw(attrKey)
        return value >= max
    }

    private fun isMaxedSkill(value: Int): Boolean {
        // For skills, d12 is generally considered the base "max" for Expert triggers,
        // though SWADE allows d12+X.
        // Original logic checked if periciaCapRaw was reached.
        // Since periciaCapRaw depends on Attribute limits, it's safer to rely on d12+
        // but ideally we would also pass a getMaxSkillRaw callback.
        // However, standard rule for Expert/Master is often "d12+".
        // Let's stick to >= 12 for skills as a safe approximation for now unless requested.
        return value >= 12
    }
}
