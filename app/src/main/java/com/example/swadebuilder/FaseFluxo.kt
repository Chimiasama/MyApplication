package com.example.swadebuilder

/**
 * Fases lineares do fluxo de construção:
 *
 * - BASE       → criação normal (atributos, perícias, vantagens, etc.)
 * - SUPERS     → fase de superpoderes (campanha supers)
 * - PROGRESSOS → ficha já em jogo, mexe só via XP
 */
enum class FaseFluxo {
    BASE,
    SUPERS,
    PROGRESSOS
}

/**
 * Fase atual do fluxo, derivada do próprio estado.
 *
 * Regras:
 * - Se já existe progresso > 0 → estamos em PROGRESSOS.
 * - Senão, se é campanha supers e o nível de supers está definido → SUPERS.
 * - Caso contrário → BASE.
 */
val CriadorState.faseFluxo: FaseFluxo
    get() = when {
        // 3) PROGRESSOS sempre domina, mas em modo supers só depois de gastar todos os superpontos
        progresso > 0 && !modoSupersComSaldoDeSupers -> FaseFluxo.PROGRESSOS

        // 2) Campanha supers, com nível já definido e pontos calculados
        modoSupers &&
                superNivelCampanha != null &&
                superPontosTotais > 0 ->
            FaseFluxo.SUPERS

        // 1) Default: criação normal
        else -> FaseFluxo.BASE
    }

private val CriadorState.modoSupersComSaldoDeSupers: Boolean
    get() = modoSupers && superPontosTotais > 0 && superPontosDisponiveis > 0

/**
 * Criação básica congelada?
 *
 * Usado em Atributos / Perícias / Complicações — coisas que
 * não são mexidas nem em Supers nem em Progressos via UI normal.
 */
val CriadorState.criacaoBasicaCongelada: Boolean
    get() = when (faseFluxo) {
        FaseFluxo.BASE       -> false
        FaseFluxo.SUPERS     -> true   // fase de supers congela a base
        FaseFluxo.PROGRESSOS -> true   // em progresso tudo da base é só leitura
    }

/**
 * Versão para telas que podem ser mexidas durante um gasto de XP.
 *
 * Usada em Vantagens e Poderes (Antecedente Arcano).
 *
 * - BASE       → livre
 * - SUPERS     → congelado
 * - PROGRESSOS → só libera se emProgresso == true
 */
val CriadorState.criacaoBasicaCongeladaComXp: Boolean
    get() = when (faseFluxo) {
        FaseFluxo.BASE       -> false
        FaseFluxo.SUPERS     -> true
        FaseFluxo.PROGRESSOS -> !emProgresso
    }
