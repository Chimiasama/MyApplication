package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.Pericia
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
        val cartaSelvagem: Boolean,
        // Resolve o nome de um requisito para a "melhor" perícia equivalente do personagem
        // (ex.: em Arte da Guerra, um requisito de "Lutar nível X" deve poder ser satisfeito
        // pela melhor categoria de Jutsu do personagem, não só pelo slot base). Por padrão
        // cai para uma busca simples por nome dentro de `pericias`.
        val getBestPericia: (String) -> Pericia? = { nome ->
            val key = nome.keyify()
            pericias.firstOrNull { it.nome.keyify() == key }
        }
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
                val per = input.getBestPericia(perNome)
                per != null && input.rawTotalPericia(per) >= minRaw
            }
            if (!atendeUma) return false
        } else {
            if (periciaMinMap.any { (perNome, minRaw) ->
                    val per = input.getBestPericia(perNome) ?: return@any false
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
                val per = input.getBestPericia(choiceKey) ?: return false
                if (input.rawTotalPericia(per) < matchEntry.value) return false
            } else {
                val atendeUmaOpc = periciaMinOpcMap.any { (perNome, minRaw) ->
                    val per = input.getBestPericia(perNome)
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
}
