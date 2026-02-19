package com.example.swadebuilder.model

data class Estagio(
    val nome: String,
    val minProgress: Int,
    val maxProgress: Int
)

val listaDeEstagios = listOf(
    Estagio("Novato", 0, 3),
    Estagio("Experiente", 4, 7),
    Estagio("Veterano", 8, 11),
    Estagio("Heroico", 12, 15),
    Estagio("Lendário", 16, Int.MAX_VALUE)
)

val nivelParaEstagio = mapOf(
    "N" to listaDeEstagios.first { it.nome == "Novato" },
    "E" to listaDeEstagios.first { it.nome == "Experiente" },
    "V" to listaDeEstagios.first { it.nome == "Veterano" },
    "H" to listaDeEstagios.first { it.nome == "Heroico" },
    "L" to listaDeEstagios.first { it.nome == "Lendário" }
)

const val TOTAL_PROGRESS_LIMIT = 20
val dynamicStageCaps = listaDeEstagios.mapIndexed { idx, st ->
    if (idx < listaDeEstagios.lastIndex) {
        if (idx == 0) {
            // Novato cobre o XP 0 inicial + 3 avanços gastáveis (N1..N3).
            (st.maxProgress - st.minProgress).coerceAtLeast(0)
        } else {
            // Demais estágios têm 4 avanços gastáveis cada.
            (st.maxProgress - st.minProgress + 1).coerceAtLeast(0)
        }
    } else {
        // Lendário ocupa o restante dos slots disponíveis da trilha total.
        val capsUsados = listaDeEstagios.dropLast(1).mapIndexed { stageIdx, stage ->
            if (stageIdx == 0) {
                (stage.maxProgress - stage.minProgress).coerceAtLeast(0)
            } else {
                (stage.maxProgress - stage.minProgress + 1).coerceAtLeast(0)
            }
        }.sum()
        (TOTAL_PROGRESS_LIMIT - capsUsados).coerceAtLeast(0)
    }
}
