package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.Complicacao
import com.example.swadebuilder.model.GrupoAlternativo
import com.example.swadebuilder.model.Pericia
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.semAcentos

class ValidatePrerequisiteUseCase {

    data class Input(
        val vantagem: Vantagem,
        val vantagensSelecionadas: List<Vantagem>,
        val complicacoesSelecionadas: Collection<Complicacao>,
        // Só usados por `requisitos.gruposAlternativos` (perícia mínima ou atributo mínimo
        // dentro de uma alternativa, ex.: "Magomecânico OU Consertar d10+ e Ciência d10+", ou
        // Ciência Ficção "Drenar a Alma" via Poderes Místicos substituindo a perícia arcana por
        // Espírito). Todo o resto desta classe (vantagensPrevias/grupoMinimo) não precisa nem
        // de perícia nem de atributo.
        val pericias: List<Pericia> = emptyList(),
        val rawTotalPericia: (Pericia) -> Int = { 0 },
        val getBestPericia: (String) -> Pericia? = { nome ->
            val key = nome.keyify()
            pericias.firstOrNull { it.nome.keyify() == key }
        },
        val valoresAtributos: Map<String, Int> = emptyMap()
    )

    private val ameacadorComplicacoesLiberadoras = setOf(
        "sanguinario",
        "desagradavel",
        "sem_escrupulos",
        "feio",
        "sombrio",
        "sinistro"
    ).map { it.keyify() }.toSet()

    private val ameacadorId = "ameacador".keyify()

    private fun atendePreviasPorComplicacaoParaAmeacador(v: Vantagem, complicacoes: Collection<Complicacao>): Boolean {
        if (v.id.keyify() != ameacadorId) return false

        val requisitadas = v.requisitos.vantagensPrevias.map { it.keyify() }.toSet()
        val liberadoras = (ameacadorComplicacoesLiberadoras + requisitadas)
        val selecionadas = complicacoes.map { it.id.keyify() }.toSet()

        return selecionadas.any { it in liberadoras }
    }

    // Extraído do antigo corpo de `execute` sem mudar comportamento nenhum — só reaproveitado
    // agora também pelos itens de `grupoMinimo`/`gruposAlternativos`, que citam a mesma
    // sintaxe de id ("ANTECEDENTE_ARCANO" pra qualquer variante específica, ou um id exato de
    // Vantagem/Complicação).
    private fun temVantagemOuComplicacao(refId: String, input: Input): Boolean {
        return when (refId.keyify().replace(" ", "_")) {
            "ANTECEDENTE_ARCANO", "ANTECEDENTE_ARCANO:*" -> {
                input.vantagensSelecionadas.any { poss ->
                    poss.id.startsWith("antecedente_arcano_") ||
                            poss.id.startsWith("aa_") ||
                            (poss.id == "antecedente_arcano" && !poss.choice.isNullOrBlank())
                }
            }
            else -> {
                val idNorm = refId.keyify().replace(" ", "_")
                val temVantagem = input.vantagensSelecionadas.any { poss ->
                    poss.id.keyify().replace(" ", "_") == idNorm
                }
                val temComplicacao = input.complicacoesSelecionadas.any {
                    it.id.keyify().replace(" ", "_") == idNorm
                }
                temVantagem || temComplicacao
            }
        }
    }

    private fun satisfazAlternativa(alt: GrupoAlternativo, input: Input): Boolean {
        val vantagensOk = alt.vantagens.all { temVantagemOuComplicacao(it, input) }
        val periciasOk = alt.pericias.all { (nome, min) ->
            val per = input.getBestPericia(nome) ?: return@all false
            input.rawTotalPericia(per) >= min
        }
        val periciaOpcionalOk = alt.periciaMinOpcional.isEmpty() || alt.periciaMinOpcional.any { (nome, min) ->
            val per = input.getBestPericia(nome)
            per != null && input.rawTotalPericia(per) >= min
        }
        val atributosOk = alt.atributos.all { (nome, min) ->
            val chaveNorm = nome.uppercase().semAcentos().trim()
            val attrKey = input.valoresAtributos.keys.firstOrNull { it.equals(chaveNorm, ignoreCase = true) } ?: chaveNorm
            (input.valoresAtributos[attrKey] ?: 0) >= min
        }
        return vantagensOk && periciasOk && periciaOpcionalOk && atributosOk
    }

    fun execute(input: Input): Boolean {
        val v = input.vantagem

        // 1) Lista fixa de pré-requisitos (E/AND) — comportamento inalterado.
        if (v.requisitos.vantagensPrevias.isNotEmpty() &&
            !atendePreviasPorComplicacaoParaAmeacador(v, input.complicacoesSelecionadas)
        ) {
            val faltam = v.requisitos.vantagensPrevias.any { prevId -> !temVantagemOuComplicacao(prevId, input) }
            if (faltam) return false
        }

        // 2) "Pelo menos N destas opções" (ex.: Bando de Guerra — Comando + pelo menos duas
        // outras Vantagens de Liderança). Independente do item 1: soma-se a ele (E), nunca o
        // substitui.
        val grupoMinimo = v.requisitos.grupoMinimo
        if (grupoMinimo != null && grupoMinimo.opcoes.isNotEmpty()) {
            val quantasTem = grupoMinimo.opcoes.count { temVantagemOuComplicacao(it, input) }
            if (quantasTem < grupoMinimo.minimo) return false
        }

        // 3) "Isto OU aquilo" — cada alternativa é um pacote de vantagens/perícias que precisa
        // ser satisfeito por completo (E dentro da alternativa); basta UMA alternativa bater
        // (ex.: Antecedente Arcano (qualquer um) OU Poderes Místicos (qualquer um); ou
        // Magomecânico OU Consertar d10+ e Ciência d10+).
        val alternativas = v.requisitos.gruposAlternativos
        if (alternativas.isNotEmpty()) {
            if (alternativas.none { satisfazAlternativa(it, input) }) return false
        }

        return true
    }
}
