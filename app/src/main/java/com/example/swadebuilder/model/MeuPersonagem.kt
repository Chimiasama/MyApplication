package com.example.swadebuilder.model

import kotlinx.serialization.Serializable

@Serializable
data class MeuPersonagem(
    val nome: String,
    val atributos: Map<String, Int>,
    val pericias: Map<String, Int>,
    val ancestralidade: String,

    // Vantagens e Complicações já persistidas por ID:
    val vantagens: List<String>,
    val complicacoes: List<String>,

    // Equipamentos com dados para impressão:
    val equipamentos: List<EquipamentoItem>,

    // Poderes arcanos, como antes:
    val poderes: Map<String, List<String>>,

    // Dinheiro e PV restantes (vantagens):
    val dinheiro: Int,
    val pontosRestantes: Int,

    // ===== NOVOS CAMPOS (SUPERS) =====
    val modoSupers: Boolean = false,
    val superPontosTotais: Int = 0,
    val superPontosDisponiveis: Int = 0,

    // Limites & favorecido
    val limitePorPoderPadrao: Int = 0,
    val limiteFavorecido: Int = 0,
    val idPoderFavorecido: String? = null,

    // Ledger de efeitos de poder (somente supers)
    val superAtributoIncs: Map<String, Int> = emptyMap(),   // chave = atributo keyify (FORCA, AGILIDADE, etc.)
    val superPericiaIncs: Map<String, Int> = emptyMap(),    // chave = periciaId/pericia.keyify()
    val bonusPararFromPower: Int = 0,
    val bonusResFromPower: Int = 0,
    val armorFromPower: Int = 0,
    val bonusMovimentacaoFromPower: Int = 0,
    val vantagensDePoder: Set<String> = emptySet(),         // ids de vantagens “compradas” por super
    val gastosPorPoder: Map<String, Int> = emptyMap(),      // PoderId -> custo gasto (para auditoria)

    // Para conferência de teto de mitigação definido pela campanha:
    val limiteDePoderDaCampanha: Int = Int.MAX_VALUE,

    // ===== ANOTAÇÕES LIVRES PARA RESUMO / FICHA =====
    val anotacoes: String = ""
)