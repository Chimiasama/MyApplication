package com.example.swadebuilder.model

import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.model.Constants
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.semAcentos
import com.example.swadebuilder.mapaAtributosDisplay
import com.example.swadebuilder.mapaPericias
import com.example.swadebuilder.listaDeEstagios
import com.example.swadebuilder.nivelParaEstagio

/**
 * Validates prerequisites for selecting Advantages (Edges).
 */
object RequirementValidator {

    fun canSelect(v: Vantagem, state: CriadorState): Boolean {
        val key = v.nome.keyify()

        // 0) Exclusividade de Classe/Prestígio (Buscatrilha)
        if (state.vantagensSelecionadas.classeExclusivaBloqueada(v)) return false

        // 1) Regra especial: O MELHOR QUE HÁ
        if (key == Constants.ID_THE_BEST_THERE_IS) {
            if (state.emProgresso) return false
            if (state.superInvestments.isEmpty()) return false
        }

        // 2) Pontos de Poder por estágio
        if (v.nome.contains(Constants.EDGE_POWER_POINTS, ignoreCase = true)) {
            val totalFeitas = state.comprasPpPorEstagio.values.sum()
            val maxPermitidas = state.maxComprasPpAteAgora()
            if (totalFeitas >= maxPermitidas) return false
        }

        // 2a) Vantagens exclusivas de Ressuscitado exigem ter a vantagem-base
        if (v.categoria == Categoria.RESSUSCITADO) {
            val temRessuscitado = state.vantagensSelecionadas.any { it.id == Constants.ID_RESSUSCITADO }
            if (!temRessuscitado) return false
        }

        // 3) Antecedente Arcano e multi-arcano
        if (key.startsWith(Constants.EDGE_ARCANE_BACKGROUND)) {
            if (state.compendioCrystalHeartAtivo) {
                // Em jogos de Crystal Heart, apenas "Agente da Agência" é permitido.
                if (v.id != Constants.ID_AA_AGENT_SYN) return false
            }

            if (!state.permiteMultiAntecedenteArcano) {
                val anyArcano = state.vantagensSelecionadas.any { it.nome.keyify().startsWith(Constants.EDGE_ARCANE_BACKGROUND) }
                if (anyArcano && state.vantagensSelecionadas.none { it.nome.keyify() == key }) {
                    return false
                }
            } else {
                val jaTemMesmoId = state.vantagensSelecionadas.any { it.id == v.id }
                if (jaTemMesmoId) return false
                if (v.id == Constants.ID_AA_PREFIX && v.choice != null) {
                    val jaTemMesmaChoice = state.vantagensSelecionadas.any {
                        it.id == Constants.ID_AA_PREFIX && it.choice?.keyify() == v.choice?.keyify()
                    }
                    if (jaTemMesmaChoice) return false
                }
            }
        }

        // 4) PROFISSIONAL / ESPECIALISTA
        if (key == Constants.EDGE_PROFESSIONAL.keyify() || key == Constants.EDGE_EXPERT.keyify()) {
            val choiceSeguro = v.choice

            if (v.requiresChoice && choiceSeguro != null) {
                val already = state.vantagensSelecionadas.any {
                    it.nome.keyify() == key &&
                            it.choice?.keyify() == choiceSeguro.keyify()
                }
                if (already) return false
            }

            if (key == Constants.EDGE_EXPERT.keyify() && choiceSeguro != null) {
                val profExist = state.vantagensSelecionadas.any {
                    it.id == "profissional" && it.choice?.keyify() == choiceSeguro.keyify()
                }
                if (!profExist) return false
            }

            if (choiceSeguro == null) {
                val anyMaxAttr = com.example.swadebuilder.listaAtributos.any { a ->
                    state.valoresAtributos[a]!!.intValue == state.atributoMaxRaw(a)
                }
                val anyMaxPer = state.periciasComIdiomas().any { p ->
                    state.rawTotal(p) == state.periciaCapRaw(p)
                }
                return anyMaxAttr || anyMaxPer
            }

            val choiceKey = choiceSeguro.keyify()
            return if (com.example.swadebuilder.listaAtributos.contains(choiceKey)) {
                state.valoresAtributos[choiceKey]!!.intValue == state.atributoMaxRaw(choiceKey)
            } else {
                val per = mapaPericias[choiceKey] ?: return false
                state.rawTotal(per) == state.periciaCapRaw(per)
            }
        }

        // 5) Estágio mínimo (respeita Nasce um Herói)
        val ignorarEstagioPorNasce = (state.nasceUmHeroi && !state.emProgresso && state.pvFromXpOutstanding == 0)
        if (!ignorarEstagioPorNasce) {
            val estagioRequerido = listaDeEstagios.firstOrNull { it.nome.equals(v.requisitos.estagio, ignoreCase = true) }
            if (estagioRequerido != null) {
                val estagioAtual = state.overrideStageForVantagem?.let { stageName ->
                    listaDeEstagios.firstOrNull { it.nome.equals(stageName, ignoreCase = true) }
                } ?: state.estagioAtual()

                if (listaDeEstagios.indexOf(estagioAtual) < listaDeEstagios.indexOf(estagioRequerido)) {
                    return false
                }
            }
        }

        // 6) Vantagens prévias
        if (v.requisitos.vantagensPrevias.isNotEmpty()) {
            val faltam = v.requisitos.vantagensPrevias.any { prevId ->
                when (prevId) {
                    Constants.ID_AA_PREFIX, "${Constants.ID_AA_PREFIX}:*" -> {
                        state.vantagensSelecionadas.none { poss ->
                            poss.id.startsWith("${Constants.ID_AA_PREFIX}_") ||
                                    (poss.id == Constants.ID_AA_PREFIX && !poss.choice.isNullOrBlank())
                        }
                    }
                    else -> {
                        state.vantagensSelecionadas.none { poss ->
                            poss.id == prevId
                        }
                    }
                }
            }
            if (faltam) return false
        }

        // 7) PPs de novo (segurança extra)
        if (v.nome.contains(Constants.EDGE_POWER_POINTS, ignoreCase = true)) {
            val totalCompras = state.comprasPpPorEstagio.values.sum()
            val limite = state.maxComprasPpAteAgora()
            if (totalCompras >= limite) return false
        }
        else if (v.limiteCompra != "infinito" && v.maxSelections > 0) {
            val ja = state.vantagensSelecionadas.count { it.id == v.id }
            if (ja >= v.maxSelections) return false
        }

        // 8) Evita repetir a MESMA choice em vantagens com escolha
        val choiceSeguro2 = v.choice
        if (v.requiresChoice && choiceSeguro2 != null) {
            val repetida = state.vantagensSelecionadas.any {
                it.id == v.id && it.choice == choiceSeguro2
            }
            if (repetida) return false
        }

        // 9) Estágio alternativo
        nivelParaEstagio[v.requisitos.estagio]?.let { estReqObj2 ->
             // Need access to effectiveProgressoParaVantagens, which is private in CriadorState.
             // We can simulate it here or expose it.
             // Simulating for now:
             val stName = state.overrideStageForVantagem ?: ""
             val prog = if (stName.isNotEmpty()) {
                 listaDeEstagios.firstOrNull { it.nome.equals(stName, ignoreCase = true) }?.minProgress ?: state.progresso
             } else state.progresso

            if (estReqObj2.minProgress > prog) return false
        }

        // 10) Atributos mínimos
        if (v.requisitos.atributoMin.any { (nome, min) ->
                val chaveNorm = nome.uppercase().semAcentos().trim()
                val attrKey = mapaAtributosDisplay.keys.firstOrNull {
                    it.equals(chaveNorm, ignoreCase = true)
                } ?: chaveNorm
                val atual = state.valoresAtributos[attrKey]?.intValue ?: return false
                atual < min
            }) return false

        // 11) Perícias mínimas obrigatórias
        val periciaMinMap = v.requisitos.periciaMin
        if (v.vinculadoPericia && periciaMinMap.isNotEmpty()) {
            val atendeUma = periciaMinMap.any { (perNome, minRaw) ->
                val per = mapaPericias[perNome.keyify()]
                per != null && state.rawTotal(per) >= minRaw
            }
            if (!atendeUma) return false
        } else {
            if (periciaMinMap.any { (perNome, minRaw) ->
                    val per = mapaPericias[perNome.keyify()] ?: return@any false
                    state.rawTotal(per) < minRaw
                }) {
                return false
            }
        }

        // 12) Perícias mínimas opcionais (qualquer uma)
        val periciaMinOpcMap = v.requisitos.periciaMinOpcional
        if (periciaMinOpcMap.isNotEmpty()) {
            val atendeUmaOpc = periciaMinOpcMap.any { (perNome, minRaw) ->
                val per = mapaPericias[perNome.keyify()]
                per != null && state.rawTotal(per) >= minRaw
            }
            if (!atendeUmaOpc) return false
        }

        // 13) Exige Carta Selvagem?
        if (v.requisitos.exigeCS && !state.cartaSelvagem) return false

        // 14) Conflitos com complicações
        // Incompatibilities are private in CriadorState, need to reproduce logic or move map to Constants/Global.
        // Replicating map here since it's small and static constants.
        val incompatibilidades: Map<String, Set<String>> = mapOf(
            Constants.EDGE_SLOW   to setOf(Constants.EDGE_FLEET_FOOTED),
            Constants.EDGE_FLEET_FOOTED to setOf(Constants.EDGE_SLOW),
            Constants.EDGE_OBESE      to setOf(Constants.EDGE_MUSCULAR),
            Constants.EDGE_MUSCULAR  to setOf(Constants.EDGE_OBESE),
            Constants.EDGE_POVERTY        to setOf(Constants.EDGE_RICH, Constants.EDGE_FILTHY_RICH),
            Constants.EDGE_RICH           to setOf(Constants.EDGE_POVERTY),
            Constants.EDGE_FILTHY_RICH  to setOf(Constants.EDGE_POVERTY)
        )

        val compsConfl = incompatibilidades[key] ?: emptySet()
        val vantKey = v.nome.trim().uppercase()
        if (vantKey == Constants.EDGE_RICH || vantKey == Constants.EDGE_FILTHY_RICH) {
            val tenhoPobreza = state.complicacoesSelecionadas.keys.any {
                it.id.trim().uppercase() == Constants.EDGE_POVERTY
            }
            if (tenhoPobreza) return false
        }
        if (state.complicacoesSelecionadas.keys
                .map { it.id.keyify() }
                .any { it in compsConfl }
        ) return false

        return true
    }
}
