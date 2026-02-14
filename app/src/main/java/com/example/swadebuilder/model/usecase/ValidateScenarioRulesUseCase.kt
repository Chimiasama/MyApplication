package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.util.keyify

class ValidateScenarioRulesUseCase {

    data class Input(
        val vantagem: Vantagem,
        val ancestralidade: String,
        val compendioCrystalHeartAtivo: Boolean,
        val compendioFantasiaAtivo: Boolean,
        val compendioPathfinderAtivo: Boolean
    )

    fun execute(input: Input): Boolean {
        val key = input.vantagem.nome.keyify()
        val vId = input.vantagem.id
        val ancestralidadeKey = input.ancestralidade.keyify()

        // Cidade do Sol a Vapor: AA (Demônio)
        if (vId == "aa_demonio") {
            val isMeioDemonio = ancestralidadeKey.contains("MEIO-DEMONIO")
            val isDemonio = ancestralidadeKey.contains("DEMONIO") && !isMeioDemonio
            if (!isMeioDemonio && !isDemonio) return false
        }

        // Crystal Heart Blocks
        if (input.compendioCrystalHeartAtivo) {
            val forbiddenIds = setOf(
                "campeao", "chi", "linguista", "resistencia_arcana", "resistencia_arcana_aprimorada",
                "rico", "podre_de_rico",
                "aristocrata", "arma_predileta", "comando", "conexoes",
                "antecedente_arcano"
            )
            val vKey = vId.keyify()

            if (vKey in forbiddenIds) return false

            // Block Power Edges unless Crystal Heart specific
            if (input.vantagem.categoria.name.equals("PODER", ignoreCase = true) &&
                input.vantagem.origem != "CRYSTAL_HEART") {
                return false
            }

            // Allow only "Antecedente Arcano: Canalizar Cristal" (aa_agente_syn)
            if (key.startsWith("ANTECEDENTE ARCANO")) {
                 if (vId != "aa_agente_syn") return false
            }
        }

        // Fantasia: "Mago" block
        if (input.compendioFantasiaAtivo && vId == "mago") return false

        // Pathfinder: Block specific ABs
        if (input.compendioPathfinderAtivo) {
            val forbiddenIds = setOf(
                "antecedente_arcano_ciencia_estranha",
                "antecedente_arcano_psionicos",
                "antecedente_arcano_dom"
            )
            if (vId in forbiddenIds) return false
        }

        return true
    }
}
