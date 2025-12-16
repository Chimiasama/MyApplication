package com.example.swadebuilder.model

import kotlinx.serialization.Serializable

@Serializable
data class ArcanoInfo(
    val key: String,
    val slots: Int,
    val pp: Int,
    val foco: String
)
