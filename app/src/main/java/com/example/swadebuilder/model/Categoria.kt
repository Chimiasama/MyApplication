package com.example.swadebuilder.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Enum que representa as categorias de Vantagem.
 * Deve corresponder exatamente aos valores que aparecem no JSON em "categoria".
 */
@Serializable
enum class Categoria {
    @SerialName("ANTECEDENTE")   ANTECEDENTE,
    @SerialName("COMBATE")       COMBATE,
    @SerialName("LIDERANCA")     LIDERANCA,
    @SerialName("PODER")         PODER,
    @SerialName("PROFISSIONAL")  PROFISSIONAL,
    @SerialName("SOCIAIS")       SOCIAIS,
    @SerialName("ESTRANHAS")     ESTRANHAS,
    @SerialName("RESSUSCITADO")  RESSUSCITADO,
    @SerialName("LENDARIAS")     LENDARIAS,
    @SerialName("SUPER")         SUPER,
    @SerialName("MONSTRUOSAS")   MONSTRUOSAS,
    @SerialName("CHI")           CHI,
    @SerialName("CLASSE")        CLASSE,
    @SerialName("PRESTIGIO")     PRESTIGIO
}
