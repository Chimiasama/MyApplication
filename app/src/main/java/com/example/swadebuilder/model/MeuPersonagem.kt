package com.example.swadebuilder.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class MeuPersonagem(
    val nome: String,
    val atributos: Map<String, Int>,
    val pericias: Map<String, Int>,
    val ancestralidade: String,
    val signoAdgSelecionado: String? = null,
    val descendenteElementalSelecionado: String? = null,
    val pacoteCulturalFantasiaSelecionado: String? = null,
    val povoDoMarOpcao: String? = null,
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
    val requisicao: Int = 0,
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
    val superNivelCampanha: Int? = null,

    // Limites & favorecido
    val limitePorPoderPadrao: Int = 0,
    val limiteFavorecido: Int = 0,
    val poderFavoritoId: String? = null,

    // Ledger de efeitos de poder (somente supers)
    val superInvestments: List<SuperInvestment> = emptyList(),
    @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
    @JsonNames("bonusPararFromPower")
    val bonusApararFromPower: Int = 0,
    val bonusResFromPower: Int = 0,
    val armorFromPower: Int = 0,
    val bonusMovimentacaoFromPower: Int = 0,
    val vantagensDePoder: Set<String> = emptySet(),         // ids de vantagens “compradas” por super
    val gastosPorPoder: Map<String, Int> = emptyMap(),      // PoderId -> custo gasto (para auditoria)

    // PP bônus extras
    val bonusPoderExtra: Int = 0,

    // Para conferência de teto de mitigação definido pela campanha:
    val limiteDePoderDaCampanha: Int = Int.MAX_VALUE,

    // ===== ANOTAÇÕES LIVRES PARA RESUMO / FICHA =====
    val anotacoes: String = "",

    val soldadoCargaAtivo: Boolean = true,
    val modoOficialAtivo: Boolean = false,
    val compendioArteDaGuerraAtivo: Boolean = false,
    val compendioPathfinderAtivo: Boolean = false,

    // ===== NOVOS CAMPOS (PDF EXTENDIDO) =====
    val coracaoCrystalSelecionado: CrystalHeart? = null,
    val dominio: Int? = null,
    val compendioDeadlandsAtivo: Boolean = false,
    val compendioHorrorAtivo: Boolean = false,
    val compendioFantasiaAtivo: Boolean = false,
    val compendioSciFiAtivo: Boolean = false,
    val compendioWiseguysAtivo: Boolean = false,
    val compendioCidadeSolVaporAtivo: Boolean = false,
    val notasPericia: Map<String, String> = emptyMap(), // PROMPT 5

    // ENGINE
    val tamanho: Int = 0,
    val movimentacao: Int = 0,
    val resistencia: Int = 0,
    val appTheme: String = "DEFAULT",
    val portraitFileName: String? = null,
    val regraFamaAtiva: Boolean = false,
    val fama: Int = 0,
    val usaRiqueza: Boolean = false,
    val usaRequisicao: Boolean = false,
    val modoProgressaoAtivo: Boolean = false,
    val complicacoesTipos: Map<String, String> = emptyMap(),
    val mechasSelecionados: List<MechaItem> = emptyList(),
    val ciberneticosInstalados: List<CiberneticoItem> = emptyList(),

    // Escolhas de Trópico/variante racial que tinham efeito mecânico (via
    // ModifierEngine/atributoBaseRacial/periciaStartRawInternal) mas nunca
    // chegavam à ficha impressa/resumo por não existirem aqui.
    val samuraiPosturasSelecionadas: List<String> = emptyList(),
    val dominioClerigoSelecionado: String? = null,
    val dominioClerigoPathfinderSelecionado: String? = null,
    val scifiVariant: String? = null
)
