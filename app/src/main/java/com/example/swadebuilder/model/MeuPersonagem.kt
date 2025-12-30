package com.example.swadebuilder.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class MeuPersonagem(
    val nome: String,
    val atributos: Map<String, Int>,
    val pericias: Map<String, Int>,
    val ancestralidade: String,
    val celestialAAMilagresDesabilitado: Boolean,
    val tropoSelecionadoId: String? = null,

    // Vantagens e Complicações já persistidas por ID:
    val vantagens: List<String>,
    val complicacoes: List<String>,
    val transtornos: List<String> = emptyList(), // PROMPT 3
    val desvantagensRaciais: List<String>,

    // Equipamentos com dados para impressão:
    val equipamentos: List<EquipamentoItem>,

    // Poderes arcanos, como antes:
    val poderes: Map<String, List<String>>,
    val manifestacoesPoderes: Map<String, String> = emptyMap(),

    // Dinheiro e PV restantes (vantagens):
    val dinheiro: Int,
    val dadoRiqueza: Int? = null,
    val pontosRestantes: Int,

    val naturalArmorFromRace: Int = 0,
    val armorBase: Int = 0,
    val advantageChoices: Map<String, List<String>> = emptyMap(),
    val vantagensRaciais: List<String> = emptyList(),
    val tecnicasIniciaisTropo: Int = 0,
    val reservaChi: Int? = null,

    // ===== NOVOS CAMPOS (SUPERS) =====
    val modoSupers: Boolean = false,
    val modoMonstroAtivo: Boolean = false,
    val tipoMonstroSelecionado: String? = null,
    val superPontosTotais: Int = 0,
    val superPontosDisponiveis: Int = 0,

    // Limites & favorecido
    val limitePorPoderPadrao: Int = 0,
    val limiteFavorecido: Int = 0,
    val poderFavoritoId: String? = null,

    // Ledger de efeitos de poder (somente supers)
    val superInvestments: List<SuperInvestment> = emptyList(),
    @JsonNames("bonusPararFromPower")
    val bonusApararFromPower: Int = 0,
    val bonusResFromPower: Int = 0,
    val armorFromPower: Int = 0,
    val bonusMovimentacaoFromPower: Int = 0,
    val vantagensDePoder: Set<String> = emptySet(),         // ids de vantagens “compradas” por super
    val gastosPorPoder: Map<String, Int> = emptyMap(),      // PoderId -> custo gasto (para auditoria)

    // Para conferência de teto de mitigação definido pela campanha:
    val limiteDePoderDaCampanha: Int = Int.MAX_VALUE,

    // ===== ANOTAÇÕES LIVRES PARA RESUMO / FICHA =====
    val anotacoes: String = "",

    val soldadoCargaAtivo: Boolean = true,
    val modoOficialAtivo: Boolean = false,
    val compendioArteDaGuerraAtivo: Boolean = false,
    val heroisSemArmadura: Boolean = false,

    // ===== NOVOS CAMPOS (PDF EXTENDIDO) =====
    val coracaoCrystalSelecionado: CrystalHeart? = null,
    val dominio: Int? = null,
    val compendioDeadlandsAtivo: Boolean = false,
    val compendioHorrorAtivo: Boolean = false,
    val notasPericia: Map<String, String> = emptyMap(), // PROMPT 5

    // ENGINE
    val tamanho: Int = 0,
    val resistencia: Int = 0,
    val appTheme: String = "DEFAULT",
    val portraitFileName: String? = null,
    val regraFamaAtiva: Boolean = false,
    val fama: Int = 0
)
