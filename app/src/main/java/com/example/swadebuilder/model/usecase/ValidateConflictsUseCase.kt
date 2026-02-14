package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.Complicacao
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.util.keyify

class ValidateConflictsUseCase {

    data class Input(
        val vantagem: Vantagem,
        val complicacoesSelecionadas: Map<Complicacao, String?>
    )

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

    fun execute(input: Input): Boolean {
        val key = input.vantagem.nome.keyify()
        val vIdKey = input.vantagem.id.keyify()

        // 14) Conflitos com complicações
        val compsConfl = (incompatibilidades[key].orEmpty() + incompatibilidades[vIdKey].orEmpty()).toSet()
        val vantKey = input.vantagem.nome.trim().uppercase()

        if (vantKey == "RICO" || vantKey == "PODRE DE RICO") {
            val tenhoPobreza = input.complicacoesSelecionadas.keys.any {
                it.id.trim().uppercase() == "POBREZA"
            }
            if (tenhoPobreza) return false
        }

        if (input.complicacoesSelecionadas.keys
                .map { it.id.keyify() }
                .any { it in compsConfl }
        ) return false

        return true
    }
}
