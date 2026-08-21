package com.example.swadebuilder.model

import kotlinx.serialization.Serializable

@Serializable
data class ArchetypeAttributeBonus(
    val attributeName: String,
    val diceIncrements: Int
)

@Serializable
data class ArchetypeSkillBonus(
    val skillName: String,
    val diceIncrements: Int
)

@Serializable
data class CreationArchetype(
    val id: String,
    val name: String,
    val description: String,
    val targetSetting: String = "BASICO",
    val attributes: List<ArchetypeAttributeBonus> = emptyList(),
    val skills: List<ArchetypeSkillBonus> = emptyList(),
    val edges: List<String> = emptyList(),
    val hindrances: List<String> = emptyList(),
    val powers: List<String> = emptyList(),
    val explanationNotes: List<String> = emptyList()
)

@Serializable
data class ArchetypeApplicationReport(
    val archetypeId: String,
    val archetypeName: String,
    val appliedAttributes: List<String>,
    val appliedSkills: List<String>,
    val appliedEdges: List<String>,
    val appliedHindrances: List<String>,
    val appliedPowers: List<String>,
    val warnings: List<String> = emptyList()
)
