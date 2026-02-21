package com.example.swadebuilder.model

enum class EquipSuperType(val label: String, val order: Int) {
    ARMAS("Armas", 1),
    ARMADURAS("Armaduras e Proteção", 2),
    VEICULOS("Veículos", 3),
    CIBERNETICO("Cibernéticos e Robótica", 4),
    GERAL("Equipamentos e Itens", 5),
    MECHA("Mechas", 6),
    ARMADURA_ENERGIZADA("Armaduras Energizadas", 7)
}

data class VantFilter(
    val origens: Set<String> = emptySet(),
    val estagios: Set<String> = emptySet(),
    val atributos: Set<String> = emptySet(),
    val pericias: Set<String> = emptySet()
) {
    fun isEmpty() =
        origens.isEmpty() && estagios.isEmpty() && atributos.isEmpty() && pericias.isEmpty()

    fun totalSelections() =
        origens.size + estagios.size + atributos.size + pericias.size
}

data class EquipFilter(
    val somenteAcessiveis: Boolean = false,
    val origens: Set<String> = emptySet(),
    val superTipos: Set<EquipSuperType> = emptySet(),
    val subSections: Set<String> = emptySet()
) {
    fun totalSelections() =
        (if (somenteAcessiveis) 1 else 0) +
                origens.size + superTipos.size + subSections.size

    fun isEmpty() = totalSelections() == 0
}
