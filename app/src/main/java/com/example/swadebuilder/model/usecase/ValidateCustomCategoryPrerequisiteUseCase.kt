package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.Vantagem

/**
 * Pré-requisito "genérico" por Categoria Customizada (ver model/CategoriaCustomizada.kt),
 * paralelo a [ValidatePrerequisiteUseCase] (que exige uma Vantagem específica por id).
 * Aqui o Mestre exige só "já ter uma Vantagem desta categoria", sem precisar saber o
 * id exato de qual — útil pra campanhas próprias com categorias tipo "Pacto Menor"/
 * "Pacto Maior" onde qualquer vantagem do pacto anterior libera a próxima.
 */
class ValidateCustomCategoryPrerequisiteUseCase {

    fun execute(vantagem: Vantagem, vantagensSelecionadas: List<Vantagem>): Boolean {
        val requeridas = vantagem.requisitos.categoriasCustomizadasRequeridas
        if (requeridas.isEmpty()) return true

        val categoriasPossuidas = vantagensSelecionadas.mapNotNull { it.categoriaCustomizadaId }.toSet()
        return requeridas.all { it in categoriasPossuidas }
    }
}
