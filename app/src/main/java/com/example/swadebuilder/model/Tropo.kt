package com.example.swadebuilder.model

import com.example.swadebuilder.EditionConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Tropo(
    val id: String,
    val nome: String,
    val categoria: String,
    val origem: String,
    @SerialName("tecnicas_iniciais")
    val tecnicasIniciais: Int = 0,
    val descricao: String,
    // Resumo genérico para a edição Lite (não reproduz o texto do livro original).
    val descricaoLite: String? = null,
    @SerialName("ganha_ao_comprar")
    val ganhaAoComprar: List<String> = emptyList(),
    @SerialName("pericias_gratuitas")
    val periciasGratuitas: Map<String, Int> = emptyMap(),
    // CAMADA NOVA (sistema de Tropo genérico, ver docs/auditoria_mecanica_racas_2026-08-31.md
    // rodada 43): mesmo formato "habilidades[] com category/traitId/targetRef" que
    // RacialModifier/MonstroTemplate já usam — Vantagem/Complicação/perícia/atributo
    // concedidos por um Tropo entram aqui, não em `ganhaAoComprar`/`periciasGratuitas`
    // (campos antigos, mantidos só pros 9 Tropos oficiais que ainda não foram migrados;
    // ver Fase 4). Um Tropo que só usa `habilidades` não precisa preencher os campos
    // antigos.
    val habilidades: List<RacialAbility> = emptyList()
) {
    fun exibido(): Tropo =
        if (!EditionConfig.isFullEdition && !descricaoLite.isNullOrBlank()) copy(descricao = descricaoLite) else this
}
