package com.example.myapplication.util

import com.example.myapplication.CriadorState
import com.example.myapplication.model.Vantagem

/**
 * Usa a mesma regra de elegibilidade já consolidada em CriadorState.podeSelecionar.
 * Isso cobre: estágio, nível de campanha (progresso), atributos/perícias mínimas,
 * opcionais, exige CS, incompatibilidades, maxSelections, choice duplicado e PP por estágio.
 */
fun Vantagem.requisitosAtendidos(state: CriadorState): Boolean =
    state.podeSelecionar(this)

/**
 * Mantém uma checagem explícita de “limite atingido” para casos onde
 * a UI queira exibir o motivo (ex.: já comprou o máximo).
 * Obs.: `podeSelecionar` já cobre esses limites; isto é apenas útil para UI.
 */
fun Vantagem.jaAtingiuLimite(state: CriadorState): Boolean {
    // Limite especial: Pontos de Poder (1 por estágio já alcançado)
    if (nome.contains("Pontos de Poder", ignoreCase = true)) {
        val totalCompras = state.comprasPpPorEstagio.values.sum()
        val limite = state.maxComprasPpAteAgora()
        if (totalCompras >= limite) return true
    }
    // Limite genérico por vantagem
    if (maxSelections > 0) {
        val ja = state.vantagensSelecionadas.count { it.nome.equals(nome, ignoreCase = true) }
        if (ja >= maxSelections) return true
    }
    // Limite por escolha idêntica (quando aplicável)
    if (requiresChoice && choice != null) {
        val repetida = state.vantagensSelecionadas.any { it.nome == nome && it.choice == choice }
        if (repetida) return true
    }
    return false
}
