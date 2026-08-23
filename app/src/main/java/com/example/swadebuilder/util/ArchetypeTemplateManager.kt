package com.example.swadebuilder.util

import com.example.swadebuilder.model.ArchetypeApplicationReport
import com.example.swadebuilder.model.CreationArchetype
import com.example.swadebuilder.model.ArchetypeAttributeBonus
import com.example.swadebuilder.model.ArchetypeSkillBonus

class ArchetypeTemplateManager {

    val defaultArchetypes: List<CreationArchetype> = listOf(
        CreationArchetype(
            id = "guerreiro_corpo_a_corpo",
            name = "Guerreiro Corpo a Corpo",
            description = "Especialista em combate próximo com alta resistência e força bruta.",
            targetSetting = "BASICO",
            attributes = listOf(
                ArchetypeAttributeBonus("FORCA", 2),
                ArchetypeAttributeBonus("VIGOR", 2)
            ),
            skills = listOf(
                ArchetypeSkillBonus("Lutar", 3),
                ArchetypeSkillBonus("Atletismo", 2)
            ),
            edges = listOf("vantagem_marcial"),
            hindrances = listOf("complicacao_leal"),
            explanationNotes = listOf("Foco em Força, Vigor e Lutar para dominar a linha de frente.")
        ),
        CreationArchetype(
            id = "atirador_pistoleiro",
            name = "Atirador / Pistoleiro",
            description = "Especialista em combate à distância com reflexos rápidos e precisão.",
            targetSetting = "BASICO",
            attributes = listOf(
                ArchetypeAttributeBonus("AGILIDADE", 2),
                ArchetypeAttributeBonus("ASTUCIA", 1)
            ),
            skills = listOf(
                ArchetypeSkillBonus("Atirar", 3),
                ArchetypeSkillBonus("Perceber", 2)
            ),
            edges = listOf("vantagem_sacar_rapido"),
            hindrances = listOf("complicacao_excesso_confianca"),
            explanationNotes = listOf("Foco em Agilidade e Atirar para rápida capacidade de resposta a distância.")
        ),
        CreationArchetype(
            id = "investigador_social",
            name = "Investigador / Social",
            description = "Perito em reunir pistas, analisar pessoas e persuadir aliados.",
            targetSetting = "BASICO",
            attributes = listOf(
                ArchetypeAttributeBonus("ASTUCIA", 2),
                ArchetypeAttributeBonus("ESPIRITO", 2)
            ),
            skills = listOf(
                ArchetypeSkillBonus("Pesquisar", 2),
                ArchetypeSkillBonus("Persuasao", 3),
                ArchetypeSkillBonus("Perceber", 2)
            ),
            edges = listOf("vantagem_carismatico"),
            hindrances = listOf("complicacao_curioso"),
            explanationNotes = listOf("Foco em Astúcia e Espírito para liderança interpessoal e dedução.")
        ),
        CreationArchetype(
            id = "conjurador_basico",
            name = "Conjurador Básico",
            description = "Mestre das artes arcanas capaz de moldar energia mágica.",
            targetSetting = "FANTASIA",
            attributes = listOf(
                ArchetypeAttributeBonus("ASTUCIA", 2),
                ArchetypeAttributeBonus("ESPIRITO", 1)
            ),
            skills = listOf(
                ArchetypeSkillBonus("Conjuracao", 3),
                ArchetypeSkillBonus("Conhecimento_Academico", 2)
            ),
            edges = listOf("antecedente_arcano_magia"),
            powers = listOf("poder_raio", "poder_protecao"),
            explanationNotes = listOf("Requer Antecedente Arcano e investe em Conjuração e Poderes.")
        )
    )

    fun getArchetypesForSetting(settingKey: String): List<CreationArchetype> {
        val normalized = settingKey.trim().uppercase()
        return defaultArchetypes.filter {
            it.targetSetting.uppercase() == "BASICO" || it.targetSetting.uppercase() == normalized
        }
    }

    fun generateReport(archetype: CreationArchetype): ArchetypeApplicationReport {
        return ArchetypeApplicationReport(
            archetypeId = archetype.id,
            archetypeName = archetype.name,
            appliedAttributes = archetype.attributes.map { "${it.attributeName} (+${it.diceIncrements})" },
            appliedSkills = archetype.skills.map { "${it.skillName} (+${it.diceIncrements})" },
            appliedEdges = archetype.edges,
            appliedHindrances = archetype.hindrances,
            appliedPowers = archetype.powers,
            warnings = if (archetype.powers.isNotEmpty() && archetype.edges.none { it.startsWith("antecedente_arcano") }) {
                listOf("Atenção: Este arquétipo possui poderes, mas pode exigir selecionar um Antecedente Arcano.")
            } else {
                emptyList()
            }
        )
    }
}
