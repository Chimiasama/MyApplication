package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.GameDataSnapshot

class ValidateGameDataSnapshotIntegrityUseCase {

    data class Result(
        val ok: Boolean,
        val issues: List<String>
    )

    fun execute(snapshot: GameDataSnapshot): Result {
        val issues = mutableListOf<String>()

        fun checkDuplicates(kind: String, ids: List<String>) {
            val duplicates = ids.groupingBy { it.trim().lowercase() }
                .eachCount()
                .filterValues { it > 1 }
                .keys
            if (duplicates.isNotEmpty()) {
                issues += "$kind com IDs duplicados: ${duplicates.sorted().joinToString(", ")}" 
            }
        }

        checkDuplicates("Vantagens", snapshot.listaVantagens.map { it.id })
        checkDuplicates("Poderes", snapshot.listaPoderes.map { it.id })
        checkDuplicates("Perícias", snapshot.listaPericias.map { it.nome })

        if (snapshot.mapaPericias.isEmpty() && snapshot.listaPericias.isNotEmpty()) {
            issues += "mapaPericias vazio com listaPericias não vazia"
        }

        return Result(ok = issues.isEmpty(), issues = issues)
    }
}
