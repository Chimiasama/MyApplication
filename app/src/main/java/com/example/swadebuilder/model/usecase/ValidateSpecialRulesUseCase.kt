package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.Complicacao
import com.example.swadebuilder.model.Estagio
import com.example.swadebuilder.model.Pericia
import com.example.swadebuilder.model.SuperInvestment
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.model.classeExclusivaBloqueada
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.model.Categoria

class ValidateSpecialRulesUseCase {

    data class Input(
        val vantagem: Vantagem,
        val vantagensSelecionadas: List<Vantagem>,
        val complicacoesSelecionadas: Map<Complicacao, String?>,
        val emProgresso: Boolean,
        val superInvestments: List<SuperInvestment>,
        val listaAtributos: List<String>,
        val valoresAtributos: Map<String, Int>,
        val atributoMaxRaw: (String) -> Int,
        val pericias: List<Pericia>,
        val rawTotalPericia: (Pericia) -> Int,
        val periciaCapRaw: (Pericia) -> Int,
        val permiteMultiAntecedenteArcano: Boolean,
        val compendioFantasiaAtivo: Boolean,
        val compendioHorrorAtivo: Boolean,
        val compendioPathfinderAtivo: Boolean,
        val compendioCrystalHeartAtivo: Boolean,
        val estagioAtual: Estagio,
        val listaDeEstagios: List<Estagio>,
        val overrideStageForVantagem: String?,
        val effectiveProgressoParaVantagens: Int,
        val nivelParaEstagio: Map<String, Estagio>,
        val nasceUmHeroi: Boolean,
        val pvFromXpOutstanding: Int,
        val compendioArteDaGuerraAtivo: Boolean,
        val tropoSelecionadoId: String?,
        val getBestPericia: (String) -> Pericia?
    )

