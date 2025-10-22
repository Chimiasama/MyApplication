// ArcanoInfo.kt
package com.example.swadebuilder.model

import kotlinx.serialization.Serializable

@Serializable
data class ArcanoInfoItem(
    val key: String,   // Ex.: "DOM", "MAGIA", "CIÊNCIA ESTRANHA"...
    val slots: Int,    // número de slots base para esse arcano
    val pp: Int,       // Pontos de Poder iniciais para esse arcano
    val foco: String   // (caso você queira usar depois, mas neste exemplo não usamos)
)
