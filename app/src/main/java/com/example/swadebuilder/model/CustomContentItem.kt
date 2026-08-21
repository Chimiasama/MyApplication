package com.example.swadebuilder.model

import kotlinx.serialization.Serializable

@Serializable
enum class CustomContentType {
    ADVANTAGE,
    HINDRANCE,
    EQUIPMENT,
    POWER
}

@Serializable
data class CustomContentItem(
    val id: String,
    val name: String,
    val type: CustomContentType,
    val category: String = "CUSTOM",
    val description: String,
    val origin: String = "FANMADE",
    val schemaVersion: Int = 1,
    val cost: Int = 0,
    val rank: String = "Novato",
    val requirementsText: String? = null
)

@Serializable
data class CustomContentPackage(
    val packageId: String,
    val packageName: String,
    val author: String,
    val version: String = "1.0",
    val description: String = "",
    val items: List<CustomContentItem> = emptyList()
)
