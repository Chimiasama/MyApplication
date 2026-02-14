package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.Pericia
import com.example.swadebuilder.model.RacialModifier
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.semAcentos

class ValidateRequirementsUseCase {

    data class Input(
        val vantagem: Vantagem,
        val valoresAtributos: Map<String, Int>,
        val pericias: List<Pericia>,
        val rawTotalPericia: (Pericia) -> Int,
        val ancestralidadeDef: RacialModifier?,
        val tipoMonstroSelecionado: String?,
        val cartaSelvagem: Boolean
    )

    fun execute(input: Input): Boolean {
        val v = input.vantagem

        // 10) Atributos mínimos
        if (v.requisitos.atributoMin.any { (nome, min) ->
                val chaveNorm = nome.uppercase().semAcentos().trim()
                val attrKey = input.valoresAtributos.keys.firstOrNull {
                    it.equals(chaveNorm, ignoreCase = true)
                } ?: chaveNorm

                val atual = input.valoresAtributos[attrKey] ?: 0
                atual < min
            }) return false

        // 11) Perícias mínimas obrigatórias
        val periciaMinMap = v.requisitos.periciaMin
        if (v.vinculadoPericia && periciaMinMap.isNotEmpty()) {
            val atendeUma = periciaMinMap.any { (perNome, minRaw) ->
                val per = getBestPericia(perNome, input.pericias)
                per != null && input.rawTotalPericia(per) >= minRaw
            }
            if (!atendeUma) return false
        } else {
            if (periciaMinMap.any { (perNome, minRaw) ->
                    val per = getBestPericia(perNome, input.pericias) ?: return@any false
                    input.rawTotalPericia(per) < minRaw
                }) {
                return false
            }
        }

        // 12) Perícias mínimas opcionais (qualquer uma)
        val periciaMinOpcMap = v.requisitos.periciaMinOpcional
        if (periciaMinOpcMap.isNotEmpty()) {
            val choice = v.choice
            if (v.vinculadoPericia && !choice.isNullOrBlank()) {
                val choiceKey = choice.keyify()
                val matchEntry = periciaMinOpcMap.entries.firstOrNull { it.key.keyify() == choiceKey }
                if (matchEntry == null) return false
                val per = getBestPericia(choiceKey, input.pericias) ?: return false
                if (input.rawTotalPericia(per) < matchEntry.value) return false
            } else {
                val atendeUmaOpc = periciaMinOpcMap.any { (perNome, minRaw) ->
                    val per = getBestPericia(perNome, input.pericias)
                    per != null && input.rawTotalPericia(per) >= minRaw
                }
                if (!atendeUmaOpc) return false
            }
        }

        // 13) Exige Carta Selvagem?
        if (v.requisitos.exigeCS && !input.cartaSelvagem) return false

        // 13a) Tags Raciais
        if (v.requisitos.tags.isNotEmpty()) {
            val ancDef = input.ancestralidadeDef
            if (ancDef == null || !ancDef.tags.containsAll(v.requisitos.tags)) {
                return false
            }
        }

        // 13c) Template Monstruoso
        if (v.requisitos.templatesRequired.isNotEmpty()) {
            val selected = input.tipoMonstroSelecionado
            if (selected == null || selected !in v.requisitos.templatesRequired) {
                return false
            }
        }

        return true
    }

    private fun getBestPericia(nome: String, pericias: List<Pericia>): Pericia? {
        val key = nome.keyify()
        return pericias.firstOrNull { it.nome.keyify() == key }
    }
}