    fun execute(input: Input): Boolean {
        val v = input.vantagem
        val key = v.nome.keyify()

        // 0) Exclusividade de Classe/Prestígio (Buscatrilha)
        if (input.vantagensSelecionadas.classeExclusivaBloqueada(v)) return false

        // 1) Regra especial: O MELHOR QUE HÁ
        if (key == "o_melhor_que_ha") {
            if (input.emProgresso) return false
            if (input.superInvestments.isEmpty()) return false
        }

        // 1a) Regra especial: CAVALEIRO (Fantasia)
        if (key == "CAVALEIRO") {
            val hasObligation = input.complicacoesSelecionadas.entries.any { (k, v) ->
                k.id.keyify() == "OBRIGACAO" && v == "Maior"
            }
            if (!hasObligation) return false
        }

        // 2a) Vantagens exclusivas de Ressuscitado exigem ter a vantagem-base
        if (v.categoria == Categoria.ATORMENTADO) {
            val temRessuscitado = input.vantagensSelecionadas.any { it.id == "atormentado" }
            if (!temRessuscitado) return false
        }

        // 3) Antecedente Arcano e multi-arcano
        if (key.startsWith("ANTECEDENTE ARCANO")) {
            if (input.compendioCrystalHeartAtivo) {
                // Handled in ValidateScenarioRules
            } else if (!input.permiteMultiAntecedenteArcano &&
                !input.compendioFantasiaAtivo &&
                !input.compendioHorrorAtivo &&
                !input.compendioPathfinderAtivo) {

                val anyArcano = input.vantagensSelecionadas.any { it.nome.keyify().startsWith("ANTECEDENTE ARCANO") }
                if (anyArcano && input.vantagensSelecionadas.none { it.nome.keyify() == key }) {
                    return false
                }
            } else {
                val jaTemMesmoId = input.vantagensSelecionadas.any { it.id == v.id }
                if (jaTemMesmoId) return false
                val vChoice = v.choice
                if (v.id == "antecedente_arcano" && vChoice != null) {
                    val jaTemMesmaChoice = input.vantagensSelecionadas.any {
                        it.id == "antecedente_arcano" && it.choice?.keyify() == vChoice.keyify()
                    }
                    if (jaTemMesmaChoice) return false
                }
            }
        }

        // 4) PROFISSIONAL / ESPECIALISTA
        if (key == "profissional" || key == "especialista") {
            val choiceSeguro = v.choice

            if (v.requiresChoice && choiceSeguro != null) {
                val already = input.vantagensSelecionadas.any {
                    it.nome.keyify() == key &&
                            it.choice?.keyify() == choiceSeguro.keyify()
                }
                if (already) return false
            }

            if (key == "especialista" && choiceSeguro != null) {
                val profExist = input.vantagensSelecionadas.any {
                    it.id == "profissional" && it.choice?.keyify() == choiceSeguro.keyify()
                }
                if (!profExist) return false
            }

            if (choiceSeguro == null) {
                val anyMaxAttr = input.listaAtributos.any { a ->
                    (input.valoresAtributos[a] ?: 0) == input.atributoMaxRaw(a)
                }
                val anyMaxPer = input.pericias.any { p ->
                    input.rawTotalPericia(p) == input.periciaCapRaw(p)
                }
                return anyMaxAttr || anyMaxPer
            }

            val choiceKey = choiceSeguro.keyify()
            return if (input.listaAtributos.contains(choiceKey)) {
                val valAttr = input.valoresAtributos[choiceKey] ?: 0
                valAttr == input.atributoMaxRaw(choiceKey)
            } else {
                val per = input.getBestPericia(choiceKey) ?: return false
                input.rawTotalPericia(per) == input.periciaCapRaw(per)
            }
        }

        // 5) Estágio mínimo (respeita Nasce um Herói)
        val ignorarEstagioPorNasce = (input.nasceUmHeroi && !input.emProgresso && input.pvFromXpOutstanding == 0)

        fun shouldIgnoreLeadershipStage(v: Vantagem): Boolean {
            if (!input.compendioArteDaGuerraAtivo || input.tropoSelecionadoId != "tropo_samurai") return false
            if (v.categoria != Categoria.LIDERANCA) return false
            val pericia = input.getBestPericia("Conhecimento Batalha") ?: return false
            return input.rawTotalPericia(pericia) >= 8
        }

        val ignorarEstagioPorSamurai = shouldIgnoreLeadershipStage(v)

        if (!ignorarEstagioPorNasce && !ignorarEstagioPorSamurai) {
            val estagioRequerido = input.listaDeEstagios.firstOrNull { it.nome.equals(v.requisitos.estagio, ignoreCase = true) }
            if (estagioRequerido != null) {
                val estagioAtual = input.overrideStageForVantagem?.let { stageName ->
                    input.listaDeEstagios.firstOrNull { it.nome.equals(stageName, ignoreCase = true) }
                } ?: input.estagioAtual

                if (input.listaDeEstagios.indexOf(estagioAtual) < input.listaDeEstagios.indexOf(estagioRequerido)) {
                    return false
                }
            }
        }

        // 8) Evita repetir a MESMA choice em vantagens com escolha
        val choiceSeguro2 = v.choice
        if (v.requiresChoice && choiceSeguro2 != null) {
            val repetida = input.vantagensSelecionadas.any {
                it.id == v.id && it.choice == choiceSeguro2
            }
            if (repetida) return false
        }

        // 9) Estágio alternativo (tabela nivelParaEstagio)
        input.nivelParaEstagio[v.requisitos.estagio]?.let { estReqObj2 ->
            if (estReqObj2.minProgress > input.effectiveProgressoParaVantagens) return false
        }

        // 13b) Tiro Duplo Aprimorado
        if (v.id == "tiro_duplo_aprimorado") {
            val base = input.vantagensSelecionadas.firstOrNull { it.id == "tiro_duplo" }
            if (base == null) return false
            val choice = base.choice
            if (choice.isNullOrBlank()) return false
            val skill = input.getBestPericia(choice) ?: return false
            if (input.rawTotalPericia(skill) < 10) return false
        }

        return true
    }
}
