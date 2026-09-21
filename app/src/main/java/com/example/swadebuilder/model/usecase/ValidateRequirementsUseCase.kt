package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.Pericia
import com.example.swadebuilder.model.RacialModifier
import com.example.swadebuilder.model.RacialTraitPointCatalog
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

        // 13a) Tags Raciais — "asas" (Golpe de Asa) e "arma_de_sopro" (Queimar) checam o
        // traço de VERDADE da raça (RacialTraitPointCatalog.temTracoVoo()/
        // temArmaDeSopro()), não a tag manual solta em ancestralidades.json: essa tag
        // podia ficar desatualizada (achado real ao implementar isso — Draconianos
        // tinham a tag "asas" cadastrada mesmo sem o traço Voo, quando o próprio livro
        // diz que "asas" é só uma Ideia Variante opcional pra Draconianos, não o padrão).
        // As demais tags (nacionalidades do Crystal Heart, ex. "BOGOVIANO") continuam
        // batendo contra a lista manual de sempre — não têm um traço mecânico
        // equivalente pra checar.
        if (v.requisitos.tags.isNotEmpty()) {
            val ancDef = input.ancestralidadeDef
            val atendeTodasAsTags = v.requisitos.tags.all { tag ->
                when (tag.keyify()) {
                    "ASAS" -> RacialTraitPointCatalog.temTracoVoo(ancDef?.habilidades)
                    "ARMA_DE_SOPRO" -> RacialTraitPointCatalog.temArmaDeSopro(ancDef?.habilidades)
                    else -> ancDef?.tags?.any { it.keyify() == tag.keyify() } == true
                }
            }
            if (!atendeTodasAsTags) return false
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
