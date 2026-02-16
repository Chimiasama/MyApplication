package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.Pericia
import com.example.swadebuilder.model.EspecializacoesDto

/**
 * Fase 2 (início): regra de domínio extraída do ViewModel.
 *
 * Garante especialização padrão para perícias visíveis quando
 * especializações de perícia estão habilitadas.
 */
class EnsureDefaultSpecializationsUseCase {

    fun execute(
        usarEspecializacoesDePericia: Boolean,
        pericias: List<Pericia>,
        rawTotalProvider: (Pericia) -> Int,
        atual: Map<String, EspecializacoesDto>
    ): Map<String, EspecializacoesDto> {
        if (!usarEspecializacoesDePericia) return atual

        val out = atual.toMutableMap()

        pericias.forEach { per ->
            val raw = rawTotalProvider(per)
            val visible = raw > 0 || per.basica

            if (visible) {
                val spec = out[per.nome]
                if (spec?.principal == null) {
                    val currentList = spec?.lista ?: emptyList()
                    out[per.nome] = EspecializacoesDto(
                        principal = "Especialização 1",
                        lista = currentList
                    )
                }
            }
        }

        return out
    }
}
